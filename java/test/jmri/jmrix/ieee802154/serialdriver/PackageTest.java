package jmri.jmrix.ieee802154.serialdriver;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        SerialSystemConnectionMemoTest.class,
        SerialTrafficControllerTest.class,
        SerialNodeTest.class,
        SerialDriverAdapterTest.class,
        ConnectionConfigTest.class,
        jmri.jmrix.ieee802154.serialdriver.configurexml.PackageTest.class,
})

/**
 * Tests for the jmri.jmrix.ieee802154.serialdriver package
 *
 * @author	Paul Bender
  */
public class PackageTest  {
}
