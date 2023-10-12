package de.unisaarland.cs.se.selab.model

import de.unisaarland.cs.se.selab.model.map.Node
import de.unisaarland.cs.se.selab.model.map.Road

/**
 * Superclass for all events that affect a road.
 */
sealed class RoadEvent : Event {

    var paused: Boolean = false

    /**
     * Whether the road is closed at the given entry.
     */
    open fun closed(entry: Node): Boolean {
        return false
    }

    /**
     * The weight of the road during the event.
     */
    open fun weight(road: Road): Int = road.length

    /**
     * Pauses the event.
     */
    open fun pauseEvent() {}

    /**
     * Resumes the event.
     */
    open fun resumeEvent() {}
}
