package de.unisaarland.cs.se.selab.model

import de.unisaarland.cs.se.selab.model.map.Node
import de.unisaarland.cs.se.selab.model.map.Road

/**
 * At a [ConstructionSite], vehicles have to drive carefully and, thus, it takes longer
 * to travel affected road segments.
 *
 * @param tick the tick when the event starts
 * @param duration the duration of the event
 * @param affectedRoad the road that is affected
 * @param factor cost of travelling on the affected road is increased by this factor
 * @param id the id of the event
 * @param blockedEntry if not null, the entry node that is blocked by the construction site
 */
class ConstructionSite(
    override var tick: Int,
    override val duration: Int,
    affectedRoad: Road,
    private val factor: Int,
    override val id: Int,
    private val blockedEntry: Node? = null,
) : SingleRoadEvent(affectedRoad) {
    override fun weight(road: Road): Int {
        val weight = road.length * factor.toLong()
        return if (weight > Int.MAX_VALUE) Int.MAX_VALUE else weight.toInt()
    }

    override fun closed(entry: Node): Boolean {
        return entry == blockedEntry
    }
}
