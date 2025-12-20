package jmri.jmrit.logixng.expressions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.NamedBean;
import jmri.NamedBeanHandle;
import jmri.NamedBeanHandleManager;
import jmri.Turnout;
import jmri.TurnoutManager;
import jmri.jmrit.logixng.LogixNG_Category;
import jmri.jmrit.logixng.ConditionalNG;
import jmri.jmrit.logixng.ConditionalNG_Manager;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.DigitalExpressionManager;
import jmri.jmrit.logixng.Is_IsNot_Enum;
import jmri.jmrit.logixng.LogixNG;
import jmri.jmrit.logixng.LogixNG_Manager;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.SocketAlreadyConnectedException;
import jmri.jmrit.logixng.actions.ActionAtomicBoolean;
import jmri.jmrit.logixng.actions.IfThenElse;
import jmri.jmrit.logixng.implementation.DefaultConditionalNGScaffold;
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
    public MaleSocket getConnectableChild() {
        return null;
    }

    @Override
    public String getExpectedPrintedTree() {
        return String.format("Turnout IT1 is Thrown ::: Use default%n");
    }

    @Override
    public String getExpectedPrintedTreeFromRoot() {
        return String.format(
                "LogixNG: A new logix for test%n" +
                "   ConditionalNG: A conditionalNG%n" +
                "      ! A%n" +
                "         If Then Else. Execute on change ::: Use default%n" +
                "            ? If%n" +
                "               Turnout IT1 is Thrown ::: Use default%n" +
                "            ! Then%n" +
                "               Set the atomic boolean to true ::: Use default%n" +
                "            ! Else%n" +
                "               Socket not connected%n");
    }

    @Override
    public NamedBean createNewBean(String systemName) {
        return new ExpressionTurnout(systemName, null);
    }

    @Override
    public boolean addNewSocket() {
        return false;
    }

    @Test
    public void testCtor() throws JmriException {
        ExpressionTurnout expression2;
        Assert.assertNotNull("turnout is not null", turnout);
        turnout.setState(Turnout.THROWN);

        expression2 = new ExpressionTurnout("IQDE321", null);
        Assert.assertNotNull("object exists", expression2);
        Assert.assertNull("Username matches", expression2.getUserName());
        Assert.assertEquals("String matches", "Turnout '' is Thrown", expression2.getLongDescription());

        expression2 = new ExpressionTurnout("IQDE321", "My turnout");
        Assert.assertNotNull("object exists", expression2);
        Assert.assertEquals("Username matches", "My turnout", expression2.getUserName());
        Assert.assertEquals("String matches", "Turnout '' is Thrown", expression2.getLongDescription());

        expression2 = new ExpressionTurnout("IQDE321", null);
        expression2.getSelectNamedBean().setNamedBean(turnout);
        Assert.assertTrue("turnout is correct", turnout == expression2.getSelectNamedBean().getNamedBean().getBean());
        Assert.assertNotNull("object exists", expression2);
        Assert.assertNull("Username matches", expression2.getUserName());
        Assert.assertEquals("String matches", "Turnout IT1 is Thrown", expression2.getLongDescription());

        Turnout t = InstanceManager.getDefault(TurnoutManager.class).provide("IT2");
        expression2 = new ExpressionTurnout("IQDE321", "My turnout");
        expression2.getSelectNamedBean().setNamedBean(t);
        Assert.assertTrue("turnout is correct", t == expression2.getSelectNamedBean().getNamedBean().getBean());
        Assert.assertNotNull("object exists", expression2);
        Assert.assertEquals("Username matches", "My turnout", expression2.getUserName());
        Assert.assertEquals("String matches", "Turnout IT2 is Thrown", expression2.getLongDescription());

        boolean thrown = false;
        try {
            // Illegal system name
            new ExpressionTurnout("IQE55:12:XY11", null);
        } catch (IllegalArgumentException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);

        thrown = false;
        try {
            // Illegal system name
            new ExpressionTurnout("IQE55:12:XY11", "A name");
        } catch (IllegalArgumentException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
    }

    @Test
    public void testGetChild() {
        Assert.assertTrue("getChildCount() returns 0", 0 == expressionTurnout.getChildCount());

        boolean hasThrown = false;
        try {
            expressionTurnout.getChild(0);
        } catch (UnsupportedOperationException ex) {
            hasThrown = true;
            Assert.assertEquals("Error message is correct", "Not supported.", ex.getMessage());
        }
        Assert.assertTrue("Exception is thrown", hasThrown);
    }

    @Test
    public void testTurnoutState() {
        Assert.assertEquals("String matches", "Closed", ExpressionTurnout.TurnoutState.Closed.toString());
        Assert.assertEquals("String matches", "Thrown", ExpressionTurnout.TurnoutState.Thrown.toString());
        Assert.assertEquals("String matches", "Other", ExpressionTurnout.TurnoutState.Other.toString());

        Assert.assertTrue("objects are equal", ExpressionTurnout.TurnoutState.Closed == ExpressionTurnout.TurnoutState.get(Turnout.CLOSED));
        Assert.assertTrue("objects are equal", ExpressionTurnout.TurnoutState.Thrown == ExpressionTurnout.TurnoutState.get(Turnout.THROWN));
        Assert.assertTrue("objects are equal", ExpressionTurnout.TurnoutState.Other == ExpressionTurnout.TurnoutState.get(Turnout.UNKNOWN));
        Assert.assertTrue("objects are equal", ExpressionTurnout.TurnoutState.Other == ExpressionTurnout.TurnoutState.get(Turnout.INCONSISTENT));
        Assert.assertTrue("objects are equal", ExpressionTurnout.TurnoutState.Other == ExpressionTurnout.TurnoutState.get(-1));

        Assert.assertEquals("ID matches", Turnout.CLOSED, ExpressionTurnout.TurnoutState.Closed.getID());
        Assert.assertEquals("ID matches", Turnout.THROWN, ExpressionTurnout.TurnoutState.Thrown.getID());
        Assert.assertEquals("ID matches", -1, ExpressionTurnout.TurnoutState.Other.getID());
    }

    @Test
    public void testCategory() {
        Assert.assertTrue("Category matches", LogixNG_Category.ITEM == _base.getCategory());
    }

    @Test
    public void testDescription() {
        // Disable the conditionalNG. This will unregister the listeners
        conditionalNG.setEnabled(false);

        expressionTurnout.getSelectNamedBean().removeNamedBean();
        Assert.assertTrue("Turnout".equals(expressionTurnout.getShortDescription()));
        Assert.assertTrue("Turnout '' is Thrown".equals(expressionTurnout.getLongDescription()));
        expressionTurnout.getSelectNamedBean().setNamedBean(turnout);
        expressionTurnout.set_Is_IsNot(Is_IsNot_Enum.Is);
        expressionTurnout.setBeanState(ExpressionTurnout.TurnoutState.Closed);
        Assert.assertTrue("Turnout IT1 is Closed".equals(expressionTurnout.getLongDescription()));
        expressionTurnout.set_Is_IsNot(Is_IsNot_Enum.IsNot);
        Assert.assertTrue("Turnout IT1 is not Closed".equals(expressionTurnout.getLongDescription()));
        expressionTurnout.setBeanState(ExpressionTurnout.TurnoutState.Other);
        Assert.assertTrue("Turnout IT1 is not Other".equals(expressionTurnout.getLongDescription()));
    }

    @Test
    public void testExpression() throws SocketAlreadyConnectedException, JmriException {
        // Clear flag
        atomicBoolean.set(false);
        // Turn light off
        turnout.setCommandedState(Turnout.CLOSED);
        // Disable the conditionalNG. This will unregister the listeners
        conditionalNG.setEnabled(false);

        expressionTurnout.getSelectNamedBean().setNamedBean(turnout);
        expressionTurnout.set_Is_IsNot(Is_IsNot_Enum.Is);
        expressionTurnout.setBeanState(ExpressionTurnout.TurnoutState.Thrown);

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
        // Clear the atomic boolean.
        atomicBoolean.set(false);
        // Close the switch. This should not execute the conditional.
        turnout.setCommandedState(Turnout.CLOSED);
        // The action should now be executed so the atomic boolean should be true
        Assert.assertFalse("atomicBoolean is false",atomicBoolean.get());

        // Test IS_NOT
        expressionTurnout.set_Is_IsNot(Is_IsNot_Enum.IsNot);
        // Throw the switch. This should not execute the conditional.
        turnout.setCommandedState(Turnout.THROWN);
        // The action should now be executed so the atomic boolean should be true
        Assert.assertFalse("atomicBoolean is false",atomicBoolean.get());
        // Close the switch. This should not execute the conditional.
        turnout.setCommandedState(Turnout.CLOSED);
        // The action should now be executed so the atomic boolean should be true
        Assert.assertTrue("atomicBoolean is true",atomicBoolean.get());
    }

    @Test
    public void testSetTurnout() {
        expressionTurnout.unregisterListeners();

        Turnout otherTurnout = InstanceManager.getDefault(TurnoutManager.class).provide("IM99");
        Assert.assertNotEquals("Turnouts are different", otherTurnout, expressionTurnout.getSelectNamedBean().getNamedBean().getBean());
        expressionTurnout.getSelectNamedBean().setNamedBean(otherTurnout);
        Assert.assertEquals("Turnouts are equal", otherTurnout, expressionTurnout.getSelectNamedBean().getNamedBean().getBean());

        NamedBeanHandle<Turnout> otherTurnoutHandle =
                InstanceManager.getDefault(NamedBeanHandleManager.class)
                        .getNamedBeanHandle(otherTurnout.getDisplayName(), otherTurnout);
        expressionTurnout.getSelectNamedBean().removeNamedBean();
        Assert.assertNull("Turnout is null", expressionTurnout.getSelectNamedBean().getNamedBean());
        expressionTurnout.getSelectNamedBean().setNamedBean(otherTurnoutHandle);
        Assert.assertEquals("Turnouts are equal", otherTurnout, expressionTurnout.getSelectNamedBean().getNamedBean().getBean());
        Assert.assertEquals("TurnoutHandles are equal", otherTurnoutHandle, expressionTurnout.getSelectNamedBean().getNamedBean());
    }

    @Test
    public void testSetTurnout2() {
        // Disable the conditionalNG. This will unregister the listeners
        conditionalNG.setEnabled(false);

        Turnout turnout11 = InstanceManager.getDefault(TurnoutManager.class).provide("IT11");
        Turnout turnout12 = InstanceManager.getDefault(TurnoutManager.class).provide("IT12");
        NamedBeanHandle<Turnout> turnoutHandle12 = InstanceManager.getDefault(NamedBeanHandleManager.class).getNamedBeanHandle(turnout12.getDisplayName(), turnout12);
        Turnout turnout13 = InstanceManager.getDefault(TurnoutManager.class).provide("IT13");
        Turnout turnout14 = InstanceManager.getDefault(TurnoutManager.class).provide("IT14");
        turnout14.setUserName("Some user name");

        expressionTurnout.getSelectNamedBean().removeNamedBean();
        Assert.assertNull("turnout handle is null", expressionTurnout.getSelectNamedBean().getNamedBean());

        expressionTurnout.getSelectNamedBean().setNamedBean(turnout11);
        Assert.assertTrue("turnout is correct", turnout11 == expressionTurnout.getSelectNamedBean().getNamedBean().getBean());

        expressionTurnout.getSelectNamedBean().removeNamedBean();
        Assert.assertNull("turnout handle is null", expressionTurnout.getSelectNamedBean().getNamedBean());

        expressionTurnout.getSelectNamedBean().setNamedBean(turnoutHandle12);
        Assert.assertTrue("turnout handle is correct", turnoutHandle12 == expressionTurnout.getSelectNamedBean().getNamedBean());

        expressionTurnout.getSelectNamedBean().setNamedBean("A non existent turnout");
        Assert.assertNull("turnout handle is null", expressionTurnout.getSelectNamedBean().getNamedBean());
        JUnitAppender.assertErrorMessage("Turnout \"A non existent turnout\" is not found");

        expressionTurnout.getSelectNamedBean().setNamedBean(turnout13.getSystemName());
        Assert.assertTrue("turnout is correct", turnout13 == expressionTurnout.getSelectNamedBean().getNamedBean().getBean());

        String userName = turnout14.getUserName();
        Assert.assertNotNull("turnout is not null", userName);
        expressionTurnout.getSelectNamedBean().setNamedBean(userName);
        Assert.assertTrue("turnout is correct", turnout14 == expressionTurnout.getSelectNamedBean().getNamedBean().getBean());
    }

    @Test
    public void testSetTurnoutException() {
        Assert.assertNotNull("Turnout is not null", turnout);
        Assert.assertNotNull("Turnout is not null", expressionTurnout.getSelectNamedBean().getNamedBean());
        expressionTurnout.registerListeners();
        boolean thrown = false;
        try {
            expressionTurnout.getSelectNamedBean().setNamedBean("A turnout");
        } catch (RuntimeException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
        JUnitAppender.assertErrorMessage("setNamedBean must not be called when listeners are registered");

        thrown = false;
        try {
            Turnout turnout99 = InstanceManager.getDefault(TurnoutManager.class).provide("IT99");
            NamedBeanHandle<Turnout> turnoutHandle99 =
                    InstanceManager.getDefault(NamedBeanHandleManager.class).getNamedBeanHandle(turnout99.getDisplayName(), turnout99);
            expressionTurnout.getSelectNamedBean().setNamedBean(turnoutHandle99);
        } catch (RuntimeException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
        JUnitAppender.assertErrorMessage("setNamedBean must not be called when listeners are registered");

        thrown = false;
        try {
            expressionTurnout.getSelectNamedBean().removeNamedBean();
        } catch (RuntimeException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
        JUnitAppender.assertErrorMessage("setNamedBean must not be called when listeners are registered");
    }

    @Test
    public void testRegisterListeners() {
        // Test registerListeners() when the ExpressionTurnout has no turnout
        conditionalNG.setEnabled(false);
        expressionTurnout.getSelectNamedBean().removeNamedBean();
        conditionalNG.setEnabled(true);
    }

    @Test
    public void testVetoableChange() throws PropertyVetoException {
        // Disable the conditionalNG. This will unregister the listeners
        conditionalNG.setEnabled(false);

        // Get the expressionTurnout and set the turnout
        Assert.assertNotNull("Turnout is not null", turnout);
        expressionTurnout.getSelectNamedBean().setNamedBean(turnout);

        // Get some other turnout for later use
        Turnout otherTurnout = InstanceManager.getDefault(TurnoutManager.class).provide("IM99");
        Assert.assertNotNull("Turnout is not null", otherTurnout);
        Assert.assertNotEquals("Turnout is not equal", turnout, otherTurnout);

        // Test vetoableChange() for some other propery
        expressionTurnout.vetoableChange(new PropertyChangeEvent(this, "CanSomething", "test", null));
        Assert.assertEquals("Turnout matches", turnout, expressionTurnout.getSelectNamedBean().getNamedBean().getBean());

        // Test vetoableChange() for a string
        expressionTurnout.vetoableChange(new PropertyChangeEvent(this, "CanDelete", "test", null));
        Assert.assertEquals("Turnout matches", turnout, expressionTurnout.getSelectNamedBean().getNamedBean().getBean());
        expressionTurnout.vetoableChange(new PropertyChangeEvent(this, "DoDelete", "test", null));
        Assert.assertEquals("Turnout matches", turnout, expressionTurnout.getSelectNamedBean().getNamedBean().getBean());

        // Test vetoableChange() for another turnout
        expressionTurnout.vetoableChange(new PropertyChangeEvent(this, "CanDelete", otherTurnout, null));
        Assert.assertEquals("Turnout matches", turnout, expressionTurnout.getSelectNamedBean().getNamedBean().getBean());
        expressionTurnout.vetoableChange(new PropertyChangeEvent(this, "DoDelete", otherTurnout, null));
        Assert.assertEquals("Turnout matches", turnout, expressionTurnout.getSelectNamedBean().getNamedBean().getBean());

        // Test vetoableChange() for its own turnout
        boolean thrown = false;
        try {
            expressionTurnout.getSelectNamedBean().vetoableChange(new PropertyChangeEvent(this, "CanDelete", turnout, null));
        } catch (PropertyVetoException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);

        Assert.assertEquals("Turnout matches", turnout, expressionTurnout.getSelectNamedBean().getNamedBean().getBean());
        expressionTurnout.getSelectNamedBean().vetoableChange(new PropertyChangeEvent(this, "DoDelete", turnout, null));
        Assert.assertNull("Turnout is null", expressionTurnout.getSelectNamedBean().getNamedBean());
    }

    // The minimal setup for log4J
    @Before
    public void setUp() throws SocketAlreadyConnectedException {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initLogixNGManager();

        _category = LogixNG_Category.ITEM;
        _isExternal = true;

        logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test");  // NOI18N
        conditionalNG = new DefaultConditionalNGScaffold("IQC1", "A conditionalNG");  // NOI18N;
        InstanceManager.getDefault(ConditionalNG_Manager.class).register(conditionalNG);
        conditionalNG.setRunDelayed(false);
        conditionalNG.setEnabled(true);

        logixNG.addConditionalNG(conditionalNG);

        IfThenElse ifThenElse = new IfThenElse("IQDA321", null);
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
        expressionTurnout.getSelectNamedBean().setNamedBean(turnout);
        turnout.setCommandedState(Turnout.THROWN);

        if (! logixNG.setParentForAllChildren(new ArrayList<>())) throw new RuntimeException();
        logixNG.activate();
        logixNG.setEnabled(true);
    }

    @After
    public void tearDown() {
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
