package jmri.jmrix.cmri.serial.nodeiolist;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * Tests for the jmri.jmrix.cmri.serial.nodeiolist package.
 *
 * @author Bob Jacobsen Copyright 2003, 2017
 * @author Paul Bender Copyright (C) 2016
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
   NodeIOListFrameTest.class,
   BundleTest.class,
   NodeIOListActionTest.class,
})

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

    private final static Logger log = LoggerFactory.getLogger(PackageTest.class);

}
