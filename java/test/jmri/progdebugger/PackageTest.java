// PackageTest.java
package jmri.progdebugger;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Invoke complete set of tests for the Jmri.progdebugger package.
 * <P>
 * Due to existing package and class names, this is both the test suite for the
 * package, but also contains some tests for the ProgDebugger class.
 *
 * @author	Bob Jacobsen, Copyright (C) 2001, 2002
 * @version $Revision$
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
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        apps.tests.AllTest.initLogging();
        TestSuite suite = new TestSuite(PackageTest.class);
        suite.addTest(jmri.progdebugger.DebugProgrammerTest.suite());
        suite.addTest(jmri.progdebugger.DebugProgrammerManagerTest.suite());
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() throws Exception {
        apps.tests.Log4JFixture.setUp();
        super.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.initInternalSensorManager();
    }

    protected void tearDown() throws Exception {
        jmri.util.JUnitUtil.resetInstanceManager();
        super.tearDown();
        apps.tests.Log4JFixture.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(PackageTest.class.getName());
}
