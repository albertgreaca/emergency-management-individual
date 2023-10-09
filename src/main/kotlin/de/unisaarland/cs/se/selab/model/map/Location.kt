package de.unisaarland.cs.se.selab.model.map

import de.unisaarland.cs.se.selab.model.NoEvent
import de.unisaarland.cs.se.selab.model.RoadEvent
import de.unisaarland.cs.se.selab.model.assets.Vehicle
import de.unisaarland.cs.se.selab.model.graph.Graph
import de.unisaarland.cs.se.selab.model.graph.Path
import de.unisaarland.cs.se.selab.parser.PrimaryStreetType

/**
 * Interface for locations on the map
 */
interface Location {
    val distancesToNodes: Map<Node, Int>
    val drivableDistancesToNodes: Map<Node, Int>
        get() = distancesToNodes
}

/**
 * A vertex in the graph.
 */
@JvmInline
value class Node(val id: Int) : Location, Comparable<Node> {
    override val distancesToNodes: Map<Node, Int>
        get() = mapOf(Pair(this, 0))

    override fun compareTo(other: Node): Int {
        return this.id.compareTo(other.id)
    }
}

/**
 * A road between two nodes.
 */
class Road(
    val villageName: String,
    val name: String,
    val length: Int,
    val primaryType: PrimaryStreetType,
    val heightLimit: Int,
    val source: Node,
    val target: Node
) : Location {

    val events: MutableList<RoadEvent> = mutableListOf()
        get() {
            field.sortBy { it.id }
            return field
        }

    var activeEvent: RoadEvent = NoEvent()

    /**
     * Whether the road is closed at the given entry.
     */
    fun closed(entry: Node): Boolean {
        return activeEvent.closed(entry)
    }

    /**
     * The weight of the road.
     */
    fun weight(): Int {
        return activeEvent.weight(this)
    }

    /**
     * Whether the road can be used by the given vehicle.
     */
    fun canBeUsedBy(vehicle: Vehicle): Boolean {
        return vehicle.vehicleHeight <= heightLimit
    }

    /**
     * Adds an event to the road.
     */
    fun addEvent(event: RoadEvent) {
        if (activeEvent is NoEvent) {
            activeEvent = event
        } else {
            events.add(event)
        }
    }

    /**
     * Pauses the activeEvent.
     */
    fun pauseEvent() {
        activeEvent.pauseEvent()
    }

    /**
     * Resumes the activeEvent.
     */
    fun resumeEvent() {
        activeEvent.resumeEvent()
    }

    override val distancesToNodes: Map<Node, Int>
        get() = mapOf(Pair(source, 0), Pair(target, 0))

    override fun toString(): String {
        return "$villageName $name source : $source target: $target"
    }
}

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

/**
 * A route which represents a path from a start location to a target location.
 * @param start the start location
 * @param target the target location
 * @param path the path from the start location to the target location
 */
open class Route(
    val start: Location,
    val target: Location,
    val path: Path<Node, Road>,
) {
    open val length: Int = if (path.vertices.isEmpty()) {
        0
    } else {
        path.length + (start.distancesToNodes[path.vertices.first()] ?: 0) +
            (target.distancesToNodes[path.vertices.last()] ?: 0)
    }

    /**
     * Moves along the route by the given distance.
     * @param distance the distance to move
     * @return the new route after moving
     */
    open fun move(distance: Int): Route {
        val distanceToNextNode = start.distancesToNodes[path.vertices.first()] ?: error(
            "Path does not start" +
                " at an vertex touching start"
        )
        if (distanceToNextNode <= distance) {
            if (path.vertices.size == 1) {
                return TargetReached(target, path.graph)
            }
            val nextNode = path.vertices.first()
            val nextRoad = path.walkEdges().first()
            return Route(
                DynamicRoadLocation(nextRoad, nextRoad.source == nextNode, nextRoad.weight()),
                target,
                Path(path.graph, path.vertices.drop(1), path.length - nextRoad.weight())
            ).move(distance - distanceToNextNode)
        } else if (start is DynamicRoadLocation) {
            val newPosition = if (start.road.source == path.vertices.first()) {
                start.position - distance
            } else {
                start.position + distance
            }
            return Route(DynamicRoadLocation(start.road, newPosition, start.length), target, path)
        }
        error("")
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Route

        if (start != other.start) return false
        if (path != other.path) return false
        return length == other.length
    }

    override fun hashCode(): Int {
        var result = start.hashCode()
        result = 31 * result + path.hashCode()
        result = 31 * result + length
        return result
    }

    override fun toString(): String {
        return path.toString()
    }
}

/**
 * A class representing a route which has reached its target.
 */
class TargetReached(target: Location, graph: Graph<Node, Road>) : Route(target, target, Path(graph, emptyList(), 0)) {

    override val length: Int = 0
    override fun move(distance: Int): Route {
        return this
    }

    override fun equals(other: Any?): Boolean {
        return other is TargetReached && target == other.target
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + length
        return result
    }
}
