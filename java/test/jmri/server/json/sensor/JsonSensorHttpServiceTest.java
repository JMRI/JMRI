package jmri.server.json.sensor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Locale;
import javax.servlet.http.HttpServletResponse;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.implementation.AbstractSensor;
import jmri.server.json.JSON;
import jmri.server.json.JsonException;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender
 * @author Randall Wood
 */
public class JsonSensorHttpServiceTest {

    @Test
    public void testDoGet() throws JmriException {
        JsonSensorHttpService service = new JsonSensorHttpService(new ObjectMapper());
        SensorManager manager = InstanceManager.getDefault(SensorManager.class);
        Sensor sensor1 = manager.provideSensor("IS1"); // no value
        JsonNode result;
        try {
            result = service.doGet(JsonSensor.SENSOR, "IS1", service.getObjectMapper().createObjectNode(), Locale.ENGLISH);
            Assert.assertNotNull(result);
            Assert.assertEquals(JsonSensor.SENSOR, result.path(JSON.TYPE).asText());
            Assert.assertEquals("IS1", result.path(JSON.DATA).path(JSON.NAME).asText());
            Assert.assertEquals(JSON.UNKNOWN, result.path(JSON.DATA).path(JSON.STATE).asInt(-1)); // -1 is not a possible value
            sensor1.setKnownState(Sensor.ACTIVE);
            result = service.doGet(JsonSensor.SENSOR, "IS1", service.getObjectMapper().createObjectNode(), Locale.ENGLISH);
            Assert.assertNotNull(result);
            Assert.assertEquals(JSON.ACTIVE, result.path(JSON.DATA).path(JSON.STATE).asInt(-1));
            sensor1.setKnownState(Sensor.INACTIVE);
            result = service.doGet(JsonSensor.SENSOR, "IS1", service.getObjectMapper().createObjectNode(), Locale.ENGLISH);
            Assert.assertNotNull(result);
            Assert.assertEquals(JSON.INACTIVE, result.path(JSON.DATA).path(JSON.STATE).asInt(-1));
            sensor1.setKnownState(Sensor.INCONSISTENT);
            result = service.doGet(JsonSensor.SENSOR, "IS1", service.getObjectMapper().createObjectNode(), Locale.ENGLISH);
            Assert.assertNotNull(result);
            Assert.assertEquals(JSON.INCONSISTENT, result.path(JSON.DATA).path(JSON.STATE).asInt(-1));
            sensor1.setKnownState(Sensor.UNKNOWN);
            result = service.doGet(JsonSensor.SENSOR, "IS1", service.getObjectMapper().createObjectNode(), Locale.ENGLISH);
            Assert.assertNotNull(result);
            Assert.assertEquals(JSON.UNKNOWN, result.path(JSON.DATA).path(JSON.STATE).asInt(-1));
        } catch (JsonException ex) {
            Assert.fail(ex.getMessage());
        }
        // test an unexpected state
        Sensor sensor2 = new ErrorSensor("IS2");
        try {
            service.doGet(sensor2, "IS2", JsonSensor.SENSOR, Locale.ENGLISH);
            Assert.fail("Expected exception not thrown");
        } catch (JsonException ex) {
            Assert.assertEquals("HTTP error code", 500, ex.getCode());
            Assert.assertEquals("Internal error message", "Internal sensor handling error. See JMRI logs for information.", ex.getMessage());
        }
    }

    @Test
    public void testDoPost() throws JmriException {
        ObjectMapper mapper = new ObjectMapper();
        JsonSensorHttpService service = new JsonSensorHttpService(mapper);
        SensorManager manager = InstanceManager.getDefault(SensorManager.class);
        Sensor sensor1 = manager.provideSensor("IS1");
        JsonNode result;
        JsonNode message;
        try {
            // set ACTIVE
            message = mapper.createObjectNode().put(JSON.NAME, "IS1").put(JSON.STATE, JSON.ACTIVE);
            result = service.doPost(JsonSensor.SENSOR, "IS1", message, Locale.ENGLISH);
            Assert.assertEquals(Sensor.ACTIVE, sensor1.getKnownState());
            Assert.assertNotNull(result);
            Assert.assertEquals(JSON.ACTIVE, result.path(JSON.DATA).path(JSON.STATE).asInt(-1)); // -1 is not a possible value
            // set INACTIVE
            message = mapper.createObjectNode().put(JSON.NAME, "IS1").put(JSON.STATE, JSON.INACTIVE);
            result = service.doPost(JsonSensor.SENSOR, "IS1", message, Locale.ENGLISH);
            Assert.assertEquals(Sensor.INACTIVE, sensor1.getKnownState());
            Assert.assertNotNull(result);
            Assert.assertEquals(JSON.INACTIVE, result.path(JSON.DATA).path(JSON.STATE).asInt(-1));
            // set UNKNOWN
            message = mapper.createObjectNode().put(JSON.NAME, "IS1").put(JSON.STATE, JSON.UNKNOWN);
            result = service.doPost(JsonSensor.SENSOR, "IS1", message, Locale.ENGLISH);
            Assert.assertEquals(Sensor.INACTIVE, sensor1.getKnownState());
            Assert.assertEquals(JSON.INACTIVE, result.path(JSON.DATA).path(JSON.STATE).asInt(-1));
            // set INCONSISTENT
            message = mapper.createObjectNode().put(JSON.NAME, "IS1").put(JSON.STATE, JSON.INCONSISTENT);
            result = service.doPost(JsonSensor.SENSOR, "IS1", message, Locale.ENGLISH);
            Assert.assertEquals(Sensor.INACTIVE, sensor1.getKnownState());
            Assert.assertEquals(JSON.INACTIVE, result.path(JSON.DATA).path(JSON.STATE).asInt(-1));
            // set inverted - becomes active
            Assert.assertFalse(sensor1.getInverted());
            message = mapper.createObjectNode().put(JSON.NAME, "IS1").put(JSON.INVERTED, true);
            result = service.doPost(JsonSensor.SENSOR, "IS1", message, Locale.ENGLISH);
            Assert.assertTrue("Sensor is inverted", sensor1.getInverted());
            Assert.assertEquals(JSON.ACTIVE, result.path(JSON.DATA).path(JSON.STATE).asInt());
            Assert.assertEquals(true, result.path(JSON.DATA).path(JSON.INVERTED).asBoolean());
            // reset inverted - becomes inactive
            message = mapper.createObjectNode().put(JSON.NAME, "IS1").put(JSON.INVERTED, false);
            result = service.doPost(JsonSensor.SENSOR, "IS1", message, Locale.ENGLISH);
            Assert.assertFalse("Sensor is not inverted", sensor1.getInverted());
            Assert.assertEquals(JSON.INACTIVE, result.path(JSON.DATA).path(JSON.STATE).asInt());
            // set invalid state
            message = mapper.createObjectNode().put(JSON.NAME, "IS1").put(JSON.STATE, 42); // Invalid value
            try {
                service.doPost(JsonSensor.SENSOR, "IS1", message, Locale.ENGLISH);
                Assert.fail("Expected exception not thrown");
            } catch (JsonException ex) {
                Assert.assertEquals(HttpServletResponse.SC_BAD_REQUEST, ex.getCode());
            }
            Assert.assertEquals(Sensor.INACTIVE, sensor1.getState());
        } catch (JsonException ex) {
            Assert.fail(ex.getMessage());
        }
        // test catching error thrown by Sensor while setting state
        Sensor sensor2 = new ErrorSensor("IS2");
        manager.register(sensor2);
        message = mapper.createObjectNode().put(JSON.NAME, "IS2").put(JSON.STATE, JSON.ACTIVE);
        try {
            service.doPost(JsonSensor.SENSOR, "IS2", message, Locale.ENGLISH);
            Assert.fail("Expected exception not thrown");
        } catch (JsonException ex) {
            Assert.assertEquals("HTTP error code", 500, ex.getCode());
            Assert.assertEquals("Internal error message", "jmri.JmriException: " + ErrorSensor.MESSAGE, ex.getMessage());
        }
    }

    @Test
    public void testDoPut() {
        ObjectMapper mapper = new ObjectMapper();
        JsonSensorHttpService service = new JsonSensorHttpService(mapper);
        SensorManager manager = InstanceManager.getDefault(SensorManager.class);
        JsonNode message;
        try {
            // add a sensor
            Assert.assertNull(manager.getSensor("IS1"));
            message = mapper.createObjectNode().put(JSON.NAME, "IS1").put(JSON.STATE, JSON.UNKNOWN);
            service.doPut(JsonSensor.SENSOR, "IS1", message, Locale.ENGLISH);
            Assert.assertNotNull(manager.getSensor("IS1"));
        } catch (JsonException ex) {
            Assert.fail(ex.getMessage());
        }
    }

    @Test
    public void testDoGetList() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonSensorHttpService service = new JsonSensorHttpService(mapper);
            SensorManager manager = InstanceManager.getDefault(SensorManager.class);
            JsonNode result;
            result = service.doGetList(JsonSensor.SENSOR, mapper.createObjectNode(), Locale.ENGLISH);
            Assert.assertNotNull(result);
            Assert.assertEquals(0, result.size());
            manager.provideSensor("IS1");
            manager.provideSensor("IS2");
            result = service.doGetList(JsonSensor.SENSOR, mapper.createObjectNode(), Locale.ENGLISH);
            Assert.assertNotNull(result);
            Assert.assertEquals(2, result.size());
        } catch (JsonException ex) {
            Assert.fail(ex.getMessage());
        }
    }

    @Test
    public void testDelete() {
        try {
            (new JsonSensorHttpService(new ObjectMapper())).doDelete(JsonSensor.SENSOR, "", Locale.ENGLISH);
        } catch (JsonException ex) {
            Assert.assertEquals(HttpServletResponse.SC_METHOD_NOT_ALLOWED, ex.getCode());
            return;
        }
        Assert.fail("Did not throw expected error.");
    }

    @Before
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initDebugThrottleManager();
    }

    @After
    public void tearDown() throws Exception {
        JUnitUtil.tearDown();
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
