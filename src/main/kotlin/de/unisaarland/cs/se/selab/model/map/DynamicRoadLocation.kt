package de.unisaarland.cs.se.selab.model.map

/**
 * A location on a road
 * @param road the road
 * @param position the position on the road relative to the source of the road
 * @param length the length of the road at creation
 * @param exit the exit of the road if the road is closed at one end
 */
data class DynamicRoadLocation(
    val road: Road,
    val position: Int,
    val length: Int = road.weight(),
    val exit: Node? = road.distancesToNodes.keys.find { road.closed(it) }
) : Location {

    constructor(road: Road, position: Boolean, length: Int = road.weight()) :
        this(road, if (position) 0 else road.weight(), length)

    override val distancesToNodes: Map<Node, Int>
        get() = mapOf(Pair(road.source, position), Pair(road.target, length - position))

    override val drivableDistancesToNodes: Map<Node, Int>
        get() = if (exit != null) {
            mapOf(Pair(exit, if (exit == road.source) position else length - position))
        } else {
            mapOf(Pair(road.source, position), Pair(road.target, length - position))
        }

    override fun equals(other: Any?): Boolean {
        return other is DynamicRoadLocation &&
            road == other.road &&
            position == other.position &&
            length == other.length
    }

    override fun hashCode(): Int {
        var result = road.hashCode()
        result = 31 * result + position
        result = 31 * result + length
        return result
    }
}
