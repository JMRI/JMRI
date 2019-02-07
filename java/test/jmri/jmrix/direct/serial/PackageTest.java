package jmri.jmrix.direct.serial;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
   ConnectionConfigTest.class,
   SerialDriverAdapterTest.class,
   jmri.jmrix.direct.serial.configurexml.PackageTest.class,
   BundleTest.class
})
/**
 * Tests for the jmri.jmrix.direct.serial package.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class PackageTest {
}
