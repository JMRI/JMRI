package jmri.jmrix.tmcc;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        SerialTurnoutTest.class,
        SerialTurnoutManagerTest.class,
        SerialMessageTest.class,
        SerialReplyTest.class,
        SerialTrafficControllerTest.class,
        jmri.jmrix.tmcc.serialdriver.PackageTest.class,
        jmri.jmrix.tmcc.simulator.PackageTest.class,
        jmri.jmrix.tmcc.configurexml.PackageTest.class,
        jmri.jmrix.tmcc.packetgen.PackageTest.class,
        TmccMenuTest.class,
        jmri.jmrix.tmcc.serialmon.PackageTest.class,
        TmccSystemConnectionMemoTest.class,
        SerialPortControllerTest.class,
        SerialConnectionTypeListTest.class,
        SerialThrottleManagerTest.class,
        SerialThrottleTest.class,
        BundleTest.class,
        jmri.jmrix.tmcc.swing.PackageTest.class,
})

/**
 * Tests for the jmri.jmrix.tmcc package.
 *
 * @author Bob Jacobsen Copyright 2003
 */
public class PackageTest  {
}
