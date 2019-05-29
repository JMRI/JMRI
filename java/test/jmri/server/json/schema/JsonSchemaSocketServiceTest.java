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
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Randall Wood Copyright 2018
 */
public class JsonSchemaSocketServiceTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
    }

    @After
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
        try {
            instance.onMessage(JSON.SCHEMA, data, JSON.DELETE, locale, 42);
            Assert.fail("Expected exception not thrown.");
        } catch (JsonException ex) {
            Assert.assertEquals("Error is HTTP 405", 405, ex.getCode());
            Assert.assertEquals("Deleting schema is not allowed.", ex.getMessage());
        }
        // GET with type != "schema" or "type"
        try {
            instance.onMessage("invalid-type", data, JSON.GET, locale, 42);
            Assert.fail("Expected exception not thrown.");
        } catch (JsonException ex) {
            Assert.assertEquals("Error is HTTP 400", 400, ex.getCode());
            Assert.assertEquals("Unknown object type invalid-type was requested.", ex.getMessage());
        }
        // GET with unknown type
        try {
            instance.onMessage(JSON.TYPE, data.put(JSON.NAME, "invalid-type"), JSON.GET, locale, 42);
            Assert.fail("Expected exception not thrown.");
        } catch (JsonException ex) {
            Assert.assertEquals("Error is HTTP 404", 404, ex.getCode());
            Assert.assertEquals("Object type type named \"invalid-type\" not found.", ex.getMessage());
        }
        // POST should fail
        try {
            instance.onMessage(JSON.SCHEMA, data, JSON.POST, locale, 42);
            Assert.fail("Expected exception not thrown.");
        } catch (JsonException ex) {
            Assert.assertEquals("Error is HTTP 405", 405, ex.getCode());
            Assert.assertEquals("Posting schema is not allowed.", ex.getMessage());
        }
        // PUT should fail
        try {
            instance.onMessage(JSON.SCHEMA, data, JSON.PUT, locale, 42);
            Assert.fail("Expected exception not thrown.");
        } catch (JsonException ex) {
            Assert.assertEquals("Error is HTTP 405", 405, ex.getCode());
            Assert.assertEquals("Putting schema is not allowed.", ex.getMessage());
        }
        try {
            instance.onMessage(JSON.SCHEMA, data, "TRACE", locale, 42);
            Assert.fail("Expected exception not thrown.");
        } catch (JsonException ex) {
            Assert.assertEquals("Error is HTTP 405", 405, ex.getCode());
            Assert.assertEquals("Method TRACE not implemented for type schema.", ex.getMessage());
        }
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
        instance.onMessage(type, data, JSON.GET, locale, 0);
        message = connection.getMessage();
        Assert.assertNotNull("Message is not null", message);
        Assert.assertTrue("Returned array", message.isArray());
        Assert.assertEquals("Returned array has 2 elements", 2, message.size());
        Assert.assertEquals("Returned schema is \"json\"", "json",
                message.get(0).path(JSON.DATA).path(JSON.NAME).asText());
        Assert.assertTrue("Returned schema is for server",
                message.get(0).path(JSON.DATA).path(JSON.SERVER).asBoolean());
        Assert.assertEquals("Returned schema is \"json\"", "json",
                message.get(1).path(JSON.DATA).path(JSON.NAME).asText());
        Assert.assertFalse("Returned schema is for client",
                message.get(1).path(JSON.DATA).path(JSON.SERVER).asBoolean());
        // Suppress a warning message (see networknt/json-schema-validator#79)
        JUnitAppender.checkForMessageStartingWith(
                "Unknown keyword exclusiveMinimum - you should define your own Meta Schema.");
        // GET with NAME returns the desired schema
        instance.onMessage(type, mapper.readTree("{\"name\":\"schema\"}"), JSON.GET, locale, 0);
        message = connection.getMessage();
        Assert.assertNotNull("Message is not null", message);
        Assert.assertTrue("Returned array", message.isArray());
        Assert.assertEquals("Returned array has 2 elements", 2, message.size());
        Assert.assertEquals("Returned schema is \"json\"", "schema",
                message.get(0).path(JSON.DATA).path(JSON.NAME).asText());
        Assert.assertTrue("Returned schema is for server",
                message.get(0).path(JSON.DATA).path(JSON.SERVER).asBoolean());
        Assert.assertEquals("Returned schema is \"json\"", "schema",
                message.get(1).path(JSON.DATA).path(JSON.NAME).asText());
        Assert.assertFalse("Returned schema is for client",
                message.get(1).path(JSON.DATA).path(JSON.SERVER).asBoolean());
        // GET with NAME and SERVER==true returns a single schema
        instance.onMessage(type, mapper.readTree("{\"name\":\"schema\", \"server\":true}"), JSON.GET, locale, 42);
        message = connection.getMessage();
        Assert.assertNotNull("Message is not null", message);
        Assert.assertTrue("Returned single object", message.isObject());
        Assert.assertEquals("Returned schema is \"json\"", "schema",
                message.path(JSON.DATA).path(JSON.NAME).asText());
        Assert.assertTrue("Returned schema is for server", message.path(JSON.DATA).path(JSON.SERVER).asBoolean());
        // GET with NAME and SERVER==false returns a single schema
        instance.onMessage(type, mapper.readTree("{\"name\":\"schema\", \"server\":false}"), JSON.GET, locale, 42);
        message = connection.getMessage();
        Assert.assertNotNull("Message is not null", message);
        Assert.assertTrue("Returned single object", message.isObject());
        Assert.assertEquals("Returned schema is \"json\"", "schema",
                message.path(JSON.DATA).path(JSON.NAME).asText());
        Assert.assertFalse("Returned schema is for client", message.path(JSON.DATA).path(JSON.SERVER).asBoolean());
        // GET with NAME and CLIENT==false returns a single schema
        instance.onMessage(type, mapper.readTree("{\"name\":\"schema\", \"client\":false}"), JSON.GET, locale, 42);
        message = connection.getMessage();
        Assert.assertNotNull("Message is not null", message);
        Assert.assertTrue("Returned single object", message.isObject());
        Assert.assertEquals("Returned schema is \"json\"", "schema",
                message.path(JSON.DATA).path(JSON.NAME).asText());
        Assert.assertTrue("Returned schema is for server", message.path(JSON.DATA).path(JSON.SERVER).asBoolean());
        // GET with NAME and CLIENT==true returns a single schema
        instance.onMessage(type, mapper.readTree("{\"name\":\"schema\", \"client\":true}"), JSON.GET, locale, 42);
        message = connection.getMessage();
        Assert.assertNotNull("Message is not null", message);
        Assert.assertTrue("Returned single object", message.isObject());
        Assert.assertEquals("Returned schema is \"json\"", "schema",
                message.path(JSON.DATA).path(JSON.NAME).asText());
        Assert.assertFalse("Returned schema is for client", message.path(JSON.DATA).path(JSON.SERVER).asBoolean());
        // GET with NAME, SERVER==true, and CLIENT==true returns the desired schema
        instance.onMessage(type, mapper.readTree("{\"name\":\"schema\", \"server\":true, \"client\":true}"),
                    JSON.GET, locale, 0);
        message = connection.getMessage();
        Assert.assertNotNull("Message is not null", message);
        Assert.assertTrue("Returned array", message.isArray());
        Assert.assertEquals("Returned array has 2 elements", 2, message.size());
        Assert.assertEquals("Returned schema is \"json\"", "schema",
                message.get(0).path(JSON.DATA).path(JSON.NAME).asText());
        Assert.assertTrue("Returned schema is for server",
                message.get(0).path(JSON.DATA).path(JSON.SERVER).asBoolean());
        Assert.assertEquals("Returned schema is \"json\"", "schema",
                message.get(1).path(JSON.DATA).path(JSON.NAME).asText());
        Assert.assertFalse("Returned schema is for client",
                message.get(1).path(JSON.DATA).path(JSON.SERVER).asBoolean());
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
        try {
            instance.onList(JSON.SCHEMA, data, locale, 42);
            Assert.fail("Expected exception not thrown.");
        } catch (JsonException ex) {
            Assert.assertEquals(HttpServletResponse.SC_BAD_REQUEST, ex.getCode());
        }
        instance.onList(JSON.TYPE, data, locale, 0);
        message = connection.getMessage();
        Assert.assertNotNull("Message is not null", message);
        Assert.assertTrue("Result is array", message.isArray());
        Assert.assertEquals("Result contains all types",
                InstanceManager.getDefault(JsonSchemaServiceCache.class).getTypes().size(), message.size());
    }

}
