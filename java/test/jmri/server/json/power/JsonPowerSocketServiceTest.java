package jmri.server.json.power;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Locale;

import javax.servlet.http.HttpServletResponse;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.PowerManager;
import jmri.jmrix.internal.InternalSystemConnectionMemo;
import jmri.server.json.JSON;
import jmri.server.json.JsonException;
import jmri.server.json.JsonMockConnection;
import jmri.server.json.JsonRequest;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        assertEquals( 1, power.getPropertyChangeListeners().length, "One listener");
        power.setPower(PowerManager.UNKNOWN);
        service.onMessage(JsonPowerServiceFactory.POWER, message, new JsonRequest(locale, JSON.V5, JSON.POST, 42));
        assertEquals( 2, power.getPropertyChangeListeners().length, "Two listeners");
        message = connection.getMessage();
        assertNotNull( message, "Message is not null");
        assertEquals(JSON.UNKNOWN, message.path(JSON.DATA).path(JSON.STATE).asInt());
        power.setPower(PowerManager.ON);
        message = connection.getMessage();
        assertNotNull( message, "Message is not null");
        assertEquals(JSON.ON, message.path(JSON.DATA).path(JSON.STATE).asInt());
        power.setPower(PowerManager.OFF);
        message = connection.getMessage();
        assertNotNull( message, "Message is not null");
        assertEquals(JSON.OFF, message.path(JSON.DATA).path(JSON.STATE).asInt());
        service.onClose();
        assertEquals( 1, power.getPropertyChangeListeners().length, "One listener");
    }

    @Test
    public void testOnMessageChange() throws IOException, JmriException, JsonException {
        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        JsonNode message = connection.getObjectMapper().readTree("{\"state\":2}"); // Power ON
        JsonPowerSocketService service = new JsonPowerSocketService(connection);
        PowerManager power = InstanceManager.getDefault(PowerManager.class);
        power.setPower(PowerManager.UNKNOWN);
        service.onMessage(JsonPowerServiceFactory.POWER, message, new JsonRequest(locale, JSON.V5, JSON.POST, 42));
        assertEquals(PowerManager.ON, power.getPower());
        message = connection.getObjectMapper().readTree("{\"name\":\"Internal\", \"state\":4}"); // Power OFF, named connection
        service.onMessage(JsonPowerServiceFactory.POWER, message, new JsonRequest(locale, JSON.V5, JSON.POST, 42));
        assertEquals(PowerManager.OFF, power.getPower());
        message = connection.getObjectMapper().readTree("{\"state\":0}"); // JSON Power UNKNOWN
        service.onMessage(JsonPowerServiceFactory.POWER, message, new JsonRequest(locale, JSON.V5, JSON.POST, 42));
        assertEquals(PowerManager.OFF, power.getPower()); // did not change
        JsonNode messageEx = connection.getObjectMapper().readTree("{\"state\":1}"); // JSON Invalid
        JsonException ex = assertThrows( JsonException.class, () ->
            service.onMessage(JsonPowerServiceFactory.POWER, messageEx,
                new JsonRequest(locale, JSON.V5, JSON.POST, 42)),
            "Expected exception not thrown");
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, ex.getCode());
        assertEquals("Attempting to set object type power to unknown state 1.", ex.getMessage());
        assertEquals(PowerManager.OFF, power.getPower()); // did not change
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
        assertNotNull(message);
        assertTrue(message.isArray());
        assertEquals(JsonPowerServiceFactory.POWER, message.path(0).path(JSON.TYPE).asText());
        assertEquals(JSON.UNKNOWN, message.path(0).path(JSON.DATA).path(JSON.STATE).asInt(-1));
        assertEquals("Internal", message.path(0).path(JSON.DATA).path(JSON.NAME).asText());
        assertTrue(message.path(0).path(JSON.DATA).path(JSON.DEFAULT).asBoolean());
        power.setPower(PowerManager.ON);
        message = connection.getMessage();
        assertNotNull(message);
        assertTrue(message.isObject());
        assertEquals(JsonPowerServiceFactory.POWER, message.path(JSON.TYPE).asText());
        assertEquals(JSON.ON, message.path(JSON.DATA).path(JSON.STATE).asInt(-1));
        assertEquals("Internal", message.path(JSON.DATA).path(JSON.NAME).asText());
        assertTrue(message.path(JSON.DATA).path(JSON.DEFAULT).asBoolean());
        service.onClose();
    }

    @Test
    public void testSendingErrors() throws IOException, JmriException, JsonException {
        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        final JsonNode message = connection.getObjectMapper().readTree("{\"state\":0}"); // Power UNKNOWN
        TestJsonPowerHttpService http = new TestJsonPowerHttpService(connection.getObjectMapper());
        JsonPowerSocketService service = new JsonPowerSocketService(connection, http);
        http.setThrowException(true);
        JsonException ex = Assertions.assertThrowsExactly(JsonException.class, () ->
            service.onMessage(JsonPowerServiceFactory.POWER, message,
                new JsonRequest(locale, JSON.V5, JSON.POST, 0)));
        assertNotNull(ex);
    }

    @Test
    public void testNoPowerManager() throws IOException, JmriException, JsonException {
        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        JsonNode message = connection.getObjectMapper().readTree("{\"state\":0}"); // Power UNKNOWN
        JsonPowerSocketService service = new JsonPowerSocketService(connection);
        InstanceManager.reset(PowerManager.class);
        service.onMessage(JsonPowerServiceFactory.POWER, message, new JsonRequest(locale, JSON.V5, JSON.GET, 42));
        message = connection.getMessage();
        assertNotNull(message);
        assertEquals(JsonPowerServiceFactory.POWER, message.path(JSON.TYPE).asText());
        assertEquals(JSON.UNKNOWN, message.path(JSON.DATA).path(JSON.STATE).asInt(-1));
    }

    /**
     * Core regression test for the multi-connection isolation bug: a client
     * that subscribed without a prefix (i.e. the JMRI web page power button)
     * must NOT receive updates when an unrelated connection's power state
     * changes — only updates for the default connection should arrive.
     */
    @Test
    public void testDefaultClientIgnoresSecondConnection() throws IOException, JmriException, JsonException {
        PowerManager defaultPm = InstanceManager.getDefault(PowerManager.class);
        defaultPm.setPower(PowerManager.OFF);
        InternalSystemConnectionMemo memo2 = new InternalSystemConnectionMemo("J", "Juliet", false);
        PowerManager pm2 = memo2.get(PowerManager.class);
        assertNotNull(pm2);
        pm2.setPower(PowerManager.OFF);
        InstanceManager.setDefault(PowerManager.class, defaultPm);

        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        JsonPowerSocketService service = new JsonPowerSocketService(connection);
        // Client subscribes with no prefix — should track only the default manager
        service.onMessage(JsonPowerServiceFactory.POWER, connection.getObjectMapper().createObjectNode(),
                new JsonRequest(locale, JSON.V5, JSON.GET, 0));
        int messageCount = connection.getMessages().size();

        // Change the second connection — client must NOT be notified
        pm2.setPower(PowerManager.ON);
        assertEquals(messageCount, connection.getMessages().size(),
                "Client subscribed to default must not receive updates from second connection");

        // Change the default connection — client MUST be notified
        defaultPm.setPower(PowerManager.ON);
        assertEquals(messageCount + 1, connection.getMessages().size(),
                "Client subscribed to default must receive update when default connection changes");
        assertEquals(JSON.ON, connection.getMessage().path(JSON.DATA).path(JSON.STATE).asInt());

        service.onClose();
    }

    /**
     * A client that subscribed with a specific prefix must only receive updates
     * for that connection; changes to the default connection must be ignored.
     */
    @Test
    public void testPrefixClientIgnoresDefaultConnection() throws IOException, JmriException, JsonException {
        PowerManager defaultPm = InstanceManager.getDefault(PowerManager.class);
        defaultPm.setPower(PowerManager.OFF);
        InternalSystemConnectionMemo memo2 = new InternalSystemConnectionMemo("J", "Juliet", false);
        PowerManager pm2 = memo2.get(PowerManager.class);
        assertNotNull(pm2);
        pm2.setPower(PowerManager.OFF);
        InstanceManager.setDefault(PowerManager.class, defaultPm);

        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        JsonPowerSocketService service = new JsonPowerSocketService(connection);
        ObjectNode data = connection.getObjectMapper().createObjectNode();
        data.put(JSON.PREFIX, "J");
        service.onMessage(JsonPowerServiceFactory.POWER, data, new JsonRequest(locale, JSON.V5, JSON.GET, 0));
        int messageCount = connection.getMessages().size();

        // Change default connection — client subscribed to "J" must NOT be notified
        defaultPm.setPower(PowerManager.ON);
        assertEquals(messageCount, connection.getMessages().size(),
                "Client subscribed to prefix J must not receive updates from default connection");

        // Change "J" connection — client MUST be notified
        pm2.setPower(PowerManager.ON);
        assertEquals(messageCount + 1, connection.getMessages().size(),
                "Client subscribed to prefix J must receive update when J connection changes");
        assertEquals(JSON.ON, connection.getMessage().path(JSON.DATA).path(JSON.STATE).asInt());

        service.onClose();
    }

    /**
     * onList must subscribe to all connections; updates from any manager must
     * be delivered to the client.
     */
    @Test
    public void testListSubscribesToAllConnections() throws IOException, JmriException, JsonException {
        PowerManager defaultPm = InstanceManager.getDefault(PowerManager.class);
        defaultPm.setPower(PowerManager.OFF);
        InternalSystemConnectionMemo memo2 = new InternalSystemConnectionMemo("J", "Juliet", false);
        PowerManager pm2 = memo2.get(PowerManager.class);
        assertNotNull(pm2);
        pm2.setPower(PowerManager.OFF);
        InstanceManager.setDefault(PowerManager.class, defaultPm);

        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        JsonPowerSocketService service = new JsonPowerSocketService(connection);
        service.onList(JsonPowerServiceFactory.POWER, connection.getObjectMapper().createObjectNode(),
                new JsonRequest(locale, JSON.V5, JSON.GET, 0));
        int messageCount = connection.getMessages().size();

        // Both connections should trigger notifications after onList
        defaultPm.setPower(PowerManager.ON);
        assertEquals(messageCount + 1, connection.getMessages().size(),
                "onList client must receive update from default connection");

        pm2.setPower(PowerManager.ON);
        assertEquals(messageCount + 2, connection.getMessages().size(),
                "onList client must receive update from second connection");

        service.onClose();
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

        TestJsonPowerHttpService(ObjectMapper mapper) {
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
