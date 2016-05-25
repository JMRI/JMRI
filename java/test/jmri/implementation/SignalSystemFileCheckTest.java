package jmri.implementation;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests of the signal system definition files.
 * <p>
 * Checks all files in the distribution directory
 *
 * @author	Bob Jacobsen Copyright (C) 2009
 */
public class SignalSystemFileCheckTest extends jmri.configurexml.SchemaTestBase {

    // from here down is testing infrastructure
    public SignalSystemFileCheckTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {SignalSystemFileCheckTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite("jmri.implementation.SignalSystemFileCheckTest");
        validateDirectory(suite, "xml/signals/");
        validateSubdirectories(suite, "xml/signals/");
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }
    static protected Logger log = LoggerFactory.getLogger(SignalSystemFileCheckTest.class.getName());
}
