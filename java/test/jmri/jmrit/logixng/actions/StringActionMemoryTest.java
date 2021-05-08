package jmri.jmrit.logixng.actions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;

import jmri.InstanceManager;
import jmri.Memory;
import jmri.MemoryManager;
import jmri.NamedBean;
import jmri.NamedBeanHandle;
import jmri.NamedBeanHandleManager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.implementation.DefaultConditionalNGScaffold;
import jmri.jmrit.logixng.implementation.DefaultSymbolTable;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test StringActionMemory
 * 
 * @author Daniel Bergqvist 2018
 */
public class StringActionMemoryTest extends AbstractStringActionTestBase {

    LogixNG logixNG;
    ConditionalNG conditionalNG;
    protected Memory _memory;
    
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
        return String.format("Set memory IM1 ::: Use default%n");
    }
    
    @Override
    public String getExpectedPrintedTreeFromRoot() {
        return String.format(
                "LogixNG: A new logix for test%n" +
                "   ConditionalNG: A conditionalNG%n" +
                "      ! A%n" +
                "         Read string E and set string A ::: Use default%n" +
                "            ?s E%n" +
                "               Socket not connected%n" +
                "            !s A%n" +
                "               Set memory IM1 ::: Use default%n");
    }
    
    @Override
    public NamedBean createNewBean(String systemName) {
        return new StringMany(systemName, null);
    }
    
    @Override
    public boolean addNewSocket() {
        return false;
    }
    
    @Test
    public void testCtor() {
        Assert.assertTrue("object exists", _base != null);
        
        StringActionMemory action2;
        Assert.assertNotNull("memory is not null", _memory);
        _memory.setValue(10.2);
        
        action2 = new StringActionMemory("IQSA11", null);
        Assert.assertNotNull("object exists", action2);
        Assert.assertTrue("Username matches", null == action2.getUserName());
        Assert.assertEquals("String matches", "Set memory none", action2.getLongDescription());
        
        action2 = new StringActionMemory("IQSA11", "My memory");
        Assert.assertNotNull("object exists", action2);
        Assert.assertTrue("Username matches", "My memory".equals(action2.getUserName()));
        Assert.assertEquals("String matches", "Set memory none", action2.getLongDescription());
        
        action2 = new StringActionMemory("IQSA11", null);
        action2.setMemory(_memory);
        Assert.assertNotNull("object exists", action2);
        Assert.assertTrue("Username matches", null == action2.getUserName());
        Assert.assertEquals("String matches", "Set memory IM1", action2.getLongDescription());
        
        action2 = new StringActionMemory("IQSA11", "My memory");
        action2.setMemory(_memory);
        Assert.assertNotNull("object exists", action2);
        Assert.assertTrue("Username matches", "My memory".equals(action2.getUserName()));
        Assert.assertEquals("String matches", "Set memory IM1", action2.getLongDescription());
        
        boolean thrown = false;
        try {
            // Illegal system name
            new StringActionMemory("IQA55:12:XY11", null);
        } catch (IllegalArgumentException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
        
        thrown = false;
        try {
            // Illegal system name
            new StringActionMemory("IQA55:12:XY11", "A name");
        } catch (IllegalArgumentException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
    }
    
    @Test
    public void testAction() throws SocketAlreadyConnectedException, SocketAlreadyConnectedException {
        StringActionMemory action = (StringActionMemory)_base;
        action.setValue("");
        Assert.assertEquals("Memory has correct value", "", _memory.getValue());
        action.setValue("Test");
        Assert.assertEquals("Memory has correct value", "Test", _memory.getValue());
        action.removeMemory();
        action.setValue("Other test");
        Assert.assertEquals("Memory has correct value", "Test", _memory.getValue());
    }
    
    @Test
    public void testMemory() {
        StringActionMemory action = (StringActionMemory)_base;
        action.removeMemory();
        Assert.assertNull("Memory is null", action.getMemory());
        ((StringActionMemory)_base).setMemory(_memory);
        Assert.assertTrue("Memory matches", _memory == action.getMemory().getBean());
        
        action.removeMemory();
        Assert.assertNull("Memory is null", action.getMemory());
        Memory otherMemory = InstanceManager.getDefault(MemoryManager.class).provide("IM99");
        Assert.assertNotNull("memory is not null", otherMemory);
        NamedBeanHandle<Memory> memoryHandle = InstanceManager.getDefault(NamedBeanHandleManager.class)
                .getNamedBeanHandle(otherMemory.getDisplayName(), otherMemory);
        ((StringActionMemory)_base).setMemory(memoryHandle);
        Assert.assertTrue("Memory matches", memoryHandle == action.getMemory());
        Assert.assertTrue("Memory matches", otherMemory == action.getMemory().getBean());
        
        action.removeMemory();
        Assert.assertNull("Memory is null", action.getMemory());
        action.setMemory(memoryHandle.getName());
        Assert.assertTrue("Memory matches", memoryHandle == action.getMemory());
        
        // Test setMemory with a memory name that doesn't exists
        action.setMemory("Non existent memory");
        Assert.assertNull("Memory is null", action.getMemory());
        JUnitAppender.assertErrorMessage("memory \"Non existent memory\" is not found");
    }
    
    @Test
    public void testVetoableChange() throws PropertyVetoException {
        // Get some other memory for later use
        Memory otherMemory = InstanceManager.getDefault(MemoryManager.class).provide("IM99");
        Assert.assertNotNull("Memory is not null", otherMemory);
        Assert.assertNotEquals("Memory is not equal", _memory, otherMemory);
        
        // Get the expression and set the memory
        StringActionMemory action = (StringActionMemory)_base;
        action.setMemory(_memory);
        Assert.assertEquals("Memory matches", _memory, action.getMemory().getBean());
        
        // Test vetoableChange() for some other propery
        action.vetoableChange(new PropertyChangeEvent(this, "CanSomething", "test", null));
        Assert.assertEquals("Memory matches", _memory, action.getMemory().getBean());
        
        // Test vetoableChange() for a string
        action.vetoableChange(new PropertyChangeEvent(this, "CanDelete", "test", null));
        Assert.assertEquals("Memory matches", _memory, action.getMemory().getBean());
        action.vetoableChange(new PropertyChangeEvent(this, "DoDelete", "test", null));
        Assert.assertEquals("Memory matches", _memory, action.getMemory().getBean());
        
        // Test vetoableChange() for another memory
        action.vetoableChange(new PropertyChangeEvent(this, "CanDelete", otherMemory, null));
        Assert.assertEquals("Memory matches", _memory, action.getMemory().getBean());
        action.vetoableChange(new PropertyChangeEvent(this, "DoDelete", otherMemory, null));
        Assert.assertEquals("Memory matches", _memory, action.getMemory().getBean());
        
        // Test vetoableChange() for its own memory
        boolean thrown = false;
        try {
            action.vetoableChange(new PropertyChangeEvent(this, "CanDelete", _memory, null));
        } catch (PropertyVetoException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
        
        Assert.assertEquals("Memory matches", _memory, action.getMemory().getBean());
        action.vetoableChange(new PropertyChangeEvent(this, "DoDelete", _memory, null));
        Assert.assertNull("Memory is null", action.getMemory());
    }
    
    @Test
    public void testCategory() {
        Assert.assertTrue("Category matches", Category.ITEM == _base.getCategory());
    }
    
    @Test
    public void testIsExternal() {
        Assert.assertTrue("is external", _base.isExternal());
    }
    
    @Test
    public void testShortDescription() {
        Assert.assertEquals("String matches", "Memory", _base.getShortDescription());
    }
    
    @Test
    public void testLongDescription() {
        Assert.assertTrue("String matches", "Set memory IM1".equals(_base.getLongDescription()));
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
        JUnitUtil.initMemoryManager();
        JUnitUtil.initLogixNGManager();
        
        logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test");  // NOI18N
        conditionalNG = new DefaultConditionalNGScaffold("IQC1", "A conditionalNG");  // NOI18N;
        InstanceManager.getDefault(ConditionalNG_Manager.class).register(conditionalNG);
        conditionalNG.setEnabled(true);
        conditionalNG.setRunDelayed(false);
        logixNG.addConditionalNG(conditionalNG);
        DoStringAction doStringAction = new DoStringAction("IQDA321", null);
        MaleSocket maleSocketDoStringAction =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(doStringAction);
        conditionalNG.getChild(0).connect(maleSocketDoStringAction);
        _memory = InstanceManager.getDefault(MemoryManager.class).provide("IM1");
        StringActionMemory stringActionMemory = new StringActionMemory("IQSA321", null);
        MaleSocket maleSocketStringActionMemory =
                InstanceManager.getDefault(StringActionManager.class).registerAction(stringActionMemory);
        doStringAction.getChild(1).connect(maleSocketStringActionMemory);
        stringActionMemory.setMemory(_memory);
        _base = stringActionMemory;
        _baseMaleSocket = maleSocketStringActionMemory;
        
        logixNG.setParentForAllChildren();
        logixNG.setEnabled(true);
    }

    @After
    public void tearDown() {
        _base.dispose();
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.tearDown();
    }
    
}
