package de.unisaarland.cs.se.selab.model

import de.unisaarland.cs.se.selab.model.map.Road
import de.unisaarland.cs.se.selab.parser.config.JsonKeys
import de.unisaarland.cs.se.selab.util.Result
import org.json.JSONObject

/**
 * This enum contains all the different types of emergencies and can construct them.
 */
enum class EmergencyType {
    ACCIDENT {
        override fun fromJson(emergency: JSONObject, road: Road): Emergency {
            return AccidentEmergency(
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
            return FireEmergency(
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
            return CrimeEmergency(
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
