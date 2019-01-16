package jmri.jmrix.zimo.mx1;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
   ConnectionConfigTest.class,
   jmri.jmrix.zimo.mx1.configurexml.PackageTest.class,
   Mx1AdapterTest.class,
   BundleTest.class
})
/**
 * Tests for the jmri.jmrix.zimo.mx1 package.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class PackageTest {
}
