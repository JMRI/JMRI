package jmri.jmrit.logixng.digital.implementation;

import java.util.Locale;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.implementation.AbstractBase;
import jmri.jmrit.logixng.digital.boolean_actions.OnChange;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test DigitalActionManager
 * 
 * @author Daniel Bergqvist 2020
 */
public class DigitalBooleanActionManagerTest {

    private DigitalBooleanActionManager m;
    
    @Test
    public void testRegisterAction() {
        MyAction myAction = new MyAction(m.getSystemNamePrefix()+"BadSystemName");
        
        boolean hasThrown = false;
        try {
            m.registerAction(myAction);
        } catch (IllegalArgumentException e) {
            hasThrown = true;
            Assert.assertEquals("Error message is correct", "System name is invalid: IQBadSystemName", e.getMessage());
        }
        Assert.assertTrue("Exception thrown", hasThrown);
        
        
        // We need a male socket to test with, so we register the action and then unregister the socket
        DigitalBooleanActionBean action = new OnChange("IQDB321", null, OnChange.ChangeType.CHANGE);
        MaleDigitalBooleanActionSocket maleSocket = m.registerAction(action);
        m.deregister(maleSocket);
        
        hasThrown = false;
        try {
            m.registerAction(maleSocket);
        } catch (IllegalArgumentException e) {
            hasThrown = true;
            Assert.assertEquals("Error message is correct", "registerAction() cannot register a MaleDigitalBooleanActionSocket. Use the method register() instead.", e.getMessage());
        }
        Assert.assertTrue("Exception thrown", hasThrown);
    }
    
    @Test
    public void testGetBeanTypeHandled() {
        Assert.assertEquals("getBeanTypeHandled() returns correct value", "Digital boolean action", m.getBeanTypeHandled());
        Assert.assertEquals("getBeanTypeHandled() returns correct value", "Digital boolean action", m.getBeanTypeHandled(false));
        Assert.assertEquals("getBeanTypeHandled() returns correct value", "Digital boolean actions", m.getBeanTypeHandled(true));
    }
    
    @Test
    public void testInstance() {
        Assert.assertNotNull("instance() is not null", DefaultDigitalBooleanActionManager.instance());
        JUnitAppender.assertWarnMessage("instance() called on wrong thread");
    }
    
    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        
        m = InstanceManager.getDefault(DigitalBooleanActionManager.class);
    }

    @After
    public void tearDown() {
        m = null;
        JUnitUtil.tearDown();
    }
    
    
    private static class MyAction extends AbstractBase implements DigitalBooleanActionBean {

        public MyAction(String sys) throws BadSystemNameException {
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
        public void execute(boolean hasChangedToTrue) throws JmriException {
            throw new UnsupportedOperationException("Not supported");
        }
        
    }
    
}
