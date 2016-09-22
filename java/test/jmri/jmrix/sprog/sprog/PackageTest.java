package jmri.jmrix.sprog.sprog;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * tests for the jmri.jmrix.sprog.sprogCS package
 *
 * @author  Paul Bender Copyright (C) 2016	
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
   ConnectionConfigTest.class,
   jmri.jmrix.sprog.sprog.configurexml.PackageTest.class
})
public class PackageTest {
}
