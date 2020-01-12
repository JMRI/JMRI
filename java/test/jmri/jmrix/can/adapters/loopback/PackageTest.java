package jmri.jmrix.can.adapters.loopback;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
   ConnectionConfigTest.class,
   jmri.jmrix.can.adapters.loopback.configurexml.PackageTest.class,
   LoopbackTrafficControllerTest.class,
   PortTest.class,
   BundleTest.class
})
/**
 * Tests for the jmri.jmrix.can.adapters.loopback package.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class PackageTest {
}
