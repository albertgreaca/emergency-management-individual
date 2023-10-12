package de.unisaarland.cs.se.selab.model

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
    fun trigger(simulationData: SimulationData): Boolean

    /**
     * Updates the event.
     */
    fun update(simulationData: SimulationData)
}
