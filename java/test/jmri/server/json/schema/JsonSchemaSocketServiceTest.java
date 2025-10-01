package jmri.server.json.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Locale;

import javax.servlet.http.HttpServletResponse;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.server.json.JSON;
import jmri.server.json.JsonException;
import jmri.server.json.JsonMockConnection;
import jmri.server.json.JsonRequest;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 * @author Randall Wood Copyright 2018
 */
public class JsonSchemaSocketServiceTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    /**
     * Test that schema are gettable, but not modifiable.
     *
     * @throws IOException   on unexpected exception
     * @throws JmriException on unexpected exception
     * @throws JsonException on unexpected exception
     */
    @Test
    public void testOnMessageInvalidRequests() throws IOException, JmriException, JsonException {
        ObjectNode data = mapper.createObjectNode().put(JSON.NAME, JSON.JSON);
        Locale locale = Locale.ENGLISH;
        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        JsonSchemaSocketService instance = new JsonSchemaSocketService(connection);
        // DELETE should fail
        JsonException ex = assertThrows( JsonException.class, () ->
            instance.onMessage(JSON.SCHEMA, data,
                new JsonRequest(locale, JSON.V5, JSON.DELETE, 42)),
            "Expected exception not thrown.");
        assertEquals( 405, ex.getCode(), "Error is HTTP 405");
        assertEquals("Deleting schema is not allowed.", ex.getMessage());

        // GET with type != "schema" or "type"
        ex = assertThrows( JsonException.class, () ->
            instance.onMessage("invalid-type", data,
                new JsonRequest(locale, JSON.V5, JSON.GET, 42)),
            "Expected exception not thrown.");
        assertEquals( 400, ex.getCode(), "Error is HTTP 400");
        assertEquals("Unknown object type invalid-type was requested.", ex.getMessage());

        // GET with unknown type
        ex = assertThrows( JsonException.class, () ->
            instance.onMessage(JSON.TYPE, data.put(JSON.NAME, "invalid-type"),
                new JsonRequest(locale, JSON.V5, JSON.GET, 42)),
            "Expected exception not thrown.");

        assertEquals( 404, ex.getCode(), "Error is HTTP 404");
        assertEquals("Object type type named \"invalid-type\" not found.", ex.getMessage());

        // POST should fail
        ex = assertThrows( JsonException.class, () ->
            instance.onMessage(JSON.SCHEMA, data,
                new JsonRequest(locale, JSON.V5, JSON.POST, 42)),
            "Expected exception not thrown.");
        assertEquals( 405, ex.getCode(), "Error is HTTP 405");
        assertEquals("Posting schema is not allowed.", ex.getMessage());

        // PUT should fail
        ex = assertThrows( JsonException.class, () ->
            instance.onMessage(JSON.SCHEMA, data,
                new JsonRequest(locale, JSON.V5, JSON.PUT, 42)),
            "Expected exception not thrown.");
        assertEquals( 405, ex.getCode(), "Error is HTTP 405");
        assertEquals("Putting schema is not allowed.", ex.getMessage());

        ex = assertThrows( JsonException.class, () ->
            instance.onMessage(JSON.SCHEMA, data,
                new JsonRequest(locale, JSON.V5, "TRACE", 42)),
            "Expected exception not thrown.");
        assertEquals( 405, ex.getCode(), "Error is HTTP 405");
        assertEquals("Method TRACE not implemented for type schema.", ex.getMessage());

    }

    /**
     * Test that schema are gettable, but not modifiable.
     *
     * @throws IOException   on unexpected exception
     * @throws JmriException on unexpected exception
     * @throws JsonException on unexpected exception
     */
    @Test
    public void testOnMessageValidRequests() throws IOException, JmriException, JsonException {
        String type = JSON.SCHEMA;
        ObjectNode data = mapper.createObjectNode();
        Locale locale = Locale.ENGLISH;
        JsonNode message;
        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        JsonSchemaSocketService instance = new JsonSchemaSocketService(connection);
        // GET without NAME returns JSON schema
        instance.onMessage(type, data, new JsonRequest(locale, JSON.V5, JSON.GET, 0));
        message = connection.getMessage();
        assertNotNull( message, "Message is not null");
        assertTrue( message.isArray(), "Returned array");
        assertEquals( 2, message.size(), "Returned array has 2 elements");
        assertEquals( "json",
            message.get(0).path(JSON.DATA).path(JSON.NAME).asText(),
            "Returned schema is \"json\"");
        assertTrue( message.get(0).path(JSON.DATA).path(JSON.SERVER).asBoolean(),
            "Returned schema is for server");
        assertEquals( "json",
            message.get(1).path(JSON.DATA).path(JSON.NAME).asText(),
            "Returned schema is \"json\"");
        assertFalse( message.get(1).path(JSON.DATA).path(JSON.SERVER).asBoolean(),
            "Returned schema is for client");
        // Suppress a warning message (see networknt/json-schema-validator#79)
        JUnitAppender.suppressWarnMessageStartsWith(
                "Unknown keyword exclusiveMinimum - you should define your own Meta Schema.");
        // GET with NAME returns the desired schema
        instance.onMessage(type, mapper.readTree("{\"name\":\"schema\"}"), new JsonRequest(locale, JSON.V5, JSON.GET, 0));
        message = connection.getMessage();
        assertNotNull( message, "Message is not null");
        assertTrue( message.isArray(), "Returned array");
        assertEquals( 2, message.size(), "Returned array has 2 elements");
        assertEquals( "schema",
            message.get(0).path(JSON.DATA).path(JSON.NAME).asText(),
            "Returned schema is \"json\"");
        assertTrue( message.get(0).path(JSON.DATA).path(JSON.SERVER).asBoolean(),
            "Returned schema is for server");
        assertEquals( "schema",
            message.get(1).path(JSON.DATA).path(JSON.NAME).asText(),
            "Returned schema is \"json\"");
        assertFalse( message.get(1).path(JSON.DATA).path(JSON.SERVER).asBoolean(),
            "Returned schema is for client");
        // GET with NAME and SERVER==true returns a single schema
        instance.onMessage(type, mapper.readTree("{\"name\":\"schema\", \"server\":true}"), new JsonRequest(locale, JSON.V5, JSON.GET, 42));
        message = connection.getMessage();
        assertNotNull( message, "Message is not null");
        assertTrue( message.isObject(), "Returned single object");
        assertEquals( "schema",
            message.path(JSON.DATA).path(JSON.NAME).asText(),
            "Returned schema is \"json\"");
        assertTrue( message.path(JSON.DATA).path(JSON.SERVER).asBoolean(), "Returned schema is for server");
        // GET with NAME and SERVER==false returns a single schema
        instance.onMessage(type, mapper.readTree("{\"name\":\"schema\", \"server\":false}"), new JsonRequest(locale, JSON.V5, JSON.GET, 42));
        message = connection.getMessage();
        assertNotNull( message, "Message is not null");
        assertTrue( message.isObject(), "Returned single object");
        assertEquals( "schema",
            message.path(JSON.DATA).path(JSON.NAME).asText(),
            "Returned schema is \"json\"");
        assertFalse( message.path(JSON.DATA).path(JSON.SERVER).asBoolean(), "Returned schema is for client");
        // GET with NAME and CLIENT==false returns a single schema
        instance.onMessage(type, mapper.readTree("{\"name\":\"schema\", \"client\":false}"), new JsonRequest(locale, JSON.V5, JSON.GET, 42));
        message = connection.getMessage();
        assertNotNull( message, "Message is not null");
        assertTrue( message.isObject(), "Returned single object");
        assertEquals( "schema",
            message.path(JSON.DATA).path(JSON.NAME).asText(),
            "Returned schema is \"json\"");
        assertTrue( message.path(JSON.DATA).path(JSON.SERVER).asBoolean(), "Returned schema is for server");
        // GET with NAME and CLIENT==true returns a single schema
        instance.onMessage(type, mapper.readTree("{\"name\":\"schema\", \"client\":true}"), new JsonRequest(locale, JSON.V5, JSON.GET, 42));
        message = connection.getMessage();
        assertNotNull( message, "Message is not null");
        assertTrue( message.isObject(), "Returned single object");
        assertEquals( "schema",
            message.path(JSON.DATA).path(JSON.NAME).asText(),
            "Returned schema is \"json\"");
        assertFalse( message.path(JSON.DATA).path(JSON.SERVER).asBoolean(), "Returned schema is for client");
        // GET with NAME, SERVER==true, and CLIENT==true returns the desired schema
        instance.onMessage(type, mapper.readTree("{\"name\":\"schema\", \"server\":true, \"client\":true}"),
                    new JsonRequest(locale, JSON.V5, JSON.GET, 0));
        message = connection.getMessage();
        assertNotNull( message, "Message is not null");
        assertTrue( message.isArray(), "Returned array");
        assertEquals( 2, message.size(), "Returned array has 2 elements");
        assertEquals( "schema",
            message.get(0).path(JSON.DATA).path(JSON.NAME).asText(),
            "Returned schema is \"json\"");
        assertTrue( message.get(0).path(JSON.DATA).path(JSON.SERVER).asBoolean(),
            "Returned schema is for server");
        assertEquals( "schema",
            message.get(1).path(JSON.DATA).path(JSON.NAME).asText(),
            "Returned schema is \"json\"");
        assertFalse( message.get(1).path(JSON.DATA).path(JSON.SERVER).asBoolean(),
            "Returned schema is for client");
    }

    /**
     * Test that schema are not listed.
     *
     * @throws IOException   on unexpected exception
     * @throws JmriException on unexpected exception
     * @throws JsonException on unexpected exception
     */
    @Test
    public void testOnList() throws IOException, JmriException, JsonException {
        ObjectNode data = mapper.createObjectNode();
        JsonNode message;
        Locale locale = Locale.ENGLISH;
        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        JsonSchemaSocketService instance = new JsonSchemaSocketService(connection);
        JsonException ex = assertThrows( JsonException.class, () ->
            instance.onList(JSON.SCHEMA, data,
                new JsonRequest(locale, JSON.V5, JSON.GET, 42)),
            "Expected exception not thrown.");
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, ex.getCode());

        instance.onList(JSON.TYPE, data, new JsonRequest(locale, JSON.V5, JSON.GET, 0));
        message = connection.getMessage();
        assertNotNull( message, "Message is not null");
        assertTrue( message.isArray(), "Result is array");
        assertEquals( InstanceManager.getDefault(JsonSchemaServiceCache.class).getTypes(JSON.V5).size(),
            message.size(), "Result contains all types");
    }

}
