package jmri.jmrix.dccpp;

import jmri.Turnout;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests for the jmri.jmrix.dccpp.DCCppTurnout class
 *
 * @author Paul Bender
 * @author Mark Underwood
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

    // Test the initialization sequence.
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
    
    @Test
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
        Assert.assertEquals(Turnout.INCONSISTENT, t.getState());
        DCCppReply r = DCCppReply.parseDCCppReply("H 42 1");
        ((DCCppTurnout) t).message(r);
        Assert.assertEquals(Turnout.THROWN, t.getState());
        
        t.setCommandedState(Turnout.CLOSED);
        //Assert.assertEquals(t.getState(), Turnout.CLOSED);
        m = dnis.outbound.elementAt(1);
        Assert.assertTrue(m.isTurnoutCmdMessage());
        Assert.assertEquals(0, m.getTOStateInt());
        Assert.assertEquals(Turnout.INCONSISTENT, t.getState());
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
        Assert.assertEquals(Turnout.INCONSISTENT, t.getState());
        r = DCCppReply.parseDCCppReply("H 42 0");
        ((DCCppTurnout) t).message(r);
        Assert.assertEquals(Turnout.THROWN, t.getState());
        
        t.setCommandedState(Turnout.CLOSED);
        //Assert.assertEquals(t.getState(), Turnout.CLOSED);
        m = dnis.outbound.elementAt(3);
        Assert.assertTrue(m.isTurnoutCmdMessage());
        Assert.assertEquals(1, m.getTOStateInt());
        Assert.assertEquals(Turnout.INCONSISTENT, t.getState());
        r = DCCppReply.parseDCCppReply("H 42 1");
        ((DCCppTurnout) t).message(r);
        Assert.assertEquals(Turnout.CLOSED, t.getState());
    }

    @Test
    public void testExactMode() throws Exception {
        // Set mode to Exact
        t.setFeedbackMode(Turnout.EXACT);
        Assert.assertEquals(Turnout.EXACT, t.getFeedbackMode());
        
        // Check that state changes appropriately
        t.setCommandedState(Turnout.THROWN);
        //Assert.assertEquals(t.getState(), Turnout.THROWN);
        DCCppMessage m = dnis.outbound.elementAt(0);
        Assert.assertTrue(m.isOutputCmdMessage());
        Assert.assertEquals(0, m.getOutputStateInt());
        Assert.assertEquals(Turnout.INCONSISTENT, t.getState());
        DCCppReply r = DCCppReply.parseDCCppReply("Y 42 0");
        ((DCCppTurnout) t).message(r);
        Assert.assertEquals(Turnout.THROWN, t.getState());

        t.setCommandedState(Turnout.CLOSED);
        //Assert.assertEquals(t.getState(), Turnout.CLOSED);
        m = dnis.outbound.elementAt(1);
        Assert.assertTrue(m.isOutputCmdMessage());
        Assert.assertEquals(1, m.getOutputStateInt());
        Assert.assertEquals(Turnout.INCONSISTENT, t.getState());
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
        Assert.assertEquals(Turnout.INCONSISTENT, t.getState());
        r = DCCppReply.parseDCCppReply("Y 42 1");
        ((DCCppTurnout) t).message(r);
        Assert.assertEquals(Turnout.THROWN, t.getState());

        t.setCommandedState(Turnout.CLOSED);
        //Assert.assertEquals(t.getState(), Turnout.CLOSED);
        m = dnis.outbound.elementAt(3);
        Assert.assertTrue(m.isOutputCmdMessage());
        Assert.assertEquals(0, m.getOutputStateInt());
        Assert.assertEquals(Turnout.INCONSISTENT, t.getState());
        r = DCCppReply.parseDCCppReply("Y 42 0");
        ((DCCppTurnout) t).message(r);
        Assert.assertEquals(Turnout.CLOSED, t.getState());
    }

    @Test
    public void testDirectMode() throws Exception {
        // Set mode to DIRECT
        t.setFeedbackMode(Turnout.DIRECT);
        Assert.assertEquals(Turnout.DIRECT, t.getFeedbackMode());
        
        // Check that state changes appropriately
        t.setCommandedState(Turnout.THROWN);
        DCCppMessage m = dnis.outbound.elementAt(0);
        Assert.assertTrue(m.isAccessoryMessage());
        Assert.assertEquals(1, m.getAccessoryStateInt());
        Assert.assertEquals(Turnout.THROWN, t.getState());
        
        t.setCommandedState(Turnout.CLOSED);
        m = dnis.outbound.elementAt(1);
        Assert.assertTrue(m.isAccessoryMessage());
        Assert.assertEquals(0, m.getAccessoryStateInt());
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


    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        // infrastructure objects
        dnis = new DCCppInterfaceScaffold(new DCCppCommandStation());
        t = new DCCppTurnout("DCCPP", 42, dnis);
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }

}
