package jmri.jmrit.logixng.digital.boolean_actions;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    jmri.jmrit.logixng.digital.boolean_actions.swing.PackageTest.class,
    jmri.jmrit.logixng.digital.boolean_actions.configurexml.PackageTest.class,
    OnChangeActionTest.class,
})

/**
 * Invokes complete set of tests in the jmri.jmrit.logixng.actions tree
 *
 * @author Daniel Bergqvist 2018
 */
public class PackageTest {
}
