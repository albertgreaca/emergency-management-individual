package de.unisaarland.cs.se.selab.model.graph

/**
 * A path through the [graph] defined by a list of [vertices].
 */
data class Path<VertexTy, EdgeTy>(
    val graph: Graph<VertexTy, EdgeTy>,
    val vertices: MutableList<VertexTy>,
    val length: Int
) {
    /**
     * Returns an iterable over the edges of this path.
     */
    fun walkEdges(): Iterable<EdgeTy> {
        return vertices.zipWithNext().mapNotNull { (s, t) -> graph.getEdgeOrNUll(s, t) }
    }

    /**
     * Returns the path as a string suitable for use with the logger.
     */
    override fun toString(): String {
        return vertices.joinToString("-", "[", "]") { it.toString() }
    }
}
