package jmri.jmrit.cabsignals;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        BundleTest.class,
        CabSignalActionTest.class,
        CabSignalPaneTest.class,
        CabSignalTableModelTest.class
})

/**
 * Invokes complete set of tests in the jmri.jmrit tree
 *
 * @author	Bob Jacobsen Copyright 2001, 2003, 2012
 */
public class PackageTest {
}

