package jmri.jmrit.logixng.string.implementation;

import java.util.Locale;
import jmri.InstanceManager;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.Category;
import jmri.jmrit.logixng.FemaleSocket;
import jmri.jmrit.logixng.MaleSocketTestBase;
import jmri.jmrit.logixng.StringExpressionManager;
import jmri.jmrit.logixng.StringExpressionBean;
import jmri.jmrit.logixng.string.expressions.AbstractStringExpression;
import jmri.jmrit.logixng.string.expressions.StringExpressionMemory;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test ExpressionTimer
 * 
 * @author Daniel Bergqvist 2018
 */
public class DefaultMaleStringExpressionSocketTest extends MaleSocketTestBase {

    @Override
    protected String getNewSystemName() {
        return InstanceManager.getDefault(StringExpressionManager.class)
                .getAutoSystemName();
    }
    
    @Test
    public void testCtor() {
        StringExpressionBean expression = new StringExpressionMemory("IQSE321", null);
        Assert.assertNotNull("exists", new DefaultMaleStringExpressionSocket(expression));
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
        
        StringExpressionBean actionA = new StringExpressionMemory("IQSE321", null);
        Assert.assertNotNull("exists", actionA);
        StringExpressionBean actionB = new MyStringExpression("IQSE322");
        Assert.assertNotNull("exists", actionA);
        
        maleSocketA =
                InstanceManager.getDefault(StringExpressionManager.class)
                        .registerExpression(actionA);
        Assert.assertNotNull("exists", maleSocketA);
        
        maleSocketB =
                InstanceManager.getDefault(StringExpressionManager.class)
                        .registerExpression(actionB);
        Assert.assertNotNull("exists", maleSocketA);
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    
    
    /**
     * This expression is different from StringExpressionMemory and is used to test the
     * male socket.
     */
    private class MyStringExpression extends AbstractStringExpression {
        
        MyStringExpression(String sysName) {
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
        public String evaluate() {
            throw new UnsupportedOperationException("Not supported.");
        }
        
        @Override
        public void reset() {
            throw new UnsupportedOperationException("Not supported.");
        }
        
    }
    
}
