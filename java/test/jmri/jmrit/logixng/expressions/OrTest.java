package jmri.jmrit.logixng.expressions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.IfThenElse;
import jmri.jmrit.logixng.implementation.DefaultConditionalNGScaffold;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test Or
 *
 * @author Daniel Bergqvist 2018
 */
public class OrTest extends AbstractDigitalExpressionTestBase {

    private LogixNG logixNG;
    private ConditionalNG conditionalNG;
    private Or expressionOr;


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
                "Or. Evaluate All ::: Use default%n" +
                "   ? E1%n" +
                "      Always true ::: Use default%n" +
                "   ? E2%n" +
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
                "               Or. Evaluate All ::: Use default%n" +
                "                  ? E1%n" +
                "                     Always true ::: Use default%n" +
                "                  ? E2%n" +
                "                     Socket not connected%n" +
                "            ! Then%n" +
                "               Socket not connected%n" +
                "            ! Else%n" +
                "               Socket not connected%n");
    }

    @Override
    public NamedBean createNewBean(String systemName) {
        return new Or(systemName, null);
    }

    @Override
    public boolean addNewSocket() throws SocketAlreadyConnectedException {
        int count = _base.getChildCount();
        for (int i=0; i < count; i++) {
            if (!_base.getChild(i).isConnected()) {
                _base.getChild(i).connect(getConnectableChild());
            }
        }
        return true;
    }

    @Test
    public void testCtor() {
        Or expression2;

        expression2 = new Or("IQDE321", null);
        assertNotNull( expression2, "object exists");
        assertNull( expression2.getUserName(), "Username matches");
        assertEquals( "Or. Evaluate All", expression2.getLongDescription(), "String matches");

        expression2 = new Or("IQDE321", "My expression");
        assertNotNull( expression2, "object exists");
        assertEquals( "My expression", expression2.getUserName(), "Username matches");
        assertEquals( "Or. Evaluate All", expression2.getLongDescription(), "String matches");

        IllegalArgumentException ex = assertThrows( IllegalArgumentException.class, () -> {
            var t = new Or("IQE55:12:XY11", null);
            fail("should not have created " + t);
        }, "Illegal system name Expected exception thrown");
        assertNotNull(ex);

        ex = assertThrows( IllegalArgumentException.class, () -> {
            var t = new Or("IQE55:12:XY11", "A name");
            fail("should not have created " + t);
        }, "Illegal system name Expected exception thrown");
        assertNotNull(ex);
    }

    // Test action when at least one child socket is not connected
    @Test
    public void testCtorAndSetup1() {
        DigitalExpressionManager m = InstanceManager.getDefault(DigitalExpressionManager.class);

        List<MaleSocket> maleSockets = new ArrayList<>();
        maleSockets.add(m.registerExpression(new ExpressionMemory("IQDE52", null)));
        maleSockets.add(null);  // This is null by purpose
        maleSockets.add(m.registerExpression(new ExpressionMemory("IQDE554", null)));
        maleSockets.add(null);  // This is null by purpose
        maleSockets.add(m.registerExpression(new ExpressionMemory("IQDE3", null)));

        List<Map.Entry<String, String>> actionSystemNames = new ArrayList<>();
        actionSystemNames.add(new java.util.HashMap.SimpleEntry<>("XYZ123", "IQDE52"));
        actionSystemNames.add(new java.util.HashMap.SimpleEntry<>("ZH12", null));   // This is null by purpose
        actionSystemNames.add(new java.util.HashMap.SimpleEntry<>("Hello", "IQDE554"));
        // IQDE61232 doesn't exist by purpose
        actionSystemNames.add(new java.util.HashMap.SimpleEntry<>("SomethingElse", "IQDE61232"));
        actionSystemNames.add(new java.util.HashMap.SimpleEntry<>("Yes123", "IQDE3"));

        Or expression = new Or("IQDE321", null, actionSystemNames);
        assertNotNull( expression, "exists");
        assertEquals( 5, expression.getChildCount(), "expression has 5 female sockets");

        for (int i=0; i < 5; i++) {
            Map.Entry<String,String> entry = actionSystemNames.get(i);
            assertEquals( entry.getKey(), expression.getChild(i).getName(),
                    "expression female socket name is "+entry.getKey());
            assertEquals(
//                    "jmri.jmrit.logixng.implementation.DefaultFemaleGenericExpressionSocket$DigitalSocket",
                "jmri.jmrit.logixng.implementation.DefaultFemaleDigitalExpressionSocket",
                    expression.getChild(i).getClass().getName(),
                    "expression female socket is of correct class");
            assertFalse( expression.getChild(i).isConnected(),
                    "expression female socket is not connected");
        }

        // Setup action. This connects the child actions to this action
        expression.setup();

        jmri.util.JUnitAppender.assertMessage("cannot load digital expression IQDE61232");

        for (int i=0; i < 5; i++) {
            Map.Entry<String,String> entry = actionSystemNames.get(i);
            assertEquals( entry.getKey(), expression.getChild(i).getName(),
                    "expression female socket name is "+entry.getKey());

            if (maleSockets.get(i) != null) {
                assertTrue( expression.getChild(i).isConnected(),
                        "expression female socket is connected");
//                Assert.assertEquals("child is correct bean",
//                        maleSockets.get(i),
//                        expression.getChild(i).getConnectedSocket());
            } else {
                assertFalse( expression.getChild(i).isConnected(),
                        "expression female socket is not connected");
            }
        }

        assertEquals( 5, expression.getChildCount(), "expression has 5 female sockets");
    }

    // Test action when at least one child socket is not connected.
    // This should never happen, but test it anyway.
    @Test
    public void testCtorAndSetup2() {
        DigitalExpressionManager m = InstanceManager.getDefault(DigitalExpressionManager.class);

        List<MaleSocket> maleSockets = new ArrayList<>();
        maleSockets.add(m.registerExpression(new ExpressionMemory("IQDE52", null)));
        maleSockets.add(m.registerExpression(new ExpressionMemory("IQDE99", null)));
        maleSockets.add(m.registerExpression(new ExpressionMemory("IQDE554", null)));
        maleSockets.add(m.registerExpression(new ExpressionMemory("IQDE61232", null)));
        maleSockets.add(m.registerExpression(new ExpressionMemory("IQDE3", null)));

        List<Map.Entry<String, String>> actionSystemNames = new ArrayList<>();
        actionSystemNames.add(new java.util.HashMap.SimpleEntry<>("XYZ123", "IQDE52"));
        actionSystemNames.add(new java.util.HashMap.SimpleEntry<>("ZH12", "IQDE99"));
        actionSystemNames.add(new java.util.HashMap.SimpleEntry<>("Hello", "IQDE554"));
        actionSystemNames.add(new java.util.HashMap.SimpleEntry<>("SomethingElse", "IQDE61232"));
        actionSystemNames.add(new java.util.HashMap.SimpleEntry<>("Yes123", "IQDE3"));

        Or expression = new Or("IQDE321", null, actionSystemNames);
        assertNotNull( expression, "exists");
        assertEquals( 5, expression.getChildCount(), "expression has 5 female sockets");

        for (int i=0; i < 5; i++) {
            Map.Entry<String,String> entry = actionSystemNames.get(i);
            assertEquals( entry.getKey(), expression.getChild(i).getName(),
                    () -> "expression female socket name is "+entry.getKey());
            assertEquals(
//                    "jmri.jmrit.logixng.implementation.DefaultFemaleGenericExpressionSocket$DigitalSocket",
                "jmri.jmrit.logixng.implementation.DefaultFemaleDigitalExpressionSocket",
                    expression.getChild(i).getClass().getName(),
                    "expression female socket is of correct class");
            assertFalse( expression.getChild(i).isConnected(),
                    "expression female socket is not connected");
        }

        // Setup action. This connects the child actions to this action
        expression.setup();

        for (int i=0; i < 5; i++) {
            Map.Entry<String,String> entry = actionSystemNames.get(i);
            assertEquals( entry.getKey(), expression.getChild(i).getName(),
                () -> "expression female socket name is "+entry.getKey());

            if (maleSockets.get(i) != null) {
                assertTrue( expression.getChild(i).isConnected(),
                        "expression female socket is connected");
//                Assert.assertEquals("child is correct bean",
//                        maleSockets.get(i),
//                        expression.getChild(i).getConnectedSocket());
            } else {
                assertFalse( expression.getChild(i).isConnected(),
                        "expression female socket is not connected");
            }
        }

        // Since all the sockets are connected, a new socket must have been created.
        assertEquals( 6, expression.getChildCount(), "expression has 6 female sockets");

        // Try run setup() again. That should not cause any problems.
        expression.setup();

        assertEquals( 6, expression.getChildCount(), "expression has 6 female sockets");
    }

    // Test calling setActionSystemNames() twice
    @Test
    public void testCtorAndSetup3() throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException {
        List<Map.Entry<String, String>> actionSystemNames = new ArrayList<>();
        actionSystemNames.add(new java.util.HashMap.SimpleEntry<>("XYZ123", "IQDE52"));

        Or expression = new Or("IQDE321", null, actionSystemNames);

        java.lang.reflect.Method method =
                expression.getClass().getDeclaredMethod("setExpressionSystemNames", new Class<?>[]{List.class});
        method.setAccessible(true);

        InvocationTargetException e = assertThrows( InvocationTargetException.class, () ->
            method.invoke(expression, new Object[]{null}), "Exception thrown");
        RuntimeException cause = assertInstanceOf( RuntimeException.class, e.getCause());
        assertEquals( "expression system names cannot be set more than once",
                cause.getMessage(), "Exception message is correct");
    }

    @Test
    public void testGetChild() throws SocketAlreadyConnectedException {
        Or expression2 = new Or("IQDE321", null);

        for (int i=0; i < 3; i++) {
            assertEquals( i+1, expression2.getChildCount(), "getChildCount() returns "+i);

            assertNotNull( expression2.getChild(0),
                    "getChild(0) returns a non null value");

            assertIndexOutOfBoundsException(expression2::getChild, i+1, i+1);

            // Connect a new child expression
            True expr = new True("IQDE"+i, null);
            MaleSocket maleSocket =
                    InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expr);
            expression2.getChild(i).connect(maleSocket);
        }
    }

    @Test
    public void testCategory() {
        assertEquals( LogixNG_Category.COMMON, _base.getCategory(), "Category matches");
    }

    // Test the methods connected(FemaleSocket) and getExpressionSystemName(int)
    @Test
    public void testConnected_getExpressionSystemName() throws SocketAlreadyConnectedException {
        Or expression = new Or("IQDE121", null);

        ExpressionMemory stringExpressionMemory = new ExpressionMemory("IQDE122", null);
        MaleSocket maleSAMSocket =
                InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(stringExpressionMemory);

        assertEquals( 1, expression.getChildCount(), "Num children is correct");

        // Test connect and disconnect
        expression.getChild(0).connect(maleSAMSocket);
        assertEquals( 2, expression.getChildCount(), "Num children is correct");
        assertEquals( "IQDE122", expression.getExpressionSystemName(0), "getExpressionSystemName(0) is correct");
        assertNull( expression.getExpressionSystemName(1), "getExpressionSystemName(1) is null");
        expression.getChild(0).disconnect();
        assertEquals( 2, expression.getChildCount(), "Num children is correct");
        assertNull( expression.getExpressionSystemName(0), "getExpressionSystemName(0) is null");
        assertNull( expression.getExpressionSystemName(1), "getExpressionSystemName(1) is null");

        expression.getChild(1).connect(maleSAMSocket);
        assertEquals( 2, expression.getChildCount(), "Num children is correct");
        assertNull( expression.getExpressionSystemName(0), "getExpressionSystemName(0) is null");
        assertEquals( "IQDE122", expression.getExpressionSystemName(1), "getExpressionSystemName(1) is correct");
        expression.getChild(0).disconnect();    // Test removing child with the wrong index.
        assertEquals( 2, expression.getChildCount(), "Num children is correct");
        assertNull( expression.getExpressionSystemName(0), "getExpressionSystemName(0) is null");
        assertEquals( "IQDE122", expression.getExpressionSystemName(1), "getExpressionSystemName(1) is correct");
        expression.getChild(1).disconnect();
        assertEquals( 2, expression.getChildCount(), "Num children is correct");
        assertNull( expression.getExpressionSystemName(0), "getExpressionSystemName(0) is null");
        assertNull( expression.getExpressionSystemName(1), "getExpressionSystemName(1) is null");
    }

    @Test
    public void testDescription() {
        Or e1 = new Or("IQDE321", null);
        assertEquals( "Or", e1.getShortDescription());
        assertEquals( "Or. Evaluate All", e1.getLongDescription());
    }

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
        conditionalNG.setRunDelayed(false);
        conditionalNG.setEnabled(true);
        logixNG.addConditionalNG(conditionalNG);
        IfThenElse ifThenElse = new IfThenElse("IQDA321", null);
        MaleSocket maleSocket =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(ifThenElse);
        conditionalNG.getChild(0).connect(maleSocket);

        expressionOr = new Or("IQDE321", null);
        MaleSocket maleSocket2 =
                InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expressionOr);
        ifThenElse.getChild(0).connect(maleSocket2);

        DigitalExpressionBean childExpression = new True("IQDE322", null);
        MaleSocket maleSocketChild =
                InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(childExpression);
        maleSocket2.getChild(0).connect(maleSocketChild);

        _base = expressionOr;
        _baseMaleSocket = maleSocket2;

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

}
