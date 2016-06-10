package jmri.jmrit.throttle;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Invokes complete set of tests in the jmri.jmrit.throttle tree
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
        TestSuite suite = new TestSuite("jmri.jmrit.throttle.PackageTest");   // no tests in this class itself

        suite.addTest(BundleTest.suite());

        if (!System.getProperty("jmri.headlesstest", "false").equals("true")) {
           suite.addTest(AddressPanelTest.suite());
           suite.addTest(BackgroundPanelTest.suite());
           suite.addTest(ControlPanelTest.suite());
           suite.addTest(ControlPanelPropertyEditorTest.suite());
           suite.addTest(FunctionButtonTest.suite());
           suite.addTest(FunctionButtonPropertyEditorTest.suite());
           suite.addTest(FunctionPanelTest.suite());
           suite.addTest(LargePowerManagerButtonTest.suite());
           suite.addTest(LoadDefaultXmlThrottlesLayoutActionTest.suite());
           suite.addTest(LoadXmlThrottlesLayoutActionTest.suite());
           suite.addTest(SmallPowerManagerButtonTest.suite());
           suite.addTest(StopAllButtonTest.suite());
           suite.addTest(StoreDefaultXmlThrottlesLayoutActionTest.suite());
           suite.addTest(StoreXmlThrottlesLayoutActionTest.suite());
           suite.addTest(ThrottleCreationActionTest.suite());
           suite.addTest(ThrottleFrameTest.suite());
           suite.addTest(ThrottleFrameManagerTest.suite());
           suite.addTest(ThrottleFramePropertyEditorTest.suite());
           suite.addTest(ThrottlesListActionTest.suite());
           suite.addTest(ThrottlesPreferencesActionTest.suite());
           suite.addTest(ThrottlesPreferencesTest.suite());
           suite.addTest(ThrottlesPreferencesPaneTest.suite());
           suite.addTest(ThrottlesListPanelTest.suite());
           suite.addTest(ThrottlesTableCellRendererTest.suite());
           suite.addTest(ThrottlesTableModelTest.suite());
           suite.addTest(ThrottleWindowTest.suite());
        }

        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }
}
