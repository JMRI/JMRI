package jmri.jmrit.logixng.expressions;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import jmri.InstanceManager;
import jmri.NamedBean;
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
 * Test Not
 * 
 * @author Daniel Bergqvist 2021
 */
public class NotTest extends AbstractDigitalExpressionTestBase {

    private LogixNG logixNG;
    private ConditionalNG conditionalNG;
    private Not expressionNot;
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
    
    private static int beanID = 901;
    
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
                "Not ::: Use default%n" +
                "   ? E%n" +
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
                "               Not ::: Use default%n" +
                "                  ? E%n" +
                "                     Socket not connected%n" +
                "            ! Then%n" +
                "               Set the atomic boolean to true ::: Use default%n" +
                "            ! Else%n" +
                "               Socket not connected%n");
    }
    
    @Override
    public NamedBean createNewBean(String systemName) {
        return new Not(systemName, null);
    }
    
    @Override
    public boolean addNewSocket() {
        return false;
    }
    
    @Test
    public void testCtor() {
        Not expression2;
        
        expression2 = new Not("IQDE321", null);
        Assert.assertNotNull("object exists", expression2);
        Assert.assertNull("Username matches", expression2.getUserName());
        Assert.assertEquals("String matches", "Not", expression2.getLongDescription());
        
        expression2 = new Not("IQDE321", "My expression");
        Assert.assertNotNull("object exists", expression2);
        Assert.assertEquals("Username matches", "My expression", expression2.getUserName());
        Assert.assertEquals("String matches", "Not", expression2.getLongDescription());
        
        boolean thrown = false;
        try {
            // Illegal system name
            new Not("IQE55:12:XY11", null);
        } catch (IllegalArgumentException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
        
        thrown = false;
        try {
            // Illegal system name
            new Not("IQE55:12:XY11", "A name");
        } catch (IllegalArgumentException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
    }
    
    @Test
    public void testCtorAndSetup1() {
        Not expression = new Not("IQDE321", null);
        Assert.assertNotNull("exists", expression);
        Assert.assertEquals("expression has 1 female socket", 1, expression.getChildCount());
        expression.getChild(0).setName("XYZ123");
        expression.setSocketSystemName("IQDE52");
        
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
        
        jmri.util.JUnitAppender.assertErrorMessage("cannot load digital expression IQDE52");
        
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
        Not expression = new Not("IQDE321", null);
        Assert.assertNotNull("exists", expression);
        Assert.assertEquals("expression has 1 female socket", 1, expression.getChildCount());
        expression.getChild(0).setName("XYZ123");
        expression.setSocketSystemName(null);
        
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
        m.registerExpression(new ExpressionMemory("IQDE554", null));
        
        Not expression = new Not("IQDE321", null);
        Assert.assertNotNull("exists", expression);
        Assert.assertEquals("expression has 1 female socket", 1, expression.getChildCount());
        expression.getChild(0).setName("XYZ123");
        expression.setSocketSystemName("IQDE52");
        
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
        
        Assert.assertTrue("expression female socket is connected",
                expression.getChild(0).isConnected());
//        Assert.assertEquals("child is correct bean",
//                childSocket0,
//                expression.getChild(0).getConnectedSocket());
        Assert.assertEquals("expression has 1 female socket", 1, expression.getChildCount());
        
        // Try run setup() again. That should not cause any problems.
        expression.setup();
        
        Assert.assertEquals("expression has 1 female socket", 1, expression.getChildCount());
    }
    
    @Test
    public void testGetChild() {
        Assert.assertTrue("getChildCount() returns 1", 1 == expressionNot.getChildCount());
        
        Assert.assertNotNull("getChild(0) returns a non null value",
                expressionNot.getChild(0));
        
        boolean hasThrown = false;
        try {
            expressionNot.getChild(1);
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
    public void testDescription() {
        Not e1 = new Not("IQDE321", null);
        Assert.assertTrue("Not".equals(e1.getShortDescription()));
        Assert.assertTrue("Not".equals(e1.getLongDescription()));
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
        
        expressionNot = new Not("IQDE321", null);
        MaleSocket maleSocket2 =
                InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expressionNot);
        ifThenElse.getChild(0).connect(maleSocket2);
        
        _base = expressionNot;
        _baseMaleSocket = maleSocket2;
        
        atomicBoolean = new AtomicBoolean(false);
        actionAtomicBoolean = new ActionAtomicBoolean(atomicBoolean, true);
        MaleSocket socketAtomicBoolean = InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionAtomicBoolean);
        ifThenElse.getChild(1).connect(socketAtomicBoolean);
        
        if (! logixNG.setParentForAllChildren(new ArrayList<>())) throw new RuntimeException();
        logixNG.setEnabled(true);
    }

    @After
    public void tearDown() {
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.tearDown();
    }
    
}
