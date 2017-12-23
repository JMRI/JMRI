package jmri.jmrix.anyma.configurexml;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    AnymaDMX_ConnectionConfigXmlTest.class,
    UsbLightManagerXmlTest.class,})

/**
 * Tests for the jmri.jmrix.acela.configurexml package.
 *
 * @author George Warner Copyright (C) 2017
 * @since 4.9.6
 */
public class PackageTest {

    // Main entry point
    static public void main(String[] args) {
        Result result = JUnitCore
                .runClasses(PackageTest.class);
        for (org.junit.runner.notification.Failure fail : result.getFailures()) {
            log.error(fail.toString());
        }
        //junit.textui.TestRunner.main(testCaseName);
        if (result.wasSuccessful()) {
            log.info("Success");
        }
    }

    private final static Logger log = LoggerFactory.getLogger(PackageTest.class);
}
