package jmri.jmrix.dccpp;

import jmri.Turnout;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * DCCppThrottleTest.java
 *
 * Description:	tests for the jmri.jmrix.dccpp.DCCppTurnout class
 *
 * @author	Paul Bender
 * @author	Mark Underwood
 */
public class DCCppTurnoutTest extends jmri.implementation.AbstractTurnoutTestBase {

    @Override
    public int numListeners() {
        return dnis.numListeners();
    }

    protected DCCppInterfaceScaffold dnis;

    @Override
    public void checkClosedMsgSent() {
        Assert.assertEquals("closed message", "a 11 1 0",
                dnis.outbound.elementAt(dnis.outbound.size() - 1).toString());
    }

    @Override
    public void checkThrownMsgSent() {
        Assert.assertEquals("thrown message", "a 11 1 1",
                dnis.outbound.elementAt(dnis.outbound.size() - 1).toString());
    }

    @Test
    public void testCtor() {
        Assert.assertNotNull(t);
    }

    // Test the initilization sequence.
    @Test
    public void testInitSequence() throws Exception {
        int num = ((DCCppTurnout)t).getNumber();
        Assert.assertEquals(42, num);
        
        int[] vals = DCCppTurnout.getModeValues();
        Assert.assertEquals(6, vals.length);
        Assert.assertEquals(Turnout.MONITORING, vals[4]);
        Assert.assertEquals(Turnout.EXACT, vals[5]);
        
        String[] names = DCCppTurnout.getModeNames();
        Assert.assertEquals(6, names.length);
        Assert.assertEquals("BSTURNOUT", names[4]);
        Assert.assertEquals("BSOUTPUT", names[5]);
        // TODO: CHeck some othr stuff
        
        // Check a few basic things
        Assert.assertTrue(t.canInvert());
        
    }
   
    public void testMonitoringMode() throws Exception {
        // Set mode to Monitoring
        t.setFeedbackMode(Turnout.MONITORING);
        Assert.assertEquals(Turnout.MONITORING, t.getFeedbackMode());
        
        // Check that state changes appropriately
        t.setCommandedState(Turnout.THROWN);
        //Assert.assertEquals(t.getState(), Turnout.THROWN);
        DCCppMessage m = dnis.outbound.elementAt(0);
        Assert.assertTrue(m.isTurnoutCmdMessage());
        Assert.assertEquals(1, m.getTOStateInt());
        DCCppReply r = DCCppReply.parseDCCppReply("H 42 1");
        ((DCCppTurnout) t).message(r);
        Assert.assertEquals(Turnout.THROWN, t.getState());
        
        t.setCommandedState(Turnout.CLOSED);
        //Assert.assertEquals(t.getState(), Turnout.CLOSED);
        m = dnis.outbound.elementAt(1);
        Assert.assertTrue(m.isTurnoutCmdMessage());
        Assert.assertEquals(0, m.getTOStateInt());
        r = DCCppReply.parseDCCppReply("H 42 0");
        ((DCCppTurnout) t).message(r);
        Assert.assertEquals(Turnout.CLOSED, t.getState());

        // Test Inverted Mode
        // Check that state changes appropriately
        t.setInverted(true);
        t.setCommandedState(Turnout.THROWN);
        //Assert.assertEquals(t.getState(), Turnout.THROWN);
        m = dnis.outbound.elementAt(2);
        Assert.assertTrue(m.isTurnoutCmdMessage());
        Assert.assertEquals(0, m.getTOStateInt());
        r = DCCppReply.parseDCCppReply("H 42 0");
        ((DCCppTurnout) t).message(r);
        Assert.assertEquals(Turnout.THROWN, t.getState());
        
        t.setCommandedState(Turnout.CLOSED);
        //Assert.assertEquals(t.getState(), Turnout.CLOSED);
        m = dnis.outbound.elementAt(3);
        Assert.assertTrue(m.isTurnoutCmdMessage());
        Assert.assertEquals(1, m.getTOStateInt());
        r = DCCppReply.parseDCCppReply("H 42 1");
        ((DCCppTurnout) t).message(r);
        Assert.assertEquals(Turnout.CLOSED, t.getState());
    }

    @Test
    public void testExactMode() throws Exception {
        // Set mode to Monitoring
        t.setFeedbackMode(Turnout.EXACT);
        Assert.assertEquals(Turnout.EXACT, t.getFeedbackMode());
        
        // Check that state changes appropriately
        t.setCommandedState(Turnout.THROWN);
        //Assert.assertEquals(t.getState(), Turnout.THROWN);
        DCCppMessage m = dnis.outbound.elementAt(0);
        Assert.assertTrue(m.isOutputCmdMessage());
        Assert.assertEquals(0, m.getOutputStateInt());
        DCCppReply r = DCCppReply.parseDCCppReply("Y 42 0");
        ((DCCppTurnout) t).message(r);
        Assert.assertEquals(Turnout.THROWN, t.getState());

        t.setCommandedState(Turnout.CLOSED);
        //Assert.assertEquals(t.getState(), Turnout.CLOSED);
        m = dnis.outbound.elementAt(1);
        Assert.assertTrue(m.isOutputCmdMessage());
        Assert.assertEquals(1, m.getOutputStateInt());
        r = DCCppReply.parseDCCppReply("Y 42 1");
        ((DCCppTurnout) t).message(r);
        Assert.assertEquals(Turnout.CLOSED, t.getState());

        // Test Inverted Mode
        // Check that state changes appropriately
        t.setInverted(true);
        t.setCommandedState(Turnout.THROWN);
        //Assert.assertEquals(t.getState(), Turnout.THROWN);
        m = dnis.outbound.elementAt(2);
        Assert.assertTrue(m.isOutputCmdMessage());
        Assert.assertEquals(1, m.getOutputStateInt());
        r = DCCppReply.parseDCCppReply("Y 42 1");
        ((DCCppTurnout) t).message(r);
        Assert.assertEquals(Turnout.THROWN, t.getState());

        t.setCommandedState(Turnout.CLOSED);
        //Assert.assertEquals(t.getState(), Turnout.CLOSED);
        m = dnis.outbound.elementAt(3);
        Assert.assertTrue(m.isOutputCmdMessage());
        Assert.assertEquals(0, m.getOutputStateInt());
        r = DCCppReply.parseDCCppReply("Y 42 0");
        ((DCCppTurnout) t).message(r);
        Assert.assertEquals(Turnout.CLOSED, t.getState());
    }

    @Test
    @Override
    public void testDispose() {
        t.setCommandedState(jmri.Turnout.CLOSED);    // in case registration with TrafficController

        //is deferred to after first use
        t.dispose();
        Assert.assertEquals("controller listeners remaining", 1, numListeners());
    }


    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        // infrastructure objects
        dnis = new DCCppInterfaceScaffold(new DCCppCommandStation());
        t = new DCCppTurnout("DCCPP", 42, dnis);
    }

    @After
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }

}
