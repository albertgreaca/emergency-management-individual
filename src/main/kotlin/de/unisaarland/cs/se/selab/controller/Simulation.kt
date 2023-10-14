package de.unisaarland.cs.se.selab.controller

import de.unisaarland.cs.se.selab.logger.Logger
import de.unisaarland.cs.se.selab.model.AccidentEmergency
import de.unisaarland.cs.se.selab.model.CrimeEmergency
import de.unisaarland.cs.se.selab.model.Emergency
import de.unisaarland.cs.se.selab.model.Event
import de.unisaarland.cs.se.selab.model.FireEmergency
import de.unisaarland.cs.se.selab.model.MedicalEmergency
import de.unisaarland.cs.se.selab.model.SimulationData
import de.unisaarland.cs.se.selab.model.assets.AssetRequest
import de.unisaarland.cs.se.selab.model.map.TargetReached
import de.unisaarland.cs.se.selab.util.getOrThrow
import io.github.oshai.kotlinlogging.KotlinLogging

/**
 * Main class of the simulation.
 */
class Simulation(
    private val logger: Logger,
    private val simulationData: SimulationData,
    private val emergencies: Collection<Emergency>,
    events: Collection<Event>
) {
    private val eventHandler = EventHandler(events, simulationData)
    private val navigation = Navigation(simulationData)
    private val newEmergencies: List<Emergency>
        get() = emergencies.filter { it.tick == simulationData.tick }.sortedBy { it.id }

    /**
     * Map of base id to base controller.
     */
    private val baseController: Map<Int, BaseController<*>> =
        simulationData.bases.associate { it.id to BaseController(it, navigation) }

    /**
     * List of [EmergencyResponse]s of active emergencies sorted by severity and then by id.
     */
    private val activeEmergencies: MutableList<EmergencyResponse> = mutableListOf()
        get() {
            field.sortBy { it.emergency.id }
            field.sortByDescending { it.emergency.severity }
            return field
        }

    /**
     * Start the Simulation.
     */
    fun init() {
        logger.start()
        // Set all vehicles to their home
        simulationData.vehicles.forEach { it.currentRoute = TargetReached(it.home, simulationData.simulationMap) }
        loop()
        logger.end()
    }

    private fun loop() {
        while (simulationData.tick < simulationData.maxTicks && emergencies.any { !it.done }) {
            tick()
            simulationData.tick++
            if (simulationData.tick % shiftLength == 0) {
                simulationData.shift = simulationData.shift.getNext()
            }
        }
    }

    private fun tick() {
        logger.tick(simulationData.tick, simulationData.shift)
        emergencyPhase()
        planningPhase()
        updatePhase()
    }

    /**
     * Assign all new emergencies to the closest base.
     */
    private fun emergencyPhase() {
        // Pause the events of the addresses of the new emergencies
        newEmergencies.forEach { it.road.pauseEvent() }
        for (emergency in newEmergencies) {
            val closestBase = when (emergency) {
                is FireEmergency -> navigation.closestFireStation(emergency.road)
                is AccidentEmergency -> navigation.closestFireStation(emergency.road)
                is CrimeEmergency -> navigation.closestPoliceStation(emergency.road)
                is MedicalEmergency -> navigation.closestHospital(emergency.road)
            }.getOrThrow(IllegalStateException("No base found"))
            activeEmergencies.add(
                EmergencyResponse(
                    emergency,
                    baseController[closestBase.first.id] ?: BaseController(closestBase.first, navigation)
                )
            )
            logger.emergency(emergency.id, closestBase.first.id, closestBase.second)
        }
    }

    /**
     * Assign all assets to emergencies.
     * And handle asset requests.
     */
    private fun planningPhase() {
        val requests: MutableList<AssetRequest> = mutableListOf()
        for (emergency in activeEmergencies.iterator()) {
            requests.addAll(emergency.assignAssets(logger, simulationData))
        }
        requests.sortBy { it.id }
        while (requests.isNotEmpty()) {
            val request = requests.removeFirst()
            val newRequests =
                baseController[request.base.id]?.handleRequest(request, logger, simulationData).orEmpty()
            requests.addAll(newRequests)
        }
    }

    /**
     * Update all emergencies.
     * If an emergency is done, resume the event.
     * If an emergency is done, remove it from the active emergencies.
     * Finally, log the emergencies.
     */
    private fun handelEmergencies() {
        activeEmergencies.forEach {
            it.update(logger, simulationData, navigation)
        }
        activeEmergencies.groupBy { it.emergency.road }.forEach { (address, emergencies) ->
            if (emergencies.all { it.emergency.done }) {
                address.resumeEvent()
            }
        }
        activeEmergencies.removeIf { it.emergency.done }
        logger.logEmergencies()
    }

    /**
     * Calculate new routes for all driving vehicles.
     * If the route for a Vehicle changed reroute it.
     */
    private fun reRouteAssets() {
        val paths = simulationData.drivingVehicles.groupBy {
            Triple(it.location, it.target, it.vehicleHeight)
        }.mapValues { navigation.shortestRoute(it.key.first, it.key.second, it.value.first()) }
        simulationData.drivingVehicles.forEach {
            val potentialNewRoute = paths[Triple(it.location, it.target, it.vehicleHeight)]?.move(0)
                ?: error("Route should exist")
            if (potentialNewRoute != it.currentRoute.move(0)) {
                it.currentRoute = paths[Triple(it.location, it.target, it.vehicleHeight)] ?: error("Route should exist")
                logger.assetRerouted(it.id, it.currentRoute.path)
                KotlinLogging.logger("Asset Rerouting").info {
                    "In tick ${simulationData.tick} asset ${it.id} " +
                        "was rerouted new time to target ${it.target}: ${it.timeToTarget}"
                }
            }
        }
    }

    /**
     * Handel the update phase.
     * First update all vehicles.
     * Then update all emergencies.
     * Then update all activeEvents.
     * Then activate all new Events.
     * Finally, update the routes of driving vehicles if events ended or started.
     */
    private fun updatePhase() {
        for (vehicle in simulationData.vehicles) {
            val hasArrived = vehicle.update()
            if (hasArrived) {
                logger.assetArrival(vehicle.id, vehicle.location.distancesToNodes.entries.minBy { it.value }.key.id)
                if (vehicle.atHome) {
                    simulationData.bases.find { it.id == vehicle.baseID }?.returnVehicle(vehicle)
                }
            }
        }
        for (staff in simulationData.staff.sortedBy { it.id }) {
            if (simulationData.tick % shiftLength == shiftEnd) {
                staff.shiftLogger(logger, simulationData.shift)
                staff.updateShifts(simulationData.shift)
            }
        }
        handelEmergencies()
        val eventsEnded = eventHandler.update(logger)
        val eventActivated = eventHandler.activateEvents(logger)
        activeEmergencies.forEach { it.emergency.road.pauseEvent() }
        val recalculateRoutes = eventsEnded || eventActivated
        if (recalculateRoutes) {
            reRouteAssets()
        }
    }
    companion object {
        const val shiftLength = 10
        const val shiftEnd = 9
    }
}
