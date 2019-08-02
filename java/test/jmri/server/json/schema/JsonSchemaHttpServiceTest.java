package jmri.server.json.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;

import jmri.server.json.JSON;
import jmri.server.json.JsonException;
import jmri.server.json.JsonHttpServiceTestBase;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Randall Wood Copyright 2018
 */
public class JsonSchemaHttpServiceTest extends JsonHttpServiceTestBase<JsonSchemaHttpService> {

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        service = new JsonSchemaHttpService(mapper);
    }

    @After
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test of doGet method, of class JsonSchemaHttpService.
     *
     * @throws jmri.server.json.JsonException on unexpected exception
     */
    @Test
    public void testDoGet() throws JsonException {
        JsonNode result = service.doGet(JSON.SCHEMA, JSON.JSON, NullNode.getInstance(), locale, 0);
        assertTrue("Is an array", result.isArray());
        assertEquals("Array has two elements", 2, result.size());
        assertTrue("1st element is JsonObject", result.get(0).isObject());
        assertTrue("2nd element is JsonObject", result.get(1).isObject());
        this.testIsSchema(result.get(0));
        this.testIsSchema(result.get(1));
        try {
            service.doGet(JSON.JSON, JSON.JSON, NullNode.getInstance(), locale, 42);
            fail("Should have thrown exception");
        } catch (JsonException ex) {
            assertEquals("Exception code is 400", 400, ex.getCode());
            assertEquals("Error message", "Unknown object type json was requested.", ex.getMessage());
        }
    }

    private void testIsSchema(JsonNode root) {
        assertEquals("Is schema", JSON.SCHEMA, root.path(JSON.TYPE).asText());
        JsonNode data = root.path(JSON.DATA);
        assertEquals("Data has three properties", 3, data.size());
        assertTrue("Data has name property that is string", data.path(JSON.NAME).isTextual());
        assertTrue("Data has server property that is boolean", data.path(JSON.SERVER).isBoolean());
        assertTrue("Data has schema property that is object", data.path(JSON.SCHEMA).isObject());
    }
}
