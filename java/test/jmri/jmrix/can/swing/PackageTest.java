package jmri.jmrix.can.swing;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@RunWith(Suite.class)
@Suite.SuiteClasses({
    jmri.jmrix.can.swing.monitor.PackageTest.class,
    jmri.jmrix.can.swing.send.PackageTest.class,
    CanMenuTest.class,
    CanComponentFactoryTest.class,
    BundleTest.class,
    CanNamedPaneActionTest.class
})
/**
 * Tests for the jmri.jmrix.can.swing.monitor package.
 *
 * @author Bob Jacobsen Copyright 2008
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
