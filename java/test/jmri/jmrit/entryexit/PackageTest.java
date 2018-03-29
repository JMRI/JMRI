package jmri.jmrit.entryexit;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    DestinationPointsTest.class,    // Keep first
    BundleTest.class,
    AddEntryExitPairFrameTest.class,
    AddEntryExitPairActionTest.class,
    AddEntryExitPairPanelTest.class,
    EntryExitPairsTest.class,
    ManuallySetRouteTest.class,
    PointDetailsTest.class,
    SourceTest.class,
    StackNXPanelTest.class,
    jmri.jmrit.entryexit.configurexml.PackageTest.class
})
/**
 * Invokes complete set of tests in the jmri.jmrit.entryexit tree
 *
 * @author	Bob Jacobsen Copyright 2001, 2003, 2012
 */
public class PackageTest {
}
