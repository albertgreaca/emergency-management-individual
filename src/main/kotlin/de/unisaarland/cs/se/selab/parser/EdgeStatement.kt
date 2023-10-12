package de.unisaarland.cs.se.selab.parser

/**
 * Data class for an edge statement
 */
data class EdgeStatement(
    val sourceIdentifier: Int,
    val targetIdentifier: Int,
    val attributeMap: StreetAttributes
)
