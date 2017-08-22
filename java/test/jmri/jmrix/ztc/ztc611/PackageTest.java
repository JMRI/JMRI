package jmri.jmrix.ztc.ztc611;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;


@RunWith(Suite.class)
@Suite.SuiteClasses({
   jmri.jmrix.ztc.ztc611.configurexml.PackageTest.class,
   ConnectionConfigTest.class,
   ZTC611AdapterTest.class,
   ZTC611XNetPacketizerTest.class,
   ZTC611XNetTurnoutTest.class,
   ZTC611XNetTurnoutManagerTest.class,
   ZTC611XNetInitializationManagerTest.class,
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
