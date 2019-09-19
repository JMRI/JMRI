package jmri.jmrit.logixng.digital.expressions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.util.concurrent.atomic.AtomicBoolean;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.NamedBeanHandle;
import jmri.Turnout;
import jmri.TurnoutManager;
import jmri.jmrit.logixng.ConditionalNG;
import jmri.jmrit.logixng.ConditionalNG_Manager;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.DigitalExpressionManager;
import jmri.jmrit.logixng.Is_IsNot_Enum;
import jmri.jmrit.logixng.LogixNG;
import jmri.jmrit.logixng.LogixNG_Manager;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.SocketAlreadyConnectedException;
import jmri.jmrit.logixng.digital.actions.ActionAtomicBoolean;
import jmri.jmrit.logixng.digital.actions.IfThenElse;
import jmri.jmrit.logixng.implementation.DefaultConditionalNG;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test ExpressionTurnout
 * 
 * @author Daniel Bergqvist 2018
 */
public class ExpressionTurnoutTest {

    @Test
    public void testCtor() {
        ExpressionTurnout t = new ExpressionTurnout("IQDE321", null);
        Assert.assertNotNull("exists",t);
    }
    
    @Test
    public void testDescription() {
        ExpressionTurnout expressionTurnout = new ExpressionTurnout("IQDE321", null);
        Assert.assertTrue("Get turnout".equals(expressionTurnout.getShortDescription()));
        Assert.assertTrue("Turnout Not selected is Thrown".equals(expressionTurnout.getLongDescription()));
        Turnout turnout = InstanceManager.getDefault(TurnoutManager.class).provide("IT1");
        expressionTurnout.setTurnout(turnout);
        expressionTurnout.set_Is_IsNot(Is_IsNot_Enum.IS);
        expressionTurnout.setTurnoutState(ExpressionTurnout.TurnoutState.CLOSED);
        Assert.assertTrue("Turnout IT1 is Closed".equals(expressionTurnout.getLongDescription()));
        expressionTurnout.set_Is_IsNot(Is_IsNot_Enum.IS_NOT);
        Assert.assertTrue("Turnout IT1 is not Closed".equals(expressionTurnout.getLongDescription()));
        expressionTurnout.setTurnoutState(ExpressionTurnout.TurnoutState.OTHER);
        Assert.assertTrue("Turnout IT1 is not Other".equals(expressionTurnout.getLongDescription()));
    }
    
    @Test
    public void testExpression() throws SocketAlreadyConnectedException, JmriException {
        Turnout turnout = InstanceManager.getDefault(TurnoutManager.class).provide("IT1");
        turnout.setCommandedState(Turnout.CLOSED);
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        LogixNG logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A logixNG");
        ConditionalNG conditionalNG = InstanceManager.getDefault(ConditionalNG_Manager.class)
                .createConditionalNG("A conditionalNG");  // NOI18N
        logixNG.addConditionalNG(conditionalNG);
        logixNG.activateLogixNG();
        
        IfThenElse actionIfThen =
                new IfThenElse(
                        InstanceManager.getDefault(
                                DigitalActionManager.class).getNewSystemName(), null,
                                IfThenElse.Type.TRIGGER_ACTION);
        MaleSocket socketIfThen = InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionIfThen);
        conditionalNG.getChild(0).connect(socketIfThen);
        
        ExpressionTurnout expressionTurnout =
                new ExpressionTurnout(
                        InstanceManager.getDefault(DigitalExpressionManager.class)
                                .getNewSystemName(), null);
        expressionTurnout.setTurnout(turnout);
        expressionTurnout.set_Is_IsNot(Is_IsNot_Enum.IS);
        expressionTurnout.setTurnoutState(ExpressionTurnout.TurnoutState.THROWN);
        MaleSocket socketTurnout = InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expressionTurnout);
        socketIfThen.getChild(0).connect(socketTurnout);
        
        ActionAtomicBoolean actionAtomicBoolean = new ActionAtomicBoolean(atomicBoolean, true);
        MaleSocket socketAtomicBoolean = InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionAtomicBoolean);
        socketIfThen.getChild(1).connect(socketAtomicBoolean);
        
        // The action is not yet executed so the atomic boolean should be false
        Assert.assertFalse("atomicBoolean is false",atomicBoolean.get());
        // Throw the switch. This should not execute the conditional.
        turnout.setCommandedState(Turnout.THROWN);
        // The conditionalNG is not yet enabled so it shouldn't be executed.
        // So the atomic boolean should be false
        Assert.assertFalse("atomicBoolean is false",atomicBoolean.get());
        // Close the switch. This should not execute the conditional.
        turnout.setCommandedState(Turnout.CLOSED);
        // Enable the conditionalNG and all its children.
        conditionalNG.setEnabled(true);
        // The action is not yet executed so the atomic boolean should be false
        Assert.assertFalse("atomicBoolean is false",atomicBoolean.get());
        // Throw the switch. This should execute the conditional.
        turnout.setCommandedState(Turnout.THROWN);
        // The action should now be executed so the atomic boolean should be true
        Assert.assertTrue("atomicBoolean is true",atomicBoolean.get());
    }
    
    @Test
    public void testSetTurnout() {
        // Test setTurnout() when listeners are registered
        Turnout turnout = InstanceManager.getDefault(TurnoutManager.class).provide("IT1");
        Assert.assertNotNull("Turnout is not null", turnout);
        ExpressionTurnout expression =
                new ExpressionTurnout(
                        InstanceManager.getDefault(DigitalExpressionManager.class)
                                .getNewSystemName(), null);
        expression.setTurnout(turnout);
        
        Assert.assertNotNull("Turnout is not null", expression.getTurnout());
        expression.registerListeners();
        boolean thrown = false;
        try {
            expression.setTurnout((String)null);
        } catch (RuntimeException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
        JUnitAppender.assertErrorMessage("setTurnout must not be called when listeners are registered");
        
        thrown = false;
        try {
            expression.setTurnout((NamedBeanHandle<Turnout>)null);
        } catch (RuntimeException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
        JUnitAppender.assertErrorMessage("setTurnout must not be called when listeners are registered");
        
        thrown = false;
        try {
            expression.setTurnout((Turnout)null);
        } catch (RuntimeException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
        JUnitAppender.assertErrorMessage("setTurnout must not be called when listeners are registered");
    }
    
    @Test
    public void testVetoableChange() throws PropertyVetoException {
        // Get the expression and set the turnout
        Turnout turnout = InstanceManager.getDefault(TurnoutManager.class).provide("IT1");
        Assert.assertNotNull("Turnout is not null", turnout);
        ExpressionTurnout expression =
                new ExpressionTurnout(
                        InstanceManager.getDefault(DigitalExpressionManager.class)
                                .getNewSystemName(), null);
        expression.setTurnout(turnout);
        
        // Get some other turnout for later use
        Turnout otherTurnout = InstanceManager.getDefault(TurnoutManager.class).provide("IM99");
        Assert.assertNotNull("Turnout is not null", otherTurnout);
        Assert.assertNotEquals("Turnout is not equal", turnout, otherTurnout);
        
        // Test vetoableChange() for some other propery
        expression.vetoableChange(new PropertyChangeEvent(this, "CanSomething", "test", null));
        Assert.assertEquals("Turnout matches", turnout, expression.getTurnout().getBean());
        
        // Test vetoableChange() for a string
        expression.vetoableChange(new PropertyChangeEvent(this, "CanDelete", "test", null));
        Assert.assertEquals("Turnout matches", turnout, expression.getTurnout().getBean());
        expression.vetoableChange(new PropertyChangeEvent(this, "DoDelete", "test", null));
        Assert.assertEquals("Turnout matches", turnout, expression.getTurnout().getBean());
        
        // Test vetoableChange() for another turnout
        expression.vetoableChange(new PropertyChangeEvent(this, "CanDelete", otherTurnout, null));
        Assert.assertEquals("Turnout matches", turnout, expression.getTurnout().getBean());
        expression.vetoableChange(new PropertyChangeEvent(this, "DoDelete", otherTurnout, null));
        Assert.assertEquals("Turnout matches", turnout, expression.getTurnout().getBean());
        
        // Test vetoableChange() for its own turnout
        boolean thrown = false;
        try {
            expression.vetoableChange(new PropertyChangeEvent(this, "CanDelete", turnout, null));
        } catch (PropertyVetoException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
        
        Assert.assertEquals("Turnout matches", turnout, expression.getTurnout().getBean());
        expression.vetoableChange(new PropertyChangeEvent(this, "DoDelete", turnout, null));
        Assert.assertNull("Turnout is null", expression.getTurnout());
    }
    
    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalTurnoutManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    
}
