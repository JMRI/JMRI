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
import jmri.jmrit.logixng.expressions.StringExpressionMemory;
import jmri.jmrit.logixng.expressions.StringExpressionConstant;
import jmri.jmrit.logixng.implementation.DefaultConditionalNGScaffold;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Before;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test DoStringAction
 *
 * @author Daniel Bergqvist 2019
 */
public class DoStringActionTest extends AbstractDigitalActionTestBase {

    private LogixNG logixNG;
    private ConditionalNG conditionalNG;
    private DoStringAction actionDoStringAction;

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
        StringExpressionBean childExpression = new StringExpressionConstant("IQSE999", null);
        MaleSocket maleSocketChild =
                InstanceManager.getDefault(StringExpressionManager.class).registerExpression(childExpression);
        return maleSocketChild;
    }

    @Override
    public String getExpectedPrintedTree() {
        return String.format(
                "Read string E and set string A ::: Use default%n" +
                "   ?s E%n" +
                "      Socket not connected%n" +
                "   !s A%n" +
                "      Socket not connected%n");
    }

    @Override
    public String getExpectedPrintedTreeFromRoot() {
        return String.format(
                "LogixNG: A new logix for test%n" +
                "   ConditionalNG: A conditionalNG%n" +
                "      ! A%n" +
                "         Read string E and set string A ::: Use default%n" +
                "            ?s E%n" +
                "               Socket not connected%n" +
                "            !s A%n" +
                "               Socket not connected%n");
    }

    @Override
    public NamedBean createNewBean(String systemName) {
        return new DoStringAction(systemName, null);
    }

    @Override
    public boolean addNewSocket() {
        return false;
    }

    @Test
    public void testCtor() {
        assertNotNull( new DoStringAction("IQDA321", null), "exists");
    }

    @Test
    public void testCtorAndSetup1() {
        DoStringAction expression = new DoStringAction("IQDA321", null);
        assertNotNull( expression, "exists");
        assertEquals( 2, expression.getChildCount(), "expression has 2 female sockets");
        expression.getChild(0).setName("XYZ123");
        expression.setStringExpressionSocketSystemName("IQSE52");
        expression.getChild(1).setName("ZH12");
        expression.setStringActionSocketSystemName("IQSA554");

        assertEquals( "XYZ123", expression.getChild(0).getName(),
                "expression female socket name is XYZ123");
        assertEquals(
//                "jmri.jmrit.logixng.implementation.DefaultFemaleGenericExpressionSocket$StringSocket",
                "jmri.jmrit.logixng.implementation.DefaultFemaleStringExpressionSocket",
                expression.getChild(0).getClass().getName(),
                "expression female socket is of correct class");
        assertFalse( expression.getChild(0).isConnected(),
                "expression female socket is not connected");

        assertEquals( "ZH12", expression.getChild(1).getName(),
                "expression female socket name is ZH12");
        assertEquals( "jmri.jmrit.logixng.implementation.DefaultFemaleStringActionSocket",
                expression.getChild(1).getClass().getName(),
                "expression female socket is of correct class");
        assertFalse( expression.getChild(1).isConnected(),
                "expression female socket is not connected");

        // Setup action. This connects the child actions to this action
        expression.setup();

        jmri.util.JUnitAppender.assertMessage("cannot load string expression IQSE52");
        jmri.util.JUnitAppender.assertMessage("cannot load string action IQSA554");

        assertEquals( "XYZ123", expression.getChild(0).getName(),
                "expression female socket name is XYZ123");
        assertEquals(
//                "jmri.jmrit.logixng.implementation.DefaultFemaleGenericExpressionSocket$StringSocket",
                "jmri.jmrit.logixng.implementation.DefaultFemaleStringExpressionSocket",
                expression.getChild(0).getClass().getName(),
                "expression female socket is of correct class");
        assertFalse( expression.getChild(0).isConnected(),
                "expression female socket is not connected");

        assertEquals( "ZH12", expression.getChild(1).getName(),
                "expression female socket name is ZH12");
        assertEquals( "jmri.jmrit.logixng.implementation.DefaultFemaleStringActionSocket",
                expression.getChild(1).getClass().getName(),
                "expression female socket is of correct class");
        assertFalse( expression.getChild(1).isConnected(),
                "expression female socket is not connected");

        assertEquals( 2, expression.getChildCount(), "expression has 2 female sockets");
    }

    @Test
    public void testCtorAndSetup2() {
        DoStringAction expression = new DoStringAction("IQDA321", null);
        assertNotNull( expression, "exists");
        assertEquals( 2, expression.getChildCount(), "expression has 2 female sockets");
        expression.getChild(0).setName("XYZ123");
        expression.setStringExpressionSocketSystemName(null);
        expression.getChild(1).setName("ZH12");
        expression.setStringActionSocketSystemName(null);

        assertEquals( "XYZ123", expression.getChild(0).getName(),
                "expression female socket name is XYZ123");
        assertEquals(
//                "jmri.jmrit.logixng.implementation.DefaultFemaleGenericExpressionSocket$StringSocket",
                "jmri.jmrit.logixng.implementation.DefaultFemaleStringExpressionSocket",
                expression.getChild(0).getClass().getName(),
                "expression female socket is of correct class");
        assertFalse( expression.getChild(0).isConnected(),
                "expression female socket is not connected");

        assertEquals( "ZH12", expression.getChild(1).getName(),
                "expression female socket name is ZH12");
        assertEquals( "jmri.jmrit.logixng.implementation.DefaultFemaleStringActionSocket",
                expression.getChild(1).getClass().getName(),
                "expression female socket is of correct class");
        assertFalse( expression.getChild(1).isConnected(),
                "expression female socket is not connected");

        // Setup action. This connects the child actions to this action
        expression.setup();

        assertEquals( "XYZ123", expression.getChild(0).getName(),
                "expression female socket name is XYZ123");
        assertEquals(
//                "jmri.jmrit.logixng.implementation.DefaultFemaleGenericExpressionSocket$StringSocket",
                "jmri.jmrit.logixng.implementation.DefaultFemaleStringExpressionSocket",
                expression.getChild(0).getClass().getName(),
                "expression female socket is of correct class");
        assertFalse( expression.getChild(0).isConnected(),
                "expression female socket is not connected");

        assertEquals( "ZH12", expression.getChild(1).getName(),
                "expression female socket name is ZH12");
        assertEquals( "jmri.jmrit.logixng.implementation.DefaultFemaleStringActionSocket",
                expression.getChild(1).getClass().getName(),
                "expression female socket is of correct class");
        assertFalse( expression.getChild(1).isConnected(),
                "expression female socket is not connected");

        assertEquals( 2, expression.getChildCount(),
                "expression has 2 female sockets");
    }

    @Test
    public void testCtorAndSetup3() {
        StringExpressionManager m0 = InstanceManager.getDefault(StringExpressionManager.class);
        StringActionManager m1 = InstanceManager.getDefault(StringActionManager.class);

        m0.registerExpression(new StringExpressionMemory("IQSE52", null));
        m1.registerAction(new StringActionMemory("IQSA554", null));

        DoStringAction expression = new DoStringAction("IQDA321", null);
        assertNotNull( expression, "exists");
        assertEquals( 2, expression.getChildCount(), "expression has 2 female sockets");
        expression.getChild(0).setName("XYZ123");
        expression.setStringExpressionSocketSystemName("IQSE52");
        expression.getChild(1).setName("ZH12");
        expression.setStringActionSocketSystemName("IQSA554");

        assertEquals( "XYZ123", expression.getChild(0).getName(),
                "expression female socket name is XYZ123");
        assertEquals(
//                "jmri.jmrit.logixng.implementation.DefaultFemaleGenericExpressionSocket$StringSocket",
                "jmri.jmrit.logixng.implementation.DefaultFemaleStringExpressionSocket",
                expression.getChild(0).getClass().getName(),
                "expression female socket is of correct class");
        assertFalse( expression.getChild(0).isConnected(),
                "expression female socket is not connected");

        assertEquals( "ZH12", expression.getChild(1).getName(),
                "expression female socket name is ZH12");
        assertEquals( "jmri.jmrit.logixng.implementation.DefaultFemaleStringActionSocket",
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
        assertEquals( 2, actionDoStringAction.getChildCount(), "getChildCount() returns 2");

        assertNotNull( actionDoStringAction.getChild(0),
                "getChild(0) returns a non null value");
        assertNotNull( actionDoStringAction.getChild(1),
                "getChild(1) returns a non null value");

        IllegalArgumentException ex = assertThrows( IllegalArgumentException.class, () ->
            actionDoStringAction.getChild(2), "Exception is thrown");
        assertEquals( "index has invalid value: 2", ex.getMessage(), "Error message is correct");
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
        actionDoStringAction = new DoStringAction("IQDA321", null);
        MaleSocket maleSocket =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionDoStringAction);
        conditionalNG.getChild(0).connect(maleSocket);
        _base = actionDoStringAction;
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
