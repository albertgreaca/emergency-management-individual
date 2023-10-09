package de.unisaarland.cs.se.selab.model.assets

import de.unisaarland.cs.se.selab.model.Emergency
import de.unisaarland.cs.se.selab.model.map.Node

/**
 * Superclass for the bases.
 */
sealed class Base<T : Vehicle>(val vehicles: List<T>) {
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

/**
 * A police station.
 */
data class PoliceStation(
    override val id: Int,
    override val location: Node,
    override var staffNumber: Int,
    private var dogNumber: Int,
    val cars: List<PoliceVehicle>
) : Base<PoliceVehicle>(cars) {
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

    override fun canMan(vehicles: List<PoliceVehicle>): Boolean {
        return super.canMan(vehicles) &&
            vehicles.count { it.vehicleType == VehicleType.K9_POLICE_CAR } <= this.dogNumber
    }

    override fun checkSpecialStaff(vehicle: PoliceVehicle) {
        if (vehicle.vehicleType == VehicleType.K9_POLICE_CAR) {
            dogNumber--
        }
    }
}

/**
 * A fire station.
 */
data class FireStation(
    override val id: Int,
    override val location: Node,
    override var staffNumber: Int,
    val trucks: List<FireTruck>,
) : Base<FireTruck>(trucks)

/**
 * A hospital.
 */
data class Hospital(
    override val id: Int,
    override val location: Node,
    override var staffNumber: Int,
    var doctorNumber: Int,
    val ambulances: List<Ambulance>,
) : Base<Ambulance>(ambulances) {
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

    override fun checkSpecialStaff(vehicle: Ambulance) {
        if (vehicle.vehicleType == VehicleType.EMERGENCY_DOCTOR_CAR) {
            doctorNumber--
        }
    }
}
