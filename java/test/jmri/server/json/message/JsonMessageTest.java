package jmri.server.json.message;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.DataOutputStream;
import java.util.Locale;
import jmri.InstanceManager;
import jmri.server.json.JSON;
import jmri.server.json.JsonMockConnection;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Randall Wood Copyright (C) 2018
 */
public class JsonMessageTest {

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    @Test
    public void testSend() {
        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        InstanceManager.getDefault(JsonMessageClientManager.class).subscribe("1", connection);
        ObjectNode context = new ObjectMapper().createObjectNode();
        context.put(JSON.ASPECT, JSON.ASPECT_UNKNOWN); // contents insignificant
        // send to all
        new JsonMessage("testSend1", Locale.ENGLISH).send();
        JsonNode message = connection.getMessage();
        Assert.assertNotNull(message);
        JsonNode data = message.path(JSON.DATA);
        Assert.assertNotNull(data);
        Assert.assertEquals(JsonMessage.INFO, data.path(JSON.TYPE).asText());
        Assert.assertEquals("testSend1", data.path(JsonMessage.MESSAGE).asText());
        Assert.assertTrue(data.path(JsonMessage.CONTEXT).isNull());
        // send to client "1" with default (null) context
        new JsonMessage(JsonMessage.TYPE.INFO, "testSend2", "1", Locale.ENGLISH).send();
        message = connection.getMessage();
        Assert.assertNotNull(message);
        data = message.path(JSON.DATA);
        Assert.assertNotNull(data);
        Assert.assertEquals(JsonMessage.INFO, data.path(JSON.TYPE).asText());
        Assert.assertEquals("testSend2", data.path(JsonMessage.MESSAGE).asText());
        Assert.assertTrue(data.path(JsonMessage.CONTEXT).isNull());
        // send to non-existent client "2"
        new JsonMessage(JsonMessage.TYPE.ERROR, "testSend3", "2", Locale.ENGLISH).send();
        message = connection.getMessage();
        Assert.assertNotNull(message);
        data = message.path(JSON.DATA);
        Assert.assertNotNull(data);
        Assert.assertEquals(JsonMessage.INFO, data.path(JSON.TYPE).asText());
        Assert.assertEquals("testSend2", data.path(JsonMessage.MESSAGE).asText());
        Assert.assertTrue(data.path(JsonMessage.CONTEXT).isNull());
        // send to client "1" with non-null context
        new JsonMessage(JsonMessage.TYPE.SUCCESS, "testSend4", "1", context, Locale.ENGLISH).send();
        message = connection.getMessage();
        Assert.assertNotNull(message);
        data = message.path(JSON.DATA);
        Assert.assertNotNull(data);
        Assert.assertEquals(JsonMessage.SUCCESS, data.path(JSON.TYPE).asText());
        Assert.assertEquals("testSend4", data.path(JsonMessage.MESSAGE).asText());
        Assert.assertFalse(data.path(JsonMessage.CONTEXT).isNull());
        Assert.assertEquals(JSON.ASPECT_UNKNOWN, data.path(JsonMessage.CONTEXT).path(JSON.ASPECT).asText());
    }

    @Test
    public void testGetClient() {
        Assert.assertNull(new JsonMessage("testGetClient", Locale.ENGLISH).getClient());
        Assert.assertEquals("1", new JsonMessage(JsonMessage.TYPE.INFO, "testGetClient", "1", Locale.ENGLISH).getClient());
    }

    @Test
    public void testGetContext() {
        ObjectNode context = new ObjectMapper().createObjectNode();
        context.put(JSON.ASPECT, JSON.ASPECT_UNKNOWN); // contents insignificant
        Assert.assertNull(new JsonMessage("testGetContext", Locale.ENGLISH).getContext());
        Assert.assertEquals(context, new JsonMessage(JsonMessage.TYPE.INFO, "testGetContext", "1", context, Locale.ENGLISH).getContext());
    }

    @Test
    public void testGetMessage() {
        Assert.assertEquals("testGetMessage", new JsonMessage("testGetMessage", Locale.ENGLISH).getMessage());
    }

    @Test
    public void testGetLocale() {
        Assert.assertEquals(Locale.CANADA_FRENCH, new JsonMessage("testGetLocale", Locale.CANADA_FRENCH).getLocale());
    }

    @Test
    public void testGetType() {
        Assert.assertEquals(JsonMessage.INFO, new JsonMessage("testGetType", Locale.ENGLISH).getType());
        Assert.assertEquals(JsonMessage.INFO, new JsonMessage(JsonMessage.TYPE.INFO, "testGetType", Locale.ENGLISH).getType());
        Assert.assertEquals(JsonMessage.SUCCESS, new JsonMessage(JsonMessage.TYPE.SUCCESS, "testGetType", Locale.ENGLISH).getType());
        Assert.assertEquals(JsonMessage.WARNING, new JsonMessage(JsonMessage.TYPE.WARNING, "testGetType", Locale.ENGLISH).getType());
        Assert.assertEquals(JsonMessage.ERROR, new JsonMessage(JsonMessage.TYPE.ERROR, "testGetType", Locale.ENGLISH).getType());
    }

}
