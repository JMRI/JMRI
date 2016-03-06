package jmri.server.json.turnout;

import apps.tests.Log4JFixture;
import com.fasterxml.jackson.databind.JsonNode;
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
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.swingui.TestRunner;

/**
 *
 * @author Paul Bender
 * @author Randall Wood
 */
public class JsonTurnoutSocketServiceTest extends TestCase {

    public void testCtorSuccess() {
        JsonTurnoutSocketService service = new JsonTurnoutSocketService(new JsonMockConnection((DataOutputStream) null));
        Assert.assertNotNull(service);
    }

    public void testTurnoutChange() {
        try {
            JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
            JsonNode message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "IT1");
            JsonTurnoutSocketService service = new JsonTurnoutSocketService(connection);
            TurnoutManager manager = InstanceManager.getDefault(TurnoutManager.class);
            Turnout turnout1 = manager.provideTurnout("IT1");
            turnout1.setCommandedState(Turnout.UNKNOWN);
            service.onMessage(JsonTurnoutServiceFactory.TURNOUT, message, Locale.ENGLISH);
            // TODO: test that service is listener in TurnoutManager
            Assert.assertEquals(JSON.UNKNOWN, connection.getMessage().path(JSON.DATA).path(JSON.STATE).asInt());
            turnout1.setCommandedState(Turnout.CLOSED);
            JUnitUtil.waitFor(() -> {
                return turnout1.getKnownState() == Turnout.CLOSED;
            }, "Turnout to close");
            Assert.assertEquals(Turnout.CLOSED, turnout1.getKnownState());
            Assert.assertEquals(JSON.CLOSED, connection.getMessage().path(JSON.DATA).path(JSON.STATE).asInt());
            turnout1.setCommandedState(Turnout.THROWN);
            JUnitUtil.waitFor(() -> {
                return turnout1.getKnownState() == Turnout.THROWN;
            }, "Turnout to throw");
            Assert.assertEquals(JSON.THROWN, connection.getMessage().path(JSON.DATA).path(JSON.STATE).asInt());
            service.onClose();
            // TODO: test that service is no longer a listener in TurnoutManager
        } catch (IOException | JmriException | JsonException ex) {
            Assert.fail(ex.getMessage());
        }
    }

    public void testOnMessageChange() {
        try {
            JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
            JsonNode message;
            JsonTurnoutSocketService service = new JsonTurnoutSocketService(connection);
            TurnoutManager manager = InstanceManager.getDefault(TurnoutManager.class);
            Turnout turnout1 = manager.provideTurnout("IT1");
            turnout1.setCommandedState(Turnout.UNKNOWN);
            // Turnout CLOSED
            message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "IT1").put(JSON.STATE, JSON.CLOSED);
            service.onMessage(JsonTurnoutServiceFactory.TURNOUT, message, Locale.ENGLISH);
            Assert.assertEquals(Turnout.CLOSED, turnout1.getState());
            // Turnout THROWN
            message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "IT1").put(JSON.STATE, JSON.THROWN);
            service.onMessage(JsonTurnoutServiceFactory.TURNOUT, message, Locale.ENGLISH);
            Assert.assertEquals(Turnout.THROWN, turnout1.getState());
            // Turnout UNKNOWN - remains THROWN
            message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "IT1").put(JSON.STATE, JSON.UNKNOWN);
            service.onMessage(JsonTurnoutServiceFactory.TURNOUT, message, Locale.ENGLISH);
            Assert.assertEquals(Turnout.THROWN, turnout1.getState());
            // Turnout Invalid State
            message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "IT1").put(JSON.STATE, 42); // invalid state
            JsonException exception = null;
            try {
                service.onMessage(JsonTurnoutServiceFactory.TURNOUT, message, Locale.ENGLISH);
            } catch (JsonException ex) {
                exception = ex;
            }
            Assert.assertEquals(Turnout.THROWN, turnout1.getState());
            Assert.assertNotNull(exception);
            Assert.assertEquals(HttpServletResponse.SC_BAD_REQUEST, exception.getCode());
        } catch (IOException | JmriException | JsonException ex) {
            Assert.fail(ex.getMessage());
        }
    }

    // from here down is testing infrastructure
    public JsonTurnoutSocketServiceTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {JsonTurnoutSocketServiceTest.class.getName()};
        TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(JsonTurnoutSocketServiceTest.class);

        return suite;
    }

    // The minimal setup for log4J
    @Override
    protected void setUp() throws Exception {
        Log4JFixture.setUp();
        super.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalTurnoutManager();
    }

    @Override
    protected void tearDown() throws Exception {
        JUnitUtil.resetInstanceManager();
        super.tearDown();
        Log4JFixture.tearDown();
    }

}
