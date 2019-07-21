package jmri.server.json.route;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Route;
import jmri.RouteManager;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.Turnout;
import jmri.TurnoutManager;
import jmri.server.json.JSON;
import jmri.server.json.JsonException;
import jmri.server.json.JsonNamedBeanHttpServiceTestBase;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Paul Bender
 * @author Randall Wood
 */
public class JsonRouteHttpServiceTest extends JsonNamedBeanHttpServiceTestBase<Route, JsonRouteHttpService>{

    private final static Logger log = LoggerFactory.getLogger(JsonRouteHttpServiceTest.class);

    @Test
    public void testDoGetWithRouteSensor() throws JmriException, JsonException {
        RouteManager manager = InstanceManager.getDefault(RouteManager.class);
        Route route1 = manager.provideRoute("IR1", "Route1");
        Sensor sensor1 = InstanceManager.getDefault(SensorManager.class).provideSensor("IS1");
        route1.setTurnoutsAlignedSensor(sensor1.getSystemName());
        JsonNode result;
        result = service.doGet(JsonRouteServiceFactory.ROUTE, "IR1", NullNode.getInstance(), locale, 42);
        validate(result);
        assertEquals(JsonRouteServiceFactory.ROUTE, result.path(JSON.TYPE).asText());
        assertEquals("IR1", result.path(JSON.DATA).path(JSON.NAME).asText());
        assertEquals(JSON.UNKNOWN, result.path(JSON.DATA).path(JSON.STATE).asInt());
        sensor1.setKnownState(Sensor.ACTIVE);
        result = service.doGet(JsonRouteServiceFactory.ROUTE, "IR1", NullNode.getInstance(), locale, 42);
        validate(result);
        assertEquals(JSON.ACTIVE, result.path(JSON.DATA).path(JSON.STATE).asInt());
        sensor1.setKnownState(Sensor.INACTIVE);
        result = service.doGet(JsonRouteServiceFactory.ROUTE, "IR1", NullNode.getInstance(), locale, 42);
        validate(result);
        assertEquals(JSON.INACTIVE, result.path(JSON.DATA).path(JSON.STATE).asInt());
    }

    @Test
    public void testDoGetWithoutRouteSensor() throws JmriException, JsonException {
        RouteManager manager = InstanceManager.getDefault(RouteManager.class);
        Route route1 = manager.provideRoute("IR1", "Route1");
        JsonNode result;
        result = service.doGet(JsonRouteServiceFactory.ROUTE, "IR1", NullNode.getInstance(), locale, 42);
        validate(result);
        assertEquals("IR1", result.path(JSON.DATA).path(JSON.NAME).asText());
        assertEquals(JSON.UNKNOWN, result.path(JSON.DATA).path(JSON.STATE).asInt());
        route1.setState(Sensor.ACTIVE);
        result = service.doGet(JsonRouteServiceFactory.ROUTE, "IR1", NullNode.getInstance(), locale, 42);
        validate(result);
        assertEquals(JSON.UNKNOWN, result.path(JSON.DATA).path(JSON.STATE).asInt());
        route1.setState(Sensor.INACTIVE);
        result = service.doGet(JsonRouteServiceFactory.ROUTE, "IR1", NullNode.getInstance(), locale, 42);
        validate(result);
        assertEquals(JSON.UNKNOWN, result.path(JSON.DATA).path(JSON.STATE).asInt());
    }

    @Test
    public void testDoPostWithRouteSensor() throws JmriException, JsonException {
        log.debug("testDoPostWithRouteSensor");
        RouteManager manager = InstanceManager.getDefault(RouteManager.class);
        Route route1 = manager.provideRoute("IR1", "Route1");
        Sensor sensor1 = InstanceManager.getDefault(SensorManager.class).provideSensor("IS1");
        Turnout turnout1 = InstanceManager.getDefault(TurnoutManager.class).provideTurnout("IT1");
        turnout1.setCommandedState(Turnout.CLOSED);
        route1.setTurnoutsAlignedSensor(sensor1.getSystemName());
        assertTrue(route1.addOutputTurnout(turnout1.getSystemName(), Turnout.THROWN));
        route1.activateRoute();
        assertNotNull(route1.getTurnoutsAlgdSensor());
        JsonNode result;
        JsonNode message;
        // set ACTIVE
        message = mapper.createObjectNode().put(JSON.NAME, "IR1").put(JSON.STATE, JSON.ACTIVE);
        assertEquals(Sensor.INACTIVE, route1.getState());
        result = service.doPost(JsonRouteServiceFactory.ROUTE, "IR1", message, locale, 42);
        JUnitUtil.waitFor(() -> {
            return route1.getState() == Sensor.ACTIVE;
        }, "Route to activate");
        assertEquals(Sensor.ACTIVE, route1.getState());
        validate(result);
        // set INACTIVE - remains ACTIVE
        message = mapper.createObjectNode().put(JSON.NAME, "IR1").put(JSON.STATE, JSON.INACTIVE);
        result = service.doPost(JsonRouteServiceFactory.ROUTE, "IR1", message, locale, 42);
        JUnitUtil.waitFor(() -> {
            return route1.getState() == Sensor.ACTIVE;
        }, "Route to activate");
        assertEquals(Sensor.ACTIVE, route1.getState());
        // not testing results content because result *may* be set before route state changes
        // so its not predictable (this is not unanticipated in the design)
        validate(result);
        // set UNKNOWN - remains ACTIVE
        message = mapper.createObjectNode().put(JSON.NAME, "IR1").put(JSON.STATE, JSON.UNKNOWN);
        result = service.doPost(JsonRouteServiceFactory.ROUTE, "IR1", message, locale, 42);
        JUnitUtil.waitFor(() -> {
            return route1.getState() == Sensor.ACTIVE;
        }, "Route to activate");
        assertEquals(Sensor.ACTIVE, route1.getState());
        validate(result);
        // reset to INACTIVE
        turnout1.setCommandedState(Turnout.CLOSED);
        JUnitUtil.waitFor(() -> {
            return route1.getState() == Sensor.INACTIVE;
        }, "Route to activate");
        assertEquals(Sensor.INACTIVE, route1.getState());
        // set TOGGLE - becomes ACTIVE
        log.debug("Toggling route in testDoPostWithRouteSensor");
        message = mapper.createObjectNode().put(JSON.NAME, "IR1").put(JSON.STATE, JSON.TOGGLE);
        result = service.doPost(JsonRouteServiceFactory.ROUTE, "IR1", message, locale, 42);
        JUnitUtil.waitFor(() -> {
            return route1.getState() == Sensor.ACTIVE;
        }, "Route to activate");
        assertEquals(Sensor.ACTIVE, route1.getState());
        validate(result);
        // set TOGGLE - remains ACTIVE
        message = mapper.createObjectNode().put(JSON.NAME, "IR1").put(JSON.STATE, JSON.TOGGLE);
        result = service.doPost(JsonRouteServiceFactory.ROUTE, "IR1", message, locale, 42);
        JUnitUtil.waitFor(() -> {
            return route1.getState() == Sensor.ACTIVE;
        }, "Route to activate");
        assertEquals(Sensor.ACTIVE, route1.getState());
        validate(result);
        // set invalid state
        message = mapper.createObjectNode().put(JSON.NAME, "IR1").put(JSON.STATE, 42); // Invalid value
        try {
            service.doPost(JsonRouteServiceFactory.ROUTE, "IR1", message, locale, 42);
            fail("Expected exception not thrown.");
        } catch (JsonException ex) {
            assertEquals(400, ex.getCode());
        }
        assertEquals(Sensor.ACTIVE, route1.getState());
    }

    @Test
    public void testDoPostWithoutRouteSensor() throws JmriException, JsonException {
        RouteManager manager = InstanceManager.getDefault(RouteManager.class);
        Route route1 = manager.provideRoute("IR1", "Route1");
        assertNull(route1.getTurnoutsAlgdSensor());
        JsonNode result;
        JsonNode message;
        // set off
        message = mapper.createObjectNode().put(JSON.NAME, "IR1").put(JSON.STATE, JSON.INACTIVE);
        result = service.doPost(JsonRouteServiceFactory.ROUTE, "IR1", message, locale, 42);
        validate(result);
        assertEquals(JSON.UNKNOWN, result.path(JSON.DATA).path(JSON.STATE).asInt());
        // set on
        message = mapper.createObjectNode().put(JSON.NAME, "IR1").put(JSON.STATE, JSON.ACTIVE);
        result = service.doPost(JsonRouteServiceFactory.ROUTE, "IR1", message, locale, 42);
        validate(result);
        assertEquals(JSON.UNKNOWN, result.path(JSON.DATA).path(JSON.STATE).asInt());
        // set unknown
        message = mapper.createObjectNode().put(JSON.NAME, "IR1").put(JSON.STATE, JSON.UNKNOWN);
        result = service.doPost(JsonRouteServiceFactory.ROUTE, "IR1", message, locale, 42);
        assertEquals(JSON.UNKNOWN, result.path(JSON.DATA).path(JSON.STATE).asInt());
        // set toggle
        message = mapper.createObjectNode().put(JSON.NAME, "IR1").put(JSON.STATE, JSON.TOGGLE);
        result = service.doPost(JsonRouteServiceFactory.ROUTE, "IR1", message, locale, 42);
        assertEquals(JSON.UNKNOWN, result.path(JSON.DATA).path(JSON.STATE).asInt());
        // set invalid state
        message = mapper.createObjectNode().put(JSON.NAME, "IR1").put(JSON.STATE, 42); // Invalid value
        try {
            service.doPost(JsonRouteServiceFactory.ROUTE, "IR1", message, locale, 42);
            fail("Expected exception not thrown.");
        } catch (JsonException ex) {
            assertEquals(400, ex.getCode());
        }
    }

    @Test
    public void testDoPut() {
        RouteManager manager = InstanceManager.getDefault(RouteManager.class);
        JsonNode message;
        assertNull(manager.getRoute("IR1"));
        try {
            // add a route
            message = mapper.createObjectNode().put(JSON.NAME, "IR1").put(JSON.STATE, Sensor.INACTIVE);
            service.doPut(JsonRouteServiceFactory.ROUTE, "IR1", message, locale, 42);
            fail("Expected exception not thrown.");
        } catch (JsonException ex) {
            assertEquals(405, ex.getCode());
        }
        assertNull(manager.getRoute("IR1"));
    }

    @Test
    public void testDoGetList() throws JsonException {
        RouteManager manager = InstanceManager.getDefault(RouteManager.class);
        JsonNode result;
        result = service.doGetList(JsonRouteServiceFactory.ROUTE, mapper.createObjectNode(), locale, 0);
        validate(result);
        assertEquals(0, result.size());
        manager.provideRoute("IR1", "Route1");
        manager.provideRoute("IR2", "Route2");
        result = service.doGetList(JsonRouteServiceFactory.ROUTE, mapper.createObjectNode(), locale, 0);
        validate(result);
        assertEquals(2, result.size());
    }

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        service = new JsonRouteHttpService(mapper);
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initRouteManager();
        JUnitUtil.initDebugThrottleManager();
    }

    @After
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

}
