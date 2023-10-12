package de.unisaarland.cs.se.selab.model.assets

import de.unisaarland.cs.se.selab.model.map.Node

/**
 * A fire station.
 */
data class FireStation(
    override val id: Int,
    override val location: Node,
    override var staffNumber: Int,
    val trucks: List<FireTruck>,
) : Base<FireTruck>(trucks)
