package jmri.server.json.block;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeNotNull;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
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
import jmri.server.json.reporter.JsonReporter;
import jmri.server.json.sensor.JsonSensor;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Randall Wood Copyright 2018
 */
public class JsonBlockHttpServiceTest extends JsonNamedBeanHttpServiceTestBase<Block, JsonBlockHttpService> {

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        service = new JsonBlockHttpService(mapper);
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initDebugPowerManager();
        JUnitUtil.initReporterManager();
    }

    @After
    @Override
    public void tearDown() throws Exception {
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
        result = service.doGet(JsonBlock.BLOCK, "IB1", NullNode.getInstance(), locale, 0);
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
        result = service.doGet(JsonBlock.BLOCK, "IB1", NullNode.getInstance(), locale, 0);
        validate(result);
        assertEquals(JSON.ACTIVE, result.path(JSON.DATA).path(JSON.STATE).asInt());
        assertEquals("value is not empty", result.path(JSON.DATA).path(JSON.VALUE).asText());
        // change block state
        block1.setState(Block.UNOCCUPIED);
        result = service.doGet(JsonBlock.BLOCK, "IB1", NullNode.getInstance(), locale, 0);
        validate(result);
        assertEquals(JSON.INACTIVE, result.path(JSON.DATA).path(JSON.STATE).asInt());
        // add a sensor and reporter to the block
        block1.setSensor(sensor1.getSystemName());
        block1.setReporter(reporter1);
        result = service.doGet(JsonBlock.BLOCK, "IB1", NullNode.getInstance(), locale, 0);
        assertEquals(sensor1.getSystemName(), result.path(JSON.DATA).path(JsonSensor.SENSOR).asText());
        assertEquals(reporter1.getSystemName(), result.path(JSON.DATA).path(JsonReporter.REPORTER).asText());
        try {
            // add an invalid block by using a turnout name instead of a block name
            assertNull(manager.getBlock("IT1"));
            service.doGet(JsonBlock.BLOCK, "IT1", NullNode.getInstance(), locale, 0);
            fail("Expected exception not thrown.");
        } catch (JsonException ex) {
            assertEquals(404, ex.getCode());
        }
    }

    @Test
    public void testDoPost() throws JmriException, IOException, JsonException {
        BlockManager manager = InstanceManager.getDefault(BlockManager.class);
        Block block1 = manager.provideBlock("IB1");
        // set off
        JsonNode message = mapper.createObjectNode().put(JSON.NAME, "IB1").put(JSON.STATE, JSON.INACTIVE);
        JsonNode result = service.doPost(JsonBlock.BLOCK, "IB1", message, locale, 0);
        assertEquals(Block.UNOCCUPIED, block1.getState());
        validate(result);
        assertEquals(JSON.INACTIVE, result.path(JSON.DATA).path(JSON.STATE).asInt());
        // set on
        message = mapper.createObjectNode().put(JSON.NAME, "IB1").put(JSON.STATE, JSON.ACTIVE);
        result = service.doPost(JsonBlock.BLOCK, "IB1", message, locale, 0);
        assertEquals(Block.OCCUPIED, block1.getState());
        validate(result);
        assertEquals(JSON.ACTIVE, result.path(JSON.DATA).path(JSON.STATE).asInt());
        // set unknown - remains on
        message = mapper.createObjectNode().put(JSON.NAME, "IB1").put(JSON.STATE, JSON.UNKNOWN);
        result = service.doPost(JsonBlock.BLOCK, "IB1", message, locale, 0);
        assertEquals(Block.OCCUPIED, block1.getState());
        validate(result);
        assertEquals(JSON.ACTIVE, result.path(JSON.DATA).path(JSON.STATE).asInt());
        // set invalid state
        message = mapper.createObjectNode().put(JSON.NAME, "IB1").put(JSON.STATE, 42); // Invalid value
        try {
            service.doPost(JsonBlock.BLOCK, "IB1", message, locale, 0);
            fail("Expected exception not thrown.");
        } catch (JsonException ex) {
            assertEquals(400, ex.getCode());
        }
        assertEquals(Block.OCCUPIED, block1.getState());
        // set value
        message = mapper.createObjectNode().put(JSON.NAME, "IB1").put(JSON.VALUE, "some value");
        assertNull("Null block value", block1.getValue());
        service.doPost(JsonBlock.BLOCK, "IB1", message, locale, 0);
        assertEquals("Non-null block value", "some value", block1.getValue());
        // set null value
        message = mapper.createObjectNode().put(JSON.NAME, "IB1").putNull(JSON.VALUE);
        assertNotNull("Non-null block value", block1.getValue());
        service.doPost(JsonBlock.BLOCK, "IB1", message, locale, 0);
        assertNull("Null block value", block1.getValue());
        // set non-existing sensor
        message = mapper.createObjectNode().put(JSON.NAME, "IB1").put(JsonSensor.SENSOR, "IS1");
        assertNull("No sensor", block1.getSensor());
        try {
            service.doPost(JsonBlock.BLOCK, "IB1", message, locale, 0);
            fail("Expected exception not thrown");
        } catch (JsonException ex) {
            assertEquals("404 Not Found", 404, ex.getCode());
        }
        // set existing sensor
        Sensor sensor1 = InstanceManager.getDefault(SensorManager.class).provide("IS1");
        service.doPost(JsonBlock.BLOCK, "IB1", message, locale, 0);
        assertEquals("Block has sensor", sensor1, block1.getSensor());
        // set null sensor
        message = mapper.createObjectNode().put(JSON.NAME, "IB1").putNull(JsonSensor.SENSOR);
        service.doPost(JsonBlock.BLOCK, "IB1", message, locale, 0);
        assertNull("Block has no sensor", block1.getSensor());
        // set non-existing reporter
        message = mapper.createObjectNode().put(JSON.NAME, "IB1").put(JsonReporter.REPORTER, "IR1");
        assertNull("No reporter", block1.getReporter());
        try {
            service.doPost(JsonBlock.BLOCK, "IB1", message, locale, 0);
            fail("Expected exception not thrown");
        } catch (JsonException ex) {
            assertEquals("404 Not Found", 404, ex.getCode());
        }
        // set existing reporter
        Reporter reporter1 = InstanceManager.getDefault(ReporterManager.class).provide("IR1");
        service.doPost(JsonBlock.BLOCK, "IB1", message, locale, 0);
        assertEquals("Block has reporter", reporter1, block1.getReporter());
        // set null reporter
        message = mapper.createObjectNode().put(JSON.NAME, "IB1").putNull(JsonReporter.REPORTER);
        service.doPost(JsonBlock.BLOCK, "IB1", message, locale, 0);
        assertNull("No reporter", block1.getReporter());
        try {
            // add an invalid block by using a turnout name instead of a block name
            assertNull(manager.getBlock("IT1"));
            message = mapper.createObjectNode().put(JSON.NAME, "II1").put(JSON.STATE, Block.UNOCCUPIED);
            service.doPost(JsonBlock.BLOCK, "IT1", message, locale, 0);
            fail("Expected exception not thrown.");
        } catch (JsonException ex) {
            assertEquals(404, ex.getCode());
        }
    }

    @Test
    public void testDoPut() throws IOException, JsonException {
        BlockManager manager = InstanceManager.getDefault(BlockManager.class);
        // add a block
        assertNull(manager.getBlock("IB1"));
        JsonNode message = mapper.createObjectNode().put(JSON.NAME, "IB1").put(JSON.STATE, Block.UNOCCUPIED);
        JsonNode result = service.doPut(JsonBlock.BLOCK, "IB1", message, locale, 0);
        validate(result);
        assertNotNull(manager.getBlock("IB1"));
        try {
            // add an invalid block by using a turnout name instead of a block name
            assertNull(manager.getBlock("IT1"));
            message = mapper.createObjectNode().put(JSON.NAME, "II1").put(JSON.STATE, Block.UNOCCUPIED);
            service.doPut(JsonBlock.BLOCK, "", message, locale, 0); // use an empty name to trigger exception
            fail("Expected exception not thrown.");
        } catch (JsonException ex) {
            assertEquals(400, ex.getCode());
        }
    }

    @Test
    @Override
    public void testDoDelete() throws JsonException {
        BlockManager manager = InstanceManager.getDefault(BlockManager.class);
        ObjectNode message = mapper.createObjectNode();
        // delete non-existent bean
        try {
            assumeNotNull(service); // protect against JUnit tests in Eclipse that test this class directly
            service.doDelete(service.getType(), "non-existant", message, locale, 42);
            fail("Expected exception not thrown.");
        } catch (JsonException ex) {
            assertEquals("Code is HTTP NOT FOUND", 404, ex.getCode());
            assertEquals("Message", "Object type block named \"non-existant\" not found.", ex.getMessage());
            assertEquals("ID is 42", 42, ex.getId());
        }
        manager.createNewBlock("IB1", null);
        // delete existing bean (no named listener)
        assertNotNull(manager.getBeanBySystemName("IB1"));
        service.doDelete(service.getType(), "IB1", message, locale, 42);
        assertNull(manager.getBeanBySystemName("IB1"));
        Block block = manager.createNewBlock("IB1", null);
        assertNotNull(block);
        block.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                // do nothing
            }
        }, "IB1", "Test Listener");
        // delete existing bean (with named listener)
        try {
            service.doDelete(service.getType(), "IB1", message, locale, 42);
            fail("Expected exception not thrown.");
        } catch (JsonException ex) {
            assertEquals(409, ex.getCode());
            assertEquals(1, ex.getAdditionalData().path(JSON.CONFLICT).size());
            assertEquals("Test Listener", ex.getAdditionalData().path(JSON.CONFLICT).path(0).asText());
            message = message.put(JSON.FORCE_DELETE, ex.getAdditionalData().path(JSON.FORCE_DELETE).asText());
        }
        assertNotNull(manager.getBeanBySystemName("IB1"));
        // will throw if prior catch failed
        service.doDelete(service.getType(), "IB1", message, locale, 0);
        assertNull(manager.getBeanBySystemName("IB1"));
        try {
            // deleting again should throw an exception
            service.doDelete(service.getType(), "IB1", NullNode.getInstance(), locale, 0);
            fail("Expected exception not thrown.");
        } catch (JsonException ex) {
            assertEquals(404, ex.getCode());
        }
    }

    /**
     * Test of doGetList method, of class JsonBlockHttpService.
     * @throws java.lang.Exception rethrows any exceptions from instance.doGetList()
     */
    @Test
    public void testDoGetList() throws Exception {
        InstanceManager.getDefault(BlockManager.class).createNewBlock("test");
        JsonBlockHttpService instance = new JsonBlockHttpService(mapper);
        JsonNode result = instance.doGetList(JsonBlock.BLOCK, mapper.createObjectNode(), locale, 0);
        assertEquals(1, result.size());
        validate(result);
    }

    /**
     * Test of doSchema method, of class JsonBlockHttpService.
     *
     * @throws jmri.server.json.JsonException if something goes wrong
     */
    @Test
    public void testDoSchema() throws JsonException {
        testDoSchema(JsonBlock.BLOCK, JsonBlock.BLOCKS);
    }

}
