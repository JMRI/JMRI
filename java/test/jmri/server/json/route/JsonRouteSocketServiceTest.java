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
import jmri.server.json.JsonRequest;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 * @author Paul Bender
 * @author Randall Wood
 */
public class JsonRouteSocketServiceTest {

    private Locale locale = Locale.ENGLISH;

    @Test
    public void testRouteChange() throws IOException, JmriException, JsonException {
        // only test with route with sensor - there are no means for getting
        // the route state on a route with a null sensor

        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        JsonNode message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "IO1");
        RouteManager manager = InstanceManager.getDefault(RouteManager.class);
        Route route1 = manager.provideRoute("IO1", "Route1");
        Sensor sensor1 = InstanceManager.getDefault(SensorManager.class).provideSensor("IS1");
        sensor1.setKnownState(Sensor.UNKNOWN);
        route1.setTurnoutsAlignedSensor(sensor1.getSystemName());
        assertEquals( 1, route1.getNumPropertyChangeListeners(), "IO1 has one listener");
        assertEquals( 1, sensor1.getNumPropertyChangeListeners(), "IS1 has one listener");
        JsonRouteSocketService service = new JsonRouteSocketService(connection);
        service.onMessage(JsonRouteServiceFactory.ROUTE, message, new JsonRequest(locale, JSON.V5, JSON.POST, 42));
        assertEquals( 2, route1.getNumPropertyChangeListeners(), "IO1 has two listeners");
        // TODO: test that service is listener in RouteManager
        message = connection.getMessage();
        assertNotNull(message);
        assertEquals(JSON.UNKNOWN, message.path(JSON.DATA).path(JSON.STATE).asInt());
        route1.activateRoute();
        JUnitUtil.waitFor(() -> {
            return route1.getState() == Sensor.ACTIVE;
        }, "Route to activate");
        message = connection.getMessage();
        assertNotNull(message);
        assertEquals(JSON.ACTIVE, message.path(JSON.DATA).path(JSON.STATE).asInt());
        sensor1.setKnownState(Sensor.INACTIVE);
        JUnitUtil.waitFor(() -> {
            return route1.getState() == Sensor.INACTIVE;
        }, "Route to deactivate");
        message = connection.getMessage();
        assertNotNull(message);
        assertEquals(Sensor.INACTIVE, route1.getState());
        assertEquals(JSON.INACTIVE, message.path(JSON.DATA).path(JSON.STATE).asInt());

        
        JUnitUtil.waitFor( () -> sensor1.getKnownState()==Sensor.INACTIVE, "Sensor goes inactive without interval ?");

        service.onClose();
        // TODO: test that service is no longer a listener in RouteManager

    }

    @Test
    public void testOnMessageChange() throws IOException, JmriException, JsonException {
        // only test with route with sensor - there are no means for getting
        // the route state on a sensorless route

        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        JsonNode message;
        JsonRouteSocketService service = new JsonRouteSocketService(connection);
        RouteManager manager = InstanceManager.getDefault(RouteManager.class);
        Route route1 = manager.provideRoute("IO1", "Route1");
        Sensor sensor1 = InstanceManager.getDefault(SensorManager.class).provideSensor("IS1");
        Turnout turnout1 = InstanceManager.getDefault(TurnoutManager.class).provideTurnout("IT1");
        turnout1.setCommandedState(Turnout.CLOSED);
        route1.setTurnoutsAlignedSensor(sensor1.getSystemName());
        assertTrue(route1.addOutputTurnout(turnout1.getSystemName(), Turnout.THROWN));
        route1.activateRoute();
        assertNotNull(route1.getTurnoutsAlgdSensor());
        // Route ACTIVE - becomes ACTIVE
        message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "IO1").put(JSON.STATE, JSON.ACTIVE);
        service.onMessage(JsonRouteServiceFactory.ROUTE, message, new JsonRequest(locale, JSON.V5, JSON.POST, 42));
        JUnitUtil.waitFor(() -> {
            return route1.getState() == Sensor.ACTIVE;
        }, "Route to activate");
        assertEquals(Sensor.ACTIVE, route1.getState());
        // Route INACTIVE - remains ACTIVE
        message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "IO1").put(JSON.STATE, JSON.INACTIVE);
        service.onMessage(JsonRouteServiceFactory.ROUTE, message, new JsonRequest(locale, JSON.V5, JSON.POST, 42));
        assertEquals(Sensor.ACTIVE, route1.getState());
        // Route UNKNOWN - remains ACTIVE
        message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "IO1").put(JSON.STATE, JSON.UNKNOWN);
        service.onMessage(JsonRouteServiceFactory.ROUTE, message, new JsonRequest(locale, JSON.V5, JSON.POST, 42));
        JUnitUtil.waitFor(() -> {
            return route1.getState() == Sensor.ACTIVE;
        }, "Route to activate");
        assertEquals(Sensor.ACTIVE, route1.getState());
        // Route TOGGLE - remains ACTIVE
        message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "IO1").put(JSON.STATE, JSON.TOGGLE);
        service.onMessage(JsonRouteServiceFactory.ROUTE, message, new JsonRequest(locale, JSON.V5, JSON.POST, 42));
        JUnitUtil.waitFor(() -> {
            return route1.getState() == Sensor.ACTIVE;
        }, "Route to activate");
        assertEquals(Sensor.ACTIVE, route1.getState());
        // Route TOGGLE - becomes ACTIVE
        sensor1.setKnownState(Sensor.INACTIVE);
        message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "IO1").put(JSON.STATE, JSON.TOGGLE);
        service.onMessage(JsonRouteServiceFactory.ROUTE, message, new JsonRequest(locale, JSON.V5, JSON.POST, 42));
        JUnitUtil.waitFor(() -> {
            return route1.getState() == Sensor.ACTIVE;
        }, "Route to activate");
        assertEquals(Sensor.ACTIVE, route1.getState());
        JUnitUtil.waitFor( () -> turnout1.getKnownState()==Turnout.THROWN, "Turnout goes Thrown");

        // Route Invalid State
        JsonNode invalidMessage = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "IO1").put(JSON.STATE, 42); // invalid state
        JsonException exception = assertThrows( JsonException.class, 
            () -> service.onMessage(JsonRouteServiceFactory.ROUTE, invalidMessage, new JsonRequest(locale, JSON.V5, JSON.POST, 42)) );

        assertEquals(Sensor.ACTIVE, route1.getState());
        assertNotNull(exception);
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, exception.getCode());
    }

    @BeforeEach
    public void setUp() {

        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initRouteManager();

        // Setting the output interval to 0ms both speeds up the test and prevents
        // Turnout interval threads.
        InstanceManager.getDefault(jmri.jmrix.internal.InternalSystemConnectionMemo.class).setOutputInterval(0);
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
