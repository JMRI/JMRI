package jmri.jmrit.logixng.analog.actions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import jmri.InstanceManager;
import jmri.Memory;
import jmri.MemoryManager;
import jmri.NamedBeanHandle;
import jmri.NamedBeanHandleManager;
import jmri.jmrit.logixng.Category;
import jmri.jmrit.logixng.SocketAlreadyConnectedException;
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

    protected Memory _memory;
    
    @Test
    public void testCtor() {
        Assert.assertTrue("object exists", _base != null);
        
        AnalogActionMemory action2;
        Assert.assertNotNull("memory is not null", _memory);
        _memory.setValue(10.2);
        
        action2 = new AnalogActionMemory("IQAA11");
        Assert.assertNotNull("object exists", action2);
        Assert.assertTrue("Username matches", null == action2.getUserName());
        Assert.assertTrue("String matches", "Set memory none".equals(action2.getLongDescription()));
        
        action2 = new AnalogActionMemory("IQAA11", "My memory");
        Assert.assertNotNull("object exists", action2);
        Assert.assertTrue("Username matches", "My memory".equals(action2.getUserName()));
        Assert.assertTrue("String matches", "Set memory none".equals(action2.getLongDescription()));
        
        action2 = new AnalogActionMemory("IQAA11");
        action2.setMemory(_memory);
        Assert.assertNotNull("object exists", action2);
        Assert.assertTrue("Username matches", null == action2.getUserName());
        Assert.assertTrue("String matches", "Set memory IM1".equals(action2.getLongDescription()));
        
        action2 = new AnalogActionMemory("IQAA11", "My memory");
        action2.setMemory(_memory);
        Assert.assertNotNull("object exists", action2);
        Assert.assertTrue("Username matches", "My memory".equals(action2.getUserName()));
        Assert.assertTrue("String matches", "Set memory IM1".equals(action2.getLongDescription()));
        
        // Test template
        action2 = (AnalogActionMemory)_base.getNewObjectBasedOnTemplate("IQAA12");
        Assert.assertNotNull("object exists", action2);
        Assert.assertNull("Username is null", action2.getUserName());
//        Assert.assertTrue("Username matches", "My memory".equals(expression2.getUserName()));
        Assert.assertTrue("String matches", "Set memory IM1".equals(action2.getLongDescription()));
        
        boolean thrown = false;
        try {
            // Illegal system name
            new AnalogActionMemory("IQA55:12:XY11");
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
    public void testAction() throws SocketAlreadyConnectedException, SocketAlreadyConnectedException {
        AnalogActionMemory action = (AnalogActionMemory)_base;
        action.setValue(0.0d);
        Assert.assertTrue("Memory has correct value", 0.0d == (Double)_memory.getValue());
        action.setValue(1.0d);
        Assert.assertTrue("Memory has correct value", 1.0d == (Double)_memory.getValue());
        action.setMemory((Memory)null);
        action.setValue(2.0d);
        Assert.assertTrue("Memory has correct value", 1.0d == (Double)_memory.getValue());
    }
    
    @Test
    public void testMemory() {
        AnalogActionMemory action = (AnalogActionMemory)_base;
        action.setMemory((Memory)null);
        Assert.assertNull("Memory is null", action.getMemory());
        ((AnalogActionMemory)_base).setMemory(_memory);
        Assert.assertTrue("Memory matches", _memory == action.getMemory().getBean());
        
        action.setMemory((NamedBeanHandle<Memory>)null);
        Assert.assertNull("Memory is null", action.getMemory());
        Memory otherMemory = InstanceManager.getDefault(MemoryManager.class).provide("IM99");
        Assert.assertNotNull("memory is not null", otherMemory);
        NamedBeanHandle<Memory> memoryHandle = InstanceManager.getDefault(NamedBeanHandleManager.class)
                .getNamedBeanHandle(otherMemory.getDisplayName(), otherMemory);
        ((AnalogActionMemory)_base).setMemory(memoryHandle);
        Assert.assertTrue("Memory matches", memoryHandle == action.getMemory());
        Assert.assertTrue("Memory matches", otherMemory == action.getMemory().getBean());
        
        action.setMemory((String)null);
        Assert.assertNull("Memory is null", action.getMemory());
        action.setMemory(memoryHandle.getName());
        Assert.assertTrue("Memory matches", memoryHandle == action.getMemory());
        
        // Test setMemory with a memory name that doesn't exists
        action.setMemory("Non existent memory");
        Assert.assertTrue("Memory matches", memoryHandle == action.getMemory());
        JUnitAppender.assertWarnMessage("memory 'Non existent memory' does not exists");
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
        Assert.assertTrue("String matches", "Set memory IM1".equals(_base.getShortDescription()));
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
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initMemoryManager();
        _memory = InstanceManager.getDefault(MemoryManager.class).provide("IM1");
        _base = new AnalogActionMemory("IQAA321", "AnalogIO_Memory");
        ((AnalogActionMemory)_base).setMemory(_memory);
    }

    @After
    public void tearDown() {
        _base.dispose();
        JUnitUtil.tearDown();
    }
    
}
