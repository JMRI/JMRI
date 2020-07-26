package jmri.server.json.turnout;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.beans.PropertyVetoException;
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

/**
 *
 * @author Paul Bender
 * @author Randall Wood
 */
public class JsonTurnoutSocketServiceTest {

    private Locale locale = Locale.ENGLISH;

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
        assertNotNull("message is not null", message);
        assertEquals(JSON.UNKNOWN, message.path(JSON.DATA).path(JSON.STATE).asInt());
        turnout1.setCommandedState(Turnout.CLOSED);
        JUnitUtil.waitFor(() -> {
            return turnout1.getKnownState() == Turnout.CLOSED;
        }, "Turnout to close");
        message = connection.getMessage();
        assertNotNull("message is not null", message);
        assertEquals(Turnout.CLOSED, turnout1.getKnownState());
        assertEquals(JSON.CLOSED, message.path(JSON.DATA).path(JSON.STATE).asInt());
        turnout1.setCommandedState(Turnout.THROWN);
        JUnitUtil.waitFor(() -> {
            return turnout1.getKnownState() == Turnout.THROWN;
        }, "Turnout to throw");
        message = connection.getMessage();
        assertNotNull("message is not null", message);
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
        message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "IT1").put(JSON.STATE, 42); // invalid state
        try {
            service.onMessage(JsonTurnout.TURNOUT, message, new JsonRequest(locale, JSON.V5, JSON.POST, 42));
            fail("Expected exception not thrown");
        } catch (JsonException ex) {
            assertEquals(Turnout.THROWN, turnout1.getState());
            assertEquals(HttpServletResponse.SC_BAD_REQUEST, ex.getCode());
        }
    }

    @Test
    public void testOnList() throws IOException, JmriException, JsonException, PropertyVetoException {
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
        assertNotNull("Message is not null", message);
        assertEquals("Manager and message have same size", manager.getNamedBeanSet().size(), message.size());
        manager.register(turnout1);
        JUnitUtil.waitFor(() -> {
            return manager.getBySystemName("IT1") != null;
        });
        message = connection.getMessage();
        assertNotNull("Message is not null", message);
        assertEquals("Manager and message have same size", manager.getNamedBeanSet().size(), message.size());
        manager.register(turnout2);
        JUnitUtil.waitFor(() -> {
            return manager.getBySystemName("IT2") != null;
        });
        message = connection.getMessage();
        assertNotNull("Message is not null", message);
        assertEquals("Manager and message have same size", manager.getNamedBeanSet().size(), message.size());
        assertNotNull("Turnout 2 exists", turnout2);
        manager.deleteBean(turnout2, "");
        JUnitUtil.waitFor(() -> {
            return manager.getBySystemName("IT2") == null;
        });
        message = connection.getMessage();
        assertNotNull("Message is not null", message);
        assertEquals("Manager and message have same size", manager.getNamedBeanSet().size(), message.size());
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
        try {
            message.put(JSON.NAME, "IT2");
            service.onMessage(JsonTurnout.TURNOUT, message, new JsonRequest(locale, JSON.V5, JSON.GET, 42));
            fail("Expected exception not thrown");
        } catch (JsonException ex) {
            assertEquals(HttpServletResponse.SC_NOT_FOUND, ex.getCode());
        }
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
