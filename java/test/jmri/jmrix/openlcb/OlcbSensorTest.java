package jmri.jmrix.openlcb;

import java.util.regex.Pattern;
import jmri.Sensor;
import jmri.jmrix.can.CanMessage;
import jmri.util.JUnitUtil;
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

    public void testIncomingChange() {
        Assert.assertNotNull("exists", t);
        OlcbSensor s = new OlcbSensor("M", "1.2.3.4.5.6.7.8;1.2.3.4.5.6.7.9", t.iface);

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

        // wait for twice timeout to make sure
        try {
            Thread.sleep(2 * OlcbSensor.ON_TIME);
        } catch (Exception e) {
        }

        Assert.assertEquals(Sensor.INACTIVE, s.getKnownState());

        // local flip
        s.setKnownState(Sensor.ACTIVE);
        Assert.assertTrue(s.getKnownState() == Sensor.ACTIVE);

        // wait for twice timeout to make sure
        try {
            Thread.sleep(2 * OlcbSensor.ON_TIME);
        } catch (Exception e) {
        }

        Assert.assertEquals(Sensor.INACTIVE, s.getKnownState());
    }

    public void testLocalChange() throws jmri.JmriException {
        OlcbSensor s = new OlcbSensor("M", "1.2.3.4.5.6.7.8;1.2.3.4.5.6.7.9", t.iface);
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

    public void testEventTable() {
        OlcbSensor s = new OlcbSensor("M", "1.2.3.4.5.6.7.8;1.2.3.4.5.6.7.9", t.iface);

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
        java.util.TreeSet<Sensor> set = new java.util.TreeSet(new jmri.util.NamedBeanComparator());
        
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
