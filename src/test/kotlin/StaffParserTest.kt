import de.unisaarland.cs.se.selab.model.assets.StaffType
import de.unisaarland.cs.se.selab.util.Failure
import de.unisaarland.cs.se.selab.util.Success
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class StaffParserTest {
    @Test
    fun FireFighter() {
        val jsonString = "{" +
            "      \"id\": 1,\n" +
            "      \"name\": \"One\",\n" +
            "      \"baseID\": 0,\n" +
            "      \"ticksHome\": 3,\n" +
            "      \"shift\": \"EARLY\",\n" +
            "      \"doubleShift\": false,\n" +
            "      \"onCall\": false,\n" +
            "      \"job\": \"FIREFIGHTER\",\n" +
            "      \"drivingLicense\": \"TRUCK\"\n" +
            "}"
        val json = JSONObject(jsonString)
        assertTrue(StaffType.createFromJson(json) is Success)
        val jsonString2 = "{" +
            "      \"id\": 1,\n" +
            "      \"name\": \"One\",\n" +
            "      \"baseID\": 0,\n" +
            "      \"ticksHome\": 3,\n" +
            "      \"shift\": \"EARLY\",\n" +
            "      \"doubleShift\": false,\n" +
            "      \"onCall\": false,\n" +
            "      \"job\": \"FIREFIGHTER\",\n" +
            "      \"drivingLicense\": \"NONE\"\n" +
            "}"
        val json2 = JSONObject(jsonString2)
        assertTrue(StaffType.createFromJson(json2) is Success)
        val jsonString3 = "{" +
            "      \"id\": 1,\n" +
            "      \"name\": \"One\",\n" +
            "      \"baseID\": 0,\n" +
            "      \"ticksHome\": 3,\n" +
            "      \"shift\": \"EARLY\",\n" +
            "      \"doubleShift\": true,\n" +
            "      \"onCall\": true,\n" +
            "      \"job\": \"FIREFIGHTER\",\n" +
            "      \"drivingLicense\": \"NONE\"\n" +
            "}"
        val json3 = JSONObject(jsonString3)
        assertTrue(StaffType.createFromJson(json3) is Failure)
        val jsonString4 = "{" +
            "      \"id\": 1,\n" +
            "      \"name\": \"One\",\n" +
            "      \"baseID\": 0,\n" +
            "      \"ticksHome\": 3,\n" +
            "      \"shift\": \"EARLY\",\n" +
            "      \"doubleShift\": true,\n" +
            "      \"onCall\": false,\n" +
            "      \"job\": \"FIREFIGHTER\",\n" +
            "      \"drivingLicense\": \"MOTORCYCLE\"\n" +
            "}"
        val json4 = JSONObject(jsonString4)
        assertTrue(StaffType.createFromJson(json4) is Failure)
    }

    @Test
    fun EMT() {
        val jsonString = "{" +
            "      \"id\": 1,\n" +
            "      \"name\": \"One\",\n" +
            "      \"baseID\": 0,\n" +
            "      \"ticksHome\": 3,\n" +
            "      \"shift\": \"EARLY\",\n" +
            "      \"doubleShift\": false,\n" +
            "      \"onCall\": false,\n" +
            "      \"job\": \"EMT\",\n" +
            "      \"drivingLicense\": \"AMBULANCE\"\n" +
            "}"
        val json = JSONObject(jsonString)
        assertTrue(StaffType.createFromJson(json) is Success)
        val jsonString2 = "{" +
            "      \"id\": 1,\n" +
            "      \"name\": \"One\",\n" +
            "      \"baseID\": 0,\n" +
            "      \"ticksHome\": 3,\n" +
            "      \"shift\": \"EARLY\",\n" +
            "      \"doubleShift\": false,\n" +
            "      \"onCall\": false,\n" +
            "      \"job\": \"EMT\",\n" +
            "      \"drivingLicense\": \"NONE\"\n" +
            "}"
        val json2 = JSONObject(jsonString2)
        StaffType.createFromJson(json2)
        val jsonString3 = "{" +
            "      \"id\": 1,\n" +
            "      \"name\": \"One\",\n" +
            "      \"baseID\": 0,\n" +
            "      \"ticksHome\": 3,\n" +
            "      \"shift\": \"EARLY\",\n" +
            "      \"doubleShift\": true,\n" +
            "      \"onCall\": true,\n" +
            "      \"job\": \"EMT\",\n" +
            "      \"drivingLicense\": \"NONE\"\n" +
            "}"
        val json3 = JSONObject(jsonString3)
        assertTrue(StaffType.createFromJson(json3) is Failure)
        val jsonString4 = "{" +
            "      \"id\": 1,\n" +
            "      \"name\": \"One\",\n" +
            "      \"baseID\": 0,\n" +
            "      \"ticksHome\": 3,\n" +
            "      \"shift\": \"EARLY\",\n" +
            "      \"doubleShift\": true,\n" +
            "      \"onCall\": false,\n" +
            "      \"job\": \"EMT\",\n" +
            "      \"drivingLicense\": \"TRUCK\"\n" +
            "}"
        val json4 = JSONObject(jsonString4)
        assertTrue(StaffType.createFromJson(json4) is Failure)
    }

    @Test
    fun EmergencyDoctor() {
        val jsonString = "{" +
            "      \"id\": 1,\n" +
            "      \"name\": \"One\",\n" +
            "      \"baseID\": 0,\n" +
            "      \"ticksHome\": 3,\n" +
            "      \"shift\": \"EARLY\",\n" +
            "      \"doubleShift\": false,\n" +
            "      \"onCall\": false,\n" +
            "      \"job\": \"EMERGENCY_DOCTOR\",\n" +
            "      \"drivingLicense\": \"NONE\"\n" +
            "}"
        val json = JSONObject(jsonString)
        assertTrue(StaffType.createFromJson(json) is Success)
        val jsonString2 = "{" +
            "      \"id\": 1,\n" +
            "      \"name\": \"One\",\n" +
            "      \"baseID\": 0,\n" +
            "      \"ticksHome\": 3,\n" +
            "      \"shift\": \"EARLY\",\n" +
            "      \"doubleShift\": false,\n" +
            "      \"onCall\": false,\n" +
            "      \"job\": \"EMERGENCY_DOCTOR\",\n" +
            "      \"drivingLicense\": \"NONE\"\n" +
            "}"
        val json2 = JSONObject(jsonString2)
        assertTrue(StaffType.createFromJson(json2) is Success)
        val jsonString3 = "{" +
            "      \"id\": 1,\n" +
            "      \"name\": \"One\",\n" +
            "      \"baseID\": 0,\n" +
            "      \"ticksHome\": 3,\n" +
            "      \"shift\": \"EARLY\",\n" +
            "      \"doubleShift\": true,\n" +
            "      \"onCall\": true,\n" +
            "      \"job\": \"EMERGENCY_DOCTOR\",\n" +
            "      \"drivingLicense\": \"NONE\"\n" +
            "}"
        val json3 = JSONObject(jsonString3)
        assertTrue(StaffType.createFromJson(json3) is Failure)
        val jsonString4 = "{" +
            "      \"id\": 1,\n" +
            "      \"name\": \"One\",\n" +
            "      \"baseID\": 0,\n" +
            "      \"ticksHome\": 3,\n" +
            "      \"shift\": \"EARLY\",\n" +
            "      \"doubleShift\": true,\n" +
            "      \"onCall\": false,\n" +
            "      \"job\": \"EMERGENCY_DOCTOR\",\n" +
            "      \"drivingLicense\": \"TRUCK\"\n" +
            "}"
        val json4 = JSONObject(jsonString4)
        assertTrue(StaffType.createFromJson(json4) is Failure)
    }

    @Test
    fun PoliceOfficer() {
        val jsonString = "{" +
            "      \"id\": 1,\n" +
            "      \"name\": \"One\",\n" +
            "      \"baseID\": 0,\n" +
            "      \"ticksHome\": 3,\n" +
            "      \"shift\": \"EARLY\",\n" +
            "      \"doubleShift\": false,\n" +
            "      \"onCall\": false,\n" +
            "      \"job\": \"POLICE_OFFICER\",\n" +
            "      \"drivingLicense\": \"MOTORCYCLE\"\n" +
            "}"
        val json = JSONObject(jsonString)
        assertTrue(StaffType.createFromJson(json) is Success)
        val jsonString2 = "{" +
            "      \"id\": 1,\n" +
            "      \"name\": \"One\",\n" +
            "      \"baseID\": 0,\n" +
            "      \"ticksHome\": 3,\n" +
            "      \"shift\": \"EARLY\",\n" +
            "      \"doubleShift\": false,\n" +
            "      \"onCall\": false,\n" +
            "      \"job\": \"POLICE_OFFICER\",\n" +
            "      \"drivingLicense\": \"NONE\"\n" +
            "}"
        val json2 = JSONObject(jsonString2)
        assertTrue(StaffType.createFromJson(json2) is Success)
        val jsonString3 = "{" +
            "      \"id\": 1,\n" +
            "      \"name\": \"One\",\n" +
            "      \"baseID\": 0,\n" +
            "      \"ticksHome\": 3,\n" +
            "      \"shift\": \"EARLY\",\n" +
            "      \"doubleShift\": true,\n" +
            "      \"onCall\": true,\n" +
            "      \"job\": \"POLICE_OFFICER\",\n" +
            "      \"drivingLicense\": \"NONE\"\n" +
            "}"
        val json3 = JSONObject(jsonString3)
        assertTrue(StaffType.createFromJson(json3) is Failure)
        val jsonString4 = "{" +
            "      \"id\": 1,\n" +
            "      \"name\": \"One\",\n" +
            "      \"baseID\": 0,\n" +
            "      \"ticksHome\": 3,\n" +
            "      \"shift\": \"EARLY\",\n" +
            "      \"doubleShift\": true,\n" +
            "      \"onCall\": false,\n" +
            "      \"job\": \"POLICE_OFFICER\",\n" +
            "      \"drivingLicense\": \"TRUCK\"\n" +
            "}"
        val json4 = JSONObject(jsonString4)
        assertTrue(StaffType.createFromJson(json4) is Failure)
    }

    @Test
    fun DogHandler() {
        val jsonString = "{" +
            "      \"id\": 1,\n" +
            "      \"name\": \"One\",\n" +
            "      \"baseID\": 0,\n" +
            "      \"ticksHome\": 3,\n" +
            "      \"shift\": \"EARLY\",\n" +
            "      \"doubleShift\": false,\n" +
            "      \"onCall\": false,\n" +
            "      \"job\": \"DOG_HANDLER\",\n" +
            "      \"drivingLicense\": \"NONE\"\n" +
            "}"
        val json = JSONObject(jsonString)
        assertTrue(StaffType.createFromJson(json) is Success)
        val jsonString2 = "{" +
            "      \"id\": 1,\n" +
            "      \"name\": \"One\",\n" +
            "      \"baseID\": 0,\n" +
            "      \"ticksHome\": 3,\n" +
            "      \"shift\": \"EARLY\",\n" +
            "      \"doubleShift\": false,\n" +
            "      \"onCall\": false,\n" +
            "      \"job\": \"DOG_HANDLER\",\n" +
            "      \"drivingLicense\": \"NONE\"\n" +
            "}"
        val json2 = JSONObject(jsonString2)
        assertTrue(StaffType.createFromJson(json2) is Success)
        val jsonString3 = "{" +
            "      \"id\": 1,\n" +
            "      \"name\": \"One\",\n" +
            "      \"baseID\": 0,\n" +
            "      \"ticksHome\": 3,\n" +
            "      \"shift\": \"EARLY\",\n" +
            "      \"doubleShift\": true,\n" +
            "      \"onCall\": true,\n" +
            "      \"job\": \"DOG_HANDLER\",\n" +
            "      \"drivingLicense\": \"NONE\"\n" +
            "}"
        val json3 = JSONObject(jsonString3)
        assertTrue(StaffType.createFromJson(json3) is Failure)
        val jsonString4 = "{" +
            "      \"id\": 1,\n" +
            "      \"name\": \"One\",\n" +
            "      \"baseID\": 0,\n" +
            "      \"ticksHome\": 3,\n" +
            "      \"shift\": \"EARLY\",\n" +
            "      \"doubleShift\": true,\n" +
            "      \"onCall\": false,\n" +
            "      \"job\": \"DOG_HANDLER\",\n" +
            "      \"drivingLicense\": \"TRUCK\"\n" +
            "}"
        val json4 = JSONObject(jsonString4)
        assertTrue(StaffType.createFromJson(json4) is Failure)
    }

    @Test
    fun wrongKeys() {
        val jsonString = "{" +
            "      \"id\": 1,\n" +
            "      \"name\": \"One\",\n" +
            "      \"baseID\": 0,\n" +
            "      \"ticksHome\": 3,\n" +
            "      \"shift\": \"EARLY\",\n" +
            "      \"doubleShift\": false,\n" +
            "      \"onCall\": false,\n" +
            "      \"job\": \"EMERGENCY_DOCTOR\",\n" +
            "      \"drivingLicense\": \"NONE\",\n" +
            "      \"water\": 5\n" +
            "}"
        val json = JSONObject(jsonString)
        assertTrue(StaffType.createFromJson(json) is Failure)
    }
}
