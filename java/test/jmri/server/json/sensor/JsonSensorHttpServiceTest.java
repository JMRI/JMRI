package jmri.server.json.sensor;

import apps.tests.Log4JFixture;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Locale;
import javax.servlet.http.HttpServletResponse;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.server.json.JSON;
import jmri.server.json.JsonException;
import jmri.util.JUnitUtil;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 *
 * @author Paul Bender
 * @author Randall Wood
 */
public class JsonSensorHttpServiceTest extends TestCase {

    public void testCtorSuccess() {
        JsonSensorHttpService service = new JsonSensorHttpService(new ObjectMapper());
        Assert.assertNotNull(service);
    }

    public void testDoGet() throws JmriException {
        JsonSensorHttpService service = new JsonSensorHttpService(new ObjectMapper());
        SensorManager manager = InstanceManager.getDefault(SensorManager.class);
        Sensor sensor1 = manager.provideSensor("IS1"); // no value
        JsonNode result;
        try {
            result = service.doGet(JsonSensorServiceFactory.SENSOR, "IS1", Locale.ENGLISH);
            Assert.assertNotNull(result);
            Assert.assertEquals("IS1", result.path(JSON.DATA).path(JSON.NAME).asText());
            Assert.assertEquals(JSON.UNKNOWN, result.path(JSON.DATA).path(JSON.STATE).asInt(-1)); // -1 is not a possible value
            sensor1.setKnownState(Sensor.ACTIVE);
            result = service.doGet(JsonSensorServiceFactory.SENSOR, "IS1", Locale.ENGLISH);
            Assert.assertNotNull(result);
            Assert.assertEquals(JSON.ACTIVE, result.path(JSON.DATA).path(JSON.STATE).asInt(-1));
            sensor1.setKnownState(Sensor.INACTIVE);
            result = service.doGet(JsonSensorServiceFactory.SENSOR, "IS1", Locale.ENGLISH);
            Assert.assertNotNull(result);
            Assert.assertEquals(Sensor.INACTIVE, result.path(JSON.DATA).path(JSON.STATE).asInt(-1));
        } catch (JsonException ex) {
            Assert.fail(ex.getMessage());
        }
    }

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
            result = service.doPost(JsonSensorServiceFactory.SENSOR, "IS1", message, Locale.ENGLISH);
            Assert.assertEquals(Sensor.ACTIVE, sensor1.getKnownState());
            Assert.assertNotNull(result);
            Assert.assertEquals(JSON.ACTIVE, result.path(JSON.DATA).path(JSON.STATE).asInt(-1)); // -1 is not a possible value
            // set INACTIVE
            message = mapper.createObjectNode().put(JSON.NAME, "IS1").put(JSON.STATE, JSON.INACTIVE);
            result = service.doPost(JsonSensorServiceFactory.SENSOR, "IS1", message, Locale.ENGLISH);
            Assert.assertEquals(Sensor.INACTIVE, sensor1.getKnownState());
            Assert.assertNotNull(result);
            Assert.assertEquals(JSON.INACTIVE, result.path(JSON.DATA).path(JSON.STATE).asInt(-1));
            // set UNKNOWN
            message = mapper.createObjectNode().put(JSON.NAME, "IS1").put(JSON.STATE, JSON.UNKNOWN);
            result = service.doPost(JsonSensorServiceFactory.SENSOR, "IS1", message, Locale.ENGLISH);
            Assert.assertEquals(Sensor.INACTIVE, sensor1.getKnownState());
            Assert.assertEquals(JSON.INACTIVE, result.path(JSON.DATA).path(JSON.STATE).asInt(-1));
            // set INCONSISTENT
            message = mapper.createObjectNode().put(JSON.NAME, "IS1").put(JSON.STATE, JSON.INCONSISTENT);
            result = service.doPost(JsonSensorServiceFactory.SENSOR, "IS1", message, Locale.ENGLISH);
            Assert.assertEquals(Sensor.INACTIVE, sensor1.getKnownState());
            Assert.assertEquals(JSON.INACTIVE, result.path(JSON.DATA).path(JSON.STATE).asInt(-1));
        } catch (JsonException ex) {
            Assert.fail(ex.getMessage());
        }
    }

    public void testDoPut() {
        ObjectMapper mapper = new ObjectMapper();
        JsonSensorHttpService service = new JsonSensorHttpService(mapper);
        SensorManager manager = InstanceManager.getDefault(SensorManager.class);
        JsonNode result;
        JsonNode message;
        try {
            // add a sensor
            Assert.assertNull(manager.getSensor("IS1"));
            message = mapper.createObjectNode().put(JSON.NAME, "IS1").put(JSON.STATE, JSON.UNKNOWN);
            result = service.doPut(JsonSensorServiceFactory.SENSOR, "IS1", message, Locale.ENGLISH);
            Assert.assertNotNull(manager.getSensor("IS1"));
        } catch (JsonException ex) {
            Assert.fail(ex.getMessage());
        }
    }
    
    public void testDoGetList() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonSensorHttpService service = new JsonSensorHttpService(mapper);
            SensorManager manager = InstanceManager.getDefault(SensorManager.class);
            JsonNode result;
            result = service.doGetList(JsonSensorServiceFactory.SENSOR, Locale.ENGLISH);
            Assert.assertNotNull(result);
            Assert.assertEquals(0, result.size());
            Sensor sensor1 = manager.provideSensor("IS1");
            Sensor sensor2 = manager.provideSensor("IS2");
            result = service.doGetList(JsonSensorServiceFactory.SENSOR, Locale.ENGLISH);
            Assert.assertNotNull(result);
            Assert.assertEquals(2, result.size());
        } catch (JsonException ex) {
            Assert.fail(ex.getMessage());
        }
    }
    
    public void testDelete() {
        try {
            (new JsonSensorHttpService(new ObjectMapper())).doDelete(JsonSensorServiceFactory.SENSOR, null, Locale.ENGLISH);
        } catch (JsonException ex) {
            Assert.assertEquals(HttpServletResponse.SC_METHOD_NOT_ALLOWED, ex.getCode());
            return;
        }
        Assert.fail("Did not throw expected error.");
    }
    
    // from here down is testing infrastructure
    public JsonSensorHttpServiceTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {JsonSensorHttpServiceTest.class.getName()};
        TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(JsonSensorHttpServiceTest.class);

        return suite;
    }

    // The minimal setup for log4J
    @Override
    protected void setUp() throws Exception {
        Log4JFixture.setUp();
        super.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initDebugThrottleManager();
    }

    @Override
    protected void tearDown() throws Exception {
        JUnitUtil.resetInstanceManager();
        super.tearDown();
        Log4JFixture.tearDown();
    }

}
