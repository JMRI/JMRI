package jmri.jmrit.logixng.expressions;

import java.util.Locale;

import jmri.StringIO;
import jmri.JmriException;
import jmri.NamedBean;
import jmri.jmrit.logixng.StringExpressionBean;
import jmri.jmrit.logixng.AbstractBaseTestBase;
import jmri.jmrit.logixng.implementation.DefaultMaleStringExpressionSocket.StringExpressionDebugConfig;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test AbstractStringExpression
 * 
 * @author Daniel Bergqvist 2018
 */
public abstract class AbstractStringExpressionTestBase extends AbstractBaseTestBase {

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
    public void testGetBeanType() {
        Assert.assertTrue("String matches", "String expression".equals(((StringExpressionBean)_base).getBeanType()));
    }
    
    @Test
    public void testState() throws JmriException {
        StringExpressionBean _expression = (StringExpressionBean)_base;
        _expression.setState(StringIO.INCONSISTENT);
        Assert.assertTrue("State matches", StringIO.INCONSISTENT == _expression.getState());
        jmri.util.JUnitAppender.assertWarnMessage("Unexpected call to getState in AbstractStringExpression.");
        _expression.setState(StringIO.UNKNOWN);
        Assert.assertTrue("State matches", StringIO.UNKNOWN == _expression.getState());
        jmri.util.JUnitAppender.assertWarnMessage("Unexpected call to getState in AbstractStringExpression.");
        _expression.setState(StringIO.INCONSISTENT);
        Assert.assertTrue("State matches", StringIO.INCONSISTENT == _expression.getState());
        jmri.util.JUnitAppender.assertWarnMessage("Unexpected call to getState in AbstractStringExpression.");
    }
    
    @Test
    public void testEnableAndEvaluate() throws Exception {
        StringExpressionBean _expression = (StringExpressionBean)_baseMaleSocket;
        Assert.assertTrue("male socket is enabled", _baseMaleSocket.isEnabled());
        Assert.assertNotEquals("Strings don't match", "", _expression.evaluate());
        _baseMaleSocket.setEnabled(false);
        Assert.assertFalse("male socket is disabled", _baseMaleSocket.isEnabled());
        Assert.assertEquals("Strings match", "", _expression.evaluate());
        _baseMaleSocket.setEnabled(true);
        Assert.assertTrue("male socket is enabled", _baseMaleSocket.isEnabled());
        Assert.assertNotEquals("Strings don't match", "", _expression.evaluate());
    }
    
    @Test
    public void testDebugConfig() throws Exception {
        String value1 = "Something else";
        String value2 = "Some other thing";
        StringExpressionBean _expression = (StringExpressionBean)_baseMaleSocket;
        Assert.assertNotEquals("Strings don't match", value1, _expression.evaluate());
        Assert.assertNotEquals("Strings don't match", value2, _expression.evaluate());
        StringExpressionDebugConfig debugConfig = new StringExpressionDebugConfig();
        debugConfig._forceResult = true;
        debugConfig._result = value1;
        _baseMaleSocket.setDebugConfig(debugConfig);
        Assert.assertEquals("String match", value1, _expression.evaluate());
        debugConfig._result = value2;
        Assert.assertEquals("String match", value2, _expression.evaluate());
        debugConfig._forceResult = false;
        Assert.assertNotEquals("String don't match", value1, _expression.evaluate());
        Assert.assertNotEquals("String don't match", value2, _expression.evaluate());
    }
    
}
