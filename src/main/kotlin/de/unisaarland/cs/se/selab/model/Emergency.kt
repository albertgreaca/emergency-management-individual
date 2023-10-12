package de.unisaarland.cs.se.selab.model

import de.unisaarland.cs.se.selab.model.map.Road

/**
 * An emergency.
 */
sealed class Emergency(
    open val id: Int,
    open val tick: Int,
    open val road: Road,
    open val severity: Int,
    open var handlingTime: Int,
    open var maxDuration: Int
) {
    var handlingStarted: Boolean = false
    var done: Boolean = false
}
