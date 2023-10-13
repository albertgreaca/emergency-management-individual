package de.unisaarland.cs.se.selab

import de.unisaarland.cs.se.selab.controller.Simulation
import de.unisaarland.cs.se.selab.logger.Logger
import de.unisaarland.cs.se.selab.model.WorldDataBuilder
import de.unisaarland.cs.se.selab.model.map.MapBuilder
import de.unisaarland.cs.se.selab.parser.MapLexer
import de.unisaarland.cs.se.selab.parser.MapParser
import de.unisaarland.cs.se.selab.parser.config.AssetParser
import de.unisaarland.cs.se.selab.parser.config.ScenarioParser
import de.unisaarland.cs.se.selab.util.andThen
import de.unisaarland.cs.se.selab.util.ifSuccessFlat
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.required
import org.slf4j.LoggerFactory
import java.io.File
import java.io.PrintWriter

/**
 * Main function of the program.
 */
fun main(args: Array<String>) {
    val parser = ArgParser("SELab Project")
    val map by parser.option(
        ArgType.String,
        shortName = "m",
        fullName = "map",
        description = "Path to the map."
    ).required()
    val assets by parser.option(
        ArgType.String,
        shortName = "a",
        fullName = "assets",
        description = "Path to the file with assets."
    ).required()
    val scenario by parser.option(
        ArgType.String,
        shortName = "s",
        fullName = "scenario",
        description = "Path to the scenario file."
    ).required()
    val maxTicks by parser.option(
        ArgType.Int,
        shortName = "t",
        fullName = "ticks",
        description = "Maximum allowed number of simulation ticks."
    ).required()
    val outputHandle by parser.option(
        ArgType.String,
        shortName = "o",
        fullName = "out",
        description = "Path to output file. Uses 'stdout' by default."
    )
    parser.parse(args)
    runSimulation(outputHandle, maxTicks, map, assets, scenario)
}

private fun runSimulation(
    outputHandle: String?,
    maxTicks: Int,
    map: String,
    assets: String,
    scenario: String
) {
    val outputPrintWriter = outputHandle?.let { File(it).printWriter() } ?: PrintWriter(System.out, true)
    outputPrintWriter.use { out: PrintWriter ->
        val logger = Logger(out)
        val simulationBuilder = WorldDataBuilder()
        simulationBuilder.maxTicks = maxTicks
        // Parse Map
        logger.initialization(map) { map ->
            val lexer = MapLexer(map)
            val tokens = lexer.tokenize()
            val mapParser = MapParser(tokens)
            val parsedGraph = mapParser.parse()
            parsedGraph.ifSuccessFlat { MapBuilder.buildGraph(it) }
        }.ifSuccessFlat { map ->
            // Parse Assets
            simulationBuilder.simulationMap = map
            logger.initialization(assets) { assets ->
                AssetParser(map).parseAssets(assets)
            }
        }.ifSuccessFlat { assets ->
            // Parse Scenario
            simulationBuilder.bases = assets.third
            simulationBuilder.staff = assets.second
            simulationBuilder.vehicles = assets.first
            logger.initialization(scenario) { scenario ->
                ScenarioParser(simulationBuilder.simulationData).parse(scenario)
            }
        }.andThen({
            // Run Simulation
            Simulation(logger, simulationBuilder.simulationData, it.emergencies, it.events).init()
        }, { failure: String -> LoggerFactory.getLogger("Main").error(failure) })
    }
}
