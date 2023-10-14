package de.unisaarland.cs.se.selab.logger

import de.unisaarland.cs.se.selab.model.assets.ShiftType
import de.unisaarland.cs.se.selab.model.graph.Path
import de.unisaarland.cs.se.selab.model.map.Node
import de.unisaarland.cs.se.selab.model.map.Road
import de.unisaarland.cs.se.selab.util.Result
import de.unisaarland.cs.se.selab.util.andThen
import java.io.File
import java.io.FileReader
import java.io.PrintWriter

/**
 * Logger for the simulation.
 */
class Logger(private val writer: PrintWriter) {

    private var numberReceivedEmergencies: Int = 0
    private var numberFailedEmergencies: Int = 0
    private var numberResolvedEmergencies: Int = 0
    private val numberShiftsWorked: Int = 0
    private val numberTicksWorked: Int = 0

    /**
     * Initializes a resource and logs the result.
     */
    fun <T> initialization(filename: String, initFunction: (file: String) -> Result<T>): Result<T> {
        val configReader = FileReader(File(filename))
        val config = configReader.readText()
        configReader.close()
        val result = initFunction(config)
        result.andThen({
            writer.println("Initialization Info: $filename successfully parsed and validated")
            writer.flush()
            it
        }) { _ ->
            writer.println("Initialization Info: $filename invalid")
            writer.flush()
        }
        return result
    }

    /**
     * Logs the start of the simulation.
     */
    fun start() {
        writer.println("Simulation starts")
        writer.flush()
    }

    /**
     * Logs the end of the simulation.
     */
    fun tick(tickNumber: Int, shiftType: ShiftType) {
        writer.println("Simulation Tick: $tickNumber $shiftType shift")
        writer.flush()
    }

    /**
     * Logs the emergency assignment.
     */
    fun emergency(emergencyId: Int, baseId: Int, shortestPath: Path<Node, Road>) {
        writer.println("Emergency Assignment: $emergencyId assigned to $baseId via $shortestPath")
        numberReceivedEmergencies++
        writer.flush()
    }

    /**
     * Logs the staff allocation.
     */
    fun staffAllocation(staffName: String, staffId: Int, assetId: Int, emergencyId: Int) {
        writer.println("Staff Allocation: $staffName($staffId) allocated to $assetId for $emergencyId")
        writer.flush()
    }

    /**
     * Logs the asset allocation.
     */
    fun allocation(assetId: Int, emergencyId: Int, ticksToArrive: Int) {
        writer.println(
            "Asset Allocation: $assetId allocated to $emergencyId; $ticksToArrive " +
                "ticks to arrive"
        )
        writer.flush()
    }

    /**
     * Logs the asset reallocation.
     */
    fun reallocation(assetId: Int, emergencyId: Int) {
        writer.println("Asset Reallocation: $assetId reallocated to $emergencyId")
        writer.flush()
    }

    /**
     * Logs the asset requests.
     */
    fun assetRequest(requestId: Int, baseId: Int, emergencyId: Int) {
        writer.println("Asset Request: $requestId sent to $baseId for $emergencyId")
        writer.flush()
    }

    /**
     * Logs the asset request failure.
     */
    fun requestFailed(emergencyId: Int) {
        writer.println("Request Failed: $emergencyId failed")
        writer.flush()
    }

    /**
     * Logs the asset arrival.
     */
    fun assetArrival(assetId: Int, vertexId: Int) {
        writer.println("Asset Arrival: $assetId arrived at $vertexId")
        writer.flush()
    }

    /**
     * Logs the shift end
     */
    fun shiftEnd(staffName: String, staffId: Int, shiftType: ShiftType) {
        writer.println("Shift End: $staffName($staffId) $shiftType shift ended")
        writer.flush()
    }

    /**
     * Logs the shift start
     */
    fun shiftStart(staffName: String, staffId: Int, shiftType: ShiftType) {
        writer.println("Shift Start: $staffName($staffId) $shiftType shift will start")
        writer.flush()
    }

    /**
     * Logs staff on-call
     */
    fun staffOnCall(staffName: String, staffId: Int) {
        writer.println("Staff On-Call: $staffName($staffId) on-call")
        writer.flush()
    }

    /**
     * Logs staff not on-call
     */
    fun staffNotOnCall(staffName: String, staffId: Int) {
        writer.println("Staff Not On-Call: $staffName($staffId) not on-call anymore")
        writer.flush()
    }

    /**
     * Logs the staff return
     */
    fun staffReturn(staffName: String, staffId: Int) {
        writer.println("Staff Return: $staffName($staffId) returned to base")
        writer.flush()
    }

    /**
     * Logs the emergency handling start.
     */
    fun emergencyHandlingStart(emergencyId: Int) {
        emergencyStartedLogs[emergencyId] = {
            writer.println("Emergency Handling Start: $emergencyId handling started")
        }
    }

    /**
     * Logs the emergency handling end success.
     */
    fun emergencyResolved(emergencyId: Int) {
        emergencyResolvedLogs[emergencyId] = { writer.println("Emergency Resolved: $emergencyId resolved") }
        numberResolvedEmergencies++
    }

    /**
     * Logs the emergency handling end failure.
     */
    fun emergencyFailed(emergencyId: Int) {
        emergencyFailedLogs[emergencyId] = { writer.println("Emergency Failed: $emergencyId failed") }
        numberFailedEmergencies++
    }

    /**
     * Add event to event end log.
     */
    fun eventEnded(eventId: Int) {
        writer.println("Event Ended: $eventId ended")
    }

    /**
     * Logs the event triggered.
     */
    fun eventTriggered(eventId: Int) {
        writer.println("Event Triggered: $eventId triggered")
        writer.flush()
    }

    /**
     * Logs the sick staff.
     */
    fun staffSick(staffName: String, staffId: Int, ticksSick: Int) {
        writer.println("Staff Sick: $staffName($staffId) sick for $ticksSick ticks")
        writer.flush()
    }

    /**
     * Logs the available staff
     */
    fun staffAvailable(staffName: String, staffId: Int) {
        writer.println("Staff Available: $staffName($staffId) available again")
        writer.flush()
    }

    /**
     * Logs canceled assets
     */
    fun assetAllocationCanceled(assetId: Int, emergencyId: Int, staffName: String, staffId: Int) {
        writer.println(
            "Asset Allocation Canceled: $assetId allocated to $emergencyId " +
                "canceled because $staffName($staffId) became sick"
        )
        writer.flush()
    }

    /**
     * Logs the asset return
     */
    fun assetReturn(assetId: Int, ticksToArrive: Int) {
        writer.println("Asset Return: $assetId returns to base; $ticksToArrive ticks to arrive")
        writer.flush()
    }

    private var reroutedAssetsAllTime = 0

    /**
     * Count the rerouted assets.
     *
     * fun assetRerouted() {
     * reroutedAssets++
     */

    /**
     * Logs the rerouted assets.
     */
    fun assetRerouted(assetId: Int, shortestPath: Path<Node, Road>) {
        writer.println("Asset Rerouted: $assetId rerouted to $shortestPath")
        writer.flush()
        reroutedAssetsAllTime++
    }

    private val emergencyStartedLogs: MutableMap<Int, () -> Unit> = mutableMapOf()
    private val emergencyResolvedLogs: MutableMap<Int, () -> Unit> = mutableMapOf()
    private val emergencyFailedLogs: MutableMap<Int, () -> Unit> = mutableMapOf()

    /**
     * Logs the emergency handling.
     */
    fun logEmergencies() {
        emergencyStartedLogs.toSortedMap().forEach { it.value() }
        emergencyResolvedLogs.toSortedMap().forEach { it.value() }
        emergencyFailedLogs.toSortedMap().forEach { it.value() }
        emergencyStartedLogs.clear()
        emergencyResolvedLogs.clear()
        emergencyFailedLogs.clear()
    }

    /**
     * Logs the simulation end.
     */
    fun end() {
        writer.println("Simulation End")
        writer.println("Simulation Statistics: $reroutedAssetsAllTime assets rerouted")
        writer.println("Simulation Statistics: $numberReceivedEmergencies received emergencies")
        writer.println(
            "Simulation Statistics: " +
                "${numberReceivedEmergencies - numberFailedEmergencies - numberResolvedEmergencies}" +
                " ongoing emergencies"
        )
        writer.println("Simulation Statistics: $numberFailedEmergencies failed emergencies")
        writer.println("Simulation Statistics: $numberResolvedEmergencies resolved emergencies")
        writer.println("Simulation Statistics: $numberShiftsWorked shifts worked")
        writer.println("Simulation Statistics: $numberTicksWorked ticks worked")
        // TODO shifts and ticks worked
        writer.flush()
    }
}
