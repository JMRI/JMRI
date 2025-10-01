package jmri.server.json;

import static jmri.server.json.JsonTestServiceFactory.TEST;

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        assertNotNull( message, "Response provided");
        assertTrue( message.isArray(), "Response is an array");
        assertEquals( 2, message.size(), "Response array contains two elements");
        assertTrue( message.get(0).isObject(), "Response array element 0 is an object");
        assertEquals( 2, message.get(0).size(), "Response array element 0 is a JSON message");
        assertTrue( message.get(1).isObject(), "Response array element 1 is an object");
        assertEquals( 2, message.get(1).size(), "Response array element 1 is a JSON message");
        // non-JSON request
        instance.onMessage("not a JSON object");
        message = connection.getMessage();
        assertNotNull( message, "Response provided");
        JUnitAppender.assertWarnMessageStartsWith("Exception processing \"not a JSON object\"");
        assertTrue( message.isObject(), "Error response is an object");
        assertEquals( JsonException.ERROR, message.path(JSON.TYPE).asText(), "Error response is an ERROR");
        assertEquals( 500,
            message.path(JSON.DATA).path(JsonException.CODE).asInt(),
            "Error response is type 500");
        // ping request (triggers special paths in JsonClientHandler)
        instance.onMessage("{\"type\":\"ping\"}");
        message = connection.getMessage();
        assertNotNull( message, "Response provided");
        assertTrue( message.isObject(), "Response is an object");
        assertEquals( 1, message.size(), "Response array contains one elements");
        assertEquals( JSON.PONG, message.path(JSON.TYPE).asText(), "Response type is pong");
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
        assertNotNull( response, "Response provided");
        assertTrue( response.isArray(), "Response is an array");
        assertEquals( 2, response.size(), "Response array contains two elements");
        assertTrue( response.get(0).isObject(), "Response array element 0 is an object");
        assertEquals( 2, response.get(0).size(), "Response array element 0 is a JSON message");
        assertTrue( response.get(1).isObject(), "Response array element 1 is an object");
        assertEquals( 2, response.get(1).size(), "Response array element 1 is a JSON message");
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
        assertNotNull( message, "Response provided");
        assertTrue( message.isObject(), "Response is an object");
        assertEquals( 404, message.path(JSON.DATA).path(JsonException.CODE).asInt(),
            "Response contains error code 404");
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
        assertNotNull( message, "Response provided");
        assertTrue( message.isObject(), "Response is an object");
        assertEquals( 404, message.path(JSON.DATA).path(JsonException.CODE).asInt(),
            "Response contains error code 404");
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
        assertNotNull( message, "Response provided");
        JsonNode data = message.path(JSON.DATA);
        assertTrue( message.isObject(), "Response is an object");
        assertEquals( JsonException.ERROR, message.path(JSON.TYPE).asText(),
            "Response is an error");
        assertEquals( 400, data.path(JsonException.CODE).asInt(), "Response contains error code 400");
        assertEquals( "Data property of JSON message missing.",
            data.path(JsonException.MESSAGE).asText(),
            "Response contains error message");
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
        assertNotNull( message, "Response provided");
        JsonNode data = message.path(JSON.DATA);
        assertTrue( message.isObject(), "Response is an object");
        assertEquals( JsonException.ERROR, message.path(JSON.TYPE).asText(),
            "Response is an error");
        assertEquals( 500, data.path(JsonException.CODE).asInt(), "Response contains error code 500");
        assertEquals( "Unsupported operation attempted: null.",
            data.path(JsonException.MESSAGE).asText(),
            "Response contains error message");
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
        assertNotNull( root, "Response provided");
        JsonNode data = root.path(JSON.DATA);
        assertTrue( root.isObject(), "Response is an object");
        assertEquals( 2, root.size(), "Response object contains two elements");
        assertEquals( TEST, root.path(JSON.TYPE).asText(), "Response object type is test");
        assertEquals( TEST, data.path(JSON.NAME).asText(), "Response object data name is test");
        assertEquals( 1, data.size(), "Response object data size is 1");
    }

    @Test
    public void testOnMessage_JsonNode_Method_get_exception() throws Exception {
        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        JsonClientHandler instance = new TestJsonClientHandler(connection);
        JsonNode node = connection.getObjectMapper()
                .readTree("{\"type\":\"test\",\"data\":{\"name\":\"JsonException\"},\"method\":\"get\"}");
        instance.onMessage(node);
        JsonNode root = connection.getMessage();
        assertNotNull( root, "Response provided");
        JsonNode data = root.path(JSON.DATA);
        assertTrue( root.isObject(), "Error response is an object");
        assertEquals( JsonException.ERROR, root.path(JSON.TYPE).asText(), "Error response is an ERROR");
        assertEquals( 499, data.path(JsonException.CODE).asInt(), "Error response is type 499");
    }

    @Test
    public void testOnMessage_JsonNode_Method_Goodbye() throws Exception {
        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        JsonClientHandler instance = new TestJsonClientHandler(connection);
        JsonNode node = connection.getObjectMapper().readTree("{\"type\":\"goodbye\"}");
        assertTrue(connection.isOpen());
        instance.onMessage(node);
        JsonNode root = connection.getMessage();
        assertNotNull( root, "Response provided");
        JsonNode data = root.path(JSON.DATA);
        assertTrue( root.isObject(), "Response is an object");
        assertEquals( JSON.GOODBYE, root.path(JSON.TYPE).asText(), "Response is a Goodbye message");
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
        assertNotNull( root, "Response provided");
        JsonNode data = root.path(JSON.DATA);
        assertTrue( root.isObject(), "Error response is an object");
        assertEquals( JsonException.ERROR, root.path(JSON.TYPE).asText(), "Error response is an ERROR");
        assertEquals( 400, data.path(JsonException.CODE).asInt(), "Error response is type 400");
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
        assertEquals( Locale.ITALY, connection.getLocale(), "Connection is IT Italian");
        instance.onMessage("{\"type\":\"locale\", \"data\":{\"locale\":\"en-US\"}}");
        assertEquals( Locale.US, connection.getLocale(), "Connection is US English");
    }

    /**
     * Test that invalid versions are handled correctly.
     */
    @Test
    public void testSetInvalidVersion() {
        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        connection.setVersion("v4"); // not valid
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> new TestJsonClientHandler(connection).getServices());
        assertNotNull(ex);
        JUnitAppender.assertErrorMessage("Unable to create handler for version v4");
    }

    private static class TestJsonClientHandler extends JsonClientHandler {

        TestJsonClientHandler(JsonConnection connection) {
            super(connection);
        }

        TestJsonClientHandler() {
            this(new JsonMockConnection((DataOutputStream) null));
        }

        @Override
        public HashMap<String, HashSet<JsonSocketService<?>>> getServices() {
            return super.getServices();
        }
    }
}
