package jmri.server.json.signalhead;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Locale;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.SignalHead;
import jmri.SignalHeadManager;
import jmri.implementation.VirtualSignalHead;
import jmri.server.json.JSON;
import jmri.server.json.JsonException;
import jmri.server.json.JsonMockConnection;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender
 * @author Randall Wood
 * @author Steve Todd
 */
public class JsonSignalHeadSocketServiceTest {

    private Locale locale = Locale.ENGLISH;

    @Test
    public void testSignalHeadChange() throws IOException, JmriException, JsonException {
        //create a signal head for testing
        String sysName = "IH1";
        String userName = "SH1";
        SignalHead s = new VirtualSignalHead(sysName, userName);
        SignalHeadManager manager = InstanceManager.getDefault(SignalHeadManager.class);
        manager.register(s);
        Assert.assertNotNull(s);
        Assert.assertEquals("One listener", 1, s.getNumPropertyChangeListeners());

        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        TestJsonSignalHeadHttpService http = new TestJsonSignalHeadHttpService(connection.getObjectMapper());
        JsonNode message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, sysName);
        JsonSignalHeadSocketService service = new JsonSignalHeadSocketService(connection, http);
        service.onMessage(JsonSignalHead.SIGNAL_HEAD, message, JSON.POST, locale, 42);
        Assert.assertEquals("Two listeners", 2, s.getNumPropertyChangeListeners());

        //signalhead defaults to Dark
        message = connection.getMessage();
        Assert.assertNotNull(message);
        Assert.assertEquals(SignalHead.DARK, message.path(JSON.DATA).path(JSON.STATE).asInt());

        //change to Green, and wait for change to show up, then verify
        s.setAppearance(SignalHead.GREEN);
        JUnitUtil.waitFor(() -> {
            return s.getState() == SignalHead.GREEN;
        }, "SignalHead is now GREEN");
        message = connection.getMessage();
        Assert.assertNotNull(message);
        Assert.assertEquals(SignalHead.GREEN, message.path(JSON.DATA).path(JSON.STATE).asInt());

        //change to Red, and wait for change to show up, then verify
        s.setAppearance(SignalHead.RED);
        JUnitUtil.waitFor(() -> {
            return s.getState() == SignalHead.RED;
        }, "SignalHead is now RED");
        message = connection.getMessage();
        Assert.assertNotNull(message);
        Assert.assertEquals(SignalHead.RED, message.path(JSON.DATA).path(JSON.STATE).asInt());

        // put a new signal head
        try {
            message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "something"); // does not matter
            service.onMessage(JsonSignalHead.SIGNAL_HEAD, message, JSON.PUT, locale, 42);
            Assert.fail("Expected exception not thrown");
        } catch (JsonException ex) {
            Assert.assertEquals("Error code is HTTP Invalid Request", 405, ex.getCode());
            Assert.assertEquals("Error message", "Putting signalHead is not allowed.", ex.getMessage());
        }

        // trap JsonException error
        http.setThrowException(1);
        s.setAppearance(SignalHead.GREEN);
        JUnitUtil.waitFor(() -> {
            return s.getState() == SignalHead.GREEN;
        }, "SignalHead is now GREEN");
        message = connection.getMessage();
        Assert.assertNotNull(message);
        Assert.assertEquals("Deliberately thrown error", 499, message.path(JSON.DATA).path(JsonException.CODE).asInt());
        Assert.assertEquals("Two listeners", 2, s.getNumPropertyChangeListeners());

        // trap IOException error
        connection.setThrowIOException(true);
        s.setAppearance(SignalHead.RED);
        JUnitUtil.waitFor(() -> {
            return s.getState() == SignalHead.RED;
        }, "SignalHead is now RED");
        Assert.assertEquals("One listener", 1, s.getNumPropertyChangeListeners());
    }

    @Test
    public void testOnList() throws IOException, JmriException, JsonException {
        //create a signal head for testing
        String sysName = "IH1";
        String userName = "SH1";
        SignalHead s1 = new VirtualSignalHead(sysName, userName);
        SignalHeadManager manager = InstanceManager.getDefault(SignalHeadManager.class);
        manager.register(s1);
        Assert.assertNotNull(s1);
        Assert.assertEquals("No listeners", 0, manager.getPropertyChangeListeners().length);

        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        TestJsonSignalHeadHttpService http = new TestJsonSignalHeadHttpService(connection.getObjectMapper());
        JsonSignalHeadSocketService service = new JsonSignalHeadSocketService(connection, http);
        service.onList(JsonSignalHead.SIGNAL_HEAD, connection.getObjectMapper().createObjectNode(), locale, 0);
        Assert.assertEquals("One listener", 1, manager.getPropertyChangeListeners().length);
        JsonNode message = connection.getMessage();
        Assert.assertNotNull(message);
        Assert.assertTrue(message.isArray());

        SignalHead s2 = new VirtualSignalHead("IH2", "SH2");
        manager.register(s2);
        message = connection.getMessage();
        Assert.assertNotNull(message);
        Assert.assertTrue(message.isArray());
        Assert.assertEquals("Two SignalHeads", 2, message.size());

        manager.deregister(s1);
        message = connection.getMessage();
        Assert.assertNotNull(message);
        Assert.assertTrue(message.isArray());
        Assert.assertEquals("One SignalHead", 1, message.size());

        // trap JsonException error
        http.setThrowException(2);
        manager.register(s1); // triggers two change reports
        message = connection.getMessage();
        Assert.assertNotNull(message);
        Assert.assertEquals("Deliberately thrown error", 499, message.path(JSON.DATA).path(JsonException.CODE).asInt());
        Assert.assertEquals("One listener", 1, manager.getPropertyChangeListeners().length);
        JUnitAppender.assertWarnMessage(
                "json error sending SignalHeads: {\"type\":\"error\",\"data\":{\"code\":499,\"message\":\"Mock Exception\"}}");
        JUnitAppender.assertWarnMessage(
                "json error sending SignalHeads: {\"type\":\"error\",\"data\":{\"code\":499,\"message\":\"Mock Exception\"}}");
        // trap IOException error
        connection.setThrowIOException(true);
        manager.deregister(s1); // an untrapped error will be thrown by the test
    }

    @Test
    public void testOnMessageChange() throws IOException, JmriException, JsonException {
        JsonNode message;
        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        JsonSignalHeadSocketService service = new JsonSignalHeadSocketService(connection);

        //create a signalhead for testing
        String sysName = "IH1";
        String userName = "SH1";
        SignalHead s = new VirtualSignalHead(sysName, userName);
        SignalHeadManager manager = InstanceManager.getDefault(SignalHeadManager.class);
        manager.register(s);
        Assert.assertNotNull(s);
        Assert.assertEquals("No listeners", 0, manager.getPropertyChangeListeners().length);

        // SignalHead Yellow
        message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, userName)
                .put(JSON.STATE, SignalHead.YELLOW);
        service.onMessage(JsonSignalHead.SIGNAL_HEAD, message, JSON.POST, locale, 42);
        Assert.assertEquals(SignalHead.YELLOW, s.getState()); //state should be Yellow
        Assert.assertEquals("No listeners", 0, manager.getPropertyChangeListeners().length);
        Assert.assertEquals("Two listeners", 2, s.getNumPropertyChangeListeners());

        // Try to set SignalHead to FLASHLUNAR, should throw error, remain at Yellow
        try {
            message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, userName)
                    .put(JSON.STATE, SignalHead.FLASHLUNAR);
            service.onMessage(JsonSignalHead.SIGNAL_HEAD, message, JSON.POST, locale, 42);
            Assert.fail("Expected exception not thrown");
        } catch (JsonException ex) {
            Assert.assertEquals("Error code is HTTP Method not allowed", 400, ex.getCode());
            Assert.assertEquals("Error message", "Attempting to set object type signalHead to unknown state 128.",
                    ex.getMessage());
        }
        Assert.assertEquals(SignalHead.YELLOW, s.getAppearance()); //state should be Yellow
        Assert.assertEquals("No listeners", 0, manager.getPropertyChangeListeners().length);
        Assert.assertEquals("Two listeners", 2, s.getNumPropertyChangeListeners());

        service.onClose();
        Assert.assertEquals("One listener", 1, s.getNumPropertyChangeListeners());
        
    }

    // The minimal setup for log4J
    @Before
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initInternalSignalHeadManager();
    }

    @After
    public void tearDown() throws Exception {
        JUnitUtil.tearDown();
    }

    private static class TestJsonSignalHeadHttpService extends JsonSignalHeadHttpService {

        private int throwException = 0;

        public TestJsonSignalHeadHttpService(ObjectMapper mapper) {
            super(mapper);
        }

        @Override
        public JsonNode doGet(String type, String name, JsonNode data, Locale locale, int id) throws JsonException {
            if (throwException > 0) {
                throwException--;
                throw new JsonException(499, "Mock Exception", id);
            }
            return super.doGet(type, name, data, locale, id);
        }

        public void setThrowException(int throwException) {
            this.throwException = throwException;
        }

    }
}
