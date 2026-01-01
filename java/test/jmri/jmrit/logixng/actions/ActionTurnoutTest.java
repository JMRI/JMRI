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

import jmri.*;
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
 * Test ActionTurnout
 *
 * @author Daniel Bergqvist 2018
 */
public class ActionTurnoutTest extends AbstractDigitalActionTestBase {

    private LogixNG logixNG;
    private ConditionalNG conditionalNG;
    private ActionTurnout actionTurnout;
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
        return String.format("Set turnout IT1 to state Thrown ::: Use default%n");
    }

    @Override
    public String getExpectedPrintedTreeFromRoot() {
        return String.format(
                "LogixNG: A logixNG%n" +
                "   ConditionalNG: A conditionalNG%n" +
                "      ! A%n" +
                "         Set turnout IT1 to state Thrown ::: Use default%n");
    }

    @Override
    public NamedBean createNewBean(String systemName) {
        return new ActionTurnout(systemName, null);
    }

    @Override
    public boolean addNewSocket() {
        return false;
    }

    @Test
    public void testCtor() throws JmriException {
        assertNotNull( _base, "object exists");

        ActionTurnout action2;
        assertNotNull( turnout, "turnout is not null");
        turnout.setState(Turnout.ON);

        action2 = new ActionTurnout("IQDA321", null);
        assertNotNull( action2, "object exists");
        assertNull( action2.getUserName(), "Username matches");
        assertEquals( "Set turnout '' to state Thrown", action2.getLongDescription(), "String matches");

        action2 = new ActionTurnout("IQDA321", "My turnout");
        assertNotNull( action2, "object exists");
        assertEquals( "My turnout", action2.getUserName(), "Username matches");
        assertEquals( "Set turnout '' to state Thrown", action2.getLongDescription(), "String matches");

        action2 = new ActionTurnout("IQDA321", null);
        action2.getSelectNamedBean().setNamedBean(turnout);
        assertSame( turnout, action2.getSelectNamedBean().getNamedBean().getBean(), "turnout is correct");
        assertNotNull( action2, "object exists");
        assertNull( action2.getUserName(), "Username matches");
        assertEquals( "Set turnout IT1 to state Thrown", action2.getLongDescription(), "String matches");

        Turnout l = InstanceManager.getDefault(TurnoutManager.class).provide("IT1");
        action2 = new ActionTurnout("IQDA321", "My turnout");
        action2.getSelectNamedBean().setNamedBean(l);
        assertSame( l, action2.getSelectNamedBean().getNamedBean().getBean(), "turnout is correct");
        assertNotNull( action2, "object exists");
        assertEquals( "My turnout", action2.getUserName(), "Username matches");
        assertEquals( "Set turnout IT1 to state Thrown", action2.getLongDescription(), "String matches");

        IllegalArgumentException ex = assertThrows( IllegalArgumentException.class, () -> {
            var t = new ActionTurnout("IQA55:12:XY11", null);
            fail("Should have thrown, created " + t);
        }, "Illegal system name Expected exception thrown");
        assertNotNull(ex);

        ex = assertThrows( IllegalArgumentException.class, () -> {
            var t = new ActionTurnout("IQA55:12:XY11", "A name");
            fail("Should have thrown, created " + t);
        }, "Illegal system name Expected exception thrown");
        assertNotNull(ex);

        // Test setup(). This method doesn't do anything, but execute it for coverage.
        _base.setup();
    }

    @Test
    public void testGetChild() {
        assertEquals( 0, actionTurnout.getChildCount(), "getChildCount() returns 0");

        UnsupportedOperationException ex = assertThrows( UnsupportedOperationException.class, () ->
            actionTurnout.getChild(0), "Exception is thrown");
        assertEquals( "Not supported.", ex.getMessage(), "Error message is correct");
    }

    @Test
    public void testTurnoutState() {
        assertEquals( "Closed", ActionTurnout.TurnoutState.Closed.toString(), "String matches");
        assertEquals( "Thrown", ActionTurnout.TurnoutState.Thrown.toString(), "String matches");
        assertEquals( "Toggle", ActionTurnout.TurnoutState.Toggle.toString(), "String matches");

        assertSame( ActionTurnout.TurnoutState.Closed, ActionTurnout.TurnoutState.get(Turnout.CLOSED), "objects are equal");
        assertSame( ActionTurnout.TurnoutState.Thrown, ActionTurnout.TurnoutState.get(Turnout.THROWN), "objects are equal");
        assertSame( ActionTurnout.TurnoutState.Toggle, ActionTurnout.TurnoutState.get(-1), "objects are equal");

        IllegalArgumentException ex = assertThrows( IllegalArgumentException.class, () ->
            ActionTurnout.TurnoutState.get(100),    // The number 100 is a state that ActionTurnout.TurnoutState isn't aware of
                "Exception is thrown");
        assertEquals( "invalid turnout state", ex.getMessage(), "Error message is correct");
    }

    @Test
    public void testSetTurnout() {
        Turnout turnout11 = InstanceManager.getDefault(TurnoutManager.class).provide("IL11");
        Turnout turnout12 = InstanceManager.getDefault(TurnoutManager.class).provide("IL12");
        NamedBeanHandle<Turnout> turnoutHandle12 = InstanceManager.getDefault(NamedBeanHandleManager.class).getNamedBeanHandle(turnout12.getDisplayName(), turnout12);
        Turnout turnout13 = InstanceManager.getDefault(TurnoutManager.class).provide("IL13");
        Turnout turnout14 = InstanceManager.getDefault(TurnoutManager.class).provide("IL14");
        turnout14.setUserName("Some user name");

        actionTurnout.getSelectNamedBean().removeNamedBean();
        assertNull( actionTurnout.getSelectNamedBean().getNamedBean(), "turnout handle is null");

        actionTurnout.getSelectNamedBean().setNamedBean(turnout11);
        assertSame( turnout11, actionTurnout.getSelectNamedBean().getNamedBean().getBean(), "turnout is correct");

        actionTurnout.getSelectNamedBean().removeNamedBean();
        assertNull( actionTurnout.getSelectNamedBean().getNamedBean(), "turnout handle is null");

        actionTurnout.getSelectNamedBean().setNamedBean(turnoutHandle12);
        assertSame( turnoutHandle12, actionTurnout.getSelectNamedBean().getNamedBean(), "turnout handle is correct");

        actionTurnout.getSelectNamedBean().setNamedBean("A non existent turnout");
        assertNull( actionTurnout.getSelectNamedBean().getNamedBean(), "turnout handle is null");
        JUnitAppender.assertErrorMessage("Turnout \"A non existent turnout\" is not found");

        actionTurnout.getSelectNamedBean().setNamedBean(turnout13.getSystemName());
        assertSame( turnout13, actionTurnout.getSelectNamedBean().getNamedBean().getBean(), "turnout is correct");

        String turnout14UserName = turnout14.getUserName();
        assertNotNull(turnout14UserName);
        actionTurnout.getSelectNamedBean().setNamedBean(turnout14UserName);
        assertSame( turnout14, actionTurnout.getSelectNamedBean().getNamedBean().getBean(), "turnout is correct");
    }

    @Test
    public void testAction() throws SocketAlreadyConnectedException, JmriException {
        // Set the light
        turnout.setCommandedState(Turnout.CLOSED);
        // The turnout should be closed
        assertEquals( Turnout.CLOSED, turnout.getCommandedState(), "turnout is closed");
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the turnout should be thrown
        assertEquals( Turnout.THROWN, turnout.getCommandedState(), "turnout is thrown");

        // Test to set turnout to closed
        actionTurnout.getSelectEnum().setEnum(ActionTurnout.TurnoutState.Closed);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the turnout should be closed
        assertEquals( Turnout.CLOSED, turnout.getCommandedState(), "turnout is closed");

        // Test to set turnout to toggle
        actionTurnout.getSelectEnum().setEnum(ActionTurnout.TurnoutState.Toggle);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the turnout should be thrown
        assertEquals( Turnout.THROWN, turnout.getCommandedState(), "turnout is thrown");

        // Test to set turnout to toggle
        actionTurnout.getSelectEnum().setEnum(ActionTurnout.TurnoutState.Toggle);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the turnout should be closed
        assertEquals( Turnout.CLOSED, turnout.getCommandedState(), "turnout is closed");
    }

    @Test
    public void testIndirectAddressing() throws JmriException {

        Memory m1 = InstanceManager.getDefault(MemoryManager.class).provide("IM1");
        m1.setValue("IT102");

        assertTrue(conditionalNG.isActive());
        Turnout t1 = InstanceManager.getDefault(TurnoutManager.class).provide("IT101");
        Turnout t2 = InstanceManager.getDefault(TurnoutManager.class).provide("IT102");
        Turnout t3 = InstanceManager.getDefault(TurnoutManager.class).provide("IT103");
        Turnout t4 = InstanceManager.getDefault(TurnoutManager.class).provide("IT104");
        Turnout t5 = InstanceManager.getDefault(TurnoutManager.class).provide("IT105");

        actionTurnout.getSelectEnum().setEnum(ActionTurnout.TurnoutState.Thrown);
        actionTurnout.getSelectNamedBean().setNamedBean(t1.getSystemName());
        actionTurnout.getSelectNamedBean().setReference("{IM1}");    // Points to "IT102"
        actionTurnout.getSelectNamedBean().setLocalVariable("myTurnout");
        actionTurnout.getSelectNamedBean().setFormula("\"IT10\" + str(index)");
        _baseMaleSocket.addLocalVariable("refTurnout", SymbolTable.InitialValueType.String, "IT103");
        _baseMaleSocket.addLocalVariable("myTurnout", SymbolTable.InitialValueType.String, "IT104");
        _baseMaleSocket.addLocalVariable("index", SymbolTable.InitialValueType.Integer, "5");

        // Test direct addressing
        actionTurnout.getSelectNamedBean().setAddressing(NamedBeanAddressing.Direct);
        t1.setState(Turnout.CLOSED);
        t2.setState(Turnout.CLOSED);
        t3.setState(Turnout.CLOSED);
        t4.setState(Turnout.CLOSED);
        t5.setState(Turnout.CLOSED);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the correct turnout should be thrown
        assertEquals(Turnout.THROWN, t1.getCommandedState());
        assertEquals(Turnout.CLOSED, t2.getCommandedState());
        assertEquals(Turnout.CLOSED, t3.getCommandedState());
        assertEquals(Turnout.CLOSED, t4.getCommandedState());
        assertEquals(Turnout.CLOSED, t5.getCommandedState());

        // Test reference by memory addressing
        actionTurnout.getSelectNamedBean().setAddressing(NamedBeanAddressing.Reference);
        t1.setState(Turnout.CLOSED);
        t2.setState(Turnout.CLOSED);
        t3.setState(Turnout.CLOSED);
        t4.setState(Turnout.CLOSED);
        t5.setState(Turnout.CLOSED);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the correct turnout should be thrown
        assertEquals(Turnout.CLOSED, t1.getCommandedState());
        assertEquals(Turnout.THROWN, t2.getCommandedState());
        assertEquals(Turnout.CLOSED, t3.getCommandedState());
        assertEquals(Turnout.CLOSED, t4.getCommandedState());
        assertEquals(Turnout.CLOSED, t5.getCommandedState());

        // Test reference by local variable addressing
        actionTurnout.getSelectNamedBean().setReference("{refTurnout}");    // Points to "IT103"
        actionTurnout.getSelectNamedBean().setAddressing(NamedBeanAddressing.Reference);
        t1.setState(Turnout.CLOSED);
        t2.setState(Turnout.CLOSED);
        t3.setState(Turnout.CLOSED);
        t4.setState(Turnout.CLOSED);
        t5.setState(Turnout.CLOSED);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the correct turnout should be thrown
        assertEquals(Turnout.CLOSED, t1.getCommandedState());
        assertEquals(Turnout.CLOSED, t2.getCommandedState());
        assertEquals(Turnout.THROWN, t3.getCommandedState());
        assertEquals(Turnout.CLOSED, t4.getCommandedState());
        assertEquals(Turnout.CLOSED, t5.getCommandedState());

        // Test local variable addressing
        actionTurnout.getSelectNamedBean().setAddressing(NamedBeanAddressing.LocalVariable);
        t1.setState(Turnout.CLOSED);
        t2.setState(Turnout.CLOSED);
        t3.setState(Turnout.CLOSED);
        t4.setState(Turnout.CLOSED);
        t5.setState(Turnout.CLOSED);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the correct turnout should be thrown
        assertEquals(Turnout.CLOSED, t1.getCommandedState());
        assertEquals(Turnout.CLOSED, t2.getCommandedState());
        assertEquals(Turnout.CLOSED, t3.getCommandedState());
        assertEquals(Turnout.THROWN, t4.getCommandedState());
        assertEquals(Turnout.CLOSED, t5.getCommandedState());

        // Test formula addressing
        actionTurnout.getSelectNamedBean().setAddressing(NamedBeanAddressing.Formula);
        t1.setState(Turnout.CLOSED);
        t2.setState(Turnout.CLOSED);
        t3.setState(Turnout.CLOSED);
        t4.setState(Turnout.CLOSED);
        t5.setState(Turnout.CLOSED);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the correct turnout should be thrown
        assertEquals(Turnout.CLOSED, t1.getCommandedState());
        assertEquals(Turnout.CLOSED, t2.getCommandedState());
        assertEquals(Turnout.CLOSED, t3.getCommandedState());
        assertEquals(Turnout.CLOSED, t4.getCommandedState());
        assertEquals(Turnout.THROWN, t5.getCommandedState());
    }

    @Test
    public void testIndirectStateAddressing() throws JmriException {

        Memory m1 = InstanceManager.getDefault(MemoryManager.class).provide("IM1");
        m1.setValue("IT102");

        assertTrue(conditionalNG.isActive());


        // Test direct addressing
        actionTurnout.getSelectEnum().setAddressing(NamedBeanAddressing.Direct);
        // Test Closed
        turnout.setState(Turnout.THROWN);
        actionTurnout.getSelectEnum().setEnum(ActionTurnout.TurnoutState.Closed);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the correct turnout should be thrown
        assertEquals(Turnout.CLOSED, turnout.getCommandedState());
        // Test Thrown
        turnout.setState(Turnout.CLOSED);
        actionTurnout.getSelectEnum().setEnum(ActionTurnout.TurnoutState.Thrown);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the correct turnout should be thrown
        assertEquals(Turnout.THROWN, turnout.getCommandedState());


        // Test reference by memory addressing
        actionTurnout.getSelectEnum().setAddressing(NamedBeanAddressing.Reference);
        actionTurnout.getSelectEnum().setReference("{IM1}");
        // Test Closed
        m1.setValue("Closed");
        turnout.setState(Turnout.THROWN);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the correct turnout should be thrown
        assertEquals(Turnout.CLOSED, turnout.getCommandedState());
        // Test Thrown
        m1.setValue("Thrown");
        turnout.setState(Turnout.CLOSED);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the correct turnout should be thrown
        assertEquals(Turnout.THROWN, turnout.getCommandedState());


        // Test reference by local variable addressing
        actionTurnout.getSelectEnum().setAddressing(NamedBeanAddressing.Reference);
        actionTurnout.getSelectEnum().setReference("{refVariable}");
        // Test Closed
        _baseMaleSocket.clearLocalVariables();
        _baseMaleSocket.addLocalVariable("refVariable", SymbolTable.InitialValueType.String, "Closed");
        turnout.setState(Turnout.THROWN);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the correct turnout should be thrown
        assertEquals(Turnout.CLOSED, turnout.getCommandedState());
        // Test Thrown
        _baseMaleSocket.clearLocalVariables();
        _baseMaleSocket.addLocalVariable("refVariable", SymbolTable.InitialValueType.String, "Thrown");
        turnout.setState(Turnout.CLOSED);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the correct turnout should be thrown
        assertEquals(Turnout.THROWN, turnout.getCommandedState());


        // Test local variable addressing
        actionTurnout.getSelectEnum().setAddressing(NamedBeanAddressing.Reference);
        actionTurnout.getSelectEnum().setLocalVariable("myVariable");
        // Test Closed
        _baseMaleSocket.clearLocalVariables();
        _baseMaleSocket.addLocalVariable("refVariable", SymbolTable.InitialValueType.String, "Closed");
        turnout.setState(Turnout.THROWN);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the correct turnout should be thrown
        assertEquals(Turnout.CLOSED, turnout.getCommandedState());
        // Test Thrown
        _baseMaleSocket.clearLocalVariables();
        _baseMaleSocket.addLocalVariable("refVariable", SymbolTable.InitialValueType.String, "Thrown");
        turnout.setState(Turnout.CLOSED);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the correct turnout should be thrown
        assertEquals(Turnout.THROWN, turnout.getCommandedState());


        // Test formula addressing
        actionTurnout.getSelectEnum().setAddressing(NamedBeanAddressing.Formula);
        actionTurnout.getSelectEnum().setFormula("refVariable + myVariable");
        // Test Closed
        _baseMaleSocket.clearLocalVariables();
        _baseMaleSocket.addLocalVariable("refVariable", SymbolTable.InitialValueType.String, "Clo");
        _baseMaleSocket.addLocalVariable("myVariable", SymbolTable.InitialValueType.String, "sed");
        turnout.setState(Turnout.THROWN);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the correct turnout should be thrown
        assertEquals(Turnout.CLOSED, turnout.getCommandedState());
        // Test Thrown
        _baseMaleSocket.clearLocalVariables();
        _baseMaleSocket.addLocalVariable("refVariable", SymbolTable.InitialValueType.String, "Thro");
        _baseMaleSocket.addLocalVariable("myVariable", SymbolTable.InitialValueType.String, "wn");
        turnout.setState(Turnout.CLOSED);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the correct turnout should be thrown
        assertEquals(Turnout.THROWN, turnout.getCommandedState());
    }

    @Test
    public void testVetoableChange() throws PropertyVetoException {
        // Get the action and set the turnout
        Turnout turnout2 = InstanceManager.getDefault(TurnoutManager.class).provide("IT1");
        assertNotNull( turnout2, "Turnout is not null");
        ActionTurnout action = new ActionTurnout(InstanceManager.getDefault(DigitalActionManager.class).getAutoSystemName(), null);
        action.getSelectNamedBean().setNamedBean(turnout2);

        // Get some other turnout for later use
        Turnout otherTurnout = InstanceManager.getDefault(TurnoutManager.class).provide("IM99");
        assertNotNull( otherTurnout, "Turnout is not null");
        assertNotEquals( turnout2, otherTurnout, "Turnout is not equal");

        // Test vetoableChange() for some other propery
        action.vetoableChange(new PropertyChangeEvent(this, "CanSomething", "test", null));
        assertEquals( turnout2, action.getSelectNamedBean().getNamedBean().getBean(), "Turnout matches");

        // Test vetoableChange() for a string
        action.vetoableChange(new PropertyChangeEvent(this, "CanDelete", "test", null));
        assertEquals( turnout2, action.getSelectNamedBean().getNamedBean().getBean(), "Turnout matches");
        action.vetoableChange(new PropertyChangeEvent(this, "DoDelete", "test", null));
        assertEquals( turnout2, action.getSelectNamedBean().getNamedBean().getBean(), "Turnout matches");

        // Test vetoableChange() for another turnout
        action.vetoableChange(new PropertyChangeEvent(this, "CanDelete", otherTurnout, null));
        assertEquals( turnout2, action.getSelectNamedBean().getNamedBean().getBean(), "Turnout matches");
        action.vetoableChange(new PropertyChangeEvent(this, "DoDelete", otherTurnout, null));
        assertEquals( turnout2, action.getSelectNamedBean().getNamedBean().getBean(), "Turnout matches");

        // Test vetoableChange() for its own turnout
        PropertyVetoException ex = assertThrows( PropertyVetoException.class, () ->
            action.getSelectNamedBean().vetoableChange(new PropertyChangeEvent(this, "CanDelete", turnout2, null)),
                "Expected exception thrown");
        assertNotNull(ex);

        assertEquals( turnout2, action.getSelectNamedBean().getNamedBean().getBean(), "Turnout matches");
        action.getSelectNamedBean().vetoableChange(new PropertyChangeEvent(this, "DoDelete", turnout2, null));
        assertNull( action.getSelectNamedBean().getNamedBean(), "Turnout is null");
    }

    @Test
    public void testCategory() {
        assertSame( LogixNG_Category.ITEM, _base.getCategory(), "Category matches");
    }

    @Test
    public void testShortDescription() {
        assertEquals( "Turnout", _base.getShortDescription(), "String matches");
    }

    @Test
    public void testLongDescription() {
        assertEquals( "Set turnout IT1 to state Thrown", _base.getLongDescription(), "String matches");
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
        JUnitUtil.initLogixNGManager();

        _category = LogixNG_Category.ITEM;
        _isExternal = true;

        turnout = InstanceManager.getDefault(TurnoutManager.class).provide("IT1");
        turnout.setCommandedState(Turnout.CLOSED);
        logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A logixNG");
        conditionalNG = new DefaultConditionalNGScaffold("IQC1", "A conditionalNG");  // NOI18N;
        InstanceManager.getDefault(ConditionalNG_Manager.class).register(conditionalNG);
        logixNG.addConditionalNG(conditionalNG);
        conditionalNG.setRunDelayed(false);
        conditionalNG.setEnabled(true);
        actionTurnout = new ActionTurnout(InstanceManager.getDefault(DigitalActionManager.class).getAutoSystemName(), null);
        actionTurnout.getSelectNamedBean().setNamedBean(turnout);
        actionTurnout.getSelectEnum().setEnum(ActionTurnout.TurnoutState.Thrown);
        MaleSocket socket = InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionTurnout);
        conditionalNG.getChild(0).connect(socket);

        _base = actionTurnout;
        _baseMaleSocket = socket;

        assertTrue( logixNG.setParentForAllChildren(new ArrayList<>()));
        logixNG.activate();
        logixNG.setEnabled(true);
    }

    @After
    @AfterEach
    public void tearDown() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.tearDown();
    }

}
