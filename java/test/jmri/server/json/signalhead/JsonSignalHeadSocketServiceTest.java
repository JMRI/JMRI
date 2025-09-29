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
import jmri.server.json.JsonRequest;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 * @author Paul Bender
 * @author Randall Wood
 * @author Steve Todd
 */
public class JsonSignalHeadSocketServiceTest {

    private final Locale locale = Locale.ENGLISH;

    @Test
    public void testSignalHeadChange() throws IOException, JmriException, JsonException {
        //create a signal head for testing
        String sysName = "IH1";
        String userName = "SH1";
        SignalHead s = new VirtualSignalHead(sysName, userName);
        SignalHeadManager manager = InstanceManager.getDefault(SignalHeadManager.class);
        manager.register(s);
        assertNotNull(s);
        assertEquals( 1, s.getNumPropertyChangeListeners(), "One listener");

        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        connection.setVersion(JSON.V5);
        TestJsonSignalHeadHttpService http = new TestJsonSignalHeadHttpService(connection.getObjectMapper());
        JsonNode message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, sysName);
        JsonSignalHeadSocketService service = new JsonSignalHeadSocketService(connection, http);
        service.onMessage(JsonSignalHead.SIGNAL_HEAD, message, new JsonRequest(locale, JSON.V5, JSON.POST, 42));
        assertEquals( 2, s.getNumPropertyChangeListeners(), "Two listeners");

        //signalhead defaults to Dark
        message = connection.getMessage();
        assertNotNull(message);
        assertEquals(SignalHead.DARK, message.path(JSON.DATA).path(JSON.STATE).asInt());

        //change to Green, and wait for change to show up, then verify
        s.setAppearance(SignalHead.GREEN);
        JUnitUtil.waitFor(() -> {
            return s.getState() == SignalHead.GREEN;
        }, "SignalHead is now GREEN");
        message = connection.getMessage();
        assertNotNull(message);
        assertEquals(SignalHead.GREEN, message.path(JSON.DATA).path(JSON.STATE).asInt());

        //change to Red, and wait for change to show up, then verify
        s.setAppearance(SignalHead.RED);
        JUnitUtil.waitFor(() -> {
            return s.getState() == SignalHead.RED;
        }, "SignalHead is now RED");
        message = connection.getMessage();
        assertNotNull(message);
        assertEquals(SignalHead.RED, message.path(JSON.DATA).path(JSON.STATE).asInt());

        // put a new signal head
        JsonException ex = assertThrows( JsonException.class, () -> {
            JsonNode messageEx = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "something"); // does not matter
            service.onMessage(JsonSignalHead.SIGNAL_HEAD, messageEx, new JsonRequest(locale, JSON.V5, JSON.PUT, 42));
        });
        assertEquals( 405, ex.getCode(), "Error code is HTTP Invalid Request");
        assertEquals( "Putting signalHead is not allowed.", ex.getMessage(), "Error message");

        // trap JsonException error
        http.setThrowException(1);
        s.setAppearance(SignalHead.GREEN);
        JUnitUtil.waitFor(() -> {
            return s.getState() == SignalHead.GREEN;
        }, "SignalHead is now GREEN");
        message = connection.getMessage();
        assertNotNull(message);
        assertEquals( 499, message.path(JSON.DATA).path(JsonException.CODE).asInt(), "Deliberately thrown error");
        assertEquals( 2, s.getNumPropertyChangeListeners(), "Two listeners");

        // trap IOException error
        connection.setThrowIOException(true);
        s.setAppearance(SignalHead.RED);
        JUnitUtil.waitFor(() -> {
            return s.getState() == SignalHead.RED;
        }, "SignalHead is now RED");
        assertEquals( 1, s.getNumPropertyChangeListeners(), "One listener");
    }

    @Test
    public void testOnList() throws IOException, JmriException, JsonException {
        //create a signal head for testing
        String sysName = "IH1";
        String userName = "SH1";
        SignalHead s1 = new VirtualSignalHead(sysName, userName);
        SignalHeadManager manager = InstanceManager.getDefault(SignalHeadManager.class);
        manager.register(s1);
        assertNotNull(s1);
        assertEquals( 0, manager.getPropertyChangeListeners().length, "No listeners");

        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        connection.setVersion(JSON.V5);
        TestJsonSignalHeadHttpService http = new TestJsonSignalHeadHttpService(connection.getObjectMapper());
        JsonSignalHeadSocketService service = new JsonSignalHeadSocketService(connection, http);
        service.onList(JsonSignalHead.SIGNAL_HEAD, connection.getObjectMapper().createObjectNode(), new JsonRequest(locale, JSON.V5, JSON.GET, 0));
        assertEquals( 1, manager.getPropertyChangeListeners().length, "One listener");
        JsonNode message = connection.getMessage();
        assertNotNull(message);
        assertTrue(message.isArray());

        SignalHead s2 = new VirtualSignalHead("IH2", "SH2");
        manager.register(s2);
        message = connection.getMessage();
        assertNotNull(message);
        assertTrue(message.isArray());
        assertEquals( 2, message.size(), "Two SignalHeads");

        manager.deregister(s1);
        message = connection.getMessage();
        assertNotNull(message);
        assertTrue(message.isArray());
        assertEquals( 1, message.size(), "One SignalHead");

        // trap JsonException error
        http.setThrowException(2);
        manager.register(s1); // triggers two change reports
        message = connection.getMessage();
        assertNotNull(message);
        assertEquals( 499, message.path(JSON.DATA).path(JsonException.CODE).asInt(), "Deliberately thrown error");
        assertEquals( 1, manager.getPropertyChangeListeners().length, "One listener");
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
        assertNotNull(s);
        assertEquals( 0, manager.getPropertyChangeListeners().length, "No listeners");

        // SignalHead Yellow
        message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, userName)
                .put(JSON.STATE, SignalHead.YELLOW);
        service.onMessage(JsonSignalHead.SIGNAL_HEAD, message, new JsonRequest(locale, JSON.V5, JSON.POST, 42));
        assertEquals(SignalHead.YELLOW, s.getState(), "state should be Yellow");
        assertEquals( 0, manager.getPropertyChangeListeners().length, "No listeners");
        assertEquals( 2, s.getNumPropertyChangeListeners(), "Two listeners");

        // Try to set SignalHead to FLASHLUNAR, should throw error, remain at Yellow
        JsonException ex = assertThrows( JsonException.class, () -> {
            JsonNode messageEx = connection.getObjectMapper().createObjectNode().put(JSON.NAME, userName)
                    .put(JSON.STATE, SignalHead.FLASHLUNAR);
            service.onMessage(JsonSignalHead.SIGNAL_HEAD, messageEx, new JsonRequest(locale, JSON.V5, JSON.POST, 42));
        });
        assertEquals( 400, ex.getCode(), "Error code is HTTP Method not allowed");
        assertEquals( "Attempting to set object type signalHead to unknown state 128.",
            ex.getMessage(), "Error message");
        assertEquals(SignalHead.YELLOW, s.getAppearance()); //state should be Yellow
        assertEquals( 0, manager.getPropertyChangeListeners().length, "No listeners");
        assertEquals( 2, s.getNumPropertyChangeListeners(), "Two listeners");

        service.onClose();
        assertEquals( 1, s.getNumPropertyChangeListeners(), "One listener");
        
    }

    @BeforeEach
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initInternalSignalHeadManager();
    }

    @AfterEach
    public void tearDown() throws Exception {
        JUnitUtil.tearDown();
    }

    private static class TestJsonSignalHeadHttpService extends JsonSignalHeadHttpService {

        private int throwException = 0;

        TestJsonSignalHeadHttpService(ObjectMapper mapper) {
            super(mapper);
        }

        @Override
        public JsonNode doGet(String type, String name, JsonNode data, JsonRequest request) throws JsonException {
            if (throwException > 0) {
                throwException--;
                throw new JsonException(499, "Mock Exception", request.id);
            }
            return super.doGet(type, name, data, request);
        }

        public void setThrowException(int throwException) {
            this.throwException = throwException;
        }

    }
}
