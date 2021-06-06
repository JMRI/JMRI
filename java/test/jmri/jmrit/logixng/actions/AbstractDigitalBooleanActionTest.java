package jmri.jmrit.logixng.actions;

import jmri.jmrit.logixng.FakeParent;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.util.JUnitUtil;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.implementation.AbstractFemaleSocket;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test AbstractDigitalBooleanAction
 * 
 * @author Daniel Bergqvist 2020
 */
public class AbstractDigitalBooleanActionTest {

    // This method is CPU intensive so we don't want to run it for every action.
    @Test
    public void testGetNewSocketName() {
        MyAction action = new MyAction();
        boolean hasThrown = false;
        try {
            // Create a bean with bad system name. This must throw an exception
            String socketName = action.getNewSocketName();
            // We should never get here.
            Assert.assertNotNull("Name is not null", socketName);
        } catch (RuntimeException e) {
            Assert.assertEquals("Exception is correct", "Unable to find a new socket name", e.getMessage());
            hasThrown = true;
        }
        Assert.assertTrue("Exception is thrown", hasThrown);
        
        
        DigitalBooleanOnChange action2 = new DigitalBooleanOnChange("IQDB1", null, DigitalBooleanOnChange.Trigger.CHANGE);
        Assert.assertEquals("New socket name is correct", "A1", action2.getNewSocketName());
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
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
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
    private static class MyAction extends AbstractDigitalBooleanAction implements FemaleSocketListener {

        private final MyFemaleSocket child = new MyFemaleSocket(this, this, "A1");
        
        public MyAction() {
            super(InstanceManager.getDefault(DigitalBooleanActionManager.class).getAutoSystemName(), null);
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

        private final Base fakeParent = new FakeParent();

        // To aid testing of the method getNewSocketName(), we let the method
        // getChild() set the name.
        // See the method testGetNewSocketName().
        @Override
        public FemaleSocket getChild(int index) throws IllegalArgumentException, UnsupportedOperationException {
            // FemaleSocket.setName() calls validate() that calls parent.getChild()
            // which gives a StackOverflowError. To protect from that, we temporary
            // set the parent to a fake parent.
            Base parent = child.getParent();
            child.setParent(fakeParent);
            child.setName("A"+index);
            child.setParent(parent);
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
        public void execute(boolean hasChangedToTrue, boolean hasChangedToFalse) {
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

        @Override
        public Base getDeepCopy(Map<String, String> map, Map<String, String> map1) throws JmriException {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public Base deepCopyChildren(Base base, Map<String, String> map, Map<String, String> map1) throws JmriException {
            throw new UnsupportedOperationException("Not supported");
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
