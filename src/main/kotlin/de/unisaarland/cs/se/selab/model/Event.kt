package de.unisaarland.cs.se.selab.model

import de.unisaarland.cs.se.selab.logger.Logger

/**
 * Superclass for all events.
 */
interface Event {
    val id: Int
    var tick: Int
    val duration: Int
    var isDone: Boolean

    /**
     * Triggers the event.
     */
    fun trigger(simulationData: SimulationData, logger: Logger): Boolean

    /**
     * Updates the event.
     */
    fun update(simulationData: SimulationData)
}
