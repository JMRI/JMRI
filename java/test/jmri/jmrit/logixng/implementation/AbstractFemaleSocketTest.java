package jmri.jmrit.logixng.implementation;

import java.beans.*;
import java.io.*;
import java.util.*;

import jmri.NamedBean;
import jmri.jmrit.logixng.*;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test AbstractFemaleSocket
 * 
 * @author Daniel Bergqvist 2020
 */
public class AbstractFemaleSocketTest {

    @Test
    public void testPropertyChangeListeners() {
        MyFemaleSocket socket = new MyFemaleSocket(null, null, "A1");
        
        boolean hasThrown = false;
        try {
            socket.addPropertyChangeListener(null);
        } catch (UnsupportedOperationException e) {
            hasThrown = true;
            Assert.assertEquals("Error message is correct", "Not supported", e.getMessage());
        }
        Assert.assertTrue("Exception thrown", hasThrown);
        
        hasThrown = false;
        try {
            socket.addPropertyChangeListener("", null);
        } catch (UnsupportedOperationException e) {
            hasThrown = true;
            Assert.assertEquals("Error message is correct", "Not supported", e.getMessage());
        }
        Assert.assertTrue("Exception thrown", hasThrown);
        
        hasThrown = false;
        try {
            socket.getPropertyChangeListeners();
        } catch (UnsupportedOperationException e) {
            hasThrown = true;
            Assert.assertEquals("Error message is correct", "Not supported", e.getMessage());
        }
        Assert.assertTrue("Exception thrown", hasThrown);
        
        hasThrown = false;
        try {
            socket.getPropertyChangeListeners("");
        } catch (UnsupportedOperationException e) {
            hasThrown = true;
            Assert.assertEquals("Error message is correct", "Not supported", e.getMessage());
        }
        Assert.assertTrue("Exception thrown", hasThrown);
        
        hasThrown = false;
        try {
            socket.removePropertyChangeListener(null);
        } catch (UnsupportedOperationException e) {
            hasThrown = true;
            Assert.assertEquals("Error message is correct", "Not supported", e.getMessage());
        }
        Assert.assertTrue("Exception thrown", hasThrown);
        
        hasThrown = false;
        try {
            socket.removePropertyChangeListener("", null);
        } catch (UnsupportedOperationException e) {
            hasThrown = true;
            Assert.assertEquals("Error message is correct", "Not supported", e.getMessage());
        }
        Assert.assertTrue("Exception thrown", hasThrown);
    }
    
    @Test
    public void testIsActive() {
        MyBase base = new MyBase();
        MyFemaleSocket socket = new MyFemaleSocket(base, null, "A1");
        
        // Check isActive when parent is not null
        
        Assert.assertNotNull("Parent is not null", socket.getParent());
        
        base._active = false;
        socket._enabled = false;
        Assert.assertFalse("Socket is not active", socket.isActive());
        
        base._active = true;
        socket._enabled = false;
        Assert.assertFalse("Socket is not active", socket.isActive());
        
        base._active = false;
        socket._enabled = true;
        Assert.assertFalse("Socket is not active", socket.isActive());
        
        base._active = true;
        socket._enabled = true;
        Assert.assertTrue("Socket is active", socket.isActive());
        
        
        // Check isActive when parent is null
        
        socket.setParent(null);
        Assert.assertNull("Parent is null", socket.getParent());
        
        socket._enabled = false;
        Assert.assertFalse("Socket is not active", socket.isActive());
        
        socket._enabled = true;
        Assert.assertTrue("Socket is active", socket.isActive());
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
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    
    
    private static class MyFemaleSocket extends AbstractFemaleSocket {

        private boolean _enabled = false;
        
        public MyFemaleSocket(Base parent, FemaleSocketListener listener, String name) {
            super(parent, listener, name);
        }

        @Override
        public boolean isEnabled() {
            return _enabled;
        }
        
        @Override
        public void disposeMe() {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public boolean isCompatible(MaleSocket socket) {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public Map<Category, List<Class<? extends Base>>> getConnectableClasses() {
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
        
    }
    
    
    private static class MyBase implements Base {

        private boolean _active = false;
        
        @Override
        public boolean isActive() {
            return _active;
        }
        
        @Override
        public String getSystemName() {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public String getUserName() {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public void setUserName(String s) throws NamedBean.BadUserNameException {
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
        public ConditionalNG getConditionalNG() {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public LogixNG getLogixNG() {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public Base getRoot() {
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
        public void setParentForAllChildren() {
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
        public void dispose() {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public void printTree(PrintWriter writer, String indent) {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public void printTree(Locale locale, PrintWriter writer, String indent) {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public void printTree(Locale locale, PrintWriter writer, String indent, String currentIndent) {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public PropertyChangeListener[] getPropertyChangeListeners() {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public PropertyChangeListener[] getPropertyChangeListeners(String propertyName) {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener, String name, String listenerRef) {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener, String name, String listenerRef) {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public void updateListenerRef(PropertyChangeListener l, String newName) {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public String getListenerRef(PropertyChangeListener l) {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public ArrayList<String> getListenerRefs() {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public int getNumPropertyChangeListeners() {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public PropertyChangeListener[] getPropertyChangeListenersByReference(String name) {
            throw new UnsupportedOperationException("Not supported");
        }
    
    }
    
}
