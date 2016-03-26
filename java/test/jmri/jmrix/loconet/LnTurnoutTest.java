//LnTurnoutTest.java
package jmri.jmrix.loconet;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the jmri.jmrix.loconet.LnTurnout class
 *
 * @author	Bob Jacobsen
 */
public class LnTurnoutTest extends jmri.implementation.AbstractTurnoutTest {

    public void setUp() {
        // prepare an interface
        lnis = new LocoNetInterfaceScaffold();

        // outwait any pending delayed sends
        try {
            synchronized (this) {
                this.wait(LnTurnout.METERINTERVAL + 25);
            }
        } catch (InterruptedException e) {
        }

        // create object under test
        t = new LnTurnout("L", 21, lnis);
    }

    public int numListeners() {
        return lnis.numListeners();
    }

    LocoNetInterfaceScaffold lnis;

    /**
     * Check that last two messages correspond to closed/on, then closed/off.
     * Why last two? For unknown reason(s), this test gets _three_ messages,
     * with the first one being a set 21 closed and off. Is it left over from
     * some previous test?
     */
    public void checkClosedMsgSent() throws InterruptedException {
        // Make sure that timed message has fired by waiting
        synchronized (this) {
            this.wait(LnTurnout.METERINTERVAL + 25);
        }

        // check results
        Assert.assertTrue("at least two messages", lnis.outbound.size() >= 2);
        Assert.assertEquals(lnis.outbound.elementAt(lnis.outbound.size() - 2).toString(),
                "B0 14 30 00");  // CLOSED/ON loconet message
        Assert.assertEquals(lnis.outbound.elementAt(lnis.outbound.size() - 1).toString(),
                "B0 14 20 00");  // CLOSED/OFF loconet message
        Assert.assertTrue(t.getCommandedState() == jmri.Turnout.CLOSED);
    }

    /**
     * Check that last two messages correspond to thrown/on, then thrown/off
     */
    public void checkThrownMsgSent() throws InterruptedException {
        // Make sure that timed message has fired by waiting
        synchronized (this) {
            this.wait(LnTurnout.METERINTERVAL + 25);
        }

        // check for messages
        Assert.assertTrue("just two messages", lnis.outbound.size() == 2);
        Assert.assertEquals(lnis.outbound.elementAt(lnis.outbound.size() - 2).toString(),
                "B0 14 10 00");  // THROWN/ON loconet message
        Assert.assertEquals(lnis.outbound.elementAt(lnis.outbound.size() - 1).toString(),
                "B0 14 00 00");  // THROWN/OFF loconet message
        Assert.assertTrue(t.getCommandedState() == jmri.Turnout.THROWN);
    }

    public void checkIncoming() {
        // notify the Ln that somebody else changed it...
        LocoNetMessage m = new LocoNetMessage(4);
        m.setOpCode(0xb0);
        m.setElement(1, 0x14);     // set CLOSED
        m.setElement(2, 0x30);
        m.setElement(3, 0x00);
        lnis.sendTestMessage(m);
        Assert.assertTrue(t.getCommandedState() == jmri.Turnout.CLOSED);

        m = new LocoNetMessage(4);
        m.setOpCode(0xb0);
        m.setElement(1, 0x14);     // set THROWN
        m.setElement(2, 0x10);
        m.setElement(3, 0x00);
        lnis.sendTestMessage(m);
        Assert.assertTrue(t.getCommandedState() == jmri.Turnout.THROWN);
    }

    // LnTurnout test for incoming status message
    public void testLnTurnoutStatusMsg() {
        // prepare an interface
        // set closed
        try {
            t.setCommandedState(jmri.Turnout.CLOSED);
        } catch (Exception e) {
            log.error("TO exception: " + e);
        }
        Assert.assertTrue(lnis.outbound.elementAt(0)
                .toString().equals("B0 14 30 00"));  // CLOSED loconet message
        Assert.assertTrue(t.getCommandedState() == jmri.Turnout.CLOSED);

        // notify the Ln that somebody else changed it...
        LocoNetMessage m = new LocoNetMessage(4);
        m.setOpCode(0xb1);
        m.setElement(1, 0x14);     // set CLOSED
        m.setElement(2, 0x20);
        m.setElement(3, 0x7b);
        lnis.sendTestMessage(m);
        Assert.assertTrue(t.getCommandedState() == jmri.Turnout.CLOSED);

    }

    // LnTurnout test for exact feedback
    public void testLnTurnoutExactFeedback() {
        LocoNetMessage m;
        // prepare a specific test
        t = new LnTurnout("L", 21, lnis); // note different address; we have traces for this address
        t.setBinaryOutput(true);
        t.setCommandedState(jmri.Turnout.CLOSED);
        t.setFeedbackMode(jmri.Turnout.EXACT);
        Assert.assertEquals("CommandedState after set CLOSED is CLOSED", jmri.Turnout.CLOSED, t.getCommandedState());
        // because this is the first time, the state is UNKNOWN; never goes back to that (in this test)
        Assert.assertEquals("KnownState after set CLOSED is UNKNOWN", jmri.Turnout.UNKNOWN, t.getKnownState());

        // notify the Ln of first feedback - AUX is thrown, so moved off 
        m = new LocoNetMessage(4);
        m.setOpCode(0xb1);
        m.setElement(1, 0x14);
        m.setElement(2, 0x40);
        m.setElement(3, 0x1A);     // AUX reports THROWN
        lnis.sendTestMessage(m);
        Assert.assertEquals("CommandedState after AUX report THROWN is CLOSED", jmri.Turnout.CLOSED, t.getCommandedState());
        Assert.assertEquals("KnownState after AUX report THROWN is INCONSISTENT", jmri.Turnout.INCONSISTENT, t.getKnownState());

        // notify the Ln of second feedback - SWITCH is closed, so moved on
        m = new LocoNetMessage(4);
        m.setOpCode(0xb1);
        m.setElement(1, 0x14);
        m.setElement(2, 0x70);
        m.setElement(3, 0x2A);     // SWITCH reports CLOSED
        lnis.sendTestMessage(m);
        Assert.assertEquals("CommandedState after SWITCH report CLOSED is CLOSED", jmri.Turnout.CLOSED, t.getCommandedState());
        Assert.assertEquals("KnownState after SWITCH report CLOSED is CLOSED", jmri.Turnout.CLOSED, t.getKnownState());

        // test transition to THROWN
        t.setCommandedState(jmri.Turnout.THROWN);
        Assert.assertEquals("CommandedState after set THROWN is THROWN", jmri.Turnout.THROWN, t.getCommandedState());
        Assert.assertEquals("KnownState after set THROWN is UNKNOWN", jmri.Turnout.CLOSED, t.getKnownState());

        // notify the Ln of first feedback - SWITCH is thrown, so moved off 
        m = new LocoNetMessage(4);
        m.setOpCode(0xb1);
        m.setElement(1, 0x14);
        m.setElement(2, 0x60);
        m.setElement(3, 0x3A);     // AUX reports THROWN
        lnis.sendTestMessage(m);
        Assert.assertEquals("CommandedState after SWITCH report THROWN is THROWN", jmri.Turnout.THROWN, t.getCommandedState());
        Assert.assertEquals("KnownState after SWITCH report THROWN is INCONSISTENT", jmri.Turnout.INCONSISTENT, t.getKnownState());

        // notify the Ln of second feedback - AUX is closed, so moved on
        m = new LocoNetMessage(4);
        m.setOpCode(0xb1);
        m.setElement(1, 0x14);
        m.setElement(2, 0x50);
        m.setElement(3, 0x0A);     // SWITCH reports CLOSED
        lnis.sendTestMessage(m);
        Assert.assertEquals("CommandedState after AUX report CLOSED is THROWN", jmri.Turnout.THROWN, t.getCommandedState());
        Assert.assertEquals("KnownState after AUX report CLOSED is THROWN", jmri.Turnout.THROWN, t.getKnownState());

        // test transition back to CLOSED
        t.setCommandedState(jmri.Turnout.CLOSED);
        Assert.assertEquals("CommandedState after 2nd set CLOSED is CLOSED", jmri.Turnout.CLOSED, t.getCommandedState());
        Assert.assertEquals("KnownState after 2nd set CLOSED is THROWN", jmri.Turnout.THROWN, t.getKnownState());

        // notify the Ln of first feedback - AUX is thrown, so moved off 
        m = new LocoNetMessage(4);
        m.setOpCode(0xb1);
        m.setElement(1, 0x14);
        m.setElement(2, 0x40);
        m.setElement(3, 0x1A);     // AUX reports THROWN
        lnis.sendTestMessage(m);
        Assert.assertEquals("CommandedState after AUX report THROWN is CLOSED", jmri.Turnout.CLOSED, t.getCommandedState());
        Assert.assertEquals("KnownState after AUX report THROWN is INCONSISTENT", jmri.Turnout.INCONSISTENT, t.getKnownState());

        // notify the Ln of second feedback - SWITCH is closed, so moved on
        m = new LocoNetMessage(4);
        m.setOpCode(0xb1);
        m.setElement(1, 0x14);
        m.setElement(2, 0x70);
        m.setElement(3, 0x2A);     // SWITCH reports CLOSED
        lnis.sendTestMessage(m);
        Assert.assertEquals("CommandedState after SWITCH report CLOSED is CLOSED", jmri.Turnout.CLOSED, t.getCommandedState());
        Assert.assertEquals("KnownState after SWITCH report CLOSED is CLOSED", jmri.Turnout.CLOSED, t.getKnownState());

        // test transition to back to THROWN in wrong order 
        t.setCommandedState(jmri.Turnout.THROWN);
        Assert.assertEquals("CommandedState after 2nd set THROWN is THROWN", jmri.Turnout.THROWN, t.getCommandedState());
        Assert.assertEquals("KnownState after 2nd set THROWN is CLOSED", jmri.Turnout.CLOSED, t.getKnownState());

        // notify the Ln of second feedback (out of order) - AUX is closed, so moved on
        m = new LocoNetMessage(4);
        m.setOpCode(0xb1);
        m.setElement(1, 0x14);
        m.setElement(2, 0x50);
        m.setElement(3, 0x0A);     // SWITCH reports CLOSED
        lnis.sendTestMessage(m);
        Assert.assertEquals("CommandedState after AUX report CLOSED is THROWN", jmri.Turnout.THROWN, t.getCommandedState());
        Assert.assertEquals("KnownState after AUX report CLOSED is THROWN", jmri.Turnout.THROWN, t.getKnownState());

        // notify the Ln of first feedback (out of order) - SWITCH is thrown, so moved off - ignored
        m = new LocoNetMessage(4);
        m.setOpCode(0xb1);
        m.setElement(1, 0x14);
        m.setElement(2, 0x60);
        m.setElement(3, 0x3A);     // AUX reports THROWN
        lnis.sendTestMessage(m);
        Assert.assertEquals("CommandedState after SWITCH report THROWN is THROWN", jmri.Turnout.THROWN, t.getCommandedState());
        Assert.assertEquals("KnownState after SWITCH report THROWN is THROWN", jmri.Turnout.THROWN, t.getKnownState());

        // test transition back to CLOSED in wrong order
        t.setCommandedState(jmri.Turnout.CLOSED);
        Assert.assertEquals("CommandedState after 2nd set CLOSED is CLOSED", jmri.Turnout.CLOSED, t.getCommandedState());
        Assert.assertEquals("KnownState after 2nd set CLOSED is THROWN", jmri.Turnout.THROWN, t.getKnownState());

        // notify the Ln of second feedback (out of order) - SWITCH is closed, so moved on
        m = new LocoNetMessage(4);
        m.setOpCode(0xb1);
        m.setElement(1, 0x14);
        m.setElement(2, 0x70);
        m.setElement(3, 0x2A);     // SWITCH reports CLOSED
        lnis.sendTestMessage(m);
        Assert.assertEquals("CommandedState after SWITCH report CLOSED is CLOSED", jmri.Turnout.CLOSED, t.getCommandedState());
        Assert.assertEquals("KnownState after SWITCH report CLOSED is CLOSED", jmri.Turnout.CLOSED, t.getKnownState());

        // notify the Ln of first feedback (out of order) - AUX is thrown, so moved off 
        m = new LocoNetMessage(4);
        m.setOpCode(0xb1);
        m.setElement(1, 0x14);
        m.setElement(2, 0x40);
        m.setElement(3, 0x1A);     // AUX reports THROWN
        lnis.sendTestMessage(m);
        Assert.assertEquals("CommandedState after AUX report THROWN is CLOSED", jmri.Turnout.CLOSED, t.getCommandedState());
        Assert.assertEquals("KnownState after AUX report THROWN is CLOSED", jmri.Turnout.CLOSED, t.getKnownState());

    }

    // test that only one message is sent when binaryOutput is set
    public void testBasicSet() throws InterruptedException {
        t = new LnTurnout("L", 121, lnis);
        t.setBinaryOutput(true);
        t.setCommandedState(jmri.Turnout.THROWN);

        // Make sure that timed message has fired by waiting
        synchronized (this) {
            this.wait(LnTurnout.METERINTERVAL + 25);
        }

        // check for messages
        Assert.assertTrue("just one messages", lnis.outbound.size() == 1);
        Assert.assertEquals(lnis.outbound.elementAt(lnis.outbound.size() - 1).toString(),
                "B0 78 10 00");  // THROWN/ON loconet message
        Assert.assertTrue(t.getCommandedState() == jmri.Turnout.THROWN);
    }

    // from here down is testing infrastructure
    public LnTurnoutTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {LnTurnoutTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(LnTurnoutTest.class);
        return suite;
    }

    private final static Logger log = LoggerFactory.getLogger(LnTurnoutTest.class.getName());

}
