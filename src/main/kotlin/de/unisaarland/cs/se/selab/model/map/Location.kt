package de.unisaarland.cs.se.selab.model.map

/**
 * Interface for locations on the map
 */
interface Location {
    val distancesToNodes: Map<Node, Int>
    val drivableDistancesToNodes: Map<Node, Int>
        get() = distancesToNodes
}
