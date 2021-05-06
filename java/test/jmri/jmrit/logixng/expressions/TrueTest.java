package jmri.jmrit.logixng.expressions;

import java.util.concurrent.atomic.AtomicBoolean;

import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.jmrit.logixng.Category;
import jmri.jmrit.logixng.ConditionalNG;
import jmri.jmrit.logixng.ConditionalNG_Manager;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import jmri.jmrit.logixng.DigitalExpressionBean;
import jmri.jmrit.logixng.DigitalExpressionManager;
import jmri.jmrit.logixng.LogixNG;
import jmri.jmrit.logixng.LogixNG_Manager;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.SocketAlreadyConnectedException;
import jmri.jmrit.logixng.actions.ActionAtomicBoolean;
import jmri.jmrit.logixng.actions.IfThenElse;
import jmri.jmrit.logixng.implementation.DefaultConditionalNGScaffold;

/**
 * Test True
 * 
 * @author Daniel Bergqvist 2018
 */
public class TrueTest extends AbstractDigitalExpressionTestBase {

    private LogixNG logixNG;
    private ConditionalNG conditionalNG;
    private True expressionTrue;
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
        return null;
    }
    
    @Override
    public String getExpectedPrintedTree() {
        return String.format("Always true ::: Use default%n");
    }
    
    @Override
    public String getExpectedPrintedTreeFromRoot() {
        return String.format(
                "LogixNG: A new logix for test%n" +
                "   ConditionalNG: A conditionalNG%n" +
                "      ! A%n" +
                "         If Then Else. Execute on change ::: Use default%n" +
                "            ? If%n" +
                "               Always true ::: Use default%n" +
                "            ! Then%n" +
                "               Set the atomic boolean to true ::: Use default%n" +
                "            ! Else%n" +
                "               Socket not connected%n");
    }
    
    @Override
    public NamedBean createNewBean(String systemName) {
        return new True(systemName, null);
    }
    
    @Override
    public boolean addNewSocket() {
        return false;
    }
    
    @Test
    public void testCtor() {
        True expression2;
        
        expression2 = new True("IQDE321", null);
        Assert.assertNotNull("object exists", expression2);
        Assert.assertNull("Username matches", expression2.getUserName());
        Assert.assertEquals("String matches", "Always true", expression2.getLongDescription());
        
        expression2 = new True("IQDE321", "My expression");
        Assert.assertNotNull("object exists", expression2);
        Assert.assertEquals("Username matches", "My expression", expression2.getUserName());
        Assert.assertEquals("String matches", "Always true", expression2.getLongDescription());
        
        boolean thrown = false;
        try {
            // Illegal system name
            new True("IQE55:12:XY11", null);
        } catch (IllegalArgumentException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
        
        thrown = false;
        try {
            // Illegal system name
            new True("IQE55:12:XY11", "A name");
        } catch (IllegalArgumentException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
    }
    
    @Test
    public void testGetChild() {
        Assert.assertTrue("getChildCount() returns 0", 0 == expressionTrue.getChildCount());
        
        boolean hasThrown = false;
        try {
            expressionTrue.getChild(0);
        } catch (UnsupportedOperationException ex) {
            hasThrown = true;
            Assert.assertEquals("Error message is correct", "Not supported.", ex.getMessage());
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
    public void testDescription() {
        DigitalExpressionBean e1 = new True("IQDE321", null);
        Assert.assertTrue("Always true".equals(e1.getShortDescription()));
        Assert.assertTrue("Always true".equals(e1.getLongDescription()));
    }
    
    @Test
    public void testExpression() throws Exception {
        DigitalExpressionBean t = new True("IQDE321", null);
        Assert.assertTrue("Expression is true",t.evaluate());
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
        conditionalNG.setRunDelayed(false);
        conditionalNG.setEnabled(true);
        logixNG.addConditionalNG(conditionalNG);
        IfThenElse ifThenElse = new IfThenElse("IQDA321", null);
        MaleSocket maleSocket =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(ifThenElse);
        conditionalNG.getChild(0).connect(maleSocket);
        
        expressionTrue = new True("IQDE321", null);
        MaleSocket maleSocket2 =
                InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expressionTrue);
        ifThenElse.getChild(0).connect(maleSocket2);
        
        _base = expressionTrue;
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
