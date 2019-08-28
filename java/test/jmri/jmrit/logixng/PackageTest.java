package jmri.jmrit.logixng;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    jmri.jmrit.logixng.analog.actions.PackageTest.class,
    jmri.jmrit.logixng.analog.expressions.PackageTest.class,
    jmri.jmrit.logixng.analog.implementation.PackageTest.class,
    jmri.jmrit.logixng.configurexml.PackageTest.class,
    jmri.jmrit.logixng.digital.actions.PackageTest.class,
    jmri.jmrit.logixng.digital.expressions.PackageTest.class,
    jmri.jmrit.logixng.digital.implementation.PackageTest.class,
    jmri.jmrit.logixng.digital.log.PackageTest.class,
    jmri.jmrit.logixng.implementation.PackageTest.class,
    jmri.jmrit.logixng.string.actions.PackageTest.class,
    jmri.jmrit.logixng.string.expressions.PackageTest.class,
    jmri.jmrit.logixng.string.implementation.PackageTest.class,
    jmri.jmrit.logixng.swing.PackageTest.class,
    jmri.jmrit.logixng.template.PackageTest.class,
    jmri.jmrit.logixng.tools.PackageTest.class,
    jmri.jmrit.logixng.util.parser.PackageTest.class,
    jmri.jmrit.logixng.ztest.LogixNG_StartupTest.class,
    DigitalExpressionTest.class,
    LogixNGCategoryTest.class,
    LogixNGTest.class,
    LogixNG_InstanceManagerTest.class,
    PluginManagerTest.class,
})

/**
 * Invokes complete set of tests in the jmri.jmrit.logixng tree
 *
 * @author Daniel Bergqvist 2018
 */
public class PackageTest {
}
