package jmri.server.json.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;

import jmri.server.json.JSON;
import jmri.server.json.JsonException;
import jmri.server.json.JsonHttpServiceTestBase;
import jmri.server.json.JsonRequest;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Randall Wood Copyright 2018
 */
public class JsonSchemaHttpServiceTest extends JsonHttpServiceTestBase<JsonSchemaHttpService> {

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();
        service = new JsonSchemaHttpService(mapper);
    }

    @AfterEach
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
        JsonNode result =
                service.doGet(JSON.SCHEMA, JSON.JSON, NullNode.getInstance(), new JsonRequest(locale, JSON.V5, JSON.GET, 0));
        assertTrue( result.isArray(), "Is an array");
        assertEquals( 2, result.size(), "Array has two elements");
        assertTrue( result.get(0).isObject(), "1st element is JsonObject");
        assertTrue( result.get(1).isObject(), "2nd element is JsonObject");
        this.testIsSchema(result.get(0));
        this.testIsSchema(result.get(1));
        JsonException ex = assertThrows( JsonException.class, () ->
            service.doGet(JSON.JSON, JSON.JSON, NullNode.getInstance(),
                new JsonRequest(locale, JSON.V5, JSON.GET, 42)),
            "Should have thrown exception");
        assertEquals( 400, ex.getCode(), "Exception code is 400");
        assertEquals( "Unknown object type json was requested.", ex.getMessage(), "Error message");
    }

    /**
     * Test of doPost method, of class JsonSchemaHttpService.
     */
    @Test
    public void testDoPost() {
        JsonException ex = assertThrows( JsonException.class, () ->
            service.doPost(JSON.SCHEMA, JSON.JSON, NullNode.getInstance(),
                new JsonRequest(locale, JSON.V5, JSON.GET, 0)),
            "Expected exception not thrown");
        JsonNode result = ex.getJsonMessage();
        assertTrue( result.isObject(), "Is an object");
        assertEquals( JsonException.ERROR, result.path(JSON.TYPE).asText(), "Is an error");
        assertEquals( 405, result.path(JSON.DATA).path(JsonException.CODE).asInt(), "Is error type 405");
    }

    private void testIsSchema(JsonNode root) {
        assertEquals( JSON.SCHEMA, root.path(JSON.TYPE).asText(), "Is schema");
        JsonNode data = root.path(JSON.DATA);
        assertEquals( 3, data.size(), "Data has three properties");
        assertTrue( data.path(JSON.NAME).isTextual(), "Data has name property that is string");
        assertTrue( data.path(JSON.SERVER).isBoolean(), "Data has server property that is boolean");
        assertTrue( data.path(JSON.SCHEMA).isObject(), "Data has schema property that is object");
    }
}
