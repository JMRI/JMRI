package jmri.jmrit.logixng.actions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.implementation.DefaultConditionalNGScaffold;
import jmri.managers.DefaultShutDownManager;
import jmri.util.*;

import org.junit.After;
import org.junit.Before;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
        return String.format("Shutdown JMRI/computer: Shut down JMRI ::: Use default%n");
    }

    @Override
    public String getExpectedPrintedTreeFromRoot() {
        return String.format(
                "LogixNG: A new logix for test%n" +
                "   ConditionalNG: A conditionalNG%n" +
                "      ! A%n" +
                "         Shutdown JMRI/computer: Shut down JMRI ::: Use default%n");
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
        assertNotNull( new ShutdownComputer("IQDA321", null), "exists");
    }

    @Test
    public void testGetChild() {
        assertEquals( 0, actionShutdownComputer.getChildCount(), "getChildCount() returns 0");

        UnsupportedOperationException ex = assertThrows( UnsupportedOperationException.class, () ->
            actionShutdownComputer.getChild(0), "Exception is thrown");
        assertEquals( "Not supported.", ex.getMessage(), "Error message is correct");
    }

    @Test
    public void testCategory() {
        assertSame( LogixNG_Category.OTHER, _base.getCategory(), "Category matches");
    }

    @Test
    @Override
    public void testMaleSocketIsActive() {
        super.testMaleSocketIsActive();
    }

    @Test
    @Override
    public void testIsActive() {
        super.testIsActive();
    }

    @Test
    public void testExecute() throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException, JmriException {
        ShutdownComputer action = new ShutdownComputer("IQDA321", null);

        action.getSelectEnum().setEnum(ShutdownComputer.Operation.ShutdownComputer);
        action.execute();
        assertEquals(MockShutDownManager.Result.SHUTDOWN_OS, mockShutDownManager.result);

        action.getSelectEnum().setEnum(ShutdownComputer.Operation.RebootComputer);
        action.execute();
        assertEquals(MockShutDownManager.Result.RESTART_OS, mockShutDownManager.result);

        action.getSelectEnum().setEnum(ShutdownComputer.Operation.ShutdownJMRI);
        action.execute();
        assertEquals(MockShutDownManager.Result.SHUTDOWN_JMRI, mockShutDownManager.result);

        action.getSelectEnum().setEnum(ShutdownComputer.Operation.RebootJMRI);
        action.execute();
        assertEquals(MockShutDownManager.Result.RESTART_JMRI, mockShutDownManager.result);
    }

    @Before
    @BeforeEach
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

        _category = LogixNG_Category.OTHER;
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

        assertTrue( logixNG.setParentForAllChildren(new ArrayList<>()));
        logixNG.activate();
        logixNG.setEnabled(true);
    }

    @After
    @AfterEach
    public void tearDown() {
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
        public void shutdown() {
            result = Result.SHUTDOWN_JMRI;
        }

        @Override
        public void restart() {
            result = Result.RESTART_JMRI;
        }

        @Override
        public void restartOS() {
            result = Result.RESTART_OS;
        }

        @Override
        public void shutdownOS() {
            result = Result.SHUTDOWN_OS;
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
