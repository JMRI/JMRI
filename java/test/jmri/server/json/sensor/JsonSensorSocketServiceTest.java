package jmri.server.json.sensor;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Locale;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.server.json.JSON;
import jmri.server.json.JsonException;
import jmri.server.json.JsonMockConnection;
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
public class JsonSensorSocketServiceTest {

    private Locale locale = Locale.ENGLISH;

    @Test
    public void testSensorChange() {
        try {
            JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
            JsonNode message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "IS1");
            JsonSensorSocketService service = new JsonSensorSocketService(connection);
            SensorManager manager = InstanceManager.getDefault(SensorManager.class);
            Sensor sensor1 = manager.provideSensor("IS1");
            service.onMessage(JsonSensor.SENSOR, message, JSON.POST, locale, 42);
            // TODO: test that service is listener in SensorManager
            message = connection.getMessage();
            Assert.assertNotNull("Message is not null", message);
            Assert.assertEquals(JSON.UNKNOWN, message.path(JSON.DATA).path(JSON.STATE).asInt(-1)); // -1 not possible value
            sensor1.setKnownState(Sensor.ACTIVE);
            JUnitUtil.waitFor(() -> {
                return sensor1.getKnownState() == Sensor.ACTIVE;
            }, "Sensor ACTIVE");
            message = connection.getMessage();
            Assert.assertNotNull("Message is not null", message);
            Assert.assertEquals(JSON.ACTIVE, message.path(JSON.DATA).path(JSON.STATE).asInt(-1));
            sensor1.setKnownState(Sensor.INACTIVE);
            JUnitUtil.waitFor(() -> {
                return sensor1.getKnownState() == Sensor.INACTIVE;
            }, "Sensor INACTIVE");
            message = connection.getMessage();
            Assert.assertNotNull("Message is not null", message);
            Assert.assertEquals(Sensor.INACTIVE, sensor1.getKnownState());
            Assert.assertEquals(JSON.INACTIVE, message.path(JSON.DATA).path(JSON.STATE).asInt(-1));
            service.onClose();
            // TODO: test that service is no longer a listener in SensorManager
        } catch (IOException | JmriException | JsonException ex) {
            Assert.fail(ex.getMessage());
        }
    }

    @Test
    public void testOnMessageChange() {
        try {
            JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
            JsonNode message;
            JsonSensorSocketService service = new JsonSensorSocketService(connection);
            SensorManager manager = InstanceManager.getDefault(SensorManager.class);
            Sensor sensor1 = manager.provideSensor("IS1");
            // Sensor INACTIVE
            message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "IS1").put(JSON.STATE, JSON.INACTIVE);
            service.onMessage(JsonSensor.SENSOR, message, JSON.POST, locale, 42);
            Assert.assertEquals(Sensor.INACTIVE, sensor1.getKnownState());
            // Sensor ACTIVE
            message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "IS1").put(JSON.STATE, JSON.ACTIVE);
            service.onMessage(JsonSensor.SENSOR, message, JSON.POST, locale, 42);
            Assert.assertEquals(Sensor.ACTIVE, sensor1.getKnownState());
            // Sensor UNKNOWN - remains ACTIVE
            message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "IS1").put(JSON.STATE, JSON.UNKNOWN);
            service.onMessage(JsonSensor.SENSOR, message, JSON.POST, locale, 42);
            Assert.assertEquals(Sensor.ACTIVE, sensor1.getKnownState());
            sensor1.setKnownState(Sensor.ACTIVE);
            // Sensor INCONSISTENT - remains ACTIVE
            message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "IS1").put(JSON.STATE, JSON.INCONSISTENT);
            service.onMessage(JsonSensor.SENSOR, message, JSON.POST, locale, 42);
            Assert.assertEquals(Sensor.ACTIVE, sensor1.getKnownState());
            sensor1.setKnownState(Sensor.ACTIVE);
            // Sensor no value
            message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "IS1");
            JsonException exception = null;
            try {
                service.onMessage(JsonSensor.SENSOR, message, JSON.POST, locale, 42);
            } catch (JsonException ex) {
                exception = ex;
            }
            Assert.assertEquals(Sensor.ACTIVE, sensor1.getKnownState());
            Assert.assertNull(exception);
        } catch (IOException | JmriException | JsonException ex) {
            Assert.fail(ex.getMessage());
        }
    }

    @Before
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initInternalSensorManager();
    }

    @After
    public void tearDown() throws Exception {
        JUnitUtil.tearDown();
    }

}
