package jmri.jmrix.ecos;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        BundleTest.class,
        jmri.jmrix.ecos.swing.PackageTest.class,
        jmri.jmrix.ecos.networkdriver.PackageTest.class,
        jmri.jmrix.ecos.configurexml.PackageTest.class,
        EcosPreferencesTest.class,
        EcosSystemConnectionMemoTest.class,
        jmri.jmrix.ecos.utilities.PackageTest.class,
        EcosReporterManagerTest.class,
        EcosTrafficControllerTest.class,
        EcosSensorManagerTest.class,
        EcosTurnoutManagerTest.class,
        EcosPortControllerTest.class,
        EcosConnectionTypeListTest.class,
        EcosMessageTest.class,
        EcosReplyTest.class,
        EcosDccThrottleManagerTest.class,
        EcosDccThrottleTest.class,
        EcosLocoAddressManagerTest.class,
        EcosLocoAddressTest.class,
        EcosPowerManagerTest.class,
        EcosProgrammerManagerTest.class,
        EcosProgrammerTest.class,
        EcosReporterTest.class,
        EcosSensorTest.class,
        EcosTurnoutTest.class,
})

/**
 * Tests for the jmri.jmrix.ecos package
 *
 * @author	Bob Jacobsen
 */
public class PackageTest  {
}
