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
import jmri.server.json.JsonRequest;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 * @author Randall Wood Copyright 2017
 */
public class JsonMessageSocketServiceTest {

    private final ObjectMapper mapper = new ObjectMapper();

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
    public void testOnMessageHello() throws IOException, JmriException, JsonException {
        String type = JSON.HELLO;
        ObjectNode data = mapper.createObjectNode();
        Locale locale = Locale.ENGLISH;
        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        JsonMessageSocketService instance = new JsonMessageSocketService(connection);
        JsonMessageClientManager manager = InstanceManager.getDefault(JsonMessageClientManager.class);
        assertNull( manager.getClient(connection), "No clients");
        assertTrue( manager.getClients(connection).isEmpty(), "No clients");
        instance.onMessage(type, data, new JsonRequest(locale, JSON.V5, JSON.POST, 0));
        assertNull(connection.getMessage());
        assertNull( manager.getClient(connection), "No clients");
        assertTrue( manager.getClients(connection).isEmpty(), "No clients");
        data.put(JsonMessage.CLIENT, ""); // will not subscribe, results in JsonException
        JsonException ex = assertThrows( JsonException.class, () ->
            instance.onMessage(type, data, new JsonRequest(locale, JSON.V5, JSON.POST, 0)),
            "Expected exception not thrown.");
        assertEquals( HttpServletResponse.SC_BAD_REQUEST, ex.getCode(), "Code is HTTP bad request");
        assertEquals("Property \"client\" must not be empty for type hello.", ex.getMessage());

        assertNull( manager.getClient(connection), "No clients");
        assertTrue( manager.getClients(connection).isEmpty(), "No clients");
        data.put(JsonMessage.CLIENT, "client1"); // will subscribe
        instance.onMessage(type, data, new JsonRequest(locale, JSON.V5, JSON.POST, 0));
        assertNull(connection.getMessage());
        assertNotNull( manager.getClient(connection), "One client");
        assertEquals( 1, manager.getClients(connection).size(), "One client");
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
        assertNull( manager.getClient(connection), "No clients");
        assertTrue( manager.getClients(connection).isEmpty(), "No clients");
        JsonException ex = assertThrows( JsonException.class, () ->
            // missing client attribute
            instance.onMessage(type, data, new JsonRequest(locale, JSON.V5, JSON.POST, 0)),
            "Expected exception not thrown.");
        assertEquals( HttpServletResponse.SC_BAD_REQUEST, ex.getCode(), "Code is HTTP bad request");
        assertEquals("Property \"client\" is required for type client.", ex.getMessage());

        assertNull( manager.getClient(connection), "No clients");
        assertTrue( manager.getClients(connection).isEmpty(), "No clients");
        data.put(JsonMessage.CLIENT, ""); // will not subscribe, results in JsonException
        ex = assertThrows( JsonException.class, () ->
            instance.onMessage(type, data, new JsonRequest(locale, JSON.V5, JSON.POST, 0)),
            "Expected exception not thrown.");
        assertEquals( HttpServletResponse.SC_BAD_REQUEST, ex.getCode(), "Code is HTTP bad request");
        assertEquals("Property \"client\" must not be empty for type client.", ex.getMessage());

        assertNull( manager.getClient(connection), "No clients");
        assertTrue( manager.getClients(connection).isEmpty(), "No clients");
        data.put(JsonMessage.CLIENT, "client1"); // will subscribe
        instance.onMessage(type, data, new JsonRequest(locale, JSON.V5, JSON.POST, 0));
        assertNull(connection.getMessage());
        assertNotNull( manager.getClient(connection), "One client");
        assertEquals( 1, manager.getClients(connection).size(), "One client");
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
        assertNull( manager.getClient(connection), "No clients");
        assertTrue( manager.getClients(connection).isEmpty(), "No clients");
        JsonException ex = assertThrows( JsonException.class, () ->
            // missing client attribute
            instance.onMessage(type, data, new JsonRequest(locale, JSON.V5, JSON.DELETE, 0)),
            "Expected exception not thrown.");
        assertEquals( HttpServletResponse.SC_BAD_REQUEST, ex.getCode(), "Code is HTTP bad request");
        assertEquals("Property \"client\" is required for type client.", ex.getMessage());

        assertNull( manager.getClient(connection), "No clients");
        assertTrue( manager.getClients(connection).isEmpty(), "No clients");
        data.put(JsonMessage.CLIENT, ""); // will not delete, results in JsonException
        ex = assertThrows( JsonException.class, () ->
            instance.onMessage(type, data, new JsonRequest(locale, JSON.V5, JSON.DELETE, 0)),
            "Expected exception not thrown.");
        assertEquals( HttpServletResponse.SC_BAD_REQUEST, ex.getCode(), "Code is HTTP bad request");
        assertEquals("Property \"client\" must not be empty for type client.", ex.getMessage());

        assertNull( manager.getClient(connection), "No clients");
        assertTrue( manager.getClients(connection).isEmpty(), "No clients");
        data.put(JsonMessage.CLIENT, "client1"); // delete non-existent client (silent)
        instance.onMessage(type, data, new JsonRequest(locale, JSON.V5, JSON.DELETE, 0));
        assertNull(connection.getMessage());
        assertNull( manager.getClient(connection), "No clients");
        assertTrue( manager.getClients(connection).isEmpty(), "No clients");
        instance.onMessage(type, data, new JsonRequest(locale, JSON.V5, JSON.POST, 0)); // add client
        assertNull(connection.getMessage());
        assertNotNull( manager.getClient(connection), "One client");
        assertEquals( 1, manager.getClients(connection).size(), "One client");
        instance.onMessage(type, data, new JsonRequest(locale, JSON.V5, JSON.DELETE, 0));
        assertNull(connection.getMessage());
        assertNull( manager.getClient(connection), "No clients");
        assertTrue( manager.getClients(connection).isEmpty(), "No clients");
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
        assertNull( manager.getClient(connection), "No clients");
        assertTrue( manager.getClients(connection).isEmpty(), "No clients");
        JsonException ex = assertThrows( JsonException.class, () ->
            // missing client attribute
            instance.onMessage(type, data, new JsonRequest(locale, JSON.V5, JSON.PUT, 0)),
            "Expected exception not thrown.");
        assertEquals( HttpServletResponse.SC_BAD_REQUEST, ex.getCode(), "Code is HTTP bad request");
        assertEquals("Property \"client\" is required for type client.", ex.getMessage());

        assertNull( manager.getClient(connection), "No clients");
        assertTrue( manager.getClients(connection).isEmpty(), "No clients");
        data.put(JsonMessage.CLIENT, ""); // will not subscribe, results in JsonException
        ex = assertThrows( JsonException.class, () ->
            instance.onMessage(type, data, new JsonRequest(locale, JSON.V5, JSON.PUT, 0)),
            "Expected exception not thrown.");
        assertEquals( HttpServletResponse.SC_BAD_REQUEST, ex.getCode(), "Code is HTTP bad request");
        assertEquals("Property \"client\" must not be empty for type client.", ex.getMessage());

        assertNull( manager.getClient(connection), "No clients");
        assertTrue( manager.getClients(connection).isEmpty(), "No clients");
        data.put(JsonMessage.CLIENT, "client1"); // will subscribe
        instance.onMessage(type, data, new JsonRequest(locale, JSON.V5, JSON.PUT, 0));
        assertNull(connection.getMessage());
        assertNotNull( manager.getClient(connection), "One client");
        assertEquals( 1, manager.getClients(connection).size(), "One client");
        instance.onMessage(type, data, new JsonRequest(locale, JSON.V5, JSON.PUT, 0)); // subscribing same client multiple times causes no change
        assertNull(connection.getMessage());
        assertNotNull( manager.getClient(connection), "One client");
        assertEquals( 1, manager.getClients(connection).size(), "One client");
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
        assertNull( manager.getClient(connection), "No clients");
        assertTrue( manager.getClients(connection).isEmpty(), "No clients");
        // auto generate client
        instance.onMessage(type, data, new JsonRequest(locale, JSON.V5, JSON.GET, 0));
        JsonNode messageEx = connection.getMessage();
        assertNotNull(messageEx);
        assertEquals(type, messageEx.path(JSON.TYPE).asText());
        assertDoesNotThrow( () ->
            UUID.fromString(messageEx.path(JSON.DATA).path(type).asText()),
            "Client is not UUID");

        assertNotNull( manager.getClient(connection), "One client");
        assertEquals( 1, manager.getClients(connection).size(), "One client");
        // list clients
        instance.onMessage(type, data, new JsonRequest(locale, JSON.V5, JSON.GET, 0));
        JsonNode message = connection.getMessage();
        assertNotNull(message);
        assertTrue(message.isArray());
        assertEquals( 1, message.size(), "Contains one element");
        // get client
        data.put(JsonMessage.CLIENT, "");
        instance.onMessage(type, data, new JsonRequest(locale, JSON.V5, JSON.GET, 0));
        message = connection.getMessage();
        assertNotNull(message);
        assertFalse(message.isArray());
        assertEquals(type, message.path(JSON.TYPE).asText());
        // get non-existent client
        data.put(JsonMessage.CLIENT, "client1"); // will subscribe
        JsonException ex = assertThrows( JsonException.class, () ->
            instance.onMessage(type, data, new JsonRequest(locale, JSON.V5, JSON.GET, 0)),
            "Expected exception not thrown.");
        assertEquals( 404, ex.getCode(), "Code is HTTP not found");
        assertEquals("No client \"client1\" found for with this connection.", ex.getMessage());

        // put client, and then get specified existent client
        instance.onMessage(type, data, new JsonRequest(locale, JSON.V5, JSON.PUT, 0));
        instance.onMessage(type, data, new JsonRequest(locale, JSON.V5, JSON.GET, 0));
        message = connection.getMessage();
        assertNotNull(message);
        assertFalse(message.isArray());
        assertEquals(type, message.path(JSON.TYPE).asText());
        assertEquals("client1", message.path(JSON.DATA).path(JSON.CLIENT).asText());
    }

    @Test
    public void testOnListHello() throws IOException, JmriException {
        String type = JSON.HELLO;
        ObjectNode data = mapper.createObjectNode();
        Locale locale = Locale.ENGLISH;
        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        JsonMessageSocketService instance = new JsonMessageSocketService(connection);
        JsonException ex = assertThrows( JsonException.class, () ->
            instance.onList(type, data, new JsonRequest(locale, JSON.V5, JSON.GET, 0)),
            "Expected exception not thrown.");
        assertEquals( HttpServletResponse.SC_BAD_REQUEST, ex.getCode(), "Code is HTTP bad request");
        assertEquals("hello cannot be listed.", ex.getMessage());
    }

    @Test
    public void testOnListClient() throws IOException, JmriException, JsonException {
        String type = JsonMessage.CLIENT;
        ObjectNode data = mapper.createObjectNode();
        Locale locale = Locale.ENGLISH;
        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        JsonMessageSocketService instance = new JsonMessageSocketService(connection);
        JsonException ex = assertThrows( JsonException.class, () ->
            instance.onList(type, data, new JsonRequest(locale, JSON.V5, JSON.GET, 0)),
            "Expected exception not thrown.");
        assertEquals( 404, ex.getCode(), "Code is HTTP not found");
        assertEquals("No client found for with this connection.", ex.getMessage());

        data.put(JsonMessage.CLIENT, "client1"); // will subscribe
        instance.onMessage(type, data, new JsonRequest(locale, JSON.V5, JSON.PUT, 0));
        data.put(JsonMessage.CLIENT, "client2"); // will subscribe
        instance.onMessage(type, data, new JsonRequest(locale, JSON.V5, JSON.PUT, 0));
        instance.onList(JsonMessage.CLIENT, mapper.createObjectNode(), new JsonRequest(locale, JSON.V5, JSON.GET, 0));
        JsonNode message = connection.getMessage();
        assertNotNull(message);
        assertTrue(message.isArray());
        assertEquals( 2, message.size(), "Two array elements");
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
        instance1.onMessage(type, data, new JsonRequest(locale, JSON.V5, JSON.PUT, 0));
        assertEquals( "client", manager.getClient(connection1), "connection1 is subscribed");
        assertNull( manager.getClient(connection2), "connection2 is not subscribed");
        JsonException ex = assertThrows( JsonException.class, () ->
            instance2.onMessage(type, data, new JsonRequest(locale, JSON.V5, JSON.PUT, 0)),
            "Expected exception not thrown");
        assertEquals( 409, ex.getCode(), "Code is HTTP conflict");
        assertEquals("Client \"client\" is in use by another connection.", ex.getMessage());

        assertEquals( "client", manager.getClient(connection1), "connection1 is subscribed");
        assertNull( manager.getClient(connection2), "connection2 is not subscribed");
    }
    
}
