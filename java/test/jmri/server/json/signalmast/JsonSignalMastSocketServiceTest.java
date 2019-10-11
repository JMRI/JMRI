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
public class JsonSignalMastSocketServiceTest {

    private Locale locale = Locale.ENGLISH;

    @Test
    public void testSignalMastChange() {
        try {
            //create a signalmast for testing
            String sysName = "IF$shsm:basic:one-searchlight(IH2)";
            String userName = "SM2";
            InstanceManager.getDefault(SignalHeadManager.class).register(new VirtualSignalHead("IH2"));
            SignalMast s = InstanceManager.getDefault(SignalMastManager.class).provideSignalMast(sysName);
            s.setUserName(userName);

            JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
            JsonNode message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, sysName);
            JsonSignalMastSocketService service = new JsonSignalMastSocketService(connection);

            service.onMessage(JsonSignalMast.SIGNAL_MAST, message, JSON.POST, locale, 42);
            // TODO: test that service is listener in SignalMastManager
            message = connection.getMessage();
            Assert.assertNotNull("Message is not null", message);
            Assert.assertEquals(JSON.ASPECT_UNKNOWN, message.path(JSON.DATA).path(JSON.STATE).asText());

            //change to Approach, and wait for change to show up
            s.setAspect("Approach");
            JUnitUtil.waitFor(() -> {
                return s.getAspect().equals("Approach");
            }, "SignalMast is now Approach");
            message = connection.getMessage();
            Assert.assertNotNull("Message is not null", message);
            Assert.assertEquals("Approach", message.path(JSON.DATA).path(JSON.STATE).asText());

            //change to Stop, and wait for change to show up
            s.setAspect("Stop");
            JUnitUtil.waitFor(() -> {
                return s.getAspect().equals("Stop");
            }, "SignalMast is now Stop");
            message = connection.getMessage();
            Assert.assertNotNull("Message is not null", message);
            Assert.assertEquals("Stop", message.path(JSON.DATA).path(JSON.STATE).asText());

            service.onClose();
//            // TODO: test that service is no longer a listener in SignalMastManager
        } catch (IOException | JmriException | JsonException ex) {
            Assert.fail(ex.getMessage());
        }
    }

    @Test
    public void testOnMessageChange() {
        JsonNode message;
        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        JsonSignalMastSocketService service = new JsonSignalMastSocketService(connection);
        //create a signalmast for testing
        String sysName = "IF$shsm:basic:one-searchlight(IH2)";
        String userName = "SM2";
        InstanceManager.getDefault(SignalHeadManager.class).register(new VirtualSignalHead("IH2"));
        SignalMast s = InstanceManager.getDefault(SignalMastManager.class).provideSignalMast(sysName);
        s.setUserName(userName);

        try {
            // SignalMast Stop
            message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, sysName).put(JSON.STATE, "Stop");
            service.onMessage(JsonSignalMast.SIGNAL_MAST, message, JSON.POST, locale, 42);
            Assert.assertEquals("Stop", s.getAspect()); //aspect should be Stop
        } catch (IOException | JmriException | JsonException ex) {
            Assert.fail(ex.getMessage());
        }

        // Try to set SignalMast to Unknown, should throw error, remain at Stop
        Exception exception = null;
        try {
            message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, sysName).put(JSON.STATE, JSON.ASPECT_UNKNOWN);
            service.onMessage(JsonSignalMast.SIGNAL_MAST, message, JSON.POST, locale, 42);
            Assert.assertEquals("Stop", s.getAspect());
        } catch (IOException | JmriException | JsonException ex) {
            exception = ex;
        }
        Assert.assertNotNull(exception);

        // set SignalMast no value, should throw error, remain at Stop
        message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, sysName);
        exception = null;
        try {
            service.onMessage(JsonSignalMast.SIGNAL_MAST, message, JSON.POST, locale, 42);
        } catch (JsonException ex) {
            exception = ex;
        } catch (IOException | JmriException ex) {
            Assert.fail(ex.getMessage());
        }
        Assert.assertNull(exception);
        Assert.assertEquals("Stop", s.getAspect());
    }

    // from here down is testing infrastructure
    // The minimal setup for log4J
    @Before
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initDefaultSignalMastManager();
        JUnitUtil.initInternalSignalHeadManager();
        JUnitUtil.initSignalMastLogicManager();
    }

    @After
    public void tearDown() throws Exception {
        JUnitUtil.tearDown();
    }

}
