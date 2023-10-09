package de.unisaarland.cs.se.selab.parser

import de.unisaarland.cs.se.selab.parser.config.Constants
import de.unisaarland.cs.se.selab.util.Result
import de.unisaarland.cs.se.selab.util.Success
import de.unisaarland.cs.se.selab.util.ifSuccess
import de.unisaarland.cs.se.selab.util.ifSuccessFlat

/**
 * Get the next token of a type T if the token is not of type T return [Result.failure] with the error message
 */
inline fun <reified T> ArrayDeque<Token>.parseNextToken(errorMsg: String): Result<T> {
    return when (val tmpToken = this.removeFirstOrNull()) {
        is T -> Result.success(tmpToken)
        else -> Result.failure(errorMsg.format(tmpToken))
    }
}

/**
 * Check if the next token is of type T
 */
inline fun <reified T> ArrayDeque<Token>.checkNextToken(): Boolean {
    return when (this.firstOrNull()) {
        is T -> true
        else -> false
    }
}

/**
 * Parse the map from a token list
 */
class MapParser(private val tokenList: ArrayDeque<Token>) {
    /**
     * Parse the map from a token list
     */
    fun parse(): Result<ParsedGraph> {
        return ParsedGraph.parseGraph(tokenList)
    }
}

/**
 * Data class for the parsed graph
 */
data class ParsedGraph(val globalStatementList: GlobalStatementList) {
    companion object {
        /**
         * Parse the graph from a token list
         */
        fun parseGraph(tokenList: ArrayDeque<Token>): Result<ParsedGraph> {
            return tokenList.parseNextToken<IdentifierT>("No graph type provided found %s.").ifSuccessFlat {
                it.convertToString().ifSuccessFlat { graphDef ->
                    if (graphDef.startsWith("digraph")) {
                        val name = graphDef.replace("digraph", "")
                        if (name.isNotEmpty()) {
                            tokenList.addFirst(IdentifierT(name))
                        }
                        Result.success(Unit)
                    } else {
                        Result.failure("Graph needs to be a digraph got $graphDef")
                    }
                }
            }.ifSuccessFlat { tokenList.parseNextToken<IdentifierT>("Graph has no name.") }.ifSuccessFlat { name ->
                name.convertToString()
            }.ifSuccessFlat { _ ->
                tokenList.parseNextToken<LBraceT>("Graph needs to start with {").ifSuccessFlat {
                    GlobalStatementList.parseGlobalStatementList(tokenList).ifSuccess {
                            globalStatementList ->
                        ParsedGraph(globalStatementList)
                    }
                }.ifSuccessFlat { graph ->
                    tokenList.parseNextToken<RBraceT>("Closing brace missing.").ifSuccess {
                        graph
                    }
                }.ifSuccessFlat { graph ->
                    if (tokenList.isEmpty()) {
                        Result.success(graph)
                    } else {
                        Result.failure("Unexpected token: ${tokenList.first()}")
                    }
                }
            }
        }
    }
}

/**
 * Data class for the parsed global statement list
 */
data class GlobalStatementList(
    val nodeStatementList: Set<Int>,
    val edgeStatementList: List<EdgeStatement>
) {

    companion object {
        /**
         * Parse the global statement list from a token list
         */
        fun parseGlobalStatementList(tokenList: ArrayDeque<Token>): Result<GlobalStatementList> {
            val nodeStatementList: MutableSet<Int> = mutableSetOf()

            val nodeResult: Result<Int> = parseNodes(nodeStatementList, tokenList)

            val edgeStatementList: MutableList<EdgeStatement> = mutableListOf()
            return nodeResult.ifSuccessFlat {
                parseEdges(edgeStatementList, tokenList, it)
                    .ifSuccess { GlobalStatementList(nodeStatementList, edgeStatementList) }
            }.ifSuccessFlat { globalStatementList ->
                globalStatementList.edgeStatementList.groupBy {
                    Pair(it.attributeMap.name, it.attributeMap.village)
                }.all { roads ->
                    roads.value.size == 1
                }.let { result ->
                    if (result) {
                        Result.success(
                            globalStatementList
                        )
                    } else {
                        Result.failure("Non unique Road in a village")
                    }
                }
            }
        }

        /**
         * Parse the nodes of the graph from a token list.
         */
        private fun parseNodes(nodeStatementList: MutableSet<Int>, tokenList: ArrayDeque<Token>): Result<Int> {
            return tokenList.parseNextToken<IdentifierT>("First token of the graph needs to be an identifier")
                .ifSuccessFlat { it.convertToValue() }
                .ifSuccessFlat { node ->
                    var nodeId = node
                    var loopResult = Result.success(nodeId)
                    while (tokenList.checkNextToken<SemicolonT>() && loopResult is Success) {
                        loopResult =
                            tokenList.parseNextToken<SemicolonT>("Found Semicolon Lost it.")
                                .ifSuccessFlat { _ ->
                                    if (nodeStatementList.contains(nodeId)) {
                                        Result.failure("Node identifier already used.")
                                    } else if (nodeId < 0) {
                                        Result.failure("Node id needs to be positive.")
                                    } else {
                                        nodeStatementList += nodeId
                                        tokenList.parseNextToken<IdentifierT>("Expected Node Identifier")
                                            .ifSuccessFlat { it.convertToValue() }.ifSuccess {
                                                nodeId = it
                                                it
                                            }
                                    }
                                }
                    }
                    return@ifSuccessFlat loopResult
                }
        }

        /**
         * Parse the edges of the graph from a token list.
         */
        private fun parseEdges(
            edgeStatementList: MutableList<EdgeStatement>,
            tokenList: ArrayDeque<Token>,
            nodeId: Int
        ): Result<Unit> {
            var sourceId = nodeId
            var loopResult = Result.success(Unit)
            while (tokenList.checkNextToken<RightarrowT>() && loopResult is Success) {
                var targetId: Int = 0 // Will be correct when used
                loopResult = tokenList.parseNextToken<RightarrowT>("Found right arrow, lost it.").ifSuccessFlat {
                    tokenList.parseNextToken<IdentifierT>("Expected Node identifier")
                }.ifSuccessFlat { it.convertToValue() }
                    .ifSuccessFlat {
                        targetId = it
                        StreetAttributes.parseAttributeList(tokenList)
                    }
                    .ifSuccessFlat { attributes ->
                        if (sourceId == targetId) {
                            return@ifSuccessFlat Result.failure("Edge with same end nodes:  $sourceId, $targetId.")
                        } else {
                            edgeStatementList.add(EdgeStatement(sourceId, targetId, attributes))
                            return@ifSuccessFlat Result.success(Unit)
                        }
                    }.ifSuccessFlat {
                        tokenList.parseNextToken<SemicolonT>("Edge definition ends with semicolon got %s")
                    }.ifSuccess { // convert to unit
                    }
                if (tokenList.checkNextToken<IdentifierT>()) {
                    loopResult = tokenList.parseNextToken<IdentifierT>("Found Id, Lost it")
                        .ifSuccessFlat { it.convertToValue() }
                        .ifSuccess { sourceId = it }
                } else {
                    return loopResult
                }
            }
            return loopResult
        }
    }
}

/**
 * Data class for an edge statement
 */
data class EdgeStatement(
    val sourceIdentifier: Int,
    val targetIdentifier: Int,
    val attributeMap: StreetAttributes
)

/**
 * Data class for the parsed street attributes
 */
data class StreetAttributes(
    val village: String,
    val name: String,
    val heightLimit: Int,
    val primaryType: PrimaryStreetType,
    val oneWay: Boolean,
    val weight: Int
) {

    /**
     * Enum class for the primary street type
     */
    class Builder {
        private lateinit var village: String
        private lateinit var name: String
        private var heightLimit: Int = 0
        private lateinit var primaryType: PrimaryStreetType
        private lateinit var secondaryType: SecondaryStreetType
        private var weight: Int = 0

        /**
         * Set the village
         */
        fun village(value: String) = apply { this.village = value }

        /**
         * Set the name
         */
        fun name(value: String) = apply { this.name = value }

        /**
         * Set the height limit
         */
        fun heightLimit(value: Int) = apply { this.heightLimit = value }

        /**
         * Set the primary street type
         */
        fun primaryType(value: PrimaryStreetType) = apply { this.primaryType = value }

        /**
         * Set the secondary street type
         */
        fun secondaryType(value: SecondaryStreetType) = apply { this.secondaryType = value }

        /**
         * Set the weight
         */
        fun weight(value: Int) = apply { this.weight = value }

        /**
         * Build the street attributes
         */
        fun checkAndBuild(): Result<StreetAttributes> {
            if (weight < 0) {
                return Result.failure("The length of roads has to be greater than  0 ")
            }
            if (secondaryType == SecondaryStreetType.TUNNEL && heightLimit > Constants.MAX_TUNNEL_HEIGHT) {
                return Result.failure("Tunnel to high!")
            }
            return Result.success(
                StreetAttributes(
                    village,
                    name,
                    heightLimit,
                    primaryType,
                    secondaryType == SecondaryStreetType.ONEWAY_STREET,
                    weight
                )
            )
        }
    }

    companion object {
        /**
         * Parse the attribute list
         */
        fun parseAttributeList(tokenList: ArrayDeque<Token>): Result<StreetAttributes> {
            val attributesBuilder = Builder()
            return tokenList.parseNextToken<LBracketT>("Opening bracket expected for attribute list. Found: %s")
                .ifSuccessFlat {
                    var loopResult = Result.success(Unit)
                    StreetAttribute.values().forEach { requiredAttribute ->
                        loopResult =
                            loopResult.ifSuccessFlat {
                                tokenList.parseNextToken<AttributeT>("Unexpected attribute found: %s")
                            }.ifSuccessFlat { attribute ->
                                if (attribute.type == requiredAttribute) {
                                    tokenList.parseNextToken<EqualsT>("Expected equals")
                                } else {
                                    Result.failure(
                                        "Expected attribute: ${requiredAttribute.rep}" +
                                            " found: ${attribute.type.rep}"
                                    )
                                }
                                    .ifSuccessFlat { _ ->
                                        tokenList.parseNextToken<IdentifierT>("Expected a Value for an $attribute")
                                            .ifSuccessFlat { attribute.type.addToBuilder(attributesBuilder, it) }
                                    }
                            }.ifSuccessFlat {
                                tokenList.parseNextToken<SemicolonT>(
                                    "Expected Semicolon to end Attribute statement found: %s"
                                )
                            }.ifSuccess { }
                    }
                    return@ifSuccessFlat loopResult
                }.ifSuccessFlat {
                    tokenList.parseNextToken<RBracketT>("Expect attributes to end with ]")
                }.ifSuccessFlat { attributesBuilder.checkAndBuild() }
        }
    }
}
