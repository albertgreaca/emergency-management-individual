package de.unisaarland.cs.se.selab.model

import de.unisaarland.cs.se.selab.model.map.Road
import de.unisaarland.cs.se.selab.parser.PrimaryStreetType

/**
 * During [RushHour], all roads of the given [roadTypes] are affected by heavy traffic
 * and, thus, it takes longer to travel them.
 *
 * @param tick the tick when the event starts
 * @param duration the duration of the event
 * @param roadTypes the types of roads that are affected
 * @param factor cost of travelling on the affected roads is increased by this factor
 * @param id the id of the event
 */
class RushHour(
    override var tick: Int,
    override val duration: Int,
    private val roadTypes: Set<PrimaryStreetType>,
    private val factor: Int,
    override val id: Int
) : RoadEvent() {
    override fun weight(road: Road): Int {
        return if (road.primaryType in roadTypes) {
            val weight = road.length * factor.toLong()
            if (weight > Int.MAX_VALUE) Int.MAX_VALUE else weight.toInt()
        } else {
            road.length
        }
    }

    override var isDone: Boolean = false

    override fun trigger(simulationData: SimulationData): Boolean {
        val affectedRoads = simulationData.simulationMap.edges().filter { road ->
            road.primaryType in roadTypes
        }
        return if (affectedRoads.any { road -> road.activeEvent == NoEvent() }) {
            affectedRoads.forEach {
                it.addEvent(this)
            }
            true
        } else {
            tick++
            false
        }
    }

    override fun update(simulationData: SimulationData) {
        if (tick + duration == simulationData.tick) {
            isDone = true
            val affectedRoads = simulationData.simulationMap.edges().filter { road ->
                road.primaryType in roadTypes
            }
            affectedRoads.forEach {
                if (it.activeEvent == this) {
                    it.activeEvent = NoEvent()
                } else {
                    it.events.remove(this)
                }
            }
        }
    }
}
