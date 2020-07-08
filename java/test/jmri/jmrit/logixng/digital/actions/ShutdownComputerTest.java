package jmri.jmrit.logixng.digital.actions;

import java.lang.reflect.Field;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.util.*;

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
    private String lastExecutedCommand;
    
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
    
    @Override
    public boolean addNewSocket() {
        return false;
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
    
    @Test
    public void testSetSeconds() {
        ShutdownComputer action = new ShutdownComputer("IQDA321", null, 52);
        Assert.assertEquals("Correct number of seconds", 52, action.getSeconds());
        action.setSeconds(7);
        Assert.assertEquals("Correct number of seconds", 7, action.getSeconds());
        boolean hasThrown = false;
        try {
            action.setSeconds(-12);
        } catch (IllegalArgumentException e) {
            hasThrown = true;
            Assert.assertEquals("error message is correct", "seconds must not be negative", e.getMessage());
        }
        Assert.assertTrue("Exception is thrown", hasThrown);
        Assert.assertEquals("Correct number of seconds", 7, action.getSeconds());
        action.setSeconds(0);
        Assert.assertEquals("Correct number of seconds", 0, action.getSeconds());
        
        // This shouldn't do anything but we call setup() for coverage
        action.setup();
    }
    
    @Test
    public void testExecute() throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        ShutdownComputer action = new ShutdownComputer("IQDA321", null, 52);
        
        // During this test, we change SystemType.type, but we reset it in tearDown()
        
        Field privateField = SystemType.class.
                getDeclaredField("type");
        
        privateField.setAccessible(true);
        
        privateField.set(SystemType.class, SystemType.WINDOWS);
        action.execute();
        Assert.assertEquals("Shutdown command is correct", "shutdown.exe", lastExecutedCommand);
        JUnitAppender.assertErrorMessage("Shutdown failed");
        
        privateField.set(SystemType.class, SystemType.MACOSX);
        action.execute();
        Assert.assertEquals("Shutdown command is correct", "shutdown", lastExecutedCommand);
        JUnitAppender.assertErrorMessage("Shutdown failed");
        
        privateField.set(SystemType.class, SystemType.LINUX);
        action.execute();
        Assert.assertEquals("Shutdown command is correct", "shutdown", lastExecutedCommand);
        JUnitAppender.assertErrorMessage("Shutdown failed");
        
        privateField.set(SystemType.class, SystemType.UNIX);
        action.execute();
        Assert.assertEquals("Shutdown command is correct", "shutdown", lastExecutedCommand);
        JUnitAppender.assertErrorMessage("Shutdown failed");
        
        boolean hasThrown = false;
        try {
            privateField.set(SystemType.class, SystemType.MACCLASSIC);
            action.execute();
        } catch (UnsupportedOperationException e) {
            hasThrown = true;
            Assert.assertTrue("Error message is correct", e.getMessage().startsWith("Unknown OS: "));
        }
        Assert.assertTrue("Exception is thrown", hasThrown);
        
        hasThrown = false;
        try {
            privateField.set(SystemType.class, SystemType.OS2);
            action.execute();
        } catch (UnsupportedOperationException e) {
            hasThrown = true;
            Assert.assertTrue("Error message is correct", e.getMessage().startsWith("Unknown OS: "));
        }
        Assert.assertTrue("Exception is thrown", hasThrown);
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
        
        InstanceManager.getDefault(LogixNGPreferences.class).setLimitRootActions(false);
        
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
    
    private void resetSystemType() throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        // Ensure we reset SystemType
        Field privateField = SystemType.class.
                getDeclaredField("isSet");
        
        privateField.setAccessible(true);
        
        // Save original value
        boolean origValue = (Boolean) privateField.get(SystemType.class);
        // Do assert to check that the code works
        Assert.assertTrue("SystemType.isSet is true", origValue);
        
        privateField.set(SystemType.class, false);
        origValue = (Boolean) privateField.get(SystemType.class);
        // Do assert to check that the code works
        Assert.assertFalse("SystemType.isSet is false", origValue);
    }
    
    @After
    public void tearDown() throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        // We change SystemType.type in the method testExecute(). But we
        // cannot reset SystemType in that method since if an assert fails,
        // or if an exception is thrown and not catched, we will leave that
        // method without SystemType being reset. So we must reset SystemType
        // here.
        resetSystemType();
        
        // Clear security mananger
        System.setSecurityManager(null);
        JUnitUtil.tearDown();
    }
    
    
    private class MySecurityManager extends SecurityManager {
        
        @Override
        public void checkExec(String cmd) {
            lastExecutedCommand = cmd;
            throw new SecurityException("exec is not allowed during test of ShutdownComputer");
        }
        
        @Override
        public void checkPermission(java.security.Permission perm) {
            // We don't want any checks, except checkExec()
        }
        
    }
    
}
