package jmri.util;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Invokes complete set of tests in the jmri.util tree
 *
 * @author	Bob Jacobsen Copyright 2003
 */
public class PackageTest extends TestCase {

    // from here down is testing infrastructure
    public PackageTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", PackageTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite("jmri.util.PackageTest");   // no tests in this class itself

        suite.addTest(BundleTest.suite());
        suite.addTest(FileUtilTest.suite());
        suite.addTest(JUnitAppenderTest.suite());
        suite.addTest(IntlUtilitiesTest.suite());
        suite.addTest(Log4JUtilTest.suite());
        suite.addTest(NamedBeanHandleTest.suite());
        suite.addTest(OrderedHashtableTest.suite());
        suite.addTest(PreferNumericComparatorTest.suite());
        suite.addTest(StringUtilTest.suite());
        suite.addTest(ThreadingUtilTest.suite());
        suite.addTest(I18NTest.suite());
        suite.addTest(ColorUtilTest.suite());

        if (!System.getProperty("jmri.headlesstest", "false").equals("true")) {
            suite.addTest(SwingTestCaseTest.suite());
        }

        suite.addTest(jmri.util.docbook.PackageTest.suite());
        suite.addTest(jmri.util.exceptionhandler.PackageTest.suite());
        suite.addTest(jmri.util.jdom.PackageTest.suite());

        if (!System.getProperty("jmri.headlesstest", "false").equals("true")) {
            suite.addTest(jmri.util.swing.PackageTest.suite());
        }

        suite.addTest(jmri.util.WaitHandlerTest.suite());
        suite.addTest(jmri.util.zeroconf.PackageTest.suite());
        suite.addTest(jmri.util.DateUtilTest.suite());
        suite.addTest(jmri.util.prefs.PackageTest.suite());

        // deliberately at end
        suite.addTest(jmri.util.Log4JErrorIsErrorTest.suite());
        
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

}
