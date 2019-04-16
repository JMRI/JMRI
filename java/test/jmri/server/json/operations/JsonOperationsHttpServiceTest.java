package jmri.server.json.operations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Locale;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import jmri.server.json.JSON;
import jmri.server.json.JsonException;
import jmri.util.JUnitUtil;

public class JsonOperationsHttpServiceTest {

    private JsonOperationsHttpService service;
    private Locale locale = Locale.ENGLISH;

    @Test
    public void testKernels() throws JsonException {
        JsonNode result = service.doGetList(JsonOperations.KERNEL, locale);
        assertNotNull(result);
        assertTrue(result.isArray());
        assertEquals(0, result.size());
        result = service.doPut(JsonOperations.KERNEL, "test1", service.getObjectMapper().createObjectNode(), locale);
        assertNotNull(result);
        assertEquals(JsonOperations.KERNEL, result.path(JSON.TYPE).asText());
        assertEquals("test1", result.path(JSON.DATA).path(JSON.NAME).asText());
        assertEquals(0, result.path(JSON.DATA).path(JSON.LENGTH).asInt());
        assertEquals(0, result.path(JSON.DATA).path(JsonOperations.WEIGHT).asInt());
        assertTrue(result.path(JSON.DATA).path(JsonOperations.CARS).isArray());
        JsonNode result2 = service.doGet(JsonOperations.KERNEL, "test1", locale);
        assertEquals(result, result2);
        result = service.doGetList(JsonOperations.KERNEL, locale);
        assertNotNull(result);
        assertTrue(result.isArray());
        assertEquals(1, result.size());
        assertEquals(JsonOperations.KERNEL, result.path(0).path(JSON.TYPE).asText());
        assertEquals("test1", result.path(0).path(JSON.DATA).path(JSON.NAME).asText());
        assertEquals(0, result.path(0).path(JSON.DATA).path(JSON.LENGTH).asInt());
        assertEquals(0, result.path(0).path(JSON.DATA).path(JsonOperations.WEIGHT).asInt());
        assertTrue(result.path(0).path(JSON.DATA).path(JsonOperations.CARS).isArray());
        service.doDelete(JsonOperations.KERNEL, "test1", service.getObjectMapper().createObjectNode(), locale);
        result = service.doGetList(JsonOperations.KERNEL, locale);
        assertNotNull(result);
        assertTrue(result.isArray());
        assertEquals(0, result.size());
    }

    @Before
    public void setUp() {
        service = new JsonOperationsHttpService(new ObjectMapper());
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        service = null;
        JUnitUtil.tearDown();
    }
}