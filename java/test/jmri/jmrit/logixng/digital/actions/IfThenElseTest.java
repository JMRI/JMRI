package jmri.jmrit.logixng.digital.actions;

import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.jmrit.logixng.Category;
import jmri.jmrit.logixng.ConditionalNG;
import jmri.jmrit.logixng.ConditionalNG_Manager;
import jmri.jmrit.logixng.DigitalExpressionManager;
import jmri.jmrit.logixng.DigitalAction;
import jmri.jmrit.logixng.DigitalActionBean;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.DigitalActionWithEnableExecution;
import jmri.jmrit.logixng.DigitalExpressionBean;
import jmri.jmrit.logixng.LogixNG;
import jmri.jmrit.logixng.LogixNG_Manager;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.SocketAlreadyConnectedException;
import jmri.jmrit.logixng.digital.expressions.ExpressionSensor;
import jmri.jmrit.logixng.digital.expressions.True;
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
public class IfThenElseTest extends AbstractDigitalActionTestBase {

    LogixNG logixNG;
    ConditionalNG conditionalNG;
    IfThenElse actionIfThenElse;
    
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
        DigitalExpressionBean childExpression = new True("IQDE999", null);
        MaleSocket maleSocketChild =
                InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(childExpression);
        return maleSocketChild;
    }
    
    @Override
    public String getExpectedPrintedTree() {
        return String.format(
                "If E then A1 else A2%n" +
                "   ? E%n" +
                "      Sensor '' is Active%n" +
                "   ! A1%n" +
                "      Set turnout '' to Thrown%n" +
                "   ! A2%n" +
                "      Socket not connected%n");
    }
    
    @Override
    public String getExpectedPrintedTreeFromRoot() {
        return String.format(
                "LogixNG: A new logix for test%n" +
                "   ConditionalNG: A conditionalNG%n" +
                "      ! %n" +
                "         If E then A1 else A2%n" +
                "            ? E%n" +
                "               Sensor '' is Active%n" +
                "            ! A1%n" +
                "               Set turnout '' to Thrown%n" +
                "            ! A2%n" +
                "               Socket not connected%n");
    }
    
    @Override
    public NamedBean createNewBean(String systemName) {
        return new IfThenElse(systemName, null, IfThenElse.Type.CONTINOUS_ACTION);
    }
    
    @Test
    public void testCtor() {
        DigitalActionBean t = new IfThenElse("IQDA321", null, IfThenElse.Type.TRIGGER_ACTION);
        Assert.assertNotNull("exists",t);
        t = new IfThenElse("IQDA321", null, IfThenElse.Type.CONTINOUS_ACTION);
        Assert.assertNotNull("exists",t);
    }
    
    @Test
    public void testGetChild() {
        Assert.assertTrue("getChildCount() returns 3", 3 == actionIfThenElse.getChildCount());
        
        Assert.assertNotNull("getChild(0) returns a non null value",
                actionIfThenElse.getChild(0));
        Assert.assertNotNull("getChild(1) returns a non null value",
                actionIfThenElse.getChild(1));
        Assert.assertNotNull("getChild(2) returns a non null value",
                actionIfThenElse.getChild(2));
        
        boolean hasThrown = false;
        try {
            actionIfThenElse.getChild(3);
        } catch (IllegalArgumentException ex) {
            hasThrown = true;
            Assert.assertEquals("Error message is correct", "index has invalid value: 3", ex.getMessage());
        }
        Assert.assertTrue("Exception is thrown", hasThrown);
    }
    
    @Test
    public void testToString() {
        DigitalActionBean a1 = new IfThenElse("IQDA321", null, IfThenElse.Type.TRIGGER_ACTION);
        Assert.assertEquals("strings are equal", "If then else", a1.getShortDescription());
        DigitalActionBean a2 = new IfThenElse("IQDA321", null, IfThenElse.Type.CONTINOUS_ACTION);
        Assert.assertEquals("strings are equal", "If E then A1 else A2", a2.getLongDescription());
    }
    
    @Test
    @Override
    public void testSupportsEnableExecution() throws SocketAlreadyConnectedException {
        Assert.assertTrue("supportsEnableExecution() returns correct value",
                ((DigitalAction)_base).supportsEnableExecution());
        Assert.assertTrue("digital action implements DigitalActionWithEnableExecution",
                _base instanceof DigitalActionWithEnableExecution);
    }
    
    @Test
    public void testCategory() {
        Assert.assertTrue("Category matches", Category.COMMON == _base.getCategory());
    }
    
    @Test
    public void testIsExternal() {
        Assert.assertFalse("is external", _base.isExternal());
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
        actionIfThenElse = new IfThenElse("IQDA321", null, IfThenElse.Type.TRIGGER_ACTION);
        MaleSocket maleSocket =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionIfThenElse);
        conditionalNG.getChild(0).connect(maleSocket);
        
        ExpressionSensor expressionSensor = new ExpressionSensor("IQDE321", null);
        MaleSocket maleSocket2 =
                InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expressionSensor);
        actionIfThenElse.getChild(0).connect(maleSocket2);
        
        ActionTurnout actionTurnout = new ActionTurnout("IQDA322", null);
        maleSocket2 =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionTurnout);
        actionIfThenElse.getChild(1).connect(maleSocket2);
        
        _base = actionIfThenElse;
        _baseMaleSocket = maleSocket;
        
        logixNG.setParentForAllChildren();
        logixNG.setEnabled(true);
        logixNG.activateLogixNG();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    
}
