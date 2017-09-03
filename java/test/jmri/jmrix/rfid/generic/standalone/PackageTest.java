package jmri.jmrix.rfid.generic.standalone;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@RunWith(Suite.class)
@Suite.SuiteClasses({
    jmri.jmrix.rfid.generic.standalone.configurexml.PackageTest.class,
    StandaloneReporterManagerTest.class,
    StandaloneSystemConnectionMemoTest.class,
    StandaloneTrafficControllerTest.class,
    StandaloneSensorManagerTest.class,
    StandaloneMessageTest.class,
    StandaloneReplyTest.class
})
/**
 * Tests for the jmri.jmrix.rfid.generic.standalone package.
 *
 * @author Paul Bender Copyright (C) 2016
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
