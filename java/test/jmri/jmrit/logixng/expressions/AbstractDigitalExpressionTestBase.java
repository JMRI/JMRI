package jmri.jmrit.logixng.expressions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Locale;

import jmri.JmriException;
import jmri.NamedBean;
import jmri.jmrit.logixng.AbstractBaseTestBase;
import jmri.jmrit.logixng.DigitalExpressionBean;
import jmri.jmrit.logixng.implementation.DefaultMaleDigitalExpressionSocket.DigitalExpressionDebugConfig;

import org.junit.jupiter.api.Test;

/**
 * Base class for classes that tests DigitalAction
 */
public abstract class AbstractDigitalExpressionTestBase extends AbstractBaseTestBase {

    public abstract NamedBean createNewBean(String systemName);

    @Test
    public void testBadSystemName() {

        IllegalArgumentException e = assertThrows( IllegalArgumentException.class, () -> {
            // Create a bean with bad system name. This must throw an exception
            NamedBean bean = createNewBean("IQ111");
            // We should never get here.
            fail("Bean is not null " + bean);
        }, "Exception is thrown");
        assertEquals( "system name is not valid", e.getMessage(), "Exception is correct");
    }

    @Test
    public void testBundle() {
        assertEquals( "Memory", Bundle.getMessage("Memory_Short"), "strings are equal");
        assertEquals( "Memory IM1 is null", Bundle.getMessage("Memory_Long_CompareNull", "IM1", Bundle.getMessage("MemoryOperation_IsNull")), "strings are equal");
        assertEquals( "Memory", Bundle.getMessage(Locale.CANADA, "Memory_Short"), "strings are equal");
        assertEquals( "Memory IM1 is null", Bundle.getMessage(Locale.CANADA, "Memory_Long_CompareNull", "IM1", Bundle.getMessage("MemoryOperation_IsNull")), "strings are equal");
    }

    @Test
    public void testGetBeanType() {
        assertEquals( "Digital expression", ((DigitalExpressionBean)_base).getBeanType(), "String matches");
    }

    @Test
    public void testEnableAndEvaluate() throws JmriException {
        DigitalExpressionBean _expression = (DigitalExpressionBean)_baseMaleSocket;
        assertTrue( _baseMaleSocket.isEnabled(), "male socket is enabled");
        assertTrue( _expression.evaluate(), "evaluate() returns true");
        _baseMaleSocket.setEnabled(false);
        assertFalse( _baseMaleSocket.isEnabled(), "male socket is disabled");
        assertFalse( _expression.evaluate(), "evaluate() returns false");
        _baseMaleSocket.setEnabled(true);
        assertTrue( _baseMaleSocket.isEnabled(), "male socket is enabled");
        assertTrue( _expression.evaluate(), "evaluate() returns true");
    }

    @Test
    public void testDebugConfig() throws JmriException {
        DigitalExpressionBean _expression = (DigitalExpressionBean)_baseMaleSocket;
        assertTrue( _expression.evaluate(), "evaluate() returns true");
        DigitalExpressionDebugConfig debugConfig = new DigitalExpressionDebugConfig();
        debugConfig._forceResult = true;
        debugConfig._result = false;
        _baseMaleSocket.setDebugConfig(debugConfig);
        assertFalse( _expression.evaluate(), "evaluate() returns true");
        debugConfig._result = true;
        assertTrue( _expression.evaluate(), "evaluate() returns true");
        debugConfig._result = false;
        assertFalse( _expression.evaluate(), "evaluate() returns true");
        debugConfig._forceResult = false;
        assertTrue( _expression.evaluate(), "evaluate() returns true");
    }
    
}
