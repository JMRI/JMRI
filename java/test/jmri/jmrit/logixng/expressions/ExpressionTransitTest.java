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

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.ActionAtomicBoolean;
import jmri.jmrit.logixng.actions.IfThenElse;
import jmri.jmrit.logixng.implementation.DefaultConditionalNGScaffold;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test ExpressionTransit
 *
 * @author Dave Sand 2023
 */
public class ExpressionTransitTest extends AbstractDigitalExpressionTestBase {

    private LogixNG logixNG;
    private ConditionalNG conditionalNG;
    private ExpressionTransit expressionTransit;
    private ActionAtomicBoolean actionAtomicBoolean;
    private AtomicBoolean atomicBoolean;
    private Transit transit1;


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
        return String.format("Transit \"transit1\" is Idle ::: Use default%n");
    }

    @Override
    public String getExpectedPrintedTreeFromRoot() {
        return String.format(
                "LogixNG: A new logix for test%n" +
                "   ConditionalNG: A conditionalNG%n" +
                "      ! A%n" +
                "         If Then Else. Execute on change ::: Use default%n" +
                "            ? If%n" +
                "               Transit \"transit1\" is Idle ::: Use default%n" +
                "            ! Then%n" +
                "               Set the atomic boolean to true ::: Use default%n" +
                "            ! Else%n" +
                "               Socket not connected%n");
    }

    @Override
    public NamedBean createNewBean(String systemName) {
        return new ExpressionBlock(systemName, null);
    }

    @Override
    public boolean addNewSocket() {
        return false;
    }

    @Test
    public void testCtor() throws JmriException {
        ExpressionTransit expression2;
        assertNotNull( transit1, "transit is not null");
        transit1.setState(Transit.IDLE);

        expression2 = new ExpressionTransit("IQDE321", null);
        assertNotNull( expression2, "object exists");
        assertNull( expression2.getUserName(), "Username matches");
        assertEquals( "Transit \"''\" is Idle", expression2.getLongDescription(),
                "String matches");

        expression2 = new ExpressionTransit("IQDE321", "My Transit");
        assertNotNull( expression2, "object exists");
        assertEquals( "My Transit", expression2.getUserName(), "Username matches");
        assertEquals( "Transit \"''\" is Idle", expression2.getLongDescription(), "String matches");

        expression2 = new ExpressionTransit("IQDE321", null);
        expression2.getSelectNamedBean().setNamedBean(transit1);
        assertSame( transit1, expression2.getSelectNamedBean().getNamedBean().getBean(), "transit is correct");
        assertNotNull( expression2, "object exists");
        assertNull( expression2.getUserName(), "Username matches");
        assertEquals( "Transit \"transit1\" is Idle", expression2.getLongDescription(), "String matches");

        Transit t = InstanceManager.getDefault(TransitManager.class).createNewTransit("transit2");
        expression2 = new ExpressionTransit("IQDE321", "My transit");
        expression2.getSelectNamedBean().setNamedBean(t);
        assertSame( t, expression2.getSelectNamedBean().getNamedBean().getBean(), "transit is correct");
        assertNotNull( expression2, "object exists");
        assertEquals( "My transit", expression2.getUserName(), "Username matches");
        assertEquals( "Transit \"transit2\" is Idle", expression2.getLongDescription(), "String matches");

        IllegalArgumentException ex = assertThrows( IllegalArgumentException.class, () -> {
            var et = new ExpressionTransit("IQE55:12:XY11", null);
            fail("Should have thrown, not created " + et);
        }, "Illegal system name Expected exception thrown");
        assertNotNull(ex);

        ex = assertThrows( IllegalArgumentException.class, () -> {
            var et = new ExpressionTransit("IQE55:12:XY11", "A name");
            fail("Should have thrown, not created " + et);
        }, "Illegal system name Expected exception thrown");
        assertNotNull(ex);
    }

    @Test
    public void testGetChild() {
        assertEquals( 0, expressionTransit.getChildCount(), "getChildCount() returns 0");

        UnsupportedOperationException ex = assertThrows( UnsupportedOperationException.class, () ->
            expressionTransit.getChild(0), "Exception is thrown");
        assertEquals( "Not supported.", ex.getMessage(), "Error message is correct");
    }

    @Test
    public void testTransitState() {
        assertEquals( "Idle", ExpressionTransit.TransitState.Idle.toString(), "String matches");
        assertEquals( "Assigned", ExpressionTransit.TransitState.Assigned.toString(), "String matches");

        assertEquals( Transit.IDLE, ExpressionTransit.TransitState.Idle.getID(), "ID matches");
        assertEquals( Transit.ASSIGNED, ExpressionTransit.TransitState.Assigned.getID(), "ID matches");
    }

    @Test
    public void testCategory() {
        assertSame( LogixNG_Category.ITEM, _base.getCategory(), "Category matches");
    }

    @Test
    public void testDescription() {
        // Disable the conditionalNG. This will unregister the listeners
        conditionalNG.setEnabled(false);

        expressionTransit.getSelectNamedBean().removeNamedBean();
        assertEquals("Transit", expressionTransit.getShortDescription());
        assertEquals("Transit \"''\" is Idle", expressionTransit.getLongDescription());
        expressionTransit.getSelectNamedBean().setNamedBean(transit1);
        expressionTransit.set_Is_IsNot(Is_IsNot_Enum.Is);
        expressionTransit.getSelectEnum().setEnum(ExpressionTransit.TransitState.Idle);
        assertEquals("Transit \"transit1\" is Idle", expressionTransit.getLongDescription());
        expressionTransit.set_Is_IsNot(Is_IsNot_Enum.IsNot);
        assertEquals("Transit \"transit1\" is not Idle", expressionTransit.getLongDescription());
        expressionTransit.getSelectEnum().setEnum(ExpressionTransit.TransitState.Assigned);
        assertEquals("Transit \"transit1\" is not Assigned", expressionTransit.getLongDescription());
    }

    @Test
    public void testExpression() throws SocketAlreadyConnectedException, JmriException {
        // Disable the conditionalNG. This will unregister the listeners
        conditionalNG.setEnabled(false);

        // Set initial states: Transit and expression states are "is Idle"
        atomicBoolean.set(false);
        transit1.setState(Transit.IDLE);
        expressionTransit.getSelectNamedBean().setNamedBean(transit1);
        expressionTransit.set_Is_IsNot(Is_IsNot_Enum.Is);
        expressionTransit.getSelectEnum().setEnum(ExpressionTransit.TransitState.Idle);

        // Toggle the transit twice since Idle is 'then' action. This should not execute the conditional.
        transit1.setState(Transit.ASSIGNED);
        transit1.setState(Transit.IDLE);
        // The conditionalNG is not yet enabled so it shouldn't be executed.
        // So the atomic boolean should be false
        assertFalse( atomicBoolean.get(), "atomicBoolean is false");

        // Enable the conditionalNG and all its children.
        conditionalNG.setEnabled(true);
        // The action is not yet executed so the atomic boolean should be false
        assertFalse( atomicBoolean.get(), "atomicBoolean is false");

        // Change the transit twice to trigger the "then" state
        transit1.setState(Transit.ASSIGNED);
        transit1.setState(Transit.IDLE);
        // The action should now be executed so the atomic boolean should be true
        assertTrue( atomicBoolean.get(), "atomicBoolean is true");

        // Clear the atomic boolean.
        atomicBoolean.set(false);
        // Change the transit to trigger the "else" state.
        transit1.setState(Transit.ASSIGNED);
        // The action should now be executed so the atomic boolean should still be false since the action is the else.
        assertFalse( atomicBoolean.get(), "atomicBoolean is false");

        // Test IS_NOT
        expressionTransit.set_Is_IsNot(Is_IsNot_Enum.IsNot);
        // Create two events to trigger on change to the "then" state.
        transit1.setState(Transit.IDLE);
        transit1.setState(Transit.ASSIGNED);
        assertTrue( atomicBoolean.get(), "atomicBoolean is true");
    }

    @Test
    public void testSetTransit() {
        expressionTransit.unregisterListeners();

        Transit otherTransit = InstanceManager.getDefault(TransitManager.class).createNewTransit("transitX");
        assertNotEquals( otherTransit, expressionTransit.getSelectNamedBean().getNamedBean().getBean(), "Transits are different");
        expressionTransit.getSelectNamedBean().setNamedBean(otherTransit);
        assertEquals( otherTransit, expressionTransit.getSelectNamedBean().getNamedBean().getBean(), "Transits are equal");

        NamedBeanHandle<Transit> otherTransitHandle =
                InstanceManager.getDefault(NamedBeanHandleManager.class)
                        .getNamedBeanHandle(otherTransit.getDisplayName(), otherTransit);
        expressionTransit.getSelectNamedBean().removeNamedBean();
        assertNull( expressionTransit.getSelectNamedBean().getNamedBean(), "Transit is null");
        expressionTransit.getSelectNamedBean().setNamedBean(otherTransitHandle);
        assertEquals( otherTransit, expressionTransit.getSelectNamedBean().getNamedBean().getBean(), "Transits are equal");
        assertEquals( otherTransitHandle, expressionTransit.getSelectNamedBean().getNamedBean(), "TransitHandles are equal");
    }

    @Test
    public void testSetTransitException() {
        assertNotNull( transit1, "Transit is not null");
        assertNotNull( expressionTransit.getSelectNamedBean().getNamedBean(), "Transit is not null");
        expressionTransit.registerListeners();
        RuntimeException ex = assertThrows( RuntimeException.class, () ->
            expressionTransit.getSelectNamedBean().setNamedBean("A transit"),
                "Expected exception thrown");
        assertNotNull(ex);
        JUnitAppender.assertErrorMessage("setNamedBean must not be called when listeners are registered");

        ex = assertThrows( RuntimeException.class, () -> {
            Transit transit99 = InstanceManager.getDefault(TransitManager.class).createNewTransit("transit99");
            NamedBeanHandle<Transit> transitHandle99 =
                    InstanceManager.getDefault(NamedBeanHandleManager.class).getNamedBeanHandle(transit99.getDisplayName(), transit99);
            expressionTransit.getSelectNamedBean().setNamedBean(transitHandle99);
        }, "Expected exception thrown");
        assertNotNull(ex);
        JUnitAppender.assertErrorMessage("setNamedBean must not be called when listeners are registered");

        ex = assertThrows( RuntimeException.class, () ->
            expressionTransit.getSelectNamedBean().removeNamedBean(),
                "Expected exception thrown");
        assertNotNull(ex);
        JUnitAppender.assertErrorMessage("setNamedBean must not be called when listeners are registered");
    }

    @Test
    public void testRegisterListeners() {
        // Test registerListeners() when the ExpressionBlock has no block
        conditionalNG.setEnabled(false);
        expressionTransit.getSelectNamedBean().removeNamedBean();
        conditionalNG.setEnabled(true);
    }

    @Test
    public void testVetoableChange() throws PropertyVetoException {
        // Disable the conditionalNG. This will unregister the listeners
        conditionalNG.setEnabled(false);

        // Get the expressionTransit and set the transit
        assertNotNull( transit1, "Transit is not null");
        expressionTransit.getSelectNamedBean().setNamedBean(transit1);

        // Get some other transit for later use
        Transit otherTransit = InstanceManager.getDefault(TransitManager.class).createNewTransit("transitQ");
        assertNotNull( otherTransit, "Transit is not null");
        assertNotEquals( transit1, otherTransit, "Transit is not equal");

        // Test vetoableChange() for some other propery
        expressionTransit.vetoableChange(new PropertyChangeEvent(this, "CanSomething", "test", null));
        assertEquals( transit1, expressionTransit.getSelectNamedBean().getNamedBean().getBean(), "Transit matches");

        // Test vetoableChange() for a string
        expressionTransit.vetoableChange(new PropertyChangeEvent(this, "CanDelete", "test", null));
        assertEquals( transit1, expressionTransit.getSelectNamedBean().getNamedBean().getBean(), "Transit matches");
        expressionTransit.vetoableChange(new PropertyChangeEvent(this, "DoDelete", "test", null));
        assertEquals( transit1, expressionTransit.getSelectNamedBean().getNamedBean().getBean(), "Transit matches");

        // Test vetoableChange() for another transit
        expressionTransit.vetoableChange(new PropertyChangeEvent(this, "CanDelete", otherTransit, null));
        assertEquals( transit1, expressionTransit.getSelectNamedBean().getNamedBean().getBean(), "Transit matches");
        expressionTransit.vetoableChange(new PropertyChangeEvent(this, "DoDelete", otherTransit, null));
        assertEquals( transit1, expressionTransit.getSelectNamedBean().getNamedBean().getBean(), "Transit matches");

        // Test vetoableChange() for its own transit
        PropertyVetoException ex = assertThrows( PropertyVetoException.class, () ->
            expressionTransit.getSelectNamedBean().vetoableChange(
                    new PropertyChangeEvent(this, "CanDelete", transit1, null)),
                "Expected exception thrown");
        assertNotNull(ex);

        assertEquals( transit1, expressionTransit.getSelectNamedBean().getNamedBean().getBean(), "Transit matches");
        expressionTransit.getSelectNamedBean().vetoableChange(new PropertyChangeEvent(this, "DoDelete", transit1, null));
        assertNull( expressionTransit.getSelectNamedBean().getNamedBean(), "Transit is null");
    }

    @BeforeEach
    public void setUp() throws SocketAlreadyConnectedException {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalSensorManager();
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

        expressionTransit = new ExpressionTransit("IQDE321", null);
        MaleSocket maleSocket2 =
                InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expressionTransit);
        ifThenElse.getChild(0).connect(maleSocket2);

        _base = expressionTransit;
        _baseMaleSocket = maleSocket2;

        atomicBoolean = new AtomicBoolean(false);
        actionAtomicBoolean = new ActionAtomicBoolean(atomicBoolean, true);
        MaleSocket socketAtomicBoolean = InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionAtomicBoolean);
        ifThenElse.getChild(1).connect(socketAtomicBoolean);

        transit1 = InstanceManager.getDefault(TransitManager.class).createNewTransit("transit1");
        expressionTransit.getSelectNamedBean().setNamedBean(transit1);

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

//     private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExpressionBlockTest.class);

}
