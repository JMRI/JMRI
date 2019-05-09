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
import jmri.ReporterManager;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.jmrit.operations.rollingstock.cars.Kernel;
import jmri.jmrit.operations.rollingstock.engines.Engine;
import jmri.jmrit.operations.rollingstock.engines.EngineManager;
import jmri.server.json.JSON;
import jmri.server.json.JsonException;
import jmri.server.json.JsonHttpServiceTestBase;
import jmri.server.json.consist.JsonConsist;
import jmri.server.json.reporter.JsonReporter;
import jmri.util.JUnitAppender;
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
    public void testEngine() throws JsonException {
        // try a non-existent engine
        try {
            service.doGet(JsonOperations.ENGINE, "", NullNode.getInstance(), locale, 0);
            fail("Expected exception not thrown");
        } catch (JsonException ex) {
            assertEquals(404, ex.getCode());
        }
        // get a known engine
        EngineManager manager = InstanceManager.getDefault(EngineManager.class);
        Engine engine = manager.getByRoadAndNumber("PC", "5524");
        JsonNode result = service.doGet(JsonOperations.ENGINE, engine.getId(), NullNode.getInstance(), locale, 42);
        validate(result);
        assertEquals(JsonOperations.ENGINE, result.path(JSON.TYPE).asText());
        assertTrue(result.path(JSON.METHOD).isMissingNode());
        assertEquals(42, result.path(JSON.ID).asInt());
        JsonNode data = result.path(JSON.DATA);
        assertEquals("Number of properties in Engine", 14, data.size());
        assertEquals(engine.getId(), data.path(JSON.NAME).asText());
        assertEquals(engine.getRoadName(), data.path(JSON.ROAD).asText());
        assertEquals(engine.getNumber(), data.path(JSON.NUMBER).asText());
        assertTrue(data.path(JSON.RFID).isValueNode());
        assertEquals(engine.getRfid(), data.path(JSON.RFID).asText());
        assertEquals(engine.getTypeName(), data.path(JSON.TYPE).asText());
        assertEquals(engine.getLengthInteger(), data.path(JSON.LENGTH).asInt());
        assertEquals(engine.getColor(), data.path(JSON.COLOR).asText());
        assertEquals(engine.getOwner(), data.path(JSON.OWNER).asText());
        assertEquals(engine.getComment(), data.path(JSON.COMMENT).asText());
        assertEquals(engine.getLocationId(), data.path(JsonOperations.LOCATION).path(JSON.NAME).asText());
        assertEquals(engine.getTrackId(), data.path(JsonOperations.LOCATION).path(JsonOperations.TRACK).path(JSON.NAME).asText());
        assertTrue(data.path(JsonOperations.DESTINATION).isValueNode());
        assertTrue(data.path(JsonOperations.DESTINATION).isNull());
        assertTrue(data.path(JsonConsist.CONSIST).isValueNode());
        assertEquals("C14", data.path(JsonConsist.CONSIST).asText());
        assertFalse(data.path(JsonOperations.OUT_OF_SERVICE).isMissingNode());
        assertFalse(data.path(JsonOperations.OUT_OF_SERVICE).asBoolean());
        assertTrue(data.path(JSON.MODEL).isValueNode());
        assertEquals("SD45", data.path(JSON.MODEL).asText());
        // add (PUT) a car
        data = mapper.createObjectNode().put(JSON.ROAD, "MEC").put(JSON.NUMBER, "3402");
        validateData(JsonOperations.ENGINE, data, false);
        result = service.doPut(JsonOperations.ENGINE, "", data, locale, 42);
        engine = manager.getByRoadAndNumber("MEC", "3402");
        assertNotNull(engine);
        validate(result);
        assertEquals(JsonOperations.ENGINE, result.path(JSON.TYPE).asText());
        assertTrue(result.path(JSON.METHOD).isMissingNode());
        assertEquals(42, result.path(JSON.ID).asInt());
        data = result.path(JSON.DATA);
        assertEquals("Number of properties in Engine", 14, data.size());
        assertEquals(engine.getId(), data.path(JSON.NAME).asText());
        assertEquals(engine.getRoadName(), data.path(JSON.ROAD).asText());
        assertEquals(engine.getNumber(), data.path(JSON.NUMBER).asText());
        assertTrue(data.path(JSON.RFID).isValueNode());
        assertEquals(engine.getRfid(), data.path(JSON.RFID).asText());
        assertEquals(engine.getTypeName(), data.path(JSON.TYPE).asText());
        assertEquals(engine.getLengthInteger(), data.path(JSON.LENGTH).asInt());
        assertEquals(engine.getColor(), data.path(JSON.COLOR).asText());
        assertEquals(engine.getOwner(), data.path(JSON.OWNER).asText());
        assertEquals(engine.getComment(), data.path(JSON.COMMENT).asText());
        assertEquals(engine.getLocationId(), data.path(JsonOperations.LOCATION).path(JSON.NAME).asText());
        assertEquals(engine.getTrackId(), data.path(JsonOperations.LOCATION).path(JsonOperations.TRACK).path(JSON.NAME).asText());
        assertTrue(data.path(JsonOperations.DESTINATION).isValueNode());
        assertTrue(data.path(JsonOperations.DESTINATION).isNull());
        assertTrue(data.path(JsonConsist.CONSIST).isValueNode());
        assertTrue(data.path(JsonConsist.CONSIST).isNull());
        assertFalse(data.path(JsonOperations.OUT_OF_SERVICE).isMissingNode());
        assertFalse(data.path(JsonOperations.OUT_OF_SERVICE).asBoolean());
        assertTrue(data.path(JSON.MODEL).isValueNode());
        assertTrue(data.path(JSON.MODEL).asText().isEmpty());
        // delete an engine
        data = mapper.createObjectNode().put(JSON.NAME, engine.getId());
        validateData(JsonOperations.ENGINE, data, false);
        service.doDelete(JsonOperations.ENGINE, engine.getId(), data, locale, 0);
        assertNull(manager.getById(engine.getId()));
        // (re)create an engine
        // add (PUT) an engine
        data = mapper.createObjectNode().put(JSON.ROAD, "MEC").put(JSON.NUMBER, "3402");
        validateData(JsonOperations.ENGINE, data, false);
        result = service.doPut(JsonOperations.ENGINE, engine.getId(), data, locale, 42);
        engine = manager.getByRoadAndNumber("MEC", "3402");
        assertNotNull(engine);
        validate(result);
        // add (PUT) the same engine again
        try {
            service.doPut(JsonOperations.ENGINE, engine.getId(), data, locale, 42);
            fail("Expected exception not thrown");
        } catch (JsonException ex) {
            assertEquals(409, ex.getCode());
            assertEquals(42, ex.getId());
            assertEquals("Unable to create new engine with road number MEC 3402 since another engine with that road number already exists.", ex.getMessage());
        }
        // edit (POST) the added engine
        String id = engine.getId();
        data = mapper.createObjectNode()
                .put(JSON.NAME, id) // can't change this directly
                .put(JSON.ROAD, "BM")
                .put(JSON.NUMBER, "216")
                .put(JSON.RFID, "1234567890AB")
                .put(JSON.MODEL, "SD 40-2")
                .put(JsonOperations.OUT_OF_SERVICE, true);
        validateData(JsonOperations.ENGINE, data, false);
        result = service.doPost(JsonOperations.ENGINE, id, data, locale, 42);
        engine = manager.getById(id); // id invalidated by changing name and number
        assertNull(engine);
        engine = manager.getByRoadAndNumber("BM", "216"); // get by name and number
        assertNotNull(engine);
        validate(result);
        assertEquals(JsonOperations.ENGINE, result.path(JSON.TYPE).asText());
        assertTrue(result.path(JSON.METHOD).isMissingNode());
        assertEquals(42, result.path(JSON.ID).asInt());
        data = result.path(JSON.DATA);
        assertEquals("Number of properties in Engine", 15, data.size()); // rename not always present
        assertEquals("BM216", data.path(JSON.NAME).asText());
        assertEquals("BM", data.path(JSON.ROAD).asText());
        assertEquals("216", data.path(JSON.NUMBER).asText());
        assertTrue(data.path(JSON.RFID).isValueNode());
        assertEquals("1234567890AB", data.path(JSON.RFID).asText());
        assertEquals(engine.getTypeName(), data.path(JSON.TYPE).asText());
        assertEquals(0, data.path(JSON.LENGTH).asInt());
        assertEquals(engine.getColor(), data.path(JSON.COLOR).asText());
        assertEquals(engine.getOwner(), data.path(JSON.OWNER).asText());
        assertEquals(engine.getComment(), data.path(JSON.COMMENT).asText());
        assertEquals(engine.getLocationId(), data.path(JsonOperations.LOCATION).path(JSON.NAME).asText());
        assertEquals(engine.getTrackId(), data.path(JsonOperations.LOCATION).path(JsonOperations.TRACK).path(JSON.NAME).asText());
        assertTrue(data.path(JsonOperations.DESTINATION).isValueNode());
        assertTrue(data.path(JsonOperations.DESTINATION).isNull());
        assertTrue(data.path(JsonConsist.CONSIST).isValueNode());
        assertTrue(data.path(JsonConsist.CONSIST).isNull());
        assertFalse(data.path(JsonOperations.OUT_OF_SERVICE).isMissingNode());
        assertTrue(data.path(JsonOperations.OUT_OF_SERVICE).asBoolean());
        assertEquals("SD 40-2", data.path(JSON.MODEL).asText());
    }

    @Test
    public void testLocation() throws JsonException {
        // try a non-existent location
        try {
            service.doGet(JsonOperations.LOCATION, "", NullNode.getInstance(), locale, 0);
            fail("Expected exception not thrown");
        } catch (JsonException ex) {
            assertEquals(404, ex.getCode());
        }
        JUnitAppender.assertErrorMessage("Unable to get location id [].");
        // get a known location
        LocationManager manager = InstanceManager.getDefault(LocationManager.class);
        Location location = manager.getLocationById("20");
        JsonNode result = service.doGet(JsonOperations.LOCATION, location.getId(), NullNode.getInstance(), locale, 42);
        validate(result);
        assertEquals(JsonOperations.LOCATION, result.path(JSON.TYPE).asText());
        assertTrue(result.path(JSON.METHOD).isMissingNode());
        assertEquals(42, result.path(JSON.ID).asInt());
        JsonNode data = result.path(JSON.DATA);
        assertEquals("Number of properties in Location", 6, data.size());
        assertEquals(location.getId(), data.path(JSON.NAME).asText());
        assertEquals(location.getName(), data.path(JSON.USERNAME).asText());
        assertTrue(data.path(JSON.COMMENT).isValueNode());
        assertEquals(location.getComment(), data.path(JSON.COMMENT).asText());
        assertTrue(data.path(JsonReporter.REPORTER).isValueNode());
        assertTrue(data.path(JsonReporter.REPORTER).isNull());
        assertTrue(data.path(JSON.TYPE).isArray());
        assertEquals(4, data.path(JSON.TYPE).size());
        assertEquals("Boxcar", data.path(JSON.TYPE).path(0).asText());
        assertEquals("Caboose", data.path(JSON.TYPE).path(1).asText());
        assertEquals("Flat", data.path(JSON.TYPE).path(2).asText());
        assertEquals("Diesel", data.path(JSON.TYPE).path(3).asText());
        // add (PUT) a location
        data = mapper.createObjectNode().put(JSON.USERNAME, "Test Site");
        validateData(JsonOperations.LOCATION, data, false);
        result = service.doPut(JsonOperations.LOCATION, "42", data, locale, 42);
        location = manager.getLocationById("42");
        assertNull(location);
        location = manager.getLocationByName("Test Site");
        assertNotNull(location);
        validate(result);
        assertEquals(JsonOperations.LOCATION, result.path(JSON.TYPE).asText());
        assertTrue(result.path(JSON.METHOD).isMissingNode());
        assertEquals(42, result.path(JSON.ID).asInt());
        data = result.path(JSON.DATA);
        assertEquals("Number of properties in Location", 6, data.size());
        assertEquals(location.getId(), data.path(JSON.NAME).asText());
        assertEquals(location.getName(), data.path(JSON.USERNAME).asText());
        // delete a location
        data = mapper.createObjectNode().put(JSON.NAME, location.getId());
        validateData(JsonOperations.LOCATION, data, false);
        service.doDelete(JsonOperations.LOCATION, location.getId(), data, locale, 0);
        assertNull(manager.getLocationById(location.getId()));
        // (re)create a location
        // add (PUT) a location
        data = mapper.createObjectNode().put(JSON.USERNAME, "Test Site");
        validateData(JsonOperations.LOCATION, data, false);
        result = service.doPut(JsonOperations.LOCATION, "42", data, locale, 42);
        location = manager.getLocationByName("Test Site");
        assertNotNull(location);
        validate(result);
        // add (PUT) the same location again with same name (id)
        try {
            service.doPut(JsonOperations.LOCATION, location.getId(), data, locale, 42);
            fail("Expected exception not thrown");
        } catch (JsonException ex) {
            assertEquals(409, ex.getCode());
            assertEquals(42, ex.getId());
            assertEquals("Unable to create object type location with name \"22\" since already exists.", ex.getMessage());
        }
        // add (PUT) the same location again with same user name, different name (id)
        try {
            service.doPut(JsonOperations.LOCATION, "42", data, locale, 42);
            fail("Expected exception not thrown");
        } catch (JsonException ex) {
            assertEquals(409, ex.getCode());
            assertEquals(42, ex.getId());
            assertEquals("Unable to create object type location with user name \"Test Site\" since already exists.", ex.getMessage());
        }
        // edit (POST) the added location
        String id = location.getId();
        data = mapper.createObjectNode()
                .put(JSON.NAME, id) // can't change this
                .put(JSON.USERNAME, "Editted Site")
                .put(JSON.COMMENT, "A comment")
                .put(JsonReporter.REPORTER, InstanceManager.getDefault(ReporterManager.class).provide("IR1").getSystemName());
        // TODO: put change to types?
        validateData(JsonOperations.LOCATION, data, false);
        result = service.doPost(JsonOperations.LOCATION, id, data, locale, 42);
        location = manager.getLocationById(id);
        assertNotNull(location);
        validate(result);
        assertEquals(JsonOperations.LOCATION, result.path(JSON.TYPE).asText());
        assertTrue(result.path(JSON.METHOD).isMissingNode());
        assertEquals(42, result.path(JSON.ID).asInt());
        data = result.path(JSON.DATA);
        assertEquals("Number of properties in Location", 6, data.size());
        assertEquals(id, data.path(JSON.NAME).asText());
        assertEquals("Editted Site", data.path(JSON.USERNAME).asText());
        assertEquals("A comment", data.path(JSON.COMMENT).asText());
        assertEquals("IR1", data.path(JsonReporter.REPORTER).asText());
        assertTrue(data.path(JSON.TYPE).isArray());
        assertEquals(4, data.path(JSON.TYPE).size());
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
        JUnitUtil.initReporterManager();
        InstanceManager.getDefault(IdTagManager.class).provideIdTag("1234567890AB");
        JUnitOperationsUtil.setupOperationsTests();
        JUnitOperationsUtil.initOperationsData();
        service = new JsonOperationsHttpService(mapper);
    }
}