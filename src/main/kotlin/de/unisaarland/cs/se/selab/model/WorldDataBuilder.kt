package de.unisaarland.cs.se.selab.model

import de.unisaarland.cs.se.selab.model.assets.Base
import de.unisaarland.cs.se.selab.model.assets.FireStation
import de.unisaarland.cs.se.selab.model.assets.Hospital
import de.unisaarland.cs.se.selab.model.assets.PoliceStation
import de.unisaarland.cs.se.selab.model.assets.Staff
import de.unisaarland.cs.se.selab.model.assets.Vehicle
import de.unisaarland.cs.se.selab.model.graph.Graph
import de.unisaarland.cs.se.selab.model.map.Node
import de.unisaarland.cs.se.selab.model.map.Road

/**
 * Builder for the [SimulationData].
 */
class WorldDataBuilder {
    lateinit var simulationMap: Graph<Node, Road>
    lateinit var bases: List<Base<*>>
    lateinit var vehicles: List<Vehicle>
    lateinit var staff: List<Staff>
    var maxTicks: Int = 0
    val simulationData: SimulationData
        get() = SimulationData(
            simulationMap,
            bases.filterIsInstance<Hospital>(),
            bases.filterIsInstance<PoliceStation>(),
            bases.filterIsInstance<FireStation>(),
            vehicles,
            staff,
            maxTicks
        )
}
