package jmri.jmrit.logix;

import jmri.util.JUnitUtil;



import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
//		Something wrong in the xsd files?  maybe using -2-9-6 version?
        SchemaTest.class,
        OBlockTest.class,
        OBlockManagerTest.class,
        OPathTest.class,
        PortalTest.class,
        WarrantTest.class,
        LogixActionTest.class,
        BundleTest.class,
        jmri.jmrit.logix.configurexml.PackageTest.class,
        NXFrameTest.class, //formerly NXWarrantTest        
        LearnWarrantTest.class,
        PortalManagerTest.class,
        ThrottleSettingTest.class,
        WarrantManagerTest.class,
        WarrantPreferencesPanelTest.class,
        WarrantPreferencesTest.class,
        TrackerTableActionTest.class,
        WarrantFrameTest.class,
        WarrantTableActionTest.class,
        WarrantTableFrameTest.class,
        WarrantTableModelTest.class,
        LearnThrottleFrameTest.class,
        TrackerTest.class,
        BlockOrderTest.class,
        ControlPanelTest.class,
        OpSessionLogTest.class,
        SCWarrantTest.class,
        EngineerTest.class,
        SpeedUtilTest.class,
        FunctionPanelTest.class,
        WarrantShutdownTaskTest.class,
        SpeedProfilePanelTest.class,
        RouteFinderTest.class,
        MergePromptTest.class,
})

/**
 * Invokes complete set of tests in the jmri.jmrit.logix tree
 *
 * @author	Bob Jacobsen Copyright 2010
 */
public class PackageTest {
}
