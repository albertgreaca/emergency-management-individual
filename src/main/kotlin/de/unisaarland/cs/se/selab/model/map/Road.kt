package de.unisaarland.cs.se.selab.model.map

import de.unisaarland.cs.se.selab.model.NoEvent
import de.unisaarland.cs.se.selab.model.RoadEvent
import de.unisaarland.cs.se.selab.model.assets.Vehicle
import de.unisaarland.cs.se.selab.parser.PrimaryStreetType

/**
 * A road between two nodes.
 */
class Road(
    val villageName: String,
    val name: String,
    val length: Int,
    val primaryType: PrimaryStreetType,
    val heightLimit: Int,
    val source: Node,
    val target: Node
) : Location {

    val events: MutableList<RoadEvent> = mutableListOf()
        get() {
            field.sortBy { it.id }
            return field
        }

    var activeEvent: RoadEvent = NoEvent()

    /**
     * Whether the road is closed at the given entry.
     */
    fun closed(entry: Node): Boolean {
        return activeEvent.closed(entry)
    }

    /**
     * The weight of the road.
     */
    fun weight(): Int {
        return activeEvent.weight(this)
    }

    /**
     * Whether the road can be used by the given vehicle.
     */
    fun canBeUsedBy(vehicle: Vehicle): Boolean {
        return vehicle.vehicleHeight <= heightLimit
    }

    /**
     * Adds an event to the road.
     */
    fun addEvent(event: RoadEvent) {
        if (activeEvent is NoEvent) {
            activeEvent = event
        } else {
            events.add(event)
        }
    }

    /**
     * Pauses the activeEvent.
     */
    fun pauseEvent() {
        activeEvent.pauseEvent()
    }

    /**
     * Resumes the activeEvent.
     */
    fun resumeEvent() {
        activeEvent.resumeEvent()
    }

    override val distancesToNodes: Map<Node, Int>
        get() = mapOf(Pair(source, 0), Pair(target, 0))

    override fun toString(): String {
        return "$villageName $name source : $source target: $target"
    }
}
