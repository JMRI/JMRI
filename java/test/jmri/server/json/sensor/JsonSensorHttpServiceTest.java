package jmri.server.json.sensor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.servlet.http.HttpServletResponse;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.implementation.AbstractSensor;
import jmri.server.json.JSON;
import jmri.server.json.JsonException;
import jmri.server.json.JsonNamedBeanHttpServiceTestBase;
import jmri.server.json.JsonRequest;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 * @author Paul Bender
 * @author Randall Wood
 */
public class JsonSensorHttpServiceTest extends JsonNamedBeanHttpServiceTestBase<Sensor, JsonSensorHttpService> {

    @Test
    @Override
    public void testDoGet() throws JmriException, JsonException {
        SensorManager manager = InstanceManager.getDefault(SensorManager.class);
        Sensor sensor1 = manager.provideSensor("IS1"); // no value
        JsonNode result;
        result = service.doGet(JsonSensor.SENSOR, "IS1", NullNode.getInstance(), new JsonRequest(locale, JSON.V5, JSON.GET, 42));
        validate(result);
        assertEquals(JsonSensor.SENSOR, result.path(JSON.TYPE).asText());
        assertEquals("IS1", result.path(JSON.DATA).path(JSON.NAME).asText());
        assertEquals(JSON.UNKNOWN, result.path(JSON.DATA).path(JSON.STATE).asInt(-1)); // -1 is not a possible value
        sensor1.setKnownState(Sensor.ACTIVE);
        result = service.doGet(JsonSensor.SENSOR, "IS1", NullNode.getInstance(), new JsonRequest(locale, JSON.V5, JSON.GET, 42));
        validate(result);
        assertEquals(JSON.ACTIVE, result.path(JSON.DATA).path(JSON.STATE).asInt(-1));
        sensor1.setKnownState(Sensor.INACTIVE);
        result = service.doGet(JsonSensor.SENSOR, "IS1", NullNode.getInstance(), new JsonRequest(locale, JSON.V5, JSON.GET, 42));
        validate(result);
        assertEquals(JSON.INACTIVE, result.path(JSON.DATA).path(JSON.STATE).asInt(-1));
        sensor1.setKnownState(Sensor.INCONSISTENT);
        result = service.doGet(JsonSensor.SENSOR, "IS1", NullNode.getInstance(), new JsonRequest(locale, JSON.V5, JSON.GET, 42));
        validate(result);
        assertEquals(JSON.INCONSISTENT, result.path(JSON.DATA).path(JSON.STATE).asInt(-1));
        sensor1.setKnownState(Sensor.UNKNOWN);
        result = service.doGet(JsonSensor.SENSOR, "IS1", NullNode.getInstance(), new JsonRequest(locale, JSON.V5, JSON.GET, 42));
        validate(result);
        assertEquals(JSON.UNKNOWN, result.path(JSON.DATA).path(JSON.STATE).asInt(-1));
        // test an unexpected state
        Sensor sensor2 = new ErrorSensor("IS2");
        JsonException ex = assertThrows( JsonException.class, () ->
            service.doGet(sensor2, "IS2", JsonSensor.SENSOR, new JsonRequest(locale, JSON.V5, JSON.GET, 42)),
            "Expected exception not thrown");
        assertEquals( 500, ex.getCode(), "HTTP error code");
        assertEquals( "Internal sensor handling error. See JMRI logs for information.",
            ex.getMessage(), "Internal error message");
    }

    @Test
    public void testDoPost() throws JmriException, JsonException {
        SensorManager manager = InstanceManager.getDefault(SensorManager.class);
        Sensor sensor1 = manager.provideSensor("IS1");
        JsonNode result;
        JsonNode message;
        // set ACTIVE
        message = mapper.createObjectNode().put(JSON.NAME, "IS1").put(JSON.STATE, JSON.ACTIVE);
        result = service.doPost(JsonSensor.SENSOR, "IS1", message, new JsonRequest(locale, JSON.V5, JSON.GET, 42));
        assertEquals(Sensor.ACTIVE, sensor1.getKnownState());
        validate(result);
        assertEquals(JSON.ACTIVE, result.path(JSON.DATA).path(JSON.STATE).asInt(-1)); // -1 is not a possible value
        // set INACTIVE
        message = mapper.createObjectNode().put(JSON.NAME, "IS1").put(JSON.STATE, JSON.INACTIVE);
        result = service.doPost(JsonSensor.SENSOR, "IS1", message, new JsonRequest(locale, JSON.V5, JSON.GET, 42));
        assertEquals(Sensor.INACTIVE, sensor1.getKnownState());
        validate(result);
        assertEquals(JSON.INACTIVE, result.path(JSON.DATA).path(JSON.STATE).asInt(-1));
        // set UNKNOWN
        message = mapper.createObjectNode().put(JSON.NAME, "IS1").put(JSON.STATE, JSON.UNKNOWN);
        result = service.doPost(JsonSensor.SENSOR, "IS1", message, new JsonRequest(locale, JSON.V5, JSON.GET, 42));
        assertEquals(Sensor.INACTIVE, sensor1.getKnownState());
        assertEquals(JSON.INACTIVE, result.path(JSON.DATA).path(JSON.STATE).asInt(-1));
        // set INCONSISTENT
        message = mapper.createObjectNode().put(JSON.NAME, "IS1").put(JSON.STATE, JSON.INCONSISTENT);
        result = service.doPost(JsonSensor.SENSOR, "IS1", message, new JsonRequest(locale, JSON.V5, JSON.GET, 42));
        assertEquals(Sensor.INACTIVE, sensor1.getKnownState());
        assertEquals(JSON.INACTIVE, result.path(JSON.DATA).path(JSON.STATE).asInt(-1));
        // set inverted - becomes active
        assertFalse(sensor1.getInverted());
        message = mapper.createObjectNode().put(JSON.NAME, "IS1").put(JSON.INVERTED, true);
        result = service.doPost(JsonSensor.SENSOR, "IS1", message, new JsonRequest(locale, JSON.V5, JSON.GET, 42));
        assertTrue( sensor1.getInverted(), "Sensor is inverted");
        assertEquals(JSON.ACTIVE, result.path(JSON.DATA).path(JSON.STATE).asInt());
        assertEquals(true, result.path(JSON.DATA).path(JSON.INVERTED).asBoolean());
        // reset inverted - becomes inactive
        message = mapper.createObjectNode().put(JSON.NAME, "IS1").put(JSON.INVERTED, false);
        result = service.doPost(JsonSensor.SENSOR, "IS1", message, new JsonRequest(locale, JSON.V5, JSON.GET, 42));
        assertFalse( sensor1.getInverted(), "Sensor is not inverted");
        assertEquals(JSON.INACTIVE, result.path(JSON.DATA).path(JSON.STATE).asInt());
        // set invalid state
        JsonNode messageEx = mapper.createObjectNode().put(JSON.NAME, "IS1").put(JSON.STATE, 42); // Invalid value
        JsonException ex = assertThrows( JsonException.class, () ->
            service.doPost(JsonSensor.SENSOR, "IS1", messageEx,
                new JsonRequest(locale, JSON.V5, JSON.GET, 42)),
            "Expected exception not thrown");
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, ex.getCode());
        assertEquals(Sensor.INACTIVE, sensor1.getState());
        // test catching error thrown by Sensor while setting state
        Sensor sensor2 = new ErrorSensor("IS2");
        manager.register(sensor2);
        JsonNode messageEx2 = mapper.createObjectNode().put(JSON.NAME, "IS2").put(JSON.STATE, JSON.ACTIVE);
        ex = assertThrows( JsonException.class, () ->
            service.doPost(JsonSensor.SENSOR, "IS2", messageEx2,
                new JsonRequest(locale, JSON.V5, JSON.GET, 42)),
            "Expected exception not thrown");
        assertEquals( 500, ex.getCode(), "HTTP error code");
        assertEquals( "jmri.JmriException: " + ErrorSensor.MESSAGE, ex.getMessage(), "Internal error message");
    }

    @Test
    public void testDoPut() throws JsonException {
        SensorManager manager = InstanceManager.getDefault(SensorManager.class);
        JsonNode message;
        // add a sensor
        assertNull(manager.getSensor("IS1"));
        message = mapper.createObjectNode().put(JSON.NAME, "IS1").put(JSON.STATE, JSON.UNKNOWN);
        service.doPut(JsonSensor.SENSOR, "IS1", message, new JsonRequest(locale, JSON.V5, JSON.GET, 42));
        assertNotNull(manager.getSensor("IS1"));
    }

    @Test
    @Override
    public void testDoDelete() throws JsonException {
        SensorManager manager = InstanceManager.getDefault(SensorManager.class);
        ObjectNode message = mapper.createObjectNode();
        // delete non-existant bean
        Assumptions.assumeTrue( service != null, "protect against JUnit tests in Eclipse that test this class directly");

        JsonException ex = assertThrows( JsonException.class, () ->
            service.doDelete(service.getType(), "non-existant", message,
                new JsonRequest(locale, JSON.V5, JSON.GET, 42)),
            "Expected exception not thrown.");
        assertEquals( 404, ex.getCode(), "Code is HTTP NOT FOUND");
        assertEquals( "Object type sensor named \"non-existant\" not found.", ex.getMessage(), "Message");
        assertEquals( 42, ex.getId(), "ID is 42");

        manager.newSensor("IS1", null);
        // delete existing bean (no named listener)
        assertNotNull(manager.getBySystemName("IS1"));
        service.doDelete(service.getType(), "IS1", message, new JsonRequest(locale, JSON.V5, JSON.GET, 42));
        assertNull(manager.getBySystemName("IS1"));
        Sensor sensor = manager.newSensor("IS1", null);
        assertNotNull(sensor);
        sensor.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                // do nothing
            }
        }, "IS1", "Test Listener");
        // delete existing bean (with named listener)
        ex = assertThrows( JsonException.class, () ->
            service.doDelete(service.getType(), "IS1", message,
                new JsonRequest(locale, JSON.V5, JSON.GET, 42)),
            "Expected exception not thrown.");
        assertEquals(409, ex.getCode());
        assertEquals(1, ex.getAdditionalData().path(JSON.CONFLICT).size());
        assertEquals("Test Listener", ex.getAdditionalData().path(JSON.CONFLICT).path(0).asText());
        JsonNode messageEx = message.put(JSON.FORCE_DELETE, ex.getAdditionalData().path(JSON.FORCE_DELETE).asText());
        assertNotNull(manager.getBySystemName("IS1"));
        // will throw if prior catch failed
        service.doDelete(service.getType(), "IS1", messageEx, new JsonRequest(locale, JSON.V5, JSON.GET, 0));
        assertNull(manager.getBySystemName("IS1"));
        ex = assertThrows( JsonException.class, () ->
            service.doDelete(service.getType(), "IS1", NullNode.getInstance(),
                new JsonRequest(locale, JSON.V5, JSON.GET, 0)),
            "deleting again should throw an exception.");
            assertEquals(404, ex.getCode());
    }

    @Test
    public void testDoGetList() throws JsonException {
        SensorManager manager = InstanceManager.getDefault(SensorManager.class);
        JsonNode result;
        result = service.doGetList(JsonSensor.SENSOR, mapper.createObjectNode(), new JsonRequest(locale, JSON.V5, JSON.GET, 0));
        validate(result);
        assertEquals(0, result.size());
        manager.provideSensor("IS1");
        manager.provideSensor("IS2");
        result = service.doGetList(JsonSensor.SENSOR, mapper.createObjectNode(), new JsonRequest(locale, JSON.V5, JSON.GET, 0));
        validate(result);
        assertEquals(2, result.size());
    }

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();
        service = new JsonSensorHttpService(mapper);
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initDebugThrottleManager();
    }

    @AfterEach
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    private static class ErrorSensor extends AbstractSensor {

        public static String MESSAGE = "Deliberately thrown error";
        
        ErrorSensor(String systemName) {
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
