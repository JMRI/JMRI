package jmri.jmrit.logixng.expressions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import jmri.StringIO;
import jmri.JmriException;
import jmri.NamedBean;
import jmri.jmrit.logixng.StringExpressionBean;
import jmri.jmrit.logixng.AbstractBaseTestBase;
import jmri.jmrit.logixng.implementation.DefaultMaleStringExpressionSocket.StringExpressionDebugConfig;
import jmri.util.JUnitAppender;

import org.junit.jupiter.api.Test;

/**
 * Test AbstractStringExpression
 * 
 * @author Daniel Bergqvist 2018
 */
public abstract class AbstractStringExpressionTestBase extends AbstractBaseTestBase {

    public abstract NamedBean createNewBean(String systemName);

    @Test
    public void testBadSystemName() {
        IllegalArgumentException e = assertThrows( IllegalArgumentException.class, () -> {
            // Create a bean with bad system name. This must throw an exception
            NamedBean bean = createNewBean("IQ111");
            // We should never get here.
            fail("Bean is not null " +  bean);
        });
        assertEquals( "system name is not valid", e.getMessage(), "Exception is correct");
    }

    @Test
    public void testGetBeanType() {
        assertEquals( "String expression", ((StringExpressionBean)_base).getBeanType(), "String matches");
    }

    @Test
    public void testState() throws JmriException {
        StringExpressionBean _expression = (StringExpressionBean)_base;
        _expression.setState(StringIO.INCONSISTENT);
        JUnitAppender.assertWarnMessage("Unexpected call to setState in AbstractStringExpression.");
        assertSame( StringIO.INCONSISTENT, _expression.getState(), "State matches");
        JUnitAppender.assertWarnMessage("Unexpected call to getState in AbstractStringExpression.");
        _expression.setState(StringIO.UNKNOWN);
        JUnitAppender.assertWarnMessage("Unexpected call to setState in AbstractStringExpression.");
        assertSame( StringIO.UNKNOWN, _expression.getState(), "State matches");
        JUnitAppender.assertWarnMessage("Unexpected call to getState in AbstractStringExpression.");
        _expression.setState(StringIO.INCONSISTENT);
        JUnitAppender.assertWarnMessage("Unexpected call to setState in AbstractStringExpression.");
        assertSame( StringIO.INCONSISTENT, _expression.getState(), "State matches");
        JUnitAppender.assertWarnMessage("Unexpected call to getState in AbstractStringExpression.");
    }

    @Test
    public void testEnableAndEvaluate() throws JmriException {
        StringExpressionBean _expression = (StringExpressionBean)_baseMaleSocket;
        assertTrue( _baseMaleSocket.isEnabled(), "male socket is enabled");
        assertNotEquals( "", _expression.evaluate(), "Strings don't match");
        _baseMaleSocket.setEnabled(false);
        assertFalse( _baseMaleSocket.isEnabled(), "male socket is disabled");
        assertEquals( "", _expression.evaluate(), "Strings match");
        _baseMaleSocket.setEnabled(true);
        assertTrue( _baseMaleSocket.isEnabled(), "male socket is enabled");
        assertNotEquals( "", _expression.evaluate(), "Strings don't match");
    }

    @Test
    public void testDebugConfig() throws JmriException {
        String value1 = "Something else";
        String value2 = "Some other thing";
        StringExpressionBean _expression = (StringExpressionBean)_baseMaleSocket;
        assertNotEquals( value1, _expression.evaluate(), "Strings don't match");
        assertNotEquals( value2, _expression.evaluate(), "Strings don't match");
        StringExpressionDebugConfig debugConfig = new StringExpressionDebugConfig();
        debugConfig._forceResult = true;
        debugConfig._result = value1;
        _baseMaleSocket.setDebugConfig(debugConfig);
        assertEquals( value1, _expression.evaluate(), "String match");
        debugConfig._result = value2;
        assertEquals( value2, _expression.evaluate(), "String match");
        debugConfig._forceResult = false;
        assertNotEquals( value1, _expression.evaluate(), "String don't match");
        assertNotEquals( value2, _expression.evaluate(), "String don't match");
    }

}
