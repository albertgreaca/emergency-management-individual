import de.unisaarland.cs.se.selab.logger.Logger
import de.unisaarland.cs.se.selab.model.SimulationData
import de.unisaarland.cs.se.selab.model.VacationEvent
import de.unisaarland.cs.se.selab.model.assets.Ambulance
import de.unisaarland.cs.se.selab.model.assets.Shift
import de.unisaarland.cs.se.selab.model.assets.ShiftType
import de.unisaarland.cs.se.selab.model.assets.Staff
import de.unisaarland.cs.se.selab.model.assets.StaffType
import de.unisaarland.cs.se.selab.model.assets.VehicleType
import de.unisaarland.cs.se.selab.model.graph.GraphImpl
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.PrintWriter

class VacationTest {

    @Test
    fun trigger() {
        val truck = Ambulance(0, 0, VehicleType.AMBULANCE, 2, 3, 0, null, needsLicense = true)
        val staff = Staff(
            0,
            "Xulescu",
            0,
            StaffType.FIREFIGHTER,
            1,
            currentShift = Shift(ShiftType.EARLY, true, true),
            nextShift = Shift(ShiftType.LATE, false, false),
            doubleShift = false,
            onCall = false,
            hasLicense = false
        )
        val simulationData = SimulationData(
            GraphImpl(),
            mutableListOf(),
            mutableListOf(),
            mutableListOf(),
            mutableListOf(),
            listOf(staff).toMutableList(),
            100
        )
        val vac = VacationEvent(1, 10, 0, 5)
        staff.allocatedTo = truck
        val logger = Logger(PrintWriter(System.out))
        vac.trigger(simulationData, logger)
        assertTrue(vac.tick == 2)
        staff.allocatedTo = null
        staff.ticksAwayFromBase = 1
        vac.trigger(simulationData, logger)
        assertTrue(vac.tick == 3)
        staff.ticksAwayFromBase = 0
        staff.unavailable = true
        vac.trigger(simulationData, logger)
        assertTrue(vac.tick == 4)
        staff.unavailable = false
        vac.trigger(simulationData, logger)
        assertTrue(staff.unavailable && vac.tick == 4)

        simulationData.tick = 1
        vac.update(simulationData)
        assertTrue(staff.unavailable)
        simulationData.tick = 100
        vac.update(simulationData)
        assertFalse(staff.unavailable)
    }
}
