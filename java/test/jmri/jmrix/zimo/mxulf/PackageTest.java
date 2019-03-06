package jmri.jmrix.zimo.mxulf;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
   ConnectionConfigTest.class,
   SerialDriverAdapterTest.class,
   jmri.jmrix.zimo.mxulf.configurexml.PackageTest.class,
   BundleTest.class,
})
/**
 * Tests for the jmri.jmrix.zimo.mxulf package.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class PackageTest {
}
