package jmri.jmrit.logixng.digital.boolean_actions;

import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.jmrit.logixng.AbstractBaseTestBase;
import jmri.jmrit.logixng.Category;
import jmri.jmrit.logixng.ConditionalNG;
import jmri.jmrit.logixng.ConditionalNG_Manager;
import jmri.jmrit.logixng.DigitalActionBean;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.DigitalExpressionManager;
import jmri.jmrit.logixng.LogixNG;
import jmri.jmrit.logixng.LogixNG_Manager;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.SocketAlreadyConnectedException;
import jmri.jmrit.logixng.digital.actions.ActionTurnout;
import jmri.jmrit.logixng.digital.actions.Logix;
import jmri.jmrit.logixng.digital.expressions.ExpressionSensor;
import jmri.jmrit.logixng.DigitalBooleanActionManager;
import jmri.jmrit.logixng.DigitalBooleanActionBean;
import jmri.jmrit.logixng.digital.actions.*;
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
                "            ! A%n" +
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
        JUnitUtil.resetProfileManager();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        
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
        
        OnChange actionOnChange = new OnChange("IQDB322", null, OnChange.ChangeType.CHANGE_TO_TRUE);
        MaleSocket maleSocketActionOnChange =
                InstanceManager.getDefault(DigitalBooleanActionManager.class).registerAction(actionOnChange);
        action.getChild(1).connect(maleSocketActionOnChange);
        
        ActionTurnout actionTurnout = new ActionTurnout("IQDA322", null);
        maleSocket2 =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionTurnout);
        actionOnChange.getChild(0).connect(maleSocket2);
        
        _base = actionOnChange;
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
