package jmri.jmrix.can.adapters.gridconnect.canusb.serialdriver;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
   ConnectionConfigTest.class,
   SerialDriverAdapterTest.class,
   BundleTest.class,
   jmri.jmrix.can.adapters.gridconnect.canusb.serialdriver.configurexml.PackageTest.class
})
/**
 * Tests for the jmri.jmrix.can.adapters.gridconnect.canusb.serialdriver package.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class PackageTest {
}
