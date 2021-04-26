package jmri.server.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeNotNull;
import static org.junit.jupiter.api.Assertions.fail;

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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

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
    protected String[] duplicates;

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
        assertThat(factory.getReceivedTypes(JSON.V5)).isEmpty();
    }

    @Test
    public void testGetSentTypesV5() {
        assertThat(factory.getSentTypes(JSON.V5)).isEmpty();
    }

    @Test
    public void testGetHttpService() {
        JSON.VERSIONS.forEach(version -> assertThat(factory.getHttpService(mapper, version)).isNotNull());
    }

    @Test
    public void testGetSocketService() {
        JsonConnection connection = new JsonMockConnection((DataOutputStream) null);
        JSON.VERSIONS.forEach(version -> assertThat(factory.getSocketService(connection, version)).isNotNull());
    }

    @Test
    public void testDoSchema() throws JsonException {
        for (String version : JSON.VERSIONS) {
            H service = factory.getHttpService(mapper, version);
            testDoSchema(factory.getTypes(version), service, null);
            testDoSchema(factory.getReceivedTypes(version), service, false);
            testDoSchema(factory.getSentTypes(version), service, true);
            // test an invalid schema
            try {
                service.doSchema("non-existant-type", false, new JsonRequest(locale, JSON.V5, JSON.GET, 42));
                fail("Expected exception not thrown");
            } catch (JsonException ex) {
                assertThat(ex.getCode()).isEqualTo(500);
                assertThat(ex.getMessage()).isEqualTo("Unknown object type non-existant-type was requested.");
                assertThat(ex.getId()).isEqualTo(42);
            }
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
        // protect against JUnit tests in Eclipse that test this class directly
        assumeNotNull(service);
        JsonNode schema;
        if (server == null || !server) {
            schema = service.doSchema(type, false, new JsonRequest(locale, JSON.V5, JSON.GET, 42));
            validate(type, schema);
            assertEquals("Type is in client schema", "jmri-json-" + type + "-client-message", schema.path(JSON.DATA).path(JSON.SCHEMA).path("title").asText());
        }
        if (server == null || server) {
            schema = service.doSchema(type, true, new JsonRequest(locale, JSON.V5, JSON.GET, 42));
            validate(type, schema);
            assertEquals("Type is in server schema", "jmri-json-" + type + "-server-message", schema.path(JSON.DATA).path(JSON.SCHEMA).path("title").asText());
        }
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
     * @param service the HTTP service that provides the schema
     * @param server true if checking server schema; false if checking client schema; null if checking both
     * @throws JsonException if an error occurs
     */
    public final void testDoSchema(String type1, String type2, H service, Boolean server) throws JsonException {
        // protect against JUnit tests in Eclipse that test this class directly
        assumeNotNull(service);
        JsonNode schema1;
        JsonNode schema2;
        if (server == null || !server) {
            schema1 = service.doSchema(type1, false, new JsonRequest(locale, JSON.V5, JSON.GET, 42));
            validate(type1, schema1);
            schema2 = service.doSchema(type2, false, new JsonRequest(locale, JSON.V5, JSON.GET, 42));
            validate(type2, schema2);
            assertEquals("Client schema objects are the same", schema1.path(JSON.DATA).path(JSON.SCHEMA), schema1.path(JSON.DATA).path(JSON.SCHEMA));
        }
        if (server == null || server) {
            schema1 = service.doSchema(type1, true, new JsonRequest(locale, JSON.V5, JSON.GET, 42));
            validate(type1, schema1);
            schema2 = service.doSchema(type2, true, new JsonRequest(locale, JSON.V5, JSON.GET, 42));
            validate(type2, schema2);
            assertEquals("Server schema objects are the same", schema1.path(JSON.DATA).path(JSON.SCHEMA), schema1.path(JSON.DATA).path(JSON.SCHEMA));
        }
        // Suppress a warning message (see networknt/json-schema-validator#79)
        JUnitAppender.checkForMessageStartingWith(
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
        assertThat(node).isNotNull();
        try {
            InstanceManager.getDefault(JsonSchemaServiceCache.class).validateMessage(node, server, new JsonRequest(locale, JSON.V5, JSON.GET, 0));
        } catch (JsonException ex) {
            fail("Unable to validate " + ((server) ? "server" : "client") + " schema for " + type + ".");
        }
    }

}
