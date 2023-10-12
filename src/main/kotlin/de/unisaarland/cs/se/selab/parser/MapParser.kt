package de.unisaarland.cs.se.selab.parser

import de.unisaarland.cs.se.selab.util.Result
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
