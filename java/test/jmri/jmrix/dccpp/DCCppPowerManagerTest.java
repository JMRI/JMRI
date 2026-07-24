package jmri.jmrix.dccpp;

import jmri.JmriException;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

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
    // TODO : Different DCC-EX versions ?

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
