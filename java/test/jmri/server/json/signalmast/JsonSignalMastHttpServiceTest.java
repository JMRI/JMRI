package jmri.server.json.signalmast;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.SignalHeadManager;
import jmri.SignalMast;
import jmri.SignalMastManager;
import jmri.implementation.VirtualSignalHead;
import jmri.server.json.JSON;
import jmri.server.json.JsonException;
import jmri.server.json.JsonNamedBeanHttpServiceTestBase;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender
 * @author Randall Wood
 * @author Steve Todd
 */
public class JsonSignalMastHttpServiceTest extends JsonNamedBeanHttpServiceTestBase<SignalMast, JsonSignalMastHttpService>{

    @Test
    @Override
    public void testDoGet() throws JmriException, JsonException {

        // create a signalmast for testing
        String sysName = "IF$shsm:basic:one-searchlight:IH2";
        String userName = "SM2";
        InstanceManager.getDefault(SignalHeadManager.class).register(new VirtualSignalHead("IH2"));
        SignalMast s = InstanceManager.getDefault(SignalMastManager.class).provideSignalMast(sysName);
        s.setUserName(userName);

        JsonNode result;
        // retrieve by systemname
        result = service.doGet(JsonSignalMast.SIGNAL_MAST, sysName, NullNode.getInstance(), locale, 42);
        validate(result);
        assertEquals(JsonSignalMast.SIGNAL_MAST, result.path(JSON.TYPE).asText());
        assertEquals(sysName, result.path(JSON.DATA).path(JSON.NAME).asText());
        // verify initial aspect/state is "Unknown"
        assertEquals(JSON.ASPECT_UNKNOWN, result.path(JSON.DATA).path(JSON.STATE).asText());
        // retrieve by username
        result = service.doGet(JsonSignalMast.SIGNAL_MAST, userName, NullNode.getInstance(), locale, 42);
        validate(result);
        assertEquals(JsonSignalMast.SIGNAL_MAST, result.path(JSON.TYPE).asText());
        assertEquals(sysName, result.path(JSON.DATA).path(JSON.NAME).asText());
        // change to Clear, then verify change
        s.setAspect("Clear");
        result = service.doGet(JsonSignalMast.SIGNAL_MAST, sysName, NullNode.getInstance(), locale, 42);
        assertEquals("Clear", result.path(JSON.DATA).path(JSON.STATE).asText());
        assertEquals("Clear", result.path(JSON.DATA).path(JSON.ASPECT).asText());
        // change to Held, then verify change
        s.setHeld(true);
        result = service.doGet(JsonSignalMast.SIGNAL_MAST, sysName, NullNode.getInstance(), locale, 42);
        assertEquals(JSON.ASPECT_HELD, result.path(JSON.DATA).path(JSON.STATE).asText());
        assertEquals("Clear", result.path(JSON.DATA).path(JSON.ASPECT).asText());
        // change to Dark, then verify change
        s.setHeld(false);
        s.setLit(false);
        result = service.doGet(JsonSignalMast.SIGNAL_MAST, sysName, NullNode.getInstance(), locale, 42);
        assertEquals(JSON.ASPECT_DARK, result.path(JSON.DATA).path(JSON.STATE).asText());
        assertEquals("Clear", result.path(JSON.DATA).path(JSON.ASPECT).asText());
    }

    @Test
    public void testDoPost() throws JmriException, JsonException {
        // create a signalmast for testing
        String sysName = "IF$shsm:basic:one-searchlight:IH2";
        String userName = "SM2";
        InstanceManager.getDefault(SignalHeadManager.class).register(new VirtualSignalHead("IH2"));
        SignalMast s = InstanceManager.getDefault(SignalMastManager.class).provideSignalMast(sysName);
        assertNotNull(s);
        s.setUserName(userName);
        JsonNode result;
        JsonNode message;

        // set signalmast to Clear and verify change
        message = mapper.createObjectNode().put(JSON.NAME, sysName).put(JSON.STATE, "Clear");
        result = service.doPost(JsonSignalMast.SIGNAL_MAST, sysName, message, locale, 42);
        validate(result);
        assertEquals("Clear", s.getAspect());
        assertEquals("Clear", result.path(JSON.DATA).path(JSON.STATE).asText());
        assertEquals("Clear", result.path(JSON.DATA).path(JSON.ASPECT).asText());

        // try to set to UNKNOWN, which should not be allowed, so state should not change
        try {
            message = mapper.createObjectNode().put(JSON.NAME, sysName).put(JSON.STATE, JSON.ASPECT_UNKNOWN);
            result = service.doPost(JsonSignalMast.SIGNAL_MAST, sysName, message, locale, 42);
            fail("Expected exceiton not thrown");
        } catch (JsonException ex) {
            assertEquals("Error code is HTTP 400", 400, ex.getCode());
            assertEquals("Error message", "Attempting to set object type signalMast to unknown state Unknown.", ex.getMessage());
        }

        // set to HELD and verify change
        message = mapper.createObjectNode().put(JSON.NAME, sysName).put(JSON.STATE, JSON.ASPECT_HELD);
        result = service.doPost(JsonSignalMast.SIGNAL_MAST, sysName, message, locale, 42);
        validate(result);
        assertTrue("Signalmast is held", s.getHeld());
        assertEquals("Clear", s.getAspect());
        assertEquals(JSON.ASPECT_HELD, result.path(JSON.DATA).path(JSON.STATE).asText());
        assertEquals("Clear", result.path(JSON.DATA).path(JSON.ASPECT).asText());

        // set to STOP and verify change
        message = mapper.createObjectNode().put(JSON.NAME, sysName).put(JSON.STATE, "Stop");
        result = service.doPost(JsonSignalMast.SIGNAL_MAST, sysName, message, locale, 42);
        validate(result);
        assertFalse("Signalmast is not held", s.getHeld());
        assertEquals("Stop", s.getAspect());
        assertEquals("Stop", result.path(JSON.DATA).path(JSON.STATE).asText());
        assertEquals("Stop", result.path(JSON.DATA).path(JSON.ASPECT).asText());
    }

    @Test
    public void testDoGetList() throws JsonException {
        SignalHeadManager headManager = InstanceManager.getDefault(SignalHeadManager.class);
        SignalMastManager mastManager = InstanceManager.getDefault(SignalMastManager.class);
        JsonNode result;
        result = service.doGetList(JsonSignalMast.SIGNAL_MAST, mapper.createObjectNode(), locale, 0);
        validate(result);
        assertEquals(0, result.size());
        headManager.register(new VirtualSignalHead("IH1"));
        mastManager.provideSignalMast("IF$shsm:basic:one-searchlight:IH1");
        headManager.register(new VirtualSignalHead("IH2"));
        mastManager.provideSignalMast("IF$shsm:basic:one-searchlight:IH2");
        result = service.doGetList(JsonSignalMast.SIGNAL_MAST, mapper.createObjectNode(), locale, 0);
        validate(result);
        assertEquals(2, result.size());
    }

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        service = new JsonSignalMastHttpService(mapper);
        JUnitUtil.initInternalSignalHeadManager();
        JUnitUtil.initDefaultSignalMastManager();
        JUnitUtil.initSignalMastLogicManager();
    }

    @After
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

}
