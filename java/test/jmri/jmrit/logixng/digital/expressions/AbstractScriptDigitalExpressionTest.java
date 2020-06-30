package jmri.jmrit.logixng.digital.expressions;

import java.beans.PropertyVetoException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import jmri.InstanceManager;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.Category;
import jmri.jmrit.logixng.DigitalExpression;
import jmri.jmrit.logixng.DigitalExpressionManager;
import jmri.jmrit.logixng.FemaleSocket;
import jmri.jmrit.logixng.FemaleSocketListener;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.implementation.AbstractFemaleSocket;

/**
 * Base class for classes that tests DigitalAction
 */
public class AbstractScriptDigitalExpressionTest {

    @Test
    public void testMethods() throws PropertyVetoException {
        ExpressionScript expressionScript = new ExpressionScript("IQDE321", null);
        MyExpression expression = new MyExpression(expressionScript);
        
        // This method calls AbstractScriptDigitalExpression.registerScriptListeners()
        // which doesn't do anything, but we do it for coverage.
        expression.registerListenersForThisClass();
        // Call the method twice to get coverage of the check of _listenersAreRegistered
        expression.registerListenersForThisClass();
        
        // This method calls AbstractScriptDigitalExpression.unregisterScriptListeners()
        // which doesn't do anything, but we do it for coverage.
        expression.unregisterListenersForThisClass();
        // Call the method twice to get coverage of the check of _listenersAreRegistered
        expression.unregisterListenersForThisClass();
        
        // This method does nothing but we do it for coverage.
        expression.reset();
        
        // This method does nothing but we do it for coverage.
        expression.setup();
        
        // This method does nothing but we do it for coverage.
        expression.vetoableChange(null);
        
        // Test getCategory()
        boolean hasThrown = false;
        try {
            expression.getCategory();
        } catch (RuntimeException e) {
            Assert.assertEquals("Exception is correct", "Not supported.", e.getMessage());
            hasThrown = true;
        }
        Assert.assertTrue("Exception is thrown", hasThrown);
        
        // Test isExternal()
        hasThrown = false;
        try {
            expression.isExternal();
        } catch (RuntimeException e) {
            Assert.assertEquals("Exception is correct", "Not supported.", e.getMessage());
            hasThrown = true;
        }
        Assert.assertTrue("Exception is thrown", hasThrown);
        
        // Test getChildCount()
        Assert.assertEquals("getChildCount() returns 0", 0, expression.getChildCount());
        
        // Test getChild()
        hasThrown = false;
        try {
            expression.getChild(0);
        } catch (RuntimeException e) {
            Assert.assertEquals("Exception is correct", "Not supported.", e.getMessage());
            hasThrown = true;
        }
        Assert.assertTrue("Exception is thrown", hasThrown);
        
        // Test getShortDescription()
        hasThrown = false;
        try {
            expression.getShortDescription();
        } catch (RuntimeException e) {
            Assert.assertEquals("Exception is correct", "Not supported.", e.getMessage());
            hasThrown = true;
        }
        Assert.assertTrue("Exception is thrown", hasThrown);
        
        // Test getLongDescription()
        hasThrown = false;
        try {
            expression.getLongDescription();
        } catch (RuntimeException e) {
            Assert.assertEquals("Exception is correct", "Not supported.", e.getMessage());
            hasThrown = true;
        }
        Assert.assertTrue("Exception is thrown", hasThrown);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initLogixNGManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    
    
    private static class MyExpression extends AbstractScriptDigitalExpression {

        public MyExpression(DigitalExpression expression) {
            super(expression);
        }

        @Override
        public boolean evaluate() {
            throw new UnsupportedOperationException("Not supported.");
        }
        
    }
    
}
