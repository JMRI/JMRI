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
import jmri.Memory;
import jmri.MemoryManager;
import jmri.NamedBean;
import jmri.NamedBeanHandle;
import jmri.NamedBeanHandleManager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.implementation.DefaultConditionalNGScaffold;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Before;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test StringActionMemory
 *
 * @author Daniel Bergqvist 2018
 */
public class StringActionMemoryTest extends AbstractStringActionTestBase {

    private LogixNG logixNG;
    private ConditionalNG conditionalNG;
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
        assertNotNull( _base, "object exists");

        StringActionMemory action2;
        assertNotNull( _memory, "memory is not null");
        _memory.setValue(10.2);

        action2 = new StringActionMemory("IQSA11", null);
        assertNotNull( action2, "object exists");
        assertNull( action2.getUserName(), "Username matches");
        assertEquals( "Set memory ''", action2.getLongDescription(), "String matches");

        action2 = new StringActionMemory("IQSA11", "My memory");
        assertNotNull( action2, "object exists");
        assertEquals( "My memory", action2.getUserName(), "Username matches");
        assertEquals( "Set memory ''", action2.getLongDescription(), "String matches");

        action2 = new StringActionMemory("IQSA11", null);
        action2.getSelectNamedBean().setNamedBean(_memory);
        assertNotNull( action2, "object exists");
        assertNull( action2.getUserName(), "Username matches");
        assertEquals( "Set memory IM1", action2.getLongDescription(), "String matches");

        action2 = new StringActionMemory("IQSA11", "My memory");
        action2.getSelectNamedBean().setNamedBean(_memory);
        assertNotNull( action2, "object exists");
        assertEquals( "My memory", action2.getUserName(), "Username matches");
        assertEquals( "Set memory IM1", action2.getLongDescription(), "String matches");

        IllegalArgumentException ex = assertThrows( IllegalArgumentException.class, () -> {
            var t = new StringActionMemory("IQA55:12:XY11", null);
            fail("Should have thrown, created " + t);
        }, "Illegal system name Expected exception thrown");
        assertNotNull(ex);

        ex = assertThrows( IllegalArgumentException.class, () -> {
            var t = new StringActionMemory("IQA55:12:XY11", "A name");
            fail("Should have thrown, created " + t);
        }, "Illegal system name Expected exception thrown");
        assertNotNull(ex);
    }

    @Test
    public void testAction() throws JmriException, SocketAlreadyConnectedException, SocketAlreadyConnectedException {
        StringActionMemory action = (StringActionMemory)_base;
        action.setValue("");
        assertEquals( "", _memory.getValue(), "Memory has correct value");
        action.setValue("Test");
        assertEquals( "Test", _memory.getValue(), "Memory has correct value");
        action.getSelectNamedBean().removeNamedBean();
        action.setValue("Other test");
        assertEquals( "Test", _memory.getValue(), "Memory has correct value");
    }

    @Test
    public void testMemory() {
        StringActionMemory action = (StringActionMemory)_base;
        action.getSelectNamedBean().removeNamedBean();
        assertNull( action.getSelectNamedBean().getNamedBean(), "Memory is null");
        ((StringActionMemory)_base).getSelectNamedBean().setNamedBean(_memory);
        assertSame( _memory, action.getSelectNamedBean().getNamedBean().getBean(), "Memory matches");

        action.getSelectNamedBean().removeNamedBean();
        assertNull( action.getSelectNamedBean().getNamedBean(), "Memory is null");
        Memory otherMemory = InstanceManager.getDefault(MemoryManager.class).provide("IM99");
        assertNotNull( otherMemory, "memory is not null");
        NamedBeanHandle<Memory> memoryHandle = InstanceManager.getDefault(NamedBeanHandleManager.class)
                .getNamedBeanHandle(otherMemory.getDisplayName(), otherMemory);
        ((StringActionMemory)_base).getSelectNamedBean().setNamedBean(memoryHandle);
        assertSame( memoryHandle, action.getSelectNamedBean().getNamedBean(), "Memory matches");
        assertSame( otherMemory, action.getSelectNamedBean().getNamedBean().getBean(), "Memory matches");

        action.getSelectNamedBean().removeNamedBean();
        assertNull( action.getSelectNamedBean().getNamedBean(), "Memory is null");
        action.getSelectNamedBean().setNamedBean(memoryHandle.getName());
        assertSame( memoryHandle, action.getSelectNamedBean().getNamedBean(), "Memory matches");

        // Test getSelectNamedBean().setNamedBean with a memory name that doesn't exists
        action.getSelectNamedBean().setNamedBean("Non existent memory");
        assertNull( action.getSelectNamedBean().getNamedBean(), "Memory is null");
        JUnitAppender.assertErrorMessage("Memory \"Non existent memory\" is not found");
    }

    @Test
    public void testVetoableChange() throws PropertyVetoException {
        // Get some other memory for later use
        Memory otherMemory = InstanceManager.getDefault(MemoryManager.class).provide("IM99");
        assertNotNull( otherMemory, "Memory is not null");
        assertNotEquals( _memory, otherMemory, "Memory is not equal");

        // Get the expression and set the memory
        StringActionMemory action = (StringActionMemory)_base;
        action.getSelectNamedBean().setNamedBean(_memory);
        assertEquals( _memory, action.getSelectNamedBean().getNamedBean().getBean(), "Memory matches");

        // Test vetoableChange() for some other propery
        action.vetoableChange(new PropertyChangeEvent(this, "CanSomething", "test", null));
        assertEquals( _memory, action.getSelectNamedBean().getNamedBean().getBean(), "Memory matches");

        // Test vetoableChange() for a string
        action.vetoableChange(new PropertyChangeEvent(this, "CanDelete", "test", null));
        assertEquals( _memory, action.getSelectNamedBean().getNamedBean().getBean(), "Memory matches");
        action.vetoableChange(new PropertyChangeEvent(this, "DoDelete", "test", null));
        assertEquals( _memory, action.getSelectNamedBean().getNamedBean().getBean(), "Memory matches");

        // Test vetoableChange() for another memory
        action.vetoableChange(new PropertyChangeEvent(this, "CanDelete", otherMemory, null));
        assertEquals( _memory, action.getSelectNamedBean().getNamedBean().getBean(), "Memory matches");
        action.vetoableChange(new PropertyChangeEvent(this, "DoDelete", otherMemory, null));
        assertEquals( _memory, action.getSelectNamedBean().getNamedBean().getBean(), "Memory matches");

        // Test vetoableChange() for its own memory
        PropertyVetoException ex = assertThrows( PropertyVetoException.class, () ->
            action.getSelectNamedBean().vetoableChange(new PropertyChangeEvent(this, "CanDelete", _memory, null)),
                "Expected exception thrown");
        assertNotNull(ex);

        assertEquals( _memory, action.getSelectNamedBean().getNamedBean().getBean(), "Memory matches");
        action.getSelectNamedBean().vetoableChange(new PropertyChangeEvent(this, "DoDelete", _memory, null));
        assertNull( action.getSelectNamedBean().getNamedBean(), "Memory is null");
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
    public void testLongDescription() {
        assertEquals( "Set memory IM1", _base.getLongDescription(), "String matches");
    }

    @Test
    public void testChild() {
        assertEquals( 0, _base.getChildCount(), "Num children is zero");
        UnsupportedOperationException ex = assertThrows( UnsupportedOperationException.class, () ->
            _base.getChild(0), "Exception is thrown");
        assertEquals( "Not supported.", ex.getMessage(), "Error message is correct");
    }

    @Before
    @BeforeEach
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
        stringActionMemory.getSelectNamedBean().setNamedBean(_memory);
        _base = stringActionMemory;
        _baseMaleSocket = maleSocketStringActionMemory;

        assertTrue( logixNG.setParentForAllChildren(new ArrayList<>()));
        logixNG.activate();
        logixNG.setEnabled(true);
    }

    @After
    @AfterEach
    public void tearDown() {
        _base.dispose();
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
