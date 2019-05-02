package jmri.server.json.signalMast;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Locale;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.SignalHeadManager;
import jmri.SignalMast;
import jmri.SignalMastManager;
import jmri.implementation.VirtualSignalHead;
import jmri.server.json.JSON;
import jmri.server.json.JsonException;
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
public class JsonSignalMastHttpServiceTest {

    @Test
    public void testDoGet() throws JmriException, JsonException {

        // create a signalmast for testing
        String sysName = "IF$shsm:basic:one-searchlight:IH2";
        String userName = "SM2";
        InstanceManager.getDefault(SignalHeadManager.class).register(new VirtualSignalHead("IH2"));
        SignalMast s = InstanceManager.getDefault(SignalMastManager.class).provideSignalMast(sysName);
        s.setUserName(userName);

        JsonNode result;
        JsonSignalMastHttpService service = new JsonSignalMastHttpService(new ObjectMapper());
        // retrieve by systemname
        result = service.doGet(JsonSignalMast.SIGNAL_MAST, sysName, service.getObjectMapper().createObjectNode(), Locale.ENGLISH);
        Assert.assertNotNull(result);
        Assert.assertEquals(JsonSignalMast.SIGNAL_MAST, result.path(JSON.TYPE).asText());
        Assert.assertEquals(sysName, result.path(JSON.DATA).path(JSON.NAME).asText());
        // verify initial aspect/state is "Unknown"
        Assert.assertEquals(JSON.ASPECT_UNKNOWN, result.path(JSON.DATA).path(JSON.STATE).asText());
        // retrieve by username
        result = service.doGet(JsonSignalMast.SIGNAL_MAST, userName, service.getObjectMapper().createObjectNode(), Locale.ENGLISH);
        Assert.assertNotNull(result);
        Assert.assertEquals(JsonSignalMast.SIGNAL_MAST, result.path(JSON.TYPE).asText());
        Assert.assertEquals(sysName, result.path(JSON.DATA).path(JSON.NAME).asText());
        // change to Clear, then verify change
        s.setAspect("Clear");
        result = service.doGet(JsonSignalMast.SIGNAL_MAST, sysName, service.getObjectMapper().createObjectNode(), Locale.ENGLISH);
        Assert.assertEquals("Clear", result.path(JSON.DATA).path(JSON.STATE).asText());
        Assert.assertEquals("Clear", result.path(JSON.DATA).path(JSON.ASPECT).asText());
        // change to Held, then verify change
        s.setHeld(true);
        result = service.doGet(JsonSignalMast.SIGNAL_MAST, sysName, service.getObjectMapper().createObjectNode(), Locale.ENGLISH);
        Assert.assertEquals(JSON.ASPECT_HELD, result.path(JSON.DATA).path(JSON.STATE).asText());
        Assert.assertEquals("Clear", result.path(JSON.DATA).path(JSON.ASPECT).asText());
        // change to Dark, then verify change
        s.setHeld(false);
        s.setLit(false);
        result = service.doGet(JsonSignalMast.SIGNAL_MAST, sysName, service.getObjectMapper().createObjectNode(), Locale.ENGLISH);
        Assert.assertEquals(JSON.ASPECT_DARK, result.path(JSON.DATA).path(JSON.STATE).asText());
        Assert.assertEquals("Clear", result.path(JSON.DATA).path(JSON.ASPECT).asText());
    }

    @Test
    public void testDoPost() throws JmriException, JsonException {
        // create a signalmast for testing
        String sysName = "IF$shsm:basic:one-searchlight:IH2";
        String userName = "SM2";
        InstanceManager.getDefault(SignalHeadManager.class).register(new VirtualSignalHead("IH2"));
        SignalMast s = InstanceManager.getDefault(SignalMastManager.class).provideSignalMast(sysName);
        Assert.assertNotNull(s);
        s.setUserName(userName);
        JsonNode result;
        JsonNode message;
        ObjectMapper mapper = new ObjectMapper();
        JsonSignalMastHttpService service = new JsonSignalMastHttpService(new ObjectMapper());

        // set signalmast to Clear and verify change
        message = mapper.createObjectNode().put(JSON.NAME, sysName).put(JSON.STATE, "Clear");
        result = service.doPost(JsonSignalMast.SIGNAL_MAST, sysName, message, Locale.ENGLISH);
        Assert.assertNotNull(result);
        Assert.assertEquals("Clear", s.getAspect());
        Assert.assertEquals("Clear", result.path(JSON.DATA).path(JSON.STATE).asText());
        Assert.assertEquals("Clear", result.path(JSON.DATA).path(JSON.ASPECT).asText());

        // try to set to UNKNOWN, which should not be allowed, so state should not change
        try {
            message = mapper.createObjectNode().put(JSON.NAME, sysName).put(JSON.STATE, JSON.ASPECT_UNKNOWN);
            result = service.doPost(JsonSignalMast.SIGNAL_MAST, sysName, message, Locale.ENGLISH);
            Assert.fail("Expected exceiton not thrown");
        } catch (JsonException ex) {
            Assert.assertEquals("Error code is HTTP 400", 400, ex.getCode());
            Assert.assertEquals("Error message", "Attempting to set object type signalMast to unknown state Unknown.", ex.getMessage());
        }

        // set to HELD and verify change
        message = mapper.createObjectNode().put(JSON.NAME, sysName).put(JSON.STATE, JSON.ASPECT_HELD);
        result = service.doPost(JsonSignalMast.SIGNAL_MAST, sysName, message, Locale.ENGLISH);
        Assert.assertNotNull(result);
        Assert.assertTrue("Signalmast is held", s.getHeld());
        Assert.assertEquals("Clear", s.getAspect());
        Assert.assertEquals(JSON.ASPECT_HELD, result.path(JSON.DATA).path(JSON.STATE).asText());
        Assert.assertEquals("Clear", result.path(JSON.DATA).path(JSON.ASPECT).asText());

        // set to STOP and verify change
        message = mapper.createObjectNode().put(JSON.NAME, sysName).put(JSON.STATE, "Stop");
        result = service.doPost(JsonSignalMast.SIGNAL_MAST, sysName, message, Locale.ENGLISH);
        Assert.assertNotNull(result);
        Assert.assertFalse("Signalmast is not held", s.getHeld());
        Assert.assertEquals("Stop", s.getAspect());
        Assert.assertEquals("Stop", result.path(JSON.DATA).path(JSON.STATE).asText());
        Assert.assertEquals("Stop", result.path(JSON.DATA).path(JSON.ASPECT).asText());
    }

    @Test
    public void testDoGetList() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonSignalMastHttpService service = new JsonSignalMastHttpService(mapper);
            SignalHeadManager headManager = InstanceManager.getDefault(SignalHeadManager.class);
            SignalMastManager mastManager = InstanceManager.getDefault(SignalMastManager.class);
            JsonNode result;
            result = service.doGetList(JsonSignalMast.SIGNAL_MAST, mapper.createObjectNode(), Locale.ENGLISH);
            Assert.assertNotNull(result);
            Assert.assertEquals(0, result.size());
            headManager.register(new VirtualSignalHead("IH1"));
            mastManager.provideSignalMast("IF$shsm:basic:one-searchlight:IH1");
            headManager.register(new VirtualSignalHead("IH2"));
            mastManager.provideSignalMast("IF$shsm:basic:one-searchlight:IH2");
            result = service.doGetList(JsonSignalMast.SIGNAL_MAST, mapper.createObjectNode(), Locale.ENGLISH);
            Assert.assertNotNull(result);
            Assert.assertEquals(2, result.size());
        } catch (JsonException ex) {
            Assert.fail(ex.getMessage());
        }
    }

    // The minimal setup for log4J
    @Before
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        JUnitUtil.initInternalSignalHeadManager();
        JUnitUtil.initDefaultSignalMastManager();
        JUnitUtil.initSignalMastLogicManager();
    }

    @After
    public void tearDown() throws Exception {
        JUnitUtil.tearDown();
    }

}
