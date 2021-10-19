package jmri.jmrit.logixng.actions;

import java.util.ArrayList;

import jmri.jmrit.logixng.expressions.ExpressionSensor;
import jmri.jmrit.logixng.expressions.ExpressionMemory;
import jmri.jmrit.logixng.expressions.True;
import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.implementation.DefaultConditionalNGScaffold;
import jmri.jmrit.logixng.implementation.DefaultSymbolTable;
import jmri.util.JUnitUtil;

import org.junit.*;

/**
 * Test Logix
 * 
 * @author Daniel Bergqvist 2018
 */
public class LogixTest extends AbstractDigitalActionTestBase {

    LogixNG logixNG;
    ConditionalNG conditionalNG;
    Logix actionLogix;
    
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
                "Logix ::: Use default%n" +
                "   ? E%n" +
                "      Sensor '' is Active ::: Use default%n" +
                "   !b A%n" +
                "      On change ::: Use default%n" +
                "         ! A%n" +
                "            Socket not connected%n");
    }
    
    @Override
    public String getExpectedPrintedTreeFromRoot() {
        return String.format(
                "LogixNG: A new logix for test%n" +
                "   ConditionalNG: A conditionalNG%n" +
                "      ! A%n" +
                "         Logix ::: Use default%n" +
                "            ? E%n" +
                "               Sensor '' is Active ::: Use default%n" +
                "            !b A%n" +
                "               On change ::: Use default%n" +
                "                  ! A%n" +
                "                     Socket not connected%n");
    }
    
    @Override
    public NamedBean createNewBean(String systemName) {
        return new Logix(systemName, null);
    }
    
    @Override
    public boolean addNewSocket() {
        return false;
    }
    
    @Test
    public void testCtor() {
        DigitalActionBean t = new Logix("IQDA321", null);
        Assert.assertNotNull("exists",t);
        t = new Logix("IQDA321", null);
        Assert.assertNotNull("exists",t);
    }
    
    @Test
    public void testCtorAndSetup1() {
        Logix expression = new Logix("IQDA321", null);
        Assert.assertNotNull("exists", expression);
        Assert.assertEquals("expression has 2 female sockets", 2, expression.getChildCount());
        expression.getChild(0).setName("XYZ123");
        expression.setExpressionSocketSystemName("IQDE52");
        expression.getChild(1).setName("ZH12");
        expression.setActionSocketSystemName("IQDB554");
        
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
                "jmri.jmrit.logixng.implementation.DefaultFemaleDigitalBooleanActionSocket",
                expression.getChild(1).getClass().getName());
        Assert.assertFalse("expression female socket is not connected",
                expression.getChild(1).isConnected());
        
        // Setup action. This connects the child actions to this action
        expression.setup();
        
        jmri.util.JUnitAppender.assertErrorMessage("cannot load digital expression IQDE52");
        jmri.util.JUnitAppender.assertErrorMessage("cannot load digital boolean action IQDB554");
        
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
                "jmri.jmrit.logixng.implementation.DefaultFemaleDigitalBooleanActionSocket",
                expression.getChild(1).getClass().getName());
        Assert.assertFalse("expression female socket is not connected",
                expression.getChild(1).isConnected());
        
        Assert.assertEquals("expression has 2 female sockets", 2, expression.getChildCount());
    }
    
    @Test
    public void testCtorAndSetup2() {
        Logix expression = new Logix("IQDA321", null);
        Assert.assertNotNull("exists", expression);
        Assert.assertEquals("expression has 2 female sockets", 2, expression.getChildCount());
        expression.getChild(0).setName("XYZ123");
        expression.setExpressionSocketSystemName(null);
        expression.getChild(1).setName("ZH12");
        expression.setActionSocketSystemName(null);
        
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
                "jmri.jmrit.logixng.implementation.DefaultFemaleDigitalBooleanActionSocket",
                expression.getChild(1).getClass().getName());
        Assert.assertFalse("expression female socket is not connected",
                expression.getChild(1).isConnected());
        
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
                "jmri.jmrit.logixng.implementation.DefaultFemaleDigitalBooleanActionSocket",
                expression.getChild(1).getClass().getName());
        Assert.assertFalse("expression female socket is not connected",
                expression.getChild(1).isConnected());
        
        Assert.assertEquals("expression has 2 female sockets", 2, expression.getChildCount());
    }
    
    @Test
    public void testCtorAndSetup3() {
        DigitalExpressionManager m0 = InstanceManager.getDefault(DigitalExpressionManager.class);
        DigitalBooleanActionManager m1 = InstanceManager.getDefault(DigitalBooleanActionManager.class);
        
        m0.registerExpression(new ExpressionMemory("IQDE52", null));
        m1.registerAction(new DigitalBooleanOnChange("IQDB554", null, DigitalBooleanOnChange.Trigger.CHANGE));
        
        Logix expression = new Logix("IQDA321", null);
        Assert.assertNotNull("exists", expression);
        Assert.assertEquals("expression has 2 female sockets", 2, expression.getChildCount());
        expression.getChild(0).setName("XYZ123");
        expression.setExpressionSocketSystemName("IQDE52");
        expression.getChild(1).setName("ZH12");
        expression.setActionSocketSystemName("IQDB554");
        
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
                "jmri.jmrit.logixng.implementation.DefaultFemaleDigitalBooleanActionSocket",
                expression.getChild(1).getClass().getName());
        Assert.assertFalse("expression female socket is not connected",
                expression.getChild(1).isConnected());
        
        // Setup action. This connects the child actions to this action
        expression.setup();
        
        Assert.assertTrue("expression female socket is connected",
                expression.getChild(0).isConnected());
//        Assert.assertEquals("child is correct bean",
//                childSocket0,
//                expression.getChild(0).getConnectedSocket());
        Assert.assertEquals("expression has 2 female sockets", 2, expression.getChildCount());
        
        Assert.assertTrue("expression female socket is connected",
                expression.getChild(1).isConnected());
//        Assert.assertEquals("child is correct bean",
//                childSocket1,
//                expression.getChild(1).getConnectedSocket());
        Assert.assertEquals("expression has 2 female sockets", 2, expression.getChildCount());
        
        Assert.assertEquals("expression has 2 female sockets", 2, expression.getChildCount());
        
        // Try run setup() again. That should not cause any problems.
        expression.setup();
        
        Assert.assertEquals("expression has 2 female sockets", 2, expression.getChildCount());
    }
    
    @Test
    public void testGetChild() {
        Assert.assertTrue("getChildCount() returns 2", 2 == actionLogix.getChildCount());
        
        Assert.assertNotNull("getChild(0) returns a non null value",
                actionLogix.getChild(0));
        Assert.assertNotNull("getChild(1) returns a non null value",
                actionLogix.getChild(1));
        
        boolean hasThrown = false;
        try {
            actionLogix.getChild(2);
        } catch (IllegalArgumentException ex) {
            hasThrown = true;
            Assert.assertEquals("Error message is correct", "index has invalid value: 2", ex.getMessage());
        }
        Assert.assertTrue("Exception is thrown", hasThrown);
    }
    
    @Test
    public void testToString() {
        DigitalActionBean a1 = new Logix("IQDA321", null);
        Assert.assertEquals("strings are equal", "Logix", a1.getShortDescription());
        DigitalActionBean a2 = new Logix("IQDA321", null);
        Assert.assertEquals("strings are equal", "Logix", a2.getLongDescription());
    }
    
    @Test
    public void testCategory() {
        Assert.assertTrue("Category matches", Category.OTHER == _base.getCategory());
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
        conditionalNG = new DefaultConditionalNGScaffold("IQC1", "A conditionalNG");  // NOI18N;
        InstanceManager.getDefault(ConditionalNG_Manager.class).register(conditionalNG);
        conditionalNG.setEnabled(true);
        conditionalNG.setRunDelayed(false);
        logixNG.addConditionalNG(conditionalNG);
        actionLogix = new Logix("IQDA321", null);
        MaleSocket maleSocket =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionLogix);
        conditionalNG.getChild(0).connect(maleSocket);
        
        ExpressionSensor expressionSensor = new ExpressionSensor("IQDE321", null);
        MaleSocket maleSocket2 =
                InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expressionSensor);
        actionLogix.getChild(0).connect(maleSocket2);
        
        DigitalBooleanOnChange actionOnChange = new DigitalBooleanOnChange("IQDB4", null, DigitalBooleanOnChange.Trigger.CHANGE);
        maleSocket2 =
                InstanceManager.getDefault(DigitalBooleanActionManager.class).registerAction(actionOnChange);
        actionLogix.getChild(1).connect(maleSocket2);
        
        _base = actionLogix;
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
