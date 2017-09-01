package jmri.jmrix.loconet.locostats;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Tests for the jmri.jmrix.loconet.locostats package.
 *
 * @author	Bob Jacobsen Copyright 2001, 2003, 2006, 2008
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    jmri.jmrix.loconet.locostats.swing.PackageTest.class,
    LocoBufferIIStatusTest.class,
    PR2StatusTest.class,
    PR3MS100ModeStatusTest.class,
    RawStatusTest.class,
    LocoStatsFuncTest.class,
    BundleTest.class,
})
public class PackageTest {
}
