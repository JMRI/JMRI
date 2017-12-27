package jmri.jmrix.nce;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        NceTurnoutTest.class,
        NceTurnoutManagerTest.class,
        NceSensorManagerTest.class,
        jmri.jmrix.nce.NceAIUTest.class,
        jmri.jmrix.nce.NceProgrammerTest.class,
        jmri.jmrix.nce.NceProgrammerManagerTest.class,
        NceTrafficControllerTest.class,
        NceSystemConnectionMemoTest.class,
        jmri.jmrix.nce.NceMessageTest.class,
        jmri.jmrix.nce.NceReplyTest.class,
        NcePowerManagerTest.class,
        BundleTest.class,
        jmri.jmrix.nce.clockmon.PackageTest.class,
        NceConsistTest.class,
        jmri.jmrix.nce.networkdriver.PackageTest.class,
        jmri.jmrix.nce.usbdriver.PackageTest.class,
        jmri.jmrix.nce.serialdriver.PackageTest.class,
        jmri.jmrix.nce.simulator.PackageTest.class,
        jmri.jmrix.nce.configurexml.PackageTest.class,
        jmri.jmrix.nce.boosterprog.PackageTest.class,
        jmri.jmrix.nce.cab.PackageTest.class,
        jmri.jmrix.nce.macro.PackageTest.class,
        jmri.jmrix.nce.usbinterface.PackageTest.class,
        jmri.jmrix.nce.ncemon.PackageTest.class,
        jmri.jmrix.nce.packetgen.PackageTest.class,
        NceNetworkPortControllerTest.class,
        NcePortControllerTest.class,
        jmri.jmrix.nce.swing.PackageTest.class,
        jmri.jmrix.nce.consist.PackageTest.class,
        NceBinaryCommandTest.class,
        NceCmdStationMemoryTest.class,
        NceConnectionTypeListTest.class,
        NceMessageCheckTest.class,
        NceUSBTest.class,
        NceAIUCheckerTest.class,
        NceClockControlTest.class,
        NceConnectionStatusTest.class,
        NceConsistManagerTest.class,
        NceLightManagerTest.class,
        NceLightTest.class,
        NceMenuTest.class,
        NceOpsModeProgrammerTest.class,
        NceSensorTest.class,
        NceThrottleManagerTest.class,
        NceThrottleTest.class,
        NceTurnoutMonitorTest.class,
})

/**
 * tests for the jmri.jmrix.nce package
 *
 * @author	Bob Jacobsen
 */
public class PackageTest  {
}
