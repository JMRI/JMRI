package jmri.jmrix.lenz.lzv200;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        LZV200AdapterTest.class,
        ConnectionConfigTest.class,
        jmri.jmrix.lenz.lzv200.configurexml.PackageTest.class,
        BundleTest.class,
})

/**
 * Tests for the jmri.jmrix.lenz.lzv200 package
 *
 * @author Paul Bender Copyright (C) 2019
 */
public class PackageTest  {
}
