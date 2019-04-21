package jmri.server.json.reporter;

import static jmri.server.json.reporter.JsonReporter.REPORTER;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Locale;
import javax.servlet.http.HttpServletResponse;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Reporter;
import jmri.ReporterManager;
import jmri.jmrix.internal.InternalReporterManager;
import jmri.server.json.JSON;
import jmri.server.json.JsonException;
import jmri.util.JUnitAppender;
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
public class JsonReporterHttpServiceTest  {

    @Test
    public void testDoGet() throws JmriException {
        JsonReporterHttpService service = new JsonReporterHttpService(new ObjectMapper());
        ReporterManager manager = InstanceManager.getDefault(ReporterManager.class);
        Reporter reporter1 = manager.provideReporter("IR1"); // no value
        JsonNode result;
        try {
            result = service.doGet(REPORTER, "IR1", service.getObjectMapper().createObjectNode(), Locale.ENGLISH);
            Assert.assertNotNull(result);
            Assert.assertEquals(REPORTER, result.path(JSON.TYPE).asText());
            Assert.assertEquals("IR1", result.path(JSON.DATA).path(JSON.NAME).asText());
            // JSON node has the text "null" if reporter is null
            Assert.assertEquals("null", result.path(JSON.DATA).path(JsonReporter.REPORT).asText());
            reporter1.setReport("throw");
            result = service.doGet(REPORTER, "IR1", service.getObjectMapper().createObjectNode(), Locale.ENGLISH);
            Assert.assertNotNull(result);
            Assert.assertEquals("throw", result.path(JSON.DATA).path(JsonReporter.REPORT).asText());
            reporter1.setReport("close");
            result = service.doGet(REPORTER, "IR1", service.getObjectMapper().createObjectNode(), Locale.ENGLISH);
            Assert.assertNotNull(result);
            Assert.assertEquals("close", result.path(JSON.DATA).path(JsonReporter.REPORT).asText());
            // Request a non-existent reporter
            try {
                service.doGet(REPORTER, "IR2", service.getObjectMapper().createObjectNode(), Locale.ENGLISH);
                Assert.fail("Expected exception not thrown.");
            } catch (JsonException ex) {
                Assert.assertEquals(404, ex.getCode());
            }
        } catch (JsonException ex) {
            Assert.fail(ex.getMessage());
        }
    }

    @Test
    public void testDoPost() throws JmriException {
        ObjectMapper mapper = new ObjectMapper();
        JsonReporterHttpService service = new JsonReporterHttpService(mapper);
        ReporterManager manager = InstanceManager.getDefault(ReporterManager.class);
        Reporter reporter1 = manager.provideReporter("IR1");
        reporter1.setUserName("test reporter");
        JsonNode result;
        JsonNode message;
        try {
            // set non-null report
            message = mapper.createObjectNode().put(JSON.NAME, "IR1").put(JsonReporter.REPORT, "close");
            result = service.doPost(REPORTER, "IR1", message, Locale.ENGLISH);
            Assert.assertEquals("close", reporter1.getCurrentReport());
            Assert.assertNotNull(result);
            Assert.assertEquals("close", result.path(JSON.DATA).path(JsonReporter.REPORT).asText());
            // set different non-null report 
            message = mapper.createObjectNode().put(JSON.NAME, "IR1").put(JsonReporter.REPORT, "throw");
            result = service.doPost(REPORTER, "IR1", message, Locale.ENGLISH);
            Assert.assertEquals("throw", reporter1.getCurrentReport());
            Assert.assertNotNull(result);
            Assert.assertEquals("throw", result.path(JSON.DATA).path(JsonReporter.REPORT).asText());
            // set null report
            message = mapper.createObjectNode().put(JSON.NAME, "IR1").putNull(JsonReporter.REPORT);
            result = service.doPost(REPORTER, "IR1", message, Locale.ENGLISH);
            Assert.assertNull(reporter1.getCurrentReport());
            Assert.assertEquals("null", result.path(JSON.DATA).path(JsonReporter.REPORT).asText());
            // set new user name
            message = mapper.createObjectNode().put(JSON.NAME, "IR1").put(JSON.USERNAME, "TEST REPORTER");
            Assert.assertEquals("expected name", "test reporter", reporter1.getUserName());
            result = service.doPost(REPORTER, "IR1", message, Locale.ENGLISH);
            Assert.assertEquals("new name", "TEST REPORTER", reporter1.getUserName());
            Assert.assertNotNull(result);
            Assert.assertEquals("new name in JSON", "TEST REPORTER", result.path(JSON.DATA).path(JSON.USERNAME).asText());
            // set comment
            message = mapper.createObjectNode().put(JSON.NAME, "IR1").put(JSON.COMMENT, "a comment");
            Assert.assertNull("null comment", reporter1.getComment());
            result = service.doPost(REPORTER, "IR1", message, Locale.ENGLISH);
            Assert.assertEquals("new comment", "a comment", reporter1.getComment());
            Assert.assertNotNull(result);
            Assert.assertEquals("new comment in JSON", "a comment", result.path(JSON.DATA).path(JSON.COMMENT).asText());
        } catch (JsonException ex) {
            Assert.fail(ex.getMessage());
        }
        // post non-existent reporter
        try {
            // set off
            message = mapper.createObjectNode().put(JSON.NAME, "JR1").put(JsonReporter.REPORT, "close");
            result = service.doPost(REPORTER, "JR1", message, Locale.ENGLISH);
            Assert.fail("Expected exception not thrown");
        } catch (JsonException ex) {
            Assert.assertEquals("Not found thrown", 404, ex.getCode());
        }
    }

    @Test
    public void testDoPut() {
        ObjectMapper mapper = new ObjectMapper();
        JsonReporterHttpService service = new JsonReporterHttpService(mapper);
        ReporterManager manager = InstanceManager.getDefault(ReporterManager.class);
        JsonNode message;
        try {
            // add a reporter
            Assert.assertNull(manager.getReporter("IR1"));
            message = mapper.createObjectNode().put(JSON.NAME, "IR1").put(JsonReporter.REPORT, "close");
            service.doPut(REPORTER, "IR1", message, Locale.ENGLISH);
            Assert.assertNotNull(manager.getReporter("IR1"));
        } catch (JsonException ex) {
            Assert.fail(ex.getMessage());
        }
        // add a reporter with invalid name
        Assert.assertNull(manager.getReporter("JR1"));
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
            service.doPut(REPORTER, "JR1", message, Locale.ENGLISH);
            Assert.fail("JR1 should not have been created");
        } catch (JsonException ex) {
            JUnitAppender.assertErrorMessageStartsWith("Invalid system name for reporter");
            Assert.assertEquals("400 name was invalid", 400, ex.getCode());
        }
    }

    @Test
    public void testDoGetList() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonReporterHttpService service = new JsonReporterHttpService(mapper);
            ReporterManager manager = InstanceManager.getDefault(ReporterManager.class);
            JsonNode result;
            result = service.doGetList(REPORTER, mapper.createObjectNode(), Locale.ENGLISH);
            Assert.assertNotNull(result);
            Assert.assertEquals(0, result.size());
            manager.provideReporter("IR1");
            manager.provideReporter("IR2");
            result = service.doGetList(REPORTER, mapper.createObjectNode(), Locale.ENGLISH);
            Assert.assertNotNull(result);
            Assert.assertEquals(2, result.size());
        } catch (JsonException ex) {
            Assert.fail(ex.getMessage());
        }
    }

    @SuppressWarnings("null")
    @Test
    public void testDelete() {
        try {
            (new JsonReporterHttpService(new ObjectMapper())).doDelete(REPORTER, null, Locale.ENGLISH);
        } catch (JsonException ex) {
            Assert.assertEquals(HttpServletResponse.SC_METHOD_NOT_ALLOWED, ex.getCode());
            return;
        }
        Assert.fail("Did not throw expected error.");
    }

    // The minimal setup for log4J
    @Before
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        JUnitUtil.initReporterManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initDebugThrottleManager();
    }

    @After
    public void tearDown() throws Exception {
        JUnitUtil.tearDown();
    }

}
