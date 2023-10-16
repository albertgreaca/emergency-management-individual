package de.unisaarland.cs.se.selab.model.assets

import de.unisaarland.cs.se.selab.controller.EmergencyResponse
import de.unisaarland.cs.se.selab.logger.Logger
import de.unisaarland.cs.se.selab.model.map.Node

/**
 * A hospital.
 */
data class Hospital(
    override val id: Int,
    override val location: Node,
    override var staffNumber: Int,
    var doctorNumber: Int,
    val ambulances: List<Ambulance>,
    val hospitalStaff: List<Staff>
) : Base<Ambulance>(ambulances, hospitalStaff) {
    override fun canMan(vehicle: Vehicle): Boolean {
        return super.canMan(vehicle) &&
            vehicle is Ambulance && (vehicle.vehicleType != VehicleType.EMERGENCY_DOCTOR_CAR || this.doctorNumber > 0)
    }

    override fun returnVehicle(vehicle: Vehicle) {
        super.returnVehicle(vehicle)
        if (vehicle.vehicleType == VehicleType.EMERGENCY_DOCTOR_CAR) {
            doctorNumber++
        }
    }

    private fun cantAllocate(
        needed: Int,
        badLicense: Boolean,
        badED: Boolean
    ): Boolean {
        if (needed == 1 && badLicense) {
            return true
        }
        if (needed == 0 && badED) {
            return true
        }
        return false
    }

    private fun updateNeeded(needed: Int, staff: Staff): Int {
        if (!(staff.staffType == StaffType.EMERGENCY_DOCTOR)) {
            return needed - 1
        }
        return needed
    }

    override fun allocateStaff(
        emergencyResponse: EmergencyResponse,
        logger: Logger,
        vehicle: Ambulance,
        ticksLimit: Int,
        request: Boolean
    ): Int {
        var needed: Int = vehicle.staffCapacity
        var needsLicense: Boolean = vehicle.needsLicense
        var needsEMD: Boolean = vehicle.vehicleType == VehicleType.EMERGENCY_DOCTOR_CAR
        if (vehicle.vehicleType == VehicleType.EMERGENCY_DOCTOR_CAR) {
            doctorNumber--
        }
        for (staff in hospitalStaff.sortedBy { it.id }) {
            val isEMD = staff.staffType == StaffType.EMERGENCY_DOCTOR
            val ok = if (isEMD) {
                needsEMD
            } else {
                needed > 0
            }
            if (staff.canBeAssignedWorking() && staff.ticksAwayFromBase <= ticksLimit && ok) {
                val badLicense = needsLicense && !staff.hasLicense
                val badEMD = needsEMD && !isEMD
                if (cantAllocate(needed, badLicense, badEMD)) {
                    continue
                }
                logger.staffAllocation(staff.name, staff.id, vehicle.id, emergencyResponse.emergency.id)
                needed = updateNeeded(needed, staff)
                needsLicense = badLicense
                needsEMD = badEMD
                staff.allocatedTo = vehicle
                vehicle.assignedStaff.add(staff)
            }
        }
        if (request) {
            return 0
        }
        return allocateStaffOnCall(emergencyResponse, logger, vehicle, ticksLimit, needed, needsLicense, needsEMD)
    }

    private fun allocateStaffOnCall(
        emergencyResponse: EmergencyResponse,
        logger: Logger,
        vehicle: Ambulance,
        ticksLimit: Int,
        needed2: Int,
        needsLicense2: Boolean,
        needsEMD2: Boolean
    ): Int {
        var needed = needed2
        var needsLicense = needsLicense2
        var needsEMD = needsEMD2
        var maxTicks = 0
        for (staff in hospitalStaff.sortedBy { it.id }) {
            val ok = needed > 0 || needsEMD
            if (staff.canBeAssignedOnCall() && staff.ticksAwayFromBase <= ticksLimit && ok) {
                val badLicense = needsLicense && !staff.hasLicense
                val badEMD = needsEMD && !(staff.staffType == StaffType.EMERGENCY_DOCTOR)
                if (cantAllocate(needed, badLicense, badEMD)) {
                    continue
                }
                logger.staffAllocation(staff.name, staff.id, vehicle.id, emergencyResponse.emergency.id)
                needed = updateNeeded(needed, staff)
                needsLicense = badLicense
                needsEMD = badEMD
                staff.allocatedTo = vehicle
                vehicle.assignedStaff.add(staff)
                staff.setReturningToBase()
                maxTicks = Math.max(maxTicks, staff.ticksAwayFromBase)
            }
        }
        return maxTicks
    }
}
