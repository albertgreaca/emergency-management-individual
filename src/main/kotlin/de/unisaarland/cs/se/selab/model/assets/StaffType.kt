package de.unisaarland.cs.se.selab.model.assets

import de.unisaarland.cs.se.selab.parser.config.JsonKeys
import de.unisaarland.cs.se.selab.util.Result
import org.json.JSONObject

/**
 * Staff constructor enum
 */
enum class StaffType {
    FIREFIGHTER {
        override fun create(json: JSONObject): Result<Staff> {
            if (json.getBoolean(JsonKeys.DOUBLE_SHIFT) && json.getBoolean(JsonKeys.ON_CALL)) {
                return Result.failure("FIREFIGHTER has both double shift and on call")
            }
            if (
                json.getString(JsonKeys.DRIVING_LICENSE) == JsonKeys.MOTORCYCLE ||
                json.getString(JsonKeys.DRIVING_LICENSE) == JsonKeys.AMBULANCE
            ) {
                return Result.failure("FIREFIGHTER has wrong driving license")
            }
            return Result.success(
                Staff(
                    json.getInt(JsonKeys.ID),
                    json.getString(JsonKeys.NAME),
                    json.getInt(JsonKeys.BASE_ID),
                    this,
                    json.getInt(JsonKeys.TICKS_HOME),
                    currentShift = Shift.createCurrentShift(json),
                    nextShift = Shift.createNextShift(json),
                    doubleShift = json.getBoolean(JsonKeys.DOUBLE_SHIFT),
                    onCall = json.getBoolean(JsonKeys.ON_CALL),
                    hasLicense = json.getString(JsonKeys.DRIVING_LICENSE) != NONE
                )
            )
        }
    },
    EMT {
        override fun create(json: JSONObject): Result<Staff> {
            if (json.getBoolean(JsonKeys.DOUBLE_SHIFT) && json.getBoolean(JsonKeys.ON_CALL)) {
                return Result.failure("EMT has both double shift and on call")
            }
            if (
                json.getString(JsonKeys.DRIVING_LICENSE) == JsonKeys.TRUCK ||
                json.getString(JsonKeys.DRIVING_LICENSE) == JsonKeys.MOTORCYCLE
            ) {
                return Result.failure("EMT has wrong driving license")
            }
            return Result.success(
                Staff(
                    json.getInt(JsonKeys.ID),
                    json.getString(JsonKeys.NAME),
                    json.getInt(JsonKeys.BASE_ID),
                    this,
                    json.getInt(JsonKeys.TICKS_HOME),
                    currentShift = Shift.createCurrentShift(json),
                    nextShift = Shift.createNextShift(json),
                    doubleShift = json.getBoolean(JsonKeys.DOUBLE_SHIFT),
                    onCall = json.getBoolean(JsonKeys.ON_CALL),
                    hasLicense = json.getString(JsonKeys.DRIVING_LICENSE) != NONE
                )
            )
        }
    },
    EMERGENCY_DOCTOR {
        override fun create(json: JSONObject): Result<Staff> {
            if (json.getBoolean(JsonKeys.DOUBLE_SHIFT) && json.getBoolean(JsonKeys.ON_CALL)) {
                return Result.failure("EMERGENCY_DOCTOR has both double shift and on call")
            }
            if (
                json.getString(JsonKeys.DRIVING_LICENSE) == JsonKeys.TRUCK ||
                json.getString(JsonKeys.DRIVING_LICENSE) == JsonKeys.MOTORCYCLE ||
                json.getString(JsonKeys.DRIVING_LICENSE) == JsonKeys.AMBULANCE
            ) {
                return Result.failure("EMERGENCY_DOCTOR has wrong driving license")
            }
            return Result.success(
                Staff(
                    json.getInt(JsonKeys.ID),
                    json.getString(JsonKeys.NAME),
                    json.getInt(JsonKeys.BASE_ID),
                    this,
                    json.getInt(JsonKeys.TICKS_HOME),
                    currentShift = Shift.createCurrentShift(json),
                    nextShift = Shift.createNextShift(json),
                    doubleShift = json.getBoolean(JsonKeys.DOUBLE_SHIFT),
                    onCall = json.getBoolean(JsonKeys.ON_CALL),
                    hasLicense = json.getString(JsonKeys.DRIVING_LICENSE) != NONE
                )
            )
        }
    },
    POLICE_OFFICER {
        override fun create(json: JSONObject): Result<Staff> {
            if (json.getBoolean(JsonKeys.DOUBLE_SHIFT) && json.getBoolean(JsonKeys.ON_CALL)) {
                return Result.failure("POLICE_OFFICER has both double shift and on call")
            }
            if (
                json.getString(JsonKeys.DRIVING_LICENSE) == JsonKeys.TRUCK ||
                json.getString(JsonKeys.DRIVING_LICENSE) == JsonKeys.AMBULANCE
            ) {
                return Result.failure("POLICE_OFFICER has wrong driving license")
            }
            return Result.success(
                Staff(
                    json.getInt(JsonKeys.ID),
                    json.getString(JsonKeys.NAME),
                    json.getInt(JsonKeys.BASE_ID),
                    this,
                    json.getInt(JsonKeys.TICKS_HOME),
                    currentShift = Shift.createCurrentShift(json),
                    nextShift = Shift.createNextShift(json),
                    doubleShift = json.getBoolean(JsonKeys.DOUBLE_SHIFT),
                    onCall = json.getBoolean(JsonKeys.ON_CALL),
                    hasLicense = json.getString(JsonKeys.DRIVING_LICENSE) != NONE
                )
            )
        }
    },
    DOG_HANDLER {
        override fun create(json: JSONObject): Result<Staff> {
            if (json.getBoolean(JsonKeys.DOUBLE_SHIFT) && json.getBoolean(JsonKeys.ON_CALL)) {
                return Result.failure("DOG_HANDLER has both double shift and on call")
            }
            if (
                json.getString(JsonKeys.DRIVING_LICENSE) == JsonKeys.TRUCK ||
                json.getString(JsonKeys.DRIVING_LICENSE) == JsonKeys.MOTORCYCLE ||
                json.getString(JsonKeys.DRIVING_LICENSE) == JsonKeys.AMBULANCE
            ) {
                return Result.failure("DOG_HANDLER has wrong driving license")
            }
            return Result.success(
                Staff(
                    json.getInt(JsonKeys.ID),
                    json.getString(JsonKeys.NAME),
                    json.getInt(JsonKeys.BASE_ID),
                    this,
                    json.getInt(JsonKeys.TICKS_HOME),
                    currentShift = Shift.createCurrentShift(json),
                    nextShift = Shift.createNextShift(json),
                    doubleShift = json.getBoolean(JsonKeys.DOUBLE_SHIFT),
                    onCall = json.getBoolean(JsonKeys.ON_CALL),
                    hasLicense = json.getString(JsonKeys.DRIVING_LICENSE) != NONE
                )
            )
        }
    };

    /**
     * Creates a staff from a JSON object
     * @param json the JSON object
     * @return the created staff as Result
     */
    abstract fun create(json: JSONObject): Result<Staff>

    /**
     * Returns the allowed keys for this staff type
     */
    fun allowedKeys(): Set<String> {
        val aux: Set<String> = emptySet()
        return aux +
            JsonKeys.ID +
            JsonKeys.NAME +
            JsonKeys.BASE_ID +
            JsonKeys.TICKS_HOME +
            JsonKeys.DOUBLE_SHIFT +
            JsonKeys.SHIFT +
            JsonKeys.ON_CALL +
            JsonKeys.JOB +
            JsonKeys.DRIVING_LICENSE
    }

    companion object {
        const val NONE = "NONE"

        /**
         * Creates a staff member from a JSON object.
         */
        fun createFromJson(json: JSONObject): Result<Staff> {
            val staffType = StaffType.valueOf(json.getString(JsonKeys.JOB))
            return if (staffType.allowedKeys() == json.keySet()) {
                staffType.create(json)
            } else {
                Result.failure("Invalid keys for staff: ${json.keySet()}")
            }
        }
    }
}
