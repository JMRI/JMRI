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
public class JsonMemorySocketServiceTest {

    private final Locale locale = Locale.ENGLISH;

    @Test
    public void testMemoryChange() throws IOException, JmriException, JsonException {

        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        JsonNode message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "IM1");
        JsonMemorySocketService service = new JsonMemorySocketService(connection);
        MemoryManager manager = InstanceManager.getDefault(MemoryManager.class);
        Memory memory1 = manager.provideMemory("IM1");
        service.onMessage(JsonMemory.MEMORY, message, new JsonRequest(locale, JSON.V5, JSON.POST, 42));
        // TODO: test that service is listener in MemoryManager
        // default null value of memory1 has text representation "null" in JSON
        message = connection.getMessage();
        assertNotNull( message, "message is not null");
        assertEquals("null", message.path(JSON.DATA).path(JSON.VALUE).asText());
        memory1.setValue("throw");
        JUnitUtil.waitFor(() -> {
            return memory1.getValue().equals("throw");
        }, "Memory to throw");
        message = connection.getMessage();
        assertNotNull( message, "message is not null");
        assertEquals("throw", message.path(JSON.DATA).path(JSON.VALUE).asText());
        memory1.setValue("close");
        JUnitUtil.waitFor(() -> {
            return memory1.getValue().equals("close");
        }, "Memory to close");
        assertEquals("close", memory1.getValue());
        message = connection.getMessage();
        assertNotNull( message, "message is not null");
        assertEquals("close", message.path(JSON.DATA).path(JSON.VALUE).asText());
        service.onClose();
        // TODO: test that service is no longer a listener in MemoryManager

    }

    @Test
    public void testOnMessageChange() throws IOException, JmriException, JsonException {

        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        JsonNode message;
        JsonMemorySocketService service = new JsonMemorySocketService(connection);
        MemoryManager manager = InstanceManager.getDefault(MemoryManager.class);
        Memory memory1 = manager.provideMemory("IM1");
        // Memory "close"
        message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "IM1").put(JSON.VALUE, "close");
        service.onMessage(JsonMemory.MEMORY, message, new JsonRequest(locale, JSON.V5, JSON.POST, 42));
        assertEquals("close", memory1.getValue());
        // Memory "throw"
        message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "IM1").put(JSON.VALUE, "throw");
        service.onMessage(JsonMemory.MEMORY, message, new JsonRequest(locale, JSON.V5, JSON.POST, 42));
        assertEquals("throw", memory1.getValue());
        // Memory UNKNOWN - remains ON
        message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "IM1").putNull(JSON.VALUE);
        service.onMessage(JsonMemory.MEMORY, message, new JsonRequest(locale, JSON.V5, JSON.POST, 42));
        assertNull( memory1.getValue());
        memory1.setValue("throw");
        // Memory no value
        JsonNode messageFinal = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "IM1");
        assertDoesNotThrow( () ->
            service.onMessage(JsonMemory.MEMORY, messageFinal,
                new JsonRequest(locale, JSON.V5, JSON.POST, 42)));

        assertEquals("throw", memory1.getValue());

    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initMemoryManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
