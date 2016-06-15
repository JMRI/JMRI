package jmri.jmrix.openlcb;

import jmri.Turnout;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.TestTrafficController;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.openlcb.OlcbTurnout class.
 *
 * @author	Bob Jacobsen Copyright 2008, 2010, 2011
 */
public class OlcbTurnoutTest extends TestCase {

    public void testIncomingChange() {
        // load dummy TrafficController
        TestTrafficController t = new TestTrafficController();
        Assert.assertNotNull("exists", t);
        OlcbTurnout s = new OlcbTurnout("MT", "1.2.3.4.5.6.7.8;1.2.3.4.5.6.7.9", t);

        // message for Active and Inactive
        CanMessage mActive = new CanMessage(
                new int[]{1, 2, 3, 4, 5, 6, 7, 8},
                0x195B4000
        );
        mActive.setExtended(true);

        CanMessage mInactive = new CanMessage(
                new int[]{1, 2, 3, 4, 5, 6, 7, 9},
                0x195B4000
        );
        mInactive.setExtended(true);

        // check states
        Assert.assertTrue(s.getCommandedState() == Turnout.UNKNOWN);

        s.message(mActive);
        Assert.assertTrue(s.getCommandedState() == Turnout.THROWN);

        s.message(mInactive);
        Assert.assertTrue(s.getCommandedState() == Turnout.CLOSED);

    }

    public void testLocalChange() throws jmri.JmriException {
        // load dummy TrafficController
        TestTrafficController t = new TestTrafficController();

        OlcbTurnout s = new OlcbTurnout("MT", "1.2.3.4.5.6.7.8;1.2.3.4.5.6.7.9", t);
        t.rcvMessage = null;
        s.setState(Turnout.THROWN);
        Assert.assertTrue(s.getCommandedState() == Turnout.THROWN);
        Assert.assertTrue(new OlcbAddress("1.2.3.4.5.6.7.8").match(t.rcvMessage));

        t.rcvMessage = null;
        s.setState(Turnout.CLOSED);
        Assert.assertTrue(s.getCommandedState() == Turnout.CLOSED);
        Assert.assertTrue(new OlcbAddress("1.2.3.4.5.6.7.9").match(t.rcvMessage));
    }

    public void testNameFormatXlower() {
        // load dummy TrafficController
        TestTrafficController t = new TestTrafficController();
        Assert.assertNotNull("tc exists", t);
        OlcbTurnout s = new OlcbTurnout("MT", "x0501010114FF2000;x0501010114FF2001", t);
        Assert.assertNotNull("to exists", s);

        // message for Active and Inactive
        CanMessage mActive = new CanMessage(
                new int[]{0x05, 0x01, 0x01, 0x01, 0x14, 0xFF, 0x20, 0x00},
                0x195B4000
        );
        mActive.setExtended(true);

        CanMessage mInactive = new CanMessage(
                new int[]{0x05, 0x01, 0x01, 0x01, 0x14, 0xFF, 0x20, 0x01},
                0x195B4000
        );
        mInactive.setExtended(true);

        // check states
        Assert.assertTrue(s.getCommandedState() == Turnout.UNKNOWN);

        s.message(mActive);
        Assert.assertTrue(s.getCommandedState() == Turnout.THROWN);

        s.message(mInactive);
        Assert.assertTrue(s.getCommandedState() == Turnout.CLOSED);

    }

    public void testNameFormatXupper() {
        // load dummy TrafficController
        TestTrafficController t = new TestTrafficController();
        Assert.assertNotNull("tc exists", t);
        OlcbTurnout s = new OlcbTurnout("MT", "X0501010114FF2000;X0501010114FF2001", t);
        Assert.assertNotNull("to exists", s);

        // message for Active and Inactive
        CanMessage mActive = new CanMessage(
                new int[]{0x05, 0x01, 0x01, 0x01, 0x14, 0xFF, 0x20, 0x00},
                0x195B4000
        );
        mActive.setExtended(true);

        CanMessage mInactive = new CanMessage(
                new int[]{0x05, 0x01, 0x01, 0x01, 0x14, 0xFF, 0x20, 0x01},
                0x195B4000
        );
        mInactive.setExtended(true);

        // check states
        Assert.assertTrue(s.getCommandedState() == Turnout.UNKNOWN);

        s.message(mActive);
        Assert.assertTrue(s.getCommandedState() == Turnout.THROWN);

        s.message(mInactive);
        Assert.assertTrue(s.getCommandedState() == Turnout.CLOSED);

    }

    // from here down is testing infrastructure
    public OlcbTurnoutTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {OlcbTurnoutTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(OlcbTurnoutTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }
}
