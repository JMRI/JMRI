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
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Before;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Test ActionTimer
 *
 * @author Daniel Bergqvist 2019
 */
public class ActionTimerTest extends AbstractDigitalActionTestBase {

    private LogixNG _logixNG;
    private ConditionalNG _conditionalNG;
    private ActionTimer _actionTimer;

    @Override
    public ConditionalNG getConditionalNG() {
        return _conditionalNG;
    }

    @Override
    public LogixNG getLogixNG() {
        return _logixNG;
    }

    @Override
    public MaleSocket getConnectableChild() {
        DigitalMany action = new DigitalMany("IQDA999", null);
        MaleSocket maleSocket =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
        return maleSocket;
    }

    @Override
    public String getExpectedPrintedTree() {
        return String.format(
                "Timer ::: Use default%n" +
                "   ? Start%n" +
                "      Socket not connected%n" +
                "   ? Stop%n" +
                "      Socket not connected%n" +
                "   ! A1%n" +
                "      Socket not connected%n");
    }

    @Override
    public String getExpectedPrintedTreeFromRoot() {
        return String.format(
                "LogixNG: A new logix for test%n" +
                "   ConditionalNG: A conditionalNG%n" +
                "      ! A%n" +
                "         Timer ::: Use default%n" +
                "            ? Start%n" +
                "               Socket not connected%n" +
                "            ? Stop%n" +
                "               Socket not connected%n" +
                "            ! A1%n" +
                "               Socket not connected%n");
    }

    @Override
    public NamedBean createNewBean(String systemName) {
        return new ActionTimer(systemName, null);
    }

    @Override
    public boolean addNewSocket() {
        return false;
    }

    @Test
    public void testCtor() {
        ActionTimer t = new ActionTimer("IQDA321", null);
        assertNotNull( t, "exists");
        t = new ActionTimer("IQDA321", null);
        assertNotNull( t, "exists");
    }
/* DISABLE FOR NOW
    @Test
    public void testCtorAndSetup1() {
        ActionTimer action = new ActionTimer("IQDA321", null);
        Assert.assertNotNull("exists", action);
        Assert.assertEquals("action has 1 female socket", 1, action.getChildCount());
        action.getChild(0).setName("ZH12");
        action.setTimerActionSocketSystemName("IQDA554");

        Assert.assertEquals("action female socket name is ZH12",
                "ZH12", action.getChild(0).getName());
        Assert.assertEquals("action female socket is of correct class",
                "jmri.jmrit.logixng.implementation.DefaultFemaleDigitalActionSocket",
                action.getChild(0).getClass().getName());
        Assert.assertFalse("action female socket is not connected",
                action.getChild(0).isConnected());

        // Setup action. This connects the child actions to this action
        action.setup();

        jmri.util.JUnitAppender.assertMessage("cannot load digital action IQDA554");

        Assert.assertEquals("action female socket name is ZH12",
                "ZH12", action.getChild(0).getName());
        Assert.assertEquals("action female socket is of correct class",
                "jmri.jmrit.logixng.implementation.DefaultFemaleDigitalActionSocket",
                action.getChild(0).getClass().getName());
        Assert.assertFalse("action female socket is not connected",
                action.getChild(0).isConnected());

        Assert.assertEquals("action has 1 female socket", 1, action.getChildCount());
    }

    @Test
    public void testCtorAndSetup2() {
        ActionTimer action = new ActionTimer("IQDA321", null);
        Assert.assertNotNull("exists", action);
        Assert.assertEquals("action has 1 female socket", 1, action.getChildCount());
        action.getChild(0).setName("ZH12");
        action.setTimerActionSocketSystemName(null);

        Assert.assertEquals("action female socket name is ZH12",
                "ZH12", action.getChild(0).getName());
        Assert.assertEquals("action female socket is of correct class",
                "jmri.jmrit.logixng.implementation.DefaultFemaleDigitalActionSocket",
                action.getChild(0).getClass().getName());
        Assert.assertFalse("action female socket is not connected",
                action.getChild(0).isConnected());

        // Setup action. This connects the child actions to this action
        action.setup();

        Assert.assertEquals("action female socket name is ZH12",
                "ZH12", action.getChild(0).getName());
        Assert.assertEquals("action female socket is of correct class",
                "jmri.jmrit.logixng.implementation.DefaultFemaleDigitalActionSocket",
                action.getChild(0).getClass().getName());
        Assert.assertFalse("action female socket is not connected",
                action.getChild(0).isConnected());

        Assert.assertEquals("action has 1 female socket", 1, action.getChildCount());
    }

    @Test
    public void testCtorAndSetup3() {
        DigitalActionManager m1 = InstanceManager.getDefault(DigitalActionManager.class);

        MaleSocket childSocket0 = m1.registerAction(new ActionMemory("IQDA554", null));

        ActionTimer action = new ActionTimer("IQDA321", null);
        Assert.assertNotNull("exists", action);
        Assert.assertEquals("action has 1 female socket", 1, action.getChildCount());
        action.getChild(0).setName("ZH12");
        action.setTimerActionSocketSystemName("IQDA554");

        Assert.assertEquals("action female socket name is ZH12",
                "ZH12", action.getChild(0).getName());
        Assert.assertEquals("action female socket is of correct class",
                "jmri.jmrit.logixng.implementation.DefaultFemaleDigitalActionSocket",
                action.getChild(0).getClass().getName());
        Assert.assertFalse("action female socket is not connected",
                action.getChild(0).isConnected());

        // Setup action. This connects the child actions to this action
        action.setup();

        Assert.assertTrue("action female socket is connected",
                action.getChild(0).isConnected());
        Assert.assertEquals("child is correct bean",
                childSocket0,
                action.getChild(0).getConnectedSocket());

        Assert.assertEquals("action has 1 female sockets", 1, action.getChildCount());

        // Try run setup() again. That should not cause any problems.
        action.setup();

        Assert.assertEquals("action has 1 female sockets", 1, action.getChildCount());
    }
*/
    @Test
    public void testGetChild() {
        assertEquals( 3, _actionTimer.getChildCount(), "getChildCount() returns 3");

        assertNotNull( _actionTimer.getChild(0), "getChild(0) returns a non null value");

        IllegalArgumentException ex = assertThrows( IllegalArgumentException.class, () ->
            _actionTimer.getChild(3), "Exception is thrown");
        assertEquals( "index has invalid value: 3", ex.getMessage(), "Error message is correct");
    }

    @Test
    public void testCategory() {
        assertSame( LogixNG_Category.COMMON, _base.getCategory(), "Category matches");
    }

    @Test
    public void testDescription() {
        ActionTimer a1 = new ActionTimer("IQDA321", null);
        assertEquals( "Timer", a1.getShortDescription(), "strings are equal");
        ActionTimer a2 = new ActionTimer("IQDA321", null);
        assertEquals( "Timer", a2.getLongDescription(), "strings are equal");
    }

    @Disabled
    @Test
    public void testTimer() throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException, JmriException {
        _logixNG.setEnabled(false);
        Turnout turnout = InstanceManager.getDefault(TurnoutManager.class).provideTurnout("IT1");
        turnout.setState(Turnout.CLOSED);
        ActionTurnout actionTurnout = new ActionTurnout("IQDA2", null);
        actionTurnout.getSelectNamedBean().setNamedBean(turnout);
        actionTurnout.getSelectEnum().setEnum(ActionTurnout.TurnoutState.Thrown);
        MaleSocket actionTurnoutSocket =
                InstanceManager.getDefault(DigitalActionManager.class)
                        .registerAction(actionTurnout);
        _actionTimer.getActionSocket(0).connect(actionTurnoutSocket);
        _actionTimer.setDelay(0, 100);
        _actionTimer.setStartImmediately(true);
        _actionTimer.setRunContinuously(false);
        _logixNG.setEnabled(true);
        assertEquals( Turnout.CLOSED, turnout.getState(), "turnout is closed");
        _actionTimer.execute();
        assertEquals( Turnout.CLOSED, turnout.getState(), "turnout is closed");
        // The timer should now trig after 100 milliseconds
        JUnitUtil.waitFor(()->{return Turnout.THROWN == turnout.getState();}, "timer has not triggered");
        assertEquals( Turnout.THROWN, turnout.getState(), "turnout is thrown");
        _logixNG.setEnabled(false);
    }
/*
    @Ignore
    @ToDo("The first socket is a digital expression socket, not a digital action socket")
    @Test
    @Override
    public void testPropertyChangeListener1() throws SocketAlreadyConnectedException {
    }

    @Ignore
    @ToDo("The first socket is a digital expression socket, not a digital action socket")
    @Test
    @Override
    public void testPropertyChangeListener2() throws SocketAlreadyConnectedException {
    }

    @Ignore
    @ToDo("The first socket is a digital expression socket, not a digital action socket")
    @Test
    @Override
    public void testPropertyChangeListener3() throws SocketAlreadyConnectedException {
    }

    @Ignore
    @ToDo("The first socket is a digital expression socket, not a digital action socket")
    @Test
    @Override
    public void testPropertyChangeListener4() throws SocketAlreadyConnectedException {
    }
*/
    @Test
    @Override
    public void testIsActive() {
        _logixNG.setEnabled(true);
        super.testIsActive();
    }

    @Test
    @Override
    public void testMaleSocketIsActive() {
        _logixNG.setEnabled(true);
        super.testMaleSocketIsActive();
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
        JUnitUtil.initLogixNGManager();

        _category = LogixNG_Category.OTHER;
        _isExternal = false;

        _logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test");  // NOI18N
        _conditionalNG = new DefaultConditionalNGScaffold("IQC1", "A conditionalNG");  // NOI18N;
        InstanceManager.getDefault(ConditionalNG_Manager.class).register(_conditionalNG);
        _conditionalNG.setEnabled(true);
        _conditionalNG.setRunDelayed(false);
        _logixNG.addConditionalNG(_conditionalNG);
        _actionTimer = new ActionTimer("IQDA321", null);
        MaleSocket maleSocket =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(_actionTimer);
        _conditionalNG.getChild(0).connect(maleSocket);
        _base = _actionTimer;
        _baseMaleSocket = maleSocket;

        assertTrue( _logixNG.setParentForAllChildren(new ArrayList<>()));
        _logixNG.activate();
        _logixNG.setEnabled(false);
    }

    @After
    @AfterEach
    public void tearDown() {
        _logixNG.setEnabled(false);
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
        _category = null;
        _logixNG = null;
        _conditionalNG = null;
        _actionTimer = null;
        _base = null;
        _baseMaleSocket = null;
    }



/*
    public class MyTimer extends Timer {

        private TimerTask _task;
//        private long _delay;

        @Override
        public void schedule(TimerTask task, long delay) {
            if (_task != null) {
                throw new RuntimeException("Only one task at the time can be executed");
            }
            _task = task;
//            _delay = delay;
        }

        @Override
        public void schedule(TimerTask task, Date time) {
            throw new UnsupportedOperationException("this method is not supported");
        }

        @Override
        public void schedule(TimerTask task, long delay, long period) {
            throw new UnsupportedOperationException("this method is not supported");
        }

        @Override
        public void schedule(TimerTask task, Date firstTime, long period) {
            throw new UnsupportedOperationException("this method is not supported");
        }

        @Override
        public void scheduleAtFixedRate(TimerTask task, long delay, long period) {
            throw new UnsupportedOperationException("this method is not supported");
        }

        @Override
        public void scheduleAtFixedRate(TimerTask task, Date firstTime, long period) {
            throw new UnsupportedOperationException("this method is not supported");
        }

        @Override
        public void cancel() {
            _task = null;
//            _delay = 0;
        }

        @Override
        public int purge() {
            return 0;
        }

        public void triggerTimer() {
            _task.run();
        }

    }
*/
}
