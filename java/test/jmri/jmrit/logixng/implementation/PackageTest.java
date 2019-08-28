package jmri.jmrit.logixng.implementation;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    jmri.jmrit.logixng.implementation.configurexml.PackageTest.class,
    JMRI_NativeNamespaceTest.class,
    DefaultFemaleGenericExpressionSocketTest.class,
    DefaultLogixNGManagerTest.class,
    DefaultLogixNGTest.class,
    DefaultLogixNG_InstanceManagerTest.class,
    JMRI_NativeNamespaceTest.class,
    VirtualNamespaceTest.class,
})

/**
 * Invokes complete set of tests in the jmri.jmrit.logixng.engine tree
 *
 * @author Daniel Bergqvist 2018
 */
public class PackageTest {
}
