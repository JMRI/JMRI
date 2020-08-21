package jmri.server.json.power;

import static org.assertj.core.api.Assertions.assertThatCode;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Locale;

import javax.servlet.http.HttpServletResponse;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.PowerManager;
import jmri.server.json.JSON;
import jmri.server.json.JsonException;
import jmri.server.json.JsonMockConnection;
import jmri.server.json.JsonRequest;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender
 * @author Randall Wood
 */
public class JsonPowerSocketServiceTest {

    private final Locale locale = Locale.ENGLISH;

    @Test
    public void testPowerChange() throws IOException, JmriException, JsonException {
        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        JsonNode message = connection.getObjectMapper().readTree("{}");
        JsonPowerSocketService service = new JsonPowerSocketService(connection);
        PowerManager power = InstanceManager.getDefault(PowerManager.class);
        Assert.assertEquals("One listener", 1, power.getPropertyChangeListeners().length);
        power.setPower(PowerManager.UNKNOWN);
        service.onMessage(JsonPowerServiceFactory.POWER, message, new JsonRequest(locale, JSON.V5, JSON.POST, 42));
        Assert.assertEquals("Two listeners", 2, power.getPropertyChangeListeners().length);
        message = connection.getMessage();
        Assert.assertNotNull("Message is not null", message);
        Assert.assertEquals(JSON.UNKNOWN, message.path(JSON.DATA).path(JSON.STATE).asInt());
        power.setPower(PowerManager.ON);
        message = connection.getMessage();
        Assert.assertNotNull("Message is not null", message);
        Assert.assertEquals(JSON.ON, message.path(JSON.DATA).path(JSON.STATE).asInt());
        power.setPower(PowerManager.OFF);
        message = connection.getMessage();
        Assert.assertNotNull("Message is not null", message);
        Assert.assertEquals(JSON.OFF, message.path(JSON.DATA).path(JSON.STATE).asInt());
        service.onClose();
        Assert.assertEquals("One listener", 1, power.getPropertyChangeListeners().length);
    }

    @Test
    public void testOnMessageChange() throws IOException, JmriException, JsonException {
        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        JsonNode message = connection.getObjectMapper().readTree("{\"state\":2}"); // Power ON
        JsonPowerSocketService service = new JsonPowerSocketService(connection);
        PowerManager power = InstanceManager.getDefault(PowerManager.class);
        power.setPower(PowerManager.UNKNOWN);
        service.onMessage(JsonPowerServiceFactory.POWER, message, new JsonRequest(locale, JSON.V5, JSON.POST, 42));
        Assert.assertEquals(PowerManager.ON, power.getPower());
        message = connection.getObjectMapper().readTree("{\"name\":\"Internal\", \"state\":4}"); // Power OFF, named connection
        service.onMessage(JsonPowerServiceFactory.POWER, message, new JsonRequest(locale, JSON.V5, JSON.POST, 42));
        Assert.assertEquals(PowerManager.OFF, power.getPower());
        message = connection.getObjectMapper().readTree("{\"state\":0}"); // JSON Power UNKNOWN
        service.onMessage(JsonPowerServiceFactory.POWER, message, new JsonRequest(locale, JSON.V5, JSON.POST, 42));
        Assert.assertEquals(PowerManager.OFF, power.getPower()); // did not change
        message = connection.getObjectMapper().readTree("{\"state\":1}"); // JSON Invalid
        try {
            service.onMessage(JsonPowerServiceFactory.POWER, message, new JsonRequest(locale, JSON.V5, JSON.POST, 42));
            Assert.fail("Expected exception not thrown");
        } catch (JsonException ex) {
            Assert.assertEquals(HttpServletResponse.SC_BAD_REQUEST, ex.getCode());
            Assert.assertEquals("Attempting to set object type power to unknown state 1.", ex.getMessage());
        }
        Assert.assertEquals(PowerManager.OFF, power.getPower()); // did not change
        service.onClose();
    }

    @Test
    public void testOnList() throws IOException, JmriException, JsonException {
        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        JsonNode message = connection.getObjectMapper().readTree("{\"state\":0}"); // Power UNKNOWN
        JsonPowerSocketService service = new JsonPowerSocketService(connection);
        PowerManager power = InstanceManager.getDefault(PowerManager.class);
        power.setPower(PowerManager.UNKNOWN);
        service.onList(JsonPowerServiceFactory.POWER, message, new JsonRequest(locale, JSON.V5, JSON.GET, 0));
        message = connection.getMessage();
        Assert.assertNotNull(message);
        Assert.assertTrue(message.isArray());
        Assert.assertEquals(JsonPowerServiceFactory.POWER, message.path(0).path(JSON.TYPE).asText());
        Assert.assertEquals(JSON.UNKNOWN, message.path(0).path(JSON.DATA).path(JSON.STATE).asInt(-1));
        Assert.assertEquals("Internal", message.path(0).path(JSON.DATA).path(JSON.NAME).asText());
        Assert.assertTrue(message.path(0).path(JSON.DATA).path(JSON.DEFAULT).asBoolean());
        power.setPower(PowerManager.ON);
        message = connection.getMessage();
        Assert.assertNotNull(message);
        Assert.assertTrue(message.isObject());
        Assert.assertEquals(JsonPowerServiceFactory.POWER, message.path(JSON.TYPE).asText());
        Assert.assertEquals(JSON.ON, message.path(JSON.DATA).path(JSON.STATE).asInt(-1));
        Assert.assertEquals("Internal", message.path(JSON.DATA).path(JSON.NAME).asText());
        Assert.assertTrue(message.path(JSON.DATA).path(JSON.DEFAULT).asBoolean());
        service.onClose();
    }

    @Test
    public void testSendingErrors() throws IOException, JmriException, JsonException {
        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        final JsonNode message = connection.getObjectMapper().readTree("{\"state\":0}"); // Power UNKNOWN
        TestJsonPowerHttpService http = new TestJsonPowerHttpService(connection.getObjectMapper());
        JsonPowerSocketService service = new JsonPowerSocketService(connection, http);
        http.setThrowException(true);
        assertThatCode(() -> service.onMessage(JsonPowerServiceFactory.POWER, message, new JsonRequest(locale, JSON.V5, JSON.POST, 0)))
        .isExactlyInstanceOf(JsonException.class);
    }

    @Test
    public void testNoPowerManager() throws IOException, JmriException, JsonException {
        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        JsonNode message = connection.getObjectMapper().readTree("{\"state\":0}"); // Power UNKNOWN
        JsonPowerSocketService service = new JsonPowerSocketService(connection);
        InstanceManager.reset(PowerManager.class);
        service.onMessage(JsonPowerServiceFactory.POWER, message, new JsonRequest(locale, JSON.V5, JSON.GET, 42));
        message = connection.getMessage();
        Assert.assertNotNull(message);
        Assert.assertEquals(JsonPowerServiceFactory.POWER, message.path(JSON.TYPE).asText());
        Assert.assertEquals(JSON.UNKNOWN, message.path(JSON.DATA).path(JSON.STATE).asInt(-1));
    }

    @BeforeEach
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initDebugThrottleManager();
        JUnitUtil.initDebugPowerManager();
    }

    @AfterEach
    public void tearDown() throws Exception {
        JUnitUtil.tearDown();
    }

    private static class TestJsonPowerHttpService extends JsonPowerHttpService {

        private boolean throwException = false;

        public TestJsonPowerHttpService(ObjectMapper mapper) {
            super(mapper);
        }

        @Override
        public JsonNode doPost(String type, String name, JsonNode data, JsonRequest request) throws JsonException {
            if (throwException) {
                throwException = false;
                throw new JsonException(499, "Mock Exception", request.id);
            }
            return super.doPost(type, name, data, request);
        }

        public void setThrowException(boolean throwException) {
            this.throwException = throwException;
        }

    }
}
