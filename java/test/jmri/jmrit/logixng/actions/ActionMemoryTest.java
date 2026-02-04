package jmri.jmrit.logixng.actions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.util.ArrayList;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.NamedBeanHandle;
import jmri.NamedBeanHandleManager;
import jmri.Memory;
import jmri.MemoryManager;
import jmri.NamedBean;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.implementation.DefaultConditionalNGScaffold;
import jmri.jmrit.logixng.util.parser.ParserException;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test ActionMemory
 *
 * @author Daniel Bergqvist 2018
 */
public class ActionMemoryTest extends AbstractDigitalActionTestBase {

    private LogixNG logixNG;
    private ConditionalNG conditionalNG;
    private ActionMemory actionMemory;
    private Memory memory;


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
        return String.format("Set memory IM1 to \"\" ::: Use default%n");
    }

    @Override
    public String getExpectedPrintedTreeFromRoot() {
        return String.format(
                "LogixNG: A logixNG%n" +
                "   ConditionalNG: A conditionalNG%n" +
                "      ! A%n" +
                "         Set memory IM1 to \"\" ::: Use default%n");
    }

    @Override
    public NamedBean createNewBean(String systemName) {
        return new ActionMemory(systemName, null);
    }

    @Override
    public boolean addNewSocket() {
        return false;
    }

    @Test
    public void testCtor() throws JmriException {
        assertNotNull( _base, "object exists");

        ActionMemory action2;
        assertNotNull( memory, "memory is not null");
        memory.setValue("Old value");

        action2 = new ActionMemory("IQDA321", null);
        action2.setMemoryOperation(ActionMemory.MemoryOperation.SetToNull);
        assertNotNull( action2, "object exists");
        assertNull( action2.getUserName(), "Username matches");
        assertEquals( "Set memory '' to null", action2.getLongDescription(), "String matches");

        action2 = new ActionMemory("IQDA321", "My memory");
        action2.setMemoryOperation(ActionMemory.MemoryOperation.SetToString);
        action2.setOtherConstantValue("New value");
        assertNotNull( action2, "object exists");
        assertEquals( "My memory", action2.getUserName(), "Username matches");
        assertEquals( "Set memory '' to \"New value\"", action2.getLongDescription(), "String matches");

        action2 = new ActionMemory("IQDA321", null);
        action2.setMemoryOperation(ActionMemory.MemoryOperation.CopyMemoryToMemory);
        action2.getSelectNamedBean().setNamedBean(memory);
        Memory otherMemory = InstanceManager.getDefault(MemoryManager.class).provide("IM12");
        action2.getSelectOtherMemoryNamedBean().setNamedBean(otherMemory);
        assertSame( memory, action2.getSelectNamedBean().getNamedBean().getBean(), "memory is correct");
        assertNotNull( action2, "object exists");
        assertNull( action2.getUserName(), "Username matches");
        assertEquals( "Set memory IM1 to the value of memory IM12", action2.getLongDescription(), "String matches");

        IllegalArgumentException ex = assertThrows( IllegalArgumentException.class, () -> {
            var t = new ActionMemory("IQA55:12:XY11", null);
            fail("Did not throw, created " + t);
        }, "Illegal system name Expected exception thrown");
        assertNotNull(ex);

        ex = assertThrows( IllegalArgumentException.class, () -> {
            var t = new ActionMemory("IQA55:12:XY11", "A name");
            fail("Did not throw, created " + t);
        }, "Illegal system name Expected exception thrown");
        assertNotNull(ex);

        // Test setup(). This method doesn't do anything, but execute it for coverage.
        _base.setup();
    }

    @Test
    public void testGetChild() {
        assertEquals( 0, actionMemory.getChildCount(), "getChildCount() returns 0");

        UnsupportedOperationException ex = assertThrows( UnsupportedOperationException.class, () ->
            actionMemory.getChild(0), "Exception is thrown");
        assertEquals( "Not supported.", ex.getMessage(), "Error message is correct");
    }

    @Test
    public void testSetMemory() {
        actionMemory.unregisterListeners();

        Memory memory11 = InstanceManager.getDefault(MemoryManager.class).provide("IM11");
        Memory memory12 = InstanceManager.getDefault(MemoryManager.class).provide("IM12");
        NamedBeanHandle<Memory> memoryHandle12 = InstanceManager.getDefault(NamedBeanHandleManager.class).getNamedBeanHandle(memory12.getDisplayName(), memory12);
        Memory memory13 = InstanceManager.getDefault(MemoryManager.class).provide("IM13");
        Memory memory14 = InstanceManager.getDefault(MemoryManager.class).provide("IM14");
        memory14.setUserName("Some user name");

        actionMemory.getSelectNamedBean().removeNamedBean();
        assertNull( actionMemory.getSelectNamedBean().getNamedBean(), "memory handle is null");

        actionMemory.getSelectNamedBean().setNamedBean(memory11);
        assertSame( memory11, actionMemory.getSelectNamedBean().getNamedBean().getBean(), "memory is correct");

        actionMemory.getSelectNamedBean().removeNamedBean();
        assertNull( actionMemory.getSelectNamedBean().getNamedBean(), "memory handle is null");

        actionMemory.getSelectNamedBean().setNamedBean(memoryHandle12);
        assertSame( memoryHandle12, actionMemory.getSelectNamedBean().getNamedBean(), "memory handle is correct");

        actionMemory.getSelectNamedBean().setNamedBean("A non existent memory");
        assertNull( actionMemory.getSelectNamedBean().getNamedBean(), "memory handle is null");
        JUnitAppender.assertErrorMessage("Memory \"A non existent memory\" is not found");

        actionMemory.getSelectNamedBean().setNamedBean(memory13.getSystemName());
        assertSame( memory13, actionMemory.getSelectNamedBean().getNamedBean().getBean(), "memory is correct");

        String memory14UserName = memory14.getUserName();
        assertNotNull(memory14UserName);
        actionMemory.getSelectNamedBean().setNamedBean(memory14UserName);
        assertSame( memory14, actionMemory.getSelectNamedBean().getNamedBean().getBean(), "memory is correct");
    }

    @Test
    public void testAction() throws SocketAlreadyConnectedException, JmriException {
        // Set the memory
        memory.setValue("Old value");
        // The memory should have the value "Old value"
        assertEquals( "Old value", memory.getValue(), "memory has correct value");
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the memory should be set
        assertEquals( "", memory.getValue(), "memory has correct value");

        // Test to set memory to null
        actionMemory.setMemoryOperation(ActionMemory.MemoryOperation.SetToNull);
        // Execute the setMemoryOperation
        conditionalNG.execute();
        // The action should now be executed so the memory should be set
        assertNull( memory.getValue(), "memory has correct value");

        // Test to set memory to string
        actionMemory.setMemoryOperation(ActionMemory.MemoryOperation.SetToString);
        actionMemory.setOtherConstantValue("New value");
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the memory should be thrown
        assertEquals( "New value", memory.getValue(), "memory has correct value");

        // Test to copy memory to memory
        Memory otherMemory = InstanceManager.getDefault(MemoryManager.class).provide("IM2");
        memory.setValue("A value");
        otherMemory.setValue("Some other value");
        actionMemory.setMemoryOperation(ActionMemory.MemoryOperation.CopyMemoryToMemory);
        actionMemory.unregisterListeners();
        actionMemory.getSelectOtherMemoryNamedBean().setNamedBean(otherMemory);
        actionMemory.registerListeners();
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the memory should been copied to the other memory
        assertEquals( "Some other value", memory.getValue(), "memory has correct value");
        assertEquals( "Some other value", otherMemory.getValue(), "memory has correct value");
    }

    @Test
    public void testVetoableChange() throws PropertyVetoException {
        // Get the action and set the memory
        memory = InstanceManager.getDefault(MemoryManager.class).provide("IM1");
        assertNotNull( memory, "Memory is not null");
        ActionMemory action = new ActionMemory(InstanceManager.getDefault(DigitalActionManager.class).getAutoSystemName(), null);
        action.getSelectNamedBean().setNamedBean(memory);

        // Get some other memory for later use
        Memory otherMemory = InstanceManager.getDefault(MemoryManager.class).provide("IM99");
        assertNotNull( otherMemory, "Memory is not null");
        assertNotEquals( memory, otherMemory, "Memory is not equal");

        // Test vetoableChange() for some other propery
        action.vetoableChange(new PropertyChangeEvent(this, "CanSomething", "test", null));
        assertEquals( memory, action.getSelectNamedBean().getNamedBean().getBean(), "Memory matches");

        // Test vetoableChange() for a string
        action.vetoableChange(new PropertyChangeEvent(this, "CanDelete", "test", null));
        assertEquals( memory, action.getSelectNamedBean().getNamedBean().getBean(), "Memory matches");
        action.vetoableChange(new PropertyChangeEvent(this, "DoDelete", "test", null));
        assertEquals( memory, action.getSelectNamedBean().getNamedBean().getBean(), "Memory matches");

        // Test vetoableChange() for another memory
        action.vetoableChange(new PropertyChangeEvent(this, "CanDelete", otherMemory, null));
        assertEquals( memory, action.getSelectNamedBean().getNamedBean().getBean(), "Memory matches");
        action.vetoableChange(new PropertyChangeEvent(this, "DoDelete", otherMemory, null));
        assertEquals( memory, action.getSelectNamedBean().getNamedBean().getBean(), "Memory matches");

        // Test vetoableChange() for its own memory
        PropertyVetoException ex = assertThrows( PropertyVetoException.class, () ->
            action.getSelectNamedBean().vetoableChange(new PropertyChangeEvent(this, "CanDelete", memory, null)),
                "Expected exception thrown");
        assertNotNull(ex);

        assertEquals( memory, action.getSelectNamedBean().getNamedBean().getBean(), "Memory matches");
        action.vetoableChange(new PropertyChangeEvent(this, "DoDelete", memory, null));
        assertEquals( memory, action.getSelectNamedBean().getNamedBean().getBean(), "Memory still matches");
    }

    @Test
    public void testCategory() {
        assertSame( LogixNG_Category.ITEM, _base.getCategory(), "Category matches");
    }

    @Test
    public void testShortDescription() {
        assertEquals( "Memory", _base.getShortDescription(), "String matches");
    }

    @Test
    public void testLongDescription() throws ParserException {
        actionMemory.unregisterListeners();

        actionMemory.setMemoryOperation(ActionMemory.MemoryOperation.SetToNull);
        assertEquals( "Set memory IM1 to null", _base.getLongDescription(), "String matches");

        actionMemory.setMemoryOperation(ActionMemory.MemoryOperation.SetToString);
        actionMemory.setOtherConstantValue("Some new value");
        assertEquals( "Set memory IM1 to \"Some new value\"", _base.getLongDescription(), "String matches");

        actionMemory.setMemoryOperation(ActionMemory.MemoryOperation.CopyMemoryToMemory);
        Memory otherMemory = InstanceManager.getDefault(MemoryManager.class).provide("IM99");
        actionMemory.getSelectOtherMemoryNamedBean().setNamedBean(otherMemory);
        assertEquals( "Set memory IM1 to the value of memory IM99", _base.getLongDescription(), "String matches");
    }

    @Test
    public void testChild() {
        assertEquals( 0, _base.getChildCount(), "Num children is zero");
        UnsupportedOperationException ex = assertThrows( UnsupportedOperationException.class, () ->
            _base.getChild(0), "Exception is thrown");
        assertEquals( "Not supported.", ex.getMessage(), "Error message is correct");
    }

    @BeforeEach
    public void setUp() throws SocketAlreadyConnectedException, ParserException {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initMemoryManager();
        JUnitUtil.initLogixNGManager();

        _category = LogixNG_Category.ITEM;
        _isExternal = true;

        memory = InstanceManager.getDefault(MemoryManager.class).provide("IM1");
        memory.setValue("Old value");
        logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A logixNG");
        conditionalNG = new DefaultConditionalNGScaffold("IQC1", "A conditionalNG");  // NOI18N;
        InstanceManager.getDefault(ConditionalNG_Manager.class).register(conditionalNG);
        logixNG.addConditionalNG(conditionalNG);
        conditionalNG.setRunDelayed(false);
        conditionalNG.setEnabled(true);
        actionMemory = new ActionMemory(InstanceManager.getDefault(DigitalActionManager.class).getAutoSystemName(), null);
        actionMemory.getSelectNamedBean().setNamedBean(memory);
        actionMemory.setMemoryOperation(ActionMemory.MemoryOperation.SetToString);
        MaleSocket socket = InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionMemory);
        conditionalNG.getChild(0).connect(socket);

        _base = actionMemory;
        _baseMaleSocket = socket;

        assertTrue( logixNG.setParentForAllChildren(new ArrayList<>()));
        logixNG.activate();
        logixNG.setEnabled(true);
    }

    @AfterEach
    public void tearDown() {
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
