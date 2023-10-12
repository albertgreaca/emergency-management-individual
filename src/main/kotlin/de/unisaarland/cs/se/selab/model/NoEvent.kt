package de.unisaarland.cs.se.selab.model

/**
 * Class representing no actual event.
 */
data class NoEvent(
    override var tick: Int = 0,
    override val duration: Int = Int.MAX_VALUE,
    override val id: Int = -1
) : RoadEvent() {
    override var isDone: Boolean = false
    override fun trigger(simulationData: SimulationData): Boolean {
        return false
    }

    override fun update(simulationData: SimulationData) {
        // nothing to do here
        Unit
    }
}
