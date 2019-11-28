package jmri.jmrit.throttle;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        BundleTest.class,
        AddressPanelTest.class,
        BackgroundPanelTest.class,
        ControlPanelTest.class,
        ControlPanelCustomSliderUITest.class,
        ControlPanelPropertyEditorTest.class,
        FunctionButtonTest.class,
        FunctionButtonPropertyEditorTest.class,
        FunctionPanelTest.class,
        LargePowerManagerButtonTest.class,
        LoadDefaultXmlThrottlesLayoutActionTest.class,
        LoadXmlThrottlesLayoutActionTest.class,
        SmallPowerManagerButtonTest.class,
        StopAllButtonTest.class,
        StoreDefaultXmlThrottlesLayoutActionTest.class,
        StoreXmlThrottlesLayoutActionTest.class,
        ThrottleCreationActionTest.class,
        ThrottleFrameTest.class,
        ThrottleFrameManagerTest.class,
        ThrottleFramePropertyEditorTest.class,
        ThrottlesListActionTest.class,
        ThrottlesPreferencesActionTest.class,
        ThrottlesPreferencesTest.class,
        ThrottlesPreferencesPaneTest.class,
        ThrottlesListPanelTest.class,
        ThrottlesTableCellRendererTest.class,
        ThrottlesTableModelTest.class,
        KeyListenerInstallerTest.class,
        WindowPreferencesTest.class,
        SpeedPanelTest.class,
        StealingThrottleTest.class,
        SharingThrottleTest.class,
        StealingOrSharingThrottleTest.class
})

/**
 * Invokes complete set of tests in the jmri.jmrit.throttle tree
 *
 * @author	Bob Jacobsen Copyright 2001, 2003, 2012
 */
public class PackageTest  {
}
