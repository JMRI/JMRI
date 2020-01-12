package jmri.jmrix.pi;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
   RaspberryPiConnectionConfigTest.class,
   RaspberryPiAdapterTest.class,
   RaspberryPiSystemConnectionMemoTest.class,
   RaspberryPiSensorManagerTest.class,
   RaspberryPiTurnoutManagerTest.class,
   jmri.jmrix.pi.configurexml.PackageTest.class,
   RaspberryPiConnectionTypeListTest.class,
   RaspberryPiSensorTest.class,
   RaspberryPiTurnoutTest.class,
   BundleTest.class
})
/**
 * Tests for the jmri.jmrix.pi package
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class PackageTest {
}
