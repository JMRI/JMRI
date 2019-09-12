package jmri.jmrit.logixng.analog.implementation;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    jmri.jmrit.logixng.analog.implementation.configurexml.PackageTest.class,
    DefaultFemaleAnalogActionSocketTest.class,
    DefaultFemaleAnalogExpressionSocketTest.class,
    DefaultMaleAnalogActionSocketTest.class,
    DefaultMaleAnalogExpressionSocketTest.class,
})

/**
 * Invokes complete set of tests in the jmri.jmrit.logixng.engine tree
 *
 * @author Daniel Bergqvist 2018
 */
public class PackageTest {

}
