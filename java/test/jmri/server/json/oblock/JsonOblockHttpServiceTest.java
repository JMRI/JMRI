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

import static org.junit.Assert.*;
import static org.junit.Assume.assumeNotNull;

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
        JUnitUtil.deregisterEditorManagerShutdownTask();
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
        try {
            // add an invalid oblock by using a turnout name instead of an oblock name
            assertNull(manager.getOBlock("IT1"));
            service.doGet(JsonOblock.OBLOCK, "IT1", NullNode.getInstance(), new JsonRequest(locale, JSON.V5, JSON.GET, 0));
            fail("Expected exception not thrown.");
        } catch (JsonException ex) {
            assertEquals(404, ex.getCode());
        }
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
        message = mapper.createObjectNode().put(JSON.NAME, "OB1").put(JsonOblock.STATUS, 111); // Invalid value
        try {
            service.doPost(JsonOblock.OBLOCK, "OB1", message, new JsonRequest(locale, JSON.V5, JSON.GET, 0));
            fail("Expected exception not thrown.");
        } catch (JsonException ex) {
            assertEquals(400, ex.getCode());
        }
        assertEquals(Block.OCCUPIED, oblock1.getState());
        // set value
        message = mapper.createObjectNode().put(JSON.NAME, "OB1").put(JSON.VALUE, "some value");
        assertNull("Null block value", oblock1.getValue());
        service.doPost(JsonOblock.OBLOCK, "OB1", message, new JsonRequest(locale, JSON.V5, JSON.GET, 0));
        assertEquals("Non-null block value", "some value", oblock1.getValue());
        // set null value
        message = mapper.createObjectNode().put(JSON.NAME, "OB1").putNull(JSON.VALUE);
        assertNotNull("Non-null block value", oblock1.getValue());
        service.doPost(JsonOblock.OBLOCK, "OB1", message, new JsonRequest(locale, JSON.V5, JSON.GET, 0));
        assertNull("Null block value", oblock1.getValue());
        // set non-existing sensor
        message = mapper.createObjectNode().put(JSON.NAME, "OB1").put(JsonSensor.SENSOR, "IS1");
        assertNull("No sensor", oblock1.getSensor());
        try {
            service.doPost(JsonOblock.OBLOCK, "OB1", message, new JsonRequest(locale, JSON.V5, JSON.GET, 0));
            fail("Expected exception not thrown");
        } catch (JsonException ex) {
            assertEquals("404 Not Found", 404, ex.getCode());
        }
        // set existing sensor
        Sensor sensor1 = InstanceManager.getDefault(SensorManager.class).provide("IS1");
        service.doPost(JsonOblock.OBLOCK, "OB1", message, new JsonRequest(locale, JSON.V5, JSON.GET, 0));
        assertEquals("Block has sensor", sensor1, oblock1.getSensor());
        // set null sensor
        message = mapper.createObjectNode().put(JSON.NAME, "OB1").putNull(JsonSensor.SENSOR);
        service.doPost(JsonOblock.OBLOCK, "OB1", message, new JsonRequest(locale, JSON.V5, JSON.GET, 0));
        assertNull("Oblock has no sensor", oblock1.getSensor());
        // reporters not copied from JsonBlockHttpServiceTest since they are not used in OBlocks
        try {
            // add an invalid oblock by using a turnout name instead of a oblock name
            assertNull(manager.getOBlock("IT1"));
            message = mapper.createObjectNode().put(JSON.NAME, "II1").put(JsonOblock.STATUS, Block.UNOCCUPIED);
            service.doPost(JsonOblock.OBLOCK, "IT1", message, new JsonRequest(locale, JSON.V5, JSON.GET, 0));
            fail("Expected exception not thrown.");
        } catch (JsonException ex) {
            assertEquals(404, ex.getCode());
        }
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
        try {
            // add an invalid Oblock by using a turnout name instead of a Oblock name
            assertNull(manager.getOBlock("IT1"));
            message = mapper.createObjectNode().put(JSON.NAME, "II1").put(JsonOblock.STATUS, Block.UNOCCUPIED);
            service.doPut(JsonOblock.OBLOCK, "", message, new JsonRequest(locale, JSON.V5, JSON.GET, 0)); // use an empty name to trigger exception
            fail("Expected exception not thrown.");
        } catch (JsonException ex) {
            assertEquals(400, ex.getCode());
        }
    }

    @Test
    @Override
    public void testDoDelete() throws JsonException {
        OBlockManager manager = InstanceManager.getDefault(OBlockManager.class);
        ObjectNode message = mapper.createObjectNode();
        // delete non-existent bean
        try {
            assumeNotNull(service); // protect against JUnit tests in Eclipse that test this class directly
            service.doDelete(service.getType(), "non-existant", message, new JsonRequest(locale, JSON.V5, JSON.GET, 42));
            fail("Expected exception not thrown.");
        } catch (JsonException ex) {
            assertEquals("Code is HTTP NOT FOUND", 404, ex.getCode());
            assertEquals("Message", "Object type oblock named \"non-existant\" not found.", ex.getMessage());
            assertEquals("ID is 42", 42, ex.getId());
        }
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
        try {
            service.doDelete(service.getType(), "OB1", message, new JsonRequest(locale, JSON.V5, JSON.GET, 42));
            fail("Expected exception not thrown.");
        } catch (JsonException ex) {
            assertEquals(409, ex.getCode());
            assertEquals(1, ex.getAdditionalData().path(JSON.CONFLICT).size());
            assertEquals("Test Listener", ex.getAdditionalData().path(JSON.CONFLICT).path(0).asText());
            message = message.put(JSON.FORCE_DELETE, ex.getAdditionalData().path(JSON.FORCE_DELETE).asText());
        }
        assertNotNull(manager.getBySystemName("OB1"));
        // will throw if prior catch failed
        service.doDelete(service.getType(), "OB1", message, new JsonRequest(locale, JSON.V5, JSON.GET, 0));
        assertNull(manager.getBySystemName("OB1"));
        try {
            // deleting again should throw an exception
            service.doDelete(service.getType(), "OB1", NullNode.getInstance(), new JsonRequest(locale, JSON.V5, JSON.GET, 0));
            fail("Expected exception not thrown.");
        } catch (JsonException ex) {
            assertEquals(404, ex.getCode());
        }
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
