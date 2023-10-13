package de.unisaarland.cs.se.selab.model

/**
 * class for staff sickness event
 */
class SicknessEvent(
    override var tick: Int,
    override val duration: Int,
    private val minTicks: Int,
    override val id: Int
) : Event {

    override var isDone: Boolean = false

    override fun trigger(simulationData: SimulationData): Boolean {
        // TODO actually implement this blin
        if (minTicks > 0) {
            return true
        } else {
            return false
        }
    }

    override fun update(simulationData: SimulationData) {
        return
    }
}
