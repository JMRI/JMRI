package jmri.server.json.reporter;

import static jmri.server.json.reporter.JsonReporter.REPORTER;

import apps.tests.Log4JFixture;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Locale;
import javax.servlet.http.HttpServletResponse;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Reporter;
import jmri.ReporterManager;
import jmri.server.json.JSON;
import jmri.server.json.JsonException;
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
    public void testCtorSuccess() {
        JsonReporterHttpService service = new JsonReporterHttpService(new ObjectMapper());
        Assert.assertNotNull(service);
    }

    @Test
    public void testDoGet() throws JmriException {
        JsonReporterHttpService service = new JsonReporterHttpService(new ObjectMapper());
        ReporterManager manager = InstanceManager.getDefault(ReporterManager.class);
        Reporter reporter1 = manager.provideReporter("IR1"); // no value
        JsonNode result;
        try {
            result = service.doGet(REPORTER, "IR1", Locale.ENGLISH);
            Assert.assertNotNull(result);
            Assert.assertEquals("IR1", result.path(JSON.DATA).path(JSON.NAME).asText());
            // JSON node has the text "null" if reporter is null
            Assert.assertEquals("null", result.path(JSON.DATA).path(JsonReporter.REPORT).asText());
            reporter1.setReport("throw");
            result = service.doGet(REPORTER, "IR1", Locale.ENGLISH);
            Assert.assertNotNull(result);
            Assert.assertEquals("throw", result.path(JSON.DATA).path(JsonReporter.REPORT).asText());
            reporter1.setReport("close");
            result = service.doGet(REPORTER, "IR1", Locale.ENGLISH);
            Assert.assertNotNull(result);
            Assert.assertEquals("close", result.path(JSON.DATA).path(JsonReporter.REPORT).asText());
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
        JsonNode result;
        JsonNode message;
        try {
            // set off
            message = mapper.createObjectNode().put(JSON.NAME, "IR1").put(JsonReporter.REPORT, "close");
            result = service.doPost(REPORTER, "IR1", message, Locale.ENGLISH);
            Assert.assertEquals("close", reporter1.getCurrentReport());
            Assert.assertNotNull(result);
            Assert.assertEquals("close", result.path(JSON.DATA).path(JsonReporter.REPORT).asText());
            // set on
            message = mapper.createObjectNode().put(JSON.NAME, "IR1").put(JsonReporter.REPORT, "throw");
            result = service.doPost(REPORTER, "IR1", message, Locale.ENGLISH);
            Assert.assertEquals("throw", reporter1.getCurrentReport());
            Assert.assertNotNull(result);
            Assert.assertEquals("throw", result.path(JSON.DATA).path(JsonReporter.REPORT).asText());
            // set null
            message = mapper.createObjectNode().put(JSON.NAME, "IR1").putNull(JsonReporter.REPORT);
            result = service.doPost(REPORTER, "IR1", message, Locale.ENGLISH);
            Assert.assertNull(reporter1.getCurrentReport());
            Assert.assertEquals("null", result.path(JSON.DATA).path(JsonReporter.REPORT).asText());
        } catch (JsonException ex) {
            Assert.fail(ex.getMessage());
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
    }
    
    @Test
    public void testDoGetList() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonReporterHttpService service = new JsonReporterHttpService(mapper);
            ReporterManager manager = InstanceManager.getDefault(ReporterManager.class);
            JsonNode result;
            result = service.doGetList(REPORTER, Locale.ENGLISH);
            Assert.assertNotNull(result);
            Assert.assertEquals(0, result.size());
            manager.provideReporter("IR1");
            manager.provideReporter("IR2");
            result = service.doGetList(REPORTER, Locale.ENGLISH);
            Assert.assertNotNull(result);
            Assert.assertEquals(2, result.size());
        } catch (JsonException ex) {
            Assert.fail(ex.getMessage());
        }
    }
    
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
        Log4JFixture.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initReporterManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initDebugThrottleManager();
    }

    @After
    public void tearDown() throws Exception {
        JUnitUtil.resetInstanceManager();
        Log4JFixture.tearDown();
    }

}
