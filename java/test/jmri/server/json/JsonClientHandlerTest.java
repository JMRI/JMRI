package jmri.server.json;

import static jmri.server.json.JsonTestServiceFactory.TEST;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.fasterxml.jackson.databind.JsonNode;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;

import jmri.profile.NullProfile;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Randall Wood Copyright 2018
 */
public class JsonClientHandlerTest {

    private Locale locale = Locale.ENGLISH;

    @BeforeEach
    public void setUp(@TempDir File folder) throws IOException {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager(new NullProfile(folder));
        JUnitUtil.initRosterConfigManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.deregisterEditorManagerShutdownTask();
        JUnitUtil.tearDown();
    }

    /**
     * Test of onClose method, of class JsonClientHandler.
     */
    @Test
    public void testOnClose() {
        JsonClientHandler instance = new TestJsonClientHandler();
        assertFalse(instance.getServices().isEmpty());
        instance.onClose();
        assertTrue(instance.getServices().isEmpty());
    }

    /**
     * Test of onMessage method, of class JsonClientHandler.
     *
     * @throws IOException if unable to pass message
     */
    @Test
    public void testOnMessage_String() throws IOException {
        // valid list request
        String string = "{\"type\":\"test\",\"method\":\"list\"}";
        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        JsonClientHandler instance = new TestJsonClientHandler(connection);
        instance.onMessage(string);
        JsonNode message = connection.getMessage();
        assertNotNull("Response provided", message);
        assertTrue("Response is an array", message.isArray());
        assertEquals("Response array contains two elements", 2, message.size());
        assertTrue("Response array element 0 is an object", message.get(0).isObject());
        assertEquals("Response array element 0 is a JSON message", 2, message.get(0).size());
        assertTrue("Response array element 1 is an object", message.get(1).isObject());
        assertEquals("Response array element 1 is a JSON message", 2, message.get(1).size());
        // non-JSON request
        instance.onMessage("not a JSON object");
        message = connection.getMessage();
        assertNotNull("Response provided", message);
        assertNotNull("Expected warning not shown",
                JUnitAppender.checkForMessageStartingWith("Exception processing \"not a JSON object\""));
        assertTrue("Error response is an object", message.isObject());
        assertEquals("Error response is an ERROR", JsonException.ERROR, message.path(JSON.TYPE).asText());
        assertEquals("Error response is type 500", 500,
                message.path(JSON.DATA).path(JsonException.CODE).asInt());
        // ping request (triggers special paths in JsonClientHandler)
        instance.onMessage("{\"type\":\"ping\"}");
        message = connection.getMessage();
        assertNotNull("Response provided", message);
        assertTrue("Response is an object", message.isObject());
        assertEquals("Response array contains one elements", 1, message.size());
        assertEquals("Response type is pong", JSON.PONG, message.path(JSON.TYPE).asText());
    }

    /**
     * Test of onMessage method, of class JsonClientHandler.
     *
     * @throws IOException if unable to pass message
     */
    @Test
    public void testOnMessage_JsonNode_Method_list() throws IOException {
        testOnMessage_JsonNode_Method_list("{\"type\":\"test\",\"method\":\"list\"}");
        testOnMessage_JsonNode_Method_list("{\"type\":\"list\",\"list\":\"test\"}");
        testOnMessage_JsonNode_Method_list("{\"list\":\"test\"}");
    }

    private void testOnMessage_JsonNode_Method_list(String message) throws IOException {
        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        JsonClientHandler instance = new TestJsonClientHandler(connection);
        JsonNode node = connection.getObjectMapper().readTree(message);
        instance.onMessage(node);
        JsonNode response = connection.getMessage();
        assertNotNull("Response provided", response);
        assertTrue("Response is an array", response.isArray());
        assertEquals("Response array contains two elements", 2, response.size());
        assertTrue("Response array element 0 is an object", response.get(0).isObject());
        assertEquals("Response array element 0 is a JSON message", 2, response.get(0).size());
        assertTrue("Response array element 1 is an object", response.get(1).isObject());
        assertEquals("Response array element 1 is a JSON message", 2, response.get(1).size());
    }

    /**
     * Test of onMessage method attempting to list an invalid type.
     *
     * @throws java.io.IOException if unable to pass message
     */
    @Test
    public void testOnMessage_JsonNode_Method_list_invalidType() throws IOException {
        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        connection.setLocale(locale);
        JsonClientHandler instance = new TestJsonClientHandler(connection);
        JsonNode node = connection.getObjectMapper().readTree("{\"type\":\"non-existant-type\",\"method\":\"list\"}");
        instance.onMessage(node);
        JUnitAppender.assertWarnMessage("Requested list type 'non-existant-type' unknown.");
        JsonNode message = connection.getMessage();
        assertNotNull("Response provided", message);
        assertTrue("Response is an object", message.isObject());
        assertEquals("Response contains error code 404", 404,
                message.path(JSON.DATA).path(JsonException.CODE).asInt());
    }

    /**
     * Test of onMessage method with an invalid type using the get method.
     *
     * @throws java.io.IOException if unable to pass message
     */
    @Test
    public void testOnMessage_JsonNode_Method_get_invalidType() throws IOException {
        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        connection.setLocale(locale);
        JsonClientHandler instance = new TestJsonClientHandler(connection);
        JsonNode node = connection.getObjectMapper().readTree("{\"type\":\"non-existant-type\",\"method\":\"get\"}");
        instance.onMessage(node);
        JUnitAppender.assertWarnMessage("Requested type 'non-existant-type' unknown.");
        JsonNode message = connection.getMessage();
        assertNotNull("Response provided", message);
        assertTrue("Response is an object", message.isObject());
        assertEquals("Response contains error code 404", 404,
                message.path(JSON.DATA).path(JsonException.CODE).asInt());
    }

    /**
     * Test of onMessage method with valid type but missing data.
     *
     * @throws java.io.IOException if unable to pass message
     */
    @Test
    public void testOnMessage_JsonNode_Method_post_missingData() throws IOException {
        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        connection.setLocale(locale);
        JsonClientHandler instance = new TestJsonClientHandler(connection);
        JsonNode node = connection.getObjectMapper().readTree("{\"type\":\"test\", \"method\":\"post\"}");
        instance.onMessage(node);
        JsonNode message = connection.getMessage();
        assertNotNull("Response provided", message);
        JsonNode data = message.path(JSON.DATA);
        assertTrue("Response is an object", message.isObject());
        assertEquals("Response is an error", JsonException.ERROR,
                message.path(JSON.TYPE).asText());
        assertEquals("Response contains error code 400", 400, data.path(JsonException.CODE).asInt());
        assertEquals("Response contains error message", "Data property of JSON message missing.",
                data.path(JsonException.MESSAGE).asText());
    }

    /**
     * Test of onMessage method when service throws JmriException. A test
     * message with the data property {@literal {"throws":"JmriException"} }
     * will throw this exception for this test.
     *
     * @throws java.io.IOException if unable to pass message
     */
    @Test
    public void testOnMessage_JsonNode_Method_service_throws_JmriException() throws IOException {
        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        connection.setLocale(locale);
        JsonClientHandler instance = new TestJsonClientHandler(connection);
        JsonNode node =
                connection.getObjectMapper().readTree("{\"type\":\"test\", \"data\":{\"throws\":\"JmriException\"}}");
        instance.onMessage(node);
        JsonNode message = connection.getMessage();
        assertNotNull("Response provided", message);
        JsonNode data = message.path(JSON.DATA);
        assertTrue("Response is an object", message.isObject());
        assertEquals("Response is an error", JsonException.ERROR,
                message.path(JSON.TYPE).asText());
        assertEquals("Response contains error code 500", 500, data.path(JsonException.CODE).asInt());
        assertEquals("Response contains error message", "Unsupported operation attempted: null.",
                data.path(JsonException.MESSAGE).asText());
        JUnitAppender.assertWarnMessage("Unsupported operation attempted {\"type\":\"test\",\"data\":{\"throws\":\"JmriException\"}}");
    }

    /**
     * Test of onMessage method with valid messages containing the get method.
     *
     * @throws java.io.IOException if unable to pass messages
     */
    @Test
    public void testOnMessage_JsonNode_Method_get() throws IOException {
        testOnMessage_JsonNode_Method_get("{\"type\":\"test\",\"data\":{\"name\":\"test\"},\"method\":\"get\"}");
        testOnMessage_JsonNode_Method_get("{\"type\":\"test\",\"data\":{\"name\":\"test\",\"method\":\"get\"}}");
    }

    private void testOnMessage_JsonNode_Method_get(String message) throws IOException {
        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        JsonClientHandler instance = new TestJsonClientHandler(connection);
        JsonNode node = connection.getObjectMapper().readTree(message);
        instance.onMessage(node);
        JsonNode root = connection.getMessage();
        assertNotNull("Response provided", root);
        JsonNode data = root.path(JSON.DATA);
        assertTrue("Response is an object", root.isObject());
        assertEquals("Response object contains two elements", 2, root.size());
        assertEquals("Response object type is test", TEST, root.path(JSON.TYPE).asText());
        assertEquals("Response object data name is test", TEST, data.path(JSON.NAME).asText());
        assertEquals("Response object data size is 1", 1, data.size());
    }

    @Test
    public void testOnMessage_JsonNode_Method_get_exception() throws Exception {
        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        JsonClientHandler instance = new TestJsonClientHandler(connection);
        JsonNode node = connection.getObjectMapper()
                .readTree("{\"type\":\"test\",\"data\":{\"name\":\"JsonException\"},\"method\":\"get\"}");
        instance.onMessage(node);
        JsonNode root = connection.getMessage();
        assertNotNull("Response provided", root);
        JsonNode data = root.path(JSON.DATA);
        assertTrue("Error response is an object", root.isObject());
        assertEquals("Error response is an ERROR", JsonException.ERROR, root.path(JSON.TYPE).asText());
        assertEquals("Error response is type 499", 499, data.path(JsonException.CODE).asInt());
    }

    @Test
    public void testOnMessage_JsonNode_Method_Goodbye() throws Exception {
        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        JsonClientHandler instance = new TestJsonClientHandler(connection);
        JsonNode node = connection.getObjectMapper().readTree("{\"type\":\"goodbye\"}");
        assertTrue(connection.isOpen());
        instance.onMessage(node);
        JsonNode root = connection.getMessage();
        assertNotNull("Response provided", root);
        JsonNode data = root.path(JSON.DATA);
        assertTrue("Response is an object", root.isObject());
        assertEquals("Response is a Goodbye message", JSON.GOODBYE, root.path(JSON.TYPE).asText());
        assertTrue(data.isMissingNode());
        assertFalse(connection.isOpen());
    }

    /**
     * Test all methods except {@code get} and {@code list} for missing data
     * node. The {@code get} and {@code list} methods are not required to have a
     * data node by the JsonClientHandler.
     *
     * @throws Exception if an unexpected exception occurs
     */
    @Test
    public void testOnMessage_JsonNode_missing_data() throws Exception {
        testOnMessage_JsonNode_missing_data("{\"type\":\"test\",\"method\":\"post\"}");
        testOnMessage_JsonNode_missing_data("{\"type\":\"test\",\"method\":\"put\"}");
        testOnMessage_JsonNode_missing_data("{\"type\":\"test\",\"method\":\"delete\"}");
    }

    private void testOnMessage_JsonNode_missing_data(String message) throws Exception {
        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        JsonClientHandler instance = new TestJsonClientHandler(connection);
        JsonNode node = connection.getObjectMapper().readTree(message);
        instance.onMessage(node);
        JsonNode root = connection.getMessage();
        assertNotNull("Response provided", root);
        JsonNode data = root.path(JSON.DATA);
        assertTrue("Error response is an object", root.isObject());
        assertEquals("Error response is an ERROR", JsonException.ERROR, root.path(JSON.TYPE).asText());
        assertEquals("Error response is type 400", 400, data.path(JsonException.CODE).asInt());
    }

    /**
     * Test that locale on connection is set by handler.
     *
     * @throws IOException if unable to pass message
     */
    @Test
    public void testSetLocale() throws IOException {
        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        JsonClientHandler instance = new TestJsonClientHandler(connection);
        connection.setLocale(Locale.ITALY);
        assertEquals("Connection is IT Italian", Locale.ITALY, connection.getLocale());
        instance.onMessage("{\"type\":\"locale\", \"data\":{\"locale\":\"en-US\"}}");
        assertEquals("Connection is US English", Locale.US, connection.getLocale());
    }

    /**
     * Test that invalid versions are handled correctly.
     */
    @Test
    public void testSetInvalidVersion() {
        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        connection.setVersion("v4"); // not valid
        try {
            new TestJsonClientHandler(connection);
            fail("Expected exception not thrown.");
        } catch (IllegalArgumentException ex) {
            // passes at this point
        }
        JUnitAppender.assertErrorMessage("Unable to create handler for version v4");
    }

    private static class TestJsonClientHandler extends JsonClientHandler {

        public TestJsonClientHandler(JsonConnection connection) {
            super(connection);
        }

        public TestJsonClientHandler() {
            this(new JsonMockConnection((DataOutputStream) null));
        }

        @Override
        public HashMap<String, HashSet<JsonSocketService<?>>> getServices() {
            return super.getServices();
        }
    }
}
