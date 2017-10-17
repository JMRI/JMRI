package jmri.jmrix.sprog.pi.pisprognano;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * tests for the jmri.jmrix.sprog.pi.pisprognano package
 *
 * @author  Paul Bender Copyright (C) 2016	
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
   PiSprogNanoSerialDriverAdapterTest.class,
   ConnectionConfigTest.class,
   jmri.jmrix.sprog.pi.pisprognano.configurexml.PackageTest.class,
   BundleTest.class
})
public class PackageTest {
}
