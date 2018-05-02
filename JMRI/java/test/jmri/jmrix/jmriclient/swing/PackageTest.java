package jmri.jmrix.jmriclient.swing;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    JMRIClientMenuTest.class,
    JMRIClientComponentFactoryTest.class,
    jmri.jmrix.jmriclient.swing.mon.PackageTest.class,
    jmri.jmrix.jmriclient.swing.packetgen.PackageTest.class
})

/**
 * Tests for the jmri.jmrix.ieee802154.xbee.swing package
 *
 * @author  Paul Bender	Copyright (C) 2016
 */
public class PackageTest{

    // Main entry point
    static public void main(String[] args) {
        apps.tests.Log4JFixture.setUp();
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
