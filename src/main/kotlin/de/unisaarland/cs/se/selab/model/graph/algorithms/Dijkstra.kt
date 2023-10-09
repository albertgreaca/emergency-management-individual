package de.unisaarland.cs.se.selab.model.graph.algorithms

import de.unisaarland.cs.se.selab.model.graph.Graph
import de.unisaarland.cs.se.selab.model.graph.Path
import de.unisaarland.cs.se.selab.util.Assertion.assertNotNull
import de.unisaarland.cs.se.selab.util.Result
import de.unisaarland.cs.se.selab.util.andThen
import kotlin.math.min

/**
 * Calculates the shortest path in a graph between the given source and target nodes
 * using Dijkstra's algorithm.
 *
 * @receiver graph to work on
 * @param source source vertex
 * @param targets set of possible target vertices
 * @param weight function that, given two vertices, returns the weight of the directed
 *               edge defined by the vertices
 * @param successorFilter function that returns `true` iff the given edge should be followed
 * @return the shortest [Path] from [source] to the closest target from [targets]
 */
fun <VertexTy : Comparable<VertexTy>, EdgeTy> Graph<VertexTy, EdgeTy>.shortestPath(
    source: VertexTy,
    targets: Set<VertexTy>,
    weight: (s: VertexTy, t: VertexTy) -> Int,
    successorFilter: (VertexTy, EdgeTy) -> Boolean = { _, _ -> true },
): Path<VertexTy, EdgeTy> {
    return dijkstraRec(
        targets,
        weight,
        successorFilter,
        listOf(source),
        emptySet(),
        emptySet(),
        mapOf(source to 0),
        emptyMap(),
    ).andThen({
        Path(
            this,
            reconstructPath(source, it),
            assertNotNull(it.distances[it.closestTargets.min()])
        )
    }, { _ -> Path(this, emptyList(), Int.MAX_VALUE) })
}

/**
 * Calculates the shortest path in a graph between the given source and target nodes
 * using Dijkstra's algorithm.
 *
 * @receiver graph to work on
 * @param source source vertex
 * @param target target vertex
 * @param weight function that, given two vertices, returns the weight of the directed
 *               edge defined by the vertices
 * @param successorFilter function that returns `true` iff the given edge should be followed
 * @return the shortest [Path] from [source] to [target]
 */
fun <VertexTy : Comparable<VertexTy>, EdgeTy> Graph<VertexTy, EdgeTy>.shortestPath(
    source: VertexTy,
    target: VertexTy,
    weight: (s: VertexTy, t: VertexTy) -> Int,
    successorFilter: (VertexTy, EdgeTy) -> Boolean = { _, _ -> true }
): Path<VertexTy, EdgeTy> {
    return shortestPath(source, setOf(target), weight, successorFilter)
}

/**
 * Class representing the result of Dijkstra's algorithm.
 *
 * @param closestTargets set of target vertices that are closest to the source
 * @param distances map from vertex to its distance from the source
 * @param predecessors map from vertex to the set of predecessors in the shortest paths
 */
private data class DijkstraResult<VertexTy>(
    val closestTargets: Set<VertexTy>,
    val distances: Map<VertexTy, Int>,
    val predecessors: Map<VertexTy, Set<VertexTy>>
)

/**
 * Tail-recursive implementation of Dijkstra's algorithm.
 */
private tailrec fun <VertexTy : Comparable<VertexTy>, EdgeTy> Graph<VertexTy, EdgeTy>.dijkstraRec(
    targets: Set<VertexTy>,
    weight: (s: VertexTy, t: VertexTy) -> Int,
    successorFilter: (VertexTy, EdgeTy) -> Boolean,
    queue: List<VertexTy>,
    visited: Set<VertexTy>,
    reachedTargets: Set<VertexTy>,
    distances: Map<VertexTy, Int>,
    predecessors: Map<VertexTy, Set<VertexTy>>,
): Result<DijkstraResult<VertexTy>> {
    if (queue.isEmpty()) {
        error("No path found.")
    }

    val sortedQueue = queue.sorted().sortedBy { distances[it] }
    val current = sortedQueue.first()
    val currentDist = assertNotNull(distances[current])

    val closestTargetDist =
        reachedTargets.firstOrNull()?.let { t: VertexTy -> distances[t] } ?: Int.MAX_VALUE

    // Mark target as reached if it has the same distance as other targets
    val newTargets = targets.toMutableSet()
    val newReachedTargets = reachedTargets.toMutableSet()
    if (current in targets && currentDist <= closestTargetDist) {
        newTargets -= current
        newReachedTargets += current
    }

    // Terminate if all targets are reached, or we are already farther away as some other target.
    if (newTargets.isEmpty() || currentDist > closestTargetDist) {
        return Result.success(DijkstraResult(newReachedTargets, distances, predecessors))
    }

    val filteredSuccessors =
        successors(current).filter { successorFilter(current, getEdge(current, it)) }
    val newFrontier = filteredSuccessors.filter { !visited.contains(it) && !queue.contains(it) }

    val newDistances = mutableMapOf<VertexTy, Int>()
    val newPredecessors = mutableMapOf<VertexTy, Set<VertexTy>>()

    for (successor in filteredSuccessors) {
        val oldDistance = distances[successor] ?: Int.MAX_VALUE
        val newDistance = saveIntAddition(currentDist, weight(current, successor))

        if (newDistance <= oldDistance) {
            newDistances[successor] = newDistance
            val newPreds = mutableSetOf(current)
            if (newDistance == oldDistance) {
                newPreds += predecessors[successor].orEmpty()
            }
            newPredecessors[successor] = newPreds
        }
    }

    return dijkstraRec(
        newTargets,
        weight,
        successorFilter,
        sortedQueue - current + newFrontier,
        visited + current,
        newReachedTargets,
        distances + newDistances,
        predecessors + newPredecessors,
    )
}

/**
 * Reconstructs the shortest path from a map of predecessors.
 *
 * Reconstructs all found (shortest) paths from the source to all given targets, sorts them
 * lexicographically by vertices, and returns the smallest one.
 */
private fun <VertexTy : Comparable<VertexTy>> reconstructPath(
    source: VertexTy,
    dijkstraResult: DijkstraResult<VertexTy>
): List<VertexTy> {
    val reconstructedPaths = dijkstraResult.closestTargets.flatMap { target ->
        reconstructPathsRec(source, target, dijkstraResult.predecessors)
    }

    fun <T : Comparable<T>> compareLists(listA: List<T>, listB: List<T>): Int {
        for (index in 0..min(listA.size, listB.size)) {
            val a = listA[index]
            val b = listB[index]
            if (a != b) {
                return a.compareTo(b)
            }
        }

        return listA.size.compareTo(listB.size)
    }

    return reconstructedPaths.minWith(::compareLists)
}

private fun <VertexTy : Comparable<VertexTy>> reconstructPathsRec(
    source: VertexTy,
    target: VertexTy,
    predecessors: Map<VertexTy, Set<VertexTy>>
): List<List<VertexTy>> {
    if (target == source) {
        return listOf(listOf(target))
    }

    return predecessors[target]?.flatMap { predecessor ->
        reconstructPathsRec(source, predecessor, predecessors).map { it + target }
    }.orEmpty()
}

/**
 * add to ints together if result is lager than Int.MAX_VALUE return Int.MAX_VALUE
 */
fun saveIntAddition(a: Int, b: Int): Int {
    return if (a.toLong() + b.toLong() > Int.MAX_VALUE) {
        Int.MAX_VALUE
    } else {
        a + b
    }
}
