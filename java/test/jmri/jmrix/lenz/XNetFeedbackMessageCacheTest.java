package jmri.jmrix.lenz;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jmri.Sensor;
import jmri.Turnout;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class XNetFeedbackMessageCacheTest {

    @Test
    public void testCTor() {
        jmri.jmrix.lenz.XNetInterfaceScaffold tc = new jmri.jmrix.lenz.XNetInterfaceScaffold(new jmri.jmrix.lenz.LenzCommandStation());
        new jmri.jmrix.lenz.XNetSystemConnectionMemo(tc);
        XNetFeedbackMessageCache t = new XNetFeedbackMessageCache(tc);
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testCachedMixedBroadcastInitializesTurnoutsAndSensors() {
        XNetInterfaceScaffold tc = new XNetInterfaceScaffold(new LenzCommandStation());
        try {
            XNetReply reply = new XNetReply("48 05 09 05 16 06 45 06 5A 00");
            reply.setParity();
            assertTrue(reply.checkParity());
            tc.getFeedbackMessageCache().message(reply);
            int[] turnoutStates = {Turnout.CLOSED, Turnout.THROWN, Turnout.THROWN, Turnout.CLOSED};
            for (int i = 0; i < turnoutStates.length; i++) {
                XNetTurnout turnout = new XNetTurnout("X", 21 + i, tc);
                try {
                    assertEquals(turnoutStates[i], turnout.getKnownState(), turnout.getSystemName());
                } finally {
                    turnout.dispose();
                }
            }
            for (int i = 0; i < 8; i++) {
                XNetSensor sensor = new XNetSensor("XS" + (49 + i), tc, "X");
                try {
                    int expected = ((i < 4 ? 0x05 : 0x0A) & (1 << (i % 4))) != 0
                            ? Sensor.ACTIVE : Sensor.INACTIVE;
                    JUnitUtil.waitFor(() -> sensor.getKnownState() == expected, sensor.getSystemName());
                    assertEquals(expected, sensor.getKnownState(), sensor.getSystemName());
                } finally {
                    sensor.dispose();
                }
            }
            assertTrue(tc.outbound.isEmpty(), "cached feedback avoids layout requests");
        } finally {
            tc.terminateThreads();
        }
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }

    // private static final Logger log = LoggerFactory.getLogger(XNetFeedbackMessageCacheTest.class);

}
