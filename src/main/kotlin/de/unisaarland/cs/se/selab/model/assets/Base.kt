package de.unisaarland.cs.se.selab.model.assets

import de.unisaarland.cs.se.selab.model.Emergency
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
                    )
        }
            .sortedBy { it.id }
    }

    /**
     * Check if the vehicle can be manned by this base.
     */
    open fun canMan(vehicle: Vehicle): Boolean {
        if (vehicle.needsLicense) {
            return vehicle.staffCapacity <= this.staffNumber && staff.any { it.hasLicense }
        }
        return vehicle.staffCapacity <= this.staffNumber
    }

    /**
     * Check if the vehicles can all be manned by this base simultaneously.
     */
    open fun canMan(vehicles: List<T>): Boolean {
        return vehicles.sumOf { it.staffCapacity } <= this.staffNumber
    }

    /**
     * Check if the base has enough special staff and reduce the number of special staff if necessary.
     */
    open fun checkSpecialStaff(vehicle: T) {
        // Do nothing by default
        Unit
    }

    /**
     * Return staff of the [vehicle] to the base.
     */
    open fun returnVehicle(vehicle: Vehicle) {
        staffNumber += vehicle.staffCapacity
    }
}
