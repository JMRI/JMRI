package jmri.server.json.signalMast;

import apps.tests.Log4JFixture;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Locale;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.SignalMast;
import jmri.SignalMastManager;
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
public class JsonSignalMastHttpServiceTest extends TestCase {

    public void testCtorSuccess() {
        JsonSignalMastHttpService service = new JsonSignalMastHttpService(new ObjectMapper());
        Assert.assertNotNull(service);
    }

    public void testDoGet() throws JmriException {
        
        //create a signalmast for testing
        String sysName = "IF$shsm:basic:one-searchlight:SM2";
        String userName = "SM2";        
        SignalMast s = InstanceManager.getDefault(jmri.SignalMastManager.class).provideSignalMast(sysName);
        s.setUserName(userName);

        JsonNode result;
        JsonSignalMastHttpService service = new JsonSignalMastHttpService(new ObjectMapper());
        try {
            //retrieve by systemname
            result = service.doGet(JsonSignalMast.SIGNAL_MAST, sysName, Locale.ENGLISH);
            Assert.assertNotNull(result); 
            Assert.assertEquals(JsonSignalMast.SIGNAL_MAST, result.path(JSON.TYPE).asText());
            Assert.assertEquals(sysName, result.path(JSON.DATA).path(JSON.NAME).asText());

            //retrieve by username, should get systemname back
            result = service.doGet(JsonSignalMast.SIGNAL_MAST, userName, Locale.ENGLISH);
            Assert.assertNotNull(result); 
            Assert.assertEquals(sysName, result.path(JSON.DATA).path(JSON.NAME).asText());
            
            //verify initial aspect/state is "Unknown"
            Assert.assertEquals(JSON.ASPECT_UNKNOWN, result.path(JSON.DATA).path(JSON.STATE).asText());
            //change to Clear, then verify change
            s.setAspect("Clear");
            result = service.doGet(JsonSignalMast.SIGNAL_MAST, userName, Locale.ENGLISH);
            Assert.assertEquals("Clear", result.path(JSON.DATA).path(JSON.STATE).asText());
        } catch (JsonException ex) {
            Assert.fail(ex.getMessage());
        }
    }

    public void testDoPost() throws JmriException {
        //create a signalmast for testing
        String sysName = "IF$shsm:basic:one-searchlight:SM2";
        String userName = "SM2";        
        SignalMast s = InstanceManager.getDefault(jmri.SignalMastManager.class).provideSignalMast(sysName);
        Assert.assertNotNull(s);
        s.setUserName(userName);
        JsonNode result;
        JsonNode message;
        ObjectMapper mapper = new ObjectMapper();
        JsonSignalMastHttpService service = new JsonSignalMastHttpService(new ObjectMapper());
       
        try {
            //set signalmast to Clear and verify change
            message = mapper.createObjectNode().put(JSON.NAME, userName).put(JSON.STATE, "Clear");
            result = service.doPost(JsonSignalMast.SIGNAL_MAST, userName, message, Locale.ENGLISH);
            Assert.assertNotNull(result);
            Assert.assertEquals("Clear", s.getAspect());
            Assert.assertEquals("Clear", result.path(JSON.DATA).path(JSON.STATE).asText());
        } catch (JsonException ex) {
            Assert.fail(ex.getMessage());
        }

        // try to set to UNKNOWN, which should not be allowed, so state should not change
        JsonException exception = null;
        try {
            message = mapper.createObjectNode().put(JSON.NAME, userName).put(JSON.STATE, JSON.ASPECT_UNKNOWN);
            result = service.doPost(JsonSignalMast.SIGNAL_MAST, userName, message, Locale.ENGLISH);
            Assert.assertNotNull(result);
            Assert.assertEquals("Clear", s.getAspect());
            Assert.assertEquals("Clear", result.path(JSON.DATA).path(JSON.STATE).asText());
        } catch (JsonException ex) {
            exception = ex;
        }
        Assert.assertNotNull(exception);
    }
   
    public void testDoGetList() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonSignalMastHttpService service = new JsonSignalMastHttpService(mapper);
            SignalMastManager manager = InstanceManager.getDefault(SignalMastManager.class);
            JsonNode result;
            result = service.doGetList(JsonSignalMast.SIGNAL_MAST, Locale.ENGLISH);
            Assert.assertNotNull(result);
            Assert.assertEquals(0, result.size());
            manager.provideSignalMast("IF$shsm:basic:one-searchlight:SM1");
            manager.provideSignalMast("IF$shsm:basic:one-searchlight:SM2");
            result = service.doGetList(JsonSignalMast.SIGNAL_MAST, Locale.ENGLISH);
            Assert.assertNotNull(result);
            Assert.assertEquals(2, result.size());
        } catch (JsonException ex) {
            Assert.fail(ex.getMessage());
        }
    }
    
    // from here down is testing infrastructure
    public JsonSignalMastHttpServiceTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {JsonSignalMastHttpServiceTest.class.getName()};
        TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(JsonSignalMastHttpServiceTest.class);

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
