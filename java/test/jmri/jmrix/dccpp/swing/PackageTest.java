package jmri.jmrix.dccpp.swing;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    BundleTest.class,
    jmri.jmrix.dccpp.swing.mon.PackageTest.class,
    jmri.jmrix.dccpp.swing.packetgen.PackageTest.class,
    ConfigBaseStationActionTest.class,
    ConfigBaseStationFrameTest.class,
    DCCppComponentFactoryTest.class,
    DCCppMenuTest.class
})

/**
 * Tests for the jmri.jmrix.dccpp.swing package
 *
 * @author	Bob Jacobsen
 * @author  Paul Bender	Copyright (C) 2016
 */
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
