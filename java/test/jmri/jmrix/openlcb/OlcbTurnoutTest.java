package jmri.jmrix.openlcb;

import jmri.Turnout;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.TestTrafficController;
import jmri.jmrix.can.adapters.loopback.LoopbackTrafficController;

import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * Tests for the jmri.jmrix.openlcb.OlcbTurnout class.
 *
 * @author	Bob Jacobsen Copyright 2008, 2010, 2011
 */
public class OlcbTurnoutTest extends TestCase {
    private final static Logger log = LoggerFactory.getLogger(OlcbTurnoutTest.class.getName());

    interface MockablePropertyChangeListener {
        void onChange(String property, Object newValue);
    }
    class FPropertyChangeListener implements PropertyChangeListener {
        MockablePropertyChangeListener m;
        FPropertyChangeListener() {
            m = mock(MockablePropertyChangeListener.class);
        }

        @Override
        public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
            m.onChange(propertyChangeEvent.getPropertyName(), propertyChangeEvent.getNewValue());
        }
    }

    protected FPropertyChangeListener l = new FPropertyChangeListener();

    private static final String COMMANDED_STATE = "CommandedState";
    private static final String KNOWN_STATE = "KnownState";
    public void testIncomingChange() {
        Assert.assertNotNull("exists", t);
        OlcbTurnout s = new OlcbTurnout("MT", "1.2.3.4.5.6.7.8;1.2.3.4.5.6.7.9", t.iface);
        s.finishLoad();

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

        s.addPropertyChangeListener(l);

        // check states
        Assert.assertTrue(s.getCommandedState() == Turnout.UNKNOWN);

        t.sendMessage(mActive);

        verify(l.m).onChange(COMMANDED_STATE, Turnout.THROWN);
        verify(l.m).onChange(KNOWN_STATE, Turnout.THROWN);
        verifyNoMoreInteractions(l.m);
        Assert.assertTrue(s.getCommandedState() == Turnout.THROWN);

        t.sendMessage(mInactive);
        verify(l.m).onChange(COMMANDED_STATE, Turnout.CLOSED);
        verify(l.m).onChange(KNOWN_STATE, Turnout.CLOSED);
        verifyNoMoreInteractions(l.m);
        Assert.assertTrue(s.getCommandedState() == Turnout.CLOSED);
    }

    public void testLocalChange() throws jmri.JmriException {
        // load dummy TrafficController
        OlcbTurnout s = new OlcbTurnout("MT", "1.2.3.4.5.6.7.8;1.2.3.4.5.6.7.9", t.iface);
        s.finishLoad();

        s.addPropertyChangeListener(l);

        t.flush();
        t.tc.rcvMessage = null;
        s.setState(Turnout.THROWN);
        t.flush();
        verify(l.m).onChange(COMMANDED_STATE, Turnout.THROWN);
        verify(l.m).onChange(KNOWN_STATE, Turnout.THROWN);
        verifyNoMoreInteractions(l.m);
        Assert.assertTrue(s.getCommandedState() == Turnout.THROWN);
        log.debug("recv msg: " + t.tc.rcvMessage + " header " + Integer.toHexString(t.tc.rcvMessage.getHeader()));
        Assert.assertTrue(new OlcbAddress("1.2.3.4.5.6.7.8").match(t.tc.rcvMessage));

        t.tc.rcvMessage = null;
        s.setState(Turnout.CLOSED);
        t.flush();
        verify(l.m).onChange(COMMANDED_STATE, Turnout.CLOSED);
        verify(l.m).onChange(KNOWN_STATE, Turnout.CLOSED);
        verifyNoMoreInteractions(l.m);
        Assert.assertTrue(s.getCommandedState() == Turnout.CLOSED);
        Assert.assertTrue(new OlcbAddress("1.2.3.4.5.6.7.9").match(t.tc.rcvMessage));
    }

    public void testDirectFeedback() throws jmri.JmriException {
        OlcbTurnout s = new OlcbTurnout("MT", "1.2.3.4.5.6.7.8;1.2.3.4.5.6.7.9", t.iface);
        s.setFeedbackMode(Turnout.DIRECT);
        s.finishLoad();

        s.addPropertyChangeListener(l);

        s.setState(Turnout.THROWN);
        t.flush();
        verify(l.m).onChange(COMMANDED_STATE, Turnout.THROWN);
        verify(l.m).onChange(KNOWN_STATE, Turnout.THROWN);

        Assert.assertEquals(Turnout.THROWN, s.getCommandedState());
        Assert.assertEquals(Turnout.THROWN, s.getKnownState());

        s.setState(Turnout.CLOSED);
        t.flush();
        verify(l.m).onChange(COMMANDED_STATE, Turnout.CLOSED);
        verify(l.m).onChange(KNOWN_STATE, Turnout.CLOSED);

        Assert.assertEquals(Turnout.CLOSED, s.getCommandedState());
        Assert.assertEquals(Turnout.CLOSED, s.getKnownState());

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
        t.sendMessage(mActive);
        Assert.assertEquals(Turnout.CLOSED, s.getCommandedState());
        Assert.assertEquals(Turnout.CLOSED, s.getKnownState());
        verifyNoMoreInteractions(l.m);

        t.sendMessage(mInactive);
        Assert.assertEquals(Turnout.CLOSED, s.getCommandedState());
        Assert.assertEquals(Turnout.CLOSED, s.getKnownState());
        verifyNoMoreInteractions(l.m);
    }

    public void testLoopback() throws jmri.JmriException {
        // Two turnouts behaving in opposite ways. One will be used to generate an event and the
        // other will be observed to make sure it catches it.
        OlcbTurnout s = new OlcbTurnout("MT", "1.2.3.4.5.6.7.8;1.2.3.4.5.6.7.9", t.iface);
        s.finishLoad();
        OlcbTurnout r = new OlcbTurnout("MT", "1.2.3.4.5.6.7.9;1.2.3.4.5.6.7.8", t.iface);
        r.finishLoad();

        r.addPropertyChangeListener(l);

        s.setState(Turnout.THROWN);
        t.flush();
        verify(l.m).onChange(COMMANDED_STATE, Turnout.CLOSED);
        verify(l.m).onChange(KNOWN_STATE, Turnout.CLOSED);
        verifyNoMoreInteractions(l.m);
        Assert.assertTrue(s.getCommandedState() == Turnout.THROWN);

        s.setState(Turnout.CLOSED);
        t.flush();
        verify(l.m).onChange(COMMANDED_STATE, Turnout.THROWN);
        verify(l.m).onChange(KNOWN_STATE, Turnout.THROWN);
        verifyNoMoreInteractions(l.m);
        Assert.assertTrue(s.getCommandedState() == Turnout.CLOSED);
    }

    public void testNameFormatXlower() {
        // load dummy TrafficController
        OlcbTurnout s = new OlcbTurnout("MT", "x0501010114FF2000;x0501010114FF2001", t.iface);
        s.finishLoad();
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

        t.sendMessage(mActive);
        Assert.assertTrue(s.getCommandedState() == Turnout.THROWN);

        t.sendMessage(mInactive);
        Assert.assertTrue(s.getCommandedState() == Turnout.CLOSED);

    }

    public void testNameFormatXupper() {
        // load dummy TrafficController
        OlcbTurnout s = new OlcbTurnout("MT", "X0501010114FF2000;X0501010114FF2001", t.iface);
        s.finishLoad();
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

        t.sendMessage(mActive);
        Assert.assertTrue(s.getCommandedState() == Turnout.THROWN);

        t.sendMessage(mInactive);
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

    OlcbTestInterface t;

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();

        // load dummy TrafficController
        t = new OlcbTestInterface();
        t.waitForStartup();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }
}
