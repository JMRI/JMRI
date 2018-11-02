package jmri.server.json.memory;

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
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender
 * @author Randall Wood
 */
public class JsonMemorySocketServiceTest {

    @Test
    public void testMemoryChange() {
        try {
            JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
            JsonNode message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "IM1");
            JsonMemorySocketService service = new JsonMemorySocketService(connection);
            MemoryManager manager = InstanceManager.getDefault(MemoryManager.class);
            Memory memory1 = manager.provideMemory("IM1");
            service.onMessage(JsonMemory.MEMORY, message, JSON.POST, Locale.ENGLISH);
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

    @Test
    public void testOnMessageChange() {
        try {
            JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
            JsonNode message;
            JsonMemorySocketService service = new JsonMemorySocketService(connection);
            MemoryManager manager = InstanceManager.getDefault(MemoryManager.class);
            Memory memory1 = manager.provideMemory("IM1");
            // Memory "close"
            message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "IM1").put(JSON.VALUE, "close");
            service.onMessage(JsonMemory.MEMORY, message, JSON.POST, Locale.ENGLISH);
            Assert.assertEquals("close", memory1.getValue());
            // Memory "throw"
            message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "IM1").put(JSON.VALUE, "throw");
            service.onMessage(JsonMemory.MEMORY, message, JSON.POST, Locale.ENGLISH);
            Assert.assertEquals("throw", memory1.getValue());
            // Memory UNKNOWN - remains ON
            message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "IM1").putNull(JSON.VALUE);
            service.onMessage(JsonMemory.MEMORY, message, JSON.POST, Locale.ENGLISH);
            Assert.assertEquals(null, memory1.getValue());
            memory1.setValue("throw");
            // Memory no value
            message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "IM1");
            JsonException exception = null;
            try {
                service.onMessage(JsonMemory.MEMORY, message, JSON.POST, Locale.ENGLISH);
            } catch (JsonException ex) {
                exception = ex;
            }
            Assert.assertEquals("throw", memory1.getValue());
            Assert.assertNull(exception);
        } catch (IOException | JmriException | JsonException ex) {
            Assert.fail(ex.getMessage());
        }
    }

    @Before
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initMemoryManager();
    }

    @After
    public void tearDown() throws Exception {
        JUnitUtil.tearDown();
    }

}
