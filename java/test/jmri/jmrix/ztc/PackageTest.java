package jmri.jmrix.ztc;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;


@RunWith(Suite.class)
@Suite.SuiteClasses({
   ZTCConnectionTypeListTest.class,
   jmri.jmrix.ztc.ztc611.PackageTest.class,
   BundleTest.class
})

/**
 * Tests for the jmri.jmrix.ztc package
 *
 * @author	Bob Jacobsen
 * @author      Paul Bender Copyright (C) 2017
 */
public class PackageTest {
}
