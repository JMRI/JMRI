package jmri.server.json.oblock;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jmri.*;
import jmri.jmrit.logix.OBlock;
import jmri.jmrit.logix.OBlockManager;
import jmri.server.json.JSON;
import jmri.server.json.JsonException;
import jmri.server.json.JsonNamedBeanHttpServiceTestBase;
import jmri.server.json.JsonRequest;
//import jmri.server.json.reporter.JsonReporter;
import jmri.server.json.sensor.JsonSensor;
import jmri.util.JUnitUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 *
 * @author Randall Wood Copyright 2018
 * @author Egbert Broerse Copyright 2020
 */
public class JsonOblockHttpServiceTest extends JsonNamedBeanHttpServiceTestBase<OBlock, JsonOblockHttpService> {

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();
        service = new JsonOblockHttpService(mapper);
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initDebugPowerManager();
        JUnitUtil.initReporterManager();
    }

    @AfterEach
    @Override
    public void tearDown() throws Exception {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        super.tearDown();
    }

    @Test
    @Override
    public void testDoGet() throws JmriException, IOException, JsonException {
        OBlockManager manager = InstanceManager.getDefault(OBlockManager.class);
        OBlock oblock1 = manager.provideOBlock("OB1");
        Sensor sensor1 = InstanceManager.getDefault(SensorManager.class).provide("IS1");
        JsonNode result;
        // test block with defaults
        result = service.doGet(JsonOblock.OBLOCK, "OB1", NullNode.getInstance(), new JsonRequest(locale, JSON.V5, JSON.GET, 0));
        validate(result);
        assertEquals(JsonOblock.OBLOCK, result.path(JSON.TYPE).asText());
        assertEquals("OB1", result.path(JSON.DATA).path(JSON.NAME).asText());
        assertEquals(JSON.UNKNOWN, result.path(JSON.DATA).path(JsonOblock.STATUS).asInt());
        assertTrue(result.path(JSON.DATA).path(JsonOblock.WARRANT).isNull());
        assertTrue(result.path(JSON.ID).isMissingNode());
        // set block state and value
        oblock1.setState(OBlock.OCCUPIED);
        oblock1.setValue("value is not empty");
        JUnitUtil.waitFor(() -> {
            return oblock1.getState() == Block.OCCUPIED;
        }, "OBlock to become occupied");
        result = service.doGet(JsonOblock.OBLOCK, "OB1", NullNode.getInstance(), new JsonRequest(locale, JSON.V5, JSON.GET, 0));
        validate(result);
        assertEquals(JSON.ACTIVE, result.path(JSON.DATA).path(JsonOblock.STATUS).asInt());
        // change oblock state
        oblock1.setState(Block.UNOCCUPIED);
        result = service.doGet(JsonOblock.OBLOCK, "OB1", NullNode.getInstance(), new JsonRequest(locale, JSON.V5, JSON.GET, 0));
        validate(result);
        assertEquals(JSON.INACTIVE, result.path(JSON.DATA).path(JsonOblock.STATUS).asInt());
        // add a sensor and reporter to the oblock
        oblock1.setSensor(sensor1.getSystemName());
        result = service.doGet(JsonOblock.OBLOCK, "OB1", NullNode.getInstance(), new JsonRequest(locale, JSON.V5, JSON.GET, 0));
        validate(result);
        assertEquals(sensor1.getSystemName(), result.path(JSON.DATA).path(JsonSensor.SENSOR).asText());
        JsonException ex = assertThrows( JsonException.class, () -> {
            // add an invalid oblock by using a turnout name instead of an oblock name
            assertNull(manager.getOBlock("IT1"));
            service.doGet(JsonOblock.OBLOCK, "IT1", NullNode.getInstance(), new JsonRequest(locale, JSON.V5, JSON.GET, 0));
        },"Expected exception not thrown.");
        assertEquals(404, ex.getCode());
    }

    @Test
    public void testDoPost() throws JmriException, IOException, JsonException {
        OBlockManager manager = InstanceManager.getDefault(OBlockManager.class);
        OBlock oblock1 = manager.provideOBlock("OB1");
        // set off
        JsonNode message = mapper.createObjectNode().put(JSON.NAME, "OB1").put(JsonOblock.STATUS, JSON.INACTIVE);
        JsonNode result = service.doPost(JsonOblock.OBLOCK, "OB1", message, new JsonRequest(locale, JSON.V5, JSON.GET, 0));
        assertEquals(Block.UNOCCUPIED, oblock1.getState());
        validate(result);
        assertEquals(JSON.INACTIVE, result.path(JSON.DATA).path(JsonOblock.STATUS).asInt());
        // set on
        message = mapper.createObjectNode().put(JSON.NAME, "OB1").put(JsonOblock.STATUS, JSON.ACTIVE);
        result = service.doPost(JsonOblock.OBLOCK, "OB1", message, new JsonRequest(locale, JSON.V5, JSON.GET, 0));
        assertEquals(Block.OCCUPIED, oblock1.getState());
        validate(result);
        assertEquals(JSON.ACTIVE, result.path(JSON.DATA).path(JsonOblock.STATUS).asInt());
        // set unknown - remains on
        message = mapper.createObjectNode().put(JSON.NAME, "OB1").put(JsonOblock.STATUS, JSON.UNKNOWN);
        result = service.doPost(JsonOblock.OBLOCK, "OB1", message, new JsonRequest(locale, JSON.V5, JSON.GET, 0));
        assertEquals(Block.OCCUPIED, oblock1.getState());
        validate(result);
        assertEquals(JSON.ACTIVE, result.path(JSON.DATA).path(JsonOblock.STATUS).asInt());
        // set invalid state
        JsonNode messageEx = mapper.createObjectNode().put(JSON.NAME, "OB1").put(JsonOblock.STATUS, 111); // Invalid value
        JsonException ex = assertThrows( JsonException.class, () ->
            service.doPost(JsonOblock.OBLOCK, "OB1", messageEx,
                new JsonRequest(locale, JSON.V5, JSON.GET, 0)),
            "Expected exception not thrown.");
        assertEquals(400, ex.getCode());

        assertEquals(Block.OCCUPIED, oblock1.getState());
        // set value
        message = mapper.createObjectNode().put(JSON.NAME, "OB1").put(JSON.VALUE, "some value");
        assertNull( oblock1.getValue(), "Null block value");
        service.doPost(JsonOblock.OBLOCK, "OB1", message, new JsonRequest(locale, JSON.V5, JSON.GET, 0));
        assertEquals( "some value", oblock1.getValue(), "Non-null block value");
        // set null value
        message = mapper.createObjectNode().put(JSON.NAME, "OB1").putNull(JSON.VALUE);
        assertNotNull( oblock1.getValue(), "Non-null block value");
        service.doPost(JsonOblock.OBLOCK, "OB1", message, new JsonRequest(locale, JSON.V5, JSON.GET, 0));
        assertNull( oblock1.getValue(), "Null block value");
        // set non-existing sensor
        JsonNode messageEx2 = mapper.createObjectNode().put(JSON.NAME, "OB1").put(JsonSensor.SENSOR, "IS1");
        assertNull( oblock1.getSensor(), "No sensor");
        ex = assertThrows( JsonException.class, () ->
            service.doPost(JsonOblock.OBLOCK, "OB1", messageEx2, new JsonRequest(locale, JSON.V5, JSON.GET, 0)),
            "Expected exception not thrown");
        assertEquals( 404, ex.getCode(), "404 Not Found");

        // set existing sensor
        Sensor sensor1 = InstanceManager.getDefault(SensorManager.class).provide("IS1");
        service.doPost(JsonOblock.OBLOCK, "OB1", messageEx2, new JsonRequest(locale, JSON.V5, JSON.GET, 0));
        assertEquals( sensor1, oblock1.getSensor(), "Block has sensor");
        // set null sensor
        message = mapper.createObjectNode().put(JSON.NAME, "OB1").putNull(JsonSensor.SENSOR);
        service.doPost(JsonOblock.OBLOCK, "OB1", message, new JsonRequest(locale, JSON.V5, JSON.GET, 0));
        assertNull( oblock1.getSensor(), "Oblock has no sensor");
        // reporters not copied from JsonBlockHttpServiceTest since they are not used in OBlocks
        ex = assertThrows( JsonException.class, () -> {
            // add an invalid oblock by using a turnout name instead of a oblock name
            assertNull(manager.getOBlock("IT1"));
            JsonNode messageEx3 = mapper.createObjectNode().put(JSON.NAME, "II1").put(JsonOblock.STATUS, Block.UNOCCUPIED);
            service.doPost(JsonOblock.OBLOCK, "IT1", messageEx3, new JsonRequest(locale, JSON.V5, JSON.GET, 0));
        }, "Expected exception not thrown.");
        assertEquals(404, ex.getCode());
    }

    @Test
    public void testDoPut() throws IOException, JsonException {
        OBlockManager manager = InstanceManager.getDefault(OBlockManager.class);
        // add a Oblock
        assertNull(manager.getOBlock("OB1"));
        JsonNode message = mapper.createObjectNode().put(JSON.NAME, "OB1").put(JsonOblock.STATUS, Block.UNOCCUPIED);
        JsonNode result = service.doPut(JsonOblock.OBLOCK, "OB1", message, new JsonRequest(locale, JSON.V5, JSON.GET, 0));
        validate(result);
        assertNotNull(manager.getOBlock("OB1"));
        JsonException ex = assertThrows( JsonException.class, () -> {
            // add an invalid Oblock by using a turnout name instead of a Oblock name
            assertNull(manager.getOBlock("IT1"));
            JsonNode messageEx = mapper.createObjectNode().put(JSON.NAME, "II1").put(JsonOblock.STATUS, Block.UNOCCUPIED);
            service.doPut(JsonOblock.OBLOCK, "", messageEx, new JsonRequest(locale, JSON.V5, JSON.GET, 0)); // use an empty name to trigger exception
        }, "Expected exception not thrown.");
        assertEquals(400, ex.getCode());
    }

    @Test
    @Override
    public void testDoDelete() throws JsonException {
        assumeTrue(service != null, "protect against JUnit tests in Eclipse that test this class directly");
        OBlockManager manager = InstanceManager.getDefault(OBlockManager.class);
        ObjectNode message = mapper.createObjectNode();
        // delete non-existent bean
        JsonException ex = assertThrows( JsonException.class, () ->
            service.doDelete(service.getType(), "non-existant", message, new JsonRequest(locale, JSON.V5, JSON.GET, 42)),
            "Expected exception not thrown.");
        assertEquals( 404, ex.getCode(), "Code is HTTP NOT FOUND");
        assertEquals( "Object type oblock named \"non-existant\" not found.", ex.getMessage(), "Message");
        assertEquals( 42, ex.getId(), "ID is 42");

        manager.createNewOBlock("OB1", null);
        // delete existing bean (no named listener)
        assertNotNull(manager.getBySystemName("OB1"));
        service.doDelete(service.getType(), "OB1", message, new JsonRequest(locale, JSON.V5, JSON.GET, 42));
        assertNull(manager.getBySystemName("OB1"));
        OBlock oblock = manager.createNewOBlock("OB1", null);
        assertNotNull(oblock);
        oblock.addPropertyChangeListener(evt -> {
            // do nothing
        }, "OB1", "Test Listener");
        // delete existing bean (with named listener)
        ex = assertThrows( JsonException.class, () ->
            service.doDelete(service.getType(), "OB1", message,
                new JsonRequest(locale, JSON.V5, JSON.GET, 42)),
            "Expected exception not thrown.");
        assertEquals(409, ex.getCode());
        assertEquals(1, ex.getAdditionalData().path(JSON.CONFLICT).size());
        assertEquals("Test Listener", ex.getAdditionalData().path(JSON.CONFLICT).path(0).asText());
        ObjectNode message2 = message.put(JSON.FORCE_DELETE, ex.getAdditionalData().path(JSON.FORCE_DELETE).asText());

        assertNotNull(manager.getBySystemName("OB1"));
        // will throw if prior catch failed
        service.doDelete(service.getType(), "OB1", message2, new JsonRequest(locale, JSON.V5, JSON.GET, 0));
        assertNull(manager.getBySystemName("OB1"));
        ex = assertThrows( JsonException.class, () ->
            // deleting again should throw an exception
            service.doDelete(service.getType(), "OB1", NullNode.getInstance(),
                new JsonRequest(locale, JSON.V5, JSON.GET, 0)),
            "Expected exception not thrown.");
        assertEquals(404, ex.getCode());
    }

    /**
     * Test of doGetList method, of class JsonOblockHttpService.
     * @throws Exception rethrows any exceptions from instance.doGetList()
     */
    @Test
    public void testDoGetList() throws Exception {
        InstanceManager.getDefault(OBlockManager.class).createNewOBlock("OB9", null);
        JsonOblockHttpService instance = new JsonOblockHttpService(mapper);
        JsonNode result = instance.doGetList(JsonOblock.OBLOCK, mapper.createObjectNode(), new JsonRequest(locale, JSON.V5, JSON.GET, 0));
        assertEquals(1, result.size());
        validate(result);
    }

    /**
     * Test of doSchema method, of class JsonOblockHttpService.
     *
     * @throws JsonException if something goes wrong
     */
    @Override
    @Test
    public void testDoSchema() throws JsonException {
        testDoSchema(JsonOblock.OBLOCK, JsonOblock.OBLOCKS);
    }

}
