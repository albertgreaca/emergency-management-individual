package de.unisaarland.cs.se.selab.parser

import de.unisaarland.cs.se.selab.util.Result
import de.unisaarland.cs.se.selab.util.ifSuccess
import de.unisaarland.cs.se.selab.util.ifSuccessFlat

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
