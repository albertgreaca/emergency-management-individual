package de.unisaarland.cs.se.selab.model.assets

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

    override fun checkSpecialStaff(vehicle: Ambulance) {
        if (vehicle.vehicleType == VehicleType.EMERGENCY_DOCTOR_CAR) {
            doctorNumber--
        }
    }
}
