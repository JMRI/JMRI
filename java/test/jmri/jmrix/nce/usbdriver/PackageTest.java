package jmri.jmrix.nce.usbdriver;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
   ConnectionConfigTest.class,
   jmri.jmrix.nce.usbdriver.configurexml.PackageTest.class,
   UsbDriverAdapterTest.class,
   BundleTest.class,
})
/**
 * Tests for the jmri.jmrix.nce.usbdriver package.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class PackageTest {
}
