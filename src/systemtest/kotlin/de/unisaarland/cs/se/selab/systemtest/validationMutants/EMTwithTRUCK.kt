package de.unisaarland.cs.se.selab.systemtest.validationMutants

import de.unisaarland.cs.se.selab.systemtest.api.SystemTest

/**
 *
 */
class EMTwithTRUCK : SystemTest() {
    override val name = "EMT with truck 146"

    override val map = "mapFiles/example_map.dot"
    override val assets = "invalidAssets/EMTwithTRUCK.json"
    override val scenario = "scenarioJsons/example_scenario.json"
    override val maxTicks = 1
    override suspend fun run() {
        assertNextLine("Initialization Info: example_map.dot successfully parsed and validated")
        assertNextLine("Initialization Info: EMTwithTRUCK.json invalid")
        assertEnd()
    }
}
