package jmri.jmrix.loconet.ms100;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
   ConnectionConfigTest.class,
   jmri.jmrix.loconet.ms100.configurexml.PackageTest.class,
   MS100AdapterTest.class,
   BundleTest.class
})
/**
 * Tests for the jmri.jmrix.loconet.ms100 package.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class PackageTest {
}
