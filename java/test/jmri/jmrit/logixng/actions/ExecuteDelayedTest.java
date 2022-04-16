package jmri.jmrit.logixng.actions;

import java.util.ArrayList;

import jmri.jmrit.logixng.util.TimerUnit;
import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.implementation.DefaultConditionalNGScaffold;
import jmri.jmrit.logixng.util.parser.ParserException;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test ExecuteDelayed
 *
 * @author Daniel Bergqvist 2021
 */
public class ExecuteDelayedTest extends AbstractDigitalActionTestBase {

    LogixNG _logixNG;
    ConditionalNG _conditionalNG;
    ExecuteDelayed _executeDelayed;

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
        Assert.assertTrue("getChildCount() returns 1", 1 == _executeDelayed.getChildCount());

        Assert.assertNotNull("getChild(0) returns a non null value",
                _executeDelayed.getChild(0));

        boolean hasThrown = false;
        try {
            _executeDelayed.getChild(3);
        } catch (IllegalArgumentException ex) {
            hasThrown = true;
            Assert.assertEquals("Error message is correct", "index has invalid value: 3", ex.getMessage());
        }
        Assert.assertTrue("Exception is thrown", hasThrown);
    }

    @Test
    public void testCategory() {
        Assert.assertTrue("Category matches", Category.COMMON == _base.getCategory());
    }

    @Test
    public void testDescription() throws ParserException {
        ExecuteDelayed a1 = new ExecuteDelayed("IQDA321", null);
        Assert.assertEquals("strings are equal", "Execute delayed", a1.getShortDescription());
        ExecuteDelayed a2 = new ExecuteDelayed("IQDA321", null);
        Assert.assertEquals("strings are equal", "Execute A after 0 milliseconds. Ignore on repeat", a2.getLongDescription());
        a2.setDelay(26);
        a2.setUnit(TimerUnit.Minutes);
        a2.setResetIfAlreadyStarted(true);
        a2.setUseIndividualTimers(false);
        Assert.assertEquals("strings are equal", "Execute A after 26 minutes. Reset on repeat", a2.getLongDescription());
        a2.setDelayAddressing(NamedBeanAddressing.Direct);
        a2.setDelay(4);
        a2.setUnit(TimerUnit.Hours);
        a2.setResetIfAlreadyStarted(false);
        a2.setUseIndividualTimers(true);
        Assert.assertEquals("strings are equal", "Execute A after 4 hours. Ignore on repeat. Use individual timers", a2.getLongDescription());
        a2.setDelayAddressing(NamedBeanAddressing.Direct);
        a2.setDelay(4);
        a2.setUnit(TimerUnit.Hours);
        a2.setResetIfAlreadyStarted(true);
        a2.setUseIndividualTimers(true);
        Assert.assertEquals("strings are equal", "Execute A after 4 hours. Reset on repeat. Use individual timers", a2.getLongDescription());
        a2.setDelayAddressing(NamedBeanAddressing.Formula);
        a2.setDelayFormula("delay*2");
        a2.setUnit(TimerUnit.Hours);
        a2.setResetIfAlreadyStarted(false);
        a2.setUseIndividualTimers(true);
        Assert.assertEquals("strings are equal", "Execute A after by formula delay*2 in unit Hours. Ignore on repeat. Use individual timers", a2.getLongDescription());
        a2.setDelayAddressing(NamedBeanAddressing.LocalVariable);
        a2.setDelayLocalVariable("myVar");
        a2.setUnit(TimerUnit.Hours);
        a2.setResetIfAlreadyStarted(false);
        a2.setUseIndividualTimers(true);
        Assert.assertEquals("strings are equal", "Execute A after by local variable myVar in unit Hours. Ignore on repeat. Use individual timers", a2.getLongDescription());
        a2.setDelayAddressing(NamedBeanAddressing.Reference);
        a2.setDelayReference("{IM1}");
        a2.setUnit(TimerUnit.Hours);
        a2.setResetIfAlreadyStarted(false);
        a2.setUseIndividualTimers(true);
        Assert.assertEquals("strings are equal", "Execute A after by reference {IM1} in unit Hours. Ignore on repeat. Use individual timers", a2.getLongDescription());
    }

    @Ignore
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
        Assert.assertTrue("turnout is closed", Turnout.CLOSED == turnout.getState());
        _executeDelayed.execute();
        Assert.assertTrue("turnout is closed", Turnout.CLOSED == turnout.getState());
        // The timer should now trig after 100 milliseconds
        JUnitUtil.waitFor(()->{return Turnout.THROWN == turnout.getState();}, "timer has not triggered");
        Assert.assertTrue("turnout is thrown", Turnout.THROWN == turnout.getState());
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
    @Ignore
    @Test
    @Override
    public void testIsActive() {
        _logixNG.setEnabled(true);
        super.testIsActive();
//        JUnitAppender.suppressErrorMessage("getConditionalNG() return null");
    }

    @Ignore
    @Test
    @Override
    public void testMaleSocketIsActive() {
        _logixNG.setEnabled(true);
        super.testMaleSocketIsActive();
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

        _category = Category.OTHER;
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

        if (! _logixNG.setParentForAllChildren(new ArrayList<>())) throw new RuntimeException();
        _logixNG.setEnabled(false);
    }

    @After
    public void tearDown() {
        _logixNG.setEnabled(false);
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
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
