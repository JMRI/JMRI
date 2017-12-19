package jmri.jmrit.display.layoutEditor;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Tests for the jmrit.display.layoutEditor package
 *
 * @author	Bob Jacobsen Copyright 2008, 2009, 2010
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        LayoutEditorConnectivityTest.class,
        BlockContentsIconTest.class,
        BlockValueFileTest.class, 
        BundleTest.class,
        ConnectivityUtilTest.class,
        jmri.jmrit.display.layoutEditor.blockRoutingTable.PackageTest.class,
        jmri.jmrit.display.layoutEditor.configurexml.PackageTest.class,
        LayoutBlockConnectivityToolsTest.class, 
        LayoutBlockManagerTest.class, 
        LayoutBlockTest.class, 
        LayoutConnectivityTest.class, 
        LayoutEditorActionTest.class, 
        LayoutEditorAuxToolsTest.class,
        LayoutEditorFindItemsTest.class,
        LayoutEditorLoadAndStoreTest.class,
        LayoutEditorTest.class,
        LayoutEditorWindowTest.class,
        LayoutSlipTest.class,
        LayoutTurnoutTest.class,
        LayoutTurntableTest.class,
        LevelXingTest.class,
        MemoryIconTest.class,
        MultiIconEditorTest.class,
        MultiSensorIconFrameTest.class,
        PositionablePointTest.class,
        SchemaTest.class,
        TrackNodeTest.class,
        TrackSegmentTest.class,
        TransitCreationToolTest.class,
        LayoutTrackEditorsTest.class,
        LayoutEditorComponentTest.class,
        LayoutEditorToolsTest.class,
        LayoutEditorChecksTest.class
})
public class PackageTest {
}
