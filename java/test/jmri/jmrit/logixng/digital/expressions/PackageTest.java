package jmri.jmrit.logixng.digital.expressions;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    jmri.jmrit.logixng.digital.expressions.configureswing.PackageTest.class,
    jmri.jmrit.logixng.digital.expressions.configurexml.PackageTest.class,
    AbstractDigitalExpressionTest.class,
    AndTest.class,
    AntecedentTest.class,
    ExpressionScriptTest.class,
    FalseTest.class,
    HoldTest.class,
    OrTest.class,
    ResetOnTrueTest.class,
    TimerTest.class,
    TriggerOnceTest.class,
    TrueTest.class,
    ExpressionLightTest.class,
    ExpressionReferenceTest.class,
    ExpressionSensorTest.class,
    ExpressionTurnoutTest.class,
})

/**
 * Invokes complete set of tests in the jmri.jmrit.logixng.expressions tree
 *
 * @author Daniel Bergqvist 2018
 */
public class PackageTest {
}
