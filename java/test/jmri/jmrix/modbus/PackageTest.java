package jmri.jmrix.modbus;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.modbus package.
 *
 * @author	Bob Jacobsen Copyright 2001, 2003, 2014
 * @version $Revision$
 */
public class PackageTest extends TestCase {

    // from here down is testing infrastructure
    public PackageTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {PackageTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite("jmri.jmrix.modbus.PackageTest");  // no tests in this class itself

        suite.addTest(jmri.jmrix.modbus.common.PackageTest.suite());
        suite.addTest(jmri.jmrix.modbus.slave.PackageTest.suite());
        suite.addTest(jmri.jmrix.modbus.master.PackageTest.suite());

        return suite;
    }

}
