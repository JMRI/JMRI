package jmri.jmrit.logixng.string.expressions;

import java.util.Locale;
import jmri.StringIO;
import jmri.JmriException;
import jmri.jmrit.logixng.StringExpressionBean;
import jmri.jmrit.logixng.AbstractBaseTestBase;
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
    
}
