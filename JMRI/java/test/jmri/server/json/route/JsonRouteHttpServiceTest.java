package jmri.server.json.route;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Locale;
import javax.servlet.http.HttpServletResponse;
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
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Paul Bender
 * @author Randall Wood
 */
public class JsonRouteHttpServiceTest {

    private final static Logger log = LoggerFactory.getLogger(JsonRouteHttpServiceTest.class);

    @Test
    public void testDoGetWithRouteSensor() throws JmriException {
        JsonRouteHttpService service = new JsonRouteHttpService(new ObjectMapper());
        RouteManager manager = InstanceManager.getDefault(RouteManager.class);
        Route route1 = manager.provideRoute("IR1", "Route1");
        Sensor sensor1 = InstanceManager.getDefault(SensorManager.class).provideSensor("IS1");
        route1.setTurnoutsAlignedSensor(sensor1.getSystemName());
        JsonNode result;
        try {
            result = service.doGet(JsonRouteServiceFactory.ROUTE, "IR1", Locale.ENGLISH);
            Assert.assertNotNull(result);
            Assert.assertEquals(JsonRouteServiceFactory.ROUTE, result.path(JSON.TYPE).asText());
            Assert.assertEquals("IR1", result.path(JSON.DATA).path(JSON.NAME).asText());
            Assert.assertEquals(JSON.UNKNOWN, result.path(JSON.DATA).path(JSON.STATE).asInt());
            sensor1.setKnownState(Sensor.ACTIVE);
            result = service.doGet(JsonRouteServiceFactory.ROUTE, "IR1", Locale.ENGLISH);
            Assert.assertNotNull(result);
            Assert.assertEquals(JSON.ACTIVE, result.path(JSON.DATA).path(JSON.STATE).asInt());
            sensor1.setKnownState(Sensor.INACTIVE);
            result = service.doGet(JsonRouteServiceFactory.ROUTE, "IR1", Locale.ENGLISH);
            Assert.assertNotNull(result);
            Assert.assertEquals(JSON.INACTIVE, result.path(JSON.DATA).path(JSON.STATE).asInt());
        } catch (JsonException ex) {
            Assert.fail(ex.getMessage());
        }
    }

    @Test
    public void testDoGetWithoutRouteSensor() throws JmriException {
        JsonRouteHttpService service = new JsonRouteHttpService(new ObjectMapper());
        RouteManager manager = InstanceManager.getDefault(RouteManager.class);
        Route route1 = manager.provideRoute("IR1", "Route1");
        JsonNode result;
        try {
            result = service.doGet(JsonRouteServiceFactory.ROUTE, "IR1", Locale.ENGLISH);
            Assert.assertNotNull(result);
            Assert.assertEquals("IR1", result.path(JSON.DATA).path(JSON.NAME).asText());
            Assert.assertEquals(JSON.UNKNOWN, result.path(JSON.DATA).path(JSON.STATE).asInt());
            route1.setState(Sensor.ACTIVE);
            result = service.doGet(JsonRouteServiceFactory.ROUTE, "IR1", Locale.ENGLISH);
            Assert.assertNotNull(result);
            Assert.assertEquals(JSON.UNKNOWN, result.path(JSON.DATA).path(JSON.STATE).asInt());
            route1.setState(Sensor.INACTIVE);
            result = service.doGet(JsonRouteServiceFactory.ROUTE, "IR1", Locale.ENGLISH);
            Assert.assertNotNull(result);
            Assert.assertEquals(JSON.UNKNOWN, result.path(JSON.DATA).path(JSON.STATE).asInt());
        } catch (JsonException ex) {
            Assert.fail(ex.getMessage());
        }
    }

    @Test
    public void testDoPostWithRouteSensor() throws JmriException {
        log.debug("testDoPostWithRouteSensor");
        ObjectMapper mapper = new ObjectMapper();
        JsonRouteHttpService service = new JsonRouteHttpService(mapper);
        RouteManager manager = InstanceManager.getDefault(RouteManager.class);
        Route route1 = manager.provideRoute("IR1", "Route1");
        Sensor sensor1 = InstanceManager.getDefault(SensorManager.class).provideSensor("IS1");
        Turnout turnout1 = InstanceManager.getDefault(TurnoutManager.class).provideTurnout("IT1");
        turnout1.setCommandedState(Turnout.CLOSED);
        route1.setTurnoutsAlignedSensor(sensor1.getSystemName());
        Assert.assertTrue(route1.addOutputTurnout(turnout1.getSystemName(), Turnout.THROWN));
        route1.activateRoute();
        Assert.assertNotNull(route1.getTurnoutsAlgdSensor());
        JsonNode result;
        JsonNode message;
        try {
            // set ACTIVE
            message = mapper.createObjectNode().put(JSON.NAME, "IR1").put(JSON.STATE, JSON.ACTIVE);
            Assert.assertEquals(Sensor.INACTIVE, route1.getState());
            result = service.doPost(JsonRouteServiceFactory.ROUTE, "IR1", message, Locale.ENGLISH);
            JUnitUtil.waitFor(() -> {
                return route1.getState() == Sensor.ACTIVE;
            }, "Route to activate");
            Assert.assertEquals(Sensor.ACTIVE, route1.getState());
            Assert.assertNotNull(result);
            // set INACTIVE - remains ACTIVE
            message = mapper.createObjectNode().put(JSON.NAME, "IR1").put(JSON.STATE, JSON.INACTIVE);
            result = service.doPost(JsonRouteServiceFactory.ROUTE, "IR1", message, Locale.ENGLISH);
            JUnitUtil.waitFor(() -> {
                return route1.getState() == Sensor.ACTIVE;
            }, "Route to activate");
            Assert.assertEquals(Sensor.ACTIVE, route1.getState());
            // not testing results content because result *may* be set before route state changes
            // so its not predictable (this is not unanticipated in the design)
            Assert.assertNotNull(result);
            // set UNKNOWN - remains ACTIVE
            message = mapper.createObjectNode().put(JSON.NAME, "IR1").put(JSON.STATE, JSON.UNKNOWN);
            result = service.doPost(JsonRouteServiceFactory.ROUTE, "IR1", message, Locale.ENGLISH);
            JUnitUtil.waitFor(() -> {
                return route1.getState() == Sensor.ACTIVE;
            }, "Route to activate");
            Assert.assertEquals(Sensor.ACTIVE, route1.getState());
            Assert.assertNotNull(result);
            // reset to INACTIVE
            turnout1.setCommandedState(Turnout.CLOSED);
            JUnitUtil.waitFor(() -> {
                return route1.getState() == Sensor.INACTIVE;
            }, "Route to activate");
            Assert.assertEquals(Sensor.INACTIVE, route1.getState());
            // set TOGGLE - becomes ACTIVE
            log.debug("Toggling route in testDoPostWithRouteSensor");
            message = mapper.createObjectNode().put(JSON.NAME, "IR1").put(JSON.STATE, JSON.TOGGLE);
            result = service.doPost(JsonRouteServiceFactory.ROUTE, "IR1", message, Locale.ENGLISH);
            JUnitUtil.waitFor(() -> {
                return route1.getState() == Sensor.ACTIVE;
            }, "Route to activate");
            Assert.assertEquals(Sensor.ACTIVE, route1.getState());
            Assert.assertNotNull(result);
            // set TOGGLE - remains ACTIVE
            message = mapper.createObjectNode().put(JSON.NAME, "IR1").put(JSON.STATE, JSON.TOGGLE);
            result = service.doPost(JsonRouteServiceFactory.ROUTE, "IR1", message, Locale.ENGLISH);
            JUnitUtil.waitFor(() -> {
                return route1.getState() == Sensor.ACTIVE;
            }, "Route to activate");
            Assert.assertEquals(Sensor.ACTIVE, route1.getState());
            Assert.assertNotNull(result);
            // set invalid state
            message = mapper.createObjectNode().put(JSON.NAME, "IR1").put(JSON.STATE, 42); // Invalid value
            JsonException exception = null;
            try {
                service.doPost(JsonRouteServiceFactory.ROUTE, "IR1", message, Locale.ENGLISH);
            } catch (JsonException ex) {
                exception = ex;
            }
            Assert.assertEquals(Sensor.ACTIVE, route1.getState());
            Assert.assertNotNull(exception);
            Assert.assertEquals(HttpServletResponse.SC_BAD_REQUEST, exception.getCode());
        } catch (JsonException ex) {
            Assert.fail(ex.getMessage());
        }
    }

    @Test
    public void testDoPostWithoutRouteSensor() throws JmriException {
        ObjectMapper mapper = new ObjectMapper();
        JsonRouteHttpService service = new JsonRouteHttpService(mapper);
        RouteManager manager = InstanceManager.getDefault(RouteManager.class);
        Route route1 = manager.provideRoute("IR1", "Route1");
        Assert.assertNull(route1.getTurnoutsAlgdSensor());
        JsonNode result;
        JsonNode message;
        try {
            // set off
            message = mapper.createObjectNode().put(JSON.NAME, "IR1").put(JSON.STATE, JSON.INACTIVE);
            result = service.doPost(JsonRouteServiceFactory.ROUTE, "IR1", message, Locale.ENGLISH);
            Assert.assertNotNull(result);
            Assert.assertEquals(JSON.UNKNOWN, result.path(JSON.DATA).path(JSON.STATE).asInt());
            // set on
            message = mapper.createObjectNode().put(JSON.NAME, "IR1").put(JSON.STATE, JSON.ACTIVE);
            result = service.doPost(JsonRouteServiceFactory.ROUTE, "IR1", message, Locale.ENGLISH);
            Assert.assertNotNull(result);
            Assert.assertEquals(JSON.UNKNOWN, result.path(JSON.DATA).path(JSON.STATE).asInt());
            // set unknown
            message = mapper.createObjectNode().put(JSON.NAME, "IR1").put(JSON.STATE, JSON.UNKNOWN);
            result = service.doPost(JsonRouteServiceFactory.ROUTE, "IR1", message, Locale.ENGLISH);
            Assert.assertEquals(JSON.UNKNOWN, result.path(JSON.DATA).path(JSON.STATE).asInt());
            // set toggle
            message = mapper.createObjectNode().put(JSON.NAME, "IR1").put(JSON.STATE, JSON.TOGGLE);
            result = service.doPost(JsonRouteServiceFactory.ROUTE, "IR1", message, Locale.ENGLISH);
            Assert.assertEquals(JSON.UNKNOWN, result.path(JSON.DATA).path(JSON.STATE).asInt());
            // set invalid state
            message = mapper.createObjectNode().put(JSON.NAME, "IR1").put(JSON.STATE, 42); // Invalid value
            JsonException exception = null;
            try {
                service.doPost(JsonRouteServiceFactory.ROUTE, "IR1", message, Locale.ENGLISH);
            } catch (JsonException ex) {
                exception = ex;
            }
            Assert.assertNotNull(exception);
            Assert.assertEquals(HttpServletResponse.SC_BAD_REQUEST, exception.getCode());
        } catch (JsonException ex) {
            Assert.fail(ex.getMessage());
        }
    }

    @Test
    public void testDoPut() {
        ObjectMapper mapper = new ObjectMapper();
        JsonRouteHttpService service = new JsonRouteHttpService(mapper);
        RouteManager manager = InstanceManager.getDefault(RouteManager.class);
        JsonNode message;
        JsonException exception = null;
        try {
            // add a route
            Assert.assertNull(manager.getRoute("IR1"));
            message = mapper.createObjectNode().put(JSON.NAME, "IR1").put(JSON.STATE, Sensor.INACTIVE);
            service.doPut(JsonRouteServiceFactory.ROUTE, "IR1", message, Locale.ENGLISH);
            Assert.assertNull(manager.getRoute("IR1"));
        } catch (JsonException ex) {
            exception = ex;
        }
        Assert.assertNotNull(exception);
        Assert.assertEquals(HttpServletResponse.SC_METHOD_NOT_ALLOWED, exception.getCode());
    }

    @Test
    public void testDoGetList() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonRouteHttpService service = new JsonRouteHttpService(mapper);
            RouteManager manager = InstanceManager.getDefault(RouteManager.class);
            JsonNode result;
            result = service.doGetList(JsonRouteServiceFactory.ROUTE, Locale.ENGLISH);
            Assert.assertNotNull(result);
            Assert.assertEquals(0, result.size());
            manager.provideRoute("IR1", "Route1");
            manager.provideRoute("IR2", "Route2");
            result = service.doGetList(JsonRouteServiceFactory.ROUTE, Locale.ENGLISH);
            Assert.assertNotNull(result);
            Assert.assertEquals(2, result.size());
        } catch (JsonException ex) {
            Assert.fail(ex.getMessage());
        }
    }

    @Test
    public void testDelete() {
        try {
            (new JsonRouteHttpService(new ObjectMapper())).doDelete(JsonRouteServiceFactory.ROUTE, "", Locale.ENGLISH);
        } catch (JsonException ex) {
            Assert.assertEquals(HttpServletResponse.SC_METHOD_NOT_ALLOWED, ex.getCode());
            return;
        }
        Assert.fail("Did not throw expected error.");
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initRouteManager();
        JUnitUtil.initDebugThrottleManager();
    }

    @After
    public void tearDown() throws Exception {
        JUnitUtil.tearDown();
    }

}
