package jmri.jmrix.sprog.pi;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * tests for the jmri.jmrix.sprog.pi package
 *
 * @author  Paul Bender Copyright (C) 2016	
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
   jmri.jmrix.sprog.pi.pisprognano.PackageTest.class,
   jmri.jmrix.sprog.pi.pisprogone.PackageTest.class,
   jmri.jmrix.sprog.pi.pisprogonecs.PackageTest.class,
   BundleTest.class
})
public class PackageTest {
}
