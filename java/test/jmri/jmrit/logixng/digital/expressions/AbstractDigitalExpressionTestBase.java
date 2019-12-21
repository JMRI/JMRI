package jmri.jmrit.logixng.digital.expressions;

import org.junit.Assert;
import org.junit.Test;
import jmri.NamedBean;
import jmri.jmrit.logixng.AbstractBaseTestBase;
import jmri.jmrit.logixng.DigitalExpressionBean;
import jmri.jmrit.logixng.digital.implementation.DefaultMaleDigitalExpressionSocket.DigitalExpressionDebugConfig;

/**
 * Base class for classes that tests DigitalAction
 */
public abstract class AbstractDigitalExpressionTestBase extends AbstractBaseTestBase {

    public abstract NamedBean createNewBean(String systemName);
    
    @Test
    public void testBadSystemName() {
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
    public void testEnableAndEvaluate() {
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
    public void testDebugConfig() {
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
    
    @Test
    public void testChildAndChildCount() {
        Assert.assertEquals("childCount is equal", _base.getChildCount(), _baseMaleSocket.getChildCount());
        for (int i=0; i < _base.getChildCount(); i++) {
            Assert.assertTrue("child is equal", _base.getChild(i) == _baseMaleSocket.getChild(i));
        }
    }
    
    @Test
    public void testBeanType() {
        Assert.assertEquals("childCount is equal",
                ((NamedBean)_base).getBeanType(),
                ((NamedBean)_baseMaleSocket).getBeanType());
    }
    
    @Test
    public void testDescribeState() {
        Assert.assertEquals("description matches",
                "Unknown",
                ((NamedBean)_base).describeState(NamedBean.UNKNOWN));
    }
    
}
