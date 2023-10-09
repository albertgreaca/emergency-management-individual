package de.unisaarland.cs.se.selab.model.graph

import de.unisaarland.cs.se.selab.util.Assertion.assertNotNull

/**
 * A simple adjacency-list-based graph implementation.
 */
class GraphImpl<NodeTy, EdgeTy> : Graph<NodeTy, EdgeTy> {
    private val succ: MutableMap<NodeTy, MutableMap<NodeTy, EdgeTy>> = mutableMapOf()
    private val pred: MutableMap<NodeTy, MutableMap<NodeTy, EdgeTy>> = mutableMapOf()

    override fun addVertex(vertex: NodeTy) {
        succ.putIfAbsent(vertex, mutableMapOf())
        pred.putIfAbsent(vertex, mutableMapOf())
    }

    override fun addEdge(source: NodeTy, target: NodeTy, edge: EdgeTy) {
        addVertex(source)
        addVertex(target)

        assertNotNull(succ[source])[target] = edge
        assertNotNull(pred[target])[source] = edge
    }

    override fun vertices(): Iterable<NodeTy> = succ.keys

    override fun edges(): Iterable<EdgeTy> = succ.values.flatMap { it.values }.toSet()
    override fun getEdge(source: NodeTy, target: NodeTy): EdgeTy {
        return succ[source]?.get(target) ?: throw IllegalArgumentException("No edge from $source to $target")
    }

    override fun getEdgeOrNUll(source: NodeTy, target: NodeTy): EdgeTy? = succ[source]?.get(target)

    override fun predecessors(vertex: NodeTy): Iterable<NodeTy> {
        return pred[vertex]?.keys.orEmpty()
    }

    override fun successors(vertex: NodeTy): Iterable<NodeTy> {
        return succ[vertex]?.keys.orEmpty()
    }

    override fun incomingEdges(vertex: NodeTy): Iterable<EdgeTy> {
        return pred[vertex]?.values.orEmpty()
    }

    override fun outgoingEdges(vertex: NodeTy): Iterable<EdgeTy> {
        return succ[vertex]?.values.orEmpty()
    }
}
