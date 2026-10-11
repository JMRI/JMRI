package jmri.jmrit.logixng.expressions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import jmri.AnalogIO;
import jmri.JmriException;
import jmri.NamedBean;
import jmri.jmrit.logixng.AnalogExpressionBean;
import jmri.jmrit.logixng.AbstractBaseTestBase;
import jmri.jmrit.logixng.implementation.DefaultMaleAnalogExpressionSocket.AnalogExpressionDebugConfig;
import jmri.util.JUnitAppender;

import org.junit.jupiter.api.Test;

/**
 * Test AbstractAnalogExpression
 * 
 * @author Daniel Bergqvist 2018
 */
public abstract class AbstractAnalogExpressionTestBase extends AbstractBaseTestBase {

    public abstract NamedBean createNewBean(String systemName);

    @Test
    public void testBadSystemName() {
        IllegalArgumentException e = assertThrows( IllegalArgumentException.class, () -> {
            // Create a bean with bad system name. This must throw an exception
            NamedBean bean = createNewBean("IQ111");
            // We should never get here.
            fail( "Bean is not null " + bean);
        }, "Exception is thrown");
        assertEquals( "system name is not valid", e.getMessage(), "Exception is correct");
    }

    @Test
    public void testGetBeanType() {
        assertEquals( "Analog expression", ((AnalogExpressionBean)_base).getBeanType(), "String matches");
    }

    @Test
    public void testState() throws JmriException {
        AnalogExpressionBean _expression = (AnalogExpressionBean)_base;
        _expression.setState(AnalogIO.INCONSISTENT);
        JUnitAppender.assertWarnMessage("Unexpected call to setState in AbstractAnalogExpression.");
        assertSame( AnalogIO.INCONSISTENT, _expression.getState(), "State matches");
        JUnitAppender.assertWarnMessage("Unexpected call to getState in AbstractAnalogExpression.");
        _expression.setState(AnalogIO.UNKNOWN);
        JUnitAppender.assertWarnMessage("Unexpected call to setState in AbstractAnalogExpression.");
        assertSame( AnalogIO.UNKNOWN, _expression.getState(), "State matches");
        JUnitAppender.assertWarnMessage("Unexpected call to getState in AbstractAnalogExpression.");
        _expression.setState(AnalogIO.INCONSISTENT);
        JUnitAppender.assertWarnMessage("Unexpected call to setState in AbstractAnalogExpression.");
        assertSame( AnalogIO.INCONSISTENT, _expression.getState(), "State matches");
        JUnitAppender.assertWarnMessage("Unexpected call to getState in AbstractAnalogExpression.");
    }

    @Test
    public void testEnableAndEvaluate() throws JmriException {
        AnalogExpressionBean _expression = (AnalogExpressionBean)_baseMaleSocket;
        assertTrue( _baseMaleSocket.isEnabled(), "male socket is enabled");
        assertNotEquals( 0.0, _expression.evaluate(), "Double don't match");
        _baseMaleSocket.setEnabled(false);
        assertFalse( _baseMaleSocket.isEnabled(), "male socket is disabled");
        assertEquals( 0.0, _expression.evaluate(), 0, "Double match");
        _baseMaleSocket.setEnabled(true);
        assertTrue( _baseMaleSocket.isEnabled(), "male socket is enabled");
        assertNotEquals( 0.0, _expression.evaluate(), "Double don't match");
    }
    
    @Test
    public void testDebugConfig() throws JmriException {
        double value1 = 88.99;
        double value2 = 99.88;
        AnalogExpressionBean _expression = (AnalogExpressionBean)_baseMaleSocket;
        assertNotEquals( value1, _expression.evaluate(), "Double don't match");
        assertNotEquals( value2, _expression.evaluate(), "Double don't match");
        AnalogExpressionDebugConfig debugConfig = new AnalogExpressionDebugConfig();
        debugConfig._forceResult = true;
        debugConfig._result = value1;
        _baseMaleSocket.setDebugConfig(debugConfig);
        assertEquals( value1, _expression.evaluate(), 0, "Double match");
        debugConfig._result = value2;
        assertEquals( value2, _expression.evaluate(), 0, "Double match");
        debugConfig._forceResult = false;
        assertNotEquals( value1, _expression.evaluate(), "Double don't match");
        assertNotEquals( value2, _expression.evaluate(), "Double don't match");
    }
    
}
