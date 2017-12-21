package jmri.jmrit.lcdclock;

import jmri.util.JUnitUtil;


import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        BundleTest.class,
        LcdClockActionTest.class,
        LcdClockFrameTest.class,
})

/**
 * Invokes complete set of tests in the jmri.jmrit.lcdclock tree
 *
 * @author	Bob Jacobsen Copyright 2001, 2003, 2012
 */
public class PackageTest {
}
