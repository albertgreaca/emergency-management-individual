package de.unisaarland.cs.se.selab.model.map

import de.unisaarland.cs.se.selab.model.graph.Path

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
                Path(path.graph, path.vertices.drop(1).toMutableList(), path.length - nextRoad.weight())
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
