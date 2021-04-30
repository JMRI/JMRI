package jmri.jmrit.logixng.actions;

import java.util.concurrent.atomic.AtomicBoolean;

import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.implementation.DefaultConditionalNGScaffold;
import jmri.jmrit.logixng.implementation.DefaultSymbolTable;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test ActionAtomicBoolean
 * 
 * @author Daniel Bergqvist 2018
 */
public class ActionAtomicBooleanTest extends AbstractDigitalActionTestBase {

    private LogixNG logixNG;
    private ConditionalNG conditionalNG;
    private AtomicBoolean atomicBoolean;
    private ActionAtomicBoolean actionAtomicBoolean;
    
    
    @Override
    public ConditionalNG getConditionalNG() {
        return conditionalNG;
    }
    
    @Override
    public LogixNG getLogixNG() {
        return logixNG;
    }
    
    @Override
    public MaleSocket getConnectableChild() {
        return null;
    }
    
    @Override
    public String getExpectedPrintedTree() {
        return String.format("Set the atomic boolean to true ::: Use default%n");
    }
    
    @Override
    public String getExpectedPrintedTreeFromRoot() {
        return String.format(
                "LogixNG: A logixNG%n" +
                "   ConditionalNG: A conditionalNG%n" +
                "      ! A%n" +
                "         Set the atomic boolean to true ::: Use default%n");
    }
    
    @Override
    public NamedBean createNewBean(String systemName) {
        return new ActionAtomicBoolean(systemName, null);
    }
    
    @Override
    public boolean addNewSocket() {
        return false;
    }
    
    @Test
    public void testCtor() {
        Assert.assertTrue("object exists", _base != null);
        
        ActionAtomicBoolean action2;
        Assert.assertNotNull("atomicBoolean is not null", atomicBoolean);
        atomicBoolean.set(true);
        
        action2 = new ActionAtomicBoolean("IQDA321", null);
        Assert.assertNotNull("object exists", action2);
        Assert.assertNull("Username matches", action2.getUserName());
        Assert.assertEquals("String matches", "Set the atomic boolean to false", action2.getLongDescription());
        
        action2 = new ActionAtomicBoolean("IQDA321", "My atomicBoolean");
        Assert.assertNotNull("object exists", action2);
        Assert.assertEquals("Username matches", "My atomicBoolean", action2.getUserName());
        Assert.assertEquals("String matches", "Set the atomic boolean to false", action2.getLongDescription());
        
        action2 = new ActionAtomicBoolean("IQDA321", null);
        action2.setAtomicBoolean(atomicBoolean);
        Assert.assertTrue("atomic boolean is correct", atomicBoolean == action2.getAtomicBoolean());
        Assert.assertNotNull("object exists", action2);
        Assert.assertNull("Username matches", action2.getUserName());
        Assert.assertEquals("String matches", "Set the atomic boolean to false", action2.getLongDescription());
        
        AtomicBoolean ab = new AtomicBoolean();
        action2 = new ActionAtomicBoolean("IQDA321", "My atomicBoolean");
        action2.setAtomicBoolean(ab);
        Assert.assertTrue("atomic boolean is correct", ab == action2.getAtomicBoolean());
        Assert.assertNotNull("object exists", action2);
        Assert.assertEquals("Username matches", "My atomicBoolean", action2.getUserName());
        Assert.assertEquals("String matches", "Set the atomic boolean to false", action2.getLongDescription());
        
        boolean thrown = false;
        try {
            // Illegal system name
            new ActionAtomicBoolean("IQA55:12:XY11", null);
        } catch (IllegalArgumentException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
        
        thrown = false;
        try {
            // Illegal system name
            new ActionAtomicBoolean("IQA55:12:XY11", "A name");
        } catch (IllegalArgumentException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
        
        // Test setup(). This method doesn't do anything, but execute it for coverage.
        _base.setup();
    }
    
    @Test
    public void testGetChild() {
        Assert.assertTrue("getChildCount() returns 0", 0 == actionAtomicBoolean.getChildCount());
        
        boolean hasThrown = false;
        try {
            actionAtomicBoolean.getChild(0);
        } catch (UnsupportedOperationException ex) {
            hasThrown = true;
            Assert.assertEquals("Error message is correct", "Not supported.", ex.getMessage());
        }
        Assert.assertTrue("Exception is thrown", hasThrown);
    }
    
    @Test
    public void testAction() throws SocketAlreadyConnectedException {
        // Set new value to true
        actionAtomicBoolean.setNewValue(true);
        Assert.assertTrue("new value is true", actionAtomicBoolean.getNewValue());
        // Set the atomic boolean
        atomicBoolean.set(false);
        // The atomic boolean should be false
        Assert.assertFalse("atomicBoolean is false",atomicBoolean.get());
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the atomic boolean should be true
        Assert.assertTrue("atomicBoolean is true",atomicBoolean.get());
        
        // Set new value to false
        actionAtomicBoolean.setNewValue(false);
        Assert.assertFalse("new value is false", actionAtomicBoolean.getNewValue());
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the atomic boolean should be true
        Assert.assertFalse("atomicBoolean is false",atomicBoolean.get());
    }
    
    @Test
    public void testCategory() {
        Assert.assertTrue("Category matches", Category.OTHER == _base.getCategory());
    }
    
    @Test
    public void testIsExternal() {
        Assert.assertTrue("is external", _base.isExternal());
    }
    
    @Test
    public void testShortDescription() {
        Assert.assertEquals("String matches", "Set the atomic boolean", _base.getShortDescription());
    }
    
    @Test
    public void testLongDescription() {
        Assert.assertEquals("String matches", "Set the atomic boolean to true", _base.getLongDescription());
    }
    
    @Test
    public void testChild() {
        Assert.assertTrue("Num children is zero", 0 == _base.getChildCount());
        boolean hasThrown = false;
        try {
            _base.getChild(0);
        } catch (UnsupportedOperationException ex) {
            hasThrown = true;
            Assert.assertTrue("Error message is correct", "Not supported.".equals(ex.getMessage()));
        }
        Assert.assertTrue("Exception is thrown", hasThrown);
    }
    
    // The minimal setup for log4J
    @Before
    public void setUp() throws SocketAlreadyConnectedException {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initLogixNGManager();
        
        _category = Category.ITEM;
        _isExternal = true;
        
        atomicBoolean = new AtomicBoolean(false);
        logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A logixNG");
        conditionalNG = new DefaultConditionalNGScaffold("IQC1", "A conditionalNG");  // NOI18N;
        InstanceManager.getDefault(ConditionalNG_Manager.class).register(conditionalNG);
        logixNG.addConditionalNG(conditionalNG);
        conditionalNG.setRunDelayed(false);
        conditionalNG.setEnabled(true);
        actionAtomicBoolean = new ActionAtomicBoolean("IQDA321", null, atomicBoolean, true);
        MaleSocket socket =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionAtomicBoolean);
        conditionalNG.getChild(0).connect(socket);
        
        _base = actionAtomicBoolean;
        _baseMaleSocket = socket;
        
        logixNG.setParentForAllChildren();
        logixNG.setEnabled(true);
    }

    @After
    public void tearDown() {
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.tearDown();
    }
    
}
