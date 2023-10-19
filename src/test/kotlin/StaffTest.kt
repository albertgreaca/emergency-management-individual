import de.unisaarland.cs.se.selab.model.SimulationData
import de.unisaarland.cs.se.selab.model.assets.PoliceVehicle
import de.unisaarland.cs.se.selab.model.assets.Shift
import de.unisaarland.cs.se.selab.model.assets.ShiftType
import de.unisaarland.cs.se.selab.model.assets.Staff
import de.unisaarland.cs.se.selab.model.assets.StaffType
import de.unisaarland.cs.se.selab.model.assets.VehicleType
import de.unisaarland.cs.se.selab.model.graph.GraphImpl
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class StaffTest {
    @Test
    fun canBeAssigned() {
        val simulationData = SimulationData(
            GraphImpl(),
            mutableListOf(),
            mutableListOf(),
            mutableListOf(),
            mutableListOf(),
            mutableListOf(),
            100
        )
        val staff = Staff(
            0,
            "Xulescu",
            0,
            StaffType.POLICE_OFFICER,
            1,
            currentShift = Shift(ShiftType.EARLY, true, false),
            nextShift = Shift(ShiftType.LATE, false, false),
            doubleShift = false,
            onCall = false,
            hasLicense = false
        )
        staff.unavailable = true
        assertFalse(staff.canBeAssigned(simulationData))
        assertFalse(staff.canBeAssignedWorking(simulationData))
        assertFalse(staff.canBeAssignedOnCall(simulationData))

        staff.unavailable = false
        simulationData.shift = ShiftType.LATE
        assertFalse(staff.canBeAssigned(simulationData))
        assertFalse(staff.canBeAssignedWorking(simulationData))
        assertFalse(staff.canBeAssignedOnCall(simulationData))

        simulationData.shift = ShiftType.EARLY
        staff.currentShift.working = false
        assertFalse(staff.canBeAssigned(simulationData))
        assertFalse(staff.canBeAssignedWorking(simulationData))
        assertFalse(staff.canBeAssignedOnCall(simulationData))

        staff.currentShift.working = true
        staff.atBase = true
        assertTrue(staff.canBeAssigned(simulationData))
        assertTrue(staff.canBeAssignedWorking(simulationData))

        staff.atBase = false
        staff.returningToBase = true
        staff.ticksAwayFromBase = 1
        assertFalse(staff.canBeAssigned(simulationData))
        assertFalse(staff.canBeAssignedWorking(simulationData))

        staff.ticksAwayFromBase = 0
        assertTrue(staff.canBeAssigned(simulationData))
        assertTrue(staff.canBeAssignedWorking(simulationData))

        staff.returningToBase = false
        staff.ticksAwayFromBase = 0
        assertFalse(staff.canBeAssigned(simulationData))
        assertFalse(staff.canBeAssignedWorking(simulationData))

        staff.ticksAwayFromBase = 1
        assertFalse(staff.canBeAssigned(simulationData))
        assertFalse(staff.canBeAssignedWorking(simulationData))
        continueWith(staff, simulationData)
    }

    private fun continueWith(staff: Staff, simulationData: SimulationData) {
        staff.currentShift.working = true
        staff.atBase = true
        staff.currentShift.onCall = false
        assertTrue(staff.canBeAssigned(simulationData))

        staff.currentShift.onCall = true
        assertTrue(staff.canBeAssigned(simulationData))
        assertTrue(staff.canBeAssignedOnCall(simulationData))

        staff.currentShift.working = false
        assertTrue(staff.canBeAssigned(simulationData))

        staff.allocatedTo = PoliceVehicle(0, 1, VehicleType.POLICE_CAR, 1, 1, 10, needsLicense = false)
        assertFalse(staff.canBeAssigned(simulationData))
        assertFalse(staff.canBeAssignedWorking(simulationData))
        assertFalse(staff.canBeAssignedOnCall(simulationData))

        staff.currentShift.working = true
        staff.atBase = true
        assertFalse(staff.canBeAssignedWorking(simulationData))
    }

    @Test
    fun setStatus() {
        val staff = Staff(
            0,
            "Xulescu",
            0,
            StaffType.POLICE_OFFICER,
            1,
            currentShift = Shift(ShiftType.EARLY, true, false),
            nextShift = Shift(ShiftType.LATE, false, false),
            doubleShift = false,
            onCall = false,
            hasLicense = false
        )
        staff.setReturningToBase()
        assertTrue(staff.returningToBase)
        assertFalse(staff.atBase)
        assertFalse(staff.goingHome)
        assertFalse(staff.atHome)

        staff.setReturningHome()
        assertFalse(staff.returningToBase)
        assertFalse(staff.atBase)
        assertTrue(staff.goingHome)
        assertFalse(staff.atHome)

        staff.setAtBase()
        assertFalse(staff.returningToBase)
        assertTrue(staff.atBase)
        assertFalse(staff.goingHome)
        assertFalse(staff.atHome)

        staff.setAtHome()
        assertFalse(staff.returningToBase)
        assertFalse(staff.atBase)
        assertFalse(staff.goingHome)
        assertTrue(staff.atHome)
    }
}
