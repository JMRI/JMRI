package jmri.jmrix.powerline.simulator;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@RunWith(Suite.class)
@Suite.SuiteClasses({
   ConnectionConfigTest.class,
   jmri.jmrix.powerline.simulator.configurexml.PackageTest.class,
   SimulatorAdapterTest.class,
   SpecificInsteonLightTest.class,
   SpecificX10LightTest.class,
   SpecificLightManagerTest.class,
   SpecificSensorManagerTest.class,
   SpecificMessageTest.class,
   SpecificReplyTest.class,
   SpecificSystemConnectionMemoTest.class,
   SpecificTrafficControllerTest.class,
   ConstantsTest.class
})
/**
 * Tests for the jmri.jmrix.powerline.cp290 package.
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
