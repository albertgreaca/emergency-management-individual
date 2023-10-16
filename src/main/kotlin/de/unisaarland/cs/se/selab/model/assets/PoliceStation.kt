package de.unisaarland.cs.se.selab.model.assets

import de.unisaarland.cs.se.selab.controller.EmergencyResponse
import de.unisaarland.cs.se.selab.logger.Logger
import de.unisaarland.cs.se.selab.model.SimulationData
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
        request: Boolean,
        simulationData: SimulationData
    ): Pair<Int, MutableList<Staff>> {
        val allocatedStaff: MutableList<Staff> = mutableListOf()
        var needed: Int = vehicle.staffCapacity
        var needsLicense: Boolean = vehicle.needsLicense
        var needsDogH: Boolean = vehicle.vehicleType == VehicleType.K9_POLICE_CAR
        if (vehicle.vehicleType == VehicleType.K9_POLICE_CAR) {
            dogNumber--
        }
        for (staff in policeStaff.sortedBy { it.id }) {
            val isdogh = staff.staffType == StaffType.DOG_HANDLER
            val ok = if (isdogh) {
                needsDogH
            } else {
                needed > 0
            }
            if (staff.canBeAssignedWorking(simulationData) && staff.ticksAwayFromBase <= ticksLimit && ok) {
                val badLicense = needsLicense && !staff.hasLicense
                val badDH = needsDogH && !isdogh
                if (cantAllocate(needed, badLicense, badDH)) {
                    continue
                }
                allocatedStaff.add(staff)
                needed = updateNeeded(needed, staff)
                needsLicense = badLicense
                needsDogH = badDH
                staff.allocatedTo = vehicle
                vehicle.assignedStaff.add(staff)
            }
        }
        if (request) {
            return Pair(0, allocatedStaff)
        }
        return allocateStaffOnCall(vehicle, needed, ticksLimit, needsLicense, needsDogH, allocatedStaff, simulationData)
    }

    private fun allocateStaffOnCall(
        vehicle: PoliceVehicle,
        needed2: Int,
        ticksLimit: Int,
        needsLicense2: Boolean,
        needsDogH2: Boolean,
        allocatedStaff: MutableList<Staff>,
        simulationData: SimulationData
    ): Pair<Int, MutableList<Staff>> {
        var needed = needed2
        var needsLicense = needsLicense2
        var needsDogH = needsDogH2
        var maxTicks = 0
        for (staff in policeStaff.sortedBy { it.id }) {
            val isdogh = staff.staffType == StaffType.DOG_HANDLER
            val ok = if (isdogh) {
                needsDogH
            } else {
                needed > 0
            }
            if (staff.canBeAssignedOnCall(simulationData) && staff.ticksAwayFromBase <= ticksLimit && ok) {
                val badLicense = needsLicense && !staff.hasLicense
                val badDH = needsDogH && !isdogh
                if (cantAllocate(needed, badLicense, badDH)) {
                    continue
                }
                allocatedStaff.add(staff)
                needed = updateNeeded(needed, staff)
                needsLicense = badLicense
                needsDogH = badDH
                staff.allocatedTo = vehicle
                vehicle.assignedStaff.add(staff)
                staff.setReturningToBase()
                maxTicks = Math.max(maxTicks, staff.ticksAwayFromBase)
            }
        }
        return Pair(maxTicks, allocatedStaff)
    }
}
