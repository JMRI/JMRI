package jmri.jmrix.can;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        jmri.jmrix.can.CanMessageTest.class,
        jmri.jmrix.can.CanReplyTest.class,
        jmri.jmrix.can.nmranet.PackageTest.class,
        jmri.jmrix.can.adapters.PackageTest.class,
        jmri.jmrix.can.swing.PackageTest.class,
        jmri.jmrix.can.cbus.PackageTest.class,
        AbstractCanTrafficControllerTest.class,
        TrafficControllerTest.class,
        CanConnectionTypeListTest.class,
        CanConstantsTest.class,
        CanSystemConnectionMemoTest.class,
        CanConfigurationManagerTest.class,
        BundleTest.class,
})

/**
 * Tests for the jmri.jmrix.can package.
 *
 * @author Bob Jacobsen Copyright 2008
 */
public class PackageTest  {
}
