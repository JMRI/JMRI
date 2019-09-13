package jmri.jmrit.logixng;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Locale;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.NamedBean;
import jmri.jmrit.logixng.Base.Lock;
import jmri.jmrit.logixng.implementation.DefaultConditionalNG;
import jmri.jmrit.logixng.digital.actions.ActionTurnout;
import jmri.jmrit.logixng.digital.actions.Many;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test LogixNG
 * 
 * @author Daniel Bergqvist 2018
 */
public class ConditionalNGTest {
    
    @Test
    public void testGetNewObjectBasedOnTemplate() {
        LogixNG logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test");  // NOI18N
        DefaultConditionalNG conditionalNG_1 = new DefaultConditionalNG(logixNG.getSystemName()+":1", null);
        logixNG.addConditionalNG(conditionalNG_1);
        DefaultConditionalNG conditionalNG_2 = new DefaultConditionalNG(logixNG.getSystemName()+":2", null, conditionalNG_1);
        Assert.assertNotNull("conditionalNG_2 is not null", conditionalNG_2);
    }
    
    @Test
    public void testGetBeanType() {
        LogixNG logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test");  // NOI18N
        DefaultConditionalNG conditionalNG = new DefaultConditionalNG(logixNG.getSystemName()+":1", null);
        Assert.assertEquals("beanType is correct", "ConditionalNG", conditionalNG.getBeanType());
    }
    
    @Test
    public void testGetParent() {
        LogixNG logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test");  // NOI18N
        ConditionalNG conditionalNG_1 = new DefaultConditionalNG(logixNG.getSystemName()+":1", null);
        Assert.assertTrue("getParent() returns correct value", null == conditionalNG_1.getParent());
        logixNG.addConditionalNG(conditionalNG_1);
        Assert.assertTrue("getParent() returns correct value", logixNG == conditionalNG_1.getParent());
    }
    
    @Test
    public void testGetLogixNG() {
        LogixNG logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test");  // NOI18N
        ConditionalNG conditionalNG_1 = new DefaultConditionalNG(logixNG.getSystemName()+":1", null);
        Assert.assertTrue("getLogixNG() returns correct value", null == conditionalNG_1.getLogixNG());
        logixNG.addConditionalNG(conditionalNG_1);
        Assert.assertTrue("getLogixNG() returns correct value", logixNG == conditionalNG_1.getLogixNG());
    }
    
    @Test
    public void testGetRoot() {
        LogixNG logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test");  // NOI18N
        ConditionalNG conditionalNG_1 = new DefaultConditionalNG(logixNG.getSystemName()+":1", null);
        Assert.assertTrue("getRoot() returns correct value", conditionalNG_1 == conditionalNG_1.getRoot());
        logixNG.addConditionalNG(conditionalNG_1);
        Assert.assertTrue("getRoot() returns correct value", logixNG == conditionalNG_1.getRoot());
    }
    
    @Test
    public void testState() throws JmriException {
        LogixNG logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test");  // NOI18N
        Assert.assertTrue("getState() returns UNKNOWN", logixNG.getState() == LogixNG.UNKNOWN);
        JUnitAppender.assertWarnMessage("Unexpected call to getState in DefaultLogixNG.");
        logixNG.setState(LogixNG.INCONSISTENT);
        JUnitAppender.assertWarnMessage("Unexpected call to setState in DefaultLogixNG.");
        Assert.assertTrue("getState() returns UNKNOWN", logixNG.getState() == LogixNG.UNKNOWN);
        JUnitAppender.assertWarnMessage("Unexpected call to getState in DefaultLogixNG.");
    }
    
    @Test
    public void testConnectDisconnect() throws SocketAlreadyConnectedException {
        LogixNG logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test");  // NOI18N
        DefaultConditionalNG conditionalNG = new DefaultConditionalNG(logixNG.getSystemName()+":1", null);
        logixNG.addConditionalNG(conditionalNG);
        Assert.assertEquals("socket name is correct", null, conditionalNG.getSocketSystemName());
        InstanceManager.getDefault(LogixNG_Manager.class).setupInitialConditionalNGTree(conditionalNG);
        MaleSocket many = conditionalNG.getChild(0).getConnectedSocket();
        Assert.assertTrue("description is correct", "Many".equals(many.getLongDescription()));
        Assert.assertEquals("socket name is correct", many.getSystemName(), conditionalNG.getSocketSystemName());
        conditionalNG.getChild(0).disconnect();
        Assert.assertEquals("socket name is correct", null, conditionalNG.getSocketSystemName());
    }
    
    @Test
    public void testSetSocketName() throws SocketAlreadyConnectedException {
        LogixNG logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test");  // NOI18N
        DefaultConditionalNG conditionalNG = new DefaultConditionalNG(logixNG.getSystemName()+":1", null);
        logixNG.addConditionalNG(conditionalNG);
        
        DigitalActionManager digitalActionManager =
                InstanceManager.getDefault(DigitalActionManager.class);

        MaleDigitalActionSocket actionManySocket =
                InstanceManager.getDefault(DigitalActionManager.class)
                        .registerAction(new Many(digitalActionManager.getNewSystemName(), null));
        
        
        Assert.assertEquals("socket name is correct", null, conditionalNG.getSocketSystemName());
        conditionalNG.setSocketSystemName("Abc");
        Assert.assertEquals("socket name is correct", "Abc", conditionalNG.getSocketSystemName());
        conditionalNG.setSocketSystemName("Def");
        Assert.assertEquals("socket name is correct", "Def", conditionalNG.getSocketSystemName());
        conditionalNG.getFemaleSocket().connect(actionManySocket);
        Assert.assertEquals("socket name is correct",
                actionManySocket.getSystemName(),
                conditionalNG.getSocketSystemName());
        conditionalNG.setSocketSystemName(null);
        Assert.assertEquals("socket name is correct", null, conditionalNG.getSocketSystemName());
    }
    
    @Test
    public void testShortDescription() {
        LogixNG logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test");  // NOI18N
        ConditionalNG conditionalNG_1 = new DefaultConditionalNG(logixNG.getSystemName()+":1", null);
        Assert.assertEquals("getLongDescription() returns correct value",
                "ConditionalNG", conditionalNG_1.getLongDescription(Locale.US));
    }
    
    @Test
    public void testLongDescription() {
        LogixNG logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test");  // NOI18N
        ConditionalNG conditionalNG_1 = new DefaultConditionalNG(logixNG.getSystemName()+":1", null);
        Assert.assertEquals("getLongDescription() returns correct value",
                "ConditionalNG", conditionalNG_1.getLongDescription(Locale.US));
    }
    
    @Ignore("Not implemented yet")
    @Test
    public void testGetChild() {
        LogixNG logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test");  // NOI18N
        boolean hasThrown = false;
        try {
            logixNG.getChild(0);
        } catch (UnsupportedOperationException e) {
            hasThrown = true;
        }
        Assert.assertTrue("exception thrown", hasThrown);
    }
    
    @Ignore("Not implemented yet")
    @Test
    public void testGetChildCount() {
        LogixNG logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test");  // NOI18N
        boolean hasThrown = false;
        try {
            logixNG.getChildCount();
        } catch (UnsupportedOperationException e) {
            hasThrown = true;
        }
        Assert.assertTrue("exception thrown", hasThrown);
    }
    
    @Ignore("Not implemented yet")
    @Test
    public void testGetCategory() {
        LogixNG logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test");  // NOI18N
        boolean hasThrown = false;
        try {
            logixNG.getCategory();
        } catch (UnsupportedOperationException e) {
            hasThrown = true;
        }
        Assert.assertTrue("exception thrown", hasThrown);
    }
    
    @Ignore("Not implemented yet")
    @Test
    public void testIsExternal() {
        LogixNG logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test");  // NOI18N
        boolean hasThrown = false;
        try {
            logixNG.isExternal();
        } catch (UnsupportedOperationException e) {
            hasThrown = true;
        }
        Assert.assertTrue("exception thrown", hasThrown);
    }
    
    @Ignore("Not implemented yet")
    @Test
    public void testGetLock() {
        LogixNG logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test");  // NOI18N
        boolean hasThrown = false;
        try {
            logixNG.getLock();
        } catch (UnsupportedOperationException e) {
            hasThrown = true;
        }
        Assert.assertTrue("exception thrown", hasThrown);
    }
    
    @Ignore("Not implemented yet")
    @Test
    public void testSetLock() {
        LogixNG logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test");  // NOI18N
        boolean hasThrown = false;
        try {
            logixNG.setLock(Lock.NONE);
        } catch (UnsupportedOperationException e) {
            hasThrown = true;
        }
        Assert.assertTrue("exception thrown", hasThrown);
    }
    
    @Ignore("Not implemented yet")
    @Test
    public void testActivateLogixNG() {
        LogixNG logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test");  // NOI18N
        MyConditionalNG conditionalNG_1 = new MyConditionalNG(logixNG.getSystemName()+":1", null);
        logixNG.addConditionalNG(conditionalNG_1);
        conditionalNG_1.setEnabled(false);
        MyConditionalNG conditionalNG_2 = new MyConditionalNG(logixNG.getSystemName()+":2", null);
        logixNG.addConditionalNG(conditionalNG_2);
        conditionalNG_1.setEnabled(true);
        MyConditionalNG conditionalNG_3 = new MyConditionalNG(logixNG.getSystemName()+":3", null);
        logixNG.addConditionalNG(conditionalNG_3);
        conditionalNG_1.setEnabled(false);
        
        Assert.assertFalse("listeners for conditionalNG_1 are not registered", conditionalNG_1.listenersAreRegistered);
        Assert.assertFalse("listeners for conditionalNG_2 are not registered", conditionalNG_2.listenersAreRegistered);
        Assert.assertFalse("listeners for conditionalNG_3 are not registered", conditionalNG_3.listenersAreRegistered);
        
        logixNG.activateLogixNG();
        Assert.assertTrue("listeners for conditionalNG_1 are registered", conditionalNG_1.listenersAreRegistered);
        Assert.assertTrue("listeners for conditionalNG_2 are registered", conditionalNG_2.listenersAreRegistered);
        Assert.assertTrue("listeners for conditionalNG_3 are registered", conditionalNG_3.listenersAreRegistered);
        
        // Activate LogixNG multiple times should not be a problem
        logixNG.activateLogixNG();
        Assert.assertTrue("listeners for conditionalNG_1 are registered", conditionalNG_1.listenersAreRegistered);
        Assert.assertTrue("listeners for conditionalNG_2 are registered", conditionalNG_2.listenersAreRegistered);
        Assert.assertTrue("listeners for conditionalNG_3 are registered", conditionalNG_3.listenersAreRegistered);
        
        logixNG.deActivateLogixNG();
        Assert.assertFalse("listeners for conditionalNG_1 are not registered", conditionalNG_1.listenersAreRegistered);
        Assert.assertFalse("listeners for conditionalNG_2 are not registered", conditionalNG_2.listenersAreRegistered);
        Assert.assertFalse("listeners for conditionalNG_3 are not registered", conditionalNG_3.listenersAreRegistered);
        
        // DeActivate LogixNG multiple times should not be a problem
        logixNG.deActivateLogixNG();
        Assert.assertFalse("listeners for conditionalNG_1 are not registered", conditionalNG_1.listenersAreRegistered);
        Assert.assertFalse("listeners for conditionalNG_2 are not registered", conditionalNG_2.listenersAreRegistered);
        Assert.assertFalse("listeners for conditionalNG_3 are not registered", conditionalNG_3.listenersAreRegistered);
    }
    
    @Ignore("Not implemented yet")
    @Test
    public void testPrintTree() {
        final String newLine = System.lineSeparator();
        StringBuilder expectedResult = new StringBuilder();
        expectedResult
                .append("LogixNG: A new logix for test").append(newLine)
                .append("...ConditionalNG").append(newLine)
                .append("......! ").append(newLine)
                .append(".........Many").append(newLine)
                .append("............! A1").append(newLine)
                .append("...............Hold anything").append(newLine)
                .append("..................? A1").append(newLine)
                .append("..................! A1").append(newLine)
                .append("..................! A1").append(newLine)
                .append("............! A2").append(newLine)
                .append("...............If E then A1 else A2").append(newLine)
                .append("..................? E").append(newLine)
                .append("..................! A1").append(newLine)
                .append("..................! A2").append(newLine)
                .append("............! A3").append(newLine);
        
        StringWriter writer = new StringWriter();
        LogixNG logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test");  // NOI18N
        ConditionalNG conditionalNG = new DefaultConditionalNG(logixNG.getSystemName()+":1", null);
        logixNG.addConditionalNG(conditionalNG);
        InstanceManager.getDefault(LogixNG_Manager.class).setupInitialConditionalNGTree(conditionalNG);
        logixNG.printTree(new PrintWriter(writer), "...");
        String resultStr = writer.toString();
        
        Assert.assertEquals("Strings matches", expectedResult.toString(), resultStr);
    }
    
    @Ignore("Not implemented yet")
    @Test
    public void testSetup() throws SocketAlreadyConnectedException {
        
        LogixNG logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test");  // NOI18N
        DefaultConditionalNG conditionalNG = new DefaultConditionalNG(logixNG.getSystemName()+":1", null);
        logixNG.addConditionalNG(conditionalNG);
        
        String systemName = InstanceManager.getDefault(DigitalActionManager.class).getNewSystemName();
        DigitalActionBean action = new ActionTurnout(systemName, "An action for test");  // NOI18N
        MaleSocket digitalActionBean = InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
        
        conditionalNG.setSocketSystemName(systemName);
        logixNG.setup();
        
        logixNG.setParentForAllChildren();
        
//        System.err.format("%s%n", conditionalNG.getChild(0).getConnectedSocket().getLongDescription());
        Assert.assertTrue("conditionalng child is correct",
                "Set turnout '' to Thrown"
                        .equals(conditionalNG.getChild(0).getConnectedSocket().getLongDescription()));
        Assert.assertTrue("conditionalng is correct", conditionalNG == digitalActionBean.getConditionalNG());
        Assert.assertTrue("logixlng is correct", logixNG == digitalActionBean.getLogixNG());
    }
    
    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initLogixNGManager();
        JUnitUtil.initDigitalExpressionManager();
        JUnitUtil.initDigitalActionManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    
    
    private class MyConditionalNG extends DefaultConditionalNG {

        private boolean listenersAreRegistered;
        
        public MyConditionalNG(String sys, String user) throws NamedBean.BadUserNameException, NamedBean.BadSystemNameException {
            super(sys, user);
        }
        
        /** {@inheritDoc} */
        @Override
        public void registerListenersForThisClass() {
            listenersAreRegistered = true;
        }

        /** {@inheritDoc} */
        @Override
        public void unregisterListenersForThisClass() {
            listenersAreRegistered = false;
        }
    }
    
}
