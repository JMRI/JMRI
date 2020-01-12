package jmri.jmrix.bachrus;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
   SpeedoSystemConnectionMemoTest.class,
   SpeedoTrafficControllerTest.class,
   SpeedoReplyTest.class,
   jmri.jmrix.bachrus.serialdriver.PackageTest.class,
   SpeedoPortControllerTest.class,
   GraphPaneTest.class,
   SpeedTest.class,
   SpeedoConnectionTypeListTest.class,
   SpeedoDialTest.class,
   jmri.jmrix.bachrus.swing.PackageTest.class,
   DccSpeedProfileTest.class,
   SpeedoMenuTest.class,
   SpeedoConsoleActionTest.class,
   SpeedoConsoleFrameTest.class,
   BundleTest.class
})

/**
 * Tests for the jmri.jmrix.bachrus package
 *
 * @author  Paul Bender	Copyright (C) 2016
 */
public class PackageTest{
}
