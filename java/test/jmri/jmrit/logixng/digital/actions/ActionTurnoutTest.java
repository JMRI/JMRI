package jmri.jmrit.logixng.digital.actions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.NamedBean;
import jmri.NamedBeanHandle;
import jmri.NamedBeanHandleManager;
import jmri.Turnout;
import jmri.TurnoutManager;
import jmri.jmrit.logixng.Category;
import jmri.jmrit.logixng.ConditionalNG;
import jmri.jmrit.logixng.ConditionalNG_Manager;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.LogixNG;
import jmri.jmrit.logixng.LogixNG_Manager;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.SocketAlreadyConnectedException;
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
        return String.format("Set turnout IT1 to Thrown%n");
    }
    
    @Override
    public String getExpectedPrintedTreeFromRoot() {
        return String.format(
                "LogixNG: A logixNG%n" +
                "   ConditionalNG: A conditionalNG%n" +
                "      ! %n" +
                "         Set turnout IT1 to Thrown%n");
    }
    
    @Override
    public NamedBean createNewBean(String systemName) {
        return new ActionTurnout(systemName, null);
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
        Assert.assertEquals("String matches", "Set turnout '' to Thrown", action2.getLongDescription());
        
        action2 = new ActionTurnout("IQDA321", "My turnout");
        Assert.assertNotNull("object exists", action2);
        Assert.assertEquals("Username matches", "My turnout", action2.getUserName());
        Assert.assertEquals("String matches", "Set turnout '' to Thrown", action2.getLongDescription());
        
        action2 = new ActionTurnout("IQDA321", null);
        action2.setTurnout(turnout);
        Assert.assertTrue("turnout is correct", turnout == action2.getTurnout().getBean());
        Assert.assertNotNull("object exists", action2);
        Assert.assertNull("Username matches", action2.getUserName());
        Assert.assertEquals("String matches", "Set turnout IT1 to Thrown", action2.getLongDescription());
        
        Turnout l = InstanceManager.getDefault(TurnoutManager.class).provide("IT1");
        action2 = new ActionTurnout("IQDA321", "My turnout");
        action2.setTurnout(l);
        Assert.assertTrue("turnout is correct", l == action2.getTurnout().getBean());
        Assert.assertNotNull("object exists", action2);
        Assert.assertEquals("Username matches", "My turnout", action2.getUserName());
        Assert.assertEquals("String matches", "Set turnout IT1 to Thrown", action2.getLongDescription());
        
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
        Assert.assertEquals("String matches", "Closed", ActionTurnout.TurnoutState.CLOSED.toString());
        Assert.assertEquals("String matches", "Thrown", ActionTurnout.TurnoutState.THROWN.toString());
        Assert.assertEquals("String matches", "Toggle", ActionTurnout.TurnoutState.TOGGLE.toString());
        
        Assert.assertTrue("objects are equal", ActionTurnout.TurnoutState.CLOSED == ActionTurnout.TurnoutState.get(Turnout.CLOSED));
        Assert.assertTrue("objects are equal", ActionTurnout.TurnoutState.THROWN == ActionTurnout.TurnoutState.get(Turnout.THROWN));
        Assert.assertTrue("objects are equal", ActionTurnout.TurnoutState.TOGGLE == ActionTurnout.TurnoutState.get(-1));
        
        boolean hasThrown = false;
        try {
            ActionTurnout.TurnoutState.get(Turnout.UNKNOWN);
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
        
        actionTurnout.setTurnout((Turnout)null);
        Assert.assertNull("turnout handle is null", actionTurnout.getTurnout());
        
        actionTurnout.setTurnout(turnout11);
        Assert.assertTrue("turnout is correct", turnout11 == actionTurnout.getTurnout().getBean());
        
        actionTurnout.setTurnout((Turnout)null);
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
        actionTurnout.setTurnoutState(ActionTurnout.TurnoutState.CLOSED);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the turnout should be thrown
        Assert.assertTrue("turnout is thrown",turnout.getCommandedState() == Turnout.CLOSED);
        
        // Test to set turnout to toggle
        actionTurnout.setTurnoutState(ActionTurnout.TurnoutState.TOGGLE);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the turnout should be thrown
        Assert.assertTrue("turnout is thrown",turnout.getCommandedState() == Turnout.THROWN);
        
        // Test to set turnout to toggle
        actionTurnout.setTurnoutState(ActionTurnout.TurnoutState.TOGGLE);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the turnout should be thrown
        Assert.assertTrue("turnout is thrown",turnout.getCommandedState() == Turnout.CLOSED);
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
    public void testIsExternal() {
        Assert.assertTrue("is external", _base.isExternal());
    }
    
    @Test
    public void testShortDescription() {
        Assert.assertEquals("String matches", "Set turnout", _base.getShortDescription());
    }
    
    @Test
    public void testLongDescription() {
        Assert.assertEquals("String matches", "Set turnout IT1 to Thrown", _base.getLongDescription());
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
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        
        _category = Category.ITEM;
        _isExternal = true;
        
        turnout = InstanceManager.getDefault(TurnoutManager.class).provide("IT1");
        turnout.setCommandedState(Turnout.CLOSED);
        logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A logixNG");
        conditionalNG = InstanceManager.getDefault(ConditionalNG_Manager.class)
                .createConditionalNG("A conditionalNG");  // NOI18N
        logixNG.addConditionalNG(conditionalNG);
        conditionalNG.setRunOnGUIDelayed(false);
        conditionalNG.setEnabled(true);
        actionTurnout = new ActionTurnout(InstanceManager.getDefault(DigitalActionManager.class).getAutoSystemName(), null);
        actionTurnout.setTurnout(turnout);
        actionTurnout.setTurnoutState(ActionTurnout.TurnoutState.THROWN);
        MaleSocket socket = InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionTurnout);
        conditionalNG.getChild(0).connect(socket);
        
        _base = actionTurnout;
        _baseMaleSocket = socket;
        
        logixNG.setParentForAllChildren();
        logixNG.setEnabled(true);
        logixNG.activateLogixNG();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    
}
