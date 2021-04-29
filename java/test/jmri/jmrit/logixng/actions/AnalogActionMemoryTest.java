package jmri.jmrit.logixng.actions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;

import jmri.*;
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
 * Test AnalogActionMemory
 * 
 * @author Daniel Bergqvist 2018
 */
public class AnalogActionMemoryTest extends AbstractAnalogActionTestBase {

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
        return String.format("Set memory IM1 ::: Log error%n");
    }
    
    @Override
    public String getExpectedPrintedTreeFromRoot() {
        return String.format(
                "LogixNG: A new logix for test%n" +
                "   ConditionalNG: A conditionalNG%n" +
                "      ! A%n" +
                "         Read analog E and set analog A ::: Log error%n" +
                "            ?~ E%n" +
                "               Socket not connected%n" +
                "            !~ A%n" +
                "               Set memory IM1 ::: Log error%n");
    }
    
    @Override
    public NamedBean createNewBean(String systemName) {
        return new AnalogMany(systemName, null);
    }
    
    @Override
    public boolean addNewSocket() {
        return false;
    }
    
    @Test
    public void testCtor() {
        Assert.assertTrue("object exists", _base != null);
        
        AnalogActionMemory action2;
        Assert.assertNotNull("memory is not null", _memory);
        _memory.setValue(10.2);
        
        action2 = new AnalogActionMemory("IQAA11", null);
        Assert.assertNotNull("object exists", action2);
        Assert.assertTrue("Username matches", null == action2.getUserName());
        Assert.assertEquals("String matches", "Set memory none", action2.getLongDescription());
        
        action2 = new AnalogActionMemory("IQAA11", "My memory");
        Assert.assertNotNull("object exists", action2);
        Assert.assertTrue("Username matches", "My memory".equals(action2.getUserName()));
        Assert.assertEquals("String matches", "Set memory none", action2.getLongDescription());
        
        action2 = new AnalogActionMemory("IQAA11", null);
        action2.setMemory(_memory);
        Assert.assertNotNull("object exists", action2);
        Assert.assertTrue("Username matches", null == action2.getUserName());
        Assert.assertEquals("String matches", "Set memory IM1", action2.getLongDescription());
        
        action2 = new AnalogActionMemory("IQAA11", "My memory");
        action2.setMemory(_memory);
        Assert.assertNotNull("object exists", action2);
        Assert.assertTrue("Username matches", "My memory".equals(action2.getUserName()));
        Assert.assertEquals("String matches", "Set memory IM1", action2.getLongDescription());
        
        boolean thrown = false;
        try {
            // Illegal system name
            new AnalogActionMemory("IQA55:12:XY11", null);
        } catch (IllegalArgumentException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
        
        thrown = false;
        try {
            // Illegal system name
            new AnalogActionMemory("IQA55:12:XY11", "A name");
        } catch (IllegalArgumentException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
    }
    
    @Test
    public void testAction() throws SocketAlreadyConnectedException, SocketAlreadyConnectedException, JmriException {
        AnalogActionMemory action = (AnalogActionMemory)_base;
        action.setValue(0.0d);
        Assert.assertTrue("Memory has correct value", 0.0d == (Double)_memory.getValue());
        action.setValue(1.0d);
        Assert.assertTrue("Memory has correct value", 1.0d == (Double)_memory.getValue());
        action.removeMemory();
        action.setValue(2.0d);
        Assert.assertTrue("Memory has correct value", 1.0d == (Double)_memory.getValue());
    }
    
    @Test
    public void testMemory() {
        AnalogActionMemory action = (AnalogActionMemory)_base;
        action.removeMemory();
        Assert.assertNull("Memory is null", action.getMemory());
        ((AnalogActionMemory)_base).setMemory(_memory);
        Assert.assertTrue("Memory matches", _memory == action.getMemory().getBean());
        
        action.removeMemory();
        Assert.assertNull("Memory is null", action.getMemory());
        Memory otherMemory = InstanceManager.getDefault(MemoryManager.class).provide("IM99");
        Assert.assertNotNull("memory is not null", otherMemory);
        NamedBeanHandle<Memory> memoryHandle = InstanceManager.getDefault(NamedBeanHandleManager.class)
                .getNamedBeanHandle(otherMemory.getDisplayName(), otherMemory);
        ((AnalogActionMemory)_base).setMemory(memoryHandle);
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
        AnalogActionMemory action = (AnalogActionMemory)_base;
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
        Assert.assertTrue("String matches", "Set memory".equals(_base.getShortDescription()));
    }
    
    @Test
    public void testLongDescription() {
        Assert.assertEquals("String matches", "Set memory IM1", _base.getLongDescription());
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
//        JUnitUtil.initLogixNGManager();
        
        logixNG = InstanceManager.getDefault(LogixNG_Manager.class)
                .createLogixNG("A new logix for test");  // NOI18N
        conditionalNG = new DefaultConditionalNGScaffold("IQC1", "A conditionalNG");  // NOI18N;
        InstanceManager.getDefault(ConditionalNG_Manager.class).register(conditionalNG);
        conditionalNG.setEnabled(true);
        conditionalNG.setRunDelayed(false);
        logixNG.addConditionalNG(conditionalNG);
        
        DoAnalogAction doAnalogAction = new DoAnalogAction("IQDA321", null);
        MaleSocket maleSocketDoAnalogAction =
                InstanceManager.getDefault(DigitalActionManager.class)
                        .registerAction(doAnalogAction);
        conditionalNG.getChild(0).connect(maleSocketDoAnalogAction);
        _memory = InstanceManager.getDefault(MemoryManager.class).provide("IM1");
        AnalogActionMemory analogActionMemory =
                new AnalogActionMemory("IQAA321", null);
        MaleSocket maleSocketAnalogActionMemory =
                InstanceManager.getDefault(AnalogActionManager.class)
                        .registerAction(analogActionMemory);
        doAnalogAction.getChild(1).connect(maleSocketAnalogActionMemory);
        analogActionMemory.setMemory(_memory);
        _base = analogActionMemory;
        _baseMaleSocket = maleSocketAnalogActionMemory;
        
        logixNG.setParentForAllChildren();
        logixNG.setEnabled(true);
        
        InstanceManager.getDefault(LogixNG_Manager.class).activateAllLogixNGs();
    }

    @After
    public void tearDown() {
        _base.dispose();
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.tearDown();
    }
    
}
