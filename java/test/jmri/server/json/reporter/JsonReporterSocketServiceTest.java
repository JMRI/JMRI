package jmri.server.json.reporter;

import com.fasterxml.jackson.databind.JsonNode;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Locale;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Reporter;
import jmri.ReporterManager;
import jmri.server.json.JSON;
import jmri.server.json.JsonException;
import jmri.server.json.JsonMockConnection;
import jmri.server.json.JsonRequest;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 *
 * @author Paul Bender
 * @author Randall Wood
 */
public class JsonReporterSocketServiceTest {

    private final Locale locale = Locale.ENGLISH;

    @Test
    public void testReporterChange() throws IOException, JmriException, JsonException {

        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        JsonNode message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "IR1");
        JsonReporterSocketService service = new JsonReporterSocketService(connection);
        ReporterManager manager = InstanceManager.getDefault(ReporterManager.class);
        Reporter memory1 = manager.provideReporter("IR1");
        service.onMessage(JsonReporter.REPORTER, message, new JsonRequest(locale, JSON.V5, JSON.POST, 42));
        // TODO: test that service is listener in ReporterManager
        // default null value of memory1 has text representation "null" in JSON
        message = connection.getMessage();
        assertNotNull( message, "Message is not null");
        assertEquals("null", message.path(JSON.DATA).path(JsonReporter.REPORT).asText());
        memory1.setReport("throw");
        JUnitUtil.waitFor(() -> {
            return memory1.getCurrentReport().equals("throw");
        }, "Reporter to throw");
        message = connection.getMessage();
        assertNotNull( message, "Message is not null");
        assertEquals("throw", message.path(JSON.DATA).path(JsonReporter.REPORT).asText());
        memory1.setReport("close");
        JUnitUtil.waitFor(() -> {
            return memory1.getCurrentReport().equals("close");
        }, "Reporter to close");
        message = connection.getMessage();
        assertNotNull( message, "Message is not null");
        assertEquals("close", memory1.getCurrentReport());
        assertEquals("close", message.path(JSON.DATA).path(JsonReporter.REPORT).asText());
        service.onClose();
        // TODO: test that service is no longer a listener in ReporterManager

    }

    @Test
    public void testOnMessageChange() throws IOException, JmriException, JsonException {

        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        JsonNode message;
        JsonReporterSocketService service = new JsonReporterSocketService(connection);
        ReporterManager manager = InstanceManager.getDefault(ReporterManager.class);
        Reporter memory1 = manager.provideReporter("IR1");
        // Reporter "close"
        message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "IR1").put(JsonReporter.REPORT, "close");
        service.onMessage(JsonReporter.REPORTER, message, new JsonRequest(locale, JSON.V5, JSON.POST, 42));
        assertEquals("close", memory1.getCurrentReport());
        // Reporter "throw"
        message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "IR1").put(JsonReporter.REPORT, "throw");
        service.onMessage(JsonReporter.REPORTER, message, new JsonRequest(locale, JSON.V5, JSON.POST, 42));
        assertEquals("throw", memory1.getCurrentReport());
        // Reporter UNKNOWN - remains ON
        message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "IR1").putNull(JsonReporter.REPORT);
        service.onMessage(JsonReporter.REPORTER, message, new JsonRequest(locale, JSON.V5, JSON.POST, 42));
        assertNull( memory1.getCurrentReport());
        memory1.setReport("throw");
        // Reporter no value
        JsonNode messageNoEx = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "IR1");
        assertDoesNotThrow( () ->
            service.onMessage(JsonReporter.REPORTER, messageNoEx,
                new JsonRequest(locale, JSON.V5, JSON.POST, 42)));
        assertEquals("throw", memory1.getCurrentReport());

    }

    @BeforeEach
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initReporterManager();
    }

    @AfterEach
    public void tearDown() throws Exception {
        JUnitUtil.tearDown();
    }

}
