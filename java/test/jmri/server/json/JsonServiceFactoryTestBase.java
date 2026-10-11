package jmri.server.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.DataOutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.annotation.OverridingMethodsMustInvokeSuper;

import jmri.InstanceManager;
import jmri.server.json.schema.JsonSchemaServiceCache;
import jmri.spi.JsonServiceFactory;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Common methods for JMRI JSON Service HTTP provider tests.
 *
 * @author Randall Wood Copyright 2018
 * @param <H> The class of JsonHttpService being tested
 * @param <I> The class of JsonSocketService being tested
 */
public abstract class JsonServiceFactoryTestBase<H extends JsonHttpService, I extends JsonSocketService<H>> {

    protected ObjectMapper mapper = null;
    protected Locale locale = Locale.ENGLISH;
    protected JsonServiceFactory<H, I> factory;

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
        factory = null;
        mapper = null;
        JUnitUtil.tearDown();
    }

    public abstract void testGetTypesV5();

    @Test
    public void testGetReceivedTypesV5() {
        assertEquals( 0, factory.getReceivedTypes(JSON.V5).length);
    }

    @Test
    public void testGetSentTypesV5() {
        assertEquals( 0, factory.getSentTypes(JSON.V5).length);
    }

    @Test
    public void testGetHttpService() {
        JSON.VERSIONS.forEach(version -> assertNotNull(factory.getHttpService(mapper, version)));
    }

    @Test
    public void testGetSocketService() {
        JsonConnection connection = new JsonMockConnection((DataOutputStream) null);
        JSON.VERSIONS.forEach(version -> assertNotNull(factory.getSocketService(connection, version)));
    }

    @Test
    public void testDoSchema() throws JsonException {
        for (String version : JSON.VERSIONS) {
            H service = factory.getHttpService(mapper, version);
            testDoSchema(factory.getTypes(version), service, null);
            testDoSchema(factory.getReceivedTypes(version), service, false);
            testDoSchema(factory.getSentTypes(version), service, true);
            // test an invalid schema
            JsonException ex = Assertions.assertThrows(JsonException.class, () ->
                service.doSchema("non-existant-type", false, new JsonRequest(locale, JSON.V5, JSON.GET, 42)));

            assertEquals( 500, ex.getCode());
            assertEquals( "Unknown object type non-existant-type was requested.", ex.getMessage());
            assertEquals( 42, ex.getId());
        }
    }

    protected void testDoSchema(String[] t, H service, Boolean server) throws JsonException {
        List<String> types = Arrays.asList(t);
        for (String type : types) {
            if (types.contains(type.substring(0, type.length() - 1))) {
                // assume FOOS is plural for FOO
                testDoSchema(type.substring(0, type.length() - 1), type, service, server);
            } else {
                // assume FOO and FOOS are keys for same type; test only FOO
                testDoSchema(type, service, server);
            }
        }
    }

    /**
     * Validate a JSON schema request exists and is schema valid for the type.
     * 
     * @param type the JSON object type
     * @param service the HTTP service that provides the schema
     * @param server true if checking server schema; false if checking client schema; null if checking both
     * @throws JsonException if an error occurs
     */
    public final void testDoSchema(String type, H service, Boolean server) throws JsonException {
        if (service == null) {
            // protect against JUnit tests in Eclipse that test this class directly
            // Compilers may complain about a potential null pointer if
            // Assumptions.assumeTrue( service != null ) is used.
            // TODO - check this with JUnit > 5.9.1
            return;
        }
        JsonNode schema;
        if (server == null || !server) {
            schema = service.doSchema(type, false, new JsonRequest(locale, JSON.V5, JSON.GET, 42));
            validate(type, schema);
            assertEquals( "jmri-json-" + type + "-client-message",
                schema.path(JSON.DATA).path(JSON.SCHEMA).path("title").asText(),
                "Type is in client schema");
        }
        if (server == null || server) {
            schema = service.doSchema(type, true, new JsonRequest(locale, JSON.V5, JSON.GET, 42));
            validate(type, schema);
            assertEquals( "jmri-json-" + type + "-server-message",
                schema.path(JSON.DATA).path(JSON.SCHEMA).path("title").asText(),
                "Type is in server schema");
        }
        // Suppress a warning message (see networknt/json-schema-validator#79)
        JUnitAppender.suppressWarnMessageStartsWith(
                "Unknown keyword exclusiveMinimum - you should define your own Meta Schema.");
    }

    /**
     * Validate a JSON schema request exists and is schema valid for the types.
     * Further verify that the schema is the same for both types.
     * 
     * @param type1 the first JSON object type
     * @param type2 the second JSON object type
     * @param service the HTTP service that provides the schema
     * @param server true if checking server schema; false if checking client schema; null if checking both
     * @throws JsonException if an error occurs
     */
    public final void testDoSchema(String type1, String type2, H service, Boolean server) throws JsonException {
        if (service == null) {
            // protect against JUnit tests in Eclipse that test this class directly
            // Compilers may complain about a potential null pointer if
            // Assumptions.assumeTrue( service != null ) is used.
            // TODO - check this with JUnit > 5.9.1
            return;
        }
        JsonNode schema1;
        JsonNode schema2;
        if (server == null || !server) {
            schema1 = service.doSchema(type1, false, new JsonRequest(locale, JSON.V5, JSON.GET, 42));
            validate(type1, schema1);
            schema2 = service.doSchema(type2, false, new JsonRequest(locale, JSON.V5, JSON.GET, 42));
            validate(type2, schema2);
            assertEquals( schema1.path(JSON.DATA).path(JSON.SCHEMA),
                schema1.path(JSON.DATA).path(JSON.SCHEMA),
                "Client schema objects are the same");
        }
        if (server == null || server) {
            schema1 = service.doSchema(type1, true, new JsonRequest(locale, JSON.V5, JSON.GET, 42));
            validate(type1, schema1);
            schema2 = service.doSchema(type2, true, new JsonRequest(locale, JSON.V5, JSON.GET, 42));
            validate(type2, schema2);
            assertEquals( schema1.path(JSON.DATA).path(JSON.SCHEMA),
                schema1.path(JSON.DATA).path(JSON.SCHEMA),
                "Server schema objects are the same");
        }
        // Suppress a warning message (see networknt/json-schema-validator#79)
        JUnitAppender.suppressWarnMessageStartsWith(
                "Unknown keyword exclusiveMinimum - you should define your own Meta Schema.");
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
        assertNotNull(node);
        assertDoesNotThrow( () ->
            InstanceManager.getDefault(JsonSchemaServiceCache.class)
                .validateMessage(node, server, new JsonRequest(locale, JSON.V5, JSON.GET, 0)),
            "Unable to validate " + ((server) ? "server" : "client") + " schema for " + type + ".");
    }

}
