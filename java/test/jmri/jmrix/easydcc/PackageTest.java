package jmri.jmrix.easydcc;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        EasyDccTurnoutTest.class,
        EasyDccTurnoutManagerTest.class,
        jmri.jmrix.easydcc.EasyDccProgrammerTest.class,
        EasyDccTrafficControllerTest.class,
        jmri.jmrix.easydcc.EasyDccMessageTest.class,
        jmri.jmrix.easydcc.EasyDccReplyTest.class,
        EasyDccPowerManagerTest.class,
        EasyDccConsistTest.class,
        EasyDccConsistManagerTest.class,
        jmri.jmrix.easydcc.serialdriver.PackageTest.class,
        jmri.jmrix.easydcc.simulator.PackageTest.class,
        jmri.jmrix.easydcc.networkdriver.PackageTest.class,
        jmri.jmrix.easydcc.configurexml.PackageTest.class,
        jmri.jmrix.easydcc.easydccmon.PackageTest.class,
        jmri.jmrix.easydcc.packetgen.PackageTest.class,
        EasyDccNetworkPortControllerTest.class,
        EasyDccSystemConnectionMemoTest.class,
        EasyDccPortControllerTest.class,
        EasyDccMenuTest.class,
        EasyDccConnectionTypeListTest.class,
        EasyDccCommandStationTest.class,
        EasyDccOpsModeProgrammerTest.class,
        EasyDccProgrammerManagerTest.class,
        EasyDccThrottleManagerTest.class,
        EasyDccThrottleTest.class,
        BundleTest.class,
        jmri.jmrix.easydcc.swing.PackageTest.class,
})

/**
 * Tests for the jmri.jmrix.easydcc package
 *
 * @author	Bob Jacobsen
 */
public class PackageTest  {
}
