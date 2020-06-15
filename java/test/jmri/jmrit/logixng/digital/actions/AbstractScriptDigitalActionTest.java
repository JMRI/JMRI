package jmri.jmrit.logixng.digital.actions;

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
 * Test AbstractScriptDigitalAction
 * 
 * @author Daniel Bergqvist 2018
 */
// public class AbstractScriptDigitalActionTest extends AbstractDigitalActionTestBase {
public class AbstractScriptDigitalActionTest {

    private LogixNG logixNG;
    private ConditionalNG conditionalNG;
    private AbstractScriptDigitalAction actionAbstractScriptDigitalAction;
/*    
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
        return new AbstractScriptDigitalAction(null) {
            @Override
            public void execute() throws JmriException {
                throw new UnsupportedOperationException("Not supported");
            }
        };
    }
    
    @Override
    public boolean addNewSocket() {
        return false;
    }
/*    
    @Test
    public void testCtor() {
        Assert.assertNotNull("exists", new AbstractScriptDigitalAction(null){
            @Override
            public void execute() throws JmriException {
                throw new UnsupportedOperationException("Not supported");
            }
        });
    }
    
    @Test
    public void testGetChild() {
        Assert.assertTrue("getChildCount() returns 0", 0 == actionAbstractScriptDigitalAction.getChildCount());
        
        boolean hasThrown = false;
        try {
            actionAbstractScriptDigitalAction.getChild(0);
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
/*    
    @Test
    @Override
    // The only purpose of override this method is to catch the error message
    // The reason for this is that we have a security manager installed to block
    // the ShusaatdownComputer action to shut down the computer during this test.
    public void testMaleSocketIsActive() {
        super.testMaleSocketIsActive();
        JUnitAppender.suppressMessage(Level.ERROR, "exec is not allowed during test of ShutdownCompuddter");
    }
    
    @Test
    @Override
    // The only purpose of override this method is to catch the error message.
    // The reason for this is that we have a security manager installed to block
    // the SddhutdownComputer action to shut down the computer during this test.
    public void testIsActive() {
        super.testIsActive();
        JUnitAppender.suppressMessage(Level.ERROR, "exec is not allowed during test of ShutdownComputerrr");
    }
    
    @Test
    public void testSetSeconds() {
        ShutdownComputdder action = new SrrhutdownComputer("IQDA321", null, 52);
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
        SffhutdownComputer action = new SrrhutdownComputer("IQDA321", null, 52);
        
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
*/    
    @Test
    public void testTest() {
        
    }
    
    // The minimal setup for log4J
    @Before
    public void setUp() throws SocketAlreadyConnectedException {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        
//        JUnitUtil.initInternalSensorManager();
//        JUnitUtil.initInternalTurnoutManager();
        
////////        _category = Category.EXRAVAGANZA;
////////        _isExternal = true;
        
        logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test");  // NOI18N
        conditionalNG = InstanceManager.getDefault(ConditionalNG_Manager.class)
                .createConditionalNG("A conditionalNG");  // NOI18N
        conditionalNG.setEnabled(true);
        conditionalNG.setRunOnGUIDelayed(false);
        logixNG.addConditionalNG(conditionalNG);
/**/        
        actionAbstractScriptDigitalAction = new AbstractScriptDigitalAction(null) {
            @Override
            public void execute() throws JmriException {
                throw new UnsupportedOperationException("Not supported");
            }
        };
        MaleSocket maleSocket =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionAbstractScriptDigitalAction);
        
        conditionalNG.getChild(0).connect(maleSocket);
//        _base = actionAbstractScriptDigitalAction;
//        _baseMaleSocket = maleSocket;
        logixNG.setParentForAllChildren();
        logixNG.setEnabled(true);
//////        logixNG.activateLogixNG();
        
//        JUnitAppender.assertErrorMessageStartsWith("Shutdown failed");
    }
    
    @After
    public void tearDown() throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        // Clear security mananger
        System.setSecurityManager(null);
        JUnitUtil.tearDown();
    }
    
}
