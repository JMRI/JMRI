package jmri.jmrix.rps.trackingpanel;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Tests for the jmri.jmrix.rps package.
 *
 * @author Bob Jacobsen Copyright 2006
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    RpsTrackingFrameTest.class,
    RpsTrackingPanelTest.class,
    RpsTrackingFrameActionTest.class,
    RpsTrackingControlPaneTest.class
})
public class PackageTest {
}
