package jmri.server.json.turnout;

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
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender
 * @author Randall Wood
 */
public class JsonTurnoutHttpServiceTest {

    @Test
    public void testDoGet() throws JmriException {
        JsonTurnoutHttpService service = new JsonTurnoutHttpService(new ObjectMapper());
        TurnoutManager manager = InstanceManager.getDefault(TurnoutManager.class);
        Turnout turnout1 = manager.provideTurnout("IT1");
        JsonNode result;
        try {
            turnout1.setState(Turnout.UNKNOWN);
            result = service.doGet(JsonTurnoutServiceFactory.TURNOUT, "IT1",
                    service.getObjectMapper().createObjectNode(), Locale.ENGLISH);
            Assert.assertNotNull(result);
            Assert.assertEquals(JsonTurnoutServiceFactory.TURNOUT, result.path(JSON.TYPE).asText());
            Assert.assertEquals("IT1", result.path(JSON.DATA).path(JSON.NAME).asText());
            Assert.assertEquals(JSON.UNKNOWN, result.path(JSON.DATA).path(JSON.STATE).asInt());
            turnout1.setState(Turnout.CLOSED);
            result = service.doGet(JsonTurnoutServiceFactory.TURNOUT, "IT1",
                    service.getObjectMapper().createObjectNode(), Locale.ENGLISH);
            Assert.assertNotNull(result);
            Assert.assertEquals(JSON.CLOSED, result.path(JSON.DATA).path(JSON.STATE).asInt());
            turnout1.setState(Turnout.THROWN);
            result = service.doGet(JsonTurnoutServiceFactory.TURNOUT, "IT1",
                    service.getObjectMapper().createObjectNode(), Locale.ENGLISH);
            Assert.assertNotNull(result);
            Assert.assertEquals(JSON.THROWN, result.path(JSON.DATA).path(JSON.STATE).asInt());
            turnout1.setState(Turnout.INCONSISTENT);
            result = service.doGet(JsonTurnoutServiceFactory.TURNOUT, "IT1",
                    service.getObjectMapper().createObjectNode(), Locale.ENGLISH);
            Assert.assertNotNull(result);
            Assert.assertEquals(JSON.INCONSISTENT, result.path(JSON.DATA).path(JSON.STATE).asInt());
        } catch (JsonException ex) {
            Assert.fail(ex.getMessage());
        }
    }

    @Test
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
            // set inverted - becomes closed
            Assert.assertFalse(turnout1.getInverted());
            message = mapper.createObjectNode().put(JSON.NAME, "IT1").put(JSON.INVERTED, true);
            result = service.doPost(JsonTurnoutServiceFactory.TURNOUT, "IT1", message, Locale.ENGLISH);
            Assert.assertTrue("Turnout is inverted", turnout1.getInverted());
            Assert.assertEquals(JSON.CLOSED, result.path(JSON.DATA).path(JSON.STATE).asInt());
            Assert.assertEquals(true, result.path(JSON.DATA).path(JSON.INVERTED).asBoolean());
            // reset inverted - becomes thrown
            message = mapper.createObjectNode().put(JSON.NAME, "IT1").put(JSON.INVERTED, false);
            result = service.doPost(JsonTurnoutServiceFactory.TURNOUT, "IT1", message, Locale.ENGLISH);
            Assert.assertFalse("Turnout is not inverted", turnout1.getInverted());
            Assert.assertEquals(JSON.THROWN, result.path(JSON.DATA).path(JSON.STATE).asInt());
            // set invalid state
            message = mapper.createObjectNode().put(JSON.NAME, "IT1").put(JSON.STATE, 42); // Invalid value
            try {
                service.doPost(JsonTurnoutServiceFactory.TURNOUT, "IT1", message, Locale.ENGLISH);
                Assert.fail("Expected exception not thrown");
            } catch (JsonException ex) {
                Assert.assertEquals(HttpServletResponse.SC_BAD_REQUEST, ex.getCode());
            }
            Assert.assertEquals(Turnout.THROWN, turnout1.getState());
        } catch (JsonException ex) {
            Assert.fail(ex.getMessage());
        }
    }

    @Test
    public void testDoPut() {
        ObjectMapper mapper = new ObjectMapper();
        JsonTurnoutHttpService service = new JsonTurnoutHttpService(mapper);
        TurnoutManager manager = InstanceManager.getDefault(TurnoutManager.class);
        JsonNode message;
        try {
            // add a turnout
            Assert.assertNull(manager.getTurnout("IT1"));
            message = mapper.createObjectNode().put(JSON.NAME, "IT1").put(JSON.STATE, Turnout.CLOSED);
            service.doPut(JsonTurnoutServiceFactory.TURNOUT, "IT1", message, Locale.ENGLISH);
            Assert.assertNotNull(manager.getTurnout("IT1"));
        } catch (JsonException ex) {
            Assert.fail(ex.getMessage());
        }
    }

    @Test
    public void testDoGetList() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonTurnoutHttpService service = new JsonTurnoutHttpService(mapper);
            TurnoutManager manager = InstanceManager.getDefault(TurnoutManager.class);
            JsonNode result;
            result = service.doGetList(JsonTurnoutServiceFactory.TURNOUT, mapper.createObjectNode(), Locale.ENGLISH);
            Assert.assertNotNull(result);
            Assert.assertEquals(0, result.size());
            manager.provideTurnout("IT1");
            result = service.doGetList(JsonTurnoutServiceFactory.TURNOUT, mapper.createObjectNode(), Locale.ENGLISH);
            Assert.assertNotNull(result);
            Assert.assertEquals(1, result.size());
            manager.provideTurnout("IT2");
            result = service.doGetList(JsonTurnoutServiceFactory.TURNOUT, mapper.createObjectNode(), Locale.ENGLISH);
            Assert.assertNotNull(result);
            Assert.assertEquals(2, result.size());
        } catch (JsonException ex) {
            Assert.fail(ex.getMessage());
        }
    }

    @Test
    public void testDelete() {
        try {
            (new JsonTurnoutHttpService(new ObjectMapper())).doDelete(JsonTurnoutServiceFactory.TURNOUT, "", Locale.ENGLISH);
        } catch (JsonException ex) {
            Assert.assertEquals(HttpServletResponse.SC_METHOD_NOT_ALLOWED, ex.getCode());
            return;
        }
        Assert.fail("Did not throw expected error.");
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initDebugThrottleManager();
    }

    @After
    public void tearDown() throws Exception {
        JUnitUtil.tearDown();
    }

}
