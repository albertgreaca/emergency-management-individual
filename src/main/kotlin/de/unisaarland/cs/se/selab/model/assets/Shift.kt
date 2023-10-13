package de.unisaarland.cs.se.selab.model.assets

import de.unisaarland.cs.se.selab.parser.config.JsonKeys
import org.json.JSONObject

/**
 * A shift contains the type and
 * if it is a working or on call one
 */
data class Shift(
    var type: ShiftType,
    var working: Boolean,
    var onCall: Boolean
) {
    companion object {
        /**
         * creates a Shift object that corresponds to the
         * first shift according to the json object
         */
        fun createCurrentShift(json: JSONObject): Shift {
            return Shift(ShiftType.valueOf(json.getString(JsonKeys.SHIFT)), true, false)
        }

        /**
         * creates a Shift object that corresponds to the
         * second shift according to the json object
         */
        fun createNextShift(json: JSONObject): Shift {
            if (json.getBoolean(JsonKeys.DOUBLE_SHIFT)) {
                return Shift(ShiftType.valueOf(json.getString(JsonKeys.SHIFT)).getNext(), true, false)
            }
            return Shift(ShiftType.valueOf(json.getString(JsonKeys.SHIFT)).getNext(), false, true)
        }
    }
}
