package de.unisaarland.cs.se.selab.model

import de.unisaarland.cs.se.selab.model.map.Node
import de.unisaarland.cs.se.selab.model.map.Road
import de.unisaarland.cs.se.selab.parser.PrimaryStreetType

/**
 * Superclass for all events.
 */
interface Event {
    val id: Int
    var tick: Int
    val duration: Int
    var isDone: Boolean

    /**
     * Triggers the event.
     */
    fun trigger(simulationData: SimulationData): Boolean

    /**
     * Updates the event.
     */
    fun update(simulationData: SimulationData)
}

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

/**
 * Superclass for all events that a affect specific road.
 */
sealed class SingleRoadEvent(val road: Road) : RoadEvent() {

    override var isDone = false
    override fun trigger(simulationData: SimulationData): Boolean {
        return when (road.activeEvent) {
            is NoEvent -> {
                road.activeEvent = this
                true
            }
            this -> false
            else -> {
                tick++
                false
            }
        }
    }

    override fun update(simulationData: SimulationData) {
        if (paused) {
            tick++
            return
        }
        if (tick + duration <= simulationData.tick) {
            isDone = true
            road.activeEvent = NoEvent()
        }
    }
}

/**
 * Class representing no actual event.
 */
data class NoEvent(
    override var tick: Int = 0,
    override val duration: Int = Int.MAX_VALUE,
    override val id: Int = -1
) : RoadEvent() {
    override var isDone: Boolean = false
    override fun trigger(simulationData: SimulationData): Boolean {
        return false
    }

    override fun update(simulationData: SimulationData) {
        // nothing to do here
        Unit
    }
}

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

/**
 * This event signals that the resource with the given [id][resourceId] is unavailable
 * for some [duration]
 *
 * @param tick the tick when the event starts
 * @param duration the duration how long the resource is unavailable
 * @param resourceId id of the affected resource
 * @param id the id of the event
 */
class VehicleUnavailable(
    override var tick: Int,
    override val duration: Int,
    private val resourceId: Int,
    override val id: Int,
) : Event {

    override var isDone: Boolean = false
    override fun trigger(simulationData: SimulationData): Boolean {
        val affectedVehicle = simulationData.vehicles.find { it.id == resourceId } ?: error(
            "Vehicle for Vehicle" +
                " Unavailable event not found"
        )
        if (!affectedVehicle.broken) {
            affectedVehicle.broken = true
        }
        return if (!affectedVehicle.inMaintenance && affectedVehicle.atHome && affectedVehicle.ready) {
            affectedVehicle.inMaintenance = true
            true
        } else {
            tick++
            false
        }
    }

    override fun update(simulationData: SimulationData) {
        if (tick + duration == simulationData.tick) {
            isDone = true
            simulationData.vehicles.find { it.id == resourceId }?.inMaintenance = false
            simulationData.vehicles.find { it.id == resourceId }?.broken = false
        }
    }
}
