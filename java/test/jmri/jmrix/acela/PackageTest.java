package jmri.jmrix.acela;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        AcelaNodeTest.class,
        AcelaLightManagerTest.class,
        AcelaLightTest.class,
        AcelaTurnoutManagerTest.class,
        AcelaTurnoutTest.class,
        jmri.jmrix.acela.configurexml.PackageTest.class,
        jmri.jmrix.acela.serialdriver.PackageTest.class,
        jmri.jmrix.acela.nodeconfig.PackageTest.class,
        jmri.jmrix.acela.acelamon.PackageTest.class,
        jmri.jmrix.acela.packetgen.PackageTest.class,
        AcelaSystemConnectionMemoTest.class,
        AcelaPortControllerTest.class,
        AcelaTrafficControllerTest.class,
        AcelaAddressTest.class,
        AcelaConnectionTypeListTest.class,
        AcelaMessageTest.class,
        AcelaReplyTest.class,
        jmri.jmrix.acela.swing.PackageTest.class,
        AcelaMenuTest.class,
        AcelaSensorManagerTest.class,
        AcelaSensorTest.class,
        AcelaSignalHeadTest.class,
        BundleTest.class,
})

/**
 * Tests for the jmri.jmrix.acela package
 *
 * @author	Bob Coleman
 */
public class PackageTest  {
}
