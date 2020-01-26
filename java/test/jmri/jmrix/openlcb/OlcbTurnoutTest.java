package jmri.jmrix.openlcb;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openlcb.EventID;
import org.openlcb.implementations.EventTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.JmriException;
import jmri.Turnout;
import jmri.jmrix.can.CanMessage;
import jmri.util.JUnitUtil;
import jmri.util.NamedBeanComparator;
import jmri.util.PropertyChangeListenerScaffold;
import jmri.util.ThreadingUtil;

/**
 * Tests for the jmri.jmrix.openlcb.OlcbTurnout class.
 *
 * @author	Bob Jacobsen Copyright 2008, 2010, 2011
 */
public class OlcbTurnoutTest {
    private final static Logger log = LoggerFactory.getLogger(OlcbTurnoutTest.class);

    protected PropertyChangeListenerScaffold l; 

    @Test
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
      
        JUnitUtil.waitFor( () -> { return l.getPropertyChanged(); });
        Assert.assertEquals("called twice",2,l.getCallCount());
        Assert.assertTrue(s.getCommandedState() == Turnout.THROWN);

        l.resetPropertyChanged();
        t.sendMessage(mInactive);
        JUnitUtil.waitFor( () -> { return l.getPropertyChanged(); });
        Assert.assertEquals("called twice",2,l.getCallCount());
        Assert.assertTrue(s.getCommandedState() == Turnout.CLOSED);
    }

    @Test
    public void testLocalChange() throws jmri.JmriException {
        // load dummy TrafficController
        OlcbTurnout s = new OlcbTurnout("M", "1.2.3.4.5.6.7.8;1.2.3.4.5.6.7.9", t.iface);
        s.finishLoad();

        s.addPropertyChangeListener(l);

        t.flush();
        t.tc.rcvMessage = null;
        s.setState(Turnout.THROWN);
        t.flush();
        JUnitUtil.waitFor( () -> { return l.getPropertyChanged(); });
        Assert.assertEquals("called twice",2,l.getCallCount());
        Assert.assertTrue(s.getCommandedState() == Turnout.THROWN);
        log.debug("recv msg: " + t.tc.rcvMessage + " header " + Integer.toHexString(t.tc.rcvMessage.getHeader()));
        Assert.assertTrue(new OlcbAddress("1.2.3.4.5.6.7.8").match(t.tc.rcvMessage));

        l.resetPropertyChanged();
        t.tc.rcvMessage = null;
        s.setState(Turnout.CLOSED);
        t.flush();
        JUnitUtil.waitFor( () -> { return l.getPropertyChanged(); });
        Assert.assertEquals("called twice",2,l.getCallCount());
        Assert.assertTrue(s.getCommandedState() == Turnout.CLOSED);
        Assert.assertTrue(new OlcbAddress("1.2.3.4.5.6.7.9").match(t.tc.rcvMessage));

        // repeated set of local state
        t.tc.rcvMessage = null;
        s.setState(Turnout.CLOSED);
        t.flush();
        JUnitUtil.waitFor( () -> { return l.getPropertyChanged(); });
        Assert.assertEquals("called twice",2,l.getCallCount());
        Assert.assertTrue(s.getCommandedState() == Turnout.CLOSED);
        Assert.assertTrue(new OlcbAddress("1.2.3.4.5.6.7.9").match(t.tc.rcvMessage));
    }

    @Test
    public void testAuthoritative() throws jmri.JmriException {
        OlcbTurnout s = new OlcbTurnout("M", "1.2.3.4.5.6.7.8;1.2.3.4.5.6.7.9", t.iface);
        s.setFeedbackMode(Turnout.MONITORING);
        s.finishLoad();

        s.setState(Turnout.THROWN);
        t.flush();

        // message for Active and Inactive
        CanMessage qActive = new CanMessage(new int[]{1, 2, 3, 4, 5, 6, 7, 8},
                0x19914123
        );
        qActive.setExtended(true);
        t.sendMessage(qActive);
        t.flush();

        CanMessage expected = new CanMessage(new int[]{1, 2, 3, 4, 5, 6, 7, 8},
                0x19544c4c);
        expected.setExtended(true);
        Assert.assertEquals(expected, t.tc.rcvMessage);
        t.tc.rcvMessage = null;

        s.setAuthoritative(false);
        s.setState(Turnout.CLOSED);
        t.flush();

        t.sendMessage(qActive);
        t.flush();
        expected = new CanMessage(new int[]{1, 2, 3, 4, 5, 6, 7, 8},
                0x19547c4c);
        expected.setExtended(true);
        Assert.assertEquals(expected, t.tc.rcvMessage);
    }

    @Test
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
        JUnitUtil.waitFor( () -> { return l.getPropertyChanged(); });
        Assert.assertEquals("called twice",2,l.getCallCount());
        Assert.assertTrue(s.getCommandedState() == Turnout.THROWN);

        l.resetPropertyChanged();
        s.setState(Turnout.CLOSED);
        t.flush();
        JUnitUtil.waitFor( () -> { return l.getPropertyChanged(); });
        Assert.assertEquals("called twice",2,l.getCallCount());
        Assert.assertTrue(s.getCommandedState() == Turnout.CLOSED);
    }

    @Test
    public void testForgetState() {
        OlcbTurnout s = new OlcbTurnout("M", "1.2.3.4.5.6.7.8;1.2.3.4.5.6.7.9", t.iface);
        s.setProperty(OlcbUtils.PROPERTY_LISTEN, Boolean.FALSE.toString());
        s.finishLoad();

        t.sendMessageAndExpectResponse(":X19914123N0102030405060708;",
                ":X19547C4CN0102030405060708;");

        Assert.assertEquals(Turnout.MONITORING, s.getFeedbackMode());
        s.setState(Turnout.THROWN);
        t.flush();
        Assert.assertEquals(Turnout.THROWN, s.getCommandedState());
        Assert.assertEquals(Turnout.THROWN, s.getKnownState());
        t.assertSentMessage(":X195B4c4cN0102030405060708;");

        s.addPropertyChangeListener(l);

        t.sendMessageAndExpectResponse(":X19914123N0102030405060708;",
                ":X19544C4CN0102030405060708;");
        // Getting a state notify will not change state now.
        t.sendMessage(":X19544123N0102030405060709;");
        Assert.assertEquals("not called",0,l.getCallCount());
        l.resetPropertyChanged();
        Assert.assertEquals(Turnout.THROWN, s.getKnownState());

        // Resets the turnout to unknown state
        s.setState(Turnout.UNKNOWN);
        JUnitUtil.waitFor( () -> { return l.getPropertyChanged(); });
        Assert.assertEquals("called twice",2,l.getCallCount());
        l.resetPropertyChanged();
        t.assertNoSentMessages();

        // state is reported as unknown to the bus
        t.sendMessageAndExpectResponse(":X19914123N0102030405060708;",
                ":X19547C4CN0102030405060708;");
        // getting a state notify will change state
        t.sendMessage(":X19544123N0102030405060709;");
        JUnitUtil.waitFor( () -> { return l.getPropertyChanged(); });
        Assert.assertEquals("called twice",2,l.getCallCount());
        l.resetPropertyChanged();
        Assert.assertEquals(Turnout.CLOSED, s.getKnownState());

        // state is reported as known (thrown==invalid)
        t.sendMessageAndExpectResponse(":X19914123N0102030405060708;",
                ":X19545C4CN0102030405060708;");

        // getting a state notify will not change state
        t.sendMessage(":X19544123N0102030405060708;");
        Assert.assertEquals("not called",0,l.getCallCount());
        l.resetPropertyChanged();
        Assert.assertEquals(Turnout.CLOSED, s.getKnownState());
    }

    @Test
    public void testQueryState() {
        OlcbTurnout s = new OlcbTurnout("M", "1.2.3.4.5.6.7.8;1.2.3.4.5.6.7.9", t.iface);
        s.finishLoad();

        t.tc.rcvMessage = null;
        s.requestUpdateFromLayout();
        t.flush();
        t.assertSentMessage(":X198F4C4CN0102030405060708;");

        s.setFeedbackMode(Turnout.DIRECT);
        t.flush();
        t.tc.rcvMessage = null;
        s.requestUpdateFromLayout();
        t.flush();
        t.assertNoSentMessages();
    }

    /**
     * In this test we simulate the following scenario: A turnout R that is being changed locally
     * by JMRI (e.g. due to a panel icon action), which triggers a Logix, and in that Logix there
     * is an action that sets a second turnout U.
     * We check that the messages sent to the layout are in the order of T:=Active, U:=Active.
    */
    @Test
    public void testListenerOutOfOrder() throws JmriException {
        final OlcbTurnout r = new OlcbTurnout("M", "1.2.3.4.5.6.7.8;1.2.3.4.5.6.7.9", t.iface);
        final OlcbTurnout u = new OlcbTurnout("M", "1.2.3.4.5.6.7.a;1.2.3.4.5.6.7.b", t.iface);
        r.finishLoad();
        u.finishLoad();
        r.setCommandedState(Turnout.CLOSED);
        u.setCommandedState(Turnout.CLOSED);

        t.clearSentMessages();

        r.addPropertyChangeListener("KnownState", new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                Assert.assertEquals(Turnout.THROWN, r.getKnownState());
                u.setCommandedState(Turnout.THROWN);
            }
        });

        ThreadingUtil.runOnLayout(new ThreadingUtil.ThreadAction() {
            @Override
            public void run() {
                r.setCommandedState(Turnout.THROWN);
            }
        });

        Assert.assertEquals(Turnout.THROWN, r.getKnownState());
        Assert.assertEquals(Turnout.THROWN, u.getKnownState());

        // Ensures that the last sent message is U==Active. Particularly important that it is NOT
        // the message ending with 0708.
        t.assertSentMessage(":X195B4C4CN010203040506070A;");
    }


    @Test
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

    @Test
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

    @Test
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

    @Test
    public void testSystemSpecificComparisonOfSpecificFormats() {

        // test by putting into a tree set, then extracting and checking order
        TreeSet<Turnout> set = new TreeSet<>(new NamedBeanComparator<>());
        
        set.add(new OlcbTurnout("M", "1.2.3.4.5.6.7.8;1.2.3.4.5.6.7.9", t.iface));
        set.add(new OlcbTurnout("M", "X0501010114FF2000;X0501010114FF2011", t.iface));
        set.add(new OlcbTurnout("M", "X0501010114FF2000;X0501010114FF2001", t.iface));
        set.add(new OlcbTurnout("M", "1.2.3.4.5.6.7.9;1.2.3.4.5.6.7.9", t.iface));
        
        Iterator<Turnout> it = set.iterator();
        
        Assert.assertEquals("MT1.2.3.4.5.6.7.8;1.2.3.4.5.6.7.9", it.next().getSystemName());
        Assert.assertEquals("MT1.2.3.4.5.6.7.9;1.2.3.4.5.6.7.9", it.next().getSystemName());
        Assert.assertEquals("MTX0501010114FF2000;X0501010114FF2001", it.next().getSystemName());
        Assert.assertEquals("MTX0501010114FF2000;X0501010114FF2011", it.next().getSystemName());
    }

    private OlcbTestInterface t;

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        l = new PropertyChangeListenerScaffold();

        // load dummy TrafficController
        t = new OlcbTestInterface();
        t.waitForStartup();
    }

    @After
    public void tearDown() {
        l = null;
        t.dispose();
        t = null;
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }
}
