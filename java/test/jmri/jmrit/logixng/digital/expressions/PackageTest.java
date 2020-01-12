package jmri.jmrit.logixng.digital.expressions;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    jmri.jmrit.logixng.digital.expressions.swing.PackageTest.class,
    jmri.jmrit.logixng.digital.expressions.configurexml.PackageTest.class,
    AbstractDigitalExpressionTest.class,
    AbstractScriptDigitalExpressionTest.class,
    AndTest.class,
    AntecedentTest.class,
    ExpressionLightTest.class,
    ExpressionMemoryTest.class,
    ExpressionReferenceTest.class,
    ExpressionScriptTest.class,
    ExpressionSensorTest.class,
    ExpressionTurnoutTest.class,
    FormulaTest.class,
    FalseTest.class,
    HoldTest.class,
    OrTest.class,
    ResetOnTrueTest.class,
    ExpressionTimerTest.class,
    TriggerOnceTest.class,
    TrueTest.class,
})

/**
 * Invokes complete set of tests in the jmri.jmrit.logixng.expressions tree
 *
 * @author Daniel Bergqvist 2018
 */
public class PackageTest {
}
