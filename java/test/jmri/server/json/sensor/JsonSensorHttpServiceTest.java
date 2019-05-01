package jmri.server.json.sensor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import javax.servlet.http.HttpServletResponse;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.implementation.AbstractSensor;
import jmri.server.json.JSON;
import jmri.server.json.JsonException;
import jmri.server.json.JsonNamedBeanHttpServiceTestBase;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender
 * @author Randall Wood
 */
public class JsonSensorHttpServiceTest extends JsonNamedBeanHttpServiceTestBase<Sensor, JsonSensorHttpService> {

    @Test
    public void testDoGet() throws JmriException, JsonException {
        SensorManager manager = InstanceManager.getDefault(SensorManager.class);
        Sensor sensor1 = manager.provideSensor("IS1"); // no value
        JsonNode result;
        result = service.doGet(JsonSensor.SENSOR, "IS1", NullNode.getInstance(), locale, 42);
        validate(result);
        assertEquals(JsonSensor.SENSOR, result.path(JSON.TYPE).asText());
        assertEquals("IS1", result.path(JSON.DATA).path(JSON.NAME).asText());
        assertEquals(JSON.UNKNOWN, result.path(JSON.DATA).path(JSON.STATE).asInt(-1)); // -1 is not a possible value
        sensor1.setKnownState(Sensor.ACTIVE);
        result = service.doGet(JsonSensor.SENSOR, "IS1", NullNode.getInstance(), locale, 42);
        validate(result);
        assertEquals(JSON.ACTIVE, result.path(JSON.DATA).path(JSON.STATE).asInt(-1));
        sensor1.setKnownState(Sensor.INACTIVE);
        result = service.doGet(JsonSensor.SENSOR, "IS1", NullNode.getInstance(), locale, 42);
        validate(result);
        assertEquals(JSON.INACTIVE, result.path(JSON.DATA).path(JSON.STATE).asInt(-1));
        sensor1.setKnownState(Sensor.INCONSISTENT);
        result = service.doGet(JsonSensor.SENSOR, "IS1", NullNode.getInstance(), locale, 42);
        validate(result);
        assertEquals(JSON.INCONSISTENT, result.path(JSON.DATA).path(JSON.STATE).asInt(-1));
        sensor1.setKnownState(Sensor.UNKNOWN);
        result = service.doGet(JsonSensor.SENSOR, "IS1", NullNode.getInstance(), locale, 42);
        validate(result);
        assertEquals(JSON.UNKNOWN, result.path(JSON.DATA).path(JSON.STATE).asInt(-1));
        // test an unexpected state
        Sensor sensor2 = new ErrorSensor("IS2");
        try {
            service.doGet(sensor2, "IS2", JsonSensor.SENSOR, locale, 42);
            fail("Expected exception not thrown");
        } catch (JsonException ex) {
            assertEquals("HTTP error code", 500, ex.getCode());
            assertEquals("Internal error message", "Internal sensor handling error. See JMRI logs for information.", ex.getMessage());
        }
    }

    @Test
    public void testDoPost() throws JmriException, JsonException {
        SensorManager manager = InstanceManager.getDefault(SensorManager.class);
        Sensor sensor1 = manager.provideSensor("IS1");
        JsonNode result;
        JsonNode message;
        // set ACTIVE
        message = mapper.createObjectNode().put(JSON.NAME, "IS1").put(JSON.STATE, JSON.ACTIVE);
        result = service.doPost(JsonSensor.SENSOR, "IS1", message, locale, 42);
        assertEquals(Sensor.ACTIVE, sensor1.getKnownState());
        validate(result);
        assertEquals(JSON.ACTIVE, result.path(JSON.DATA).path(JSON.STATE).asInt(-1)); // -1 is not a possible value
        // set INACTIVE
        message = mapper.createObjectNode().put(JSON.NAME, "IS1").put(JSON.STATE, JSON.INACTIVE);
        result = service.doPost(JsonSensor.SENSOR, "IS1", message, locale, 42);
        assertEquals(Sensor.INACTIVE, sensor1.getKnownState());
        validate(result);
        assertEquals(JSON.INACTIVE, result.path(JSON.DATA).path(JSON.STATE).asInt(-1));
        // set UNKNOWN
        message = mapper.createObjectNode().put(JSON.NAME, "IS1").put(JSON.STATE, JSON.UNKNOWN);
        result = service.doPost(JsonSensor.SENSOR, "IS1", message, locale, 42);
        assertEquals(Sensor.INACTIVE, sensor1.getKnownState());
        assertEquals(JSON.INACTIVE, result.path(JSON.DATA).path(JSON.STATE).asInt(-1));
        // set INCONSISTENT
        message = mapper.createObjectNode().put(JSON.NAME, "IS1").put(JSON.STATE, JSON.INCONSISTENT);
        result = service.doPost(JsonSensor.SENSOR, "IS1", message, locale, 42);
        assertEquals(Sensor.INACTIVE, sensor1.getKnownState());
        assertEquals(JSON.INACTIVE, result.path(JSON.DATA).path(JSON.STATE).asInt(-1));
        // set inverted - becomes active
        assertFalse(sensor1.getInverted());
        message = mapper.createObjectNode().put(JSON.NAME, "IS1").put(JSON.INVERTED, true);
        result = service.doPost(JsonSensor.SENSOR, "IS1", message, locale, 42);
        assertTrue("Sensor is inverted", sensor1.getInverted());
        assertEquals(JSON.ACTIVE, result.path(JSON.DATA).path(JSON.STATE).asInt());
        assertEquals(true, result.path(JSON.DATA).path(JSON.INVERTED).asBoolean());
        // reset inverted - becomes inactive
        message = mapper.createObjectNode().put(JSON.NAME, "IS1").put(JSON.INVERTED, false);
        result = service.doPost(JsonSensor.SENSOR, "IS1", message, locale, 42);
        assertFalse("Sensor is not inverted", sensor1.getInverted());
        assertEquals(JSON.INACTIVE, result.path(JSON.DATA).path(JSON.STATE).asInt());
        // set invalid state
        message = mapper.createObjectNode().put(JSON.NAME, "IS1").put(JSON.STATE, 42); // Invalid value
        try {
            service.doPost(JsonSensor.SENSOR, "IS1", message, locale, 42);
            fail("Expected exception not thrown");
        } catch (JsonException ex) {
            assertEquals(HttpServletResponse.SC_BAD_REQUEST, ex.getCode());
        }
        assertEquals(Sensor.INACTIVE, sensor1.getState());
        // test catching error thrown by Sensor while setting state
        Sensor sensor2 = new ErrorSensor("IS2");
        manager.register(sensor2);
        message = mapper.createObjectNode().put(JSON.NAME, "IS2").put(JSON.STATE, JSON.ACTIVE);
        try {
            service.doPost(JsonSensor.SENSOR, "IS2", message, locale, 42);
            fail("Expected exception not thrown");
        } catch (JsonException ex) {
            assertEquals("HTTP error code", 500, ex.getCode());
            assertEquals("Internal error message", "jmri.JmriException: " + ErrorSensor.MESSAGE, ex.getMessage());
        }
    }

    @Test
    public void testDoPut() throws JsonException {
        SensorManager manager = InstanceManager.getDefault(SensorManager.class);
        JsonNode message;
        // add a sensor
        assertNull(manager.getSensor("IS1"));
        message = mapper.createObjectNode().put(JSON.NAME, "IS1").put(JSON.STATE, JSON.UNKNOWN);
        service.doPut(JsonSensor.SENSOR, "IS1", message, locale, 42);
        assertNotNull(manager.getSensor("IS1"));
    }

    @Test
    public void testDoGetList() throws JsonException {
        SensorManager manager = InstanceManager.getDefault(SensorManager.class);
        JsonNode result;
        result = service.doGetList(JsonSensor.SENSOR, mapper.createObjectNode(), locale, 0);
        validate(result);
        assertEquals(0, result.size());
        manager.provideSensor("IS1");
        manager.provideSensor("IS2");
        result = service.doGetList(JsonSensor.SENSOR, mapper.createObjectNode(), locale, 0);
        validate(result);
        assertEquals(2, result.size());
    }

    @Test
    public void testDelete() {
        try {
            service.doDelete(JsonSensor.SENSOR, "", NullNode.getInstance(), locale, 42);
        } catch (JsonException ex) {
            assertEquals(HttpServletResponse.SC_METHOD_NOT_ALLOWED, ex.getCode());
            return;
        }
        fail("Did not throw expected error.");
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        service = new JsonSensorHttpService(mapper);
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initDebugThrottleManager();
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    private static class ErrorSensor extends AbstractSensor {

        public static String MESSAGE = "Deliberately thrown error";
        
        public ErrorSensor(String systemName) {
            super(systemName);
        }

        @Override
        public void requestUpdateFromLayout() {
            // do nothing
        }
        
        @Override
        public int getKnownState() {
            return -1; // return an expected to be invalid value
        }

        @Override
        public void setKnownState(int state) throws JmriException {
            throw new JmriException(MESSAGE);
        }
    }
}
