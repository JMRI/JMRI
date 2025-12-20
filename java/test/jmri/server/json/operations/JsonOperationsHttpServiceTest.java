package jmri.server.json.operations;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Arrays;

import jmri.*;
import jmri.jmrit.operations.locations.*;
import jmri.jmrit.operations.rollingstock.cars.*;
import jmri.jmrit.operations.rollingstock.engines.Engine;
import jmri.jmrit.operations.rollingstock.engines.EngineManager;
import jmri.server.json.*;
import jmri.server.json.consist.JsonConsist;
import jmri.server.json.reporter.JsonReporter;
import jmri.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JsonOperationsHttpServiceTest extends JsonHttpServiceTestBase<JsonOperationsHttpService> {

    @Test
    public void testGetRollingStock() {
        JsonException ex = assertThrows( JsonException.class, () ->
            service.doGet(JsonOperations.ROLLING_STOCK, "", NullNode.getInstance(),
                    new JsonRequest(locale, JSON.V5, JSON.GET, 0)),
            "Expected exception not thrown");
        assertEquals( 405, ex.getCode());
    }

    @Test
    public void testDeleteInvalidType() {
        JsonException ex = assertThrows( JsonException.class, () ->
            service.doGet("invalid-type", "", NullNode.getInstance(),
                new JsonRequest(locale, JSON.V5, JSON.DELETE, 0)),
            "Expected exception not thrown");
        assertEquals( 500, ex.getCode());
    }

    @Test
    public void testGetInvalidType() {
        JsonException ex = assertThrows( JsonException.class, () ->
            service.doGet("invalid-type", "", NullNode.getInstance(),
                new JsonRequest(locale, JSON.V5, JSON.GET, 0)),
            "Expected exception not thrown");
        assertEquals( 500, ex.getCode());
    }

    @Test
    public void testPostInvalidType() {
        JsonException ex = assertThrows( JsonException.class, () ->
            service.doPost("invalid-type", "", NullNode.getInstance(),
                new JsonRequest(locale, JSON.V5, JSON.GET, 0)),
            "Expected exception not thrown");
        assertEquals( 405, ex.getCode());
    }

    @Test
    public void testPutInvalidType() {
        JsonException ex = assertThrows( JsonException.class, () ->
            service.doPut("invalid-type", "", NullNode.getInstance(),
                new JsonRequest(locale, JSON.V5, JSON.GET, 0)),
            "Expected exception not thrown");
        assertEquals( 405, ex.getCode());
    }

    @Test
    public void testCar() throws JsonException {
        // try a non-existent car
        JsonException ex = assertThrows( JsonException.class, () ->
            service.doGet(JsonOperations.CAR, "", NullNode.getInstance(),
                    new JsonRequest(locale, JSON.V5, JSON.GET, 0)),
            "Expected exception not thrown");
        assertEquals(404, ex.getCode());
        // get a known car
        CarManager manager = InstanceManager.getDefault(CarManager.class);
        Car car = manager.getByRoadAndNumber("CP", "C10099");
        JsonNode result = service.doGet(JsonOperations.CAR, car.getId(), NullNode.getInstance(),
                new JsonRequest(locale, JSON.V5, JSON.GET, 42));
        validate(result);
        assertEquals(JsonOperations.CAR, result.path(JSON.TYPE).asText());
        assertTrue(result.path(JSON.METHOD).isMissingNode());
        assertEquals(42, result.path(JSON.ID).asInt());
        JsonNode data = result.path(JSON.DATA);
        assertEquals( 39, data.size(), "Number of properties in Car");
        assertEquals(car.getId(), data.path(JSON.NAME).asText());
        assertEquals(car.getRoadName(), data.path(JsonOperations.ROAD).asText());
        assertEquals(car.getNumber(), data.path(JsonOperations.NUMBER).asText());
        assertTrue(data.path(JSON.RFID).isValueNode());
        assertTrue(data.path(JSON.WHERELASTSEEN).isValueNode());
        assertTrue(data.path(JSON.WHENLASTSEEN).isValueNode());
        assertEquals(car.getRfid(), data.path(JSON.RFID).asText());
        assertTrue(data.path(JSON.WHERELASTSEEN).isNull());
        assertTrue(data.path(JSON.WHENLASTSEEN).isNull());
        assertEquals(car.getTypeName(), data.path(JsonOperations.TYPE).asText());
        assertTrue(data.path(JsonOperations.CAR_SUB_TYPE).asText().isEmpty());
        assertEquals(car.getLengthInteger(), data.path(JSON.LENGTH).asInt());
        assertEquals(car.getAdjustedWeightTons(), data.path(JsonOperations.WEIGHT).asInt());
        assertEquals(car.getWeightTons(), data.path(JsonOperations.WEIGHT_TONS).asText());
        assertEquals(car.getColor(), data.path(JsonOperations.COLOR).asText());
        assertEquals(car.getOwnerName(), data.path(JsonOperations.OWNER).asText());
        assertEquals(car.getComment(), data.path(JsonOperations.COMMENT).asText());
        assertEquals(car.getLocationId(), data.path(JSON.LOCATION).path(JSON.NAME).asText());
        assertEquals(car.getTrackId(),
                data.path(JSON.LOCATION).path(JSON.TRACK).path(JSON.NAME).asText());
        assertTrue(data.path(JsonOperations.LOCATION_UNKNOWN).isValueNode());
        assertFalse(data.path(JsonOperations.LOCATION_UNKNOWN).asBoolean());
        assertFalse(data.path(JsonOperations.TRAIN_ID).isMissingNode());
        assertTrue(data.path(JsonOperations.TRAIN_ID).isNull());
        assertTrue(data.path(JsonOperations.DESTINATION).isValueNode());
        assertTrue(data.path(JsonOperations.DESTINATION).isNull());
        assertEquals(car.getLoadName(), data.path(JsonOperations.LOAD).asText());
        assertTrue(data.path(JsonOperations.HAZARDOUS).isValueNode());
        assertFalse(data.path(JsonOperations.HAZARDOUS).asBoolean());
        assertTrue(data.path(JsonOperations.CABOOSE).isValueNode());
        assertTrue(data.path(JsonOperations.CABOOSE).asBoolean());
        assertTrue(data.path(JsonOperations.PASSENGER).isValueNode());
        assertFalse(data.path(JsonOperations.PASSENGER).asBoolean());
        assertTrue(data.path(JsonOperations.FRED).isValueNode());
        assertFalse(data.path(JsonOperations.FRED).asBoolean());
        assertTrue(data.path(JsonOperations.PICKUP_COMMENT).isValueNode());
        assertTrue(data.path(JsonOperations.PICKUP_COMMENT).asText().isEmpty());
        assertTrue(data.path(JsonOperations.SETOUT_COMMENT).isValueNode());
        assertTrue(data.path(JsonOperations.SETOUT_COMMENT).asText().isEmpty());
        assertTrue(data.path(JsonOperations.KERNEL).isValueNode());
        assertTrue(data.path(JsonOperations.KERNEL).asText().isEmpty());
        assertTrue(data.path(JsonOperations.UTILITY).isValueNode());
        assertFalse(data.path(JsonOperations.UTILITY).asBoolean());
        assertTrue(data.path(JsonOperations.FINAL_DESTINATION).isValueNode());
        assertTrue(data.path(JsonOperations.FINAL_DESTINATION).isNull());
        assertTrue(data.path(JsonOperations.RETURN_WHEN_EMPTY).isValueNode());
        assertTrue(data.path(JsonOperations.RETURN_WHEN_EMPTY).isNull());
        assertFalse(data.path(JsonOperations.OUT_OF_SERVICE).isMissingNode());
        assertFalse(data.path(JsonOperations.OUT_OF_SERVICE).asBoolean());
        assertFalse(data.path(JsonOperations.LOCATION_UNKNOWN).isMissingNode());
        assertFalse(data.path(JsonOperations.LOCATION_UNKNOWN).asBoolean());
        assertFalse(data.path(JsonOperations.TRAIN_ID).isMissingNode());
        assertTrue(data.path(JsonOperations.TRAIN_ID).isNull());
        assertTrue(data.path(JSON.STATUS).isValueNode());
        assertTrue(data.path(JSON.STATUS).asText().isEmpty());
        assertTrue(data.path(JsonOperations.IS_LOCAL).isValueNode());
        assertFalse(data.path(JsonOperations.IS_LOCAL).asBoolean());
        // add (PUT) a car
        data = mapper.createObjectNode().put(JsonOperations.ROAD, "MEC").put(JsonOperations.NUMBER, "31995");
        validateData(JsonOperations.CAR, data, false);
        result = service.doPut(JsonOperations.CAR, "", data, new JsonRequest(locale, JSON.V5, JSON.GET, 42));
        car = manager.getByRoadAndNumber("MEC", "31995");
        assertNotNull(car);
        validate(result);
        assertEquals(JsonOperations.CAR, result.path(JSON.TYPE).asText());
        assertTrue(result.path(JSON.METHOD).isMissingNode());
        assertEquals(42, result.path(JSON.ID).asInt());
        data = result.path(JSON.DATA);
        assertEquals( 39, data.size(), "Number of properties in Car");
        assertEquals(car.getId(), data.path(JSON.NAME).asText());
        assertEquals(car.getRoadName(), data.path(JsonOperations.ROAD).asText());
        assertEquals(car.getNumber(), data.path(JsonOperations.NUMBER).asText());
        assertTrue(data.path(JSON.RFID).isValueNode());
        assertTrue(data.path(JSON.WHERELASTSEEN).isValueNode());
        assertTrue(data.path(JSON.WHENLASTSEEN).isValueNode());
        assertEquals(car.getRfid(), data.path(JSON.RFID).asText());
        assertTrue(data.path(JSON.WHERELASTSEEN).isNull());
        assertTrue(data.path(JSON.WHENLASTSEEN).isNull());
        assertEquals(car.getTypeName(), data.path(JsonOperations.TYPE).asText());
        assertTrue(data.path(JsonOperations.CAR_SUB_TYPE).asText().isEmpty());
        assertEquals(car.getLengthInteger(), data.path(JsonOperations.LENGTH).asInt());
        assertEquals(car.getWeight(), data.path(JsonOperations.WEIGHT).asText());
        assertEquals(car.getWeightTons(), data.path(JsonOperations.WEIGHT_TONS).asText());
        assertEquals(car.getColor(), data.path(JsonOperations.COLOR).asText());
        assertEquals(car.getOwnerName(), data.path(JsonOperations.OWNER).asText());
        assertEquals(car.getBuilt(), data.path(JsonOperations.BUILT).asText());
        assertEquals(car.getComment(), data.path(JsonOperations.COMMENT).asText());
        assertEquals(car.getLocationId(), data.path(JSON.LOCATION).path(JSON.NAME).asText());
        assertEquals(car.getTrackId(),
                data.path(JSON.LOCATION).path(JSON.TRACK).path(JSON.NAME).asText());
        assertTrue(data.path(JsonOperations.DESTINATION).isValueNode());
        assertTrue(data.path(JsonOperations.DESTINATION).isNull());
        assertEquals(car.getLoadName(), data.path(JsonOperations.LOAD).asText());
        assertTrue(data.path(JsonOperations.HAZARDOUS).isValueNode());
        assertFalse(data.path(JsonOperations.HAZARDOUS).asBoolean());
        assertTrue(data.path(JsonOperations.CABOOSE).isValueNode());
        assertFalse(data.path(JsonOperations.CABOOSE).asBoolean());
        assertTrue(data.path(JsonOperations.PASSENGER).isValueNode());
        assertFalse(data.path(JsonOperations.PASSENGER).asBoolean());
        assertTrue(data.path(JsonOperations.FRED).isValueNode());
        assertFalse(data.path(JsonOperations.FRED).asBoolean());
        assertTrue(data.path(JsonOperations.PICKUP_COMMENT).isValueNode());
        assertTrue(data.path(JsonOperations.PICKUP_COMMENT).asText().isEmpty());
        assertTrue(data.path(JsonOperations.SETOUT_COMMENT).isValueNode());
        assertTrue(data.path(JsonOperations.SETOUT_COMMENT).asText().isEmpty());
        assertTrue(data.path(JsonOperations.KERNEL).isValueNode());
        assertTrue(data.path(JsonOperations.KERNEL).asText().isEmpty());
        assertTrue(data.path(JsonOperations.UTILITY).isValueNode());
        assertFalse(data.path(JsonOperations.UTILITY).asBoolean());
        assertTrue(data.path(JsonOperations.FINAL_DESTINATION).isValueNode());
        assertTrue(data.path(JsonOperations.FINAL_DESTINATION).isNull());
        assertTrue(data.path(JsonOperations.RETURN_WHEN_EMPTY).isValueNode());
        assertTrue(data.path(JsonOperations.RETURN_WHEN_EMPTY).isNull());
        assertFalse(data.path(JsonOperations.OUT_OF_SERVICE).isMissingNode());
        assertFalse(data.path(JsonOperations.OUT_OF_SERVICE).asBoolean());
        assertFalse(data.path(JsonOperations.LOCATION_UNKNOWN).isMissingNode());
        assertFalse(data.path(JsonOperations.LOCATION_UNKNOWN).asBoolean());
        assertFalse(data.path(JsonOperations.TRAIN_ID).isMissingNode());
        assertTrue(data.path(JsonOperations.TRAIN_ID).isNull());
        assertTrue(data.path(JSON.STATUS).isValueNode());
        assertTrue(data.path(JSON.STATUS).asText().isEmpty());
        assertTrue(data.path(JsonOperations.IS_LOCAL).isValueNode());
        assertFalse(data.path(JsonOperations.IS_LOCAL).asBoolean());
        // delete a car
        data = mapper.createObjectNode().put(JSON.NAME, car.getId());
        validateData(JsonOperations.CAR, data, false);
        service.doDelete(JsonOperations.CAR, car.getId(), data, new JsonRequest(locale, JSON.V5, JSON.GET, 0));
        assertNull(manager.getById(car.getId()));
        // (re)create a car
        // add (PUT) a car
        JsonNode dataEx = mapper.createObjectNode().put(JsonOperations.ROAD, "MEC").put(JsonOperations.NUMBER, "31995");
        validateData(JsonOperations.CAR, dataEx, false);
        result = service.doPut(JsonOperations.CAR, car.getId(), dataEx,
            new JsonRequest(locale, JSON.V5, JSON.GET, 42));
        car = manager.getByRoadAndNumber("MEC", "31995");
        assertNotNull(car);
        validate(result);
        String carId = car.getId(); // final version of car ID for exception test
        // add (PUT) the same car again
        ex = assertThrows( JsonException.class, () ->
            service.doPut(JsonOperations.CAR, carId, dataEx,
                new JsonRequest(locale, JSON.V5, JSON.GET, 42)),
            "Expected exception not thrown");
        assertEquals(409, ex.getCode());
        assertEquals(42, ex.getId());
        assertEquals(
            "Unable to create new car with road number MEC 31995 since another car with that road number already exists.",
            ex.getMessage());
        // edit (POST) the added car
        String id = car.getId();
        data = mapper.createObjectNode()
                .put(JSON.NAME, id) // can't change this directly
                .put(JsonOperations.ROAD, "BM")
                .put(JsonOperations.NUMBER, "216")
                .put(JSON.RFID, "1234567890AB")
                .put(JsonOperations.HAZARDOUS, true)
                .put(JsonOperations.CABOOSE, true)
                .put(JsonOperations.PASSENGER, true)
                .put(JsonOperations.FRED, true)
                .put(JsonOperations.UTILITY, true)
                .put(JsonOperations.OUT_OF_SERVICE, true)
                .put(JsonOperations.LOCATION_UNKNOWN, false)
                .put(JsonOperations.BUILT, "13-1234")
                .put(JsonOperations.WEIGHT, 160)
                .put(JsonOperations.WEIGHT_TONS, "160")
                .put(JsonOperations.TYPE, "Combine-MOW");
        validateData(JsonOperations.CAR, data, false);
        result = service.doPost(JsonOperations.CAR, id, data, new JsonRequest(locale, JSON.V5, JSON.GET, 42));
        car = manager.getById(id); // id invalidated by changing name and number
        assertNull(car);
        car = manager.getByRoadAndNumber("BM", "216"); // get by name and number
        assertNotNull(car);
        validate(result);
        assertEquals(JsonOperations.CAR, result.path(JSON.TYPE).asText());
        assertTrue(result.path(JSON.METHOD).isMissingNode());
        assertEquals(42, result.path(JSON.ID).asInt());
        data = result.path(JSON.DATA);
        // rename not always present
        assertEquals( 40, data.size(), "Number of properties in Car");
        // TODO: verify against car and known values
        assertEquals(car.getId(), data.path(JSON.NAME).asText());
        assertEquals(car.getRoadName(), data.path(JsonOperations.ROAD).asText());
        assertEquals(car.getNumber(), data.path(JsonOperations.NUMBER).asText());
        assertTrue(data.path(JSON.RFID).isValueNode());
        assertEquals(car.getRfid(), data.path(JSON.RFID).asText());
        assertNotEquals( car.getTypeName(), data.path(JsonOperations.TYPE).asText());
        assertEquals("Combine", data.path(JsonOperations.TYPE).asText());
        assertEquals("MOW", data.path(JsonOperations.CAR_SUB_TYPE).asText());
        assertEquals(car.getLengthInteger(), data.path(JsonOperations.LENGTH).asInt());
        //        assertEquals(car.getAdjustedWeightTons(), data.path(JsonOperations.WEIGHT).asInt());
        assertEquals(car.getWeightTons(), data.path(JsonOperations.WEIGHT_TONS).asText());
        assertEquals(car.getColor(), data.path(JsonOperations.COLOR).asText());
        assertEquals(car.getOwnerName(), data.path(JsonOperations.OWNER).asText());
        assertEquals(car.getComment(), data.path(JsonOperations.COMMENT).asText());
        assertEquals(car.getLocationId(), data.path(JSON.LOCATION).path(JSON.NAME).asText());
        assertEquals(car.getTrackId(),
                data.path(JSON.LOCATION).path(JSON.TRACK).path(JSON.NAME).asText());
        assertTrue(data.path(JsonOperations.DESTINATION).isValueNode());
        assertTrue(data.path(JsonOperations.DESTINATION).isNull());
        assertEquals(car.getLoadName(), data.path(JsonOperations.LOAD).asText());
        assertTrue(data.path(JsonOperations.HAZARDOUS).isValueNode());
        assertTrue(data.path(JsonOperations.HAZARDOUS).asBoolean());
        assertTrue(data.path(JsonOperations.CABOOSE).isValueNode());
        assertTrue(data.path(JsonOperations.CABOOSE).asBoolean());
        assertTrue(data.path(JsonOperations.PASSENGER).isValueNode());
        assertTrue(data.path(JsonOperations.PASSENGER).asBoolean());
        assertTrue(data.path(JsonOperations.FRED).isValueNode());
        assertTrue(data.path(JsonOperations.FRED).asBoolean());
        assertTrue(data.path(JsonOperations.PICKUP_COMMENT).isValueNode());
        assertTrue(data.path(JsonOperations.PICKUP_COMMENT).asText().isEmpty());
        assertTrue(data.path(JsonOperations.SETOUT_COMMENT).isValueNode());
        assertTrue(data.path(JsonOperations.SETOUT_COMMENT).asText().isEmpty());
        assertTrue(data.path(JsonOperations.KERNEL).isValueNode());
        assertTrue(data.path(JsonOperations.KERNEL).asText().isEmpty());
        assertTrue(data.path(JsonOperations.UTILITY).isValueNode());
        assertTrue(data.path(JsonOperations.UTILITY).asBoolean());
        assertTrue(data.path(JsonOperations.FINAL_DESTINATION).isValueNode());
        assertTrue(data.path(JsonOperations.FINAL_DESTINATION).isNull());
        assertTrue(data.path(JsonOperations.RETURN_WHEN_EMPTY).isValueNode());
        assertTrue(data.path(JsonOperations.RETURN_WHEN_EMPTY).isNull());
        assertFalse(data.path(JsonOperations.OUT_OF_SERVICE).isMissingNode());
        assertTrue(data.path(JsonOperations.OUT_OF_SERVICE).asBoolean());
        assertFalse(data.path(JsonOperations.LOCATION_UNKNOWN).isMissingNode());
        assertFalse(data.path(JsonOperations.LOCATION_UNKNOWN).asBoolean());
        assertFalse(data.path(JsonOperations.TRAIN_ID).isMissingNode());
        assertTrue(data.path(JsonOperations.TRAIN_ID).isNull());
        assertTrue(data.path(JSON.STATUS).isValueNode());
        assertEquals( "&lt;O&gt; ", data.path(JSON.STATUS).asText(),
            "Out of service status");
        assertTrue(data.path(JsonOperations.IS_LOCAL).isValueNode());
        assertFalse(data.path(JsonOperations.IS_LOCAL).asBoolean());
        // edit a non-existent car
        assertNull(manager.getById("-1"));

        JsonException exc = assertThrowsExactly( JsonException.class, () ->
            service.doPost(JsonOperations.CAR, "-1", NullNode.getInstance(),
                new JsonRequest(locale, JSON.V5, JSON.PUT, 0)));
        assertEquals( 404, exc.getJsonMessage().path(JSON.DATA).path(JsonException.CODE).asInt() );

    }

    @Test
    public void testEngine() throws JsonException {
        // try a non-existent engine
        JsonException nonExistentEx = assertThrows( JsonException.class, () ->
            service.doGet(JsonOperations.ENGINE, "", NullNode.getInstance(),
                    new JsonRequest(locale, JSON.V5, JSON.GET, 0)),
            "Expected exception not thrown");
        assertEquals(404, nonExistentEx.getCode());
        // get a known engine
        EngineManager manager = InstanceManager.getDefault(EngineManager.class);
        Engine engine = manager.getByRoadAndNumber("PC", "5524");
        JsonNode result = service.doGet(JsonOperations.ENGINE, engine.getId(), NullNode.getInstance(),
                new JsonRequest(locale, JSON.V5, JSON.GET, 42));
        validate(result);
        assertEquals(JsonOperations.ENGINE, result.path(JSON.TYPE).asText());
        assertTrue(result.path(JSON.METHOD).isMissingNode());
        assertEquals(42, result.path(JSON.ID).asInt());
        JsonNode data = result.path(JSON.DATA);
        assertEquals( 25, data.size(), "Number of properties in Engine");
        assertEquals(engine.getId(), data.path(JSON.NAME).asText());
        assertEquals(engine.getRoadName(), data.path(JsonOperations.ROAD).asText());
        assertEquals(engine.getNumber(), data.path(JsonOperations.NUMBER).asText());
        assertTrue(data.path(JSON.RFID).isValueNode());
        assertTrue(data.path(JSON.WHERELASTSEEN).isValueNode());
        assertTrue(data.path(JSON.WHENLASTSEEN).isValueNode());
        assertEquals(engine.getRfid(), data.path(JSON.RFID).asText());
        assertTrue(data.path(JSON.WHERELASTSEEN).isNull());
        assertTrue(data.path(JSON.WHENLASTSEEN).isNull());
        assertEquals(engine.getTypeName(), data.path(JsonOperations.TYPE).asText());
        assertTrue(data.path(JsonOperations.CAR_SUB_TYPE).asText().isEmpty());
        assertEquals(engine.getLengthInteger(), data.path(JsonOperations.LENGTH).asInt());
        assertEquals(engine.getAdjustedWeightTons(), data.path(JsonOperations.WEIGHT).asInt());
        assertEquals(engine.getWeightTons(), data.path(JsonOperations.WEIGHT_TONS).asText());
        assertEquals(engine.getColor(), data.path(JsonOperations.COLOR).asText());
        assertEquals(engine.getOwnerName(), data.path(JsonOperations.OWNER).asText());
        assertEquals(engine.getComment(), data.path(JsonOperations.COMMENT).asText());
        assertEquals(engine.getLocationId(), data.path(JSON.LOCATION).path(JSON.NAME).asText());
        assertEquals(engine.getTrackId(),
                data.path(JSON.LOCATION).path(JSON.TRACK).path(JSON.NAME).asText());
        assertTrue(data.path(JsonOperations.DESTINATION).isValueNode());
        assertTrue(data.path(JsonOperations.DESTINATION).isNull());
        assertTrue(data.path(JsonConsist.CONSIST).isValueNode());
        assertEquals("C14", data.path(JsonConsist.CONSIST).asText());
        assertFalse(data.path(JsonOperations.OUT_OF_SERVICE).isMissingNode());
        assertFalse(data.path(JsonOperations.OUT_OF_SERVICE).asBoolean());
        assertFalse(data.path(JsonOperations.LOCATION_UNKNOWN).isMissingNode());
        assertFalse(data.path(JsonOperations.LOCATION_UNKNOWN).asBoolean());
        assertFalse(data.path(JsonOperations.TRAIN_ID).isMissingNode());
        assertTrue(data.path(JsonOperations.TRAIN_ID).isNull());
        assertTrue(data.path(JsonOperations.MODEL).isValueNode());
        assertEquals("SD45", data.path(JsonOperations.MODEL).asText());

        // add (PUT) an engine
        data = mapper.createObjectNode().put(JsonOperations.ROAD, "MEC").put(JsonOperations.NUMBER, "3402");
        validateData(JsonOperations.ENGINE, data, false);
        result = service.doPut(JsonOperations.ENGINE, "", data, new JsonRequest(locale, JSON.V5, JSON.GET, 42));
        engine = manager.getByRoadAndNumber("MEC", "3402");
        assertNotNull(engine);
        validate(result);
        assertEquals(JsonOperations.ENGINE, result.path(JSON.TYPE).asText());
        assertTrue(result.path(JSON.METHOD).isMissingNode());
        assertEquals(42, result.path(JSON.ID).asInt());
        data = result.path(JSON.DATA);
        assertEquals( 25, data.size(), "Number of properties in Engine");
        assertEquals(engine.getId(), data.path(JSON.NAME).asText());
        assertEquals(engine.getRoadName(), data.path(JsonOperations.ROAD).asText());
        assertEquals(engine.getNumber(), data.path(JsonOperations.NUMBER).asText());
        assertTrue(data.path(JSON.RFID).isValueNode());
        assertTrue(data.path(JSON.WHERELASTSEEN).isValueNode());
        assertTrue(data.path(JSON.WHENLASTSEEN).isValueNode());
        assertEquals(engine.getRfid(), data.path(JSON.RFID).asText());
        assertTrue(data.path(JSON.WHERELASTSEEN).isNull());
        assertTrue(data.path(JSON.WHENLASTSEEN).isNull());
        assertEquals(engine.getTypeName(), data.path(JsonOperations.TYPE).asText());
        assertTrue(data.path(JsonOperations.CAR_SUB_TYPE).asText().isEmpty());
        assertEquals(engine.getLengthInteger(), data.path(JsonOperations.LENGTH).asInt());
        assertEquals(Double.parseDouble(engine.getWeight()), data.path(JsonOperations.WEIGHT).asDouble(), 0.0);

        // engine.getWeightTons() currently returns empty String if unset.
        // Double.parseDouble(engine.getWeightTons()) therefore throws NumberFormatException
        assertEquals(engine.getWeightTons(), data.path(JsonOperations.WEIGHT_TONS).asText());
        assertEquals(0.0, data.path(JsonOperations.WEIGHT_TONS).asDouble(), 0.0);

        assertEquals(engine.getColor(), data.path(JsonOperations.COLOR).asText());
        assertEquals(engine.getOwnerName(), data.path(JsonOperations.OWNER).asText());
        assertEquals(engine.getComment(), data.path(JsonOperations.COMMENT).asText());
        assertEquals(engine.getLocationId(), data.path(JSON.LOCATION).path(JSON.NAME).asText());
        assertEquals(engine.getTrackId(),
                data.path(JSON.LOCATION).path(JSON.TRACK).path(JSON.NAME).asText());
        assertTrue(data.path(JsonOperations.DESTINATION).isValueNode());
        assertTrue(data.path(JsonOperations.DESTINATION).isNull());
        assertTrue(data.path(JsonConsist.CONSIST).isValueNode());
        assertTrue(data.path(JsonConsist.CONSIST).asText().isEmpty());
        assertFalse(data.path(JsonOperations.OUT_OF_SERVICE).isMissingNode());
        assertFalse(data.path(JsonOperations.OUT_OF_SERVICE).asBoolean());
        assertFalse(data.path(JsonOperations.LOCATION_UNKNOWN).isMissingNode());
        assertFalse(data.path(JsonOperations.LOCATION_UNKNOWN).asBoolean());
        assertFalse(data.path(JsonOperations.TRAIN_ID).isMissingNode());
        assertTrue(data.path(JsonOperations.TRAIN_ID).isNull());
        assertTrue(data.path(JsonOperations.MODEL).isValueNode());
        assertTrue(data.path(JsonOperations.MODEL).asText().isEmpty());
        // delete an engine
        data = mapper.createObjectNode().put(JSON.NAME, engine.getId());
        validateData(JsonOperations.ENGINE, data, false);
        service.doDelete(JsonOperations.ENGINE, engine.getId(), data, new JsonRequest(locale, JSON.V5, JSON.GET, 0));
        assertNull(manager.getById(engine.getId()));
        // (re)create an engine
        // add (PUT) an engine
        data = mapper.createObjectNode().put(JsonOperations.ROAD, "MEC").put(JsonOperations.NUMBER, "3402");
        validateData(JsonOperations.ENGINE, data, false);
        result = service.doPut(JsonOperations.ENGINE, engine.getId(), data,
                new JsonRequest(locale, JSON.V5, JSON.GET, 42));
        engine = manager.getByRoadAndNumber("MEC", "3402");
        assertNotNull(engine);
        validate(result);
        // add (PUT) the same engine again
        var engineEx2 = engine; // final
        var dataEx2 = data; // final
        JsonException ex = assertThrows( JsonException.class, () ->
            service.doPut(JsonOperations.ENGINE, engineEx2.getId(), dataEx2,
                new JsonRequest(locale, JSON.V5, JSON.GET, 42)),
            "Expected exception not thrown");
        assertEquals(409, ex.getCode());
        assertEquals(42, ex.getId());
        assertEquals(
            "Unable to create new engine with road number MEC 3402 since another engine with that road number already exists.",
            ex.getMessage());

        // edit (POST) the added engine
        String id = engine.getId();
        data = mapper.createObjectNode()
                .put(JSON.NAME, id) // can't change this directly
                .put(JsonOperations.ROAD, "BM")
                .put(JsonOperations.NUMBER, "216")
                .put(JSON.RFID, "1234567890AB")
                .put(JsonOperations.MODEL, "SD 40-2")
                .put(JsonOperations.BUILT, "10-1978")
                .put(JsonOperations.WEIGHT, 242)
                .put(JsonOperations.WEIGHT_TONS, "242")
                .put(JsonOperations.TYPE, "Diesel-Rebuild");
        validateData(JsonOperations.ENGINE, data, false);
        result = service.doPost(JsonOperations.ENGINE, id, data, new JsonRequest(locale, JSON.V5, JSON.GET, 42));
        // id invalidated by changing name and number
        engine = manager.getById(id);
        assertNull(engine);
        // get by name and number
        engine = manager.getByRoadAndNumber("BM", "216");
        assertNotNull(engine);
        validate(result);
        assertEquals(JsonOperations.ENGINE, result.path(JSON.TYPE).asText());
        assertTrue(result.path(JSON.METHOD).isMissingNode());
        assertEquals(42, result.path(JSON.ID).asInt());
        data = result.path(JSON.DATA);
        // rename not always present
        assertEquals( 26, data.size(), "Number of properties in Engine");
        assertEquals("BM216", data.path(JSON.NAME).asText());
        assertEquals("BM", data.path(JsonOperations.ROAD).asText());
        assertEquals("216", data.path(JsonOperations.NUMBER).asText());
        assertTrue(data.path(JSON.RFID).isValueNode());
        assertEquals("ID1234567890AB", data.path(JSON.RFID).asText());
        assertNotEquals( engine.getTypeName(), data.path(JsonOperations.TYPE).asText());
        assertEquals("Diesel", data.path(JsonOperations.TYPE).asText());
        assertEquals("Rebuild", data.path(JsonOperations.CAR_SUB_TYPE).asText());
        assertEquals(0, data.path(JsonOperations.LENGTH).asInt());
        assertEquals(engine.getAdjustedWeightTons(), data.path(JsonOperations.WEIGHT).asInt());
        assertEquals(engine.getWeightTons(), data.path(JsonOperations.WEIGHT_TONS).asText());
        assertEquals(engine.getColor(), data.path(JsonOperations.COLOR).asText());
        assertEquals(engine.getOwnerName(), data.path(JsonOperations.OWNER).asText());
        assertEquals(engine.getComment(), data.path(JsonOperations.COMMENT).asText());
        assertEquals(engine.getLocationId(), data.path(JSON.LOCATION).path(JSON.NAME).asText());
        assertEquals(engine.getTrackId(),
                data.path(JSON.LOCATION).path(JSON.TRACK).path(JSON.NAME).asText());
        assertTrue(data.path(JsonOperations.DESTINATION).isValueNode());
        assertTrue(data.path(JsonOperations.DESTINATION).isNull());
        assertTrue(data.path(JsonConsist.CONSIST).isValueNode());
        assertTrue(data.path(JsonConsist.CONSIST).asText().isEmpty());
        assertFalse(data.path(JsonOperations.OUT_OF_SERVICE).isMissingNode());
        assertFalse(data.path(JsonOperations.OUT_OF_SERVICE).asBoolean());
        assertFalse(data.path(JsonOperations.LOCATION_UNKNOWN).isMissingNode());
        assertFalse(data.path(JsonOperations.LOCATION_UNKNOWN).asBoolean());
        assertFalse(data.path(JsonOperations.TRAIN_ID).isMissingNode());
        assertTrue(data.path(JsonOperations.TRAIN_ID).isNull());
        assertEquals("SD 40-2", data.path(JsonOperations.MODEL).asText());
        JUnitAppender.assertErrorMessage("Rolling stock (BM 216) length () is not valid");
        // edit a non-existent car
        assertNull(manager.getById("-1"));
        JsonException exc = assertThrowsExactly( JsonException.class, () ->
            service.doPost(JsonOperations.ENGINE, "-1", NullNode.getInstance(),
                new JsonRequest(locale, JSON.V5, JSON.PUT, 0)));
        assertEquals( 404, exc.getJsonMessage().path(JSON.DATA).path(JsonException.CODE).asInt() );

    }

    @Test
    public void testLocation() throws JsonException {
        // try a non-existent location
        JsonException ex = assertThrows( JsonException.class, () ->
            service.doGet(JSON.LOCATION, "", NullNode.getInstance(),
                    new JsonRequest(locale, JSON.V5, JSON.GET, 0)),
            "Expected exception not thrown");
        assertEquals(404, ex.getCode());
        JUnitAppender.assertErrorMessage("Unable to get location id [].");
        // get a known location
        LocationManager manager = InstanceManager.getDefault(LocationManager.class);
        Location location = manager.getLocationById("20");
        JsonNode result = service.doGet(JSON.LOCATION, location.getId(), NullNode.getInstance(),
                new JsonRequest(locale, JSON.V5, JSON.GET, 42));
        validate(result);
        assertEquals(JSON.LOCATION, result.path(JSON.TYPE).asText());
        assertTrue(result.path(JSON.METHOD).isMissingNode());
        assertEquals(42, result.path(JSON.ID).asInt());
        JsonNode data = result.path(JSON.DATA);
        assertEquals( 7, data.size(), "Number of properties in Location");
        assertEquals(location.getId(), data.path(JSON.NAME).asText());
        assertEquals(location.getName(), data.path(JSON.USERNAME).asText());
        assertTrue(data.path(JSON.COMMENT).isValueNode());
        assertEquals(location.getCommentWithColor(), data.path(JSON.COMMENT).asText());
        assertTrue(data.path(JsonReporter.REPORTER).isValueNode());
        assertTrue(data.path(JsonReporter.REPORTER).asText().isEmpty());
        assertTrue(data.path(JsonOperations.CAR_TYPE).isArray());
        assertEquals(4, data.path(JsonOperations.CAR_TYPE).size());
        assertEquals("Boxcar", data.path(JsonOperations.CAR_TYPE).path(0).asText());
        assertEquals("Caboose", data.path(JsonOperations.CAR_TYPE).path(1).asText());
        assertEquals("Flat", data.path(JsonOperations.CAR_TYPE).path(2).asText());
        assertEquals("Diesel", data.path(JsonOperations.CAR_TYPE).path(3).asText());
        assertTrue(data.path(JSON.TRACK).isArray());
        assertEquals(1, data.path(JSON.TRACK).size());
        assertEquals("20s1", data.path(JSON.TRACK).path(0).path(JSON.NAME).asText());
        assertEquals("NI Yard", data.path(JSON.TRACK).path(0).path(JSON.USERNAME).asText());
        assertTrue(data.path(JSON.TRACK).path(0).path(JsonReporter.REPORTER).isValueNode());
        assertTrue(data.path(JSON.TRACK).path(0).path(JsonReporter.REPORTER).asText().isEmpty());
        // add (PUT) a location
        data = mapper.createObjectNode().put(JSON.USERNAME, "Test Site");
        validateData(JSON.LOCATION, data, false);
        result = service.doPut(JSON.LOCATION, "42", data, new JsonRequest(locale, JSON.V5, JSON.GET, 42));
        location = manager.getLocationById("42");
        assertNull(location);
        location = manager.getLocationByName("Test Site");
        assertNotNull(location);
        validate(result);
        assertEquals(JSON.LOCATION, result.path(JSON.TYPE).asText());
        assertTrue(result.path(JSON.METHOD).isMissingNode());
        assertEquals(42, result.path(JSON.ID).asInt());
        data = result.path(JSON.DATA);
        assertEquals( 7, data.size(), "Number of properties in Location");
        assertEquals(location.getId(), data.path(JSON.NAME).asText());
        assertEquals(location.getName(), data.path(JSON.USERNAME).asText());
        // delete a location
        data = mapper.createObjectNode().put(JSON.NAME, location.getId());
        validateData(JSON.LOCATION, data, false);
        service.doDelete(JSON.LOCATION, location.getId(), data,
                new JsonRequest(locale, JSON.V5, JSON.GET, 0));
        assertNull(manager.getLocationById(location.getId()));
        // (re)create a location
        // add (PUT) a location
        var dataEx = mapper.createObjectNode().put(JSON.USERNAME, "Test Site");
        validateData(JSON.LOCATION, data, false);
        result = service.doPut(JSON.LOCATION, "42", dataEx, new JsonRequest(locale, JSON.V5, JSON.GET, 42));
        location = manager.getLocationByName("Test Site");
        assertNotNull(location);
        validate(result);

        // add (PUT) the same location again with same name (id)
        var locIdEx = location.getId(); // final
        ex = assertThrows( JsonException.class, () ->
            service.doPut(JSON.LOCATION, locIdEx, dataEx,
                    new JsonRequest(locale, JSON.V5, JSON.GET, 42)),
            "Expected exception not thrown");
        assertEquals(409, ex.getCode());
        assertEquals(42, ex.getId());
        assertEquals("Unable to create object type location with name \"22\" since already exists.",
            ex.getMessage());
        // add (PUT) the same location again with same user name, different name
        // (id)
        ex = assertThrows( JsonException.class, () ->
            service.doPut(JSON.LOCATION, "42", dataEx,
                new JsonRequest(locale, JSON.V5, JSON.GET, 42)),
            "Expected exception not thrown");
        assertEquals(409, ex.getCode());
        assertEquals(42, ex.getId());
        assertEquals("Unable to create object type location with user name \"Test Site\" since already exists.",
            ex.getMessage());

        // edit (POST) the added location
        String id = location.getId();
        data = mapper.createObjectNode()
                .put(JSON.NAME, id) // can't change this
                .put(JSON.USERNAME, "Editted Site")
                .put(JSON.COMMENT, "A comment")
                .put(JsonReporter.REPORTER,
                        InstanceManager.getDefault(ReporterManager.class).provide("IR1").getSystemName());
        // TODO: put change to types?
        validateData(JSON.LOCATION, data, false);
        result = service.doPost(JSON.LOCATION, id, data, new JsonRequest(locale, JSON.V5, JSON.GET, 42));
        location = manager.getLocationById(id);
        assertNotNull(location);
        validate(result);
        assertEquals(JSON.LOCATION, result.path(JSON.TYPE).asText());
        assertTrue(result.path(JSON.METHOD).isMissingNode());
        assertEquals(42, result.path(JSON.ID).asInt());
        data = result.path(JSON.DATA);
        assertEquals( 7, data.size(), "Number of properties in Location");
        assertEquals(id, data.path(JSON.NAME).asText());
        assertEquals("Editted Site", data.path(JSON.USERNAME).asText());
        assertEquals("A comment", data.path(JSON.COMMENT).asText());
        assertEquals("IR1", data.path(JsonReporter.REPORTER).asText());
        assertTrue(data.path(JsonOperations.CAR_TYPE).isArray());
        assertEquals(4, data.path(JsonOperations.CAR_TYPE).size());
        assertTrue(data.path(JSON.TRACK).isArray());
        assertEquals(0, data.path(JSON.TRACK).size());
    }

    @Test
    public void testPutLocationNoName() {
        JsonException exc = assertThrowsExactly( JsonException.class, () ->
            service.doPut(JSON.LOCATION, "", mapper.createObjectNode(),
                new JsonRequest(locale, JSON.V5, JSON.PUT, 42)));
        assertEquals( "Property \"userName\" is required to create a new location.",
            exc.getJsonMessage().path(JSON.DATA).path(JsonException.MESSAGE).asText() );
    }

    @Test
    public void testTrack() throws JsonException {
        // try not including a location property
        JsonException ex = assertThrows( JsonException.class, () ->
            service.doGet(JSON.TRACK, "", NullNode.getInstance(),
                new JsonRequest(locale, JSON.V5, JSON.GET, 0)),
            "Expected exception not thrown");
        assertEquals(400, ex.getCode());
        assertEquals("Property \"location\" is required for type track.", ex.getMessage());

        // try a non-existent track in existing location
        ObjectNode messageEx = mapper.createObjectNode().put(JSON.LOCATION, "20");
        ex = assertThrows( JsonException.class, () ->
            service.doGet(JSON.TRACK, "", messageEx,
                new JsonRequest(locale, JSON.V5, JSON.GET, 0)),
            "Expected exception not thrown");
        assertEquals(404, ex.getCode());
        assertEquals("Object type track named \"\" not found.", ex.getMessage());

        // try a non-existent track in non-existent location
        ObjectNode messageEx2 = mapper.createObjectNode().put(JSON.LOCATION, "");
        ex = assertThrows( JsonException.class, () ->
            service.doGet(JSON.TRACK, "", messageEx2, new JsonRequest(locale, JSON.V5, JSON.GET, 0)),
            "Expected exception not thrown");
        assertEquals(404, ex.getCode());
        assertEquals("Object type location named \"\" not found.", ex.getMessage());

        // get a known track from a known location
        LocationManager manager = InstanceManager.getDefault(LocationManager.class);
        Location location = manager.getLocationById("20");
        assertNotNull(location);
        Track track = location.getTrackById("20s1");
        assertNotNull(track);
        ObjectNode message = mapper.createObjectNode().put(JSON.LOCATION, "20");
        JsonNode result = service.doGet(JSON.TRACK, track.getId(), message,
                new JsonRequest(locale, JSON.V5, JSON.GET, 42));
        validate(result);
        assertEquals(JSON.TRACK, result.path(JSON.TYPE).asText());
        assertTrue(result.path(JSON.METHOD).isMissingNode());
        assertEquals(42, result.path(JSON.ID).asInt());
        JsonNode data = result.path(JSON.DATA);
        assertEquals( 8, data.size(), "Number of properties in Track");
        assertEquals(track.getId(), data.path(JSON.NAME).asText());
        assertEquals(track.getName(), data.path(JSON.USERNAME).asText());
        assertTrue(data.path(JSON.COMMENT).isValueNode());
        assertEquals(track.getComment(), data.path(JSON.COMMENT).asText());
        assertTrue(data.path(JsonReporter.REPORTER).isValueNode());
        assertTrue(data.path(JsonReporter.REPORTER).asText().isEmpty());
        assertTrue(data.path(JsonOperations.CAR_TYPE).isArray());
        assertEquals(4, data.path(JsonOperations.CAR_TYPE).size());
        assertEquals("Boxcar", data.path(JsonOperations.CAR_TYPE).path(0).asText());
        assertEquals("Caboose", data.path(JsonOperations.CAR_TYPE).path(1).asText());
        assertEquals("Flat", data.path(JsonOperations.CAR_TYPE).path(2).asText());
        assertEquals("Diesel", data.path(JsonOperations.CAR_TYPE).path(3).asText());
        assertEquals(432, data.findPath(JSON.LENGTH).asInt());
        assertEquals("20", data.path(JSON.LOCATION).asText());
        assertEquals("Yard", data.path(JSON.TYPE).asText());
        // add (PUT) a track
        data = mapper.createObjectNode()
                .put(JSON.USERNAME, "Test Site")
                .put(JSON.TYPE, "Siding")
                .put(JSON.LOCATION, "20");
        validateData(JSON.TRACK, data, false);
        result = service.doPut(JSON.TRACK, "42", data, new JsonRequest(locale, JSON.V5, JSON.GET, 42));
        track = location.getTrackById("42");
        assertNull(track);
        track = location.getTrackByName("Test Site", "Siding");
        assertNotNull(track);
        validate(result);
        assertEquals(JSON.TRACK, result.path(JSON.TYPE).asText());
        assertTrue(result.path(JSON.METHOD).isMissingNode());
        assertEquals(42, result.path(JSON.ID).asInt());
        data = result.path(JSON.DATA);
        assertEquals( 8, data.size(), "Number of properties in Track");
        assertEquals(track.getId(), data.path(JSON.NAME).asText());
        assertEquals(track.getName(), data.path(JSON.USERNAME).asText());
        // delete a location
        data = mapper.createObjectNode()
                .put(JSON.NAME, track.getId())
                .put(JSON.LOCATION, location.getId());
        validateData(JSON.TRACK, data, false);
        service.doDelete(JSON.TRACK, track.getId(), data, new JsonRequest(locale, JSON.V5, JSON.GET, 0));
        assertNull(location.getTrackById(track.getId()));
        // (re)create a location
        // add (PUT) a location
        data = mapper.createObjectNode()
                .put(JSON.USERNAME, "Test Site")
                .put(JSON.LOCATION, "20")
                .put(JSON.TYPE, "Siding");
        validateData(JSON.TRACK, data, false);
        result = service.doPut(JSON.TRACK, "42", data,
            new JsonRequest(locale, JSON.V5, JSON.GET, 42));
        track = location.getTrackByName("Test Site", "Siding");
        assertNotNull(track);
        validate(result);
        // add (PUT) the same track again with same name (id)
        var trackIdEx = track.getId(); // final
        var dataEx = data; // final
        ex = assertThrows( JsonException.class, () ->
            service.doPut(JSON.TRACK, trackIdEx, dataEx,
                new JsonRequest(locale, JSON.V5, JSON.GET, 42)),
            "Expected exception not thrown");
        assertEquals(409, ex.getCode());
        assertEquals(42, ex.getId());
        assertEquals("Unable to create object type track with name \"20s3\" since already exists.",
            ex.getMessage());

        // add (PUT) the same track again with same user name, different name
        // (id)
        ex = assertThrows( JsonException.class, () ->
            service.doPut(JSON.TRACK, "42", dataEx,
                new JsonRequest(locale, JSON.V5, JSON.GET, 42)),
            "Expected exception not thrown");
        assertEquals(409, ex.getCode());
        assertEquals(42, ex.getId());
        assertEquals("Unable to create object type track with user name \"Test Site\" since already exists.",
            ex.getMessage());

        // edit (POST) the added location
        String id = track.getId();
        data = mapper.createObjectNode()
                .put(JSON.NAME, id) // can't change this
                .put(JSON.USERNAME, "Editted Site")
                .put(JSON.COMMENT, "A comment")
                .put(JSON.LENGTH, 1000)
                .put(JSON.LOCATION, location.getId())
                .put(JsonReporter.REPORTER,
                        InstanceManager.getDefault(ReporterManager.class).provide("IR1").getSystemName());
        // TODO: put change to types?
        validateData(JSON.TRACK, data, false);
        result = service.doPost(JSON.TRACK, id, data, new JsonRequest(locale, JSON.V5, JSON.GET, 42));
        track = location.getTrackById(id);
        assertNotNull(track);
        assertNotNull(location);
        validate(result);
        assertEquals(JSON.TRACK, result.path(JSON.TYPE).asText());
        assertTrue(result.path(JSON.METHOD).isMissingNode());
        assertEquals(42, result.path(JSON.ID).asInt());
        data = result.path(JSON.DATA);
        assertEquals( 8, data.size(), "Number of properties in Track");
        assertEquals(id, data.path(JSON.NAME).asText());
        assertEquals("Editted Site", data.path(JSON.USERNAME).asText());
        assertEquals("A comment", data.path(JSON.COMMENT).asText());
        assertEquals(1000, data.path(JSON.LENGTH).asInt());
        assertEquals("IR1", data.path(JsonReporter.REPORTER).asText());
        assertTrue(data.path(JsonOperations.CAR_TYPE).isArray());
        assertEquals(4, data.path(JsonOperations.CAR_TYPE).size());
    }

    @Test
    public void testPutTrackNoUserName() {
        JsonException exc = assertThrowsExactly( JsonException.class, () ->
            service.doPut(JSON.TRACK, "", mapper.createObjectNode(),
                new JsonRequest(locale, JSON.V5, JSON.PUT, 42)));
        assertEquals( "Property \"userName\" is required to create a new track.",
            exc.getJsonMessage().path(JSON.DATA).path(JsonException.MESSAGE).asText() );
    }

    @Test
    public void testPutTrackNoType() {
        JsonException exc = assertThrowsExactly( JsonException.class, () ->
            service.doPut(JSON.TRACK, "", mapper.createObjectNode().put(JSON.USERNAME, "test"),
                new JsonRequest(locale, JSON.V5, JSON.PUT, 42)));
        assertEquals( "Property \"type\" is required to create a new track.",
            exc.getJsonMessage().path(JSON.DATA).path(JsonException.MESSAGE).asText());
    }

    @Test
    public void testPutTrackNoLocation() {
        JsonException exc = assertThrowsExactly( JsonException.class, () ->
            service.doPut(JSON.TRACK, "",
                mapper.createObjectNode().put(JSON.USERNAME, "test").put(JSON.TYPE, "test"),
                new JsonRequest(locale, JSON.V5, JSON.PUT, 42)));
        assertEquals( "Property \"location\" is required to create a new track.",
            exc.getJsonMessage().path(JSON.DATA).path(JsonException.MESSAGE).asText());
    }

    @Test
    public void testPutTrackInvalidLocation() {
        JsonException exc = assertThrowsExactly( JsonException.class, () ->
            service.doPut(JSON.TRACK, "",
                mapper.createObjectNode().put(JSON.USERNAME, "test").put(JSON.TYPE, "test").put(JSON.LOCATION,
                        "invalid-location"),
                new JsonRequest(locale, JSON.V5, JSON.PUT, 42)));
        assertEquals( "Object type location named \"invalid-location\" not found.",
            exc.getJsonMessage().path(JSON.DATA).path(JsonException.MESSAGE).asText());
    }

    @Test
    public void testDoGetListCarEngineRollingStock() throws JsonException {
        JsonNode result = service.doGetList(JsonOperations.CAR, NullNode.getInstance(),
                new JsonRequest(locale, JSON.V5, JSON.GET, 0));
        validate(result);
        assertEquals( 9, result.size(), "9 cars");
        result = service.doGetList(JsonOperations.ENGINE, NullNode.getInstance(),
                new JsonRequest(locale, JSON.V5, JSON.GET, 0));
        validate(result);
        assertEquals( 4, result.size(), "4 engines");
        result = service.doGetList(JsonOperations.ROLLING_STOCK, NullNode.getInstance(),
                new JsonRequest(locale, JSON.V5, JSON.GET, 0));
        validate(result);
        assertEquals( 13, result.size(), "13 rolling stock");
    }

    @Test
    public void testCarType() throws JsonException {
        // get the car types
        JsonNode result = service.doGetList(JsonOperations.CAR_TYPE, NullNode.getInstance(),
                new JsonRequest(locale, JSON.V5, JSON.GET, 0));
        validate(result);
        assertTrue(result.isArray());
        assertEquals(3, result.size());
        // add a car type
        result = service.doPut(JsonOperations.CAR_TYPE, "test1", NullNode.getInstance(),
                new JsonRequest(locale, JSON.V5, JSON.GET, 42));
        validate(result);
        assertEquals(JsonOperations.CAR_TYPE, result.path(JSON.TYPE).asText());
        assertEquals("test1", result.path(JSON.DATA).path(JSON.NAME).asText());
        assertTrue(result.path(JSON.DATA).path(JsonOperations.CARS).isArray());
        assertTrue(result.path(JSON.DATA).path(JSON.LOCATIONS).isArray());
        // get the added car type
        JsonNode result2 = service.doGet(JsonOperations.CAR_TYPE, "test1", NullNode.getInstance(),
                new JsonRequest(locale, JSON.V5, JSON.GET, 42));
        assertEquals(result, result2);
        // get the car types again (the list changed)
        result = service.doGetList(JsonOperations.CAR_TYPE, NullNode.getInstance(),
                new JsonRequest(locale, JSON.V5, JSON.GET, 0));
        validate(result);
        assertTrue(result.isArray());
        assertEquals(4, result.size());
        assertEquals(JsonOperations.CAR_TYPE, result.path(0).path(JSON.TYPE).asText());
        assertEquals("Boxcar", result.path(0).path(JSON.DATA).path(JSON.NAME).asText());
        assertTrue(result.path(0).path(JSON.DATA).path(JsonOperations.CARS).isArray());
        assertTrue(result.path(0).path(JSON.DATA).path(JSON.LOCATIONS).isArray());
        // rename the added car type
        result = service.doPost(JsonOperations.CAR_TYPE, "test1",
                mapper.createObjectNode().put(JSON.RENAME, "test2"),
                new JsonRequest(locale, JSON.V5, JSON.POST, 0));
        validate(result);
        assertEquals(JsonOperations.CAR_TYPE, result.path(JSON.TYPE).asText());
        assertEquals("test2", result.path(JSON.DATA).path(JSON.NAME).asText());
        assertEquals("test1", result.path(JSON.DATA).path(JSON.RENAME).asText());
        assertTrue(result.path(JSON.DATA).path(JsonOperations.CARS).isArray());
        assertTrue(result.path(JSON.DATA).path(JSON.LOCATIONS).isArray());
        // delete the renamed car type
        service.doDelete(JsonOperations.CAR_TYPE, "test2", NullNode.getInstance(),
                new JsonRequest(locale, JSON.V5, JSON.GET, 42));
        result = service.doGetList(JsonOperations.CAR_TYPE, NullNode.getInstance(),
                new JsonRequest(locale, JSON.V5, JSON.GET, 0));
        validate(result);
        assertTrue(result.isArray());
        assertEquals(3, result.size());
        // add empty name
        JsonException exc = assertThrowsExactly( JsonException.class, () ->
            service.doPut(JsonOperations.CAR_TYPE, "", NullNode.getInstance(),
                new JsonRequest(locale, JSON.V5, JSON.PUT, 0)));
        assertEquals( 400, exc.getJsonMessage().path(JSON.DATA).path(JsonException.CODE).asInt());

        // get missing name
        exc = assertThrowsExactly( JsonException.class, () ->
            service.doGet(JsonOperations.CAR_TYPE, "invalid-type", NullNode.getInstance(),
                new JsonRequest(locale, JSON.V5, JSON.GET, 0)));
        assertEquals( 404, exc.getJsonMessage().path(JSON.DATA).path(JsonException.CODE).asInt());
        // delete an in-use car type
        CarTypes manager = InstanceManager.getDefault(CarTypes.class);
        assertTrue(Arrays.asList(manager.getNames()).contains("Caboose"));
        JsonException ex = assertThrows(JsonException.class, () -> service.doDelete(JsonOperations.CAR_TYPE, "Caboose",
                NullNode.getInstance(), new JsonRequest(locale, JSON.V5, JSON.DELETE, 0)));
        assertEquals( 409, ex.getCode());
        assertTrue(ex.getAdditionalData().path(JSON.FORCE_DELETE).isTextual());
        assertTrue( Arrays.asList(manager.getNames()).contains("Caboose"));
        service.doDelete(JsonOperations.CAR_TYPE, "Caboose", ex.getAdditionalData(),
                new JsonRequest(locale, JSON.V5, JSON.DELETE, 0));
        assertFalse( Arrays.asList(manager.getNames()).contains("Caboose"));
    }

    @Test
    public void testKernel() throws JsonException {
        // get the kernels (should be none)
        JsonNode result = service.doGetList(JsonOperations.KERNEL, NullNode.getInstance(),
                new JsonRequest(locale, JSON.V5, JSON.GET, 0));
        validate(result);
        assertTrue(result.isArray());
        assertEquals(0, result.size());
        // create a kernel
        result = service.doPut(JsonOperations.KERNEL, "test1", NullNode.getInstance(),
                new JsonRequest(locale, JSON.V5, JSON.PUT, 42));
        validate(result);
        assertEquals(JsonOperations.KERNEL, result.path(JSON.TYPE).asText());
        assertEquals("test1", result.path(JSON.DATA).path(JSON.NAME).asText());
        assertEquals(0, result.path(JSON.DATA).path(JSON.LENGTH).asInt());
        assertEquals(0, result.path(JSON.DATA).path(JSON.WEIGHT).asInt());
        assertTrue(result.path(JSON.DATA).path(JsonOperations.CARS).isArray());
        // get the newly created kernel
        JsonNode result2 = service.doGet(JsonOperations.KERNEL, "test1", NullNode.getInstance(),
                new JsonRequest(locale, JSON.V5, JSON.GET, 42));
        assertEquals(result, result2);
        // get the kernels (should be one)
        result = service.doGetList(JsonOperations.KERNEL, NullNode.getInstance(),
                new JsonRequest(locale, JSON.V5, JSON.GET, 0));
        validate(result);
        assertTrue(result.isArray());
        assertEquals(1, result.size());
        assertEquals(JsonOperations.KERNEL, result.path(0).path(JSON.TYPE).asText());
        assertEquals("test1", result.path(0).path(JSON.DATA).path(JSON.NAME).asText());
        assertEquals(0, result.path(0).path(JSON.DATA).path(JSON.LENGTH).asInt());
        assertEquals(0, result.path(0).path(JSON.DATA).path(JSON.WEIGHT).asInt());
        assertTrue(result.path(0).path(JSON.DATA).path(JsonOperations.CARS).isArray());
        // rename the kernel
        result = service.doPost(JsonOperations.KERNEL, "test1",
                mapper.createObjectNode().put(JSON.RENAME, "test2"),
                new JsonRequest(locale, JSON.V5, JSON.POST, 0));
        validate(result);
        assertEquals(JsonOperations.KERNEL, result.path(JSON.TYPE).asText());
        assertEquals("test2", result.path(JSON.DATA).path(JSON.NAME).asText());
        assertEquals("test1", result.path(JSON.DATA).path(JSON.RENAME).asText());
        assertEquals(0, result.path(0).path(JSON.DATA).path(JSON.LENGTH).asInt());
        assertEquals(0, result.path(0).path(JSON.DATA).path(JSON.WEIGHT).asInt());
        assertTrue(result.path(JSON.DATA).path(JsonOperations.CARS).isArray());
        // delete the kernel
        service.doDelete(JsonOperations.KERNEL, "test2", NullNode.getInstance(),
                new JsonRequest(locale, JSON.V5, JSON.GET, 42));
        // get the kernels (should be empty)
        result = service.doGetList(JsonOperations.KERNEL, NullNode.getInstance(),
                new JsonRequest(locale, JSON.V5, JSON.GET, 0));
        validate(result);
        assertTrue(result.isArray());
        assertEquals(0, result.size());
        // create without name
        JsonException exc = assertThrowsExactly( JsonException.class, () ->
            service.doPut(JsonOperations.KERNEL, "", NullNode.getInstance(),
                new JsonRequest(locale, JSON.V5, JSON.PUT, 42)));
        assertEquals( 400, exc.getJsonMessage().path(JSON.DATA).path(JsonException.CODE).asInt());
    }

    @Test
    public void testDeleteNonExistantKernel() throws JsonException {
        assertNull(InstanceManager.getDefault(KernelManager.class).getKernelByName("invalid-name"));
        JsonException exc = assertThrowsExactly( JsonException.class, () ->
            service.doDelete(JsonOperations.KERNEL, "invalid-name", NullNode.getInstance(),
                new JsonRequest(locale, JSON.V5, JSON.DELETE, 42)));
        assertEquals( 404, exc.getJsonMessage().path(JSON.DATA).path(JsonException.CODE).asInt());
    }

    @Test
    public void testDeleteInUseKernel() throws JsonException {
        CarManager manager = InstanceManager.getDefault(CarManager.class);
        Kernel k1 = InstanceManager.getDefault(KernelManager.class).newKernel("test1");
        Car c1 = manager.newRS("road", "1");
        c1.setLength("40");
        c1.setWeight("1000");
        c1.setWeightTons("10");
        c1.setLoadName("L");
        c1.setKernel(k1);
        JsonException ex = assertThrows( JsonException.class, () ->
            service.doDelete(JsonOperations.KERNEL, "test1", NullNode.getInstance(),
                    new JsonRequest(locale, JSON.V5, JSON.GET, 42)),
            "Expected exception not thrown");
        assertEquals(409, ex.getCode());
        String deleteToken = ex.getAdditionalData().path(JSON.FORCE_DELETE).asText();
        assertTrue(ex.getJsonMessage().path(JSON.DATA).path(JSON.CONFLICT).isArray());
        assertEquals(1, ex.getJsonMessage().path(JSON.DATA).path(JSON.CONFLICT).size());
        assertEquals(JsonOperations.CAR,
            ex.getJsonMessage().path(JSON.DATA).path(JSON.CONFLICT).path(0).path(JSON.TYPE).asText());

        assertNotNull(deleteToken);
        ObjectNode data = service.getObjectMapper().createObjectNode().put(JSON.FORCE_DELETE, deleteToken);
        service.doDelete(JsonOperations.KERNEL, "test1", data, new JsonRequest(locale, JSON.V5, JSON.GET, 42));
        assertNull(c1.getKernel());
    }

    @Test
    public void testDoPutInvalidCar() {
        // put a car without a road
        JsonException ex = assertThrows( JsonException.class, () ->
            service.doPut(JsonOperations.CAR, "", service.getObjectMapper().createObjectNode()
                    .put(JSON.NUMBER, "1234"),
                    new JsonRequest(locale, JSON.V5, JSON.PUT, 0)),
            "Expected exception not thrown");
        assertEquals( 400, ex.getCode());
        assertEquals("Property \"road\" is required to create a new car.", ex.getMessage());

        // put a car without a number
        ex = assertThrows( JsonException.class, () ->
            service.doPut(JsonOperations.CAR, "", service.getObjectMapper().createObjectNode()
                    .put(JsonOperations.ROAD, "GNWR"),
                    new JsonRequest(locale, JSON.V5, JSON.PUT, 0)),
            "Expected exception not thrown");
        assertEquals( 400, ex.getCode());
        assertEquals("Property \"number\" is required to create a new car.", ex.getMessage());

        // put an already existing car by road and number
        Car car = InstanceManager.getDefault(CarManager.class).getById("CP777");
        assertNotNull(car);
        ex = assertThrows( JsonException.class, () ->
            service.doPut(JsonOperations.CAR, "", service.getObjectMapper().createObjectNode()
                    .put(JsonOperations.ROAD, car.getRoadName())
                    .put(JsonOperations.NUMBER, car.getNumber()),
                    new JsonRequest(locale, JSON.V5, JSON.PUT, 0)),
            "Expected exception not thrown");
        assertEquals( 409, ex.getCode());
        assertEquals(
            "Unable to create new car with road number CP 777 since another car with that road number already exists.",
            ex.getMessage());

        // put an already existing car by id
        ex = assertThrows( JsonException.class, () ->
            service.doPut(JsonOperations.CAR, car.getId(), service.getObjectMapper().createObjectNode()
                    .put(JsonOperations.ROAD, car.getRoadName())
                    .put(JsonOperations.NUMBER, car.getNumber()),
                    new JsonRequest(locale, JSON.V5, JSON.PUT, 0)),
            "Expected exception not thrown");
        assertEquals( 409, ex.getCode());
        assertEquals(
            "Unable to create new car with road number CP 777 since another car with that road number already exists.",
            ex.getMessage());

    }

    @Test
    public void testDoPutInvalidEngine() {
        // put an engine without a road
        JsonException ex = assertThrows( JsonException.class, () ->
            service.doPut(JsonOperations.ENGINE, "", service.getObjectMapper().createObjectNode()
                    .put(JsonOperations.NUMBER, "1234"),
                    new JsonRequest(locale, JSON.V5, JSON.PUT, 0)),
            "Expected exception not thrown");
        assertEquals( 400, ex.getCode());
        assertEquals("Property \"road\" is required to create a new engine.",
            ex.getMessage());

        // put an engine without a number
        ex = assertThrows( JsonException.class, () ->
            service.doPut(JsonOperations.ENGINE, "", service.getObjectMapper().createObjectNode()
                    .put(JsonOperations.ROAD, "GNWR"),
                    new JsonRequest(locale, JSON.V5, JSON.PUT, 0)),
            "Expected exception not thrown");
        assertEquals( 400, ex.getCode());
        assertEquals("Property \"number\" is required to create a new engine.",
            ex.getMessage());

        // put an already existing engine by road and number
        Engine engine = InstanceManager.getDefault(EngineManager.class).getById("PC5016");
        assertNotNull(engine);
        ex = assertThrows( JsonException.class, () ->
            service.doPut(JsonOperations.ENGINE, "", service.getObjectMapper().createObjectNode()
                    .put(JsonOperations.ROAD, engine.getRoadName())
                    .put(JsonOperations.NUMBER, engine.getNumber()),
                    new JsonRequest(locale, JSON.V5, JSON.PUT, 0)),
            "Expected exception not thrown");
        assertEquals( 409, ex.getCode());
        assertEquals(
            "Unable to create new engine with road number PC 5016 since another engine with that road number already exists.",
            ex.getMessage());

        // put an already existing engine by id
        ex = assertThrows( JsonException.class, () ->
            service.doPut(JsonOperations.ENGINE, engine.getId(), service.getObjectMapper().createObjectNode()
                    .put(JsonOperations.ROAD, engine.getRoadName())
                    .put(JsonOperations.NUMBER, engine.getNumber()),
                    new JsonRequest(locale, JSON.V5, JSON.PUT, 0)),
            "Expected exception not thrown");
        assertEquals( 409, ex.getCode());
        assertEquals(
            "Unable to create new engine with road number PC 5016 since another engine with that road number already exists.",
            ex.getMessage());

    }

    @Test
    public void testDoDeleteInvalidType() {
        JsonException ex = assertThrows( JsonException.class, () ->
            service.doDelete("invalid-type", "", NullNode.getInstance(),
                    new JsonRequest(locale, JSON.V5, JSON.DELETE, 0)),
            "Expected exception not thrown");
        assertEquals( 405, ex.getCode());
    }

    @Test
    public void testPostLocationInvalidReporter() throws JsonException {
        Location location = InstanceManager.getDefault(LocationManager.class).getLocationById("1");
        assertNull(location.getReporter());
        JsonException ex = assertThrows( JsonException.class, () ->
            service.doPost(JSON.LOCATION, location.getId(),
                    service.getObjectMapper().createObjectNode().put(JsonReporter.REPORTER, "no-such-reporter"),
                    new JsonRequest(locale, JSON.V5, JSON.POST, 0)),
            "Expected exception not thrown");
        assertEquals( 404, ex.getCode());
        assertEquals("Object type reporter named \"no-such-reporter\" not found.",
            ex.getMessage());
    }

    @Test
    public void testPostTrackInvalidReporter() throws JsonException {
        Location location = InstanceManager.getDefault(LocationManager.class).getLocationById("1");
        Track track = location.getTrackById("1s1");
        assertNull(track.getReporter());
        JsonException ex = assertThrows( JsonException.class, () ->
            service.doPost(JSON.TRACK, track.getId(),
                    mapper.createObjectNode()
                            .put(JsonReporter.REPORTER, "no-such-reporter")
                            .put(JSON.LOCATION, location.getId()),
                    new JsonRequest(locale, JSON.V5, JSON.POST, 0)),
            "Expected exception not thrown");
        assertEquals( 404, ex.getCode());
        assertEquals("Object type reporter named \"no-such-reporter\" not found.",
            ex.getMessage());
    }

    @Test
    public void testMovingCar() throws JsonException {
        Car car = InstanceManager.getDefault(CarManager.class).getByRoadAndNumber("CP", "C10099");
        Location location = car.getLocation();
        assertEquals( "Caboose", car.getTypeName());
        assertEquals( "1", car.getLocation().getId());
        assertEquals( "1s1", car.getTrack().getId());
        // move to same location
        service.doPost(JsonOperations.CAR, car.getId(), mapper.createObjectNode()
                .set(JSON.LOCATION, mapper.createObjectNode()
                        .put(JSON.NAME, "1")
                        .set(JSON.TRACK, mapper.createObjectNode().put(JSON.NAME, "1s1"))),
                new JsonRequest(locale, JSON.V5, JSON.POST, 0));
        assertEquals( "1", car.getLocation().getId());
        assertEquals( "1s1", car.getTrack().getId());
        // move to location with empty id
        JsonException exc = assertThrowsExactly( JsonException.class, () ->
            service.doPost(JsonOperations.CAR, car.getId(), mapper.createObjectNode()
                .set(JSON.LOCATION,
                        mapper.createObjectNode()
                                .put(JSON.NAME, "")),
                new JsonRequest(locale, JSON.V5, JSON.POST, 0)));
        assertEquals( 404, exc.getJsonMessage().path(JSON.DATA).path(JsonException.CODE).asInt());

        // move to location with empty track id
        service.doPost(JsonOperations.CAR, car.getId(), mapper.createObjectNode()
                .set(JSON.LOCATION, mapper.createObjectNode()
                        .put(JSON.NAME, "1")
                        .set(JSON.TRACK, mapper.createObjectNode().put(JSON.NAME, ""))),
                new JsonRequest(locale, JSON.V5, JSON.POST, 0));
        assertEquals( "1", car.getLocation().getId());
        assertNull(car.getTrack());
        // move to non-existent location
        exc = assertThrowsExactly( JsonException.class, () ->
            service.doPost(JsonOperations.CAR, car.getId(), mapper.createObjectNode()
                .set(JSON.LOCATION, mapper.createObjectNode()
                        .put(JSON.NAME, "2")
                        .set(JSON.TRACK, mapper.createObjectNode().put(JSON.NAME, "2s1"))),
                new JsonRequest(locale, JSON.V5, JSON.POST, 0)));
        assertEquals( 404, exc.getJsonMessage().path(JSON.DATA).path(JsonException.CODE).asInt());

        assertEquals( "1", car.getLocation().getId());
        assertNull(car.getTrack());
        // move to non-existent track
        exc = assertThrowsExactly( JsonException.class, () ->
            service.doPost(JsonOperations.CAR, car.getId(), mapper.createObjectNode()
                .set(JSON.LOCATION, mapper.createObjectNode()
                        .put(JSON.NAME, "3")
                        .set(JSON.TRACK, mapper.createObjectNode().put(JSON.NAME, "invalid-track"))),
                new JsonRequest(locale, JSON.V5, JSON.POST, 0)));
        assertEquals( 404, exc.getJsonMessage().path(JSON.DATA).path(JsonException.CODE).asInt());

        assertEquals( "1", car.getLocation().getId());
        assertNull(car.getTrack());
        // move to new location
        service.doPost(JsonOperations.CAR, car.getId(), mapper.createObjectNode()
                .set(JSON.LOCATION, mapper.createObjectNode()
                        .put(JSON.NAME, "20")
                        .set(JSON.TRACK, mapper.createObjectNode().put(JSON.NAME, "20s1"))),
                new JsonRequest(locale, JSON.V5, JSON.POST, 0));
        assertEquals( "20", car.getLocation().getId());
        assertEquals( "20s1", car.getTrack().getId());
        // move to new location without track
        service.doPost(JsonOperations.CAR, car.getId(), mapper.createObjectNode()
                .set(JSON.LOCATION, mapper.createObjectNode()
                        .put(JSON.NAME, "3")),
                new JsonRequest(locale, JSON.V5, JSON.POST, 0));
        assertEquals( "3", car.getLocation().getId());
        assertNull(car.getTrack());
        // block car from track 1s1 by removing car type from track
        location.getTrackById("1s1").deleteTypeName(car.getTypeName());
        assertTrue( location.getTrackById("1s1").isRollingStockAccepted(car).
            startsWith(Track.TYPE) );

        // move to unusable location
        exc = assertThrowsExactly( JsonException.class, () ->
            service.doPost(JsonOperations.CAR, car.getId(), mapper.createObjectNode()
                .set(JSON.LOCATION, mapper.createObjectNode()
                        .put(JSON.NAME, "1")
                        .set(JSON.TRACK, mapper.createObjectNode().put(JSON.NAME, "1s1"))),
                new JsonRequest(locale, JSON.V5, JSON.POST, 0)));
        assertEquals( 409, exc.getJsonMessage().path(JSON.DATA).path(JsonException.CODE).asInt());

    }

    @Test
    public void testSettingDestinationForCar() throws JsonException {
        Car car = InstanceManager.getDefault(CarManager.class).getByRoadAndNumber("CP", "C10099");
        Location location = car.getLocation();
        car.setDestination(location, location.getTrackById("1s1"));
        assertEquals( "Caboose", car.getTypeName());
        assertEquals( "1", car.getDestination().getId());
        assertEquals( "1s1", car.getDestinationTrack().getId());
        // move to same location
        service.doPost(JsonOperations.CAR, car.getId(), mapper.createObjectNode()
                .set(JsonOperations.DESTINATION, mapper.createObjectNode()
                        .put(JSON.NAME, "1")
                        .set(JSON.TRACK, mapper.createObjectNode().put(JSON.NAME, "1s1"))),
                new JsonRequest(locale, JSON.V5, JSON.POST, 0));
        assertEquals( "1", car.getDestination().getId());
        assertEquals( "1s1", car.getDestinationTrack().getId());
        // move to location with empty id
        JsonException exc = assertThrowsExactly( JsonException.class, () ->
            service.doPost(JsonOperations.CAR, car.getId(), mapper.createObjectNode()
                .set(JsonOperations.DESTINATION,
                        mapper.createObjectNode()
                                .put(JSON.NAME, "")),
                new JsonRequest(locale, JSON.V5, JSON.POST, 0)));
        assertEquals( 404, exc.getJsonMessage().path(JSON.DATA).path(JsonException.CODE).asInt());

        // move to location with empty track id
        service.doPost(JsonOperations.CAR, car.getId(), mapper.createObjectNode()
                .set(JsonOperations.DESTINATION, mapper.createObjectNode()
                        .put(JSON.NAME, "1")
                        .set(JSON.TRACK, mapper.createObjectNode().put(JSON.NAME, ""))),
                new JsonRequest(locale, JSON.V5, JSON.POST, 0));
        assertEquals( "1", car.getDestination().getId());
        assertNull(car.getDestinationTrack());
        // move to non-existent location
        exc = assertThrowsExactly( JsonException.class, () ->
            service.doPost(JsonOperations.CAR, car.getId(), mapper.createObjectNode()
                .set(JsonOperations.DESTINATION, mapper.createObjectNode()
                        .put(JSON.NAME, "2")
                        .set(JSON.TRACK, mapper.createObjectNode().put(JSON.NAME, "2s1"))),
                new JsonRequest(locale, JSON.V5, JSON.POST, 0)));
        assertEquals( 404, exc.getJsonMessage().path(JSON.DATA).path(JsonException.CODE).asInt());
        assertEquals( "1", car.getDestination().getId());
        assertNull(car.getDestinationTrack());

        // move to non-existent track
        exc = assertThrowsExactly( JsonException.class, () ->
            service.doPost(JsonOperations.CAR, car.getId(), mapper.createObjectNode()
                .set(JsonOperations.DESTINATION, mapper.createObjectNode()
                        .put(JSON.NAME, "3")
                        .set(JSON.TRACK, mapper.createObjectNode().put(JSON.NAME, "invalid-track"))),
                new JsonRequest(locale, JSON.V5, JSON.POST, 0)));
        assertEquals( 404, exc.getJsonMessage().path(JSON.DATA).path(JsonException.CODE).asInt());
        assertEquals( "1", car.getDestination().getId());
        assertNull(car.getDestinationTrack());

        // move to new location
        service.doPost(JsonOperations.CAR, car.getId(), mapper.createObjectNode()
                .set(JsonOperations.DESTINATION, mapper.createObjectNode()
                        .put(JSON.NAME, "20")
                        .set(JSON.TRACK, mapper.createObjectNode().put(JSON.NAME, "20s1"))),
                new JsonRequest(locale, JSON.V5, JSON.POST, 0));
        assertEquals( "20", car.getDestination().getId());
        assertEquals( "20s1", car.getDestinationTrack().getId());
        // move to new location without track
        service.doPost(JsonOperations.CAR, car.getId(), mapper.createObjectNode()
                .set(JsonOperations.DESTINATION, mapper.createObjectNode()
                        .put(JSON.NAME, "3")),
                new JsonRequest(locale, JSON.V5, JSON.POST, 0));
        assertEquals( "3", car.getDestination().getId());
        assertNull(car.getDestinationTrack());
        // block car from track 1s1 by removing car type from track
        location.getTrackById("1s1").deleteTypeName(car.getTypeName());
        assertTrue( location.getTrackById("1s1").isRollingStockAccepted(car).
            startsWith(Track.TYPE) );
        // move to unusable destination
        exc = assertThrowsExactly( JsonException.class, () ->
            service.doPost(JsonOperations.CAR, car.getId(), mapper.createObjectNode()
                .set(JsonOperations.DESTINATION, mapper.createObjectNode()
                        .put(JSON.NAME, "1")
                        .set(JSON.TRACK, mapper.createObjectNode().put(JSON.NAME, "1s1"))),
                new JsonRequest(locale, JSON.V5, JSON.POST, 0)));
        assertEquals( 409, exc.getJsonMessage().path(JSON.DATA).path(JsonException.CODE).asInt());

    }

    @BeforeEach
    @Override
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
