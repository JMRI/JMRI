package jmri.jmrit.throttle;

import jmri.util.JUnitUtil;
import junit.framework.JUnit4TestAdapter;
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

        suite.addTest(new junit.framework.JUnit4TestAdapter(BundleTest.class));
        suite.addTest(AddressPanelTest.suite());
        suite.addTest(new JUnit4TestAdapter(BackgroundPanelTest.class));
        suite.addTest(new JUnit4TestAdapter(ControlPanelTest.class));
        suite.addTest(new JUnit4TestAdapter(ControlPanelPropertyEditorTest.class));
        suite.addTest(FunctionButtonTest.suite());
        suite.addTest(new JUnit4TestAdapter(FunctionButtonPropertyEditorTest.class));
        suite.addTest(new JUnit4TestAdapter(FunctionPanelTest.class));
        suite.addTest(LargePowerManagerButtonTest.suite());
        suite.addTest(LoadDefaultXmlThrottlesLayoutActionTest.suite());
        suite.addTest(LoadXmlThrottlesLayoutActionTest.suite());
        suite.addTest(SmallPowerManagerButtonTest.suite());
        suite.addTest(StopAllButtonTest.suite());
        suite.addTest(StoreDefaultXmlThrottlesLayoutActionTest.suite());
        suite.addTest(StoreXmlThrottlesLayoutActionTest.suite());
        suite.addTest(ThrottleCreationActionTest.suite());
        suite.addTest(new JUnit4TestAdapter(ThrottleFrameTest.class));
        suite.addTest(new JUnit4TestAdapter(ThrottleFrameManagerTest.class));
        suite.addTest(new JUnit4TestAdapter(ThrottleFramePropertyEditorTest.class));
        suite.addTest(new JUnit4TestAdapter(ThrottlesListActionTest.class));
        suite.addTest(ThrottlesPreferencesActionTest.suite());
        suite.addTest(ThrottlesPreferencesTest.suite());
        suite.addTest(ThrottlesPreferencesPaneTest.suite());
        suite.addTest(ThrottlesListPanelTest.suite());
        suite.addTest(ThrottlesTableCellRendererTest.suite());
        suite.addTest(new JUnit4TestAdapter(ThrottlesTableModelTest.class));
        suite.addTest(new JUnit4TestAdapter(KeyListenerInstallerTest.class));
        suite.addTest(new JUnit4TestAdapter(WindowPreferencesTest.class));
        suite.addTest(new JUnit4TestAdapter(SpeedPanelTest.class));
        return suite;
    }

    // The minimal setup for log4J
    @Override
    public void setUp() {
        JUnitUtil.setUp();
    }

    @Override
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
