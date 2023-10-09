package de.unisaarland.cs.se.selab.model.graph

/**
 * Interface for (directed) graphs.
 */
interface Graph<VertexTy, EdgeTy> {

    /**
     * Adds the given vertex to the graph.
     *
     * @param vertex vertex to add
     */
    fun addVertex(vertex: VertexTy)

    /**
     * Adds an edge to the graph.
     *
     * If [source] or [target] do not exist in the graph they will be added.
     * Existing edges will be overwritten.
     *
     * @param source source vertex of the edge
     * @param target target vertex of the edge
     * @param edge edge data
     */
    fun addEdge(source: VertexTy, target: VertexTy, edge: EdgeTy)

    /**
     * Access the graph's vertices.
     *
     * @return an iterable over the graph's vertices
     */
    fun vertices(): Iterable<VertexTy>

    /**
     * Access the graph's edges.
     *
     * @return an iterable over the graph's edges
     */
    fun edges(): Iterable<EdgeTy>

    /**
     * Retrieve an edge between two vertices. If no such edge exists, `null` is returned.
     */
    fun getEdgeOrNUll(source: VertexTy, target: VertexTy): EdgeTy?

    /**
     *  Retrieve an edge between two vertices. If no such edge exists, an exception is thrown.
     */
    fun getEdge(source: VertexTy, target: VertexTy): EdgeTy

    /**
     * Retrieve the predecessors of a vertex.
     *
     * @param vertex to get predecessors for
     * @return an iterable over the vertex's predecessors
     */
    fun predecessors(vertex: VertexTy): Iterable<VertexTy>

    /**
     * Retrieve the successors of a vertex.
     *
     * @param vertex to get successors for
     * @return an iterable over the vertex's successors
     */
    fun successors(vertex: VertexTy): Iterable<VertexTy>

    /**
     * Retrieve the outgoing edges of a vertex.
     *
     * @param vertex to get outgoing edges for
     * @return an iterable over the vertex's outgoing edges
     */
    fun outgoingEdges(vertex: VertexTy): Iterable<EdgeTy>

    /**
     * Retrieve the incoming edges of a vertex.
     *
     * @param vertex to get incoming edges for
     * @return an iterable over the vertex's incoming edges
     */
    fun incomingEdges(vertex: VertexTy): Iterable<EdgeTy>
}
