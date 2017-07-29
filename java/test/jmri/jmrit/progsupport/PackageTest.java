package jmri.jmrit.progsupport;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Invokes complete set of tests in the jmri.jmrit.progsupport tree
 *
 * @author	Bob Jacobsen Copyright 2001, 2003, 2012
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    ProgServiceModePaneTest.class,
    BundleTest.class,
    ProgModeExceptionTest.class,
    ProgDeferredServiceModePaneTest.class,
    ProgServiceModeComboBoxTest.class,
    ProgModePaneTest.class,
    ProgOpsModePaneTest.class
})
public class PackageTest {
}
