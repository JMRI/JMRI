package jmri.jmrit.logixng.digital.expressions;

import java.util.concurrent.atomic.AtomicBoolean;
import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.jmrit.logixng.Category;
import jmri.jmrit.logixng.ConditionalNG;
import jmri.jmrit.logixng.ConditionalNG_Manager;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.util.JUnitUtil;
import jmri.jmrit.logixng.DigitalExpressionManager;
import jmri.jmrit.logixng.LogixNG;
import jmri.jmrit.logixng.LogixNG_Manager;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.SocketAlreadyConnectedException;
import jmri.jmrit.logixng.digital.actions.ActionAtomicBoolean;
import jmri.jmrit.logixng.digital.actions.IfThenElse;
import jmri.jmrit.logixng.digital.expressions.Timer.TimerType;
import jmri.util.JUnitAppender;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test Timer
 * 
 * @author Daniel Bergqvist 2018
 */
public class TimerTest extends AbstractDigitalExpressionTestBase {

    private LogixNG logixNG;
    private ConditionalNG conditionalNG;
    private Timer expressionTimer;
    private ActionAtomicBoolean actionAtomicBoolean;
    private AtomicBoolean atomicBoolean;
    
    
    @Override
    public ConditionalNG getConditionalNG() {
        return conditionalNG;
    }
    
    @Override
    public LogixNG getLogixNG() {
        return logixNG;
    }
    
    @Override
    public String getExpectedPrintedTree() {
        return String.format("One shot timer: Wait 0 seconds and trigger once.%n");
    }
    
    @Override
    public String getExpectedPrintedTreeFromRoot() {
        return String.format(
                "LogixNG: A new logix for test%n" +
                "   ConditionalNG: A conditionalNG%n" +
                "      ! %n" +
                "         If E then A1 else A2%n" +
                "            ? E%n" +
                "               One shot timer: Wait 0 seconds and trigger once.%n" +
                "            ! A1%n" +
                "               Set the atomic boolean to true%n" +
                "            ! A2%n" +
                "               Socket not connected%n");
    }
    
    @Override
    public NamedBean createNewBean(String systemName) {
        return new Timer(systemName, null);
    }
    
    @Test
    public void testCtor() {
        Timer expression2;
        
        expression2 = new Timer("IQDE321", null);
        Assert.assertNotNull("object exists", expression2);
        Assert.assertNull("Username matches", expression2.getUserName());
        Assert.assertEquals("String matches", "One shot timer: Wait 0 seconds and trigger once.", expression2.getLongDescription());
        
        expression2 = new Timer("IQDE321", "My expression");
        Assert.assertNotNull("object exists", expression2);
        Assert.assertEquals("Username matches", "My expression", expression2.getUserName());
        Assert.assertEquals("String matches", "One shot timer: Wait 0 seconds and trigger once.", expression2.getLongDescription());
        
        boolean thrown = false;
        try {
            // Illegal system name
            new Timer("IQE55:12:XY11", null);
        } catch (IllegalArgumentException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
        
        thrown = false;
        try {
            // Illegal system name
            new Timer("IQE55:12:XY11", "A name");
        } catch (IllegalArgumentException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
    }
    
    @Test
    public void testGetChild() {
        Assert.assertTrue("getChildCount() returns 0", 0 == expressionTimer.getChildCount());
        
        boolean hasThrown = false;
        try {
            expressionTimer.getChild(0);
        } catch (UnsupportedOperationException ex) {
            hasThrown = true;
            Assert.assertEquals("Error message is correct", "Not supported.", ex.getMessage());
        }
        Assert.assertTrue("Exception is thrown", hasThrown);
    }
    
    @Test
    public void testDescription() {
        Timer e1 = new Timer("IQDE321", null);
        Assert.assertTrue("Timer".equals(e1.getShortDescription()));
        
        e1.setTimerType(TimerType.WAIT_ONCE_TRIG_ONCE);
        e1.setTimerDelay(10, 0);
        Assert.assertEquals("One shot timer: Wait 10 seconds and trigger once.", e1.getLongDescription());
        
        e1.setTimerType(TimerType.WAIT_ONCE_TRIG_UNTIL_RESET);
        e1.setTimerDelay(20, 0);
        Assert.assertEquals("One shot timer: Wait 20 seconds. Restart timer when reset.", e1.getLongDescription());
        
        e1.setTimerType(TimerType.REPEAT_SINGLE_DELAY);
        e1.setTimerDelay(30, 0);
        Assert.assertEquals("Continuous timer: Wait 30 seconds and trigger once.", e1.getLongDescription());
        
        e1.setTimerType(TimerType.REPEAT_DOUBLE_DELAY);
        e1.setTimerDelay(40, 50);
        Assert.assertEquals("Continuous timer: Wait 40 seconds and then trigger. Stay on for 50 seconds.", e1.getLongDescription());
    }
    
    @Test
    public void testGetCategory() {
        Assert.assertTrue(Category.COMMON.equals(new Timer("IQDE321", null).getCategory()));
    }
    
    @Test
    public void setTimerType() {
        expressionTimer.setTimerDelay(10,20);
        
        expressionTimer.setTimerType(TimerType.WAIT_ONCE_TRIG_ONCE);
        Assert.assertEquals("timerType is correct", TimerType.WAIT_ONCE_TRIG_ONCE, expressionTimer.getTimerType());
        
        expressionTimer.setTimerType(TimerType.REPEAT_DOUBLE_DELAY);
        Assert.assertEquals("timerType is correct", TimerType.REPEAT_DOUBLE_DELAY, expressionTimer.getTimerType());
        
        expressionTimer.registerListeners();
        boolean hasThrown = false;
        try {
            expressionTimer.setTimerType(TimerType.WAIT_ONCE_TRIG_ONCE);
        } catch (RuntimeException ex) {
            hasThrown = true;
            Assert.assertEquals("Error message is correct", "setTimerType must not be called when listeners are registered", ex.getMessage());
        }
        Assert.assertTrue("Exception is thrown", hasThrown);
        JUnitAppender.assertErrorMessage("setTimerType must not be called when listeners are registered");
    }
    
    @Test
    public void testSetTimerDelay() {
        expressionTimer.setTimerDelay(10,20);
        Assert.assertEquals("delayOff is correct", 10, expressionTimer.getTimerDelayOff());
        Assert.assertEquals("delayOn is correct", 20, expressionTimer.getTimerDelayOn());
        
        expressionTimer.setTimerDelay(43,28);
        Assert.assertEquals("delayOff is correct", 43, expressionTimer.getTimerDelayOff());
        Assert.assertEquals("delayOn is correct", 28, expressionTimer.getTimerDelayOn());
        
        expressionTimer.registerListeners();
        boolean hasThrown = false;
        try {
            expressionTimer.setTimerDelay(3,2);
        } catch (RuntimeException ex) {
            hasThrown = true;
            Assert.assertEquals("Error message is correct", "setTimerDelay must not be called when listeners are registered", ex.getMessage());
        }
        Assert.assertTrue("Exception is thrown", hasThrown);
        JUnitAppender.assertErrorMessage("setTimerDelay must not be called when listeners are registered");
    }
    
    @Test
    public void testExecute() {
        atomicBoolean.set(false);
        expressionTimer.setTimerType(TimerType.WAIT_ONCE_TRIG_ONCE);
        expressionTimer.setTimerDelay(2, 0);
        expressionTimer.registerListeners();
//        JUnitUtil.waitFor(()->{return atomicBoolean.get();}, "timer has not triggered");
        
        
        try {
            Thread.sleep(5000);
        } catch (InterruptedException ex) {
            System.out.println("Interrupted exception");
        }
    }
/*    
    @Test
    public void testExecuteAndReset() {
        Timer e1 = new Timer("IQDE321", null);
        
        
        Assert.assertTrue("Timer".equals(e1.getShortDescription()));
        Assert.assertTrue("Timer".equals(e1.getLongDescription()));
    }
*/    
    
    // The minimal setup for log4J
    @Before
    public void setUp() throws SocketAlreadyConnectedException {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        
        _category = Category.COMMON;
        _isExternal = false;
        
        logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test");  // NOI18N
        conditionalNG = InstanceManager.getDefault(ConditionalNG_Manager.class)
                .createConditionalNG("A conditionalNG");  // NOI18N
        conditionalNG.setRunOnGUIDelayed(false);
        logixNG.addConditionalNG(conditionalNG);
        IfThenElse ifThenElse = new IfThenElse("IQDA321", null, IfThenElse.Type.TRIGGER_ACTION);
        MaleSocket maleSocket =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(ifThenElse);
        conditionalNG.getChild(0).connect(maleSocket);
        
        expressionTimer = new Timer("IQDE321", null);
        // We want our own timer class for testing
//        expressionTimer._timer = new MyTimer(null);
        MaleSocket maleSocket2 =
                InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expressionTimer);
        ifThenElse.getChild(0).connect(maleSocket2);
        
        _base = expressionTimer;
        _baseMaleSocket = maleSocket2;
        
        atomicBoolean = new AtomicBoolean(false);
        actionAtomicBoolean = new ActionAtomicBoolean(atomicBoolean, true);
        MaleSocket socketAtomicBoolean = InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionAtomicBoolean);
        ifThenElse.getChild(1).connect(socketAtomicBoolean);
        
        logixNG.setParentForAllChildren();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    
    
/*    
    private class MyTimer extends java.util.Timer {
        
        private final AtomicBoolean _abTimerTrigged;
        private final AtomicBoolean _abTimerCancelled;
        
        public MyTimer(AtomicBoolean abTimerTrigged, AtomicBoolean abTimerCancelled) {
            super(true);    // Run timer as daemon
            _abTimerTrigged = abTimerTrigged;
            _abTimerCancelled = abTimerCancelled;
        }
        
        @Override
        public void cancel() {
            _abTimerCancelled.set(true);
            // Terminates this timer, discarding any currently scheduled tasks.
//            throw new UnsupportedOperationException("Not supported.");
        }
        
        @Override
        public int purge() {
            // Removes all cancelled tasks from this timer's task queue.
            throw new UnsupportedOperationException("Not supported.");
//            return 0;
        }
        
        @Override
        public void schedule(java.util.TimerTask task, java.util.Date time) {
            // Schedules the specified task for execution at the specified time.
            throw new UnsupportedOperationException("Not supported.");
        }
        
        @Override
        public void schedule(java.util.TimerTask task, java.util.Date firstTime, long period) {
            // Schedules the specified task for repeated fixed-delay execution, beginning at the specified time.
            throw new UnsupportedOperationException("Not supported.");
        }
        
        @Override
        public void schedule(java.util.TimerTask task, long delay) {
            // Schedules the specified task for execution after the specified delay.
            throw new UnsupportedOperationException("Not supported.");
        }
        
        @Override
        public void schedule(java.util.TimerTask task, long delay, long period) {
            // Schedules the specified task for repeated fixed-delay execution, beginning after the specified delay.
            throw new UnsupportedOperationException("Not supported.");
        }
        
        @Override
        public void scheduleAtFixedRate(java.util.TimerTask task, java.util.Date firstTime, long period) {
            // Schedules the specified task for repeated fixed-rate execution, beginning at the specified time.
            throw new UnsupportedOperationException("Not supported.");
        }
        
        @Override
        public void scheduleAtFixedRate(java.util.TimerTask task, long delay, long period) {
            // Schedules the specified task for repeated fixed-rate execution, beginning after the specified delay.
            throw new UnsupportedOperationException("Not supported.");
        }
        
    }
*/    
}
