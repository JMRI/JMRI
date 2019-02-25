package jmri.jmrix.marklin;


import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
   jmri.jmrix.marklin.networkdriver.PackageTest.class,
   jmri.jmrix.marklin.configurexml.PackageTest.class,
   jmri.jmrix.marklin.swing.PackageTest.class,
   MarklinConnectionTypeListTest.class,
   MarklinSystemConnectionMemoTest.class,
   MarklinTrafficControllerTest.class,
   MarklinPortControllerTest.class,
   MarklinConstantsTest.class,
   MarklinMessageTest.class,
   MarklinReplyTest.class,
   MarklinPowerManagerTest.class,
   MarklinSensorManagerTest.class,
   MarklinSensorTest.class,
   MarklinThrottleManagerTest.class,
   MarklinThrottleTest.class,
   MarklinTurnoutManagerTest.class,
   MarklinTurnoutTest.class,
   BundleTest.class
})
/**
 * Tests for the jmri.jmrix.marklin package
 *
 * @author  Paul Bender	Copyright (C) 2016
 */
public class PackageTest {
}
