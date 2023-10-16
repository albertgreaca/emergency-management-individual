package de.unisaarland.cs.se.selab.model.map

import de.unisaarland.cs.se.selab.model.graph.Graph
import de.unisaarland.cs.se.selab.model.graph.Path

/**
 * A class representing a route which has reached its target.
 */
class TargetReached(
    target: Location,
    graph: Graph<Node, Road>
) : Route(target, target, Path(graph, mutableListOf(), 0)) {

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
