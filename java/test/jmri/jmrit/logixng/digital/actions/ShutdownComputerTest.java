package jmri.jmrit.logixng.digital.actions;

import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.jmrit.logixng.Category;
import jmri.jmrit.logixng.ConditionalNG;
import jmri.jmrit.logixng.ConditionalNG_Manager;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.LogixNG;
import jmri.jmrit.logixng.LogixNG_Manager;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.SocketAlreadyConnectedException;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;
import org.apache.log4j.Level;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test ShutdownComputer
 * 
 * @author Daniel Bergqvist 2018
 */
public class ShutdownComputerTest extends AbstractDigitalActionTestBase {

    private LogixNG logixNG;
    private ConditionalNG conditionalNG;
    private ShutdownComputer actionShutdownComputer;
    
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
        return String.format("Shutdown computer after 0 seconds%n");
    }
    
    @Override
    public String getExpectedPrintedTreeFromRoot() {
        return String.format(
                "LogixNG: A new logix for test%n" +
                "   ConditionalNG: A conditionalNG%n" +
                "      ! %n" +
                "         Shutdown computer after 0 seconds%n");
    }
    
    @Override
    public NamedBean createNewBean(String systemName) {
        return new ShutdownComputer(systemName, null, 0);
    }
    
    @Test
    public void testCtor() {
        Assert.assertNotNull("exists", new ShutdownComputer("IQDA321", null, 0));
    }
    
    @Test
    public void testGetChild() {
        Assert.assertTrue("getChildCount() returns 0", 0 == actionShutdownComputer.getChildCount());
        
        boolean hasThrown = false;
        try {
            actionShutdownComputer.getChild(0);
        } catch (UnsupportedOperationException ex) {
            hasThrown = true;
            Assert.assertEquals("Error message is correct", "Not supported.", ex.getMessage());
        }
        Assert.assertTrue("Exception is thrown", hasThrown);
    }
    
    @Test
    public void testCategory() {
        Assert.assertTrue("Category matches", Category.EXRAVAGANZA == _base.getCategory());
    }
    
    @Test
    public void testIsExternal() {
        Assert.assertTrue("is external", _base.isExternal());
    }
    
    @Test
    @Override
    // The only purpose of override this method is to catch the error message
    // The reason for this is that we have a security manager installed to block
    // the ShutdownComputer action to shut down the computer during this test.
    public void testMaleSocketIsActive() {
        super.testMaleSocketIsActive();
        JUnitAppender.suppressMessage(Level.ERROR, "exec is not allowed during test of ShutdownComputer");
    }
    
    @Test
    @Override
    // The only purpose of override this method is to catch the error message.
    // The reason for this is that we have a security manager installed to block
    // the ShutdownComputer action to shut down the computer during this test.
    public void testIsActive() {
        super.testIsActive();
        JUnitAppender.suppressMessage(Level.ERROR, "exec is not allowed during test of ShutdownComputer");
    }
    
    // The minimal setup for log4J
    @Before
    public void setUp() throws SocketAlreadyConnectedException {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        
        // Set a secority manager since we don't want this test to shut down
        // the computer!
        System.setSecurityManager(new MySecurityManager());
        
        _category = Category.EXRAVAGANZA;
        _isExternal = true;
        
        logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test");  // NOI18N
        conditionalNG = InstanceManager.getDefault(ConditionalNG_Manager.class)
                .createConditionalNG("A conditionalNG");  // NOI18N
        conditionalNG.setEnabled(true);
        conditionalNG.setRunOnGUIDelayed(false);
        logixNG.addConditionalNG(conditionalNG);
        actionShutdownComputer = new ShutdownComputer("IQDA321", null, 0);
        MaleSocket maleSocket =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionShutdownComputer);
        conditionalNG.getChild(0).connect(maleSocket);
        _base = actionShutdownComputer;
        _baseMaleSocket = maleSocket;
        
        logixNG.setParentForAllChildren();
        logixNG.setEnabled(true);
        logixNG.activateLogixNG();
        JUnitAppender.assertErrorMessageStartsWith("Shutdown failed");
    }

    @After
    public void tearDown() {
        // Clear security mananger
        System.setSecurityManager(null);
        JUnitUtil.tearDown();
    }
    
    
    private static class MySecurityManager extends SecurityManager {
        
        @Override
        public void checkExec(String cmd) {
            throw new SecurityException("exec is not allowed during test of ShutdownComputer");
        }
        
        @Override
        public void checkPermission(java.security.Permission perm) {
            // We don't want any checks, except checkExec()
        }
        
    }
    
}
