package jmri.jmrix.powerline.cm11;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
   ConnectionConfigTest.class,
   jmri.jmrix.powerline.cm11.configurexml.PackageTest.class,
   SpecificDriverAdapterTest.class,
   SpecificLightTest.class,
   SpecificLightManagerTest.class,
   SpecificSensorManagerTest.class,
   SpecificMessageTest.class,
   SpecificReplyTest.class,
   SpecificSystemConnectionMemoTest.class,
   SpecificTrafficControllerTest.class,
   ConstantsTest.class,
   BundleTest.class
})
/**
 * Tests for the jmri.jmrix.powerline.cm11 package.
 *
 * @author Bob Jacobsen Copyright 2003, 2007, 2008
 * @author Paul Bender Copyright (C) 2016
 */
public class PackageTest {
}
