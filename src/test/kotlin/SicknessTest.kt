import de.unisaarland.cs.se.selab.logger.Logger
import de.unisaarland.cs.se.selab.model.SicknessEvent
import de.unisaarland.cs.se.selab.model.SimulationData
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

class SicknessTest {

    @Test
    fun triggerUpdate() {
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
        val sic = SicknessEvent(10, 10, 5, 1)
        staff.ticksSpentAtEmergencies = 0
        val logger = Logger(PrintWriter(System.out))
        sic.trigger(simulationData, logger)
        assertTrue(sic.tick == 11)
        staff.ticksSpentAtEmergencies = 10
        staff.isSick = true
        sic.trigger(simulationData, logger)
        assertTrue(sic.tick == 12)
        staff.isSick = false
        staff.allocatedTo = truck
        sic.trigger(simulationData, logger)
        assertTrue(staff.unavailable && sic.tick == 12 && truck.returnB)

        simulationData.tick = 1
        sic.update(simulationData)
        assertFalse(sic.isDone)
        simulationData.tick = 100
        sic.update(simulationData)
        assertTrue(sic.isDone)
    }
}
