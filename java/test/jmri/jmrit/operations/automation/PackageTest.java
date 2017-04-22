package jmri.jmrit.operations.automation;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Invokes complete set of tests in the jmri.jmrit.operations.automations tree
 *
 * @author	Bob Jacobsen Copyright 2001, 2003, 2012
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
        TestSuite suite = new TestSuite("jmri.jmrit.operations.automations.PackageTest");   // no tests in this class itself

        suite.addTest(AutomationManagerTest.suite());
        suite.addTest(AutomationItemTest.suite());
        suite.addTest(new junit.framework.JUnit4TestAdapter(BundleTest.class));
        suite.addTest(jmri.jmrit.operations.automation.actions.PackageTest.suite());
        suite.addTest(new junit.framework.JUnit4TestAdapter(AutomationTableFrameGuiTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(AutomationCopyFrameGuiTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(AutomationCopyActionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(AutomationTableModelTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(AutomationsTableFrameActionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(AutomationsTableFrameTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(AutomationsTableFrameGuiTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(AutomationsTableModelTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(XmlTest.class));        
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
