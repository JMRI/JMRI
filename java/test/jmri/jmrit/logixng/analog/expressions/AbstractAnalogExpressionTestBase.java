package jmri.jmrit.logixng.analog.expressions;

import java.util.Locale;
import jmri.AnalogIO;
import jmri.JmriException;
import jmri.jmrit.logixng.AnalogExpressionBean;
import jmri.jmrit.logixng.AbstractBaseTestBase;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test AbstractAnalogExpression
 * 
 * @author Daniel Bergqvist 2018
 */
public abstract class AbstractAnalogExpressionTestBase extends AbstractBaseTestBase {

    @Test
    public void testBundle() {
        Assert.assertEquals("strings are equal", "Get memory", Bundle.getMessage("AnalogExpressionMemory0"));
        Assert.assertEquals("strings are equal", "Get memory IM1", Bundle.getMessage("AnalogExpressionMemory1", "IM1"));
        Assert.assertEquals("strings are equal", "Get memory", Bundle.getMessage(Locale.CANADA, "AnalogExpressionMemory0"));
        Assert.assertEquals("strings are equal", "Get memory IM1", Bundle.getMessage(Locale.CANADA, "AnalogExpressionMemory1", "IM1"));
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
    
}
