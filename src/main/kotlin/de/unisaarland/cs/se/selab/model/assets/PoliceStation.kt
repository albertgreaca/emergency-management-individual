package de.unisaarland.cs.se.selab.model.assets

import de.unisaarland.cs.se.selab.controller.EmergencyResponse
import de.unisaarland.cs.se.selab.logger.Logger
import de.unisaarland.cs.se.selab.model.map.Node

/**
 * A police station.
 */
data class PoliceStation(
    override val id: Int,
    override val location: Node,
    override var staffNumber: Int,
    private var dogNumber: Int,
    val cars: List<PoliceVehicle>,
    val policeStaff: List<Staff>
) : Base<PoliceVehicle>(cars, policeStaff) {
    override fun canMan(vehicle: Vehicle): Boolean {
        return super.canMan(vehicle) &&
            vehicle is PoliceVehicle && (vehicle.vehicleType != VehicleType.K9_POLICE_CAR || this.dogNumber > 0)
    }

    /**
     * Return staff of the [vehicle] to the base if it is a [VehicleType.K9_POLICE_CAR] also return a dog.
     */
    override fun returnVehicle(vehicle: Vehicle) {
        super.returnVehicle(vehicle)
        if (vehicle.vehicleType == VehicleType.K9_POLICE_CAR) {
            dogNumber++
        }
    }

    private fun cantAllocate(
        needed: Int,
        badLicense: Boolean,
        badDH: Boolean,
        hasBoth: Boolean,
        badBoth: Boolean
    ): Boolean {
        val cond1 = needed == 1 && (badLicense || badDH)
        val cond2 = needed == 2 && !hasBoth && badBoth
        if (cond1 || cond2) {
            return true
        }
        return false
    }

    override fun allocateStaff(
        emergencyResponse: EmergencyResponse,
        logger: Logger,
        vehicle: PoliceVehicle,
        request: Boolean
    ): Int {
        var needed: Int = vehicle.staffCapacity
        var needsLicense: Boolean = vehicle.needsLicense
        var needsDogH: Boolean = vehicle.vehicleType == VehicleType.K9_POLICE_CAR
        if (vehicle.vehicleType == VehicleType.K9_POLICE_CAR) {
            dogNumber--
        }
        val hasBoth = policeStaff.any { it.canBeAssigned() && it.hasLicense && it.staffType == StaffType.DOG_HANDLER }
        for (staff in policeStaff.sortedBy { it.id }) {
            if (staff.canBeAssignedWorking() && needed > 0) {
                val badLicense = needsLicense && !staff.hasLicense
                val badDH = needsDogH && !(staff.staffType == StaffType.DOG_HANDLER)
                val badBoth = badLicense && badDH
                if (cantAllocate(needed, badLicense, badDH, hasBoth, badBoth)) {
                    continue
                }
                logger.staffAllocation(staff.name, staff.id, vehicle.id, emergencyResponse.emergency.id)
                needed -= 1
                needsLicense = badLicense
                needsDogH = badDH
                staff.allocatedTo = vehicle
            }
        }
        if (request) {
            return 0
        }
        return allocateStaffOnCall(emergencyResponse, logger, vehicle, needed, needsLicense, needsDogH, hasBoth)
    }

    private fun allocateStaffOnCall(
        emergencyResponse: EmergencyResponse,
        logger: Logger,
        vehicle: PoliceVehicle,
        needed2: Int,
        needsLicense2: Boolean,
        needsDogH2: Boolean,
        hasBoth2: Boolean
    ): Int {
        var needed = needed2
        var needsLicense = needsLicense2
        var needsDogH = needsDogH2
        val hasBoth = hasBoth2
        var maxTicks = 0
        for (staff in policeStaff.sortedBy { it.id }) {
            if (staff.canBeAssignedOnCall() && needed > 0) {
                val badLicense = needsLicense && !staff.hasLicense
                val badDH = needsDogH && !(staff.staffType == StaffType.DOG_HANDLER)
                val badBoth = badLicense && badDH
                if (cantAllocate(needed, badLicense, badDH, hasBoth, badBoth)) {
                    continue
                }
                logger.staffAllocation(staff.name, staff.id, vehicle.id, emergencyResponse.emergency.id)
                needed -= 1
                needsLicense = badLicense
                needsDogH = badDH
                staff.allocatedTo = vehicle
                staff.setReturningToBase()
                maxTicks = Math.max(maxTicks, staff.ticksAwayFromBase)
            }
        }
        return maxTicks
    }
}
