// HeadLessTest.java

package jmri;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Invoke complete set of tests for the jmri package
 * @author	Bob Jacobsen, Copyright (C) 2001, 2002, 2007
 * @version         $Revision: 1.2 $
 */
public class HeadLessTest extends TestCase {

    // from here down is testing infrastructure

    public HeadLessTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {HeadLessTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        apps.tests.AllTest.initLogging();
        TestSuite suite = new TestSuite("jmri.JmriTest");  // no tests in this class itself
		suite.addTest(jmri.BlockTest.suite());
		suite.addTest(jmri.RouteTest.suite());
		suite.addTest(jmri.BlockManagerTest.suite());
		suite.addTest(jmri.BeanSettingTest.suite());
		suite.addTest(jmri.PathTest.suite());
        suite.addTest(jmri.DccLocoAddressTest.suite());
        suite.addTest(jmri.progdebugger.ProgDebuggerTest.suite());
        suite.addTest(jmri.NmraPacketTest.suite());
        suite.addTest(jmri.configurexml.ConfigXmlTest.suite());
        suite.addTest(jmri.util.UtilTest.suite());
        suite.addTest(jmri.jmrit.JmritTest.suite());
        return suite;
    }

}
