package de.unisaarland.cs.se.selab.systemtest

import de.unisaarland.cs.se.selab.systemtest.basictests.ExampleTest
import de.unisaarland.cs.se.selab.systemtest.runner.SystemTestManager
import de.unisaarland.cs.se.selab.systemtest.simulationMutants.DiffTicksOnCall
import de.unisaarland.cs.se.selab.systemtest.simulationMutants.EarlySickness
import de.unisaarland.cs.se.selab.systemtest.simulationMutants.HeDeservesABreak
import de.unisaarland.cs.se.selab.systemtest.simulationMutants.Reroute
import de.unisaarland.cs.se.selab.systemtest.simulationMutants.ShouldVacation
import de.unisaarland.cs.se.selab.systemtest.simulationMutants.SickCancelWay
import de.unisaarland.cs.se.selab.systemtest.simulationMutants.StayHealthy
import de.unisaarland.cs.se.selab.systemtest.simulationMutants.VacationForEver
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
import de.unisaarland.cs.se.selab.systemtest.validationMutants.MinTick
import de.unisaarland.cs.se.selab.systemtest.validationMutants.NoLicenseFireBase
import de.unisaarland.cs.se.selab.systemtest.validationMutants.POLICEOFFICERwithTRUCK
import de.unisaarland.cs.se.selab.systemtest.validationMutants.StaffNotMatchingFireStation
import de.unisaarland.cs.se.selab.systemtest.validationMutants.StaffNotMatchingHospital
import de.unisaarland.cs.se.selab.systemtest.validationMutants.StaffNotMatchingPoliceStation
import de.unisaarland.cs.se.selab.systemtest.validationMutants.WhereStaff
import de.unisaarland.cs.se.selab.systemtest.validationMutants.WheresMyBase

/**
 * Register systemtests here.
 */
object SystemTestRegistration {
    fun registerSystemTestsReferenceImpl(manager: SystemTestManager) {
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
        manager.registerTest(WheresMyBase())
        manager.registerTest(MinTick())
        manager.registerTest(WhereStaff())
        manager.registerTest(HeDeservesABreak())
        manager.registerTest(DiffTicksOnCall())
        manager.registerTest(EarlySickness())
        manager.registerTest(Reroute())
        manager.registerTest(SickCancelWay())
        manager.registerTest(StayHealthy())
        manager.registerTest(ShouldVacation())
        manager.registerTest(VacationForEver())
    }

    fun registerSystemTestsMutantSimulation(manager: SystemTestManager) {
        registerSystemTestsMutantValidation(manager)
    }
}
