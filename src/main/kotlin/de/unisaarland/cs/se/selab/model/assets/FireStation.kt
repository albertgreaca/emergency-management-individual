package de.unisaarland.cs.se.selab.model.assets

import de.unisaarland.cs.se.selab.controller.EmergencyResponse
import de.unisaarland.cs.se.selab.logger.Logger
import de.unisaarland.cs.se.selab.model.map.Node

/**
 * A fire station.
 */
data class FireStation(
    override val id: Int,
    override val location: Node,
    override var staffNumber: Int,
    val trucks: List<FireTruck>,
    val fireStaff: List<Staff>
) : Base<FireTruck>(trucks, fireStaff) {

    override fun allocateStaff(
        emergencyResponse: EmergencyResponse,
        logger: Logger,
        vehicle: FireTruck,
        request: Boolean
    ): Int {
        var needed: Int = vehicle.staffCapacity
        var needsLicense: Boolean = vehicle.needsLicense
        for (staff in fireStaff.sortedBy { it.id }) {
            if (staff.canBeAssignedWorking() && needed > 0) {
                val badLicense = needsLicense && !staff.hasLicense
                if (needed == 1 && badLicense) {
                    continue
                }
                logger.staffAllocation(staff.name, staff.id, vehicle.id, emergencyResponse.emergency.id)
                needed -= 1
                needsLicense = badLicense
                staff.allocatedTo = vehicle
                vehicle.assignedStaff.add(staff)
            }
        }
        if (request) {
            return 0
        }
        return allocateStaffOnCall(emergencyResponse, logger, vehicle, needed, needsLicense)
    }

    private fun allocateStaffOnCall(
        emergencyResponse: EmergencyResponse,
        logger: Logger,
        vehicle: FireTruck,
        needed2: Int,
        needsLicense2: Boolean,
    ): Int {
        var needed = needed2
        var needsLicense = needsLicense2
        var maxTicks = 0
        for (staff in fireStaff.sortedBy { it.id }) {
            if (staff.canBeAssignedOnCall() && needed > 0) {
                val badLicense = needsLicense && !staff.hasLicense
                if (needed == 1 && badLicense) {
                    continue
                }
                logger.staffAllocation(staff.name, staff.id, vehicle.id, emergencyResponse.emergency.id)
                needed -= 1
                needsLicense = badLicense
                staff.allocatedTo = vehicle
                vehicle.assignedStaff.add(staff)
                staff.setReturningToBase()
                maxTicks = Math.max(maxTicks, staff.ticksAwayFromBase)
            }
        }
        return maxTicks
    }
}
