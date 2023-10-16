package de.unisaarland.cs.se.selab.model.assets

import de.unisaarland.cs.se.selab.controller.EmergencyResponse
import de.unisaarland.cs.se.selab.logger.Logger
import de.unisaarland.cs.se.selab.model.SimulationData
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
        ticksLimit: Int,
        request: Boolean,
        simulationData: SimulationData
    ): Pair<Int, MutableList<Staff>> {
        val allocatedStaff: MutableList<Staff> = mutableListOf()
        var needed: Int = vehicle.staffCapacity
        var needsLicense: Boolean = vehicle.needsLicense
        for (staff in fireStaff.sortedBy { it.id }) {
            if (staff.canBeAssignedWorking(simulationData) && staff.ticksAwayFromBase <= ticksLimit && needed > 0) {
                val badLicense = needsLicense && !staff.hasLicense
                if (needed == 1 && badLicense) {
                    continue
                }
                allocatedStaff.add(staff)
                needed -= 1
                needsLicense = badLicense
                staff.allocatedTo = vehicle
                vehicle.assignedStaff.add(staff)
            }
        }
        if (request) {
            return Pair(0, allocatedStaff)
        }
        return allocateStaffOnCall(vehicle, ticksLimit, needed, needsLicense, allocatedStaff, simulationData)
    }

    private fun allocateStaffOnCall(
        vehicle: FireTruck,
        ticksLimit: Int,
        needed2: Int,
        needsLicense2: Boolean,
        allocatedStaff: MutableList<Staff>,
        simulationData: SimulationData
    ): Pair<Int, MutableList<Staff>> {
        var needed = needed2
        var needsLicense = needsLicense2
        var maxTicks = 0
        for (staff in fireStaff.sortedBy { it.id }) {
            if (staff.canBeAssignedOnCall(simulationData) && staff.ticksAwayFromBase <= ticksLimit && needed > 0) {
                val badLicense = needsLicense && !staff.hasLicense
                if (needed == 1 && badLicense) {
                    continue
                }
                allocatedStaff.add(staff)
                needed -= 1
                needsLicense = badLicense
                staff.allocatedTo = vehicle
                vehicle.assignedStaff.add(staff)
                staff.setReturningToBase()
                maxTicks = Math.max(maxTicks, staff.ticksAwayFromBase)
            }
        }
        return Pair(maxTicks, allocatedStaff)
    }
}
