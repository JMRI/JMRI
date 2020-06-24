package jmri.jmrit.logixng.digital.actions;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test ActionTimer
 * 
 * @author Daniel Bergqvist 2019
 */
public class ActionTimerTest extends AbstractDigitalActionTestBase {

    LogixNG logixNG;
    ConditionalNG conditionalNG;
    ActionTimer actionTimer;
    
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
        Many action = new Many("IQDA999", null);
        MaleSocket maleSocket =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
        return maleSocket;
    }
    
    @Override
    public String getExpectedPrintedTree() {
        return String.format(
                "Execute A after 0 milliseconds%n" +
                "   ! A%n" +
                "      Socket not connected%n");
    }
    
    @Override
    public String getExpectedPrintedTreeFromRoot() {
        return String.format(
                "LogixNG: A new logix for test%n" +
                "   ConditionalNG: A conditionalNG%n" +
                "      ! %n" +
                "         Execute A after 0 milliseconds%n" +
                "            ! A%n" +
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
                "jmri.jmrit.logixng.digital.implementation.DefaultFemaleDigitalActionSocket",
                action.getChild(0).getClass().getName());
        Assert.assertFalse("action female socket is not connected",
                action.getChild(0).isConnected());
        
        // Setup action. This connects the child actions to this action
        action.setup();
        
        jmri.util.JUnitAppender.assertMessage("cannot load digital action IQDA554");
        
        Assert.assertEquals("action female socket name is ZH12",
                "ZH12", action.getChild(0).getName());
        Assert.assertEquals("action female socket is of correct class",
                "jmri.jmrit.logixng.digital.implementation.DefaultFemaleDigitalActionSocket",
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
                "jmri.jmrit.logixng.digital.implementation.DefaultFemaleDigitalActionSocket",
                action.getChild(0).getClass().getName());
        Assert.assertFalse("action female socket is not connected",
                action.getChild(0).isConnected());
        
        // Setup action. This connects the child actions to this action
        action.setup();
        
        Assert.assertEquals("action female socket name is ZH12",
                "ZH12", action.getChild(0).getName());
        Assert.assertEquals("action female socket is of correct class",
                "jmri.jmrit.logixng.digital.implementation.DefaultFemaleDigitalActionSocket",
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
                "jmri.jmrit.logixng.digital.implementation.DefaultFemaleDigitalActionSocket",
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
    
    @Test
    public void testGetChild() {
        Assert.assertTrue("getChildCount() returns 1", 1 == actionTimer.getChildCount());
        
        Assert.assertNotNull("getChild(0) returns a non null value",
                actionTimer.getChild(0));
        
        boolean hasThrown = false;
        try {
            actionTimer.getChild(1);
        } catch (IllegalArgumentException ex) {
            hasThrown = true;
            Assert.assertEquals("Error message is correct", "index has invalid value: 1", ex.getMessage());
        }
        Assert.assertTrue("Exception is thrown", hasThrown);
    }
    
    @Test
    public void testCategory() {
        Assert.assertTrue("Category matches", Category.OTHER == _base.getCategory());
    }
    
    @Test
    public void testIsExternal() {
        Assert.assertFalse("is external", _base.isExternal());
    }
    
    @Test
    public void testDescription() {
        ActionTimer a1 = new ActionTimer("IQDA321", null);
        Assert.assertEquals("strings are equal", "Execute after delay", a1.getShortDescription());
        ActionTimer a2 = new ActionTimer("IQDA321", null);
        Assert.assertEquals("strings are equal", "Execute A after 0 milliseconds", a2.getLongDescription());
    }
    
    @Test
    public void testTimer() throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException, JmriException {
        ActionTimer t = new ActionTimer("IQDA1", null);
        Assert.assertNotNull("exists",t);
        Turnout turnout = InstanceManager.getDefault(TurnoutManager.class).provideTurnout("IT1");
        turnout.setState(Turnout.CLOSED);
        ActionTurnout actionTurnout = new ActionTurnout("IQDA2", null);
        actionTurnout.setTurnout(turnout);
        actionTurnout.setTurnoutState(ActionTurnout.TurnoutState.THROWN);
        MaleSocket actionTurnoutSocket =
                InstanceManager.getDefault(DigitalActionManager.class)
                        .registerAction(actionTurnout);
        t.getThenActionSocket().connect(actionTurnoutSocket);
        t.setDelay(100);
        Assert.assertTrue("turnout is closed", Turnout.CLOSED == turnout.getState());
        t.execute();
        Assert.assertTrue("turnout is closed", Turnout.CLOSED == turnout.getState());
        // The timer should now trig after 100 milliseconds
        JUnitUtil.waitFor(()->{return Turnout.THROWN == turnout.getState();}, "timer has not triggered");
        Assert.assertTrue("turnout is thrown", Turnout.THROWN == turnout.getState());
    }
    
    // The minimal setup for log4J
    @Before
    public void setUp() throws SocketAlreadyConnectedException {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        
        _category = Category.OTHER;
        _isExternal = false;
        
        logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test");  // NOI18N
        conditionalNG = InstanceManager.getDefault(ConditionalNG_Manager.class)
                .createConditionalNG("A conditionalNG");  // NOI18N
        conditionalNG.setEnabled(true);
        conditionalNG.setRunOnGUIDelayed(false);
        logixNG.addConditionalNG(conditionalNG);
        actionTimer = new ActionTimer("IQDA321", null);
        MaleSocket maleSocket =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionTimer);
        conditionalNG.getChild(0).connect(maleSocket);
        _base = actionTimer;
        _baseMaleSocket = maleSocket;
        
        logixNG.setParentForAllChildren();
        logixNG.setEnabled(true);
        logixNG.activateLogixNG();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
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
