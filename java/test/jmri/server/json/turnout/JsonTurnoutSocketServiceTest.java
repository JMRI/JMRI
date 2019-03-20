package jmri.server.json.turnout;

import com.fasterxml.jackson.databind.JsonNode;

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
public class JsonTurnoutSocketServiceTest {

    @Test
    public void testTurnoutChange() {
        try {
            JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
            JsonNode message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "IT1");
            JsonTurnoutSocketService service = new JsonTurnoutSocketService(connection);
            TurnoutManager manager = InstanceManager.getDefault(TurnoutManager.class);
            Turnout turnout1 = manager.provideTurnout("IT1");
            turnout1.setCommandedState(Turnout.UNKNOWN);
            service.onMessage(JsonTurnoutServiceFactory.TURNOUT, message, JSON.GET, Locale.ENGLISH);
            // TODO: test that service is listener in TurnoutManager
            message = connection.getMessage();
            Assert.assertNotNull("message is not null", message);
            Assert.assertEquals(JSON.UNKNOWN, message.path(JSON.DATA).path(JSON.STATE).asInt());
            turnout1.setCommandedState(Turnout.CLOSED);
            JUnitUtil.waitFor(() -> {
                return turnout1.getKnownState() == Turnout.CLOSED;
            }, "Turnout to close");
            message = connection.getMessage();
            Assert.assertNotNull("message is not null", message);
            Assert.assertEquals(Turnout.CLOSED, turnout1.getKnownState());
            Assert.assertEquals(JSON.CLOSED, message.path(JSON.DATA).path(JSON.STATE).asInt());
            turnout1.setCommandedState(Turnout.THROWN);
            JUnitUtil.waitFor(() -> {
                return turnout1.getKnownState() == Turnout.THROWN;
            }, "Turnout to throw");
            message = connection.getMessage();
            Assert.assertNotNull("message is not null", message);
            Assert.assertEquals(JSON.THROWN, message.path(JSON.DATA).path(JSON.STATE).asInt());
            service.onClose();
            // TODO: test that service is no longer a listener in TurnoutManager
        } catch (
                IOException |
                JmriException |
                JsonException ex) {
            Assert.fail(ex.getMessage());
        }
    }

    @Test
    public void testOnMessageChange() {
        try {
            JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
            JsonNode message;
            JsonTurnoutSocketService service = new JsonTurnoutSocketService(connection);
            TurnoutManager manager = InstanceManager.getDefault(TurnoutManager.class);
            Turnout turnout1 = manager.provideTurnout("IT1");
            turnout1.setCommandedState(Turnout.UNKNOWN);
            // Turnout CLOSED
            message =
                    connection.getObjectMapper().createObjectNode().put(JSON.NAME, "IT1").put(JSON.STATE, JSON.CLOSED);
            service.onMessage(JsonTurnoutServiceFactory.TURNOUT, message, JSON.POST, Locale.ENGLISH);
            Assert.assertEquals(Turnout.CLOSED, turnout1.getState());
            // Turnout THROWN
            message =
                    connection.getObjectMapper().createObjectNode().put(JSON.NAME, "IT1").put(JSON.STATE, JSON.THROWN);
            service.onMessage(JsonTurnoutServiceFactory.TURNOUT, message, JSON.POST, Locale.ENGLISH);
            Assert.assertEquals(Turnout.THROWN, turnout1.getState());
            // Turnout UNKNOWN - remains THROWN
            message =
                    connection.getObjectMapper().createObjectNode().put(JSON.NAME, "IT1").put(JSON.STATE, JSON.UNKNOWN);
            service.onMessage(JsonTurnoutServiceFactory.TURNOUT, message, JSON.POST, Locale.ENGLISH);
            Assert.assertEquals(Turnout.THROWN, turnout1.getState());
            // Turnout Invalid State
            message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "IT1").put(JSON.STATE, 42); // invalid state
            try {
                service.onMessage(JsonTurnoutServiceFactory.TURNOUT, message, JSON.POST, Locale.ENGLISH);
                Assert.fail("Expected exception not thrown");
            } catch (JsonException ex) {
                Assert.assertEquals(Turnout.THROWN, turnout1.getState());
                Assert.assertEquals(HttpServletResponse.SC_BAD_REQUEST, ex.getCode());
            }
        } catch (
                IOException |
                JmriException |
                JsonException ex) {
            Assert.fail(ex.getMessage());
        }
    }

    @Test
    public void testOnList() {
        try {
            JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
            JsonNode message;
            JsonTurnoutSocketService service = new JsonTurnoutSocketService(connection);
            TurnoutManager manager = InstanceManager.getDefault(TurnoutManager.class);
            // use register instead of provide in tests to avoid messages from the Turnouts themselves
            Turnout turnout1 = manager.provide("IT1");
            Turnout turnout2 = manager.provide("IT2");
            manager.deregister(turnout1);
            manager.deregister(turnout2);
            service.onList(JsonTurnoutServiceFactory.TURNOUT, connection.getObjectMapper().createObjectNode(),
                    Locale.ENGLISH);
            message = connection.getMessage();
            Assert.assertNotNull("Message is not null", message);
            Assert.assertEquals("Manager and message have same size", manager.getNamedBeanSet().size(), message.size());
            manager.register(turnout1);
            JUnitUtil.waitFor(() -> {
                return manager.getBeanBySystemName("IT1") != null;
            });
            message = connection.getMessage();
            Assert.assertNotNull("Message is not null", message);
            Assert.assertEquals("Manager and message have same size", manager.getNamedBeanSet().size(), message.size());
            manager.register(turnout2);
            JUnitUtil.waitFor(() -> {
                return manager.getBeanBySystemName("IT2") != null;
            });
            message = connection.getMessage();
            Assert.assertNotNull("Message is not null", message);
            Assert.assertEquals("Manager and message have same size", manager.getNamedBeanSet().size(), message.size());
            Assert.assertNotNull("Turnout 2 exists", turnout2);
            manager.deleteBean(turnout2, "");
            JUnitUtil.waitFor(() -> {
                return manager.getBeanBySystemName("IT2") == null;
            });
            message = connection.getMessage();
            Assert.assertNotNull("Message is not null", message);
            Assert.assertEquals("Manager and message have same size", manager.getNamedBeanSet().size(), message.size());
        } catch (
                IOException |
                JmriException |
                JsonException |
                PropertyVetoException ex) {
            Assert.fail(ex.getMessage());
        }
    }

    @Before
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initInternalTurnoutManager();
    }

    @After
    public void tearDown() throws Exception {
        JUnitUtil.tearDown();
    }

}
