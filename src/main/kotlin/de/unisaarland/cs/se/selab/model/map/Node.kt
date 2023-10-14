package de.unisaarland.cs.se.selab.model.map

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

    override fun toString(): String {
        return id.toString()
    }
}
