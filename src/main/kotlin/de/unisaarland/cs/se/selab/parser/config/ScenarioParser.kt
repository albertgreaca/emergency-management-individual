package de.unisaarland.cs.se.selab.parser.config

import de.unisaarland.cs.se.selab.model.Emergency
import de.unisaarland.cs.se.selab.model.EmergencyType
import de.unisaarland.cs.se.selab.model.Event
import de.unisaarland.cs.se.selab.model.EventType
import de.unisaarland.cs.se.selab.model.ScenarioData
import de.unisaarland.cs.se.selab.model.SimulationData
import de.unisaarland.cs.se.selab.util.Result
import de.unisaarland.cs.se.selab.util.getSchema
import de.unisaarland.cs.se.selab.util.ifSuccess
import de.unisaarland.cs.se.selab.util.ifSuccessFlat
import org.everit.json.schema.ValidationException
import org.json.JSONArray
import org.json.JSONObject

/**
 * This class parses the scenario file and creates a list of events and emergencies.
 */
class ScenarioParser(private val simulationData: SimulationData) {
    private val eventMap: MutableMap<Int, Event> = mutableMapOf()
    private val emergencyMap: MutableMap<Int, Emergency> = mutableMapOf()

    /**
     * This function parses the scenario file and creates a list of events and emergencies.
     */
    fun parse(jsonString: String): Result<ScenarioData> {
        val schema = getSchema(this.javaClass, "simulation.schema") ?: error("Could not load scenario schema")

        val json = JSONObject(jsonString)
        try {
            schema.validate(json)
        } catch (e: ValidationException) {
            return Result.failure(
                e.causingExceptions.fold(
                    e.message.orEmpty()
                ) { acc: String, validationException: ValidationException -> acc + "\n" + validationException.message }
            )
        }
        val events = json.getJSONArray(JsonKeys.EVENTS)
        val emergencies = json.getJSONArray(JsonKeys.EMERGENCIES)
        var result: Result<Unit> = Result.success(Unit)
        result = result.ifSuccessFlat { parseEvents(events) }
        result = result.ifSuccessFlat { parseEmergencies(emergencies) }
        return result.ifSuccess { ScenarioData(emergencyMap.values, eventMap.values) }
    }

    private fun parseEvents(
        events: JSONArray
    ): Result<Unit> {
        var result = Result.success(Unit)
        events.forEach { event ->
            if (event is JSONObject) {
                result = result.ifSuccessFlat { _ ->
                    EventType.createFromJson(event, simulationData).ifSuccessFlat {
                        if (eventMap.containsKey(it.id)) {
                            Result.failure("Event with id ${it.id} already exists")
                        } else {
                            eventMap[it.id] = it
                            Result.success(Unit)
                        }
                    }
                }
            }
        }
        return result
    }

    private fun parseEmergencies(
        emergencies: JSONArray
    ): Result<Unit> {
        var result = Result.success(Unit)
        emergencies.forEach { emergency ->
            if (emergency is JSONObject) {
                result = result.ifSuccessFlat { EmergencyType.createFormJson(emergency, simulationData) }
                    .ifSuccessFlat {
                        if (emergencyMap.containsKey(it.id)) {
                            Result.failure("Emergency with id ${it.id} already exists")
                        } else {
                            emergencyMap[it.id] = it
                            Result.success(Unit)
                        }
                    }
            }
        }
        return result
    }
}
