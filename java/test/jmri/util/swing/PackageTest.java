package jmri.util.swing;

import jmri.util.JUnitUtil;
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
        suite.addTest(new JUnit4TestAdapter(JmriBeanComboBoxTest.class));
        suite.addTest(new JUnit4TestAdapter(JMenuUtilTest.class));
        suite.addTest(new JUnit4TestAdapter(JComboBoxUtilTest.class));
        suite.addTest(new JUnit4TestAdapter(JToolBarUtilTest.class));
        suite.addTest(new JUnit4TestAdapter(JTreeUtilTest.class));
        suite.addTest(new JUnit4TestAdapter(JmriPanelTest.class));
        suite.addTest(new JUnit4TestAdapter(ResizableImagePanelTest.class));
        suite.addTest(new JUnit4TestAdapter(SliderSnapTest.class));
        suite.addTest(new JUnit4TestAdapter(StatusBarTest.class));
        suite.addTest(new JUnit4TestAdapter(SwingSettingsTest.class));
        suite.addTest(new JUnit4TestAdapter(VerticalLabelUITest.class));
        suite.addTest(new JUnit4TestAdapter(XTableColumnModelTest.class));
        suite.addTest(new JUnit4TestAdapter(JFrameInterfaceTest.class));
        suite.addTest(new JUnit4TestAdapter(JmriNamedPaneActionTest.class));
        suite.addTest(new JUnit4TestAdapter(BusyDialogTest.class));
        suite.addTest(new JUnit4TestAdapter(TextFilterTest.class));
        suite.addTest(new JUnit4TestAdapter(BeanSelectCreatePanelTest.class));
        suite.addTest(new JUnit4TestAdapter(ValidatedTextFieldTest.class));
        suite.addTest(new JUnit4TestAdapter(ComboBoxColorChooserPanelTest.class));
        suite.addTest(new JUnit4TestAdapter(ButtonGroupColorChooserPanelTest.class));
        suite.addTest(new JUnit4TestAdapter(ButtonSwatchColorChooserPanelTest.class));
        suite.addTest(new JUnit4TestAdapter(DrawSquaresTest.class));
        suite.addTest(new JUnit4TestAdapter(ImagePanelTest.class));

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
