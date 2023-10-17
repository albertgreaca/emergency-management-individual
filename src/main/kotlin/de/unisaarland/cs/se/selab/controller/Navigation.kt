package de.unisaarland.cs.se.selab.controller

import de.unisaarland.cs.se.selab.model.SimulationData
import de.unisaarland.cs.se.selab.model.assets.Base
import de.unisaarland.cs.se.selab.model.assets.FireStation
import de.unisaarland.cs.se.selab.model.assets.Hospital
import de.unisaarland.cs.se.selab.model.assets.PoliceStation
import de.unisaarland.cs.se.selab.model.assets.Vehicle
import de.unisaarland.cs.se.selab.model.graph.Path
import de.unisaarland.cs.se.selab.model.graph.algorithms.shortestPath
import de.unisaarland.cs.se.selab.model.map.DynamicRoadLocation
import de.unisaarland.cs.se.selab.model.map.Location
import de.unisaarland.cs.se.selab.model.map.Node
import de.unisaarland.cs.se.selab.model.map.Road
import de.unisaarland.cs.se.selab.model.map.Route
import de.unisaarland.cs.se.selab.util.Result
import de.unisaarland.cs.se.selab.util.Success

/**
 * Class to handle the shortest route calculations.
 */
class Navigation(val simulationData: SimulationData) {
    /**
     * Calculate the shortest route from [start] to [target] using the constraints of [vehicle] if given.
     * @param start The source [Location].
     * @param target The target [Location].
     * @param vehicle The vehicle to use for the route.
     *
     * @return The shortest [Route] from [start] to [target] using the constraints of [vehicle] if given.
     */
    fun shortestRoute(start: Location, target: Location, vehicle: Vehicle? = null): Route {
        val paths = start.drivableDistancesToNodes.filter {
            if (start is DynamicRoadLocation) {
                it.key == start.road.target ||
                    simulationData.simulationMap.getEdgeOrNUll(start.road.target, it.key) != null
            } else {
                true
            }
        }.map { (startNode, _) ->
            val path = shortestRoute(startNode, target.distancesToNodes.keys, vehicle)
            if (target is Road) {
                Route(start, DynamicRoadLocation(target, path.vertices.last() == target.source), path)
            } else {
                Route(start, target, path)
            }
        }
        return paths.sortedBy { it.path.vertices.size }.minBy { it.length }
    }

    /**
     * Returns the shortest route from [source] to [target] using the constraints of [vehicle] if given.
     * @param source The source node.
     * @param target The target nodes.
     * @param vehicle The vehicle to use for the route.
     *
     * @return The shortest [Path] from [source] to [target] using the constraints of [vehicle] if given.
     */
    private fun shortestRoute(source: Node, target: Set<Node>, vehicle: Vehicle? = null): Path<Node, Road> {
        val roadFilter = { start: Node, road: Road ->
            !road.closed(start) && vehicle?.let { road.canBeUsedBy(it) } ?: true
        }
        return simulationData.simulationMap.shortestPath(source, target, { s, t ->
            simulationData.simulationMap.getEdgeOrNUll(s, t)?.weight() ?: Int.MAX_VALUE
        }, roadFilter)
    }

    /**
     * Get the closest FireStation to a given [Location] by default searches from each base to the Location.
     * @param address The [Location] to find the closest FireStation to.
     * @param excludeBases A list of FireStations to exclude from the search.
     * @param reverse If true, search from the Location to the FireStation instead.
     *
     * @return The closest FireStation to the given [Location] or a [Result.failure] if no FireStation is available.
     */
    fun closestFireStation(address: Location, excludeBases: List<Int> = emptyList(), reverse: Boolean = false):
        Result<Pair<FireStation, Path<Node, Road>>> {
        return findClosestBase(
            address,
            simulationData.fireStations.filter { excludeBases.contains(it.id).not() },
            reverse
        )
    }

    /**
     * Get the closest PoliceStation to a given [Location] by default searches from each base to the Location.
     * @param address The [Location] to find the closest PoliceStation to.
     * @param excludeBases A list of PoliceStations to exclude from the search.
     * @param reverse If true, search from the Location to the PoliceStation instead.
     *
     * @return The closest PoliceStation to the given [Location] or a [Result.failure] if no PoliceStation is available.
     */
    fun closestPoliceStation(address: Location, excludeBases: List<Int> = emptyList(), reverse: Boolean = false):
        Result<Pair<PoliceStation, Path<Node, Road>>> {
        return findClosestBase(
            address,
            simulationData.policeStations.filter { excludeBases.contains(it.id).not() },
            reverse
        )
    }

    /**
     * Get the closest Hospital to a given [Location] by default searches from each base to the Location.
     * @param address The [Location] to find the closest Hospital to.
     * @param excludeBases A list of Hospitals to exclude from the search.
     * @param reverse If true, search from the Location to the Hospital instead.
     *
     * @return The closest Hospital to the given [Location] or a [Result.failure] if no Hospital is available.
     */
    fun closestHospital(address: Location, excludeBases: List<Int> = emptyList(), reverse: Boolean = false):
        Result<Pair<Hospital, Path<Node, Road>>> {
        return findClosestBase(address, simulationData.hospitals.filter { excludeBases.contains(it.id).not() }, reverse)
    }

    /**
     * Get the closest Base to a given [Location] by default searches from each base to the Location.
     * @param address The [Location] to find the closest Base to.
     * @param bases The list of bases to search from.
     * @param reverse If true, search from the Location to the Base instead.
     *
     * @return The closest Base to the given [Location] or a [Result.failure] if no Base is available.
     */
    private fun <T : Base<*>> findClosestBase(
        address: Location,
        bases: List<T>,
        reverse: Boolean
    ): Result<Pair<T, Path<Node, Road>>> {
        if (bases.isEmpty()) return Result.failure("No bases available")
        if (reverse && address is Node) {
            val shortestPath = shortestRoute(address, bases.map { it.location }.toSet())
            return Success(
                Pair(
                    bases.find { it.location == shortestPath.vertices.last() } ?: error("Closest base not found"),
                    shortestPath
                )
            )
        }
        val paths = bases.map { Pair(it, shortestRoute(it.location, address)) }
        val shortest = paths.minOf { it.second.length }
        val closest = paths.filter { it.second.length == shortest }.minByOrNull { it.first.id }
            ?: error("Closest base not found")
        return Success(Pair(closest.first, closest.second.path))
    }
}
