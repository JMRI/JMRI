package jmri.server.json.throttle;

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
import jmri.server.json.roster.JsonRoster;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class JsonThrottleSocketServiceTest {

    private Locale locale = Locale.ENGLISH;

    @Test
    public void testOnList() {
        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        JsonThrottleSocketService service = new JsonThrottleSocketService(connection);
        try {
            service.onList(JsonThrottle.THROTTLE, connection.getObjectMapper().createObjectNode(), locale, 42);
            Assert.fail("Expected exception not thrown");
        } catch (JsonException ex) {
            Assert.assertEquals("Error code is HTTP Bad Request", 400, ex.getCode());
            Assert.assertEquals("Error message", "throttle cannot be listed.", ex.getMessage());
        }
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
        Assert.assertEquals("No throttles", 0, manager.getThrottles().size());
        ObjectNode data = connection.getObjectMapper().createObjectNode();
        // get the throttle
        data.put(JSON.NAME, "42").put(JSON.ADDRESS, 42);
        service.onMessage(JsonThrottle.THROTTLE, data, JSON.POST, locale, 42);
        Assert.assertEquals("One throttle", 1, manager.getThrottles().size());
        JsonNode message = connection.getMessage();
        Assert.assertNotNull(message);
        Assert.assertEquals("Address", 42, message.path(JSON.DATA).path(JSON.ADDRESS).asInt());
        Assert.assertEquals("Speed", 0.0, message.path(JSON.DATA).path(JSON.SPEED).asDouble(), 0.0);
        Assert.assertTrue("Forward", message.path(JSON.DATA).path(JSON.FORWARD).asBoolean());
        Assert.assertEquals("Speed Steps", 126, message.path(JSON.DATA).path(JsonThrottle.SPEED_STEPS).asInt());
        Assert.assertEquals("Clients", 1, message.path(JSON.DATA).path(JsonThrottle.CLIENTS).asInt());
        Assert.assertEquals("Throttle", "42", message.path(JSON.DATA).path(JSON.NAME).asText());
        Assert.assertEquals("Throttle", "42", message.path(JSON.DATA).path(JsonThrottle.THROTTLE).asText());
        Assert.assertEquals("Throttle status has 36 data elements", 36, message.findPath(JSON.DATA).size());
        connection.sendMessage((JsonNode) null, 42); // clear messages
        // set a speed of 50% in reverse
        data = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "42");
        data.put(JSON.SPEED, 0.5);
        data.put(JSON.FORWARD, false);
        Assert.assertTrue("No address", data.path(JSON.ADDRESS).isMissingNode());
        service.onMessage(JsonThrottle.THROTTLE, data, JSON.POST, locale, 42);
        message = connection.getMessages(); // should be two messages, first speed change, then direction change
        Assert.assertNotNull(message);
        Assert.assertTrue(message.isArray());
        Assert.assertEquals("Two messages", 2, message.size());
        Assert.assertEquals("Speed", 0.5, message.path(0).path(JSON.DATA).path(JSON.SPEED).asDouble(), 0.0);
        Assert.assertEquals("Throttle", "42", message.path(0).path(JSON.DATA).path(JsonThrottle.THROTTLE).asText());
        Assert.assertEquals("Throttle change has two elements", 2, message.path(0).size());
        Assert.assertFalse("Forward", message.path(1).path(JSON.DATA).path(JSON.FORWARD).asBoolean());
        Assert.assertEquals("Throttle", "42", message.path(1).path(JSON.DATA).path(JSON.NAME).asText());
        Assert.assertEquals("Throttle", "42", message.path(1).path(JSON.DATA).path(JsonThrottle.THROTTLE).asText());
        Assert.assertEquals("Throttle change has two elements", 2, message.path(1).size());
        // request status
        data = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "42");
        data.putNull(JSON.STATUS);
        service.onMessage(JsonThrottle.THROTTLE, data, JSON.POST, locale, 42);
        message = connection.getMessage();
        Assert.assertNotNull(message);
        Assert.assertEquals("Address", 42, message.path(JSON.DATA).path(JSON.ADDRESS).asInt());
        Assert.assertEquals("Speed", 0.5, message.path(JSON.DATA).path(JSON.SPEED).asDouble(), 0.0);
        Assert.assertFalse("Forward", message.path(JSON.DATA).path(JSON.FORWARD).asBoolean());
        Assert.assertEquals("Speed Steps", 126, message.path(JSON.DATA).path(JsonThrottle.SPEED_STEPS).asInt());
        Assert.assertEquals("Clients", 1, message.path(JSON.DATA).path(JsonThrottle.CLIENTS).asInt());
        Assert.assertEquals("Throttle", "42", message.path(JSON.DATA).path(JSON.NAME).asText());
        Assert.assertEquals("Throttle", "42", message.path(JSON.DATA).path(JsonThrottle.THROTTLE).asText());
        Assert.assertEquals("Throttle status has 36 data elements", 36, message.findPath(JSON.DATA).size());
        // Emergency Stop
        data = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "42");
        data.putNull(JsonThrottle.ESTOP);
        service.onMessage(JsonThrottle.THROTTLE, data, JSON.POST, locale, 42);
        message = connection.getMessage();
        Assert.assertNotNull(message);
        Assert.assertEquals("Speed", -1.0, message.path(JSON.DATA).path(JSON.SPEED).asDouble(), 0.0);
        Assert.assertEquals("Throttle", "42", message.path(JSON.DATA).path(JSON.NAME).asText());
        Assert.assertEquals("Throttle", "42", message.path(JSON.DATA).path(JsonThrottle.THROTTLE).asText());
        Assert.assertEquals("Throttle change has two elements", 2, message.size());
        // Idle
        data = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "42");
        data.putNull(JsonThrottle.IDLE);
        service.onMessage(JsonThrottle.THROTTLE, data, JSON.POST, locale, 42);
        message = connection.getMessage();
        Assert.assertNotNull(message);
        Assert.assertEquals("Speed", 0.0, message.path(JSON.DATA).path(JSON.SPEED).asDouble(), 0.0);
        Assert.assertEquals("Throttle", "42", message.path(JSON.DATA).path(JSON.NAME).asText());
        Assert.assertEquals("Throttle", "42", message.path(JSON.DATA).path(JsonThrottle.THROTTLE).asText());
        Assert.assertEquals("Throttle change has two elements", 2, message.size());
        // release
        data = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "42");
        data.putNull(JsonThrottle.RELEASE);
        service.onMessage(JsonThrottle.THROTTLE, data, JSON.POST, locale, 42);
        message = connection.getMessage();
        Assert.assertNotNull(message);
        Assert.assertTrue("Release", message.path(JSON.DATA).path(JsonThrottle.RELEASE).isNull());
        Assert.assertEquals("Throttle", "42", message.path(JSON.DATA).path(JSON.NAME).asText());
        Assert.assertEquals("Throttle", "42", message.path(JSON.DATA).path(JsonThrottle.THROTTLE).asText());
        Assert.assertEquals("Throttle change has two elements", 2, message.size());
        service.onClose();
        Assert.assertEquals("No throttles", 0, manager.getThrottles().size());
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
        Assert.assertEquals("No throttles", 0, manager.getThrottles().size());
        ObjectNode data = connection.getObjectMapper().createObjectNode();
        RosterEntry re = new RosterEntry();
        re.setOpen(true); // prevent writes when the AbstractThrottle gets released
        re.setId("42");
        re.setDccAddress("3");
        re.setLongAddress(false);
        re.setFunctionLockable(0, true);
        re.setFunctionLockable(1, false);
        Roster.getDefault().addEntry(re);
        // get the throttle
        data.put(JsonThrottle.THROTTLE, "42").put(JsonRoster.ROSTER_ENTRY, 42);
        service.onMessage(JsonThrottle.THROTTLE, data, JSON.POST, locale, 42);
        Assert.assertEquals("One throttle", 1, manager.getThrottles().size());
        Throttle throttle = manager.get(new DccLocoAddress(3, false)).getThrottle();
        throttle.setF0Momentary(!re.getFunctionLockable(0));
        throttle.setF1Momentary(!re.getFunctionLockable(1));
        Assert.assertFalse("F0 is not momentary", throttle.getF0Momentary());
        Assert.assertTrue("F1 is momentary", throttle.getF1Momentary());
        JsonNode message = connection.getMessage();
        Assert.assertNotNull(message);
        Assert.assertEquals("Address", 3, message.path(JSON.DATA).path(JSON.ADDRESS).asInt());
        Assert.assertEquals("Speed", 0.0, message.path(JSON.DATA).path(JSON.SPEED).asDouble(), 0.0);
        Assert.assertTrue("Forward", message.path(JSON.DATA).path(JSON.FORWARD).asBoolean());
        Assert.assertEquals("Speed Steps", 126, message.path(JSON.DATA).path(JsonThrottle.SPEED_STEPS).asInt());
        Assert.assertEquals("Clients", 1, message.path(JSON.DATA).path(JsonThrottle.CLIENTS).asInt());
        Assert.assertFalse(F0, message.path(JSON.DATA).path(F0).asBoolean());
        Assert.assertFalse(F1, message.path(JSON.DATA).path(F1).asBoolean());
        Assert.assertEquals("Throttle", "42", message.path(JSON.DATA).path(JSON.NAME).asText());
        Assert.assertEquals("Throttle", "42", message.path(JSON.DATA).path(JsonThrottle.THROTTLE).asText());
        Assert.assertEquals("Throttle status has 37 data elements", 37, message.findPath(JSON.DATA).size());
        connection.sendMessage((JsonNode) null, 42); // clear messages
        // press F1 and F2
        data = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "42");
        data.put(F0, true);
        data.put(F1, true);
        Assert.assertTrue("No address", data.path(JSON.ADDRESS).isMissingNode());
        service.onMessage(JsonThrottle.THROTTLE, data, JSON.POST, locale, 42);
        message = connection.getMessages(); // should be two messages, first speed change, then direction change
        Assert.assertNotNull(message);
        Assert.assertTrue(message.isArray());
        Assert.assertEquals("Two messages", 2, message.size());
        Assert.assertFalse("F0 exists", message.path(0).path(JSON.DATA).path(F0).isMissingNode());
        Assert.assertTrue("F0 becomes true", message.path(0).path(JSON.DATA).path(F0).asBoolean());
        Assert.assertEquals("Throttle", "42", message.path(0).path(JSON.DATA).path(JSON.NAME).asText());
        Assert.assertEquals("Throttle", "42", message.path(0).path(JSON.DATA).path(JsonThrottle.THROTTLE).asText());
        Assert.assertEquals("Throttle change has two elements", 2, message.path(0).size());
        Assert.assertFalse("F1 exists", message.path(1).path(JSON.DATA).path(F1).isMissingNode());
        Assert.assertTrue("F1 becomes true", message.path(1).path(JSON.DATA).path(F1).asBoolean());
        Assert.assertEquals("Throttle", "42", message.path(1).path(JSON.DATA).path(JSON.NAME).asText());
        Assert.assertEquals("Throttle", "42", message.path(1).path(JSON.DATA).path(JsonThrottle.THROTTLE).asText());
        Assert.assertEquals("Throttle change has two elements", 2, message.path(1).size());
        // request status
        data = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "42");
        data.putNull(JSON.STATUS);
        service.onMessage(JsonThrottle.THROTTLE, data, JSON.POST, locale, 42);
        message = connection.getMessage();
        Assert.assertNotNull(message);
        Assert.assertEquals("Address", 3, message.path(JSON.DATA).path(JSON.ADDRESS).asInt());
        Assert.assertEquals("Speed", 0.0, message.path(JSON.DATA).path(JSON.SPEED).asDouble(), 0.0);
        Assert.assertTrue("Forward", message.path(JSON.DATA).path(JSON.FORWARD).asBoolean());
        Assert.assertEquals("Speed Steps", 126, message.path(JSON.DATA).path(JsonThrottle.SPEED_STEPS).asInt());
        Assert.assertEquals("Clients", 1, message.path(JSON.DATA).path(JsonThrottle.CLIENTS).asInt());
        Assert.assertTrue(F0, message.path(JSON.DATA).path(F0).asBoolean());
        Assert.assertTrue(F1, message.path(JSON.DATA).path(F1).asBoolean());
        Assert.assertEquals("Throttle", "42", message.path(JSON.DATA).path(JSON.NAME).asText());
        Assert.assertEquals("Throttle", "42", message.path(JSON.DATA).path(JsonThrottle.THROTTLE).asText());
        Assert.assertEquals("Throttle status has 37 data elements", 37, message.findPath(JSON.DATA).size());
        // Close without release
        service.onClose();
        Assert.assertEquals("No throttles", 0, manager.getThrottles().size());
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
        Assert.assertEquals("No throttles", 0, manager.getThrottles().size());
        ObjectNode data1 = connection1.getObjectMapper().createObjectNode().put(JsonThrottle.THROTTLE, "client1");
        ObjectNode data2 = connection2.getObjectMapper().createObjectNode().put(JsonThrottle.THROTTLE, "client2");
        service1.onMessage(JsonThrottle.THROTTLE, data1.put(JSON.ADDRESS, 3), JSON.POST, locale, 42);
        JsonNode message1 = connection1.getMessage();
        Assert.assertNotNull(message1);
        Assert.assertEquals("One client", 1, message1.path(JSON.DATA).path(JsonThrottle.CLIENTS).asInt());
        service2.onMessage(JsonThrottle.THROTTLE, data2.put(JSON.ADDRESS, 3), JSON.POST, locale, 42);
        Assert.assertEquals("One throttle", 1, manager.getThrottles().size());
        Assert.assertEquals("Two services", 2, manager.getServers(manager.get(new DccLocoAddress(3, false))).size());
        message1 = connection1.getMessage();
        Assert.assertNotNull(message1);
        Assert.assertEquals("Two clients", 2, message1.path(JSON.DATA).path(JsonThrottle.CLIENTS).asInt());
        Assert.assertEquals("Client 1", "client1", message1.path(JSON.DATA).path(JsonThrottle.THROTTLE).asText());
        JsonNode message2 = connection2.getMessage();
        Assert.assertNotNull(message2);
        Assert.assertEquals("Two clients", 2, message2.path(JSON.DATA).path(JsonThrottle.CLIENTS).asInt());
        Assert.assertEquals("Client 2", "client2", message2.path(JSON.DATA).path(JsonThrottle.THROTTLE).asText());
        data1 = connection1.getObjectMapper().createObjectNode().put(JsonThrottle.THROTTLE, "client1");
        service1.onMessage(JsonThrottle.THROTTLE, data1.put(JSON.SPEED, 0.5), JSON.POST, locale, 42);
        message1 = connection1.getMessage();
        Assert.assertNotNull(message1);
        Assert.assertEquals("50% Speed", 0.5, message1.path(JSON.DATA).path(JSON.SPEED).asDouble(), 0.0);
        Assert.assertEquals("Client 1", "client1", message1.path(JSON.DATA).path(JsonThrottle.THROTTLE).asText());
        message2 = connection2.getMessage();
        Assert.assertNotNull(message2);
        Assert.assertEquals("50% Speed", 0.5, message2.path(JSON.DATA).path(JSON.SPEED).asDouble(), 0.0);
        Assert.assertEquals("Client 2", "client2", message2.path(JSON.DATA).path(JsonThrottle.THROTTLE).asText());
        connection1.setThrowIOException(true);
        connection2.sendMessage((JsonNode) null, 42);
        data2 = connection2.getObjectMapper().createObjectNode().put(JsonThrottle.THROTTLE, "client2");
        service2.onMessage(JsonThrottle.THROTTLE, data2.putNull(JsonThrottle.ESTOP), JSON.POST, locale, 42);
        message2 = connection2.getMessages();
        Assert.assertNotNull(message2);
        Assert.assertTrue(message2.isArray());
        Assert.assertEquals("Two messages expected", 2, message2.size());
        // order is dropped client followed by ESTOP because first client errors sending ESTOP and notification that
        // client was dropped is sent before loop to send ESTOP to second client iterates
        Assert.assertEquals("One client", 1, message2.path(0).path(JSON.DATA).path(JsonThrottle.CLIENTS).asInt());
        Assert.assertEquals("Client 2", "client2",
                message2.path(0).path(JSON.DATA).path(JsonThrottle.THROTTLE).asText());
        Assert.assertEquals("Emergency Stop", -1.0,
                message2.path(1).path(JSON.DATA).path(JSON.SPEED).asDouble(), 0.0);
        Assert.assertEquals("Client 2", "client2",
                message2.path(1).path(JSON.DATA).path(JsonThrottle.THROTTLE).asText());
        JUnitAppender.assertWarnMessage("Unable to send message, closing connection: null");
    }

    @Test
    public void testRequestThrottleNoAddress() throws IOException, JmriException {
        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        JsonThrottleSocketService service = new JsonThrottleSocketService(connection);
        ObjectNode data = connection.getObjectMapper().createObjectNode();
        data.put(JSON.NAME, "42");
        try {
            service.onMessage(JsonThrottle.THROTTLE, data, JSON.POST, locale, 42);
            Assert.fail("Expected exception not thrown");
        } catch (JsonException ex) {
            Assert.assertEquals("Error code is HTTP Bad Request", 400, ex.getCode());
            Assert.assertEquals("Error message", "Throttles must be requested with an address.", ex.getMessage());
        }
        JUnitAppender.assertWarnMessage("No address specified");
    }

    @Test
    public void testRequestEmptyThrottle() throws IOException, JmriException {
        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        JsonThrottleSocketService service = new JsonThrottleSocketService(connection);
        ObjectNode data = connection.getObjectMapper().createObjectNode();
        data.put(JsonThrottle.THROTTLE, ""); // empty string
        try {
            service.onMessage(JsonThrottle.THROTTLE, data, JSON.POST, locale, 42);
            Assert.fail("Expected exception not thrown");
        } catch (JsonException ex) {
            Assert.assertEquals("Error code is HTTP Bad Request", 400, ex.getCode());
            Assert.assertEquals("Error message", "Throttles must be assigned a client ID.", ex.getMessage());
        }
        JUnitAppender.assertWarnMessage("JSON throttle \"\" requested using \"throttle\" instead of \"name\"");
        data.put(JSON.NAME, ""); // empty string
        try {
            service.onMessage(JsonThrottle.THROTTLE, data, JSON.POST, locale, 42);
            Assert.fail("Expected exception not thrown");
        } catch (JsonException ex) {
            Assert.assertEquals("Error code is HTTP Bad Request", 400, ex.getCode());
            Assert.assertEquals("Error message", "Throttles must be assigned a client ID.", ex.getMessage());
        }
        JUnitAppender.assertWarnMessage("JSON throttle \"\" requested using \"throttle\" instead of \"name\"");
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initRosterConfigManager();
        JUnitUtil.initDebugThrottleManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
