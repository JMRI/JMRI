package jmri.jmrit.logixng.actions;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.implementation.DefaultConditionalNGScaffold;
import jmri.jmrit.logixng.implementation.DefaultSymbolTable;
import jmri.managers.DefaultShutDownManager;
import jmri.util.*;

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
    private MockShutDownManager mockShutDownManager;
    
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
        return String.format("Shutdown computer: Shut down JMRI ::: Use default%n");
    }
    
    @Override
    public String getExpectedPrintedTreeFromRoot() {
        return String.format(
                "LogixNG: A new logix for test%n" +
                "   ConditionalNG: A conditionalNG%n" +
                "      ! A%n" +
                "         Shutdown computer: Shut down JMRI ::: Use default%n");
    }
    
    @Override
    public NamedBean createNewBean(String systemName) {
        return new ShutdownComputer(systemName, null);
    }
    
    @Override
    public boolean addNewSocket() {
        return false;
    }
    
    @Test
    public void testCtor() {
        Assert.assertNotNull("exists", new ShutdownComputer("IQDA321", null));
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
        Assert.assertTrue("Category matches", Category.OTHER == _base.getCategory());
    }
    
    @Test
    public void testIsExternal() {
        Assert.assertTrue("is external", _base.isExternal());
    }
    
    @Test
    @Override
    public void testMaleSocketIsActive() {
        super.testMaleSocketIsActive();
        JUnitAppender.assertErrorMessage("Shutdown failed");
        JUnitAppender.assertErrorMessage("Shutdown failed");
    }
    
    @Test
    @Override
    public void testIsActive() {
        super.testIsActive();
        JUnitAppender.assertErrorMessage("Shutdown failed");
        JUnitAppender.assertErrorMessage("Shutdown failed");
    }
    
    @Test
    public void testExecute() throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        ShutdownComputer action = new ShutdownComputer("IQDA321", null);
        
        action.setOperation(ShutdownComputer.Operation.ShutdownComputer);
        action.execute();
        Assert.assertEquals(MockShutDownManager.Result.SHUTDOWN_OS, mockShutDownManager.result);
        JUnitAppender.assertErrorMessage("Shutdown failed");
        
        action.setOperation(ShutdownComputer.Operation.RebootComputer);
        action.execute();
        Assert.assertEquals(MockShutDownManager.Result.RESTART_OS, mockShutDownManager.result);
        JUnitAppender.assertErrorMessage("Shutdown failed");
        
        action.setOperation(ShutdownComputer.Operation.ShutdownJMRI);
        action.execute();
        Assert.assertEquals(MockShutDownManager.Result.SHUTDOWN_JMRI, mockShutDownManager.result);
        JUnitAppender.assertErrorMessage("Shutdown failed");
        
        action.setOperation(ShutdownComputer.Operation.RebootJMRI);
        action.execute();
        Assert.assertEquals(MockShutDownManager.Result.RESTART_JMRI, mockShutDownManager.result);
        JUnitAppender.assertErrorMessage("Shutdown failed");
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
        JUnitUtil.initLogixNGManager(true);
        
        mockShutDownManager = new MockShutDownManager();
        InstanceManager.setDefault(ShutDownManager.class, mockShutDownManager);
        
        _category = Category.OTHER;
        _isExternal = true;
        
        logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test");  // NOI18N
        conditionalNG = new DefaultConditionalNGScaffold("IQC1", "A conditionalNG");  // NOI18N;
        InstanceManager.getDefault(ConditionalNG_Manager.class).register(conditionalNG);
        conditionalNG.setEnabled(true);
        conditionalNG.setRunDelayed(false);
        logixNG.addConditionalNG(conditionalNG);
        actionShutdownComputer = new ShutdownComputer("IQDA321", null);
        MaleSocket maleSocket =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionShutdownComputer);
        conditionalNG.getChild(0).connect(maleSocket);
        _base = actionShutdownComputer;
        _baseMaleSocket = maleSocket;
        
        logixNG.setParentForAllChildren();
        logixNG.setEnabled(true);
    }
    
    @After
    public void tearDown() {
        JUnitAppender.assertErrorMessage("Shutdown failed");
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.tearDown();
    }
    
    
    
    private static class MockShutDownManager extends DefaultShutDownManager {
        
        public enum Result {
            SHUTDOWN_JMRI,
            SHUTDOWN_OS,
            RESTART_JMRI,
            RESTART_OS,
        }
        
        public Result result = null;
        
        @Override
        public boolean shutdown() {
            result = Result.SHUTDOWN_JMRI;
            return true;
        }

        @Override
        public boolean restart() {
            result = Result.RESTART_JMRI;
            return true;
        }

        @Override
        public boolean restartOS() {
            result = Result.RESTART_OS;
            return true;
        }

        @Override
        public boolean shutdownOS() {
            result = Result.SHUTDOWN_OS;
            return true;
        }
    }
    
    
    /**
     * Exception thrown by restartOS() when simulating shutdown.
     */
    public static class RestartOSException extends RuntimeException {
    }

    /**
     * Exception thrown by restart() when simulating shutdown.
     */
    public static class RestartException extends RuntimeException {
    }

    /**
     * Exception thrown by shutdownOS() when simulating shutdown.
     */
    public static class ShutdownOSException extends RuntimeException {
    }

    /**
     * Exception thrown by shutdown() when simulating shutdown.
     */
    public static class ShutdownException extends RuntimeException {
    }
    
}
