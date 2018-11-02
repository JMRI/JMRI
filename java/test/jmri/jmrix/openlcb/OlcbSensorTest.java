package jmri.jmrix.openlcb;

import java.util.regex.Pattern;

import jmri.JmriException;
import jmri.Sensor;
import jmri.Turnout;
import jmri.jmrix.can.CanMessage;
import jmri.util.JUnitUtil;
import jmri.util.PropertyChangeListenerScaffold;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.openlcb.EventID;
import org.openlcb.implementations.EventTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the jmri.jmrix.openlcb.OlcbSensor class.
 *
 * @author	Bob Jacobsen Copyright 2008, 2010
 */
public class OlcbSensorTest extends jmri.implementation.AbstractSensorTestBase {

    @Rule
    public jmri.util.junit.rules.RetryRule retryRule = new jmri.util.junit.rules.RetryRule(3);  // allow 3 retries of tests

    private final static Logger log = LoggerFactory.getLogger(OlcbSensorTest.class);
    protected PropertyChangeListenerScaffold l; 

    @Override
    public int numListeners() {return 0;}

    @Override
    public void checkOnMsgSent() {
        Assert.assertTrue(new OlcbAddress("1.2.3.4.5.6.7.8").match(ti.tc.rcvMessage));
    }

    @Override
    public void checkOffMsgSent() {
        Assert.assertTrue(new OlcbAddress("1.2.3.4.5.6.7.9").match(ti.tc.rcvMessage));
    }
        
    @Override
    public void checkStatusRequestMsgSent() {
        ti.flush();
        ti.assertSentMessage(":X198F4C4CN0102030405060708;");
    }

    @Test
    public void testIncomingChange() {
        // message for Active and Inactive
        CanMessage mActive = new CanMessage(
                new int[]{1, 2, 3, 4, 5, 6, 7, 8},
                0x195B4123
        );
        mActive.setExtended(true);

        CanMessage mInactive = new CanMessage(
                new int[]{1, 2, 3, 4, 5, 6, 7, 9},
                0x195B4123
        );
        mInactive.setExtended(true);

        // check states
        Assert.assertTrue(t.getKnownState() == Sensor.UNKNOWN);

        ti.sendMessage(mActive);
        Assert.assertTrue(t.getKnownState() == Sensor.ACTIVE);

        ti.sendMessage(mInactive);
        Assert.assertTrue(t.getKnownState() == Sensor.INACTIVE);

    }

    @Test
    public void testRecoverUponStartup() {
        ti.flush();

        Assert.assertNotNull(ti.tc.rcvMessage);
        log.debug("recv msg: " + ti.tc.rcvMessage + " header " + Integer.toHexString(ti.tc.rcvMessage.getHeader()));
        CanMessage expected = new CanMessage(new byte[]{1,2,3,4,5,6,7,8}, 0x198F4C4C);
        expected.setExtended(true);
        Assert.assertEquals(expected, ti.tc.rcvMessage);


        // message for Active and Inactive
        CanMessage mStateActive = new CanMessage(
                new int[]{1, 2, 3, 4, 5, 6, 7, 8},
                0x194C4123
        );
        mStateActive.setExtended(true);

        CanMessage mStateInactive = new CanMessage(
                new int[]{1, 2, 3, 4, 5, 6, 7, 9},
                0x194C4123
        );
        mStateInactive.setExtended(true);

        // check states
        Assert.assertTrue(t.getKnownState() == Sensor.UNKNOWN);

        ti.sendMessage(mStateActive);
        Assert.assertTrue(t.getKnownState() == Sensor.ACTIVE);

        ti.sendMessage(mStateInactive);
        Assert.assertTrue(t.getKnownState() == Sensor.INACTIVE);
    }

    @Test
    public void testMomentarySensor() throws Exception {
        OlcbSensor s = new OlcbSensor("M", "1.2.3.4.5.6.7.8", ti.iface);
        s.finishLoad();
    	    // message for Active and Inactive
        CanMessage mActive = new CanMessage(
                new int[]{1, 2, 3, 4, 5, 6, 7, 8},
                0x195B4123
        );
        mActive.setExtended(true);

        // check states
        Assert.assertTrue(s.getKnownState() == Sensor.UNKNOWN);

        ti.sendMessage(mActive);
        Assert.assertTrue(s.getKnownState() == Sensor.ACTIVE);

        JUnitUtil.waitFor( ()->{ return(s.getKnownState() != Sensor.ACTIVE); });

        Assert.assertEquals(Sensor.INACTIVE, s.getKnownState());

        // local flip
        s.setKnownState(Sensor.ACTIVE);
        Assert.assertTrue(s.getKnownState() == Sensor.ACTIVE);

        JUnitUtil.waitFor( ()->{ return(s.getKnownState() != Sensor.ACTIVE); });

        Assert.assertEquals(Sensor.INACTIVE, s.getKnownState());
    }

    @Test
    public void testLocalChange() throws jmri.JmriException {
        ti.tc.rcvMessage = null;
        t.setKnownState(Sensor.ACTIVE);
        Assert.assertEquals(Sensor.ACTIVE, t.getKnownState());
        ti.flush();
        Assert.assertNotNull(ti.tc.rcvMessage);
        log.debug("recv msg: " + ti.tc.rcvMessage + " header " + Integer.toHexString(ti.tc.rcvMessage.getHeader()));
        checkOnMsgSent();
        ti.tc.rcvMessage = null;
        t.setKnownState(Sensor.INACTIVE);
        Assert.assertEquals(Sensor.INACTIVE, t.getKnownState());
        ti.flush();
        checkOffMsgSent();

        // Repeat send
        ti.tc.rcvMessage = null;
        t.setKnownState(Sensor.INACTIVE);
        Assert.assertEquals(Sensor.INACTIVE, t.getKnownState());
        ti.flush();
        checkOffMsgSent();
    }

    @Test
    public void testAuthoritative() throws jmri.JmriException {
        t.setState(Sensor.ACTIVE);
        ti.flush();

        // message for Active and Inactive
        CanMessage qActive = new CanMessage(new int[]{1, 2, 3, 4, 5, 6, 7, 8},
                0x19914123
        );
        qActive.setExtended(true);
        ti.sendMessage(qActive);
        ti.flush();

        CanMessage expected = new CanMessage(new int[]{1, 2, 3, 4, 5, 6, 7, 8},
                0x19544c4c);
        expected.setExtended(true);
        Assert.assertEquals(expected, ti.tc.rcvMessage);
        ti.tc.rcvMessage = null;

        ((OlcbSensor)t).setAuthoritative(false);
        t.setState(Sensor.INACTIVE);
        ti.flush();

        ti.sendMessage(qActive);
        ti.flush();
        expected = new CanMessage(new int[]{1, 2, 3, 4, 5, 6, 7, 8},
                0x19547c4c);
        expected.setExtended(true);
        Assert.assertEquals(expected, ti.tc.rcvMessage);
    }
    
    @Test
    public void testForgetState() throws JmriException {
	t.dispose(); // dispose of the existing sensor.
        OlcbSensor s = new OlcbSensor("M", "1.2.3.4.5.6.7.8;1.2.3.4.5.6.7.9", ti.iface);
        s.setProperty(OlcbUtils.PROPERTY_LISTEN, Boolean.FALSE.toString());
        s.finishLoad();

	t = s;  // give t a value so the test teardown functions.
	ti.flush();
        ti.tc.rcvMessage = null;

        ti.sendMessageAndExpectResponse(":X19914123N0102030405060708;",
                ":X19547C4CN0102030405060708;");

        s.setKnownState(Sensor.ACTIVE);
        ti.flush();
        Assert.assertEquals(Sensor.ACTIVE, s.getKnownState());
        ti.assertSentMessage(":X195B4c4cN0102030405060708;");

        s.addPropertyChangeListener(l);

        ti.sendMessageAndExpectResponse(":X19914123N0102030405060708;",
                ":X19544C4CN0102030405060708;");
        // Getting a state notify will not change state now.
        ti.sendMessage(":X19544123N0102030405060709;");
        Assert.assertEquals("no call",0,l.getCallCount());
        l.resetPropertyChanged();
        Assert.assertEquals(Sensor.ACTIVE, s.getKnownState());

        // Resets the turnout to unknown state
        s.setState(Sensor.UNKNOWN);
        JUnitUtil.waitFor( () -> { return l.getPropertyChanged(); });
        Assert.assertEquals("called once",1,l.getCallCount());
        l.resetPropertyChanged();
        ti.assertNoSentMessages();

        // state is reported as unknown to the bus
        ti.sendMessageAndExpectResponse(":X19914123N0102030405060708;",
                ":X19547C4CN0102030405060708;");
        // getting a state notify will change state
        ti.sendMessage(":X19544123N0102030405060709;");
        JUnitUtil.waitFor( () -> { return l.getPropertyChanged(); });
        Assert.assertEquals("called once",1,l.getCallCount());
        l.resetPropertyChanged();
        Assert.assertEquals(Sensor.INACTIVE, s.getKnownState());

        // state is reported as known (thrown==invalid)
        ti.sendMessageAndExpectResponse(":X19914123N0102030405060708;",
                ":X19545C4CN0102030405060708;");

        // getting a state notify will not change state
        ti.sendMessage(":X19544123N0102030405060708;");
        Assert.assertEquals("no call",0,l.getCallCount());
        l.resetPropertyChanged();
        Assert.assertEquals(Sensor.INACTIVE, s.getKnownState());
    }

    @Test
    public void testEventTable() {
        EventTable.EventTableEntry[] elist = ti.iface.getEventTable()
                .getEventInfo(new EventID("1.2.3.4.5.6.7.8")).getAllEntries();

        Assert.assertEquals(1, elist.length);
        Assert.assertTrue("Incorrect name: " + elist[0].getDescription(), Pattern.compile("Sensor.*Active").matcher(elist[0].getDescription()).matches());

        t.setUserName("MyInput");

        elist = ti.iface.getEventTable()
                .getEventInfo(new EventID("1.2.3.4.5.6.7.8")).getAllEntries();

        Assert.assertEquals(1, elist.length);
        Assert.assertEquals("Sensor MyInput Active", elist[0].getDescription());

        elist = ti.iface.getEventTable()
                .getEventInfo(new EventID("1.2.3.4.5.6.7.9")).getAllEntries();

        Assert.assertEquals(1, elist.length);
        Assert.assertEquals("Sensor MyInput Inactive", elist[0].getDescription());
    }

    @Test
    public void testSystemSpecificComparisonOfSpecificFormats() {

        // test by putting into a tree set, then extracting and checking order
        java.util.TreeSet<Sensor> set = new java.util.TreeSet<>(new jmri.util.NamedBeanComparator());
        
        set.add(new OlcbSensor("M", "1.2.3.4.5.6.7.8;1.2.3.4.5.6.7.9", ti.iface));
        set.add(new OlcbSensor("M", "X0501010114FF2000;X0501010114FF2011", ti.iface));
        set.add(new OlcbSensor("M", "X0501010114FF2000;X0501010114FF2001", ti.iface));
        set.add(new OlcbSensor("M", "1.2.3.4.5.6.7.9;1.2.3.4.5.6.7.9", ti.iface));
        
        java.util.Iterator<Sensor> it = set.iterator();
        
        Assert.assertEquals("MS1.2.3.4.5.6.7.8;1.2.3.4.5.6.7.9", it.next().getSystemName());
        Assert.assertEquals("MS1.2.3.4.5.6.7.9;1.2.3.4.5.6.7.9", it.next().getSystemName());
        Assert.assertEquals("MSX0501010114FF2000;X0501010114FF2001", it.next().getSystemName());
        Assert.assertEquals("MSX0501010114FF2000;X0501010114FF2011", it.next().getSystemName());
    }

    OlcbTestInterface ti;

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        l = new PropertyChangeListenerScaffold();
        // load dummy TrafficController
        ti = new OlcbTestInterface();
        ti.waitForStartup();
        t = new OlcbSensor("M", "1.2.3.4.5.6.7.8;1.2.3.4.5.6.7.9", ti.iface);
        ((OlcbSensor)t).finishLoad();
    }

    @After
    public void tearDown() {
	t.dispose();
        l.resetPropertyChanged();
	l = null;
        ti.dispose();
        JUnitUtil.tearDown();
    }
}
