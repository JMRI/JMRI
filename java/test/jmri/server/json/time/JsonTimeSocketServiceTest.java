package jmri.server.json.time;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Locale;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.util.StdDateFormat;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Timebase;
import jmri.server.json.JSON;
import jmri.server.json.JsonException;
import jmri.server.json.JsonMockConnection;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;
import jmri.util.junit.rules.RetryRule;

public class JsonTimeSocketServiceTest {

    private Locale locale = Locale.ENGLISH;

    @Rule
    // this test is sensitive to load on test system, so allow a single retry before failing
    public RetryRule retryRule = new RetryRule(1);

    @Test
    public void testOnMessage() throws IOException, JmriException, JsonException {
        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        JsonTimeSocketService service = new JsonTimeSocketService(connection);
        Timebase manager = InstanceManager.getDefault(Timebase.class);
        TimebaseTimeListener listener = new TimebaseTimeListener();
        StdDateFormat formatter = new StdDateFormat();
        int rate = 60; // rate is one minute every six seconds
        Assert.assertEquals("No change listeners", 0, manager.getPropertyChangeListeners().length);
        manager.setRun(false); // stop for testing
        // GET method
        service.onMessage(JSON.TIME, connection.getObjectMapper().createObjectNode(), JSON.GET,
                locale, 42);
        JsonNode message = connection.getMessage();
        Date current = manager.getTime();
        Assert.assertNotNull("Message is not null", message);
        Assert.assertEquals("Rate is realtime", 1.0, message.path(JSON.DATA).path(JSON.RATE).asDouble(), 0.0);
        Assert.assertEquals("Timebase is on", JSON.OFF, message.path(JSON.DATA).path(JSON.STATE).asInt());
        Assert.assertEquals("Time is correct",
                formatter.format(current),
                message.findPath(JSON.DATA).path(JSON.TIME).asText());
        Assert.assertEquals("Service is listening to changes", 1, manager.getPropertyChangeListeners().length);
        // Add second listener
        manager.addPropertyChangeListener("time", listener);
        // POST method
        ObjectNode data = connection.getObjectMapper().createObjectNode();
        data.put(JSON.RATE, rate); // integer
        data.put(JSON.STATE, JSON.ON); // start the fast clock -- to test that listeners set in onMessage work
        service.onMessage(JSON.TIME, data, JSON.POST, locale, 42);
        message = connection.getMessage();
        current = manager.getTime(); // time before fast clock starts
        Assert.assertNotNull("Message is not null", message);
        Assert.assertEquals("Rate is fast", rate, message.path(JSON.DATA).path(JSON.RATE).asDouble(), 0.0);
        Assert.assertEquals("Timebase is on", JSON.ON, message.path(JSON.DATA).path(JSON.STATE).asInt());
        // a timing issue can cause the message turning the fast clock on to
        // not get the time at start of running, so don't test that
        Assert.assertEquals("Service and listener are listening to changes", 2, manager.getPropertyChangeListeners().length);
        Date waitFor = current;
        JUnitUtil.waitFor(() -> {
            return !manager.getTime().equals(waitFor);
        });
        current = listener.getTime(); // get time from listener
        message = connection.getMessage();
        Assert.assertNotNull("Message is not null", message);
        Assert.assertEquals("Rate is fast", rate, message.path(JSON.DATA).path(JSON.RATE).asDouble(), 0.0);
        Assert.assertEquals("Timebase is on", JSON.ON, message.path(JSON.DATA).path(JSON.STATE).asInt());
        data.put(JSON.STATE, JSON.OFF); // stop the fast clock
        service.onMessage(JSON.TIME, data, JSON.POST, Locale.ENGLISH, 42);
        current = manager.getTime();
        message = connection.getMessage();
        Assert.assertNotNull("Message is not null", message);
        Assert.assertEquals("Rate is fast", rate, message.path(JSON.DATA).path(JSON.RATE).asDouble(), 0.0);
        Assert.assertEquals("Timebase is off", JSON.OFF, message.path(JSON.DATA).path(JSON.STATE).asInt());
        // a failure on next line indicates JsonTimeHttpService has been changed to send message to client
        // before posting changes instead of after posting changes and that change should be undone
        Assert.assertEquals("Time is current", formatter.format(current),
                message.path(JSON.DATA).path(JSON.TIME).asText());
        // POST unreasonable rate
        data.put(JSON.RATE, 123456.789); // double so that both integers and doubles are tested
        try {
            service.onMessage(JSON.TIME, data, JSON.POST, locale, 42);
            Assert.fail("Expected exception not thrown");
        } catch (JsonException ex) {
            Assert.assertEquals("HTTP Invalid Request", 400, ex.getCode());
            Assert.assertEquals("Error message", "Error setting rate.", ex.getMessage());
        }
        JUnitAppender.assertErrorMessage("rate of 123456.789 is out of reasonable range");
        // POST bad time
        data.put(JSON.RATE, 100); // set rate to max valid rate
        data.put(JSON.TIME, "this is not a time");
        try {
            service.onMessage(JSON.TIME, data, JSON.POST, locale, 42);
            Assert.fail("Expected exception not thrown");
        } catch (JsonException ex) {
            Assert.assertEquals("HTTP Invalid Request", 400, ex.getCode());
            Assert.assertEquals("Error message", "Time not in ISO 8601 format.", ex.getMessage());
        }
        // POST good time
        data.put(JSON.TIME, formatter.format(waitFor));
        service.onMessage(JSON.TIME, data, JSON.POST, locale, 42);
        message = connection.getMessage();
        current = manager.getTime();
        Assert.assertNotNull("Message is not null", message);
        Assert.assertEquals("Rate is fast", 100, message.path(JSON.DATA).path(JSON.RATE).asDouble(), 0.0);
        Assert.assertEquals("Timebase is off", JSON.OFF, message.path(JSON.DATA).path(JSON.STATE).asInt());
        Assert.assertEquals("Time is current", formatter.format(current),
                message.path(JSON.DATA).path(JSON.TIME).asText());
        service.onClose(); // clean up listeners
        manager.removePropertyChangeListener("time", listener);
        Assert.assertEquals("Service is not listening to changes", 0, manager.getPropertyChangeListeners().length);
    }

    @Test
    public void testOnList() {
        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        JsonTimeSocketService service = new JsonTimeSocketService(connection);
        try {
            service.onList(JSON.TIME, connection.getObjectMapper().createObjectNode(),
                    locale, 42);
            Assert.fail("Expected exception not thrown");
        } catch (JsonException ex) {
            Assert.assertEquals("Code is HTTP BAD REQUEST", 400, ex.getCode());
            Assert.assertEquals("Message is unlistable", "time cannot be listed.", ex.getMessage());
        }
    }

    /**
     * Test that listener handles error states correctly.
     * 
     * @throws JsonException if unexpected exception occurs
     * @throws JmriException if unexpected exception occurs
     * @throws IOException   if unexpected exception occurs
     */
    @Test
    public void testListenerErrorHandling() throws IOException, JmriException, JsonException {
        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        JsonTimeHttpService http = new JsonTimeHttpService(connection.getObjectMapper());
        JsonTimeSocketService service = new JsonTimeSocketService(connection, http);
        Timebase manager = InstanceManager.getDefault(Timebase.class);
        manager.setRun(false); // stop for testing
        // GET method
        service.onMessage(JSON.TIME, connection.getObjectMapper().createObjectNode(), JSON.GET,
                locale, 42);
        // We should be listening so make a change
        manager.setRate(60); // one minute per second
        // Thrown IOException on next message
        connection.setThrowIOException(true);
        manager.setRate(10);
        // Since the deliberately thrown IOException should have been caught and discarded,
        // this test should simply pass at this point
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    
    private static class TimebaseTimeListener implements PropertyChangeListener {

        private Date time = null;
        
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals("time")) {
                this.time = (Date) evt.getNewValue();
            }
        }
        
        public Date getTime() {
            return this.time;
        }
    }

}
