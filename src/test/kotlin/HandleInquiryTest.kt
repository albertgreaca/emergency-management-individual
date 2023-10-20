import de.unisaarland.cs.se.selab.controller.BaseController
import de.unisaarland.cs.se.selab.controller.EmergencyResponse
import de.unisaarland.cs.se.selab.controller.Navigation
import de.unisaarland.cs.se.selab.logger.Logger
import de.unisaarland.cs.se.selab.model.AccidentEmergency
import de.unisaarland.cs.se.selab.model.SimulationData
import de.unisaarland.cs.se.selab.model.assets.Ambulance
import de.unisaarland.cs.se.selab.model.assets.AssetInquiry
import de.unisaarland.cs.se.selab.model.assets.Hospital
import de.unisaarland.cs.se.selab.model.assets.Shift
import de.unisaarland.cs.se.selab.model.assets.ShiftType
import de.unisaarland.cs.se.selab.model.assets.Staff
import de.unisaarland.cs.se.selab.model.assets.StaffType
import de.unisaarland.cs.se.selab.model.assets.VehicleType
import de.unisaarland.cs.se.selab.model.graph.GraphImpl
import de.unisaarland.cs.se.selab.model.map.Node
import de.unisaarland.cs.se.selab.model.map.Road
import de.unisaarland.cs.se.selab.parser.PrimaryStreetType
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.PrintWriter

class HandleInquiryTest {

    @Test
    fun handleInquiry() {
        val truck = Ambulance(0, 0, VehicleType.AMBULANCE, 1, 3, 0, null, needsLicense = true)
        val logger = Logger(PrintWriter(System.out))
        val staff = Staff(
            0,
            "Xulescu",
            0,
            StaffType.EMT, 0,
            currentShift = Shift(ShiftType.EARLY, true, false),
            nextShift = Shift(ShiftType.LATE, false, false),
            doubleShift = false,
            onCall = false,
            hasLicense = true
        )
        val graph = GraphImpl<Node, Road>()
        val source = Node(1)
        val target = Node(2)
        truck.location = source
        graph.addVertex(source)
        graph.addVertex(target)
        val road = Road(" ", " ", 10, PrimaryStreetType.MAIN_STREET, 10, source, target)
        graph.addEdge(source, target, road)
        graph.addEdge(target, source, road)
        val simulationData = SimulationData(
            graph,
            mutableListOf(),
            mutableListOf(),
            mutableListOf(),
            mutableListOf(),
            mutableListOf(staff),
            100
        )
        val navigation = Navigation(simulationData)
        val base = Hospital(0, Node(1), 3, 2, listOf(truck), listOf(staff))
        val baseController = BaseController(base, navigation)
        val emergency = AccidentEmergency(
            0,
            1,
            road,
            1,
            10,
            100
        )
        val emergencyResponse = EmergencyResponse(emergency, baseController)
        val assetInquiry = AssetInquiry(listOf(truck).toMutableList(), 0, 0, 0)
        baseController.handleInquiry(
            emergencyResponse,
            logger,
            listOf(truck).toMutableList(),
            assetInquiry,
            false,
            simulationData
        )
        assertTrue(staff.allocatedTo == truck)
    }
}
