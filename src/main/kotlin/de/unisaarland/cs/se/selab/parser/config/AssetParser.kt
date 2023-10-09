package de.unisaarland.cs.se.selab.parser.config

import de.unisaarland.cs.se.selab.model.assets.Base
import de.unisaarland.cs.se.selab.model.assets.FireStation
import de.unisaarland.cs.se.selab.model.assets.Hospital
import de.unisaarland.cs.se.selab.model.assets.PoliceStation
import de.unisaarland.cs.se.selab.model.assets.Vehicle
import de.unisaarland.cs.se.selab.model.assets.VehicleType
import de.unisaarland.cs.se.selab.model.graph.Graph
import de.unisaarland.cs.se.selab.model.map.Node
import de.unisaarland.cs.se.selab.model.map.Road
import de.unisaarland.cs.se.selab.parser.BaseType
import de.unisaarland.cs.se.selab.util.Result
import de.unisaarland.cs.se.selab.util.getSchema
import de.unisaarland.cs.se.selab.util.ifSuccess
import de.unisaarland.cs.se.selab.util.ifSuccessFlat
import org.everit.json.schema.ValidationException
import org.json.JSONObject

/**
 * Asset parser for parsing assets from a JSONString
 */
class AssetParser(val map: Graph<Node, Road>) {

    val vehicles = HashMap<Int, Vehicle>()
    private val baseMap = HashMap<Int, Base<*>>()
    private val fireStations: List<FireStation>
        get() = baseMap.values.filterIsInstance<FireStation>()
    private val hospitals: List<Hospital>
        get() = baseMap.values.filterIsInstance<Hospital>()
    private val policeStations: List<PoliceStation>
        get() = baseMap.values.filterIsInstance<PoliceStation>()

    /**
     * Parses assets from a JSONString
     */
    fun parseAssets(jsonString: String): Result<Pair<List<Vehicle>, List<Base<*>>>> {
        val schema = getSchema(this.javaClass, "assets.schema") ?: error("Could not load assets schema")

        val json = JSONObject(jsonString)
        try {
            schema.validate(json)
        } catch (e: ValidationException) {
            return Result.failure(e.toString())
        }
        val bases = json.getJSONArray(JsonKeys.BASES)
        val jsonVehicles = json.getJSONArray(JsonKeys.VEHICLES)
        var assetsValid = Result.success(Unit)
        for (vehicle in jsonVehicles) {
            if (vehicle is JSONObject) {
                assetsValid = assetsValid.ifSuccessFlat { parseVehicle(vehicle) }
            } else {
                return Result.failure("Vehicle is not a JSONObject")
            }
        }

        for (base in bases) {
            if (base is JSONObject) {
                assetsValid = assetsValid.ifSuccessFlat { parseBase(base) }
            } else {
                return Result.failure("Base is not a JSONObject")
            }
        }
        return assetsValid
            .ifSuccessFlat { validateAssets() }
            .ifSuccess { Pair(vehicles.values.toList(), baseMap.values.toList()) }
    }

    private fun validateAssets(): Result<Unit> {
        var assetsValid1 = (policeStations.isNotEmpty() && fireStations.isNotEmpty() && hospitals.isNotEmpty())
            .let { result ->
                if (result) {
                    Result.success(Unit)
                } else {
                    Result.failure("Not all base types are present")
                }
            }.ifSuccessFlat {
                vehicles.values.all { base -> base.baseID in baseMap.keys }.let { result ->
                    if (result) {
                        Result.success(Unit)
                    } else {
                        Result.failure("Not all base types are present")
                    }
                }
            }
        assetsValid1 = checkBaseVehicleAssigning(assetsValid1)
        return assetsValid1
    }

    private fun checkBaseVehicleAssigning(assetsValid1: Result<Unit>): Result<Unit> {
        var assetsValid11 = assetsValid1
        for (base in baseMap.values) {
            assetsValid11 = assetsValid11.ifSuccessFlat {
                base.vehicles.all { vehicle -> base.canMan(vehicle) }
                    .let { result ->
                        if (result) {
                            Result.success(Unit)
                        } else {
                            Result.failure("${base.id} can not man all its vehicles")
                        }
                    }
            }
            base.vehicles.forEach {
                it.home = base.location
                it.location = base.location
            }
        }
        return assetsValid11
    }

    private fun parseBase(base: JSONObject): Result<Unit> {
        if (!base.has(JsonKeys.BASE_TYPE)) {
            return Result.failure("Base has no type")
        }
        val id = base.getInt(JsonKeys.ID)
        val location = Node(base.getInt(JsonKeys.LOCATION))
        // check if location exits on map
        if (!map.vertices().contains(location)) {
            return Result.failure("Base $id is not on the map")
        }
        if (baseMap.values.any { it.location == location }) {
            return Result.failure("Base $id is on the same location as another base")
        }
        if (baseMap.contains(id)) {
            return Result.failure("Base $id is already defined")
        }
        return BaseType.create(base, vehicles.values).ifSuccess { baseMap[id] = it }
    }

    private fun parseVehicle(vehicle: JSONObject): Result<Unit> {
        val id = vehicle.getInt(JsonKeys.ID)
        if (vehicles.contains(id)) {
            return Result.failure("Vehicle $id is already defined")
        }
        return VehicleType.createFromJson(vehicle).ifSuccess { vehicles[it.id] = it }
    }
}
