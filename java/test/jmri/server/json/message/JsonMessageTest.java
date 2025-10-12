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

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 * @author Randall Wood Copyright (C) 2018
 */
public class JsonMessageTest {

    private final Locale locale = Locale.ENGLISH;

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
    }

    @AfterEach
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
        new JsonMessage("testSend1", locale).send();
        JsonNode message = connection.getMessage();
        assertNotNull(message);
        JsonNode data = message.path(JSON.DATA);
        assertNotNull(data);
        assertEquals(JsonMessage.TYPE.INFO.toString(), data.path(JSON.TYPE).asText());
        assertEquals("testSend1", data.path(JsonMessage.MESSAGE).asText());
        assertTrue(data.path(JsonMessage.CONTEXT).isNull());
        // send to client "1" with default (null) context
        new JsonMessage(JsonMessage.TYPE.INFO, "testSend2", "1", locale).send();
        message = connection.getMessage();
        assertNotNull(message);
        data = message.path(JSON.DATA);
        assertNotNull(data);
        assertEquals(JsonMessage.TYPE.INFO.toString(), data.path(JSON.TYPE).asText());
        assertEquals("testSend2", data.path(JsonMessage.MESSAGE).asText());
        assertTrue(data.path(JsonMessage.CONTEXT).isNull());
        // send to non-existent client "2"
        new JsonMessage(JsonMessage.TYPE.ERROR, "testSend3", "2", locale).send();
        message = connection.getMessage();
        assertNotNull(message);
        data = message.path(JSON.DATA);
        assertNotNull(data);
        assertEquals(JsonMessage.TYPE.INFO.toString(), data.path(JSON.TYPE).asText());
        assertEquals("testSend2", data.path(JsonMessage.MESSAGE).asText());
        assertTrue(data.path(JsonMessage.CONTEXT).isNull());
        // send to client "1" with non-null context
        new JsonMessage(JsonMessage.TYPE.SUCCESS, "testSend4", "1", context, locale).send();
        message = connection.getMessage();
        assertNotNull(message);
        data = message.path(JSON.DATA);
        assertNotNull(data);
        assertEquals(JsonMessage.TYPE.SUCCESS.toString(), data.path(JSON.TYPE).asText());
        assertEquals("testSend4", data.path(JsonMessage.MESSAGE).asText());
        assertFalse(data.path(JsonMessage.CONTEXT).isNull());
        assertEquals(JSON.ASPECT_UNKNOWN, data.path(JsonMessage.CONTEXT).path(JSON.ASPECT).asText());
    }

    @Test
    public void testGetClient() {
        assertNull(new JsonMessage("testGetClient", locale).getClient());
        assertEquals("1", new JsonMessage(JsonMessage.TYPE.INFO, "testGetClient", "1", locale).getClient());
    }

    @Test
    public void testGetContext() {
        ObjectNode context = new ObjectMapper().createObjectNode();
        context.put(JSON.ASPECT, JSON.ASPECT_UNKNOWN); // contents insignificant
        assertNull(new JsonMessage("testGetContext", locale).getContext());
        assertEquals(context, new JsonMessage(JsonMessage.TYPE.INFO, "testGetContext", "1", context, locale).getContext());
    }

    @Test
    public void testGetMessage() {
        assertEquals("testGetMessage", new JsonMessage("testGetMessage", locale).getMessage());
    }

    @Test
    public void testGetLocale() {
        assertEquals(Locale.CANADA_FRENCH, new JsonMessage("testGetLocale", Locale.CANADA_FRENCH).getLocale());
    }

    @Test
    public void testGetType() {
        assertEquals(JsonMessage.TYPE.INFO.toString(), new JsonMessage("testGetType", locale).getType());
        assertEquals(JsonMessage.TYPE.INFO.toString(), new JsonMessage(JsonMessage.TYPE.INFO, "testGetType", locale).getType());
        assertEquals(JsonMessage.TYPE.SUCCESS.toString(), new JsonMessage(JsonMessage.TYPE.SUCCESS, "testGetType", locale).getType());
        assertEquals(JsonMessage.TYPE.WARNING.toString(), new JsonMessage(JsonMessage.TYPE.WARNING, "testGetType", locale).getType());
        assertEquals(JsonMessage.TYPE.ERROR.toString(), new JsonMessage(JsonMessage.TYPE.ERROR, "testGetType", locale).getType());
    }

}
