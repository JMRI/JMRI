package jmri.jmrit.display.configurexml;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * PackageTest.java
 * <p>
 * Description:	Tests for the jmrit.display.configurexml package
 *
 * @author	Bob Jacobsen Copyright 2009, 2014
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    SchemaTest.class,
    LoadAndStoreTest.class,
    AnalogClock2DisplayXmlTest.class,
    BlockContentsIconXmlTest.class,
    IndicatorTrackIconXmlTest.class,
    LayoutBlockManagerXmlTest.class,
    LayoutTurnoutXmlTest.class,
    LayoutTurntableXmlTest.class,
    LevelXingXmlTest.class,
    LightIconXmlTest.class,
    MemoryComboIconXmlTest.class,
    MemoryIconXmlTest.class,
    MemoryInputIconXmlTest.class,
    MemorySpinnerIconXmlTest.class,
    MultiSensorIconXmlTest.class,
    PositionableLabelXmlTest.class,
    PositionablePointXmlTest.class,
    ReporterIconXmlTest.class,
    RpsPositionIconXmlTest.class,
    SensorIconXmlTest.class,
    SignalHeadIconXmlTest.class,
    SignalMastIconXmlTest.class,
    SlipTurnoutIconXmlTest.class,
    TrackSegmentXmlTest.class,
    TurnoutIconXmlTest.class,
    IndicatorTurnoutIconXmlTest.class,
    LinkingLabelXmlTest.class,
    LocoIconXmlTest.class
})
public class PackageTest {
}
