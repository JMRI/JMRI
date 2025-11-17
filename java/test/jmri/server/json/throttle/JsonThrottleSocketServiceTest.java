package jmri.server.json.throttle;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Locale;

import jmri.DccLocoAddress;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Throttle;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.server.json.JSON;
import jmri.server.json.JsonException;
import jmri.server.json.JsonMockConnection;
import jmri.server.json.JsonRequest;
import jmri.server.json.roster.JsonRoster;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class JsonThrottleSocketServiceTest {

    private final Locale locale = Locale.ENGLISH;

    @Test
    public void testOnList() {
        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        JsonThrottleSocketService service = new JsonThrottleSocketService(connection);
        JsonException ex = assertThrows( JsonException.class, () ->
            service.onList(JsonThrottle.THROTTLE, connection.getObjectMapper().createObjectNode(),
                new JsonRequest(locale, JSON.V5, JSON.GET, 42))
            ,"Expected exception not thrown");
        assertEquals( 400, ex.getCode(), "Error code is HTTP Bad Request");
        assertEquals( "throttle cannot be listed.", ex.getMessage(), "Error message");
    }

    /**
     * Test that opens a throttle using an address (not a RosterEntry), runs it,
     * and releases it.
     *
     * @throws JsonException if an unexpected exception occurs
     * @throws JmriException if an unexpected exception occurs
     * @throws IOException   if an unexpected exception occurs
     */
    @Test
    public void testRunThrottleAddress() throws IOException, JmriException, JsonException {
        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        JsonThrottleSocketService service = new JsonThrottleSocketService(connection);
        JsonThrottleManager manager = InstanceManager.getDefault(JsonThrottleManager.class);
        assertEquals( 0, manager.getThrottles().size(), "No throttles");
        ObjectNode data = connection.getObjectMapper().createObjectNode();
        // get the throttle
        data.put(JSON.NAME, "42").put(JSON.ADDRESS, 42);
        service.onMessage(JsonThrottle.THROTTLE, data, new JsonRequest(locale, JSON.V5, JSON.POST, 42));
        assertEquals( 1, manager.getThrottles().size(), "One throttle");
        JsonNode message = connection.getMessage();
        assertNotNull(message);
        assertEquals( 42, message.path(JSON.DATA).path(JSON.ADDRESS).asInt(), "Address");
        assertEquals( 0.0, message.path(JSON.DATA).path(JSON.SPEED).asDouble(), 0.0, "Speed");
        assertTrue( message.path(JSON.DATA).path(JSON.FORWARD).asBoolean(), "Forward");
        assertEquals( 126, message.path(JSON.DATA).path(JsonThrottle.SPEED_STEPS).asInt(), "Speed Steps");
        assertEquals( 1, message.path(JSON.DATA).path(JsonThrottle.CLIENTS).asInt(), "Clients");
        assertEquals( "42", message.path(JSON.DATA).path(JSON.NAME).asText(), "Throttle");
        assertEquals( "42", message.path(JSON.DATA).path(JsonThrottle.THROTTLE).asText(), "Throttle");
        assertEquals( 36, message.findPath(JSON.DATA).size(), "Throttle status has 36 data elements");
        connection.sendMessage(null, 42); // clear messages
        // set a speed of 50% in reverse
        data = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "42");
        data.put(JSON.SPEED, 0.5);
        data.put(JSON.FORWARD, false);
        assertTrue( data.path(JSON.ADDRESS).isMissingNode(), "No address");
        service.onMessage(JsonThrottle.THROTTLE, data, new JsonRequest(locale, JSON.V5, JSON.POST, 42));
        message = connection.getMessages(); // should be two messages, first speed change, then direction change
        assertNotNull(message);
        assertTrue(message.isArray());
        assertEquals( 2, message.size(), "Two messages");
        assertEquals( 0.5, message.path(0).path(JSON.DATA).path(JSON.SPEED).asDouble(), 0.0, "Speed");
        assertEquals( "42", message.path(0).path(JSON.DATA).path(JsonThrottle.THROTTLE).asText(), "Throttle");
        assertEquals( 2, message.path(0).size(), "Throttle change has two elements");
        assertFalse( message.path(1).path(JSON.DATA).path(JSON.FORWARD).asBoolean(), "Forward");
        assertEquals( "42", message.path(1).path(JSON.DATA).path(JSON.NAME).asText(), "Throttle");
        assertEquals( "42", message.path(1).path(JSON.DATA).path(JsonThrottle.THROTTLE).asText(), "Throttle");
        assertEquals( 2, message.path(1).size(), "Throttle change has two elements");
        // request status
        data = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "42");
        data.putNull(JSON.STATUS);
        service.onMessage(JsonThrottle.THROTTLE, data, new JsonRequest(locale, JSON.V5, JSON.POST, 42));
        message = connection.getMessage();
        assertNotNull(message);
        assertEquals( 42, message.path(JSON.DATA).path(JSON.ADDRESS).asInt(), "Address");
        assertEquals( 0.5, message.path(JSON.DATA).path(JSON.SPEED).asDouble(), 0.0, "Speed");
        assertFalse( message.path(JSON.DATA).path(JSON.FORWARD).asBoolean(), "Forward");
        assertEquals( 126, message.path(JSON.DATA).path(JsonThrottle.SPEED_STEPS).asInt(), "Speed Steps");
        assertEquals( 1, message.path(JSON.DATA).path(JsonThrottle.CLIENTS).asInt(), "Clients");
        assertEquals( "42", message.path(JSON.DATA).path(JSON.NAME).asText(), "Throttle");
        assertEquals( "42", message.path(JSON.DATA).path(JsonThrottle.THROTTLE).asText(), "Throttle");
        assertEquals( 36, message.findPath(JSON.DATA).size(), "Throttle status has 36 data elements");
        // Emergency Stop
        data = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "42");
        data.putNull(JsonThrottle.ESTOP);
        service.onMessage(JsonThrottle.THROTTLE, data, new JsonRequest(locale, JSON.V5, JSON.POST, 42));
        message = connection.getMessage();
        assertNotNull(message);
        assertEquals( -1.0, message.path(JSON.DATA).path(JSON.SPEED).asDouble(), 0.0, "Speed");
        assertEquals( "42", message.path(JSON.DATA).path(JSON.NAME).asText(), "Throttle");
        assertEquals( "42", message.path(JSON.DATA).path(JsonThrottle.THROTTLE).asText(), "Throttle");
        assertEquals( 2, message.size(), "Throttle change has two elements");
        // Idle
        data = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "42");
        data.putNull(JsonThrottle.IDLE);
        service.onMessage(JsonThrottle.THROTTLE, data, new JsonRequest(locale, JSON.V5, JSON.POST, 42));
        message = connection.getMessage();
        assertNotNull(message);
        assertEquals( 0.0, message.path(JSON.DATA).path(JSON.SPEED).asDouble(), 0.0, "Speed");
        assertEquals( "42", message.path(JSON.DATA).path(JSON.NAME).asText(), "Throttle");
        assertEquals( "42", message.path(JSON.DATA).path(JsonThrottle.THROTTLE).asText(), "Throttle");
        assertEquals( 2, message.size(), "Throttle change has two elements");
        // release
        data = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "42");
        data.putNull(JsonThrottle.RELEASE);
        service.onMessage(JsonThrottle.THROTTLE, data, new JsonRequest(locale, JSON.V5, JSON.POST, 42));
        message = connection.getMessage();
        assertNotNull(message);
        assertTrue( message.path(JSON.DATA).path(JsonThrottle.RELEASE).isNull(), "Release");
        assertEquals( "42", message.path(JSON.DATA).path(JSON.NAME).asText(), "Throttle");
        assertEquals( "42", message.path(JSON.DATA).path(JsonThrottle.THROTTLE).asText(), "Throttle");
        assertEquals( 2, message.size(), "Throttle change has two elements");
        service.onClose();
        assertEquals( 0, manager.getThrottles().size(), "No throttles");
    }

    /**
     * Test that opens a throttle using a RosterEntry ID, presses function keys,
     * and closes it without releasing it.
     *
     * @throws JsonException if an unexpected exception occurs
     * @throws JmriException if an unexpected exception occurs
     * @throws IOException   if an unexpected exception occurs
     */
    @Test
    public void testRunThrottleRosterEntry() throws IOException, JmriException, JsonException {
        String F0 = JSON.F + 0;
        String F1 = JSON.F + 1;
        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        JsonThrottleSocketService service = new JsonThrottleSocketService(connection);
        JsonThrottleManager manager = InstanceManager.getDefault(JsonThrottleManager.class);
        assertEquals( 0, manager.getThrottles().size(), "No throttles");
        ObjectNode data = connection.getObjectMapper().createObjectNode();
        RosterEntry re = new RosterEntry();
        re.setOpen(true); // prevent writes when the AbstractThrottle gets released
        re.setId("42");
        re.setDccAddress("3");
        re.setLongAddress(false);
        re.setFunctionLockable(0, true);
        re.setFunctionLockable(1, false);
        Roster.getDefault().addEntry(re);
        // get the throttle by JSON.NAME
        data.put(JSON.NAME, "42").put(JsonRoster.ROSTER_ENTRY, 42);
        service.onMessage(JsonThrottle.THROTTLE, data, new JsonRequest(locale, JSON.V5, JSON.POST, 42));
        assertEquals( 1, manager.getThrottles().size(), "One throttle");
        Throttle throttle = manager.get(new DccLocoAddress(3, false)).getThrottle();
        throttle.setFunctionMomentary( 0, !re.getFunctionLockable(0));
        throttle.setFunctionMomentary( 1, !re.getFunctionLockable(1));
        assertFalse( throttle.getFunctionMomentary(0), "F0 is not momentary");
        assertTrue( throttle.getFunctionMomentary(1), "F1 is momentary");
        JsonNode message = connection.getMessage();
        assertNotNull(message);
        assertEquals( 3, message.path(JSON.DATA).path(JSON.ADDRESS).asInt(), "Address");
        assertEquals( 0.0, message.path(JSON.DATA).path(JSON.SPEED).asDouble(), 0.0, "Speed");
        assertTrue( message.path(JSON.DATA).path(JSON.FORWARD).asBoolean(), "Forward");
        assertEquals( 126, message.path(JSON.DATA).path(JsonThrottle.SPEED_STEPS).asInt(), "Speed Steps");
        assertEquals( 1, message.path(JSON.DATA).path(JsonThrottle.CLIENTS).asInt(), "Clients");
        assertFalse( message.path(JSON.DATA).path(F0).asBoolean(), F0);
        assertFalse( message.path(JSON.DATA).path(F1).asBoolean(), F1);
        assertEquals( "42", message.path(JSON.DATA).path(JSON.NAME).asText(), "Throttle");
        assertEquals( "42", message.path(JSON.DATA).path(JsonThrottle.THROTTLE).asText(), "Throttle");
        assertEquals( 37, message.findPath(JSON.DATA).size(), "Throttle status has 37 data elements");
        connection.sendMessage(null, 42); // clear messages
        // press F1 and F2
        data = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "42");
        data.put(F0, true);
        data.put(F1, true);
        assertTrue( data.path(JSON.ADDRESS).isMissingNode(), "No address");
        service.onMessage(JsonThrottle.THROTTLE, data, new JsonRequest(locale, JSON.V5, JSON.POST, 42));
        message = connection.getMessages(); // should be two messages, first speed change, then direction change
        assertNotNull(message);
        assertTrue(message.isArray());
        assertEquals( 2, message.size(), "Two messages");
        assertFalse( message.path(0).path(JSON.DATA).path(F0).isMissingNode(), "F0 exists");
        assertTrue( message.path(0).path(JSON.DATA).path(F0).asBoolean(), "F0 becomes true");
        assertEquals( "42", message.path(0).path(JSON.DATA).path(JSON.NAME).asText(), "Throttle");
        assertEquals( "42", message.path(0).path(JSON.DATA).path(JsonThrottle.THROTTLE).asText(), "Throttle");
        assertEquals( 2, message.path(0).size(), "Throttle change has two elements");
        assertFalse( message.path(1).path(JSON.DATA).path(F1).isMissingNode(), "F1 exists");
        assertTrue( message.path(1).path(JSON.DATA).path(F1).asBoolean(), "F1 becomes true");
        assertEquals( "42", message.path(1).path(JSON.DATA).path(JSON.NAME).asText(), "Throttle");
        assertEquals( "42", message.path(1).path(JSON.DATA).path(JsonThrottle.THROTTLE).asText(), "Throttle");
        assertEquals( 2, message.path(1).size(), "Throttle change has two elements");
        // request status
        data = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "42");
        data.putNull(JSON.STATUS);
        service.onMessage(JsonThrottle.THROTTLE, data, new JsonRequest(locale, JSON.V5, JSON.POST, 42));
        message = connection.getMessage();
        assertNotNull(message);
        assertEquals( 3, message.path(JSON.DATA).path(JSON.ADDRESS).asInt(), "Address");
        assertEquals( 0.0, message.path(JSON.DATA).path(JSON.SPEED).asDouble(), 0.0, "Speed");
        assertTrue( message.path(JSON.DATA).path(JSON.FORWARD).asBoolean(), "Forward");
        assertEquals( 126, message.path(JSON.DATA).path(JsonThrottle.SPEED_STEPS).asInt(), "Speed Steps");
        assertEquals( 1, message.path(JSON.DATA).path(JsonThrottle.CLIENTS).asInt(), "Clients");
        assertTrue( message.path(JSON.DATA).path(F0).asBoolean(), F0);
        assertTrue( message.path(JSON.DATA).path(F1).asBoolean(), F1);
        assertEquals( "42", message.path(JSON.DATA).path(JSON.NAME).asText(), "Throttle");
        assertEquals( "42", message.path(JSON.DATA).path(JsonThrottle.THROTTLE).asText(), "Throttle");
        assertEquals( 37, message.findPath(JSON.DATA).size(), "Throttle status has 37 data elements");
        // Close without release
        service.onClose();
        assertEquals( 0, manager.getThrottles().size(), "No throttles");
        JUnitAppender.assertWarnMessage("Roster Entry 42 running time not saved as entry is already open for editing");
    }

    /**
     * Test that opens a throttle with one service, opens it with another
     * service, verifies both services receive updates to changes made by the
     * other service; then test that one service gets correct messages when
     * other service throws IOException
     *
     * @throws JsonException if an unexpected exception occurs
     * @throws JmriException if an unexpected exception occurs
     * @throws IOException   if an unexpected exception occurs
     */
    @Test
    public void testRunThrottleMultipleClients() throws IOException, JmriException, JsonException {
        JsonMockConnection connection1 = new JsonMockConnection((DataOutputStream) null);
        JsonThrottleSocketService service1 = new JsonThrottleSocketService(connection1);
        JsonMockConnection connection2 = new JsonMockConnection((DataOutputStream) null);
        JsonThrottleSocketService service2 = new JsonThrottleSocketService(connection2);
        JsonThrottleManager manager = InstanceManager.getDefault(JsonThrottleManager.class);
        assertEquals( 0, manager.getThrottles().size(), "No throttles");
        ObjectNode data1 = connection1.getObjectMapper().createObjectNode().put(JsonThrottle.THROTTLE, "client1");
        ObjectNode data2 = connection2.getObjectMapper().createObjectNode().put(JsonThrottle.THROTTLE, "client2");
        // get the throttle by JsonThrottle.THROTTLE produces WARN client1
        service1.onMessage(JsonThrottle.THROTTLE, data1.put(JSON.ADDRESS, 3), new JsonRequest(locale, JSON.V5, JSON.POST, 42));
        JUnitAppender.assertWarnMessage("JSON throttle \"client1\" requested using \"throttle\" instead of \"name\"");
        JsonNode message1 = connection1.getMessage();
        assertNotNull(message1);
        assertEquals( 1, message1.path(JSON.DATA).path(JsonThrottle.CLIENTS).asInt(), "One client");
        service2.onMessage(JsonThrottle.THROTTLE, data2.put(JSON.ADDRESS, 3), new JsonRequest(locale, JSON.V5, JSON.POST, 42));
        // get the throttle by JsonThrottle.THROTTLE produces WARN client2
        JUnitAppender.assertWarnMessage("JSON throttle \"client2\" requested using \"throttle\" instead of \"name\"");
        assertEquals( 1, manager.getThrottles().size(), "One throttle");
        assertEquals( 2, manager.getServers(manager.get(new DccLocoAddress(3, false))).size(), "Two services");
        message1 = connection1.getMessage();
        assertNotNull(message1);
        assertEquals( 2, message1.path(JSON.DATA).path(JsonThrottle.CLIENTS).asInt(), "Two clients");
        assertEquals( "client1", message1.path(JSON.DATA).path(JsonThrottle.THROTTLE).asText(), "Client 1");
        JsonNode message2 = connection2.getMessage();
        assertNotNull(message2);
        assertEquals( 2, message2.path(JSON.DATA).path(JsonThrottle.CLIENTS).asInt(), "Two clients");
        assertEquals( "client2", message2.path(JSON.DATA).path(JsonThrottle.THROTTLE).asText(), "Client 2");
        data1 = connection1.getObjectMapper().createObjectNode().put(JsonThrottle.THROTTLE, "client1");
        service1.onMessage(JsonThrottle.THROTTLE, data1.put(JSON.SPEED, 0.5), new JsonRequest(locale, JSON.V5, JSON.POST, 42));
        JUnitAppender.assertWarnMessage("JSON throttle \"client1\" requested using \"throttle\" instead of \"name\"");
        message1 = connection1.getMessage();
        assertNotNull(message1);
        assertEquals( 0.5, message1.path(JSON.DATA).path(JSON.SPEED).asDouble(), 0.0, "50% Speed");
        assertEquals( "client1", message1.path(JSON.DATA).path(JsonThrottle.THROTTLE).asText(), "Client 1");
        message2 = connection2.getMessage();
        assertNotNull(message2);
        assertEquals( 0.5, message2.path(JSON.DATA).path(JSON.SPEED).asDouble(), 0.0, "50% Speed");
        assertEquals( "client2", message2.path(JSON.DATA).path(JsonThrottle.THROTTLE).asText(), "Client 2");
        connection1.setThrowIOException(true);
        connection2.sendMessage(null, 42);
        data2 = connection2.getObjectMapper().createObjectNode().put(JsonThrottle.THROTTLE, "client2");
        service2.onMessage(JsonThrottle.THROTTLE, data2.putNull(JsonThrottle.ESTOP), new JsonRequest(locale, JSON.V5, JSON.POST, 42));
        JUnitAppender.assertWarnMessage("JSON throttle \"client2\" requested using \"throttle\" instead of \"name\"");
        message2 = connection2.getMessages();
        assertNotNull(message2);
        assertTrue(message2.isArray());
        assertEquals( 2, message2.size(), "Two messages expected");
        // order is dropped client followed by ESTOP because first client errors sending ESTOP and notification that
        // client was dropped is sent before loop to send ESTOP to second client iterates
        assertEquals( 1, message2.path(0).path(JSON.DATA).path(JsonThrottle.CLIENTS).asInt(), "One client");
        assertEquals( "client2",
            message2.path(0).path(JSON.DATA).path(JsonThrottle.THROTTLE).asText(),
            "Client 2");
        assertEquals( -1.0, message2.path(1).path(JSON.DATA).path(JSON.SPEED).asDouble(), 0.0,
            "Emergency Stop");
        assertEquals( "client2",
            message2.path(1).path(JSON.DATA).path(JsonThrottle.THROTTLE).asText(),
            "Client 2");
        JUnitAppender.assertWarnMessage("Unable to send message, closing connection: null");
    }

    @Test
    public void testRequestThrottleNoAddress() throws IOException, JmriException {
        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        JsonThrottleSocketService service = new JsonThrottleSocketService(connection);
        ObjectNode data = connection.getObjectMapper().createObjectNode();
        data.put(JSON.NAME, "42");
        JsonException ex = assertThrows(JsonException.class, () ->
            service.onMessage(JsonThrottle.THROTTLE, data,
                new JsonRequest(locale, JSON.V5, JSON.POST, 42))
            ,"Expected exception not thrown");
        assertEquals( 400, ex.getCode(), "Error code is HTTP Bad Request");
        assertEquals( "Throttles must be requested with an address.", ex.getMessage(), "Error message");
        JUnitAppender.assertWarnMessage("No address specified");
    }

    @Test
    public void testRequestEmptyThrottle() throws IOException, JmriException {
        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        JsonThrottleSocketService service = new JsonThrottleSocketService(connection);
        ObjectNode data = connection.getObjectMapper().createObjectNode();
        data.put(JsonThrottle.THROTTLE, ""); // empty string

        JsonException ex = assertThrows(JsonException.class, () ->
            service.onMessage(JsonThrottle.THROTTLE, data,
                new JsonRequest(locale, JSON.V5, JSON.POST, 42)),
            "Expected exception not thrown");
        assertEquals( 400, ex.getCode(), "Error code is HTTP Bad Request");
        assertEquals( "Throttles must be assigned a client ID.", ex.getMessage(), "Error message");
        JUnitAppender.assertWarnMessage("JSON throttle \"\" requested using \"throttle\" instead of \"name\"");

        data.put(JSON.NAME, ""); // empty string
        ex = assertThrows( JsonException.class, () ->
            service.onMessage(JsonThrottle.THROTTLE, data,
                new JsonRequest(locale, JSON.V5, JSON.POST, 42)),
            "Expected exception not thrown");
        assertEquals( 400, ex.getCode(), "Error code is HTTP Bad Request");
        assertEquals( "Throttles must be assigned a client ID.", ex.getMessage(), "Error message");
        JUnitAppender.assertWarnMessage("JSON throttle \"\" requested using \"throttle\" instead of \"name\"");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initRosterConfigManager();
        JUnitUtil.initDebugThrottleManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
