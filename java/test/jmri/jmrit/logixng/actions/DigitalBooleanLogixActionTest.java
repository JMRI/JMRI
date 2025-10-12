package jmri.jmrit.logixng.actions;

import java.util.ArrayList;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.expressions.ExpressionSensor;
import jmri.jmrit.logixng.actions.DigitalBooleanLogixAction.When;
import jmri.jmrit.logixng.implementation.DefaultConditionalNGScaffold;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test OnChange
 *
 * @author Daniel Bergqvist 2018
 */
public class DigitalBooleanLogixActionTest extends AbstractDigitalBooleanActionTestBase {

    LogixNG logixNG;
    ConditionalNG conditionalNG;
    DigitalBooleanLogixAction _actionOnChange;
    ActionTurnout _actionTurnout;

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
        DigitalActionBean childAction =
                new jmri.jmrit.logixng.actions.DigitalMany("IQDA999", null);
        MaleSocket maleSocketChild =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(childAction);
        return maleSocketChild;
    }

    @Override
    public NamedBean createNewBean(String systemName) {
        return new DigitalBooleanLogixAction(systemName, null, When.Either);
    }

    @Override
    public String getExpectedPrintedTree() {
        return String.format(
                "Logix Action. True ::: Use default%n" +
                "   ! A%n" +
                "      Set turnout '' to state Thrown ::: Use default%n");
    }

    @Override
    public String getExpectedPrintedTreeFromRoot() {
        return String.format(
                "LogixNG: A new logix for test%n" +
                "   ConditionalNG: A conditionalNG%n" +
                "      ! A%n" +
                "         Logix. Execute Actions on change of state only ::: Use default%n" +
                "            ? E%n" +
                "               Sensor '' is Active ::: Use default%n" +
                "            !b A%n" +
                "               Logix Action. True ::: Use default%n" +
                "                  ! A%n" +
                "                     Set turnout '' to state Thrown ::: Use default%n");
    }

    @Override
    public boolean addNewSocket() {
        return false;
    }

    @Test
    public void testCtor() {
        DigitalBooleanActionBean t = new DigitalBooleanLogixAction("IQDB321", null, DigitalBooleanLogixAction.When.Either);
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testCtorAndSetup1() {
        DigitalBooleanLogixAction action = new DigitalBooleanLogixAction("IQDB321", null, DigitalBooleanLogixAction.When.Either);
        Assert.assertNotNull("exists", action);
        Assert.assertEquals("action has 1 female socket", 1, action.getChildCount());
        action.getChild(0).setName("ZH12");
        action.setActionSocketSystemName("IQDA554");

        Assert.assertEquals("action female socket name is ZH12",
                "ZH12", action.getChild(0).getName());
        Assert.assertEquals("action female socket is of correct class",
                "jmri.jmrit.logixng.implementation.DefaultFemaleDigitalActionSocket",
                action.getChild(0).getClass().getName());
        Assert.assertFalse("action female socket is not connected",
                action.getChild(0).isConnected());

        // Setup action. This connects the child actions to this action
        action.setup();

        jmri.util.JUnitAppender.assertMessage("cannot load digital action IQDA554");

        Assert.assertEquals("action female socket name is ZH12",
                "ZH12", action.getChild(0).getName());
        Assert.assertEquals("action female socket is of correct class",
                "jmri.jmrit.logixng.implementation.DefaultFemaleDigitalActionSocket",
                action.getChild(0).getClass().getName());
        Assert.assertFalse("action female socket is not connected",
                action.getChild(0).isConnected());

        Assert.assertEquals("action has 1 female socket", 1, action.getChildCount());
    }

    @Test
    public void testCtorAndSetup2() {
        DigitalBooleanLogixAction action = new DigitalBooleanLogixAction("IQDB321", null, DigitalBooleanLogixAction.When.Either);
        Assert.assertNotNull("exists", action);
        Assert.assertEquals("action has 1 female socket", 1, action.getChildCount());
        action.getChild(0).setName("ZH12");
        action.setActionSocketSystemName(null);

        Assert.assertEquals("action female socket name is ZH12",
                "ZH12", action.getChild(0).getName());
        Assert.assertEquals("action female socket is of correct class",
                "jmri.jmrit.logixng.implementation.DefaultFemaleDigitalActionSocket",
                action.getChild(0).getClass().getName());
        Assert.assertFalse("action female socket is not connected",
                action.getChild(0).isConnected());

        // Setup action. This connects the child actions to this action
        action.setup();

        Assert.assertEquals("action female socket name is ZH12",
                "ZH12", action.getChild(0).getName());
        Assert.assertEquals("action female socket is of correct class",
                "jmri.jmrit.logixng.implementation.DefaultFemaleDigitalActionSocket",
                action.getChild(0).getClass().getName());
        Assert.assertFalse("action female socket is not connected",
                action.getChild(0).isConnected());

        Assert.assertEquals("action has 1 female socket", 1, action.getChildCount());
    }

    @Test
    public void testCtorAndSetup3() {
        DigitalActionManager m1 = InstanceManager.getDefault(DigitalActionManager.class);

        MaleSocket childSocket0 = m1.registerAction(new ActionMemory("IQDA554", null));

        DigitalBooleanLogixAction action = new DigitalBooleanLogixAction("IQDB321", null, DigitalBooleanLogixAction.When.Either);
        Assert.assertNotNull("exists", action);
        Assert.assertEquals("action has 1 female socket", 1, action.getChildCount());
        action.getChild(0).setName("ZH12");
        action.setActionSocketSystemName("IQDA554");

        Assert.assertEquals("action female socket name is ZH12",
                "ZH12", action.getChild(0).getName());
        Assert.assertEquals("action female socket is of correct class",
                "jmri.jmrit.logixng.implementation.DefaultFemaleDigitalActionSocket",
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
        Assert.assertTrue("getChildCount() returns 1", 1 == _actionOnChange.getChildCount());

        Assert.assertNotNull("getChild(0) returns a non null value",
                _actionOnChange.getChild(0));

        boolean hasThrown = false;
        try {
            _actionOnChange.getChild(1);
        } catch (IllegalArgumentException ex) {
            hasThrown = true;
            Assert.assertEquals("Error message is correct", "index has invalid value: 1", ex.getMessage());
        }
        Assert.assertTrue("Exception is thrown", hasThrown);
    }

    @Test
    public void testCategory() {
        Assert.assertTrue("Category matches", LogixNG_Category.COMMON == _base.getCategory());
    }

    @Test
    public void testGetShortDescription() {
        DigitalBooleanActionBean a1 = new DigitalBooleanLogixAction("IQDB321", null, DigitalBooleanLogixAction.When.True);
        Assert.assertEquals("strings are equal", "Logix Action", a1.getShortDescription());
        DigitalBooleanActionBean a2 = new DigitalBooleanLogixAction("IQDB322", null, DigitalBooleanLogixAction.When.False);
        Assert.assertEquals("strings are equal", "Logix Action", a2.getShortDescription());
        DigitalBooleanActionBean a3 = new DigitalBooleanLogixAction("IQDB323", null, DigitalBooleanLogixAction.When.Either);
        Assert.assertEquals("strings are equal", "Logix Action", a3.getShortDescription());
    }

    @Test
    public void testGetLongDescription() {
        DigitalBooleanActionBean a1 = new DigitalBooleanLogixAction("IQDB321", null, DigitalBooleanLogixAction.When.True);
        Assert.assertEquals("strings are equal", "Logix Action. True", a1.getLongDescription());
        DigitalBooleanActionBean a2 = new DigitalBooleanLogixAction("IQDB322", null, DigitalBooleanLogixAction.When.False);
        Assert.assertEquals("strings are equal", "Logix Action. False", a2.getLongDescription());
        DigitalBooleanActionBean a3 = new DigitalBooleanLogixAction("IQDB323", null, DigitalBooleanLogixAction.When.Either);
        Assert.assertEquals("strings are equal", "Logix Action. Either", a3.getLongDescription());
    }

    @Test
    public void testTrigger() {
        _actionOnChange.setTrigger(When.Either);
        Assert.assertEquals(When.Either, _actionOnChange.getTrigger());

        _actionOnChange.setTrigger(When.False);
        Assert.assertEquals(When.False, _actionOnChange.getTrigger());

        _actionOnChange.setTrigger(When.True);
        Assert.assertEquals(When.True, _actionOnChange.getTrigger());
    }

    @Test
    public void testExecute() throws JmriException {

        Turnout turnout = InstanceManager.getDefault(TurnoutManager.class).provide("IT1");
        _actionTurnout.getSelectNamedBean().setNamedBean(turnout);
        _actionTurnout.getSelectEnum().setEnum(ActionTurnout.TurnoutState.Thrown);

        // Ensure last execute is false
        _actionOnChange.execute(false);

        // Test Trigger.CHANGE
        _actionOnChange.setTrigger(When.Either);
        turnout.setState(Turnout.CLOSED);
        _actionOnChange.execute(true);
        Assert.assertEquals(Turnout.THROWN, turnout.getState());
        turnout.setState(Turnout.CLOSED);
        _actionOnChange.execute(false);
        Assert.assertEquals(Turnout.THROWN, turnout.getState());
        turnout.setState(Turnout.CLOSED);
        _actionOnChange.execute(true);
        Assert.assertEquals(Turnout.THROWN, turnout.getState());

        // Ensure last execute is false
        _actionOnChange.execute(false);

        // Test Trigger.CHANGE_TO_FALSE
        _actionOnChange.setTrigger(When.False);
        turnout.setState(Turnout.CLOSED);
        _actionOnChange.execute(true);
        Assert.assertEquals(Turnout.CLOSED, turnout.getState());
        turnout.setState(Turnout.CLOSED);
        _actionOnChange.execute(false);
        Assert.assertEquals(Turnout.THROWN, turnout.getState());
        turnout.setState(Turnout.CLOSED);
        _actionOnChange.execute(true);
        Assert.assertEquals(Turnout.CLOSED, turnout.getState());

        // Ensure last execute is false
        _actionOnChange.execute(false);

        // Test Trigger.CHANGE_TO_TRUE
        _actionOnChange.setTrigger(When.True);
        turnout.setState(Turnout.CLOSED);
        _actionOnChange.execute(true);
        Assert.assertEquals(Turnout.THROWN, turnout.getState());
        turnout.setState(Turnout.CLOSED);
        _actionOnChange.execute(false);
        Assert.assertEquals(Turnout.CLOSED, turnout.getState());
        turnout.setState(Turnout.CLOSED);
        _actionOnChange.execute(true);
        Assert.assertEquals(Turnout.THROWN, turnout.getState());
    }

    // The minimal setup for log4J
    @Before
    public void setUp() throws SocketAlreadyConnectedException {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();

        InstanceManager.getDefault(LogixNGPreferences.class).setInstallDebugger(false);
        JUnitUtil.initLogixNGManager();

        _category = LogixNG_Category.COMMON;
        _isExternal = false;

        logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test");  // NOI18N
        conditionalNG = new DefaultConditionalNGScaffold("IQC1", "A conditionalNG");  // NOI18N;
        InstanceManager.getDefault(ConditionalNG_Manager.class).register(conditionalNG);
        conditionalNG.setEnabled(true);
        conditionalNG.setRunDelayed(false);
        logixNG.addConditionalNG(conditionalNG);
        Logix action = new Logix("IQDA321", null);
        MaleSocket maleSocket =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
        conditionalNG.getChild(0).connect(maleSocket);

        ExpressionSensor expressionSensor = new ExpressionSensor("IQDE321", null);
        MaleSocket maleSocket2 =
                InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expressionSensor);
        action.getChild(0).connect(maleSocket2);

        _actionOnChange = new DigitalBooleanLogixAction("IQDB322", null, DigitalBooleanLogixAction.When.True);
        MaleSocket maleSocketActionOnChange =
                InstanceManager.getDefault(DigitalBooleanActionManager.class).registerAction(_actionOnChange);
        action.getChild(1).connect(maleSocketActionOnChange);

        _actionTurnout = new ActionTurnout("IQDA322", null);
        maleSocket2 =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(_actionTurnout);
        _actionOnChange.getChild(0).connect(maleSocket2);

        _base = _actionOnChange;
        _baseMaleSocket = maleSocketActionOnChange;

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
