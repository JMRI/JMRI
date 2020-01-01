package jmri.server.json;

import static jmri.server.json.JsonTestServiceFactory.TEST;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import jmri.profile.NullProfile;
import jmri.util.FileUtil;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Randall Wood Copyright 2018
 */
public class JsonClientHandlerTest {

    private Locale locale = Locale.ENGLISH;

    @Before
    public void setUp() throws IOException {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager(
                new NullProfile("JsonClientHandlerTest", "12345678", FileUtil.getFile("program:test")));
        JUnitUtil.initRosterConfigManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    /**
     * Test of onClose method, of class JsonClientHandler.
     */
    @Test
    public void testOnClose() {
        JsonClientHandler instance = new TestJsonClientHandler();
        Assert.assertFalse(instance.getServices().isEmpty());
        instance.onClose();
        Assert.assertTrue(instance.getServices().isEmpty());
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
        Assert.assertNotNull("Response provided", message);
        Assert.assertTrue("Response is an array", message.isArray());
        Assert.assertEquals("Response array contains two elements", 2, message.size());
        Assert.assertTrue("Response array element 0 is an object", message.get(0).isObject());
        Assert.assertEquals("Response array element 0 is a JSON message", 2, message.get(0).size());
        Assert.assertTrue("Response array element 1 is an object", message.get(1).isObject());
        Assert.assertEquals("Response array element 1 is a JSON message", 2, message.get(1).size());
        // non-JSON request
        instance.onMessage("not a JSON object");
        message = connection.getMessage();
        Assert.assertNotNull("Response provided", message);
        Assert.assertNotNull("Expected warning not shown",
                JUnitAppender.checkForMessageStartingWith("Exception processing \"not a JSON object\""));
        Assert.assertTrue("Error response is an object", message.isObject());
        Assert.assertEquals("Error response is an ERROR", JsonException.ERROR, message.path(JSON.TYPE).asText());
        Assert.assertEquals("Error response is type 500", 500,
                message.path(JSON.DATA).path(JsonException.CODE).asInt());
        // ping request (triggers special paths in JsonClientHandler)
        instance.onMessage("{\"type\":\"ping\"}");
        message = connection.getMessage();
        Assert.assertNotNull("Response provided", message);
        Assert.assertTrue("Response is an object", message.isObject());
        Assert.assertEquals("Response array contains one elements", 1, message.size());
        Assert.assertEquals("Response type is pong", JSON.PONG, message.path(JSON.TYPE).asText());
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
        Assert.assertNotNull("Response provided", response);
        Assert.assertTrue("Response is an array", response.isArray());
        Assert.assertEquals("Response array contains two elements", 2, response.size());
        Assert.assertTrue("Response array element 0 is an object", response.get(0).isObject());
        Assert.assertEquals("Response array element 0 is a JSON message", 2, response.get(0).size());
        Assert.assertTrue("Response array element 1 is an object", response.get(1).isObject());
        Assert.assertEquals("Response array element 1 is a JSON message", 2, response.get(1).size());
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
        Assert.assertNotNull("Response provided", message);
        Assert.assertTrue("Response is an object", message.isObject());
        Assert.assertEquals("Response contains error code 404", 404,
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
        Assert.assertNotNull("Response provided", message);
        Assert.assertTrue("Response is an object", message.isObject());
        Assert.assertEquals("Response contains error code 404", 404,
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
        Assert.assertNotNull("Response provided", message);
        JsonNode data = message.path(JSON.DATA);
        Assert.assertTrue("Response is an object", message.isObject());
        Assert.assertEquals("Response is an error", JsonException.ERROR,
                message.path(JSON.TYPE).asText());
        Assert.assertEquals("Response contains error code 400", 400, data.path(JsonException.CODE).asInt());
        Assert.assertEquals("Response contains error message", "Data property of JSON message missing.",
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
        Assert.assertNotNull("Response provided", message);
        JsonNode data = message.path(JSON.DATA);
        Assert.assertTrue("Response is an object", message.isObject());
        Assert.assertEquals("Response is an error", JsonException.ERROR,
                message.path(JSON.TYPE).asText());
        Assert.assertEquals("Response contains error code 500", 500, data.path(JsonException.CODE).asInt());
        Assert.assertEquals("Response contains error message", "Unsupported operation attempted: null.",
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
        Assert.assertNotNull("Response provided", root);
        JsonNode data = root.path(JSON.DATA);
        Assert.assertTrue("Response is an object", root.isObject());
        Assert.assertEquals("Response object contains two elements", 2, root.size());
        Assert.assertEquals("Response object type is test", TEST, root.path(JSON.TYPE).asText());
        Assert.assertEquals("Response object data name is test", TEST, data.path(JSON.NAME).asText());
        Assert.assertEquals("Response object data size is 1", 1, data.size());
    }

    @Test
    public void testOnMessage_JsonNode_Method_get_exception() throws Exception {
        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        JsonClientHandler instance = new TestJsonClientHandler(connection);
        JsonNode node = connection.getObjectMapper()
                .readTree("{\"type\":\"test\",\"data\":{\"name\":\"JsonException\"},\"method\":\"get\"}");
        instance.onMessage(node);
        JsonNode root = connection.getMessage();
        Assert.assertNotNull("Response provided", root);
        JsonNode data = root.path(JSON.DATA);
        Assert.assertTrue("Error response is an object", root.isObject());
        Assert.assertEquals("Error response is an ERROR", JsonException.ERROR, root.path(JSON.TYPE).asText());
        Assert.assertEquals("Error response is type 499", 499, data.path(JsonException.CODE).asInt());
    }

    @Test
    public void testOnMessage_JsonNode_Method_Goodbye() throws Exception {
        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        JsonClientHandler instance = new TestJsonClientHandler(connection);
        JsonNode node = connection.getObjectMapper().readTree("{\"type\":\"goodbye\"}");
        Assert.assertTrue(connection.isOpen());
        instance.onMessage(node);
        JsonNode root = connection.getMessage();
        Assert.assertNotNull("Response provided", root);
        JsonNode data = root.path(JSON.DATA);
        Assert.assertTrue("Response is an object", root.isObject());
        Assert.assertEquals("Response is a Goodbye message", JSON.GOODBYE, root.path(JSON.TYPE).asText());
        Assert.assertTrue(data.isMissingNode());
        Assert.assertFalse(connection.isOpen());
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
        Assert.assertNotNull("Response provided", root);
        JsonNode data = root.path(JSON.DATA);
        Assert.assertTrue("Error response is an object", root.isObject());
        Assert.assertEquals("Error response is an ERROR", JsonException.ERROR, root.path(JSON.TYPE).asText());
        Assert.assertEquals("Error response is type 400", 400, data.path(JsonException.CODE).asInt());
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
        Assert.assertEquals("Connection is IT Italian", Locale.ITALY, connection.getLocale());
        instance.onMessage("{\"type\":\"locale\", \"data\":{\"locale\":\"en-US\"}}");
        Assert.assertEquals("Connection is US English", Locale.US, connection.getLocale());
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
