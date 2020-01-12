package jmri.jmrix.can.adapters.lawicell.canusb.serialdriver;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
   ConnectionConfigTest.class,
   jmri.jmrix.can.adapters.lawicell.canusb.serialdriver.configurexml.PackageTest.class,
   CanUsbDriverAdapterTest.class,
   BundleTest.class,
})
/**
 * Tests for the jmri.jmrix.can.adatpers.lawicell.canusb.serialdriver package.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class PackageTest {
}
