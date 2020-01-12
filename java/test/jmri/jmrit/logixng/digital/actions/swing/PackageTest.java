package jmri.jmrit.logixng.digital.actions.swing;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    ActionLightSwingTest.class,
    ActionSensorSwingTest.class,
    ActionTurnoutSwingTest.class,
    DoAnalogActionSwingTest.class,
    DoStringActionSwingTest.class,
    IfThenElseSwingTest.class,
    ManySwingTest.class,
    ShutdownComputerSwingTest.class,
})

/**
 * Invokes complete set of tests in the jmri.jmrit.logixng.actions.configurexml tree
 *
 * @author Daniel Bergqvist 2018
 */
public class PackageTest {

}
