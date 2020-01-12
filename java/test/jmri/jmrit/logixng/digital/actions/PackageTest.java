package jmri.jmrit.logixng.digital.actions;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    jmri.jmrit.logixng.digital.actions.swing.PackageTest.class,
    jmri.jmrit.logixng.digital.actions.configurexml.PackageTest.class,
    AbstractDigitalActionTest.class,
    ActionAtomicBooleanTest.class,
    ActionLightTest.class,
    ActionListenOnBeansTest.class,
    ActionMemoryTest.class,
    ActionScriptTest.class,
    ActionSensorTest.class,
    ActionThrottleTest.class,
    ActionTimerTest.class,
    ActionTurnoutTest.class,
    DigitalActionPluginSocketTest.class,
    DoAnalogActionTest.class,
    DoStringActionTest.class,
    IfThenElseTest.class,
    ManyTest.class,
    ShutdownComputerTest.class,
    SocketTest.class,
})

/**
 * Invokes complete set of tests in the jmri.jmrit.logixng.actions tree
 *
 * @author Daniel Bergqvist 2018
 */
public class PackageTest {
}
