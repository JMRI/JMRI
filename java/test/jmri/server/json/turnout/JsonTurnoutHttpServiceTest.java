package jmri.server.json.turnout;

import apps.tests.Log4JFixture;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Locale;
import javax.servlet.http.HttpServletResponse;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Turnout;
import jmri.TurnoutManager;
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
public class JsonTurnoutHttpServiceTest extends TestCase {

    public void testCtorSuccess() {
        JsonTurnoutHttpService service = new JsonTurnoutHttpService(new ObjectMapper());
        Assert.assertNotNull(service);
    }

    public void testDoGet() throws JmriException {
        JsonTurnoutHttpService service = new JsonTurnoutHttpService(new ObjectMapper());
        TurnoutManager manager = InstanceManager.getDefault(TurnoutManager.class);
        Turnout turnout1 = manager.provideTurnout("IT1");
        JsonNode result;
        try {
            turnout1.setState(Turnout.UNKNOWN);
            result = service.doGet(JsonTurnoutServiceFactory.TURNOUT, "IT1", Locale.ENGLISH);
            Assert.assertNotNull(result);
            Assert.assertEquals("IT1", result.path(JSON.DATA).path(JSON.NAME).asText());
            Assert.assertEquals(JSON.UNKNOWN, result.path(JSON.DATA).path(JSON.STATE).asInt());
            turnout1.setState(Turnout.CLOSED);
            result = service.doGet(JsonTurnoutServiceFactory.TURNOUT, "IT1", Locale.ENGLISH);
            Assert.assertNotNull(result);
            Assert.assertEquals(JSON.CLOSED, result.path(JSON.DATA).path(JSON.STATE).asInt());
            turnout1.setState(Turnout.THROWN);
            result = service.doGet(JsonTurnoutServiceFactory.TURNOUT, "IT1", Locale.ENGLISH);
            Assert.assertNotNull(result);
            Assert.assertEquals(JSON.THROWN, result.path(JSON.DATA).path(JSON.STATE).asInt());
        } catch (JsonException ex) {
            Assert.fail(ex.getMessage());
        }
    }

    public void testDoPost() throws JmriException {
        ObjectMapper mapper = new ObjectMapper();
        JsonTurnoutHttpService service = new JsonTurnoutHttpService(mapper);
        TurnoutManager manager = InstanceManager.getDefault(TurnoutManager.class);
        Turnout turnout1 = manager.provideTurnout("IT1");
        JsonNode result;
        JsonNode message;
        try {
            turnout1.setState(Turnout.UNKNOWN);
            // set closed
            message = mapper.createObjectNode().put(JSON.NAME, "IT1").put(JSON.STATE, JSON.CLOSED);
            result = service.doPost(JsonTurnoutServiceFactory.TURNOUT, "IT1", message, Locale.ENGLISH);
            Assert.assertEquals(Turnout.CLOSED, turnout1.getState());
            Assert.assertNotNull(result);
            Assert.assertEquals(JSON.CLOSED, result.path(JSON.DATA).path(JSON.STATE).asInt());
            // set thrown
            message = mapper.createObjectNode().put(JSON.NAME, "IT1").put(JSON.STATE, JSON.THROWN);
            result = service.doPost(JsonTurnoutServiceFactory.TURNOUT, "IT1", message, Locale.ENGLISH);
            Assert.assertEquals(Turnout.THROWN, turnout1.getState());
            Assert.assertNotNull(result);
            Assert.assertEquals(JSON.THROWN, result.path(JSON.DATA).path(JSON.STATE).asInt());
            // set unknown - remains thrown
            message = mapper.createObjectNode().put(JSON.NAME, "IT1").put(JSON.STATE, JSON.UNKNOWN);
            result = service.doPost(JsonTurnoutServiceFactory.TURNOUT, "IT1", message, Locale.ENGLISH);
            Assert.assertEquals(Turnout.THROWN, turnout1.getState());
            Assert.assertEquals(JSON.THROWN, result.path(JSON.DATA).path(JSON.STATE).asInt());
            // set invalid state
            message = mapper.createObjectNode().put(JSON.NAME, "IT1").put(JSON.STATE, 42); // Invalid value
            JsonException exception = null;
            try {
                service.doPost(JsonTurnoutServiceFactory.TURNOUT, "IT1", message, Locale.ENGLISH);
            } catch (JsonException ex) {
                exception = ex;
            }
            Assert.assertEquals(Turnout.THROWN, turnout1.getState());
            Assert.assertNotNull(exception);
            Assert.assertEquals(HttpServletResponse.SC_BAD_REQUEST, exception.getCode());
        } catch (JsonException ex) {
            Assert.fail(ex.getMessage());
        }
    }

    public void testDoPut() {
        ObjectMapper mapper = new ObjectMapper();
        JsonTurnoutHttpService service = new JsonTurnoutHttpService(mapper);
        TurnoutManager manager = InstanceManager.getDefault(TurnoutManager.class);
        JsonNode result;
        JsonNode message;
        try {
            // add a turnout
            Assert.assertNull(manager.getTurnout("IT1"));
            message = mapper.createObjectNode().put(JSON.NAME, "IT1").put(JSON.STATE, Turnout.CLOSED);
            result = service.doPut(JsonTurnoutServiceFactory.TURNOUT, "IT1", message, Locale.ENGLISH);
            Assert.assertNotNull(manager.getTurnout("IT1"));
        } catch (JsonException ex) {
            Assert.fail(ex.getMessage());
        }
    }
    
    public void testDoGetList() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonTurnoutHttpService service = new JsonTurnoutHttpService(mapper);
            TurnoutManager manager = InstanceManager.getDefault(TurnoutManager.class);
            JsonNode result;
            result = service.doGetList(JsonTurnoutServiceFactory.TURNOUT, Locale.ENGLISH);
            Assert.assertNotNull(result);
            Assert.assertEquals(0, result.size());
            Turnout turnout1 = manager.provideTurnout("IT1");
            Turnout turnout2 = manager.provideTurnout("IT2");
            result = service.doGetList(JsonTurnoutServiceFactory.TURNOUT, Locale.ENGLISH);
            Assert.assertNotNull(result);
            Assert.assertEquals(2, result.size());
        } catch (JsonException ex) {
            Assert.fail(ex.getMessage());
        }
    }
    
    public void testDelete() {
        try {
            (new JsonTurnoutHttpService(new ObjectMapper())).doDelete(JsonTurnoutServiceFactory.TURNOUT, null, Locale.ENGLISH);
        } catch (JsonException ex) {
            Assert.assertEquals(HttpServletResponse.SC_METHOD_NOT_ALLOWED, ex.getCode());
            return;
        }
        Assert.fail("Did not throw expected error.");
    }
    
    // from here down is testing infrastructure
    public JsonTurnoutHttpServiceTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {JsonTurnoutHttpServiceTest.class.getName()};
        TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(JsonTurnoutHttpServiceTest.class);

        return suite;
    }

    // The minimal setup for log4J
    @Override
    protected void setUp() throws Exception {
        Log4JFixture.setUp();
        super.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalTurnoutManager();
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
