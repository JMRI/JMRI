package jmri.beans;

import jmri.util.JUnitUtil;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Invoke complete set of tests for the Jmri package
 *
 * @author	Bob Jacobsen, Copyright (C) 2001, 2002, 2007
 */
public class PackageTest extends TestCase {

    // from here down is testing infrastructure
    public PackageTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", PackageTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite("jmri.beans.PackageTest");  // no tests in this class itself
        suite.addTest(new TestSuite(BeansTest.class));
        suite.addTest(new TestSuite(UnboundBeanTest.class));
        suite.addTest(new TestSuite(ArbitraryPropertySupportTest.class));
        suite.addTest(new TestSuite(UnboundArbitraryBeanTest.class));
	suite.addTest(new junit.framework.JUnit4TestAdapter(ConstrainedArbitraryBeanTest.class));
        return suite;
    }

    // The minimal setup for log4J
    @Override
    protected void setUp() {
        JUnitUtil.setUp();
    }

    @Override
    protected void tearDown() {
        JUnitUtil.tearDown();
    }

}

