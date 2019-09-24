package jmri.jmrit.logixng.digital.actions_with_change;

import jmri.InstanceManager;
import jmri.jmrit.logixng.AbstractBaseTestBase;
import jmri.jmrit.logixng.ConditionalNG;
import jmri.jmrit.logixng.ConditionalNG_Manager;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.DigitalActionWithChangeBean;
import jmri.jmrit.logixng.DigitalActionWithChangeManager;
import jmri.jmrit.logixng.DigitalExpressionManager;
import jmri.jmrit.logixng.LogixNG;
import jmri.jmrit.logixng.LogixNG_Manager;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.SocketAlreadyConnectedException;
import jmri.jmrit.logixng.digital.actions.ActionTurnout;
import jmri.jmrit.logixng.digital.actions.Logix;
import jmri.jmrit.logixng.digital.actions_with_change.OnChangeAction;
import jmri.jmrit.logixng.digital.expressions.ExpressionSensor;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test IfThenElse
 * 
 * @author Daniel Bergqvist 2018
 */
public class OnChangeActionTest extends AbstractBaseTestBase {

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
    public String getExpectedPrintedTree() {
        return String.format(
                "Logix%n" +
                "   ? E%n" +
                "      Sensor Not selected is Active%n" +
                "   ! A%n" +
                "      On change to true%n" +
                "         ! A%n" +
                "            Set turnout '' to Thrown%n");
    }
    
    @Override
    public String getExpectedPrintedTreeFromRoot() {
        return String.format(
                "LogixNG: A new logix for test%n" +
                "   ConditionalNG: A conditionalNG%n" +
                "      ! %n" +
                "         Logix%n" +
                "            ? E%n" +
                "               Sensor Not selected is Active%n" +
                "            ! A%n" +
                "               On change to true%n" +
                "                  ! A%n" +
                "                     Set turnout '' to Thrown%n");
    }
    
    @Test
    public void testCtor() {
        DigitalActionWithChangeBean t = new OnChangeAction("IQDC321", null, OnChangeAction.ChangeType.CHANGE);
        Assert.assertNotNull("exists",t);
    }
    
    @Test
    public void testSupportsEnableExecution() throws SocketAlreadyConnectedException {
        DigitalActionWithChangeBean da = new OnChangeAction("IQDC321", null, OnChangeAction.ChangeType.CHANGE_TO_TRUE);
        
        Assert.assertFalse("supportsEnableExecution() returns correct value",
                da.supportsEnableExecution());
    }
    
    @Test
    public void testGetShortDescription() {
        DigitalActionWithChangeBean a1 = new OnChangeAction("IQDC321", null, OnChangeAction.ChangeType.CHANGE_TO_TRUE);
        Assert.assertEquals("strings are equal", "On change", a1.getShortDescription());
        DigitalActionWithChangeBean a2 = new OnChangeAction("IQDC322", null, OnChangeAction.ChangeType.CHANGE_TO_FALSE);
        Assert.assertEquals("strings are equal", "On change", a2.getShortDescription());
        DigitalActionWithChangeBean a3 = new OnChangeAction("IQDC323", null, OnChangeAction.ChangeType.CHANGE);
        Assert.assertEquals("strings are equal", "On change", a3.getShortDescription());
    }
    
    @Test
    public void testGetLongDescription() {
        DigitalActionWithChangeBean a1 = new OnChangeAction("IQDC321", null, OnChangeAction.ChangeType.CHANGE_TO_TRUE);
        Assert.assertEquals("strings are equal", "On change to true", a1.getLongDescription());
        DigitalActionWithChangeBean a2 = new OnChangeAction("IQDC322", null, OnChangeAction.ChangeType.CHANGE_TO_FALSE);
        Assert.assertEquals("strings are equal", "On change to false", a2.getLongDescription());
        DigitalActionWithChangeBean a3 = new OnChangeAction("IQDC323", null, OnChangeAction.ChangeType.CHANGE);
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
        
        logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test");  // NOI18N
        conditionalNG = InstanceManager.getDefault(ConditionalNG_Manager.class)
                .createConditionalNG("A conditionalNG");  // NOI18N
        logixNG.addConditionalNG(conditionalNG);
        Logix action = new Logix("IQDA321", null);
        MaleSocket maleSocket =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
        conditionalNG.getChild(0).connect(maleSocket);
        
        ExpressionSensor expressionSensor = new ExpressionSensor("IQDE321", null);
        MaleSocket maleSocket2 =
                InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expressionSensor);
        action.getChild(0).connect(maleSocket2);
        
        OnChangeAction actionOnChange = new OnChangeAction("IQDC322", null, OnChangeAction.ChangeType.CHANGE_TO_TRUE);
        maleSocket2 =
                InstanceManager.getDefault(DigitalActionWithChangeManager.class).registerAction(actionOnChange);
        action.getChild(1).connect(maleSocket2);
        
        ActionTurnout actionTurnout = new ActionTurnout("IQDA322", null);
        maleSocket2 =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionTurnout);
        actionOnChange.getChild(0).connect(maleSocket2);
        
        _base = action;
        _baseMaleSocket = maleSocket;
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    
}
