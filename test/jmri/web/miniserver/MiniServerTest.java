// MiniServerTest.java

package jmri.web.miniserver;

import junit.framework.*;

/**
 * Invokes complete set of tests in the jmri.web.miniserver tree
 *
 * @author	    Bob Jacobsen  Copyright 2008
 * @version         $Revision: 1.2 $
 */
public class MiniServerTest extends TestCase {

    // from here down is testing infrastructure
    public MiniServerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {MiniServerTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite("jmri.web.miniserver.MiniServerTest");   // no tests in this class itself
        suite.addTest(jmri.web.miniserver.servlet.ServletTest.suite());
        suite.addTest(MiniServletContextTest.suite());
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }
}
