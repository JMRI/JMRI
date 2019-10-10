package jmri.jmrix.powerline.simulator;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
   ConnectionConfigTest.class,
   jmri.jmrix.powerline.simulator.configurexml.PackageTest.class,
   SimulatorAdapterTest.class,
   SpecificInsteonLightTest.class,
   SpecificX10LightTest.class,
   SpecificLightManagerTest.class,
   SpecificSensorManagerTest.class,
   SpecificMessageTest.class,
   SpecificReplyTest.class,
   SpecificSystemConnectionMemoTest.class,
   SpecificTrafficControllerTest.class,
   ConstantsTest.class,
   BundleTest.class,
})
/**
 * Tests for the jmri.jmrix.powerline.cp290 package.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class PackageTest {
}
