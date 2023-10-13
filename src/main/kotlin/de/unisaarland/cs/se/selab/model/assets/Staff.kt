package de.unisaarland.cs.se.selab.model.assets

/**
 * Class to represent staff members
 */
data class Staff(
    val id: Int,
    val name: String,
    val baseID: Int,
    val staffType: StaffType,
    val ticksHome: Int,
    var ticksAwayFromBase: Int = 0,
    var currentShift: Shift,
    var nextShift: Shift,
    val doubleShift: Boolean,
    val onCall: Boolean,
    val hasLicense: Boolean,
    var allocatedTo: Vehicle? = null,
    var unavailable: Boolean = false,
    var ticksSpentAtEmergencies: Int = 0,
    var atBase: Boolean = true,
    var goingHome: Boolean = false,
    var atHome: Boolean = false,
    var returningToBase: Boolean = false
)
