package jmri.jmrix.loconet.locostats.swing;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Tests for the jmri.jmrix.loconet.locostats package.
 *
 * @author	Bob Jacobsen Copyright 2001, 2003, 2006, 2008
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    BundleTest.class,
    LocoStatsFrameTest.class,
    LocoStatsPanelTest.class
})
public class PackageTest {
}
