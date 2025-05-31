package jmri.jmrit.logixng.expressions;

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

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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
        Assert.assertNotNull("transit is not null", transit1);
        transit1.setState(Transit.IDLE);

        expression2 = new ExpressionTransit("IQDE321", null);
        Assert.assertNotNull("object exists", expression2);
        Assert.assertNull("Username matches", expression2.getUserName());
        Assert.assertEquals("String matches", "Transit \"''\" is Idle", expression2.getLongDescription());

        expression2 = new ExpressionTransit("IQDE321", "My Transit");
        Assert.assertNotNull("object exists", expression2);
        Assert.assertEquals("Username matches", "My Transit", expression2.getUserName());
        Assert.assertEquals("String matches", "Transit \"''\" is Idle", expression2.getLongDescription());

        expression2 = new ExpressionTransit("IQDE321", null);
        expression2.getSelectNamedBean().setNamedBean(transit1);
        Assert.assertTrue("transit is correct", transit1 == expression2.getSelectNamedBean().getNamedBean().getBean());
        Assert.assertNotNull("object exists", expression2);
        Assert.assertNull("Username matches", expression2.getUserName());
        Assert.assertEquals("String matches", "Transit \"transit1\" is Idle", expression2.getLongDescription());

        Transit t = InstanceManager.getDefault(TransitManager.class).createNewTransit("transit2");
        expression2 = new ExpressionTransit("IQDE321", "My transit");
        expression2.getSelectNamedBean().setNamedBean(t);
        Assert.assertTrue("transit is correct", t == expression2.getSelectNamedBean().getNamedBean().getBean());
        Assert.assertNotNull("object exists", expression2);
        Assert.assertEquals("Username matches", "My transit", expression2.getUserName());
        Assert.assertEquals("String matches", "Transit \"transit2\" is Idle", expression2.getLongDescription());

        boolean thrown = false;
        try {
            // Illegal system name
            new ExpressionTransit("IQE55:12:XY11", null);
        } catch (IllegalArgumentException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);

        thrown = false;
        try {
            // Illegal system name
            new ExpressionTransit("IQE55:12:XY11", "A name");
        } catch (IllegalArgumentException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
    }

    @Test
    public void testGetChild() {
        Assert.assertTrue("getChildCount() returns 0", 0 == expressionTransit.getChildCount());

        boolean hasThrown = false;
        try {
            expressionTransit.getChild(0);
        } catch (UnsupportedOperationException ex) {
            hasThrown = true;
            Assert.assertEquals("Error message is correct", "Not supported.", ex.getMessage());
        }
        Assert.assertTrue("Exception is thrown", hasThrown);
    }

    @Test
    public void testTransitState() {
        Assert.assertEquals("String matches", "Idle", ExpressionTransit.TransitState.Idle.toString());
        Assert.assertEquals("String matches", "Assigned", ExpressionTransit.TransitState.Assigned.toString());

        Assert.assertEquals("ID matches", Transit.IDLE, ExpressionTransit.TransitState.Idle.getID());
        Assert.assertEquals("ID matches", Transit.ASSIGNED, ExpressionTransit.TransitState.Assigned.getID());
    }

    @Test
    public void testCategory() {
        Assert.assertTrue("Category matches", LogixNG_Category.ITEM == _base.getCategory());
    }

    @Test
    public void testDescription() {
        // Disable the conditionalNG. This will unregister the listeners
        conditionalNG.setEnabled(false);

        expressionTransit.getSelectNamedBean().removeNamedBean();
        Assert.assertEquals("Transit", expressionTransit.getShortDescription());
        Assert.assertEquals("Transit \"''\" is Idle", expressionTransit.getLongDescription());
        expressionTransit.getSelectNamedBean().setNamedBean(transit1);
        expressionTransit.set_Is_IsNot(Is_IsNot_Enum.Is);
        expressionTransit.getSelectEnum().setEnum(ExpressionTransit.TransitState.Idle);
        Assert.assertEquals("Transit \"transit1\" is Idle", expressionTransit.getLongDescription());
        expressionTransit.set_Is_IsNot(Is_IsNot_Enum.IsNot);
        Assert.assertEquals("Transit \"transit1\" is not Idle", expressionTransit.getLongDescription());
        expressionTransit.getSelectEnum().setEnum(ExpressionTransit.TransitState.Assigned);
        Assert.assertEquals("Transit \"transit1\" is not Assigned", expressionTransit.getLongDescription());
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
        Assert.assertFalse("atomicBoolean is false",atomicBoolean.get());

        // Enable the conditionalNG and all its children.
        conditionalNG.setEnabled(true);
        // The action is not yet executed so the atomic boolean should be false
        Assert.assertFalse("atomicBoolean is false",atomicBoolean.get());

        // Change the transit twice to trigger the "then" state
        transit1.setState(Transit.ASSIGNED);
        transit1.setState(Transit.IDLE);
        // The action should now be executed so the atomic boolean should be true
        Assert.assertTrue("atomicBoolean is true",atomicBoolean.get());

        // Clear the atomic boolean.
        atomicBoolean.set(false);
        // Change the transit to trigger the "else" state.
        transit1.setState(Transit.ASSIGNED);
        // The action should now be executed so the atomic boolean should still be false since the action is the else.
        Assert.assertFalse("atomicBoolean is false",atomicBoolean.get());

        // Test IS_NOT
        expressionTransit.set_Is_IsNot(Is_IsNot_Enum.IsNot);
        // Create two events to trigger on change to the "then" state.
        transit1.setState(Transit.IDLE);
        transit1.setState(Transit.ASSIGNED);
        Assert.assertTrue("atomicBoolean is true",atomicBoolean.get());
    }

    @Test
    public void testSetTransit() {
        expressionTransit.unregisterListeners();

        Transit otherTransit = InstanceManager.getDefault(TransitManager.class).createNewTransit("transitX");
        Assert.assertNotEquals("Transits are different", otherTransit, expressionTransit.getSelectNamedBean().getNamedBean().getBean());
        expressionTransit.getSelectNamedBean().setNamedBean(otherTransit);
        Assert.assertEquals("Transits are equal", otherTransit, expressionTransit.getSelectNamedBean().getNamedBean().getBean());

        NamedBeanHandle<Transit> otherTransitHandle =
                InstanceManager.getDefault(NamedBeanHandleManager.class)
                        .getNamedBeanHandle(otherTransit.getDisplayName(), otherTransit);
        expressionTransit.getSelectNamedBean().removeNamedBean();
        Assert.assertNull("Transit is null", expressionTransit.getSelectNamedBean().getNamedBean());
        expressionTransit.getSelectNamedBean().setNamedBean(otherTransitHandle);
        Assert.assertEquals("Transits are equal", otherTransit, expressionTransit.getSelectNamedBean().getNamedBean().getBean());
        Assert.assertEquals("TransitHandles are equal", otherTransitHandle, expressionTransit.getSelectNamedBean().getNamedBean());
    }

    @Test
    public void testSetTransitException() {
        Assert.assertNotNull("Transit is not null", transit1);
        Assert.assertNotNull("Transit is not null", expressionTransit.getSelectNamedBean().getNamedBean());
        expressionTransit.registerListeners();
        boolean thrown = false;
        try {
            expressionTransit.getSelectNamedBean().setNamedBean("A transit");
        } catch (RuntimeException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
        JUnitAppender.assertErrorMessage("setNamedBean must not be called when listeners are registered");

        thrown = false;
        try {
            Transit transit99 = InstanceManager.getDefault(TransitManager.class).createNewTransit("transit99");
            NamedBeanHandle<Transit> transitHandle99 =
                    InstanceManager.getDefault(NamedBeanHandleManager.class).getNamedBeanHandle(transit99.getDisplayName(), transit99);
            expressionTransit.getSelectNamedBean().setNamedBean(transitHandle99);
        } catch (RuntimeException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
        JUnitAppender.assertErrorMessage("setNamedBean must not be called when listeners are registered");

        thrown = false;
        try {
            expressionTransit.getSelectNamedBean().removeNamedBean();
        } catch (RuntimeException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
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
        Assert.assertNotNull("Transit is not null", transit1);
        expressionTransit.getSelectNamedBean().setNamedBean(transit1);

        // Get some other transit for later use
        Transit otherTransit = InstanceManager.getDefault(TransitManager.class).createNewTransit("transitQ");
        Assert.assertNotNull("Transit is not null", otherTransit);
        Assert.assertNotEquals("Transit is not equal", transit1, otherTransit);

        // Test vetoableChange() for some other propery
        expressionTransit.vetoableChange(new PropertyChangeEvent(this, "CanSomething", "test", null));
        Assert.assertEquals("Transit matches", transit1, expressionTransit.getSelectNamedBean().getNamedBean().getBean());

        // Test vetoableChange() for a string
        expressionTransit.vetoableChange(new PropertyChangeEvent(this, "CanDelete", "test", null));
        Assert.assertEquals("Transit matches", transit1, expressionTransit.getSelectNamedBean().getNamedBean().getBean());
        expressionTransit.vetoableChange(new PropertyChangeEvent(this, "DoDelete", "test", null));
        Assert.assertEquals("Transit matches", transit1, expressionTransit.getSelectNamedBean().getNamedBean().getBean());

        // Test vetoableChange() for another transit
        expressionTransit.vetoableChange(new PropertyChangeEvent(this, "CanDelete", otherTransit, null));
        Assert.assertEquals("Transit matches", transit1, expressionTransit.getSelectNamedBean().getNamedBean().getBean());
        expressionTransit.vetoableChange(new PropertyChangeEvent(this, "DoDelete", otherTransit, null));
        Assert.assertEquals("Transit matches", transit1, expressionTransit.getSelectNamedBean().getNamedBean().getBean());

        // Test vetoableChange() for its own transit
        boolean thrown = false;
        try {
            expressionTransit.getSelectNamedBean().vetoableChange(new PropertyChangeEvent(this, "CanDelete", transit1, null));
        } catch (PropertyVetoException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);

        Assert.assertEquals("Transit matches", transit1, expressionTransit.getSelectNamedBean().getNamedBean().getBean());
        expressionTransit.getSelectNamedBean().vetoableChange(new PropertyChangeEvent(this, "DoDelete", transit1, null));
        Assert.assertNull("Transit is null", expressionTransit.getSelectNamedBean().getNamedBean());
    }

    // The minimal setup for log4J
    @Before
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

//     private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExpressionBlockTest.class);

}
