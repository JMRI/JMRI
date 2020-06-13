package jmri.jmrit.logixng.digital.actions;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.NamedBean;
import jmri.Turnout;
import jmri.TurnoutManager;
import jmri.jmrit.logixng.Category;
import jmri.jmrit.logixng.ConditionalNG;
import jmri.jmrit.logixng.ConditionalNG_Manager;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.DigitalExpressionBean;
import jmri.jmrit.logixng.DigitalExpressionManager;
import jmri.jmrit.logixng.LogixNG;
import jmri.jmrit.logixng.LogixNG_Manager;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.SocketAlreadyConnectedException;
import jmri.jmrit.logixng.digital.expressions.True;
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
    public void testToString() {
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
