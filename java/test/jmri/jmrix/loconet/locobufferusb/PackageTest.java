package jmri.jmrix.loconet.locobufferusb;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
   ConnectionConfigTest.class,
   jmri.jmrix.loconet.locobufferusb.configurexml.PackageTest.class,
   LocoBufferUsbAdapterTest.class,
   BundleTest.class,
})
/**
 * Tests for the jmri.jmrix.loconet.locobufferusb package.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class PackageTest {
}
