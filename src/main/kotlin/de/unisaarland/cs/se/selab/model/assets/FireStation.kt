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
    private fun cantAllocate(
        needed: Int,
        badLicense: Boolean
    ): Boolean {
        val cond1 = needed == 1 && badLicense
        if (cond1) {
            return true
        }
        return false
    }

    override fun allocateStaff(emergencyResponse: EmergencyResponse, logger: Logger, vehicle: FireTruck): Int {
        var needed: Int = vehicle.staffCapacity
        var needsLicense: Boolean = vehicle.needsLicense
        for (staff in fireStaff.sortedBy { it.id }) {
            if (staff.canBeAssignedWorking()) {
                val badLicense = needsLicense && !staff.hasLicense
                if (cantAllocate(needed, badLicense)) {
                    continue
                }
                logger.staffAllocation(staff.name, staff.id, vehicle.id, emergencyResponse.emergency.id)
                needed -= 1
                needsLicense = badLicense
                staff.allocatedTo = vehicle
            }
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
            if (staff.canBeAssignedOnCall()) {
                val badLicense = needsLicense && !staff.hasLicense
                if (cantAllocate(needed, badLicense)) {
                    continue
                }
                logger.staffAllocation(staff.name, staff.id, vehicle.id, emergencyResponse.emergency.id)
                needed -= 1
                needsLicense = badLicense
                staff.allocatedTo = vehicle
                maxTicks = Math.max(maxTicks, staff.ticksAwayFromBase)
            }
        }
        return maxTicks
    }
}
