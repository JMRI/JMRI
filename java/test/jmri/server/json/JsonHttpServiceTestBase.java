package jmri.server.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeNotNull;

import java.util.Locale;

import javax.annotation.OverridingMethodsMustInvokeSuper;

import jmri.InstanceManager;
import jmri.jmris.json.JsonServerPreferences;
import jmri.server.json.schema.JsonSchemaServiceCache;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;
import org.junit.Assert;
import org.junit.Test;

/**
 * Common methods for JMRI JSON Service HTTP provider tests.
 *
 * @author Randall Wood Copyright 2018
 * @param <I> The class of JsonHttpService being tested
 */
public class JsonHttpServiceTestBase<I extends JsonHttpService> {

    protected ObjectMapper mapper = null;
    protected Locale locale = Locale.ENGLISH;
    protected I service;

    /**
     * @throws Exception to allow overriding methods to throw any exception
     */
    @OverridingMethodsMustInvokeSuper
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        mapper = new ObjectMapper();
        // require valid inputs and outputs for tests by default
        InstanceManager.getDefault(JsonServerPreferences.class).setValidateClientMessages(true);
        InstanceManager.getDefault(JsonServerPreferences.class).setValidateServerMessages(true);
    }

    /**
     * @throws Exception to allow overriding methods to throw any exception
     */
    @OverridingMethodsMustInvokeSuper
    public void tearDown() throws Exception {
        service = null;
        mapper = null;
        JUnitUtil.tearDown();
    }

    @Test
    public void testDoDelete() throws JsonException {
        try {
            // protect against JUnit tests in Eclipse that test this class directly
            assumeNotNull(service);
            service.doDelete("foo", "foo", NullNode.getInstance(), locale, 42);
            fail("Expected exception not thrown.");
        } catch (JsonException ex) {
            assertEquals("Code is HTTP METHOD NOT ALLOWED", 405, ex.getCode());
            assertEquals("Message", "Deleting foo is not allowed.", ex.getMessage());
            assertEquals("ID is 42", 42, ex.getId());
        }
    }

    /**
     * Validate a JSON schema request exists and is schema valid for the type.
     * 
     * @param type the JSON object type
     * @throws JsonException if an error occurs
     */
    public final void testDoSchema(String type) throws JsonException {
        // protect against JUnit tests in Eclipse that test this class directly
        assumeNotNull(service);
        JsonNode schema = service.doSchema(type, false, locale, 42);
        validate(schema);
        assertEquals("Type is in client schema", "jmri-json-" + type + "-client-message", schema.path(JSON.DATA).path(JSON.SCHEMA).path("title").asText());
        schema = service.doSchema(type, true, locale, 42);
        validate(schema);
        assertEquals("Type is in server schema", "jmri-json-" + type + "-server-message", schema.path(JSON.DATA).path(JSON.SCHEMA).path("title").asText());
        // Suppress a warning message (see networknt/json-schema-validator#79)
        JUnitAppender.checkForMessageStartingWith(
                "Unknown keyword exclusiveMinimum - you should define your own Meta Schema.");
    }

    /**
     * Validate a JSON schema request exists and is schema valid for the types.
     * Further verify that the schema is the same for both types.
     * 
     * @param type1 the first JSON object type
     * @param type2 the second JSON object type
     * @throws JsonException if an error occurs
     */
    public final void testDoSchema(String type1, String type2) throws JsonException {
        // protect against JUnit tests in Eclipse that test this class directly
        assumeNotNull(service);
        JsonNode schema1 = service.doSchema(type1, false, locale, 42);
        validate(schema1);
        JsonNode schema2 = service.doSchema(type2, false, locale, 42);
        validate(schema2);
        assertEquals("Client schema objects are the same", schema1.path(JSON.DATA).path(JSON.SCHEMA), schema1.path(JSON.DATA).path(JSON.SCHEMA));
        schema1 = service.doSchema(type1, true, locale, 42);
        validate(schema1);
        schema2 = service.doSchema(type2, true, locale, 42);
        validate(schema2);
        assertEquals("Server schema objects are the same", schema1.path(JSON.DATA).path(JSON.SCHEMA), schema1.path(JSON.DATA).path(JSON.SCHEMA));
        // Suppress a warning message (see networknt/json-schema-validator#79)
        JUnitAppender.checkForMessageStartingWith(
                "Unknown keyword exclusiveMinimum - you should define your own Meta Schema.");
    }

    /**
     * Validate a JsonNode message produced by the JMRI JSON server against
     * published JMRI JSON service schema. Asserts a failure if the node is not
     * schema valid.
     *
     * @param node the node to validate
     */
    public final void validate(JsonNode node) {
        validate(node, true);
    }

    /**
     * Validate a JsonNode message against published JMRI JSON service schema.
     * Asserts a failure if the node is not schema valid.
     *
     * @param node   the node to validate
     * @param server true if the node is generated by a server; false if node is
     *               a client node
     */
    public final void validate(JsonNode node, boolean server) {
        Assert.assertNotNull("Node is not null.", node);
        try {
            InstanceManager.getDefault(JsonSchemaServiceCache.class).validateMessage(node, server, locale, 0);
        } catch (JsonException ex) {
            Assert.fail("Unable to validate schema.");
        }
    }

    /**
     * Validate a JsonNode data object produced by the JMRI JSON server against
     * published JMRI JSON service schema. Asserts a failure if the node is not
     * schema valid.
     *
     * @param type the type against which to validate node against
     * @param node the node to validate
     */
    public final void validateData(String type, JsonNode node) {
        validateData(type, node, true);
    }

    /**
     * Validate a JsonNode data object against published JMRI JSON service
     * schema. Asserts a failure if the node is not schema valid.
     *
     * @param type   the type against which to validate node against
     * @param node   the node to validate
     * @param server true if the node is generated by a server; false if node is
     *               a client node
     */
    public final void validateData(String type, JsonNode node, boolean server) {
        Assert.assertNotNull("Node is not null.", node);
        try {
            InstanceManager.getDefault(JsonSchemaServiceCache.class).validateData(type, node, server, locale, 0);
        } catch (JsonException ex) {
            Assert.fail("Unable to validate schema.");
        }
    }

}
