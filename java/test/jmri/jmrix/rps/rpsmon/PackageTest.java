package jmri.jmrix.rps.rpsmon;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Tests for the jmri.jmrix.rps.rpsmon package.
 *
 * @author Bob Jacobsen Copyright 2008
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    RpsMonActionTest.class,
    RpsMonTest.class,
    RpsMonFrameTest.class
})
public class PackageTest {
}
