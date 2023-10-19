package de.unisaarland.cs.se.selab.model.assets

import de.unisaarland.cs.se.selab.controller.Navigation
import de.unisaarland.cs.se.selab.logger.Logger
import de.unisaarland.cs.se.selab.model.Emergency
import de.unisaarland.cs.se.selab.model.map.Location
import de.unisaarland.cs.se.selab.model.map.Route
import de.unisaarland.cs.se.selab.model.map.TargetReached
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

const val SPEED = 10

/**
 * A vehicle.
 */
interface Vehicle {

    val ready: Boolean
    val allocatable: Boolean
        get() = ready && !broken && !inMaintenance
    val atHome: Boolean
        get() = location == home
    val id: Int
    val baseID: Int
    val vehicleType: VehicleType
    val vehicleHeight: Int
    val staffCapacity: Int
    var home: Location
    var currentEmergency: Emergency?
    var location: Location
    var broken: Boolean
    var specialCapacity: Int
    var currentRoute: Route
    var inMaintenance: Boolean
    var manning: Int
    val assignedStaff: MutableList<Staff>
    val needsLicense: Boolean
    var returnB: Boolean

    val target: Location
        get() = currentEmergency?.road ?: home

    /**
     * Checks if this vehicle fulfills the requirements of the given [Vehicle].
     */
    fun fulfillsSpec(requirements: Vehicle): Boolean
    var atTarget: Boolean

    val timeToTarget: Int
        get() = ceil(currentRoute.length.toDouble() / SPEED).toInt()

    /**
     * Removes the needed capacity from this vehicle and returns the remaining capacity.
     */
    fun removeCapacity(neededCapacity: Int): Int {
        val usedCapacity = min(specialCapacity, neededCapacity)
        specialCapacity -= usedCapacity
        return neededCapacity - usedCapacity
    }

    /**
     * Updates this Vehicle.
     * If the vehicle was allocated this tick, it will not move.
     * Otherwise, it will move along its current route if it has a route.
     * If it was at home and is not assigned to an emergency, it will restore its special capacity.
     *
     * @return True if the vehicle arrived at target.
     */
    fun update(): Boolean {
        if (manning > 0) {
            manning--
            return false
        }
        currentRoute = currentRoute.move(SPEED)
        location = currentRoute.start
        if (currentRoute is TargetReached) {
            if (!atTarget) {
                atTarget = true
                return true
            } else if (atHome) {
                restore()
            }
        }
        return false
    }

    /**
     * Handles the restoration of the specialCapacity.
     */
    fun restore()

    /**
     * returns the vehicle to its base when a staff member gets sick
     */
    fun returnToBase(navigation: Navigation, logger: Logger) {
        if (!returnB || (currentEmergency != null && requireNotNull(currentEmergency).handlingStarted)) {
            return
        }
        currentRoute = navigation.shortestRoute(location, navigation.simulationData.findBase(baseID).location, this)
        var ok = false
        for (staff in assignedStaff) {
            if (staff.isSick) {
                if (currentEmergency != null) {
                    logger.assetAllocationCanceled(id, requireNotNull(currentEmergency).id, staff.name, staff.id)
                    logger.assetReturn(id, max(1, timeToTarget))
                }
                ok = true
                break
            }
        }
        if (ok) {
            for (staff in assignedStaff) {
                logger.numberTicksWorked--
                staff.workedTicksThisShift--
                staff.lastTickWorked = false
            }
        }
        currentEmergency = null
        atTarget = false
        manning = 0
        returnB = false
    }
}
