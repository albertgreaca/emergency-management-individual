package de.unisaarland.cs.se.selab.model.assets

import de.unisaarland.cs.se.selab.parser.config.JsonKeys
import de.unisaarland.cs.se.selab.util.Result
import org.json.JSONObject

/**
 * The types of vehicles.
 */
enum class VehicleType(private val specificKeys: Set<String>) {
    POLICE_CAR(setOf(JsonKeys.CRIMINAL_CAPACITY)) {
        override fun create(json: JSONObject): Result<Vehicle> {
            return Result.success(
                PoliceVehicle(
                    json.getInt(JsonKeys.ID),
                    json.getInt(JsonKeys.BASE_ID),
                    this,
                    json.getInt(JsonKeys.STAFF_CAPACITY),
                    json.getInt(JsonKeys.CRIMINAL_CAPACITY),
                    json.getInt(JsonKeys.VEHICLE_HEIGHT),
                    needsLicense = false
                )
            )
        }

        override fun createDummy(parameter: Int): Vehicle {
            return PoliceVehicle(
                -1,
                -1,
                this,
                0,
                parameter,
                0,
                needsLicense = false
            )
        }
    },
    K9_POLICE_CAR(emptySet()) {
        override fun create(json: JSONObject): Result<Vehicle> {
            return Result.success(
                PoliceVehicle(
                    json.getInt(JsonKeys.ID),
                    json.getInt(JsonKeys.BASE_ID),
                    this,
                    json.getInt(JsonKeys.STAFF_CAPACITY),
                    0,
                    json.getInt(JsonKeys.VEHICLE_HEIGHT),
                    needsLicense = false
                )
            )
        }

        override fun createDummy(parameter: Int): Vehicle {
            return PoliceVehicle(
                -1,
                -1,
                this,
                0,
                0,
                0,
                needsLicense = false
            )
        }
    },
    POLICE_MOTORCYCLE(emptySet()) {
        override fun create(json: JSONObject): Result<Vehicle> {
            return Result.success(
                PoliceVehicle(
                    json.getInt(JsonKeys.ID),
                    json.getInt(JsonKeys.BASE_ID),
                    this,
                    json.getInt(JsonKeys.STAFF_CAPACITY),
                    0,
                    json.getInt(JsonKeys.VEHICLE_HEIGHT),
                    needsLicense = true
                )
            )
        }

        override fun createDummy(parameter: Int): Vehicle {
            return PoliceVehicle(
                -1,
                -1,
                this,
                0,
                0,
                0,
                needsLicense = true
            )
        }
    },
    FIRE_TRUCK_WATER(setOf(JsonKeys.WATER_CAPACITY)) {
        override fun create(json: JSONObject): Result<Vehicle> {
            return Result.success(
                FireTruck(
                    json.getInt(JsonKeys.ID),
                    json.getInt(JsonKeys.BASE_ID),
                    this,
                    json.getInt(JsonKeys.STAFF_CAPACITY),
                    json.getInt(JsonKeys.WATER_CAPACITY),
                    0,
                    json.getInt(JsonKeys.VEHICLE_HEIGHT),
                    needsLicense = true
                )
            )
        }

        override fun createDummy(parameter: Int): Vehicle {
            return FireTruck(
                -1,
                -1,
                this,
                0,
                0,
                0,
                0,
                needsLicense = true
            )
        }
    },
    FIRE_TRUCK_TECHNICAL(emptySet()) {
        override fun create(json: JSONObject): Result<Vehicle> {
            return Result.success(
                FireTruck(
                    json.getInt(JsonKeys.ID),
                    json.getInt(JsonKeys.BASE_ID),
                    this,
                    json.getInt(JsonKeys.STAFF_CAPACITY),
                    0,
                    0,
                    json.getInt(JsonKeys.VEHICLE_HEIGHT),
                    needsLicense = true
                )
            )
        }

        override fun createDummy(parameter: Int): Vehicle {
            return FireTruck(
                -1,
                -1,
                this,
                0,
                0,
                0,
                0,
                needsLicense = true
            )
        }
    },
    FIRE_TRUCK_LADDER(setOf(JsonKeys.LADDER_LENGTH)) {
        override fun create(json: JSONObject): Result<Vehicle> {
            return Result.success(
                FireTruck(
                    json.getInt(JsonKeys.ID),
                    json.getInt(JsonKeys.BASE_ID),
                    this,
                    json.getInt(JsonKeys.STAFF_CAPACITY),
                    0,
                    json.getInt(JsonKeys.LADDER_LENGTH),
                    json.getInt(JsonKeys.VEHICLE_HEIGHT),
                    needsLicense = true
                )
            )
        }

        override fun createDummy(parameter: Int): Vehicle {
            return FireTruck(
                -1,
                -1,
                this,
                0,
                0,
                parameter,
                0,
                needsLicense = true
            )
        }
    },
    FIREFIGHTER_TRANSPORTER(emptySet()) {
        override fun create(json: JSONObject): Result<Vehicle> {
            return Result.success(
                FireTruck(
                    json.getInt(JsonKeys.ID),
                    json.getInt(JsonKeys.BASE_ID),
                    this,
                    json.getInt(JsonKeys.STAFF_CAPACITY),
                    0,
                    0,
                    json.getInt(JsonKeys.VEHICLE_HEIGHT),
                    needsLicense = false
                )
            )
        }

        override fun createDummy(parameter: Int): Vehicle {
            return FireTruck(
                -1,
                -1,
                this,
                0,
                0,
                0,
                0,
                needsLicense = false
            )
        }
    },
    AMBULANCE(emptySet()) {
        override fun create(json: JSONObject): Result<Vehicle> {
            return Result.success(
                Ambulance(
                    json.getInt(JsonKeys.ID),
                    json.getInt(JsonKeys.BASE_ID),
                    this,
                    json.getInt(JsonKeys.STAFF_CAPACITY),
                    1,
                    json.getInt(JsonKeys.VEHICLE_HEIGHT),
                    needsLicense = true
                )
            )
        }

        override fun createDummy(parameter: Int): Vehicle {
            return Ambulance(
                -1,
                -1,
                this,
                0,
                1,
                0,
                needsLicense = true
            )
        }
    },
    EMERGENCY_DOCTOR_CAR(emptySet()) {
        override fun create(json: JSONObject): Result<Vehicle> {
            return Result.success(
                Ambulance(
                    json.getInt(JsonKeys.ID),
                    json.getInt(JsonKeys.BASE_ID),
                    this,
                    json.getInt(JsonKeys.STAFF_CAPACITY),
                    0,
                    json.getInt(JsonKeys.VEHICLE_HEIGHT),
                    needsLicense = false
                )
            )
        }

        override fun createDummy(parameter: Int): Vehicle {
            return Ambulance(
                -1,
                -1,
                this,
                0,
                0,
                0,
                needsLicense = false
            )
        }
    };

    /**
     * Creates a vehicle from a JSON object
     * @param json the JSON object
     * @return the created vehicle
     */
    abstract fun create(json: JSONObject): Result<Vehicle>

    /**
     * Creates a dummy vehicle with the given parameter
     */
    abstract fun createDummy(parameter: Int): Vehicle

    /**
     * This function returns the allowed keys for the event.
     */
    fun allowedKeys(): Set<String> {
        return setOf(
            JsonKeys.VEHICLE_TYPE,
            JsonKeys.ID,
            JsonKeys.VEHICLE_HEIGHT,
            JsonKeys.STAFF_CAPACITY,
            JsonKeys.BASE_ID
        ) + specificKeys
    }

    companion object {
        /**
         * This function creates the [Vehicle] from a json object.
         */
        fun createFromJson(vehicle: JSONObject): Result<Vehicle> {
            val vehicleType = VehicleType.valueOf(vehicle.getString(JsonKeys.VEHICLE_TYPE))
            return if (vehicleType.allowedKeys() == vehicle.keySet()) {
                vehicleType.create(vehicle)
            } else {
                Result.failure(
                    "Wrong keys for the Event Type $vehicleType expected  ${vehicleType.allowedKeys().sorted()}" +
                        " got ${vehicle.keySet().sorted()}"
                )
            }
        }
    }
}
