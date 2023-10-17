package de.unisaarland.cs.se.selab.model

import de.unisaarland.cs.se.selab.logger.Logger
import de.unisaarland.cs.se.selab.model.assets.Staff

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
    var affectedStaffs: List<Staff> = emptyList()

    override fun trigger(simulationData: SimulationData, logger: Logger): Boolean {
        affectedStaffs = simulationData.staff.filter { it.ticksSpentAtEmergencies >= minTicks }
        if (affectedStaffs.isEmpty()) {
            tick++
            return false
        }
        affectedStaffs.forEach {
            it.unavailable = true
            it.isSick = true
            it.logSick = true
            it.wasUnavailable = true
            if (it.allocatedTo != null) {
                requireNotNull(it.allocatedTo).returnB = true
            }
            it.ticksSick = duration
            if (it.lastTickWorked) {
                logger.numberTicksWorked--
                it.workedTicksThisShift--
            }
            it.setReturningHome()
        }
        return true
    }

    override fun update(simulationData: SimulationData) {
        if (tick + duration <= simulationData.tick) {
            isDone = true
            affectedStaffs.forEach {
                it.unavailable = false
                it.isSick = false
                it.logAvailable = true
            }
        }
    }
}
