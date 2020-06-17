package jmri.jmrit.logixng.implementation;

import java.beans.PropertyChangeListener;
import java.io.*;
import java.util.*;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.digital.actions.AbstractDigitalAction;
import jmri.jmrit.logixng.digital.actions.IfThenElse;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test DefaultConditionalNG
 * 
 * @author Daniel Bergqvist 2020
 */
public class DefaultConditionalNGTest {

    @Test
    public void testCtor() {
        DefaultConditionalNG conditionalNG = new DefaultConditionalNG("IQC123", null);
        Assert.assertNotNull("exists", conditionalNG);
        
        boolean hasThrown = false;
        try {
            // Bad system name
            new DefaultConditionalNG("IQCAbc", null);
        } catch (IllegalArgumentException e) {
            hasThrown = true;
            Assert.assertEquals("Error message is correct", "system name is not valid", e.getMessage());
        }
        Assert.assertTrue("Exception thrown", hasThrown);
    }
    
    @Test
    public void testEnableExecution() throws SocketAlreadyConnectedException {
        MyDigitalAction action = new MyDigitalAction("IQDA1", null);
        DefaultConditionalNG conditionalNG = new DefaultConditionalNG("IQC123", null);
        
        MaleSocket socket = InstanceManager.getDefault(DigitalActionManager.class)
                .registerAction(action);
        
        Assert.assertFalse("Enable execution is not supported when no child is connected",
                conditionalNG.supportsEnableExecution());
        
        conditionalNG.getChild(0).connect(socket);
        
        action._supportsEnableExecution = false;
        Assert.assertFalse("Enable execution is not supported",
                conditionalNG.supportsEnableExecution());
        
        // Enable execution cannot be enabled for actions that don't implement
        // the DigitalActionWithEnableExecution interface
        action._supportsEnableExecution = true;
        Assert.assertFalse("Enable execution is not supported",
                conditionalNG.supportsEnableExecution());
        
        // Test with an action that implements DigitalActionWithEnableExecution
        DigitalActionBean actionSupportExecution = new IfThenElse("IQDA2", null, IfThenElse.Type.TRIGGER_ACTION);
        socket = InstanceManager.getDefault(DigitalActionManager.class)
                .registerAction(actionSupportExecution);
        conditionalNG.getChild(0).disconnect();
        conditionalNG.getChild(0).connect(socket);
        Assert.assertTrue("Enable execution is supported",
                conditionalNG.supportsEnableExecution());
    }
    
    @Test
    public void testSetEnableExecution() {
        MyDigitalAction action = new MyDigitalAction("IQDA1", null);
        DefaultConditionalNG conditionalNG = new DefaultConditionalNG("IQC123", null);
        
//        conditionalNG.setEnableExecution(true);
        
        DigitalActionBean actionSupportExecution = new IfThenElse("IQDA1", null, IfThenElse.Type.TRIGGER_ACTION);
        conditionalNG = new DefaultConditionalNG("IQC123", null);
    }
    
    @Test
    public void testLock() {
        DefaultConditionalNG conditionalNG = new DefaultConditionalNG("IQC123", null);
        
        conditionalNG.setLock(Base.Lock.NONE);
        Assert.assertEquals("Lock is correct", Base.Lock.NONE, conditionalNG.getLock());
        
        conditionalNG.setLock(Base.Lock.USER_LOCK);
        Assert.assertEquals("Lock is correct", Base.Lock.USER_LOCK, conditionalNG.getLock());
        
        conditionalNG.setLock(Base.Lock.HARD_LOCK);
        Assert.assertEquals("Lock is correct", Base.Lock.HARD_LOCK, conditionalNG.getLock());
    }
    
    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    
    
    
    private static class MyDigitalAction extends AbstractDigitalAction {

        boolean _supportsEnableExecution = false;
        
        public MyDigitalAction(String sys, String user) throws BadUserNameException, BadSystemNameException {
            super(sys, user);
        }

        @Override
        public boolean supportsEnableExecution() {
            return _supportsEnableExecution;
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
        public String getShortDescription(Locale locale) {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public String getLongDescription(Locale locale) {
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
        public void setup() {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public void execute() throws JmriException {
            throw new UnsupportedOperationException("Not supported");
        }
        
    }
    
}
