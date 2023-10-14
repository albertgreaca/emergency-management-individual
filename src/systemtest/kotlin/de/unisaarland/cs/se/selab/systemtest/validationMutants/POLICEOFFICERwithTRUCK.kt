package de.unisaarland.cs.se.selab.systemtest.validationMutants

import de.unisaarland.cs.se.selab.systemtest.api.SystemTest

/**
 *
 */
class POLICEOFFICERwithTRUCK : SystemTest() {
    override val name = "Police officer with truck 79"

    override val map = "mapFiles/example_map.dot"
    override val assets = "invalidAssets/POLICEOFFICERwithTRUCK.json"
    override val scenario = "scenarioJsons/example_scenario.json"
    override val maxTicks = 1
    override suspend fun run() {
        assertNextLine("Initialization Info: example_map.dot successfully parsed and validated")
        assertNextLine("Initialization Info: POLICEOFFICERwithTRUCK.json invalid")
        assertEnd()
    }
}
