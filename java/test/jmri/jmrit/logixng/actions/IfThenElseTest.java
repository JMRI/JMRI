package jmri.jmrit.logixng.actions;

import java.util.ArrayList;

import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.expressions.ExpressionSensor;
import jmri.jmrit.logixng.expressions.ExpressionMemory;
import jmri.jmrit.logixng.expressions.True;
import jmri.jmrit.logixng.implementation.DefaultConditionalNGScaffold;
import jmri.util.JUnitUtil;

import org.junit.*;

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
                "If Then Else. Execute on change ::: Use default%n" +
                "   ? If%n" +
                "      Sensor '' is Active ::: Use default%n" +
                "   ! Then%n" +
                "      Set turnout '' to state Thrown ::: Use default%n" +
                "   ! Else%n" +
                "      Socket not connected%n");
    }
    
    @Override
    public String getExpectedPrintedTreeFromRoot() {
        return String.format(
                "LogixNG: A new logix for test%n" +
                "   ConditionalNG: A conditionalNG%n" +
                "      ! A%n" +
                "         If Then Else. Execute on change ::: Use default%n" +
                "            ? If%n" +
                "               Sensor '' is Active ::: Use default%n" +
                "            ! Then%n" +
                "               Set turnout '' to state Thrown ::: Use default%n" +
                "            ! Else%n" +
                "               Socket not connected%n");
    }
    
    @Override
    public NamedBean createNewBean(String systemName) {
        return new IfThenElse(systemName, null);
    }
    
    @Override
    public boolean addNewSocket() {
        return false;
    }
    
    @Test
    public void testCtor() {
        IfThenElse t = new IfThenElse("IQDA321", null);
        t.setType(IfThenElse.Type.ExecuteOnChange);
        Assert.assertNotNull("exists",t);
        t = new IfThenElse("IQDA321", null);
        t.setType(IfThenElse.Type.AlwaysExecute);
        Assert.assertNotNull("exists",t);
    }
    
    @Test
    public void testCtorAndSetup1() {
        IfThenElse expression = new IfThenElse("IQDA321", null);
        expression.setType(IfThenElse.Type.ExecuteOnChange);
        Assert.assertNotNull("exists", expression);
        Assert.assertEquals("expression has 3 female sockets", 3, expression.getChildCount());
        expression.getChild(0).setName("XYZ123");
        expression.setIfExpressionSocketSystemName("IQDE52");
        expression.getChild(1).setName("ZH12");
        expression.setThenActionSocketSystemName("IQDA554");
        expression.getChild(2).setName("Bj23");
        expression.setElseActionSocketSystemName("IQDA594");
        
        Assert.assertEquals("expression female socket name is XYZ123",
                "XYZ123", expression.getChild(0).getName());
        Assert.assertEquals("expression female socket is of correct class",
//                "jmri.jmrit.logixng.implementation.DefaultFemaleGenericExpressionSocket$DigitalSocket",
                "jmri.jmrit.logixng.implementation.DefaultFemaleDigitalExpressionSocket",
                expression.getChild(0).getClass().getName());
        Assert.assertFalse("expression female socket is not connected",
                expression.getChild(0).isConnected());
        
        Assert.assertEquals("expression female socket name is ZH12",
                "ZH12", expression.getChild(1).getName());
        Assert.assertEquals("expression female socket is of correct class",
                "jmri.jmrit.logixng.implementation.DefaultFemaleDigitalActionSocket",
                expression.getChild(1).getClass().getName());
        Assert.assertFalse("expression female socket is not connected",
                expression.getChild(1).isConnected());
        
        Assert.assertEquals("expression female socket name is Bj23",
                "Bj23", expression.getChild(2).getName());
        Assert.assertEquals("expression female socket is of correct class",
                "jmri.jmrit.logixng.implementation.DefaultFemaleDigitalActionSocket",
                expression.getChild(2).getClass().getName());
        Assert.assertFalse("expression female socket is not connected",
                expression.getChild(2).isConnected());
        
        // Setup action. This connects the child actions to this action
        expression.setup();
        
        jmri.util.JUnitAppender.assertMessage("cannot load digital expression IQDE52");
        jmri.util.JUnitAppender.assertMessage("cannot load digital action IQDA554");
        jmri.util.JUnitAppender.assertMessage("cannot load digital action IQDA594");
        
        Assert.assertEquals("expression female socket name is XYZ123",
                "XYZ123", expression.getChild(0).getName());
        Assert.assertEquals("expression female socket is of correct class",
//                "jmri.jmrit.logixng.implementation.DefaultFemaleGenericExpressionSocket$DigitalSocket",
                "jmri.jmrit.logixng.implementation.DefaultFemaleDigitalExpressionSocket",
                expression.getChild(0).getClass().getName());
        Assert.assertFalse("expression female socket is not connected",
                expression.getChild(0).isConnected());
        
        Assert.assertEquals("expression female socket name is ZH12",
                "ZH12", expression.getChild(1).getName());
        Assert.assertEquals("expression female socket is of correct class",
                "jmri.jmrit.logixng.implementation.DefaultFemaleDigitalActionSocket",
                expression.getChild(1).getClass().getName());
        Assert.assertFalse("expression female socket is not connected",
                expression.getChild(1).isConnected());
        
        Assert.assertEquals("expression female socket name is Bj23",
                "Bj23", expression.getChild(2).getName());
        Assert.assertEquals("expression female socket is of correct class",
                "jmri.jmrit.logixng.implementation.DefaultFemaleDigitalActionSocket",
                expression.getChild(2).getClass().getName());
        Assert.assertFalse("expression female socket is not connected",
                expression.getChild(2).isConnected());
        
        Assert.assertEquals("expression has 3 female sockets", 3, expression.getChildCount());
    }
    
    @Test
    public void testCtorAndSetup2() {
        IfThenElse expression = new IfThenElse("IQDA321", null);
        expression.setType(IfThenElse.Type.ExecuteOnChange);
        Assert.assertNotNull("exists", expression);
        Assert.assertEquals("expression has 3 female sockets", 3, expression.getChildCount());
        expression.getChild(0).setName("XYZ123");
        expression.setIfExpressionSocketSystemName(null);
        expression.getChild(1).setName("ZH12");
        expression.setThenActionSocketSystemName(null);
        expression.getChild(2).setName("Bj23");
        expression.setElseActionSocketSystemName(null);
        
        Assert.assertEquals("expression female socket name is XYZ123",
                "XYZ123", expression.getChild(0).getName());
        Assert.assertEquals("expression female socket is of correct class",
//                "jmri.jmrit.logixng.implementation.DefaultFemaleGenericExpressionSocket$DigitalSocket",
                "jmri.jmrit.logixng.implementation.DefaultFemaleDigitalExpressionSocket",
                expression.getChild(0).getClass().getName());
        Assert.assertFalse("expression female socket is not connected",
                expression.getChild(0).isConnected());
        
        Assert.assertEquals("expression female socket name is ZH12",
                "ZH12", expression.getChild(1).getName());
        Assert.assertEquals("expression female socket is of correct class",
                "jmri.jmrit.logixng.implementation.DefaultFemaleDigitalActionSocket",
                expression.getChild(1).getClass().getName());
        Assert.assertFalse("expression female socket is not connected",
                expression.getChild(1).isConnected());
        
        Assert.assertEquals("expression female socket name is Bj23",
                "Bj23", expression.getChild(2).getName());
        Assert.assertEquals("expression female socket is of correct class",
                "jmri.jmrit.logixng.implementation.DefaultFemaleDigitalActionSocket",
                expression.getChild(2).getClass().getName());
        Assert.assertFalse("expression female socket is not connected",
                expression.getChild(2).isConnected());
        
        // Setup action. This connects the child actions to this action
        expression.setup();
        
        Assert.assertEquals("expression female socket name is XYZ123",
                "XYZ123", expression.getChild(0).getName());
        Assert.assertEquals("expression female socket is of correct class",
//                "jmri.jmrit.logixng.implementation.DefaultFemaleGenericExpressionSocket$DigitalSocket",
                "jmri.jmrit.logixng.implementation.DefaultFemaleDigitalExpressionSocket",
                expression.getChild(0).getClass().getName());
        Assert.assertFalse("expression female socket is not connected",
                expression.getChild(0).isConnected());
        
        Assert.assertEquals("expression female socket name is ZH12",
                "ZH12", expression.getChild(1).getName());
        Assert.assertEquals("expression female socket is of correct class",
                "jmri.jmrit.logixng.implementation.DefaultFemaleDigitalActionSocket",
                expression.getChild(1).getClass().getName());
        Assert.assertFalse("expression female socket is not connected",
                expression.getChild(1).isConnected());
        
        Assert.assertEquals("expression female socket name is Bj23",
                "Bj23", expression.getChild(2).getName());
        Assert.assertEquals("expression female socket is of correct class",
                "jmri.jmrit.logixng.implementation.DefaultFemaleDigitalActionSocket",
                expression.getChild(2).getClass().getName());
        Assert.assertFalse("expression female socket is not connected",
                expression.getChild(2).isConnected());
        
        Assert.assertEquals("expression has 3 female sockets", 3, expression.getChildCount());
    }
    
    @Test
    public void testCtorAndSetup3() {
        DigitalExpressionManager m0 = InstanceManager.getDefault(DigitalExpressionManager.class);
        DigitalActionManager m1 = InstanceManager.getDefault(DigitalActionManager.class);
        
        MaleSocket childSocket0 = m0.registerExpression(new ExpressionMemory("IQDE52", null));
        MaleSocket childSocket1 = m1.registerAction(new ActionMemory("IQDA554", null));
        MaleSocket childSocket2 = m1.registerAction(new ActionMemory("IQDA594", null));
        
        IfThenElse expression = new IfThenElse("IQDA321", null);
        expression.setType(IfThenElse.Type.ExecuteOnChange);
        Assert.assertNotNull("exists", expression);
        Assert.assertEquals("expression has 3 female sockets", 3, expression.getChildCount());
        expression.getChild(0).setName("XYZ123");
        expression.setIfExpressionSocketSystemName("IQDE52");
        expression.getChild(1).setName("ZH12");
        expression.setThenActionSocketSystemName("IQDA554");
        expression.getChild(2).setName("Bj23");
        expression.setElseActionSocketSystemName("IQDA594");
        
        Assert.assertEquals("expression female socket name is XYZ123",
                "XYZ123", expression.getChild(0).getName());
        Assert.assertEquals("expression female socket is of correct class",
//                "jmri.jmrit.logixng.implementation.DefaultFemaleGenericExpressionSocket$DigitalSocket",
                "jmri.jmrit.logixng.implementation.DefaultFemaleDigitalExpressionSocket",
                expression.getChild(0).getClass().getName());
        Assert.assertFalse("expression female socket is not connected",
                expression.getChild(0).isConnected());
        
        Assert.assertEquals("expression female socket name is ZH12",
                "ZH12", expression.getChild(1).getName());
        Assert.assertEquals("expression female socket is of correct class",
                "jmri.jmrit.logixng.implementation.DefaultFemaleDigitalActionSocket",
                expression.getChild(1).getClass().getName());
        Assert.assertFalse("expression female socket is not connected",
                expression.getChild(1).isConnected());
        
        Assert.assertEquals("expression female socket name is Bj23",
                "Bj23", expression.getChild(2).getName());
        Assert.assertEquals("expression female socket is of correct class",
                "jmri.jmrit.logixng.implementation.DefaultFemaleDigitalActionSocket",
                expression.getChild(2).getClass().getName());
        Assert.assertFalse("expression female socket is not connected",
                expression.getChild(2).isConnected());
        
        // Setup action. This connects the child actions to this action
        expression.setup();
        
        Assert.assertTrue("expression female socket is connected",
                expression.getChild(0).isConnected());
        Assert.assertEquals("child is correct bean",
                childSocket0,
                expression.getChild(0).getConnectedSocket());
        Assert.assertEquals("expression has 3 female sockets", 3, expression.getChildCount());
        
        Assert.assertTrue("expression female socket is connected",
                expression.getChild(1).isConnected());
        Assert.assertEquals("child is correct bean",
                childSocket1,
                expression.getChild(1).getConnectedSocket());
        Assert.assertEquals("expression has 3 female sockets", 3, expression.getChildCount());
        
        Assert.assertTrue("expression female socket is connected",
                expression.getChild(2).isConnected());
        Assert.assertEquals("child is correct bean",
                childSocket2,
                expression.getChild(2).getConnectedSocket());
        
        Assert.assertEquals("expression has 3 female sockets", 3, expression.getChildCount());
        
        // Try run setup() again. That should not cause any problems.
        expression.setup();
        
        Assert.assertEquals("expression has 3 female sockets", 3, expression.getChildCount());
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
        IfThenElse action = new IfThenElse("IQDA321", null);
        action.setType(IfThenElse.Type.ExecuteOnChange);
        Assert.assertEquals("strings are equal", "If Then Else", action.getShortDescription());
        Assert.assertEquals("strings are equal", "If Then Else. Execute on change", action.getLongDescription());
        
        action = new IfThenElse("IQDA321", null);
        action.setType(IfThenElse.Type.AlwaysExecute);
        Assert.assertEquals("strings are equal", "If Then Else", action.getShortDescription());
        Assert.assertEquals("strings are equal", "If Then Else. Always execute", action.getLongDescription());
    }
    
    @Test
    public void testCategory() {
        Assert.assertTrue("Category matches", Category.COMMON == _base.getCategory());
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
        
        InstanceManager.getDefault(LogixNGPreferences.class).setInstallDebugger(false);
        JUnitUtil.initLogixNGManager();
        
        _category = Category.COMMON;
        _isExternal = false;
        
        logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test");  // NOI18N
        conditionalNG = new DefaultConditionalNGScaffold("IQC1", "A conditionalNG");  // NOI18N;
        InstanceManager.getDefault(ConditionalNG_Manager.class).register(conditionalNG);
        conditionalNG.setEnabled(true);
        conditionalNG.setRunDelayed(false);
        logixNG.addConditionalNG(conditionalNG);
        actionIfThenElse = new IfThenElse("IQDA321", null);
        actionIfThenElse.setType(IfThenElse.Type.ExecuteOnChange);
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
        
        if (! logixNG.setParentForAllChildren(new ArrayList<>())) throw new RuntimeException();
        logixNG.setEnabled(true);
    }

    @After
    public void tearDown() {
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.tearDown();
    }
    
}
