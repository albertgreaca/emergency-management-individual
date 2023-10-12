package de.unisaarland.cs.se.selab.model

import de.unisaarland.cs.se.selab.model.map.Node
import de.unisaarland.cs.se.selab.model.map.Road

/**
 * A [RoadClosure] cannot be passed by any vehicle.
 *
 * @param tick the tick when the event starts
 * @param duration the duration of the event
 * @param affectedRoad the road that is closed
 * @param id the id of the event
 */
class RoadClosure(
    override var tick: Int,
    override val duration: Int,
    affectedRoad: Road,
    override val id: Int,
) : SingleRoadEvent(affectedRoad) {

    override fun closed(entry: Node): Boolean {
        return !paused
    }

    override fun pauseEvent() {
        if (!paused) {
            tick++
            paused = true
        }
    }

    override fun resumeEvent() {
        paused = false
    }
}
