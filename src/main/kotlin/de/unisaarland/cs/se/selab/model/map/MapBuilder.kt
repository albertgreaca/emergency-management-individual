package de.unisaarland.cs.se.selab.model.map

import de.unisaarland.cs.se.selab.model.graph.Graph
import de.unisaarland.cs.se.selab.model.graph.GraphImpl
import de.unisaarland.cs.se.selab.parser.GlobalStatementList
import de.unisaarland.cs.se.selab.parser.ParsedGraph
import de.unisaarland.cs.se.selab.parser.PrimaryStreetType
import de.unisaarland.cs.se.selab.parser.StreetAttributes
import de.unisaarland.cs.se.selab.util.Result
import de.unisaarland.cs.se.selab.util.ifSuccess
import de.unisaarland.cs.se.selab.util.ifSuccessFlat

/**
 * A map builder.
 *
 * This class is used to build a map from a parsed graph.
 */
class MapBuilder {

    val simulationMap: Graph<Node, Road> = GraphImpl()

    /**
     * Add a node to the map.
     */
    fun addNode(id: Int): Node {
        val node = Node(id)
        simulationMap.addVertex(node)
        return node
    }

    /**
     * Check if all vertices have at least one edge.
     */
    fun addRoad(
        source: Node,
        target: Node,
        attributes: StreetAttributes
    ): Result<Unit> {
        val road = Road(
            attributes.village,
            attributes.name,
            attributes.weight,
            attributes.primaryType,
            attributes.heightLimit,
            source,
            target
        )
        if (simulationMap.getEdgeOrNUll(source, target) != null ||
            simulationMap.getEdgeOrNUll(target, source) != null
        ) {
            return Result.failure(
                "edge from $source to $target already existed"
            )
        }
        simulationMap.vertices().any { it.id == source.id } || return Result.failure("source node does not exist")
        simulationMap.vertices().any { it.id == target.id } || return Result.failure("target node does not exist")
        simulationMap.addEdge(source, target, road)
        if (!attributes.oneWay) {
            simulationMap.addEdge(target, source, road)
        }
        return Result.success(Unit)
    }

    companion object {
        /**
         * Build a map from a parsed graph and validate it.
         */
        fun buildGraph(parsedGraph: ParsedGraph): Result<Graph<Node, Road>> {
            val mapBuilder = MapBuilder()
            val nodes: MutableMap<Int, Node> = mutableMapOf()

            val globalStatements: GlobalStatementList = parsedGraph.globalStatementList
            val counties = globalStatements.edgeStatementList.filter {
                it.attributeMap.primaryType == PrimaryStreetType.COUNTY_ROAD
            }.map { it.attributeMap.village }.toSet()
            globalStatements.nodeStatementList.forEach { nodes[it] = mapBuilder.addNode(it) }
            var loopResult = Result.success(Unit)
            for (edgeStatement in globalStatements.edgeStatementList) {
                val sourceID = edgeStatement.sourceIdentifier
                val targetID = edgeStatement.targetIdentifier
                val attributes = edgeStatement.attributeMap
                val sourceNode = nodes[sourceID]
                val targetNode = nodes[targetID]

                if (sourceNode == null || targetNode == null) {
                    return Result.failure("Edge contains undefined source or target id.")
                }
                if (attributes.primaryType != PrimaryStreetType.COUNTY_ROAD && attributes.village in counties) {
                    return Result.failure("Associated village for non county road is a county name.")
                }

                loopResult = loopResult.ifSuccessFlat {
                    mapBuilder.addRoad(
                        sourceNode,
                        targetNode,
                        attributes
                    )
                }
            }

            return loopResult.ifSuccessFlat {
                checkVillages(
                    mapBuilder
                )
            }.ifSuccessFlat { checkVertices(mapBuilder) }.ifSuccessFlat {
                if (mapBuilder.simulationMap.edges().any { road ->
                    road.primaryType == PrimaryStreetType.SIDE_STREET
                }
                ) {
                    Result.success(Unit)
                } else {
                    Result.failure("No side street present")
                }
            }
                .ifSuccess { mapBuilder.simulationMap }
        }

        private fun checkVillages(mapBuilder: MapBuilder): Result<Unit> {
            var result = Result.success(Unit)
            mapBuilder.simulationMap.edges().filter {
                it.primaryType != PrimaryStreetType.COUNTY_ROAD
            }.groupBy {
                it.villageName
            }.forEach { villageRoads ->
                result = result.ifSuccessFlat {
                    if (villageRoads.value.any { road -> road.primaryType == PrimaryStreetType.MAIN_STREET }) {
                        Result.success(Unit)
                    } else {
                        Result.failure(
                            "${villageRoads.key} does not have a main street!"
                        )
                    }
                }
            }
            return result
        }

        private fun checkVertices(mapBuilder: MapBuilder): Result<Unit> {
            var result = Result.success(Unit)
            mapBuilder.simulationMap.vertices().forEach { node ->
                result = result.ifSuccessFlat {
                    if (mapBuilder.simulationMap.incomingEdges(node).iterator().hasNext()) {
                        Result.success(Unit)
                    } else {
                        Result.failure("Found Isolated Vertex!")
                    }
                }.ifSuccessFlat {
                    allEdgesFromOneVillage(mapBuilder.simulationMap.incomingEdges(node))
                }
            }
            return result
        }

        private fun allEdgesFromOneVillage(edges: Iterable<Road>): Result<Unit> {
            val villageGroups = edges.filter {
                it.primaryType != PrimaryStreetType.COUNTY_ROAD
            }.groupBy { it.villageName }
            return if (villageGroups.size > 1) {
                Result.failure("Vertex with more than one village found")
            } else {
                Result.success(Unit)
            }
        }
    }
}
