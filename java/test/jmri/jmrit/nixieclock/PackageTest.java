package jmri.jmrit.nixieclock;

import jmri.util.JUnitUtil;


import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        BundleTest.class,
        NixieClockActionTest.class,
        NixieClockFrameTest.class,
})

/**
 * Invokes complete set of tests in the jmri.jmrit.nixieclock tree
 *
 * @author	Bob Jacobsen Copyright 2001, 2003, 2012
 */
public class PackageTest {
}
