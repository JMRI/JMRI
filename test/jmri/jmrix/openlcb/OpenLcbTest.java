// OpenLcbTest.java

package jmri.jmrix.openlcb;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.can.nmranet package.
 * @author      Bob Jacobsen  Copyright 2009
 * @version   $Revision: 1.1 $
 */
public class OpenLcbTest extends TestCase {

    public void testDefinitions() {
    }
    
    // from here down is testing infrastructure

    public OpenLcbTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        apps.tests.AllTest.initLogging();
        String[] testCaseName = {"-noloading", OpenLcbTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite("jmri.jmrix.can.openlcb.OpenLcbTest");
        suite.addTest(CanConverterTest.suite());
        suite.addTest(jmri.jmrix.openlcb.CanConverterTest.suite());

        if (!System.getProperty("jmri.headlesstest","false").equals("true")) {
            suite.addTest(jmri.jmrix.openlcb.swing.tie.TieToolFrameTest.suite());
        }

        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }
}
