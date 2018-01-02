package jmri.jmrit.picker;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        BundleTest.class,
        PickFrameTest.class,
        PickSinglePanelTest.class,
        PickPanelTest.class,
})

/**
 * Invokes complete set of tests in the jmri.jmrit.picker tree
 *
 * @author	Bob Jacobsen Copyright 2001, 2003, 2012
 */
public class PackageTest  {
}
