package jmri.jmrit.logixng.digital.actions;

import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.digital.expressions.*;
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
    
    @Override
    public boolean addNewSocket() {
        return false;
    }
    
    @Test
    public void testCtor() {
        DigitalActionBean t = new IfThenElse("IQDA321", null, IfThenElse.Type.TRIGGER_ACTION);
        Assert.assertNotNull("exists",t);
        t = new IfThenElse("IQDA321", null, IfThenElse.Type.CONTINOUS_ACTION);
        Assert.assertNotNull("exists",t);
    }
    
    @Test
    public void testCtorAndSetup1() {
        IfThenElse expression = new IfThenElse("IQDA321", null, IfThenElse.Type.TRIGGER_ACTION);
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
                "jmri.jmrit.logixng.digital.implementation.DefaultFemaleDigitalExpressionSocket",
                expression.getChild(0).getClass().getName());
        Assert.assertFalse("expression female socket is not connected",
                expression.getChild(0).isConnected());
        
        Assert.assertEquals("expression female socket name is ZH12",
                "ZH12", expression.getChild(1).getName());
        Assert.assertEquals("expression female socket is of correct class",
                "jmri.jmrit.logixng.digital.implementation.DefaultFemaleDigitalActionSocket",
                expression.getChild(1).getClass().getName());
        Assert.assertFalse("expression female socket is not connected",
                expression.getChild(1).isConnected());
        
        Assert.assertEquals("expression female socket name is Bj23",
                "Bj23", expression.getChild(2).getName());
        Assert.assertEquals("expression female socket is of correct class",
                "jmri.jmrit.logixng.digital.implementation.DefaultFemaleDigitalActionSocket",
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
                "jmri.jmrit.logixng.digital.implementation.DefaultFemaleDigitalExpressionSocket",
                expression.getChild(0).getClass().getName());
        Assert.assertFalse("expression female socket is not connected",
                expression.getChild(0).isConnected());
        
        Assert.assertEquals("expression female socket name is ZH12",
                "ZH12", expression.getChild(1).getName());
        Assert.assertEquals("expression female socket is of correct class",
                "jmri.jmrit.logixng.digital.implementation.DefaultFemaleDigitalActionSocket",
                expression.getChild(1).getClass().getName());
        Assert.assertFalse("expression female socket is not connected",
                expression.getChild(1).isConnected());
        
        Assert.assertEquals("expression female socket name is Bj23",
                "Bj23", expression.getChild(2).getName());
        Assert.assertEquals("expression female socket is of correct class",
                "jmri.jmrit.logixng.digital.implementation.DefaultFemaleDigitalActionSocket",
                expression.getChild(2).getClass().getName());
        Assert.assertFalse("expression female socket is not connected",
                expression.getChild(2).isConnected());
        
        Assert.assertEquals("expression has 3 female sockets", 3, expression.getChildCount());
    }
    
    @Test
    public void testCtorAndSetup2() {
        IfThenElse expression = new IfThenElse("IQDA321", null, IfThenElse.Type.TRIGGER_ACTION);
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
                "jmri.jmrit.logixng.digital.implementation.DefaultFemaleDigitalExpressionSocket",
                expression.getChild(0).getClass().getName());
        Assert.assertFalse("expression female socket is not connected",
                expression.getChild(0).isConnected());
        
        Assert.assertEquals("expression female socket name is ZH12",
                "ZH12", expression.getChild(1).getName());
        Assert.assertEquals("expression female socket is of correct class",
                "jmri.jmrit.logixng.digital.implementation.DefaultFemaleDigitalActionSocket",
                expression.getChild(1).getClass().getName());
        Assert.assertFalse("expression female socket is not connected",
                expression.getChild(1).isConnected());
        
        Assert.assertEquals("expression female socket name is Bj23",
                "Bj23", expression.getChild(2).getName());
        Assert.assertEquals("expression female socket is of correct class",
                "jmri.jmrit.logixng.digital.implementation.DefaultFemaleDigitalActionSocket",
                expression.getChild(2).getClass().getName());
        Assert.assertFalse("expression female socket is not connected",
                expression.getChild(2).isConnected());
        
        // Setup action. This connects the child actions to this action
        expression.setup();
        
        Assert.assertEquals("expression female socket name is XYZ123",
                "XYZ123", expression.getChild(0).getName());
        Assert.assertEquals("expression female socket is of correct class",
//                "jmri.jmrit.logixng.implementation.DefaultFemaleGenericExpressionSocket$DigitalSocket",
                "jmri.jmrit.logixng.digital.implementation.DefaultFemaleDigitalExpressionSocket",
                expression.getChild(0).getClass().getName());
        Assert.assertFalse("expression female socket is not connected",
                expression.getChild(0).isConnected());
        
        Assert.assertEquals("expression female socket name is ZH12",
                "ZH12", expression.getChild(1).getName());
        Assert.assertEquals("expression female socket is of correct class",
                "jmri.jmrit.logixng.digital.implementation.DefaultFemaleDigitalActionSocket",
                expression.getChild(1).getClass().getName());
        Assert.assertFalse("expression female socket is not connected",
                expression.getChild(1).isConnected());
        
        Assert.assertEquals("expression female socket name is Bj23",
                "Bj23", expression.getChild(2).getName());
        Assert.assertEquals("expression female socket is of correct class",
                "jmri.jmrit.logixng.digital.implementation.DefaultFemaleDigitalActionSocket",
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
        
        IfThenElse expression = new IfThenElse("IQDA321", null, IfThenElse.Type.TRIGGER_ACTION);
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
                "jmri.jmrit.logixng.digital.implementation.DefaultFemaleDigitalExpressionSocket",
                expression.getChild(0).getClass().getName());
        Assert.assertFalse("expression female socket is not connected",
                expression.getChild(0).isConnected());
        
        Assert.assertEquals("expression female socket name is ZH12",
                "ZH12", expression.getChild(1).getName());
        Assert.assertEquals("expression female socket is of correct class",
                "jmri.jmrit.logixng.digital.implementation.DefaultFemaleDigitalActionSocket",
                expression.getChild(1).getClass().getName());
        Assert.assertFalse("expression female socket is not connected",
                expression.getChild(1).isConnected());
        
        Assert.assertEquals("expression female socket name is Bj23",
                "Bj23", expression.getChild(2).getName());
        Assert.assertEquals("expression female socket is of correct class",
                "jmri.jmrit.logixng.digital.implementation.DefaultFemaleDigitalActionSocket",
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
        DigitalActionBean a1 = new IfThenElse("IQDA321", null, IfThenElse.Type.TRIGGER_ACTION);
        Assert.assertEquals("strings are equal", "If then else", a1.getShortDescription());
        DigitalActionBean a2 = new IfThenElse("IQDA321", null, IfThenElse.Type.CONTINOUS_ACTION);
        Assert.assertEquals("strings are equal", "If E then A1 else A2", a2.getLongDescription());
    }
    
    @Test
    @Override
    public void testSupportsEnableExecution() throws SocketAlreadyConnectedException {
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
