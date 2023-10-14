package de.unisaarland.cs.se.selab.systemtest.validationMutants

import de.unisaarland.cs.se.selab.systemtest.api.SystemTest

/**
 *
 */
class StaffNotMatchingPoliceStation : SystemTest() {
    override val name = "Number of staff doesn't match assigned staff in police station"

    override val map = "mapFiles/example_map.dot"
    override val assets = "invalidAssets/StaffNotMatchingPoliceStation.json"
    override val scenario = "scenarioJsons/example_scenario.json"
    override val maxTicks = 1
    override suspend fun run() {
        assertNextLine("Initialization Info: example_map.dot successfully parsed and validated")
        assertNextLine("Initialization Info: StaffNotMatchingPoliceStation.json invalid")
        assertEnd()
    }
}
