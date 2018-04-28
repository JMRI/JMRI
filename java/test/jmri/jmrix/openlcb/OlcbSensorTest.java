package jmri.jmrix.openlcb;

import java.util.regex.Pattern;

import jmri.JmriException;
import jmri.Sensor;
import jmri.Turnout;
import jmri.jmrix.can.CanMessage;
import jmri.util.JUnitUtil;
import jmri.util.PropertyChangeListenerScaffold;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.Assert;
import org.openlcb.EventID;
import org.openlcb.implementations.EventTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the jmri.jmrix.openlcb.OlcbSensor class.
 *
 * @author	Bob Jacobsen Copyright 2008, 2010
 */
public class OlcbSensorTest extends TestCase {
    private final static Logger log = LoggerFactory.getLogger(OlcbSensorTest.class);
    protected PropertyChangeListenerScaffold l; 

    public void testIncomingChange() {
        Assert.assertNotNull("exists", t);
        OlcbSensor s = new OlcbSensor("M", "1.2.3.4.5.6.7.8;1.2.3.4.5.6.7.9", t.iface);
        s.finishLoad();

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
        Assert.assertTrue(s.getKnownState() == Sensor.UNKNOWN);

        t.sendMessage(mActive);
        Assert.assertTrue(s.getKnownState() == Sensor.ACTIVE);

        t.sendMessage(mInactive);
        Assert.assertTrue(s.getKnownState() == Sensor.INACTIVE);

    }

    public void testRecoverUponStartup() {
        Assert.assertNotNull("exists", t);
        t.tc.rcvMessage = null;

        OlcbSensor s = new OlcbSensor("M", "1.2.3.4.5.6.7.8;1.2.3.4.5.6.7.9", t.iface);
        s.finishLoad();
        t.flush();

        assertNotNull(t.tc.rcvMessage);
        log.debug("recv msg: " + t.tc.rcvMessage + " header " + Integer.toHexString(t.tc.rcvMessage.getHeader()));
        CanMessage expected = new CanMessage(new byte[]{1,2,3,4,5,6,7,8}, 0x198F4C4C);
        expected.setExtended(true);
        Assert.assertEquals(expected, t.tc.rcvMessage);


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
        Assert.assertTrue(s.getKnownState() == Sensor.UNKNOWN);

        t.sendMessage(mStateActive);
        Assert.assertTrue(s.getKnownState() == Sensor.ACTIVE);

        t.sendMessage(mStateInactive);
        Assert.assertTrue(s.getKnownState() == Sensor.INACTIVE);
    }

    public void testMomentarySensor() throws Exception {
        Assert.assertNotNull("exists", t);
        OlcbSensor s = new OlcbSensor("M", "1.2.3.4.5.6.7.8", t.iface);
        s.finishLoad();

        // message for Active and Inactive
        CanMessage mActive = new CanMessage(
                new int[]{1, 2, 3, 4, 5, 6, 7, 8},
                0x195B4123
        );
        mActive.setExtended(true);

        // check states
        Assert.assertTrue(s.getKnownState() == Sensor.UNKNOWN);

        t.sendMessage(mActive);
        Assert.assertTrue(s.getKnownState() == Sensor.ACTIVE);

        JUnitUtil.waitFor( ()->{ return(s.getKnownState() != Sensor.ACTIVE); });

        Assert.assertEquals(Sensor.INACTIVE, s.getKnownState());

        // local flip
        s.setKnownState(Sensor.ACTIVE);
        Assert.assertTrue(s.getKnownState() == Sensor.ACTIVE);

        JUnitUtil.waitFor( ()->{ return(s.getKnownState() != Sensor.ACTIVE); });

        Assert.assertEquals(Sensor.INACTIVE, s.getKnownState());
    }

    public void testLocalChange() throws jmri.JmriException {
        OlcbSensor s = new OlcbSensor("M", "1.2.3.4.5.6.7.8;1.2.3.4.5.6.7.9", t.iface);
        s.finishLoad();
        t.waitForStartup();

        t.tc.rcvMessage = null;
        s.setKnownState(Sensor.ACTIVE);
        Assert.assertEquals(Sensor.ACTIVE, s.getKnownState());
        t.flush();
        assertNotNull(t.tc.rcvMessage);
        log.debug("recv msg: " + t.tc.rcvMessage + " header " + Integer.toHexString(t.tc.rcvMessage.getHeader()));
        Assert.assertTrue(new OlcbAddress("1.2.3.4.5.6.7.8").match(t.tc.rcvMessage));

        t.tc.rcvMessage = null;
        s.setKnownState(Sensor.INACTIVE);
        Assert.assertEquals(Sensor.INACTIVE, s.getKnownState());
        t.flush();
        Assert.assertTrue(new OlcbAddress("1.2.3.4.5.6.7.9").match(t.tc.rcvMessage));

        // Repeat send
        t.tc.rcvMessage = null;
        s.setKnownState(Sensor.INACTIVE);
        Assert.assertEquals(Sensor.INACTIVE, s.getKnownState());
        t.flush();
        Assert.assertTrue(new OlcbAddress("1.2.3.4.5.6.7.9").match(t.tc.rcvMessage));
    }

    public void testAuthoritative() throws jmri.JmriException {
        OlcbSensor s = new OlcbSensor("M", "1.2.3.4.5.6.7.8;1.2.3.4.5.6.7.9", t.iface);
        s.finishLoad();

        s.setState(Sensor.ACTIVE);
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
        s.setState(Sensor.INACTIVE);
        t.flush();

        t.sendMessage(qActive);
        t.flush();
        expected = new CanMessage(new int[]{1, 2, 3, 4, 5, 6, 7, 8},
                0x19547c4c);
        expected.setExtended(true);
        Assert.assertEquals(expected, t.tc.rcvMessage);
    }
    
    public void testForgetState() throws JmriException {
        OlcbSensor s = new OlcbSensor("M", "1.2.3.4.5.6.7.8;1.2.3.4.5.6.7.9", t.iface);
        s.setProperty(OlcbUtils.PROPERTY_LISTEN, Boolean.FALSE.toString());
        s.finishLoad();

        t.sendMessageAndExpectResponse(":X19914123N0102030405060708;",
                ":X19547C4CN0102030405060708;");

        s.setKnownState(Sensor.ACTIVE);
        t.flush();
        Assert.assertEquals(Sensor.ACTIVE, s.getKnownState());
        t.assertSentMessage(":X195B4c4cN0102030405060708;");

        s.addPropertyChangeListener(l);

        t.sendMessageAndExpectResponse(":X19914123N0102030405060708;",
                ":X19544C4CN0102030405060708;");
        // Getting a state notify will not change state now.
        t.sendMessage(":X19544123N0102030405060709;");
        assertEquals("no call",0,l.getCallCount());
        l.resetPropertyChanged();
        assertEquals(Sensor.ACTIVE, s.getKnownState());

        // Resets the turnout to unknown state
        s.setState(Sensor.UNKNOWN);
        JUnitUtil.waitFor( () -> { return l.getPropertyChanged(); });
        assertEquals("called once",1,l.getCallCount());
        l.resetPropertyChanged();
        t.assertNoSentMessages();

        // state is reported as unknown to the bus
        t.sendMessageAndExpectResponse(":X19914123N0102030405060708;",
                ":X19547C4CN0102030405060708;");
        // getting a state notify will change state
        t.sendMessage(":X19544123N0102030405060709;");
        JUnitUtil.waitFor( () -> { return l.getPropertyChanged(); });
        assertEquals("called once",1,l.getCallCount());
        l.resetPropertyChanged();
        assertEquals(Sensor.INACTIVE, s.getKnownState());

        // state is reported as known (thrown==invalid)
        t.sendMessageAndExpectResponse(":X19914123N0102030405060708;",
                ":X19545C4CN0102030405060708;");

        // getting a state notify will not change state
        t.sendMessage(":X19544123N0102030405060708;");
        assertEquals("no call",0,l.getCallCount());
        l.resetPropertyChanged();
        assertEquals(Sensor.INACTIVE, s.getKnownState());
    }

    public void testQueryState() {
        OlcbSensor s = new OlcbSensor("M", "1.2.3.4.5.6.7.8;1.2.3.4.5.6.7.9", t.iface);
        s.finishLoad();

        t.tc.rcvMessage = null;
        s.requestUpdateFromLayout();
        t.flush();
        t.assertSentMessage(":X198F4C4CN0102030405060708;");
    }

    public void testEventTable() {
        OlcbSensor s = new OlcbSensor("M", "1.2.3.4.5.6.7.8;1.2.3.4.5.6.7.9", t.iface);
        s.finishLoad();

        EventTable.EventTableEntry[] elist = t.iface.getEventTable()
                .getEventInfo(new EventID("1.2.3.4.5.6.7.8")).getAllEntries();

        Assert.assertEquals(1, elist.length);
        Assert.assertTrue("Incorrect name: " + elist[0].getDescription(), Pattern.compile("Sensor.*Active").matcher(elist[0].getDescription()).matches());

        s.setUserName("MyInput");

        elist = t.iface.getEventTable()
                .getEventInfo(new EventID("1.2.3.4.5.6.7.8")).getAllEntries();

        Assert.assertEquals(1, elist.length);
        Assert.assertEquals("Sensor MyInput Active", elist[0].getDescription());

        elist = t.iface.getEventTable()
                .getEventInfo(new EventID("1.2.3.4.5.6.7.9")).getAllEntries();

        Assert.assertEquals(1, elist.length);
        Assert.assertEquals("Sensor MyInput Inactive", elist[0].getDescription());
    }

    public void testSystemSpecificComparisonOfSpecificFormats() {

        // test by putting into a tree set, then extracting and checking order
        java.util.TreeSet<Sensor> set = new java.util.TreeSet<>(new jmri.util.NamedBeanComparator());
        
        set.add(new OlcbSensor("M", "1.2.3.4.5.6.7.8;1.2.3.4.5.6.7.9", t.iface));
        set.add(new OlcbSensor("M", "X0501010114FF2000;X0501010114FF2011", t.iface));
        set.add(new OlcbSensor("M", "X0501010114FF2000;X0501010114FF2001", t.iface));
        set.add(new OlcbSensor("M", "1.2.3.4.5.6.7.9;1.2.3.4.5.6.7.9", t.iface));
        
        java.util.Iterator<Sensor> it = set.iterator();
        
        Assert.assertEquals("MS1.2.3.4.5.6.7.8;1.2.3.4.5.6.7.9", it.next().getSystemName());
        Assert.assertEquals("MS1.2.3.4.5.6.7.9;1.2.3.4.5.6.7.9", it.next().getSystemName());
        Assert.assertEquals("MSX0501010114FF2000;X0501010114FF2001", it.next().getSystemName());
        Assert.assertEquals("MSX0501010114FF2000;X0501010114FF2011", it.next().getSystemName());
    }

    OlcbTestInterface t;

    // from here down is testing infrastructure
    public OlcbSensorTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {OlcbSensorTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(OlcbSensorTest.class);
        return suite;
    }

    // The minimal setup for log4J
    @Override
    protected void setUp() {
        JUnitUtil.setUp();
        l = new PropertyChangeListenerScaffold();
        // load dummy TrafficController
        t = new OlcbTestInterface();
        t.waitForStartup();
    }

    @Override
    protected void tearDown() {
        JUnitUtil.tearDown();
    }
}
