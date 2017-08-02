package jmri.jmrix.sprog.pi.pisprogone;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * tests for the jmri.jmrix.sprog.pi.pisprogone package
 *
 * @author  Paul Bender Copyright (C) 2016	
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
   PiSprogOneSerialDriverAdapterTest.class,
   ConnectionConfigTest.class,
   jmri.jmrix.sprog.pi.pisprogone.configurexml.PackageTest.class,
   BundleTest.class
})
public class PackageTest {
}
