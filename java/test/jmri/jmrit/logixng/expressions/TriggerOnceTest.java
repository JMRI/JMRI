package jmri.jmrit.logixng.expressions;

import java.util.concurrent.atomic.AtomicBoolean;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import jmri.jmrit.logixng.actions.ActionAtomicBoolean;
import jmri.jmrit.logixng.actions.IfThenElse;
import jmri.jmrit.logixng.implementation.DefaultConditionalNGScaffold;

/**
 * Test TriggerOnce
 * 
 * @author Daniel Bergqvist 2018
 */
public class TriggerOnceTest extends AbstractDigitalExpressionTestBase {

    private LogixNG logixNG;
    private ConditionalNG conditionalNG;
    private TriggerOnce expressionTriggerOnce;
    private ActionAtomicBoolean actionAtomicBoolean;
    private AtomicBoolean atomicBoolean;
    
    
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
        DigitalExpressionBean childExpression = new True("IQDE"+Integer.toString(beanID++), null);
        MaleSocket maleSocketChild =
                InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(childExpression);
        return maleSocketChild;
    }
    
    @Override
    public String getExpectedPrintedTree() {
        return String.format(
                "Trigger once ::: Log error%n" +
                "   ? E%n" +
                "      Socket not connected%n");
    }
    
    @Override
    public String getExpectedPrintedTreeFromRoot() {
        return String.format(
                "LogixNG: A new logix for test%n" +
                "   ConditionalNG: A conditionalNG%n" +
                "      ! A%n" +
                "         If Then Else. Execute on change ::: Log error%n" +
                "            ? If%n" +
                "               Trigger once ::: Log error%n" +
                "                  ? E%n" +
                "                     Socket not connected%n" +
                "            ! Then%n" +
                "               Set the atomic boolean to true ::: Log error%n" +
                "            ! Else%n" +
                "               Socket not connected%n");
    }
    
    @Override
    public NamedBean createNewBean(String systemName) {
        return new TriggerOnce(systemName, null);
    }
    
    @Override
    public boolean addNewSocket() {
        return false;
    }
    
    @Test
    public void testCtor()
            throws NamedBean.BadUserNameException,
                    NamedBean.BadSystemNameException,
                    SocketAlreadyConnectedException {
        
        TriggerOnce expression2;
        
        expression2 = new TriggerOnce("IQDE321", null);
        Assert.assertNotNull("object exists", expression2);
        Assert.assertNull("Username matches", expression2.getUserName());
        Assert.assertEquals("String matches", "Trigger once", expression2.getLongDescription());
        
        expression2 = new TriggerOnce("IQDE321", "My sensor");
        Assert.assertNotNull("object exists", expression2);
        Assert.assertEquals("Username matches", "My sensor", expression2.getUserName());
        Assert.assertEquals("String matches", "Trigger once", expression2.getLongDescription());
        
        boolean thrown = false;
        try {
            // Illegal system name
            new TriggerOnce("IQE55:12:XY11", null);
        } catch (IllegalArgumentException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
        
        thrown = false;
        try {
            // Illegal system name
            new TriggerOnce("IQE55:12:XY11", "A name");
        } catch (IllegalArgumentException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
    }
    
    // Test action when at least one child socket is not connected
    @Test
    public void testCtorAndSetup1() {
        TriggerOnce expression = new TriggerOnce("IQDE321", null);
        Assert.assertNotNull("exists", expression);
        Assert.assertEquals("expression has 1 female socket", 1, expression.getChildCount());
        expression.getChild(0).setName("XYZ123");
        expression.setChildSocketSystemName("IQDE52");
        
        Assert.assertEquals("expression female socket name is XYZ123",
                "XYZ123", expression.getChild(0).getName());
        Assert.assertEquals("expression female socket is of correct class",
//                "jmri.jmrit.logixng.implementation.DefaultFemaleGenericExpressionSocket$DigitalSocket",
                "jmri.jmrit.logixng.implementation.DefaultFemaleDigitalExpressionSocket",
                expression.getChild(0).getClass().getName());
        Assert.assertFalse("expression female socket is not connected",
                expression.getChild(0).isConnected());
        
        // Setup action. This connects the child actions to this action
        expression.setup();
        
        jmri.util.JUnitAppender.assertMessage("cannot load digital expression IQDE52");
        
        Assert.assertEquals("expression female socket name is XYZ123",
                "XYZ123", expression.getChild(0).getName());
        Assert.assertEquals("expression female socket is of correct class",
//                "jmri.jmrit.logixng.implementation.DefaultFemaleGenericExpressionSocket$DigitalSocket",
                "jmri.jmrit.logixng.implementation.DefaultFemaleDigitalExpressionSocket",
                expression.getChild(0).getClass().getName());
        Assert.assertFalse("expression female socket is not connected",
                expression.getChild(0).isConnected());
        
        Assert.assertEquals("expression has 1 female socket", 1, expression.getChildCount());
    }
    
    @Test
    public void testCtorAndSetup2() {
        TriggerOnce expression = new TriggerOnce("IQDE321", null);
        Assert.assertNotNull("exists", expression);
        Assert.assertEquals("expression has 1 female socket", 1, expression.getChildCount());
        expression.getChild(0).setName("XYZ123");
        expression.setChildSocketSystemName(null);
        
        Assert.assertEquals("expression female socket name is XYZ123",
                "XYZ123", expression.getChild(0).getName());
        Assert.assertEquals("expression female socket is of correct class",
//                "jmri.jmrit.logixng.implementation.DefaultFemaleGenericExpressionSocket$DigitalSocket",
                "jmri.jmrit.logixng.implementation.DefaultFemaleDigitalExpressionSocket",
                expression.getChild(0).getClass().getName());
        Assert.assertFalse("expression female socket is not connected",
                expression.getChild(0).isConnected());
        
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
        
        Assert.assertEquals("expression has 1 female socket", 1, expression.getChildCount());
    }
    
    @Test
    public void testCtorAndSetup3() {
        DigitalExpressionManager m = InstanceManager.getDefault(DigitalExpressionManager.class);
        
        m.registerExpression(new ExpressionMemory("IQDE52", null));
        
        TriggerOnce expression = new TriggerOnce("IQDE321", null);
        Assert.assertNotNull("exists", expression);
        Assert.assertEquals("expression has 1 female socket", 1, expression.getChildCount());
        expression.getChild(0).setName("XYZ123");
        expression.setChildSocketSystemName("IQDE52");
        
        Assert.assertEquals("expression female socket name is XYZ123",
                "XYZ123", expression.getChild(0).getName());
        Assert.assertEquals("expression female socket is of correct class",
//                "jmri.jmrit.logixng.implementation.DefaultFemaleGenericExpressionSocket$DigitalSocket",
                "jmri.jmrit.logixng.implementation.DefaultFemaleDigitalExpressionSocket",
                expression.getChild(0).getClass().getName());
        Assert.assertFalse("expression female socket is not connected",
                expression.getChild(0).isConnected());
        
        // Setup action. This connects the child actions to this action
        expression.setup();
        
        Assert.assertEquals("expression female socket name is XYZ123",
                "XYZ123", expression.getChild(0).getName());
        Assert.assertTrue("expression female socket is connected",
                expression.getChild(0).isConnected());
//        Assert.assertEquals("child is correct bean",
//                childSocket,
//                expression.getChild(0).getConnectedSocket());
        
        Assert.assertEquals("expression has 1 female socket", 1, expression.getChildCount());
        
        // Try run setup() again. That should not cause any problems.
        expression.setup();
        
        Assert.assertEquals("expression has 1 female socket", 1, expression.getChildCount());
    }
    
    private static int beanID = 901;
    
    @Test
    public void testGetChild() {
        Assert.assertTrue("getChildCount() returns 1", 1 == expressionTriggerOnce.getChildCount());
        
        Assert.assertNotNull("getChild(0) returns a non null value",
                expressionTriggerOnce.getChild(0));
        
        boolean hasThrown = false;
        try {
            expressionTriggerOnce.getChild(1);
        } catch (IllegalArgumentException ex) {
            hasThrown = true;
            Assert.assertEquals("Error message is correct", "index has invalid value: 1", ex.getMessage());
        }
        Assert.assertTrue("Exception is thrown", hasThrown);
    }
    
    @Test
    public void testCategory() {
        Assert.assertTrue("Category matches", Category.OTHER == _base.getCategory());
    }
    
    @Test
    public void testIsExternal() {
        Assert.assertFalse("is external", _base.isExternal());
    }
    
    @Test
    public void testDescription()
            throws NamedBean.BadUserNameException,
                    NamedBean.BadSystemNameException,
                    SocketAlreadyConnectedException {
        DigitalExpressionBean e1 = new TriggerOnce("IQDE321", null);
        Assert.assertTrue("Trigger once".equals(e1.getShortDescription()));
        Assert.assertTrue("Trigger once".equals(e1.getLongDescription()));
    }
    
    @Test
    @Override
    public void testEnableAndEvaluate() {
        // Not implemented.
        // This method is implemented for other digital expressions so no need
        // to add support here. It doesn't need to be tested for every digital
        // expression.
    }
    
    @Test
    @Override
    public void testDebugConfig() {
        // Not implemented.
        // This method is implemented for other digital expressions so no need
        // to add support here. It doesn't need to be tested for every digital
        // expression.
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
        
        _category = Category.OTHER;
        _isExternal = false;
        
        logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test");  // NOI18N
        conditionalNG = new DefaultConditionalNGScaffold("IQC1", "A conditionalNG");  // NOI18N;
        InstanceManager.getDefault(ConditionalNG_Manager.class).register(conditionalNG);
        conditionalNG.setRunDelayed(false);
        conditionalNG.setEnabled(true);
        logixNG.addConditionalNG(conditionalNG);
        IfThenElse ifThenElse = new IfThenElse("IQDA321", null);
        MaleSocket maleSocket =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(ifThenElse);
        conditionalNG.getChild(0).connect(maleSocket);
        
        expressionTriggerOnce = new TriggerOnce("IQDE321", null);
        MaleSocket maleSocket2 =
                InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expressionTriggerOnce);
        ifThenElse.getChild(0).connect(maleSocket2);
        
        _base = expressionTriggerOnce;
        _baseMaleSocket = maleSocket2;
        
        atomicBoolean = new AtomicBoolean(false);
        actionAtomicBoolean = new ActionAtomicBoolean(atomicBoolean, true);
        MaleSocket socketAtomicBoolean = InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionAtomicBoolean);
        ifThenElse.getChild(1).connect(socketAtomicBoolean);
        
        logixNG.setParentForAllChildren();
        logixNG.setEnabled(true);
    }

    @After
    public void tearDown() {
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.tearDown();
    }
    
}
