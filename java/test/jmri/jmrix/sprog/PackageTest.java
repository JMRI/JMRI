package jmri.jmrix.sprog;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Tests for the jmri.jmrix.sprog package.
 *
 * @author	Bob Jacobsen
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
   SprogSystemConnectionMemoTest.class,
   SprogTrafficControllerTest.class,
   SprogMessageTest.class,
   SprogCSStreamPortControllerTest.class,
   SprogPowerManagerTest.class,
   SprogTurnoutManagerTest.class,
   SprogTurnoutTest.class,
   SprogCSTurnoutTest.class,
   SprogCommandStationTest.class,
   SprogConnectionTypeListTest.class,
   jmri.jmrix.sprog.pi.PackageTest.class,
   jmri.jmrix.sprog.serialdriver.PackageTest.class,
   jmri.jmrix.sprog.sprog.PackageTest.class,
   jmri.jmrix.sprog.sprogCS.PackageTest.class,
   jmri.jmrix.sprog.sprognano.PackageTest.class,
   jmri.jmrix.sprog.configurexml.PackageTest.class,
   jmri.jmrix.sprog.swing.PackageTest.class,
   jmri.jmrix.sprog.packetgen.PackageTest.class,
   jmri.jmrix.sprog.console.PackageTest.class,
   jmri.jmrix.sprog.sprogmon.PackageTest.class,
   jmri.jmrix.sprog.sprogslotmon.PackageTest.class,
   jmri.jmrix.sprog.simulator.PackageTest.class,
   SPROGMenuTest.class,
   SPROGCSMenuTest.class,
   SprogPortControllerTest.class,
   SprogOpsModeProgrammerTest.class,
   SprogProgrammerTest.class,
   SprogProgrammerManagerTest.class,
   SprogThrottleManagerTest.class,
   SprogCSThrottleManagerTest.class,
   SprogThrottleTest.class,
   SprogCSThrottleTest.class,
   SprogConstantsTest.class,
   SprogReplyTest.class,
   jmri.jmrix.sprog.update.PackageTest.class,
   SprogSlotTest.class,
   BundleTest.class,
   SprogCSStreamConnectionConfigTest.class,
})

public class PackageTest {
}
