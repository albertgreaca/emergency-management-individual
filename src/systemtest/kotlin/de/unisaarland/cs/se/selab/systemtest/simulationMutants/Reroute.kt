package de.unisaarland.cs.se.selab.systemtest.simulationMutants

import de.unisaarland.cs.se.selab.systemtest.api.SystemTest

/**
 *
 */
class Reroute : SystemTest() {
    override val name = "Reroute"

    override val map = "reroute/map.dot"
    override val assets = "reroute/assets.json"
    override val scenario = "reroute/scenario.json"
    override val maxTicks = 100
    override suspend fun run() {
        assertNextLine("Initialization Info: map.dot successfully parsed and validated")
        assertNextLine("Initialization Info: assets.json successfully parsed and validated")
        assertNextLine("Initialization Info: scenario.json successfully parsed and validated")
        assertNextLine("Simulation starts")
        assertNextLine("Simulation Tick: 0 early shift")
        assertNextLine("Simulation Tick: 1 early shift")
        assertNextLine("Simulation Tick: 2 early shift")
        assertNextLine("Emergency Assignment: 0 assigned to 0 via [4-3-1]")
        assertNextLine("Staff Allocation: One(1) allocated to 1 for 0")
        assertNextLine("Staff Allocation: OneTwo(12) allocated to 1 for 0")
        assertNextLine("Asset Allocation: 1 allocated to 0; 4 ticks to arrive")
        assertNextLine("Simulation Tick: 3 early shift")
        assertNextLine("Event Triggered: 1 triggered")
        assertNextLine("Asset Rerouted: 1 rerouted to [4-2]")
        assertNextLine("Simulation Tick: 4 early shift")
        assertNextLine("Simulation Tick: 5 early shift")
        assertNextLine("Simulation Tick: 6 early shift")
        assertNextLine("Simulation Tick: 7 early shift")
        assertNextLine("Simulation Tick: 8 early shift")
        assertNextLine("Simulation Tick: 9 early shift")
        assertNextLine("Asset Arrival: 1 arrived at 2")
        assertNextLine("Shift End: One(1) early shift ended")
        assertNextLine("Shift End: Two(2) early shift ended")
        assertNextLine("Shift End: Three(3) early shift ended")
        assertNextLine("Shift End: OneTwo(12) early shift ended")
        assertNextLine("Emergency Handling Start: 0 handling started")
        assertNextLine("Simulation Tick: 10 late shift")
        assertNextLine("Emergency Resolved: 0 resolved")
        assertNextLine("Simulation End")
        assertNextLine("Simulation Statistics: 1 assets rerouted")
        assertNextLine("Simulation Statistics: 1 received emergencies")
        assertNextLine("Simulation Statistics: 0 ongoing emergencies")
        assertNextLine("Simulation Statistics: 0 failed emergencies")
        assertNextLine("Simulation Statistics: 1 resolved emergencies")
        assertNextLine("Simulation Statistics: 4 shifts worked")
        assertNextLine("Simulation Statistics: 16 ticks worked")
    }
}
