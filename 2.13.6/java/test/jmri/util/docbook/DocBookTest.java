// DocBookTest

package jmri.util.docbook;

import junit.framework.*;

/**
 * Tests for the jmri.util.docbook package
 * @author	Bob Jacobsen     Copyright (C) 2010
 * @version     $Revision$
 */
public class DocBookTest extends TestCase {

    public void testExtra() {
    }
    
    // from here down is testing infrastructure

    public DocBookTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", DocBookTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(DocBookTest.class);

        suite.addTest(RevHistoryTest.suite());

        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }

}
