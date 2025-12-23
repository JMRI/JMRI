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
 * Test Hold
 *
 * @author Daniel Bergqvist 2018
 */
public class HoldTest extends AbstractDigitalExpressionTestBase {

    private LogixNG logixNG;
    private ConditionalNG conditionalNG;
    private Hold expressionHold;
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
                "Trigger on expression Trigger. Hold while expression Hold ::: Use default%n" +
                "   ? Trigger%n" +
                "      Socket not connected%n" +
                "   ? Hold%n" +
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
                "               Trigger on expression Trigger. Hold while expression Hold ::: Use default%n" +
                "                  ? Trigger%n" +
                "                     Socket not connected%n" +
                "                  ? Hold%n" +
                "                     Socket not connected%n" +
                "            ! Then%n" +
                "               Set the atomic boolean to true ::: Use default%n" +
                "            ! Else%n" +
                "               Socket not connected%n");
    }

    @Override
    public NamedBean createNewBean(String systemName) {
        return new Hold(systemName, null);
    }

    @Override
    public boolean addNewSocket() {
        return false;
    }

    @Test
    public void testCtor() {
        Hold expression2;

        expression2 = new Hold("IQDE321", null);
        assertNotNull( expression2, "object exists");
        assertNull( expression2.getUserName(), "Username matches");
        assertEquals( "Trigger on expression Trigger. Hold while expression Hold",
                expression2.getLongDescription(), "String matches");

        expression2 = new Hold("IQDE321", "My expression");
        assertNotNull( expression2, "object exists");
        assertEquals( "My expression", expression2.getUserName(), "Username matches");
        assertEquals( "Trigger on expression Trigger. Hold while expression Hold",
                expression2.getLongDescription(), "String matches");

        IllegalArgumentException ex = assertThrows( IllegalArgumentException.class, () -> {
            var t = new Hold("IQE55:12:XY11", null);
            fail("should have thrown, no created " + t);
        }, "Illegal system name Expected exception thrown");
        assertNotNull(ex);

        ex = assertThrows( IllegalArgumentException.class, () -> {
            var t = new Hold("IQE55:12:XY11", "A name");
            fail("should have thrown, no created " + t);
        }, "Illegal system name Expected exception thrown");
        assertNotNull(ex);
    }

    @Test
    public void testCtorAndSetup1() {
        Hold expression = new Hold("IQDE321", null);
        assertNotNull( expression, "exists");
        assertEquals( 2, expression.getChildCount(), "expression has 2 female sockets");
        expression.getChild(0).setName("XYZ123");
        expression.setTriggerExpressionSocketSystemName("IQDE52");
        expression.getChild(1).setName("ZH12");
        expression.setHoldActionSocketSystemName("IQDE554");

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
//                "jmri.jmrit.logixng.implementation.DefaultFemaleGenericExpressionSocket$DigitalSocket",
                "jmri.jmrit.logixng.implementation.DefaultFemaleDigitalExpressionSocket",
                expression.getChild(1).getClass().getName(),
                "expression female socket is of correct class");
        assertFalse( expression.getChild(1).isConnected(),
                "expression female socket is not connected");

        // Setup action. This connects the child actions to this action
        expression.setup();

        jmri.util.JUnitAppender.assertMessage("cannot load digital expression IQDE52");
        jmri.util.JUnitAppender.assertMessage("cannot load digital expression IQDE554");

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
//                "jmri.jmrit.logixng.implementation.DefaultFemaleGenericExpressionSocket$DigitalSocket",
                "jmri.jmrit.logixng.implementation.DefaultFemaleDigitalExpressionSocket",
                expression.getChild(1).getClass().getName(),
                "expression female socket is of correct class");
        assertFalse( expression.getChild(1).isConnected(),
                "expression female socket is not connected");

        assertEquals( 2, expression.getChildCount(), "expression has 2 female sockets");
    }

    @Test
    public void testCtorAndSetup2() {
        Hold expression = new Hold("IQDE321", null);
        assertNotNull( expression, "exists");
        assertEquals( 2, expression.getChildCount(), "expression has 2 female sockets");
        expression.getChild(0).setName("XYZ123");
        expression.setHoldActionSocketSystemName(null);
        expression.getChild(1).setName("ZH12");
        expression.setTriggerExpressionSocketSystemName(null);

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
//                "jmri.jmrit.logixng.implementation.DefaultFemaleGenericExpressionSocket$DigitalSocket",
                "jmri.jmrit.logixng.implementation.DefaultFemaleDigitalExpressionSocket",
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
        assertEquals(
//                "jmri.jmrit.logixng.implementation.DefaultFemaleGenericExpressionSocket$DigitalSocket",
                "jmri.jmrit.logixng.implementation.DefaultFemaleDigitalExpressionSocket",
                expression.getChild(1).getClass().getName(),
                "expression female socket is of correct class");
        assertFalse( expression.getChild(1).isConnected(),
                "expression female socket is not connected");

        assertEquals( 2, expression.getChildCount(), "expression has 2 female sockets");
    }

    @Test
    public void testCtorAndSetup3() {
        DigitalExpressionManager m = InstanceManager.getDefault(DigitalExpressionManager.class);

        m.registerExpression(new ExpressionMemory("IQDE52", null));
        m.registerExpression(new ExpressionMemory("IQDE554", null));

        Hold expression = new Hold("IQDE321", null);
        assertNotNull( expression, "exists");
        assertEquals( 2, expression.getChildCount(), "expression has 2 female sockets");
        expression.getChild(0).setName("XYZ123");
        expression.setHoldActionSocketSystemName("IQDE52");
        expression.getChild(1).setName("ZH12");
        expression.setTriggerExpressionSocketSystemName("IQDE554");

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
//                "jmri.jmrit.logixng.implementation.DefaultFemaleGenericExpressionSocket$DigitalSocket",
                "jmri.jmrit.logixng.implementation.DefaultFemaleDigitalExpressionSocket",
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
        assertEquals( 2, expression.getChildCount(),
                "expression has 2 female sockets");

        // Try run setup() again. That should not cause any problems.
        expression.setup();

        assertEquals( 2, expression.getChildCount(), "expression has 2 female sockets");
    }

    @Test
    public void testGetChild() {
        assertEquals( 2, expressionHold.getChildCount(), "getChildCount() returns 2");

        assertNotNull( expressionHold.getChild(0),
                "getChild(0) returns a non null value");
        assertNotNull( expressionHold.getChild(1),
                "getChild(1) returns a non null value");

        IllegalArgumentException ex = assertThrows( IllegalArgumentException.class, () ->
            expressionHold.getChild(2), "Exception is thrown");
        assertEquals( "index has invalid value: 2", ex.getMessage(), "Error message is correct");
    }

    @Test
    public void testCategory() {
        assertEquals( LogixNG_Category.OTHER, _base.getCategory(), "Category matches");
    }

    @Test
    public void testDescription() {
        Hold e1 = new Hold("IQDE321", null);
        assertEquals( "Hold", e1.getShortDescription());
        assertEquals( "Trigger on expression Trigger. Hold while expression Hold",
                e1.getLongDescription());
    }

    @Test
    @Override
    @Disabled("Not implemented.")
    public void testEnableAndEvaluate() {
        // Not implemented.
        // This method is implemented for other digital expressions so no need
        // to add support here. It doesn't need to be tested for every digital
        // expression.
    }

    @Test
    @Override
    @Disabled("Not implemented.")
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

        expressionHold = new Hold("IQDE321", null);
        MaleSocket maleSocket2 =
                InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expressionHold);
        ifThenElse.getChild(0).connect(maleSocket2);

        _base = expressionHold;
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
