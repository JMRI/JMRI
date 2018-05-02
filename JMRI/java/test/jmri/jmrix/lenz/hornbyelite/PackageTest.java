package jmri.jmrix.lenz.hornbyelite;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        HornbyEliteCommandStationTest.class,
        EliteAdapterTest.class,
        EliteConnectionTypeListTest.class,
        EliteXNetInitializationManagerTest.class,
        EliteXNetThrottleManagerTest.class,
        EliteXNetThrottleTest.class,
        EliteXNetTurnoutTest.class,
        EliteXNetTurnoutManagerTest.class,
        EliteXNetProgrammerTest.class,
        ConnectionConfigTest.class,
        jmri.jmrix.lenz.hornbyelite.configurexml.PackageTest.class,
        EliteXNetSystemConnectionMemoTest.class,
        BundleTest.class,
})

/**
 * Tests for the jmri.jmrix.lenz.hornbyelite package
 *
 * @author Paul Bender
 */
public class PackageTest  {
}
