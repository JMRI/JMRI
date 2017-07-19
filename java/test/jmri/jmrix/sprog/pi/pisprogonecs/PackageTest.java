package jmri.jmrix.sprog.pi.pisprogonecs;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * tests for the jmri.jmrix.sprog.pi.pisprogonecs package
 *
 * @author  Paul Bender Copyright (C) 2016	
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
   PiSprogOneCSSerialDriverAdapterTest.class,
   ConnectionConfigTest.class,
   jmri.jmrix.sprog.pi.pisprogonecs.configurexml.PackageTest.class,
   BundleTest.class
})
public class PackageTest {
}
