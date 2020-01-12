package jmri.jmrit.logixng.ztest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    LogixNG_StartupTest.class,
    TestLogixNG_Test.class,
})

/**
 * Invokes complete set of tests in the jmri.jmrit.logixng.util tree
 *
 * @author Daniel Bergqvist 2018
 */
public class PackageTest {
}
