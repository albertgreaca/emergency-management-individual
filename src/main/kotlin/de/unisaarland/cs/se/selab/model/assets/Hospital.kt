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

    override fun canMan(vehicles: List<Ambulance>): Boolean {
        return super.canMan(vehicles) &&
            vehicles.count { it.vehicleType == VehicleType.EMERGENCY_DOCTOR_CAR } <= this.doctorNumber
    }

    private fun cantAllocate(
        needed: Int,
        badLicense: Boolean,
        badED: Boolean,
        hasBoth: Boolean,
        badBoth: Boolean
    ): Boolean {
        val cond1 = needed == 1 && (badLicense || badED)
        val cond2 = needed == 2 && !hasBoth && badBoth
        if (cond1 || cond2) {
            return true
        }
        return false
    }

    override fun allocateStaff(emergencyResponse: EmergencyResponse, logger: Logger, vehicle: Ambulance): Int {
        var needed: Int = vehicle.staffCapacity
        var needsLicense: Boolean = vehicle.needsLicense
        var needsEMD: Boolean = vehicle.vehicleType == VehicleType.EMERGENCY_DOCTOR_CAR
        if (vehicle.vehicleType == VehicleType.EMERGENCY_DOCTOR_CAR) {
            doctorNumber--
        }
        val hasBoth = hospitalStaff.any {
            it.canBeAssigned() &&
                it.hasLicense &&
                it.staffType == StaffType.EMERGENCY_DOCTOR
        }
        for (staff in hospitalStaff.sortedBy { it.id }) {
            if (staff.canBeAssignedWorking()) {
                val badLicense = needsLicense && !staff.hasLicense
                val badEMD = needsEMD && !(staff.staffType == StaffType.EMERGENCY_DOCTOR)
                val badBoth = badLicense && badEMD
                if (cantAllocate(needed, badLicense, badEMD, hasBoth, badBoth)) {
                    continue
                }
                logger.staffAllocation(staff.name, staff.id, vehicle.id, emergencyResponse.emergency.id)
                needed -= 1
                needsLicense = badLicense
                needsEMD = badEMD
                staff.allocatedTo = vehicle
            }
        }
        return allocateStaffOnCall(emergencyResponse, logger, vehicle, needed, needsLicense, needsEMD, hasBoth)
    }

    private fun allocateStaffOnCall(
        emergencyResponse: EmergencyResponse,
        logger: Logger,
        vehicle: Ambulance,
        needed2: Int,
        needsLicense2: Boolean,
        needsEMD2: Boolean,
        hasBoth2: Boolean
    ): Int {
        var needed = needed2
        var needsLicense = needsLicense2
        var needsEMD = needsEMD2
        val hasBoth = hasBoth2
        var maxTicks = 0
        for (staff in hospitalStaff.sortedBy { it.id }) {
            if (staff.canBeAssignedOnCall()) {
                val badLicense = needsLicense && !staff.hasLicense
                val badEMD = needsEMD && !(staff.staffType == StaffType.EMERGENCY_DOCTOR)
                val badBoth = badLicense && badEMD
                if (cantAllocate(needed, badLicense, badEMD, hasBoth, badBoth)) {
                    continue
                }
                logger.staffAllocation(staff.name, staff.id, vehicle.id, emergencyResponse.emergency.id)
                needed -= 1
                needsLicense = badLicense
                needsEMD = badEMD
                staff.allocatedTo = vehicle
                maxTicks = Math.max(maxTicks, staff.ticksAwayFromBase)
            }
        }
        return maxTicks
    }
}
