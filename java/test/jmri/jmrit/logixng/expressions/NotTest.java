package jmri.jmrit.logixng.expressions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.ActionAtomicBoolean;
import jmri.jmrit.logixng.actions.IfThenElse;
import jmri.jmrit.logixng.implementation.DefaultConditionalNGScaffold;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Before;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Test Not
 *
 * @author Daniel Bergqvist 2021
 */
public class NotTest extends AbstractDigitalExpressionTestBase {

    private LogixNG logixNG;
    private ConditionalNG conditionalNG;
    private Not expressionNot;
    private ActionAtomicBoolean actionAtomicBoolean;
    private AtomicBoolean atomicBoolean;


    @Override
    public ConditionalNG getConditionalNG() {
        return conditionalNG;
    }

    @Override
    public LogixNG getLogixNG() {
        return logixNG;
    }

    private int beanID = 901;

    @Override
    public MaleSocket getConnectableChild() {
        beanID++;
        DigitalExpressionBean childExpression = new True("IQDE"+Integer.toString(beanID), null);
        MaleSocket maleSocketChild =
                InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(childExpression);
        return maleSocketChild;
    }

    @Override
    public String getExpectedPrintedTree() {
        return String.format(
                "Not ::: Use default%n" +
                "   ? E%n" +
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
                "               Not ::: Use default%n" +
                "                  ? E%n" +
                "                     Socket not connected%n" +
                "            ! Then%n" +
                "               Set the atomic boolean to true ::: Use default%n" +
                "            ! Else%n" +
                "               Socket not connected%n");
    }

    @Override
    public NamedBean createNewBean(String systemName) {
        return new Not(systemName, null);
    }

    @Override
    public boolean addNewSocket() {
        return false;
    }

    @Test
    public void testCtor() {
        Not expression2;

        expression2 = new Not("IQDE321", null);
        assertNotNull( expression2, "object exists");
        assertNull( expression2.getUserName(), "Username matches");
        assertEquals( "Not", expression2.getLongDescription(), "String matches");

        expression2 = new Not("IQDE321", "My expression");
        assertNotNull( expression2, "object exists");
        assertEquals( "My expression", expression2.getUserName(), "Username matches");
        assertEquals( "Not", expression2.getLongDescription(), "String matches");

        IllegalArgumentException ex = assertThrows( IllegalArgumentException.class, () -> {
            var t = new Not("IQE55:12:XY11", null);
            fail("created " + t);
        }, "Illegal system name Expected exception thrown");
        assertNotNull(ex);

        ex = assertThrows( IllegalArgumentException.class, () -> {
            var t = new Not("IQE55:12:XY11", "A name");
            fail("created " + t);
        }, "Illegal system name Expected exception thrown");
        assertNotNull(ex);
    }

    @Test
    public void testCtorAndSetup1() {
        Not expression = new Not("IQDE321", null);
        assertNotNull( expression, "exists");
        assertEquals( 1, expression.getChildCount(), "expression has 1 female socket");
        expression.getChild(0).setName("XYZ123");
        expression.setSocketSystemName("IQDE52");

        assertEquals( "XYZ123", expression.getChild(0).getName(),
                "expression female socket name is XYZ123");
        assertEquals(
//                "jmri.jmrit.logixng.implementation.DefaultFemaleGenericExpressionSocket$DigitalSocket",
                "jmri.jmrit.logixng.implementation.DefaultFemaleDigitalExpressionSocket",
                expression.getChild(0).getClass().getName(),
                "expression female socket is of correct class");
        assertFalse( expression.getChild(0).isConnected(),
                "expression female socket is not connected");

        // Setup action. This connects the child actions to this action
        expression.setup();

        jmri.util.JUnitAppender.assertMessage("cannot load digital expression IQDE52");

        assertEquals( "XYZ123", expression.getChild(0).getName(),
                "expression female socket name is XYZ123");
        assertEquals(
//                "jmri.jmrit.logixng.implementation.DefaultFemaleGenericExpressionSocket$DigitalSocket",
                "jmri.jmrit.logixng.implementation.DefaultFemaleDigitalExpressionSocket",
                expression.getChild(0).getClass().getName(),
                "expression female socket is of correct class");
        assertFalse( expression.getChild(0).isConnected(),
                "expression female socket is not connected");

        assertEquals( 1, expression.getChildCount(), "expression has 1 female socket");
    }

    @Test
    public void testCtorAndSetup2() {
        Not expression = new Not("IQDE321", null);
        assertNotNull( expression, "exists");
        assertEquals( 1, expression.getChildCount(), "expression has 1 female socket");
        expression.getChild(0).setName("XYZ123");
        expression.setSocketSystemName(null);

        assertEquals( "XYZ123", expression.getChild(0).getName(),
                "expression female socket name is XYZ123");
        assertEquals(
//                "jmri.jmrit.logixng.implementation.DefaultFemaleGenericExpressionSocket$DigitalSocket",
                "jmri.jmrit.logixng.implementation.DefaultFemaleDigitalExpressionSocket",
                expression.getChild(0).getClass().getName(),
                "expression female socket is of correct class");
        assertFalse( expression.getChild(0).isConnected(),
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

        assertEquals( 1, expression.getChildCount(), "expression has 1 female socket");
    }

    @Test
    public void testCtorAndSetup3() {
        DigitalExpressionManager m = InstanceManager.getDefault(DigitalExpressionManager.class);

        m.registerExpression(new ExpressionMemory("IQDE52", null));
        m.registerExpression(new ExpressionMemory("IQDE554", null));

        Not expression = new Not("IQDE321", null);
        assertNotNull( expression, "exists");
        assertEquals( 1, expression.getChildCount(), "expression has 1 female socket");
        expression.getChild(0).setName("XYZ123");
        expression.setSocketSystemName("IQDE52");

        assertEquals( "XYZ123", expression.getChild(0).getName(),
                "expression female socket name is XYZ123");
        assertEquals(
//                "jmri.jmrit.logixng.implementation.DefaultFemaleGenericExpressionSocket$DigitalSocket",
                "jmri.jmrit.logixng.implementation.DefaultFemaleDigitalExpressionSocket",
                expression.getChild(0).getClass().getName(),
                "expression female socket is of correct class");
        assertFalse( expression.getChild(0).isConnected(),
                "expression female socket is not connected");

        // Setup action. This connects the child actions to this action
        expression.setup();

        assertTrue( expression.getChild(0).isConnected(),
                "expression female socket is connected");
//        assertEquals( childSocket0,
//                expression.getChild(0).getConnectedSocket(), "child is correct bean");
        assertEquals( 1, expression.getChildCount(), "expression has 1 female socket");

        // Try run setup() again. That should not cause any problems.
        expression.setup();

        assertEquals( 1, expression.getChildCount(), "expression has 1 female socket");
    }

    @Test
    public void testGetChild() {
        assertEquals( 1, expressionNot.getChildCount(), "getChildCount() returns 1");

        assertNotNull( expressionNot.getChild(0),
                "getChild(0) returns a non null value");

        IllegalArgumentException ex = assertThrows( IllegalArgumentException.class, () ->
            expressionNot.getChild(1), "Exception is thrown");
        assertEquals( "index has invalid value: 1", ex.getMessage(), "Error message is correct");
    }

    @Test
    public void testCategory() {
        assertEquals( LogixNG_Category.COMMON, _base.getCategory(), "Category matches");
    }

    @Test
    public void testDescription() {
        Not e1 = new Not("IQDE321", null);
        assertEquals( "Not", e1.getShortDescription());
        assertEquals( "Not", e1.getLongDescription());
    }

    @Test
    @Override
    @Disabled("Not implemented")
    public void testEnableAndEvaluate() {
        // Not implemented.
        // This method is implemented for other digital expressions so no need
        // to add support here. It doesn't need to be tested for every digital
        // expression.
    }

    @Test
    @Override
    @Disabled("Not implemented")
    public void testDebugConfig() {
        // Not implemented.
        // This method is implemented for other digital expressions so no need
        // to add support here. It doesn't need to be tested for every digital
        // expression.
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
        conditionalNG.setRunDelayed(false);
        conditionalNG.setEnabled(true);
        logixNG.addConditionalNG(conditionalNG);
        IfThenElse ifThenElse = new IfThenElse("IQDA321", null);
        MaleSocket maleSocket =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(ifThenElse);
        conditionalNG.getChild(0).connect(maleSocket);

        expressionNot = new Not("IQDE321", null);
        MaleSocket maleSocket2 =
                InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expressionNot);
        ifThenElse.getChild(0).connect(maleSocket2);

        _base = expressionNot;
        _baseMaleSocket = maleSocket2;

        atomicBoolean = new AtomicBoolean(false);
        actionAtomicBoolean = new ActionAtomicBoolean(atomicBoolean, true);
        MaleSocket socketAtomicBoolean = InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionAtomicBoolean);
        ifThenElse.getChild(1).connect(socketAtomicBoolean);

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
