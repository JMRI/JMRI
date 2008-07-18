// CbusTest.java

package jmri.jmrix.can.cbus;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.can.cbus package.
 * @author      Bob Jacobsen  Copyright 2008
 * @version   $Revision: 1.1 $
 */
public class CbusTest extends TestCase {

    // from here down is testing infrastructure

    public CbusTest(String s) {
        super(s);
    }

    public void testDefinitions() {
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", CbusTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        apps.tests.AllTest.initLogging();
        TestSuite suite = new TestSuite("jmri.jmrix.can.cbus.CbusTest");
        suite.addTest(jmri.jmrix.can.cbus.CbusAddressTest.suite());
        suite.addTest(jmri.jmrix.can.cbus.CbusSensorManagerTest.suite());
        suite.addTest(jmri.jmrix.can.cbus.CbusSensorTest.suite());
        return suite;
    }

}
