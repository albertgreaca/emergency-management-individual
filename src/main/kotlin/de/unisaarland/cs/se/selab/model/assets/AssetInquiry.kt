package de.unisaarland.cs.se.selab.model.assets

import de.unisaarland.cs.se.selab.controller.EmergencyResponse
import de.unisaarland.cs.se.selab.model.Accident
import de.unisaarland.cs.se.selab.model.Crime
import de.unisaarland.cs.se.selab.model.Emergency
import de.unisaarland.cs.se.selab.model.Fire
import de.unisaarland.cs.se.selab.model.MedicalEmergency
import kotlin.math.max

/**
 * Data Class containing the inquiries for all three emergency types.
 * @param policeInquiry The inquiry for the police.
 * @param ambulanceInquiry The inquiry for the ambulance.
 * @param fireInquiry The inquiry for the fire department.
 */
data class InquiryTriple(
    val policeInquiry: AssetInquiry,
    val ambulanceInquiry: AssetInquiry,
    val fireInquiry: AssetInquiry
)

/**
 * class representing the assets required for an emergency.
 * @param vehicles The vehicles required for the emergency.
 * @param water The amount of water required for the emergency.
 * @param patients The amount of patients required for the emergency.
 * @param criminals The amount of criminals required for the emergency.
 */
data class AssetInquiry(
    val vehicles: List<Vehicle>,
    val water: Int,
    val patients: Int,
    val criminals: Int,
) {
    /**
     * Split the inquiry into three inquiries for each emergency type.
     *
     * @return The inquiries for each emergency type.
     */
    fun split(): InquiryTriple {
        return InquiryTriple(
            AssetInquiry(vehicles.filterIsInstance<PoliceVehicle>(), 0, 0, criminals),
            AssetInquiry(vehicles.filterIsInstance<Ambulance>(), 0, patients, 0),
            AssetInquiry(vehicles.filterIsInstance<FireTruck>(), water, 0, 0)
        )
    }

    /**
     * Check if the inquiry can be still fulfilled if the given vehicle would be sent to the emergency.
     *
     * @param vehicle The vehicle to check.
     *
     * @return True if the inquiry can be fulfilled with the given vehicle.
     */
    fun isFulfillable(vehicle: Vehicle): Boolean {
        return when (vehicle.vehicleType) {
            VehicleType.FIRE_TRUCK_WATER ->
                vehicles.count { it.vehicleType == VehicleType.FIRE_TRUCK_WATER } > 1 ||
                    water - vehicle.specialCapacity <= 0

            VehicleType.POLICE_CAR ->
                vehicles.count { it.vehicleType == VehicleType.POLICE_CAR } > 1 ||
                    criminals - vehicle.specialCapacity <= 0

            else -> true
        }
    }

    /**
     * Check if the given vehicle can help with the inquiry.
     *
     * @param vehicle The vehicle to check.
     *
     * @return True if the vehicle can help with the inquiry.
     */
    fun canHelp(vehicle: Vehicle): Boolean {
        return vehicles.any { vehicle.fulfillsSpec(it) }
    }

    /**
     * Get the remaining assets based on a given list of vehicles.
     *
     * @param vehicles The vehicles which are already assigned.
     *
     * @return An inquiry for the remaining assets.
     *
     */
    fun remainingAssets(vehicles: List<Vehicle>): AssetInquiry {
        val remainingVehicles = this.vehicles.toMutableList()
        vehicles.forEach { asset ->
            remainingVehicles.remove(
                remainingVehicles.firstOrNull {
                    asset.fulfillsSpec(
                        it
                    )
                }
            )
        }
        return AssetInquiry(
            remainingVehicles,
            max(
                0,
                water - vehicles.filterIsInstance<FireTruck>()
                    .fold(0) { w, v -> w + v.specialCapacity }
            ),
            max(
                0,
                patients - vehicles.filterIsInstance<Ambulance>()
                    .fold(0) { w, v -> w + v.specialCapacity }
            ),
            max(
                0,
                criminals - vehicles.filterIsInstance<PoliceVehicle>()
                    .fold(0) { w, v -> w + v.specialCapacity }
            )
        )
    }

    /**
     * Fulfill the inquiry with the given assets.
     * And remove the used capacity from the assets.
     * @param assets The assets to fulfill the inquiry with.
     */
    fun fulfill(assets: List<Vehicle>) {
        var neededWater = water
        assets.sortedBy { it.id }.filterIsInstance<FireTruck>().forEach { neededWater = it.removeCapacity(neededWater) }
        var criminalsToArrest = criminals
        assets.sortedBy { it.id }.filterIsInstance<PoliceVehicle>()
            .forEach { criminalsToArrest = it.removeCapacity(criminalsToArrest) }
        var patientsToTransport = patients
        assets.sortedBy { it.id }.filterIsInstance<Ambulance>()
            .forEach { patientsToTransport = it.removeCapacity(patientsToTransport) }
    }

    /**
     * Check if the inquiry is fulfilled.
     */
    val isFulfilled: Boolean = vehicles.isEmpty() && water <= 0 && patients <= 0 && criminals <= 0

    /**
     * Check if the inquiry is fulfillable.
     */
    val isFulfillable: Boolean = (
        vehicles.any {
            it.vehicleType == VehicleType.FIRE_TRUCK_WATER
        } || water <= 0
        ) &&
        (vehicles.any { it.vehicleType == VehicleType.POLICE_CAR } || criminals <= 0)
}

/**
 * Get the remaining [AssetInquiry] for an [Emergency] and a [List] of [Vehicle] which are already allocated.
 *
 */
fun getNecessaryAssets(emergency: Emergency, assets: List<Vehicle>): AssetInquiry {
    return getNecessaryAssets(emergency).remainingAssets(assets)
}

/**
 * Get the necessary assets for the given [Emergency].
 */
fun getNecessaryAssets(emergency: Emergency): AssetInquiry {
    return when (emergency) {
        is Fire -> getNecessaryAssets(emergency)
        is Crime -> getNecessaryAssets(emergency)
        is MedicalEmergency -> getNecessaryAssets(emergency)
        is Accident -> getNecessaryAssets(emergency)
    }
}

private const val SEVERITY2_WATER = 3000

private const val SEVERITY3_WATER = 5400

private const val SEVERITY1_WATER = 1200

private const val SEVERITY2_LADDER_HEIGHT = 30

private const val SEVERITY3_LADDER_HEIGHT = 40

private fun getNecessaryAssets(fire: Fire): AssetInquiry {
    return when (fire.severity) {
        1 -> AssetInquiry(
            listOf(
                VehicleType.FIRE_TRUCK_WATER.createDummy(0),
                VehicleType.FIRE_TRUCK_WATER.createDummy(0)
            ),
            SEVERITY1_WATER,
            0,
            0
        )

        2 -> AssetInquiry(
            listOf(
                VehicleType.FIRE_TRUCK_WATER.createDummy(0),
                VehicleType.FIRE_TRUCK_WATER.createDummy(0),
                VehicleType.FIRE_TRUCK_WATER.createDummy(0),
                VehicleType.FIRE_TRUCK_WATER.createDummy(0),
                VehicleType.FIRE_TRUCK_LADDER.createDummy(SEVERITY2_LADDER_HEIGHT),
                VehicleType.FIREFIGHTER_TRANSPORTER.createDummy(0),
                VehicleType.AMBULANCE.createDummy(0)
            ),
            SEVERITY2_WATER,
            1,
            0
        )

        3 -> AssetInquiry(
            listOf(
                VehicleType.FIRE_TRUCK_WATER.createDummy(0),
                VehicleType.FIRE_TRUCK_WATER.createDummy(0),
                VehicleType.FIRE_TRUCK_WATER.createDummy(0),
                VehicleType.FIRE_TRUCK_WATER.createDummy(0),
                VehicleType.FIRE_TRUCK_WATER.createDummy(0),
                VehicleType.FIRE_TRUCK_WATER.createDummy(0),
                VehicleType.FIRE_TRUCK_LADDER.createDummy(SEVERITY3_LADDER_HEIGHT),
                VehicleType.FIRE_TRUCK_LADDER.createDummy(SEVERITY3_LADDER_HEIGHT),
                VehicleType.FIREFIGHTER_TRANSPORTER.createDummy(0),
                VehicleType.FIREFIGHTER_TRANSPORTER.createDummy(0),
                VehicleType.AMBULANCE.createDummy(0),
                VehicleType.AMBULANCE.createDummy(0),
                VehicleType.EMERGENCY_DOCTOR_CAR.createDummy(0)
            ),
            SEVERITY3_WATER,
            2,
            0
        )

        else -> error("Severity of fire is not in range 1-3")
    }
}

const val SEVERITY3_CRIMINALS = 8

const val SEVERITY2_CRIMINAL = 4

// get the necessary assets for a crime:
// severity 1: 1 Police Car
//              1 Criminal
// severity 2: 4 Police Cars
// 4 Criminals
// 1 K9
// 1 Ambulance
// severity 3: 6 Police Cars
// 8 Criminals
// 2 Police Motorcycles
// 2 K9
// 2 Ambulances
// 1 Patient
// 1 Firefighter Transporter
// 1 Emergency Doctor Car
private fun getNecessaryAssets(crime: Crime): AssetInquiry {
    return when (crime.severity) {
        1 -> AssetInquiry(
            listOf(VehicleType.POLICE_CAR.createDummy(0)),
            0,
            0,
            1
        )

        2 -> AssetInquiry(
            listOf(
                VehicleType.POLICE_CAR.createDummy(0),
                VehicleType.POLICE_CAR.createDummy(0),
                VehicleType.POLICE_CAR.createDummy(0),
                VehicleType.POLICE_CAR.createDummy(0),
                VehicleType.K9_POLICE_CAR.createDummy(0),
                VehicleType.AMBULANCE.createDummy(0)
            ),
            0,
            0,
            SEVERITY2_CRIMINAL
        )

        3 -> AssetInquiry(
            listOf(
                VehicleType.POLICE_CAR.createDummy(
                    0
                ),
                VehicleType.POLICE_CAR.createDummy(
                    0
                ),
                VehicleType.POLICE_CAR.createDummy(0), VehicleType.POLICE_CAR.createDummy(0),
                VehicleType.POLICE_CAR.createDummy(0), VehicleType.POLICE_CAR.createDummy(0),
                VehicleType.POLICE_MOTORCYCLE.createDummy(0),
                VehicleType.POLICE_MOTORCYCLE.createDummy(0),
                VehicleType.K9_POLICE_CAR.createDummy(0),
                VehicleType.K9_POLICE_CAR.createDummy(0),
                VehicleType.AMBULANCE.createDummy(0),
                VehicleType.AMBULANCE.createDummy(0),
                VehicleType.FIREFIGHTER_TRANSPORTER.createDummy(0)
            ),
            0,
            1,
            SEVERITY3_CRIMINALS
        )

        else -> error("Severity of crime is not in range 1-3")
    }
}

private const val SEVERITY3_PATIENTS = 5

// get the necessary assets for a medical emergency:
// severity 1: 1 Ambulance
// severity 2: 2 Ambulances
// 2 Patients
// 1 Emergency Doctor
// severity 3: 5 Ambulances
// 5 Patients
// 2 Emergency Doctors
// 2 technical Firetrucks
private fun getNecessaryAssets(medicalEmergency: MedicalEmergency): AssetInquiry {
    return when (medicalEmergency.severity) {
        1 -> AssetInquiry(
            listOf(VehicleType.AMBULANCE.createDummy(0)),
            0,
            0,
            0
        )

        2 -> AssetInquiry(
            listOf(
                VehicleType.AMBULANCE.createDummy(0),
                VehicleType.AMBULANCE.createDummy(0),
                VehicleType.EMERGENCY_DOCTOR_CAR.createDummy(0)
            ),
            0,
            2,
            0
        )

        3 -> AssetInquiry(
            listOf(
                VehicleType.AMBULANCE.createDummy(0),
                VehicleType.AMBULANCE.createDummy(0),
                VehicleType.AMBULANCE.createDummy(0), VehicleType.AMBULANCE.createDummy(0),
                VehicleType.AMBULANCE.createDummy(0),
                VehicleType.EMERGENCY_DOCTOR_CAR.createDummy(0),
                VehicleType.EMERGENCY_DOCTOR_CAR.createDummy(0),
                VehicleType.FIRE_TRUCK_TECHNICAL.createDummy(0),
                VehicleType.FIRE_TRUCK_TECHNICAL.createDummy(0)
            ),
            0,
            SEVERITY3_PATIENTS,
            0
        )

        else -> error("Severity of medical emergency is not in range 1-3")
    }
}

// get the necessary assets for an accident:
// severity 1: 1 technical Firetruck
// severity 2: 2 technical Firetrucks
// 1 Police Motorcycle
// 1 Police Car
// 1 Ambulance
// 1 Patient
// severity 3: 4 technical Firetrucks
// 2 Police Motorcycles
// 4 Police Cars
// 3 Ambulances
// 2 Patients
// 1 Emergency Doctor
private fun getNecessaryAssets(accident: Accident): AssetInquiry {
    return when (accident.severity) {
        1 -> AssetInquiry(
            listOf(VehicleType.FIRE_TRUCK_TECHNICAL.createDummy(0)),
            0,
            0,
            0
        )

        2 -> AssetInquiry(
            listOf(
                VehicleType.FIRE_TRUCK_TECHNICAL.createDummy(0),
                VehicleType.FIRE_TRUCK_TECHNICAL.createDummy(0),
                VehicleType.POLICE_MOTORCYCLE.createDummy(0),
                VehicleType.POLICE_CAR.createDummy(0),
                VehicleType.AMBULANCE.createDummy(0)
            ),
            0,
            1,
            0
        )

        3 -> AssetInquiry(
            listOf(
                VehicleType.FIRE_TRUCK_TECHNICAL.createDummy(0),
                VehicleType.FIRE_TRUCK_TECHNICAL.createDummy(0),
                VehicleType.FIRE_TRUCK_TECHNICAL.createDummy(0),
                VehicleType.FIRE_TRUCK_TECHNICAL.createDummy(0),
                VehicleType.POLICE_MOTORCYCLE.createDummy(0),
                VehicleType.POLICE_MOTORCYCLE.createDummy(0),
                VehicleType.POLICE_CAR.createDummy(0),
                VehicleType.POLICE_CAR.createDummy(0),
                VehicleType.POLICE_CAR.createDummy(0),
                VehicleType.POLICE_CAR.createDummy(0),
                VehicleType.AMBULANCE.createDummy(0),
                VehicleType.AMBULANCE.createDummy(0),
                VehicleType.AMBULANCE.createDummy(0),
                VehicleType.EMERGENCY_DOCTOR_CAR.createDummy(0)
            ),
            0,
            2,
            0
        )

        else -> error("Severity of accident is not in range 1-3")
    }
}

/**
 * This class represents a request for assets.
 */
data class AssetRequest(
    val id: Int,
    val base: Base<*>,
    val emergencyResponse: EmergencyResponse,
    val assetInquiry: AssetInquiry,
    val checkedBases: List<Int>
)
