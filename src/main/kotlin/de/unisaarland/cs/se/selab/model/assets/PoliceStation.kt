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
        badDH: Boolean
    ): Boolean {
        if (needed == 1 && badLicense) {
            return true
        }
        if (needed == 0 && badDH) {
            return true
        }
        return false
    }

    private fun updateNeeded(needed: Int, staff: Staff): Int {
        if (!(staff.staffType == StaffType.DOG_HANDLER)) {
            return needed - 1
        }
        return needed
    }

    override fun allocateStaff(
        emergencyResponse: EmergencyResponse,
        logger: Logger,
        vehicle: PoliceVehicle,
        ticksLimit: Int,
        request: Boolean
    ): Int {
        var needed: Int = vehicle.staffCapacity
        var needsLicense: Boolean = vehicle.needsLicense
        var needsDogH: Boolean = vehicle.vehicleType == VehicleType.K9_POLICE_CAR
        if (vehicle.vehicleType == VehicleType.K9_POLICE_CAR) {
            dogNumber--
        }
        for (staff in policeStaff.sortedBy { it.id }) {
            val ok = needed > 0 || needsDogH
            if (staff.canBeAssignedWorking() && staff.ticksAwayFromBase <= ticksLimit && ok) {
                val badLicense = needsLicense && !staff.hasLicense
                val badDH = needsDogH && !(staff.staffType == StaffType.DOG_HANDLER)
                if (cantAllocate(needed, badLicense, badDH)) {
                    continue
                }
                logger.staffAllocation(staff.name, staff.id, vehicle.id, emergencyResponse.emergency.id)
                needed = updateNeeded(needed, staff)
                needsLicense = badLicense
                needsDogH = badDH
                staff.allocatedTo = vehicle
                vehicle.assignedStaff.add(staff)
            }
        }
        if (request) {
            return 0
        }
        return allocateStaffOnCall(emergencyResponse, logger, vehicle, needed, ticksLimit, needsLicense, needsDogH)
    }

    private fun allocateStaffOnCall(
        emergencyResponse: EmergencyResponse,
        logger: Logger,
        vehicle: PoliceVehicle,
        ticksLimit: Int,
        needed2: Int,
        needsLicense2: Boolean,
        needsDogH2: Boolean
    ): Int {
        var needed = needed2
        var needsLicense = needsLicense2
        var needsDogH = needsDogH2
        var maxTicks = 0
        for (staff in policeStaff.sortedBy { it.id }) {
            val ok = needed > 0 || needsDogH
            if (staff.canBeAssignedOnCall() && staff.ticksAwayFromBase <= ticksLimit && ok) {
                val badLicense = needsLicense && !staff.hasLicense
                val badDH = needsDogH && !(staff.staffType == StaffType.DOG_HANDLER)
                if (cantAllocate(needed, badLicense, badDH)) {
                    continue
                }
                logger.staffAllocation(staff.name, staff.id, vehicle.id, emergencyResponse.emergency.id)
                needed = updateNeeded(needed, staff)
                needsLicense = badLicense
                needsDogH = badDH
                staff.allocatedTo = vehicle
                vehicle.assignedStaff.add(staff)
                staff.setReturningToBase()
                maxTicks = Math.max(maxTicks, staff.ticksAwayFromBase)
            }
        }
        return maxTicks
    }
}
