package jmri.server.json.signalHead;

import apps.tests.Log4JFixture;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Locale;
import jmri.JmriException;
import jmri.SignalHead;
import jmri.server.json.JSON;
import jmri.server.json.JsonException;
import jmri.util.JUnitUtil;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 *
 * @author Paul Bender
 * @author Randall Wood
 * @author Steve Todd
 */
public class JsonSignalHeadHttpServiceTest extends TestCase {

    public void testCtorSuccess() {
        JsonSignalHeadHttpService service = new JsonSignalHeadHttpService(new ObjectMapper());
        Assert.assertNotNull(service);
    }

    public void testDoGet() throws JmriException {
        
        //create a signalhead for testing
        String sysName = "IH1";
        String userName = "SH1";        
        SignalHead s = new jmri.implementation.VirtualSignalHead(sysName, userName);
        jmri.InstanceManager.getDefault(jmri.SignalHeadManager.class).register(s);
        Assert.assertNotNull(s);

        JsonNode result;
        JsonSignalHeadHttpService service = new JsonSignalHeadHttpService(new ObjectMapper());
        try {
            //retrieve by systemname
            result = service.doGet(JsonSignalHead.SIGNAL_HEAD, sysName, Locale.ENGLISH);
            Assert.assertNotNull(result); 
            Assert.assertEquals(JsonSignalHead.SIGNAL_HEAD, result.path(JSON.TYPE).asText());
            Assert.assertEquals(sysName, result.path(JSON.DATA).path(JSON.NAME).asText());

            //retrieve by username, should get systemname back
            result = service.doGet(JsonSignalHead.SIGNAL_HEAD, userName, Locale.ENGLISH);
            Assert.assertNotNull(result); 
            Assert.assertEquals(sysName, result.path(JSON.DATA).path(JSON.NAME).asText());
            
            //verify initial aspect/state is Dark
            Assert.assertEquals(SignalHead.DARK, result.path(JSON.DATA).path(JSON.STATE).asInt());
            //change to Green, then verify change
            s.setAppearance(SignalHead.GREEN);
            result = service.doGet(JsonSignalHead.SIGNAL_HEAD, userName, Locale.ENGLISH);
            Assert.assertEquals(SignalHead.GREEN, result.path(JSON.DATA).path(JSON.STATE).asInt());
            //set Held, then verify change
            s.setHeld(true);
            result = service.doGet(JsonSignalHead.SIGNAL_HEAD, userName, Locale.ENGLISH);
            Assert.assertEquals(true, result.path(JSON.DATA).path(JSON.TOKEN_HELD).asBoolean());
            //set to Not Held, then verify change
            s.setHeld(false);
            result = service.doGet(JsonSignalHead.SIGNAL_HEAD, userName, Locale.ENGLISH);
            Assert.assertEquals(false, result.path(JSON.DATA).path(JSON.TOKEN_HELD).asBoolean());
        } catch (JsonException ex) {
            Assert.fail(ex.getMessage());
        }
    }

    public void testDoPost() throws JmriException {
        //create a signalhead for testing
        String sysName = "IH1";
        String userName = "SH1";        
        SignalHead s = new jmri.implementation.VirtualSignalHead(sysName, userName);
        jmri.InstanceManager.getDefault(jmri.SignalHeadManager.class).register(s);
        Assert.assertNotNull(s);

        JsonNode result = null;
        JsonNode message = null;
        ObjectMapper mapper = new ObjectMapper();
        JsonSignalHeadHttpService service = new JsonSignalHeadHttpService(new ObjectMapper());
        Assert.assertNotNull(service);
       
        try {
            //set signalhead to Green and verify change
            message = mapper.createObjectNode().put(JSON.NAME, userName).put(JSON.STATE, SignalHead.GREEN);
            result = service.doPost(JsonSignalHead.SIGNAL_HEAD, userName, message, Locale.ENGLISH);
            Assert.assertNotNull(result);
            Assert.assertEquals(SignalHead.GREEN, s.getState());
            Assert.assertEquals(SignalHead.GREEN, result.path(JSON.DATA).path(JSON.STATE).asInt());
        } catch (JsonException ex) {
            Assert.fail(ex.getMessage());
        }

        // try to set to FLASHLUNAR, which should not be allowed for this signalHead, 
        //  so check for error, and verify state does not change
        JsonException exception = null;
        result = null;
        message = null;
        try {
            message = mapper.createObjectNode().put(JSON.NAME, userName).put(JSON.STATE, SignalHead.FLASHLUNAR);
            result = service.doPost(JsonSignalHead.SIGNAL_HEAD, userName, message, Locale.ENGLISH);
        } catch (JsonException ex) {
            exception = ex;
        }
        Assert.assertNotNull(exception);
        Assert.assertEquals(SignalHead.GREEN, s.getState());

        Assert.assertEquals(false, s.getHeld());
        // set signalmast to Held, then verify 
        try {
            message = mapper.createObjectNode().put(JSON.NAME, userName).put(JSON.STATE, SignalHead.HELD);
            result = service.doPost(JsonSignalHead.SIGNAL_HEAD, userName, message, Locale.ENGLISH);
            Assert.assertEquals(true, s.getHeld());
        } catch (JsonException ex) {
            Assert.fail(ex.getMessage());
        }

        Assert.assertEquals(true, s.getHeld());
        // set signalmast to something other than Held, then verify Held is released 
        try {
            message = mapper.createObjectNode().put(JSON.NAME, userName).put(JSON.STATE, SignalHead.RED);
            result = service.doPost(JsonSignalHead.SIGNAL_HEAD, userName, message, Locale.ENGLISH);
            Assert.assertEquals(false, s.getHeld());
        } catch (JsonException ex) {
            Assert.fail(ex.getMessage());
        }
    }
   
    public void testDoGetList() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonSignalHeadHttpService service = new JsonSignalHeadHttpService(mapper);
            JsonNode result;
            result = service.doGetList(JsonSignalHead.SIGNAL_HEAD, Locale.ENGLISH);
            Assert.assertNotNull(result);
            Assert.assertEquals(0, result.size());
            jmri.InstanceManager.getDefault(jmri.SignalHeadManager.class).register(new jmri.implementation.VirtualSignalHead("IH1","Head 1"));
            jmri.InstanceManager.getDefault(jmri.SignalHeadManager.class).register(new jmri.implementation.VirtualSignalHead("IH2","Head 2"));
            result = service.doGetList(JsonSignalHead.SIGNAL_HEAD, Locale.ENGLISH);
            Assert.assertNotNull(result);
            Assert.assertEquals(2, result.size());
        } catch (JsonException ex) {
            Assert.fail(ex.getMessage());
        }
    }
    
    // from here down is testing infrastructure
    public JsonSignalHeadHttpServiceTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {JsonSignalHeadHttpServiceTest.class.getName()};
        TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(JsonSignalHeadHttpServiceTest.class);

        return suite;
    }

    // The minimal setup for log4J
    @Override
    protected void setUp() throws Exception {
        Log4JFixture.setUp();
        super.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalSignalHeadManager();
        JUnitUtil.initDebugThrottleManager();
    }

    @Override
    protected void tearDown() throws Exception {
        JUnitUtil.resetInstanceManager();
        super.tearDown();
        Log4JFixture.tearDown();
    }

}
