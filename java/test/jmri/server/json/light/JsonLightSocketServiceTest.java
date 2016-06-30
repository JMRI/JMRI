package jmri.server.json.light;

import apps.tests.Log4JFixture;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Locale;
import javax.servlet.http.HttpServletResponse;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Light;
import jmri.LightManager;
import jmri.server.json.JSON;
import jmri.server.json.JsonException;
import jmri.server.json.JsonMockConnection;
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
public class JsonLightSocketServiceTest extends TestCase {

    public void testCtorSuccess() {
        JsonLightSocketService service = new JsonLightSocketService(new JsonMockConnection((DataOutputStream) null));
        Assert.assertNotNull(service);
    }

    public void testLightChange() {
        try {
            JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
            JsonNode message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "IL1");
            JsonLightSocketService service = new JsonLightSocketService(connection);
            LightManager manager = InstanceManager.getDefault(LightManager.class);
            Light light1 = manager.provideLight("IL1");
            service.onMessage(JsonLightServiceFactory.LIGHT, message, Locale.ENGLISH);
            // TODO: test that service is listener in LightManager
            Assert.assertEquals(JSON.OFF, connection.getMessage().path(JSON.DATA).path(JSON.STATE).asInt());
            light1.setState(Light.ON);
            JUnitUtil.waitFor(() -> {
                return light1.getState() == Light.ON;
            }, "Light to throw");
            Assert.assertEquals(JSON.ON, connection.getMessage().path(JSON.DATA).path(JSON.STATE).asInt());
            light1.setState(Light.OFF);
            JUnitUtil.waitFor(() -> {
                return light1.getState() == Light.OFF;
            }, "Light to close");
            Assert.assertEquals(Light.OFF, light1.getState());
            Assert.assertEquals(JSON.OFF, connection.getMessage().path(JSON.DATA).path(JSON.STATE).asInt());
            service.onClose();
            // TODO: test that service is no longer a listener in LightManager
        } catch (IOException | JmriException | JsonException ex) {
            Assert.fail(ex.getMessage());
        }
    }

    public void testOnMessageChange() {
        try {
            JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
            JsonNode message;
            JsonLightSocketService service = new JsonLightSocketService(connection);
            LightManager manager = InstanceManager.getDefault(LightManager.class);
            Light light1 = manager.provideLight("IL1");
            // Light OFF
            message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "IL1").put(JSON.STATE, JSON.OFF);
            service.onMessage(JsonLightServiceFactory.LIGHT, message, Locale.ENGLISH);
            Assert.assertEquals(Light.OFF, light1.getState());
            // Light ON
            message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "IL1").put(JSON.STATE, JSON.ON);
            service.onMessage(JsonLightServiceFactory.LIGHT, message, Locale.ENGLISH);
            Assert.assertEquals(Light.ON, light1.getState());
            // Light UNKNOWN - remains ON
            message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "IL1").put(JSON.STATE, JSON.UNKNOWN);
            service.onMessage(JsonLightServiceFactory.LIGHT, message, Locale.ENGLISH);
            Assert.assertEquals(Light.ON, light1.getState());
            // Light Invalid State
            message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "IL1").put(JSON.STATE, 42); // invalid state
            JsonException exception = null;
            try {
                service.onMessage(JsonLightServiceFactory.LIGHT, message, Locale.ENGLISH);
            } catch (JsonException ex) {
                exception = ex;
            }
            Assert.assertEquals(Light.ON, light1.getState());
            Assert.assertNotNull(exception);
            Assert.assertEquals(HttpServletResponse.SC_BAD_REQUEST, exception.getCode());
        } catch (IOException | JmriException | JsonException ex) {
            Assert.fail(ex.getMessage());
        }
    }

    // from here down is testing infrastructure
    public JsonLightSocketServiceTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {JsonLightSocketServiceTest.class.getName()};
        TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(JsonLightSocketServiceTest.class);

        return suite;
    }

    // The minimal setup for log4J
    @Override
    protected void setUp() throws Exception {
        Log4JFixture.setUp();
        super.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalLightManager();
    }

    @Override
    protected void tearDown() throws Exception {
        JUnitUtil.resetInstanceManager();
        super.tearDown();
        Log4JFixture.tearDown();
    }

}
