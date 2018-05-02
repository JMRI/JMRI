package jmri.jmrix.powerline.insteon2412s;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


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
   ConstantsTest.class
})
/**
 * Tests for the jmri.jmrix.powerline.insteon2412s package.
 *
 * @author Bob Jacobsen Copyright 2003, 2007, 2008, 2009
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
