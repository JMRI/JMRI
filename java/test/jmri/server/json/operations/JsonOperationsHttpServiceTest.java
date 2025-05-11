package jmri.server.json.operations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import jmri.*;
import jmri.jmrit.operations.locations.*;
import jmri.jmrit.operations.rollingstock.cars.*;
import jmri.jmrit.operations.rollingstock.engines.Engine;
import jmri.jmrit.operations.rollingstock.engines.EngineManager;
import jmri.server.json.*;
import jmri.server.json.consist.JsonConsist;
import jmri.server.json.reporter.JsonReporter;
import jmri.util.*;

public class JsonOperationsHttpServiceTest extends JsonHttpServiceTestBase<JsonOperationsHttpService> {

    @Test
    public void testGetRollingStock() {
        try {
            service.doGet(JsonOperations.ROLLING_STOCK, "", NullNode.getInstance(),
                    new JsonRequest(locale, JSON.V5, JSON.GET, 0));
            fail("Expected exception not thrown");
        } catch (JsonException ex) {
            assertThat(ex.getCode()).isEqualTo(405);
        }
    }

    @Test
    public void testDeleteInvalidType() {
        try {
            service.doGet("invalid-type", "", NullNode.getInstance(), new JsonRequest(locale, JSON.V5, JSON.DELETE, 0));
            fail("Expected exception not thrown");
        } catch (JsonException ex) {
            assertThat(ex.getCode()).isEqualTo(500);
        }
    }

    @Test
    public void testGetInvalidType() {
        try {
            service.doGet("invalid-type", "", NullNode.getInstance(), new JsonRequest(locale, JSON.V5, JSON.GET, 0));
            fail("Expected exception not thrown");
        } catch (JsonException ex) {
            assertThat(ex.getCode()).isEqualTo(500);
        }
    }

    @Test
    public void testPostInvalidType() {
        try {
            service.doPost("invalid-type", "", NullNode.getInstance(), new JsonRequest(locale, JSON.V5, JSON.GET, 0));
            fail("Expected exception not thrown");
        } catch (JsonException ex) {
            assertThat(ex.getCode()).isEqualTo(405);
        }
    }

    @Test
    public void testPutInvalidType() {
        try {
            service.doPut("invalid-type", "", NullNode.getInstance(), new JsonRequest(locale, JSON.V5, JSON.GET, 0));
            fail("Expected exception not thrown");
        } catch (JsonException ex) {
            assertThat(ex.getCode()).isEqualTo(405);
        }
    }

    @Test
    public void testCar() throws JsonException {
        // try a non-existent car
        try {
            service.doGet(JsonOperations.CAR, "", NullNode.getInstance(),
                    new JsonRequest(locale, JSON.V5, JSON.GET, 0));
            fail("Expected exception not thrown");
        } catch (JsonException ex) {
            assertEquals(404, ex.getCode());
        }
        // get a known car
        CarManager manager = InstanceManager.getDefault(CarManager.class);
        Car car = manager.getByRoadAndNumber("CP", "C10099");
        JsonNode result = service.doGet(JsonOperations.CAR, car.getId(), NullNode.getInstance(),
                new JsonRequest(locale, JSON.V5, JSON.GET, 42));
        validate(result);
        assertEquals(JsonOperations.CAR, result.path(JSON.TYPE).asText());
        assertThat(result.path(JSON.METHOD).isMissingNode()).isTrue();
        assertEquals(42, result.path(JSON.ID).asInt());
        JsonNode data = result.path(JSON.DATA);
        assertEquals("Number of properties in Car", 39, data.size());
        assertEquals(car.getId(), data.path(JSON.NAME).asText());
        assertEquals(car.getRoadName(), data.path(JsonOperations.ROAD).asText());
        assertEquals(car.getNumber(), data.path(JsonOperations.NUMBER).asText());
        assertThat(data.path(JSON.RFID).isValueNode()).isTrue();
        assertThat(data.path(JSON.WHERELASTSEEN).isValueNode()).isTrue();
        assertThat(data.path(JSON.WHENLASTSEEN).isValueNode()).isTrue();
        assertEquals(car.getRfid(), data.path(JSON.RFID).asText());
        assertThat(data.path(JSON.WHERELASTSEEN).isNull()).isTrue();
        assertThat(data.path(JSON.WHENLASTSEEN).isNull()).isTrue();
        assertEquals(car.getTypeName(), data.path(JsonOperations.TYPE).asText());
        assertThat(data.path(JsonOperations.CAR_SUB_TYPE).asText().isEmpty()).isTrue();
        assertEquals(car.getLengthInteger(), data.path(JSON.LENGTH).asInt());
        assertEquals(car.getAdjustedWeightTons(), data.path(JsonOperations.WEIGHT).asInt());
        assertEquals(car.getWeightTons(), data.path(JsonOperations.WEIGHT_TONS).asText());
        assertEquals(car.getColor(), data.path(JsonOperations.COLOR).asText());
        assertEquals(car.getOwnerName(), data.path(JsonOperations.OWNER).asText());
        assertEquals(car.getComment(), data.path(JsonOperations.COMMENT).asText());
        assertEquals(car.getLocationId(), data.path(JSON.LOCATION).path(JSON.NAME).asText());
        assertEquals(car.getTrackId(),
                data.path(JSON.LOCATION).path(JSON.TRACK).path(JSON.NAME).asText());
        assertThat(data.path(JsonOperations.LOCATION_UNKNOWN).isValueNode()).isTrue();
        assertThat(data.path(JsonOperations.LOCATION_UNKNOWN).asBoolean()).isFalse();
        assertThat(data.path(JsonOperations.TRAIN_ID).isMissingNode()).isFalse();
        assertThat(data.path(JsonOperations.TRAIN_ID).isNull()).isTrue();
        assertThat(data.path(JsonOperations.DESTINATION).isValueNode()).isTrue();
        assertThat(data.path(JsonOperations.DESTINATION).isNull()).isTrue();
        assertEquals(car.getLoadName(), data.path(JsonOperations.LOAD).asText());
        assertThat(data.path(JsonOperations.HAZARDOUS).isValueNode()).isTrue();
        assertThat(data.path(JsonOperations.HAZARDOUS).asBoolean()).isFalse();
        assertThat(data.path(JsonOperations.CABOOSE).isValueNode()).isTrue();
        assertThat(data.path(JsonOperations.CABOOSE).asBoolean()).isTrue();
        assertThat(data.path(JsonOperations.PASSENGER).isValueNode()).isTrue();
        assertThat(data.path(JsonOperations.PASSENGER).asBoolean()).isFalse();
        assertThat(data.path(JsonOperations.FRED).isValueNode()).isTrue();
        assertThat(data.path(JsonOperations.FRED).asBoolean()).isFalse();
        assertThat(data.path(JsonOperations.PICKUP_COMMENT).isValueNode()).isTrue();
        assertThat(data.path(JsonOperations.PICKUP_COMMENT).asText().isEmpty()).isTrue();
        assertThat(data.path(JsonOperations.SETOUT_COMMENT).isValueNode()).isTrue();
        assertThat(data.path(JsonOperations.SETOUT_COMMENT).asText().isEmpty()).isTrue();
        assertThat(data.path(JsonOperations.KERNEL).isValueNode()).isTrue();
        assertThat(data.path(JsonOperations.KERNEL).asText().isEmpty()).isTrue();
        assertThat(data.path(JsonOperations.UTILITY).isValueNode()).isTrue();
        assertThat(data.path(JsonOperations.UTILITY).asBoolean()).isFalse();
        assertThat(data.path(JsonOperations.FINAL_DESTINATION).isValueNode()).isTrue();
        assertThat(data.path(JsonOperations.FINAL_DESTINATION).isNull()).isTrue();
        assertThat(data.path(JsonOperations.RETURN_WHEN_EMPTY).isValueNode()).isTrue();
        assertThat(data.path(JsonOperations.RETURN_WHEN_EMPTY).isNull()).isTrue();
        assertThat(data.path(JsonOperations.OUT_OF_SERVICE).isMissingNode()).isFalse();
        assertThat(data.path(JsonOperations.OUT_OF_SERVICE).asBoolean()).isFalse();
        assertThat(data.path(JsonOperations.LOCATION_UNKNOWN).isMissingNode()).isFalse();
        assertThat(data.path(JsonOperations.LOCATION_UNKNOWN).asBoolean()).isFalse();
        assertThat(data.path(JsonOperations.TRAIN_ID).isMissingNode()).isFalse();
        assertThat(data.path(JsonOperations.TRAIN_ID).isNull()).isTrue();
        assertThat(data.path(JSON.STATUS).isValueNode()).isTrue();
        assertThat(data.path(JSON.STATUS).asText().isEmpty()).isTrue();
        assertThat(data.path(JsonOperations.IS_LOCAL).isValueNode()).isTrue();
        assertThat(data.path(JsonOperations.IS_LOCAL).asBoolean()).isFalse();
        // add (PUT) a car
        data = mapper.createObjectNode().put(JsonOperations.ROAD, "MEC").put(JsonOperations.NUMBER, "31995");
        validateData(JsonOperations.CAR, data, false);
        result = service.doPut(JsonOperations.CAR, "", data, new JsonRequest(locale, JSON.V5, JSON.GET, 42));
        car = manager.getByRoadAndNumber("MEC", "31995");
        assertThat(car).isNotNull();
        validate(result);
        assertEquals(JsonOperations.CAR, result.path(JSON.TYPE).asText());
        assertThat(result.path(JSON.METHOD).isMissingNode()).isTrue();
        assertEquals(42, result.path(JSON.ID).asInt());
        data = result.path(JSON.DATA);
        assertEquals("Number of properties in Car", 39, data.size());
        assertEquals(car.getId(), data.path(JSON.NAME).asText());
        assertEquals(car.getRoadName(), data.path(JsonOperations.ROAD).asText());
        assertEquals(car.getNumber(), data.path(JsonOperations.NUMBER).asText());
        assertThat(data.path(JSON.RFID).isValueNode()).isTrue();
        assertThat(data.path(JSON.WHERELASTSEEN).isValueNode()).isTrue();
        assertThat(data.path(JSON.WHENLASTSEEN).isValueNode()).isTrue();
        assertEquals(car.getRfid(), data.path(JSON.RFID).asText());
        assertThat(data.path(JSON.WHERELASTSEEN).isNull()).isTrue();
        assertThat(data.path(JSON.WHENLASTSEEN).isNull()).isTrue();
        assertEquals(car.getTypeName(), data.path(JsonOperations.TYPE).asText());
        assertThat(data.path(JsonOperations.CAR_SUB_TYPE).asText().isEmpty()).isTrue();
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
        assertThat(data.path(JsonOperations.DESTINATION).isValueNode()).isTrue();
        assertThat(data.path(JsonOperations.DESTINATION).isNull()).isTrue();
        assertEquals(car.getLoadName(), data.path(JsonOperations.LOAD).asText());
        assertThat(data.path(JsonOperations.HAZARDOUS).isValueNode()).isTrue();
        assertThat(data.path(JsonOperations.HAZARDOUS).asBoolean()).isFalse();
        assertThat(data.path(JsonOperations.CABOOSE).isValueNode()).isTrue();
        assertThat(data.path(JsonOperations.CABOOSE).asBoolean()).isFalse();
        assertThat(data.path(JsonOperations.PASSENGER).isValueNode()).isTrue();
        assertThat(data.path(JsonOperations.PASSENGER).asBoolean()).isFalse();
        assertThat(data.path(JsonOperations.FRED).isValueNode()).isTrue();
        assertThat(data.path(JsonOperations.FRED).asBoolean()).isFalse();
        assertThat(data.path(JsonOperations.PICKUP_COMMENT).isValueNode()).isTrue();
        assertThat(data.path(JsonOperations.PICKUP_COMMENT).asText().isEmpty()).isTrue();
        assertThat(data.path(JsonOperations.SETOUT_COMMENT).isValueNode()).isTrue();
        assertThat(data.path(JsonOperations.SETOUT_COMMENT).asText().isEmpty()).isTrue();
        assertThat(data.path(JsonOperations.KERNEL).isValueNode()).isTrue();
        assertThat(data.path(JsonOperations.KERNEL).asText().isEmpty()).isTrue();
        assertThat(data.path(JsonOperations.UTILITY).isValueNode()).isTrue();
        assertThat(data.path(JsonOperations.UTILITY).asBoolean()).isFalse();
        assertThat(data.path(JsonOperations.FINAL_DESTINATION).isValueNode()).isTrue();
        assertThat(data.path(JsonOperations.FINAL_DESTINATION).isNull()).isTrue();
        assertThat(data.path(JsonOperations.RETURN_WHEN_EMPTY).isValueNode()).isTrue();
        assertThat(data.path(JsonOperations.RETURN_WHEN_EMPTY).isNull()).isTrue();
        assertThat(data.path(JsonOperations.OUT_OF_SERVICE).isMissingNode()).isFalse();
        assertThat(data.path(JsonOperations.OUT_OF_SERVICE).asBoolean()).isFalse();
        assertThat(data.path(JsonOperations.LOCATION_UNKNOWN).isMissingNode()).isFalse();
        assertThat(data.path(JsonOperations.LOCATION_UNKNOWN).asBoolean()).isFalse();
        assertThat(data.path(JsonOperations.TRAIN_ID).isMissingNode()).isFalse();
        assertThat(data.path(JsonOperations.TRAIN_ID).isNull()).isTrue();
        assertThat(data.path(JSON.STATUS).isValueNode()).isTrue();
        assertThat(data.path(JSON.STATUS).asText().isEmpty()).isTrue();
        assertThat(data.path(JsonOperations.IS_LOCAL).isValueNode()).isTrue();
        assertThat(data.path(JsonOperations.IS_LOCAL).asBoolean()).isFalse();
        // delete a car
        data = mapper.createObjectNode().put(JSON.NAME, car.getId());
        validateData(JsonOperations.CAR, data, false);
        service.doDelete(JsonOperations.CAR, car.getId(), data, new JsonRequest(locale, JSON.V5, JSON.GET, 0));
        assertThat(manager.getById(car.getId())).isNull();
        // (re)create a car
        // add (PUT) a car
        data = mapper.createObjectNode().put(JsonOperations.ROAD, "MEC").put(JsonOperations.NUMBER, "31995");
        validateData(JsonOperations.CAR, data, false);
        result = service.doPut(JsonOperations.CAR, car.getId(), data, new JsonRequest(locale, JSON.V5, JSON.GET, 42));
        car = manager.getByRoadAndNumber("MEC", "31995");
        assertThat(car).isNotNull();
        validate(result);
        // add (PUT) the same car again
        try {
            service.doPut(JsonOperations.CAR, car.getId(), data, new JsonRequest(locale, JSON.V5, JSON.GET, 42));
            fail("Expected exception not thrown");
        } catch (JsonException ex) {
            assertEquals(409, ex.getCode());
            assertEquals(42, ex.getId());
            assertEquals(
                    "Unable to create new car with road number MEC 31995 since another car with that road number already exists.",
                    ex.getMessage());
        }
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
        assertThat(car).isNull();
        car = manager.getByRoadAndNumber("BM", "216"); // get by name and number
        assertThat(car).isNotNull();
        validate(result);
        assertEquals(JsonOperations.CAR, result.path(JSON.TYPE).asText());
        assertThat(result.path(JSON.METHOD).isMissingNode()).isTrue();
        assertEquals(42, result.path(JSON.ID).asInt());
        data = result.path(JSON.DATA);
        // rename not always present
        assertEquals("Number of properties in Car", 40, data.size());
        // TODO: verify against car and known values
        assertEquals(car.getId(), data.path(JSON.NAME).asText());
        assertEquals(car.getRoadName(), data.path(JsonOperations.ROAD).asText());
        assertEquals(car.getNumber(), data.path(JsonOperations.NUMBER).asText());
        assertThat(data.path(JSON.RFID).isValueNode()).isTrue();
        assertEquals(car.getRfid(), data.path(JSON.RFID).asText());
        assertThat(data.path(JsonOperations.TYPE).asText()).isNotEqualTo(car.getTypeName());
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
        assertThat(data.path(JsonOperations.DESTINATION).isValueNode()).isTrue();
        assertThat(data.path(JsonOperations.DESTINATION).isNull()).isTrue();
        assertEquals(car.getLoadName(), data.path(JsonOperations.LOAD).asText());
        assertThat(data.path(JsonOperations.HAZARDOUS).isValueNode()).isTrue();
        assertThat(data.path(JsonOperations.HAZARDOUS).asBoolean()).isTrue();
        assertThat(data.path(JsonOperations.CABOOSE).isValueNode()).isTrue();
        assertThat(data.path(JsonOperations.CABOOSE).asBoolean()).isTrue();
        assertThat(data.path(JsonOperations.PASSENGER).isValueNode()).isTrue();
        assertThat(data.path(JsonOperations.PASSENGER).asBoolean()).isTrue();
        assertThat(data.path(JsonOperations.FRED).isValueNode()).isTrue();
        assertThat(data.path(JsonOperations.FRED).asBoolean()).isTrue();
        assertThat(data.path(JsonOperations.PICKUP_COMMENT).isValueNode()).isTrue();
        assertThat(data.path(JsonOperations.PICKUP_COMMENT).asText().isEmpty()).isTrue();
        assertThat(data.path(JsonOperations.SETOUT_COMMENT).isValueNode()).isTrue();
        assertThat(data.path(JsonOperations.SETOUT_COMMENT).asText().isEmpty()).isTrue();
        assertThat(data.path(JsonOperations.KERNEL).isValueNode()).isTrue();
        assertThat(data.path(JsonOperations.KERNEL).asText().isEmpty()).isTrue();
        assertThat(data.path(JsonOperations.UTILITY).isValueNode()).isTrue();
        assertThat(data.path(JsonOperations.UTILITY).asBoolean()).isTrue();
        assertThat(data.path(JsonOperations.FINAL_DESTINATION).isValueNode()).isTrue();
        assertThat(data.path(JsonOperations.FINAL_DESTINATION).isNull()).isTrue();
        assertThat(data.path(JsonOperations.RETURN_WHEN_EMPTY).isValueNode()).isTrue();
        assertThat(data.path(JsonOperations.RETURN_WHEN_EMPTY).isNull()).isTrue();
        assertThat(data.path(JsonOperations.OUT_OF_SERVICE).isMissingNode()).isFalse();
        assertThat(data.path(JsonOperations.OUT_OF_SERVICE).asBoolean()).isTrue();
        assertThat(data.path(JsonOperations.LOCATION_UNKNOWN).isMissingNode()).isFalse();
        assertThat(data.path(JsonOperations.LOCATION_UNKNOWN).asBoolean()).isFalse();
        assertThat(data.path(JsonOperations.TRAIN_ID).isMissingNode()).isFalse();
        assertThat(data.path(JsonOperations.TRAIN_ID).isNull()).isTrue();
        assertThat(data.path(JSON.STATUS).isValueNode()).isTrue();
        assertThat(data.path(JSON.STATUS).asText()).as("Out of service status").isEqualTo("&lt;O&gt; ");
        assertThat(data.path(JsonOperations.IS_LOCAL).isValueNode()).isTrue();
        assertThat(data.path(JsonOperations.IS_LOCAL).asBoolean()).isFalse();
        // edit a non-existent car
        assertThat(manager.getById("-1")).isNull();
        assertThatCode(() -> service.doPost(JsonOperations.CAR, "-1", NullNode.getInstance(),
                new JsonRequest(locale, JSON.V5, JSON.PUT, 0)))
                        .isExactlyInstanceOf(JsonException.class)
                        .hasFieldOrPropertyWithValue(JsonException.CODE, 404);
    }

    @Test
    public void testEngine() throws JsonException {
        // try a non-existent engine
        try {
            service.doGet(JsonOperations.ENGINE, "", NullNode.getInstance(),
                    new JsonRequest(locale, JSON.V5, JSON.GET, 0));
            fail("Expected exception not thrown");
        } catch (JsonException ex) {
            assertEquals(404, ex.getCode());
        }
        // get a known engine
        EngineManager manager = InstanceManager.getDefault(EngineManager.class);
        Engine engine = manager.getByRoadAndNumber("PC", "5524");
        JsonNode result = service.doGet(JsonOperations.ENGINE, engine.getId(), NullNode.getInstance(),
                new JsonRequest(locale, JSON.V5, JSON.GET, 42));
        validate(result);
        assertEquals(JsonOperations.ENGINE, result.path(JSON.TYPE).asText());
        assertThat(result.path(JSON.METHOD).isMissingNode()).isTrue();
        assertEquals(42, result.path(JSON.ID).asInt());
        JsonNode data = result.path(JSON.DATA);
        assertEquals("Number of properties in Engine", 25, data.size());
        assertEquals(engine.getId(), data.path(JSON.NAME).asText());
        assertEquals(engine.getRoadName(), data.path(JsonOperations.ROAD).asText());
        assertEquals(engine.getNumber(), data.path(JsonOperations.NUMBER).asText());
        assertThat(data.path(JSON.RFID).isValueNode()).isTrue();
        assertThat(data.path(JSON.WHERELASTSEEN).isValueNode()).isTrue();
        assertThat(data.path(JSON.WHENLASTSEEN).isValueNode()).isTrue();
        assertEquals(engine.getRfid(), data.path(JSON.RFID).asText());
        assertThat(data.path(JSON.WHERELASTSEEN).isNull()).isTrue();
        assertThat(data.path(JSON.WHENLASTSEEN).isNull()).isTrue();
        assertEquals(engine.getTypeName(), data.path(JsonOperations.TYPE).asText());
        assertThat(data.path(JsonOperations.CAR_SUB_TYPE).asText().isEmpty()).isTrue();
        assertEquals(engine.getLengthInteger(), data.path(JsonOperations.LENGTH).asInt());
        assertEquals(engine.getAdjustedWeightTons(), data.path(JsonOperations.WEIGHT).asInt());
        assertEquals(engine.getWeightTons(), data.path(JsonOperations.WEIGHT_TONS).asText());
        assertEquals(engine.getColor(), data.path(JsonOperations.COLOR).asText());
        assertEquals(engine.getOwnerName(), data.path(JsonOperations.OWNER).asText());
        assertEquals(engine.getComment(), data.path(JsonOperations.COMMENT).asText());
        assertEquals(engine.getLocationId(), data.path(JSON.LOCATION).path(JSON.NAME).asText());
        assertEquals(engine.getTrackId(),
                data.path(JSON.LOCATION).path(JSON.TRACK).path(JSON.NAME).asText());
        assertThat(data.path(JsonOperations.DESTINATION).isValueNode()).isTrue();
        assertThat(data.path(JsonOperations.DESTINATION).isNull()).isTrue();
        assertThat(data.path(JsonConsist.CONSIST).isValueNode()).isTrue();
        assertEquals("C14", data.path(JsonConsist.CONSIST).asText());
        assertThat(data.path(JsonOperations.OUT_OF_SERVICE).isMissingNode()).isFalse();
        assertThat(data.path(JsonOperations.OUT_OF_SERVICE).asBoolean()).isFalse();
        assertThat(data.path(JsonOperations.LOCATION_UNKNOWN).isMissingNode()).isFalse();
        assertThat(data.path(JsonOperations.LOCATION_UNKNOWN).asBoolean()).isFalse();
        assertThat(data.path(JsonOperations.TRAIN_ID).isMissingNode()).isFalse();
        assertThat(data.path(JsonOperations.TRAIN_ID).isNull()).isTrue();
        assertThat(data.path(JsonOperations.MODEL).isValueNode()).isTrue();
        assertEquals("SD45", data.path(JsonOperations.MODEL).asText());
        // add (PUT) an engine
        data = mapper.createObjectNode().put(JsonOperations.ROAD, "MEC").put(JsonOperations.NUMBER, "3402");
        validateData(JsonOperations.ENGINE, data, false);
        result = service.doPut(JsonOperations.ENGINE, "", data, new JsonRequest(locale, JSON.V5, JSON.GET, 42));
        engine = manager.getByRoadAndNumber("MEC", "3402");
        assertThat(engine).isNotNull();
        validate(result);
        assertEquals(JsonOperations.ENGINE, result.path(JSON.TYPE).asText());
        assertThat(result.path(JSON.METHOD).isMissingNode()).isTrue();
        assertEquals(42, result.path(JSON.ID).asInt());
        data = result.path(JSON.DATA);
        assertEquals("Number of properties in Engine", 25, data.size());
        assertEquals(engine.getId(), data.path(JSON.NAME).asText());
        assertEquals(engine.getRoadName(), data.path(JsonOperations.ROAD).asText());
        assertEquals(engine.getNumber(), data.path(JsonOperations.NUMBER).asText());
        assertThat(data.path(JSON.RFID).isValueNode()).isTrue();
        assertThat(data.path(JSON.WHERELASTSEEN).isValueNode()).isTrue();
        assertThat(data.path(JSON.WHENLASTSEEN).isValueNode()).isTrue();
        assertEquals(engine.getRfid(), data.path(JSON.RFID).asText());
        assertThat(data.path(JSON.WHERELASTSEEN).isNull()).isTrue();
        assertThat(data.path(JSON.WHENLASTSEEN).isNull()).isTrue();
        assertEquals(engine.getTypeName(), data.path(JsonOperations.TYPE).asText());
        assertThat(data.path(JsonOperations.CAR_SUB_TYPE).asText().isEmpty()).isTrue();
        assertEquals(engine.getLengthInteger(), data.path(JsonOperations.LENGTH).asInt());
        assertEquals(Double.parseDouble(engine.getWeight()), data.path(JsonOperations.WEIGHT).asDouble(), 0.0);
        try {
            assertEquals(Double.parseDouble(engine.getWeightTons()), data.path(JsonOperations.WEIGHT_TONS).asDouble(),
                    0.0);
        } catch (NumberFormatException ex) {
            assertEquals(0.0, data.path(JsonOperations.WEIGHT_TONS).asDouble(), 0.0);
        }
        assertEquals(engine.getColor(), data.path(JsonOperations.COLOR).asText());
        assertEquals(engine.getOwnerName(), data.path(JsonOperations.OWNER).asText());
        assertEquals(engine.getComment(), data.path(JsonOperations.COMMENT).asText());
        assertEquals(engine.getLocationId(), data.path(JSON.LOCATION).path(JSON.NAME).asText());
        assertEquals(engine.getTrackId(),
                data.path(JSON.LOCATION).path(JSON.TRACK).path(JSON.NAME).asText());
        assertThat(data.path(JsonOperations.DESTINATION).isValueNode()).isTrue();
        assertThat(data.path(JsonOperations.DESTINATION).isNull()).isTrue();
        assertThat(data.path(JsonConsist.CONSIST).isValueNode()).isTrue();
        assertThat(data.path(JsonConsist.CONSIST).asText().isEmpty()).isTrue();
        assertThat(data.path(JsonOperations.OUT_OF_SERVICE).isMissingNode()).isFalse();
        assertThat(data.path(JsonOperations.OUT_OF_SERVICE).asBoolean()).isFalse();
        assertThat(data.path(JsonOperations.LOCATION_UNKNOWN).isMissingNode()).isFalse();
        assertThat(data.path(JsonOperations.LOCATION_UNKNOWN).asBoolean()).isFalse();
        assertThat(data.path(JsonOperations.TRAIN_ID).isMissingNode()).isFalse();
        assertThat(data.path(JsonOperations.TRAIN_ID).isNull()).isTrue();
        assertThat(data.path(JsonOperations.MODEL).isValueNode()).isTrue();
        assertThat(data.path(JsonOperations.MODEL).asText().isEmpty()).isTrue();
        // delete an engine
        data = mapper.createObjectNode().put(JSON.NAME, engine.getId());
        validateData(JsonOperations.ENGINE, data, false);
        service.doDelete(JsonOperations.ENGINE, engine.getId(), data, new JsonRequest(locale, JSON.V5, JSON.GET, 0));
        assertThat(manager.getById(engine.getId())).isNull();
        // (re)create an engine
        // add (PUT) an engine
        data = mapper.createObjectNode().put(JsonOperations.ROAD, "MEC").put(JsonOperations.NUMBER, "3402");
        validateData(JsonOperations.ENGINE, data, false);
        result = service.doPut(JsonOperations.ENGINE, engine.getId(), data,
                new JsonRequest(locale, JSON.V5, JSON.GET, 42));
        engine = manager.getByRoadAndNumber("MEC", "3402");
        assertThat(engine).isNotNull();
        validate(result);
        // add (PUT) the same engine again
        try {
            service.doPut(JsonOperations.ENGINE, engine.getId(), data, new JsonRequest(locale, JSON.V5, JSON.GET, 42));
            fail("Expected exception not thrown");
        } catch (JsonException ex) {
            assertEquals(409, ex.getCode());
            assertEquals(42, ex.getId());
            assertEquals(
                    "Unable to create new engine with road number MEC 3402 since another engine with that road number already exists.",
                    ex.getMessage());
        }
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
        assertThat(engine).isNull();
        // get by name and number
        engine = manager.getByRoadAndNumber("BM", "216");
        assertThat(engine).isNotNull();
        validate(result);
        assertEquals(JsonOperations.ENGINE, result.path(JSON.TYPE).asText());
        assertThat(result.path(JSON.METHOD).isMissingNode()).isTrue();
        assertEquals(42, result.path(JSON.ID).asInt());
        data = result.path(JSON.DATA);
        // rename not always present
        assertEquals("Number of properties in Engine", 26, data.size());
        assertEquals("BM216", data.path(JSON.NAME).asText());
        assertEquals("BM", data.path(JsonOperations.ROAD).asText());
        assertEquals("216", data.path(JsonOperations.NUMBER).asText());
        assertThat(data.path(JSON.RFID).isValueNode()).isTrue();
        assertEquals("ID1234567890AB", data.path(JSON.RFID).asText());
        assertThat(data.path(JsonOperations.TYPE).asText()).isNotEqualTo(engine.getTypeName());
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
        assertThat(data.path(JsonOperations.DESTINATION).isValueNode()).isTrue();
        assertThat(data.path(JsonOperations.DESTINATION).isNull()).isTrue();
        assertThat(data.path(JsonConsist.CONSIST).isValueNode()).isTrue();
        assertThat(data.path(JsonConsist.CONSIST).asText().isEmpty()).isTrue();
        assertThat(data.path(JsonOperations.OUT_OF_SERVICE).isMissingNode()).isFalse();
        assertThat(data.path(JsonOperations.OUT_OF_SERVICE).asBoolean()).isFalse();
        assertThat(data.path(JsonOperations.LOCATION_UNKNOWN).isMissingNode()).isFalse();
        assertThat(data.path(JsonOperations.LOCATION_UNKNOWN).asBoolean()).isFalse();
        assertThat(data.path(JsonOperations.TRAIN_ID).isMissingNode()).isFalse();
        assertThat(data.path(JsonOperations.TRAIN_ID).isNull()).isTrue();
        assertEquals("SD 40-2", data.path(JsonOperations.MODEL).asText());
        JUnitAppender.assertErrorMessage("Rolling stock (BM 216) length () is not valid");
        // edit a non-existent car
        assertThat(manager.getById("-1")).isNull();
        assertThatCode(() -> service.doPost(JsonOperations.ENGINE, "-1", NullNode.getInstance(),
                new JsonRequest(locale, JSON.V5, JSON.PUT, 0)))
                        .isExactlyInstanceOf(JsonException.class)
                        .hasFieldOrPropertyWithValue(JsonException.CODE, 404);
    }

    @Test
    public void testLocation() throws JsonException {
        // try a non-existent location
        try {
            service.doGet(JSON.LOCATION, "", NullNode.getInstance(),
                    new JsonRequest(locale, JSON.V5, JSON.GET, 0));
            fail("Expected exception not thrown");
        } catch (JsonException ex) {
            assertEquals(404, ex.getCode());
        }
        JUnitAppender.assertErrorMessage("Unable to get location id [].");
        // get a known location
        LocationManager manager = InstanceManager.getDefault(LocationManager.class);
        Location location = manager.getLocationById("20");
        JsonNode result = service.doGet(JSON.LOCATION, location.getId(), NullNode.getInstance(),
                new JsonRequest(locale, JSON.V5, JSON.GET, 42));
        validate(result);
        assertEquals(JSON.LOCATION, result.path(JSON.TYPE).asText());
        assertThat(result.path(JSON.METHOD).isMissingNode()).isTrue();
        assertEquals(42, result.path(JSON.ID).asInt());
        JsonNode data = result.path(JSON.DATA);
        assertEquals("Number of properties in Location", 7, data.size());
        assertEquals(location.getId(), data.path(JSON.NAME).asText());
        assertEquals(location.getName(), data.path(JSON.USERNAME).asText());
        assertThat(data.path(JSON.COMMENT).isValueNode()).isTrue();
        assertEquals(location.getCommentWithColor(), data.path(JSON.COMMENT).asText());
        assertThat(data.path(JsonReporter.REPORTER).isValueNode()).isTrue();
        assertThat(data.path(JsonReporter.REPORTER).asText().isEmpty()).isTrue();
        assertThat(data.path(JsonOperations.CAR_TYPE).isArray()).isTrue();
        assertEquals(4, data.path(JsonOperations.CAR_TYPE).size());
        assertEquals("Boxcar", data.path(JsonOperations.CAR_TYPE).path(0).asText());
        assertEquals("Caboose", data.path(JsonOperations.CAR_TYPE).path(1).asText());
        assertEquals("Flat", data.path(JsonOperations.CAR_TYPE).path(2).asText());
        assertEquals("Diesel", data.path(JsonOperations.CAR_TYPE).path(3).asText());
        assertThat(data.path(JSON.TRACK).isArray()).isTrue();
        assertEquals(1, data.path(JSON.TRACK).size());
        assertEquals("20s1", data.path(JSON.TRACK).path(0).path(JSON.NAME).asText());
        assertEquals("NI Yard", data.path(JSON.TRACK).path(0).path(JSON.USERNAME).asText());
        assertThat(data.path(JSON.TRACK).path(0).path(JsonReporter.REPORTER).isValueNode()).isTrue();
        assertThat(data.path(JSON.TRACK).path(0).path(JsonReporter.REPORTER).asText().isEmpty()).isTrue();
        // add (PUT) a location
        data = mapper.createObjectNode().put(JSON.USERNAME, "Test Site");
        validateData(JSON.LOCATION, data, false);
        result = service.doPut(JSON.LOCATION, "42", data, new JsonRequest(locale, JSON.V5, JSON.GET, 42));
        location = manager.getLocationById("42");
        assertThat(location).isNull();
        location = manager.getLocationByName("Test Site");
        assertThat(location).isNotNull();
        validate(result);
        assertEquals(JSON.LOCATION, result.path(JSON.TYPE).asText());
        assertThat(result.path(JSON.METHOD).isMissingNode()).isTrue();
        assertEquals(42, result.path(JSON.ID).asInt());
        data = result.path(JSON.DATA);
        assertEquals("Number of properties in Location", 7, data.size());
        assertEquals(location.getId(), data.path(JSON.NAME).asText());
        assertEquals(location.getName(), data.path(JSON.USERNAME).asText());
        // delete a location
        data = mapper.createObjectNode().put(JSON.NAME, location.getId());
        validateData(JSON.LOCATION, data, false);
        service.doDelete(JSON.LOCATION, location.getId(), data,
                new JsonRequest(locale, JSON.V5, JSON.GET, 0));
        assertThat(manager.getLocationById(location.getId())).isNull();
        // (re)create a location
        // add (PUT) a location
        data = mapper.createObjectNode().put(JSON.USERNAME, "Test Site");
        validateData(JSON.LOCATION, data, false);
        result = service.doPut(JSON.LOCATION, "42", data, new JsonRequest(locale, JSON.V5, JSON.GET, 42));
        location = manager.getLocationByName("Test Site");
        assertThat(location).isNotNull();
        validate(result);
        // add (PUT) the same location again with same name (id)
        try {
            service.doPut(JSON.LOCATION, location.getId(), data,
                    new JsonRequest(locale, JSON.V5, JSON.GET, 42));
            fail("Expected exception not thrown");
        } catch (JsonException ex) {
            assertEquals(409, ex.getCode());
            assertEquals(42, ex.getId());
            assertEquals("Unable to create object type location with name \"22\" since already exists.",
                    ex.getMessage());
        }
        // add (PUT) the same location again with same user name, different name
        // (id)
        try {
            service.doPut(JSON.LOCATION, "42", data, new JsonRequest(locale, JSON.V5, JSON.GET, 42));
            fail("Expected exception not thrown");
        } catch (JsonException ex) {
            assertEquals(409, ex.getCode());
            assertEquals(42, ex.getId());
            assertEquals("Unable to create object type location with user name \"Test Site\" since already exists.",
                    ex.getMessage());
        }
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
        assertThat(location).isNotNull();
        validate(result);
        assertEquals(JSON.LOCATION, result.path(JSON.TYPE).asText());
        assertThat(result.path(JSON.METHOD).isMissingNode()).isTrue();
        assertEquals(42, result.path(JSON.ID).asInt());
        data = result.path(JSON.DATA);
        assertEquals("Number of properties in Location", 7, data.size());
        assertEquals(id, data.path(JSON.NAME).asText());
        assertEquals("Editted Site", data.path(JSON.USERNAME).asText());
        assertEquals("A comment", data.path(JSON.COMMENT).asText());
        assertEquals("IR1", data.path(JsonReporter.REPORTER).asText());
        assertThat(data.path(JsonOperations.CAR_TYPE).isArray()).isTrue();
        assertEquals(4, data.path(JsonOperations.CAR_TYPE).size());
        assertThat(data.path(JSON.TRACK).isArray()).isTrue();
        assertEquals(0, data.path(JSON.TRACK).size());
    }

    @Test
    public void testPutLocationNoName() {
        assertThatCode(() -> service.doPut(JSON.LOCATION, "", mapper.createObjectNode(),
                new JsonRequest(locale, JSON.V5, JSON.PUT, 42))).isExactlyInstanceOf(JsonException.class)
                        .hasFieldOrPropertyWithValue(JsonException.MESSAGE,
                                "Property \"userName\" is required to create a new location.");
    }

    @Test
    public void testTrack() throws JsonException {
        // try not including a location property
        try {
            service.doGet(JSON.TRACK, "", NullNode.getInstance(),
                    new JsonRequest(locale, JSON.V5, JSON.GET, 0));
            fail("Expected exception not thrown");
        } catch (JsonException ex) {
            assertEquals(400, ex.getCode());
            assertEquals("Property \"location\" is required for type track.", ex.getMessage());
        }
        // try a non-existent track in existing location
        ObjectNode message = mapper.createObjectNode().put(JSON.LOCATION, "20");
        try {
            service.doGet(JSON.TRACK, "", message, new JsonRequest(locale, JSON.V5, JSON.GET, 0));
            fail("Expected exception not thrown");
        } catch (JsonException ex) {
            assertEquals(404, ex.getCode());
            assertEquals("Object type track named \"\" not found.", ex.getMessage());
        }
        // try a non-existent track in non-existent location
        message = mapper.createObjectNode().put(JSON.LOCATION, "");
        try {
            service.doGet(JSON.TRACK, "", message, new JsonRequest(locale, JSON.V5, JSON.GET, 0));
            fail("Expected exception not thrown");
        } catch (JsonException ex) {
            assertEquals(404, ex.getCode());
            assertEquals("Object type location named \"\" not found.", ex.getMessage());
        }
        // get a known track from a known location
        LocationManager manager = InstanceManager.getDefault(LocationManager.class);
        Location location = manager.getLocationById("20");
        assertThat(location).isNotNull();
        Track track = location.getTrackById("20s1");
        assertThat(track).isNotNull();
        message = mapper.createObjectNode().put(JSON.LOCATION, "20");
        JsonNode result = service.doGet(JSON.TRACK, track.getId(), message,
                new JsonRequest(locale, JSON.V5, JSON.GET, 42));
        validate(result);
        assertEquals(JSON.TRACK, result.path(JSON.TYPE).asText());
        assertThat(result.path(JSON.METHOD).isMissingNode()).isTrue();
        assertEquals(42, result.path(JSON.ID).asInt());
        JsonNode data = result.path(JSON.DATA);
        assertEquals("Number of properties in Track", 8, data.size());
        assertEquals(track.getId(), data.path(JSON.NAME).asText());
        assertEquals(track.getName(), data.path(JSON.USERNAME).asText());
        assertThat(data.path(JSON.COMMENT).isValueNode()).isTrue();
        assertEquals(track.getComment(), data.path(JSON.COMMENT).asText());
        assertThat(data.path(JsonReporter.REPORTER).isValueNode()).isTrue();
        assertThat(data.path(JsonReporter.REPORTER).asText().isEmpty()).isTrue();
        assertThat(data.path(JsonOperations.CAR_TYPE).isArray()).isTrue();
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
        assertThat(track).isNull();
        track = location.getTrackByName("Test Site", "Siding");
        assertThat(track).isNotNull();
        validate(result);
        assertEquals(JSON.TRACK, result.path(JSON.TYPE).asText());
        assertThat(result.path(JSON.METHOD).isMissingNode()).isTrue();
        assertEquals(42, result.path(JSON.ID).asInt());
        data = result.path(JSON.DATA);
        assertEquals("Number of properties in Track", 8, data.size());
        assertEquals(track.getId(), data.path(JSON.NAME).asText());
        assertEquals(track.getName(), data.path(JSON.USERNAME).asText());
        // delete a location
        data = mapper.createObjectNode()
                .put(JSON.NAME, track.getId())
                .put(JSON.LOCATION, location.getId());
        validateData(JSON.TRACK, data, false);
        service.doDelete(JSON.TRACK, track.getId(), data, new JsonRequest(locale, JSON.V5, JSON.GET, 0));
        assertThat(location.getTrackById(track.getId())).isNull();
        // (re)create a location
        // add (PUT) a location
        data = mapper.createObjectNode()
                .put(JSON.USERNAME, "Test Site")
                .put(JSON.LOCATION, "20")
                .put(JSON.TYPE, "Siding");
        validateData(JSON.TRACK, data, false);
        result = service.doPut(JSON.TRACK, "42", data, new JsonRequest(locale, JSON.V5, JSON.GET, 42));
        track = location.getTrackByName("Test Site", "Siding");
        assertThat(track).isNotNull();
        validate(result);
        // add (PUT) the same track again with same name (id)
        try {
            service.doPut(JSON.TRACK, track.getId(), data, new JsonRequest(locale, JSON.V5, JSON.GET, 42));
            fail("Expected exception not thrown");
        } catch (JsonException ex) {
            assertEquals(409, ex.getCode());
            assertEquals(42, ex.getId());
            assertEquals("Unable to create object type track with name \"20s3\" since already exists.",
                    ex.getMessage());
        }
        // add (PUT) the same track again with same user name, different name
        // (id)
        try {
            service.doPut(JSON.TRACK, "42", data, new JsonRequest(locale, JSON.V5, JSON.GET, 42));
            fail("Expected exception not thrown");
        } catch (JsonException ex) {
            assertEquals(409, ex.getCode());
            assertEquals(42, ex.getId());
            assertEquals("Unable to create object type track with user name \"Test Site\" since already exists.",
                    ex.getMessage());
        }
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
        assertThat(track).isNotNull();
        assertThat(location).isNotNull();
        validate(result);
        assertEquals(JSON.TRACK, result.path(JSON.TYPE).asText());
        assertThat(result.path(JSON.METHOD).isMissingNode()).isTrue();
        assertEquals(42, result.path(JSON.ID).asInt());
        data = result.path(JSON.DATA);
        assertEquals("Number of properties in Track", 8, data.size());
        assertEquals(id, data.path(JSON.NAME).asText());
        assertEquals("Editted Site", data.path(JSON.USERNAME).asText());
        assertEquals("A comment", data.path(JSON.COMMENT).asText());
        assertEquals(1000, data.path(JSON.LENGTH).asInt());
        assertEquals("IR1", data.path(JsonReporter.REPORTER).asText());
        assertThat(data.path(JsonOperations.CAR_TYPE).isArray()).isTrue();
        assertEquals(4, data.path(JsonOperations.CAR_TYPE).size());
    }

    @Test
    public void testPutTrackNoUserName() {
        assertThatCode(() -> service.doPut(JSON.TRACK, "", mapper.createObjectNode(),
                new JsonRequest(locale, JSON.V5, JSON.PUT, 42))).isExactlyInstanceOf(JsonException.class)
                        .hasFieldOrPropertyWithValue(JsonException.MESSAGE,
                                "Property \"userName\" is required to create a new track.");
    }

    @Test
    public void testPutTrackNoType() {
        assertThatCode(
                () -> service.doPut(JSON.TRACK, "", mapper.createObjectNode().put(JSON.USERNAME, "test"),
                        new JsonRequest(locale, JSON.V5, JSON.PUT, 42))).isExactlyInstanceOf(JsonException.class)
                                .hasFieldOrPropertyWithValue(JsonException.MESSAGE,
                                        "Property \"type\" is required to create a new track.");
    }

    @Test
    public void testPutTrackNoLocation() {
        assertThatCode(() -> service.doPut(JSON.TRACK, "",
                mapper.createObjectNode().put(JSON.USERNAME, "test").put(JSON.TYPE, "test"),
                new JsonRequest(locale, JSON.V5, JSON.PUT, 42))).isExactlyInstanceOf(JsonException.class)
                        .hasFieldOrPropertyWithValue(JsonException.MESSAGE,
                                "Property \"location\" is required to create a new track.");
    }

    @Test
    public void testPutTrackInvalidLocation() {
        assertThatCode(() -> service.doPut(JSON.TRACK, "",
                mapper.createObjectNode().put(JSON.USERNAME, "test").put(JSON.TYPE, "test").put(JSON.LOCATION,
                        "invalid-location"),
                new JsonRequest(locale, JSON.V5, JSON.PUT, 42))).isExactlyInstanceOf(JsonException.class)
                        .hasFieldOrPropertyWithValue(JsonException.MESSAGE,
                                "Object type location named \"invalid-location\" not found.");
    }

    @Test
    public void testDoGetListCarEngineRollingStock() throws JsonException {
        JsonNode result = service.doGetList(JsonOperations.CAR, NullNode.getInstance(),
                new JsonRequest(locale, JSON.V5, JSON.GET, 0));
        validate(result);
        assertEquals("9 cars", 9, result.size());
        result = service.doGetList(JsonOperations.ENGINE, NullNode.getInstance(),
                new JsonRequest(locale, JSON.V5, JSON.GET, 0));
        validate(result);
        assertEquals("4 engines", 4, result.size());
        result = service.doGetList(JsonOperations.ROLLING_STOCK, NullNode.getInstance(),
                new JsonRequest(locale, JSON.V5, JSON.GET, 0));
        validate(result);
        assertEquals("13 rolling stock", 13, result.size());
    }

    @Test
    public void testCarType() throws JsonException {
        // get the car types
        JsonNode result = service.doGetList(JsonOperations.CAR_TYPE, NullNode.getInstance(),
                new JsonRequest(locale, JSON.V5, JSON.GET, 0));
        validate(result);
        assertThat(result.isArray()).isTrue();
        assertEquals(3, result.size());
        // add a car type
        result = service.doPut(JsonOperations.CAR_TYPE, "test1", NullNode.getInstance(),
                new JsonRequest(locale, JSON.V5, JSON.GET, 42));
        validate(result);
        assertEquals(JsonOperations.CAR_TYPE, result.path(JSON.TYPE).asText());
        assertEquals("test1", result.path(JSON.DATA).path(JSON.NAME).asText());
        assertThat(result.path(JSON.DATA).path(JsonOperations.CARS).isArray()).isTrue();
        assertThat(result.path(JSON.DATA).path(JSON.LOCATIONS).isArray()).isTrue();
        // get the added car type
        JsonNode result2 = service.doGet(JsonOperations.CAR_TYPE, "test1", NullNode.getInstance(),
                new JsonRequest(locale, JSON.V5, JSON.GET, 42));
        assertEquals(result, result2);
        // get the car types again (the list changed)
        result = service.doGetList(JsonOperations.CAR_TYPE, NullNode.getInstance(),
                new JsonRequest(locale, JSON.V5, JSON.GET, 0));
        validate(result);
        assertThat(result.isArray()).isTrue();
        assertEquals(4, result.size());
        assertEquals(JsonOperations.CAR_TYPE, result.path(0).path(JSON.TYPE).asText());
        assertEquals("Boxcar", result.path(0).path(JSON.DATA).path(JSON.NAME).asText());
        assertThat(result.path(0).path(JSON.DATA).path(JsonOperations.CARS).isArray()).isTrue();
        assertThat(result.path(0).path(JSON.DATA).path(JSON.LOCATIONS).isArray()).isTrue();
        // rename the added car type
        result = service.doPost(JsonOperations.CAR_TYPE, "test1",
                mapper.createObjectNode().put(JSON.RENAME, "test2"),
                new JsonRequest(locale, JSON.V5, JSON.POST, 0));
        validate(result);
        assertEquals(JsonOperations.CAR_TYPE, result.path(JSON.TYPE).asText());
        assertEquals("test2", result.path(JSON.DATA).path(JSON.NAME).asText());
        assertEquals("test1", result.path(JSON.DATA).path(JSON.RENAME).asText());
        assertThat(result.path(JSON.DATA).path(JsonOperations.CARS).isArray()).isTrue();
        assertThat(result.path(JSON.DATA).path(JSON.LOCATIONS).isArray()).isTrue();
        // delete the renamed car type
        service.doDelete(JsonOperations.CAR_TYPE, "test2", NullNode.getInstance(),
                new JsonRequest(locale, JSON.V5, JSON.GET, 42));
        result = service.doGetList(JsonOperations.CAR_TYPE, NullNode.getInstance(),
                new JsonRequest(locale, JSON.V5, JSON.GET, 0));
        validate(result);
        assertThat(result.isArray()).isTrue();
        assertEquals(3, result.size());
        // add empty name
        assertThatCode(() -> service.doPut(JsonOperations.CAR_TYPE, "", NullNode.getInstance(),
                new JsonRequest(locale, JSON.V5, JSON.PUT, 0)))
                        .isExactlyInstanceOf(JsonException.class)
                        .hasFieldOrPropertyWithValue(JsonException.CODE, 400);
        // get missing name
        assertThatCode(() -> service.doGet(JsonOperations.CAR_TYPE, "invalid-type", NullNode.getInstance(),
                new JsonRequest(locale, JSON.V5, JSON.GET, 0)))
                        .isExactlyInstanceOf(JsonException.class)
                        .hasFieldOrPropertyWithValue(JsonException.CODE, 404);
        // delete an in-use car type
        CarTypes manager = InstanceManager.getDefault(CarTypes.class);
        assertThat(manager.getNames()).contains("Caboose");
        JsonException ex = assertThrows(JsonException.class, () -> service.doDelete(JsonOperations.CAR_TYPE, "Caboose",
                NullNode.getInstance(), new JsonRequest(locale, JSON.V5, JSON.DELETE, 0)));
        assertThat(ex.getCode()).isEqualTo(409);
        assertThat(ex.getAdditionalData().path(JSON.FORCE_DELETE).isTextual()).isTrue();
        assertThat(manager.getNames()).contains("Caboose");
        service.doDelete(JsonOperations.CAR_TYPE, "Caboose", ex.getAdditionalData(),
                new JsonRequest(locale, JSON.V5, JSON.DELETE, 0));
        assertThat(manager.getNames()).doesNotContain("Caboose");
    }

    @Test
    public void testKernel() throws JsonException {
        // get the kernels (should be none)
        JsonNode result = service.doGetList(JsonOperations.KERNEL, NullNode.getInstance(),
                new JsonRequest(locale, JSON.V5, JSON.GET, 0));
        validate(result);
        assertThat(result.isArray()).isTrue();
        assertEquals(0, result.size());
        // create a kernel
        result = service.doPut(JsonOperations.KERNEL, "test1", NullNode.getInstance(),
                new JsonRequest(locale, JSON.V5, JSON.PUT, 42));
        validate(result);
        assertEquals(JsonOperations.KERNEL, result.path(JSON.TYPE).asText());
        assertEquals("test1", result.path(JSON.DATA).path(JSON.NAME).asText());
        assertEquals(0, result.path(JSON.DATA).path(JSON.LENGTH).asInt());
        assertEquals(0, result.path(JSON.DATA).path(JSON.WEIGHT).asInt());
        assertThat(result.path(JSON.DATA).path(JsonOperations.CARS).isArray()).isTrue();
        // get the newly created kernel
        JsonNode result2 = service.doGet(JsonOperations.KERNEL, "test1", NullNode.getInstance(),
                new JsonRequest(locale, JSON.V5, JSON.GET, 42));
        assertEquals(result, result2);
        // get the kernels (should be one)
        result = service.doGetList(JsonOperations.KERNEL, NullNode.getInstance(),
                new JsonRequest(locale, JSON.V5, JSON.GET, 0));
        validate(result);
        assertThat(result.isArray()).isTrue();
        assertEquals(1, result.size());
        assertEquals(JsonOperations.KERNEL, result.path(0).path(JSON.TYPE).asText());
        assertEquals("test1", result.path(0).path(JSON.DATA).path(JSON.NAME).asText());
        assertEquals(0, result.path(0).path(JSON.DATA).path(JSON.LENGTH).asInt());
        assertEquals(0, result.path(0).path(JSON.DATA).path(JSON.WEIGHT).asInt());
        assertThat(result.path(0).path(JSON.DATA).path(JsonOperations.CARS).isArray()).isTrue();
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
        assertThat(result.path(JSON.DATA).path(JsonOperations.CARS).isArray()).isTrue();
        // delete the kernel
        service.doDelete(JsonOperations.KERNEL, "test2", NullNode.getInstance(),
                new JsonRequest(locale, JSON.V5, JSON.GET, 42));
        // get the kernels (should be empty)
        result = service.doGetList(JsonOperations.KERNEL, NullNode.getInstance(),
                new JsonRequest(locale, JSON.V5, JSON.GET, 0));
        validate(result);
        assertThat(result.isArray()).isTrue();
        assertEquals(0, result.size());
        // create without name
        assertThatCode(() -> service.doPut(JsonOperations.KERNEL, "", NullNode.getInstance(),
                new JsonRequest(locale, JSON.V5, JSON.PUT, 42)))
                        .isExactlyInstanceOf(JsonException.class)
                        .hasFieldOrPropertyWithValue(JsonException.CODE, 400);
    }

    @Test
    public void testDeleteNonExistantKernel() throws JsonException {
        assertThat(InstanceManager.getDefault(KernelManager.class).getKernelByName("invalid-name")).isNull();
        assertThatCode(() -> service.doDelete(JsonOperations.KERNEL, "invalid-name", NullNode.getInstance(),
                new JsonRequest(locale, JSON.V5, JSON.DELETE, 42)))
                        .isExactlyInstanceOf(JsonException.class)
                        .hasFieldOrPropertyWithValue(JsonException.CODE, 404);
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
        String deleteToken = null;
        try {
            service.doDelete(JsonOperations.KERNEL, "test1", NullNode.getInstance(),
                    new JsonRequest(locale, JSON.V5, JSON.GET, 42));
            fail("Expected exception not thrown");
        } catch (JsonException ex) {
            assertEquals(409, ex.getCode());
            deleteToken = ex.getAdditionalData().path(JSON.FORCE_DELETE).asText();
            assertThat(ex.getJsonMessage().path(JSON.DATA).path(JSON.CONFLICT).isArray()).isTrue();
            assertEquals(1, ex.getJsonMessage().path(JSON.DATA).path(JSON.CONFLICT).size());
            assertEquals(JsonOperations.CAR,
                    ex.getJsonMessage().path(JSON.DATA).path(JSON.CONFLICT).path(0).path(JSON.TYPE).asText());
        }
        assertThat(deleteToken).isNotNull();
        ObjectNode data = service.getObjectMapper().createObjectNode().put(JSON.FORCE_DELETE, deleteToken);
        service.doDelete(JsonOperations.KERNEL, "test1", data, new JsonRequest(locale, JSON.V5, JSON.GET, 42));
        assertThat(c1.getKernel()).isNull();
    }

    @Test
    public void testDoPutInvalidCar() {
        // put a car without a road
        try {
            service.doPut(JsonOperations.CAR, "", service.getObjectMapper().createObjectNode()
                    .put(JSON.NUMBER, "1234"),
                    new JsonRequest(locale, JSON.V5, JSON.PUT, 0));
            fail("Expected exception not thrown");
        } catch (JsonException ex) {
            assertThat(ex.getCode()).isEqualTo(400);
            assertThat(ex.getMessage()).isEqualTo("Property \"road\" is required to create a new car.");
        }
        // put a car without a number
        try {
            service.doPut(JsonOperations.CAR, "", service.getObjectMapper().createObjectNode()
                    .put(JsonOperations.ROAD, "GNWR"),
                    new JsonRequest(locale, JSON.V5, JSON.PUT, 0));
            fail("Expected exception not thrown");
        } catch (JsonException ex) {
            assertThat(ex.getCode()).isEqualTo(400);
            assertThat(ex.getMessage()).isEqualTo("Property \"number\" is required to create a new car.");
        }
        // put an already existing car by road and number
        Car car = InstanceManager.getDefault(CarManager.class).getById("CP777");
        assertThat(car).isNotNull();
        try {
            service.doPut(JsonOperations.CAR, "", service.getObjectMapper().createObjectNode()
                    .put(JsonOperations.ROAD, car.getRoadName())
                    .put(JsonOperations.NUMBER, car.getNumber()),
                    new JsonRequest(locale, JSON.V5, JSON.PUT, 0));
            fail("Expected exception not thrown");
        } catch (JsonException ex) {
            assertThat(ex.getCode()).isEqualTo(409);
            assertThat(ex.getMessage()).isEqualTo(
                    "Unable to create new car with road number CP 777 since another car with that road number already exists.");
        }
        // put an already existing car by id
        try {
            service.doPut(JsonOperations.CAR, car.getId(), service.getObjectMapper().createObjectNode()
                    .put(JsonOperations.ROAD, car.getRoadName())
                    .put(JsonOperations.NUMBER, car.getNumber()),
                    new JsonRequest(locale, JSON.V5, JSON.PUT, 0));
            fail("Expected exception not thrown");
        } catch (JsonException ex) {
            assertThat(ex.getCode()).isEqualTo(409);
            assertThat(ex.getMessage()).isEqualTo(
                    "Unable to create new car with road number CP 777 since another car with that road number already exists.");
        }
    }

    @Test
    public void testDoPutInvalidEngine() {
        // put an engine without a road
        try {
            service.doPut(JsonOperations.ENGINE, "", service.getObjectMapper().createObjectNode()
                    .put(JsonOperations.NUMBER, "1234"),
                    new JsonRequest(locale, JSON.V5, JSON.PUT, 0));
            fail("Expected exception not thrown");
        } catch (JsonException ex) {
            assertThat(ex.getCode()).isEqualTo(400);
            assertThat(ex.getMessage()).isEqualTo("Property \"road\" is required to create a new engine.");
        }
        // put an engine without a number
        try {
            service.doPut(JsonOperations.ENGINE, "", service.getObjectMapper().createObjectNode()
                    .put(JsonOperations.ROAD, "GNWR"),
                    new JsonRequest(locale, JSON.V5, JSON.PUT, 0));
            fail("Expected exception not thrown");
        } catch (JsonException ex) {
            assertThat(ex.getCode()).isEqualTo(400);
            assertThat(ex.getMessage()).isEqualTo("Property \"number\" is required to create a new engine.");
        }
        // put an already existing engine by road and number
        Engine engine = InstanceManager.getDefault(EngineManager.class).getById("PC5016");
        assertThat(engine).isNotNull();
        try {
            service.doPut(JsonOperations.ENGINE, "", service.getObjectMapper().createObjectNode()
                    .put(JsonOperations.ROAD, engine.getRoadName())
                    .put(JsonOperations.NUMBER, engine.getNumber()),
                    new JsonRequest(locale, JSON.V5, JSON.PUT, 0));
            fail("Expected exception not thrown");
        } catch (JsonException ex) {
            assertThat(ex.getCode()).isEqualTo(409);
            assertThat(ex.getMessage()).isEqualTo(
                    "Unable to create new engine with road number PC 5016 since another engine with that road number already exists.");
        }
        // put an already existing engine by id
        try {
            service.doPut(JsonOperations.ENGINE, engine.getId(), service.getObjectMapper().createObjectNode()
                    .put(JsonOperations.ROAD, engine.getRoadName())
                    .put(JsonOperations.NUMBER, engine.getNumber()),
                    new JsonRequest(locale, JSON.V5, JSON.PUT, 0));
            fail("Expected exception not thrown");
        } catch (JsonException ex) {
            assertThat(ex.getCode()).isEqualTo(409);
            assertThat(ex.getMessage()).isEqualTo(
                    "Unable to create new engine with road number PC 5016 since another engine with that road number already exists.");
        }
    }

    @Test
    public void testDoDeleteInvalidType() {
        try {
            service.doDelete("invalid-type", "", NullNode.getInstance(),
                    new JsonRequest(locale, JSON.V5, JSON.DELETE, 0));
            fail("Expected exception not thrown");
        } catch (JsonException ex) {
            assertThat(ex.getCode()).isEqualTo(405);
        }
    }

    @Test
    public void testPostLocationInvalidReporter() throws JsonException {
        Location location = InstanceManager.getDefault(LocationManager.class).getLocationById("1");
        assertThat(location.getReporter()).isNull();
        try {
            service.doPost(JSON.LOCATION, location.getId(),
                    service.getObjectMapper().createObjectNode().put(JsonReporter.REPORTER, "no-such-reporter"),
                    new JsonRequest(locale, JSON.V5, JSON.POST, 0));
            fail("Expected exception not thrown");
        } catch (JsonException ex) {
            assertThat(ex.getCode()).isEqualTo(404);
            assertThat(ex.getMessage()).isEqualTo("Object type reporter named \"no-such-reporter\" not found.");
        }
    }

    @Test
    public void testPostTrackInvalidReporter() throws JsonException {
        Location location = InstanceManager.getDefault(LocationManager.class).getLocationById("1");
        Track track = location.getTrackById("1s1");
        assertThat(track.getReporter()).isNull();
        try {
            service.doPost(JSON.TRACK, track.getId(),
                    mapper.createObjectNode()
                            .put(JsonReporter.REPORTER, "no-such-reporter")
                            .put(JSON.LOCATION, location.getId()),
                    new JsonRequest(locale, JSON.V5, JSON.POST, 0));
            fail("Expected exception not thrown");
        } catch (JsonException ex) {
            assertThat(ex.getCode()).isEqualTo(404);
            assertThat(ex.getMessage()).isEqualTo("Object type reporter named \"no-such-reporter\" not found.");
        }
    }

    @Test
    public void testMovingCar() throws JsonException {
        Car car = InstanceManager.getDefault(CarManager.class).getByRoadAndNumber("CP", "C10099");
        Location location = car.getLocation();
        assertThat(car.getTypeName()).isEqualTo("Caboose");
        assertThat(car.getLocation().getId()).isEqualTo("1");
        assertThat(car.getTrack().getId()).isEqualTo("1s1");
        // move to same location
        service.doPost(JsonOperations.CAR, car.getId(), mapper.createObjectNode()
                .set(JSON.LOCATION, mapper.createObjectNode()
                        .put(JSON.NAME, "1")
                        .set(JSON.TRACK, mapper.createObjectNode().put(JSON.NAME, "1s1"))),
                new JsonRequest(locale, JSON.V5, JSON.POST, 0));
        assertThat(car.getLocation().getId()).isEqualTo("1");
        assertThat(car.getTrack().getId()).isEqualTo("1s1");
        // move to location with empty id
        assertThatCode(() -> service.doPost(JsonOperations.CAR, car.getId(), mapper.createObjectNode()
                .set(JSON.LOCATION,
                        mapper.createObjectNode()
                                .put(JSON.NAME, "")),
                new JsonRequest(locale, JSON.V5, JSON.POST, 0)))
                        .isExactlyInstanceOf(JsonException.class)
                        .hasFieldOrPropertyWithValue(JsonException.CODE, 404);
        // move to location with empty track id
        service.doPost(JsonOperations.CAR, car.getId(), mapper.createObjectNode()
                .set(JSON.LOCATION, mapper.createObjectNode()
                        .put(JSON.NAME, "1")
                        .set(JSON.TRACK, mapper.createObjectNode().put(JSON.NAME, ""))),
                new JsonRequest(locale, JSON.V5, JSON.POST, 0));
        assertThat(car.getLocation().getId()).isEqualTo("1");
        assertThat(car.getTrack()).isNull();
        // move to non-existent location
        assertThatCode(() -> service.doPost(JsonOperations.CAR, car.getId(), mapper.createObjectNode()
                .set(JSON.LOCATION, mapper.createObjectNode()
                        .put(JSON.NAME, "2")
                        .set(JSON.TRACK, mapper.createObjectNode().put(JSON.NAME, "2s1"))),
                new JsonRequest(locale, JSON.V5, JSON.POST, 0)))
                        .isExactlyInstanceOf(JsonException.class)
                        .hasFieldOrPropertyWithValue(JsonException.CODE, 404);
        assertThat(car.getLocation().getId()).isEqualTo("1");
        assertThat(car.getTrack()).isNull();
        // move to non-existent track
        assertThatCode(() -> service.doPost(JsonOperations.CAR, car.getId(), mapper.createObjectNode()
                .set(JSON.LOCATION, mapper.createObjectNode()
                        .put(JSON.NAME, "3")
                        .set(JSON.TRACK, mapper.createObjectNode().put(JSON.NAME, "invalid-track"))),
                new JsonRequest(locale, JSON.V5, JSON.POST, 0)))
                        .isExactlyInstanceOf(JsonException.class)
                        .hasFieldOrPropertyWithValue(JsonException.CODE, 404);
        assertThat(car.getLocation().getId()).isEqualTo("1");
        assertThat(car.getTrack()).isNull();
        // move to new location
        service.doPost(JsonOperations.CAR, car.getId(), mapper.createObjectNode()
                .set(JSON.LOCATION, mapper.createObjectNode()
                        .put(JSON.NAME, "20")
                        .set(JSON.TRACK, mapper.createObjectNode().put(JSON.NAME, "20s1"))),
                new JsonRequest(locale, JSON.V5, JSON.POST, 0));
        assertThat(car.getLocation().getId()).isEqualTo("20");
        assertThat(car.getTrack().getId()).isEqualTo("20s1");
        // move to new location without track
        service.doPost(JsonOperations.CAR, car.getId(), mapper.createObjectNode()
                .set(JSON.LOCATION, mapper.createObjectNode()
                        .put(JSON.NAME, "3")),
                new JsonRequest(locale, JSON.V5, JSON.POST, 0));
        assertThat(car.getLocation().getId()).isEqualTo("3");
        assertThat(car.getTrack()).isNull();
        // block car from track 1s1 by removing car type from track
        location.getTrackById("1s1").deleteTypeName(car.getTypeName());
        assertThat(location.getTrackById("1s1").isRollingStockAccepted(car)).startsWith(Track.TYPE);
        // move to unusable location
        assertThatCode(() -> service.doPost(JsonOperations.CAR, car.getId(), mapper.createObjectNode()
                .set(JSON.LOCATION, mapper.createObjectNode()
                        .put(JSON.NAME, "1")
                        .set(JSON.TRACK, mapper.createObjectNode().put(JSON.NAME, "1s1"))),
                new JsonRequest(locale, JSON.V5, JSON.POST, 0)))
                        .isExactlyInstanceOf(JsonException.class)
                        .hasFieldOrPropertyWithValue(JsonException.CODE, 409);
    }

    @Test
    public void testSettingDestinationForCar() throws JsonException {
        Car car = InstanceManager.getDefault(CarManager.class).getByRoadAndNumber("CP", "C10099");
        Location location = car.getLocation();
        car.setDestination(location, location.getTrackById("1s1"));
        assertThat(car.getTypeName()).isEqualTo("Caboose");
        assertThat(car.getDestination().getId()).isEqualTo("1");
        assertThat(car.getDestinationTrack().getId()).isEqualTo("1s1");
        // move to same location
        service.doPost(JsonOperations.CAR, car.getId(), mapper.createObjectNode()
                .set(JsonOperations.DESTINATION, mapper.createObjectNode()
                        .put(JSON.NAME, "1")
                        .set(JSON.TRACK, mapper.createObjectNode().put(JSON.NAME, "1s1"))),
                new JsonRequest(locale, JSON.V5, JSON.POST, 0));
        assertThat(car.getDestination().getId()).isEqualTo("1");
        assertThat(car.getDestinationTrack().getId()).isEqualTo("1s1");
        // move to location with empty id
        assertThatCode(() -> service.doPost(JsonOperations.CAR, car.getId(), mapper.createObjectNode()
                .set(JsonOperations.DESTINATION,
                        mapper.createObjectNode()
                                .put(JSON.NAME, "")),
                new JsonRequest(locale, JSON.V5, JSON.POST, 0)))
                        .isExactlyInstanceOf(JsonException.class)
                        .hasFieldOrPropertyWithValue(JsonException.CODE, 404);
        // move to location with empty track id
        service.doPost(JsonOperations.CAR, car.getId(), mapper.createObjectNode()
                .set(JsonOperations.DESTINATION, mapper.createObjectNode()
                        .put(JSON.NAME, "1")
                        .set(JSON.TRACK, mapper.createObjectNode().put(JSON.NAME, ""))),
                new JsonRequest(locale, JSON.V5, JSON.POST, 0));
        assertThat(car.getDestination().getId()).isEqualTo("1");
        assertThat(car.getDestinationTrack()).isNull();
        // move to non-existent location
        assertThatCode(() -> service.doPost(JsonOperations.CAR, car.getId(), mapper.createObjectNode()
                .set(JsonOperations.DESTINATION, mapper.createObjectNode()
                        .put(JSON.NAME, "2")
                        .set(JSON.TRACK, mapper.createObjectNode().put(JSON.NAME, "2s1"))),
                new JsonRequest(locale, JSON.V5, JSON.POST, 0)))
                        .isExactlyInstanceOf(JsonException.class)
                        .hasFieldOrPropertyWithValue(JsonException.CODE, 404);
        assertThat(car.getDestination().getId()).isEqualTo("1");
        assertThat(car.getDestinationTrack()).isNull();
        // move to non-existent track
        assertThatCode(() -> service.doPost(JsonOperations.CAR, car.getId(), mapper.createObjectNode()
                .set(JsonOperations.DESTINATION, mapper.createObjectNode()
                        .put(JSON.NAME, "3")
                        .set(JSON.TRACK, mapper.createObjectNode().put(JSON.NAME, "invalid-track"))),
                new JsonRequest(locale, JSON.V5, JSON.POST, 0)))
                        .isExactlyInstanceOf(JsonException.class)
                        .hasFieldOrPropertyWithValue(JsonException.CODE, 404);
        assertThat(car.getDestination().getId()).isEqualTo("1");
        assertThat(car.getDestinationTrack()).isNull();
        // move to new location
        service.doPost(JsonOperations.CAR, car.getId(), mapper.createObjectNode()
                .set(JsonOperations.DESTINATION, mapper.createObjectNode()
                        .put(JSON.NAME, "20")
                        .set(JSON.TRACK, mapper.createObjectNode().put(JSON.NAME, "20s1"))),
                new JsonRequest(locale, JSON.V5, JSON.POST, 0));
        assertThat(car.getDestination().getId()).isEqualTo("20");
        assertThat(car.getDestinationTrack().getId()).isEqualTo("20s1");
        // move to new location without track
        service.doPost(JsonOperations.CAR, car.getId(), mapper.createObjectNode()
                .set(JsonOperations.DESTINATION, mapper.createObjectNode()
                        .put(JSON.NAME, "3")),
                new JsonRequest(locale, JSON.V5, JSON.POST, 0));
        assertThat(car.getDestination().getId()).isEqualTo("3");
        assertThat(car.getDestinationTrack()).isNull();
        // block car from track 1s1 by removing car type from track
        location.getTrackById("1s1").deleteTypeName(car.getTypeName());
        assertThat(location.getTrackById("1s1").isRollingStockAccepted(car)).startsWith(Track.TYPE);
        // move to unusable destination
        assertThatCode(() -> service.doPost(JsonOperations.CAR, car.getId(), mapper.createObjectNode()
                .set(JsonOperations.DESTINATION, mapper.createObjectNode()
                        .put(JSON.NAME, "1")
                        .set(JSON.TRACK, mapper.createObjectNode().put(JSON.NAME, "1s1"))),
                new JsonRequest(locale, JSON.V5, JSON.POST, 0)))
                        .isExactlyInstanceOf(JsonException.class)
                        .hasFieldOrPropertyWithValue(JsonException.CODE, 409);
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
