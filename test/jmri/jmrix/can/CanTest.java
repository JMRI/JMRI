// CanTest.java

package jmri.jmrix.can;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.can package.
 * @author      Bob Jacobsen  Copyright 2008
 * @version   $Revision: 1.1 $
 */
public class CanTest extends TestCase {

    public void testDefinitions() {
    }
    
    // from here down is testing infrastructure

    public CanTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", CanTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        apps.tests.AllTest.initLogging();
        TestSuite suite = new TestSuite("jmri.jmrix.can.CanTest");
        suite.addTest(jmri.jmrix.can.cbus.CbusTest.suite());
        return suite;
    }

}
