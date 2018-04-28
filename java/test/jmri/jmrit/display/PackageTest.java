package jmri.jmrit.display;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Tests for the jmrit.display package
 *
 * @author	Bob Jacobsen Copyright 2008, 2009, 2010, 2015
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        SchemaTest.class,
        PositionableLabelTest.class,
        LinkingLabelTest.class,
        MemoryIconTest.class,
        MemorySpinnerIconTest.class,
        PanelEditorTest.class,
        ReporterIconTest.class,
        RpsPositionIconTest.class,
        SensorIconWindowTest.class,
        SignalMastIconTest.class,
        SignalSystemTest.class,
        TurnoutIconWindowTest.class,
        TurnoutIconTest.class,
        IndicatorTurnoutIconTest.class,
        IconEditorWindowTest.class,
        jmri.jmrit.display.configurexml.PackageTest.class,
        jmri.jmrit.display.switchboardEditor.PackageTest.class,
        jmri.jmrit.display.layoutEditor.PackageTest.class,
        jmri.jmrit.display.panelEditor.PackageTest.class,
        jmri.jmrit.display.palette.PackageTest.class,
        jmri.jmrit.display.controlPanelEditor.PackageTest.class,
        BundleTest.class,
        SensorTextEditTest.class,
        AnalogClock2DisplayTest.class,
        BlockContentsIconTest.class,
        CoordinateEditTest.class,
        IconAdderTest.class,
        IndicatorTrackIconTest.class,
        IndicatorTrackPathsTest.class,
        LightIconTest.class,
        LocoIconTest.class,
        MemoryComboIconTest.class,
        MemoryIconCoordinateEditTest.class,
        MultiIconEditorTest.class,
        MultiSensorIconTest.class,
        MultiSensorIconAdderTest.class,
        PositionableIconTest.class,
        SensorIconTest.class,
        SignalHeadIconTest.class,
        SlipIconAdderTest.class,
        SlipTurnoutIconTest.class,
        SlipTurnoutTextEditTest.class,
        NewPanelActionTest.class,
        MemoryInputIconTest.class,
        PositionableJComponentTest.class,
        PositionableJPanelTest.class,
        ToolTipTest.class,
        PanelMenuTest.class,
        PositionablePopupUtilTest.class,
        PositionablePropertiesUtilTest.class,
        DisplayFrameTest.class,
})
public class PackageTest {
}
