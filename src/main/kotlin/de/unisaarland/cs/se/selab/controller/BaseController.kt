package de.unisaarland.cs.se.selab.controller

import de.unisaarland.cs.se.selab.logger.Logger
import de.unisaarland.cs.se.selab.model.SimulationData
import de.unisaarland.cs.se.selab.model.assets.AssetInquiry
import de.unisaarland.cs.se.selab.model.assets.AssetRequest
import de.unisaarland.cs.se.selab.model.assets.Base
import de.unisaarland.cs.se.selab.model.assets.SPEED
import de.unisaarland.cs.se.selab.model.assets.Vehicle
import de.unisaarland.cs.se.selab.model.assets.getNecessaryAssets
import de.unisaarland.cs.se.selab.util.ifSuccess
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.math.ceil
import kotlin.math.max

/**
 * class to handle assetRequests of a base.
 */
class BaseController<T : Vehicle>(
    val base: Base<T>,
    private val navigation: Navigation
) {
    /**
     * Assign assets to an emergency.
     * @param emergencyResponse The emergency to assign the assets to.
     * @param logger The logger to log to.
     * @param simulationData The world data.
     *
     * @return The requests for the remaining assets.
     */
    fun assignAssets(
        emergencyResponse: EmergencyResponse,
        logger: Logger,
        simulationData: SimulationData
    ): List<AssetRequest> {
        val assetInquiry =
            getNecessaryAssets(emergencyResponse.emergency, emergencyResponse.allocatedAssets(simulationData))
        val potentialVehicles = base.availableVehicles.filter { vehicle ->
            assetInquiry.canHelp(vehicle)
        }
        var sendRequests = true
        if (potentialVehicles.isNotEmpty()) {
            val newAssetInquiry = handleInquiry(emergencyResponse, logger, potentialVehicles, assetInquiry, false)
            if (assetInquiry == newAssetInquiry && emergencyResponse.allocatedAssets(simulationData).isEmpty()) {
                sendRequests = false
            }
        }
        val remainingAssetInquiry = reallocateAssets(emergencyResponse, simulationData, logger)
        if (
            potentialVehicles.isNotEmpty() &&
            (
                emergencyResponse.allocatedAssets(simulationData).isEmpty() ||
                    remainingAssetInquiry.isFulfilled
                )
        ) {
            return emptyList()
        }
        if (sendRequests) {
            val requests = generateRequests(remainingAssetInquiry, simulationData, emergencyResponse)
            requests.sortedBy { it.id }.forEach {
                logger.assetRequest(it.id, it.base.id, it.emergencyResponse.emergency.id)
            }
            return requests
        }
        return emptyList()
    }

    /**
     * Generate requests for the remaining assets.
     * @param assetInquiry The inquiry for the remaining assets.
     * @param simulationData The world data.
     * @param emergency The emergency to generate the requests for.
     * @param checkedBases The bases that have already been checked.
     *
     * @return The requests for the remaining assets.
     */
    private fun generateRequests(
        assetInquiry: AssetInquiry,
        simulationData: SimulationData,
        emergency: EmergencyResponse,
        checkedBases: List<Int> = listOf(base.id)
    ): List<AssetRequest> {
        val remainingInquiries = assetInquiry.split()
        val requests = mutableListOf<Triple<Base<*>, EmergencyResponse, AssetInquiry>>()
        if (!remainingInquiries.ambulanceInquiry.isFulfilled) {
            navigation.closestHospital(base.location, checkedBases + base.id, true).ifSuccess {
                requests.add(Triple(it.first, emergency, remainingInquiries.ambulanceInquiry))
            }
        }
        if (!remainingInquiries.fireInquiry.isFulfilled) {
            navigation.closestFireStation(base.location, checkedBases + base.id, true).ifSuccess {
                requests.add(Triple(it.first, emergency, remainingInquiries.fireInquiry))
            }
        }
        if (!remainingInquiries.policeInquiry.isFulfilled) {
            navigation.closestPoliceStation(base.location, checkedBases + base.id, true).ifSuccess {
                requests.add(Triple(it.first, emergency, remainingInquiries.policeInquiry))
            }
        }
        return requests.sortedBy { it.first.id }.map { requestTriple ->
            AssetRequest(
                simulationData.nextRequestID(),
                requestTriple.first,
                requestTriple.second,
                requestTriple.third,
                checkedBases + listOf(base.id)
            )
        }
    }

    /**
     * Handle an inquiry for the base.
     * @param emergencyResponse The emergency to handle the inquiry for.
     * @param logger The logger to log to.
     * @param vehicles The vehicles that can be used.
     * @param assetInquiry The inquiry to handle.
     */
    private fun handleInquiry(
        emergencyResponse: EmergencyResponse,
        logger: Logger,
        vehicles: List<T>,
        assetInquiry2: AssetInquiry,
        request: Boolean
    ): AssetInquiry {
        var potentialVehicles = vehicles
        var assetInquiry = assetInquiry2
        val potentialRoutes = routes(potentialVehicles, emergencyResponse)[base.location].orEmpty()
        potentialVehicles = potentialVehicles.filter {
            ceil(
                (
                    potentialRoutes[it.vehicleHeight]?.length
                        ?: error("A route from the base to the emergency needs to exist")
                    ) / SPEED.toDouble()
            ) <= emergencyResponse.maxTravelTime
        }
        val allocatedVehicles: MutableList<T> = mutableListOf()
        for (vehicle in potentialVehicles.sortedBy { it.id }) {
            val difference = emergencyResponse.maxTravelTime - ceil(
                (potentialRoutes[vehicle.vehicleHeight]?.length ?: error("route needs to exist")) /
                    SPEED.toDouble()
            ).toInt()
            if (
                assetInquiry.isFulfillable(vehicle) &&
                assetInquiry.canHelp(vehicle) &&
                base.canManSimulationBool(
                    vehicle,
                    difference,
                    request
                )
            ) {
                vehicle.currentEmergency = emergencyResponse.emergency
                vehicle.currentRoute = potentialRoutes[vehicle.vehicleHeight]?.move(0)
                    ?: error("A route from the base to the emergency needs to exist")
                vehicle.location = vehicle.currentRoute.start
                vehicle.atTarget = false
                val extra = base.allocateStaff(emergencyResponse, logger, vehicle, difference, request)
                vehicle.manning = Math.max(1, 1 + extra)
                base.staffNumber -= vehicle.staffCapacity
                logger.allocation(vehicle.id, emergencyResponse.emergency.id, max(1, extra + vehicle.timeToTarget))
                allocatedVehicles.add(vehicle)
                assetInquiry = assetInquiry.remainingAssets(listOf(vehicle))
            }
        }
        return assetInquiry
    }

    /**
     * Reallocate assets for an emergency.
     * @param emergencyResponse The emergency to reallocate assets for.
     * @param simulationData The world data.
     * @param logger The logger to log to.
     *
     * @return The remaining required assets.
     */
    private fun reallocateAssets(
        emergencyResponse: EmergencyResponse,
        simulationData: SimulationData,
        logger: Logger
    ): AssetInquiry {
        var assetInquiry =
            getNecessaryAssets(emergencyResponse.emergency, emergencyResponse.allocatedAssets(simulationData))
        var potentialVehicles = base.reAllocatableVehicles(emergencyResponse.emergency).filter { vehicle ->
            assetInquiry.canHelp(vehicle)
        }
        val potentialRoutes = routes(potentialVehicles, emergencyResponse)
        potentialVehicles = potentialVehicles.filter {
            ceil(
                (
                    potentialRoutes[it.location]?.get(it.vehicleHeight)?.length
                        ?: error("A route needs to exist")
                    ) / SPEED.toDouble()
            ) - 1 <= emergencyResponse.maxTravelTime
        }
        val reallocatedVehicles = mutableListOf<Vehicle>()
        for (vehicle in potentialVehicles.sortedBy { it.id }) {
            if (assetInquiry.isFulfillable(vehicle) && assetInquiry.canHelp(vehicle)) {
                vehicle.currentEmergency = emergencyResponse.emergency
                vehicle.currentRoute = potentialRoutes[vehicle.location]?.get(vehicle.vehicleHeight)
                    ?: error("A route needs to exist")
                reallocatedVehicles.add(vehicle)
                logger.reallocation(vehicle.id, emergencyResponse.emergency.id)
                KotlinLogging.logger("AssetReallocation").info {
                    "Asset ${vehicle.id} was reallocated to emergency ${emergencyResponse.emergency.id}" +
                        " new time to target ${vehicle.timeToTarget}"
                }
                assetInquiry = assetInquiry.remainingAssets(listOf(vehicle))
            }
        }
        return assetInquiry
    }

    /**
     * Get the routes for all vehicles to the emergency.
     * @param potentialVehicles The vehicles that can be used.
     * @param emergencyResponse The emergency to get the routes for.
     *
     * @return The routes for all vehicles to the emergency,
     * as a map from the vehicle location and vehicle height to the routes.
     */
    private fun routes(
        potentialVehicles: List<T>,
        emergencyResponse: EmergencyResponse
    ) = potentialVehicles.groupBy { it.location }.mapValues {
        it.value.groupBy { vehicle ->
            vehicle.vehicleHeight
        }
            .mapValues { heightVehicleEntry ->
                navigation.shortestRoute(
                    heightVehicleEntry.value.first().location,
                    emergencyResponse.emergency.road,
                    heightVehicleEntry.value.first()
                )
            }
    }

    /**
     * Handle the requests form other bases.
     * @param request The request to handle.
     * @param logger The logger to log to.
     * @param simulationData The world data.
     *
     * @return requests for remaining assets.
     */
    fun handleRequest(request: AssetRequest, logger: Logger, simulationData: SimulationData): List<AssetRequest> {
        val remainingInquiry = handleInquiry(
            request.emergencyResponse,
            logger,
            base.availableVehicles.filter { vehicle -> request.assetInquiry.canHelp(vehicle) },
            request.assetInquiry,
            true
        )
        if (remainingInquiry.isFulfilled) {
            return emptyList()
        } else {
            val requests =
                generateRequests(remainingInquiry, simulationData, request.emergencyResponse, request.checkedBases)
            if (requests.isEmpty()) {
                logger.requestFailed(request.emergencyResponse.emergency.id)
                return emptyList()
            }
            requests.sortedBy { it.id }.forEach {
                logger.assetRequest(it.id, it.base.id, it.emergencyResponse.emergency.id)
            }
            return requests
        }
    }
}
