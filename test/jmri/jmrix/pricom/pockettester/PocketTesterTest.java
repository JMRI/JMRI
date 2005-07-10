// PocketTesterTest.java

package jmri.jmrix.pricom.pockettester;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.pricom.pockettester package.
 * @author      Bob Jacobsen  Copyright 2005
 * @version   $Revision: 1.1 $
 */
public class PocketTesterTest extends TestCase {

    // from here down is testing infrastructure

    public PocketTesterTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {PocketTesterTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        apps.tests.AllTest.initLogging();
        TestSuite suite = new TestSuite("jmri.jmrix.pricom.pockettester.PocketTesterTest");
        suite.addTest(jmri.jmrix.pricom.pockettester.MonitorFrameTest.suite());
        return suite;
    }

}
