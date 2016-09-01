package jmri.jmrit.logix;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Invokes complete set of tests in the jmri.jmrit.logix tree
 *
 * @author	Bob Jacobsen Copyright 2010
 */
public class PackageTest extends TestCase {

    // from here down is testing infrastructure
    public PackageTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        apps.tests.Log4JFixture.initLogging();
        String[] testCaseName = {"-noloading", PackageTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite("jmri.jmrit.logix.PackageTest");   // no tests in this class itself

//		Something wrong in the xsd files?  maybe using -2-9-6 version?
        suite.addTest(SchemaTest.suite());
        suite.addTest(OBlockTest.suite());
        suite.addTest(OBlockManagerTest.suite());
        suite.addTest(OPathTest.suite());
        suite.addTest(WarrantTest.suite());
        suite.addTest(LogixActionTest.suite());
        suite.addTest(BundleTest.suite());
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrit.logix.configurexml.PackageTest.class));

        if (!System.getProperty("jmri.headlesstest", "false").equals("true")) {
            suite.addTest(NXFrameTest.suite()); //formerly NXWarrantTest        
            suite.addTest(LearnWarrantTest.suite());            
        }
        return suite;
    }

    // The minimal setup for log4J
    @Override
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    @Override
    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

}
