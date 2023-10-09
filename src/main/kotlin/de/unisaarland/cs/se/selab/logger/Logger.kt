package de.unisaarland.cs.se.selab.logger

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
    fun tick(tickNumber: Int) {
        writer.println("Simulation Tick: $tickNumber")
        writer.flush()
    }

    /**
     * Logs the emergency assignment.
     */
    fun emergency(emergencyId: Int, baseId: Int) {
        writer.println("Emergency Assignment: $emergencyId assigned to $baseId")
        numberReceivedEmergencies++
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

    private var reroutedAssets = 0
    private var reroutedAssetsAllTime = 0

    /**
     * Count the rerouted assets.
     */
    fun assetRerouted() {
        reroutedAssets++
    }

    /**
     * Logs the rerouted assets.
     */
    fun logRerouting() {
        if (reroutedAssets == 0) return
        writer.println("Assets Rerouted: $reroutedAssets")
        writer.flush()
        reroutedAssetsAllTime += reroutedAssets
        reroutedAssets = 0
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

    private val emergencyStartedLogs: MutableMap<Int, () -> Unit> = mutableMapOf()
    private val emergencyResolvedLogs: MutableMap<Int, () -> Unit> = mutableMapOf()
    private val emergencyFailedLogs: MutableMap<Int, () -> Unit> = mutableMapOf()

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
        writer.flush()
    }
}
