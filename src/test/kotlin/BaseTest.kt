import de.unisaarland.cs.se.selab.controller.BaseController
import de.unisaarland.cs.se.selab.controller.EmergencyResponse
import de.unisaarland.cs.se.selab.controller.Navigation
import de.unisaarland.cs.se.selab.logger.Logger
import de.unisaarland.cs.se.selab.model.AccidentEmergency
import de.unisaarland.cs.se.selab.model.Emergency
import de.unisaarland.cs.se.selab.model.SimulationData
import de.unisaarland.cs.se.selab.model.assets.Ambulance
import de.unisaarland.cs.se.selab.model.assets.FireStation
import de.unisaarland.cs.se.selab.model.assets.FireTruck
import de.unisaarland.cs.se.selab.model.assets.Hospital
import de.unisaarland.cs.se.selab.model.assets.PoliceStation
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

class BaseTest {

    @Test
    fun canMan() {
        val simulationData = SimulationData(
            GraphImpl(),
            mutableListOf(),
            mutableListOf(),
            mutableListOf(),
            mutableListOf(),
            mutableListOf(),
            100
        )
        val truck = FireTruck(0, 0, VehicleType.FIRE_TRUCK_WATER, 2, 600, 0, 10, needsLicense = true)
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
        val base = FireStation(0, Node(1), 1, listOf(truck), listOf(staff))
        assertFalse(base.canMan(truck))
        assertFalse(base.canManSimulation(truck, 100, simulationData))
        assertFalse(base.canManSimulationRequest(truck, 100, simulationData))
        base.staff[0].hasLicense = true
        assertFalse(base.canManSimulation(truck, -1, simulationData))
        assertFalse(base.canManSimulationRequest(truck, -1, simulationData))
        base.staff[0].hasLicense = false
        base.vehicles[0].vehicleType = VehicleType.K9_POLICE_CAR
        base.staff[0].staffType = StaffType.DOG_HANDLER
        assertFalse(base.canMan(truck))
        assertFalse(base.canManSimulation(truck, 100, simulationData))
        assertFalse(base.canManSimulationRequest(truck, 100, simulationData))
        assertFalse(base.canManSimulation(truck, -1, simulationData))
        assertFalse(base.canManSimulationRequest(truck, -1, simulationData))
        base.vehicles[0].vehicleType = VehicleType.EMERGENCY_DOCTOR_CAR
        base.staff[0].staffType = StaffType.EMERGENCY_DOCTOR
        assertFalse(base.canMan(truck))
        assertFalse(base.canManSimulation(truck, 100, simulationData))
        assertFalse(base.canManSimulationRequest(truck, 100, simulationData))
        assertFalse(base.canManSimulation(truck, -1, simulationData))
        assertFalse(base.canManSimulationRequest(truck, -1, simulationData))

        base.vehicles[0].vehicleType = VehicleType.FIRE_TRUCK_WATER
        base.staff[0].staffType = StaffType.FIREFIGHTER
        base.vehicles[0].staffCapacity = 1
        assertFalse(base.canMan(truck))
        assertFalse(base.canManSimulation(truck, 100, simulationData))
        assertFalse(base.canManSimulationRequest(truck, 100, simulationData))
        assertFalse(base.canManSimulation(truck, -1, simulationData))
        assertFalse(base.canManSimulationRequest(truck, -1, simulationData))

        continueWith(base, truck, simulationData)
    }

    private fun continueWith(base: FireStation, truck: FireTruck, simulationData: SimulationData) {
        base.vehicles[0].needsLicense = false
        assertTrue(base.canMan(truck))
        assertTrue(base.canManSimulation(truck, 100, simulationData))
        assertTrue(base.canManSimulationRequest(truck, 100, simulationData))
        assertFalse(base.canManSimulation(truck, -1, simulationData))
        assertFalse(base.canManSimulationRequest(truck, -1, simulationData))
        base.vehicles[0].staffCapacity = 2
        assertFalse(base.canMan(truck))
        assertFalse(base.canManSimulation(truck, 100, simulationData))
        assertFalse(base.canManSimulationRequest(truck, 100, simulationData))
        assertFalse(base.canManSimulation(truck, -1, simulationData))
        assertFalse(base.canManSimulationRequest(truck, -1, simulationData))

        assertFalse(base.canManSimulationBool(truck, 100, true, simulationData))
        assertFalse(base.canManSimulationBool(truck, 100, false, simulationData))
    }

    @Test
    fun returnVehicle() {
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
        val truck = FireTruck(
            0,
            0,
            VehicleType.FIRE_TRUCK_WATER,
            2,
            600,
            0,
            10,
            needsLicense = true,
            assignedStaff = listOf(staff).toMutableList()
        )
        val base = FireStation(0, Node(1), 1, listOf(truck), listOf(staff))
        base.returnVehicle(truck)
        assertTrue(base.staffNumber == 3)
        assertTrue(staff.outputLog)
    }

    @Test
    fun auxiliaryFunctions() {
        val truck = PoliceVehicle(0, 0, VehicleType.POLICE_CAR, 2, 3, 0, null, needsLicense = true)
        val staff = Staff(
            0,
            "Xulescu",
            0,
            StaffType.POLICE_OFFICER,
            1,
            currentShift = Shift(ShiftType.EARLY, true, true),
            nextShift = Shift(ShiftType.LATE, false, false),
            doubleShift = false,
            onCall = false,
            hasLicense = false
        )
        val base = PoliceStation(0, Node(1), 1, 2, listOf(truck), listOf(staff))

        assertTrue(base.cantAllocate(1, true, false))
        assertFalse(base.cantAllocate(1, false, false))
        assertFalse(base.cantAllocate(0, true, false))
        assertFalse(base.cantAllocate(0, false, false))
        assertTrue(base.cantAllocate(0, false, true))

        assertTrue(base.updateNeeded(4, staff) == 3)
        staff.staffType = StaffType.DOG_HANDLER
        assertTrue(base.updateNeeded(4, staff) == 4)

        val truck2 = Ambulance(0, 0, VehicleType.AMBULANCE, 2, 3, 0, null, needsLicense = true)
        val staff2 = Staff(
            0,
            "Xulescu",
            0,
            StaffType.EMT,
            1,
            currentShift = Shift(ShiftType.EARLY, true, true),
            nextShift = Shift(ShiftType.LATE, false, false),
            doubleShift = false,
            onCall = false,
            hasLicense = false
        )
        val base2 = Hospital(0, Node(1), 1, 2, listOf(truck2), listOf(staff2))

        assertTrue(base2.cantAllocate(1, true, false))
        assertFalse(base2.cantAllocate(1, false, false))
        assertFalse(base2.cantAllocate(0, true, false))
        assertFalse(base2.cantAllocate(0, false, false))
        assertTrue(base2.cantAllocate(0, false, true))

        assertTrue(base2.updateNeeded(4, staff) == 3)
        staff.staffType = StaffType.EMERGENCY_DOCTOR
        assertTrue(base2.updateNeeded(4, staff) == 4)
    }

    @Test
    fun policeAllocation() {
        val truck = PoliceVehicle(0, 0, VehicleType.POLICE_CAR, 2, 3, 0, null, needsLicense = true)
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
            StaffType.POLICE_OFFICER, 0,
            currentShift = Shift(ShiftType.EARLY, true, false),
            nextShift = Shift(ShiftType.LATE, false, false),
            doubleShift = false,
            onCall = false,
            hasLicense = false
        )
        val staff2 = Staff(
            1,
            "Xulescu",
            0,
            StaffType.POLICE_OFFICER, 0,
            currentShift = Shift(ShiftType.EARLY, false, true),
            nextShift = Shift(ShiftType.LATE, false, false),
            doubleShift = false,
            onCall = false,
            hasLicense = true
        )
        val staff3 = Staff(
            2,
            "Xulescu",
            0,
            StaffType.POLICE_OFFICER, 0,
            currentShift = Shift(ShiftType.EARLY, true, false),
            nextShift = Shift(ShiftType.LATE, false, false),
            doubleShift = false,
            onCall = false,
            hasLicense = false
        )
        val base = PoliceStation(0, Node(1), 3, 2, listOf(truck), listOf(staff, staff2, staff3))
        val baseController = BaseController(base, Navigation(simulationData))
        continueWith(truck, staff, staff2, staff3, base, baseController, simulationData)
    }

    private fun continueWith(
        truck: PoliceVehicle,
        staff: Staff,
        staff2: Staff,
        staff3: Staff,
        base: PoliceStation,
        baseController: BaseController<PoliceVehicle>,
        simulationData: SimulationData
    ) {
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
        assertTrue(
            base.allocateStaff(
                EmergencyResponse(
                    emergency,
                    baseController
                ),
                Logger(
                    PrintWriter(
                        System.out
                    )
                ),
                truck,
                200,
                false,
                simulationData
            ).second == listOf(staff, staff2)
        )
        staff.allocatedTo = null
        staff2.allocatedTo = null
        assertTrue(
            base.allocateStaff(
                EmergencyResponse(
                    emergency,
                    baseController
                ),
                Logger(
                    PrintWriter(
                        System.out
                    )
                ),
                truck,
                200,
                true,
                simulationData
            ).second == listOf(staff)
        )
        continueWith2(truck, staff, staff2, staff3, base, emergency, baseController, simulationData)
    }

    private fun continueWith2(
        truck: PoliceVehicle,
        staff: Staff,
        staff2: Staff,
        staff3: Staff,
        base: PoliceStation,
        emergency: Emergency,
        baseController: BaseController<PoliceVehicle>,
        simulationData: SimulationData
    ) {
        truck.vehicleType = VehicleType.K9_POLICE_CAR
        staff2.staffType = StaffType.DOG_HANDLER
        staff.allocatedTo = null
        staff3.allocatedTo = null
        assertTrue(
            base.allocateStaff(
                EmergencyResponse(
                    emergency,
                    baseController
                ),
                Logger(
                    PrintWriter(
                        System.out
                    )
                ),
                truck,
                200,
                false,
                simulationData
            ).second == listOf(staff, staff2)
        )
    }

    @Test
    fun fireAllocation() {
        val truck = FireTruck(0, 0, VehicleType.FIRE_TRUCK_WATER, 2, 3, 0, 5, needsLicense = true)
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
            StaffType.FIREFIGHTER, 0,
            currentShift = Shift(ShiftType.EARLY, true, false),
            nextShift = Shift(ShiftType.LATE, false, false),
            doubleShift = false,
            onCall = false,
            hasLicense = false
        )
        val staff2 = Staff(
            1,
            "Xulescu",
            0,
            StaffType.FIREFIGHTER, 0,
            currentShift = Shift(ShiftType.EARLY, false, true),
            nextShift = Shift(ShiftType.LATE, false, false),
            doubleShift = false,
            onCall = false,
            hasLicense = true
        )
        val staff3 = Staff(
            2,
            "Xulescu",
            0,
            StaffType.FIREFIGHTER, 0,
            currentShift = Shift(ShiftType.EARLY, true, false),
            nextShift = Shift(ShiftType.LATE, false, false),
            doubleShift = false,
            onCall = false,
            hasLicense = false
        )
        val base = FireStation(0, Node(1), 3, listOf(truck), listOf(staff, staff2, staff3))
        val baseController = BaseController(base, Navigation(simulationData))
        continueWith3(truck, staff, staff2, base, baseController, simulationData)
    }

    private fun continueWith3(
        truck: FireTruck,
        staff: Staff,
        staff2: Staff,
        base: FireStation,
        baseController: BaseController<FireTruck>,
        simulationData: SimulationData
    ) {
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
        assertTrue(
            base.allocateStaff(
                EmergencyResponse(
                    emergency,
                    baseController
                ),
                Logger(
                    PrintWriter(
                        System.out
                    )
                ),
                truck,
                200,
                false,
                simulationData
            ).second == listOf(staff, staff2)
        )
        staff.allocatedTo = null
        staff2.allocatedTo = null
        assertTrue(
            base.allocateStaff(
                EmergencyResponse(
                    emergency,
                    baseController
                ),
                Logger(
                    PrintWriter(
                        System.out
                    )
                ),
                truck,
                200,
                true,
                simulationData
            ).second == listOf(staff)
        )
    }

    @Test
    fun ambulanceAllocation() {
        val truck = Ambulance(0, 0, VehicleType.AMBULANCE, 2, 3, 0, null, needsLicense = true)
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
            StaffType.EMT, 0,
            currentShift = Shift(ShiftType.EARLY, true, false),
            nextShift = Shift(ShiftType.LATE, false, false),
            doubleShift = false,
            onCall = false,
            hasLicense = false
        )
        val staff2 = Staff(
            1,
            "Xulescu",
            0,
            StaffType.EMT, 0,
            currentShift = Shift(ShiftType.EARLY, false, true),
            nextShift = Shift(ShiftType.LATE, false, false),
            doubleShift = false,
            onCall = false,
            hasLicense = true
        )
        val staff3 = Staff(
            2,
            "Xulescu",
            0,
            StaffType.EMT, 0,
            currentShift = Shift(ShiftType.EARLY, true, false),
            nextShift = Shift(ShiftType.LATE, false, false),
            doubleShift = false,
            onCall = false,
            hasLicense = false
        )
        val base = Hospital(0, Node(1), 3, 2, listOf(truck), listOf(staff, staff2, staff3))
        val baseController = BaseController(base, Navigation(simulationData))
        continueWith4(truck, staff, staff2, staff3, base, baseController, simulationData)
    }

    private fun continueWith4(
        truck: Ambulance,
        staff: Staff,
        staff2: Staff,
        staff3: Staff,
        base: Hospital,
        baseController: BaseController<Ambulance>,
        simulationData: SimulationData
    ) {
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
        assertTrue(
            base.allocateStaff(
                EmergencyResponse(
                    emergency,
                    baseController
                ),
                Logger(
                    PrintWriter(
                        System.out
                    )
                ),
                truck,
                200,
                false,
                simulationData
            ).second == listOf(staff, staff2)
        )
        staff.allocatedTo = null
        staff2.allocatedTo = null
        assertTrue(
            base.allocateStaff(
                EmergencyResponse(
                    emergency,
                    baseController
                ),
                Logger(
                    PrintWriter(
                        System.out
                    )
                ),
                truck,
                200,
                true,
                simulationData
            ).second == listOf(staff)
        )
        continueWith5(truck, staff, staff2, staff3, base, emergency, baseController, simulationData)
    }

    private fun continueWith5(
        truck: Ambulance,
        staff: Staff,
        staff2: Staff,
        staff3: Staff,
        base: Hospital,
        emergency: Emergency,
        baseController: BaseController<Ambulance>,
        simulationData: SimulationData
    ) {
        truck.vehicleType = VehicleType.EMERGENCY_DOCTOR_CAR
        staff2.staffType = StaffType.EMERGENCY_DOCTOR
        staff.allocatedTo = null
        staff3.allocatedTo = null
        assertTrue(
            base.allocateStaff(
                EmergencyResponse(
                    emergency,
                    baseController
                ),
                Logger(
                    PrintWriter(
                        System.out
                    )
                ),
                truck,
                200,
                false,
                simulationData
            ).second == listOf(staff, staff2)
        )
    }
}
