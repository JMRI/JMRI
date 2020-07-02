package jmri.jmrit.logixng.digital.boolean_actions;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.digital.actions.ActionTurnout;
import jmri.jmrit.logixng.digital.expressions.ExpressionSensor;
import jmri.jmrit.logixng.digital.actions.*;
import jmri.jmrit.logixng.digital.actions.Logix;
import jmri.jmrit.logixng.digital.boolean_actions.OnChange.ChangeType;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test OnChange
 * 
 * @author Daniel Bergqvist 2018
 */
public class OnChangeTest extends AbstractDigitalBooleanActionTestBase {

    LogixNG logixNG;
    ConditionalNG conditionalNG;
    OnChange _actionOnChange;
    
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
        DigitalActionBean childAction = new Many("IQDA999", null);
        MaleSocket maleSocketChild =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(childAction);
        return maleSocketChild;
    }
    
    @Override
    public NamedBean createNewBean(String systemName) {
        return new OnChange(systemName, null, ChangeType.CHANGE);
    }
    
    @Override
    public String getExpectedPrintedTree() {
        return String.format(
                "On change to true%n" +
                "   ! A%n" +
                "      Set turnout '' to Thrown%n");
    }
    
    @Override
    public String getExpectedPrintedTreeFromRoot() {
        return String.format(
                "LogixNG: A new logix for test%n" +
                "   ConditionalNG: A conditionalNG%n" +
                "      ! %n" +
                "         Logix%n" +
                "            ? E%n" +
                "               Sensor '' is Active%n" +
                "            !b A%n" +
                "               On change to true%n" +
                "                  ! A%n" +
                "                     Set turnout '' to Thrown%n");
    }
    
    @Override
    public boolean addNewSocket() {
        return false;
    }
    
    @Test
    public void testCtor() {
        DigitalBooleanActionBean t = new OnChange("IQDB321", null, OnChange.ChangeType.CHANGE);
        Assert.assertNotNull("exists",t);
    }
    
    @Test
    public void testCtorAndSetup1() {
        OnChange action = new OnChange("IQDB321", null, OnChange.ChangeType.CHANGE);
        Assert.assertNotNull("exists", action);
        Assert.assertEquals("action has 1 female socket", 1, action.getChildCount());
        action.getChild(0).setName("ZH12");
        action.setActionSocketSystemName("IQDA554");
        
        Assert.assertEquals("action female socket name is ZH12",
                "ZH12", action.getChild(0).getName());
        Assert.assertEquals("action female socket is of correct class",
                "jmri.jmrit.logixng.digital.implementation.DefaultFemaleDigitalActionSocket",
                action.getChild(0).getClass().getName());
        Assert.assertFalse("action female socket is not connected",
                action.getChild(0).isConnected());
        
        // Setup action. This connects the child actions to this action
        action.setup();
        
        jmri.util.JUnitAppender.assertMessage("cannot load digital action IQDA554");
        
        Assert.assertEquals("action female socket name is ZH12",
                "ZH12", action.getChild(0).getName());
        Assert.assertEquals("action female socket is of correct class",
                "jmri.jmrit.logixng.digital.implementation.DefaultFemaleDigitalActionSocket",
                action.getChild(0).getClass().getName());
        Assert.assertFalse("action female socket is not connected",
                action.getChild(0).isConnected());
        
        Assert.assertEquals("action has 1 female socket", 1, action.getChildCount());
    }
    
    @Test
    public void testCtorAndSetup2() {
        OnChange action = new OnChange("IQDB321", null, OnChange.ChangeType.CHANGE);
        Assert.assertNotNull("exists", action);
        Assert.assertEquals("action has 1 female socket", 1, action.getChildCount());
        action.getChild(0).setName("ZH12");
        action.setActionSocketSystemName(null);
        
        Assert.assertEquals("action female socket name is ZH12",
                "ZH12", action.getChild(0).getName());
        Assert.assertEquals("action female socket is of correct class",
                "jmri.jmrit.logixng.digital.implementation.DefaultFemaleDigitalActionSocket",
                action.getChild(0).getClass().getName());
        Assert.assertFalse("action female socket is not connected",
                action.getChild(0).isConnected());
        
        // Setup action. This connects the child actions to this action
        action.setup();
        
        Assert.assertEquals("action female socket name is ZH12",
                "ZH12", action.getChild(0).getName());
        Assert.assertEquals("action female socket is of correct class",
                "jmri.jmrit.logixng.digital.implementation.DefaultFemaleDigitalActionSocket",
                action.getChild(0).getClass().getName());
        Assert.assertFalse("action female socket is not connected",
                action.getChild(0).isConnected());
        
        Assert.assertEquals("action has 1 female socket", 1, action.getChildCount());
    }
    
    @Test
    public void testCtorAndSetup3() {
        DigitalActionManager m1 = InstanceManager.getDefault(DigitalActionManager.class);
        
        MaleSocket childSocket0 = m1.registerAction(new ActionMemory("IQDA554", null));
        
        OnChange action = new OnChange("IQDB321", null, OnChange.ChangeType.CHANGE);
        Assert.assertNotNull("exists", action);
        Assert.assertEquals("action has 1 female socket", 1, action.getChildCount());
        action.getChild(0).setName("ZH12");
        action.setActionSocketSystemName("IQDA554");
        
        Assert.assertEquals("action female socket name is ZH12",
                "ZH12", action.getChild(0).getName());
        Assert.assertEquals("action female socket is of correct class",
                "jmri.jmrit.logixng.digital.implementation.DefaultFemaleDigitalActionSocket",
                action.getChild(0).getClass().getName());
        Assert.assertFalse("action female socket is not connected",
                action.getChild(0).isConnected());
        
        // Setup action. This connects the child actions to this action
        action.setup();
        
        Assert.assertTrue("action female socket is connected",
                action.getChild(0).isConnected());
        Assert.assertEquals("child is correct bean",
                childSocket0,
                action.getChild(0).getConnectedSocket());
        
        Assert.assertEquals("action has 1 female sockets", 1, action.getChildCount());
        
        // Try run setup() again. That should not cause any problems.
        action.setup();
        
        Assert.assertEquals("action has 1 female sockets", 1, action.getChildCount());
    }
    
    @Test
    public void testGetChild() {
        Assert.assertTrue("getChildCount() returns 1", 1 == _actionOnChange.getChildCount());
        
        Assert.assertNotNull("getChild(0) returns a non null value",
                _actionOnChange.getChild(0));
        
        boolean hasThrown = false;
        try {
            _actionOnChange.getChild(1);
        } catch (IllegalArgumentException ex) {
            hasThrown = true;
            Assert.assertEquals("Error message is correct", "index has invalid value: 1", ex.getMessage());
        }
        Assert.assertTrue("Exception is thrown", hasThrown);
    }
    
    @Test
    public void testCategory() {
        Assert.assertTrue("Category matches", Category.COMMON == _base.getCategory());
    }
    
    @Test
    public void testIsExternal() {
        Assert.assertFalse("is external", _base.isExternal());
    }
    
    @Test
    public void testGetShortDescription() {
        DigitalBooleanActionBean a1 = new OnChange("IQDB321", null, OnChange.ChangeType.CHANGE_TO_TRUE);
        Assert.assertEquals("strings are equal", "On change", a1.getShortDescription());
        DigitalBooleanActionBean a2 = new OnChange("IQDB322", null, OnChange.ChangeType.CHANGE_TO_FALSE);
        Assert.assertEquals("strings are equal", "On change", a2.getShortDescription());
        DigitalBooleanActionBean a3 = new OnChange("IQDB323", null, OnChange.ChangeType.CHANGE);
        Assert.assertEquals("strings are equal", "On change", a3.getShortDescription());
    }
    
    @Test
    public void testGetLongDescription() {
        DigitalBooleanActionBean a1 = new OnChange("IQDB321", null, OnChange.ChangeType.CHANGE_TO_TRUE);
        Assert.assertEquals("strings are equal", "On change to true", a1.getLongDescription());
        DigitalBooleanActionBean a2 = new OnChange("IQDB322", null, OnChange.ChangeType.CHANGE_TO_FALSE);
        Assert.assertEquals("strings are equal", "On change to false", a2.getLongDescription());
        DigitalBooleanActionBean a3 = new OnChange("IQDB323", null, OnChange.ChangeType.CHANGE);
        Assert.assertEquals("strings are equal", "On change", a3.getLongDescription());
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
        
        _category = Category.COMMON;
        _isExternal = false;
        
        logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test");  // NOI18N
        conditionalNG = InstanceManager.getDefault(ConditionalNG_Manager.class)
                .createConditionalNG("A conditionalNG");  // NOI18N
        conditionalNG.setEnabled(true);
        conditionalNG.setRunOnGUIDelayed(false);
        logixNG.addConditionalNG(conditionalNG);
        Logix action = new Logix("IQDA321", null);
        MaleSocket maleSocket =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
        conditionalNG.getChild(0).connect(maleSocket);
        
        ExpressionSensor expressionSensor = new ExpressionSensor("IQDE321", null);
        MaleSocket maleSocket2 =
                InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expressionSensor);
        action.getChild(0).connect(maleSocket2);
        
        _actionOnChange = new OnChange("IQDB322", null, OnChange.ChangeType.CHANGE_TO_TRUE);
        MaleSocket maleSocketActionOnChange =
                InstanceManager.getDefault(DigitalBooleanActionManager.class).registerAction(_actionOnChange);
        action.getChild(1).connect(maleSocketActionOnChange);
        
        ActionTurnout actionTurnout = new ActionTurnout("IQDA322", null);
        maleSocket2 =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionTurnout);
        _actionOnChange.getChild(0).connect(maleSocket2);
        
        _base = _actionOnChange;
        _baseMaleSocket = maleSocketActionOnChange;
        
        logixNG.setParentForAllChildren();
        logixNG.setEnabled(true);
        logixNG.activateLogixNG();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    
}
