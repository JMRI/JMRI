package jmri.server.json.memory;

import apps.tests.Log4JFixture;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Locale;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Memory;
import jmri.MemoryManager;
import jmri.server.json.JSON;
import jmri.server.json.JsonException;
import jmri.server.json.JsonMockConnection;
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
public class JsonMemorySocketServiceTest extends TestCase {

    public void testCtorSuccess() {
        JsonMemorySocketService service = new JsonMemorySocketService(new JsonMockConnection((DataOutputStream) null));
        Assert.assertNotNull(service);
    }

    public void testMemoryChange() {
        try {
            JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
            JsonNode message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "IM1");
            JsonMemorySocketService service = new JsonMemorySocketService(connection);
            MemoryManager manager = InstanceManager.getDefault(MemoryManager.class);
            Memory memory1 = manager.provideMemory("IM1");
            service.onMessage(JsonMemoryServiceFactory.MEMORY, message, Locale.ENGLISH);
            // TODO: test that service is listener in MemoryManager
            // default null value of memory1 has text representation "null" in JSON
            Assert.assertEquals("null", connection.getMessage().path(JSON.DATA).path(JSON.VALUE).asText());
            memory1.setValue("throw");
            JUnitUtil.waitFor(() -> {
                return memory1.getValue().equals("throw");
            }, "Memory to throw");
            Assert.assertEquals("throw", connection.getMessage().path(JSON.DATA).path(JSON.VALUE).asText());
            memory1.setValue("close");
            JUnitUtil.waitFor(() -> {
                return memory1.getValue().equals("close");
            }, "Memory to close");
            Assert.assertEquals("close", memory1.getValue());
            Assert.assertEquals("close", connection.getMessage().path(JSON.DATA).path(JSON.VALUE).asText());
            service.onClose();
            // TODO: test that service is no longer a listener in MemoryManager
        } catch (IOException | JmriException | JsonException ex) {
            Assert.fail(ex.getMessage());
        }
    }

    public void testOnMessageChange() {
        try {
            JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
            JsonNode message;
            JsonMemorySocketService service = new JsonMemorySocketService(connection);
            MemoryManager manager = InstanceManager.getDefault(MemoryManager.class);
            Memory memory1 = manager.provideMemory("IM1");
            // Memory "close"
            message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "IM1").put(JSON.VALUE, "close");
            service.onMessage(JsonMemoryServiceFactory.MEMORY, message, Locale.ENGLISH);
            Assert.assertEquals("close", memory1.getValue());
            // Memory "throw"
            message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "IM1").put(JSON.VALUE, "throw");
            service.onMessage(JsonMemoryServiceFactory.MEMORY, message, Locale.ENGLISH);
            Assert.assertEquals("throw", memory1.getValue());
            // Memory UNKNOWN - remains ON
            message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "IM1").putNull(JSON.VALUE);
            service.onMessage(JsonMemoryServiceFactory.MEMORY, message, Locale.ENGLISH);
            Assert.assertEquals(null, memory1.getValue());
            memory1.setValue("throw");
            // Memory no value
            message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "IM1");
            JsonException exception = null;
            try {
                service.onMessage(JsonMemoryServiceFactory.MEMORY, message, Locale.ENGLISH);
            } catch (JsonException ex) {
                exception = ex;
            }
            Assert.assertEquals("throw", memory1.getValue());
            Assert.assertNull(exception);
        } catch (IOException | JmriException | JsonException ex) {
            Assert.fail(ex.getMessage());
        }
    }

    // from here down is testing infrastructure
    public JsonMemorySocketServiceTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {JsonMemorySocketServiceTest.class.getName()};
        TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(JsonMemorySocketServiceTest.class);

        return suite;
    }

    // The minimal setup for log4J
    @Override
    protected void setUp() throws Exception {
        Log4JFixture.setUp();
        super.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initMemoryManager();
    }

    @Override
    protected void tearDown() throws Exception {
        JUnitUtil.resetInstanceManager();
        super.tearDown();
        Log4JFixture.tearDown();
    }

}
