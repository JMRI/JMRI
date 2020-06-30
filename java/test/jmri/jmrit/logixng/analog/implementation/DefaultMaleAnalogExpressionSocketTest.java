package jmri.jmrit.logixng.analog.implementation;

import java.util.Locale;
import jmri.InstanceManager;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import jmri.jmrit.logixng.AnalogExpressionBean;
import jmri.jmrit.logixng.AnalogExpressionManager;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.Category;
import jmri.jmrit.logixng.FemaleSocket;
import jmri.jmrit.logixng.MaleSocketTestBase;
import jmri.jmrit.logixng.analog.expressions.AbstractAnalogExpression;
import jmri.jmrit.logixng.analog.expressions.AnalogExpressionMemory;

/**
 * Test ExpressionTimer
 * 
 * @author Daniel Bergqvist 2018
 */
public class DefaultMaleAnalogExpressionSocketTest extends MaleSocketTestBase {

    @Override
    protected String getNewSystemName() {
        return InstanceManager.getDefault(AnalogExpressionManager.class)
                .getAutoSystemName();
    }
    
    @Test
    public void testCtor() {
        AnalogExpressionBean expression = new AnalogExpressionMemory("IQAE321", null);
        Assert.assertNotNull("object exists", new DefaultMaleAnalogExpressionSocket(expression));
    }
    
    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initLogixNGManager();
        
        AnalogExpressionBean expressionA = new AnalogExpressionMemory("IQAE321", null);
        Assert.assertNotNull("exists", expressionA);
        AnalogExpressionBean expressionB = new MyAnalogExpression("IQAE322");
        Assert.assertNotNull("exists", expressionA);
        
        maleSocketA =
                InstanceManager.getDefault(AnalogExpressionManager.class)
                        .registerExpression(expressionA);
        Assert.assertNotNull("exists", maleSocketA);
        
        maleSocketB =
                InstanceManager.getDefault(AnalogExpressionManager.class)
                        .registerExpression(expressionB);
        Assert.assertNotNull("exists", maleSocketA);
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    
    
    /**
     * This action is different from AnalogExpressionMemory and is used to test the
     * male socket.
     */
    private class MyAnalogExpression extends AbstractAnalogExpression {
        
        MyAnalogExpression(String sysName) {
            super(sysName, null);
        }

        @Override
        protected void registerListenersForThisClass() {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        protected void unregisterListenersForThisClass() {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        protected void disposeMe() {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public String getShortDescription(Locale locale) {
            return "My short description";
        }

        @Override
        public String getLongDescription(Locale locale) {
            return "My long description";
        }

        @Override
        public FemaleSocket getChild(int index) throws IllegalArgumentException, UnsupportedOperationException {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public int getChildCount() {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public Category getCategory() {
            return Category.COMMON;
        }

        @Override
        public boolean isExternal() {
            return false;
        }

        @Override
        public void setup() {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public double evaluate() {
            throw new UnsupportedOperationException("Not supported.");
        }
        
        @Override
        public void reset() {
            throw new UnsupportedOperationException("Not supported.");
        }
        
    }
    
}
