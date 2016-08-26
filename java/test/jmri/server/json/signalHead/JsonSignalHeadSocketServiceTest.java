package jmri.server.json.signalHead;

import apps.tests.Log4JFixture;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Locale;
import jmri.JmriException;
import jmri.SignalHead;
import jmri.server.json.JSON;
import jmri.server.json.JsonException;
import jmri.server.json.JsonMockConnection;
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
public class JsonSignalHeadSocketServiceTest extends TestCase {

    public void testCtorSuccess() {
        JsonSignalHeadSocketService service = new JsonSignalHeadSocketService(new JsonMockConnection((DataOutputStream) null));
        Assert.assertNotNull(service);
    }

    public void testSignalHeadChange() {
        try {
            //create a signalhead for testing
            String sysName = "IH1";
            String userName = "SH1";        
            SignalHead s = new jmri.implementation.VirtualSignalHead(sysName, userName);
            jmri.InstanceManager.getDefault(jmri.SignalHeadManager.class).register(s);
            Assert.assertNotNull(s);

            JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
            JsonNode message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, sysName);
            JsonSignalHeadSocketService service = new JsonSignalHeadSocketService(connection);
            service.onMessage(JsonSignalHead.SIGNAL_HEAD, message, Locale.ENGLISH);
            
            //signalhead defaults to Dark
            Assert.assertEquals(SignalHead.DARK, connection.getMessage().path(JSON.DATA).path(JSON.STATE).asInt());

            //change to Green, and wait for change to show up, then verify
            s.setAppearance(SignalHead.GREEN);
            JUnitUtil.waitFor(() -> {
                return s.getState() == SignalHead.GREEN;
            }, "SignalHead is now GREEN");
            Assert.assertEquals(SignalHead.GREEN, connection.getMessage().path(JSON.DATA).path(JSON.STATE).asInt());
            
            //change to Red, and wait for change to show up, then verify
            s.setAppearance(SignalHead.RED);
            JUnitUtil.waitFor(() -> {
                return s.getState() == SignalHead.RED;
            }, "SignalHead is now RED");
            Assert.assertEquals(SignalHead.RED, connection.getMessage().path(JSON.DATA).path(JSON.STATE).asInt());
            
            service.onClose();

        } catch (IOException | JmriException | JsonException ex) {
            Assert.fail(ex.getMessage());
        }
    }

    public void testOnMessageChange() {
        JsonNode message;
        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        JsonSignalHeadSocketService service = new JsonSignalHeadSocketService(connection);

        //create a signalhead for testing
        String sysName = "IH1";
        String userName = "SH1";        
        SignalHead s = new jmri.implementation.VirtualSignalHead(sysName, userName);
        jmri.InstanceManager.getDefault(jmri.SignalHeadManager.class).register(s);
        Assert.assertNotNull(s);

        try {
            // SignalHead Yellow
            message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, userName)
                    .put(JSON.STATE, SignalHead.YELLOW);
            service.onMessage(JsonSignalHead.SIGNAL_HEAD, message, Locale.ENGLISH);           
            Assert.assertEquals(SignalHead.YELLOW, s.getState()); //state should be Yellow
        } catch (IOException | JmriException | JsonException ex) {
            Assert.fail(ex.getMessage());
        }

        // Try to set SignalHead to FLASHLUNAR, should throw error, remain at Yellow
        Exception exception = null;
        try {
            message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, userName)
                    .put(JSON.STATE, SignalHead.FLASHLUNAR);
            service.onMessage(JsonSignalHead.SIGNAL_HEAD, message, Locale.ENGLISH);
        } catch (IOException | JmriException | JsonException ex) {
            exception = ex;
        }
        Assert.assertNotNull(exception);
        Assert.assertEquals(SignalHead.YELLOW, s.getAppearance()); //state should be Yellow
        
    }

    // from here down is testing infrastructure
    public JsonSignalHeadSocketServiceTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {JsonSignalHeadSocketServiceTest.class.getName()};
        TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(JsonSignalHeadSocketServiceTest.class);

        return suite;
    }

    // The minimal setup for log4J
    @Override
    protected void setUp() throws Exception {
        Log4JFixture.setUp();
        super.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalSignalHeadManager();
    }

    @Override
    protected void tearDown() throws Exception {
        JUnitUtil.resetInstanceManager();
        super.tearDown();
        Log4JFixture.tearDown();
    }

}
