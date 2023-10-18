package de.unisaarland.cs.se.selab.model.assets

import de.unisaarland.cs.se.selab.controller.EmergencyResponse
import de.unisaarland.cs.se.selab.logger.Logger
import de.unisaarland.cs.se.selab.model.Emergency
import de.unisaarland.cs.se.selab.model.SimulationData
import de.unisaarland.cs.se.selab.model.map.Node

/**
 * Superclass for the bases.
 */
sealed class Base<T : Vehicle>(val vehicles: List<T>, val staff: List<Staff>) {
    abstract val id: Int
    abstract val location: Node
    abstract var staffNumber: Int
    val requests: MutableList<AssetInquiry> = mutableListOf()

    val availableVehicles: List<T>
        get() = vehicles.filter {
            it.currentEmergency == null &&
                it.location == location &&
                it.allocatable &&
                canMan(it)
        }.sortedBy { it.id }

    /**
     * Get a list of vehicles which could be reallocated to handle the given emergency.
     */
    fun reAllocatableVehicles(emergency: Emergency): List<T> {
        return vehicles.filter {
            it.ready && !it.broken &&
                (
                    (!it.atHome && it.currentEmergency == null) ||
                        (
                            it.currentEmergency != null &&
                                !it.atTarget &&
                                (it.currentEmergency?.severity ?: 0) < emergency.severity
                            )
                    ) && !it.assignedStaff.any { it2 -> it2.isSick }
        }
            .sortedBy { it.id }
    }

    /**
     * Check if the vehicle can be manned by this base during parsing.
     */
    open fun canMan(vehicle: Vehicle): Boolean {
        val cnt = this.staff.count {
            val ok = it.staffType != StaffType.DOG_HANDLER && it.staffType != StaffType.EMERGENCY_DOCTOR
            ok
        }
        if (vehicle.needsLicense) {
            return vehicle.staffCapacity <= cnt && staff.any { it.hasLicense }
        }
        return vehicle.staffCapacity <= cnt
    }

    /**
     * Check if the vehicle can be manned by this base during simulation.
     */
    open fun canManSimulation(vehicle: Vehicle, ticksLimit: Int, simulationData: SimulationData): Boolean {
        var ans: Boolean = vehicle.staffCapacity <= this.staff.count {
            val ok = it.staffType != StaffType.DOG_HANDLER && it.staffType != StaffType.EMERGENCY_DOCTOR
            it.canBeAssigned(simulationData) && it.ticksAwayFromBase <= ticksLimit && ok
        }
        if (vehicle.needsLicense) {
            ans = ans && staff.any { it.canBeAssigned(simulationData) && it.hasLicense }
        }
        if (vehicle.vehicleType == VehicleType.K9_POLICE_CAR) {
            ans = ans && staff.any { it.canBeAssigned(simulationData) && it.staffType == StaffType.DOG_HANDLER }
        }
        if (vehicle.vehicleType == VehicleType.EMERGENCY_DOCTOR_CAR) {
            ans = ans && staff.any { it.canBeAssigned(simulationData) && it.staffType == StaffType.EMERGENCY_DOCTOR }
        }
        return ans
    }

    /**
     * Check if the vehicle can be manned by this base during a request.
     */
    open fun canManSimulationRequest(vehicle: Vehicle, ticksLimit: Int, simulationData: SimulationData): Boolean {
        var ans: Boolean = vehicle.staffCapacity <= this.staff.count {
            val ok = it.staffType != StaffType.DOG_HANDLER && it.staffType != StaffType.EMERGENCY_DOCTOR
            it.canBeAssignedWorking(simulationData) && it.ticksAwayFromBase <= ticksLimit && ok
        }
        if (vehicle.needsLicense) {
            ans = ans && staff.any { it.canBeAssignedWorking(simulationData) && it.hasLicense }
        }
        if (vehicle.vehicleType == VehicleType.K9_POLICE_CAR) {
            ans = ans && staff.any { it.canBeAssignedWorking(simulationData) && it.staffType == StaffType.DOG_HANDLER }
        }
        if (vehicle.vehicleType == VehicleType.EMERGENCY_DOCTOR_CAR) {
            ans = ans &&
                staff.any { it.canBeAssignedWorking(simulationData) && it.staffType == StaffType.EMERGENCY_DOCTOR }
        }
        return ans
    }

    /**
     * general function for can man
     */
    open fun canManSimulationBool(
        vehicle: Vehicle,
        ticksLimit: Int,
        request: Boolean,
        simulationData: SimulationData
    ): Boolean {
        if (request) {
            return canManSimulationRequest(vehicle, ticksLimit, simulationData)
        }
        return canManSimulation(vehicle, ticksLimit, simulationData)
    }

    /**
     * Check if the base has enough special staff and reduce the number of special staff if necessary.
     */
    open fun allocateStaff(
        emergencyResponse: EmergencyResponse,
        logger: Logger,
        vehicle: T,
        ticksLimit: Int,
        request: Boolean,
        simulationData: SimulationData
    ): Pair<Int, MutableList<Staff>> {
        // Do nothing by default
        return Pair(0, mutableListOf())
    }

    /**
     * Return staff of the [vehicle] to the base.
     */
    open fun returnVehicle(vehicle: Vehicle) {
        staffNumber += vehicle.staffCapacity
        for (staff in vehicle.assignedStaff) {
            staff.allocatedTo = null
            staff.outputLog = true
        }
        vehicle.assignedStaff.clear()
    }
}
