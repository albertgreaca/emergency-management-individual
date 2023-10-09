package de.unisaarland.cs.se.selab.systemtest

import de.unisaarland.cs.se.selab.systemtest.basictests.ExampleTest
import de.unisaarland.cs.se.selab.systemtest.runner.SystemTestManager

/**
 * Register systemtests here.
 */
object SystemTestRegistration {
    fun registerSystemTestsReferenceImpl(manager: SystemTestManager) {
        manager.registerTest(ExampleTest())
    }

    fun registerSystemTestsMutantValidation(manager: SystemTestManager) {
        manager.registerTest(ExampleTest())
    }

    fun registerSystemTestsMutantSimulation(manager: SystemTestManager) {
        manager.registerTest(ExampleTest())
    }
}
