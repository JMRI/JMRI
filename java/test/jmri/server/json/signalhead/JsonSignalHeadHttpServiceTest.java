package jmri.server.json.signalhead;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import jmri.JmriException;
import jmri.SignalHead;
import jmri.server.json.JSON;
import jmri.server.json.JsonException;
import jmri.server.json.JsonHttpServiceTestBase;
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
public class JsonSignalHeadHttpServiceTest extends JsonHttpServiceTestBase<JsonSignalHeadHttpService> {

    @Test
    public void testDoGet() throws JmriException, JsonException {

        //create a signalhead for testing
        String sysName = "IH1";
        String userName = "SH1";
        SignalHead s = new jmri.implementation.VirtualSignalHead(sysName, userName);
        jmri.InstanceManager.getDefault(jmri.SignalHeadManager.class).register(s);
        assertNotNull(s);

        JsonNode result;
        //retrieve by systemname
        result = service.doGet(JsonSignalHead.SIGNAL_HEAD, sysName, NullNode.getInstance(), locale, 42);
        validate(result);
        assertEquals(JsonSignalHead.SIGNAL_HEAD, result.path(JSON.TYPE).asText());
        assertEquals(sysName, result.path(JSON.DATA).path(JSON.NAME).asText());

        //retrieve by username, should get systemname back
        result = service.doGet(JsonSignalHead.SIGNAL_HEAD, userName, NullNode.getInstance(), locale, 42);
        validate(result);
        assertEquals(sysName, result.path(JSON.DATA).path(JSON.NAME).asText());

        //verify initial aspect/state is Dark
        assertEquals(SignalHead.DARK, result.path(JSON.DATA).path(JSON.STATE).asInt());
        //change to Green, then verify change
        s.setAppearance(SignalHead.GREEN);
        result = service.doGet(JsonSignalHead.SIGNAL_HEAD, userName, NullNode.getInstance(), locale, 42);
        assertEquals(SignalHead.GREEN, result.path(JSON.DATA).path(JSON.STATE).asInt());
        //set Held, then verify change
        s.setHeld(true);
        result = service.doGet(JsonSignalHead.SIGNAL_HEAD, userName, NullNode.getInstance(), locale, 42);
        assertTrue(result.path(JSON.DATA).path(JSON.TOKEN_HELD).asBoolean());
        //set to Not Held, then verify change
        s.setHeld(false);
        result = service.doGet(JsonSignalHead.SIGNAL_HEAD, userName, NullNode.getInstance(), locale, 42);
        assertFalse(result.path(JSON.DATA).path(JSON.TOKEN_HELD).asBoolean());
    }

    @Test
    public void testDoPost() throws JmriException, JsonException {
        //create a signalhead for testing
        String sysName = "IH1";
        String userName = "SH1";
        SignalHead s = new jmri.implementation.VirtualSignalHead(sysName, userName);
        jmri.InstanceManager.getDefault(jmri.SignalHeadManager.class).register(s);
        assertNotNull(s);

        JsonNode result = null;
        JsonNode message = null;

        //set signalhead to Green and verify change
        message = mapper.createObjectNode().put(JSON.NAME, userName).put(JSON.STATE, SignalHead.GREEN);
        result = service.doPost(JsonSignalHead.SIGNAL_HEAD, userName, message, locale, 42);
        validate(result);
        assertEquals(SignalHead.GREEN, s.getState());
        assertEquals(SignalHead.GREEN, result.path(JSON.DATA).path(JSON.STATE).asInt());

        // try to set to FLASHLUNAR, which should not be allowed for this signalHead,
        //  so check for error, and verify state does not change
        result = null;
        message = null;
        try {
            message = mapper.createObjectNode().put(JSON.NAME, userName).put(JSON.STATE, SignalHead.FLASHLUNAR);
            result = service.doPost(JsonSignalHead.SIGNAL_HEAD, userName, message, locale, 42);
            fail("Expected exception not thrown");
        } catch (JsonException ex) {
            assertEquals(400, ex.getCode());
        }
        assertEquals(SignalHead.GREEN, s.getState());

        assertEquals(false, s.getHeld());
        // set signalmast to Held, then verify
        message = mapper.createObjectNode().put(JSON.NAME, userName).put(JSON.STATE, SignalHead.HELD);
        result = service.doPost(JsonSignalHead.SIGNAL_HEAD, userName, message, locale, 42);
        assertEquals(true, s.getHeld());

        assertEquals(true, s.getHeld());
        // set signalmast to something other than Held, then verify Held is released
        message = mapper.createObjectNode().put(JSON.NAME, userName).put(JSON.STATE, SignalHead.RED);
        result = service.doPost(JsonSignalHead.SIGNAL_HEAD, userName, message, locale, 42);
        assertEquals(false, s.getHeld());
    }

    @Test
    public void testDoGetList() throws JsonException {
        JsonNode result;
        result = service.doGetList(JsonSignalHead.SIGNAL_HEAD, NullNode.getInstance(), locale, 0);
        validate(result);
        assertEquals(0, result.size());
        jmri.InstanceManager.getDefault(jmri.SignalHeadManager.class).register(new jmri.implementation.VirtualSignalHead("IH1","Head 1"));
        jmri.InstanceManager.getDefault(jmri.SignalHeadManager.class).register(new jmri.implementation.VirtualSignalHead("IH2","Head 2"));
        result = service.doGetList(JsonSignalHead.SIGNAL_HEAD, NullNode.getInstance(), locale, 0);
        validate(result);
        assertEquals(2, result.size());
    }

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        service = new JsonSignalHeadHttpService(mapper);
        JUnitUtil.initInternalSignalHeadManager();
        JUnitUtil.initDebugThrottleManager();
    }

    @After
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }
}
