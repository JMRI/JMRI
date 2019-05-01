package jmri.server.json.reporter;

import static jmri.server.json.reporter.JsonReporter.REPORTER;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;

import java.util.Locale;
import javax.servlet.http.HttpServletResponse;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Reporter;
import jmri.ReporterManager;
import jmri.jmrix.internal.InternalReporterManager;
import jmri.server.json.JSON;
import jmri.server.json.JsonException;
import jmri.server.json.JsonNamedBeanHttpServiceTestBase;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender
 * @author Randall Wood
 */
public class JsonReporterHttpServiceTest extends JsonNamedBeanHttpServiceTestBase<Reporter, JsonReporterHttpService> {

    private Locale locale = Locale.ENGLISH;

    @Test
    public void testDoGet() throws JmriException, JsonException {
        ReporterManager manager = InstanceManager.getDefault(ReporterManager.class);
        Reporter reporter1 = manager.provideReporter("IR1"); // no value
        JsonNode result;
        result = service.doGet(REPORTER, "IR1", NullNode.getInstance(), locale, 42);
        validate(result);
        assertEquals(REPORTER, result.path(JSON.TYPE).asText());
        assertEquals("IR1", result.path(JSON.DATA).path(JSON.NAME).asText());
        // JSON node has the text "null" if reporter is null
        assertEquals("null", result.path(JSON.DATA).path(JsonReporter.REPORT).asText());
        reporter1.setReport("throw");
        result = service.doGet(REPORTER, "IR1", service.getObjectMapper().createObjectNode(), locale, 42);
        validate(result);
        assertEquals("throw", result.path(JSON.DATA).path(JsonReporter.REPORT).asText());
        reporter1.setReport("close");
        result = service.doGet(REPORTER, "IR1", service.getObjectMapper().createObjectNode(), locale, 42);
        validate(result);
        assertEquals("close", result.path(JSON.DATA).path(JsonReporter.REPORT).asText());
        // Request a non-existent reporter
        try {
            service.doGet(REPORTER, "IR2", service.getObjectMapper().createObjectNode(), locale, 42);
            fail("Expected exception not thrown.");
        } catch (JsonException ex) {
            assertEquals(404, ex.getCode());
        }
    }

    @Test
    public void testDoPost() throws JmriException, JsonException {
        ReporterManager manager = InstanceManager.getDefault(ReporterManager.class);
        Reporter reporter1 = manager.provideReporter("IR1");
        reporter1.setUserName("test reporter");
        JsonNode result;
        JsonNode message;
        // set non-null report
        message = mapper.createObjectNode().put(JSON.NAME, "IR1").put(JsonReporter.REPORT, "close");
        result = service.doPost(REPORTER, "IR1", message, locale, 42);
        assertEquals("close", reporter1.getCurrentReport());
        validate(result);
        assertEquals("close", result.path(JSON.DATA).path(JsonReporter.REPORT).asText());
        // set different non-null report 
        message = mapper.createObjectNode().put(JSON.NAME, "IR1").put(JsonReporter.REPORT, "throw");
        result = service.doPost(REPORTER, "IR1", message, locale, 42);
        assertEquals("throw", reporter1.getCurrentReport());
        validate(result);
        assertEquals("throw", result.path(JSON.DATA).path(JsonReporter.REPORT).asText());
        // set null report
        message = mapper.createObjectNode().put(JSON.NAME, "IR1").putNull(JsonReporter.REPORT);
        result = service.doPost(REPORTER, "IR1", message, locale, 42);
        assertNull(reporter1.getCurrentReport());
        assertEquals("null", result.path(JSON.DATA).path(JsonReporter.REPORT).asText());
        // set new user name
        message = mapper.createObjectNode().put(JSON.NAME, "IR1").put(JSON.USERNAME, "TEST REPORTER");
        assertEquals("expected name", "test reporter", reporter1.getUserName());
        result = service.doPost(REPORTER, "IR1", message, locale, 42);
        assertEquals("new name", "TEST REPORTER", reporter1.getUserName());
        validate(result);
        assertEquals("new name in JSON", "TEST REPORTER", result.path(JSON.DATA).path(JSON.USERNAME).asText());
        // set comment
        message = mapper.createObjectNode().put(JSON.NAME, "IR1").put(JSON.COMMENT, "a comment");
        assertNull("null comment", reporter1.getComment());
        result = service.doPost(REPORTER, "IR1", message, locale, 42);
        assertEquals("new comment", "a comment", reporter1.getComment());
        validate(result);
        assertEquals("new comment in JSON", "a comment", result.path(JSON.DATA).path(JSON.COMMENT).asText());
        // post non-existent reporter
        try {
            // set off
            message = mapper.createObjectNode().put(JSON.NAME, "JR1").put(JsonReporter.REPORT, "close");
            result = service.doPost(REPORTER, "JR1", message, locale, 42);
            fail("Expected exception not thrown");
        } catch (JsonException ex) {
            assertEquals("Not found thrown", 404, ex.getCode());
        }
    }

    @Test
    public void testDoPut() throws JsonException {
        ReporterManager manager = InstanceManager.getDefault(ReporterManager.class);
        JsonNode message;
        // add a reporter
        assertNull(manager.getReporter("IR1"));
        message = mapper.createObjectNode().put(JSON.NAME, "IR1").put(JsonReporter.REPORT, "close");
        service.doPut(REPORTER, "IR1", message, locale, 42);
        assertNotNull(manager.getReporter("IR1"));
        // add a reporter with invalid name
        assertNull(manager.getReporter("JR1"));
        // create a default reporter manager that overrides provide() to require valid system name
        // this allows testing that invalid names are reported to clients correctly
        InstanceManager.setDefault(ReporterManager.class, new InternalReporterManager() {
            @Override
            public Reporter provide(String name) {
                return this.newReporter(name, null);
            }
        });
        message = mapper.createObjectNode().put(JSON.NAME, "JR1").put(JsonReporter.REPORT, "close");
        try {
            service.doPut(REPORTER, "JR1", message, locale, 42);
            fail("JR1 should not have been created");
        } catch (JsonException ex) {
            JUnitAppender.assertErrorMessageStartsWith("Invalid system name for reporter");
            assertEquals("400 name was invalid", 400, ex.getCode());
        }
    }

    @Test
    public void testDoGetList() throws JsonException {
        ReporterManager manager = InstanceManager.getDefault(ReporterManager.class);
        JsonNode result;
        result = service.doGetList(REPORTER, mapper.createObjectNode(), locale, 0);
        validate(result);
        assertEquals(0, result.size());
        manager.provideReporter("IR1");
        manager.provideReporter("IR2");
        result = service.doGetList(REPORTER, mapper.createObjectNode(), locale, 0);
        validate(result);
        assertEquals(2, result.size());
    }

    @Test
    public void testDelete() {
        try {
            service.doDelete(REPORTER, "", NullNode.getInstance(), locale, 42);
            fail("Did not throw expected error.");
        } catch (JsonException ex) {
            assertEquals(HttpServletResponse.SC_METHOD_NOT_ALLOWED, ex.getCode());
        }
    }

    // The minimal setup for log4J
    @Before
    public void setUp() throws Exception {
        super.setUp();
        service = new JsonReporterHttpService(mapper);
        JUnitUtil.initReporterManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initDebugThrottleManager();
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

}
