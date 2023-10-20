import de.unisaarland.cs.se.selab.logger.Logger
import de.unisaarland.cs.se.selab.model.AccidentEmergency
import de.unisaarland.cs.se.selab.model.SimulationData
import de.unisaarland.cs.se.selab.model.assets.FireTruck
import de.unisaarland.cs.se.selab.model.assets.PoliceVehicle
import de.unisaarland.cs.se.selab.model.assets.Shift
import de.unisaarland.cs.se.selab.model.assets.ShiftType
import de.unisaarland.cs.se.selab.model.assets.Staff
import de.unisaarland.cs.se.selab.model.assets.StaffType
import de.unisaarland.cs.se.selab.model.assets.VehicleType
import de.unisaarland.cs.se.selab.model.graph.GraphImpl
import de.unisaarland.cs.se.selab.model.map.Node
import de.unisaarland.cs.se.selab.model.map.Road
import de.unisaarland.cs.se.selab.parser.PrimaryStreetType
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.PrintWriter

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

    @Test
    fun updateShifts() {
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
        staff.updateShifts(ShiftType.LATE)
        assertTrue(staff.currentShift == Shift(ShiftType.EARLY, true, false))

        staff.updateShifts(ShiftType.EARLY)
        assertTrue(staff.currentShift == Shift(ShiftType.LATE, false, false))

        staff.updateShifts(ShiftType.LATE)
        staff.updateShifts(ShiftType.NIGHT)
        assertTrue(staff.currentShift == Shift(ShiftType.EARLY, true, false))

        staff.doubleShift = true
        staff.nextShift.working = true

        staff.updateShifts(ShiftType.EARLY)
        assertTrue(staff.currentShift == Shift(ShiftType.LATE, true, false))
        staff.updateShifts(ShiftType.LATE)
        staff.updateShifts(ShiftType.NIGHT)
        assertTrue(staff.currentShift == Shift(ShiftType.EARLY, false, false))

        staff.currentShift = Shift(ShiftType.EARLY, true, false)
        staff.nextShift = Shift(ShiftType.LATE, false, true)
        staff.onCall = true
        staff.doubleShift = false

        staff.updateShifts(ShiftType.EARLY)
        assertTrue(staff.nextShift == Shift(ShiftType.NIGHT, false, false))

        staff.updateShifts(ShiftType.LATE)
        assertTrue(staff.nextShift == Shift(ShiftType.EARLY, true, false))

        staff.updateShifts(ShiftType.NIGHT)
        assertTrue(staff.nextShift == Shift(ShiftType.LATE, false, true))
    }

    @Test
    fun updatePostion() {
        val staff = Staff(
            0,
            "Xulescu",
            0,
            StaffType.POLICE_OFFICER,
            3,
            currentShift = Shift(ShiftType.EARLY, true, false),
            nextShift = Shift(ShiftType.LATE, false, false),
            doubleShift = false,
            onCall = false,
            hasLicense = false,
            ticksAwayFromBase = 0
        )
        staff.allocatedTo = PoliceVehicle(0, 1, VehicleType.POLICE_CAR, 1, 1, 10, needsLicense = false)
        staff.updatePosition()
        assertTrue(staff.ticksAwayFromBase == 0)

        staff.allocatedTo = null
        staff.goingHome = true
        staff.updatePosition()
        assertTrue(staff.ticksAwayFromBase == 1)
        staff.updatePosition()
        staff.updatePosition()
        assertTrue(staff.ticksAwayFromBase == 3)
        assertTrue(staff.atHome)
        staff.goingHome = true
        staff.updatePosition()
        assertTrue(staff.ticksAwayFromBase == 3)

        staff.goingHome = false
        staff.returningToBase = true
        staff.updatePosition()
        assertTrue(staff.ticksAwayFromBase == 2)

        staff.updatePosition()
        staff.updatePosition()
        assertTrue(staff.atBase)
        staff.updatePosition()
        staff.returningToBase = true
        staff.updatePosition()
        assertTrue(staff.ticksAwayFromBase == 0)
    }

    @Test
    fun countTicks() {
        val logger = Logger(PrintWriter(System.out))
        val staff = Staff(
            0,
            "Xulescu",
            0,
            StaffType.POLICE_OFFICER,
            3,
            currentShift = Shift(ShiftType.EARLY, true, false),
            nextShift = Shift(ShiftType.LATE, false, false),
            doubleShift = false,
            onCall = false,
            hasLicense = false,
            ticksAwayFromBase = 0
        )
        staff.allocatedTo = null
        staff.unavailable = false
        staff.countTicks(logger)
        assertTrue(logger.numberTicksWorked == 0)
        staff.increaseSpentEmergency()
        assertTrue(staff.ticksSpentAtEmergencies == 0)

        staff.unavailable = true
        staff.countTicks(logger)
        assertTrue(staff.wasUnavailable)

        val emergency = AccidentEmergency(
            0,
            1,
            Road(
                " ",
                " ",
                2,
                PrimaryStreetType.COUNTY_ROAD,
                10,
                Node(2),
                Node(3)
            ),
            3,
            10,
            100
        )
        val truck = FireTruck(0, 0, VehicleType.FIRE_TRUCK_WATER, 2, 600, 0, 10, needsLicense = true)
        staff.allocatedTo = truck
        truck.currentEmergency = emergency
        staff.countTicks(logger)
        assertTrue(logger.numberTicksWorked == 1)
        requireNotNull(staff.allocatedTo).atTarget = false
        staff.increaseSpentEmergency()
        assertTrue(staff.ticksSpentAtEmergencies == 0)
        requireNotNull(staff.allocatedTo).atTarget = true
        staff.increaseSpentEmergency()
        assertTrue(staff.ticksSpentAtEmergencies == 1)
    }

    @Test
    fun updateWhereGoing() {
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
            3,
            currentShift = Shift(ShiftType.EARLY, true, false),
            nextShift = Shift(ShiftType.LATE, false, false),
            doubleShift = false,
            onCall = false,
            hasLicense = false,
            ticksAwayFromBase = 0
        )
        staff.unavailable = true
        staff.updateWhereGoing(simulationData)
        assertFalse(staff.returningToBase)

        staff.unavailable = false
        staff.allocatedTo = PoliceVehicle(0, 1, VehicleType.POLICE_CAR, 1, 1, 10, needsLicense = false)
        staff.updateWhereGoing(simulationData)
        assertFalse(staff.returningToBase)

        staff.allocatedTo = null
        staff.nextShift.working = true
        staff.ticksAwayFromBase = 1
        simulationData.tick = 9
        staff.returningToBase = false
        staff.updateWhereGoing(simulationData)
        assertTrue(staff.returningToBase)
        staff.currentShift.type = ShiftType.LATE
        staff.updateWhereGoing(simulationData)
        assertTrue(staff.returningToBase)
        staff.currentShift.type = ShiftType.EARLY

        staff.nextShift.working = false
        staff.currentShift.working = false
        staff.updateWhereGoing(simulationData)
        assertTrue(staff.goingHome)

        staff.nextShift.working = true
        staff.returningToBase = false
        simulationData.tick = 8
        staff.updateWhereGoing(simulationData)
        assertFalse(staff.returningToBase)
        staff.currentShift.type = ShiftType.LATE
        staff.currentShift.working = true
        staff.updateWhereGoing(simulationData)
        assertFalse(staff.returningToBase)
        staff.currentShift.type = ShiftType.EARLY
    }

    @Test
    fun logging() {
        val staff = Staff(
            0,
            "Xulescu",
            0,
            StaffType.POLICE_OFFICER,
            3,
            currentShift = Shift(ShiftType.EARLY, true, false),
            nextShift = Shift(ShiftType.LATE, false, false),
            doubleShift = false,
            onCall = false,
            hasLicense = false,
            ticksAwayFromBase = 0
        )
        val simulationData = SimulationData(
            GraphImpl(),
            mutableListOf(),
            mutableListOf(),
            mutableListOf(),
            mutableListOf(),
            mutableListOf(),
            100
        )
        val logger = Logger(PrintWriter(System.out))
        simulationData.tick = 9
        simulationData.shift = ShiftType.EARLY
        staff.currentShift.working = false
        staff.nextShift.working = true
        staff.logShiftStart(logger, simulationData)
        staff.currentShift.working = true
        staff.nextShift.working = false
        staff.logShiftEnd(logger, simulationData)
        staff.currentShift.onCall = true
        staff.logStaffNotOnCall(logger, simulationData)
        staff.nextShift.onCall = true
        staff.logStaffOnCall(logger, simulationData)
        simulationData.shift = ShiftType.NIGHT
        staff.logShiftStart(logger, simulationData)

        simulationData.tick = 10
        staff.logShiftStart(logger, simulationData)
        staff.logShiftEnd(logger, simulationData)
        staff.logStaffOnCall(logger, simulationData)
        staff.logStaffNotOnCall(logger, simulationData)

        staff.outputLog = true
        staff.ticksAwayFromBase = 0
        staff.logReturn(logger)
        staff.outputLog = true
        staff.ticksAwayFromBase = 1
        staff.logReturn(logger)
        staff.ticksAwayFromBase = 0
        staff.logReturn(logger)
        staff.ticksAwayFromBase = 1
        staff.logReturn(logger)
    }

    @Test
    fun updateAndCount() {
        val staff = Staff(
            0,
            "Xulescu",
            0,
            StaffType.POLICE_OFFICER,
            3,
            currentShift = Shift(ShiftType.EARLY, true, false),
            nextShift = Shift(ShiftType.LATE, false, false),
            doubleShift = false,
            onCall = false,
            hasLicense = false,
            ticksAwayFromBase = 0
        )
        val simulationData = SimulationData(
            GraphImpl(),
            mutableListOf(),
            mutableListOf(),
            mutableListOf(),
            mutableListOf(),
            mutableListOf(),
            100
        )
        val logger = Logger(PrintWriter(System.out))
        staff.updateAndCount(logger, simulationData)
        simulationData.tick = 9
        staff.updateAndCount(logger, simulationData)
        assertTrue(logger.numberShiftsWorked == 1)
        staff.wasUnavailable = true
        staff.currentShift.working = false
        staff.workedTicksThisShift = 9
        staff.updateAndCount(logger, simulationData)
        assertTrue(logger.numberShiftsWorked == 1)
    }
}
