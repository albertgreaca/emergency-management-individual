package de.unisaarland.cs.se.selab.systemtest.simulationMutants

import de.unisaarland.cs.se.selab.systemtest.api.SystemTest

/**
 *
 */
class ShouldVacation : SystemTest() {
    override val name = "shouldVacation"

    override val map = "shouldVacation/map.dot"
    override val assets = "shouldVacation/assets.json"
    override val scenario = "shouldVacation/scenario.json"
    override val maxTicks = 100
    override suspend fun run() {
        assertNextLine("Initialization Info: map.dot successfully parsed and validated")
        assertNextLine("Initialization Info: assets.json successfully parsed and validated")
        assertNextLine("Initialization Info: scenario.json successfully parsed and validated")
        assertNextLine("Simulation starts")
        assertNextLine("Simulation Tick: 0 early shift")
        assertNextLine("Simulation Tick: 1 early shift")
        assertNextLine("Simulation Tick: 2 early shift")
        assertNextLine("Simulation Tick: 3 early shift")
        assertNextLine("Simulation Tick: 4 early shift")
        assertNextLine("Simulation Tick: 5 early shift")
        assertNextLine("Simulation Tick: 6 early shift")
        assertNextLine("Simulation Tick: 7 early shift")
        assertNextLine("Simulation Tick: 8 early shift")
        assertNextLine("Simulation Tick: 9 early shift")
        assertNextLine("Shift End: Two(2) early shift ended")
        assertNextLine("Shift End: Three(3) early shift ended")
        assertNextLine("Shift Start: One(1) late shift will start")
        assertNextLine("Simulation Tick: 10 late shift")
        assertNextLine("Simulation Tick: 11 late shift")
        assertNextLine("Event Triggered: 1 triggered")
        assertNextLine("Simulation Tick: 12 late shift")
        assertNextLine("Emergency Assignment: 0 assigned to 0 via [4-2]")
        assertNextLine("Simulation Tick: 13 late shift")
        assertNextLine("Simulation Tick: 14 late shift")
        assertNextLine("Simulation Tick: 15 late shift")
        assertNextLine("Event Ended: 1 ended")
        assertNextLine("Simulation Tick: 16 late shift")
        assertNextLine("Staff Allocation: One(1) allocated to 1 for 0")
        assertNextLine("Asset Allocation: 1 allocated to 0; 1 ticks to arrive")
        assertNextLine("Simulation Tick: 17 late shift")
        assertNextLine("Asset Arrival: 1 arrived at 2")
        assertNextLine("Emergency Handling Start: 0 handling started")
        assertNextLine("Simulation Tick: 18 late shift")
        assertNextLine("Simulation Tick: 19 late shift")
        assertNextLine("Shift End: One(1) late shift ended")
        assertNextLine("Simulation Tick: 20 night shift")
        assertNextLine("Simulation Tick: 21 night shift")
        assertNextLine("Emergency Resolved: 0 resolved")
        assertNextLine("Simulation End")
        assertNextLine("Simulation Statistics: 0 assets rerouted")
        assertNextLine("Simulation Statistics: 1 received emergencies")
        assertNextLine("Simulation Statistics: 0 ongoing emergencies")
        assertNextLine("Simulation Statistics: 0 failed emergencies")
        assertNextLine("Simulation Statistics: 1 resolved emergencies")
        assertNextLine("Simulation Statistics: 2 shifts worked")
        assertNextLine("Simulation Statistics: 5 ticks worked")
    }
}
