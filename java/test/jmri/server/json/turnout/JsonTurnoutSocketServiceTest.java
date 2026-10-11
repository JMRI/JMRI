package jmri.server.json.turnout;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Locale;

import javax.servlet.http.HttpServletResponse;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Turnout;
import jmri.TurnoutManager;
import jmri.server.json.JSON;
import jmri.server.json.JsonException;
import jmri.server.json.JsonMockConnection;
import jmri.server.json.JsonRequest;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 * @author Paul Bender
 * @author Randall Wood
 */
public class JsonTurnoutSocketServiceTest {

    private final Locale locale = Locale.ENGLISH;

    @Test
    public void testTurnoutChange() throws IOException, JmriException, JsonException {
        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        JsonNode message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "IT1");
        JsonTurnoutSocketService service = new JsonTurnoutSocketService(connection);
        TurnoutManager manager = InstanceManager.getDefault(TurnoutManager.class);
        Turnout turnout1 = manager.provideTurnout("IT1");
        turnout1.setCommandedState(Turnout.UNKNOWN);
        service.onMessage(JsonTurnout.TURNOUT, message, new JsonRequest(locale, JSON.V5, JSON.GET, 42));
        // TODO: test that service is listener in TurnoutManager
        message = connection.getMessage();
        assertNotNull( message, "message is not null");
        assertEquals(JSON.UNKNOWN, message.path(JSON.DATA).path(JSON.STATE).asInt());
        turnout1.setCommandedState(Turnout.CLOSED);
        JUnitUtil.waitFor(() -> {
            return turnout1.getKnownState() == Turnout.CLOSED;
        }, "Turnout to close");
        message = connection.getMessage();
        assertNotNull( message, "message is not null");
        assertEquals(Turnout.CLOSED, turnout1.getKnownState());
        assertEquals(JSON.CLOSED, message.path(JSON.DATA).path(JSON.STATE).asInt());
        turnout1.setCommandedState(Turnout.THROWN);
        JUnitUtil.waitFor(() -> {
            return turnout1.getKnownState() == Turnout.THROWN;
        }, "Turnout to throw");
        message = connection.getMessage();
        assertNotNull( message, "message is not null");
        assertEquals(JSON.THROWN, message.path(JSON.DATA).path(JSON.STATE).asInt());
        service.onClose();
        // TODO: test that service is no longer a listener in TurnoutManager
    }

    @Test
    public void testOnMessageChange() throws IOException, JmriException, JsonException {
        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        JsonNode message;
        JsonTurnoutSocketService service = new JsonTurnoutSocketService(connection);
        TurnoutManager manager = InstanceManager.getDefault(TurnoutManager.class);
        Turnout turnout1 = manager.provideTurnout("IT1");
        turnout1.setCommandedState(Turnout.UNKNOWN);
        // Turnout CLOSED
        message =
                connection.getObjectMapper().createObjectNode().put(JSON.NAME, "IT1").put(JSON.STATE, JSON.CLOSED);
        service.onMessage(JsonTurnout.TURNOUT, message, new JsonRequest(locale, JSON.V5, JSON.POST, 42));
        assertEquals(Turnout.CLOSED, turnout1.getState());
        // Turnout THROWN
        message =
                connection.getObjectMapper().createObjectNode().put(JSON.NAME, "IT1").put(JSON.STATE, JSON.THROWN);
        service.onMessage(JsonTurnout.TURNOUT, message, new JsonRequest(locale, JSON.V5, JSON.POST, 42));
        assertEquals(Turnout.THROWN, turnout1.getState());
        // Turnout UNKNOWN - remains THROWN
        message =
                connection.getObjectMapper().createObjectNode().put(JSON.NAME, "IT1").put(JSON.STATE, JSON.UNKNOWN);
        service.onMessage(JsonTurnout.TURNOUT, message, new JsonRequest(locale, JSON.V5, JSON.POST, 42));
        assertEquals(Turnout.THROWN, turnout1.getState());
        // Turnout Invalid State
        JsonNode messageEx = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "IT1").put(JSON.STATE, 42); // invalid state
        JsonException ex = assertThrows( JsonException.class, () ->
            service.onMessage(JsonTurnout.TURNOUT, messageEx,
                new JsonRequest(locale, JSON.V5, JSON.POST, 42)),
            "Expected exception not thrown");
        assertEquals(Turnout.THROWN, turnout1.getState());
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, ex.getCode());

    }

    @Test
    public void testOnList() throws IOException, JmriException, JsonException {
        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        JsonNode message;
        JsonTurnoutSocketService service = new JsonTurnoutSocketService(connection);
        TurnoutManager manager = InstanceManager.getDefault(TurnoutManager.class);
        // provide and deregister in tests to avoid messages from the Turnouts themselves
        Turnout turnout1 = manager.provide("IT1");
        Turnout turnout2 = manager.provide("IT2");
        manager.deregister(turnout1);
        manager.deregister(turnout2);
        service.onList(JsonTurnout.TURNOUT, connection.getObjectMapper().createObjectNode(),
                new JsonRequest(locale, JSON.V5, JSON.GET, 0));
        message = connection.getMessage();
        assertNotNull( message, "Message is not null");
        assertEquals( manager.getNamedBeanSet().size(), message.size(), "Manager and message have same size");
        manager.register(turnout1);
        JUnitUtil.waitFor(() -> {
            return manager.getBySystemName("IT1") != null;
        },"IT1 not null");
        message = connection.getMessage();
        assertNotNull( message, "Message is not null");
        assertEquals( manager.getNamedBeanSet().size(), message.size(), "Manager and message have same size");
        manager.register(turnout2);
        JUnitUtil.waitFor(() -> {
            return manager.getBySystemName("IT2") != null;
        },"match for IT2");
        message = connection.getMessage();
        assertNotNull( message, "Message is not null");
        assertEquals( manager.getNamedBeanSet().size(), message.size(), "Manager and message have same size");
        assertNotNull( turnout2, "Turnout 2 exists");
        assertDoesNotThrow( () ->
            manager.deleteBean(turnout2, TurnoutManager.PROPERTY_DO_DELETE),
            "Exception deleting bean ");

        JUnitUtil.waitFor(() -> {
            return manager.getBySystemName("IT2") == null;
        },"no match for IT2");
        message = connection.getMessage();
        assertNotNull( message, "Message is not null");
        assertEquals( manager.getNamedBeanSet().size(), message.size(), "Manager and message have same size");

        connection.close();
    }

    @Test
    public void testWarningOnUserName() throws IOException, JmriException, JsonException {
        String userName = "Internal Turnout 1";
        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        ObjectNode message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, userName);
        JsonTurnoutSocketService service = new JsonTurnoutSocketService(connection);
        TurnoutManager manager = InstanceManager.getDefault(TurnoutManager.class);
        Turnout t1 = manager.provide("IT1");
        t1.setUserName(userName);
        // request with user name should log a warning
        service.onMessage(JsonTurnout.TURNOUT, message, new JsonRequest(locale, JSON.V5, JSON.GET, 42));
        JUnitAppender.assertWarnMessage("get request for turnout made with user name \"" + userName + "\"; should use system name");
        // request with system name should not log a warning
        message.put(JSON.NAME, "IT1");
        service.onMessage(JsonTurnout.TURNOUT, message, new JsonRequest(locale, JSON.V5, JSON.GET, 42));
        assertTrue(JUnitAppender.verifyNoBacklog());
        // request with name of non-existent bean should not log a warning (but should throw exception)
        JsonException ex = assertThrows( JsonException.class, () -> {
            message.put(JSON.NAME, "IT2");
            service.onMessage(JsonTurnout.TURNOUT, message, new JsonRequest(locale, JSON.V5, JSON.GET, 42));
        }, "Expected exception not thrown");
        assertEquals(HttpServletResponse.SC_NOT_FOUND, ex.getCode());
        assertTrue(JUnitAppender.verifyNoBacklog());
    }

    @BeforeEach
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initInternalTurnoutManager();
    }

    @AfterEach
    public void tearDown() throws Exception {
        JUnitUtil.tearDown();
    }

}
