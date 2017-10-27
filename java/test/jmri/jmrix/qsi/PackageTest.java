package jmri.jmrix.qsi;

import jmri.util.JUnitUtil;
import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.qsi package
 *
 * @author	Bob Jacobsen
 */
public class PackageTest extends TestCase {

    // from here down is testing infrastructure
    public PackageTest(String s) {
        super(s);
    }

    // a dummy test to avoid JUnit warning
    public void testDemo() {
        assertTrue(true);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", PackageTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        apps.tests.AllTest.initLogging();
        TestSuite suite = new TestSuite("jmri.jmrix.qsi.QsiTest");
        suite.addTest(jmri.jmrix.qsi.QsiTrafficControllerTest.suite());
        suite.addTest(jmri.jmrix.qsi.QsiMessageTest.suite());
        suite.addTest(jmri.jmrix.qsi.QsiReplyTest.suite());
        suite.addTest(new JUnit4TestAdapter(jmri.jmrix.qsi.serialdriver.PackageTest.class));
        suite.addTest(new JUnit4TestAdapter(jmri.jmrix.qsi.qsimon.PackageTest.class));
        suite.addTest(new JUnit4TestAdapter(jmri.jmrix.qsi.packetgen.PackageTest.class));
        suite.addTest(new JUnit4TestAdapter(QsiSystemConnectionMemoTest.class));
        suite.addTest(new JUnit4TestAdapter(QsiPortControllerTest.class));
        suite.addTest(new JUnit4TestAdapter(jmri.jmrix.swing.PackageTest.class));
        suite.addTest(new JUnit4TestAdapter(QSIConnectionTypeListTest.class));
        suite.addTest(new JUnit4TestAdapter(QSIMenuTest.class));
        suite.addTest(new JUnit4TestAdapter(QsiProgrammerTest.class));

        return suite;
    }

    // The minimal setup for log4J
    @Override
    protected void setUp() {
        JUnitUtil.setUp();
    }

    @Override
    protected void tearDown() {
        JUnitUtil.tearDown();
    }
}
