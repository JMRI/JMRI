package jmri.jmrix.openlcb;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import jmri.util.JUnitUtil;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.regex.Pattern;
import jmri.Turnout;
import jmri.jmrix.can.CanMessage;
import jmri.util.MockPropertyChangeListener;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.Assert;
import org.openlcb.EventID;
import org.openlcb.implementations.EventTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * Tests for the jmri.jmrix.openlcb.OlcbTurnout class.
 *
 * @author	Bob Jacobsen Copyright 2008, 2010, 2011
 */
public class OlcbTurnoutTest extends TestCase {
    private final static Logger log = LoggerFactory.getLogger(OlcbTurnoutTest.class);

    protected MockPropertyChangeListener l = new MockPropertyChangeListener();

    private static final String COMMANDED_STATE = "CommandedState";
    private static final String KNOWN_STATE = "KnownState";
    public void testIncomingChange() {
        Assert.assertNotNull("exists", t);
        OlcbTurnout s = new OlcbTurnout("M", "1.2.3.4.5.6.7.8;1.2.3.4.5.6.7.9", t.iface);
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
        OlcbTurnout s = new OlcbTurnout("M", "1.2.3.4.5.6.7.8;1.2.3.4.5.6.7.9", t.iface);
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

        // repeated set of local state
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
        OlcbTurnout s = new OlcbTurnout("M", "1.2.3.4.5.6.7.8;1.2.3.4.5.6.7.9", t.iface);
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
        OlcbTurnout s = new OlcbTurnout("M", "1.2.3.4.5.6.7.8;1.2.3.4.5.6.7.9", t.iface);
        s.finishLoad();
        OlcbTurnout r = new OlcbTurnout("M", "1.2.3.4.5.6.7.9;1.2.3.4.5.6.7.8", t.iface);
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

    public void testEventTable() {
        OlcbTurnout s = new OlcbTurnout("M", "1.2.3.4.5.6.7.8;1.2.3.4.5.6.7.9", t.iface);
        s.finishLoad();

        EventTable.EventTableEntry[] elist = t.iface.getEventTable()
                .getEventInfo(new EventID("1.2.3.4.5.6.7.8")).getAllEntries();

        Assert.assertEquals(1, elist.length);
        Assert.assertTrue("Incorrect name: " + elist[0].getDescription(), Pattern.compile("Turnout.*Thrown").matcher(elist[0].getDescription()).matches());

        s.setUserName("MySwitch");

        elist = t.iface.getEventTable()
                .getEventInfo(new EventID("1.2.3.4.5.6.7.8")).getAllEntries();

        Assert.assertEquals(1, elist.length);
        Assert.assertEquals("Turnout MySwitch Thrown", elist[0].getDescription());

        elist = t.iface.getEventTable()
                .getEventInfo(new EventID("1.2.3.4.5.6.7.9")).getAllEntries();

        Assert.assertEquals(1, elist.length);
        Assert.assertEquals("Turnout MySwitch Closed", elist[0].getDescription());
    }

    public void testNameFormatXlower() {
        // load dummy TrafficController
        OlcbTurnout s = new OlcbTurnout("M", "x0501010114FF2000;x0501010114FF2001", t.iface);
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
        OlcbTurnout s = new OlcbTurnout("M", "X0501010114FF2000;X0501010114FF2001", t.iface);
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

    public void testSystemSpecificComparisonOfSpecificFormats() {

        // test by putting into a tree set, then extracting and checking order
        java.util.TreeSet<Turnout> set = new java.util.TreeSet(new jmri.util.NamedBeanComparator());
        
        set.add(new OlcbTurnout("M", "1.2.3.4.5.6.7.8;1.2.3.4.5.6.7.9", t.iface));
        set.add(new OlcbTurnout("M", "X0501010114FF2000;X0501010114FF2011", t.iface));
        set.add(new OlcbTurnout("M", "X0501010114FF2000;X0501010114FF2001", t.iface));
        set.add(new OlcbTurnout("M", "1.2.3.4.5.6.7.9;1.2.3.4.5.6.7.9", t.iface));
        
        java.util.Iterator<Turnout> it = set.iterator();
        
        Assert.assertEquals("MT1.2.3.4.5.6.7.8;1.2.3.4.5.6.7.9", it.next().getSystemName());
        Assert.assertEquals("MT1.2.3.4.5.6.7.9;1.2.3.4.5.6.7.9", it.next().getSystemName());
        Assert.assertEquals("MTX0501010114FF2000;X0501010114FF2001", it.next().getSystemName());
        Assert.assertEquals("MTX0501010114FF2000;X0501010114FF2011", it.next().getSystemName());
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
    @Override
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();

        // load dummy TrafficController
        t = new OlcbTestInterface();
        t.waitForStartup();
    }

    @Override
    protected void tearDown() {
        JUnitUtil.tearDown();
    }
}
