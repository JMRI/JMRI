package jmri.jmrit.logixng.implementation;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    jmri.jmrit.logixng.implementation.configurexml.PackageTest.class,
    DefaultAnonymousTableTest.class,
    DefaultFemaleGenericExpressionSocketTest.class,
    DefaultLogixNGManagerTest.class,
    DefaultNamedTableTest.class,
})

/**
 * Invokes complete set of tests in the jmri.jmrit.logixng.engine tree
 *
 * @author Daniel Bergqvist 2018
 */
public class PackageTest {
}
