// PackageTest.java

package jmri.util;

import junit.framework.*;

/**
 * Invokes complete set of tests in the jmri.util tree
 *
 * @author	    Bob Jacobsen  Copyright 2003
 * @version         $Revision$
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
        TestSuite suite = new TestSuite("jmri.util.UtilTest");   // no tests in this class itself

        suite.addTest(FileUtilTest.suite());
        suite.addTest(JUnitAppenderTest.suite());
        suite.addTest(NamedBeanHandleTest.suite());
        suite.addTest(OrderedHashtableTest.suite());
        suite.addTest(StringUtilTest.suite());
        
        if (!System.getProperty("jmri.headlesstest","false").equals("true"))
            suite.addTest(SwingTestCaseTest.suite());

        suite.addTest(jmri.util.docbook.PackageTest.suite());
        suite.addTest(jmri.util.exceptionhandler.PackageTest.suite());
        suite.addTest(jmri.util.jdom.PackageTest.suite());

        if (!System.getProperty("jmri.headlesstest","false").equals("true"))
            suite.addTest(jmri.util.swing.PackageTest.suite());

        suite.addTest(jmri.util.WaitHandlerTest.suite());

        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }

}
