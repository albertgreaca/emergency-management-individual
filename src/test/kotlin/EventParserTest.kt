import de.unisaarland.cs.se.selab.model.EventType
import de.unisaarland.cs.se.selab.model.SimulationData
import de.unisaarland.cs.se.selab.model.assets.Shift
import de.unisaarland.cs.se.selab.model.assets.ShiftType
import de.unisaarland.cs.se.selab.model.assets.Staff
import de.unisaarland.cs.se.selab.model.assets.StaffType
import de.unisaarland.cs.se.selab.model.graph.GraphImpl
import de.unisaarland.cs.se.selab.util.Failure
import de.unisaarland.cs.se.selab.util.Success
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class EventParserTest {

    @Test
    fun Vacation() {
        val jsonString = "    {\n" +
            "      \"id\": 2,\n" +
            "      \"type\": \"VACATION\",\n" +
            "      \"tick\": 10,\n" +
            "      \"duration\": 2,\n" +
            "      \"staffID\": 2\n" +
            "    }"
        val staff = Staff(
            2,
            "Xulescu",
            0,
            StaffType.POLICE_OFFICER,
            3,
            currentShift = Shift(ShiftType.EARLY, true, false),
            nextShift = Shift(ShiftType.LATE, false, false),
            doubleShift = false,
            onCall = false,
            hasLicense = false,
            ticksAwayFromBase = 0
        )
        val simulationData = SimulationData(
            GraphImpl(),
            mutableListOf(),
            mutableListOf(),
            mutableListOf(),
            mutableListOf(),
            listOf(staff).toMutableList(),
            100
        )
        val json = JSONObject(jsonString)
        assertTrue(EventType.createFromJson(json, simulationData) is Success)
        staff.id = 1
        assertTrue(EventType.createFromJson(json, simulationData) is Failure)
    }

    @Test
    fun Sickness() {
        val jsonString = "    {\n" +
            "      \"id\": 2,\n" +
            "      \"type\": \"SICKNESS\",\n" +
            "      \"tick\": 10,\n" +
            "      \"duration\": 2,\n" +
            "      \"minTicks\": 2\n" +
            "    }"
        val staff = Staff(
            2,
            "Xulescu",
            0,
            StaffType.POLICE_OFFICER,
            3,
            currentShift = Shift(ShiftType.EARLY, true, false),
            nextShift = Shift(ShiftType.LATE, false, false),
            doubleShift = false,
            onCall = false,
            hasLicense = false,
            ticksAwayFromBase = 0
        )
        val simulationData = SimulationData(
            GraphImpl(),
            mutableListOf(),
            mutableListOf(),
            mutableListOf(),
            mutableListOf(),
            listOf(staff).toMutableList(),
            100
        )
        val json = JSONObject(jsonString)
        assertTrue(EventType.createFromJson(json, simulationData) is Success)
        val jsonString2 = "    {\n" +
            "      \"id\": 2,\n" +
            "      \"type\": \"SICKNESS\",\n" +
            "      \"tick\": 10,\n" +
            "      \"duration\": 2,\n" +
            "      \"minTicks\": 15\n" +
            "    }"
        val json2 = JSONObject(jsonString2)
        assertTrue(EventType.createFromJson(json2, simulationData) is Failure)
    }
}
