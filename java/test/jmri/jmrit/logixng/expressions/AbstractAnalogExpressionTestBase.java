package jmri.jmrit.logixng.expressions;

import java.util.Locale;
import jmri.AnalogIO;
import jmri.JmriException;
import jmri.NamedBean;
import jmri.jmrit.logixng.AnalogExpressionBean;
import jmri.jmrit.logixng.AbstractBaseTestBase;
import jmri.jmrit.logixng.implementation.DefaultMaleAnalogExpressionSocket.AnalogExpressionDebugConfig;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test AbstractAnalogExpression
 * 
 * @author Daniel Bergqvist 2018
 */
public abstract class AbstractAnalogExpressionTestBase extends AbstractBaseTestBase {

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
        Assert.assertTrue("String matches", "Analog expression".equals(((AnalogExpressionBean)_base).getBeanType()));
    }
    
    @Test
    public void testState() throws JmriException {
        AnalogExpressionBean _expression = (AnalogExpressionBean)_base;
        _expression.setState(AnalogIO.INCONSISTENT);
        Assert.assertTrue("State matches", AnalogIO.INCONSISTENT == _expression.getState());
        jmri.util.JUnitAppender.assertWarnMessage("Unexpected call to getState in AbstractAnalogExpression.");
        _expression.setState(AnalogIO.UNKNOWN);
        Assert.assertTrue("State matches", AnalogIO.UNKNOWN == _expression.getState());
        jmri.util.JUnitAppender.assertWarnMessage("Unexpected call to getState in AbstractAnalogExpression.");
        _expression.setState(AnalogIO.INCONSISTENT);
        Assert.assertTrue("State matches", AnalogIO.INCONSISTENT == _expression.getState());
        jmri.util.JUnitAppender.assertWarnMessage("Unexpected call to getState in AbstractAnalogExpression.");
    }
    
    @Test
    public void testEnableAndEvaluate() throws Exception {
        AnalogExpressionBean _expression = (AnalogExpressionBean)_baseMaleSocket;
        Assert.assertTrue("male socket is enabled", _baseMaleSocket.isEnabled());
        Assert.assertNotEquals("Double don't match", 0.0, _expression.evaluate());
        _baseMaleSocket.setEnabled(false);
        Assert.assertFalse("male socket is disabled", _baseMaleSocket.isEnabled());
        Assert.assertEquals("Double match", 0.0, _expression.evaluate(), 0);
        _baseMaleSocket.setEnabled(true);
        Assert.assertTrue("male socket is enabled", _baseMaleSocket.isEnabled());
        Assert.assertNotEquals("Double don't match", 0.0, _expression.evaluate());
    }
    
    @Test
    public void testDebugConfig() throws Exception {
        double value1 = 88.99;
        double value2 = 99.88;
        AnalogExpressionBean _expression = (AnalogExpressionBean)_baseMaleSocket;
        Assert.assertNotEquals("Double don't match", value1, _expression.evaluate());
        Assert.assertNotEquals("Double don't match", value2, _expression.evaluate());
        AnalogExpressionDebugConfig debugConfig = new AnalogExpressionDebugConfig();
        debugConfig._forceResult = true;
        debugConfig._result = value1;
        _baseMaleSocket.setDebugConfig(debugConfig);
        Assert.assertEquals("Double match", value1, _expression.evaluate(), 0);
        debugConfig._result = value2;
        Assert.assertEquals("Double match", value2, _expression.evaluate(), 0);
        debugConfig._forceResult = false;
        Assert.assertNotEquals("Double don't match", value1, _expression.evaluate());
        Assert.assertNotEquals("Double don't match", value2, _expression.evaluate());
    }
    
}
