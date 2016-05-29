package jmri.server.json.memory;

import apps.tests.Log4JFixture;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Locale;
import javax.servlet.http.HttpServletResponse;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Memory;
import jmri.MemoryManager;
import jmri.server.json.JSON;
import jmri.server.json.JsonException;
import jmri.util.JUnitUtil;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 *
 * @author Paul Bender
 * @author Randall Wood
 */
public class JsonMemoryHttpServiceTest extends TestCase {

    public void testCtorSuccess() {
        JsonMemoryHttpService service = new JsonMemoryHttpService(new ObjectMapper());
        Assert.assertNotNull(service);
    }

    public void testDoGet() throws JmriException {
        JsonMemoryHttpService service = new JsonMemoryHttpService(new ObjectMapper());
        MemoryManager manager = InstanceManager.getDefault(MemoryManager.class);
        Memory memory1 = manager.provideMemory("IM1"); // no value
        JsonNode result;
        try {
            result = service.doGet(JsonMemoryServiceFactory.MEMORY, "IM1", Locale.ENGLISH);
            Assert.assertNotNull(result);
            Assert.assertEquals("IM1", result.path(JSON.DATA).path(JSON.NAME).asText());
            // JSON node has the text "null" if memory is null
            Assert.assertEquals("null", result.path(JSON.DATA).path(JSON.VALUE).asText());
            memory1.setValue("throw");
            result = service.doGet(JsonMemoryServiceFactory.MEMORY, "IM1", Locale.ENGLISH);
            Assert.assertNotNull(result);
            Assert.assertEquals("throw", result.path(JSON.DATA).path(JSON.VALUE).asText());
            memory1.setValue("close");
            result = service.doGet(JsonMemoryServiceFactory.MEMORY, "IM1", Locale.ENGLISH);
            Assert.assertNotNull(result);
            Assert.assertEquals("close", result.path(JSON.DATA).path(JSON.VALUE).asText());
        } catch (JsonException ex) {
            Assert.fail(ex.getMessage());
        }
    }

    public void testDoPost() throws JmriException {
        ObjectMapper mapper = new ObjectMapper();
        JsonMemoryHttpService service = new JsonMemoryHttpService(mapper);
        MemoryManager manager = InstanceManager.getDefault(MemoryManager.class);
        Memory memory1 = manager.provideMemory("IM1");
        JsonNode result;
        JsonNode message;
        try {
            // set off
            message = mapper.createObjectNode().put(JSON.NAME, "IM1").put(JSON.VALUE, "close");
            result = service.doPost(JsonMemoryServiceFactory.MEMORY, "IM1", message, Locale.ENGLISH);
            Assert.assertEquals("close", memory1.getValue());
            Assert.assertNotNull(result);
            Assert.assertEquals("close", result.path(JSON.DATA).path(JSON.VALUE).asText());
            // set on
            message = mapper.createObjectNode().put(JSON.NAME, "IM1").put(JSON.VALUE, "throw");
            result = service.doPost(JsonMemoryServiceFactory.MEMORY, "IM1", message, Locale.ENGLISH);
            Assert.assertEquals("throw", memory1.getValue());
            Assert.assertNotNull(result);
            Assert.assertEquals("throw", result.path(JSON.DATA).path(JSON.VALUE).asText());
            // set null
            message = mapper.createObjectNode().put(JSON.NAME, "IM1").putNull(JSON.VALUE);
            result = service.doPost(JsonMemoryServiceFactory.MEMORY, "IM1", message, Locale.ENGLISH);
            Assert.assertNull(memory1.getValue());
            Assert.assertEquals("null", result.path(JSON.DATA).path(JSON.VALUE).asText());
        } catch (JsonException ex) {
            Assert.fail(ex.getMessage());
        }
    }

    public void testDoPut() {
        ObjectMapper mapper = new ObjectMapper();
        JsonMemoryHttpService service = new JsonMemoryHttpService(mapper);
        MemoryManager manager = InstanceManager.getDefault(MemoryManager.class);
        JsonNode result;
        JsonNode message;
        try {
            // add a memory
            Assert.assertNull(manager.getMemory("IM1"));
            message = mapper.createObjectNode().put(JSON.NAME, "IM1").put(JSON.VALUE, "close");
            result = service.doPut(JsonMemoryServiceFactory.MEMORY, "IM1", message, Locale.ENGLISH);
            Assert.assertNotNull(manager.getMemory("IM1"));
        } catch (JsonException ex) {
            Assert.fail(ex.getMessage());
        }
    }
    
    public void testDoGetList() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonMemoryHttpService service = new JsonMemoryHttpService(mapper);
            MemoryManager manager = InstanceManager.getDefault(MemoryManager.class);
            JsonNode result;
            result = service.doGetList(JsonMemoryServiceFactory.MEMORY, Locale.ENGLISH);
            Assert.assertNotNull(result);
            Assert.assertEquals(0, result.size());
            Memory memory1 = manager.provideMemory("IM1");
            Memory memory2 = manager.provideMemory("IM2");
            result = service.doGetList(JsonMemoryServiceFactory.MEMORY, Locale.ENGLISH);
            Assert.assertNotNull(result);
            Assert.assertEquals(2, result.size());
        } catch (JsonException ex) {
            Assert.fail(ex.getMessage());
        }
    }
    
    public void testDelete() {
        try {
            (new JsonMemoryHttpService(new ObjectMapper())).doDelete(JsonMemoryServiceFactory.MEMORY, null, Locale.ENGLISH);
        } catch (JsonException ex) {
            Assert.assertEquals(HttpServletResponse.SC_METHOD_NOT_ALLOWED, ex.getCode());
            return;
        }
        Assert.fail("Did not throw expected error.");
    }
    
    // from here down is testing infrastructure
    public JsonMemoryHttpServiceTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {JsonMemoryHttpServiceTest.class.getName()};
        TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(JsonMemoryHttpServiceTest.class);

        return suite;
    }

    // The minimal setup for log4J
    @Override
    protected void setUp() throws Exception {
        Log4JFixture.setUp();
        super.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initMemoryManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initDebugThrottleManager();
    }

    @Override
    protected void tearDown() throws Exception {
        JUnitUtil.resetInstanceManager();
        super.tearDown();
        Log4JFixture.tearDown();
    }

}
