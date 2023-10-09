package de.unisaarland.cs.se.selab.controller

import de.unisaarland.cs.se.selab.logger.Logger
import de.unisaarland.cs.se.selab.model.SimulationData
import de.unisaarland.cs.se.selab.model.assets.AssetRequest
import de.unisaarland.cs.se.selab.model.assets.Vehicle
import de.unisaarland.cs.se.selab.model.assets.getNecessaryAssets
import de.unisaarland.cs.se.selab.model.Emergency

/**
 * A response to an emergency.
 */
class EmergencyResponse(val emergency: Emergency, private val handlingBase: BaseController<*>) {

    /**
     * Returns the assets allocated to the emergency.
     */
    fun allocatedAssets(simulationData: SimulationData): List<Vehicle> {
        return simulationData.vehicles.filter { it.currentEmergency == emergency }
    }

    val maxTravelTime: Int
        get() = emergency.maxDuration - emergency.handlingTime

    /**
     * Allocate the assets of the handling base to the emergency call and return requests for the remaining assets.
     * @param logger The logger to log to.
     * @param simulationData The world data.
     * @return The requests for the remaining assets.
     */
    fun assignAssets(logger: Logger, simulationData: SimulationData): Collection<AssetRequest> {
        return handlingBase.assignAssets(this, logger, simulationData)
    }

    private fun sendAssetsHome(simulationData: SimulationData, navigation: Navigation) {
        val paths = allocatedAssets(simulationData).groupBy {
            it.location
        }.mapValues { locationGroups ->
            locationGroups.value.groupBy {
                it.home
            }.mapValues { baseGroups ->
                baseGroups.value.groupBy {
                    it.vehicleHeight
                }.mapValues { navigation.shortestRoute(locationGroups.key, baseGroups.key, it.value.first()) }
            }
        }
        allocatedAssets(simulationData).forEach {
            it.currentRoute = paths[it.location]?.get(it.home)?.get(it.vehicleHeight)
                ?: error("The strictly connectedness is a lie")
            it.currentEmergency = null
            it.atTarget = false
        }
    }

    /**
     * Update the emergency.
     * @param logger The logger to log to.
     * @param simulationData The world data.
     */
    fun update(logger: Logger, simulationData: SimulationData, navigation: Navigation) {
        if (emergency.handlingStarted) {
            if (--emergency.handlingTime == 0) {
                logger.emergencyResolved(emergency.id)
                getNecessaryAssets(emergency).fulfill(allocatedAssets(simulationData).sortedBy { it.id })
                sendAssetsHome(simulationData, navigation)
                emergency.done = true
            }
        } else {
            if (getNecessaryAssets(emergency, allocatedAssets(simulationData)).isFulfilled &&
                allocatedAssets(simulationData).all {
                    it.atTarget
                }
            ) {
                emergency.handlingStarted = true
                logger.emergencyHandlingStart(emergency.id)
            } else if (emergency.maxDuration <= emergency.handlingTime) {
                logger.emergencyFailed(emergency.id)
                sendAssetsHome(simulationData, navigation)
                emergency.done = true
            }
        }
        emergency.maxDuration--
    }
}
