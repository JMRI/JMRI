package jmri.jmrit.logixng.expressions;

import java.util.Locale;

import org.junit.Assert;
import org.junit.Test;

import jmri.NamedBean;
import jmri.jmrit.logixng.AbstractBaseTestBase;
import jmri.jmrit.logixng.DigitalExpressionBean;
import jmri.jmrit.logixng.implementation.DefaultMaleDigitalExpressionSocket.DigitalExpressionDebugConfig;

/**
 * Base class for classes that tests DigitalAction
 */
public abstract class AbstractDigitalExpressionTestBase extends AbstractBaseTestBase {

    public abstract NamedBean createNewBean(String systemName) throws Exception;
    
    @Test
    public void testBadSystemName() throws Exception {
        boolean hasThrown = false;
        try {
            // Create a bean with bad system name. This must throw an exception
            NamedBean bean = createNewBean("IQ111");
            // We should never get here.
            Assert.assertNotNull("Bean is not null", bean);
        } catch (IllegalArgumentException e) {
            Assert.assertEquals("Exception is correct", "system name is not valid", e.getMessage());
            hasThrown = true;
        }
        Assert.assertTrue("Exception is thrown", hasThrown);
    }
    
    @Test
    public void testBundle() {
        Assert.assertEquals("strings are equal", "Compare memory", Bundle.getMessage("Memory_Short"));
        Assert.assertEquals("strings are equal", "Memory IM1 is null", Bundle.getMessage("Memory_Long_CompareNull", "IM1", Bundle.getMessage("MemoryOperation_IsNull")));
        Assert.assertEquals("strings are equal", "Compare memory", Bundle.getMessage(Locale.CANADA, "Memory_Short"));
        Assert.assertEquals("strings are equal", "Memory IM1 is null", Bundle.getMessage(Locale.CANADA, "Memory_Long_CompareNull", "IM1", Bundle.getMessage("MemoryOperation_IsNull")));
    }
    
    @Test
    public void testGetBeanType() {
        Assert.assertTrue("String matches", "Digital expression".equals(((DigitalExpressionBean)_base).getBeanType()));
    }
    
    @Test
    public void testEnableAndEvaluate() throws Exception {
        DigitalExpressionBean _expression = (DigitalExpressionBean)_baseMaleSocket;
        Assert.assertTrue("male socket is enabled", _baseMaleSocket.isEnabled());
        Assert.assertTrue("evaluate() returns true", _expression.evaluate());
        _baseMaleSocket.setEnabled(false);
        Assert.assertFalse("male socket is disabled", _baseMaleSocket.isEnabled());
        Assert.assertFalse("evaluate() returns false", _expression.evaluate());
        _baseMaleSocket.setEnabled(true);
        Assert.assertTrue("male socket is enabled", _baseMaleSocket.isEnabled());
        Assert.assertTrue("evaluate() returns true", _expression.evaluate());
    }
    
    @Test
    public void testDebugConfig() throws Exception {
        DigitalExpressionBean _expression = (DigitalExpressionBean)_baseMaleSocket;
        Assert.assertTrue("evaluate() returns true", _expression.evaluate());
        DigitalExpressionDebugConfig debugConfig = new DigitalExpressionDebugConfig();
        debugConfig._forceResult = true;
        debugConfig._result = false;
        _baseMaleSocket.setDebugConfig(debugConfig);
        Assert.assertFalse("evaluate() returns true", _expression.evaluate());
        debugConfig._result = true;
        Assert.assertTrue("evaluate() returns true", _expression.evaluate());
        debugConfig._result = false;
        Assert.assertFalse("evaluate() returns true", _expression.evaluate());
        debugConfig._forceResult = false;
        Assert.assertTrue("evaluate() returns true", _expression.evaluate());
    }
    
}
