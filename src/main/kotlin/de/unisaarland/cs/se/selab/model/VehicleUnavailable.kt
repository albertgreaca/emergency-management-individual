package de.unisaarland.cs.se.selab.model

import de.unisaarland.cs.se.selab.logger.Logger

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
    override fun trigger(simulationData: SimulationData, logger: Logger): Boolean {
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
