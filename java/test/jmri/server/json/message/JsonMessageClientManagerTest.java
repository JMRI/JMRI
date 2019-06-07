package jmri.server.json.message;

import java.io.DataOutputStream;
import java.util.Locale;
import jmri.server.json.JSON;
import jmri.server.json.JsonMockConnection;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;

/**
 *
 * @author Randall Wood (C) 2017
 */
public class JsonMessageClientManagerTest {

    private Locale locale = Locale.ENGLISH;

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
    public void testSubscribe() {
        JsonMockConnection connection1 = new JsonMockConnection((DataOutputStream) null);
        JsonMockConnection connection2 = new JsonMockConnection((DataOutputStream) null);
        JsonNode message1;
        JsonNode message2;
        JsonMessageClientManager instance = new JsonMessageClientManager();
        instance.subscribe("1", connection1);
        boolean exceptionThrown = false;
        try {
            instance.subscribe("1", connection2);
        } catch (IllegalArgumentException ex) {
            exceptionThrown = true;
        }
        Assert.assertTrue(exceptionThrown);
        instance.subscribe("2", connection2);
        instance.send(new JsonMessage("testSubscribe1", locale));
        message1 = connection1.getMessage();
        message2 = connection2.getMessage();
        Assert.assertNotNull("message1 is not null", message1);
        Assert.assertNotNull("message2 is not null", message2);
        Assert.assertEquals("testSubscribe1", message1.path(JSON.DATA).path(JsonMessage.MESSAGE).asText());
        Assert.assertEquals("testSubscribe1", message2.path(JSON.DATA).path(JsonMessage.MESSAGE).asText());
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
        Assert.assertNotNull("message1 is not null", message1);
        Assert.assertNotNull("message2 is not null", message2);
        Assert.assertEquals("testUnsubscribe1", message1.path(JSON.DATA).path(JsonMessage.MESSAGE).asText());
        Assert.assertEquals("testUnsubscribe1", message2.path(JSON.DATA).path(JsonMessage.MESSAGE).asText());
        instance.unsubscribe("2");
        instance.send(new JsonMessage("testUnsubscribe2", locale));
        message1 = connection1.getMessage();
        message2 = connection2.getMessage();
        Assert.assertNotNull("message1 is not null", message1);
        Assert.assertNotNull("message2 is not null", message2);
        Assert.assertEquals("testUnsubscribe2", message1.path(JSON.DATA).path(JsonMessage.MESSAGE).asText());
        Assert.assertEquals("testUnsubscribe1", message2.path(JSON.DATA).path(JsonMessage.MESSAGE).asText());
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
        Assert.assertNotNull("message1 is not null", message1);
        Assert.assertNotNull("message2 is not null", message2);
        Assert.assertEquals("testSend1", message1.path(JSON.DATA).path(JsonMessage.MESSAGE).asText());
        Assert.assertEquals("testSend1", message2.path(JSON.DATA).path(JsonMessage.MESSAGE).asText());
        instance.send(new JsonMessage(JsonMessage.TYPE.INFO, "testSend2", "1", locale));
        message1 = connection1.getMessage();
        message2 = connection2.getMessage();
        Assert.assertNotNull("message1 is not null", message1);
        Assert.assertNotNull("message2 is not null", message2);
        Assert.assertEquals("testSend2", message1.path(JSON.DATA).path(JsonMessage.MESSAGE).asText());
        Assert.assertEquals("testSend1", message2.path(JSON.DATA).path(JsonMessage.MESSAGE).asText());
    }

}
