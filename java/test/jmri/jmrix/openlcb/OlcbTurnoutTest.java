package jmri.jmrix.openlcb;

import jmri.Turnout;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.TestTrafficController;
import jmri.jmrix.can.adapters.loopback.LoopbackTrafficController;

import org.junit.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Tests for the jmri.jmrix.openlcb.OlcbTurnout class.
 *
 * @author	Bob Jacobsen Copyright 2008, 2010, 2011
 */
public class OlcbTurnoutTest extends TestCase {

    public class FakePropertyChangeListener implements PropertyChangeListener {
        private String property;
        public int eventCount;
        private int expectedCount;
        private Object expectedValue;
        public FakePropertyChangeListener(String property) {
            this.property = property;
            eventCount = 0;
            expectedValue = null;
            expectedCount = 0;
        }

        public void expectChange(Object newValue, int count) {
            verifyExpectations();
            expectedValue = newValue;
            expectedCount += count;
        }
        public void expectChange(Object newValue) {
            expectChange(newValue, 1);
        }

        public void verifyExpectations() {
            Assert.assertEquals(property + ": expected count mismatch. last expected change: " +
                    (expectedValue != null ? expectedValue.toString() : "null") + ". ",
                    expectedCount, eventCount);
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (!evt.getPropertyName().equals(property)) {
                return;
            }
            Assert.assertTrue("Unexpected property change for " + property, eventCount <
                    expectedCount);
            ++eventCount;
            if (expectedValue != null) {
                Assert.assertEquals(evt.getNewValue(), expectedValue);
            }
        }
    }

    private static final String COMMANDED_STATE = "CommandedState";
    private static final String KNOWN_STATE = "KnownState";
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

        FakePropertyChangeListener commandedListener = new FakePropertyChangeListener(COMMANDED_STATE);
        s.addPropertyChangeListener(commandedListener);
        FakePropertyChangeListener knownListener = new FakePropertyChangeListener(KNOWN_STATE);
        s.addPropertyChangeListener(knownListener);

        // check states
        Assert.assertTrue(s.getCommandedState() == Turnout.UNKNOWN);

        commandedListener.expectChange(Turnout.THROWN);
        knownListener.expectChange(Turnout.THROWN);
        s.message(mActive);
        Assert.assertTrue(s.getCommandedState() == Turnout.THROWN);

        commandedListener.expectChange(Turnout.CLOSED);
        knownListener.expectChange(Turnout.CLOSED);
        s.message(mInactive);
        Assert.assertTrue(s.getCommandedState() == Turnout.CLOSED);

        commandedListener.verifyExpectations();
        knownListener.verifyExpectations();
    }

    public void testLocalChange() throws jmri.JmriException {
        // load dummy TrafficController
        TestTrafficController t = new TestTrafficController();

        OlcbTurnout s = new OlcbTurnout("MT", "1.2.3.4.5.6.7.8;1.2.3.4.5.6.7.9", t);

        FakePropertyChangeListener knownListener = new FakePropertyChangeListener(KNOWN_STATE);
        s.addPropertyChangeListener(knownListener);
        FakePropertyChangeListener commandedListener = new FakePropertyChangeListener(COMMANDED_STATE);
        s.addPropertyChangeListener(commandedListener);

        t.rcvMessage = null;
        knownListener.expectChange(Turnout.THROWN);
        commandedListener.expectChange(Turnout.THROWN);
        s.setState(Turnout.THROWN);
        Assert.assertTrue(s.getCommandedState() == Turnout.THROWN);
        Assert.assertTrue(new OlcbAddress("1.2.3.4.5.6.7.8").match(t.rcvMessage));

        t.rcvMessage = null;
        knownListener.expectChange(Turnout.CLOSED);
        commandedListener.expectChange(Turnout.CLOSED);
        s.setState(Turnout.CLOSED);
        Assert.assertTrue(s.getCommandedState() == Turnout.CLOSED);
        Assert.assertTrue(new OlcbAddress("1.2.3.4.5.6.7.9").match(t.rcvMessage));

        knownListener.verifyExpectations();
    }

    public void testDirectFeedback() throws jmri.JmriException {
        // load dummy TrafficController
        TestTrafficController t = new TestTrafficController();

        OlcbTurnout s = new OlcbTurnout("MT", "1.2.3.4.5.6.7.8;1.2.3.4.5.6.7.9", t);
        s.setFeedbackMode(Turnout.DIRECT);

        FakePropertyChangeListener knownListener = new FakePropertyChangeListener(KNOWN_STATE);
        s.addPropertyChangeListener(knownListener);

        knownListener.expectChange(Turnout.THROWN);
        s.setState(Turnout.THROWN);
        Assert.assertEquals(Turnout.THROWN, s.getCommandedState());
        Assert.assertEquals(Turnout.THROWN, s.getKnownState());

        knownListener.expectChange(Turnout.CLOSED);
        s.setState(Turnout.CLOSED);
        Assert.assertEquals(Turnout.CLOSED, s.getCommandedState());
        Assert.assertEquals(Turnout.CLOSED, s.getKnownState());

        knownListener.verifyExpectations();

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

        //  Feedback is ignored. Neither known nor commanded state changes.
        s.message(mActive);
        Assert.assertEquals(Turnout.CLOSED, s.getCommandedState());
        Assert.assertEquals(Turnout.CLOSED, s.getKnownState());

        s.message(mInactive);
        Assert.assertEquals(Turnout.CLOSED, s.getCommandedState());
        Assert.assertEquals(Turnout.CLOSED, s.getKnownState());

        knownListener.verifyExpectations();
    }

    public void testLoopback() throws jmri.JmriException {
        // need a real TrafficController here to loopback the CAN messages to ourselves.
        LoopbackTrafficController t = new LoopbackTrafficController();

        // Two turnouts behaving in opposite ways. One will be used to generate an event and the
        // other will be observed to make sure it catches it.
        OlcbTurnout s = new OlcbTurnout("MT", "1.2.3.4.5.6.7.8;1.2.3.4.5.6.7.9", t);
        OlcbTurnout r = new OlcbTurnout("MT", "1.2.3.4.5.6.7.9;1.2.3.4.5.6.7.8", t);

        FakePropertyChangeListener knownListener = new FakePropertyChangeListener(KNOWN_STATE);
        r.addPropertyChangeListener(knownListener);

        knownListener.expectChange(Turnout.CLOSED);
        s.setState(Turnout.THROWN);
        Assert.assertTrue(s.getCommandedState() == Turnout.THROWN);

        knownListener.expectChange(Turnout.THROWN);
        s.setState(Turnout.CLOSED);
        Assert.assertTrue(s.getCommandedState() == Turnout.CLOSED);

        knownListener.verifyExpectations();
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
