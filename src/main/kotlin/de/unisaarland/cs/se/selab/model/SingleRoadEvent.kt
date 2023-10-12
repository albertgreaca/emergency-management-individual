package de.unisaarland.cs.se.selab.model

import de.unisaarland.cs.se.selab.model.map.Road

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
