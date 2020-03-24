package jmri.server.json.time;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.DataOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.util.StdDateFormat;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Timebase;
import jmri.server.json.JSON;
import jmri.server.json.JsonException;
import jmri.server.json.JsonMockConnection;
import jmri.server.json.JsonRequest;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

public class JsonTimeSocketServiceTest {

    private Locale locale = Locale.ENGLISH;

    @Test
    public void testOnMessage() throws IOException, JmriException, JsonException, ParseException {
        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        JsonTimeSocketService service = new JsonTimeSocketService(connection);
        Timebase manager = InstanceManager.getDefault(Timebase.class);
        TimebaseTimeListener listener = new TimebaseTimeListener();
        StdDateFormat formatter = new StdDateFormat();
        int rate = 60; // rate is one minute every six seconds
        assertThat(manager.getPropertyChangeListeners().length).isEqualTo(0);
        manager.setRun(false); // stop for testing
        // GET method
        service.onMessage(JSON.TIME, connection.getObjectMapper().createObjectNode(),
                new JsonRequest(locale, JSON.V5, JSON.GET, 42));
        JsonNode message = connection.getMessage();
        Date current = manager.getTime();
        assertThat(message).isNotNull();
        assertThat(message.path(JSON.DATA).path(JSON.RATE).asDouble()).isEqualTo(1.0);
        assertThat(message.path(JSON.DATA).path(JSON.STATE).asInt()).isEqualTo(JSON.OFF);
        assertThat(formatter.parse(message.path(JSON.DATA).path(JSON.TIME).asText())).isCloseTo(current, 1000);
        assertThat(manager.getPropertyChangeListeners().length).isEqualTo(1);
        // Add second listener
        manager.addPropertyChangeListener("time", listener);
        // POST method
        ObjectNode data = connection.getObjectMapper().createObjectNode();
        data.put(JSON.RATE, rate); // integer
        data.put(JSON.STATE, JSON.ON); // start the fast clock -- to test that listeners set in onMessage work
        service.onMessage(JSON.TIME, data, new JsonRequest(locale, JSON.V5, JSON.POST, 42));
        message = connection.getMessage();
        current = manager.getTime(); // time before fast clock starts
        assertThat(message).isNotNull();
        assertThat(message.path(JSON.DATA).path(JSON.RATE).asDouble()).isEqualTo(rate);
        assertThat(message.path(JSON.DATA).path(JSON.STATE).asInt()).isEqualTo(JSON.ON);
        // a timing issue can cause the message turning the fast clock on to
        // not get the time at start of running, so don't test that
        assertThat(manager.getPropertyChangeListeners().length).isEqualTo(2);
        Date waitFor = current;
        JUnitUtil.waitFor(() -> !manager.getTime().equals(waitFor));
        current = listener.getTime(); // get time from listener
        message = connection.getMessage();
        assertThat(message).isNotNull();
        assertThat(message.path(JSON.DATA).path(JSON.RATE).asDouble()).isEqualTo(rate);
        assertThat(message.path(JSON.DATA).path(JSON.STATE).asInt()).isEqualTo(JSON.ON);
        data.put(JSON.STATE, JSON.OFF); // stop the fast clock
        service.onMessage(JSON.TIME, data, new JsonRequest(Locale.ENGLISH, JSON.V5, JSON.POST, 42));
        current = manager.getTime();
        message = connection.getMessage();
        assertThat(message).isNotNull();
        assertThat(message.path(JSON.DATA).path(JSON.RATE).asDouble()).isEqualTo(rate);
        assertThat(message.path(JSON.DATA).path(JSON.STATE).asInt()).isEqualTo(JSON.OFF);
        // a failure on next line indicates JsonTimeHttpService has been changed to send message to client
        // before posting changes instead of after posting changes and that change should be undone
        assertThat(message.path(JSON.DATA).path(JSON.TIME).asText()).isEqualTo(formatter.format(current));
        // POST unreasonable rate
        data.put(JSON.RATE, 123456.789); // double so that both integers and doubles are tested
        try {
            service.onMessage(JSON.TIME, data, new JsonRequest(locale, JSON.V5, JSON.POST, 42));
            fail("Expected exception not thrown");
        } catch (JsonException ex) {
            assertThat(ex.getCode()).isEqualTo(400);
            assertThat(ex.getMessage()).isEqualTo("Error setting rate.");
        }
        JUnitAppender.assertErrorMessage("rate of 123456.789 is out of reasonable range");
        // POST bad time
        data.put(JSON.RATE, 100); // set rate to max valid rate
        data.put(JSON.TIME, "this is not a time");
        try {
            service.onMessage(JSON.TIME, data, new JsonRequest(locale, JSON.V5, JSON.POST, 42));
            fail("Expected exception not thrown");
        } catch (JsonException ex) {
            assertThat(ex.getCode()).isEqualTo(400);
            assertThat(ex.getMessage()).isEqualTo("Time not in ISO 8601 format.");
        }
        // POST good time
        data.put(JSON.TIME, formatter.format(waitFor));
        service.onMessage(JSON.TIME, data, new JsonRequest(locale, JSON.V5, JSON.POST, 42));
        message = connection.getMessage();
        current = manager.getTime();
        assertThat(message).isNotNull();
        assertThat(message.path(JSON.DATA).path(JSON.RATE).asDouble()).isEqualTo(100.0);
        assertThat(message.path(JSON.DATA).path(JSON.STATE).asInt()).isEqualTo(JSON.OFF);
        assertThat(formatter.parse(message.path(JSON.DATA).path(JSON.TIME).asText())).isCloseTo(current, 1000);
        service.onClose(); // clean up listeners
        manager.removePropertyChangeListener("time", listener);
        assertThat(manager.getPropertyChangeListeners().length).isEqualTo(0);
    }

    @Test
    public void testOnList() {
        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        JsonTimeSocketService service = new JsonTimeSocketService(connection);
        try {
            service.onList(JSON.TIME, connection.getObjectMapper().createObjectNode(), new JsonRequest(locale, JSON.V5, JSON.GET, 42));
            fail("Expected exception not thrown");
        } catch (JsonException ex) {
            assertThat(ex.getCode()).isEqualTo(400);
            assertThat(ex.getMessage()).isEqualTo("time cannot be listed.");
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
        service.onMessage(JSON.TIME, connection.getObjectMapper().createObjectNode(),
                new JsonRequest(locale, JSON.V5, JSON.GET, 42));
        // We should be listening so make a change
        manager.setRate(60); // one minute per second
        // Thrown IOException on next message
        connection.setThrowIOException(true);
        int size = connection.getMessages().size();
        manager.setRate(10);
        // The deliberately thrown IOException should have been caught and discarded
        assertThat(connection.getMessages().size()).isEqualTo(size);
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
