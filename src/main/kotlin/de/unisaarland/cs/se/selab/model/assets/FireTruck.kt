package de.unisaarland.cs.se.selab.model.assets

import de.unisaarland.cs.se.selab.model.Emergency
import de.unisaarland.cs.se.selab.model.map.Location
import de.unisaarland.cs.se.selab.model.map.Route

const val REFILL_RATE = 300

/**
 * A fire truck.
 */
data class FireTruck(
    override val id: Int,
    override val baseID: Int,
    override val vehicleType: VehicleType,
    override val staffCapacity: Int,
    val waterCapacity: Int,
    val ladderLength: Int,
    override val vehicleHeight: Int,
    override var currentEmergency: Emergency? = null,
    override var broken: Boolean = false,
    override var inMaintenance: Boolean = false,
    override var manning: Int = 0,
    override var atTarget: Boolean = true,
    override var assignedStaff: MutableList<Staff> = mutableListOf(),
    override var needsLicense: Boolean
) : Vehicle {
    override val ready: Boolean
        get() = vehicleType != VehicleType.FIRE_TRUCK_WATER ||
            (!atHome && specialCapacity > 0) ||
            (atHome && specialCapacity == waterCapacity)

    override lateinit var home: Location
    override lateinit var location: Location
    override var specialCapacity: Int = waterCapacity
    override lateinit var currentRoute: Route

    override fun fulfillsSpec(requirements: Vehicle): Boolean {
        return requirements is FireTruck &&
            requirements.vehicleType == this.vehicleType &&
            requirements.waterCapacity <= this.waterCapacity &&
            requirements.ladderLength <= this.ladderLength
    }

    override fun restore() {
        if (specialCapacity < waterCapacity) {
            specialCapacity = minOf(waterCapacity, specialCapacity + REFILL_RATE)
        }
    }
}
