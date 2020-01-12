package jmri.jmrix.loconet.streamport;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
   LnStreamPortPacketizerTest.class,
   LnStreamPortControllerTest.class,
   BundleTest.class,
   LnStreamConnectionConfigTest.class,
   jmri.jmrix.loconet.streamport.configurexml.PackageTest.class,
})

/**
 * Tests for the jmri.jmrix.loconet.streamport package.
 *
 * @author Paul Bender Copyright (C) 2018
 */
public class PackageTest  {
}
