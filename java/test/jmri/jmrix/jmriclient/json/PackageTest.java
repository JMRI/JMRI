package jmri.jmrix.jmriclient.json;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        BundleTest.class,
        jmri.jmrix.jmriclient.json.swing.PackageTest.class,
        jmri.jmrix.jmriclient.json.configurexml.PackageTest.class,
        JsonNetworkConnectionConfigTest.class,
        JsonNetworkPortControllerTest.class,
        JsonClientTrafficControllerTest.class,
        JsonClientReplyTest.class,
        JsonClientSystemConnectionMemoTest.class,
        JsonClientLightManagerTest.class,
        JsonClientLightTest.class,
        JsonClientMessageTest.class,
        JsonClientPowerManagerTest.class,
})

/**
 * Tests for the jmri.jmrix.jmriclient package
 *
 * @author	Bob Jacobsen
 * 
 */
public class PackageTest  {
}
