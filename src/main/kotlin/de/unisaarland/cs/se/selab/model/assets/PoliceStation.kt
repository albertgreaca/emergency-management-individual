package de.unisaarland.cs.se.selab.model.assets

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
