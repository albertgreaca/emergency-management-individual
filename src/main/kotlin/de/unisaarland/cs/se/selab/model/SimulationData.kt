package de.unisaarland.cs.se.selab.model

import de.unisaarland.cs.se.selab.model.assets.Base
import de.unisaarland.cs.se.selab.model.assets.FireStation
import de.unisaarland.cs.se.selab.model.assets.Hospital
import de.unisaarland.cs.se.selab.model.assets.PoliceStation
import de.unisaarland.cs.se.selab.model.assets.Vehicle
import de.unisaarland.cs.se.selab.model.graph.Graph
import de.unisaarland.cs.se.selab.model.map.Node
import de.unisaarland.cs.se.selab.model.map.Road

/**
 * The world data.
 */
data class SimulationData(
    val simulationMap: Graph<Node, Road>,
    val hospitals: List<Hospital>,
    val policeStations: List<PoliceStation>,
    val fireStations: List<FireStation>,
    val vehicles: List<Vehicle>,
    val maxTicks: Int,
    var tick: Int = 0,
) {
    private var nextRequestId: Int = 1

    /**
     * Returns all vehicles that are currently driving.
     */
    val drivingVehicles: List<Vehicle>
        get() = vehicles.filter { !it.atTarget }.sortedBy { it.id }

    val bases: List<Base<*>> = hospitals + policeStations + fireStations

    /**
     * get the next request id
     * @return the next request id
     */
    fun nextRequestID(): Int {
        val currentRequestId = nextRequestId
        nextRequestId++
        return currentRequestId
    }
}
