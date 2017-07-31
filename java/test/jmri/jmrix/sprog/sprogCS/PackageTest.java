package jmri.jmrix.sprog.sprogCS;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * tests for the jmri.jmrix.sprog.sprogCS package
 *
 * @author  Paul Bender Copyright (C) 2016	
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
   SprogCSSerialDriverAdapterTest.class,
   ConnectionConfigTest.class,
   jmri.jmrix.sprog.sprogCS.configurexml.PackageTest.class,
   BundleTest.class
})
public class PackageTest {
}
