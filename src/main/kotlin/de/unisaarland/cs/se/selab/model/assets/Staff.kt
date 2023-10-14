package de.unisaarland.cs.se.selab.model.assets

import de.unisaarland.cs.se.selab.logger.Logger

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
    val doubleShift: Boolean,
    val onCall: Boolean,
    val hasLicense: Boolean,
    var allocatedTo: Vehicle? = null,
    var unavailable: Boolean = false,
    var ticksSpentAtEmergencies: Int = 0,
    var atBase: Boolean = true,
    var goingHome: Boolean = false,
    var atHome: Boolean = false,
    var returningToBase: Boolean = false
) {

    var outputLog: Boolean = false

    /**
     * check if the staff can be assigned at all
     */
    fun canBeAssigned(): Boolean {
        // TODO events
        return (currentShift.working || currentShift.onCall) && allocatedTo == null
    }

    /**
     * check if the staff can be assigned and is working this shift
     */
    fun canBeAssignedWorking(): Boolean {
        // TODO events
        return currentShift.working && allocatedTo == null
    }

    /**
     * check if the staff can be assigned and is on call
     */
    fun canBeAssignedOnCall(): Boolean {
        // TODO events
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
     * logs shift changes
     */
    fun shiftLogger(logger: Logger, shift: ShiftType) {
        if (currentShift.type == shift.getNext()) {
            // his first shift is about to start
            logger.shiftStart(name, id, currentShift.type)
        }
        if (currentShift.type == shift) {
            // a shift is ending and another is beginning
            if (currentShift.working && !nextShift.working) {
                logger.shiftEnd(name, id, currentShift.type)
            }
            if (!currentShift.working && nextShift.working) {
                logger.shiftStart(name, id, nextShift.type)
            }
            if (currentShift.onCall) {
                logger.staffNotOnCall(name, id)
            }
            if (nextShift.onCall) {
                logger.staffOnCall(name, id)
            }
        }
        if (outputLog) {
            logger.staffReturn(name, id)
            outputLog = false
        }
    }

    /**
     * updates the current and next shift for this staff member
     */
    fun updateShifts(shift: ShiftType) {
        if (currentShift.type != shift) {
            return
        }
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
}
