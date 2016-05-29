package jmri.server.json.route;

import apps.tests.Log4JFixture;
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
public class JsonRouteSocketServiceTest extends TestCase {

    public void testCtorSuccess() {
        JsonRouteSocketService service = new JsonRouteSocketService(new JsonMockConnection((DataOutputStream) null));
        Assert.assertNotNull(service);
    }

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
            service.onMessage(JsonRouteServiceFactory.ROUTE, message, Locale.ENGLISH);
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
            service.onMessage(JsonRouteServiceFactory.ROUTE, message, Locale.ENGLISH);
            JUnitUtil.waitFor(() -> {
                return route1.getState() == Sensor.ACTIVE;
            }, "Route to activate");
            Assert.assertEquals(Sensor.ACTIVE, route1.getState());
            // Route INACTIVE - remains ACTIVE
            message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "IR1").put(JSON.STATE, JSON.INACTIVE);
            service.onMessage(JsonRouteServiceFactory.ROUTE, message, Locale.ENGLISH);
            Assert.assertEquals(Sensor.ACTIVE, route1.getState());
            // Route UNKNOWN - remains ACTIVE
            message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "IR1").put(JSON.STATE, JSON.UNKNOWN);
            service.onMessage(JsonRouteServiceFactory.ROUTE, message, Locale.ENGLISH);
            JUnitUtil.waitFor(() -> {
                return route1.getState() == Sensor.ACTIVE;
            }, "Route to activate");
            Assert.assertEquals(Sensor.ACTIVE, route1.getState());
            // Route TOGGLE - remains ACTIVE
            message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "IR1").put(JSON.STATE, JSON.TOGGLE);
            service.onMessage(JsonRouteServiceFactory.ROUTE, message, Locale.ENGLISH);
            JUnitUtil.waitFor(() -> {
                return route1.getState() == Sensor.ACTIVE;
            }, "Route to activate");
            Assert.assertEquals(Sensor.ACTIVE, route1.getState());
            // Route TOGGLE - becomes ACTIVE
            sensor1.setKnownState(Sensor.INACTIVE);
            message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "IR1").put(JSON.STATE, JSON.TOGGLE);
            service.onMessage(JsonRouteServiceFactory.ROUTE, message, Locale.ENGLISH);
            JUnitUtil.waitFor(() -> {
                return route1.getState() == Sensor.ACTIVE;
            }, "Route to activate");
            Assert.assertEquals(Sensor.ACTIVE, route1.getState());
            // Route Invalid State
            message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "IR1").put(JSON.STATE, 42); // invalid state
            JsonException exception = null;
            try {
                service.onMessage(JsonRouteServiceFactory.ROUTE, message, Locale.ENGLISH);
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

    // from here down is testing infrastructure
    public JsonRouteSocketServiceTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {JsonRouteSocketServiceTest.class.getName()};
        TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(JsonRouteSocketServiceTest.class);

        return suite;
    }

    // The minimal setup for log4J
    @Override
    protected void setUp() throws Exception {
        Log4JFixture.setUp();
        super.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initRouteManager();
    }

    @Override
    protected void tearDown() throws Exception {
        JUnitUtil.resetInstanceManager();
        super.tearDown();
        Log4JFixture.tearDown();
    }

}
