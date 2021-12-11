package jmri.jmrit.logixng.actions;

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

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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
        Assert.assertTrue("object exists", _base != null);
        
        ActionTurnoutLock action2;
        Assert.assertNotNull("turnout is not null", turnout);
        turnout.setState(Turnout.ON);
        
        action2 = new ActionTurnoutLock("IQDA321", null);
        Assert.assertNotNull("object exists", action2);
        Assert.assertNull("Username matches", action2.getUserName());
        Assert.assertEquals("String matches", "Set lock for turnout '' to Unlock", action2.getLongDescription());
        
        action2 = new ActionTurnoutLock("IQDA321", "My turnout");
        Assert.assertNotNull("object exists", action2);
        Assert.assertEquals("Username matches", "My turnout", action2.getUserName());
        Assert.assertEquals("String matches", "Set lock for turnout '' to Unlock", action2.getLongDescription());
        
        action2 = new ActionTurnoutLock("IQDA321", null);
        action2.setTurnout(turnout);
        Assert.assertTrue("turnout is correct", turnout == action2.getTurnout().getBean());
        Assert.assertNotNull("object exists", action2);
        Assert.assertNull("Username matches", action2.getUserName());
        Assert.assertEquals("String matches", "Set lock for turnout IT1 to Unlock", action2.getLongDescription());
        
        Turnout l = InstanceManager.getDefault(TurnoutManager.class).provide("IT1");
        action2 = new ActionTurnoutLock("IQDA321", "My turnout");
        action2.setTurnout(l);
        Assert.assertTrue("turnout is correct", l == action2.getTurnout().getBean());
        Assert.assertNotNull("object exists", action2);
        Assert.assertEquals("Username matches", "My turnout", action2.getUserName());
        Assert.assertEquals("String matches", "Set lock for turnout IT1 to Unlock", action2.getLongDescription());
        
        boolean thrown = false;
        try {
            // Illegal system name
            new ActionTurnoutLock("IQA55:12:XY11", null);
        } catch (IllegalArgumentException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
        
        thrown = false;
        try {
            // Illegal system name
            new ActionTurnoutLock("IQA55:12:XY11", "A name");
        } catch (IllegalArgumentException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
        
        // Test setup(). This method doesn't do anything, but execute it for coverage.
        _base.setup();
    }
    
    @Test
    public void testGetChild() {
        Assert.assertTrue("getChildCount() returns 0", 0 == actionTurnoutLock.getChildCount());
        
        boolean hasLock = false;
        try {
            actionTurnoutLock.getChild(0);
        } catch (UnsupportedOperationException ex) {
            hasLock = true;
            Assert.assertEquals("Error message is correct", "Not supported.", ex.getMessage());
        }
        Assert.assertTrue("Exception is thrown", hasLock);
    }
    
    @Test
    public void testTurnoutLock() {
        Assert.assertEquals("String matches", "Unlock", ActionTurnoutLock.TurnoutLock.Unlock.toString());
        Assert.assertEquals("String matches", "Lock", ActionTurnoutLock.TurnoutLock.Lock.toString());
        Assert.assertEquals("String matches", "Toggle", ActionTurnoutLock.TurnoutLock.Toggle.toString());
    }
    
    @Test
    public void testSetTurnout() {
        Turnout turnout11 = InstanceManager.getDefault(TurnoutManager.class).provide("IL11");
        Turnout turnout12 = InstanceManager.getDefault(TurnoutManager.class).provide("IL12");
        NamedBeanHandle<Turnout> turnoutHandle12 = InstanceManager.getDefault(NamedBeanHandleManager.class).getNamedBeanHandle(turnout12.getDisplayName(), turnout12);
        Turnout turnout13 = InstanceManager.getDefault(TurnoutManager.class).provide("IL13");
        Turnout turnout14 = InstanceManager.getDefault(TurnoutManager.class).provide("IL14");
        turnout14.setUserName("Some user name");
        
        actionTurnoutLock.removeTurnout();
        Assert.assertNull("turnout handle is null", actionTurnoutLock.getTurnout());
        
        actionTurnoutLock.setTurnout(turnout11);
        Assert.assertTrue("turnout is correct", turnout11 == actionTurnoutLock.getTurnout().getBean());
        
        actionTurnoutLock.removeTurnout();
        Assert.assertNull("turnout handle is null", actionTurnoutLock.getTurnout());
        
        actionTurnoutLock.setTurnout(turnoutHandle12);
        Assert.assertTrue("turnout handle is correct", turnoutHandle12 == actionTurnoutLock.getTurnout());
        
        actionTurnoutLock.setTurnout("A non existent turnout");
        Assert.assertNull("turnout handle is null", actionTurnoutLock.getTurnout());
        JUnitAppender.assertErrorMessage("turnout \"A non existent turnout\" is not found");
        
        actionTurnoutLock.setTurnout(turnout13.getSystemName());
        Assert.assertTrue("turnout is correct", turnout13 == actionTurnoutLock.getTurnout().getBean());
        
        actionTurnoutLock.setTurnout(turnout14.getUserName());
        Assert.assertTrue("turnout is correct", turnout14 == actionTurnoutLock.getTurnout().getBean());
    }
    
    @Test
    public void testAction() throws SocketAlreadyConnectedException, JmriException {
        // Set the light
        turnout.setLocked(Turnout.CABLOCKOUT + Turnout.PUSHBUTTONLOCKOUT, false);
        // The turnout should be closed
        Assert.assertTrue(!turnout.getLocked(Turnout.CABLOCKOUT));
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the turnout should be thrown
        Assert.assertTrue(turnout.getLocked(Turnout.CABLOCKOUT));
        
        // Test to set turnout to closed
        actionTurnoutLock.setTurnoutLock(ActionTurnoutLock.TurnoutLock.Unlock);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the turnout should be thrown
        Assert.assertTrue(!turnout.getLocked(Turnout.CABLOCKOUT));
        
        // Test to set turnout to toggle
        actionTurnoutLock.setTurnoutLock(ActionTurnoutLock.TurnoutLock.Toggle);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the turnout should be thrown
        Assert.assertTrue(turnout.getLocked(Turnout.CABLOCKOUT));
        
        // Test to set turnout to toggle
        actionTurnoutLock.setTurnoutLock(ActionTurnoutLock.TurnoutLock.Toggle);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the turnout should be thrown
        Assert.assertTrue(!turnout.getLocked(Turnout.CABLOCKOUT));
    }
    
    @Test
    public void testIndirectAddressing() throws JmriException {
        
        Memory m1 = InstanceManager.getDefault(MemoryManager.class).provide("IM1");
        m1.setValue("IT102");
        
        Assert.assertTrue(conditionalNG.isActive());
        Turnout t1 = new MyTurnout("IT101"); InstanceManager.getDefault(TurnoutManager.class).register(t1);
        Turnout t2 = new MyTurnout("IT102"); InstanceManager.getDefault(TurnoutManager.class).register(t2);
        Turnout t3 = new MyTurnout("IT103"); InstanceManager.getDefault(TurnoutManager.class).register(t3);
        Turnout t4 = new MyTurnout("IT104"); InstanceManager.getDefault(TurnoutManager.class).register(t4);
        Turnout t5 = new MyTurnout("IT105"); InstanceManager.getDefault(TurnoutManager.class).register(t5);
        
        actionTurnoutLock.setTurnoutLock(ActionTurnoutLock.TurnoutLock.Lock);
        actionTurnoutLock.setTurnout(t1.getSystemName());
        actionTurnoutLock.setReference("{IM1}");    // Points to "IT102"
        actionTurnoutLock.setLocalVariable("myTurnout");
        actionTurnoutLock.setFormula("\"IT10\" + str(index)");
        _baseMaleSocket.addLocalVariable("refTurnout", SymbolTable.InitialValueType.String, "IT103");
        _baseMaleSocket.addLocalVariable("myTurnout", SymbolTable.InitialValueType.String, "IT104");
        _baseMaleSocket.addLocalVariable("index", SymbolTable.InitialValueType.Integer, "5");
        
        // Test direct addressing
        actionTurnoutLock.setAddressing(NamedBeanAddressing.Direct);
        t1.setLocked(Turnout.CABLOCKOUT + Turnout.PUSHBUTTONLOCKOUT, false);
        t2.setLocked(Turnout.CABLOCKOUT + Turnout.PUSHBUTTONLOCKOUT, false);
        t3.setLocked(Turnout.CABLOCKOUT + Turnout.PUSHBUTTONLOCKOUT, false);
        t4.setLocked(Turnout.CABLOCKOUT + Turnout.PUSHBUTTONLOCKOUT, false);
        t5.setLocked(Turnout.CABLOCKOUT + Turnout.PUSHBUTTONLOCKOUT, false);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the correct turnout should be thrown
        Assert.assertTrue(t1.getLocked(Turnout.CABLOCKOUT));
        Assert.assertTrue(!t2.getLocked(Turnout.CABLOCKOUT));
        Assert.assertTrue(!t3.getLocked(Turnout.CABLOCKOUT));
        Assert.assertTrue(!t4.getLocked(Turnout.CABLOCKOUT));
        Assert.assertTrue(!t5.getLocked(Turnout.CABLOCKOUT));
        
        // Test reference by memory addressing
        actionTurnoutLock.setAddressing(NamedBeanAddressing.Reference);
        t1.setLocked(Turnout.CABLOCKOUT + Turnout.PUSHBUTTONLOCKOUT, false);
        t2.setLocked(Turnout.CABLOCKOUT + Turnout.PUSHBUTTONLOCKOUT, false);
        t3.setLocked(Turnout.CABLOCKOUT + Turnout.PUSHBUTTONLOCKOUT, false);
        t4.setLocked(Turnout.CABLOCKOUT + Turnout.PUSHBUTTONLOCKOUT, false);
        t5.setLocked(Turnout.CABLOCKOUT + Turnout.PUSHBUTTONLOCKOUT, false);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the correct turnout should be thrown
        Assert.assertTrue(!t1.getLocked(Turnout.CABLOCKOUT));
        Assert.assertTrue(t2.getLocked(Turnout.CABLOCKOUT));
        Assert.assertTrue(!t3.getLocked(Turnout.CABLOCKOUT));
        Assert.assertTrue(!t4.getLocked(Turnout.CABLOCKOUT));
        Assert.assertTrue(!t5.getLocked(Turnout.CABLOCKOUT));
        
        // Test reference by local variable addressing
        actionTurnoutLock.setReference("{refTurnout}");    // Points to "IT103"
        actionTurnoutLock.setAddressing(NamedBeanAddressing.Reference);
        t1.setLocked(Turnout.CABLOCKOUT + Turnout.PUSHBUTTONLOCKOUT, false);
        t2.setLocked(Turnout.CABLOCKOUT + Turnout.PUSHBUTTONLOCKOUT, false);
        t3.setLocked(Turnout.CABLOCKOUT + Turnout.PUSHBUTTONLOCKOUT, false);
        t4.setLocked(Turnout.CABLOCKOUT + Turnout.PUSHBUTTONLOCKOUT, false);
        t5.setLocked(Turnout.CABLOCKOUT + Turnout.PUSHBUTTONLOCKOUT, false);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the correct turnout should be thrown
        Assert.assertTrue(!t1.getLocked(Turnout.CABLOCKOUT));
        Assert.assertTrue(!t2.getLocked(Turnout.CABLOCKOUT));
        Assert.assertTrue(t3.getLocked(Turnout.CABLOCKOUT));
        Assert.assertTrue(!t4.getLocked(Turnout.CABLOCKOUT));
        Assert.assertTrue(!t5.getLocked(Turnout.CABLOCKOUT));
        
        // Test local variable addressing
        actionTurnoutLock.setAddressing(NamedBeanAddressing.LocalVariable);
        t1.setLocked(Turnout.CABLOCKOUT + Turnout.PUSHBUTTONLOCKOUT, false);
        t2.setLocked(Turnout.CABLOCKOUT + Turnout.PUSHBUTTONLOCKOUT, false);
        t3.setLocked(Turnout.CABLOCKOUT + Turnout.PUSHBUTTONLOCKOUT, false);
        t4.setLocked(Turnout.CABLOCKOUT + Turnout.PUSHBUTTONLOCKOUT, false);
        t5.setLocked(Turnout.CABLOCKOUT + Turnout.PUSHBUTTONLOCKOUT, false);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the correct turnout should be thrown
        Assert.assertTrue(!t1.getLocked(Turnout.CABLOCKOUT));
        Assert.assertTrue(!t2.getLocked(Turnout.CABLOCKOUT));
        Assert.assertTrue(!t3.getLocked(Turnout.CABLOCKOUT));
        Assert.assertTrue(t4.getLocked(Turnout.CABLOCKOUT));
        Assert.assertTrue(!t5.getLocked(Turnout.CABLOCKOUT));
        
        // Test formula addressing
        actionTurnoutLock.setAddressing(NamedBeanAddressing.Formula);
        t1.setLocked(Turnout.CABLOCKOUT + Turnout.PUSHBUTTONLOCKOUT, false);
        t2.setLocked(Turnout.CABLOCKOUT + Turnout.PUSHBUTTONLOCKOUT, false);
        t3.setLocked(Turnout.CABLOCKOUT + Turnout.PUSHBUTTONLOCKOUT, false);
        t4.setLocked(Turnout.CABLOCKOUT + Turnout.PUSHBUTTONLOCKOUT, false);
        t5.setLocked(Turnout.CABLOCKOUT + Turnout.PUSHBUTTONLOCKOUT, false);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the correct turnout should be thrown
        Assert.assertTrue(!t1.getLocked(Turnout.CABLOCKOUT));
        Assert.assertTrue(!t2.getLocked(Turnout.CABLOCKOUT));
        Assert.assertTrue(!t3.getLocked(Turnout.CABLOCKOUT));
        Assert.assertTrue(!t4.getLocked(Turnout.CABLOCKOUT));
        Assert.assertTrue(t5.getLocked(Turnout.CABLOCKOUT));
    }
    
    @Test
    public void testIndirectStateAddressing() throws JmriException {
        
        Memory m1 = InstanceManager.getDefault(MemoryManager.class).provide("IM1");
        m1.setValue("IT102");
        
        Assert.assertTrue(conditionalNG.isActive());
        
        
        // Test direct addressing
        actionTurnoutLock.setLockAddressing(NamedBeanAddressing.Direct);
        // Test Unlock
        turnout.setLocked(Turnout.CABLOCKOUT + Turnout.PUSHBUTTONLOCKOUT, true);
        actionTurnoutLock.setTurnoutLock(ActionTurnoutLock.TurnoutLock.Unlock);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the correct turnout should be thrown
        Assert.assertFalse(turnout.getLocked(Turnout.CABLOCKOUT));
        // Test Lock
        turnout.setLocked(Turnout.CABLOCKOUT + Turnout.PUSHBUTTONLOCKOUT, false);
        actionTurnoutLock.setTurnoutLock(ActionTurnoutLock.TurnoutLock.Lock);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the correct turnout should be thrown
        Assert.assertTrue(turnout.getLocked(Turnout.CABLOCKOUT));
        
        // Test reference by memory addressing
        actionTurnoutLock.setLockAddressing(NamedBeanAddressing.Reference);
        actionTurnoutLock.setLockReference("{IM1}");
        // Test Unlock
        m1.setValue("Unlock");
        turnout.setLocked(Turnout.CABLOCKOUT + Turnout.PUSHBUTTONLOCKOUT, true);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the correct turnout should be thrown
        Assert.assertFalse(turnout.getLocked(Turnout.CABLOCKOUT));
        // Test Lock
        m1.setValue("Lock");
        turnout.setLocked(Turnout.CABLOCKOUT + Turnout.PUSHBUTTONLOCKOUT, false);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the correct turnout should be thrown
        Assert.assertTrue(turnout.getLocked(Turnout.CABLOCKOUT));
        
        
        // Test reference by local variable addressing
        actionTurnoutLock.setLockAddressing(NamedBeanAddressing.Reference);
        actionTurnoutLock.setLockReference("{refVariable}");
        // Test Unlock
        _baseMaleSocket.clearLocalVariables();
        _baseMaleSocket.addLocalVariable("refVariable", SymbolTable.InitialValueType.String, "Unlock");
        turnout.setLocked(Turnout.CABLOCKOUT + Turnout.PUSHBUTTONLOCKOUT, true);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the correct turnout should be thrown
        Assert.assertFalse(turnout.getLocked(Turnout.CABLOCKOUT));
        // Test Lock
        _baseMaleSocket.clearLocalVariables();
        _baseMaleSocket.addLocalVariable("refVariable", SymbolTable.InitialValueType.String, "Lock");
        turnout.setLocked(Turnout.CABLOCKOUT + Turnout.PUSHBUTTONLOCKOUT, false);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the correct turnout should be thrown
        Assert.assertTrue(turnout.getLocked(Turnout.CABLOCKOUT));
        
        
        // Test local variable addressing
        actionTurnoutLock.setLockAddressing(NamedBeanAddressing.Reference);
        actionTurnoutLock.setLockLocalVariable("myVariable");
        // Test Unlock
        _baseMaleSocket.clearLocalVariables();
        _baseMaleSocket.addLocalVariable("refVariable", SymbolTable.InitialValueType.String, "Unlock");
        turnout.setLocked(Turnout.CABLOCKOUT + Turnout.PUSHBUTTONLOCKOUT, true);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the correct turnout should be thrown
        Assert.assertFalse(turnout.getLocked(Turnout.CABLOCKOUT));
        // Test Lock
        _baseMaleSocket.clearLocalVariables();
        _baseMaleSocket.addLocalVariable("refVariable", SymbolTable.InitialValueType.String, "Lock");
        turnout.setLocked(Turnout.CABLOCKOUT + Turnout.PUSHBUTTONLOCKOUT, false);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the correct turnout should be thrown
        Assert.assertTrue(turnout.getLocked(Turnout.CABLOCKOUT));
        
        
        // Test formula addressing
        actionTurnoutLock.setLockAddressing(NamedBeanAddressing.Formula);
        actionTurnoutLock.setLockFormula("refVariable + myVariable");
        // Test Unlock
        _baseMaleSocket.clearLocalVariables();
        _baseMaleSocket.addLocalVariable("refVariable", SymbolTable.InitialValueType.String, "Unlo");
        _baseMaleSocket.addLocalVariable("myVariable", SymbolTable.InitialValueType.String, "ck");
        turnout.setLocked(Turnout.CABLOCKOUT + Turnout.PUSHBUTTONLOCKOUT, true);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the correct turnout should be thrown
        Assert.assertFalse(turnout.getLocked(Turnout.CABLOCKOUT));
        // Test Lock
        _baseMaleSocket.clearLocalVariables();
        _baseMaleSocket.addLocalVariable("refVariable", SymbolTable.InitialValueType.String, "Lo");
        _baseMaleSocket.addLocalVariable("myVariable", SymbolTable.InitialValueType.String, "ck");
        turnout.setLocked(Turnout.CABLOCKOUT + Turnout.PUSHBUTTONLOCKOUT, false);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the correct turnout should be thrown
        Assert.assertTrue(turnout.getLocked(Turnout.CABLOCKOUT));
    }
    
    @Test
    public void testVetoableChange() throws PropertyVetoException {
        // Get the action and set the turnout
        Turnout turnout = InstanceManager.getDefault(TurnoutManager.class).provide("IT1");
        Assert.assertNotNull("Turnout is not null", turnout);
        ActionTurnoutLock action = new ActionTurnoutLock(InstanceManager.getDefault(DigitalActionManager.class).getAutoSystemName(), null);
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
        Assert.assertEquals("String matches", "Turnout, lock", _base.getShortDescription());
    }
    
    @Test
    public void testLongDescription() {
        Assert.assertEquals("String matches", "Set lock for turnout IT1 to Lock", _base.getLongDescription());
    }
    
    @Test
    public void testChild() {
        Assert.assertTrue("Num children is zero", 0 == _base.getChildCount());
        boolean hasLock = false;
        try {
            _base.getChild(0);
        } catch (UnsupportedOperationException ex) {
            hasLock = true;
            Assert.assertTrue("Error message is correct", "Not supported.".equals(ex.getMessage()));
        }
        Assert.assertTrue("Exception is thrown", hasLock);
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
        actionTurnoutLock.setTurnout(turnout);
        actionTurnoutLock.setTurnoutLock(ActionTurnoutLock.TurnoutLock.Lock);
        MaleSocket socket = InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionTurnoutLock);
        conditionalNG.getChild(0).connect(socket);
        
        _base = actionTurnoutLock;
        _baseMaleSocket = socket;
        
        if (! logixNG.setParentForAllChildren(new ArrayList<>())) throw new RuntimeException();
        logixNG.setEnabled(true);
    }

    @After
    public void tearDown() {
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
