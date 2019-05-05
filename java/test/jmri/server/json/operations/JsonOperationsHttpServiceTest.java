package jmri.server.json.operations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.junit.Before;
import org.junit.Test;

import jmri.IdTagManager;
import jmri.InstanceManager;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.jmrit.operations.rollingstock.cars.Kernel;
import jmri.server.json.JSON;
import jmri.server.json.JsonException;
import jmri.server.json.JsonHttpServiceTestBase;
import jmri.util.JUnitOperationsUtil;
import jmri.util.JUnitUtil;

public class JsonOperationsHttpServiceTest extends JsonHttpServiceTestBase<JsonOperationsHttpService> {

    @Test
    public void testCar() throws JsonException {
        // try a non-existent car
        try {
            service.doGet(JsonOperations.CAR, "", NullNode.getInstance(), locale, 0);
            fail("Expected exception not thrown");
        } catch (JsonException ex) {
            assertEquals(404, ex.getCode());
        }
        // get a known car
        CarManager manager = InstanceManager.getDefault(CarManager.class);
        Car car = manager.getByRoadAndNumber("CP", "C10099");
        JsonNode result = service.doGet(JsonOperations.CAR, car.getId(), NullNode.getInstance(), locale, 42);
        validate(result);
        assertEquals(JsonOperations.CAR, result.path(JSON.TYPE).asText());
        assertTrue(result.path(JSON.METHOD).isMissingNode());
        assertEquals(42, result.path(JSON.ID).asInt());
        JsonNode data = result.path(JSON.DATA);
        assertEquals("Number of properties in Car", 24, data.size());
        assertEquals(car.getId(), data.path(JSON.NAME).asText());
        assertEquals(car.getRoadName(), data.path(JSON.ROAD).asText());
        assertEquals(car.getNumber(), data.path(JSON.NUMBER).asText());
        assertTrue(data.path(JSON.RFID).isValueNode());
        assertEquals(car.getRfid(), data.path(JSON.RFID).asText());
        assertEquals(car.getTypeName(), data.path(JSON.TYPE).asText());
        assertEquals(car.getLengthInteger(), data.path(JSON.LENGTH).asInt());
        assertEquals(car.getColor(), data.path(JSON.COLOR).asText());
        assertEquals(car.getOwner(), data.path(JSON.OWNER).asText());
        assertEquals(car.getComment(), data.path(JSON.COMMENT).asText());
        assertEquals(car.getLocationId(), data.path(JsonOperations.LOCATION).path(JSON.NAME).asText());
        assertEquals(car.getTrackId(), data.path(JsonOperations.LOCATION).path(JsonOperations.TRACK).path(JSON.NAME).asText());
        assertTrue(data.path(JsonOperations.DESTINATION).isValueNode());
        assertTrue(data.path(JsonOperations.DESTINATION).isNull());
        assertEquals(car.getLoadName(), data.path(JSON.LOAD).asText());
        assertTrue(data.path(JSON.HAZARDOUS).isValueNode());
        assertFalse(data.path(JSON.HAZARDOUS).asBoolean());
        assertTrue(data.path(JsonOperations.CABOOSE).isValueNode());
        assertTrue(data.path(JsonOperations.CABOOSE).asBoolean());
        assertTrue(data.path(JsonOperations.PASSENGER).isValueNode());
        assertFalse(data.path(JsonOperations.PASSENGER).asBoolean());
        assertTrue(data.path(JsonOperations.FRED).isValueNode());
        assertFalse(data.path(JsonOperations.FRED).asBoolean());
        assertTrue(data.path(JSON.ADD_COMMENT).isValueNode());
        assertTrue(data.path(JSON.ADD_COMMENT).asText().isEmpty());
        assertTrue(data.path(JSON.REMOVE_COMMENT).isValueNode());
        assertTrue(data.path(JSON.REMOVE_COMMENT).asText().isEmpty());
        assertTrue(data.path(JsonOperations.KERNEL).isValueNode());
        assertTrue(data.path(JsonOperations.KERNEL).isNull());
        assertTrue(data.path(JSON.UTILITY).isValueNode());
        assertFalse(data.path(JSON.UTILITY).asBoolean());
        assertTrue(data.path(JSON.FINAL_DESTINATION).isValueNode());
        assertTrue(data.path(JSON.FINAL_DESTINATION).isNull());
        assertTrue(data.path(JSON.RETURN_WHEN_EMPTY).isValueNode());
        assertTrue(data.path(JSON.RETURN_WHEN_EMPTY).isNull());
        assertFalse(data.path(JsonOperations.OUT_OF_SERVICE).isMissingNode());
        assertFalse(data.path(JsonOperations.OUT_OF_SERVICE).asBoolean());
        assertTrue(data.path(JSON.STATUS).isValueNode());
        assertTrue(data.path(JSON.STATUS).asText().isEmpty());
        // add (PUT) a car
        data = mapper.createObjectNode().put(JSON.ROAD, "MEC").put(JSON.NUMBER, "31995");
        validateData(JsonOperations.CAR, data, false);
        result = service.doPut(JsonOperations.CAR, "", data, locale, 42);
        car = manager.getByRoadAndNumber("MEC", "31995");
        assertNotNull(car);
        validate(result);
        assertEquals(JsonOperations.CAR, result.path(JSON.TYPE).asText());
        assertTrue(result.path(JSON.METHOD).isMissingNode());
        assertEquals(42, result.path(JSON.ID).asInt());
        data = result.path(JSON.DATA);
        assertEquals("Number of properties in Car", 24, data.size());
        assertEquals(car.getId(), data.path(JSON.NAME).asText());
        assertEquals(car.getRoadName(), data.path(JSON.ROAD).asText());
        assertEquals(car.getNumber(), data.path(JSON.NUMBER).asText());
        assertTrue(data.path(JSON.RFID).isValueNode());
        assertEquals(car.getRfid(), data.path(JSON.RFID).asText());
        assertEquals(car.getTypeName(), data.path(JSON.TYPE).asText());
        assertEquals(car.getLengthInteger(), data.path(JSON.LENGTH).asInt());
        assertEquals(car.getColor(), data.path(JSON.COLOR).asText());
        assertEquals(car.getOwner(), data.path(JSON.OWNER).asText());
        assertEquals(car.getComment(), data.path(JSON.COMMENT).asText());
        assertEquals(car.getLocationId(), data.path(JsonOperations.LOCATION).path(JSON.NAME).asText());
        assertEquals(car.getTrackId(), data.path(JsonOperations.LOCATION).path(JsonOperations.TRACK).path(JSON.NAME).asText());
        assertTrue(data.path(JsonOperations.DESTINATION).isValueNode());
        assertTrue(data.path(JsonOperations.DESTINATION).isNull());
        assertEquals(car.getLoadName(), data.path(JSON.LOAD).asText());
        assertTrue(data.path(JSON.HAZARDOUS).isValueNode());
        assertFalse(data.path(JSON.HAZARDOUS).asBoolean());
        assertTrue(data.path(JsonOperations.CABOOSE).isValueNode());
        assertFalse(data.path(JsonOperations.CABOOSE).asBoolean());
        assertTrue(data.path(JsonOperations.PASSENGER).isValueNode());
        assertFalse(data.path(JsonOperations.PASSENGER).asBoolean());
        assertTrue(data.path(JsonOperations.FRED).isValueNode());
        assertFalse(data.path(JsonOperations.FRED).asBoolean());
        assertTrue(data.path(JSON.ADD_COMMENT).isValueNode());
        assertTrue(data.path(JSON.ADD_COMMENT).asText().isEmpty());
        assertTrue(data.path(JSON.REMOVE_COMMENT).isValueNode());
        assertTrue(data.path(JSON.REMOVE_COMMENT).asText().isEmpty());
        assertTrue(data.path(JsonOperations.KERNEL).isValueNode());
        assertTrue(data.path(JsonOperations.KERNEL).isNull());
        assertTrue(data.path(JSON.UTILITY).isValueNode());
        assertFalse(data.path(JSON.UTILITY).asBoolean());
        assertTrue(data.path(JSON.FINAL_DESTINATION).isValueNode());
        assertTrue(data.path(JSON.FINAL_DESTINATION).isNull());
        assertTrue(data.path(JSON.RETURN_WHEN_EMPTY).isValueNode());
        assertTrue(data.path(JSON.RETURN_WHEN_EMPTY).isNull());
        assertFalse(data.path(JsonOperations.OUT_OF_SERVICE).isMissingNode());
        assertFalse(data.path(JsonOperations.OUT_OF_SERVICE).asBoolean());
        assertTrue(data.path(JSON.STATUS).isValueNode());
        assertTrue(data.path(JSON.STATUS).asText().isEmpty());
        // delete a car
        data = mapper.createObjectNode().put(JSON.NAME, car.getId());
        validateData(JsonOperations.CAR, data, false);
        service.doDelete(JsonOperations.CAR, car.getId(), data, locale, 0);
        assertNull(manager.getById(car.getId()));
        // (re)create a car
        // add (PUT) a car
        data = mapper.createObjectNode().put(JSON.ROAD, "MEC").put(JSON.NUMBER, "31995");
        validateData(JsonOperations.CAR, data, false);
        result = service.doPut(JsonOperations.CAR, car.getId(), data, locale, 42);
        car = manager.getByRoadAndNumber("MEC", "31995");
        assertNotNull(car);
        validate(result);
        // add (PUT) the same car again
        try {
            service.doPut(JsonOperations.CAR, car.getId(), data, locale, 42);
            fail("Expected exception not thrown");
        } catch (JsonException ex) {
            assertEquals(409, ex.getCode());
            assertEquals(42, ex.getId());
            assertEquals("Unable to create new car with road number MEC 31995 since another car with that road number already exists.", ex.getMessage());
        }
        // edit (POST) the added car
        String id = car.getId();
        data = mapper.createObjectNode()
                .put(JSON.NAME, id) // can't change this directly
                .put(JSON.ROAD, "BM")
                .put(JSON.NUMBER, "216")
                .put(JSON.RFID, "1234567890AB")
                .put(JSON.HAZARDOUS, true)
                .put(JsonOperations.CABOOSE, true)
                .put(JsonOperations.PASSENGER, true)
                .put(JsonOperations.FRED, true)
                .put(JSON.UTILITY, true)
                .put(JsonOperations.OUT_OF_SERVICE, true);
        validateData(JsonOperations.CAR, data, false);
        result = service.doPost(JsonOperations.CAR, id, data, locale, 42);
        car = manager.getById(id); // id invalidated by changing name and number
        assertNull(car);
        car = manager.getByRoadAndNumber("BM", "216"); // get by name and number
        assertNotNull(car);
        validate(result);
        assertEquals(JsonOperations.CAR, result.path(JSON.TYPE).asText());
        assertTrue(result.path(JSON.METHOD).isMissingNode());
        assertEquals(42, result.path(JSON.ID).asInt());
        data = result.path(JSON.DATA);
        assertEquals("Number of properties in Car", 25, data.size()); // rename not always present
        assertEquals(car.getId(), data.path(JSON.NAME).asText());
        assertEquals(car.getRoadName(), data.path(JSON.ROAD).asText());
        assertEquals(car.getNumber(), data.path(JSON.NUMBER).asText());
        assertTrue(data.path(JSON.RFID).isValueNode());
        assertEquals(car.getRfid(), data.path(JSON.RFID).asText());
        assertEquals(car.getTypeName(), data.path(JSON.TYPE).asText());
        assertEquals(car.getLengthInteger(), data.path(JSON.LENGTH).asInt());
        assertEquals(car.getColor(), data.path(JSON.COLOR).asText());
        assertEquals(car.getOwner(), data.path(JSON.OWNER).asText());
        assertEquals(car.getComment(), data.path(JSON.COMMENT).asText());
        assertEquals(car.getLocationId(), data.path(JsonOperations.LOCATION).path(JSON.NAME).asText());
        assertEquals(car.getTrackId(), data.path(JsonOperations.LOCATION).path(JsonOperations.TRACK).path(JSON.NAME).asText());
        assertTrue(data.path(JsonOperations.DESTINATION).isValueNode());
        assertTrue(data.path(JsonOperations.DESTINATION).isNull());
        assertEquals(car.getLoadName(), data.path(JSON.LOAD).asText());
        assertTrue(data.path(JSON.HAZARDOUS).isValueNode());
        assertTrue(data.path(JSON.HAZARDOUS).asBoolean());
        assertTrue(data.path(JsonOperations.CABOOSE).isValueNode());
        assertTrue(data.path(JsonOperations.CABOOSE).asBoolean());
        assertTrue(data.path(JsonOperations.PASSENGER).isValueNode());
        assertTrue(data.path(JsonOperations.PASSENGER).asBoolean());
        assertTrue(data.path(JsonOperations.FRED).isValueNode());
        assertTrue(data.path(JsonOperations.FRED).asBoolean());
        assertTrue(data.path(JSON.ADD_COMMENT).isValueNode());
        assertTrue(data.path(JSON.ADD_COMMENT).asText().isEmpty());
        assertTrue(data.path(JSON.REMOVE_COMMENT).isValueNode());
        assertTrue(data.path(JSON.REMOVE_COMMENT).asText().isEmpty());
        assertTrue(data.path(JsonOperations.KERNEL).isValueNode());
        assertTrue(data.path(JsonOperations.KERNEL).isNull());
        assertTrue(data.path(JSON.UTILITY).isValueNode());
        assertTrue(data.path(JSON.UTILITY).asBoolean());
        assertTrue(data.path(JSON.FINAL_DESTINATION).isValueNode());
        assertTrue(data.path(JSON.FINAL_DESTINATION).isNull());
        assertTrue(data.path(JSON.RETURN_WHEN_EMPTY).isValueNode());
        assertTrue(data.path(JSON.RETURN_WHEN_EMPTY).isNull());
        assertFalse(data.path(JsonOperations.OUT_OF_SERVICE).isMissingNode());
        assertTrue(data.path(JsonOperations.OUT_OF_SERVICE).asBoolean());
        assertTrue(data.path(JSON.STATUS).isValueNode());
        assertEquals("Out of service status", "<O> ", data.path(JSON.STATUS).asText());
    }

    @Test
    public void testDoGetListCarEngineRollingStock() throws JsonException {
        JsonNode result = service.doGetList(JsonOperations.CAR, NullNode.getInstance(), locale, 0);
        validate(result);
        assertEquals("9 cars", 9, result.size());
        result = service.doGetList(JsonOperations.ENGINE, NullNode.getInstance(), locale, 0);
        validate(result);
        assertEquals("4 engines", 4, result.size());
        result = service.doGetList(JsonOperations.ROLLING_STOCK, NullNode.getInstance(), locale, 0);
        validate(result);
        assertEquals("13 rolling stock", 13, result.size());
    }

    @Test
    public void testCarType() throws JsonException {
        JsonNode result = service.doGetList(JsonOperations.CAR_TYPE, NullNode.getInstance(), locale, 0);
        validate(result);
        assertTrue(result.isArray());
        assertEquals(3, result.size());
        result = service.doPut(JsonOperations.CAR_TYPE, "test1", NullNode.getInstance(), locale, 42);
        validate(result);
        assertEquals(JsonOperations.CAR_TYPE, result.path(JSON.TYPE).asText());
        assertEquals("test1", result.path(JSON.DATA).path(JSON.NAME).asText());
        assertTrue(result.path(JSON.DATA).path(JsonOperations.CARS).isArray());
        assertTrue(result.path(JSON.DATA).path(JsonOperations.LOCATIONS).isArray());
        JsonNode result2 = service.doGet(JsonOperations.CAR_TYPE, "test1", NullNode.getInstance(), locale, 42);
        assertEquals(result, result2);
        result = service.doGetList(JsonOperations.CAR_TYPE, NullNode.getInstance(), locale, 0);
        validate(result);
        assertTrue(result.isArray());
        assertEquals(4, result.size());
        assertEquals(JsonOperations.CAR_TYPE, result.path(0).path(JSON.TYPE).asText());
        assertEquals("test1", result.path(0).path(JSON.DATA).path(JSON.NAME).asText());
        assertTrue(result.path(0).path(JSON.DATA).path(JsonOperations.CARS).isArray());
        assertTrue(result.path(0).path(JSON.DATA).path(JsonOperations.LOCATIONS).isArray());
        service.doDelete(JsonOperations.CAR_TYPE, "test1", NullNode.getInstance(), locale, 42);
        result = service.doGetList(JsonOperations.CAR_TYPE, NullNode.getInstance(), locale, 0);
        validate(result);
        assertTrue(result.isArray());
        assertEquals(3, result.size());
    }

    @Test
    public void testKernel() throws JsonException {
        JsonNode result = service.doGetList(JsonOperations.KERNEL, NullNode.getInstance(), locale, 0);
        validate(result);
        assertTrue(result.isArray());
        assertEquals(0, result.size());
        result = service.doPut(JsonOperations.KERNEL, "test1", NullNode.getInstance(), locale, 42);
        validate(result);
        assertEquals(JsonOperations.KERNEL, result.path(JSON.TYPE).asText());
        assertEquals("test1", result.path(JSON.DATA).path(JSON.NAME).asText());
        assertEquals(0, result.path(JSON.DATA).path(JSON.LENGTH).asInt());
        assertEquals(0, result.path(JSON.DATA).path(JsonOperations.WEIGHT).asInt());
        assertTrue(result.path(JSON.DATA).path(JsonOperations.CARS).isArray());
        JsonNode result2 = service.doGet(JsonOperations.KERNEL, "test1", NullNode.getInstance(), locale, 42);
        assertEquals(result, result2);
        result = service.doGetList(JsonOperations.KERNEL, NullNode.getInstance(), locale, 0);
        validate(result);
        assertTrue(result.isArray());
        assertEquals(1, result.size());
        assertEquals(JsonOperations.KERNEL, result.path(0).path(JSON.TYPE).asText());
        assertEquals("test1", result.path(0).path(JSON.DATA).path(JSON.NAME).asText());
        assertEquals(0, result.path(0).path(JSON.DATA).path(JSON.LENGTH).asInt());
        assertEquals(0, result.path(0).path(JSON.DATA).path(JsonOperations.WEIGHT).asInt());
        assertTrue(result.path(0).path(JSON.DATA).path(JsonOperations.CARS).isArray());
        service.doDelete(JsonOperations.KERNEL, "test1", NullNode.getInstance(), locale, 42);
        result = service.doGetList(JsonOperations.KERNEL, NullNode.getInstance(), locale, 0);
        validate(result);
        assertTrue(result.isArray());
        assertEquals(0, result.size());
    }

    @Test
    public void testDeleteInUseKernel() throws JsonException {
        CarManager manager = InstanceManager.getDefault(CarManager.class);
        Kernel k1 = manager.newKernel("test1");
        Car c1 = manager.newRS("road", "1");
        c1.setLength("40");
        c1.setWeight("1000");
        c1.setWeightTons("10");
        c1.setLoadName("L");
        c1.setKernel(k1);
        String deleteToken = null;
        try {
            service.doDelete(JsonOperations.KERNEL, "test1", NullNode.getInstance(), locale, 42);
            fail("Expected exception not thrown");
        } catch (JsonException ex) {
            assertEquals(409, ex.getCode());
            deleteToken = ex.getAdditionalData().path(JSON.FORCE_DELETE).asText();
            assertTrue(ex.getJsonMessage().path(JSON.DATA).path(JSON.CONFLICT).isArray());
            assertEquals(1, ex.getJsonMessage().path(JSON.DATA).path(JSON.CONFLICT).size());
            assertEquals(JsonOperations.CAR,
                    ex.getJsonMessage().path(JSON.DATA).path(JSON.CONFLICT).path(0).path(JSON.TYPE).asText());
        }
        assertNotNull(deleteToken);
        ObjectNode data = service.getObjectMapper().createObjectNode().put(JSON.FORCE_DELETE, deleteToken);
        service.doDelete(JsonOperations.KERNEL, "test1", data, locale, 42);
        assertNull(c1.getKernel());
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        JUnitUtil.initIdTagManager();
        InstanceManager.getDefault(IdTagManager.class).provideIdTag("1234567890AB");
        JUnitOperationsUtil.setupOperationsTests();
        JUnitOperationsUtil.initOperationsData();
        service = new JsonOperationsHttpService(mapper);
    }
}