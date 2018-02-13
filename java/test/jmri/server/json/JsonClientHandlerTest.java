package jmri.server.json;

import static jmri.server.json.JsonTestServiceFactory.TEST;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import jmri.InstanceManager;
import jmri.Version;
import jmri.jmris.json.JsonServerPreferences;
import jmri.profile.NullProfile;
import jmri.profile.Profile;
import jmri.profile.ProfileManager;
import jmri.util.FileUtil;
import jmri.util.JUnitUtil;
import jmri.util.node.NodeIdentity;
import jmri.web.server.WebServerPreferences;
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

    // The minimal setup for log4J
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

    /**
     * Test of sendHello method, of class JsonClientHandler.
     */
    @Test
    public void testSendHello() throws Exception {
        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        JsonClientHandler instance = new TestJsonClientHandler(connection);
        instance.sendHello(1000);
        JsonNode root = connection.getMessage();
        JsonNode data = root.path(JSON.DATA);
        Assert.assertEquals("Hello type", JSON.HELLO, root.path(JSON.TYPE).asText());
        Assert.assertEquals("JMRI Version", Version.name(), data.path(JSON.JMRI).asText());
        Assert.assertEquals("JSON Verson", JSON.JSON_PROTOCOL_VERSION, data.path(JSON.JSON).asText());
        Assert.assertEquals("Heartbeat", Math.round(InstanceManager.getDefault(JsonServerPreferences.class).getHeartbeatInterval() * 0.9f), data.path(JSON.HEARTBEAT).asInt());
        Assert.assertEquals("RR Name", InstanceManager.getDefault(WebServerPreferences.class).getRailroadName(), data.path(JSON.RAILROAD).asText());
        Assert.assertEquals("Node Identity", NodeIdentity.identity(), data.path(JSON.NODE).asText());
        Profile profile = ProfileManager.getDefault().getActiveProfile();
        Assert.assertNotNull(profile);
        Assert.assertEquals("Profile", profile.getName(), data.path(JSON.ACTIVE_PROFILE).asText());
        Assert.assertEquals("Message has 2 elements", 2, root.size());
        Assert.assertEquals("Message data has 6 elements", 6, data.size());
    }

    private class TestJsonClientHandler extends JsonClientHandler {

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
