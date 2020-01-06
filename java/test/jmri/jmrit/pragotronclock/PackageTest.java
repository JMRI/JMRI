package jmri.jmrit.pragotronclock;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        BundleTest.class,
        PragotronClockActionTest.class,
        PragotronClockFrameTest.class,
})

/**
 * Invokes complete set of tests in the jmri.jmrit.analogclock tree
 *
 * @author	Bob Jacobsen Copyright 2001, 2003, 2012, 2020
 */
public class PackageTest {
}
