package de.unisaarland.cs.se.selab.parser

import de.unisaarland.cs.se.selab.util.Result
import de.unisaarland.cs.se.selab.util.Success
import de.unisaarland.cs.se.selab.util.ifSuccess
import de.unisaarland.cs.se.selab.util.ifSuccessFlat

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
