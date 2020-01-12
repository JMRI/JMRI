package jmri.jmrix.bachrus.serialdriver;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
   ConnectionConfigTest.class,
   SerialDriverAdapterTest.class,
   jmri.jmrix.bachrus.serialdriver.configurexml.PackageTest.class,
   BundleTest.class
})
/**
 * Tests for the jmri.jmrix.bachrus.serialdriver package.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class PackageTest {
}
