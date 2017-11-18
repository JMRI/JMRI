package jmri.jmrix.marklin;


import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


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

    // Main entry point
    static public void main(String[] args) {
        org.junit.runner.Result result = org.junit.runner.JUnitCore
                 .runClasses(PackageTest.class);
        for(org.junit.runner.notification.Failure fail: result.getFailures()) {
            log.error(fail.toString());
        }
        //junit.textui.TestRunner.main(testCaseName);
        if (result.wasSuccessful()) {
            log.info("Success");
        }
    }

    private final static Logger log = LoggerFactory.getLogger(PackageTest.class);


}
