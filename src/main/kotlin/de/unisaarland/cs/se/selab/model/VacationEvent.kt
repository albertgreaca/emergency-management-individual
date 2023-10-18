package de.unisaarland.cs.se.selab.model

import de.unisaarland.cs.se.selab.logger.Logger

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

    override fun trigger(simulationData: SimulationData, logger: Logger): Boolean {
        val affectedStaff = simulationData.staff.find { it.id == staffId } ?: error(
            "Staff for vacation event not found"
        )
        if (affectedStaff.allocatedTo != null || affectedStaff.ticksAwayFromBase != 0 || affectedStaff.unavailable) {
            tick++
            return false
        }
        affectedStaff.unavailable = true
        affectedStaff.ticksSpentAtEmergencies = 0

        val working = affectedStaff.currentShift.type == simulationData.shift && affectedStaff.currentShift.working
        val workingDoubleShift = affectedStaff.currentShift.type == simulationData.shift &&
            affectedStaff.currentShift.working &&
            affectedStaff.nextShift.working

        val okInCaseWorking = !working || simulationData.tick % shiftLength + duration > shiftEnd
        val okInCaseDoubleShift = !workingDoubleShift ||
            simulationData.tick % shiftLength + duration > shiftLength + shiftEnd

        if (okInCaseWorking && okInCaseDoubleShift) {
            affectedStaff.setReturningHome()
        }
        return true
    }

    override fun update(simulationData: SimulationData) {
        val affectedStaff = simulationData.staff.find { it.id == staffId } ?: error(
            "Staff for vacation event not found"
        )
        if (tick + duration <= simulationData.tick) {
            isDone = true
            affectedStaff.unavailable = false
        }
        return
    }

    companion object {
        const val shiftLength = 10
        const val shiftEnd = 9
    }
}
