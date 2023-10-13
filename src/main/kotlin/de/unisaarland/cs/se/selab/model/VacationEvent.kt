package de.unisaarland.cs.se.selab.model

/**
 * Event for staff being on vacation
 */
class VacationEvent(
    override var tick: Int,
    override val duration: Int,
    private val staffId: Int,
    override val id: Int
) : Event {

    override var isDone: Boolean = false

    override fun trigger(simulationData: SimulationData): Boolean {
        // TODO actually implement this blin
        val affectedStaff = simulationData.staff.find { it.id == staffId } ?: error(
            "Staff for vacation event not found"
        )
        if (!affectedStaff.unavailable) {
            affectedStaff.unavailable = true
        }
        return true
    }

    override fun update(simulationData: SimulationData) {
        return
    }
}
