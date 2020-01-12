package jmri.jmrix.loconet.locobuffer;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
   ConnectionConfigTest.class,
   jmri.jmrix.loconet.locobuffer.configurexml.PackageTest.class,
   LocoBufferAdapterTest.class,
   BundleTest.class,
})
/**
 * Tests for the jmri.jmrix.loconet.locobuffer package.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class PackageTest {
}
