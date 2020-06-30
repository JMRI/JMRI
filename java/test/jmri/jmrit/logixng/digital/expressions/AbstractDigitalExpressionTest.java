package jmri.jmrit.logixng.digital.expressions;

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
import jmri.jmrit.logixng.DigitalExpressionManager;
import jmri.jmrit.logixng.FemaleSocket;
import jmri.jmrit.logixng.FemaleSocketListener;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.implementation.AbstractFemaleSocket;

/**
 * Test AbstractDigitalExpression
 */
public class AbstractDigitalExpressionTest {

    // This method is CPU intensive so we don't want to run it for every expression.
    @Test
    public void testGetNewSocketName() {
        MyExpression expression = new MyExpression();
        boolean hasThrown = false;
        try {
            // Create a bean with bad system name. This must throw an exception
            String socketName = expression.getNewSocketName();
            // We should never get here.
            Assert.assertNotNull("Name is not null", socketName);
        } catch (RuntimeException e) {
            Assert.assertEquals("Exception is correct", "Unable to find a new socket name", e.getMessage());
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
    
    
    // The purpose of this class is to test the method
    // AbstractDigitalAction.getNewSocketName(). We want
    // to test that the method throws an exception if no
    // available name can be found for a new child. The
    // method AbstractDigitalAction.getNewSocketName()
    // tries 10000 names before it gives up, and we don't
    // want to create 10000 new sockets only to check this,
    // so we cheat by only create one socket and then
    // change its name on every request of a new socket.
    private static class MyExpression extends AbstractDigitalExpression implements FemaleSocketListener {

        private final MyFemaleSocket child = new MyFemaleSocket(this, this, "E1");
        
        public MyExpression() {
            super(InstanceManager.getDefault(DigitalExpressionManager.class).getAutoSystemName(), null);
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
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public String getLongDescription(Locale locale) {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public FemaleSocket getChild(int index) throws IllegalArgumentException, UnsupportedOperationException {
            child.setName("E"+index);
            return child;
        }

        @Override
        public int getChildCount() {
            return Integer.MAX_VALUE;
        }

        @Override
        public Category getCategory() {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public boolean isExternal() {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public void setup() {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public boolean evaluate() {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public void reset() {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public void connected(FemaleSocket socket) {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public void disconnected(FemaleSocket socket) {
            throw new UnsupportedOperationException("Not supported.");
        }
        
    }
    
    
    private static class MyFemaleSocket extends AbstractFemaleSocket {
    
        public MyFemaleSocket(Base parent, FemaleSocketListener listener, String name) {
            super(parent, listener, name);
        }
        
        @Override
        public void disposeMe() {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public boolean isCompatible(MaleSocket socket) {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public Map<Category, List<Class<? extends Base>>> getConnectableClasses() {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public String getShortDescription(Locale locale) {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public String getLongDescription(Locale locale) {
            throw new UnsupportedOperationException("Not supported.");
        }
    
    }
    
}
