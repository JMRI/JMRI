package jmri.jmrit.logixng.expressions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
        assertNotNull( turnout, "turnout is not null");
        turnout.setState(Turnout.THROWN);

        expression2 = new ExpressionTurnout("IQDE321", null);
        assertNotNull( expression2, "object exists");
        assertNull( expression2.getUserName(), "Username matches");
        assertEquals( "Turnout '' is Thrown", expression2.getLongDescription(), "String matches");

        expression2 = new ExpressionTurnout("IQDE321", "My turnout");
        assertNotNull( expression2, "object exists");
        assertEquals( "My turnout", expression2.getUserName(), "Username matches");
        assertEquals( "Turnout '' is Thrown", expression2.getLongDescription(), "String matches");

        expression2 = new ExpressionTurnout("IQDE321", null);
        expression2.getSelectNamedBean().setNamedBean(turnout);
        assertEquals( turnout, expression2.getSelectNamedBean().getNamedBean().getBean(),
                "turnout is correct");
        assertNotNull( expression2, "object exists");
        assertNull( expression2.getUserName(), "Username matches");
        assertEquals( "Turnout IT1 is Thrown", expression2.getLongDescription(), "String matches");

        Turnout t = InstanceManager.getDefault(TurnoutManager.class).provide("IT2");
        expression2 = new ExpressionTurnout("IQDE321", "My turnout");
        expression2.getSelectNamedBean().setNamedBean(t);
        assertEquals( t, expression2.getSelectNamedBean().getNamedBean().getBean(), "turnout is correct");
        assertNotNull( expression2, "object exists");
        assertEquals( "My turnout", expression2.getUserName(), "Username matches");
        assertEquals( "Turnout IT2 is Thrown", expression2.getLongDescription(), "String matches");

        IllegalArgumentException ex = assertThrows( IllegalArgumentException.class, () -> {
            var et = new ExpressionTurnout("IQE55:12:XY11", null);
            fail("Should have tyhtown, created " + et);
        }, "Illegal system name Expected exception thrown");
        assertNotNull(ex);

        ex = assertThrows( IllegalArgumentException.class, () -> {
            var et = new ExpressionTurnout("IQE55:12:XY11", "A name");
            fail("Should have tyhtown, created " + et);
        }, "Illegal system name Expected exception thrown");
        assertNotNull(ex);

    }

    @Test
    public void testGetChild() {
        assertEquals( 0, expressionTurnout.getChildCount(), "getChildCount() returns 0");

        UnsupportedOperationException ex = assertThrows( UnsupportedOperationException.class, () ->
            expressionTurnout.getChild(0), "Exception is thrown");
        assertEquals( "Not supported.", ex.getMessage(), "Error message is correct");
    }

    @Test
    public void testTurnoutState() {
        assertEquals( "Closed", ExpressionTurnout.TurnoutState.Closed.toString(), "String matches");
        assertEquals( "Thrown", ExpressionTurnout.TurnoutState.Thrown.toString(), "String matches");
        assertEquals( "Other", ExpressionTurnout.TurnoutState.Other.toString(), "String matches");

        assertEquals( ExpressionTurnout.TurnoutState.Closed, ExpressionTurnout.TurnoutState.get(Turnout.CLOSED), "objects are equal");
        assertEquals( ExpressionTurnout.TurnoutState.Thrown, ExpressionTurnout.TurnoutState.get(Turnout.THROWN), "objects are equal");
        assertEquals( ExpressionTurnout.TurnoutState.Other, ExpressionTurnout.TurnoutState.get(Turnout.UNKNOWN), "objects are equal");
        assertEquals( ExpressionTurnout.TurnoutState.Other, ExpressionTurnout.TurnoutState.get(Turnout.INCONSISTENT), "objects are equal");
        assertEquals( ExpressionTurnout.TurnoutState.Other, ExpressionTurnout.TurnoutState.get(-1), "objects are equal");

        assertEquals( Turnout.CLOSED, ExpressionTurnout.TurnoutState.Closed.getID(), "ID matches");
        assertEquals( Turnout.THROWN, ExpressionTurnout.TurnoutState.Thrown.getID(), "ID matches");
        assertEquals( -1, ExpressionTurnout.TurnoutState.Other.getID(), "ID matches");
    }

    @Test
    public void testCategory() {
        assertEquals( LogixNG_Category.ITEM, _base.getCategory(), "Category matches");
    }

    @Test
    public void testDescription() {
        // Disable the conditionalNG. This will unregister the listeners
        conditionalNG.setEnabled(false);

        expressionTurnout.getSelectNamedBean().removeNamedBean();
        assertEquals( "Turnout", expressionTurnout.getShortDescription());
        assertEquals( "Turnout '' is Thrown", expressionTurnout.getLongDescription());
        expressionTurnout.getSelectNamedBean().setNamedBean(turnout);
        expressionTurnout.set_Is_IsNot(Is_IsNot_Enum.Is);
        expressionTurnout.setBeanState(ExpressionTurnout.TurnoutState.Closed);
        assertEquals( "Turnout IT1 is Closed", expressionTurnout.getLongDescription());
        expressionTurnout.set_Is_IsNot(Is_IsNot_Enum.IsNot);
        assertEquals( "Turnout IT1 is not Closed", expressionTurnout.getLongDescription());
        expressionTurnout.setBeanState(ExpressionTurnout.TurnoutState.Other);
        assertEquals( "Turnout IT1 is not Other", expressionTurnout.getLongDescription());
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
        assertFalse( atomicBoolean.get(), "atomicBoolean is false");
        // Throw the switch. This should not execute the conditional.
        turnout.setCommandedState(Turnout.THROWN);
        // The conditionalNG is not yet enabled so it shouldn't be executed.
        // So the atomic boolean should be false
        assertFalse( atomicBoolean.get(), "atomicBoolean is false");
        // Close the switch. This should not execute the conditional.
        turnout.setCommandedState(Turnout.CLOSED);
        // Enable the conditionalNG and all its children.
        conditionalNG.setEnabled(true);
        // The action is not yet executed so the atomic boolean should be false
        assertFalse( atomicBoolean.get(), "atomicBoolean is false");
        // Throw the switch. This should execute the conditional.
        turnout.setCommandedState(Turnout.THROWN);
        // The action should now be executed so the atomic boolean should be true
        assertTrue( atomicBoolean.get(), "atomicBoolean is true");
        // Clear the atomic boolean.
        atomicBoolean.set(false);
        // Close the switch. This should not execute the conditional.
        turnout.setCommandedState(Turnout.CLOSED);
        // The action should now be executed so the atomic boolean should be true
        assertFalse( atomicBoolean.get(), "atomicBoolean is false");

        // Test IS_NOT
        expressionTurnout.set_Is_IsNot(Is_IsNot_Enum.IsNot);
        // Throw the switch. This should not execute the conditional.
        turnout.setCommandedState(Turnout.THROWN);
        // The action should now be executed so the atomic boolean should be true
        assertFalse( atomicBoolean.get(), "atomicBoolean is false");
        // Close the switch. This should not execute the conditional.
        turnout.setCommandedState(Turnout.CLOSED);
        // The action should now be executed so the atomic boolean should be true
        assertTrue( atomicBoolean.get(), "atomicBoolean is true");
    }

    @Test
    public void testSetTurnout() {
        expressionTurnout.unregisterListeners();

        Turnout otherTurnout = InstanceManager.getDefault(TurnoutManager.class).provide("IM99");
        assertNotEquals( otherTurnout, expressionTurnout.getSelectNamedBean().getNamedBean().getBean(),
                "Turnouts are different");
        expressionTurnout.getSelectNamedBean().setNamedBean(otherTurnout);
        assertEquals( otherTurnout, expressionTurnout.getSelectNamedBean().getNamedBean().getBean(), "Turnouts are equal");

        NamedBeanHandle<Turnout> otherTurnoutHandle =
                InstanceManager.getDefault(NamedBeanHandleManager.class)
                        .getNamedBeanHandle(otherTurnout.getDisplayName(), otherTurnout);
        expressionTurnout.getSelectNamedBean().removeNamedBean();
        assertNull( expressionTurnout.getSelectNamedBean().getNamedBean(), "Turnout is null");
        expressionTurnout.getSelectNamedBean().setNamedBean(otherTurnoutHandle);
        assertEquals( otherTurnout, expressionTurnout.getSelectNamedBean().getNamedBean().getBean(),
                "Turnouts are equal");
        assertEquals( otherTurnoutHandle, expressionTurnout.getSelectNamedBean().getNamedBean(),
                "TurnoutHandles are equal");
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
        assertNull( expressionTurnout.getSelectNamedBean().getNamedBean(), "turnout handle is null");

        expressionTurnout.getSelectNamedBean().setNamedBean(turnout11);
        assertSame( turnout11, expressionTurnout.getSelectNamedBean().getNamedBean().getBean(), "turnout is correct");

        expressionTurnout.getSelectNamedBean().removeNamedBean();
        assertNull( expressionTurnout.getSelectNamedBean().getNamedBean(), "turnout handle is null");

        expressionTurnout.getSelectNamedBean().setNamedBean(turnoutHandle12);
        assertSame( turnoutHandle12, expressionTurnout.getSelectNamedBean().getNamedBean(), "turnout handle is correct");

        expressionTurnout.getSelectNamedBean().setNamedBean("A non existent turnout");
        assertNull( expressionTurnout.getSelectNamedBean().getNamedBean(), "turnout handle is null");
        JUnitAppender.assertErrorMessage("Turnout \"A non existent turnout\" is not found");

        expressionTurnout.getSelectNamedBean().setNamedBean(turnout13.getSystemName());
        assertSame( turnout13, expressionTurnout.getSelectNamedBean().getNamedBean().getBean(), "turnout is correct");

        String userName = turnout14.getUserName();
        assertNotNull( userName, "turnout is not null");
        expressionTurnout.getSelectNamedBean().setNamedBean(userName);
        assertSame( turnout14, expressionTurnout.getSelectNamedBean().getNamedBean().getBean(), "turnout is correct");
    }

    @Test
    public void testSetTurnoutException() {
        assertNotNull( turnout, "Turnout is not null");
        assertNotNull( expressionTurnout.getSelectNamedBean().getNamedBean(), "Turnout is not null");
        expressionTurnout.registerListeners();
        RuntimeException ex = assertThrows( RuntimeException.class, () ->
            expressionTurnout.getSelectNamedBean().setNamedBean("A turnout"),
                "Expected exception thrown");
        assertNotNull(ex);
        JUnitAppender.assertErrorMessage("setNamedBean must not be called when listeners are registered");

        ex = assertThrows( RuntimeException.class, () -> {
            Turnout turnout99 = InstanceManager.getDefault(TurnoutManager.class).provide("IT99");
            NamedBeanHandle<Turnout> turnoutHandle99 =
                    InstanceManager.getDefault(NamedBeanHandleManager.class).getNamedBeanHandle(turnout99.getDisplayName(), turnout99);
            expressionTurnout.getSelectNamedBean().setNamedBean(turnoutHandle99);
        }, "Expected exception thrown");
        assertNotNull(ex);
        JUnitAppender.assertErrorMessage("setNamedBean must not be called when listeners are registered");

        ex = assertThrows( RuntimeException.class, () ->
            expressionTurnout.getSelectNamedBean().removeNamedBean(),
                "Expected exception thrown");
        assertNotNull(ex);
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
        assertNotNull( turnout, "Turnout is not null");
        expressionTurnout.getSelectNamedBean().setNamedBean(turnout);

        // Get some other turnout for later use
        Turnout otherTurnout = InstanceManager.getDefault(TurnoutManager.class).provide("IM99");
        assertNotNull( otherTurnout, "Turnout is not null");
        assertNotEquals( turnout, otherTurnout, "Turnout is not equal");

        // Test vetoableChange() for some other propery
        expressionTurnout.vetoableChange(new PropertyChangeEvent(this, "CanSomething", "test", null));
        assertEquals( turnout, expressionTurnout.getSelectNamedBean().getNamedBean().getBean(), "Turnout matches");

        // Test vetoableChange() for a string
        expressionTurnout.vetoableChange(new PropertyChangeEvent(this, "CanDelete", "test", null));
        assertEquals( turnout, expressionTurnout.getSelectNamedBean().getNamedBean().getBean(), "Turnout matches");
        expressionTurnout.vetoableChange(new PropertyChangeEvent(this, "DoDelete", "test", null));
        assertEquals( turnout, expressionTurnout.getSelectNamedBean().getNamedBean().getBean(), "Turnout matches");

        // Test vetoableChange() for another turnout
        expressionTurnout.vetoableChange(new PropertyChangeEvent(this, "CanDelete", otherTurnout, null));
        assertEquals( turnout, expressionTurnout.getSelectNamedBean().getNamedBean().getBean(), "Turnout matches");
        expressionTurnout.vetoableChange(new PropertyChangeEvent(this, "DoDelete", otherTurnout, null));
        assertEquals( turnout, expressionTurnout.getSelectNamedBean().getNamedBean().getBean(), "Turnout matches");

        // Test vetoableChange() for its own turnout
        PropertyVetoException ex = assertThrows( PropertyVetoException.class, () ->
            expressionTurnout.getSelectNamedBean().vetoableChange(
                    new PropertyChangeEvent(this, "CanDelete", turnout, null)),
                "Expected exception thrown");
        assertNotNull(ex);

        assertEquals( turnout, expressionTurnout.getSelectNamedBean().getNamedBean().getBean(), "Turnout matches");
        expressionTurnout.getSelectNamedBean().vetoableChange(new PropertyChangeEvent(this, "DoDelete", turnout, null));
        assertNull( expressionTurnout.getSelectNamedBean().getNamedBean(), "Turnout is null");
    }

    @BeforeEach
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

        assertTrue( logixNG.setParentForAllChildren(new ArrayList<>()));
        logixNG.activate();
        logixNG.setEnabled(true);
    }

    @AfterEach
    public void tearDown() {
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
