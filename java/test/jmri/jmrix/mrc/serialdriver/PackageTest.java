package jmri.jmrix.mrc.serialdriver;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
   ConnectionConfigTest.class,
   SerialDriverAdapterTest.class,
   jmri.jmrix.mrc.serialdriver.configurexml.PackageTest.class,
        BundleTest.class,
})
/**
 * Tests for the jmri.jmrix.mrc.serialdriver package.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class PackageTest {
}
