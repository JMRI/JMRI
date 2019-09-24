package jmri.jmrit.logixng.digital.actions_with_change;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    jmri.jmrit.logixng.digital.actions_with_change.configureswing.PackageTest.class,
    jmri.jmrit.logixng.digital.actions_with_change.configurexml.PackageTest.class,
    OnChangeActionTest.class,
})

/**
 * Invokes complete set of tests in the jmri.jmrit.logixng.actions tree
 *
 * @author Daniel Bergqvist 2018
 */
public class PackageTest {
}
