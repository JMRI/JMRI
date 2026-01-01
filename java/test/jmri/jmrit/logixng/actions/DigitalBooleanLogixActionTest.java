package jmri.jmrit.logixng.actions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.expressions.ExpressionSensor;
import jmri.jmrit.logixng.actions.DigitalBooleanLogixAction.When;
import jmri.jmrit.logixng.implementation.DefaultConditionalNGScaffold;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Before;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test OnChange
 *
 * @author Daniel Bergqvist 2018
 */
public class DigitalBooleanLogixActionTest extends AbstractDigitalBooleanActionTestBase {

    private LogixNG logixNG;
    private ConditionalNG conditionalNG;
    private DigitalBooleanLogixAction _actionOnChange;
    private ActionTurnout _actionTurnout;

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
        assertNotNull( t, "exists");
    }

    @Test
    public void testCtorAndSetup1() {
        DigitalBooleanLogixAction action = new DigitalBooleanLogixAction("IQDB321", null, DigitalBooleanLogixAction.When.Either);
        assertNotNull( action, "exists");
        assertEquals( 1, action.getChildCount(), "action has 1 female socket");
        action.getChild(0).setName("ZH12");
        action.setActionSocketSystemName("IQDA554");

        assertEquals( "ZH12", action.getChild(0).getName(),
                "action female socket name is ZH12");
        assertEquals( "jmri.jmrit.logixng.implementation.DefaultFemaleDigitalActionSocket",
                action.getChild(0).getClass().getName(),
                "action female socket is of correct class");
        assertFalse( action.getChild(0).isConnected(),
                "action female socket is not connected");

        // Setup action. This connects the child actions to this action
        action.setup();

        jmri.util.JUnitAppender.assertMessage("cannot load digital action IQDA554");

        assertEquals( "ZH12", action.getChild(0).getName(),
                "action female socket name is ZH12");
        assertEquals( "jmri.jmrit.logixng.implementation.DefaultFemaleDigitalActionSocket",
                action.getChild(0).getClass().getName(),
                "action female socket is of correct class");
        assertFalse( action.getChild(0).isConnected(),
                "action female socket is not connected");

        assertEquals( 1, action.getChildCount(), "action has 1 female socket");
    }

    @Test
    public void testCtorAndSetup2() {
        DigitalBooleanLogixAction action = new DigitalBooleanLogixAction("IQDB321", null, DigitalBooleanLogixAction.When.Either);
        assertNotNull( action, "exists");
        assertEquals( 1, action.getChildCount(), "action has 1 female socket");
        action.getChild(0).setName("ZH12");
        action.setActionSocketSystemName(null);

        assertEquals( "ZH12", action.getChild(0).getName(),
                "action female socket name is ZH12");
        assertEquals( "jmri.jmrit.logixng.implementation.DefaultFemaleDigitalActionSocket",
                action.getChild(0).getClass().getName(),
                "action female socket is of correct class");
        assertFalse( action.getChild(0).isConnected(),
                "action female socket is not connected");

        // Setup action. This connects the child actions to this action
        action.setup();

        assertEquals( "ZH12", action.getChild(0).getName(),
                "action female socket name is ZH12");
        assertEquals( "jmri.jmrit.logixng.implementation.DefaultFemaleDigitalActionSocket",
                action.getChild(0).getClass().getName(),
                "action female socket is of correct class");
        assertFalse( action.getChild(0).isConnected(),
                "action female socket is not connected");

        assertEquals( 1, action.getChildCount(), "action has 1 female socket");
    }

    @Test
    public void testCtorAndSetup3() {
        DigitalActionManager m1 = InstanceManager.getDefault(DigitalActionManager.class);

        MaleSocket childSocket0 = m1.registerAction(new ActionMemory("IQDA554", null));

        DigitalBooleanLogixAction action = new DigitalBooleanLogixAction("IQDB321", null, DigitalBooleanLogixAction.When.Either);
        assertNotNull( action, "exists");
        assertEquals( 1, action.getChildCount(), "action has 1 female socket");
        action.getChild(0).setName("ZH12");
        action.setActionSocketSystemName("IQDA554");

        assertEquals( "ZH12", action.getChild(0).getName(),
                "action female socket name is ZH12");
        assertEquals( "jmri.jmrit.logixng.implementation.DefaultFemaleDigitalActionSocket",
                action.getChild(0).getClass().getName(),
                "action female socket is of correct class");
        assertFalse( action.getChild(0).isConnected(),
                "action female socket is not connected");

        // Setup action. This connects the child actions to this action
        action.setup();

        assertTrue( action.getChild(0).isConnected(),
                "action female socket is connected");
        assertEquals( childSocket0,
                action.getChild(0).getConnectedSocket(),
                "child is correct bean");

        assertEquals( 1, action.getChildCount(),
                "action has 1 female sockets");

        // Try run setup() again. That should not cause any problems.
        action.setup();

        assertEquals( 1, action.getChildCount(), "action has 1 female sockets");
    }

    @Test
    public void testGetChild() {
        assertEquals( 1, _actionOnChange.getChildCount(), "getChildCount() returns 1");

        assertNotNull( _actionOnChange.getChild(0),
                "getChild(0) returns a non null value");

        IllegalArgumentException ex = assertThrows( IllegalArgumentException.class, () ->
            _actionOnChange.getChild(1), "Exception is thrown");
        assertEquals( "index has invalid value: 1", ex.getMessage(),
                "Error message is correct");
    }

    @Test
    public void testCategory() {
        assertSame( LogixNG_Category.COMMON, _base.getCategory(), "Category matches");
    }

    @Test
    public void testGetShortDescription() {
        DigitalBooleanActionBean a1 = new DigitalBooleanLogixAction("IQDB321", null, DigitalBooleanLogixAction.When.True);
        assertEquals( "Logix Action", a1.getShortDescription(), "strings are equal");
        DigitalBooleanActionBean a2 = new DigitalBooleanLogixAction("IQDB322", null, DigitalBooleanLogixAction.When.False);
        assertEquals( "Logix Action", a2.getShortDescription(), "strings are equal");
        DigitalBooleanActionBean a3 = new DigitalBooleanLogixAction("IQDB323", null, DigitalBooleanLogixAction.When.Either);
        assertEquals( "Logix Action", a3.getShortDescription(), "strings are equal");
    }

    @Test
    public void testGetLongDescription() {
        DigitalBooleanActionBean a1 = new DigitalBooleanLogixAction("IQDB321", null, DigitalBooleanLogixAction.When.True);
        assertEquals( "Logix Action. True", a1.getLongDescription(), "strings are equal");
        DigitalBooleanActionBean a2 = new DigitalBooleanLogixAction("IQDB322", null, DigitalBooleanLogixAction.When.False);
        assertEquals( "Logix Action. False", a2.getLongDescription(), "strings are equal");
        DigitalBooleanActionBean a3 = new DigitalBooleanLogixAction("IQDB323", null, DigitalBooleanLogixAction.When.Either);
        assertEquals( "Logix Action. Either", a3.getLongDescription(), "strings are equal");
    }

    @Test
    public void testTrigger() {
        _actionOnChange.setTrigger(When.Either);
        assertEquals(When.Either, _actionOnChange.getTrigger());

        _actionOnChange.setTrigger(When.False);
        assertEquals(When.False, _actionOnChange.getTrigger());

        _actionOnChange.setTrigger(When.True);
        assertEquals(When.True, _actionOnChange.getTrigger());
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
        assertEquals(Turnout.THROWN, turnout.getState());
        turnout.setState(Turnout.CLOSED);
        _actionOnChange.execute(false);
        assertEquals(Turnout.THROWN, turnout.getState());
        turnout.setState(Turnout.CLOSED);
        _actionOnChange.execute(true);
        assertEquals(Turnout.THROWN, turnout.getState());

        // Ensure last execute is false
        _actionOnChange.execute(false);

        // Test Trigger.CHANGE_TO_FALSE
        _actionOnChange.setTrigger(When.False);
        turnout.setState(Turnout.CLOSED);
        _actionOnChange.execute(true);
        assertEquals(Turnout.CLOSED, turnout.getState());
        turnout.setState(Turnout.CLOSED);
        _actionOnChange.execute(false);
        assertEquals(Turnout.THROWN, turnout.getState());
        turnout.setState(Turnout.CLOSED);
        _actionOnChange.execute(true);
        assertEquals(Turnout.CLOSED, turnout.getState());

        // Ensure last execute is false
        _actionOnChange.execute(false);

        // Test Trigger.CHANGE_TO_TRUE
        _actionOnChange.setTrigger(When.True);
        turnout.setState(Turnout.CLOSED);
        _actionOnChange.execute(true);
        assertEquals(Turnout.THROWN, turnout.getState());
        turnout.setState(Turnout.CLOSED);
        _actionOnChange.execute(false);
        assertEquals(Turnout.CLOSED, turnout.getState());
        turnout.setState(Turnout.CLOSED);
        _actionOnChange.execute(true);
        assertEquals(Turnout.THROWN, turnout.getState());
    }

    @Before
    @BeforeEach
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

        assertTrue( logixNG.setParentForAllChildren(new ArrayList<>()));
        logixNG.activate();
        logixNG.setEnabled(true);
    }

    @After
    @AfterEach
    public void tearDown() {
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
