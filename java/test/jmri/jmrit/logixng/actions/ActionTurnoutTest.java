package jmri.jmrit.logixng.actions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.util.ArrayList;

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
        Assert.assertTrue("object exists", _base != null);
        
        ActionTurnout action2;
        Assert.assertNotNull("turnout is not null", turnout);
        turnout.setState(Turnout.ON);
        
        action2 = new ActionTurnout("IQDA321", null);
        Assert.assertNotNull("object exists", action2);
        Assert.assertNull("Username matches", action2.getUserName());
        Assert.assertEquals("String matches", "Set turnout '' to state Thrown", action2.getLongDescription());
        
        action2 = new ActionTurnout("IQDA321", "My turnout");
        Assert.assertNotNull("object exists", action2);
        Assert.assertEquals("Username matches", "My turnout", action2.getUserName());
        Assert.assertEquals("String matches", "Set turnout '' to state Thrown", action2.getLongDescription());
        
        action2 = new ActionTurnout("IQDA321", null);
        action2.setTurnout(turnout);
        Assert.assertTrue("turnout is correct", turnout == action2.getTurnout().getBean());
        Assert.assertNotNull("object exists", action2);
        Assert.assertNull("Username matches", action2.getUserName());
        Assert.assertEquals("String matches", "Set turnout IT1 to state Thrown", action2.getLongDescription());
        
        Turnout l = InstanceManager.getDefault(TurnoutManager.class).provide("IT1");
        action2 = new ActionTurnout("IQDA321", "My turnout");
        action2.setTurnout(l);
        Assert.assertTrue("turnout is correct", l == action2.getTurnout().getBean());
        Assert.assertNotNull("object exists", action2);
        Assert.assertEquals("Username matches", "My turnout", action2.getUserName());
        Assert.assertEquals("String matches", "Set turnout IT1 to state Thrown", action2.getLongDescription());
        
        boolean thrown = false;
        try {
            // Illegal system name
            new ActionTurnout("IQA55:12:XY11", null);
        } catch (IllegalArgumentException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
        
        thrown = false;
        try {
            // Illegal system name
            new ActionTurnout("IQA55:12:XY11", "A name");
        } catch (IllegalArgumentException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
        
        // Test setup(). This method doesn't do anything, but execute it for coverage.
        _base.setup();
    }
    
    @Test
    public void testGetChild() {
        Assert.assertTrue("getChildCount() returns 0", 0 == actionTurnout.getChildCount());
        
        boolean hasThrown = false;
        try {
            actionTurnout.getChild(0);
        } catch (UnsupportedOperationException ex) {
            hasThrown = true;
            Assert.assertEquals("Error message is correct", "Not supported.", ex.getMessage());
        }
        Assert.assertTrue("Exception is thrown", hasThrown);
    }
    
    @Test
    public void testTurnoutState() {
        Assert.assertEquals("String matches", "Closed", ActionTurnout.TurnoutState.Closed.toString());
        Assert.assertEquals("String matches", "Thrown", ActionTurnout.TurnoutState.Thrown.toString());
        Assert.assertEquals("String matches", "Toggle", ActionTurnout.TurnoutState.Toggle.toString());
        
        Assert.assertTrue("objects are equal", ActionTurnout.TurnoutState.Closed == ActionTurnout.TurnoutState.get(Turnout.CLOSED));
        Assert.assertTrue("objects are equal", ActionTurnout.TurnoutState.Thrown == ActionTurnout.TurnoutState.get(Turnout.THROWN));
        Assert.assertTrue("objects are equal", ActionTurnout.TurnoutState.Toggle == ActionTurnout.TurnoutState.get(-1));
        
        boolean hasThrown = false;
        try {
            ActionTurnout.TurnoutState.get(100);    // The number 100 is a state that ActionTurnout.TurnoutState isn't aware of
        } catch (IllegalArgumentException ex) {
            hasThrown = true;
            Assert.assertTrue("Error message is correct", "invalid turnout state".equals(ex.getMessage()));
        }
        Assert.assertTrue("Exception is thrown", hasThrown);
    }
    
    @Test
    public void testSetTurnout() {
        Turnout turnout11 = InstanceManager.getDefault(TurnoutManager.class).provide("IL11");
        Turnout turnout12 = InstanceManager.getDefault(TurnoutManager.class).provide("IL12");
        NamedBeanHandle<Turnout> turnoutHandle12 = InstanceManager.getDefault(NamedBeanHandleManager.class).getNamedBeanHandle(turnout12.getDisplayName(), turnout12);
        Turnout turnout13 = InstanceManager.getDefault(TurnoutManager.class).provide("IL13");
        Turnout turnout14 = InstanceManager.getDefault(TurnoutManager.class).provide("IL14");
        turnout14.setUserName("Some user name");
        
        actionTurnout.removeTurnout();
        Assert.assertNull("turnout handle is null", actionTurnout.getTurnout());
        
        actionTurnout.setTurnout(turnout11);
        Assert.assertTrue("turnout is correct", turnout11 == actionTurnout.getTurnout().getBean());
        
        actionTurnout.removeTurnout();
        Assert.assertNull("turnout handle is null", actionTurnout.getTurnout());
        
        actionTurnout.setTurnout(turnoutHandle12);
        Assert.assertTrue("turnout handle is correct", turnoutHandle12 == actionTurnout.getTurnout());
        
        actionTurnout.setTurnout("A non existent turnout");
        Assert.assertNull("turnout handle is null", actionTurnout.getTurnout());
        JUnitAppender.assertErrorMessage("turnout \"A non existent turnout\" is not found");
        
        actionTurnout.setTurnout(turnout13.getSystemName());
        Assert.assertTrue("turnout is correct", turnout13 == actionTurnout.getTurnout().getBean());
        
        actionTurnout.setTurnout(turnout14.getUserName());
        Assert.assertTrue("turnout is correct", turnout14 == actionTurnout.getTurnout().getBean());
    }
    
    @Test
    public void testAction() throws SocketAlreadyConnectedException, JmriException {
        // Set the light
        turnout.setCommandedState(Turnout.CLOSED);
        // The turnout should be closed
        Assert.assertTrue("turnout is closed",turnout.getCommandedState() == Turnout.CLOSED);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the turnout should be thrown
        Assert.assertTrue("turnout is thrown",turnout.getCommandedState() == Turnout.THROWN);
        
        // Test to set turnout to closed
        actionTurnout.setBeanState(ActionTurnout.TurnoutState.Closed);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the turnout should be thrown
        Assert.assertTrue("turnout is thrown",turnout.getCommandedState() == Turnout.CLOSED);
        
        // Test to set turnout to toggle
        actionTurnout.setBeanState(ActionTurnout.TurnoutState.Toggle);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the turnout should be thrown
        Assert.assertTrue("turnout is thrown",turnout.getCommandedState() == Turnout.THROWN);
        
        // Test to set turnout to toggle
        actionTurnout.setBeanState(ActionTurnout.TurnoutState.Toggle);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the turnout should be thrown
        Assert.assertTrue("turnout is thrown",turnout.getCommandedState() == Turnout.CLOSED);
    }
    
    @Test
    public void testIndirectAddressing() throws JmriException {
        
        Memory m1 = InstanceManager.getDefault(MemoryManager.class).provide("IM1");
        m1.setValue("IT102");
        
        Assert.assertTrue(conditionalNG.isActive());
        Turnout t1 = InstanceManager.getDefault(TurnoutManager.class).provide("IT101");
        Turnout t2 = InstanceManager.getDefault(TurnoutManager.class).provide("IT102");
        Turnout t3 = InstanceManager.getDefault(TurnoutManager.class).provide("IT103");
        Turnout t4 = InstanceManager.getDefault(TurnoutManager.class).provide("IT104");
        Turnout t5 = InstanceManager.getDefault(TurnoutManager.class).provide("IT105");
        
        actionTurnout.setBeanState(ActionTurnout.TurnoutState.Thrown);
        actionTurnout.setTurnout(t1.getSystemName());
        actionTurnout.setReference("{IM1}");    // Points to "IT102"
        actionTurnout.setLocalVariable("myTurnout");
        actionTurnout.setFormula("\"IT10\" + str(index)");
        _baseMaleSocket.addLocalVariable("refTurnout", SymbolTable.InitialValueType.String, "IT103");
        _baseMaleSocket.addLocalVariable("myTurnout", SymbolTable.InitialValueType.String, "IT104");
        _baseMaleSocket.addLocalVariable("index", SymbolTable.InitialValueType.Integer, "5");
        
        // Test direct addressing
        actionTurnout.setAddressing(NamedBeanAddressing.Direct);
        t1.setState(Turnout.CLOSED);
        t2.setState(Turnout.CLOSED);
        t3.setState(Turnout.CLOSED);
        t4.setState(Turnout.CLOSED);
        t5.setState(Turnout.CLOSED);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the correct turnout should be thrown
        Assert.assertEquals(Turnout.THROWN, t1.getCommandedState());
        Assert.assertEquals(Turnout.CLOSED, t2.getCommandedState());
        Assert.assertEquals(Turnout.CLOSED, t3.getCommandedState());
        Assert.assertEquals(Turnout.CLOSED, t4.getCommandedState());
        Assert.assertEquals(Turnout.CLOSED, t5.getCommandedState());
        
        // Test reference by memory addressing
        actionTurnout.setAddressing(NamedBeanAddressing.Reference);
        t1.setState(Turnout.CLOSED);
        t2.setState(Turnout.CLOSED);
        t3.setState(Turnout.CLOSED);
        t4.setState(Turnout.CLOSED);
        t5.setState(Turnout.CLOSED);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the correct turnout should be thrown
        Assert.assertEquals(Turnout.CLOSED, t1.getCommandedState());
        Assert.assertEquals(Turnout.THROWN, t2.getCommandedState());
        Assert.assertEquals(Turnout.CLOSED, t3.getCommandedState());
        Assert.assertEquals(Turnout.CLOSED, t4.getCommandedState());
        Assert.assertEquals(Turnout.CLOSED, t5.getCommandedState());
        
        // Test reference by local variable addressing
        actionTurnout.setReference("{refTurnout}");    // Points to "IT103"
        actionTurnout.setAddressing(NamedBeanAddressing.Reference);
        t1.setState(Turnout.CLOSED);
        t2.setState(Turnout.CLOSED);
        t3.setState(Turnout.CLOSED);
        t4.setState(Turnout.CLOSED);
        t5.setState(Turnout.CLOSED);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the correct turnout should be thrown
        Assert.assertEquals(Turnout.CLOSED, t1.getCommandedState());
        Assert.assertEquals(Turnout.CLOSED, t2.getCommandedState());
        Assert.assertEquals(Turnout.THROWN, t3.getCommandedState());
        Assert.assertEquals(Turnout.CLOSED, t4.getCommandedState());
        Assert.assertEquals(Turnout.CLOSED, t5.getCommandedState());
        
        // Test local variable addressing
        actionTurnout.setAddressing(NamedBeanAddressing.LocalVariable);
        t1.setState(Turnout.CLOSED);
        t2.setState(Turnout.CLOSED);
        t3.setState(Turnout.CLOSED);
        t4.setState(Turnout.CLOSED);
        t5.setState(Turnout.CLOSED);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the correct turnout should be thrown
        Assert.assertEquals(Turnout.CLOSED, t1.getCommandedState());
        Assert.assertEquals(Turnout.CLOSED, t2.getCommandedState());
        Assert.assertEquals(Turnout.CLOSED, t3.getCommandedState());
        Assert.assertEquals(Turnout.THROWN, t4.getCommandedState());
        Assert.assertEquals(Turnout.CLOSED, t5.getCommandedState());
        
        // Test formula addressing
        actionTurnout.setAddressing(NamedBeanAddressing.Formula);
        t1.setState(Turnout.CLOSED);
        t2.setState(Turnout.CLOSED);
        t3.setState(Turnout.CLOSED);
        t4.setState(Turnout.CLOSED);
        t5.setState(Turnout.CLOSED);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the correct turnout should be thrown
        Assert.assertEquals(Turnout.CLOSED, t1.getCommandedState());
        Assert.assertEquals(Turnout.CLOSED, t2.getCommandedState());
        Assert.assertEquals(Turnout.CLOSED, t3.getCommandedState());
        Assert.assertEquals(Turnout.CLOSED, t4.getCommandedState());
        Assert.assertEquals(Turnout.THROWN, t5.getCommandedState());
    }
    
    @Test
    public void testIndirectStateAddressing() throws JmriException {
        
        Memory m1 = InstanceManager.getDefault(MemoryManager.class).provide("IM1");
        m1.setValue("IT102");
        
        Assert.assertTrue(conditionalNG.isActive());
        
        
        // Test direct addressing
        actionTurnout.setStateAddressing(NamedBeanAddressing.Direct);
        // Test Closed
        turnout.setState(Turnout.THROWN);
        actionTurnout.setBeanState(ActionTurnout.TurnoutState.Closed);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the correct turnout should be thrown
        Assert.assertEquals(Turnout.CLOSED, turnout.getCommandedState());
        // Test Thrown
        turnout.setState(Turnout.CLOSED);
        actionTurnout.setBeanState(ActionTurnout.TurnoutState.Thrown);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the correct turnout should be thrown
        Assert.assertEquals(Turnout.THROWN, turnout.getCommandedState());
        
        
        // Test reference by memory addressing
        actionTurnout.setStateAddressing(NamedBeanAddressing.Reference);
        actionTurnout.setStateReference("{IM1}");
        // Test Closed
        m1.setValue("Closed");
        turnout.setState(Turnout.THROWN);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the correct turnout should be thrown
        Assert.assertEquals(Turnout.CLOSED, turnout.getCommandedState());
        // Test Thrown
        m1.setValue("Thrown");
        turnout.setState(Turnout.CLOSED);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the correct turnout should be thrown
        Assert.assertEquals(Turnout.THROWN, turnout.getCommandedState());
        
        
        // Test reference by local variable addressing
        actionTurnout.setStateAddressing(NamedBeanAddressing.Reference);
        actionTurnout.setStateReference("{refVariable}");
        // Test Closed
        _baseMaleSocket.clearLocalVariables();
        _baseMaleSocket.addLocalVariable("refVariable", SymbolTable.InitialValueType.String, "Closed");
        turnout.setState(Turnout.THROWN);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the correct turnout should be thrown
        Assert.assertEquals(Turnout.CLOSED, turnout.getCommandedState());
        // Test Thrown
        _baseMaleSocket.clearLocalVariables();
        _baseMaleSocket.addLocalVariable("refVariable", SymbolTable.InitialValueType.String, "Thrown");
        turnout.setState(Turnout.CLOSED);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the correct turnout should be thrown
        Assert.assertEquals(Turnout.THROWN, turnout.getCommandedState());
        
        
        // Test local variable addressing
        actionTurnout.setStateAddressing(NamedBeanAddressing.Reference);
        actionTurnout.setStateLocalVariable("myVariable");
        // Test Closed
        _baseMaleSocket.clearLocalVariables();
        _baseMaleSocket.addLocalVariable("refVariable", SymbolTable.InitialValueType.String, "Closed");
        turnout.setState(Turnout.THROWN);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the correct turnout should be thrown
        Assert.assertEquals(Turnout.CLOSED, turnout.getCommandedState());
        // Test Thrown
        _baseMaleSocket.clearLocalVariables();
        _baseMaleSocket.addLocalVariable("refVariable", SymbolTable.InitialValueType.String, "Thrown");
        turnout.setState(Turnout.CLOSED);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the correct turnout should be thrown
        Assert.assertEquals(Turnout.THROWN, turnout.getCommandedState());
        
        
        // Test formula addressing
        actionTurnout.setStateAddressing(NamedBeanAddressing.Formula);
        actionTurnout.setStateFormula("refVariable + myVariable");
        // Test Closed
        _baseMaleSocket.clearLocalVariables();
        _baseMaleSocket.addLocalVariable("refVariable", SymbolTable.InitialValueType.String, "Clo");
        _baseMaleSocket.addLocalVariable("myVariable", SymbolTable.InitialValueType.String, "sed");
        turnout.setState(Turnout.THROWN);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the correct turnout should be thrown
        Assert.assertEquals(Turnout.CLOSED, turnout.getCommandedState());
        // Test Thrown
        _baseMaleSocket.clearLocalVariables();
        _baseMaleSocket.addLocalVariable("refVariable", SymbolTable.InitialValueType.String, "Thro");
        _baseMaleSocket.addLocalVariable("myVariable", SymbolTable.InitialValueType.String, "wn");
        turnout.setState(Turnout.CLOSED);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the correct turnout should be thrown
        Assert.assertEquals(Turnout.THROWN, turnout.getCommandedState());
    }
    
    @Test
    public void testVetoableChange() throws PropertyVetoException {
        // Get the action and set the turnout
        Turnout turnout = InstanceManager.getDefault(TurnoutManager.class).provide("IT1");
        Assert.assertNotNull("Turnout is not null", turnout);
        ActionTurnout action = new ActionTurnout(InstanceManager.getDefault(DigitalActionManager.class).getAutoSystemName(), null);
        action.setTurnout(turnout);
        
        // Get some other turnout for later use
        Turnout otherTurnout = InstanceManager.getDefault(TurnoutManager.class).provide("IM99");
        Assert.assertNotNull("Turnout is not null", otherTurnout);
        Assert.assertNotEquals("Turnout is not equal", turnout, otherTurnout);
        
        // Test vetoableChange() for some other propery
        action.vetoableChange(new PropertyChangeEvent(this, "CanSomething", "test", null));
        Assert.assertEquals("Turnout matches", turnout, action.getTurnout().getBean());
        
        // Test vetoableChange() for a string
        action.vetoableChange(new PropertyChangeEvent(this, "CanDelete", "test", null));
        Assert.assertEquals("Turnout matches", turnout, action.getTurnout().getBean());
        action.vetoableChange(new PropertyChangeEvent(this, "DoDelete", "test", null));
        Assert.assertEquals("Turnout matches", turnout, action.getTurnout().getBean());
        
        // Test vetoableChange() for another turnout
        action.vetoableChange(new PropertyChangeEvent(this, "CanDelete", otherTurnout, null));
        Assert.assertEquals("Turnout matches", turnout, action.getTurnout().getBean());
        action.vetoableChange(new PropertyChangeEvent(this, "DoDelete", otherTurnout, null));
        Assert.assertEquals("Turnout matches", turnout, action.getTurnout().getBean());
        
        // Test vetoableChange() for its own turnout
        boolean thrown = false;
        try {
            action.vetoableChange(new PropertyChangeEvent(this, "CanDelete", turnout, null));
        } catch (PropertyVetoException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
        
        Assert.assertEquals("Turnout matches", turnout, action.getTurnout().getBean());
        action.vetoableChange(new PropertyChangeEvent(this, "DoDelete", turnout, null));
        Assert.assertNull("Turnout is null", action.getTurnout());
    }
    
    @Test
    public void testCategory() {
        Assert.assertTrue("Category matches", Category.ITEM == _base.getCategory());
    }
    
    @Test
    public void testShortDescription() {
        Assert.assertEquals("String matches", "Turnout", _base.getShortDescription());
    }
    
    @Test
    public void testLongDescription() {
        Assert.assertEquals("String matches", "Set turnout IT1 to state Thrown", _base.getLongDescription());
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
        
        turnout = InstanceManager.getDefault(TurnoutManager.class).provide("IT1");
        turnout.setCommandedState(Turnout.CLOSED);
        logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A logixNG");
        conditionalNG = new DefaultConditionalNGScaffold("IQC1", "A conditionalNG");  // NOI18N;
        InstanceManager.getDefault(ConditionalNG_Manager.class).register(conditionalNG);
        logixNG.addConditionalNG(conditionalNG);
        conditionalNG.setRunDelayed(false);
        conditionalNG.setEnabled(true);
        actionTurnout = new ActionTurnout(InstanceManager.getDefault(DigitalActionManager.class).getAutoSystemName(), null);
        actionTurnout.setTurnout(turnout);
        actionTurnout.setBeanState(ActionTurnout.TurnoutState.Thrown);
        MaleSocket socket = InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionTurnout);
        conditionalNG.getChild(0).connect(socket);
        
        _base = actionTurnout;
        _baseMaleSocket = socket;
        
        if (! logixNG.setParentForAllChildren(new ArrayList<>())) throw new RuntimeException();
        logixNG.setEnabled(true);
    }

    @After
    public void tearDown() {
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.tearDown();
    }
    
}
