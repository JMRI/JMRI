package jmri.jmrit.logixng.actions;

import java.beans.PropertyVetoException;

import java.util.concurrent.atomic.AtomicBoolean;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.util.*;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test AbstractScriptDigitalAction
 * 
 * @author Daniel Bergqvist 2018
 */
public class AbstractScriptDigitalActionTest {

    private LogixNG logixNG;
    private ConditionalNG conditionalNG;
    private AbstractScriptDigitalAction actionAbstractScriptDigitalAction;
    
    @Test
    public void testgetCategory() {
        Assert.assertEquals("Correct category",
                Category.ITEM,
                actionAbstractScriptDigitalAction.getCategory());
    }
    
    @Test
    public void testIsExternal() {
        Assert.assertTrue("isEnabled() returns true by default",
                actionAbstractScriptDigitalAction.isExternal());
    }
    
    @Test
    public void testGetChild() {
        boolean hasThrown = false;
        try {
            actionAbstractScriptDigitalAction.getChild(0);
        } catch (UnsupportedOperationException e) {
            hasThrown = true;
            Assert.assertEquals("Error message is correct",
                    "Not supported", e.getMessage());
        }
        Assert.assertTrue("Exception is thrown", hasThrown);
    }
    
    @Test
    public void testGetChildCount() {
        Assert.assertEquals("action has no children", 0,
                actionAbstractScriptDigitalAction.getChildCount());
    }
    
    @Test
    public void testGetShortDescription() {
        boolean hasThrown = false;
        try {
            actionAbstractScriptDigitalAction.getShortDescription();
        } catch (UnsupportedOperationException e) {
            hasThrown = true;
            Assert.assertEquals("Error message is correct",
                    "Not supported", e.getMessage());
        }
        Assert.assertTrue("Exception is thrown", hasThrown);
    }
    
    @Test
    public void testGetLongDescription() {
        boolean hasThrown = false;
        try {
            actionAbstractScriptDigitalAction.getLongDescription();
        } catch (UnsupportedOperationException e) {
            hasThrown = true;
            Assert.assertEquals("Error message is correct",
                    "Not supported", e.getMessage());
        }
        Assert.assertTrue("Exception is thrown", hasThrown);
    }
    
    @Test
    public void testMethods() throws PropertyVetoException {
        // This shouldn't do anything but call it for coverage
        actionAbstractScriptDigitalAction.vetoableChange(null);
        
        // This shouldn't do anything but call it for coverage
        actionAbstractScriptDigitalAction.setup();
        
        AtomicBoolean registerCalled = new AtomicBoolean(false);
        AtomicBoolean unregisterCalled = new AtomicBoolean(false);
        
        AbstractScriptDigitalAction action = new AbstractScriptDigitalAction(null) {
            @Override
            public void execute() throws JmriException {
                throw new UnsupportedOperationException("Not supported");
            }
            
            @Override
            public void registerScriptListeners() {
                // Call super method for coverage
                super.registerScriptListeners();
                registerCalled.set(true);
            }
            
            @Override
            public void unregisterScriptListeners() {
                // Call super method for coverage
                super.unregisterScriptListeners();
                unregisterCalled.set(true);
            }
            
        };
        
        // Test that listeners get registered
        Assert.assertFalse("registerCalled is false", registerCalled.get());
        action.registerListeners();
        Assert.assertTrue("registerCalled is true", registerCalled.get());
        
        // Test that listeners don't get registered again when they are registered
        registerCalled.set(false);
        action.registerListeners();
        Assert.assertFalse("registerCalled is false", registerCalled.get());
        
        // Test that listeners get unregistered
        Assert.assertFalse("registerCalled is false", unregisterCalled.get());
        action.unregisterListeners();
        Assert.assertTrue("registerCalled is true", unregisterCalled.get());
        
        // Test that listeners don't get unregistered again when they are unregistered
        unregisterCalled.set(false);
        action.unregisterListeners();
        Assert.assertFalse("registerCalled is false", unregisterCalled.get());
    }
    
    // The minimal setup for log4J
    @Before
    public void setUp() throws SocketAlreadyConnectedException {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initLogixNGManager();
        
        logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test");  // NOI18N
        conditionalNG = InstanceManager.getDefault(ConditionalNG_Manager.class)
                .createConditionalNG("A conditionalNG");  // NOI18N
        conditionalNG.setEnabled(false);
        conditionalNG.setRunDelayed(false);
        logixNG.addConditionalNG(conditionalNG);
        
        actionAbstractScriptDigitalAction = new AbstractScriptDigitalAction(null) {
            @Override
            public void execute() throws JmriException {
                throw new UnsupportedOperationException("Not supported");
            }
        };
        MaleSocket maleSocket =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionAbstractScriptDigitalAction);
        
        conditionalNG.getChild(0).connect(maleSocket);
        logixNG.setParentForAllChildren();
        logixNG.setEnabled(true);
    }
    
    @After
    public void tearDown() throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.tearDown();
    }
    
}
