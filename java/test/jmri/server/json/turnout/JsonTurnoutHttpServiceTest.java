package jmri.server.json.turnout;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import javax.servlet.http.HttpServletResponse;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Turnout;
import jmri.TurnoutManager;
import jmri.server.json.JSON;
import jmri.server.json.JsonException;
import jmri.server.json.JsonNamedBeanHttpServiceTestBase;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender
 * @author Randall Wood
 */
public class JsonTurnoutHttpServiceTest extends JsonNamedBeanHttpServiceTestBase<Turnout, JsonTurnoutHttpService> {

    @Test
    @Override
    public void testDoGet() throws JmriException, JsonException {
        TurnoutManager manager = InstanceManager.getDefault(TurnoutManager.class);
        Turnout turnout1 = manager.provideTurnout("IT1");
        JsonNode result;
        turnout1.setState(Turnout.UNKNOWN);
        result = service.doGet(JsonTurnoutServiceFactory.TURNOUT, "IT1",
                NullNode.getInstance(), locale, 42);
        validate(result);
        assertEquals(JsonTurnoutServiceFactory.TURNOUT, result.path(JSON.TYPE).asText());
        assertEquals("IT1", result.path(JSON.DATA).path(JSON.NAME).asText());
        assertEquals(JSON.UNKNOWN, result.path(JSON.DATA).path(JSON.STATE).asInt());
        turnout1.setState(Turnout.CLOSED);
        result = service.doGet(JsonTurnoutServiceFactory.TURNOUT, "IT1",
                NullNode.getInstance(), locale, 42);
        validate(result);
        assertEquals(JSON.CLOSED, result.path(JSON.DATA).path(JSON.STATE).asInt());
        turnout1.setState(Turnout.THROWN);
        result = service.doGet(JsonTurnoutServiceFactory.TURNOUT, "IT1",
                NullNode.getInstance(), locale, 42);
        validate(result);
        assertEquals(JSON.THROWN, result.path(JSON.DATA).path(JSON.STATE).asInt());
        turnout1.setState(Turnout.INCONSISTENT);
        result = service.doGet(JsonTurnoutServiceFactory.TURNOUT, "IT1",
                NullNode.getInstance(), locale, 42);
        validate(result);
        assertEquals(JSON.INCONSISTENT, result.path(JSON.DATA).path(JSON.STATE).asInt());
    }

    @Test
    public void testDoPost() throws JmriException, JsonException {
        TurnoutManager manager = InstanceManager.getDefault(TurnoutManager.class);
        Turnout turnout1 = manager.provideTurnout("IT1");
        JsonNode result;
        JsonNode message;
        turnout1.setState(Turnout.UNKNOWN);
        // set closed
        message = mapper.createObjectNode().put(JSON.NAME, "IT1").put(JSON.STATE, JSON.CLOSED);
        result = service.doPost(JsonTurnoutServiceFactory.TURNOUT, "IT1", message, locale, 42);
        assertEquals(Turnout.CLOSED, turnout1.getState());
        validate(result);
        assertEquals(JSON.CLOSED, result.path(JSON.DATA).path(JSON.STATE).asInt());
        // set thrown
        message = mapper.createObjectNode().put(JSON.NAME, "IT1").put(JSON.STATE, JSON.THROWN);
        result = service.doPost(JsonTurnoutServiceFactory.TURNOUT, "IT1", message, locale, 42);
        assertEquals(Turnout.THROWN, turnout1.getState());
        validate(result);
        assertEquals(JSON.THROWN, result.path(JSON.DATA).path(JSON.STATE).asInt());
        // set unknown - remains thrown
        message = mapper.createObjectNode().put(JSON.NAME, "IT1").put(JSON.STATE, JSON.UNKNOWN);
        result = service.doPost(JsonTurnoutServiceFactory.TURNOUT, "IT1", message, locale, 42);
        assertEquals(Turnout.THROWN, turnout1.getState());
        assertEquals(JSON.THROWN, result.path(JSON.DATA).path(JSON.STATE).asInt());
        // set inverted - becomes closed
        assertFalse(turnout1.getInverted());
        message = mapper.createObjectNode().put(JSON.NAME, "IT1").put(JSON.INVERTED, true);
        result = service.doPost(JsonTurnoutServiceFactory.TURNOUT, "IT1", message, locale, 42);
        assertTrue("Turnout is inverted", turnout1.getInverted());
        assertEquals(JSON.CLOSED, result.path(JSON.DATA).path(JSON.STATE).asInt());
        assertEquals(true, result.path(JSON.DATA).path(JSON.INVERTED).asBoolean());
        // reset inverted - becomes thrown
        message = mapper.createObjectNode().put(JSON.NAME, "IT1").put(JSON.INVERTED, false);
        result = service.doPost(JsonTurnoutServiceFactory.TURNOUT, "IT1", message, locale, 42);
        assertFalse("Turnout is not inverted", turnout1.getInverted());
        assertEquals(JSON.THROWN, result.path(JSON.DATA).path(JSON.STATE).asInt());
        // set invalid state
        message = mapper.createObjectNode().put(JSON.NAME, "IT1").put(JSON.STATE, 42); // Invalid value
        try {
            service.doPost(JsonTurnoutServiceFactory.TURNOUT, "IT1", message, locale, 42);
            fail("Expected exception not thrown");
        } catch (JsonException ex) {
            assertEquals(HttpServletResponse.SC_BAD_REQUEST, ex.getCode());
        }
        assertEquals(Turnout.THROWN, turnout1.getState());
    }

    @Test
    public void testDoPut() throws JsonException {
        TurnoutManager manager = InstanceManager.getDefault(TurnoutManager.class);
        JsonNode message;
        // add a turnout
        assertNull(manager.getTurnout("IT1"));
        message = mapper.createObjectNode().put(JSON.NAME, "IT1").put(JSON.STATE, Turnout.CLOSED);
        service.doPut(JsonTurnoutServiceFactory.TURNOUT, "IT1", message, locale, 42);
        assertNotNull(manager.getTurnout("IT1"));
    }

    @Test
    public void testDoGetList() throws JsonException {
        TurnoutManager manager = InstanceManager.getDefault(TurnoutManager.class);
        JsonNode result;
        result = service.doGetList(JsonTurnoutServiceFactory.TURNOUT, mapper.createObjectNode(), locale, 0);
        validate(result);
        assertEquals(0, result.size());
        manager.provideTurnout("IT1");
        result = service.doGetList(JsonTurnoutServiceFactory.TURNOUT, mapper.createObjectNode(), locale, 0);
        validate(result);
        assertEquals(1, result.size());
        manager.provideTurnout("IT2");
        result = service.doGetList(JsonTurnoutServiceFactory.TURNOUT, mapper.createObjectNode(), locale, 0);
        validate(result);
        assertEquals(2, result.size());
    }

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        service = new JsonTurnoutHttpService(mapper);
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initDebugThrottleManager();
    }

    @After
    @Override
    public void tearDown() throws Exception {
        JUnitUtil.tearDown();
    }

}
