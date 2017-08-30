package jmri.progdebugger;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Invoke complete set of tests for the Jmri.progdebugger package.
 * <P>
 * Due to existing package and class names, this is both the test suite for the
 * package, but also contains some tests for the ProgDebugger class.
 *
 * @author	Bob Jacobsen, Copyright (C) 2001, 2002
 */
public class PackageTest extends TestCase {

    public void testCtor() {
    }

    public PackageTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {PackageTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        apps.tests.AllTest.initLogging();
        TestSuite suite = new TestSuite(PackageTest.class);
        suite.addTest(jmri.progdebugger.DebugProgrammerTest.suite());
        suite.addTest(jmri.progdebugger.DebugProgrammerManagerTest.suite());
        suite.addTest(new junit.framework.JUnit4TestAdapter(ProgDebuggerTest.class));
        return suite;
    }

    // The minimal setup for log4J
    @Override
    protected void setUp() throws Exception {
        apps.tests.Log4JFixture.setUp();
        super.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.initInternalSensorManager();
    }

    @Override
    protected void tearDown() throws Exception {
        jmri.util.JUnitUtil.tearDown();
    }
}
