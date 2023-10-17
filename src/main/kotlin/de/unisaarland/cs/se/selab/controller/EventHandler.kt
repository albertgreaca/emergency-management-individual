package de.unisaarland.cs.se.selab.controller

import de.unisaarland.cs.se.selab.logger.Logger
import de.unisaarland.cs.se.selab.model.Event
import de.unisaarland.cs.se.selab.model.NoEvent
import de.unisaarland.cs.se.selab.model.RoadEvent
import de.unisaarland.cs.se.selab.model.SimulationData

/**
 * Handles events.
 */
class EventHandler(val events: Collection<Event>, private val simulationData: SimulationData) {

    /**
     * List of active events sorted by id.
     */
    private val activeEvents: List<Event>
        get() = events.filter {
            simulationData.tick >= it.tick && !it.isDone
        }.sortedBy { it.id }

    /**
     * List of new events sorted by id.
     */
    private val newEvents: List<Event>
        get() = events.filter { it.tick == simulationData.tick }.sortedBy { it.id }

    /**
     * Activate events that are triggered in the current tick.
     * @param logger The logger to log to.
     * @return Whether a road event was triggered.
     */
    fun activateEvents(logger: Logger): Boolean {
        var eventStarted = false
        for (event in newEvents) {
            val triggered = event.trigger(simulationData, logger)
            if (triggered) {
                logger.eventTriggered(event.id)
                eventStarted = event is RoadEvent
            }
        }
        return eventStarted
    }

    /**
     * Update the events.
     * @param logger The logger to log to.
     * @return Whether a road event ended.
     */
    fun update(logger: Logger): Boolean {
        val activeEvents = activeEvents
        for (event in activeEvents) {
            event.update(simulationData)
        }
        for (road in simulationData.simulationMap.edges()) {
            if (road.events.isNotEmpty() && road.activeEvent == NoEvent()) {
                road.activeEvent = road.events.removeFirst()
            }
        }
        val endedEvents = activeEvents.filter { it.isDone }
        for (event in endedEvents) {
            logger.eventEnded(event.id)
        }
        return endedEvents.any { it is RoadEvent }
    }
}
