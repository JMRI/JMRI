package jmri.jmrit.logixng.implementation;

import java.util.*;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.AbstractDigitalAction;
import jmri.jmrit.logixng.actions.IfThenElse;
import jmri.util.JUnitAppender;
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
    public void testLock() {
        DefaultConditionalNG conditionalNG = new DefaultConditionalNG("IQC123", null);
        
        conditionalNG.setLock(Base.Lock.NONE);
        Assert.assertEquals("Lock is correct", Base.Lock.NONE, conditionalNG.getLock());
        
        conditionalNG.setLock(Base.Lock.USER_LOCK);
        Assert.assertEquals("Lock is correct", Base.Lock.USER_LOCK, conditionalNG.getLock());
        
        conditionalNG.setLock(Base.Lock.HARD_LOCK);
        Assert.assertEquals("Lock is correct", Base.Lock.HARD_LOCK, conditionalNG.getLock());
    }
    
    @Test
    public void testState() throws JmriException {
        DefaultConditionalNG conditionalNG = new DefaultConditionalNG("IQC123", null);
        conditionalNG.setState(NamedBean.INCONSISTENT);
        JUnitAppender.assertWarnMessage("Unexpected call to setState in DefaultConditionalNG.");
        
        Assert.assertEquals("State is correct", NamedBean.UNKNOWN, conditionalNG.getState());
        JUnitAppender.assertWarnMessage("Unexpected call to getState in DefaultConditionalNG.");
    }
    
    @Test
    public void testExecute() throws SocketAlreadyConnectedException, JmriException {
        DefaultConditionalNG conditionalNG = new DefaultConditionalNG("IQC123", null);
        conditionalNG.setRunDelayed(false);
        MyDigitalAction action = new MyDigitalAction("IQDA1", null);
        MaleSocket socket = InstanceManager.getDefault(DigitalActionManager.class)
                .registerAction(action);
        conditionalNG.getChild(0).connect(socket);
        conditionalNG.setParentForAllChildren();
        
        socket.setErrorHandlingType(MaleSocket.ErrorHandlingType.ThrowException);
        
        action.throwOnExecute = false;
        action.hasExecuted = false;
        conditionalNG.execute();
        Assert.assertTrue("Action is executed", action.hasExecuted);
        
        action.throwOnExecute = true;
        action.hasExecuted = false;
        conditionalNG.execute();
        JUnitAppender.assertWarnMessage("ConditionalNG IQC123 got an exception during execute: jmri.JmriException: An error has occured");
    }
    
    @Test
    public void testDescription() {
        DefaultConditionalNG conditionalNG = new DefaultConditionalNG("IQC123", null);
        Assert.assertEquals("Short description is correct", "ConditionalNG: IQC123", conditionalNG.getShortDescription());
        Assert.assertEquals("Long description is correct", "ConditionalNG: IQC123", conditionalNG.getLongDescription());
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
        InstanceManager.getDefault(LogixNGPreferences.class).setInstallDebugger(false);
        JUnitUtil.initLogixNGManager();
    }

    @After
    public void tearDown() {
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.tearDown();
    }
    
    
    
    private static class MyDigitalAction extends AbstractDigitalAction {

        private boolean hasExecuted;
        private boolean throwOnExecute;
        
        public MyDigitalAction(String sys, String user) throws BadUserNameException, BadSystemNameException {
            super(sys, user);
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
            return "MyDigitalAction";
        }

        @Override
        public String getLongDescription(Locale locale) {
            return "MyDigitalAction";
        }

        @Override
        public FemaleSocket getChild(int index) throws IllegalArgumentException, UnsupportedOperationException {
            throw new UnsupportedOperationException("Not supported6");
        }

        @Override
        public int getChildCount() {
            return 0;
        }

        @Override
        public Category getCategory() {
            throw new UnsupportedOperationException("Not supported7");
        }

        @Override
        public boolean isExternal() {
            throw new UnsupportedOperationException("Not supported8");
        }

        @Override
        public void setup() {
            throw new UnsupportedOperationException("Not supported9");
        }

        @Override
        public void execute() throws JmriException {
            if (throwOnExecute) {
                throw new JmriException ("An error has occured");
            } else {
                hasExecuted = true;
            }
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
    
}
