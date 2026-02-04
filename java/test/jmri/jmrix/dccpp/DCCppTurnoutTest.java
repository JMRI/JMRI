package jmri.jmrix.dccpp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jmri.Turnout;
import jmri.util.JUnitUtil;

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

    private DCCppInterfaceScaffold dnis;

    @Override
    public void checkClosedMsgSent() {
        assertEquals( "a 11 1 0",
            dnis.outbound.elementAt(dnis.outbound.size() - 1).toString(),
            "closed message");
    }

    @Override
    public void checkThrownMsgSent() {
        assertEquals( "a 11 1 1",
            dnis.outbound.elementAt(dnis.outbound.size() - 1).toString(),
            "thrown message");
    }

    @Test
    public void testCtor() {
        assertNotNull(t);
    }

    // Test the initialization sequence.
    @Test
    public void testInitSequence() {
        int num = ((DCCppTurnout)t).getNumber();
        assertEquals(42, num);
        
        int[] vals = DCCppTurnout.getModeValues();
        assertEquals(6, vals.length);
        assertEquals(Turnout.MONITORING, vals[4]);
        assertEquals(Turnout.EXACT, vals[5]);
        
        String[] names = DCCppTurnout.getModeNames();
        assertEquals(6, names.length);
        assertEquals("BSTURNOUT", names[4]);
        assertEquals("BSOUTPUT", names[5]);
        // TODO: CHeck some othr stuff
        
        // Check a few basic things
        assertTrue(t.canInvert());
        
    }
    
    @Test
    public void testMonitoringMode() {
        // Set mode to Monitoring
        t.setFeedbackMode(Turnout.MONITORING);
        assertEquals(Turnout.MONITORING, t.getFeedbackMode());
        
        // Check that state changes appropriately
        t.setCommandedState(Turnout.THROWN);
        //Assert.assertEquals(t.getState(), Turnout.THROWN);
        DCCppMessage m = dnis.outbound.elementAt(0);
        assertTrue(m.isTurnoutCmdMessage());
        assertEquals(1, m.getTOStateInt());
        assertEquals(Turnout.INCONSISTENT, t.getState());
        DCCppReply r = DCCppReply.parseDCCppReply("H 42 1");
        ((DCCppTurnout) t).message(r);
        assertEquals(Turnout.THROWN, t.getState());
        
        t.setCommandedState(Turnout.CLOSED);
        //Assert.assertEquals(t.getState(), Turnout.CLOSED);
        m = dnis.outbound.elementAt(1);
        assertTrue(m.isTurnoutCmdMessage());
        assertEquals(0, m.getTOStateInt());
        assertEquals(Turnout.INCONSISTENT, t.getState());
        r = DCCppReply.parseDCCppReply("H 42 0");
        ((DCCppTurnout) t).message(r);
        assertEquals(Turnout.CLOSED, t.getState());

        // Test Inverted Mode
        // Check that state changes appropriately
        t.setInverted(true);
        t.setCommandedState(Turnout.THROWN);
        //Assert.assertEquals(t.getState(), Turnout.THROWN);
        m = dnis.outbound.elementAt(2);
        assertTrue(m.isTurnoutCmdMessage());
        assertEquals(0, m.getTOStateInt());
        assertEquals(Turnout.INCONSISTENT, t.getState());
        r = DCCppReply.parseDCCppReply("H 42 0");
        ((DCCppTurnout) t).message(r);
        assertEquals(Turnout.THROWN, t.getState());
        
        t.setCommandedState(Turnout.CLOSED);
        //Assert.assertEquals(t.getState(), Turnout.CLOSED);
        m = dnis.outbound.elementAt(3);
        assertTrue(m.isTurnoutCmdMessage());
        assertEquals(1, m.getTOStateInt());
        assertEquals(Turnout.INCONSISTENT, t.getState());
        r = DCCppReply.parseDCCppReply("H 42 1");
        ((DCCppTurnout) t).message(r);
        assertEquals(Turnout.CLOSED, t.getState());
    }

    @Test
    public void testExactMode() {
        // Set mode to Exact
        t.setFeedbackMode(Turnout.EXACT);
        assertEquals(Turnout.EXACT, t.getFeedbackMode());
        
        // Check that state changes appropriately
        t.setCommandedState(Turnout.THROWN);
        //Assert.assertEquals(t.getState(), Turnout.THROWN);
        DCCppMessage m = dnis.outbound.elementAt(0);
        assertTrue(m.isOutputCmdMessage());
        assertEquals(0, m.getOutputStateInt());
        assertEquals(Turnout.INCONSISTENT, t.getState());
        DCCppReply r = DCCppReply.parseDCCppReply("Y 42 0");
        ((DCCppTurnout) t).message(r);
        assertEquals(Turnout.THROWN, t.getState());

        t.setCommandedState(Turnout.CLOSED);
        //Assert.assertEquals(t.getState(), Turnout.CLOSED);
        m = dnis.outbound.elementAt(1);
        assertTrue(m.isOutputCmdMessage());
        assertEquals(1, m.getOutputStateInt());
        assertEquals(Turnout.INCONSISTENT, t.getState());
        r = DCCppReply.parseDCCppReply("Y 42 1");
        ((DCCppTurnout) t).message(r);
        assertEquals(Turnout.CLOSED, t.getState());

        // Test Inverted Mode
        // Check that state changes appropriately
        t.setInverted(true);
        t.setCommandedState(Turnout.THROWN);
        //Assert.assertEquals(t.getState(), Turnout.THROWN);
        m = dnis.outbound.elementAt(2);
        assertTrue(m.isOutputCmdMessage());
        assertEquals(1, m.getOutputStateInt());
        assertEquals(Turnout.INCONSISTENT, t.getState());
        r = DCCppReply.parseDCCppReply("Y 42 1");
        ((DCCppTurnout) t).message(r);
        assertEquals(Turnout.THROWN, t.getState());

        t.setCommandedState(Turnout.CLOSED);
        //Assert.assertEquals(t.getState(), Turnout.CLOSED);
        m = dnis.outbound.elementAt(3);
        assertTrue(m.isOutputCmdMessage());
        assertEquals(0, m.getOutputStateInt());
        assertEquals(Turnout.INCONSISTENT, t.getState());
        r = DCCppReply.parseDCCppReply("Y 42 0");
        ((DCCppTurnout) t).message(r);
        assertEquals(Turnout.CLOSED, t.getState());
    }

    @Test
    public void testDirectMode() {
        // Set mode to DIRECT
        t.setFeedbackMode(Turnout.DIRECT);
        assertEquals(Turnout.DIRECT, t.getFeedbackMode());
        
        // Check that state changes appropriately
        t.setCommandedState(Turnout.THROWN);
        DCCppMessage m = dnis.outbound.elementAt(0);
        assertTrue(m.isAccessoryMessage());
        assertEquals(1, m.getAccessoryStateInt());
        assertEquals(Turnout.THROWN, t.getState());
        
        t.setCommandedState(Turnout.CLOSED);
        m = dnis.outbound.elementAt(1);
        assertTrue(m.isAccessoryMessage());
        assertEquals(0, m.getAccessoryStateInt());
        assertEquals(Turnout.CLOSED, t.getState());
    }

    @Test
    @Override
    public void testDispose() {
        t.setCommandedState(jmri.Turnout.CLOSED);    // in case registration with TrafficController

        //is deferred to after first use
        t.dispose();
        assertEquals( 1, numListeners(), "controller listeners remaining");
    }


    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        // infrastructure objects
        dnis = new DCCppInterfaceScaffold(new DCCppCommandStation());
        t = new DCCppTurnout("DCCPP", 42, dnis);
    }

    @Override
    @AfterEach
    public void tearDown() {
        dnis.terminateThreads();
        dnis = null;
        JUnitUtil.tearDown();

    }

}
