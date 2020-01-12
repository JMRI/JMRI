package jmri.jmrix.loconet.locobufferii;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
   ConnectionConfigTest.class,
   jmri.jmrix.loconet.locobufferii.configurexml.PackageTest.class,
   LocoBufferIIAdapterTest.class,
   BundleTest.class,
})
/**
 * Tests for the jmri.jmrix.loconet.locobufferii package.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class PackageTest {
}
