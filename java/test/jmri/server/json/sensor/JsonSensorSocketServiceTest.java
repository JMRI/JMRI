package jmri.server.json.sensor;

import apps.tests.Log4JFixture;
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
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.swingui.TestRunner;

/**
 *
 * @author Paul Bender
 * @author Randall Wood
 */
public class JsonSensorSocketServiceTest extends TestCase {

    public void testCtorSuccess() {
        JsonSensorSocketService service = new JsonSensorSocketService(new JsonMockConnection((DataOutputStream) null));
        Assert.assertNotNull(service);
    }

    public void testSensorChange() {
        try {
            JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
            JsonNode message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "IS1");
            JsonSensorSocketService service = new JsonSensorSocketService(connection);
            SensorManager manager = InstanceManager.getDefault(SensorManager.class);
            Sensor sensor1 = manager.provideSensor("IS1");
            service.onMessage(JsonSensorServiceFactory.SENSOR, message, Locale.ENGLISH);
            // TODO: test that service is listener in SensorManager
            Assert.assertEquals(JSON.UNKNOWN, connection.getMessage().path(JSON.DATA).path(JSON.STATE).asInt(-1)); // -1 not possible value
            sensor1.setKnownState(Sensor.ACTIVE);
            JUnitUtil.waitFor(() -> {
                return sensor1.getKnownState() == Sensor.ACTIVE;
            }, "Sensor ACTIVE");
            Assert.assertEquals(JSON.ACTIVE, connection.getMessage().path(JSON.DATA).path(JSON.STATE).asInt(-1));
            sensor1.setKnownState(Sensor.INACTIVE);
            JUnitUtil.waitFor(() -> {
                return sensor1.getKnownState() == Sensor.INACTIVE;
            }, "Sensor INACTIVE");
            Assert.assertEquals(Sensor.INACTIVE, sensor1.getKnownState());
            Assert.assertEquals(JSON.INACTIVE, connection.getMessage().path(JSON.DATA).path(JSON.STATE).asInt(-1));
            service.onClose();
            // TODO: test that service is no longer a listener in SensorManager
        } catch (IOException | JmriException | JsonException ex) {
            Assert.fail(ex.getMessage());
        }
    }

    public void testOnMessageChange() {
        try {
            JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
            JsonNode message;
            JsonSensorSocketService service = new JsonSensorSocketService(connection);
            SensorManager manager = InstanceManager.getDefault(SensorManager.class);
            Sensor sensor1 = manager.provideSensor("IS1");
            // Sensor INACTIVE
            message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "IS1").put(JSON.STATE, JSON.INACTIVE);
            service.onMessage(JsonSensorServiceFactory.SENSOR, message, Locale.ENGLISH);
            Assert.assertEquals(Sensor.INACTIVE, sensor1.getKnownState());
            // Sensor ACTIVE
            message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "IS1").put(JSON.STATE, JSON.ACTIVE);
            service.onMessage(JsonSensorServiceFactory.SENSOR, message, Locale.ENGLISH);
            Assert.assertEquals(Sensor.ACTIVE, sensor1.getKnownState());
            // Sensor UNKNOWN - remains ACTIVE
            message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "IS1").put(JSON.STATE, JSON.UNKNOWN);
            service.onMessage(JsonSensorServiceFactory.SENSOR, message, Locale.ENGLISH);
            Assert.assertEquals(Sensor.ACTIVE, sensor1.getKnownState());
            sensor1.setKnownState(Sensor.ACTIVE);
            // Sensor INCONSISTENT - remains ACTIVE
            message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "IS1").put(JSON.STATE, JSON.INCONSISTENT);
            service.onMessage(JsonSensorServiceFactory.SENSOR, message, Locale.ENGLISH);
            Assert.assertEquals(Sensor.ACTIVE, sensor1.getKnownState());
            sensor1.setKnownState(Sensor.ACTIVE);
            // Sensor no value
            message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "IS1");
            JsonException exception = null;
            try {
                service.onMessage(JsonSensorServiceFactory.SENSOR, message, Locale.ENGLISH);
            } catch (JsonException ex) {
                exception = ex;
            }
            Assert.assertEquals(Sensor.ACTIVE, sensor1.getKnownState());
            Assert.assertNull(exception);
        } catch (IOException | JmriException | JsonException ex) {
            Assert.fail(ex.getMessage());
        }
    }

    // from here down is testing infrastructure
    public JsonSensorSocketServiceTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {JsonSensorSocketServiceTest.class.getName()};
        TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(JsonSensorSocketServiceTest.class);

        return suite;
    }

    // The minimal setup for log4J
    @Override
    protected void setUp() throws Exception {
        Log4JFixture.setUp();
        super.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalSensorManager();
    }

    @Override
    protected void tearDown() throws Exception {
        JUnitUtil.resetInstanceManager();
        super.tearDown();
        Log4JFixture.tearDown();
    }

}
