package de.unisaarland.cs.se.selab.systemtest.simulationMutants

import de.unisaarland.cs.se.selab.systemtest.api.SystemTest

/**
 *
 */
class SickCancelWay : SystemTest() {
    override val name = "sickCancelWay"

    override val map = "sickCancelWay/map.dot"
    override val assets = "sickCancelWay/assets.json"
    override val scenario = "sickCancelWay/scenario.json"
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
        assertNextLine("Emergency Assignment: 0 assigned to 0 via [4-2]")
        assertNextLine("Staff Allocation: One(1) allocated to 1 for 0")
        assertNextLine("Asset Allocation: 1 allocated to 0; 2 ticks to arrive")
        assertNextLine("Simulation Tick: 12 late shift")
        assertNextLine("Simulation Tick: 13 late shift")
        assertNextLine("Asset Arrival: 1 arrived at 2")
        assertNextLine("Emergency Handling Start: 0 handling started")
        assertNextLine("Simulation Tick: 14 late shift")
        assertNextLine("Simulation Tick: 15 late shift")
        assertNextLine("Emergency Resolved: 0 resolved")
        assertNextLine("Simulation Tick: 16 late shift")
        assertNextLine("Simulation Tick: 17 late shift")
        assertNextLine("Emergency Assignment: 1 assigned to 0 via [4-2]")
        assertNextLine("Asset Reallocation: 1 reallocated to 1")
        assertNextLine("Asset Arrival: 1 arrived at 2")
        assertNextLine("Emergency Handling Start: 1 handling started")
        assertNextLine("Simulation Tick: 18 late shift")
        assertNextLine("Emergency Resolved: 1 resolved")
        assertNextLine("Event Triggered: 1 triggered")
        assertNextLine("Staff Sick: One(1) sick for 10 ticks")
        assertNextLine("Simulation End")
        assertNextLine("Simulation Statistics: 0 assets rerouted")
        assertNextLine("Simulation Statistics: 2 received emergencies")
        assertNextLine("Simulation Statistics: 0 ongoing emergencies")
        assertNextLine("Simulation Statistics: 0 failed emergencies")
        assertNextLine("Simulation Statistics: 2 resolved emergencies")
        assertNextLine("Simulation Statistics: 2 shifts worked")
        assertNextLine("Simulation Statistics: 5 ticks worked")
    }
}
