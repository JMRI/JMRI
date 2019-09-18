package jmri.jmrit.logixng.analog.expressions;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    jmri.jmrit.logixng.analog.expressions.configurexml.PackageTest.class,
    AnalogExpressionConstantTest.class,
    AnalogExpressionMemoryTest.class,
})

/**
 * Invokes complete set of tests in the jmri.jmrit.logixng.analogexpressions tree
 *
 * @author Daniel Bergqvist 2018
 */
public class PackageTest {
}
