package jmri.jmrit.logixng.string.implementation;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    jmri.jmrit.logixng.string.implementation.configurexml.PackageTest.class,
    DefaultFemaleStringActionSocketTest.class,
    DefaultFemaleStringExpressionSocketTest.class,
    DefaultMaleStringActionSocketTest.class,
    DefaultMaleStringExpressionSocketTest.class,
})

/**
 * Invokes complete set of tests in the jmri.jmrit.logixng.engine tree
 *
 * @author Daniel Bergqvist 2018
 */
public class PackageTest {

}
