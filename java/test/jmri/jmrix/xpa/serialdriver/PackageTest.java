package jmri.jmrix.xpa.serialdriver;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
   ConnectionConfigTest.class,
   SerialDriverAdapterTest.class,
   jmri.jmrix.xpa.serialdriver.configurexml.PackageTest.class,
   BundleTest.class
})
/**
 * Tests for the jmri.jmrix.xpa.serialdriver package.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class PackageTest {
}
