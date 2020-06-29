package jmri.jmrit.logixng.analog.implementation;

import java.util.Locale;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.analog.expressions.AnalogExpressionMemory;
import jmri.jmrit.logixng.implementation.AbstractBase;
import jmri.jmrit.logixng.implementation.LogixNGPreferences;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test AnalogExpressionManager
 * 
 * @author Daniel Bergqvist 2020
 */
public class AnalogExpressionManagerTest {

    private AnalogExpressionManager m;
    
    @Test
    public void testRegisterExpression() {
        MyExpression myExpression = new MyExpression(m.getSystemNamePrefix()+"BadSystemName");
        
        boolean hasThrown = false;
        try {
            m.registerExpression(myExpression);
        } catch (IllegalArgumentException e) {
            hasThrown = true;
            Assert.assertEquals("Error message is correct", "System name is invalid: IQBadSystemName", e.getMessage());
        }
        Assert.assertTrue("Exception thrown", hasThrown);
        
        
        // We need a male socket to test with, so we register the action and then unregister the socket
        AnalogExpressionBean action = new AnalogExpressionMemory("IQAE321", null);
        MaleAnalogExpressionSocket maleSocket = m.registerExpression(action);
        m.deregister(maleSocket);
        
        hasThrown = false;
        try {
            m.registerExpression(maleSocket);
        } catch (IllegalArgumentException e) {
            hasThrown = true;
            Assert.assertEquals("Error message is correct", "registerExpression() cannot register a MaleAnalogExpressionSocket. Use the method register() instead.", e.getMessage());
        }
        Assert.assertTrue("Exception thrown", hasThrown);
    }
    
    @Test
    public void testCreateFemaleSocket() {
        FemaleSocket socket;
        AnalogExpressionManagerTest.MyExpression myExpression = new AnalogExpressionManagerTest.MyExpression("IQSA1");
        FemaleSocketListener listener = new AnalogExpressionManagerTest.MyFemaleSocketListener();
        LogixNGPreferences preferences = InstanceManager.getDefault(LogixNGPreferences.class);
        
        preferences.setUseGenericFemaleSockets(false);
        socket = m.createFemaleSocket(myExpression, listener, "E1");
        Assert.assertEquals("Class is correct", "jmri.jmrit.logixng.analog.implementation.DefaultFemaleAnalogExpressionSocket", socket.getClass().getName());
        
        preferences.setUseGenericFemaleSockets(true);
        socket = m.createFemaleSocket(myExpression, listener, "E2");
        Assert.assertEquals("Class is correct", "jmri.jmrit.logixng.implementation.DefaultFemaleGenericExpressionSocket$AnalogSocket", socket.getClass().getName());
    }
    
    @Test
    public void testGetBeanTypeHandled() {
        Assert.assertEquals("getBeanTypeHandled() returns correct value", "Analog expression", m.getBeanTypeHandled());
        Assert.assertEquals("getBeanTypeHandled() returns correct value", "Analog expression", m.getBeanTypeHandled(false));
        Assert.assertEquals("getBeanTypeHandled() returns correct value", "Analog expressions", m.getBeanTypeHandled(true));
    }
    
    @Test
    public void testInstance() {
        Assert.assertNotNull("instance() is not null", DefaultAnalogExpressionManager.instance());
        JUnitAppender.assertWarnMessage("instance() called on wrong thread");
    }
    
    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        
        m = InstanceManager.getDefault(AnalogExpressionManager.class);
    }

    @After
    public void tearDown() {
        m = null;
        JUnitUtil.tearDown();
    }
    
    
    private static class MyExpression extends AbstractBase implements AnalogExpressionBean {

        public MyExpression(String sys) throws BadSystemNameException {
            super(sys);
        }

        @Override
        protected void registerListenersForThisClass() {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        protected void unregisterListenersForThisClass() {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        protected void disposeMe() {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public void setState(int s) throws JmriException {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public int getState() {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public String getBeanType() {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public String getShortDescription(Locale locale) {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public String getLongDescription(Locale locale) {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public Base getParent() {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public void setParent(Base parent) {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public FemaleSocket getChild(int index) throws IllegalArgumentException, UnsupportedOperationException {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public int getChildCount() {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public Category getCategory() {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public boolean isExternal() {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public Lock getLock() {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public void setLock(Lock lock) {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public void setup() {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public double evaluate() throws JmriException {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public void reset() {
            throw new UnsupportedOperationException("Not supported");
        }
        
    }
    
    
    private static class MyFemaleSocketListener implements FemaleSocketListener {
        @Override
        public void connected(FemaleSocket socket) {
            // Do nothing
        }

        @Override
        public void disconnected(FemaleSocket socket) {
            // Do nothing
        }
    };
    
}
