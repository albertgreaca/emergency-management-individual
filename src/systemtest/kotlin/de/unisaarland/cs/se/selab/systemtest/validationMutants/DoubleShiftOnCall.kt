package de.unisaarland.cs.se.selab.systemtest.validationMutants

import de.unisaarland.cs.se.selab.systemtest.api.SystemTest

/**
 *
 */
class DoubleShiftOnCall : SystemTest() {
    override val name = "Both double shift and on call"

    override val map = "mapFiles/example_map.dot"
    override val assets = "invalidAssets/DoubleShiftOnCall.json"
    override val scenario = "scenarioJsons/example_scenario.json"
    override val maxTicks = 1
    override suspend fun run() {
        assertNextLine("Initialization Info: example_map.dot successfully parsed and validated")
        assertNextLine("Initialization Info: DoubleShiftOnCall.json invalid")
        assertEnd()
    }
}
