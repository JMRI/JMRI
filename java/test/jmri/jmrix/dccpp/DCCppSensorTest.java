package jmri.jmrix.dccpp;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * DCCppSensorTest.java
 * <p>
 * Test for the jmri.jmrix.dccpp.DCCppSensor class
 *
 * @author Bob Jacobsen
 * @author Mark Underwood
 * @author Paul Bender Copyright (C) 2018
 */
public class DCCppSensorTest extends jmri.implementation.AbstractSensorTestBase {

    @Override
    public int numListeners() {
        return 0;
    }

    @Override
    public void checkActiveMsgSent() {
    }

    @Override
    public void checkInactiveMsgSent() {
    }

    @Override
    public void checkStatusRequestMsgSent() {
    }

    private DCCppInterfaceScaffold xnis = null;

    // DCCppSensor test for incoming status message
    @Test
    public void testDCCppSensorStatusMsg() {
        DCCppReply m;

        // Verify this was created in UNKNOWN state
        Assert.assertTrue(t.getKnownState() == jmri.Sensor.UNKNOWN);

        // notify the Sensor that somebody else changed it...
        m = DCCppReply.parseDCCppReply("Q 4");
        ((DCCppSensor) t).message(m);
        Assert.assertEquals("Known state after activate ", jmri.Sensor.ACTIVE, t.getKnownState());

        m = DCCppReply.parseDCCppReply("q 4");
        ((DCCppSensor) t).message(m);

        Assert.assertEquals("Known state after inactivate ", jmri.Sensor.INACTIVE, t.getKnownState());

    }

    // DCCppSensor test for incoming status message
    @Test
    public void testDCCppSensorInvertStatusMsg() {
        DCCppReply m;

        // Verify this was created in UNKNOWN state
        Assert.assertTrue(t.getKnownState() == jmri.Sensor.UNKNOWN);

        // Set the inverted flag
        t.setInverted(true);

        // notify the Sensor that somebody else changed it...
        m = DCCppReply.parseDCCppReply("Q 4");
        ((DCCppSensor) t).message(m);
        Assert.assertEquals("Known state after activate ", jmri.Sensor.INACTIVE, t.getKnownState());

        m = DCCppReply.parseDCCppReply("q 4");
        ((DCCppSensor) t).message(m);

        Assert.assertEquals("Known state after inactivate ", jmri.Sensor.ACTIVE, t.getKnownState());

    }

    @Test
    public void testIncomingReplyGivesItsReceivedTime() {
        java.util.List<Long> times = new java.util.ArrayList<>();
        java.util.List<Integer> states = new java.util.ArrayList<>();
        t.addRawStateListener((sensor, oldState, newState, timeNanos) -> {
            states.add(newState);
            times.add(timeNanos);
        });

        DCCppReply rActive = DCCppReply.parseDCCppReply("Q 4");
        rActive.setReceivedTime(1234L);
        ((DCCppSensor) t).message(rActive);

        long before = System.nanoTime();
        ((DCCppSensor) t).message(DCCppReply.parseDCCppReply("q 4")); // not stamped, no time recorded
        long after = System.nanoTime();

        Assert.assertEquals(java.util.List.of(jmri.Sensor.ACTIVE, jmri.Sensor.INACTIVE), states);
        Assert.assertEquals("the time the reply was received", 1234L, (long) times.get(0));
        Assert.assertTrue("not stamped: dated when set", times.get(1) >= before && times.get(1) <= after);
    }

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        xnis = new DCCppInterfaceScaffold(new DCCppCommandStation());
        t = new DCCppSensor("DCCPPS04", xnis);
    }

    @Override
    @AfterEach
    public void tearDown() {
        t.dispose();
        xnis = null;
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }

}
