package jmri.server.json.memory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;

import java.util.Locale;
import javax.servlet.http.HttpServletResponse;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Memory;
import jmri.MemoryManager;
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
public class JsonMemoryHttpServiceTest {

    private Locale locale = Locale.ENGLISH;

    @Test
    public void testDoGet() throws JmriException {
        JsonMemoryHttpService service = new JsonMemoryHttpService(new ObjectMapper());
        MemoryManager manager = InstanceManager.getDefault(MemoryManager.class);
        Memory memory1 = manager.provideMemory("IM1"); // no value
        JsonNode result;
        try {
            result = service.doGet(JsonMemory.MEMORY, "IM1", service.getObjectMapper().createObjectNode(), locale, 42);
            Assert.assertNotNull(result);
            Assert.assertEquals(JsonMemory.MEMORY, result.path(JSON.TYPE).asText());
            Assert.assertEquals("IM1", result.path(JSON.DATA).path(JSON.NAME).asText());
            // JSON node has the text "null" if memory is null
            Assert.assertEquals("null", result.path(JSON.DATA).path(JSON.VALUE).asText());
            memory1.setValue("throw");
            result = service.doGet(JsonMemory.MEMORY, "IM1", service.getObjectMapper().createObjectNode(), locale, 42);
            Assert.assertNotNull(result);
            Assert.assertEquals("throw", result.path(JSON.DATA).path(JSON.VALUE).asText());
            memory1.setValue("close");
            result = service.doGet(JsonMemory.MEMORY, "IM1", service.getObjectMapper().createObjectNode(), locale, 42);
            Assert.assertNotNull(result);
            Assert.assertEquals("close", result.path(JSON.DATA).path(JSON.VALUE).asText());
        } catch (JsonException ex) {
            Assert.fail(ex.getMessage());
        }
    }

    @Test
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
            result = service.doPost(JsonMemory.MEMORY, "IM1", message, locale, 42);
            Assert.assertEquals("close", memory1.getValue());
            Assert.assertNotNull(result);
            Assert.assertEquals("close", result.path(JSON.DATA).path(JSON.VALUE).asText());
            // set on
            message = mapper.createObjectNode().put(JSON.NAME, "IM1").put(JSON.VALUE, "throw");
            result = service.doPost(JsonMemory.MEMORY, "IM1", message, locale, 42);
            Assert.assertEquals("throw", memory1.getValue());
            Assert.assertNotNull(result);
            Assert.assertEquals("throw", result.path(JSON.DATA).path(JSON.VALUE).asText());
            // set null
            message = mapper.createObjectNode().put(JSON.NAME, "IM1").putNull(JSON.VALUE);
            result = service.doPost(JsonMemory.MEMORY, "IM1", message, locale, 42);
            Assert.assertNull(memory1.getValue());
            Assert.assertEquals("null", result.path(JSON.DATA).path(JSON.VALUE).asText());
        } catch (JsonException ex) {
            Assert.fail(ex.getMessage());
        }
    }

    @Test
    public void testDoPut() {
        ObjectMapper mapper = new ObjectMapper();
        JsonMemoryHttpService service = new JsonMemoryHttpService(mapper);
        MemoryManager manager = InstanceManager.getDefault(MemoryManager.class);
        JsonNode message;
        try {
            // add a memory
            Assert.assertNull(manager.getMemory("IM1"));
            message = mapper.createObjectNode().put(JSON.NAME, "IM1").put(JSON.VALUE, "close");
            service.doPut(JsonMemory.MEMORY, "IM1", message, locale, 42);
            Assert.assertNotNull(manager.getMemory("IM1"));
        } catch (JsonException ex) {
            Assert.fail(ex.getMessage());
        }
    }

    @Test
    public void testDoGetList() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonMemoryHttpService service = new JsonMemoryHttpService(mapper);
            MemoryManager manager = InstanceManager.getDefault(MemoryManager.class);
            JsonNode result;
            result = service.doGetList(JsonMemory.MEMORY, NullNode.getInstance(), locale, 0);
            Assert.assertNotNull(result);
            Assert.assertEquals(0, result.size());
            manager.provideMemory("IM1");
            manager.provideMemory("IM2");
            result = service.doGetList(JsonMemory.MEMORY, NullNode.getInstance(), locale, 0);
            Assert.assertNotNull(result);
            Assert.assertEquals(2, result.size());
        } catch (JsonException ex) {
            Assert.fail(ex.getMessage());
        }
    }

    @Test
    public void testDelete() {
        try {
            (new JsonMemoryHttpService(new ObjectMapper())).doDelete(JsonMemory.MEMORY, "", NullNode.getInstance(), locale, 42);
        } catch (JsonException ex) {
            Assert.assertEquals(HttpServletResponse.SC_METHOD_NOT_ALLOWED, ex.getCode());
            return;
        }
        Assert.fail("Did not throw expected error.");
    }

    @Before
    public void setUp() throws Exception {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() throws Exception {
        JUnitUtil.tearDown();
    }

}
