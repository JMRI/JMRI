package jmri.jmrit.display.logixng;

import java.awt.GraphicsEnvironment;

import jmri.jmrit.logixng.actions.*;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.util.ArrayList;

import jmri.*;
import jmri.jmrit.display.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.implementation.DefaultConditionalNGScaffold;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.*;

/**
 * Test ActionPositionable
 * 
 * @author Daniel Bergqvist 2018
 */
public class ActionPositionableTest extends AbstractDigitalActionTestBase {

    private LogixNG logixNG;
    private ConditionalNG conditionalNG;
    private ActionPositionable actionPositionable;
    private Turnout turnout;
    private Editor editor;
    private Positionable positionable1;
    private Positionable positionable2;
    private Positionable positionable3;
    
    
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
        return String.format("Set icon/label \"Some other id\" on panel \"A panel editor\" to \"Disable\" ::: Use default%n");
    }
    
    @Override
    public String getExpectedPrintedTreeFromRoot() {
        return String.format(
                "LogixNG: A logixNG%n" +
                "   ConditionalNG: A conditionalNG%n" +
                "      ! A%n" +
                "         Set icon/label \"Some other id\" on panel \"A panel editor\" to \"Disable\" ::: Use default%n");
    }
    
    @Override
    public NamedBean createNewBean(String systemName) {
        return new ActionPositionable(systemName, null);
    }
    
    @Override
    public boolean addNewSocket() {
        return false;
    }
    
    @Test
    public void testCtor() throws JmriException {
        Assert.assertTrue("object exists", _base != null);
        
        ActionPositionable action2;
        Assert.assertNotNull("turnout is not null", turnout);
        turnout.setState(Turnout.ON);
        
        action2 = new ActionPositionable("IQDA321", null);
        Assert.assertNotNull("object exists", action2);
        Assert.assertNull("Username matches", action2.getUserName());
        Assert.assertEquals("String matches", "Set icon/label \"''\" on panel \"''\" to \"Enable\"", action2.getLongDescription());
        
        action2 = new ActionPositionable("IQDA321", "My turnout");
        Assert.assertNotNull("object exists", action2);
        Assert.assertEquals("Username matches", "My turnout", action2.getUserName());
        Assert.assertEquals("String matches", "Set icon/label \"''\" on panel \"''\" to \"Enable\"", action2.getLongDescription());
/*        
        action2 = new ActionPositionable("IQDA321", null);
        action2.setTurnout(turnout);
        Assert.assertTrue("turnout is correct", turnout == action2.getTurnout().getBean());
        Assert.assertNotNull("object exists", action2);
        Assert.assertNull("Username matches", action2.getUserName());
        Assert.assertEquals("String matches", "Set turnout IT1 to state Thrown", action2.getLongDescription());
        
        Turnout l = InstanceManager.getDefault(TurnoutManager.class).provide("IT1");
        action2 = new ActionPositionable("IQDA321", "My turnout");
        action2.setTurnout(l);
        Assert.assertTrue("turnout is correct", l == action2.getTurnout().getBean());
        Assert.assertNotNull("object exists", action2);
        Assert.assertEquals("Username matches", "My turnout", action2.getUserName());
        Assert.assertEquals("String matches", "Set turnout IT1 to state Thrown", action2.getLongDescription());
*/        
        boolean thrown = false;
        try {
            // Illegal system name
            new ActionPositionable("IQA55:12:XY11", null);
        } catch (IllegalArgumentException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
        
        thrown = false;
        try {
            // Illegal system name
            new ActionPositionable("IQA55:12:XY11", "A name");
        } catch (IllegalArgumentException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
        
        // Test setup(). This method doesn't do anything, but execute it for coverage.
        _base.setup();
    }
    
    @Test
    public void testGetChild() {
        Assert.assertTrue("getChildCount() returns 0", 0 == actionPositionable.getChildCount());
        
        boolean hasThrown = false;
        try {
            actionPositionable.getChild(0);
        } catch (UnsupportedOperationException ex) {
            hasThrown = true;
            Assert.assertEquals("Error message is correct", "Not supported.", ex.getMessage());
        }
        Assert.assertTrue("Exception is thrown", hasThrown);
    }
    
    @Test
    public void testTurnoutState() {
        Assert.assertEquals("String matches", "Disable", ActionPositionable.Operation.Disable.toString());
        Assert.assertEquals("String matches", "Enable", ActionPositionable.Operation.Enable.toString());
    }
/*    
    @Test
    public void testSetTurnout() {
        Turnout turnout11 = InstanceManager.getDefault(TurnoutManager.class).provide("IL11");
        Turnout turnout12 = InstanceManager.getDefault(TurnoutManager.class).provide("IL12");
        NamedBeanHandle<Turnout> turnoutHandle12 = InstanceManager.getDefault(NamedBeanHandleManager.class).getNamedBeanHandle(turnout12.getDisplayName(), turnout12);
        Turnout turnout13 = InstanceManager.getDefault(TurnoutManager.class).provide("IL13");
        Turnout turnout14 = InstanceManager.getDefault(TurnoutManager.class).provide("IL14");
        turnout14.setUserName("Some user name");
        
        actionEnableDisable.removeTurnout();
        Assert.assertNull("turnout handle is null", actionEnableDisable.getTurnout());
        
        actionEnableDisable.setTurnout(turnout11);
        Assert.assertTrue("turnout is correct", turnout11 == actionEnableDisable.getTurnout().getBean());
        
        actionEnableDisable.removeTurnout();
        Assert.assertNull("turnout handle is null", actionEnableDisable.getTurnout());
        
        actionEnableDisable.setTurnout(turnoutHandle12);
        Assert.assertTrue("turnout handle is correct", turnoutHandle12 == actionEnableDisable.getTurnout());
        
        actionEnableDisable.setTurnout("A non existent turnout");
        Assert.assertNull("turnout handle is null", actionEnableDisable.getTurnout());
        JUnitAppender.assertErrorMessage("turnout \"A non existent turnout\" is not found");
        
        actionEnableDisable.setTurnout(turnout13.getSystemName());
        Assert.assertTrue("turnout is correct", turnout13 == actionEnableDisable.getTurnout().getBean());
        
        actionEnableDisable.setTurnout(turnout14.getUserName());
        Assert.assertTrue("turnout is correct", turnout14 == actionEnableDisable.getTurnout().getBean());
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
        actionEnableDisable.setBeanState(ActionPositionable.TurnoutState.Closed);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the turnout should be thrown
        Assert.assertTrue("turnout is thrown",turnout.getCommandedState() == Turnout.CLOSED);
        
        // Test to set turnout to toggle
        actionEnableDisable.setBeanState(ActionPositionable.TurnoutState.Toggle);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the turnout should be thrown
        Assert.assertTrue("turnout is thrown",turnout.getCommandedState() == Turnout.THROWN);
        
        // Test to set turnout to toggle
        actionEnableDisable.setBeanState(ActionPositionable.TurnoutState.Toggle);
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
        
        actionEnableDisable.setBeanState(ActionPositionable.TurnoutState.Thrown);
        actionEnableDisable.setTurnout(t1.getSystemName());
        actionEnableDisable.setReference("{IM1}");    // Points to "IT102"
        actionEnableDisable.setLocalVariable("myTurnout");
        actionEnableDisable.setFormula("\"IT10\" + str(index)");
        _baseMaleSocket.addLocalVariable("refTurnout", SymbolTable.InitialValueType.String, "IT103");
        _baseMaleSocket.addLocalVariable("myTurnout", SymbolTable.InitialValueType.String, "IT104");
        _baseMaleSocket.addLocalVariable("index", SymbolTable.InitialValueType.Integer, "5");
        
        // Test direct addressing
        actionEnableDisable.setAddressing(NamedBeanAddressing.Direct);
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
        actionEnableDisable.setAddressing(NamedBeanAddressing.Reference);
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
        actionEnableDisable.setReference("{refTurnout}");    // Points to "IT103"
        actionEnableDisable.setAddressing(NamedBeanAddressing.Reference);
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
        actionEnableDisable.setAddressing(NamedBeanAddressing.LocalVariable);
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
        actionEnableDisable.setAddressing(NamedBeanAddressing.Formula);
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
        actionEnableDisable.setStateAddressing(NamedBeanAddressing.Direct);
        // Test Closed
        turnout.setState(Turnout.THROWN);
        actionEnableDisable.setBeanState(ActionPositionable.TurnoutState.Closed);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the correct turnout should be thrown
        Assert.assertEquals(Turnout.CLOSED, turnout.getCommandedState());
        // Test Thrown
        turnout.setState(Turnout.CLOSED);
        actionEnableDisable.setBeanState(ActionPositionable.TurnoutState.Thrown);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the correct turnout should be thrown
        Assert.assertEquals(Turnout.THROWN, turnout.getCommandedState());
        
        
        // Test reference by memory addressing
        actionEnableDisable.setStateAddressing(NamedBeanAddressing.Reference);
        actionEnableDisable.setStateReference("{IM1}");
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
        actionEnableDisable.setStateAddressing(NamedBeanAddressing.Reference);
        actionEnableDisable.setStateReference("{refVariable}");
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
        actionEnableDisable.setStateAddressing(NamedBeanAddressing.Reference);
        actionEnableDisable.setStateLocalVariable("myVariable");
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
        actionEnableDisable.setStateAddressing(NamedBeanAddressing.Formula);
        actionEnableDisable.setStateFormula("refVariable + myVariable");
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
        ActionPositionable action = new ActionPositionable(InstanceManager.getDefault(DigitalActionManager.class).getAutoSystemName(), null);
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
*/    
    @Test
    public void testCategory() {
        Assert.assertTrue("Category matches", CategoryDisplay.DISPLAY == _base.getCategory());
    }
    
    @Test
    public void testIsExternal() {
        Assert.assertTrue("is external", _base.isExternal());
    }
    
    @Test
    public void testShortDescription() {
        Assert.assertEquals("String matches", "Icon/Label on panel", _base.getShortDescription());
    }
    
    @Test
    public void testLongDescription() {
        Assert.assertEquals("String matches", "Set icon/label \"Some other id\" on panel \"A panel editor\" to \"Disable\"", _base.getLongDescription());
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
    public void setUp() throws SocketAlreadyConnectedException, Positionable.DuplicateIdException {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initLogixNGManager();
        
        _category = Category.ITEM;
        _isExternal = true;
        
        EditorManager editorManager = InstanceManager.getDefault(EditorManager.class);
        editor = new jmri.jmrit.display.panelEditor.PanelEditor("Some panel");
        editor.setName("A panel editor");
        editorManager.add(editor);
        
        positionable1 = new jmri.jmrit.display.MemoryIcon("Memory", editor);
        editor.putItem(positionable1);
        positionable1.setId("Some id");
        
        positionable2 = new jmri.jmrit.display.MemoryIcon("Memory", editor);
        editor.putItem(positionable2);
        positionable2.setId("Some other id");
        
        positionable3 = new jmri.jmrit.display.MemoryIcon("Memory", editor);
        editor.putItem(positionable3);
        
        turnout = InstanceManager.getDefault(TurnoutManager.class).provide("IT1");
        turnout.setCommandedState(Turnout.CLOSED);
        logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A logixNG");
        conditionalNG = new DefaultConditionalNGScaffold("IQC1", "A conditionalNG");  // NOI18N;
        InstanceManager.getDefault(ConditionalNG_Manager.class).register(conditionalNG);
        logixNG.addConditionalNG(conditionalNG);
        conditionalNG.setRunDelayed(false);
        conditionalNG.setEnabled(true);
        actionPositionable = new ActionPositionable(InstanceManager.getDefault(DigitalActionManager.class).getAutoSystemName(), null);
        actionPositionable.setEditor(editor.getName());
        actionPositionable.setPositionable(positionable2.getId());
        actionPositionable.setOperation(ActionPositionable.Operation.Disable);
        MaleSocket socket = InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionPositionable);
        conditionalNG.getChild(0).connect(socket);
        
        _base = actionPositionable;
        _baseMaleSocket = socket;
        
        if (! logixNG.setParentForAllChildren(new ArrayList())) throw new RuntimeException();
        logixNG.setEnabled(true);
    }

    @After
    public void tearDown() {
        editor = null;
        positionable1 = null;
        positionable2 = null;
        positionable3 = null;
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.deregisterEditorManagerShutdownTask();
        JUnitUtil.tearDown();
    }
    
}
