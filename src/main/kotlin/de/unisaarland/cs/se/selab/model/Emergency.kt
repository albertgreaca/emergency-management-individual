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

/**
 * A fire based emergency.
 */
data class Fire(
    override val id: Int,
    override val tick: Int,
    override val road: Road,
    override val severity: Int,
    override var handlingTime: Int,
    override var maxDuration: Int
) : Emergency(id, tick, road, severity, handlingTime, maxDuration)

/**
 * An accident.
 */
data class Accident(
    override val id: Int,
    override val tick: Int,
    override val road: Road,
    override val severity: Int,
    override var handlingTime: Int,
    override var maxDuration: Int
) : Emergency(id, tick, road, severity, handlingTime, maxDuration)

/**
 * A crime.
 */
data class Crime(
    override val id: Int,
    override val tick: Int,
    override val road: Road,
    override val severity: Int,
    override var handlingTime: Int,
    override var maxDuration: Int,
) : Emergency(id, tick, road, severity, handlingTime, maxDuration)

/**
 * A medical emergency.
 */
data class MedicalEmergency(
    override val id: Int,
    override val tick: Int,
    override val road: Road,
    override val severity: Int,
    override var handlingTime: Int,
    override var maxDuration: Int
) : Emergency(id, tick, road, severity, handlingTime, maxDuration)
