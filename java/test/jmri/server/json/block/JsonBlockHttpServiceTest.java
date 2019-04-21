package jmri.server.json.block;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.Locale;
import javax.servlet.http.HttpServletResponse;
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
import jmri.server.json.JsonHttpServiceTestBase;
import jmri.server.json.reporter.JsonReporter;
import jmri.server.json.sensor.JsonSensor;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Randall Wood Copyright 2018
 */
public class JsonBlockHttpServiceTest extends JsonHttpServiceTestBase {

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
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
    public void testDoGet() throws JmriException, IOException {
        JsonBlockHttpService service = new JsonBlockHttpService(mapper);
        BlockManager manager = InstanceManager.getDefault(BlockManager.class);
        Block block1 = manager.provideBlock("IB1");
        Sensor sensor1 = InstanceManager.getDefault(SensorManager.class).provide("IS1");
        Reporter reporter1 = InstanceManager.getDefault(ReporterManager.class).provide("IR1");
        JsonNode result;
        try {
            // test block with defaults
            result = service.doGet(JsonBlock.BLOCK, "IB1", service.getObjectMapper().createObjectNode(), locale);
            Assert.assertNotNull(result);
            this.validate(result);
            Assert.assertEquals(JsonBlock.BLOCK, result.path(JSON.TYPE).asText());
            Assert.assertEquals("IB1", result.path(JSON.DATA).path(JSON.NAME).asText());
            Assert.assertEquals(JSON.UNKNOWN, result.path(JSON.DATA).path(JSON.STATE).asInt());
            Assert.assertTrue(result.path(JSON.DATA).path(JSON.VALUE).isNull());
            Assert.assertTrue(result.path(JSON.DATA).path(JsonSensor.SENSOR).isNull());
            Assert.assertTrue(result.path(JSON.DATA).path(JsonReporter.REPORTER).isNull());
            // set block state and value
            block1.setState(Block.OCCUPIED);
            block1.setValue("value is not empty");
            JUnitUtil.waitFor(() -> {
                return block1.getState() == Block.OCCUPIED;
            }, "Block to become occupied");
            result = service.doGet(JsonBlock.BLOCK, "IB1", service.getObjectMapper().createObjectNode(), locale);
            Assert.assertNotNull(result);
            this.validate(result);
            Assert.assertEquals(JSON.ACTIVE, result.path(JSON.DATA).path(JSON.STATE).asInt());
            Assert.assertEquals("value is not empty", result.path(JSON.DATA).path(JSON.VALUE).asText());
            // change block state
            block1.setState(Block.UNOCCUPIED);
            result = service.doGet(JsonBlock.BLOCK, "IB1", service.getObjectMapper().createObjectNode(), locale);
            Assert.assertNotNull(result);
            this.validate(result);
            Assert.assertEquals(JSON.INACTIVE, result.path(JSON.DATA).path(JSON.STATE).asInt());
            // add a sensor and reporter to the block
            block1.setSensor(sensor1.getSystemName());
            block1.setReporter(reporter1);
            result = service.doGet(JsonBlock.BLOCK, "IB1", service.getObjectMapper().createObjectNode(), locale);
            Assert.assertEquals(sensor1.getSystemName(), result.path(JSON.DATA).path(JsonSensor.SENSOR).asText());
            Assert.assertEquals(reporter1.getSystemName(), result.path(JSON.DATA).path(JsonReporter.REPORTER).asText());
        } catch (JsonException ex) {
            Assert.fail(ex.getMessage());
        }
        try {
            // add an invalid block by using a turnout name instead of a block name
            Assert.assertNull(manager.getBlock("IT1"));
            service.doGet(JsonBlock.BLOCK, "IT1", service.getObjectMapper().createObjectNode(), locale);
            Assert.fail("Expected exception not thrown.");
        } catch (JsonException ex) {
            Assert.assertEquals(404, ex.getCode());
        }
    }

    @Test
    public void testDoPost() throws JmriException, IOException {
        JsonBlockHttpService service = new JsonBlockHttpService(mapper);
        BlockManager manager = InstanceManager.getDefault(BlockManager.class);
        Block block1 = manager.provideBlock("IB1");
        try {
            // set off
            JsonNode message = mapper.createObjectNode().put(JSON.NAME, "IB1").put(JSON.STATE, JSON.INACTIVE);
            JsonNode result = service.doPost(JsonBlock.BLOCK, "IB1", message, locale);
            Assert.assertEquals(Block.UNOCCUPIED, block1.getState());
            Assert.assertNotNull(result);
            this.validate(result);
            Assert.assertEquals(JSON.INACTIVE, result.path(JSON.DATA).path(JSON.STATE).asInt());
            // set on
            message = mapper.createObjectNode().put(JSON.NAME, "IB1").put(JSON.STATE, JSON.ACTIVE);
            result = service.doPost(JsonBlock.BLOCK, "IB1", message, locale);
            Assert.assertEquals(Block.OCCUPIED, block1.getState());
            Assert.assertNotNull(result);
            this.validate(result);
            Assert.assertEquals(JSON.ACTIVE, result.path(JSON.DATA).path(JSON.STATE).asInt());
            // set unknown - remains on
            message = mapper.createObjectNode().put(JSON.NAME, "IB1").put(JSON.STATE, JSON.UNKNOWN);
            result = service.doPost(JsonBlock.BLOCK, "IB1", message, locale);
            Assert.assertEquals(Block.OCCUPIED, block1.getState());
            Assert.assertNotNull(result);
            this.validate(result);
            Assert.assertEquals(JSON.ACTIVE, result.path(JSON.DATA).path(JSON.STATE).asInt());
            // set invalid state
            message = mapper.createObjectNode().put(JSON.NAME, "IB1").put(JSON.STATE, 42); // Invalid value
            JsonException exception = null;
            try {
                service.doPost(JsonBlock.BLOCK, "IB1", message, locale);
            } catch (JsonException ex) {
                exception = ex;
            }
            Assert.assertEquals(Block.OCCUPIED, block1.getState());
            Assert.assertNotNull(exception);
            Assert.assertEquals(HttpServletResponse.SC_BAD_REQUEST, exception.getCode());
            // set value
            message = mapper.createObjectNode().put(JSON.NAME, "IB1").put(JSON.VALUE, "some value");
            Assert.assertNull("Null block value", block1.getValue());
            service.doPost(JsonBlock.BLOCK, "IB1", message, locale);
            Assert.assertEquals("Non-null block value", "some value", block1.getValue());
            // set null value
            message = mapper.createObjectNode().put(JSON.NAME, "IB1").putNull(JSON.VALUE);
            Assert.assertNotNull("Non-null block value", block1.getValue());
            service.doPost(JsonBlock.BLOCK, "IB1", message, locale);
            Assert.assertNull("Null block value", block1.getValue());
            // set non-existing sensor
            message = mapper.createObjectNode().put(JSON.NAME, "IB1").put(JsonSensor.SENSOR, "IS1");
            Assert.assertNull("No sensor", block1.getSensor());
            try {
                service.doPost(JsonBlock.BLOCK, "IB1", message, locale);
                Assert.fail("Expected exception not thrown");
            } catch (JsonException ex) {
                Assert.assertEquals("404 Not Found", 404, ex.getCode());
            }
            // set existing sensor
            Sensor sensor1 = InstanceManager.getDefault(SensorManager.class).provide("IS1");
            service.doPost(JsonBlock.BLOCK, "IB1", message, locale);
            Assert.assertEquals("Block has sensor", sensor1, block1.getSensor());
            // set null sensor
            message = mapper.createObjectNode().put(JSON.NAME, "IB1").putNull(JsonSensor.SENSOR);
            service.doPost(JsonBlock.BLOCK, "IB1", message, locale);
            Assert.assertNull("Block has no sensor", block1.getSensor());
            // set non-existing reporter
            message = mapper.createObjectNode().put(JSON.NAME, "IB1").put(JsonReporter.REPORTER, "IR1");
            Assert.assertNull("No reporter", block1.getReporter());
            try {
                service.doPost(JsonBlock.BLOCK, "IB1", message, locale);
                Assert.fail("Expected exception not thrown");
            } catch (JsonException ex) {
                Assert.assertEquals("404 Not Found", 404, ex.getCode());
            }
            // set existing reporter
            Reporter reporter1 = InstanceManager.getDefault(ReporterManager.class).provide("IR1");
            service.doPost(JsonBlock.BLOCK, "IB1", message, locale);
            Assert.assertEquals("Block has reporter", reporter1, block1.getReporter());
            // set null reporter
            message = mapper.createObjectNode().put(JSON.NAME, "IB1").putNull(JsonReporter.REPORTER);
            service.doPost(JsonBlock.BLOCK, "IB1", message, locale);
            Assert.assertNull("No reporter", block1.getReporter());
        } catch (JsonException ex) {
            Assert.fail(ex.getMessage());
        }
        try {
            // add an invalid block by using a turnout name instead of a block name
            Assert.assertNull(manager.getBlock("IT1"));
            JsonNode message = mapper.createObjectNode().put(JSON.NAME, "II1").put(JSON.STATE, Block.UNOCCUPIED);
            service.doPost(JsonBlock.BLOCK, "IT1", message, locale);
            Assert.fail("Expected exception not thrown.");
        } catch (JsonException ex) {
            Assert.assertEquals(404, ex.getCode());
        }
    }

    @SuppressWarnings("null")
    @Test
    public void testDoPut() throws IOException {
        JsonBlockHttpService service = new JsonBlockHttpService(mapper);
        BlockManager manager = InstanceManager.getDefault(BlockManager.class);
        try {
            // add a block
            Assert.assertNull(manager.getBlock("IB1"));
            JsonNode message = mapper.createObjectNode().put(JSON.NAME, "IB1").put(JSON.STATE, Block.UNOCCUPIED);
            JsonNode result = service.doPut(JsonBlock.BLOCK, "IB1", message, locale);
            Assert.assertNotNull(result);
            this.validate(result);
            Assert.assertNotNull(manager.getBlock("IB1"));
        } catch (JsonException ex) {
            Assert.fail(ex.getMessage());
        }
        try {
            // add an invalid block by using a turnout name instead of a block name
            Assert.assertNull(manager.getBlock("IT1"));
            JsonNode message = mapper.createObjectNode().put(JSON.NAME, "II1").put(JSON.STATE, Block.UNOCCUPIED);
            service.doPut(JsonBlock.BLOCK, null, message, locale); // use null for @Nonnull parameter to force failure
            Assert.fail("Expected exception not thrown.");
        } catch (JsonException ex) {
            Assert.assertEquals(500, ex.getCode());
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
        JsonNode result = instance.doGetList(JsonBlock.BLOCK, mapper.createObjectNode(), Locale.ITALY);
        Assert.assertEquals(1, result.size());
        this.validate(result);
    }

    /**
     * Test of doSchema method, of class JsonBlockHttpService.
     *
     * @throws jmri.server.json.JsonException if something goes wrong
     */
    @Test
    public void testDoSchema() throws JsonException {
        JsonBlockHttpService instance = new JsonBlockHttpService(mapper);
        JsonNode block = instance.doSchema(JsonBlock.BLOCK, false, Locale.ITALY);
        JsonNode blocks = instance.doSchema(JsonBlock.BLOCK, false, Locale.ITALY);
        Assert.assertNotNull("Has client schema for block", block);
        Assert.assertNotNull("Has client schema for blocks", blocks);
        Assert.assertEquals("Client schema for block and blocks is the same", block, blocks);
        block = instance.doSchema(JsonBlock.BLOCK, true, Locale.ITALY);
        blocks = instance.doSchema(JsonBlock.BLOCK, true, Locale.ITALY);
        Assert.assertNotNull("Has server schema for block", block);
        Assert.assertNotNull("Has server schema for blocks", blocks);
        Assert.assertEquals("Server schema for block and blocks is the same", block, blocks);
    }

}
