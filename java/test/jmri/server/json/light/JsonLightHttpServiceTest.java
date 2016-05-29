package jmri.server.json.light;

import apps.tests.Log4JFixture;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Locale;
import javax.servlet.http.HttpServletResponse;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Light;
import jmri.LightManager;
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
public class JsonLightHttpServiceTest extends TestCase {

    public void testCtorSuccess() {
        JsonLightHttpService service = new JsonLightHttpService(new ObjectMapper());
        Assert.assertNotNull(service);
    }

    public void testDoGet() throws JmriException {
        JsonLightHttpService service = new JsonLightHttpService(new ObjectMapper());
        LightManager manager = InstanceManager.getDefault(LightManager.class);
        Light light1 = manager.provideLight("IL1");
        JsonNode result;
        try {
            result = service.doGet(JsonLightServiceFactory.LIGHT, "IL1", Locale.ENGLISH);
            Assert.assertNotNull(result);
            Assert.assertEquals("IL1", result.path(JSON.DATA).path(JSON.NAME).asText());
            Assert.assertEquals(JSON.OFF, result.path(JSON.DATA).path(JSON.STATE).asInt());
            light1.setState(Light.ON);
            result = service.doGet(JsonLightServiceFactory.LIGHT, "IL1", Locale.ENGLISH);
            Assert.assertNotNull(result);
            Assert.assertEquals(JSON.ON, result.path(JSON.DATA).path(JSON.STATE).asInt());
            light1.setState(Light.OFF);
            result = service.doGet(JsonLightServiceFactory.LIGHT, "IL1", Locale.ENGLISH);
            Assert.assertNotNull(result);
            Assert.assertEquals(JSON.OFF, result.path(JSON.DATA).path(JSON.STATE).asInt());
        } catch (JsonException ex) {
            Assert.fail(ex.getMessage());
        }
    }

    public void testDoPost() throws JmriException {
        ObjectMapper mapper = new ObjectMapper();
        JsonLightHttpService service = new JsonLightHttpService(mapper);
        LightManager manager = InstanceManager.getDefault(LightManager.class);
        Light light1 = manager.provideLight("IL1");
        JsonNode result;
        JsonNode message;
        try {
            // set off
            message = mapper.createObjectNode().put(JSON.NAME, "IL1").put(JSON.STATE, JSON.OFF);
            result = service.doPost(JsonLightServiceFactory.LIGHT, "IL1", message, Locale.ENGLISH);
            Assert.assertEquals(Light.OFF, light1.getState());
            Assert.assertNotNull(result);
            Assert.assertEquals(JSON.OFF, result.path(JSON.DATA).path(JSON.STATE).asInt());
            // set on
            message = mapper.createObjectNode().put(JSON.NAME, "IL1").put(JSON.STATE, JSON.ON);
            result = service.doPost(JsonLightServiceFactory.LIGHT, "IL1", message, Locale.ENGLISH);
            Assert.assertEquals(Light.ON, light1.getState());
            Assert.assertNotNull(result);
            Assert.assertEquals(JSON.ON, result.path(JSON.DATA).path(JSON.STATE).asInt());
            // set unknown - remains on
            message = mapper.createObjectNode().put(JSON.NAME, "IL1").put(JSON.STATE, JSON.UNKNOWN);
            result = service.doPost(JsonLightServiceFactory.LIGHT, "IL1", message, Locale.ENGLISH);
            Assert.assertEquals(Light.ON, light1.getState());
            Assert.assertEquals(JSON.ON, result.path(JSON.DATA).path(JSON.STATE).asInt());
            // set invalid state
            message = mapper.createObjectNode().put(JSON.NAME, "IL1").put(JSON.STATE, 42); // Invalid value
            JsonException exception = null;
            try {
                service.doPost(JsonLightServiceFactory.LIGHT, "IL1", message, Locale.ENGLISH);
            } catch (JsonException ex) {
                exception = ex;
            }
            Assert.assertEquals(Light.ON, light1.getState());
            Assert.assertNotNull(exception);
            Assert.assertEquals(HttpServletResponse.SC_BAD_REQUEST, exception.getCode());
        } catch (JsonException ex) {
            Assert.fail(ex.getMessage());
        }
    }

    public void testDoPut() {
        ObjectMapper mapper = new ObjectMapper();
        JsonLightHttpService service = new JsonLightHttpService(mapper);
        LightManager manager = InstanceManager.getDefault(LightManager.class);
        JsonNode result;
        JsonNode message;
        try {
            // add a light
            Assert.assertNull(manager.getLight("IL1"));
            message = mapper.createObjectNode().put(JSON.NAME, "IL1").put(JSON.STATE, Light.OFF);
            result = service.doPut(JsonLightServiceFactory.LIGHT, "IL1", message, Locale.ENGLISH);
            Assert.assertNotNull(manager.getLight("IL1"));
        } catch (JsonException ex) {
            Assert.fail(ex.getMessage());
        }
    }
    
    public void testDoGetList() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonLightHttpService service = new JsonLightHttpService(mapper);
            LightManager manager = InstanceManager.getDefault(LightManager.class);
            JsonNode result;
            result = service.doGetList(JsonLightServiceFactory.LIGHT, Locale.ENGLISH);
            Assert.assertNotNull(result);
            Assert.assertEquals(0, result.size());
            Light light1 = manager.provideLight("IL1");
            Light light2 = manager.provideLight("IL2");
            result = service.doGetList(JsonLightServiceFactory.LIGHT, Locale.ENGLISH);
            Assert.assertNotNull(result);
            Assert.assertEquals(2, result.size());
        } catch (JsonException ex) {
            Assert.fail(ex.getMessage());
        }
    }
    
    public void testDelete() {
        try {
            (new JsonLightHttpService(new ObjectMapper())).doDelete(JsonLightServiceFactory.LIGHT, null, Locale.ENGLISH);
        } catch (JsonException ex) {
            Assert.assertEquals(HttpServletResponse.SC_METHOD_NOT_ALLOWED, ex.getCode());
            return;
        }
        Assert.fail("Did not throw expected error.");
    }
    
    // from here down is testing infrastructure
    public JsonLightHttpServiceTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {JsonLightHttpServiceTest.class.getName()};
        TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(JsonLightHttpServiceTest.class);

        return suite;
    }

    // The minimal setup for log4J
    @Override
    protected void setUp() throws Exception {
        Log4JFixture.setUp();
        super.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initInternalLightManager();
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
