package jmri.jmrix.dccpp;

import org.junit.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import jmri.Turnout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DCCppThrottleTest.java
 *
 * Description:	tests for the jmri.jmrix.dccpp.DCCppThrottle class
 *
 * @author	Paul Bender
 * @author	Mark Underwood
 */
public class DCCppTurnoutTest extends TestCase {

    private final static Logger log = LoggerFactory.getLogger(DCCppTurnoutTest.class.getName());

    public void testCtor() {
        // infrastructure objects
        DCCppInterfaceScaffold tc = new DCCppInterfaceScaffold(new DCCppCommandStation());

        DCCppTurnout t = new DCCppTurnout("DCCPP", 1, tc);
        Assert.assertNotNull(t);
    }

    // Test the initilization sequence.
    public void testInitSequence() throws Exception {
        DCCppInterfaceScaffold tc = new DCCppInterfaceScaffold(new DCCppCommandStation());
        //int n = tc.outbound.size();
        DCCppTurnout t = new DCCppTurnout("DCCPP", 42, tc);
        Assert.assertNotNull(t);
        
        int num = t.getNumber();
        Assert.assertEquals(42, num);
        
        int[] vals = DCCppTurnout.getModeValues();
        Assert.assertEquals(5, vals.length);
        Assert.assertEquals(Turnout.MONITORING, vals[3]);
        Assert.assertEquals(Turnout.EXACT, vals[4]);
        
        String[] names = DCCppTurnout.getModeNames();
        Assert.assertEquals(5, names.length);
        Assert.assertEquals("BSTURNOUT", names[3]);
        Assert.assertEquals("BSOUTPUT", names[4]);
        // TODO: CHeck some othr stuff
        
        // Check a few basic things
        Assert.assertTrue(t.canInvert());
        
    }
    
    public void testDirectMode() throws Exception {
        DCCppInterfaceScaffold tc = new DCCppInterfaceScaffold(new DCCppCommandStation());
        //int n = tc.outbound.size();
        DCCppTurnout t = new DCCppTurnout("DCCPP", 42, tc);
        Assert.assertNotNull(t);

        // Default mode is DIRECT
        Assert.assertEquals(Turnout.DIRECT, t.getFeedbackMode());
        
        // Check that state changes appropriately
        t.setCommandedState(Turnout.THROWN);
        //Assert.assertEquals(t.getState(), Turnout.THROWN);
        DCCppMessage m = tc.outbound.elementAt(0);
        Assert.assertTrue(m.isAccessoryMessage());
        Assert.assertEquals(1, m.getAccessoryStateInt());
        t.setCommandedState(Turnout.CLOSED);
        //Assert.assertEquals(t.getState(), Turnout.CLOSED);
        m = tc.outbound.elementAt(1);
        Assert.assertTrue(m.isAccessoryMessage());
        Assert.assertEquals(0, m.getAccessoryStateInt());
        
        // Test Inverted state
        // Check that state changes appropriately
        t.setInverted(true);
        t.setCommandedState(Turnout.THROWN);
        //Assert.assertEquals(t.getState(), Turnout.THROWN);
        m = tc.outbound.elementAt(2);
        log.debug("Inverted Direct: {}", m.toString());
        Assert.assertTrue(m.isAccessoryMessage());
        Assert.assertEquals(0, m.getAccessoryStateInt());
        t.setCommandedState(Turnout.CLOSED);
        //Assert.assertEquals(t.getState(), Turnout.CLOSED);
        m = tc.outbound.elementAt(3);
        Assert.assertTrue(m.isAccessoryMessage());
        Assert.assertEquals(1, m.getAccessoryStateInt());        
    }
    
    public void testMonitoringMode() throws Exception {
        DCCppInterfaceScaffold tc = new DCCppInterfaceScaffold(new DCCppCommandStation());
        //int n = tc.outbound.size();
        DCCppTurnout t = new DCCppTurnout("DCCPP", 42, tc);
        Assert.assertNotNull(t);

        // Set mode to Monitoring
        t.setFeedbackMode(Turnout.MONITORING);
        Assert.assertEquals(Turnout.MONITORING, t.getFeedbackMode());
        
        // Check that state changes appropriately
        t.setCommandedState(Turnout.THROWN);
        //Assert.assertEquals(t.getState(), Turnout.THROWN);
        DCCppMessage m = tc.outbound.elementAt(0);
        Assert.assertTrue(m.isTurnoutCmdMessage());
        Assert.assertEquals(1, m.getTOStateInt());
        DCCppReply r = DCCppReply.parseDCCppReply("H 42 1");
        t.message(r);
        Assert.assertEquals(Turnout.THROWN, t.getState());
        
        t.setCommandedState(Turnout.CLOSED);
        //Assert.assertEquals(t.getState(), Turnout.CLOSED);
        m = tc.outbound.elementAt(1);
        Assert.assertTrue(m.isTurnoutCmdMessage());
        Assert.assertEquals(0, m.getTOStateInt());
        r = DCCppReply.parseDCCppReply("H 42 0");
        t.message(r);
        Assert.assertEquals(Turnout.CLOSED, t.getState());

        // Test Inverted Mode
        // Check that state changes appropriately
        t.setInverted(true);
        t.setCommandedState(Turnout.THROWN);
        //Assert.assertEquals(t.getState(), Turnout.THROWN);
        m = tc.outbound.elementAt(2);
        Assert.assertTrue(m.isTurnoutCmdMessage());
        Assert.assertEquals(0, m.getTOStateInt());
        r = DCCppReply.parseDCCppReply("H 42 0");
        t.message(r);
        Assert.assertEquals(Turnout.THROWN, t.getState());
        
        t.setCommandedState(Turnout.CLOSED);
        //Assert.assertEquals(t.getState(), Turnout.CLOSED);
        m = tc.outbound.elementAt(3);
        Assert.assertTrue(m.isTurnoutCmdMessage());
        Assert.assertEquals(1, m.getTOStateInt());
        r = DCCppReply.parseDCCppReply("H 42 1");
        t.message(r);
        Assert.assertEquals(Turnout.CLOSED, t.getState());
    }

    public void testExactMode() throws Exception {
        DCCppInterfaceScaffold tc = new DCCppInterfaceScaffold(new DCCppCommandStation());
        //int n = tc.outbound.size();
        DCCppTurnout t = new DCCppTurnout("DCCPP", 42, tc);
        Assert.assertNotNull(t);

        // Set mode to Monitoring
        t.setFeedbackMode(Turnout.EXACT);
        Assert.assertEquals(Turnout.EXACT, t.getFeedbackMode());
        
        // Check that state changes appropriately
        t.setCommandedState(Turnout.THROWN);
        //Assert.assertEquals(t.getState(), Turnout.THROWN);
        DCCppMessage m = tc.outbound.elementAt(0);
        Assert.assertTrue(m.isOutputCmdMessage());
        Assert.assertEquals(1, m.getOutputStateInt());
        DCCppReply r = DCCppReply.parseDCCppReply("Y 42 0");
        t.message(r);
        Assert.assertEquals(Turnout.THROWN, t.getState());

        t.setCommandedState(Turnout.CLOSED);
        //Assert.assertEquals(t.getState(), Turnout.CLOSED);
        m = tc.outbound.elementAt(1);
        Assert.assertTrue(m.isOutputCmdMessage());
        Assert.assertEquals(0, m.getOutputStateInt());
        r = DCCppReply.parseDCCppReply("Y 42 1");
        t.message(r);
        Assert.assertEquals(Turnout.CLOSED, t.getState());

        // Test Inverted Mode
        // Check that state changes appropriately
        t.setInverted(true);
        t.setCommandedState(Turnout.THROWN);
        //Assert.assertEquals(t.getState(), Turnout.THROWN);
        m = tc.outbound.elementAt(2);
        Assert.assertTrue(m.isOutputCmdMessage());
        Assert.assertEquals(0, m.getOutputStateInt());
        r = DCCppReply.parseDCCppReply("Y 42 1");
        t.message(r);
        Assert.assertEquals(Turnout.THROWN, t.getState());

        t.setCommandedState(Turnout.CLOSED);
        //Assert.assertEquals(t.getState(), Turnout.CLOSED);
        m = tc.outbound.elementAt(3);
        Assert.assertTrue(m.isOutputCmdMessage());
        Assert.assertEquals(1, m.getOutputStateInt());
        r = DCCppReply.parseDCCppReply("Y 42 0");
        t.message(r);
        Assert.assertEquals(Turnout.CLOSED, t.getState());
    }

    // from here down is testing infrastructure
    public DCCppTurnoutTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", DCCppTurnoutTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(DCCppTurnoutTest.class);
        return suite;
    }

    // The minimal setup for log4J
    @Override
    protected void setUp() throws Exception {
        apps.tests.Log4JFixture.setUp();
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        apps.tests.Log4JFixture.tearDown();
    }

}
