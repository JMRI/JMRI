package jmri.server.json.message;

import java.io.DataOutputStream;
import java.util.Locale;

import jmri.server.json.JSON;
import jmri.server.json.JsonMockConnection;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import com.fasterxml.jackson.databind.JsonNode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 *
 * @author Randall Wood (C) 2017
 */
public class JsonMessageClientManagerTest {

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
    public void testSubscribe() {
        JsonMockConnection connection1 = new JsonMockConnection((DataOutputStream) null);
        JsonMockConnection connection2 = new JsonMockConnection((DataOutputStream) null);
        JsonNode message1;
        JsonNode message2;
        JsonMessageClientManager instance = new JsonMessageClientManager();
        instance.subscribe("1", connection1);
        IllegalArgumentException ex = assertThrows( IllegalArgumentException.class, () ->
            instance.subscribe("1", connection2));
        assertNotNull(ex);
        instance.subscribe("2", connection2);
        instance.send(new JsonMessage("testSubscribe1", locale));
        message1 = connection1.getMessage();
        message2 = connection2.getMessage();
        assertNotNull( message1, "message1 is not null");
        assertNotNull( message2, "message2 is not null");
        assertEquals("testSubscribe1", message1.path(JSON.DATA).path(JsonMessage.MESSAGE).asText());
        assertEquals("testSubscribe1", message2.path(JSON.DATA).path(JsonMessage.MESSAGE).asText());
    }

    @Test
    public void testUnsubscribe() {
        JsonMockConnection connection1 = new JsonMockConnection((DataOutputStream) null);
        JsonMockConnection connection2 = new JsonMockConnection((DataOutputStream) null);
        JsonNode message1;
        JsonNode message2;
        JsonMessageClientManager instance = new JsonMessageClientManager();
        instance.subscribe("1", connection1);
        instance.subscribe("2", connection2);
        instance.send(new JsonMessage("testUnsubscribe1", locale));
        message1 = connection1.getMessage();
        message2 = connection2.getMessage();
        assertNotNull( message1, "message1 is not null");
        assertNotNull( message2, "message2 is not null");
        assertEquals("testUnsubscribe1", message1.path(JSON.DATA).path(JsonMessage.MESSAGE).asText());
        assertEquals("testUnsubscribe1", message2.path(JSON.DATA).path(JsonMessage.MESSAGE).asText());
        instance.unsubscribe("2");
        instance.send(new JsonMessage("testUnsubscribe2", locale));
        message1 = connection1.getMessage();
        message2 = connection2.getMessage();
        assertNotNull( message1, "message1 is not null");
        assertNotNull( message2, "message2 is not null");
        assertEquals("testUnsubscribe2", message1.path(JSON.DATA).path(JsonMessage.MESSAGE).asText());
        assertEquals("testUnsubscribe1", message2.path(JSON.DATA).path(JsonMessage.MESSAGE).asText());
    }

    @Test
    public void testSend() {
        JsonMockConnection connection1 = new JsonMockConnection((DataOutputStream) null);
        JsonMockConnection connection2 = new JsonMockConnection((DataOutputStream) null);
        JsonNode message1;
        JsonNode message2;
        JsonMessageClientManager instance = new JsonMessageClientManager();
        instance.subscribe("1", connection1);
        instance.subscribe("2", connection2);
        instance.send(new JsonMessage("testSend1", locale));
        message1 = connection1.getMessage();
        message2 = connection2.getMessage();
        assertNotNull( message1, "message1 is not null");
        assertNotNull( message2, "message2 is not null");
        assertEquals("testSend1", message1.path(JSON.DATA).path(JsonMessage.MESSAGE).asText());
        assertEquals("testSend1", message2.path(JSON.DATA).path(JsonMessage.MESSAGE).asText());
        instance.send(new JsonMessage(JsonMessage.TYPE.INFO, "testSend2", "1", locale));
        message1 = connection1.getMessage();
        message2 = connection2.getMessage();
        assertNotNull( message1, "message1 is not null");
        assertNotNull( message2, "message2 is not null");
        assertEquals("testSend2", message1.path(JSON.DATA).path(JsonMessage.MESSAGE).asText());
        assertEquals("testSend1", message2.path(JSON.DATA).path(JsonMessage.MESSAGE).asText());
    }

}
