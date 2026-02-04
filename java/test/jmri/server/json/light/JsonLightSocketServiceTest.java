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
import jmri.server.json.JsonRequest;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 *
 * @author Paul Bender
 * @author Randall Wood
 */
public class JsonLightSocketServiceTest {

    private final Locale locale = Locale.ENGLISH;

    @Test
    public void testLightChange() throws IOException, JmriException, JsonException {
        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        JsonNode message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "IL1");
        // create light *before* creating service to ensure service does not pick up change in number
        // of lights when creating light for test
        LightManager manager = InstanceManager.getDefault(LightManager.class);
        Light light1 = manager.provideLight("IL1");
        assertEquals( 1, light1.getNumPropertyChangeListeners(), "Light has only one listener");
        JsonLightSocketService service = new JsonLightSocketService(connection);
        service.onMessage(JsonLight.LIGHT, message, new JsonRequest(locale, JSON.V5, JSON.POST, 42));
        assertEquals( 2, light1.getNumPropertyChangeListeners(), "Light is being listened to by service");
        message = connection.getMessage();
        assertNotNull(message);
        assertEquals(JSON.OFF, message.path(JSON.DATA).path(JSON.STATE).asInt());
        light1.setState(Light.ON);
        JUnitUtil.waitFor(() -> {
            return light1.getState() == Light.ON;
        }, "Light to throw");
        message = connection.getMessage();
        assertNotNull(message);
        assertEquals(JSON.ON, message.path(JSON.DATA).path(JSON.STATE).asInt());
        light1.setState(Light.OFF);
        JUnitUtil.waitFor(() -> {
            return light1.getState() == Light.OFF;
        }, "Light to close");
        assertEquals(Light.OFF, light1.getState());
        message = connection.getMessage();
        assertNotNull(message);
        assertEquals(JSON.OFF, message.path(JSON.DATA).path(JSON.STATE).asInt());
        // test IOException handling when listening by triggering exception and
        // observing that light1 is no longer being listened to
        connection.setThrowIOException(true);
        light1.setState(Light.ON);
        JUnitUtil.waitFor(() -> {
            return light1.getState() == Light.ON;
        }, "Light to close");
        assertEquals(Light.ON, light1.getState());
        assertEquals( 1, light1.getNumPropertyChangeListeners(), "Light is no longer listened to by service");
        message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "IL1");
        service.onMessage(JsonLight.LIGHT, message, new JsonRequest(locale, JSON.V5, JSON.POST, 42));
        assertEquals( 2, light1.getNumPropertyChangeListeners(), "Light is being listened to by service");
        service.onClose();
        assertEquals( 1, light1.getNumPropertyChangeListeners(), "Light is no longer listened to by service");
    }

    @Test
    public void testOnMessageChange() throws IOException, JmriException, JsonException {
        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        JsonNode message;
        JsonLightSocketService service = new JsonLightSocketService(connection);
        LightManager manager = InstanceManager.getDefault(LightManager.class);
        Light light1 = manager.provideLight("IL1");
        // Light OFF
        message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "IL1").put(JSON.STATE, JSON.OFF);
        service.onMessage(JsonLight.LIGHT, message, new JsonRequest(locale, JSON.V5, JSON.POST, 42));
        assertEquals(Light.OFF, light1.getState());
        // Light ON
        message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "IL1").put(JSON.STATE, JSON.ON);
        service.onMessage(JsonLight.LIGHT, message, new JsonRequest(locale, JSON.V5, JSON.POST, 42));
        assertEquals(Light.ON, light1.getState());
        // Light UNKNOWN - remains ON
        message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "IL1").put(JSON.STATE, JSON.UNKNOWN);
        service.onMessage(JsonLight.LIGHT, message, new JsonRequest(locale, JSON.V5, JSON.POST, 42));
        assertEquals(Light.ON, light1.getState());
        // Light Invalid State
        JsonNode messageEx = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "IL1").put(JSON.STATE, 42); // invalid state
        JsonException exception = assertThrows( JsonException.class, () ->
            service.onMessage(JsonLight.LIGHT, messageEx,
                new JsonRequest(locale, JSON.V5, JSON.POST, 42)));

        assertEquals(Light.ON, light1.getState());
        assertNotNull(exception);
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, exception.getCode());
    }

    @Test
    public void testOnMessagePut() throws IOException, JmriException, JsonException {
        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        JsonNode message;
        JsonLightSocketService service = new JsonLightSocketService(connection);
        LightManager manager = InstanceManager.getDefault(LightManager.class);
        // Light OFF
        message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "IL1").put(JSON.STATE, JSON.OFF);
        service.onMessage(JsonLight.LIGHT, message, new JsonRequest(locale, JSON.V5, JSON.PUT, 42));
        Light light1 = manager.getBySystemName("IL1");
        assertNotNull( light1, "Light was created by PUT");
        assertEquals(Light.OFF, light1.getState());
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
    }

    @AfterEach
    public void tearDown() throws Exception {
        JUnitUtil.tearDown();
    }

}
