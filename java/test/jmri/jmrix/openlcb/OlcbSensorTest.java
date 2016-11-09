package jmri.jmrix.openlcb;

import jmri.Sensor;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.TestTrafficController;
import org.junit.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the jmri.jmrix.openlcb.OlcbSensor class.
 *
 * @author	Bob Jacobsen Copyright 2008, 2010
 */
public class OlcbSensorTest extends TestCase {
    private final static Logger log = LoggerFactory.getLogger(OlcbSensorTest.class.getName());

    public void testIncomingChange() {
        // load dummy TrafficController
        OlcbTestInterface t = new OlcbTestInterface();
        t.waitForStartup();
        Assert.assertNotNull("exists", t);
        OlcbSensor s = new OlcbSensor("MS", "1.2.3.4.5.6.7.8;1.2.3.4.5.6.7.9", t.iface);

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

    public void testMomentarySensor() throws Exception {
        // load dummy TrafficController
        OlcbTestInterface t = new OlcbTestInterface();
        t.waitForStartup();
        Assert.assertNotNull("exists", t);
        OlcbSensor s = new OlcbSensor("MS", "1.2.3.4.5.6.7.8", t.iface);

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
        // load dummy TrafficController
        OlcbTestInterface t = new OlcbTestInterface();
        OlcbSensor s = new OlcbSensor("MS", "1.2.3.4.5.6.7.8;1.2.3.4.5.6.7.9", t.iface);
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
    }

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
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }
}
