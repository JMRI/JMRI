package jmri.server.json.memory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;

import jmri.IdTagManager;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Memory;
import jmri.MemoryManager;
import jmri.ReporterManager;
import jmri.server.json.JSON;
import jmri.server.json.JsonException;
import jmri.server.json.JsonNamedBeanHttpServiceTestBase;
import jmri.server.json.JsonRequest;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 *
 * @author Paul Bender
 * @author Randall Wood
 */
public class JsonMemoryHttpServiceTest extends JsonNamedBeanHttpServiceTestBase<Memory, JsonMemoryHttpService> {

    @Test
    @Override
    public void testDoGet() throws JmriException, JsonException {
        MemoryManager manager = InstanceManager.getDefault(MemoryManager.class);
        Memory memory1 = manager.provideMemory("IM1"); // no value
        // properties of each kind, to check the shape the schema describes
        memory1.setProperty("text", "value");
        memory1.setProperty("number", 42);
        memory1.setProperty("empty", null);
        JsonNode result;
        result = service.doGet(JsonMemory.MEMORY, "IM1", service.getObjectMapper().createObjectNode(), new JsonRequest(locale, JSON.V5, JSON.GET, 42));
        validate(result);
        assertEquals(JsonMemory.MEMORY, result.path(JSON.TYPE).asText());
        assertEquals("IM1", result.path(JSON.DATA).path(JSON.NAME).asText());
        // JSON node has the text "null" if memory is null
        assertEquals("null", result.path(JSON.DATA).path(JSON.VALUE).asText());
        memory1.setValue("throw");
        result = service.doGet(JsonMemory.MEMORY, "IM1", service.getObjectMapper().createObjectNode(), new JsonRequest(locale, JSON.V5, JSON.GET, 42));
        validate(result);
        assertEquals("throw", result.path(JSON.DATA).path(JSON.VALUE).asText());
        memory1.setValue("close");
        result = service.doGet(JsonMemory.MEMORY, "IM1", service.getObjectMapper().createObjectNode(), new JsonRequest(locale, JSON.V5, JSON.GET, 42));
        validate(result);
        assertEquals("close", result.path(JSON.DATA).path(JSON.VALUE).asText());
    }

    @Test
    public void testDoPost() throws JmriException, JsonException {
        MemoryManager manager = InstanceManager.getDefault(MemoryManager.class);
        Memory memory1 = manager.provideMemory("IM1");
        JsonNode result;
        JsonNode message;
        // set off
        message = mapper.createObjectNode().put(JSON.NAME, "IM1").put(JSON.VALUE, "close");
        result = service.doPost(JsonMemory.MEMORY, "IM1", message, new JsonRequest(locale, JSON.V5, JSON.GET, 42));
        assertEquals("close", memory1.getValue());
        validate(result);
        assertEquals("close", result.path(JSON.DATA).path(JSON.VALUE).asText());
        // set on
        message = mapper.createObjectNode().put(JSON.NAME, "IM1").put(JSON.VALUE, "throw");
        result = service.doPost(JsonMemory.MEMORY, "IM1", message, new JsonRequest(locale, JSON.V5, JSON.GET, 42));
        assertEquals("throw", memory1.getValue());
        validate(result);
        assertEquals("throw", result.path(JSON.DATA).path(JSON.VALUE).asText());
        // set null
        message = mapper.createObjectNode().put(JSON.NAME, "IM1").putNull(JSON.VALUE);
        result = service.doPost(JsonMemory.MEMORY, "IM1", message, new JsonRequest(locale, JSON.V5, JSON.GET, 42));
        assertNull(memory1.getValue());
        assertEquals("null", result.path(JSON.DATA).path(JSON.VALUE).asText());
    }

    @Test
    public void testDoPut() throws JsonException {
        MemoryManager manager = InstanceManager.getDefault(MemoryManager.class);
        JsonNode message;
        // add a memory
        assertNull(manager.getMemory("IM1"));
        message = mapper.createObjectNode().put(JSON.NAME, "IM1").put(JSON.VALUE, "close");
        service.doPut(JsonMemory.MEMORY, "IM1", message, new JsonRequest(locale, JSON.V5, JSON.GET, 42));
        assertNotNull(manager.getMemory("IM1"));
    }

    @Test
    public void testDoGetList() throws JsonException {
        MemoryManager manager = InstanceManager.getDefault(MemoryManager.class);
        JsonNode result;
        result = service.doGetList(JsonMemory.MEMORY, NullNode.getInstance(), new JsonRequest(locale, JSON.V5, JSON.GET, 0));
        validate(result);
        assertEquals(0, result.size());
        manager.provideMemory("IM1");
        manager.provideMemory("IM2");
        result = service.doGetList(JsonMemory.MEMORY, NullNode.getInstance(), new JsonRequest(locale, JSON.V5, JSON.GET, 0));
        validate(result);
        assertEquals(2, result.size());
    }

    @Test
    public void testDoGetObjectValues() throws JsonException {
        Memory memory1 = InstanceManager.getDefault(MemoryManager.class).provideMemory("IM1");
        // an IdTag or a Reporter as the value is sent as an object
        memory1.setValue(InstanceManager.getDefault(IdTagManager.class).provideIdTag("ID1"));
        JsonNode result = service.doGet(JsonMemory.MEMORY, "IM1", NullNode.getInstance(), new JsonRequest(locale, JSON.V5, JSON.GET, 42));
        validate(result);
        assertEquals("ID1", result.path(JSON.DATA).path(JSON.VALUE).path(JSON.DATA).path(JSON.NAME).asText());
        memory1.setValue(InstanceManager.getDefault(ReporterManager.class).provideReporter("IR1"));
        result = service.doGet(JsonMemory.MEMORY, "IM1", NullNode.getInstance(), new JsonRequest(locale, JSON.V5, JSON.GET, 42));
        validate(result);
        assertEquals("IR1", result.path(JSON.DATA).path(JSON.VALUE).path(JSON.DATA).path(JSON.NAME).asText());
    }

    @Test
    public void testClientSchema() throws JsonException {
        Memory memory1 = InstanceManager.getDefault(MemoryManager.class).provideMemory("IM1");
        memory1.setValue("a value");
        memory1.setComment("a comment");
        // nulls clear the value and the comment
        JsonNode data = mapper.createObjectNode().put(JSON.NAME, "IM1").putNull(JSON.VALUE).putNull(JSON.COMMENT);
        validateData(JsonMemory.MEMORY, data, false);
        service.doPost(JsonMemory.MEMORY, "IM1", data, new JsonRequest(locale, JSON.V5, JSON.POST, 42));
        assertNull(memory1.getValue());
        assertNull(memory1.getComment());
    }

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();
        service = new JsonMemoryHttpService(mapper);
        JUnitUtil.initIdTagManager();
        JUnitUtil.initReporterManager();
    }

    @AfterEach
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

}
