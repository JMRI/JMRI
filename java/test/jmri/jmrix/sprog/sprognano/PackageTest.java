package jmri.jmrix.sprog.sprognano;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * tests for the jmri.jmrix.sprog.sprognano package
 *
 * @author  Paul Bender Copyright (C) 2016	
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
   SprogNanoSerialDriverAdapterTest.class,
   ConnectionConfigTest.class,
   jmri.jmrix.sprog.sprognano.configurexml.PackageTest.class,
   BundleTest.class
})
public class PackageTest {
}
