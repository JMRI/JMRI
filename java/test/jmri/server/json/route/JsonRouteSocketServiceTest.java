package jmri.server.json.route;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.DataOutputStream;
import java.io.IOException;
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
public class JsonRouteSocketServiceTest {

    @Test
    public void testRouteChange() {
        // only test with route with sensor - there are no means for getting
        // the route state on a sensorless route
        try {
            JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
            JsonNode message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "IR1");
            JsonRouteSocketService service = new JsonRouteSocketService(connection);
            RouteManager manager = InstanceManager.getDefault(RouteManager.class);
            Route route1 = manager.provideRoute("IR1", "Route1");
            Sensor sensor1 = InstanceManager.getDefault(SensorManager.class).provideSensor("IS1");
            sensor1.setKnownState(Sensor.UNKNOWN);
            route1.setTurnoutsAlignedSensor(sensor1.getSystemName());
            service.onMessage(JsonRouteServiceFactory.ROUTE, message, JSON.POST, Locale.ENGLISH);
            // TODO: test that service is listener in RouteManager
            Assert.assertEquals(JSON.UNKNOWN, connection.getMessage().path(JSON.DATA).path(JSON.STATE).asInt());
            sensor1.setKnownState(Sensor.ACTIVE);
            JUnitUtil.waitFor(() -> {
                return route1.getState() == Sensor.ACTIVE;
            }, "Route to activate");
            Assert.assertEquals(JSON.ACTIVE, connection.getMessage().path(JSON.DATA).path(JSON.STATE).asInt());
            sensor1.setKnownState(Sensor.INACTIVE);
            JUnitUtil.waitFor(() -> {
                return route1.getState() == Sensor.INACTIVE;
            }, "Route to deactivate");
            Assert.assertEquals(Sensor.INACTIVE, route1.getState());
            Assert.assertEquals(JSON.INACTIVE, connection.getMessage().path(JSON.DATA).path(JSON.STATE).asInt());
            service.onClose();
            // TODO: test that service is no longer a listener in RouteManager
        } catch (IOException | JmriException | JsonException ex) {
            Assert.fail(ex.getMessage());
        }
    }

    @Test
    public void testOnMessageChange() {
        // only test with route with sensor - there are no means for getting
        // the route state on a sensorless route
        try {
            JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
            JsonNode message;
            JsonRouteSocketService service = new JsonRouteSocketService(connection);
            RouteManager manager = InstanceManager.getDefault(RouteManager.class);
            Route route1 = manager.provideRoute("IR1", "Route1");
            Sensor sensor1 = InstanceManager.getDefault(SensorManager.class).provideSensor("IS1");
            Turnout turnout1 = InstanceManager.getDefault(TurnoutManager.class).provideTurnout("IT1");
            turnout1.setCommandedState(Turnout.CLOSED);
            route1.setTurnoutsAlignedSensor(sensor1.getSystemName());
            Assert.assertTrue(route1.addOutputTurnout(turnout1.getSystemName(), Turnout.THROWN));
            route1.activateRoute();
            Assert.assertNotNull(route1.getTurnoutsAlgdSensor());
            // Route ACTIVE - becomes ACTIVE
            message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "IR1").put(JSON.STATE, JSON.ACTIVE);
            service.onMessage(JsonRouteServiceFactory.ROUTE, message, JSON.POST, Locale.ENGLISH);
            JUnitUtil.waitFor(() -> {
                return route1.getState() == Sensor.ACTIVE;
            }, "Route to activate");
            Assert.assertEquals(Sensor.ACTIVE, route1.getState());
            // Route INACTIVE - remains ACTIVE
            message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "IR1").put(JSON.STATE, JSON.INACTIVE);
            service.onMessage(JsonRouteServiceFactory.ROUTE, message, JSON.POST, Locale.ENGLISH);
            Assert.assertEquals(Sensor.ACTIVE, route1.getState());
            // Route UNKNOWN - remains ACTIVE
            message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "IR1").put(JSON.STATE, JSON.UNKNOWN);
            service.onMessage(JsonRouteServiceFactory.ROUTE, message, JSON.POST, Locale.ENGLISH);
            JUnitUtil.waitFor(() -> {
                return route1.getState() == Sensor.ACTIVE;
            }, "Route to activate");
            Assert.assertEquals(Sensor.ACTIVE, route1.getState());
            // Route TOGGLE - remains ACTIVE
            message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "IR1").put(JSON.STATE, JSON.TOGGLE);
            service.onMessage(JsonRouteServiceFactory.ROUTE, message, JSON.POST, Locale.ENGLISH);
            JUnitUtil.waitFor(() -> {
                return route1.getState() == Sensor.ACTIVE;
            }, "Route to activate");
            Assert.assertEquals(Sensor.ACTIVE, route1.getState());
            // Route TOGGLE - becomes ACTIVE
            sensor1.setKnownState(Sensor.INACTIVE);
            message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "IR1").put(JSON.STATE, JSON.TOGGLE);
            service.onMessage(JsonRouteServiceFactory.ROUTE, message, JSON.POST, Locale.ENGLISH);
            JUnitUtil.waitFor(() -> {
                return route1.getState() == Sensor.ACTIVE;
            }, "Route to activate");
            Assert.assertEquals(Sensor.ACTIVE, route1.getState());
            // Route Invalid State
            message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "IR1").put(JSON.STATE, 42); // invalid state
            JsonException exception = null;
            try {
                service.onMessage(JsonRouteServiceFactory.ROUTE, message, JSON.POST, Locale.ENGLISH);
            } catch (JsonException ex) {
                exception = ex;
            }
            Assert.assertEquals(Sensor.ACTIVE, route1.getState());
            Assert.assertNotNull(exception);
            Assert.assertEquals(HttpServletResponse.SC_BAD_REQUEST, exception.getCode());
        } catch (IOException | JmriException | JsonException ex) {
            Assert.fail(ex.getMessage());
        }
    }

    @Before
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initRouteManager();
    }

    @After
    public void tearDown() throws Exception {
        JUnitUtil.tearDown();
    }

}
