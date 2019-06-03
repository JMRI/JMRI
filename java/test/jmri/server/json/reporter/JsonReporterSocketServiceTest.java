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
public class JsonReporterSocketServiceTest {

    private Locale locale = Locale.ENGLISH;

    @Test
    public void testReporterChange() {
        try {
            JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
            JsonNode message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "IR1");
            JsonReporterSocketService service = new JsonReporterSocketService(connection);
            ReporterManager manager = InstanceManager.getDefault(ReporterManager.class);
            Reporter memory1 = manager.provideReporter("IR1");
            service.onMessage(JsonReporter.REPORTER, message, JSON.POST, locale, 42);
            // TODO: test that service is listener in ReporterManager
            // default null value of memory1 has text representation "null" in JSON
            message = connection.getMessage();
            Assert.assertNotNull("Message is not null", message);
            Assert.assertEquals("null", message.path(JSON.DATA).path(JsonReporter.REPORT).asText());
            memory1.setReport("throw");
            JUnitUtil.waitFor(() -> {
                return memory1.getCurrentReport().equals("throw");
            }, "Reporter to throw");
            message = connection.getMessage();
            Assert.assertNotNull("Message is not null", message);
            Assert.assertEquals("throw", message.path(JSON.DATA).path(JsonReporter.REPORT).asText());
            memory1.setReport("close");
            JUnitUtil.waitFor(() -> {
                return memory1.getCurrentReport().equals("close");
            }, "Reporter to close");
            message = connection.getMessage();
            Assert.assertNotNull("Message is not null", message);
            Assert.assertEquals("close", memory1.getCurrentReport());
            Assert.assertEquals("close", message.path(JSON.DATA).path(JsonReporter.REPORT).asText());
            service.onClose();
            // TODO: test that service is no longer a listener in ReporterManager
        } catch (IOException | JmriException | JsonException ex) {
            Assert.fail(ex.getMessage());
        }
    }

    @Test
    public void testOnMessageChange() {
        try {
            JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
            JsonNode message;
            JsonReporterSocketService service = new JsonReporterSocketService(connection);
            ReporterManager manager = InstanceManager.getDefault(ReporterManager.class);
            Reporter memory1 = manager.provideReporter("IR1");
            // Reporter "close"
            message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "IR1").put(JsonReporter.REPORT, "close");
            service.onMessage(JsonReporter.REPORTER, message, JSON.POST, locale, 42);
            Assert.assertEquals("close", memory1.getCurrentReport());
            // Reporter "throw"
            message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "IR1").put(JsonReporter.REPORT, "throw");
            service.onMessage(JsonReporter.REPORTER, message, JSON.POST, locale, 42);
            Assert.assertEquals("throw", memory1.getCurrentReport());
            // Reporter UNKNOWN - remains ON
            message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "IR1").putNull(JsonReporter.REPORT);
            service.onMessage(JsonReporter.REPORTER, message, JSON.POST, locale, 42);
            Assert.assertEquals(null, memory1.getCurrentReport());
            memory1.setReport("throw");
            // Reporter no value
            message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "IR1");
            JsonException exception = null;
            try {
                service.onMessage(JsonReporter.REPORTER, message, JSON.POST, locale, 42);
            } catch (JsonException ex) {
                exception = ex;
            }
            Assert.assertEquals("throw", memory1.getCurrentReport());
            Assert.assertNull(exception);
        } catch (IOException | JmriException | JsonException ex) {
            Assert.fail(ex.getMessage());
        }
    }

    // The minimal setup for log4J
    @Before
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initReporterManager();
    }

    @After
    public void tearDown() throws Exception {
        JUnitUtil.tearDown();
    }

}
