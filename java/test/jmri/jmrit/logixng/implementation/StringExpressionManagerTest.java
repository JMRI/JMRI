package jmri.jmrit.logixng.implementation;

import java.util.Locale;
import java.util.Map;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.expressions.StringExpressionMemory;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test StringExpressionManager
 * 
 * @author Daniel Bergqvist 2020
 */
public class StringExpressionManagerTest extends AbstractManagerTestBase {

    private StringExpressionManager _m;
    
    @Test
    public void testRegisterExpression() {
        MyExpression myExpression = new MyExpression(_m.getSystemNamePrefix()+"BadSystemName");
        
        boolean hasThrown = false;
        try {
            _m.registerExpression(myExpression);
        } catch (IllegalArgumentException e) {
            hasThrown = true;
            Assert.assertEquals("Error message is correct", "System name is invalid: IQBadSystemName", e.getMessage());
        }
        Assert.assertTrue("Exception thrown", hasThrown);
        
        
        // We need a male socket to test with, so we register the action and then unregister the socket
        StringExpressionBean action = new StringExpressionMemory("IQSE321", null);
        MaleStringExpressionSocket maleSocket = _m.registerExpression(action);
        _m.deregister(maleSocket);
        
        hasThrown = false;
        try {
            _m.registerExpression(maleSocket);
        } catch (IllegalArgumentException e) {
            hasThrown = true;
            Assert.assertEquals("Error message is correct", "registerExpression() cannot register a MaleStringExpressionSocket. Use the method register() instead.", e.getMessage());
        }
        Assert.assertTrue("Exception thrown", hasThrown);
    }
    
    @Test
    public void testCreateFemaleSocket() {
        FemaleSocket socket;
        MyExpression myExpression = new MyExpression("IQSA1");
        FemaleSocketListener listener = new MyFemaleSocketListener();
        LogixNGPreferences preferences = InstanceManager.getDefault(LogixNGPreferences.class);
        
        socket = _m.createFemaleSocket(myExpression, listener, "E1");
        Assert.assertEquals("Class is correct", "jmri.jmrit.logixng.implementation.DefaultFemaleStringExpressionSocket", socket.getClass().getName());
    }
    
    @Test
    public void testGetBeanTypeHandled() {
        Assert.assertEquals("getBeanTypeHandled() returns correct value", "String expression", _m.getBeanTypeHandled());
        Assert.assertEquals("getBeanTypeHandled() returns correct value", "String expression", _m.getBeanTypeHandled(false));
        Assert.assertEquals("getBeanTypeHandled() returns correct value", "String expressions", _m.getBeanTypeHandled(true));
    }
    
    @Test
    public void testInstance() {
        Assert.assertNotNull("instance() is not null", DefaultStringExpressionManager.instance());
        JUnitAppender.assertWarnMessage("instance() called on wrong thread");
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
        
        _m = InstanceManager.getDefault(StringExpressionManager.class);
        _manager = _m;
    }

    @After
    public void tearDown() {
        _m = null;
        _manager = null;
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.tearDown();
    }
    
    
    private static class MyExpression extends AbstractBase implements StringExpressionBean {

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
        public String evaluate() throws JmriException {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public void setTriggerOnChange(boolean triggerOnChange) {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public boolean getTriggerOnChange() {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public Base getDeepCopy(Map<String, String> map, Map<String, String> map1) throws JmriException {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public Base deepCopyChildren(Base base, Map<String, String> map, Map<String, String> map1) throws JmriException {
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
    }
    
}
