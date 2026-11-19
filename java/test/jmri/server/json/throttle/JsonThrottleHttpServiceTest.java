package jmri.server.json.throttle;

import com.fasterxml.jackson.databind.node.ObjectNode;

import jmri.InstanceManager;
import jmri.server.json.JSON;
import jmri.server.json.JsonException;
import jmri.server.json.JsonHttpServiceTestBase;
import jmri.server.json.JsonRequest;
import jmri.server.json.schema.JsonSchemaServiceCache;
import jmri.util.JUnitAppender;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Randall Wood Copyright (C) 2019
 */
public class JsonThrottleHttpServiceTest extends JsonHttpServiceTestBase<JsonThrottleHttpService> {

    @Test
    public void testDoGet() {
        JsonException ex = assertThrows( JsonException.class, () ->
            service.doGet(JsonThrottle.THROTTLE, "", mapper.createObjectNode(),
                new JsonRequest(locale, JSON.V5, JSON.GET, 42)),
            "Expected exception not thrown.");
        assertEquals( 405, ex.getCode(), "Error code is HTTP Method Not Allowed");
        assertEquals( "Getting throttle is not allowed.", ex.getMessage(), "Error message");
    }

    @Test
    public void testDoPut() {
        JsonException ex = assertThrows( JsonException.class, () ->
            service.doPut(JsonThrottle.THROTTLE, "", mapper.createObjectNode(),
                new JsonRequest(locale, JSON.V5, JSON.GET, 42)),
            "Expected exception not thrown.");
        assertEquals( 405, ex.getCode(), "Error code is HTTP Method Not Allowed");
        assertEquals( "Putting throttle is not allowed.", ex.getMessage(), "Error message");
    }

    @Test
    public void testDoPost() {
        JsonException ex = assertThrows( JsonException.class, () ->
            service.doPost(JsonThrottle.THROTTLE, "", mapper.createObjectNode(),
                new JsonRequest(locale, JSON.V5, JSON.GET, 42)),
            "Expected exception not thrown.");
        assertEquals( 405, ex.getCode(), "Error code is HTTP Method Not Allowed");
        assertEquals( "Posting throttle is not allowed.", ex.getMessage(), "Error message");
    }

    @Test
    public void testDoList() {
        JsonException ex = assertThrows( JsonException.class, () ->
            service.doGetList(JsonThrottle.THROTTLE, mapper.createObjectNode(),
                new JsonRequest(locale, JSON.V5, JSON.GET, 42)),
            "Expected exception not thrown.");
        assertEquals( 400, ex.getCode(), "Error code is HTTP Bad Request");
        assertEquals( "throttle cannot be listed.", ex.getMessage(), "Error message");
    }

    @Test
    public void testClientSchemaAcceptsSpeed() {
        ObjectNode data = mapper.createObjectNode().put(JSON.NAME, "42").put(JSON.SPEED, 0.5);
        validateData(JsonThrottle.THROTTLE, data, false);
    }

    private ObjectNode throttleData(boolean server) {
        // client messages identify a throttle by name or throttle; server messages must have both
        ObjectNode data = mapper.createObjectNode().put(JSON.NAME, "42");
        return server ? data.put(JsonThrottle.THROTTLE, "42") : data;
    }

    private void assertInvalid(JsonSchemaServiceCache schemas, ObjectNode data, boolean server, String message) {
        JsonRequest request = new JsonRequest(locale, JSON.V5, JSON.GET, 42);
        assertThrows(JsonException.class, () -> schemas.validateData(JsonThrottle.THROTTLE, data, server, request), message);
        // consume the warnings logged for the rejected message
        assertNotNull(JUnitAppender.checkForMessageStartingWith("Errors validating"), message);
        while (JUnitAppender.checkForMessageStartingWith("JSON Validation Error") != null) {
            // drain
        }
    }

    @Test
    public void testSchemasAcceptFunctionRange() {
        for (boolean server : new boolean[] {false, true}) {
            ObjectNode data = throttleData(server);
            for (int i : new int[] {0, 1, 28, 29, 68, 84}) {
                data.put(JSON.F + i, true);
            }
            validateData(JsonThrottle.THROTTLE, data, server);
        }
    }

    @Test
    public void testSchemasRejectInvalidFunctions() {
        JsonSchemaServiceCache schemas = InstanceManager.getDefault(JsonSchemaServiceCache.class);
        for (boolean server : new boolean[] {false, true}) {
            validateData(JsonThrottle.THROTTLE, throttleData(server), server); // baseline is valid
            for (String key : new String[] {"F85", "F100", "F01", "F-1", "Fx"}) {
                ObjectNode data = throttleData(server).put(key, true);
                assertInvalid(schemas, data, server, key);
            }
            ObjectNode data = throttleData(server).put(JSON.F + 1, "true");
            assertInvalid(schemas, data, server, "non-boolean");
        }
    }

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();
        service = new JsonThrottleHttpService(mapper);
    }

    @AfterEach
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

}
