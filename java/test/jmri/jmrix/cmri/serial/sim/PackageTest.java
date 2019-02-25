package jmri.jmrix.cmri.serial.sim;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
   ConnectionConfigTest.class,
   SimDriverAdapterTest.class,
   jmri.jmrix.cmri.serial.sim.configurexml.PackageTest.class,
   BundleTest.class
})
/**
 * Tests for the jmri.jmrix.cmri.serial.sim package.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class PackageTest {
}
