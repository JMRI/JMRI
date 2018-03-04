package jmri.server.json;

import static jmri.server.json.JsonTestServiceFactory.TEST;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
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

    @Test
    public void testCTor() {
        JsonMockConnection mc = new JsonMockConnection((DataOutputStream) null);
        JsonClientHandler t = new JsonClientHandler(mc);
        Assert.assertNotNull("exists", t);
    }

    @Before
    public void setUp() throws IOException {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager(new NullProfile("JsonClientHandlerTest", "12345678", FileUtil.getFile("program:test")));
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
     */
    @Test
    public void testOnMessage_String() throws Exception {
        String string = "{\"type\":\"test\",\"method\":\"list\"}";
        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        JsonClientHandler instance = new TestJsonClientHandler(connection);
        instance.onMessage(string);
        Assert.assertTrue("Response is an array", connection.getMessage().isArray());
        Assert.assertEquals("Response array contains two elements", 2, connection.getMessage().size());
        Assert.assertTrue("Response array element 0 is an object", connection.getMessage().get(0).isObject());
        Assert.assertEquals("Response array element 0 is empty", 0, connection.getMessage().get(0).size());
        Assert.assertTrue("Response array element 1 is an object", connection.getMessage().get(1).isObject());
        Assert.assertEquals("Response array element 1 is empty", 0, connection.getMessage().get(1).size());
        instance.onMessage("not a JSON object");
        Assert.assertNotNull("Expected warning not shown", JUnitAppender.checkForMessageStartingWith("Exception processing \"not a JSON object\""));
        Assert.assertTrue("Error response is an object", connection.getMessage().isObject());
        Assert.assertEquals("Error response is an ERROR", JsonException.ERROR, connection.getMessage().path(JSON.TYPE).asText());
        Assert.assertEquals("Error response is type 500", 500, connection.getMessage().path(JSON.DATA).path(JsonException.CODE).asInt());
    }

    /**
     * Test of onMessage method, of class JsonClientHandler.
     */
    @Test
    public void testOnMessage_JsonNode_Method_list() throws Exception {
        testOnMessage_JsonNode_Method_list("{\"type\":\"test\",\"method\":\"list\"}");
        testOnMessage_JsonNode_Method_list("{\"type\":\"list\",\"list\":\"test\"}");
        testOnMessage_JsonNode_Method_list("{\"list\":\"test\"}");
    }

    private void testOnMessage_JsonNode_Method_list(String message) throws Exception {
        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        JsonClientHandler instance = new TestJsonClientHandler(connection);
        JsonNode node = connection.getObjectMapper().readTree(message);
        instance.onMessage(node);
        Assert.assertTrue("Response is an array", connection.getMessage().isArray());
        Assert.assertEquals("Response array contains two elements", 2, connection.getMessage().size());
        Assert.assertTrue("Response array element 0 is an object", connection.getMessage().get(0).isObject());
        Assert.assertEquals("Response array element 0 is empty", 0, connection.getMessage().get(0).size());
        Assert.assertTrue("Response array element 1 is an object", connection.getMessage().get(1).isObject());
        Assert.assertEquals("Response array element 1 is empty", 0, connection.getMessage().get(1).size());
    }

    /**
     * Test of onMessage method, of class JsonClientHandler.
     */
    @Test
    public void testOnMessage_JsonNode_Method_get() throws Exception {
        testOnMessage_JsonNode_Method_get("{\"type\":\"test\",\"data\":{\"name\":\"test\"},\"method\":\"get\"}");
        testOnMessage_JsonNode_Method_get("{\"type\":\"test\",\"data\":{\"name\":\"test\",\"method\":\"get\"}}");
    }

    private void testOnMessage_JsonNode_Method_get(String message) throws Exception {
        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        JsonClientHandler instance = new TestJsonClientHandler(connection);
        JsonNode node = connection.getObjectMapper().readTree(message);
        instance.onMessage(node);
        JsonNode root = connection.getMessage();
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
        JsonNode node = connection.getObjectMapper().readTree("{\"type\":\"test\",\"data\":{\"name\":\"JsonException\"},\"method\":\"get\"}");
        instance.onMessage(node);
        JsonNode root = connection.getMessage();
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
        JsonNode data = root.path(JSON.DATA);
        Assert.assertTrue("Error response is an object", root.isObject());
        Assert.assertEquals("Error response is an ERROR", JsonException.ERROR, root.path(JSON.TYPE).asText());
        Assert.assertEquals("Error response is type 400", 400, data.path(JsonException.CODE).asInt());
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
