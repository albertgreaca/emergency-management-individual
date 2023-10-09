package de.unisaarland.cs.se.selab.model.assets

import de.unisaarland.cs.se.selab.model.Emergency
import de.unisaarland.cs.se.selab.model.map.Location
import de.unisaarland.cs.se.selab.model.map.Route

/**
 * An ambulance.
 */
data class Ambulance(
    override val id: Int,
    override val baseID: Int,
    override val vehicleType: VehicleType,
    override val staffCapacity: Int,
    val patientCapacity: Int,
    override val vehicleHeight: Int,
    override var currentEmergency: Emergency? = null,
    override var broken: Boolean = false,
    override var inMaintenance: Boolean = false,
    override var manning: Boolean = false,
    override var atTarget: Boolean = true

) : Vehicle {
    override val ready: Boolean
        get() = vehicleType != VehicleType.AMBULANCE || specialCapacity > 0
    override lateinit var home: Location
    override lateinit var location: Location
    override var specialCapacity: Int = patientCapacity

    override lateinit var currentRoute: Route

    override fun fulfillsSpec(requirements: Vehicle): Boolean {
        return requirements is Ambulance &&
            this.vehicleType == requirements.vehicleType &&
            requirements.patientCapacity <= this.patientCapacity
    }

    override fun restore() {
        if (!ready) {
            specialCapacity = patientCapacity
        }
    }
}
