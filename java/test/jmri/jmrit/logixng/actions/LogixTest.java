package jmri.jmrit.logixng.actions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;

import jmri.jmrit.logixng.expressions.ExpressionSensor;
import jmri.jmrit.logixng.expressions.ExpressionMemory;
import jmri.jmrit.logixng.expressions.True;
import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.implementation.DefaultConditionalNGScaffold;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Before;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test Logix
 *
 * @author Daniel Bergqvist 2018
 */
public class LogixTest extends AbstractDigitalActionTestBase {

    private LogixNG logixNG;
    private ConditionalNG conditionalNG;
    private Logix actionLogix;

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
                "Logix. Execute Actions on change of state only ::: Use default%n" +
                "   ? E%n" +
                "      Sensor '' is Active ::: Use default%n" +
                "   !b A%n" +
                "      Logix Action. Either ::: Use default%n" +
                "         ! A%n" +
                "            Socket not connected%n");
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
                "               Logix Action. Either ::: Use default%n" +
                "                  ! A%n" +
                "                     Socket not connected%n");
    }

    @Override
    public NamedBean createNewBean(String systemName) {
        return new Logix(systemName, null);
    }

    @Override
    public boolean addNewSocket() {
        return false;
    }

    @Test
    public void testCtor() {
        DigitalActionBean t = new Logix("IQDA321", null);
        assertNotNull( t, "exists");
        t = new Logix("IQDA321", null);
        assertNotNull( t, "exists");
    }

    @Test
    public void testCtorAndSetup1() {
        Logix expression = new Logix("IQDA321", null);
        assertNotNull( expression, "exists");
        assertEquals( 2, expression.getChildCount(), "expression has 2 female sockets");
        expression.getChild(0).setName("XYZ123");
        expression.setExpressionSocketSystemName("IQDE52");
        expression.getChild(1).setName("ZH12");
        expression.setActionSocketSystemName("IQDB554");

        assertEquals( "XYZ123", expression.getChild(0).getName(),
                "expression female socket name is XYZ123");
        assertEquals(
//                "jmri.jmrit.logixng.implementation.DefaultFemaleGenericExpressionSocket$DigitalSocket",
                "jmri.jmrit.logixng.implementation.DefaultFemaleDigitalExpressionSocket",
                expression.getChild(0).getClass().getName(),
                "expression female socket is of correct class");
        assertFalse( expression.getChild(0).isConnected(),
                "expression female socket is not connected");

        assertEquals( "ZH12", expression.getChild(1).getName(),
                "expression female socket name is ZH12");
        assertEquals(
                "jmri.jmrit.logixng.implementation.DefaultFemaleDigitalBooleanActionSocket",
                expression.getChild(1).getClass().getName(),
                "expression female socket is of correct class");
        assertFalse( expression.getChild(1).isConnected(),
                "expression female socket is not connected");

        // Setup action. This connects the child actions to this action
        expression.setup();

        jmri.util.JUnitAppender.assertMessage("cannot load digital expression IQDE52");
        jmri.util.JUnitAppender.assertMessage("cannot load digital boolean action IQDB554");

        assertEquals( "XYZ123", expression.getChild(0).getName(),
                "expression female socket name is XYZ123");
        assertEquals(
//                "jmri.jmrit.logixng.implementation.DefaultFemaleGenericExpressionSocket$DigitalSocket",
                "jmri.jmrit.logixng.implementation.DefaultFemaleDigitalExpressionSocket",
                expression.getChild(0).getClass().getName(),
                "expression female socket is of correct class");
        assertFalse( expression.getChild(0).isConnected(),
                "expression female socket is not connected");

        assertEquals( "ZH12", expression.getChild(1).getName(),
                "expression female socket name is ZH12");
        assertEquals( "jmri.jmrit.logixng.implementation.DefaultFemaleDigitalBooleanActionSocket",
                expression.getChild(1).getClass().getName(),
                "expression female socket is of correct class");
        assertFalse( expression.getChild(1).isConnected(),
                "expression female socket is not connected");

        assertEquals( 2, expression.getChildCount(), "expression has 2 female sockets");
    }

    @Test
    public void testCtorAndSetup2() {
        Logix expression = new Logix("IQDA321", null);
        assertNotNull( expression, "exists");
        assertEquals( 2, expression.getChildCount(), "expression has 2 female sockets");
        expression.getChild(0).setName("XYZ123");
        expression.setExpressionSocketSystemName(null);
        expression.getChild(1).setName("ZH12");
        expression.setActionSocketSystemName(null);

        assertEquals( "XYZ123", expression.getChild(0).getName(),
                "expression female socket name is XYZ123");
        assertEquals(
//                "jmri.jmrit.logixng.implementation.DefaultFemaleGenericExpressionSocket$DigitalSocket",
                "jmri.jmrit.logixng.implementation.DefaultFemaleDigitalExpressionSocket",
                expression.getChild(0).getClass().getName(),
                "expression female socket is of correct class");
        assertFalse( expression.getChild(0).isConnected(),
                "expression female socket is not connected");

        assertEquals( "ZH12", expression.getChild(1).getName(),
                "expression female socket name is ZH12");
        assertEquals( "jmri.jmrit.logixng.implementation.DefaultFemaleDigitalBooleanActionSocket",
                expression.getChild(1).getClass().getName(),
                "expression female socket is of correct class");
        assertFalse( expression.getChild(1).isConnected(),
                "expression female socket is not connected");

        // Setup action. This connects the child actions to this action
        expression.setup();

        assertEquals( "XYZ123", expression.getChild(0).getName(),
                "expression female socket name is XYZ123");
        assertEquals(
//                "jmri.jmrit.logixng.implementation.DefaultFemaleGenericExpressionSocket$DigitalSocket",
                "jmri.jmrit.logixng.implementation.DefaultFemaleDigitalExpressionSocket",
                expression.getChild(0).getClass().getName(),
                "expression female socket is of correct class");
        assertFalse( expression.getChild(0).isConnected(),
                "expression female socket is not connected");

        assertEquals( "ZH12", expression.getChild(1).getName(),
                "expression female socket name is ZH12");
        assertEquals( "jmri.jmrit.logixng.implementation.DefaultFemaleDigitalBooleanActionSocket",
                expression.getChild(1).getClass().getName(),
                "expression female socket is of correct class");
        assertFalse( expression.getChild(1).isConnected(),
                "expression female socket is not connected");

        assertEquals( 2, expression.getChildCount(), "expression has 2 female sockets");
    }

    @Test
    public void testCtorAndSetup3() {
        DigitalExpressionManager m0 = InstanceManager.getDefault(DigitalExpressionManager.class);
        DigitalBooleanActionManager m1 = InstanceManager.getDefault(DigitalBooleanActionManager.class);

        m0.registerExpression(new ExpressionMemory("IQDE52", null));
        m1.registerAction(new DigitalBooleanLogixAction("IQDB554", null, DigitalBooleanLogixAction.When.Either));

        Logix expression = new Logix("IQDA321", null);
        assertNotNull( expression, "exists");
        assertEquals( 2, expression.getChildCount(), "expression has 2 female sockets");
        expression.getChild(0).setName("XYZ123");
        expression.setExpressionSocketSystemName("IQDE52");
        expression.getChild(1).setName("ZH12");
        expression.setActionSocketSystemName("IQDB554");

        assertEquals( "XYZ123", expression.getChild(0).getName(),
                "expression female socket name is XYZ123");
        assertEquals(
//                "jmri.jmrit.logixng.implementation.DefaultFemaleGenericExpressionSocket$DigitalSocket",
                "jmri.jmrit.logixng.implementation.DefaultFemaleDigitalExpressionSocket",
                expression.getChild(0).getClass().getName(),
                "expression female socket is of correct class");
        assertFalse( expression.getChild(0).isConnected(),
                "expression female socket is not connected");

        assertEquals( "ZH12", expression.getChild(1).getName(),
                "expression female socket name is ZH12");
        assertEquals( "jmri.jmrit.logixng.implementation.DefaultFemaleDigitalBooleanActionSocket",
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

        assertTrue( expression.getChild(1).isConnected(), "expression female socket is connected");
//        Assert.assertEquals("child is correct bean",
//                childSocket1,
//                expression.getChild(1).getConnectedSocket());
        assertEquals( 2, expression.getChildCount(), "expression has 2 female sockets");

        assertEquals( 2, expression.getChildCount(), "expression has 2 female sockets");

        // Try run setup() again. That should not cause any problems.
        expression.setup();

        assertEquals( 2, expression.getChildCount(), "expression has 2 female sockets");
    }

    @Test
    public void testGetChild() {
        assertEquals( 2, actionLogix.getChildCount(), "getChildCount() returns 2");

        assertNotNull( actionLogix.getChild(0),
                "getChild(0) returns a non null value");
        assertNotNull( actionLogix.getChild(1),
                "getChild(1) returns a non null value");

        IllegalArgumentException ex = assertThrows( IllegalArgumentException.class, () ->
            actionLogix.getChild(2), "Exception is thrown");
        assertEquals( "index has invalid value: 2", ex.getMessage(), "Error message is correct");
    }

    @Test
    public void testToString() {
        DigitalActionBean a1 = new Logix("IQDA321", null);
        assertEquals( "Logix", a1.getShortDescription(), "strings are equal");
        DigitalActionBean a2 = new Logix("IQDA321", null);
        assertEquals( "Logix. Execute Actions on change of state only",
                a2.getLongDescription(), "strings are equal");
    }

    @Test
    public void testCategory() {
        assertSame( LogixNG_Category.OTHER, _base.getCategory(), "Category matches");
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

        _category = LogixNG_Category.COMMON;
        _isExternal = false;

        logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test");  // NOI18N
        conditionalNG = new DefaultConditionalNGScaffold("IQC1", "A conditionalNG");  // NOI18N;
        InstanceManager.getDefault(ConditionalNG_Manager.class).register(conditionalNG);
        conditionalNG.setEnabled(true);
        conditionalNG.setRunDelayed(false);
        logixNG.addConditionalNG(conditionalNG);
        actionLogix = new Logix("IQDA321", null);
        MaleSocket maleSocket =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionLogix);
        conditionalNG.getChild(0).connect(maleSocket);

        ExpressionSensor expressionSensor = new ExpressionSensor("IQDE321", null);
        MaleSocket maleSocket2 =
                InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expressionSensor);
        actionLogix.getChild(0).connect(maleSocket2);

        DigitalBooleanLogixAction actionOnChange = new DigitalBooleanLogixAction("IQDB4", null, DigitalBooleanLogixAction.When.Either);
        maleSocket2 =
                InstanceManager.getDefault(DigitalBooleanActionManager.class).registerAction(actionOnChange);
        actionLogix.getChild(1).connect(maleSocket2);

        _base = actionLogix;
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
