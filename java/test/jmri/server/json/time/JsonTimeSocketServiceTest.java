package jmri.server.json.time;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Locale;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Timebase;
import jmri.server.json.JSON;
import jmri.server.json.JsonException;
import jmri.server.json.JsonMockConnection;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

public class JsonTimeSocketServiceTest {

    @Test
    public void testOnMessage() throws IOException, JmriException, JsonException {
        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        JsonTimeSocketService service = new JsonTimeSocketService(connection);
        Timebase manager = InstanceManager.getDefault(Timebase.class);
        ISO8601DateFormat formatter = new ISO8601DateFormat();
        Assert.assertEquals("No time listeners", 0, manager.getMinuteChangeListeners().length);
        Assert.assertEquals("No change listeners", 0, manager.getNumPropertyChangeListeners());
        manager.setRun(false); // stop for testing
        // GET method
        service.onMessage(JsonTimeServiceFactory.TIME, connection.getObjectMapper().createObjectNode(), JSON.GET,
                Locale.ENGLISH);
        JsonNode message = connection.getMessage();
        Date current = manager.getTime();
        Assert.assertNotNull("Message is not null", message);
        Assert.assertEquals("Rate is realtime", 1.0, message.path(JSON.DATA).path(JSON.RATE).asDouble(), 0.0);
        Assert.assertEquals("Timebase is on", JSON.OFF, message.path(JSON.DATA).path(JSON.STATE).asInt());
        Assert.assertEquals("Time is correct",
                formatter.format(current),
                message.findPath(JSON.DATA).path(JsonTimeServiceFactory.TIME).asText());
        Assert.assertEquals("Service is listening to time", 1, manager.getMinuteChangeListeners().length);
        Assert.assertEquals("Service is listening to changes", 1, manager.getNumPropertyChangeListeners());
        // POST method
        ObjectNode data = connection.getObjectMapper().createObjectNode();
        data.put(JSON.RATE, 60); // integer, one minute per second
        data.put(JSON.STATE, JSON.ON); // start the fast clock -- to test that listeners set in onMessage work
        service.onMessage(JsonTimeServiceFactory.TIME, data, JSON.POST, Locale.ENGLISH);
        message = connection.getMessage();
        current = manager.getTime();
        Assert.assertNotNull("Message is not null", message);
        Assert.assertEquals("Rate is fast", 60, message.path(JSON.DATA).path(JSON.RATE).asDouble(), 0.0);
        Assert.assertEquals("Timebase is on", JSON.ON, message.path(JSON.DATA).path(JSON.STATE).asInt());
        Assert.assertEquals("Time is current", formatter.format(current), message.path(JSON.DATA).path(JsonTimeServiceFactory.TIME).asText());
        Assert.assertEquals("Service is listening to time", 1, manager.getMinuteChangeListeners().length);
        Assert.assertEquals("Service is listening to changes", 1, manager.getNumPropertyChangeListeners());
        Date waitFor = current;
        JUnitUtil.waitFor(() -> {
            return !manager.getTime().equals(waitFor);
        });
        current = manager.getTime();
        message = connection.getMessage();
        Assert.assertNotNull("Message is not null", message);
        Assert.assertEquals("Rate is fast", 60, message.path(JSON.DATA).path(JSON.RATE).asDouble(), 0.0);
        Assert.assertEquals("Timebase is on", JSON.ON, message.path(JSON.DATA).path(JSON.STATE).asInt());
        // Don't actually test times -- systems are too sensitive
        // Assert.assertEquals("Time is current", formatter.format(current), message.path(JSON.DATA).path(JsonTimeServiceFactory.TIME).asText());
        data.put(JSON.STATE, JSON.OFF); // stop the fast clock
        service.onMessage(JsonTimeServiceFactory.TIME, data, JSON.POST, Locale.ENGLISH);
        current = manager.getTime();
        message = connection.getMessage();
        Assert.assertNotNull("Message is not null", message);
        Assert.assertEquals("Rate is fast", 60, message.path(JSON.DATA).path(JSON.RATE).asDouble(), 0.0);
        Assert.assertEquals("Timebase is off", JSON.OFF, message.path(JSON.DATA).path(JSON.STATE).asInt());
        // Don't actually test times -- systems are too sensitive
        // Assert.assertEquals("Time is current", formatter.format(current), message.path(JSON.DATA).path(JsonTimeServiceFactory.TIME).asText());
        // POST unreasonable rate
        data.put(JSON.RATE, 123456.789); // double so that both integers and doubles are tested
        try {
            service.onMessage(JsonTimeServiceFactory.TIME, data, JSON.POST, Locale.ENGLISH);
            Assert.fail("Expected exception not thrown");
        } catch (JsonException ex) {
            Assert.assertEquals("HTTP Invalid Request", 400, ex.getCode());
            Assert.assertEquals("Error message", "Error setting rate.", ex.getMessage());
        }
        JUnitAppender.assertErrorMessage("rate of 123456.789 is out of reasonable range");
        // POST bad time
        data.put(JSON.RATE, 100); // set rate to valid rate again
        data.put(JsonTimeServiceFactory.TIME, "this is not a time");
        try {
            service.onMessage(JsonTimeServiceFactory.TIME, data, JSON.POST, Locale.ENGLISH);
            Assert.fail("Expected exception not thrown");
        } catch (JsonException ex) {
            Assert.assertEquals("HTTP Invalid Request", 400, ex.getCode());
            Assert.assertEquals("Error message", "Time not in ISO 8601 format.", ex.getMessage());
        }
        // POST good time
        data.put(JsonTimeServiceFactory.TIME, formatter.format(waitFor));
        service.onMessage(JsonTimeServiceFactory.TIME, data, JSON.POST, Locale.ENGLISH);
        message = connection.getMessage();
        current = manager.getTime();
        Assert.assertNotNull("Message is not null", message);
        Assert.assertEquals("Rate is fast", 100, message.path(JSON.DATA).path(JSON.RATE).asDouble(), 0.0);
        Assert.assertEquals("Timebase is off", JSON.OFF, message.path(JSON.DATA).path(JSON.STATE).asInt());
        Assert.assertEquals("Time is current", formatter.format(current), message.path(JSON.DATA).path(JsonTimeServiceFactory.TIME).asText());
        service.onClose(); // clean up listeners
        Assert.assertEquals("Service is not listening to time", 0, manager.getMinuteChangeListeners().length);
        Assert.assertEquals("Service is not listening to changes", 0, manager.getNumPropertyChangeListeners());
    }

    @Test
    public void testOnList() {
        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        JsonTimeSocketService service = new JsonTimeSocketService(connection);
        try {
            service.onList(JsonTimeServiceFactory.TIME, connection.getObjectMapper().createObjectNode(),
                    Locale.ENGLISH);
            Assert.fail("Expected exception not thrown");
        } catch (JsonException ex) {
            Assert.assertEquals("Code is HTTP BAD REQUEST", 400, ex.getCode());
            Assert.assertEquals("Message is unlistable", "time cannot be listed.", ex.getMessage());
        }
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
