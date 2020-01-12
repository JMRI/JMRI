package jmri.jmrix.dccpp.serial;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
   ConnectionConfigTest.class,
   jmri.jmrix.dccpp.serial.configurexml.PackageTest.class,
   SerialDCCppPacketizerTest.class,
   DCCppAdapterTest.class,
   BundleTest.class
})
/**
 * Tests for the jmri.jmrix.dccpp.serial package.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class PackageTest {
}
