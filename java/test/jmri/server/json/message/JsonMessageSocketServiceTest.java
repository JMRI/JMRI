package jmri.server.json.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Locale;
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
        instance.onMessage(type, data, JSON.POST, locale);
        Assert.assertNull(connection.getMessage());
        Assert.assertNull("No clients", manager.getClient(connection));
        Assert.assertTrue("No clients", manager.getClients(connection).isEmpty());
        data.put(JsonMessage.CLIENT, ""); // will not subscribe, results in JsonException
        try {
            instance.onMessage(type, data, JSON.POST, locale);
            Assert.fail("Expected exception not thrown.");
        } catch (JsonException ex) {
            Assert.assertEquals("Code is HTTP bad request", HttpServletResponse.SC_BAD_REQUEST, ex.getCode());
            Assert.assertEquals("Data attribute \"client\" for type \"hello\" must not be empty.", ex.getMessage());
        }
        Assert.assertNull("No clients", manager.getClient(connection));
        Assert.assertTrue("No clients", manager.getClients(connection).isEmpty());
        data.put(JsonMessage.CLIENT, "client1"); // will subscribe
        instance.onMessage(type, data, JSON.POST, locale);
        Assert.assertNull(connection.getMessage());
        Assert.assertNotNull("One client", manager.getClient(connection));
        Assert.assertEquals("One client", 1, manager.getClients(connection).size());
        instance.onClose(); // clean up
    }
    }

    @Test
    public void testOnListHello() throws IOException, JmriException {
        String type = JSON.HELLO;
        ObjectNode data = mapper.createObjectNode();
        Locale locale = Locale.ENGLISH;
        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        JsonMessageSocketService instance = new JsonMessageSocketService(connection);
        try {
            instance.onList(type, data, locale);
            Assert.fail("Expected exception not thrown.");
        } catch (JsonException ex) {
            Assert.assertEquals("Code is HTTP bad request", HttpServletResponse.SC_BAD_REQUEST, ex.getCode());
            Assert.assertEquals("hello cannot be listed.", ex.getMessage());
        }
    }

}
