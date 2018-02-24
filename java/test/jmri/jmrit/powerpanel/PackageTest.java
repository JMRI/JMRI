package jmri.jmrit.powerpanel;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        PowerPaneTest.class,
        PowerButtonActionTest.class,
        PowerPanelActionTest.class,
        PowerPanelFrameTest.class,
        BundleTest.class,
})

/**
 * Tests for the jmrit.PowerPanel package
 *
 * @author	Bob Jacobsen
 */
public class PackageTest  {
}
