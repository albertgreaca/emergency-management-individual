package de.unisaarland.cs.se.selab.model

import de.unisaarland.cs.se.selab.model.map.Road

/**
 * A fire based emergency.
 */
data class FireEmergency(
    override val id: Int,
    override val tick: Int,
    override val road: Road,
    override val severity: Int,
    override var handlingTime: Int,
    override var maxDuration: Int
) : Emergency(id, tick, road, severity, handlingTime, maxDuration)
