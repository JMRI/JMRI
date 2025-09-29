package jmri.server.json.block;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;

import jmri.Block;
import jmri.BlockManager;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Reporter;
import jmri.ReporterManager;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.server.json.JSON;
import jmri.server.json.JsonException;
import jmri.server.json.JsonNamedBeanHttpServiceTestBase;
import jmri.server.json.JsonRequest;
import jmri.server.json.reporter.JsonReporter;
import jmri.server.json.sensor.JsonSensor;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 *
 * @author Randall Wood Copyright 2018
 */
public class JsonBlockHttpServiceTest extends JsonNamedBeanHttpServiceTestBase<Block, JsonBlockHttpService> {

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();
        service = new JsonBlockHttpService(mapper);
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
        BlockManager manager = InstanceManager.getDefault(BlockManager.class);
        Block block1 = manager.provideBlock("IB1");
        Sensor sensor1 = InstanceManager.getDefault(SensorManager.class).provide("IS1");
        Reporter reporter1 = InstanceManager.getDefault(ReporterManager.class).provide("IR1");
        JsonNode result;
        // test block with defaults
        result = service.doGet(JsonBlock.BLOCK, "IB1", NullNode.getInstance(), new JsonRequest(locale, JSON.V5, JSON.GET, 0));
        validate(result);
        assertEquals(JsonBlock.BLOCK, result.path(JSON.TYPE).asText());
        assertEquals("IB1", result.path(JSON.DATA).path(JSON.NAME).asText());
        assertEquals(JSON.UNKNOWN, result.path(JSON.DATA).path(JSON.STATE).asInt());
        assertTrue(result.path(JSON.DATA).path(JSON.VALUE).isNull());
        assertTrue(result.path(JSON.DATA).path(JsonSensor.SENSOR).isNull());
        assertTrue(result.path(JSON.DATA).path(JsonReporter.REPORTER).isNull());
        assertTrue(result.path(JSON.ID).isMissingNode());
        // set block state and value
        block1.setState(Block.OCCUPIED);
        block1.setValue("value is not empty");
        JUnitUtil.waitFor(() -> {
            return block1.getState() == Block.OCCUPIED;
        }, "Block to become occupied");
        result = service.doGet(JsonBlock.BLOCK, "IB1", NullNode.getInstance(), new JsonRequest(locale, JSON.V5, JSON.GET, 0));
        validate(result);
        assertEquals(JSON.ACTIVE, result.path(JSON.DATA).path(JSON.STATE).asInt());
        assertEquals("value is not empty", result.path(JSON.DATA).path(JSON.VALUE).asText());
        // change block state
        block1.setState(Block.UNOCCUPIED);
        result = service.doGet(JsonBlock.BLOCK, "IB1", NullNode.getInstance(), new JsonRequest(locale, JSON.V5, JSON.GET, 0));
        validate(result);
        assertEquals(JSON.INACTIVE, result.path(JSON.DATA).path(JSON.STATE).asInt());
        // add a sensor and reporter to the block
        block1.setSensor(sensor1.getSystemName());
        block1.setReporter(reporter1);
        result = service.doGet(JsonBlock.BLOCK, "IB1", NullNode.getInstance(), new JsonRequest(locale, JSON.V5, JSON.GET, 0));
        assertEquals(sensor1.getSystemName(), result.path(JSON.DATA).path(JsonSensor.SENSOR).asText());
        assertEquals(reporter1.getSystemName(), result.path(JSON.DATA).path(JsonReporter.REPORTER).asText());
        JsonException ex = assertThrows( JsonException.class, () -> {
            // add an invalid block by using a turnout name instead of a block name
            assertNull(manager.getBlock("IT1"));
            service.doGet(JsonBlock.BLOCK, "IT1", NullNode.getInstance(),
                new JsonRequest(locale, JSON.V5, JSON.GET, 0));
        }, "Expected exception not thrown.");
        assertEquals(404, ex.getCode());
    }

    @Test
    public void testDoPost() throws JmriException, IOException, JsonException {
        BlockManager manager = InstanceManager.getDefault(BlockManager.class);
        Block block1 = manager.provideBlock("IB1");
        // set off
        JsonNode message = mapper.createObjectNode().put(JSON.NAME, "IB1").put(JSON.STATE, JSON.INACTIVE);
        JsonNode result = service.doPost(JsonBlock.BLOCK, "IB1", message, new JsonRequest(locale, JSON.V5, JSON.GET, 0));
        assertEquals(Block.UNOCCUPIED, block1.getState());
        validate(result);
        assertEquals(JSON.INACTIVE, result.path(JSON.DATA).path(JSON.STATE).asInt());
        // set on
        message = mapper.createObjectNode().put(JSON.NAME, "IB1").put(JSON.STATE, JSON.ACTIVE);
        result = service.doPost(JsonBlock.BLOCK, "IB1", message, new JsonRequest(locale, JSON.V5, JSON.GET, 0));
        assertEquals(Block.OCCUPIED, block1.getState());
        validate(result);
        assertEquals(JSON.ACTIVE, result.path(JSON.DATA).path(JSON.STATE).asInt());
        // set unknown - remains on
        message = mapper.createObjectNode().put(JSON.NAME, "IB1").put(JSON.STATE, JSON.UNKNOWN);
        result = service.doPost(JsonBlock.BLOCK, "IB1", message, new JsonRequest(locale, JSON.V5, JSON.GET, 0));
        assertEquals(Block.OCCUPIED, block1.getState());
        validate(result);
        assertEquals(JSON.ACTIVE, result.path(JSON.DATA).path(JSON.STATE).asInt());
        // set invalid state
        JsonNode message2 = mapper.createObjectNode().put(JSON.NAME, "IB1").put(JSON.STATE, 42); // Invalid value
        JsonException ex = assertThrows( JsonException.class, () ->
            service.doPost(JsonBlock.BLOCK, "IB1", message2,
                new JsonRequest(locale, JSON.V5, JSON.GET, 0)),
            "Expected exception not thrown.");
        assertEquals(400, ex.getCode());

        assertEquals(Block.OCCUPIED, block1.getState());
        // set value
        message = mapper.createObjectNode().put(JSON.NAME, "IB1").put(JSON.VALUE, "some value");
        assertNull( block1.getValue(), "Null block value");
        service.doPost(JsonBlock.BLOCK, "IB1", message, new JsonRequest(locale, JSON.V5, JSON.GET, 0));
        assertEquals( "some value", block1.getValue(), "Non-null block value");
        // set null value
        message = mapper.createObjectNode().put(JSON.NAME, "IB1").putNull(JSON.VALUE);
        assertNotNull( block1.getValue(), "Non-null block value");
        service.doPost(JsonBlock.BLOCK, "IB1", message, new JsonRequest(locale, JSON.V5, JSON.GET, 0));
        assertNull( block1.getValue(), "Null block value");
        // set non-existing sensor
        JsonNode message3 = mapper.createObjectNode().put(JSON.NAME, "IB1").put(JsonSensor.SENSOR, "IS1");
        assertNull( block1.getSensor(), "No sensor");
        ex = assertThrows( JsonException.class, () ->
            service.doPost(JsonBlock.BLOCK, "IB1", message3,
                new JsonRequest(locale, JSON.V5, JSON.GET, 0)),
            "Expected exception not thrown");
        assertEquals( 404, ex.getCode(), "404 Not Found");

        // set existing sensor
        Sensor sensor1 = InstanceManager.getDefault(SensorManager.class).provide("IS1");
        service.doPost(JsonBlock.BLOCK, "IB1", message3, new JsonRequest(locale, JSON.V5, JSON.GET, 0));
        assertEquals( sensor1, block1.getSensor(), "Block has sensor");
        // set null sensor
        message = mapper.createObjectNode().put(JSON.NAME, "IB1").putNull(JsonSensor.SENSOR);
        service.doPost(JsonBlock.BLOCK, "IB1", message, new JsonRequest(locale, JSON.V5, JSON.GET, 0));
        assertNull( block1.getSensor(), "Block has no sensor");
        // set non-existing reporter
        JsonNode message4 = mapper.createObjectNode().put(JSON.NAME, "IB1").put(JsonReporter.REPORTER, "IR1");
        assertNull( block1.getReporter(), "No reporter");
        ex = assertThrows( JsonException.class, () ->
            service.doPost(JsonBlock.BLOCK, "IB1", message4, new JsonRequest(locale, JSON.V5, JSON.GET, 0)),
            "Expected exception not thrown");
        assertEquals( 404, ex.getCode(), "404 Not Found");

        // set existing reporter
        Reporter reporter1 = InstanceManager.getDefault(ReporterManager.class).provide("IR1");
        service.doPost(JsonBlock.BLOCK, "IB1", message4, new JsonRequest(locale, JSON.V5, JSON.GET, 0));
        assertEquals( reporter1, block1.getReporter(), "Block has reporter");
        // set null reporter
        message = mapper.createObjectNode().put(JSON.NAME, "IB1").putNull(JsonReporter.REPORTER);
        service.doPost(JsonBlock.BLOCK, "IB1", message, new JsonRequest(locale, JSON.V5, JSON.GET, 0));
        assertNull( block1.getReporter(), "No reporter");
        ex = assertThrows( JsonException.class, () -> {
            // add an invalid block by using a turnout name instead of a block name
            assertNull(manager.getBlock("IT1"));
            JsonNode message5 = mapper.createObjectNode().put(JSON.NAME, "II1").put(JSON.STATE, Block.UNOCCUPIED);
            service.doPost(JsonBlock.BLOCK, "IT1", message5, new JsonRequest(locale, JSON.V5, JSON.GET, 0));
        }, "Expected exception not thrown.");
        assertEquals(404, ex.getCode());
    }

    @Test
    public void testDoPut() throws IOException, JsonException {
        BlockManager manager = InstanceManager.getDefault(BlockManager.class);
        // add a block
        assertNull(manager.getBlock("IB1"));
        JsonNode message = mapper.createObjectNode().put(JSON.NAME, "IB1").put(JSON.STATE, Block.UNOCCUPIED);
        JsonNode result = service.doPut(JsonBlock.BLOCK, "IB1", message, new JsonRequest(locale, JSON.V5, JSON.GET, 0));
        validate(result);
        assertNotNull(manager.getBlock("IB1"));
        JsonException ex = assertThrows( JsonException.class, () -> {
            // add an invalid block by using a turnout name instead of a block name
            assertNull(manager.getBlock("IT1"));
            JsonNode message2 = mapper.createObjectNode().put(JSON.NAME, "II1").put(JSON.STATE, Block.UNOCCUPIED);
            service.doPut(JsonBlock.BLOCK, "", message2, new JsonRequest(locale, JSON.V5, JSON.GET, 0)); // use an empty name to trigger exception
        }, "Expected exception not thrown.");
        assertEquals(400, ex.getCode());
    }

    @Test
    @Override
    public void testDoDelete() throws JsonException {
        assumeTrue(service != null, "protect against JUnit tests in Eclipse that test this class directly");
        BlockManager manager = InstanceManager.getDefault(BlockManager.class);
        ObjectNode message = mapper.createObjectNode();
        // delete non-existent bean
        JsonException ex = assertThrows( JsonException.class, () ->
            service.doDelete(service.getType(), "non-existant", message,
                new JsonRequest(locale, JSON.V5, JSON.GET, 42)),
            "Expected exception not thrown.");
        assertEquals( 404, ex.getCode(), "Code is HTTP NOT FOUND");
        assertEquals( "Object type block named \"non-existant\" not found.", ex.getMessage(), "Message");
        assertEquals( 42, ex.getId(), "ID is 42");

        manager.createNewBlock("IB1", null);
        // delete existing bean (no named listener)
        assertNotNull(manager.getBySystemName("IB1"));
        service.doDelete(service.getType(), "IB1", message, new JsonRequest(locale, JSON.V5, JSON.GET, 42));
        assertNull(manager.getBySystemName("IB1"));
        Block block = manager.createNewBlock("IB1", null);
        assertNotNull(block);
        block.addPropertyChangeListener(evt -> {
            // do nothing
        }, "IB1", "Test Listener");
        // delete existing bean (with named listener)
        ex = assertThrows( JsonException.class, () ->
            service.doDelete(service.getType(), "IB1", message,
                new JsonRequest(locale, JSON.V5, JSON.GET, 42)),
            "Expected exception not thrown.");
        assertEquals(409, ex.getCode());
        assertEquals(1, ex.getAdditionalData().path(JSON.CONFLICT).size());
        assertEquals("Test Listener", ex.getAdditionalData().path(JSON.CONFLICT).path(0).asText());
        ObjectNode message2 = message.put(JSON.FORCE_DELETE, ex.getAdditionalData().path(JSON.FORCE_DELETE).asText());

        assertNotNull(manager.getBySystemName("IB1"));
        // will throw if prior catch failed
        service.doDelete(service.getType(), "IB1", message2, new JsonRequest(locale, JSON.V5, JSON.GET, 0));
        assertNull(manager.getBySystemName("IB1"));
        ex = assertThrows( JsonException.class, () ->
            // deleting again should throw an exception
            service.doDelete(service.getType(), "IB1", NullNode.getInstance(),
                new JsonRequest(locale, JSON.V5, JSON.GET, 0)),
            "Expected exception not thrown.");
        assertEquals(404, ex.getCode());
    }

    /**
     * Test of doGetList method, of class JsonBlockHttpService.
     * @throws java.lang.Exception rethrows any exceptions from instance.doGetList()
     */
    @Test
    public void testDoGetList() throws Exception {
        InstanceManager.getDefault(BlockManager.class).createNewBlock("test");
        JsonBlockHttpService instance = new JsonBlockHttpService(mapper);
        JsonNode result = instance.doGetList(JsonBlock.BLOCK, mapper.createObjectNode(), new JsonRequest(locale, JSON.V5, JSON.GET, 0));
        assertEquals(1, result.size());
        validate(result);
    }

    /**
     * Test of doSchema method, of class JsonBlockHttpService.
     *
     * @throws jmri.server.json.JsonException if something goes wrong
     */
    @Override
    @Test
    public void testDoSchema() throws JsonException {
        testDoSchema(JsonBlock.BLOCK, JsonBlock.BLOCKS);
    }

}
