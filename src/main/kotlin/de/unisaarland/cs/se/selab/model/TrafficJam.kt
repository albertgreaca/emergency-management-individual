package de.unisaarland.cs.se.selab.model

import de.unisaarland.cs.se.selab.model.map.Road

/**
 * If a [TrafficJam] occurs, the given [Road] becomes jammed and, thus,
 * it takes longer to travel on it.
 *
 * @param tick the tick when the event starts
 * @param duration the duration of the event
 * @param jammedRoad the road that is jammed
 * @param factor cost of travelling on the affected road is increased by this factor
 * @param id the id of the event
 */
class TrafficJam(
    override var tick: Int,
    override val duration: Int,
    jammedRoad: Road,
    private val factor: Int,
    override val id: Int,
) : SingleRoadEvent(jammedRoad) {
    override fun weight(road: Road): Int {
        val weight = road.length * factor.toLong()
        return if (weight > Int.MAX_VALUE) Int.MAX_VALUE else weight.toInt()
    }
}
