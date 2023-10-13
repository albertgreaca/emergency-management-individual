package de.unisaarland.cs.se.selab.model.assets

/**
 * Shift types
 */
enum class ShiftType {
    EARLY,
    LATE,
    NIGHT;

    /**
     * Returns the type of the shift that follows
     * the current one
     */
    fun getNext(): ShiftType {
        if (this == EARLY) {
            return LATE
        }
        if (this == LATE) {
            return NIGHT
        }
        return EARLY
    }
}
