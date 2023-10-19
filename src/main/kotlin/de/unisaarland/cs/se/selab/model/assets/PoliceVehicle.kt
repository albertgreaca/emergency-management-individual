package de.unisaarland.cs.se.selab.model.assets

import de.unisaarland.cs.se.selab.model.Emergency
import de.unisaarland.cs.se.selab.model.map.Location
import de.unisaarland.cs.se.selab.model.map.Route

/**
 * A police vehicle.
 */
data class PoliceVehicle(
    override val id: Int,
    override val baseID: Int,
    override val vehicleType: VehicleType,
    override val staffCapacity: Int,
    val criminalCapacity: Int,
    override val vehicleHeight: Int,
    override var currentEmergency: Emergency? = null,
    override var broken: Boolean = false,
    override var inMaintenance: Boolean = false,
    override var manning: Int = 0,
    override var atTarget: Boolean = true,
    override var assignedStaff: MutableList<Staff> = mutableListOf(),
    override var needsLicense: Boolean,
    override var returnB: Boolean = false,
    override var arrivedThisTick: Boolean = false
) : Vehicle {
    override val ready: Boolean
        get() = vehicleType != VehicleType.POLICE_CAR ||
            (!atHome && specialCapacity > 0) ||
            (atHome && specialCapacity == criminalCapacity)
    override lateinit var home: Location
    override lateinit var location: Location
    override var specialCapacity: Int = criminalCapacity
    override lateinit var currentRoute: Route

    override fun fulfillsSpec(requirements: Vehicle): Boolean {
        return requirements is PoliceVehicle &&
            requirements.vehicleType == this.vehicleType &&
            requirements.criminalCapacity <= this.criminalCapacity
    }

    private var handlingCriminal = false
    override fun restore() {
        if (!ready) {
            handlingCriminal = !handlingCriminal
            if (!handlingCriminal) {
                specialCapacity = criminalCapacity
            }
        }
    }
}
