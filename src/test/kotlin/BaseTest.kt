import de.unisaarland.cs.se.selab.model.SimulationData
import de.unisaarland.cs.se.selab.model.assets.FireStation
import de.unisaarland.cs.se.selab.model.assets.FireTruck
import de.unisaarland.cs.se.selab.model.assets.Shift
import de.unisaarland.cs.se.selab.model.assets.ShiftType
import de.unisaarland.cs.se.selab.model.assets.Staff
import de.unisaarland.cs.se.selab.model.assets.StaffType
import de.unisaarland.cs.se.selab.model.assets.VehicleType
import de.unisaarland.cs.se.selab.model.graph.GraphImpl
import de.unisaarland.cs.se.selab.model.map.Node
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

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
}
