package de.unisaarland.cs.se.selab.model

import de.unisaarland.cs.se.selab.model.map.Node
import de.unisaarland.cs.se.selab.parser.PrimaryStreetType
import de.unisaarland.cs.se.selab.parser.config.JsonKeys
import de.unisaarland.cs.se.selab.util.Result
import org.json.JSONObject

private const val EDGE_NOT_FOUND_ERROR = "Given Edge does not exist"

/**
 * This enum contains all the different types of events and can construct them.
 */
enum class EventType(private val specificKeys: Set<String>) {
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
    },
    VACATION(setOf(JsonKeys.STAFF_ID)) {
        override fun fromJson(event: JSONObject, simulationData: SimulationData): Result<Event> {
            if (simulationData.staff.none { it.id == event.getInt(JsonKeys.STAFF_ID) }) {
                return Result.failure("Staff with id ${event.getInt(JsonKeys.STAFF_ID)}")
            }
            return Result.success(
                VacationEvent(
                    event.getInt(JsonKeys.TICK),
                    event.getInt(JsonKeys.DURATION),
                    event.getInt(JsonKeys.STAFF_ID),
                    event.getInt(JsonKeys.ID)
                )
            )
        }
    },
    SICKNESS(setOf(JsonKeys.MINTICKS)) {
        override fun fromJson(event: JSONObject, simulationData: SimulationData): Result<Event> {
            return Result.success(
                SicknessEvent(
                    event.getInt(JsonKeys.TICK),
                    event.getInt(JsonKeys.DURATION),
                    event.getInt(JsonKeys.MINTICKS),
                    event.getInt(JsonKeys.ID)
                )
            )
        }
    }
    ;

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
            val eventType = EventType.valueOf(event.getString(JsonKeys.EVENT_TYPE))
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
