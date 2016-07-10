package jmri.jmrix.cmri.serial;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * Tests for the jmri.jmrix.cmri.serial package.
 *
 * @author Bob Jacobsen Copyright 2003
 * @author Paul Bender Copyright (C) 2016
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
   jmri.jmrix.cmri.serial.SerialTurnoutTest.class,
   jmri.jmrix.cmri.serial.SerialTurnoutManagerTest.class,
   jmri.jmrix.cmri.serial.SerialSensorManagerTest.class,
   jmri.jmrix.cmri.serial.SerialNodeTest.class,
   jmri.jmrix.cmri.serial.SerialMessageTest.class,
   jmri.jmrix.cmri.serial.SerialTrafficControllerTest.class,
   jmri.jmrix.cmri.serial.SerialAddressTest.class})

public class PackageTest{

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

    private final static Logger log = LoggerFactory.getLogger(PackageTest.class.getName());

}
