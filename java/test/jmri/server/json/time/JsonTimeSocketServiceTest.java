package jmri.server.json.time;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.util.StdDateFormat;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Locale;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Timebase;
import jmri.server.json.JSON;
import jmri.server.json.JsonException;
import jmri.server.json.JsonMockConnection;
import jmri.server.json.JsonRequest;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class JsonTimeSocketServiceTest {

    private final Locale locale = Locale.ENGLISH;

    @Test
    public void testOnMessage() throws IOException, JmriException, JsonException {
        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        JsonTimeSocketService service = new JsonTimeSocketService(connection);
        Timebase manager = InstanceManager.getDefault(Timebase.class);
        TimebaseTimeListener listener = new TimebaseTimeListener();
        Assertions.assertEquals(0, listener.getTime().getTime());
        StdDateFormat formatter = new StdDateFormat();
        int rate = 60; // rate is one minute every six seconds
        assertEquals( 0, manager.getPropertyChangeListeners().length, "No change listeners");
        manager.setRun(false); // stop for testing
        // GET method
        service.onMessage(JSON.TIME, connection.getObjectMapper().createObjectNode(),
                new JsonRequest(locale, JSON.V5, JSON.GET, 42));
        JsonNode message = connection.getMessage();
        Date current = manager.getTime();
        assertNotNull( message, "Message is not null");
        assertEquals( 1.0, message.path(JSON.DATA).path(JSON.RATE).asDouble(), 0.0, "Rate is realtime");
        assertEquals( JSON.OFF, message.path(JSON.DATA).path(JSON.STATE).asInt(), "Timebase is on");
        assertEquals( formatter.format(current),
            message.findPath(JSON.DATA).path(JSON.TIME).asText(), "Time is correct");
        assertEquals( 1, manager.getPropertyChangeListeners().length, "Service is listening to changes");
        // Add second listener
        manager.addPropertyChangeListener("time", listener);
        // POST method
        ObjectNode data = connection.getObjectMapper().createObjectNode();
        data.put(JSON.RATE, rate); // integer
        data.put(JSON.STATE, JSON.ON); // start the fast clock -- to test that listeners set in onMessage work
        service.onMessage(JSON.TIME, data, new JsonRequest(locale, JSON.V5, JSON.POST, 42));
        message = connection.getMessage();
        current = manager.getTime(); // time before fast clock starts
        assertNotNull( message, "Message is not null");
        assertEquals( rate, message.path(JSON.DATA).path(JSON.RATE).asDouble(), 0.0, "Rate is fast");
        assertEquals( JSON.ON, message.path(JSON.DATA).path(JSON.STATE).asInt(), "Timebase is on");
        // a timing issue can cause the message turning the fast clock on to
        // not get the time at start of running, so don't test that
        assertEquals( 2, manager.getPropertyChangeListeners().length, "Service and listener are listening to changes");
        Date waitFor = current;
        JUnitUtil.waitFor(() -> {
            return !manager.getTime().equals(waitFor);
        },"current time not different to manager getTime");
        // current = listener.getTime(); // get time from listener
        message = connection.getMessage();
        assertNotNull( message, "Message is not null");
        assertEquals( rate, message.path(JSON.DATA).path(JSON.RATE).asDouble(), 0.0, "Rate is fast");
        assertEquals( JSON.ON, message.path(JSON.DATA).path(JSON.STATE).asInt(), "Timebase is on");
        data.put(JSON.STATE, JSON.OFF); // stop the fast clock
        service.onMessage(JSON.TIME, data, new JsonRequest(Locale.ENGLISH, JSON.V5, JSON.POST, 42));
        current = manager.getTime();
        message = connection.getMessage();
        assertNotNull( message, "Message is not null");
        assertEquals( rate, message.path(JSON.DATA).path(JSON.RATE).asDouble(), 0.0, "Rate is fast");
        assertEquals( JSON.OFF, message.path(JSON.DATA).path(JSON.STATE).asInt(), "Timebase is off");
        // a failure on next line indicates JsonTimeHttpService has been changed to send message to client
        // before posting changes instead of after posting changes and that change should be undone
        assertEquals( formatter.format(current),
            message.path(JSON.DATA).path(JSON.TIME).asText(), "Time is current");
        // POST unreasonable rate
        data.put(JSON.RATE, 123456.789); // double so that both integers and doubles are tested
        JsonException ex = assertThrows( JsonException.class, () ->
            service.onMessage(JSON.TIME, data,
                new JsonRequest(locale, JSON.V5, JSON.POST, 42)),
            "Expected exception not thrown");
        assertEquals( 400, ex.getCode(), "HTTP Invalid Request");
        assertEquals( "Error setting rate.", ex.getMessage(), "Error message");
        JUnitAppender.assertErrorMessage("rate of 123456.789 is out of reasonable range 0.1 - 100.0");
        // POST bad time
        data.put(JSON.RATE, 100); // set rate to max valid rate
        data.put(JSON.TIME, "this is not a time");
        ex = assertThrows( JsonException.class, () ->
            service.onMessage(JSON.TIME, data, new JsonRequest(locale, JSON.V5, JSON.POST, 42)),
            "Expected exception not thrown");
        assertEquals( 400, ex.getCode(), "HTTP Invalid Request");
        assertEquals( "Time not in ISO 8601 format.", ex.getMessage(), "Error message");
        // POST good time
        data.put(JSON.TIME, formatter.format(waitFor));
        service.onMessage(JSON.TIME, data, new JsonRequest(locale, JSON.V5, JSON.POST, 42));
        message = connection.getMessage();
        current = manager.getTime();
        assertNotNull( message, "Message is not null");
        assertEquals( 100, message.path(JSON.DATA).path(JSON.RATE).asDouble(), 0.0, "Rate is fast");
        assertEquals( JSON.OFF, message.path(JSON.DATA).path(JSON.STATE).asInt(), "Timebase is off");
        assertEquals( formatter.format(current),
            message.path(JSON.DATA).path(JSON.TIME).asText(), "Time is current");
        service.onClose(); // clean up listeners
        manager.removePropertyChangeListener("time", listener);
        assertEquals( 0, manager.getPropertyChangeListeners().length, "Service is not listening to changes");
    }

    @Test
    public void testOnList() {
        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        JsonTimeSocketService service = new JsonTimeSocketService(connection);
        JsonException ex = assertThrows( JsonException.class, () ->
            service.onList(JSON.TIME, connection.getObjectMapper().createObjectNode(),
                new JsonRequest(locale, JSON.V5, JSON.GET, 42)),
            "Expected exception not thrown");
        assertEquals( 400, ex.getCode(), "Code is HTTP BAD REQUEST");
        assertEquals( "time cannot be listed.", ex.getMessage(), "Message is unlistable");
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
        service.onMessage(JSON.TIME, connection.getObjectMapper().createObjectNode(),
                new JsonRequest(locale, JSON.V5, JSON.GET, 42));
        // We should be listening so make a change
        manager.setRate(60); // one minute per second
        // Thrown IOException on next message
        connection.setThrowIOException(true);
        int size = connection.getMessages().size();
        manager.setRate(10);
        // The deliberately thrown IOException should have been caught and discarded
        assertEquals( size, connection.getMessages().size(), "message not sent sfter throwing exception");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    
    private static class TimebaseTimeListener implements PropertyChangeListener {

        private Date time = new Date();

        private TimebaseTimeListener() {
            time.setTime(0);
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals("time")) {
                this.time = (Date) evt.getNewValue();
            }
        }
        
        public Date getTime() {
            return new Date(this.time.getTime());
        }
    }

}
