package de.unisaarland.cs.se.selab.systemtest.basictests

import de.unisaarland.cs.se.selab.systemtest.api.SystemTest

/**
 * Small example test to show how to write a system test.
 * Loads valid map, assets and scenario files and checks if the output is correct.
 */
class ExampleTest : SystemTest() {
    override val name = "Example Test"

    override val map = "mapFiles/example_map.dot"
    override val assets = "assetsJsons/example_assets.json"
    override val scenario = "scenarioJsons/example_scenario.json"
    override val maxTicks = 1

    override suspend fun run() {
        // everything is parsed and validated
        assertNextLine("Initialization Info: example_map.dot successfully parsed and validated")
        assertNextLine("Initialization Info: example_assets.json successfully parsed and validated")
        assertNextLine("Initialization Info: example_scenario.json successfully parsed and validated")
        // The Simulation starts with tick 0
        assertNextLine("Simulation starts")
        assertNextLine("Simulation Tick: 0 early shift")
        // The Simulation should end
        assertNextLine("Simulation End")
        // Statistics
        assertNextLine("Simulation Statistics: 0 assets rerouted")
        assertNextLine("Simulation Statistics: 0 received emergencies")
        assertNextLine("Simulation Statistics: 0 ongoing emergencies")
        assertNextLine("Simulation Statistics: 0 failed emergencies")
        assertNextLine("Simulation Statistics: 0 resolved emergencies")
        assertNextLine("Simulation Statistics: 0 shifts worked")
        assertNextLine("Simulation Statistics: 0 ticks worked")
        // end of file is reached
        assertEnd()
    }
}
