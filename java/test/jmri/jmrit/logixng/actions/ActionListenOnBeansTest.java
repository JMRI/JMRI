package jmri.jmrit.logixng.actions;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.implementation.DefaultConditionalNGScaffold;
import jmri.jmrit.logixng.implementation.DefaultSymbolTable;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test ActionListenOnBeans
 * 
 * @author Daniel Bergqvist 2019
 */
public class ActionListenOnBeansTest extends AbstractDigitalActionTestBase {

    private LogixNG logixNG;
    private ConditionalNG conditionalNG;
    private ActionListenOnBeans actionListenOnBeans;
    
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
        return String.format("Listen on beans ::: Log error%n");
    }
    
    @Override
    public String getExpectedPrintedTreeFromRoot() {
        return String.format(
                "LogixNG: A logixNG%n" +
                "   ConditionalNG: A conditionalNG%n" +
                "      ! A%n" +
                "         Listen on beans ::: Log error%n");
    }
    
    @Override
    public NamedBean createNewBean(String systemName) {
        return new ActionListenOnBeans(systemName, null);
    }
    
    @Override
    public boolean addNewSocket() {
        return false;
    }
    
    @Test
    public void testCtor() {
        ActionListenOnBeans t = new ActionListenOnBeans("IQDA1", null);
        Assert.assertNotNull("not null", t);
    }
    
    @Test
    public void testGetChild() {
        Assert.assertTrue("getChildCount() returns 0", 0 == actionListenOnBeans.getChildCount());
        
        boolean hasThrown = false;
        try {
            actionListenOnBeans.getChild(0);
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
        Assert.assertTrue("is external", _base.isExternal());
    }
    
    @Test
    public void testShortDescription() {
        Assert.assertEquals("String matches", "Listen on beans", _base.getShortDescription());
    }
    
    @Test
    public void testLongDescription() {
        ActionListenOnBeans a1 = new ActionListenOnBeans("IQDA321", null);
        Assert.assertEquals("strings are equal", "Listen on beans", a1.getShortDescription());
        ActionListenOnBeans a2 = new ActionListenOnBeans("IQDA321", null);
        Assert.assertEquals("strings are equal", "Listen on beans", a2.getLongDescription());
    }
    
    // The minimal setup for log4J
    @Before
    public void setUp() throws SocketAlreadyConnectedException {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initLogixNGManager();
        
        _category = Category.ITEM;
        _isExternal = true;
        
        logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A logixNG");
        conditionalNG = new DefaultConditionalNGScaffold("IQC1", "A conditionalNG");  // NOI18N;
        InstanceManager.getDefault(ConditionalNG_Manager.class).register(conditionalNG);
        logixNG.addConditionalNG(conditionalNG);
        conditionalNG.setRunDelayed(false);
        conditionalNG.setEnabled(true);
        actionListenOnBeans = new ActionListenOnBeans(InstanceManager.getDefault(DigitalActionManager.class).getAutoSystemName(), null);
        MaleSocket socket = InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionListenOnBeans);
        conditionalNG.getChild(0).connect(socket);
        
        _base = actionListenOnBeans;
        _baseMaleSocket = socket;
        
        logixNG.setParentForAllChildren();
        logixNG.setEnabled(true);
    }

    @After
    public void tearDown() {
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.tearDown();
    }

}
