package jmri.jmrit.logixng.actions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;

import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.expressions.ExpressionSensor;
import jmri.jmrit.logixng.expressions.ExpressionMemory;
import jmri.jmrit.logixng.expressions.True;
import jmri.jmrit.logixng.implementation.DefaultConditionalNGScaffold;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Before;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test IfThenElse
 *
 * @author Daniel Bergqvist 2018
 */
public class IfThenElseTest extends AbstractDigitalActionTestBase {

    private LogixNG logixNG;
    private ConditionalNG conditionalNG;
    private IfThenElse actionIfThenElse;

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
        DigitalExpressionBean childExpression = new True("IQDE999", null);
        MaleSocket maleSocketChild =
                InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(childExpression);
        return maleSocketChild;
    }

    @Override
    public String getExpectedPrintedTree() {
        return String.format(
                "If Then Else. Execute on change ::: Use default%n" +
                "   ? If%n" +
                "      Sensor '' is Active ::: Use default%n" +
                "   ! Then%n" +
                "      Set turnout '' to state Thrown ::: Use default%n" +
                "   ! Else%n" +
                "      Socket not connected%n");
    }

    @Override
    public String getExpectedPrintedTreeFromRoot() {
        return String.format(
                "LogixNG: A new logix for test%n" +
                "   ConditionalNG: A conditionalNG%n" +
                "      ! A%n" +
                "         If Then Else. Execute on change ::: Use default%n" +
                "            ? If%n" +
                "               Sensor '' is Active ::: Use default%n" +
                "            ! Then%n" +
                "               Set turnout '' to state Thrown ::: Use default%n" +
                "            ! Else%n" +
                "               Socket not connected%n");
    }

    @Override
    public NamedBean createNewBean(String systemName) {
        return new IfThenElse(systemName, null);
    }

    @Override
    public boolean addNewSocket() {
        return false;
    }

    @Test
    public void testCtor() {
        IfThenElse t = new IfThenElse("IQDA321", null);
        t.setExecuteType(IfThenElse.ExecuteType.ExecuteOnChange);
        assertNotNull( t, "exists");
        t = new IfThenElse("IQDA321", null);
        t.setExecuteType(IfThenElse.ExecuteType.AlwaysExecute);
        assertNotNull( t, "exists");
    }

    @Test
    public void testCtorAndSetup1() {
        IfThenElse action = new IfThenElse("IQDA321", null);
        action.setExecuteType(IfThenElse.ExecuteType.ExecuteOnChange);
        assertNotNull( action, "exists");
        assertEquals( 3, action.getChildCount(), "expression has 3 female sockets");
        action.getChild(0).setName("XYZ123");
        action.setExpressionSocketSystemName(0, "IQDE52");
        action.getChild(1).setName("ZH12");
        action.setActionSocketSystemName(0, "IQDA554");
        action.getChild(2).setName("Bj23");
        action.setActionSocketSystemName(1, "IQDA594");

        assertEquals( "XYZ123", action.getChild(0).getName(),
                "expression female socket name is XYZ123");
        assertEquals(
//                "jmri.jmrit.logixng.implementation.DefaultFemaleGenericExpressionSocket$DigitalSocket",
                "jmri.jmrit.logixng.implementation.DefaultFemaleDigitalExpressionSocket",
                action.getChild(0).getClass().getName(),
                "expression female socket is of correct class");
        assertFalse( action.getChild(0).isConnected(),
                "expression female socket is not connected");

        assertEquals( "ZH12", action.getChild(1).getName(),
                "expression female socket name is ZH12");
        assertEquals( "jmri.jmrit.logixng.implementation.DefaultFemaleDigitalActionSocket",
                action.getChild(1).getClass().getName(),
                "expression female socket is of correct class");
        assertFalse( action.getChild(1).isConnected(),
                "expression female socket is not connected");

        assertEquals( "Bj23", action.getChild(2).getName(),
                "expression female socket name is Bj23");
        assertEquals( "jmri.jmrit.logixng.implementation.DefaultFemaleDigitalActionSocket",
                action.getChild(2).getClass().getName(),
                "expression female socket is of correct class");
        assertFalse( action.getChild(2).isConnected(),
                "expression female socket is not connected");

        // Setup action. This connects the child actions to this action
        action.setup();

        jmri.util.JUnitAppender.assertMessage("cannot load digital expression IQDE52");
        jmri.util.JUnitAppender.assertMessage("cannot load digital action IQDA554");
        jmri.util.JUnitAppender.assertMessage("cannot load digital action IQDA594");

        assertEquals( "XYZ123", action.getChild(0).getName(),
                "expression female socket name is XYZ123");
        assertEquals(
//                "jmri.jmrit.logixng.implementation.DefaultFemaleGenericExpressionSocket$DigitalSocket",
                "jmri.jmrit.logixng.implementation.DefaultFemaleDigitalExpressionSocket",
                action.getChild(0).getClass().getName(),
                "expression female socket is of correct class");
        assertFalse( action.getChild(0).isConnected(),
                "expression female socket is not connected");

        assertEquals( "ZH12", action.getChild(1).getName(),
                "expression female socket name is ZH12");
        assertEquals( "jmri.jmrit.logixng.implementation.DefaultFemaleDigitalActionSocket",
                action.getChild(1).getClass().getName(),
                "expression female socket is of correct class");
        assertFalse( action.getChild(1).isConnected(),
                "expression female socket is not connected");

        assertEquals( "Bj23", action.getChild(2).getName(),
                "expression female socket name is Bj23");
        assertEquals( "jmri.jmrit.logixng.implementation.DefaultFemaleDigitalActionSocket",
                action.getChild(2).getClass().getName(),
                "expression female socket is of correct class");
        assertFalse( action.getChild(2).isConnected(),
                "expression female socket is not connected");

        assertEquals( 3, action.getChildCount(), "expression has 3 female sockets");
    }

    @Test
    public void testCtorAndSetup2() {
        IfThenElse action = new IfThenElse("IQDA321", null);
        action.setExecuteType(IfThenElse.ExecuteType.ExecuteOnChange);
        assertNotNull( action, "exists");
        assertEquals( 3, action.getChildCount(), "expression has 3 female sockets");
        action.getChild(0).setName("XYZ123");
        action.setExpressionSocketSystemName(0, null);
        action.getChild(1).setName("ZH12");
        action.setActionSocketSystemName(0, null);
        action.getChild(2).setName("Bj23");
        action.setActionSocketSystemName(1, null);

        assertEquals( "XYZ123", action.getChild(0).getName(),
                "expression female socket name is XYZ123");
        assertEquals(
//                "jmri.jmrit.logixng.implementation.DefaultFemaleGenericExpressionSocket$DigitalSocket",
                "jmri.jmrit.logixng.implementation.DefaultFemaleDigitalExpressionSocket",
                action.getChild(0).getClass().getName(),
                "expression female socket is of correct class");
        assertFalse( action.getChild(0).isConnected(),
                "expression female socket is not connected");

        assertEquals( "ZH12", action.getChild(1).getName(),
                "expression female socket name is ZH12");
        assertEquals( "jmri.jmrit.logixng.implementation.DefaultFemaleDigitalActionSocket",
                action.getChild(1).getClass().getName(),
                "expression female socket is of correct class");
        assertFalse( action.getChild(1).isConnected(),
                "expression female socket is not connected");

        assertEquals( "Bj23", action.getChild(2).getName(),
                "expression female socket name is Bj23");
        assertEquals( "jmri.jmrit.logixng.implementation.DefaultFemaleDigitalActionSocket",
                action.getChild(2).getClass().getName(),
                "expression female socket is of correct class");
        assertFalse( action.getChild(2).isConnected(),
                "expression female socket is not connected");

        // Setup action. This connects the child actions to this action
        action.setup();

        assertEquals( "XYZ123", action.getChild(0).getName(),
                "expression female socket name is XYZ123");
        assertEquals(
//                "jmri.jmrit.logixng.implementation.DefaultFemaleGenericExpressionSocket$DigitalSocket",
                "jmri.jmrit.logixng.implementation.DefaultFemaleDigitalExpressionSocket",
                action.getChild(0).getClass().getName(),
                "expression female socket is of correct class");
        assertFalse( action.getChild(0).isConnected(),
                "expression female socket is not connected");

        assertEquals( "ZH12", action.getChild(1).getName(),
                "expression female socket name is ZH12");
        assertEquals( "jmri.jmrit.logixng.implementation.DefaultFemaleDigitalActionSocket",
                action.getChild(1).getClass().getName(),
                "expression female socket is of correct class");
        assertFalse( action.getChild(1).isConnected(),
                "expression female socket is not connected");

        assertEquals( "Bj23", action.getChild(2).getName(),
                "expression female socket name is Bj23");
        assertEquals( "jmri.jmrit.logixng.implementation.DefaultFemaleDigitalActionSocket",
                action.getChild(2).getClass().getName(),
                "expression female socket is of correct class");
        assertFalse( action.getChild(2).isConnected(),
                "expression female socket is not connected");

        assertEquals( 3, action.getChildCount(), "expression has 3 female sockets");
    }

    @Test
    public void testCtorAndSetup3() {
        DigitalExpressionManager m0 = InstanceManager.getDefault(DigitalExpressionManager.class);
        DigitalActionManager m1 = InstanceManager.getDefault(DigitalActionManager.class);

        MaleSocket childSocket0 = m0.registerExpression(new ExpressionMemory("IQDE52", null));
        MaleSocket childSocket1 = m1.registerAction(new ActionMemory("IQDA554", null));
        MaleSocket childSocket2 = m1.registerAction(new ActionMemory("IQDA594", null));

        IfThenElse action = new IfThenElse("IQDA321", null);
        action.setExecuteType(IfThenElse.ExecuteType.ExecuteOnChange);
        assertNotNull( action, "exists");
        assertEquals( 3, action.getChildCount(), "expression has 3 female sockets");
        action.getChild(0).setName("XYZ123");
        action.setExpressionSocketSystemName(0, "IQDE52");
        action.getChild(1).setName("ZH12");
        action.setActionSocketSystemName(0, "IQDA554");
        action.getChild(2).setName("Bj23");
        action.setActionSocketSystemName(1, "IQDA594");

        assertEquals( "XYZ123", action.getChild(0).getName(),
                "expression female socket name is XYZ123");
        assertEquals(
//                "jmri.jmrit.logixng.implementation.DefaultFemaleGenericExpressionSocket$DigitalSocket",
                "jmri.jmrit.logixng.implementation.DefaultFemaleDigitalExpressionSocket",
                action.getChild(0).getClass().getName(),
                "expression female socket is of correct class");
        assertFalse( action.getChild(0).isConnected(),
                "expression female socket is not connected");

        assertEquals( "ZH12", action.getChild(1).getName(),
                "expression female socket name is ZH12");
        assertEquals( "jmri.jmrit.logixng.implementation.DefaultFemaleDigitalActionSocket",
                action.getChild(1).getClass().getName(),
                "expression female socket is of correct class");
        assertFalse( action.getChild(1).isConnected(),
                "expression female socket is not connected");

        assertEquals( "Bj23", action.getChild(2).getName(),
                "expression female socket name is Bj23");
        assertEquals( "jmri.jmrit.logixng.implementation.DefaultFemaleDigitalActionSocket",
                action.getChild(2).getClass().getName(),
                "expression female socket is of correct class");
        assertFalse( action.getChild(2).isConnected(),
                "expression female socket is not connected");

        // Setup action. This connects the child actions to this action
        action.setup();

        assertTrue( action.getChild(0).isConnected(),
                "expression female socket is connected");
        assertEquals( childSocket0,
                action.getChild(0).getConnectedSocket(), "child is correct bean");
        assertEquals( 3, action.getChildCount(), "expression has 3 female sockets");

        assertTrue( action.getChild(1).isConnected(),
                "expression female socket is connected");
        assertEquals( childSocket1,
                action.getChild(1).getConnectedSocket(), "child is correct bean");
        assertEquals( 3, action.getChildCount(), "expression has 3 female sockets");

        assertTrue( action.getChild(2).isConnected(),
                "expression female socket is connected");
        assertEquals( childSocket2,
                action.getChild(2).getConnectedSocket(),
                "child is correct bean");

        assertEquals( 3, action.getChildCount(), "expression has 3 female sockets");

        // Try run setup() again. That should not cause any problems.
        action.setup();

        assertEquals( 3, action.getChildCount(), "expression has 3 female sockets");
    }

    @Test
    public void testGetChild() {
        assertEquals( 3, actionIfThenElse.getChildCount(), "getChildCount() returns 3");

        assertNotNull( actionIfThenElse.getChild(0),
                "getChild(0) returns a non null value");
        assertNotNull( actionIfThenElse.getChild(1),
                "getChild(1) returns a non null value");
        assertNotNull( actionIfThenElse.getChild(2),
                "getChild(2) returns a non null value");

        IllegalArgumentException ex = assertThrows( IllegalArgumentException.class, () ->
            actionIfThenElse.getChild(3), "Exception is thrown");
        assertEquals( "index has invalid value: 3", ex.getMessage(), "Error message is correct");
    }

    @Test
    public void testToString() {
        IfThenElse action = new IfThenElse("IQDA321", null);
        action.setExecuteType(IfThenElse.ExecuteType.ExecuteOnChange);
        assertEquals( "If Then Else", action.getShortDescription(), "strings are equal");
        assertEquals( "If Then Else. Execute on change", action.getLongDescription(), "strings are equal");

        action = new IfThenElse("IQDA321", null);
        action.setExecuteType(IfThenElse.ExecuteType.AlwaysExecute);
        assertEquals( "If Then Else", action.getShortDescription(), "strings are equal");
        assertEquals( "If Then Else. Always execute", action.getLongDescription(), "strings are equal");
    }

    @Test
    public void testCategory() {
        assertSame( LogixNG_Category.FLOW_CONTROL, _base.getCategory(), "Category matches");
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
        actionIfThenElse = new IfThenElse("IQDA321", null);
        actionIfThenElse.setExecuteType(IfThenElse.ExecuteType.ExecuteOnChange);
        MaleSocket maleSocket =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionIfThenElse);
        conditionalNG.getChild(0).connect(maleSocket);

        ExpressionSensor expressionSensor = new ExpressionSensor("IQDE321", null);
        MaleSocket maleSocket2 =
                InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expressionSensor);
        actionIfThenElse.getChild(0).connect(maleSocket2);

        ActionTurnout actionTurnout = new ActionTurnout("IQDA322", null);
        maleSocket2 =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionTurnout);
        actionIfThenElse.getChild(1).connect(maleSocket2);

        _base = actionIfThenElse;
        _baseMaleSocket = maleSocket;

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
