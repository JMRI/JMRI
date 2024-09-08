package jmri.server.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;

import java.util.Locale;

import javax.annotation.OverridingMethodsMustInvokeSuper;

import jmri.InstanceManager;
import jmri.server.json.schema.JsonSchemaServiceCache;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Common methods for JMRI JSON Service HTTP provider tests.
 *
 * @author Randall Wood Copyright 2018
 * @param <I> The class of JsonHttpService being tested
 */
public abstract class JsonHttpServiceTestBase<I extends JsonHttpService> {

    protected ObjectMapper mapper = null;
    protected Locale locale = Locale.ENGLISH;
    protected I service;

    /**
     * @throws Exception to allow overriding methods to throw any exception
     */
    @BeforeEach
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
    @AfterEach
    @OverridingMethodsMustInvokeSuper
    public void tearDown() throws Exception {
        service = null;
        mapper = null;
        assertTrue(JUnitUtil.resetZeroConfServiceManager());
        JUnitUtil.tearDown();
    }

    @Test
    public void testDoDelete() throws JsonException {
        try {
            // protect against JUnit tests in Eclipse that test this class directly
            Assumptions.assumeTrue( service != null);
            service.doDelete("foo", "foo", NullNode.getInstance(), new JsonRequest(locale, JSON.V5, JSON.GET, 42));
            fail("Expected exception not thrown.");
        } catch (JsonException ex) {
            assertEquals( 405, ex.getCode(), "Code is HTTP METHOD NOT ALLOWED");
            assertEquals( "Deleting foo is not allowed.", ex.getMessage(), "Message");
            assertEquals( 42, ex.getId(), "ID is 42");
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
        Assumptions.assumeTrue( service != null);
        JsonNode schema = service.doSchema(type, false, new JsonRequest(locale, JSON.V5, JSON.GET, 42));
        validate(type, schema);
        assertEquals( "jmri-json-" + type + "-client-message",
            schema.path(JSON.DATA).path(JSON.SCHEMA).path("title").asText(),
            "Type is in client schema");
        schema = service.doSchema(type, true, new JsonRequest(locale, JSON.V5, JSON.GET, 42));
        validate(type, schema);
        assertEquals( "jmri-json-" + type + "-server-message",
            schema.path(JSON.DATA).path(JSON.SCHEMA).path("title").asText(),
            "Type is in server schema");
        // Suppress a warning message (see networknt/json-schema-validator#79)
        JUnitAppender.checkForMessageStartingWith(
                "Unknown keyword exclusiveMinimum - you should define your own Meta Schema.");
        // test an invalid schema
        try {
            schema = service.doSchema("non-existant-type", false, new JsonRequest(locale, JSON.V5, JSON.GET, 42));
            Assertions.fail("schema should not be valid: " + schema.toPrettyString());
        } catch (JsonException ex) {
            assertEquals( 500, ex.getCode());
            assertEquals("Unknown object type non-existant-type was requested.", ex.getMessage());
            assertEquals( 42, ex.getId());
        }
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
        Assumptions.assumeTrue( service != null);
        JsonNode schema1 = service.doSchema(type1, false, new JsonRequest(locale, JSON.V5, JSON.GET, 42));
        validate(type1, schema1);
        JsonNode schema2 = service.doSchema(type2, false, new JsonRequest(locale, JSON.V5, JSON.GET, 42));
        validate(type2, schema2);
        assertEquals( schema1.path(JSON.DATA).path(JSON.SCHEMA),
            schema2.path(JSON.DATA).path(JSON.SCHEMA), "Client schema objects are the same");
        schema1 = service.doSchema(type1, true, new JsonRequest(locale, JSON.V5, JSON.GET, 42));
        validate(type1, schema1);
        schema2 = service.doSchema(type2, true, new JsonRequest(locale, JSON.V5, JSON.GET, 42));
        validate(type2, schema2);
        assertEquals( schema1.path(JSON.DATA).path(JSON.SCHEMA),
            schema2.path(JSON.DATA).path(JSON.SCHEMA), "Server schema objects are the same");
        // Suppress a warning message (see networknt/json-schema-validator#79)
        JUnitAppender.checkForMessageStartingWith(
                "Unknown keyword exclusiveMinimum - you should define your own Meta Schema.");
    }

    /**
     * Validate a JsonNode message produced by the JMRI JSON server against
     * published JMRI JSON service schema. Asserts a failure if the node is not
     * schema valid. Assertion message will contain the type {@code contextual}
     *
     * @param node the node to validate
     */
    public final void validate(JsonNode node) {
        validate("contextual", node, true);
    }

    /**
     * Validate a JsonNode message produced by the JMRI JSON server against
     * published JMRI JSON service schema. Asserts a failure if the node is not
     * schema valid.
     *
     * @param type the JSON object type
     * @param node the node to validate
     */
    public final void validate(String type, JsonNode node) {
        validate(type, node, true);
    }

    /**
     * Validate a JsonNode message against published JMRI JSON service schema.
     * Asserts a failure if the node is not schema valid.
     *
     * @param type   the JSON object type
     * @param node   the node to validate
     * @param server true if the node is generated by a server; false if node is
     *               a client node
     */
    public final void validate(String type, JsonNode node, boolean server) {
        assertNotNull( node, "Node is not null.");
        try {
            InstanceManager.getDefault(JsonSchemaServiceCache.class).validateMessage(node, server, new JsonRequest(locale, JSON.V5, JSON.GET, 0));
        } catch (JsonException ex) {
            fail("Unable to validate " + ((server) ? "server" : "client") + " schema for " + type + ".");
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
        assertNotNull( node, "Node is not null.");
        try {
            InstanceManager.getDefault(JsonSchemaServiceCache.class).validateData(type, node, server, new JsonRequest(locale, JSON.V5, JSON.GET, 0));
        } catch (JsonException ex) {
            fail("Unable to validate schema.");
        }
    }

}
