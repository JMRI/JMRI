package jmri.jmrix.ieee802154;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        IEEE802154MessageTest.class,
        IEEE802154ReplyTest.class,
        IEEE802154SystemConnectionMemoTest.class,
        IEEE802154TrafficControllerTest.class,
        jmri.jmrix.ieee802154.xbee.PackageTest.class,
        jmri.jmrix.ieee802154.serialdriver.PackageTest.class,
        IEEE802154NodeTest.class,
        BundleTest.class,
        jmri.jmrix.ieee802154.swing.PackageTest.class,
        IEEE802154PortControllerTest.class,
        SerialConnectionTypeListTest.class,
})

/**
 * Tests for the jmri.jmrix.ieee802154 package
 *
 * @author	Paul Bender
  */
public class PackageTest  {
}
