package jmri.server.json.message;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.server.json.JSON;
import jmri.server.json.JsonException;
import jmri.server.json.JsonMockConnection;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Randall Wood Copyright 2017
 */
public class JsonMessageSocketServiceTest {

    private final ObjectMapper mapper = new ObjectMapper();

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
    public void testOnMessageHello() throws IOException, JmriException, JsonException {
        String type = JSON.HELLO;
        ObjectNode data = mapper.createObjectNode();
        Locale locale = Locale.ENGLISH;
        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        JsonMessageSocketService instance = new JsonMessageSocketService(connection);
        JsonMessageClientManager manager = InstanceManager.getDefault(JsonMessageClientManager.class);
        Assert.assertNull("No clients", manager.getClient(connection));
        Assert.assertTrue("No clients", manager.getClients(connection).isEmpty());
        instance.onMessage(type, data, JSON.POST, locale, 0);
        Assert.assertNull(connection.getMessage());
        Assert.assertNull("No clients", manager.getClient(connection));
        Assert.assertTrue("No clients", manager.getClients(connection).isEmpty());
        data.put(JsonMessage.CLIENT, ""); // will not subscribe, results in JsonException
        try {
            instance.onMessage(type, data, JSON.POST, locale, 0);
            Assert.fail("Expected exception not thrown.");
        } catch (JsonException ex) {
            Assert.assertEquals("Code is HTTP bad request", HttpServletResponse.SC_BAD_REQUEST, ex.getCode());
            Assert.assertEquals("Property \"client\" must not be empty for type hello.", ex.getMessage());
        }
        Assert.assertNull("No clients", manager.getClient(connection));
        Assert.assertTrue("No clients", manager.getClients(connection).isEmpty());
        data.put(JsonMessage.CLIENT, "client1"); // will subscribe
        instance.onMessage(type, data, JSON.POST, locale, 0);
        Assert.assertNull(connection.getMessage());
        Assert.assertNotNull("One client", manager.getClient(connection));
        Assert.assertEquals("One client", 1, manager.getClients(connection).size());
        instance.onClose(); // clean up
    }

    @Test
    public void testOnMessagePostClient() throws IOException, JmriException, JsonException {
        String type = JsonMessage.CLIENT;
        ObjectNode data = mapper.createObjectNode();
        Locale locale = Locale.ENGLISH;
        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        JsonMessageSocketService instance = new JsonMessageSocketService(connection);
        JsonMessageClientManager manager = InstanceManager.getDefault(JsonMessageClientManager.class);
        Assert.assertNull("No clients", manager.getClient(connection));
        Assert.assertTrue("No clients", manager.getClients(connection).isEmpty());
        try {
            // missing client attribute
            instance.onMessage(type, data, JSON.POST, locale, 0);
            Assert.fail("Expected exception not thrown.");
        } catch (JsonException ex) {
            Assert.assertEquals("Code is HTTP bad request", HttpServletResponse.SC_BAD_REQUEST, ex.getCode());
            Assert.assertEquals("Property \"client\" is required for type client.", ex.getMessage());
        }
        Assert.assertNull("No clients", manager.getClient(connection));
        Assert.assertTrue("No clients", manager.getClients(connection).isEmpty());
        data.put(JsonMessage.CLIENT, ""); // will not subscribe, results in JsonException
        try {
            instance.onMessage(type, data, JSON.POST, locale, 0);
            Assert.fail("Expected exception not thrown.");
        } catch (JsonException ex) {
            Assert.assertEquals("Code is HTTP bad request", HttpServletResponse.SC_BAD_REQUEST, ex.getCode());
            Assert.assertEquals("Property \"client\" must not be empty for type client.", ex.getMessage());
        }
        Assert.assertNull("No clients", manager.getClient(connection));
        Assert.assertTrue("No clients", manager.getClients(connection).isEmpty());
        data.put(JsonMessage.CLIENT, "client1"); // will subscribe
        instance.onMessage(type, data, JSON.POST, locale, 0);
        Assert.assertNull(connection.getMessage());
        Assert.assertNotNull("One client", manager.getClient(connection));
        Assert.assertEquals("One client", 1, manager.getClients(connection).size());
        instance.onClose(); // clean up
    }

    @Test
    public void testOnMessageDeleteClient() throws IOException, JmriException, JsonException {
        String type = JsonMessage.CLIENT;
        ObjectNode data = mapper.createObjectNode();
        Locale locale = Locale.ENGLISH;
        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        JsonMessageSocketService instance = new JsonMessageSocketService(connection);
        JsonMessageClientManager manager = InstanceManager.getDefault(JsonMessageClientManager.class);
        Assert.assertNull("No clients", manager.getClient(connection));
        Assert.assertTrue("No clients", manager.getClients(connection).isEmpty());
        try {
            // missing client attribute
            instance.onMessage(type, data, JSON.DELETE, locale, 0);
            Assert.fail("Expected exception not thrown.");
        } catch (JsonException ex) {
            Assert.assertEquals("Code is HTTP bad request", HttpServletResponse.SC_BAD_REQUEST, ex.getCode());
            Assert.assertEquals("Property \"client\" is required for type client.", ex.getMessage());
        }
        Assert.assertNull("No clients", manager.getClient(connection));
        Assert.assertTrue("No clients", manager.getClients(connection).isEmpty());
        data.put(JsonMessage.CLIENT, ""); // will not delete, results in JsonException
        try {
            instance.onMessage(type, data, JSON.DELETE, locale, 0);
            Assert.fail("Expected exception not thrown.");
        } catch (JsonException ex) {
            Assert.assertEquals("Code is HTTP bad request", HttpServletResponse.SC_BAD_REQUEST, ex.getCode());
            Assert.assertEquals("Property \"client\" must not be empty for type client.", ex.getMessage());
        }
        Assert.assertNull("No clients", manager.getClient(connection));
        Assert.assertTrue("No clients", manager.getClients(connection).isEmpty());
        data.put(JsonMessage.CLIENT, "client1"); // delete non-existent client (silent)
        instance.onMessage(type, data, JSON.DELETE, locale, 0);
        Assert.assertNull(connection.getMessage());
        Assert.assertNull("No clients", manager.getClient(connection));
        Assert.assertTrue("No clients", manager.getClients(connection).isEmpty());
        instance.onMessage(type, data, JSON.POST, locale, 0); // add client
        Assert.assertNull(connection.getMessage());
        Assert.assertNotNull("One client", manager.getClient(connection));
        Assert.assertEquals("One client", 1, manager.getClients(connection).size());
        instance.onMessage(type, data, JSON.DELETE, locale, 0);
        Assert.assertNull(connection.getMessage());
        Assert.assertNull("No clients", manager.getClient(connection));
        Assert.assertTrue("No clients", manager.getClients(connection).isEmpty());
        instance.onClose(); // clean up
    }

    @Test
    public void testOnMessagePutClient() throws IOException, JmriException, JsonException {
        String type = JsonMessage.CLIENT;
        ObjectNode data = mapper.createObjectNode();
        Locale locale = Locale.ENGLISH;
        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        JsonMessageSocketService instance = new JsonMessageSocketService(connection);
        JsonMessageClientManager manager = InstanceManager.getDefault(JsonMessageClientManager.class);
        Assert.assertNull("No clients", manager.getClient(connection));
        Assert.assertTrue("No clients", manager.getClients(connection).isEmpty());
        try {
            // missing client attribute
            instance.onMessage(type, data, JSON.PUT, locale, 0);
            Assert.fail("Expected exception not thrown.");
        } catch (JsonException ex) {
            Assert.assertEquals("Code is HTTP bad request", HttpServletResponse.SC_BAD_REQUEST, ex.getCode());
            Assert.assertEquals("Property \"client\" is required for type client.", ex.getMessage());
        }
        Assert.assertNull("No clients", manager.getClient(connection));
        Assert.assertTrue("No clients", manager.getClients(connection).isEmpty());
        data.put(JsonMessage.CLIENT, ""); // will not subscribe, results in JsonException
        try {
            instance.onMessage(type, data, JSON.PUT, locale, 0);
            Assert.fail("Expected exception not thrown.");
        } catch (JsonException ex) {
            Assert.assertEquals("Code is HTTP bad request", HttpServletResponse.SC_BAD_REQUEST, ex.getCode());
            Assert.assertEquals("Property \"client\" must not be empty for type client.", ex.getMessage());
        }
        Assert.assertNull("No clients", manager.getClient(connection));
        Assert.assertTrue("No clients", manager.getClients(connection).isEmpty());
        data.put(JsonMessage.CLIENT, "client1"); // will subscribe
        instance.onMessage(type, data, JSON.PUT, locale, 0);
        Assert.assertNull(connection.getMessage());
        Assert.assertNotNull("One client", manager.getClient(connection));
        Assert.assertEquals("One client", 1, manager.getClients(connection).size());
        instance.onMessage(type, data, JSON.PUT, locale, 0); // subscribing same client multiple times causes no change
        Assert.assertNull(connection.getMessage());
        Assert.assertNotNull("One client", manager.getClient(connection));
        Assert.assertEquals("One client", 1, manager.getClients(connection).size());
        instance.onClose(); // clean up
    }


    @Test
    public void testOnMessageGetClient() throws IOException, JmriException, JsonException {
        String type = JsonMessage.CLIENT;
        ObjectNode data = mapper.createObjectNode();
        Locale locale = Locale.ENGLISH;
        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        JsonMessageSocketService instance = new JsonMessageSocketService(connection);
        JsonMessageClientManager manager = InstanceManager.getDefault(JsonMessageClientManager.class);
        Assert.assertNull("No clients", manager.getClient(connection));
        Assert.assertTrue("No clients", manager.getClients(connection).isEmpty());
        // auto generate client
        instance.onMessage(type, data, JSON.GET, locale, 0);
        JsonNode message = connection.getMessage();
        Assert.assertNotNull(message);
        Assert.assertEquals(type, message.path(JSON.TYPE).asText());
        try {
            UUID.fromString(message.path(JSON.DATA).path(type).asText());
        } catch (IllegalArgumentException ex) {
            Assert.fail("Client is not UUID");
        }
        Assert.assertNotNull("One client", manager.getClient(connection));
        Assert.assertEquals("One client", 1, manager.getClients(connection).size());
        // list clients
        instance.onMessage(type, data, JSON.GET, locale, 0);
        message = connection.getMessage();
        Assert.assertNotNull(message);
        Assert.assertTrue(message.isArray());
        Assert.assertEquals("Contains one element", 1, message.size());
        // get client
        data.put(JsonMessage.CLIENT, "");
        instance.onMessage(type, data, JSON.GET, locale, 0);
        message = connection.getMessage();
        Assert.assertNotNull(message);
        Assert.assertFalse(message.isArray());
        Assert.assertEquals(type, message.path(JSON.TYPE).asText());
        // get non-existent client
        data.put(JsonMessage.CLIENT, "client1"); // will subscribe
        try {
            instance.onMessage(type, data, JSON.GET, locale, 0);
            Assert.fail("Expected exception not thrown.");
        } catch (JsonException ex) {
            Assert.assertEquals("Code is HTTP not found", 404, ex.getCode());
            Assert.assertEquals("No client \"client1\" found for with this connection.", ex.getMessage());
        }
        // put client, and then get specified existent client
        instance.onMessage(type, data, JSON.PUT, locale, 0);
        instance.onMessage(type, data, JSON.GET, locale, 0);
        message = connection.getMessage();
        Assert.assertNotNull(message);
        Assert.assertFalse(message.isArray());
        Assert.assertEquals(type, message.path(JSON.TYPE).asText());
        Assert.assertEquals("client1", message.path(JSON.DATA).path(JSON.CLIENT).asText());
    }

    @Test
    public void testOnListHello() throws IOException, JmriException {
        String type = JSON.HELLO;
        ObjectNode data = mapper.createObjectNode();
        Locale locale = Locale.ENGLISH;
        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        JsonMessageSocketService instance = new JsonMessageSocketService(connection);
        try {
            instance.onList(type, data, locale, 0);
            Assert.fail("Expected exception not thrown.");
        } catch (JsonException ex) {
            Assert.assertEquals("Code is HTTP bad request", HttpServletResponse.SC_BAD_REQUEST, ex.getCode());
            Assert.assertEquals("hello cannot be listed.", ex.getMessage());
        }
    }

    @Test
    public void testOnListClient() throws IOException, JmriException, JsonException {
        String type = JsonMessage.CLIENT;
        ObjectNode data = mapper.createObjectNode();
        Locale locale = Locale.ENGLISH;
        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        JsonMessageSocketService instance = new JsonMessageSocketService(connection);
        try {
            instance.onList(type, data, locale, 0);
            Assert.fail("Expected exception not thrown.");
        } catch (JsonException ex) {
            Assert.assertEquals("Code is HTTP not found", 404, ex.getCode());
            Assert.assertEquals("No client found for with this connection.", ex.getMessage());
        }
        data.put(JsonMessage.CLIENT, "client1"); // will subscribe
        instance.onMessage(type, data, JSON.PUT, locale, 0);
        data.put(JsonMessage.CLIENT, "client2"); // will subscribe
        instance.onMessage(type, data, JSON.PUT, locale, 0);
        instance.onList(JsonMessage.CLIENT, mapper.createObjectNode(), locale, 0);
        JsonNode message = connection.getMessage();
        Assert.assertNotNull(message);
        Assert.assertTrue(message.isArray());
        Assert.assertEquals("Two array elements", 2, message.size());
    }

    @Test
    public void testOnMessageClientConflicts() throws IOException, JmriException, JsonException {
        String type = JsonMessage.CLIENT;
        ObjectNode data = mapper.createObjectNode().put(JsonMessage.CLIENT, "client");
        Locale locale = Locale.ENGLISH;
        JsonMessageClientManager manager = InstanceManager.getDefault(JsonMessageClientManager.class);
        JsonMockConnection connection1 = new JsonMockConnection((DataOutputStream) null);
        JsonMessageSocketService instance1 = new JsonMessageSocketService(connection1);
        JsonMockConnection connection2 = new JsonMockConnection((DataOutputStream) null);
        JsonMessageSocketService instance2 = new JsonMessageSocketService(connection2);
        instance1.onMessage(type, data, JSON.PUT, locale, 0);
        Assert.assertEquals("connection1 is subscribed", "client", manager.getClient(connection1));
        Assert.assertNull("connection2 is not subscribed", manager.getClient(connection2));
        try {
            instance2.onMessage(type, data, JSON.PUT, locale, 0);
            Assert.fail("Expected exception not thrown");
        } catch (JsonException ex) {
            Assert.assertEquals("Code is HTTP conflict", 409, ex.getCode());
            Assert.assertEquals("Client \"client\" is in use by another connection.", ex.getMessage());
        }
        Assert.assertEquals("connection1 is subscribed", "client", manager.getClient(connection1));
        Assert.assertNull("connection2 is not subscribed", manager.getClient(connection2));
    }
    
}
