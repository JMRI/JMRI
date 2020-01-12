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
import jmri.jmrit.logixng.digital.expressions.ExpressionTimer.TimerType;
import jmri.util.JUnitAppender;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test ExpressionTimer
 * 
 * @author Daniel Bergqvist 2018
 */
public class ExpressionTimerTest extends AbstractDigitalExpressionTestBase {

    private LogixNG _logixNG;
    private ConditionalNG _conditionalNG;
    private ExpressionTimer _expressionTimer;
    private ActionAtomicBoolean _actionAtomicBoolean;
    private AtomicBoolean _atomicBoolean;
    
    
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
        return null;
    }
    
    @Override
    public String getExpectedPrintedTree() {
        return String.format("One shot timer: Wait 0 milliseconds and trigger once.%n");
    }
    
    @Override
    public String getExpectedPrintedTreeFromRoot() {
        return String.format(
                "LogixNG: A new logix for test%n" +
                "   ConditionalNG: A conditionalNG%n" +
                "      ! %n" +
                "         If E then A1 else A2%n" +
                "            ? E%n" +
                "               One shot timer: Wait 0 milliseconds and trigger once.%n" +
                "            ! A1%n" +
                "               Set the atomic boolean to true%n" +
                "            ! A2%n" +
                "               Socket not connected%n");
    }
    
    @Override
    public NamedBean createNewBean(String systemName) {
        return new ExpressionTimer(systemName, null);
    }
    
    @Test
    public void testCtor() {
        ExpressionTimer expression2;
        
        expression2 = new ExpressionTimer("IQDE321", null);
        Assert.assertNotNull("object exists", expression2);
        Assert.assertNull("Username matches", expression2.getUserName());
        Assert.assertEquals("String matches", "One shot timer: Wait 0 milliseconds and trigger once.", expression2.getLongDescription());
        
        expression2 = new ExpressionTimer("IQDE321", "My expression");
        Assert.assertNotNull("object exists", expression2);
        Assert.assertEquals("Username matches", "My expression", expression2.getUserName());
        Assert.assertEquals("String matches", "One shot timer: Wait 0 milliseconds and trigger once.", expression2.getLongDescription());
        
        boolean thrown = false;
        try {
            // Illegal system name
            new ExpressionTimer("IQE55:12:XY11", null);
        } catch (IllegalArgumentException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
        
        thrown = false;
        try {
            // Illegal system name
            new ExpressionTimer("IQE55:12:XY11", "A name");
        } catch (IllegalArgumentException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
    }
    
    @Test
    public void testGetChild() {
        Assert.assertTrue("getChildCount() returns 0", 0 == _expressionTimer.getChildCount());
        
        boolean hasThrown = false;
        try {
            _expressionTimer.getChild(0);
        } catch (UnsupportedOperationException ex) {
            hasThrown = true;
            Assert.assertEquals("Error message is correct", "Not supported.", ex.getMessage());
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
        ExpressionTimer e1 = new ExpressionTimer("IQDE321", null);
        Assert.assertTrue("Timer".equals(e1.getShortDescription()));
        
        e1.setTimerType(TimerType.WAIT_ONCE_TRIG_ONCE);
        e1.setTimerDelay(100, 0);
        Assert.assertEquals("One shot timer: Wait 100 milliseconds and trigger once.", e1.getLongDescription());
        
        e1.setTimerType(TimerType.WAIT_ONCE_TRIG_UNTIL_RESET);
        e1.setTimerDelay(200, 0);
        Assert.assertEquals("One shot timer: Wait 200 milliseconds. Restart timer when reset.", e1.getLongDescription());
        
        e1.setTimerType(TimerType.REPEAT_SINGLE_DELAY);
        e1.setTimerDelay(300, 0);
        Assert.assertEquals("Continuous timer: Wait 300 milliseconds and trigger once.", e1.getLongDescription());
        
        e1.setTimerType(TimerType.REPEAT_DOUBLE_DELAY);
        e1.setTimerDelay(400, 500);
        Assert.assertEquals("Continuous timer: Wait 400 milliseconds and then trigger. Stay on for 500 milliseconds.", e1.getLongDescription());
    }
    
    @Test
    public void testGetCategory() {
        Assert.assertTrue(Category.COMMON.equals(new ExpressionTimer("IQDE321", null).getCategory()));
    }
    
    @Test
    public void setTimerType() {
        // Disable the _conditionalNG. This will unregister the listeners
        _conditionalNG.setEnabled(false);
        
        _expressionTimer.setTimerDelay(100, 200);
        
        _expressionTimer.setTimerType(TimerType.WAIT_ONCE_TRIG_ONCE);
        Assert.assertEquals("timerType is correct", TimerType.WAIT_ONCE_TRIG_ONCE, _expressionTimer.getTimerType());
        
//        _expressionTimer.setTimerType(TimerType.REPEAT_DOUBLE_DELAY);
//        Assert.assertEquals("timerType is correct", TimerType.REPEAT_DOUBLE_DELAY, _expressionTimer.getTimerType());
        
        // Enable the conditionalNG. This will register the listeners
        _expressionTimer.getConditionalNG().setEnabled(true);
        
        boolean hasThrown = false;
        try {
            _expressionTimer.setTimerType(TimerType.WAIT_ONCE_TRIG_ONCE);
        } catch (RuntimeException ex) {
            hasThrown = true;
            Assert.assertEquals("Error message is correct", "setTimerType must not be called when listeners are registered", ex.getMessage());
        }
        Assert.assertTrue("Exception is thrown", hasThrown);
        JUnitAppender.assertErrorMessage("setTimerType must not be called when listeners are registered");
    }
    
    private void sleep(long time) {
        long startTime = System.currentTimeMillis();
        while ((System.currentTimeMillis() - startTime) < time) {
            try {
                Thread.sleep(time);
            } catch (InterruptedException ex) {
                Assert.fail("failed due to InterruptedException");
            }
        }
    }
    
    @Test
    public void testSetTimerDelay() {
        // Disable the _conditionalNG. This will unregister the listeners
        _conditionalNG.setEnabled(false);
        
        _expressionTimer.setTimerDelay(100, 200);
        Assert.assertEquals("delayOff is correct", 100, _expressionTimer.getTimerDelayOff());
        Assert.assertEquals("delayOn is correct", 200, _expressionTimer.getTimerDelayOn());
        
        _expressionTimer.setTimerDelay(43, 28);
        Assert.assertEquals("delayOff is correct", 43, _expressionTimer.getTimerDelayOff());
        Assert.assertEquals("delayOn is correct", 28, _expressionTimer.getTimerDelayOn());
        
        // Enable the conditionalNG. This will register the listeners
        _expressionTimer.getConditionalNG().setEnabled(true);
        
        boolean hasThrown = false;
        try {
            _expressionTimer.setTimerDelay(300, 200);
        } catch (RuntimeException ex) {
            hasThrown = true;
            Assert.assertEquals("Error message is correct", "setTimerDelay must not be called when listeners are registered", ex.getMessage());
        }
        Assert.assertTrue("Exception is thrown", hasThrown);
        JUnitAppender.assertErrorMessage("setTimerDelay must not be called when listeners are registered");
    }
    
    @Test
    public void testExecuteWaitOnceTrigOnce() {
        // Disable the _conditionalNG. This will unregister the listeners
        _conditionalNG.setEnabled(false);
        _atomicBoolean.set(false);
        _expressionTimer.setTimerType(TimerType.WAIT_ONCE_TRIG_ONCE);
        _expressionTimer.setTimerDelay(100, 0);
        Assert.assertFalse("atomicBoolean is not set", _atomicBoolean.get());
        // Enable the _conditionalNG. This will register the listeners, reset
        // the timer and execute the conditionalNG.
        _conditionalNG.setEnabled(true);
        // The timer should now trig after 100 milliseconds
        JUnitUtil.waitFor(()->{return _atomicBoolean.get();}, "timer has not triggered");
        // Clear the flag
        _atomicBoolean.set(false);
        // Execute the conditionalNG to evaluate the timer
        _conditionalNG.execute();
        // Check that the timer is not running
        sleep(1000);
        Assert.assertFalse("timer has not triggered", _atomicBoolean.get());
        // Reset the timer. This will also execute the conditionalNG which in
        // turn will restart the timer again.
        _expressionTimer.reset();
        // The timer should now trig after 100 milliseconds
        JUnitUtil.waitFor(()->{return _atomicBoolean.get();}, "timer has not triggered");
        // Clear the flag
        _atomicBoolean.set(false);
        // Execute the conditionalNG to evaluate the timer
        _conditionalNG.execute();
        // Check that the timer is not running
        sleep(1000);
        Assert.assertFalse("timer has not triggered", _atomicBoolean.get());
    }
    
    @Test
    public void testExecuteWaitOnceTrigUntilReset() {
        // Disable the _conditionalNG. This will unregister the listeners
        _conditionalNG.setEnabled(false);
        _atomicBoolean.set(false);
        _expressionTimer.setTimerType(TimerType.WAIT_ONCE_TRIG_UNTIL_RESET);
        _expressionTimer.setTimerDelay(100, 0);
        Assert.assertFalse("atomicBoolean is not set", _atomicBoolean.get());
        // Enable the _conditionalNG. This will register the listeners, reset
        // the timer and execute the conditionalNG.
        _conditionalNG.setEnabled(true);
        // The timer should now trig after 100 milliseconds
        JUnitUtil.waitFor(()->{return _atomicBoolean.get();}, "timer has not triggered");
        // Clear the flag
        _atomicBoolean.set(false);
        // Execute the conditionalNG to evaluate the timer
        _conditionalNG.execute();
        // The flag should now be true since the timer will return 'true' until
        // the timer is reset.
        Assert.assertTrue("atomicBoolean is set", _atomicBoolean.get());
        // Clear the flag
        _atomicBoolean.set(false);
        // Reset the timer. This will also execute the conditionalNG which in
        // turn will restart the timer again.
        _expressionTimer.reset();
        // Check that the flag is not set
        Assert.assertFalse("atomicBoolean is not set", _atomicBoolean.get());
        // Check that the flag is not set.
        Assert.assertFalse("atomicBoolean is not set", _atomicBoolean.get());
        // The timer should now trig after 100 milliseconds
        JUnitUtil.waitFor(()->{return _atomicBoolean.get();}, "timer has not triggered");
        // Clear the flag
        _atomicBoolean.set(false);
        // Execute the conditionalNG to evaluate the timer
        _conditionalNG.execute();
        // The flag should now be true since the timer will return 'true' until
        // the timer is reset.
        Assert.assertTrue("atomicBoolean is set", _atomicBoolean.get());
        
        // Clear the flag
        _atomicBoolean.set(false);
        // Reset the timer. This will also execute the conditionalNG which in
        // turn will restart the timer again.
        _expressionTimer.reset();
        // Check that the flag is not set
        Assert.assertFalse("atomicBoolean is not set", _atomicBoolean.get());
        // _expressionTimer._timerStatusRef is now STARTED. Try evaluate()
        _expressionTimer.evaluate();
        // Check that the flag is not set.
        Assert.assertFalse("atomicBoolean is not set", _atomicBoolean.get());
        // The timer should now trig after 100 milliseconds
        JUnitUtil.waitFor(()->{return _atomicBoolean.get();}, "timer has not triggered");
        // Clear the flag
        _atomicBoolean.set(false);
        // Execute the conditionalNG to evaluate the timer
        _conditionalNG.execute();
        // The flag should now be true since the timer will return 'true' until
        // the timer is reset.
        Assert.assertTrue("atomicBoolean is set", _atomicBoolean.get());
    }
    
    @Test
    public void testExecuteSingleDelay() {
        // Disable the _conditionalNG. This will unregister the listeners
        _conditionalNG.setEnabled(false);
        
        // This test requires that runOnGUIDelayed is true
        _conditionalNG.setRunOnGUIDelayed(true);
        
        _atomicBoolean.set(false);
        _expressionTimer.setTimerType(TimerType.REPEAT_SINGLE_DELAY);
        _expressionTimer.setTimerDelay(100, 0);
        Assert.assertFalse("atomicBoolean is not set", _atomicBoolean.get());
        // Enable the _conditionalNG. This will register the listeners, reset
        // the timer and execute the conditionalNG.
        _conditionalNG.setEnabled(true);
        // The timer should now trig after 100 milliseconds
        JUnitUtil.waitFor(()->{return _atomicBoolean.get();}, "timer has not triggered");
        // Clear the flag
        _atomicBoolean.set(false);
        // The timer should now trig after 100 milliseconds
        JUnitUtil.waitFor(()->{return _atomicBoolean.get();}, "timer has not triggered");
        // Clear the flag
        _atomicBoolean.set(false);
        // The timer should now trig after 100 milliseconds
        JUnitUtil.waitFor(()->{return _atomicBoolean.get();}, "timer has not triggered");
        
        // Clear the flag
        _atomicBoolean.set(false);
        // _expressionTimer._timerStatusRef is now STARTED. Try evaluate()
        _expressionTimer.evaluate();
        // The timer should now trig after 100 milliseconds
        JUnitUtil.waitFor(()->{return _atomicBoolean.get();}, "timer has not triggered");
        // Clear the flag
        _atomicBoolean.set(false);
        // _expressionTimer._timerStatusRef is now STARTED. Try evaluate()
        _expressionTimer.evaluate();
        // The timer should now trig after 100 milliseconds
        JUnitUtil.waitFor(()->{return _atomicBoolean.get();}, "timer has not triggered");
    }
    
    @Test
    public void testExecuteDoubleDelay() {
        // Disable the _conditionalNG. This will unregister the listeners
        _conditionalNG.setEnabled(false);
        
        // This test requires that runOnGUIDelayed is true
        _conditionalNG.setRunOnGUIDelayed(true);
        
        _atomicBoolean.set(false);
        _expressionTimer.setTimerType(TimerType.REPEAT_DOUBLE_DELAY);
        _expressionTimer.setTimerDelay(100, 100);
        // Enable the _conditionalNG. This will register the listeners, reset
        // the timer and execute the conditionalNG.
        _conditionalNG.setEnabled(true);
        
        // Check that the timer gets true after 100 milliseconds
        // Check that the timer is false
        Assert.assertFalse("timer is false", _expressionTimer.evaluate());
        // The timer should now trig after 1 milliseconds
        JUnitUtil.waitFor(()->{return _expressionTimer.evaluate();}, "timer is still not true");
        
        // Check that the timer gets false after 100 milliseconds
        // Check that the timer is still true
        Assert.assertTrue("timer is true", _expressionTimer.evaluate());
        // The timer should now trig after 1 second
        JUnitUtil.waitFor(()->{return !_expressionTimer.evaluate();}, "timer is still true");
        
        // Check that the timer gets true after 100 milliseconds
        // Check that the timer is false
        Assert.assertFalse("timer is false", _expressionTimer.evaluate());
        // The timer should now trig after 100 milliseconds
        JUnitUtil.waitFor(()->{return _expressionTimer.evaluate();}, "timer is still not true");
        
        // Check that the timer gets false after 100 milliseconds
        // Check that the timer is still true
        Assert.assertTrue("timer is true", _expressionTimer.evaluate());
        // The timer should now trig after 100 milliseconds
        JUnitUtil.waitFor(()->{return !_expressionTimer.evaluate();}, "timer is still true");
    }
    
    @Test
    public void testTimerType() {
        // The purpose of this test is mostly to get coverage of the methods
        // in the TimerType enum.
        
        Assert.assertEquals("One shot timer: Wait some time and trigger once", TimerType.WAIT_ONCE_TRIG_ONCE.toString());
        Assert.assertEquals("One shot timer: Wait some time and trigger until reset. Restart timer when reset", TimerType.WAIT_ONCE_TRIG_UNTIL_RESET.toString());
        Assert.assertEquals("Continuous timer: Wait some time and trigger once", TimerType.REPEAT_SINGLE_DELAY.toString());
        Assert.assertEquals("Continuous timer: Wait some time and then trigger. Stay on for some time", TimerType.REPEAT_DOUBLE_DELAY.toString());
        
        Assert.assertEquals("Can be used to throw a turnout after some time", TimerType.WAIT_ONCE_TRIG_ONCE.getExplanation());
        Assert.assertEquals("Useful for holding a light lit after some time, until reset", TimerType.WAIT_ONCE_TRIG_UNTIL_RESET.getExplanation());
        Assert.assertEquals("Useful for logging a message repeatedly at some interval", TimerType.REPEAT_SINGLE_DELAY.getExplanation());
        Assert.assertEquals("Useful for turning a light on and off repeatedly at some interval", TimerType.REPEAT_DOUBLE_DELAY.getExplanation());
    }
    
    @Test
    @Override
    public void testEnableAndEvaluate() {
        // Not implemented.
        // This method is implemented for other digital expressions so no need
        // to add support here. It doesn't need to be tested for every digital
        // expression.
    }
    
    @Test
    @Override
    public void testDebugConfig() {
        // Not implemented.
        // This method is implemented for other digital expressions so no need
        // to add support here. It doesn't need to be tested for every digital
        // expression.
    }
    
    // The minimal setup for log4J
    @Before
    public void setUp() throws SocketAlreadyConnectedException {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        
        _category = Category.COMMON;
        _isExternal = false;
        
        _logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test");  // NOI18N
        _conditionalNG = InstanceManager.getDefault(ConditionalNG_Manager.class)
                .createConditionalNG("A conditionalNG");  // NOI18N
        _conditionalNG.setRunOnGUIDelayed(false);
        _conditionalNG.setEnabled(true);
        
        _logixNG.addConditionalNG(_conditionalNG);
        
        IfThenElse ifThenElse = new IfThenElse("IQDA321", null, IfThenElse.Type.TRIGGER_ACTION);
        MaleSocket maleSocket =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(ifThenElse);
        _conditionalNG.getChild(0).connect(maleSocket);
        
        _expressionTimer = new ExpressionTimer("IQDE321", null);
        MaleSocket maleSocket2 =
                InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(_expressionTimer);
        ifThenElse.getChild(0).connect(maleSocket2);
        
        _base = _expressionTimer;
        _baseMaleSocket = maleSocket2;
        
        _atomicBoolean = new AtomicBoolean(false);
        _actionAtomicBoolean = new ActionAtomicBoolean(_atomicBoolean, true);
        MaleSocket socketAtomicBoolean = InstanceManager.getDefault(DigitalActionManager.class).registerAction(_actionAtomicBoolean);
        ifThenElse.getChild(1).connect(socketAtomicBoolean);
        
	_logixNG.setParentForAllChildren();
        _logixNG.setEnabled(true);
        _logixNG.activateLogixNG();
    }

    @After
    public void tearDown() {
        _expressionTimer.dispose();
        JUnitUtil.tearDown();
    }
    
}
