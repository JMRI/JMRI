package jmri.util.swing;

import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Invokes complete set of tests in the jmri.util.swing tree
 *
 * @author	Bob Jacobsen Copyright 2003
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
        TestSuite suite = new TestSuite("jmri.util.swing.PackageTest");   // no tests in this class itself

        suite.addTest(new JUnit4TestAdapter(BundleTest.class));
        suite.addTest(JmriAbstractActionTest.suite());
        suite.addTest(jmri.util.swing.multipane.PackageTest.suite());
        suite.addTest(jmri.util.swing.sdi.PackageTest.suite());
        suite.addTest(new JUnit4TestAdapter(jmri.util.swing.mdi.PackageTest.class));
        suite.addTest(jmri.util.swing.JCBHandleTest.suite());
        suite.addTest(new JUnit4TestAdapter(FontComboUtilTest.class));
        suite.addTest(new JUnit4TestAdapter(EditableResizableImagePanelTest.class));
        suite.addTest(new JUnit4TestAdapter(GuiUtilBaseTest.class));
        suite.addTest(new JUnit4TestAdapter(JMenuUtilTest.class));
        suite.addTest(new JUnit4TestAdapter(JToolBarUtilTest.class));
        suite.addTest(new JUnit4TestAdapter(JTreeUtilTest.class));
        suite.addTest(new JUnit4TestAdapter(JmriPanelTest.class));
        suite.addTest(new JUnit4TestAdapter(ResizableImagePanelTest.class));
        suite.addTest(new JUnit4TestAdapter(SliderSnapTest.class));
        suite.addTest(new JUnit4TestAdapter(StatusBarTest.class));
        suite.addTest(new JUnit4TestAdapter(SwingSettingsTest.class));
        suite.addTest(new JUnit4TestAdapter(VerticalLabelUITest.class));
        suite.addTest(new JUnit4TestAdapter(XTableColumnModelTest.class));

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
