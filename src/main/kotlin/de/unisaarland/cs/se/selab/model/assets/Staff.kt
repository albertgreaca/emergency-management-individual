package de.unisaarland.cs.se.selab.model.assets

import de.unisaarland.cs.se.selab.controller.Simulation
import de.unisaarland.cs.se.selab.logger.Logger
import de.unisaarland.cs.se.selab.model.SimulationData

/**
 * Class to represent staff members
 */
data class Staff(
    val id: Int,
    val name: String,
    val baseID: Int,
    val staffType: StaffType,
    val ticksHome: Int,
    var ticksAwayFromBase: Int = 0,
    var currentShift: Shift,
    var nextShift: Shift,
    var doubleShift: Boolean,
    var onCall: Boolean,
    val hasLicense: Boolean,
    var allocatedTo: Vehicle? = null,
    var unavailable: Boolean = false,
    var isSick: Boolean = false,
    var ticksSick: Int = 0,
    var logSick: Boolean = false,
    var logAvailable: Boolean = false,
    var ticksSpentAtEmergencies: Int = 0,
    var atBase: Boolean = true,
    var goingHome: Boolean = false,
    var atHome: Boolean = false,
    var returningToBase: Boolean = false,
    var workedTicksThisShift: Int = 0,
    var wasUnavailable: Boolean = false,
    var lastTickWorked: Boolean = false
) {

    var outputLog: Boolean = false

    /**
     * check if the staff can be assigned at all
     */
    fun canBeAssigned(simulationData: SimulationData): Boolean {
        if (unavailable) {
            return false
        }
        if (currentShift.type != simulationData.shift) {
            return false
        }
        val ok = currentShift.working && (atBase || (returningToBase && ticksAwayFromBase == 0))
        return (ok || currentShift.onCall) && allocatedTo == null
    }

    /**
     * check if the staff can be assigned and is working this shift
     */
    fun canBeAssignedWorking(simulationData: SimulationData): Boolean {
        if (unavailable) {
            return false
        }
        if (currentShift.type != simulationData.shift) {
            return false
        }
        val ok = currentShift.working && (atBase || (returningToBase && ticksAwayFromBase == 0))
        return ok && allocatedTo == null
    }

    /**
     * check if the staff can be assigned and is on call
     */
    fun canBeAssignedOnCall(simulationData: SimulationData): Boolean {
        if (unavailable) {
            return false
        }
        if (currentShift.type != simulationData.shift) {
            return false
        }
        return currentShift.onCall && allocatedTo == null
    }

    /**
     * sets staff as going back to base
     */
    fun setReturningToBase() {
        returningToBase = true
        atBase = false
        goingHome = false
        atHome = false
    }

    /**
     * sends a staff member home
     */
    fun setReturningHome() {
        returningToBase = false
        atBase = false
        goingHome = true
        atHome = false
    }

    /**
     * puts the staff at the base
     */
    fun setAtBase() {
        returningToBase = false
        atBase = true
        goingHome = false
        atHome = false
        ticksAwayFromBase = 0
    }

    /**
     * puts the staff at home
     */
    fun setAtHome() {
        returningToBase = false
        atBase = false
        goingHome = false
        atHome = true
        ticksAwayFromBase = ticksHome
    }

    /**
     * updates the current and next shift for this staff member
     */
    fun updateShifts(shift: ShiftType) {
        if (currentShift.type != shift) {
            return
        }
        workedTicksThisShift = 0
        wasUnavailable = false
        if (doubleShift) {
            val aux: Shift = Shift(nextShift.type.getNext(), false, false)
            if (!currentShift.working) {
                aux.working = true
            }
            currentShift = nextShift
            nextShift = aux
            return
        }
        if (onCall) {
            val aux: Shift = Shift(nextShift.type.getNext(), false, false)
            if (currentShift.onCall) {
                aux.working = true
            }
            if (nextShift.working) {
                aux.onCall = true
            }
            currentShift = nextShift
            nextShift = aux
            return
        }
        val aux: Shift = Shift(nextShift.type.getNext(), false, false)
        if (!currentShift.working && !nextShift.working) {
            aux.working = true
        }
        currentShift = nextShift
        nextShift = aux
    }

    /**
     * updates the position of an asset based on boolean flags
     */
    fun updatePosition() {
        if (allocatedTo != null) {
            return
        }
        if (goingHome) {
            if (ticksAwayFromBase == ticksHome) {
                return
            }
            ticksAwayFromBase++
            atBase = false
            if (ticksAwayFromBase == ticksHome) {
                atHome = true
                goingHome = false
            }
            return
        }
        if (returningToBase) {
            if (ticksAwayFromBase == 0) {
                return
            }
            ticksAwayFromBase--
            atHome = false
            if (ticksAwayFromBase == 0) {
                atBase = true
                returningToBase = false
            }
        }
    }

    /**
     * counts working ticks
     */
    fun countTicks(logger: Logger) {
        lastTickWorked = false
        if (allocatedTo != null && requireNotNull(allocatedTo).currentEmergency != null) {
            logger.numberTicksWorked++
            workedTicksThisShift++
            lastTickWorked = true
        }
        if (unavailable) {
            wasUnavailable = true
        }
    }

    private fun increaseSpentEmergency() {
        if (allocatedTo != null && requireNotNull(allocatedTo).currentEmergency != null) {
            if (requireNotNull(allocatedTo).atTarget && !requireNotNull(allocatedTo).arrivedThisTick) {
                ticksSpentAtEmergencies++
            }
        }
    }

    /**
     * updates a staff member's position, shifts and booleans
     */
    fun updateAndCount(logger: Logger, simulationData: SimulationData) {
        increaseSpentEmergency()
        countTicks(logger)
        updateWhereGoing(simulationData)
        updatePosition()
        if (simulationData.tick % Simulation.shiftLength == Simulation.shiftEnd) {
            if (currentShift.type == simulationData.shift && currentShift.working && !wasUnavailable) {
                logger.numberShiftsWorked++
            } else if (
                currentShift.type == simulationData.shift &&
                workedTicksThisShift == Simulation.shiftLength
            ) {
                logger.numberShiftsWorked++
            }
            updateShifts(simulationData.shift)
        }
    }

    private fun detekt(unavailable: Boolean, allocatedTo: Vehicle?): Boolean {
        return unavailable || allocatedTo != null
    }

    /**
     * updates where staff member goes
     */
    fun updateWhereGoing(simulationData: SimulationData) {
        if (detekt(unavailable, allocatedTo)) {
            return
        }
        if (currentShift.type != simulationData.shift) {
            if (
                currentShift.working &&
                simulationData.tick % Simulation.shiftLength + ticksAwayFromBase == Simulation.shiftLength
            ) {
                setReturningToBase()
                return
            }
            if (
                !currentShift.working ||
                !(currentShift.type != ShiftType.LATE) ||
                2 * (ticksHome - ticksAwayFromBase) + 1 + simulationData.tick % Simulation.shiftLength <=
                Simulation.shiftLength
            ) {
                setReturningHome()
            }
            return
        }
        if (
            nextShift.working &&
            simulationData.tick % Simulation.shiftLength + ticksAwayFromBase == Simulation.shiftLength
        ) {
            setReturningToBase()
            return
        }
        val endOfWorkShift = !currentShift.working ||
            simulationData.tick % Simulation.shiftLength == Simulation.shiftEnd
        if (
            endOfWorkShift &&
            (
                !nextShift.working ||
                    2 * (ticksHome - ticksAwayFromBase) + 1 + simulationData.tick % Simulation.shiftLength <=
                    Simulation.shiftLength
                )
        ) {
            setReturningHome()
        }
    }

    /**
     * logs shift start if needed
     */
    fun logShiftStart(logger: Logger, simulationData: SimulationData) {
        if (simulationData.tick % Simulation.shiftLength == Simulation.shiftEnd) {
            if (currentShift.type == simulationData.shift.getNext()) {
                // his first shift is about to start
                logger.shiftStart(name, id, currentShift.type)
                return
            }
            if (currentShift.type == simulationData.shift && !currentShift.working && nextShift.working) {
                logger.shiftStart(name, id, nextShift.type)
                return
            }
        }
    }

    /**
     * logs shift end
     */
    fun logShiftEnd(logger: Logger, simulationData: SimulationData) {
        if (simulationData.tick % Simulation.shiftLength == Simulation.shiftEnd) {
            if (currentShift.type == simulationData.shift && currentShift.working && !nextShift.working) {
                logger.shiftEnd(name, id, currentShift.type)
            }
        }
    }

    /**
     * logs staff on call
     */
    fun logStaffOnCall(logger: Logger, simulationData: SimulationData) {
        if (simulationData.tick % Simulation.shiftLength == Simulation.shiftEnd) {
            if (currentShift.type == simulationData.shift && nextShift.onCall) {
                logger.staffOnCall(name, id)
            }
        }
    }

    /**
     * logs staff not on call
     */
    fun logStaffNotOnCall(logger: Logger, simulationData: SimulationData) {
        if (simulationData.tick % Simulation.shiftLength == Simulation.shiftEnd) {
            if (currentShift.type == simulationData.shift && currentShift.onCall) {
                logger.staffNotOnCall(name, id)
            }
        }
    }

    /**
     * logs return if needed
     */
    fun logReturn(logger: Logger) {
        if (outputLog && ticksAwayFromBase == 0) {
            logger.staffReturn(name, id)
        }
        outputLog = false
    }
}
