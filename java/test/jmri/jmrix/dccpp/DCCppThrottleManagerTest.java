package jmri.jmrix.dccpp;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * DCCppThrottleManagerTest.java
 * <p>
 * Test for the jmri.jmrix.dccpp.DCCppThrottleManager class
 *
 * @author Paul Bender
 * @author Mark Underwood (C) 2015
 */
public class DCCppThrottleManagerTest extends jmri.managers.AbstractThrottleManagerTestBase {

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        DCCppCommandStation cs = new DCCppCommandStation();
        cs.setCommandStationMaxNumSlots(12); // the "traditional" value for DCC++
        DCCppInterfaceScaffold tc = new DCCppInterfaceScaffold(cs);
        tm = new DCCppThrottleManager(new DCCppSystemConnectionMemo(tc));
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.resetWindows(false, false);
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }

}
