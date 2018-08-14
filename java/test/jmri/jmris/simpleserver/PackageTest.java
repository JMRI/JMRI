package jmri.jmris.simpleserver;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
   SimpleServerTest.class,
   jmri.jmris.simpleserver.parser.PackageTest.class,
   SimpleTurnoutServerTest.class,
   SimplePowerServerTest.class,
   SimpleReporterServerTest.class,
   SimpleSensorServerTest.class,
   SimpleLightServerTest.class,
   SimpleSignalHeadServerTest.class,
   SimpleOperationsServerTest.class,
   SimpleServerManagerTest.class,
   BundleTest.class,
   SimpleServerFrameTest.class,
   SimpleServerActionTest.class,
   SimpleServerMenuTest.class,
   SimpleServerPreferencesPanelTest.class,
   SimpleServerPreferencesTest.class
})

/**
 * Tests for the jmri.jmris.simpleserver package
 *
 * @author Paul Bender copyright (C) 2016
 */
public class PackageTest {

}
