package jmri.server.json.signalmast;

import com.fasterxml.jackson.databind.JsonNode;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Locale;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.SignalHeadManager;
import jmri.SignalMast;
import jmri.SignalMastManager;
import jmri.implementation.VirtualSignalHead;
import jmri.server.json.JSON;
import jmri.server.json.JsonException;
import jmri.server.json.JsonMockConnection;
import jmri.server.json.JsonRequest;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 *
 * @author Paul Bender
 * @author Randall Wood
 * @author Steve Todd
 */
public class JsonSignalMastSocketServiceTest {

    private final Locale locale = Locale.ENGLISH;

    @Test
    public void testSignalMastChange() throws IOException, JmriException, JsonException {

        //create a signalmast for testing
        String sysName = "IF$shsm:basic:one-searchlight(IH2)";
        String userName = "SM2";
        InstanceManager.getDefault(SignalHeadManager.class).register(new VirtualSignalHead("IH2"));
        SignalMast s = InstanceManager.getDefault(SignalMastManager.class).provideSignalMast(sysName);
        s.setUserName(userName);

        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        JsonNode message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, sysName);
        JsonSignalMastSocketService service = new JsonSignalMastSocketService(connection);

        service.onMessage(JsonSignalMast.SIGNAL_MAST, message, new JsonRequest(locale, JSON.V5, JSON.POST, 42));
        // TODO: test that service is listener in SignalMastManager
        message = connection.getMessage();
        assertNotNull( message, "Message is not null");
        assertEquals(JSON.ASPECT_UNKNOWN, message.path(JSON.DATA).path(JSON.STATE).asText());

        //change to Approach, and wait for change to show up
        s.setAspect("Approach");
        JUnitUtil.waitFor(() -> {
            var aspect = s.getAspect();
            return ( aspect != null && "Approach".equals(aspect));
        }, "SignalMast is now Approach");
        message = connection.getMessage();
        assertNotNull( message, "Message is not null");
        assertEquals("Approach", message.path(JSON.DATA).path(JSON.STATE).asText());

        //change to Stop, and wait for change to show up
        s.setAspect("Stop");
        JUnitUtil.waitFor(() -> {
            var aspect = s.getAspect();
            return ( aspect != null && "Stop".equals(aspect));
        }, "SignalMast is now Stop");
        message = connection.getMessage();
        assertNotNull( message, "Message is not null");
        assertEquals("Stop", message.path(JSON.DATA).path(JSON.STATE).asText());

        service.onClose();
        // TODO: test that service is no longer a listener in SignalMastManager

    }

    @Test
    public void testOnMessageChange() {

        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        JsonSignalMastSocketService service = new JsonSignalMastSocketService(connection);
        //create a signalmast for testing
        String sysName = "IF$shsm:basic:one-searchlight(IH2)";
        String userName = "SM2";
        InstanceManager.getDefault(SignalHeadManager.class).register(new VirtualSignalHead("IH2"));
        SignalMast s = InstanceManager.getDefault(SignalMastManager.class).provideSignalMast(sysName);
        s.setUserName(userName);

        assertDoesNotThrow( () -> {
            // SignalMast Stop
            JsonNode message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, sysName).put(JSON.STATE, "Stop");
            service.onMessage(JsonSignalMast.SIGNAL_MAST, message, new JsonRequest(locale, JSON.V5, JSON.POST, 42));
        });
        assertEquals("Stop", s.getAspect(), "aspect should be Stop");

        JsonException exception = assertThrows( JsonException.class, () -> {
            JsonNode message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, sysName).put(JSON.STATE, JSON.ASPECT_UNKNOWN);
            service.onMessage(JsonSignalMast.SIGNAL_MAST, message, new JsonRequest(locale, JSON.V5, JSON.POST, 42));
        },"set SignalMast to Unknown, should throw error, remain at Stop");
        assertNotNull(exception);

        assertDoesNotThrow( () -> {
            JsonNode message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, sysName);
            service.onMessage(JsonSignalMast.SIGNAL_MAST, message,
                new JsonRequest(locale, JSON.V5, JSON.POST, 42));
            }, "set SignalMast no value, should remain at Stop");
        assertEquals("Stop", s.getAspect());
    }

    // from here down is testing infrastructure
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initDefaultSignalMastManager();
        JUnitUtil.initInternalSignalHeadManager();
        JUnitUtil.initSignalMastLogicManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
