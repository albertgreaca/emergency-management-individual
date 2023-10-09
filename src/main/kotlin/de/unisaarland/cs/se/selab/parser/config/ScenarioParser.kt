package de.unisaarland.cs.se.selab.parser.config

import de.unisaarland.cs.se.selab.model.ScenarioData
import de.unisaarland.cs.se.selab.model.SimulationData
import de.unisaarland.cs.se.selab.model.Accident
import de.unisaarland.cs.se.selab.model.ConstructionSite
import de.unisaarland.cs.se.selab.model.Crime
import de.unisaarland.cs.se.selab.model.Emergency
import de.unisaarland.cs.se.selab.model.Event
import de.unisaarland.cs.se.selab.model.Fire
import de.unisaarland.cs.se.selab.model.MedicalEmergency
import de.unisaarland.cs.se.selab.model.VehicleUnavailable
import de.unisaarland.cs.se.selab.model.RoadClosure
import de.unisaarland.cs.se.selab.model.RushHour
import de.unisaarland.cs.se.selab.model.TrafficJam
import de.unisaarland.cs.se.selab.model.map.Node
import de.unisaarland.cs.se.selab.model.map.Road
import de.unisaarland.cs.se.selab.parser.PrimaryStreetType
import de.unisaarland.cs.se.selab.util.Result
import de.unisaarland.cs.se.selab.util.getSchema
import de.unisaarland.cs.se.selab.util.ifSuccess
import de.unisaarland.cs.se.selab.util.ifSuccessFlat
import org.everit.json.schema.ValidationException
import org.json.JSONArray
import org.json.JSONObject

private const val EDGE_NOT_FOUND_ERROR = "Given Edge does not exist"

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
                    Events.createFromJson(event, simulationData).ifSuccessFlat {
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

    /**
     * This enum contains all the different types of events and can construct them.
     */
    enum class Events(private val specificKeys: Set<String>) {
        RUSH_HOUR(setOf(JsonKeys.ROAD_TYPES, JsonKeys.FACTOR)) {
            override fun fromJson(event: JSONObject, simulationData: SimulationData): Result<Event> {
                val roadTypesJson = event.getJSONArray(JsonKeys.ROAD_TYPES).map {
                    PrimaryStreetType.valueOf(it.toString())
                }
                val roadTypes = roadTypesJson.toSet()
                if (roadTypesJson.size != roadTypes.size) {
                    return Result.failure("Road Types must be unique")
                }
                return Result.success(
                    RushHour(
                        event.getInt(JsonKeys.TICK),
                        event.getInt(JsonKeys.DURATION),
                        roadTypes,
                        event.getInt(JsonKeys.FACTOR),
                        event.getInt(JsonKeys.ID)
                    )
                )
            }
        },
        TRAFFIC_JAM(setOf(JsonKeys.FACTOR, JsonKeys.SOURCE, JsonKeys.TARGET)) {
            override fun fromJson(event: JSONObject, simulationData: SimulationData): Result<Event> {
                val source = Node(event.getInt(JsonKeys.SOURCE))
                val target = Node(event.getInt(JsonKeys.TARGET))
                val edge = simulationData.simulationMap.getEdgeOrNUll(source, target)
                    ?: simulationData.simulationMap.getEdgeOrNUll(target, source)
                    ?: return Result.failure(EDGE_NOT_FOUND_ERROR)
                return Result.success(
                    TrafficJam(
                        event.getInt(JsonKeys.TICK),
                        event.getInt(JsonKeys.DURATION),
                        edge,
                        event.getInt(JsonKeys.FACTOR),
                        event.getInt(JsonKeys.ID)
                    )
                )
            }
        },
        CONSTRUCTION_SITE(setOf(JsonKeys.ONE_WAY_STREET, JsonKeys.SOURCE, JsonKeys.TARGET, JsonKeys.FACTOR)) {

            override fun fromJson(event: JSONObject, simulationData: SimulationData): Result<Event> {
                val source = Node(event.getInt(JsonKeys.SOURCE))
                val target = Node(event.getInt(JsonKeys.TARGET))
                val sourceEdge = simulationData.simulationMap.getEdgeOrNUll(source, target)
                val targetEdge = simulationData.simulationMap.getEdgeOrNUll(target, source)
                val edge = sourceEdge ?: targetEdge ?: return Result.failure(EDGE_NOT_FOUND_ERROR)
                val alreadyOneWay = sourceEdge == null || targetEdge == null
                return Result.success(
                    ConstructionSite(
                        event.getInt(JsonKeys.TICK),
                        event.getInt(JsonKeys.DURATION),
                        edge,
                        event.getInt(JsonKeys.FACTOR),
                        event.getInt(JsonKeys.ID),
                        event.getBoolean(JsonKeys.ONE_WAY_STREET).let { if (it && !alreadyOneWay) target else null }
                    )
                )
            }
        },
        ROAD_CLOSURE(setOf(JsonKeys.SOURCE, JsonKeys.TARGET)) {
            override fun fromJson(event: JSONObject, simulationData: SimulationData): Result<Event> {
                val source = Node(event.getInt(JsonKeys.SOURCE))
                val target = Node(event.getInt(JsonKeys.TARGET))
                val edge = simulationData.simulationMap.getEdgeOrNUll(source, target)
                    ?: simulationData.simulationMap.getEdgeOrNUll(target, source)
                    ?: return Result.failure(EDGE_NOT_FOUND_ERROR)
                return Result.success(
                    RoadClosure(
                        event.getInt(JsonKeys.TICK),
                        event.getInt(JsonKeys.DURATION),
                        edge,
                        event.getInt(JsonKeys.ID)
                    )
                )
            }
        },
        VEHICLE_UNAVAILABLE(setOf(JsonKeys.VEHICLE_ID)) {
            override fun fromJson(event: JSONObject, simulationData: SimulationData): Result<Event> {
                if (simulationData.vehicles.none { it.id == event.getInt(JsonKeys.VEHICLE_ID) }) {
                    return Result.failure("Vehicle with id ${event.getInt(JsonKeys.VEHICLE_ID)} does not exist")
                }
                return Result.success(
                    VehicleUnavailable(
                        event.getInt(JsonKeys.TICK),
                        event.getInt(JsonKeys.DURATION),
                        event.getInt(JsonKeys.VEHICLE_ID),
                        event.getInt(JsonKeys.ID)
                    )
                )
            }
        };

        /**
         * This function creates the event from a json object.
         */
        abstract fun fromJson(event: JSONObject, simulationData: SimulationData): Result<Event>

        /**
         * This function returns the allowed keys for the event.
         */
        fun allowedKeys(): Set<String> {
            return setOf(JsonKeys.EVENT_TYPE, JsonKeys.ID, JsonKeys.DURATION, JsonKeys.TICK) + specificKeys
        }

        companion object {
            /**
             * This function creates the event from a json object.
             */
            fun createFromJson(event: JSONObject, simulationData: SimulationData): Result<Event> {
                val eventType = Events.valueOf(event.getString(JsonKeys.EVENT_TYPE))
                return if (eventType.allowedKeys() == event.keySet()) {
                    eventType.fromJson(event, simulationData)
                } else {
                    Result.failure(
                        "Wrong keys for the Event Type $eventType expected  ${eventType.allowedKeys().sorted()}" +
                            " got ${event.keySet().sorted()}"
                    )
                }
            }
        }
    }

    /**
     * This enum contains all the different types of emergencies and can construct them.
     */
    enum class EmergencyType {
        ACCIDENT {
            override fun fromJson(emergency: JSONObject, road: Road): Emergency {
                return Accident(
                    emergency.getInt(JsonKeys.ID),
                    emergency.getInt(JsonKeys.TICK),
                    road,
                    emergency.getInt(JsonKeys.SEVERITY),
                    emergency.getInt(JsonKeys.HANDLE_TIME),
                    emergency.getInt(JsonKeys.MAX_DURATION),
                )
            }
        },
        FIRE {
            override fun fromJson(emergency: JSONObject, road: Road): Emergency {
                return Fire(
                    emergency.getInt(JsonKeys.ID),
                    emergency.getInt(JsonKeys.TICK),
                    road,
                    emergency.getInt(JsonKeys.SEVERITY),
                    emergency.getInt(JsonKeys.HANDLE_TIME),
                    emergency.getInt(JsonKeys.MAX_DURATION),
                )
            }
        },
        MEDICAL {
            override fun fromJson(emergency: JSONObject, road: Road): Emergency {
                return MedicalEmergency(
                    emergency.getInt(JsonKeys.ID),
                    emergency.getInt(JsonKeys.TICK),
                    road,
                    emergency.getInt(JsonKeys.SEVERITY),
                    emergency.getInt(JsonKeys.HANDLE_TIME),
                    emergency.getInt(JsonKeys.MAX_DURATION),
                )
            }
        },
        CRIME {
            override fun fromJson(emergency: JSONObject, road: Road): Emergency {
                return Crime(
                    emergency.getInt(JsonKeys.ID),
                    emergency.getInt(JsonKeys.TICK),
                    road,
                    emergency.getInt(JsonKeys.SEVERITY),
                    emergency.getInt(JsonKeys.HANDLE_TIME),
                    emergency.getInt(JsonKeys.MAX_DURATION),
                )
            }
        };

        /**
         * This function creates the emergency from a json object.
         */
        abstract fun fromJson(emergency: JSONObject, road: Road): Emergency

        companion object {
            /**
             * This function creates the emergency from a json object.
             */
            fun createFormJson(emergency: JSONObject, map: SimulationData): Result<Emergency> {
                val emergencyType = EmergencyType.valueOf(emergency.getString(JsonKeys.EMERGENCY_TYPE))
                if (emergency.getInt(JsonKeys.MAX_DURATION) <= emergency.getInt(JsonKeys.HANDLE_TIME)) {
                    return Result.failure("Max Duration must be larger than Handle Time")
                }
                return map.simulationMap.edges().associateBy { Pair(it.villageName, it.name) }[
                    Pair(
                        emergency.getString(JsonKeys.VILLAGE),
                        emergency.getString(JsonKeys.ROAD_NAME)
                    )
                ].let {
                    if (it != null) {
                        Result.success(
                            emergencyType.fromJson(emergency, it)
                        )
                    } else {
                        Result.failure("Road of Emergency ${emergency.getInt(JsonKeys.ID)} does not Exist")
                    }
                }
            }
        }
    }
}
