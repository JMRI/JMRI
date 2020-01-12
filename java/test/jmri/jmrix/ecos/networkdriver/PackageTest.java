package jmri.jmrix.ecos.networkdriver;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
   ConnectionConfigTest.class,
   jmri.jmrix.ecos.networkdriver.configurexml.PackageTest.class,
   NetworkDriverAdapterTest.class,
   BundleTest.class
})
/**
 * Tests for the jmri.jmrix.ecos.networkdriver package.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class PackageTest {
}
