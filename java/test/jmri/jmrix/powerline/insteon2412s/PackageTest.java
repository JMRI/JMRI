package jmri.jmrix.powerline.insteon2412s;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
   ConnectionConfigTest.class,
   jmri.jmrix.powerline.insteon2412s.configurexml.PackageTest.class,
   SpecificDriverAdapterTest.class,
   SpecificInsteonLightTest.class,
   SpecificX10LightTest.class,
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
 * Tests for the jmri.jmrix.powerline.insteon2412s package.
 *
 * @author Bob Jacobsen Copyright 2003, 2007, 2008, 2009
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class PackageTest {
}
