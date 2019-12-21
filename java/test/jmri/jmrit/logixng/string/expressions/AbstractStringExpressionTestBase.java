package jmri.jmrit.logixng.string.expressions;

import java.util.Locale;
import jmri.StringIO;
import jmri.JmriException;
import jmri.NamedBean;
import jmri.jmrit.logixng.StringExpressionBean;
import jmri.jmrit.logixng.AbstractBaseTestBase;
import jmri.jmrit.logixng.string.implementation.DefaultMaleStringExpressionSocket.StringExpressionDebugConfig;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test AbstractStringExpression
 * 
 * @author Daniel Bergqvist 2018
 */
public abstract class AbstractStringExpressionTestBase extends AbstractBaseTestBase {

    @Test
    public void testBundle() {
        Assert.assertEquals("strings are equal", "Get memory", Bundle.getMessage("StringExpressionMemory0"));
        Assert.assertEquals("strings are equal", "Get memory IM1", Bundle.getMessage("StringExpressionMemory1", "IM1"));
        Assert.assertEquals("strings are equal", "Get memory", Bundle.getMessage(Locale.CANADA, "StringExpressionMemory0"));
        Assert.assertEquals("strings are equal", "Get memory IM1", Bundle.getMessage(Locale.CANADA, "StringExpressionMemory1", "IM1"));
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
    public void testEnableAndEvaluate() {
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
    public void testDebugConfig() {
        String value1 = "Something";
        String value2 = "Some other thing";
        StringExpressionBean _expression = (StringExpressionBean)_baseMaleSocket;
        Assert.assertNotEquals("Double don't match", value1, _expression.evaluate());
        Assert.assertNotEquals("Double don't match", value2, _expression.evaluate());
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
