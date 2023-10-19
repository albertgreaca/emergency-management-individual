package de.unisaarland.cs.se.selab.systemtest

import de.unisaarland.cs.se.selab.systemtest.basictests.ExampleTest
import de.unisaarland.cs.se.selab.systemtest.runner.SystemTestManager
import de.unisaarland.cs.se.selab.systemtest.validationMutants.DOGHANDLERwithMOTORCYCLE
import de.unisaarland.cs.se.selab.systemtest.validationMutants.DOGHANDLERwithTRUCK
import de.unisaarland.cs.se.selab.systemtest.validationMutants.DoctorsNotMatching
import de.unisaarland.cs.se.selab.systemtest.validationMutants.DoubleShiftOnCall
import de.unisaarland.cs.se.selab.systemtest.validationMutants.EMERGENCYDOCTORwithMOTORCYCLE
import de.unisaarland.cs.se.selab.systemtest.validationMutants.EMERGENCYDOCTORwithTRUCK
import de.unisaarland.cs.se.selab.systemtest.validationMutants.EMTwithMOTORCYCLE
import de.unisaarland.cs.se.selab.systemtest.validationMutants.EMTwithTRUCK
import de.unisaarland.cs.se.selab.systemtest.validationMutants.FIREFIGHTERwithMOTORCYCLE
import de.unisaarland.cs.se.selab.systemtest.validationMutants.IdNotUnique
import de.unisaarland.cs.se.selab.systemtest.validationMutants.NoLicenseFireBase
import de.unisaarland.cs.se.selab.systemtest.validationMutants.POLICEOFFICERwithTRUCK
import de.unisaarland.cs.se.selab.systemtest.validationMutants.StaffNotMatchingFireStation
import de.unisaarland.cs.se.selab.systemtest.validationMutants.StaffNotMatchingHospital
import de.unisaarland.cs.se.selab.systemtest.validationMutants.StaffNotMatchingPoliceStation

/**
 * Register systemtests here.
 */
object SystemTestRegistration {
    fun registerSystemTestsReferenceImpl(manager: SystemTestManager) {
        manager.registerTest(HeDeservesABreak())
        registerSystemTestsMutantValidation(manager)
    }

    fun registerSystemTestsMutantValidation(manager: SystemTestManager) {
        manager.registerTest(ExampleTest())
        manager.registerTest(StaffNotMatchingFireStation())
        manager.registerTest(StaffNotMatchingPoliceStation())
        manager.registerTest(StaffNotMatchingHospital())
        manager.registerTest(DoubleShiftOnCall())
        manager.registerTest(EMTwithTRUCK())
        manager.registerTest(EMERGENCYDOCTORwithTRUCK())
        manager.registerTest(POLICEOFFICERwithTRUCK())
        manager.registerTest(DOGHANDLERwithTRUCK())
        manager.registerTest(FIREFIGHTERwithMOTORCYCLE())
        manager.registerTest(EMTwithMOTORCYCLE())
        manager.registerTest(EMERGENCYDOCTORwithMOTORCYCLE())
        manager.registerTest(DOGHANDLERwithMOTORCYCLE())
        manager.registerTest(NoLicenseFireBase())
        manager.registerTest(DoctorsNotMatching())
        manager.registerTest(IdNotUnique())
    }

    fun registerSystemTestsMutantSimulation(manager: SystemTestManager) {
        registerSystemTestsMutantValidation(manager)
    }
}
