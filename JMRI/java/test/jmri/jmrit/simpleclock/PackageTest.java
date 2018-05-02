package jmri.jmrit.simpleclock;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        jmri.jmrit.simpleclock.SimpleTimebaseTest.class,
        BundleTest.class,
        jmri.jmrit.simpleclock.configurexml.PackageTest.class,
        SimpleClockActionTest.class,
        SimpleClockFrameTest.class,
})

/**
 * Tests for the jmrit.simpleclock package
 *
 * @author	Bob Jacobsen
 */
public class PackageTest  {
}
