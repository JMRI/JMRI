package jmri.jmrix.jmriclient;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        JMRIClientMessageTest.class,
        JMRIClientReplyTest.class,
        JMRIClientTurnoutTest.class,
        JMRIClientSensorTest.class,
        JMRIClientReporterTest.class,
        JMRIClientTurnoutManagerTest.class,
        JMRIClientSensorManagerTest.class,
        JMRIClientReporterManagerTest.class,
        JMRIClientTrafficControllerTest.class,
        JMRIClientSystemConnectionMemoTest.class,
        JMRIClientPowerManagerTest.class,
        BundleTest.class,
        jmri.jmrix.jmriclient.networkdriver.PackageTest.class,
        jmri.jmrix.jmriclient.configurexml.PackageTest.class,
        jmri.jmrix.jmriclient.json.PackageTest.class,
        jmri.jmrix.jmriclient.swing.PackageTest.class,
        JMRIClientPortControllerTest.class,
        JMRIClientConnectionTypeListTest.class,
        JMRIClientLightManagerTest.class,
        JMRIClientLightTest.class,
})

/**
 * Tests for the jmri.jmrix.jmriclient package
 *
 * @author	Bob Jacobsen
 */
public class PackageTest  {
}
