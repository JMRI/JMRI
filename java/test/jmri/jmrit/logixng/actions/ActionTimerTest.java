package jmri.jmrit.logixng.actions;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.implementation.DefaultConditionalNGScaffold;
import jmri.jmrit.logixng.implementation.DefaultSymbolTable;
import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.ToDo;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test ActionTimer
 * 
 * @author Daniel Bergqvist 2019
 */
public class ActionTimerTest extends AbstractDigitalActionTestBase {

    LogixNG _logixNG;
    ConditionalNG _conditionalNG;
    ActionTimer _actionTimer;
    
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
        Assert.assertNotNull("exists",t);
        t = new ActionTimer("IQDA321", null);
        Assert.assertNotNull("exists",t);
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
        Assert.assertTrue("getChildCount() returns 3", 3 == _actionTimer.getChildCount());
        
        Assert.assertNotNull("getChild(0) returns a non null value",
                _actionTimer.getChild(0));
        
        boolean hasThrown = false;
        try {
            _actionTimer.getChild(3);
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
    public void testIsExternal() {
        Assert.assertFalse("is external", _base.isExternal());
    }
    
    @Test
    public void testDescription() {
        ActionTimer a1 = new ActionTimer("IQDA321", null);
        Assert.assertEquals("strings are equal", "Timer", a1.getShortDescription());
        ActionTimer a2 = new ActionTimer("IQDA321", null);
        Assert.assertEquals("strings are equal", "Timer", a2.getLongDescription());
    }
    
    @Ignore
    @Test
    public void testTimer() throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException, JmriException {
        _logixNG.setEnabled(false);
        Turnout turnout = InstanceManager.getDefault(TurnoutManager.class).provideTurnout("IT1");
        turnout.setState(Turnout.CLOSED);
        ActionTurnout actionTurnout = new ActionTurnout("IQDA2", null);
        actionTurnout.setTurnout(turnout);
        actionTurnout.setBeanState(ActionTurnout.TurnoutState.Thrown);
        MaleSocket actionTurnoutSocket =
                InstanceManager.getDefault(DigitalActionManager.class)
                        .registerAction(actionTurnout);
        _actionTimer.getActionSocket(0).connect(actionTurnoutSocket);
        _actionTimer.setDelay(0, 100);
        _actionTimer.setStartImmediately(true);
        _actionTimer.setRunContinuously(false);
        _logixNG.setEnabled(true);
        Assert.assertTrue("turnout is closed", Turnout.CLOSED == turnout.getState());
        _actionTimer.execute();
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
        _actionTimer = new ActionTimer("IQDA321", null);
        MaleSocket maleSocket =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(_actionTimer);
        _conditionalNG.getChild(0).connect(maleSocket);
        _base = _actionTimer;
        _baseMaleSocket = maleSocket;
        
        _logixNG.setParentForAllChildren();
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
