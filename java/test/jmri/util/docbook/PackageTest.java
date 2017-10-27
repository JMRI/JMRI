// PackageTest
package jmri.util.docbook;

import jmri.util.JUnitUtil;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.util.docbook package
 *
 * @author	Bob Jacobsen Copyright (C) 2010
 */
public class PackageTest extends TestCase {

    public void testExtra() {
    }

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
        TestSuite suite = new TestSuite(PackageTest.class);

        suite.addTest(RevHistoryTest.suite());
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.util.docbook.configurexml.PackageTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(RevisionTest.class));
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
