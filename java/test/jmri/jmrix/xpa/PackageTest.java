package jmri.jmrix.xpa;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        XpaMessageTest.class,
        XpaTrafficControllerTest.class,
        XpaSystemConnectionMemoTest.class,
        XpaTurnoutTest.class,
        XpaThrottleTest.class,
        XpaTurnoutManagerTest.class,
        XpaPowerManagerTest.class,
        XpaThrottleManagerTest.class,
        jmri.jmrix.xpa.serialdriver.PackageTest.class,
        jmri.jmrix.xpa.configurexml.PackageTest.class,
        jmri.jmrix.xpa.swing.PackageTest.class,
        XpaPortControllerTest.class,
        BundleTest.class,
})

/**
 * Tests for the jmri.jmrix.xpa package
 *
 * @author	Paul Bender Copyright (C) 2012,2016
  */
public class PackageTest  {
}
