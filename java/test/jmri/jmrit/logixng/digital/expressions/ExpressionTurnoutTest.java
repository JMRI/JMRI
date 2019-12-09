package jmri.jmrit.logixng.digital.expressions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.util.concurrent.atomic.AtomicBoolean;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.NamedBean;
import jmri.NamedBeanHandle;
import jmri.NamedBeanHandleManager;
import jmri.Turnout;
import jmri.TurnoutManager;
import jmri.jmrit.logixng.Category;
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
public class ExpressionTurnoutTest extends AbstractDigitalExpressionTestBase {

    private LogixNG logixNG;
    private ConditionalNG conditionalNG;
    private ExpressionTurnout expressionTurnout;
    private ActionAtomicBoolean actionAtomicBoolean;
    private AtomicBoolean atomicBoolean;
    private Turnout turnout;
    
    
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
        return String.format("Turnout IT1 is Thrown%n");
    }
    
    @Override
    public String getExpectedPrintedTreeFromRoot() {
        return String.format(
                "LogixNG: A new logix for test%n" +
                "   ConditionalNG: A conditionalNG%n" +
                "      ! %n" +
                "         If E then A1 else A2%n" +
                "            ? E%n" +
                "               Turnout IT1 is Thrown%n" +
                "            ! A1%n" +
                "               Set the atomic boolean to true%n" +
                "            ! A2%n" +
                "               Socket not connected%n");
    }
    
    @Override
    public NamedBean createNewBean(String systemName) {
        return new ExpressionTurnout(systemName, null);
    }
    
    @Test
    public void testCtor() {
        ExpressionTurnout t = new ExpressionTurnout("IQDE321", null);
        Assert.assertNotNull("exists",t);
    }
    
    @Test
    public void testDescription() {
        expressionTurnout.setTurnout((Turnout)null);
        Assert.assertTrue("Get turnout".equals(expressionTurnout.getShortDescription()));
        Assert.assertTrue("Turnout Not selected is Thrown".equals(expressionTurnout.getLongDescription()));
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
        expressionTurnout.setTurnout(turnout);
        expressionTurnout.set_Is_IsNot(Is_IsNot_Enum.IS);
        expressionTurnout.setTurnoutState(ExpressionTurnout.TurnoutState.THROWN);
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
        expressionTurnout.unregisterListeners();
        
        Turnout otherTurnout = InstanceManager.getDefault(TurnoutManager.class).provide("IM99");
        Assert.assertNotEquals("Turnouts are different", otherTurnout, expressionTurnout.getTurnout().getBean());
        expressionTurnout.setTurnout(otherTurnout);
        Assert.assertEquals("Turnouts are equal", otherTurnout, expressionTurnout.getTurnout().getBean());
        
        NamedBeanHandle<Turnout> otherTurnoutHandle =
                InstanceManager.getDefault(NamedBeanHandleManager.class)
                        .getNamedBeanHandle(otherTurnout.getDisplayName(), otherTurnout);
        expressionTurnout.setTurnout((Turnout)null);
        Assert.assertNull("Turnout is null", expressionTurnout.getTurnout());
        expressionTurnout.setTurnout(otherTurnoutHandle);
        Assert.assertEquals("Turnouts are equal", otherTurnout, expressionTurnout.getTurnout().getBean());
        Assert.assertEquals("TurnoutHandles are equal", otherTurnoutHandle, expressionTurnout.getTurnout());
    }
    
    @Test
    public void testSetTurnoutException() {
        Assert.assertNotNull("Turnout is not null", turnout);
        Assert.assertNotNull("Turnout is not null", expressionTurnout.getTurnout());
        expressionTurnout.registerListeners();
        boolean thrown = false;
        try {
            expressionTurnout.setTurnout((String)null);
        } catch (RuntimeException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
        JUnitAppender.assertErrorMessage("setTurnout must not be called when listeners are registered");
        
        thrown = false;
        try {
            expressionTurnout.setTurnout((NamedBeanHandle<Turnout>)null);
        } catch (RuntimeException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
        JUnitAppender.assertErrorMessage("setTurnout must not be called when listeners are registered");
        
        thrown = false;
        try {
            expressionTurnout.setTurnout((Turnout)null);
        } catch (RuntimeException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
        JUnitAppender.assertErrorMessage("setTurnout must not be called when listeners are registered");
    }
    
    @Test
    public void testVetoableChange() throws PropertyVetoException {
        // Get the expressionTurnout and set the turnout
        Assert.assertNotNull("Turnout is not null", turnout);
        expressionTurnout.setTurnout(turnout);
        
        // Get some other turnout for later use
        Turnout otherTurnout = InstanceManager.getDefault(TurnoutManager.class).provide("IM99");
        Assert.assertNotNull("Turnout is not null", otherTurnout);
        Assert.assertNotEquals("Turnout is not equal", turnout, otherTurnout);
        
        // Test vetoableChange() for some other propery
        expressionTurnout.vetoableChange(new PropertyChangeEvent(this, "CanSomething", "test", null));
        Assert.assertEquals("Turnout matches", turnout, expressionTurnout.getTurnout().getBean());
        
        // Test vetoableChange() for a string
        expressionTurnout.vetoableChange(new PropertyChangeEvent(this, "CanDelete", "test", null));
        Assert.assertEquals("Turnout matches", turnout, expressionTurnout.getTurnout().getBean());
        expressionTurnout.vetoableChange(new PropertyChangeEvent(this, "DoDelete", "test", null));
        Assert.assertEquals("Turnout matches", turnout, expressionTurnout.getTurnout().getBean());
        
        // Test vetoableChange() for another turnout
        expressionTurnout.vetoableChange(new PropertyChangeEvent(this, "CanDelete", otherTurnout, null));
        Assert.assertEquals("Turnout matches", turnout, expressionTurnout.getTurnout().getBean());
        expressionTurnout.vetoableChange(new PropertyChangeEvent(this, "DoDelete", otherTurnout, null));
        Assert.assertEquals("Turnout matches", turnout, expressionTurnout.getTurnout().getBean());
        
        // Test vetoableChange() for its own turnout
        boolean thrown = false;
        try {
            expressionTurnout.vetoableChange(new PropertyChangeEvent(this, "CanDelete", turnout, null));
        } catch (PropertyVetoException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
        
        Assert.assertEquals("Turnout matches", turnout, expressionTurnout.getTurnout().getBean());
        expressionTurnout.vetoableChange(new PropertyChangeEvent(this, "DoDelete", turnout, null));
        Assert.assertNull("Turnout is null", expressionTurnout.getTurnout());
    }
    
    // The minimal setup for log4J
    @Before
    public void setUp() throws SocketAlreadyConnectedException {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalTurnoutManager();
        
        _category = Category.ITEM;
        _isExternal = true;
        
        logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test");  // NOI18N
        conditionalNG = InstanceManager.getDefault(ConditionalNG_Manager.class)
                .createConditionalNG("A conditionalNG");  // NOI18N
        conditionalNG.setRunOnGUIDelayed(false);
        logixNG.addConditionalNG(conditionalNG);
        logixNG.activateLogixNG();
        IfThenElse ifThenElse = new IfThenElse("IQDA321", null, IfThenElse.Type.TRIGGER_ACTION);
        MaleSocket maleSocket =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(ifThenElse);
        conditionalNG.getChild(0).connect(maleSocket);
        
        expressionTurnout = new ExpressionTurnout("IQDE321", null);
        MaleSocket maleSocket2 =
                InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expressionTurnout);
        ifThenElse.getChild(0).connect(maleSocket2);
        
        _base = expressionTurnout;
        _baseMaleSocket = maleSocket2;
        
        atomicBoolean = new AtomicBoolean(false);
        actionAtomicBoolean = new ActionAtomicBoolean(atomicBoolean, true);
        MaleSocket socketAtomicBoolean = InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionAtomicBoolean);
        ifThenElse.getChild(1).connect(socketAtomicBoolean);
        
        turnout = InstanceManager.getDefault(TurnoutManager.class).provide("IT1");
        expressionTurnout.setTurnout(turnout);
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    
}
