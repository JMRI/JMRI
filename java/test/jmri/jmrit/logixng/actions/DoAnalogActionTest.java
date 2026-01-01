package jmri.jmrit.logixng.actions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;

import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.expressions.AnalogExpressionConstant;
import jmri.jmrit.logixng.expressions.AnalogExpressionMemory;
import jmri.jmrit.logixng.implementation.DefaultConditionalNGScaffold;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Before;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test DoAnalogAction
 *
 * @author Daniel Bergqvist 2018
 */
public class DoAnalogActionTest extends AbstractDigitalActionTestBase {

    private LogixNG logixNG;
    private ConditionalNG conditionalNG;
    private DoAnalogAction actionDoAnalogAction;

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
        AnalogExpressionBean childExpression = new AnalogExpressionConstant("IQAE999", null);
        MaleSocket maleSocketChild =
                InstanceManager.getDefault(AnalogExpressionManager.class).registerExpression(childExpression);
        return maleSocketChild;
    }

    @Override
    public String getExpectedPrintedTree() {
        return String.format(
                "Read analog E and set analog A ::: Use default%n" +
                "   ?~ E%n" +
                "      Socket not connected%n" +
                "   !~ A%n" +
                "      Socket not connected%n");
    }

    @Override
    public String getExpectedPrintedTreeFromRoot() {
        return String.format(
                "LogixNG: A new logix for test%n" +
                "   ConditionalNG: A conditionalNG%n" +
                "      ! A%n" +
                "         Read analog E and set analog A ::: Use default%n" +
                "            ?~ E%n" +
                "               Socket not connected%n" +
                "            !~ A%n" +
                "               Socket not connected%n");
    }

    @Override
    public NamedBean createNewBean(String systemName) {
        return new DoAnalogAction(systemName, null);
    }

    @Override
    public boolean addNewSocket() {
        return false;
    }

    @Test
    public void testCtor() {
        assertNotNull( new DoAnalogAction("IQDA321", null), "exists");
    }

    @Test
    public void testCtorAndSetup1() {
        DoAnalogAction expression = new DoAnalogAction("IQDA321", null);
        assertNotNull( expression, "exists");
        assertEquals( 2, expression.getChildCount(), "expression has 2 female sockets");
        expression.getChild(0).setName("XYZ123");
        expression.setAnalogExpressionSocketSystemName("IQAE52");
        expression.getChild(1).setName("ZH12");
        expression.setAnalogActionSocketSystemName("IQAA554");

        assertEquals( "XYZ123", expression.getChild(0).getName(),
                "expression female socket name is XYZ123");
        assertEquals(
//                "jmri.jmrit.logixng.implementation.DefaultFemaleGenericExpressionSocket$AnalogSocket",
                "jmri.jmrit.logixng.implementation.DefaultFemaleAnalogExpressionSocket",
                expression.getChild(0).getClass().getName(),
                "expression female socket is of correct class");
        assertFalse( expression.getChild(0).isConnected(),
                "expression female socket is not connected");

        assertEquals( "ZH12", expression.getChild(1).getName(),
                "expression female socket name is ZH12");
        assertEquals( "jmri.jmrit.logixng.implementation.DefaultFemaleAnalogActionSocket",
                expression.getChild(1).getClass().getName(),
                "expression female socket is of correct class");
        assertFalse( expression.getChild(1).isConnected(),
                "expression female socket is not connected");

        // Setup action. This connects the child actions to this action
        expression.setup();

        jmri.util.JUnitAppender.assertMessage("cannot load analog expression IQAE52");
        jmri.util.JUnitAppender.assertMessage("cannot load analog action IQAA554");

        assertEquals( "XYZ123", expression.getChild(0).getName(),
                "expression female socket name is XYZ123");
        assertEquals(
//                "jmri.jmrit.logixng.implementation.DefaultFemaleGenericExpressionSocket$AnalogSocket",
                "jmri.jmrit.logixng.implementation.DefaultFemaleAnalogExpressionSocket",
                expression.getChild(0).getClass().getName(),
                "expression female socket is of correct class");
        assertFalse( expression.getChild(0).isConnected(),
                "expression female socket is not connected");

        assertEquals( "ZH12", expression.getChild(1).getName(),
                "expression female socket name is ZH12");
        assertEquals( "jmri.jmrit.logixng.implementation.DefaultFemaleAnalogActionSocket",
                expression.getChild(1).getClass().getName(), "expression female socket is of correct class");
        assertFalse( expression.getChild(1).isConnected(),
                "expression female socket is not connected");

        assertEquals( 2, expression.getChildCount(), "expression has 2 female sockets");
    }

    @Test
    public void testCtorAndSetup2() {
        DoAnalogAction expression = new DoAnalogAction("IQDA321", null);
        assertNotNull( expression, "exists");
        assertEquals( 2, expression.getChildCount(), "expression has 2 female sockets");
        expression.getChild(0).setName("XYZ123");
        expression.setAnalogExpressionSocketSystemName(null);
        expression.getChild(1).setName("ZH12");
        expression.setAnalogActionSocketSystemName(null);

        assertEquals( "XYZ123", expression.getChild(0).getName(),
                "expression female socket name is XYZ123");
        assertEquals(
//                "jmri.jmrit.logixng.implementation.DefaultFemaleGenericExpressionSocket$AnalogSocket",
                "jmri.jmrit.logixng.implementation.DefaultFemaleAnalogExpressionSocket",
                expression.getChild(0).getClass().getName(),
                "expression female socket is of correct class");
        assertFalse( expression.getChild(0).isConnected(),
                "expression female socket is not connected");

        assertEquals( "ZH12", expression.getChild(1).getName(),
                "expression female socket name is ZH12");
        assertEquals( "jmri.jmrit.logixng.implementation.DefaultFemaleAnalogActionSocket",
                expression.getChild(1).getClass().getName(),
                "expression female socket is of correct class");
        assertFalse( expression.getChild(1).isConnected(),
                "expression female socket is not connected");

        // Setup action. This connects the child actions to this action
        expression.setup();

        assertEquals( "XYZ123", expression.getChild(0).getName(),
                "expression female socket name is XYZ123");
        assertEquals(
//                "jmri.jmrit.logixng.implementation.DefaultFemaleGenericExpressionSocket$AnalogSocket",
                "jmri.jmrit.logixng.implementation.DefaultFemaleAnalogExpressionSocket",
                expression.getChild(0).getClass().getName(),
                "expression female socket is of correct class");
        assertFalse( expression.getChild(0).isConnected(),
                "expression female socket is not connected");

        assertEquals( "ZH12", expression.getChild(1).getName(),
                "expression female socket name is ZH12");
        assertEquals( "jmri.jmrit.logixng.implementation.DefaultFemaleAnalogActionSocket",
                expression.getChild(1).getClass().getName(),
                "expression female socket is of correct class");
        assertFalse( expression.getChild(1).isConnected(),
                "expression female socket is not connected");

        assertEquals( 2, expression.getChildCount(), "expression has 2 female sockets");
    }

    @Test
    public void testCtorAndSetup3() {
        AnalogExpressionManager m0 = InstanceManager.getDefault(AnalogExpressionManager.class);
        AnalogActionManager m1 = InstanceManager.getDefault(AnalogActionManager.class);

        m0.registerExpression(new AnalogExpressionMemory("IQAE52", null));
        m1.registerAction(new AnalogActionMemory("IQAA554", null));

        DoAnalogAction expression = new DoAnalogAction("IQDA321", null);
        assertNotNull( expression, "exists");
        assertEquals( 2, expression.getChildCount(), "expression has 2 female sockets");
        expression.getChild(0).setName("XYZ123");
        expression.setAnalogExpressionSocketSystemName("IQAE52");
        expression.getChild(1).setName("ZH12");
        expression.setAnalogActionSocketSystemName("IQAA554");

        assertEquals( "XYZ123", expression.getChild(0).getName(),
                "expression female socket name is XYZ123");
        assertEquals(
//                "jmri.jmrit.logixng.implementation.DefaultFemaleGenericExpressionSocket$AnalogSocket",
                "jmri.jmrit.logixng.implementation.DefaultFemaleAnalogExpressionSocket",
                expression.getChild(0).getClass().getName(),
                "expression female socket is of correct class");
        assertFalse( expression.getChild(0).isConnected(),
                "expression female socket is not connected");

        assertEquals( "ZH12", expression.getChild(1).getName(),
                "expression female socket name is ZH12");
        assertEquals( "jmri.jmrit.logixng.implementation.DefaultFemaleAnalogActionSocket",
                expression.getChild(1).getClass().getName(),
                "expression female socket is of correct class");
        assertFalse( expression.getChild(1).isConnected(),
                "expression female socket is not connected");

        // Setup action. This connects the child actions to this action
        expression.setup();

        assertTrue( expression.getChild(0).isConnected(),
                "expression female socket is connected");
//        Assert.assertEquals("child is correct bean",
//                childSocket0,
//                expression.getChild(0).getConnectedSocket());
        assertEquals( 2, expression.getChildCount(), "expression has 2 female sockets");

        assertTrue( expression.getChild(1).isConnected(),
                "expression female socket is connected");
//        Assert.assertEquals("child is correct bean",
//                childSocket1,
//                expression.getChild(1).getConnectedSocket());
        assertEquals( 2, expression.getChildCount(), "expression has 2 female sockets");

        // Try run setup() again. That should not cause any problems.
        expression.setup();

        assertEquals( 2, expression.getChildCount(), "expression has 2 female sockets");
    }

    @Test
    public void testGetChild() {
        assertEquals( 2, actionDoAnalogAction.getChildCount(), "getChildCount() returns 2");

        assertNotNull( actionDoAnalogAction.getChild(0),
                "getChild(0) returns a non null value");
        assertNotNull( actionDoAnalogAction.getChild(1),
                "getChild(1) returns a non null value");

        IllegalArgumentException ex = assertThrows( IllegalArgumentException.class, () ->
            actionDoAnalogAction.getChild(2), "Exception is thrown");
        assertEquals( "index has invalid value: 2", ex.getMessage(),
                "Error message is correct");
    }

    @Test
    public void testCategory() {
        assertEquals( LogixNG_Category.COMMON, _base.getCategory(), "Category matches");
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
        JUnitUtil.initLogixNGManager();

        _category = LogixNG_Category.OTHER;
        _isExternal = false;

        logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test");  // NOI18N
        conditionalNG = new DefaultConditionalNGScaffold("IQC1", "A conditionalNG");  // NOI18N;
        InstanceManager.getDefault(ConditionalNG_Manager.class).register(conditionalNG);
        conditionalNG.setEnabled(true);
        conditionalNG.setRunDelayed(false);
        logixNG.addConditionalNG(conditionalNG);
        actionDoAnalogAction = new DoAnalogAction("IQDA321", null);
        MaleSocket maleSocket =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionDoAnalogAction);
        conditionalNG.getChild(0).connect(maleSocket);
        _base = actionDoAnalogAction;
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
