package jmri.server.json.light;

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
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender
 * @author Randall Wood
 */
public class JsonLightSocketServiceTest {

    @Test
    public void testLightChange() {
        try {
            JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
            JsonNode message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "IL1");
            JsonLightSocketService service = new JsonLightSocketService(connection);
            LightManager manager = InstanceManager.getDefault(LightManager.class);
            Light light1 = manager.provideLight("IL1");
            service.onMessage(JsonLight.LIGHT, message, JSON.POST, Locale.ENGLISH);
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

    @Test
    public void testOnMessageChange() {
        try {
            JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
            JsonNode message;
            JsonLightSocketService service = new JsonLightSocketService(connection);
            LightManager manager = InstanceManager.getDefault(LightManager.class);
            Light light1 = manager.provideLight("IL1");
            // Light OFF
            message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "IL1").put(JSON.STATE, JSON.OFF);
            service.onMessage(JsonLight.LIGHT, message, JSON.POST, Locale.ENGLISH);
            Assert.assertEquals(Light.OFF, light1.getState());
            // Light ON
            message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "IL1").put(JSON.STATE, JSON.ON);
            service.onMessage(JsonLight.LIGHT, message, JSON.POST, Locale.ENGLISH);
            Assert.assertEquals(Light.ON, light1.getState());
            // Light UNKNOWN - remains ON
            message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "IL1").put(JSON.STATE, JSON.UNKNOWN);
            service.onMessage(JsonLight.LIGHT, message, JSON.POST, Locale.ENGLISH);
            Assert.assertEquals(Light.ON, light1.getState());
            // Light Invalid State
            message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "IL1").put(JSON.STATE, 42); // invalid state
            JsonException exception = null;
            try {
                service.onMessage(JsonLight.LIGHT, message, JSON.POST, Locale.ENGLISH);
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

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() throws Exception {
        JUnitUtil.tearDown();
    }

}
