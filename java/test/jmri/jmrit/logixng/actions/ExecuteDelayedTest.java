package jmri.jmrit.logixng.actions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;

import jmri.util.TimerUnit;
import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.implementation.DefaultConditionalNGScaffold;
import jmri.jmrit.logixng.util.parser.ParserException;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Before;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Test ExecuteDelayed
 *
 * @author Daniel Bergqvist 2021
 */
public class ExecuteDelayedTest extends AbstractDigitalActionTestBase {

    private LogixNG _logixNG;
    private ConditionalNG _conditionalNG;
    private ExecuteDelayed _executeDelayed;

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
                "Execute A after 0 milliseconds. Ignore on repeat ::: Use default%n" +
                "   ! A%n" +
                "      Socket not connected%n");
    }

    @Override
    public String getExpectedPrintedTreeFromRoot() {
        return String.format(
                "LogixNG: A new logix for test%n" +
                "   ConditionalNG: A conditionalNG%n" +
                "      ! A%n" +
                "         Execute A after 0 milliseconds. Ignore on repeat ::: Use default%n" +
                "            ! A%n" +
                "               Socket not connected%n");
    }

    @Override
    public NamedBean createNewBean(String systemName) {
        return new ExecuteDelayed(systemName, null);
    }

    @Override
    public boolean addNewSocket() {
        return false;
    }

    @Test
    public void testGetChild() {
        assertEquals( 1, _executeDelayed.getChildCount(), "getChildCount() returns 1");

        assertNotNull( _executeDelayed.getChild(0),
                "getChild(0) returns a non null value");

        IllegalArgumentException ex = assertThrows( IllegalArgumentException.class, () ->
            _executeDelayed.getChild(3), "Exception is thrown");
        assertEquals( "index has invalid value: 3", ex.getMessage(), "Error message is correct");
    }

    @Test
    public void testCategory() {
        assertSame( LogixNG_Category.COMMON, _base.getCategory(), "Category matches");
    }

    @Test
    public void testDescription() throws ParserException {
        ExecuteDelayed a1 = new ExecuteDelayed("IQDA321", null);
        assertEquals( "Execute delayed", a1.getShortDescription(), "strings are equal");
        ExecuteDelayed a2 = new ExecuteDelayed("IQDA321", null);
        assertEquals( "Execute A after 0 milliseconds. Ignore on repeat",
                a2.getLongDescription(), "strings are equal");
        a2.setDelay(26);
        a2.setUnit(TimerUnit.Minutes);
        a2.setResetIfAlreadyStarted(true);
        a2.setUseIndividualTimers(false);
        assertEquals( "Execute A after 26 minutes. Reset on repeat",
                a2.getLongDescription(), "strings are equal");
        a2.setDelayAddressing(NamedBeanAddressing.Direct);
        a2.setDelay(4);
        a2.setUnit(TimerUnit.Hours);
        a2.setResetIfAlreadyStarted(false);
        a2.setUseIndividualTimers(true);
        assertEquals( "Execute A after 4 hours. Ignore on repeat. Use individual timers",
                a2.getLongDescription(), "strings are equal");
        a2.setDelayAddressing(NamedBeanAddressing.Direct);
        a2.setDelay(4);
        a2.setUnit(TimerUnit.Hours);
        a2.setResetIfAlreadyStarted(true);
        a2.setUseIndividualTimers(true);
        assertEquals( "Execute A after 4 hours. Reset on repeat. Use individual timers",
                a2.getLongDescription(), "strings are equal");
        a2.setDelayAddressing(NamedBeanAddressing.Formula);
        a2.setDelayFormula("delay*2");
        a2.setUnit(TimerUnit.Hours);
        a2.setResetIfAlreadyStarted(false);
        a2.setUseIndividualTimers(true);
        assertEquals( "Execute A after by formula delay*2 in unit Hours. Ignore on repeat. Use individual timers",
                a2.getLongDescription(), "strings are equal");
        a2.setDelayAddressing(NamedBeanAddressing.LocalVariable);
        a2.setDelayLocalVariable("myVar");
        a2.setUnit(TimerUnit.Hours);
        a2.setResetIfAlreadyStarted(false);
        a2.setUseIndividualTimers(true);
        assertEquals( "Execute A after by local variable myVar in unit Hours. Ignore on repeat. Use individual timers",
                a2.getLongDescription(), "strings are equal");
        a2.setDelayAddressing(NamedBeanAddressing.Reference);
        a2.setDelayReference("{IM1}");
        a2.setUnit(TimerUnit.Hours);
        a2.setResetIfAlreadyStarted(false);
        a2.setUseIndividualTimers(true);
        assertEquals( "Execute A after by reference {IM1} in unit Hours. Ignore on repeat. Use individual timers",
                a2.getLongDescription(), "strings are equal");
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
        _executeDelayed.getSocket().connect(actionTurnoutSocket);
        _executeDelayed.setDelay(100);
        _logixNG.setEnabled(true);
        assertEquals( Turnout.CLOSED, turnout.getState(), "turnout is closed");
        _executeDelayed.execute();
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
    @Disabled
    @Test
    @Override
    public void testIsActive() {
        _logixNG.setEnabled(true);
        super.testIsActive();
//        JUnitAppender.suppressErrorMessage("getConditionalNG() return null");
    }

    @Disabled
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
        _executeDelayed = new ExecuteDelayed("IQDA321", null);
        MaleSocket maleSocket =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(_executeDelayed);
        _conditionalNG.getChild(0).connect(maleSocket);
        _base = _executeDelayed;
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
        _executeDelayed = null;
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
