package jmri.jmrit.logixng.actions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

import javax.annotation.Nonnull;

import jmri.*;
import jmri.implementation.AbstractTurnout;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.implementation.DefaultConditionalNGScaffold;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test ActionTurnoutLock
 *
 * @author Daniel Bergqvist 2018
 */
public class ActionTurnoutLockTest extends AbstractDigitalActionTestBase {

    private LogixNG logixNG;
    private ConditionalNG conditionalNG;
    private ActionTurnoutLock actionTurnoutLock;
    private Turnout turnout;


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
        return String.format("Set lock for turnout IT1 to Lock ::: Use default%n");
    }

    @Override
    public String getExpectedPrintedTreeFromRoot() {
        return String.format(
                "LogixNG: A logixNG%n" +
                "   ConditionalNG: A conditionalNG%n" +
                "      ! A%n" +
                "         Set lock for turnout IT1 to Lock ::: Use default%n");
    }

    @Override
    public NamedBean createNewBean(String systemName) {
        return new ActionTurnoutLock(systemName, null);
    }

    @Override
    public boolean addNewSocket() {
        return false;
    }

    @Test
    public void testCtor() throws JmriException {
        assertNotNull( _base, "object exists");

        ActionTurnoutLock action2;
        assertNotNull( turnout, "turnout is not null");
        turnout.setState(Turnout.ON);

        action2 = new ActionTurnoutLock("IQDA321", null);
        assertNotNull( action2, "object exists");
        assertNull( action2.getUserName(), "Username matches");
        assertEquals( "Set lock for turnout '' to Unlock", action2.getLongDescription(), "String matches");

        action2 = new ActionTurnoutLock("IQDA321", "My turnout");
        assertNotNull( action2, "object exists");
        assertEquals( "My turnout", action2.getUserName(), "Username matches");
        assertEquals( "Set lock for turnout '' to Unlock", action2.getLongDescription(), "String matches");

        action2 = new ActionTurnoutLock("IQDA321", null);
        action2.getSelectNamedBean().setNamedBean(turnout);
        assertSame( turnout, action2.getSelectNamedBean().getNamedBean().getBean(), "turnout is correct");
        assertNotNull( action2, "object exists");
        assertNull( action2.getUserName(), "Username matches");
        assertEquals( "Set lock for turnout IT1 to Unlock", action2.getLongDescription(), "String matches");

        Turnout l = InstanceManager.getDefault(TurnoutManager.class).provide("IT1");
        action2 = new ActionTurnoutLock("IQDA321", "My turnout");
        action2.getSelectNamedBean().setNamedBean(l);
        assertSame( l, action2.getSelectNamedBean().getNamedBean().getBean(), "turnout is correct");
        assertNotNull( action2, "object exists");
        assertEquals( "My turnout", action2.getUserName(), "Username matches");
        assertEquals( "Set lock for turnout IT1 to Unlock", action2.getLongDescription(), "String matches");

        IllegalArgumentException ex = assertThrows( IllegalArgumentException.class, () -> {
            var t = new ActionTurnoutLock("IQA55:12:XY11", null);
            fail("Should have thrown, created " + t);
        }, "Illegal system name Expected exception thrown");
        assertNotNull(ex);

        ex = assertThrows( IllegalArgumentException.class, () -> {
            var t = new ActionTurnoutLock("IQA55:12:XY11", "A name");
            fail("Should have thrown, created " + t);
        }, "Illegal system name Expected exception thrown");
        assertNotNull(ex);

        // Test setup(). This method doesn't do anything, but execute it for coverage.
        _base.setup();
    }

    @Test
    public void testGetChild() {
        assertEquals( 0, actionTurnoutLock.getChildCount(), "getChildCount() returns 0");

        UnsupportedOperationException ex = assertThrows( UnsupportedOperationException.class, () ->
            actionTurnoutLock.getChild(0), "Exception is thrown");
        assertEquals( "Not supported.", ex.getMessage(), "Error message is correct");
    }

    @Test
    public void testTurnoutLock() {
        assertEquals( "Unlock", ActionTurnoutLock.TurnoutLock.Unlock.toString(), "String matches");
        assertEquals( "Lock", ActionTurnoutLock.TurnoutLock.Lock.toString(), "String matches");
        assertEquals( "Toggle", ActionTurnoutLock.TurnoutLock.Toggle.toString(), "String matches");
    }

    @Test
    public void testSetTurnout() {
        Turnout turnout11 = InstanceManager.getDefault(TurnoutManager.class).provide("IL11");
        Turnout turnout12 = InstanceManager.getDefault(TurnoutManager.class).provide("IL12");
        NamedBeanHandle<Turnout> turnoutHandle12 = InstanceManager.getDefault(NamedBeanHandleManager.class).getNamedBeanHandle(turnout12.getDisplayName(), turnout12);
        Turnout turnout13 = InstanceManager.getDefault(TurnoutManager.class).provide("IL13");
        Turnout turnout14 = InstanceManager.getDefault(TurnoutManager.class).provide("IL14");
        turnout14.setUserName("Some user name");

        actionTurnoutLock.getSelectNamedBean().removeNamedBean();
        assertNull( actionTurnoutLock.getSelectNamedBean().getNamedBean(), "turnout handle is null");

        actionTurnoutLock.getSelectNamedBean().setNamedBean(turnout11);
        assertSame( turnout11, actionTurnoutLock.getSelectNamedBean().getNamedBean().getBean(), "turnout is correct");

        actionTurnoutLock.getSelectNamedBean().removeNamedBean();
        assertNull( actionTurnoutLock.getSelectNamedBean().getNamedBean(), "turnout handle is null");

        actionTurnoutLock.getSelectNamedBean().setNamedBean(turnoutHandle12);
        assertSame( turnoutHandle12, actionTurnoutLock.getSelectNamedBean().getNamedBean(), "turnout handle is correct");

        actionTurnoutLock.getSelectNamedBean().setNamedBean("A non existent turnout");
        assertNull( actionTurnoutLock.getSelectNamedBean().getNamedBean(), "turnout handle is null");
        JUnitAppender.assertErrorMessage("Turnout \"A non existent turnout\" is not found");

        actionTurnoutLock.getSelectNamedBean().setNamedBean(turnout13.getSystemName());
        assertSame( turnout13, actionTurnoutLock.getSelectNamedBean().getNamedBean().getBean(), "turnout is correct");

        String turnout14userName = turnout14.getUserName();
        assertNotNull(turnout14userName);
        actionTurnoutLock.getSelectNamedBean().setNamedBean(turnout14userName);
        assertSame( turnout14, actionTurnoutLock.getSelectNamedBean().getNamedBean().getBean(), "turnout is correct");
    }

    @Test
    public void testAction() throws SocketAlreadyConnectedException, JmriException {
        // Set the light
        turnout.setLocked(Turnout.CABLOCKOUT + Turnout.PUSHBUTTONLOCKOUT, false);
        // The turnout should be closed
        assertFalse( turnout.getLocked(Turnout.CABLOCKOUT));
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the turnout should be thrown
        assertTrue(turnout.getLocked(Turnout.CABLOCKOUT));

        // Test to set turnout to closed
        actionTurnoutLock.getSelectEnum().setEnum(ActionTurnoutLock.TurnoutLock.Unlock);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the turnout should be thrown
        assertFalse( turnout.getLocked(Turnout.CABLOCKOUT));

        // Test to set turnout to toggle
        actionTurnoutLock.getSelectEnum().setEnum(ActionTurnoutLock.TurnoutLock.Toggle);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the turnout should be thrown
        assertTrue(turnout.getLocked(Turnout.CABLOCKOUT));

        // Test to set turnout to toggle
        actionTurnoutLock.getSelectEnum().setEnum(ActionTurnoutLock.TurnoutLock.Toggle);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the turnout should be thrown
        assertFalse( turnout.getLocked(Turnout.CABLOCKOUT));
    }

    @Test
    public void testIndirectAddressing() throws JmriException {

        Memory m1 = InstanceManager.getDefault(MemoryManager.class).provide("IM1");
        m1.setValue("IT102");

        assertTrue(conditionalNG.isActive());
        Turnout t1 = new MyTurnout("IT101"); InstanceManager.getDefault(TurnoutManager.class).register(t1);
        Turnout t2 = new MyTurnout("IT102"); InstanceManager.getDefault(TurnoutManager.class).register(t2);
        Turnout t3 = new MyTurnout("IT103"); InstanceManager.getDefault(TurnoutManager.class).register(t3);
        Turnout t4 = new MyTurnout("IT104"); InstanceManager.getDefault(TurnoutManager.class).register(t4);
        Turnout t5 = new MyTurnout("IT105"); InstanceManager.getDefault(TurnoutManager.class).register(t5);

        actionTurnoutLock.getSelectEnum().setEnum(ActionTurnoutLock.TurnoutLock.Lock);
        actionTurnoutLock.getSelectNamedBean().setNamedBean(t1.getSystemName());
        actionTurnoutLock.getSelectNamedBean().setReference("{IM1}");    // Points to "IT102"
        actionTurnoutLock.getSelectNamedBean().setLocalVariable("myTurnout");
        actionTurnoutLock.getSelectNamedBean().setFormula("\"IT10\" + str(index)");
        _baseMaleSocket.addLocalVariable("refTurnout", SymbolTable.InitialValueType.String, "IT103");
        _baseMaleSocket.addLocalVariable("myTurnout", SymbolTable.InitialValueType.String, "IT104");
        _baseMaleSocket.addLocalVariable("index", SymbolTable.InitialValueType.Integer, "5");

        // Test direct addressing
        actionTurnoutLock.getSelectNamedBean().setAddressing(NamedBeanAddressing.Direct);
        t1.setLocked(Turnout.CABLOCKOUT + Turnout.PUSHBUTTONLOCKOUT, false);
        t2.setLocked(Turnout.CABLOCKOUT + Turnout.PUSHBUTTONLOCKOUT, false);
        t3.setLocked(Turnout.CABLOCKOUT + Turnout.PUSHBUTTONLOCKOUT, false);
        t4.setLocked(Turnout.CABLOCKOUT + Turnout.PUSHBUTTONLOCKOUT, false);
        t5.setLocked(Turnout.CABLOCKOUT + Turnout.PUSHBUTTONLOCKOUT, false);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the correct turnout should be thrown
        assertTrue(t1.getLocked(Turnout.CABLOCKOUT));
        assertFalse( t2.getLocked(Turnout.CABLOCKOUT));
        assertFalse( t3.getLocked(Turnout.CABLOCKOUT));
        assertFalse( t4.getLocked(Turnout.CABLOCKOUT));
        assertFalse( t5.getLocked(Turnout.CABLOCKOUT));

        // Test reference by memory addressing
        actionTurnoutLock.getSelectNamedBean().setAddressing(NamedBeanAddressing.Reference);
        t1.setLocked(Turnout.CABLOCKOUT + Turnout.PUSHBUTTONLOCKOUT, false);
        t2.setLocked(Turnout.CABLOCKOUT + Turnout.PUSHBUTTONLOCKOUT, false);
        t3.setLocked(Turnout.CABLOCKOUT + Turnout.PUSHBUTTONLOCKOUT, false);
        t4.setLocked(Turnout.CABLOCKOUT + Turnout.PUSHBUTTONLOCKOUT, false);
        t5.setLocked(Turnout.CABLOCKOUT + Turnout.PUSHBUTTONLOCKOUT, false);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the correct turnout should be thrown
        assertFalse( t1.getLocked(Turnout.CABLOCKOUT));
        assertTrue(t2.getLocked(Turnout.CABLOCKOUT));
        assertFalse( t3.getLocked(Turnout.CABLOCKOUT));
        assertFalse( t4.getLocked(Turnout.CABLOCKOUT));
        assertFalse( t5.getLocked(Turnout.CABLOCKOUT));

        // Test reference by local variable addressing
        actionTurnoutLock.getSelectNamedBean().setReference("{refTurnout}");    // Points to "IT103"
        actionTurnoutLock.getSelectNamedBean().setAddressing(NamedBeanAddressing.Reference);
        t1.setLocked(Turnout.CABLOCKOUT + Turnout.PUSHBUTTONLOCKOUT, false);
        t2.setLocked(Turnout.CABLOCKOUT + Turnout.PUSHBUTTONLOCKOUT, false);
        t3.setLocked(Turnout.CABLOCKOUT + Turnout.PUSHBUTTONLOCKOUT, false);
        t4.setLocked(Turnout.CABLOCKOUT + Turnout.PUSHBUTTONLOCKOUT, false);
        t5.setLocked(Turnout.CABLOCKOUT + Turnout.PUSHBUTTONLOCKOUT, false);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the correct turnout should be thrown
        assertFalse(t1.getLocked(Turnout.CABLOCKOUT));
        assertFalse(t2.getLocked(Turnout.CABLOCKOUT));
        assertTrue(t3.getLocked(Turnout.CABLOCKOUT));
        assertFalse( t4.getLocked(Turnout.CABLOCKOUT));
        assertFalse( t5.getLocked(Turnout.CABLOCKOUT));

        // Test local variable addressing
        actionTurnoutLock.getSelectNamedBean().setAddressing(NamedBeanAddressing.LocalVariable);
        t1.setLocked(Turnout.CABLOCKOUT + Turnout.PUSHBUTTONLOCKOUT, false);
        t2.setLocked(Turnout.CABLOCKOUT + Turnout.PUSHBUTTONLOCKOUT, false);
        t3.setLocked(Turnout.CABLOCKOUT + Turnout.PUSHBUTTONLOCKOUT, false);
        t4.setLocked(Turnout.CABLOCKOUT + Turnout.PUSHBUTTONLOCKOUT, false);
        t5.setLocked(Turnout.CABLOCKOUT + Turnout.PUSHBUTTONLOCKOUT, false);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the correct turnout should be thrown
        assertFalse( t1.getLocked(Turnout.CABLOCKOUT));
        assertFalse( t2.getLocked(Turnout.CABLOCKOUT));
        assertFalse( t3.getLocked(Turnout.CABLOCKOUT));
        assertTrue(t4.getLocked(Turnout.CABLOCKOUT));
        assertFalse( t5.getLocked(Turnout.CABLOCKOUT));

        // Test formula addressing
        actionTurnoutLock.getSelectNamedBean().setAddressing(NamedBeanAddressing.Formula);
        t1.setLocked(Turnout.CABLOCKOUT + Turnout.PUSHBUTTONLOCKOUT, false);
        t2.setLocked(Turnout.CABLOCKOUT + Turnout.PUSHBUTTONLOCKOUT, false);
        t3.setLocked(Turnout.CABLOCKOUT + Turnout.PUSHBUTTONLOCKOUT, false);
        t4.setLocked(Turnout.CABLOCKOUT + Turnout.PUSHBUTTONLOCKOUT, false);
        t5.setLocked(Turnout.CABLOCKOUT + Turnout.PUSHBUTTONLOCKOUT, false);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the correct turnout should be thrown
        assertFalse( t1.getLocked(Turnout.CABLOCKOUT));
        assertFalse( t2.getLocked(Turnout.CABLOCKOUT));
        assertFalse( t3.getLocked(Turnout.CABLOCKOUT));
        assertFalse( t4.getLocked(Turnout.CABLOCKOUT));
        assertTrue(t5.getLocked(Turnout.CABLOCKOUT));
    }

    @Test
    public void testIndirectStateAddressing() throws JmriException {

        Memory m1 = InstanceManager.getDefault(MemoryManager.class).provide("IM1");
        m1.setValue("IT102");

        assertTrue(conditionalNG.isActive());


        // Test direct addressing
        actionTurnoutLock.getSelectEnum().setAddressing(NamedBeanAddressing.Direct);
        // Test Unlock
        turnout.setLocked(Turnout.CABLOCKOUT + Turnout.PUSHBUTTONLOCKOUT, true);
        actionTurnoutLock.getSelectEnum().setEnum(ActionTurnoutLock.TurnoutLock.Unlock);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the correct turnout should be thrown
        assertFalse(turnout.getLocked(Turnout.CABLOCKOUT));
        // Test Lock
        turnout.setLocked(Turnout.CABLOCKOUT + Turnout.PUSHBUTTONLOCKOUT, false);
        actionTurnoutLock.getSelectEnum().setEnum(ActionTurnoutLock.TurnoutLock.Lock);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the correct turnout should be thrown
        assertTrue(turnout.getLocked(Turnout.CABLOCKOUT));

        // Test reference by memory addressing
        actionTurnoutLock.getSelectEnum().setAddressing(NamedBeanAddressing.Reference);
        actionTurnoutLock.getSelectEnum().setReference("{IM1}");
        // Test Unlock
        m1.setValue("Unlock");
        turnout.setLocked(Turnout.CABLOCKOUT + Turnout.PUSHBUTTONLOCKOUT, true);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the correct turnout should be thrown
        assertFalse(turnout.getLocked(Turnout.CABLOCKOUT));
        // Test Lock
        m1.setValue("Lock");
        turnout.setLocked(Turnout.CABLOCKOUT + Turnout.PUSHBUTTONLOCKOUT, false);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the correct turnout should be thrown
        assertTrue(turnout.getLocked(Turnout.CABLOCKOUT));


        // Test reference by local variable addressing
        actionTurnoutLock.getSelectEnum().setAddressing(NamedBeanAddressing.Reference);
        actionTurnoutLock.getSelectEnum().setReference("{refVariable}");
        // Test Unlock
        _baseMaleSocket.clearLocalVariables();
        _baseMaleSocket.addLocalVariable("refVariable", SymbolTable.InitialValueType.String, "Unlock");
        turnout.setLocked(Turnout.CABLOCKOUT + Turnout.PUSHBUTTONLOCKOUT, true);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the correct turnout should be thrown
        assertFalse(turnout.getLocked(Turnout.CABLOCKOUT));
        // Test Lock
        _baseMaleSocket.clearLocalVariables();
        _baseMaleSocket.addLocalVariable("refVariable", SymbolTable.InitialValueType.String, "Lock");
        turnout.setLocked(Turnout.CABLOCKOUT + Turnout.PUSHBUTTONLOCKOUT, false);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the correct turnout should be thrown
        assertTrue(turnout.getLocked(Turnout.CABLOCKOUT));


        // Test local variable addressing
        actionTurnoutLock.getSelectEnum().setAddressing(NamedBeanAddressing.Reference);
        actionTurnoutLock.getSelectEnum().setLocalVariable("myVariable");
        // Test Unlock
        _baseMaleSocket.clearLocalVariables();
        _baseMaleSocket.addLocalVariable("refVariable", SymbolTable.InitialValueType.String, "Unlock");
        turnout.setLocked(Turnout.CABLOCKOUT + Turnout.PUSHBUTTONLOCKOUT, true);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the correct turnout should be thrown
        assertFalse(turnout.getLocked(Turnout.CABLOCKOUT));
        // Test Lock
        _baseMaleSocket.clearLocalVariables();
        _baseMaleSocket.addLocalVariable("refVariable", SymbolTable.InitialValueType.String, "Lock");
        turnout.setLocked(Turnout.CABLOCKOUT + Turnout.PUSHBUTTONLOCKOUT, false);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the correct turnout should be thrown
        assertTrue(turnout.getLocked(Turnout.CABLOCKOUT));


        // Test formula addressing
        actionTurnoutLock.getSelectEnum().setAddressing(NamedBeanAddressing.Formula);
        actionTurnoutLock.getSelectEnum().setFormula("refVariable + myVariable");
        // Test Unlock
        _baseMaleSocket.clearLocalVariables();
        _baseMaleSocket.addLocalVariable("refVariable", SymbolTable.InitialValueType.String, "Unlo");
        _baseMaleSocket.addLocalVariable("myVariable", SymbolTable.InitialValueType.String, "ck");
        turnout.setLocked(Turnout.CABLOCKOUT + Turnout.PUSHBUTTONLOCKOUT, true);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the correct turnout should be thrown
        assertFalse(turnout.getLocked(Turnout.CABLOCKOUT));
        // Test Lock
        _baseMaleSocket.clearLocalVariables();
        _baseMaleSocket.addLocalVariable("refVariable", SymbolTable.InitialValueType.String, "Lo");
        _baseMaleSocket.addLocalVariable("myVariable", SymbolTable.InitialValueType.String, "ck");
        turnout.setLocked(Turnout.CABLOCKOUT + Turnout.PUSHBUTTONLOCKOUT, false);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the correct turnout should be thrown
        assertTrue(turnout.getLocked(Turnout.CABLOCKOUT));
    }

    @Test
    public void testVetoableChange() throws PropertyVetoException {
        // Get the action and set the turnout
        turnout = InstanceManager.getDefault(TurnoutManager.class).provide("IT1");
        assertNotNull( turnout, "Turnout is not null");
        ActionTurnoutLock action = new ActionTurnoutLock(InstanceManager.getDefault(DigitalActionManager.class).getAutoSystemName(), null);
        action.getSelectNamedBean().setNamedBean(turnout);

        // Get some other turnout for later use
        Turnout otherTurnout = InstanceManager.getDefault(TurnoutManager.class).provide("IM99");
        assertNotNull( otherTurnout, "Turnout is not null");
        assertNotEquals( turnout, otherTurnout, "Turnout is not equal");

        // Test vetoableChange() for some other propery
        action.vetoableChange(new PropertyChangeEvent(this, "CanSomething", "test", null));
        assertEquals( turnout, action.getSelectNamedBean().getNamedBean().getBean(), "Turnout matches");

        // Test vetoableChange() for a string
        action.vetoableChange(new PropertyChangeEvent(this, "CanDelete", "test", null));
        assertEquals( turnout, action.getSelectNamedBean().getNamedBean().getBean(), "Turnout matches");
        action.vetoableChange(new PropertyChangeEvent(this, "DoDelete", "test", null));
        assertEquals( turnout, action.getSelectNamedBean().getNamedBean().getBean(), "Turnout matches");

        // Test vetoableChange() for another turnout
        action.vetoableChange(new PropertyChangeEvent(this, "CanDelete", otherTurnout, null));
        assertEquals( turnout, action.getSelectNamedBean().getNamedBean().getBean(), "Turnout matches");
        action.vetoableChange(new PropertyChangeEvent(this, "DoDelete", otherTurnout, null));
        assertEquals( turnout, action.getSelectNamedBean().getNamedBean().getBean(), "Turnout matches");

        // Test vetoableChange() for its own turnout
        PropertyVetoException ex = assertThrows( PropertyVetoException.class, () ->
            action.getSelectNamedBean().vetoableChange(
                    new PropertyChangeEvent(this, "CanDelete", turnout, null)),
                "Expected exception thrown");
        assertNotNull(ex);

        assertEquals( turnout, action.getSelectNamedBean().getNamedBean().getBean(), "Turnout matches");
        action.getSelectNamedBean().vetoableChange(new PropertyChangeEvent(this, "DoDelete", turnout, null));
        assertNull( action.getSelectNamedBean().getNamedBean(), "Turnout is null");
    }

    @Test
    public void testCategory() {
        assertSame( LogixNG_Category.ITEM, _base.getCategory(), "Category matches");
    }

    @Test
    public void testShortDescription() {
        assertEquals( "Turnout, lock", _base.getShortDescription(), "String matches");
    }

    @Test
    public void testLongDescription() {
        assertEquals( "Set lock for turnout IT1 to Lock", _base.getLongDescription(), "String matches");
    }

    @Test
    public void testChild() {
        assertEquals( 0, _base.getChildCount(), "Num children is zero");
        UnsupportedOperationException ex = assertThrows( UnsupportedOperationException.class, () ->
            _base.getChild(0), "Exception is thrown");
        assertEquals( "Not supported.", ex.getMessage(), "Error message is correct");
    }

    @BeforeEach
    public void setUp() throws SocketAlreadyConnectedException {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initLogixNGManager();

        _category = LogixNG_Category.ITEM;
        _isExternal = true;

//        turnout = InstanceManager.getDefault(TurnoutManager.class).provide("IT1");
        turnout = new MyTurnout("IT1");
        InstanceManager.getDefault(TurnoutManager.class).register(turnout);
        turnout.setLocked(Turnout.CABLOCKOUT + Turnout.PUSHBUTTONLOCKOUT, false);
        logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A logixNG");
        conditionalNG = new DefaultConditionalNGScaffold("IQC1", "A conditionalNG");  // NOI18N;
        InstanceManager.getDefault(ConditionalNG_Manager.class).register(conditionalNG);
        logixNG.addConditionalNG(conditionalNG);
        conditionalNG.setRunDelayed(false);
        conditionalNG.setEnabled(true);
        actionTurnoutLock = new ActionTurnoutLock(InstanceManager.getDefault(DigitalActionManager.class).getAutoSystemName(), null);
        actionTurnoutLock.getSelectNamedBean().setNamedBean(turnout);
        actionTurnoutLock.getSelectEnum().setEnum(ActionTurnoutLock.TurnoutLock.Lock);
        MaleSocket socket = InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionTurnoutLock);
        conditionalNG.getChild(0).connect(socket);

        _base = actionTurnoutLock;
        _baseMaleSocket = socket;

        assertTrue( logixNG.setParentForAllChildren(new ArrayList<>()));
        logixNG.activate();
        logixNG.setEnabled(true);
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.tearDown();
    }


    private static class MyTurnout extends AbstractTurnout {

        MyTurnout(@Nonnull String systemName) {
            super(systemName);
        }

        @Override
        protected void forwardCommandChangeToLayout(int s) {
            // Do nothing
        }

        @Override
        protected void turnoutPushbuttonLockout(boolean locked) {
            // Do nothing
        }

        @Override
        public boolean canLock(int turnoutLockout) {
            return true;
        }

    }

}
