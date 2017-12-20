package jmri.jmrix.tams;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        TamsTurnoutManagerTest.class,
        jmri.jmrix.tams.simulator.PackageTest.class,
        jmri.jmrix.tams.serialdriver.PackageTest.class,
        jmri.jmrix.tams.configurexml.PackageTest.class,
        jmri.jmrix.tams.swing.PackageTest.class,
        TamsSystemConnectionMemoTest.class,
        TamsPortControllerTest.class,
        TamsTrafficControllerTest.class,
        TamsConnectionTypeListTest.class,
        TamsConstantsTest.class,
        TamsMessageTest.class,
        TamsReplyTest.class,
        TamsOpsModeProgrammerTest.class,
        TamsProgrammerTest.class,
        TamsProgrammerManagerTest.class,
        TamsPowerManagerTest.class,
        TamsSensorManagerTest.class,
        TamsSensorTest.class,
        TamsThrottleManagerTest.class,
        TamsThrottleTest.class,
        TamsTurnoutTest.class,
        BundleTest.class,
})

/**
 * Tests for the jmri.jmrix.tams package.
 *
 * @author Bob Jacobsen Copyright 2003, 2016
 * @author  Paul Bender Copyright (C) 2016	
 */
public class PackageTest  {
}
