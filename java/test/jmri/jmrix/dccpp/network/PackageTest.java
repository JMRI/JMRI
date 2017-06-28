package jmri.jmrix.dccpp.network;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.dccpp.network package
 *
 * @author Paul Bender
 * @author Mark Underwood Copyright (C) 2015
 */
public class PackageTest extends TestCase {

    // from here down is testing infrastructure
    public PackageTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {PackageTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite("jmri.jmrix.dccpp.network");  // no tests in this class itself
        suite.addTest(new TestSuite(DCCppEthernetAdapterTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(DCCppEthernetPacketizerTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(ConnectionConfigTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrix.dccpp.network.configurexml.PackageTest.class));
        return suite;
    }

}
