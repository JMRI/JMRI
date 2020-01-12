package jmri.jmrit.logixng.digital.expressions.swing;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    AndSwingTest.class,
    AntecedentSwingTest.class,
    ExpressionLightSwingTest.class,
    ExpressionSensorSwingTest.class,
    ExpressionTurnoutSwingTest.class,
    FalseSwingTest.class,
    HoldSwingTest.class,
    OrSwingTest.class,
    ResetOnTrueSwingTest.class,
    TimerSwingTest.class,
    TriggerOnceSwingTest.class,
    TrueSwingTest.class,
})

/**
 * Invokes complete set of tests in the jmri.jmrit.logixng.actions.configurexml tree
 *
 * @author Daniel Bergqvist 2018
 */
public class PackageTest {

}
