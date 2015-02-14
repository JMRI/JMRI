// PackageTest.java

package jmri.jmrit.symbolicprog.tabbedframe;

import junit.framework.*;

/**
 * Invokes complete set of tests in the jmri.jmrit.symbolicprog.tabbedframe tree
 *
 * @author	    Bob Jacobsen  Copyright 2001, 2003, 2012
 * @version         $Revision: 21497 $
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
        TestSuite suite = new TestSuite("jmri.jmrit.symbolicprog.tabbedframe.PackageTest");   // no tests in this class itself

        suite.addTest(jmri.jmrit.symbolicprog.tabbedframe.PaneProgFrameTest.suite());
        suite.addTest(jmri.jmrit.symbolicprog.tabbedframe.PaneProgFrameTest.suite());
        suite.addTest(jmri.jmrit.symbolicprog.tabbedframe.CheckProgrammerNames.suite());
        suite.addTest(jmri.jmrit.symbolicprog.tabbedframe.QualifiedVarTest.suite());

        if (!System.getProperty("jmri.headlesstest","false").equals("true")) {
        }
                
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }
}
