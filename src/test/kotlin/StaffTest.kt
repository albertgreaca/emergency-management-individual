import de.unisaarland.cs.se.selab.model.SimulationData
import de.unisaarland.cs.se.selab.model.assets.Shift
import de.unisaarland.cs.se.selab.model.assets.ShiftType
import de.unisaarland.cs.se.selab.model.assets.Staff
import de.unisaarland.cs.se.selab.model.assets.StaffType
import de.unisaarland.cs.se.selab.model.graph.GraphImpl
import org.junit.jupiter.api.Assertions.assertFalse
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
        staff.unavailable = false
        simulationData.shift = ShiftType.LATE
        assertFalse(staff.canBeAssigned(simulationData))
        simulationData.shift = ShiftType.EARLY
        staff.currentShift.working = false
        assertFalse(staff.canBeAssigned(simulationData))
    }
}
