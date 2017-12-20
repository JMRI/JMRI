package jmri.util.swing;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        BundleTest.class,
        JmriAbstractActionTest.class,
        jmri.util.swing.multipane.PackageTest.class,
        jmri.util.swing.sdi.PackageTest.class,
        jmri.util.swing.mdi.PackageTest.class,
        jmri.util.swing.JCBHandleTest.class,
        FontComboUtilTest.class,
        EditableResizableImagePanelTest.class,
        GuiUtilBaseTest.class,
        JmriBeanComboBoxTest.class,
        JMenuUtilTest.class,
        JComboBoxUtilTest.class,
        JToolBarUtilTest.class,
        JTreeUtilTest.class,
        JmriPanelTest.class,
        ResizableImagePanelTest.class,
        SliderSnapTest.class,
        StatusBarTest.class,
        SwingSettingsTest.class,
        VerticalLabelUITest.class,
        XTableColumnModelTest.class,
        JFrameInterfaceTest.class,
        JmriNamedPaneActionTest.class,
        BusyDialogTest.class,
        TextFilterTest.class,
        BeanSelectCreatePanelTest.class,
        ValidatedTextFieldTest.class,
        ComboBoxColorChooserPanelTest.class,
        ButtonGroupColorChooserPanelTest.class,
        ButtonSwatchColorChooserPanelTest.class,
        DrawSquaresTest.class,
        ImagePanelTest.class,
})

/**
 * Invokes complete set of tests in the jmri.util.swing tree
 *
 * @author	Bob Jacobsen Copyright 2003
 */
public class PackageTest  {
}
