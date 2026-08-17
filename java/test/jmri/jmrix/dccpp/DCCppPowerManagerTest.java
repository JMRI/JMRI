package jmri.jmrix.dccpp;

import jmri.JmriException;
import jmri.PowerManager;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * DCCppPowerManagerTest.java
 *
 * Test for the jmri.jmrix.dccpp.DCCppPowerManager class
 *
 * @author Paul Bender
 * @author Mark Underwood (C) 2015
 * Based on XNetPowerManagerTest
 */
public class DCCppPowerManagerTest {

    // TODO : Extend from AbstractPowerManagerTestBase
    // TODO : Test command station messages ( containing power status? )
    // TODO : Test trackmanager responses

    @Test
    public void testCtor() {
        assertNotNull(pwr, "DccppPowerManager created");
        assertEquals(2, tc.outbound.size(),"Request messages sent");
        assertEquals("Status Cmd ", tc.outbound.get(0).toMonitorString());
        assertEquals("Request TrackManager Config: '='", tc.outbound.get(1).toMonitorString());
    }

    @Test
    public void testMsgsSentOnRequestUpdate() {
        assertNotNull(pwr);
        tc.outbound.clear();
        pwr.requestUpdateFromLayout();
        assertEquals(2, tc.outbound.size());
        assertEquals("Status Cmd ", tc.outbound.get(0).toMonitorString());
        assertEquals("Request TrackManager Config: '='", tc.outbound.get(1).toMonitorString());
    }

    @Test
    public void testSendOn() {
        assertNotNull(pwr, "DccppPowerManager created");
        tc.outbound.clear();

        assertDoesNotThrow( () -> { pwr.setPower(PowerManager.ON); },
            "Does not throw set ON");
        assertEquals(3, tc.outbound.size(),"ON + Request messages sent");
        assertEquals("Track Power ON Cmd ", tc.outbound.get(0).toMonitorString());
        assertEquals("Status Cmd ", tc.outbound.get(1).toMonitorString());
        assertEquals("Request TrackManager Config: '='", tc.outbound.get(2).toMonitorString());
    }

    @Test
    public void testSendOff() {
        assertNotNull(pwr, "DccppPowerManager created");
        tc.outbound.clear();

        assertDoesNotThrow( () -> { pwr.setPower(PowerManager.OFF); },
            "Does not throw set OFF");
        assertEquals(3, tc.outbound.size(),"OFF + Request messages sent");
        assertEquals("Track Power OFF Cmd ", tc.outbound.get(0).toMonitorString());
        assertEquals("Status Cmd ", tc.outbound.get(1).toMonitorString());
        assertEquals("Request TrackManager Config: '='", tc.outbound.get(2).toMonitorString());
    }

    @Test
    public void testPowerOnOffOnReply() {
        assertNotNull(pwr, "DccppPowerManager created");
        tc.outbound.clear();
        assertEquals(PowerManager.UNKNOWN, pwr.getPower(), "starts unknown");

        DCCppReply r = DCCppReply.parseDCCppReply("p1");
        pwr.message(r);
        assertEquals(PowerManager.ON, pwr.getPower(), "power ON notification");

        r = DCCppReply.parseDCCppReply("p0");
        pwr.message(r);
        assertEquals(PowerManager.OFF, pwr.getPower(), "power OFF notification");

        r = DCCppReply.parseDCCppReply("p1");
        pwr.message(r);
        assertEquals(PowerManager.ON, pwr.getPower(), "power back ON notification");
    }

    private DCCppPowerManager pwr; // local copy of DCCppPowerManager
    private DCCppInterfaceScaffold tc;

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        tc = new DCCppInterfaceScaffold(new DCCppCommandStation());
        pwr = new DCCppPowerManager(new DCCppSystemConnectionMemo(tc));
        tc.getCommandStation().setCommandStationInfo(DCCppReply.parseDCCppReply(
                "iDCC++BASE STATION FOR ARDUINO MEGA / ARDUINO MOTOR SHIELD: BUILD 24 Nov 2015 23:59:59"));
    }

    @AfterEach
    public void tearDown() throws JmriException {
        pwr.dispose();
        pwr = null;
        tc.terminateThreads();
        tc = null;
        JUnitUtil.tearDown();
    }

}
