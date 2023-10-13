package de.unisaarland.cs.se.selab.model.assets

import de.unisaarland.cs.se.selab.model.map.Node
import de.unisaarland.cs.se.selab.parser.config.JsonKeys
import de.unisaarland.cs.se.selab.util.Result
import org.json.JSONObject

/**
 * Base type enum used to parse the Bases.
 */
enum class BaseType(private val specificKeys: Set<String>) {
    POLICE_STATION(setOf(JsonKeys.DOGS)) {
        override fun create(
            json: JSONObject,
            vehicles: Collection<Vehicle>,
            staff: Collection<Staff>
        ): Result<Base<*>> {
            val baseVehicles =
                vehicles.filter { it.baseID == json.getInt(JsonKeys.ID) }
            val baseVehiclesPolice = baseVehicles.filterIsInstance<PoliceVehicle>()
            if (baseVehicles.size != baseVehiclesPolice.size) {
                return Result.failure("Not all vehicles for police station ${json.getInt(JsonKeys.ID)} are police cars")
            }
            if (baseVehicles.isEmpty()) {
                return Result.failure("No vehicles for police station ${json.getInt(JsonKeys.ID)}")
            }
            val baseStaff =
                staff.filter { it.baseID == json.getInt(JsonKeys.ID) }
            val baseStaffPolice =
                baseStaff.filter { it.staffType == StaffType.POLICE_OFFICER || it.staffType == StaffType.DOG_HANDLER }
            // TODO conditions
            if (
                baseStaffPolice.size != json.getInt(JsonKeys.STAFF) ||
                baseStaff.size != baseStaffPolice.size
            ) {
                return Result.failure(
                    "Not all staff members for police station ${json.getInt(JsonKeys.ID)} are " +
                        "police staff or number of staff doesn't match"
                )
            }
            if (
                baseStaffPolice.count { it.staffType == StaffType.DOG_HANDLER } !=
                json.getInt(JsonKeys.DOGS)
            ) {
                return Result.failure(
                    "Number of dogs doesn't match number of staff members with the job DOG_HANDLER"
                )
            }
            return Result.success(
                PoliceStation(
                    json.getInt(JsonKeys.ID),
                    Node(json.getInt(JsonKeys.LOCATION)),
                    json.getInt(JsonKeys.STAFF),
                    json.getInt(JsonKeys.DOGS),
                    baseVehiclesPolice,
                    baseStaffPolice
                )
            )
        }
    },
    HOSPITAL(setOf(JsonKeys.DOCTORS)) {
        override fun create(
            json: JSONObject,
            vehicles: Collection<Vehicle>,
            staff: Collection<Staff>
        ): Result<Base<*>> {
            val baseVehicles = vehicles.filter { it.baseID == json.getInt(JsonKeys.ID) }
            val baseVehiclesAmbulance = baseVehicles.filterIsInstance<Ambulance>()
            if (baseVehicles.isEmpty()) {
                return Result.failure("No vehicles for hospital ${json.getInt(JsonKeys.ID)}")
            }
            if (baseVehicles.size != baseVehiclesAmbulance.size) {
                return Result.failure("Not all vehicles for hospital ${json.getInt(JsonKeys.ID)} are ambulances")
            }
            val baseStaff =
                staff.filter { it.baseID == json.getInt(JsonKeys.ID) }
            val baseStaffAmbulance =
                baseStaff.filter { it.staffType == StaffType.EMT || it.staffType == StaffType.EMERGENCY_DOCTOR }
            // TODO conditions
            if (baseStaffAmbulance.size != json.getInt(JsonKeys.STAFF) ||
                baseStaff.size != baseStaffAmbulance.size
            ) {
                return Result.failure(
                    "Not all staff members for hospital ${json.getInt(JsonKeys.ID)} are " +
                        "hospital staff or number of staff doesn't match"
                )
            }
            if (
                baseStaffAmbulance.count { it.staffType == StaffType.EMERGENCY_DOCTOR } !=
                json.getInt(JsonKeys.DOCTORS)
            ) {
                return Result.failure(
                    "Number of emergency doctors doesn't match number of staff members with the job EMERGENCY_DOCTORS"
                )
            }
            return Result.success(
                Hospital(
                    json.getInt(JsonKeys.ID),
                    Node(json.getInt(JsonKeys.LOCATION)),
                    json.getInt(JsonKeys.STAFF),
                    json.getInt(JsonKeys.DOCTORS),
                    baseVehiclesAmbulance,
                    baseStaffAmbulance
                )
            )
        }
    },
    FIRE_STATION(emptySet()) {
        override fun create(
            json: JSONObject,
            vehicles: Collection<Vehicle>,
            staff: Collection<Staff>
        ): Result<Base<*>> {
            val baseVehicles = vehicles.filter { it.baseID == json.getInt(JsonKeys.ID) }
            val baseVehiclesFire = baseVehicles.filterIsInstance<FireTruck>()
            if (baseVehicles.size != baseVehiclesFire.size) {
                return Result.failure("Not all vehicles for fire station ${json.getInt(JsonKeys.ID)} are fire trucks")
            }
            if (baseVehicles.isEmpty()) {
                return Result.failure("No vehicles for fire station ${json.getInt(JsonKeys.ID)}")
            }
            val baseStaff =
                staff.filter { it.baseID == json.getInt(JsonKeys.ID) }
            val baseStaffFire =
                baseStaff.filter { it.staffType == StaffType.FIREFIGHTER }
            // TODO conditions
            if (baseStaffFire.size != json.getInt(JsonKeys.STAFF) ||
                baseStaff.size != baseStaffFire.size
            ) {
                return Result.failure(
                    "Not all staff members for fire station ${json.getInt(JsonKeys.ID)} are " +
                        "fire staff or number of staff doesn't match"
                )
            }
            return Result.success(
                FireStation(
                    json.getInt(JsonKeys.ID),
                    Node(json.getInt(JsonKeys.LOCATION)),
                    json.getInt(JsonKeys.STAFF),
                    baseVehiclesFire,
                    baseStaffFire
                )
            )
        }
    };

    /**
     * Creates a base from a JSON object.
     */
    abstract fun create(json: JSONObject, vehicles: Collection<Vehicle>, staff: Collection<Staff>): Result<Base<*>>

    /**
     * Returns the allowed keys for this base type.
     */
    fun allowedKeys(): Set<String> {
        return specificKeys + JsonKeys.ID + JsonKeys.BASE_TYPE + JsonKeys.LOCATION + JsonKeys.STAFF
    }

    companion object {
        /**
         * Creates a base from a JSON object.
         */
        fun create(json: JSONObject, vehicles: Collection<Vehicle>, staff: Collection<Staff>): Result<Base<*>> {
            val baseType = BaseType.valueOf(json.getString(JsonKeys.BASE_TYPE))
            return if (baseType.allowedKeys() == json.keySet()) {
                baseType.create(json, vehicles, staff)
            } else {
                Result.failure("Invalid keys for base: ${json.keySet()}")
            }
        }
    }
}
