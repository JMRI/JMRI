package jmri.jmrit.logixng.expressions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.beans.PropertyChangeEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.ActionAtomicBoolean;
import jmri.jmrit.logixng.actions.IfThenElse;
import jmri.jmrit.logixng.expressions.DigitalFormula.SocketData;
import jmri.jmrit.logixng.implementation.DefaultConditionalNGScaffold;
import jmri.jmrit.logixng.util.parser.ParserException;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Before;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test Formula
 *
 * @author Daniel Bergqvist 2018
 */
public class DigitalFormulaTest extends AbstractDigitalExpressionTestBase {

    private LogixNG logixNG;
    private ConditionalNG conditionalNG;
    private DigitalFormula expressionFormula;
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
        DigitalExpressionBean childExpression = new True("IQDE"+Integer.toString(beanID), null);
        beanID++;
        MaleSocket maleSocketChild =
                InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(childExpression);
        return maleSocketChild;
    }

    @Override
    public String getExpectedPrintedTree() {
        return String.format(
                "Digital Formula: E1 ::: Use default%n" +
                "   ?* E1%n" +
                "      Always true ::: Use default%n" +
                "   ?* E2%n" +
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
                "               Digital Formula: E1 ::: Use default%n" +
                "                  ?* E1%n" +
                "                     Always true ::: Use default%n" +
                "                  ?* E2%n" +
                "                     Socket not connected%n" +
                "            ! Then%n" +
                "               Set the atomic boolean to true ::: Use default%n" +
                "            ! Else%n" +
                "               Socket not connected%n");
    }

    @Override
    public NamedBean createNewBean(String systemName) {
        DigitalFormula a = new DigitalFormula(systemName, null);
//        a.setFormula("R1");
        return a;
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
        DigitalFormula expression2;

        expression2 = new DigitalFormula("IQDE321", null);
//        expression2.setFormula("R1");
        assertNotNull( expression2, "object exists");
        assertNull( expression2.getUserName(), "Username matches");
//        Assert.assertEquals("String matches", "Formula: R1", expression2.getLongDescription());

        expression2 = new DigitalFormula("IQDE321", "My expression");
//        expression2.setFormula("R1");
        assertNotNull( expression2, "object exists");
        assertEquals( "My expression", expression2.getUserName(), "Username matches");
//        Assert.assertEquals("String matches", "Formula: R1", expression2.getLongDescription());

        expression2 = new DigitalFormula("IQDE321", null);
//        expression2.setFormula("R1 and R2");
        assertNotNull( expression2, "object exists");
        assertNull( expression2.getUserName(), "Username matches");
//        Assert.assertEquals("String matches", "Formula: R1 and R2", expression2.getLongDescription());

        expression2 = new DigitalFormula("IQDE321", "My expression");
//        expression2.setFormula("R1 or R2");
        assertNotNull( expression2, "object exists");
        assertEquals( "My expression", expression2.getUserName(), "Username matches");
//        Assert.assertEquals("String matches", "Formula: R1 or R2", expression2.getLongDescription());

        IllegalArgumentException ex = assertThrows( IllegalArgumentException.class,
            () -> {
                DigitalFormula df = new DigitalFormula("IQE55:12:XY11", null);
                assertNull(df, "should not reach here");
            }, "Illegal system name Expected exception thrown");
        assertNotNull(ex);

        ex = assertThrows( IllegalArgumentException.class,
            () -> {
                DigitalFormula df = new DigitalFormula("IQE55:12:XY11", "A name");
                assertNull(df, "should not reach here");
            }, "Illegal system name Expected exception thrown");
        assertNotNull(ex);
    }

    // Test action when at least one child socket is not connected
    @Test
    public void testCtorAndSetup1() {
        DigitalExpressionManager m = InstanceManager.getDefault(DigitalExpressionManager.class);
        String managerName = m.getClass().getName();

        List<MaleSocket> maleSockets = new ArrayList<>();
        maleSockets.add(m.registerExpression(new ExpressionMemory("IQDE52", null)));
        maleSockets.add(null);  // This is null by purpose
        maleSockets.add(m.registerExpression(new ExpressionMemory("IQDE554", null)));
        maleSockets.add(null);  // This is null by purpose
        maleSockets.add(m.registerExpression(new ExpressionMemory("IQDE3", null)));

        List<SocketData> expressionSystemNames = new ArrayList<>();
        expressionSystemNames.add(new SocketData("XYZ123", "IQDE52", managerName));
        expressionSystemNames.add(new SocketData("ZH12", null, managerName));   // This is null by purpose
        expressionSystemNames.add(new SocketData("Hello", "IQDE554", managerName));
        // IQDE61232 doesn't exist by purpose
        expressionSystemNames.add(new SocketData("SomethingElse", "IQDE61232", managerName));
        expressionSystemNames.add(new SocketData("Yes123", "IQDE3", managerName));

        jmri.jmrit.logixng.expressions.DigitalFormula expression = new jmri.jmrit.logixng.expressions.DigitalFormula("IQDE321", null, expressionSystemNames);
        assertNotNull( expression, "exists");
        assertEquals( 5, expression.getChildCount(), "expression has 5 female sockets");

        for (int i=0; i < 5; i++) {
            SocketData socketData = expressionSystemNames.get(i);
            assertEquals( socketData._socketName, expression.getChild(i).getName(),
                () -> "expression female socket name is "+socketData._socketName);
            assertEquals( "jmri.jmrit.logixng.implementation.DefaultFemaleGenericExpressionSocket",
                expression.getChild(i).getClass().getName(),
                "expression female socket is of correct class");
            assertFalse( expression.getChild(i).isConnected(),
                "expression female socket is not connected");
        }

        // Setup action. This connects the child actions to this action
        expression.setup();

        jmri.util.JUnitAppender.assertMessage("cannot load digital expression IQDE61232");

        for (int i=0; i < 5; i++) {
            SocketData socketData = expressionSystemNames.get(i);
            assertEquals( socketData._socketName, expression.getChild(i).getName(),
                () -> "expression female socket name is "+socketData._socketName);

            if (maleSockets.get(i) != null) {
                assertTrue( expression.getChild(i).isConnected(),
                    "expression female socket is connected");
//                assertEquals( maleSockets.get(i),
//                        expression.getChild(i).getConnectedSocket(), "child is correct bean");
            } else {
                assertFalse( expression.getChild(i).isConnected(),
                    "expression female socket is not connected");
            }
        }

        assertEquals( 5, expression.getChildCount(), "expression has 5 female sockets");
    }

    @Test
    public void testCtorAndSetup2() {
        DigitalExpressionManager m = InstanceManager.getDefault(DigitalExpressionManager.class);
        String managerName = m.getClass().getName();

        List<MaleSocket> maleSockets = new ArrayList<>();
        maleSockets.add(m.registerExpression(new ExpressionMemory("IQDE52", null)));
        maleSockets.add(m.registerExpression(new ExpressionMemory("IQDE99", null)));
        maleSockets.add(m.registerExpression(new ExpressionMemory("IQDE554", null)));
        maleSockets.add(m.registerExpression(new ExpressionMemory("IQDE61232", null)));
        maleSockets.add(m.registerExpression(new ExpressionMemory("IQDE3", null)));

        List<SocketData> expressionSystemNames = new ArrayList<>();
        expressionSystemNames.add(new SocketData("XYZ123", "IQDE52", managerName));
        expressionSystemNames.add(new SocketData("ZH12", "IQDE99", managerName));
        expressionSystemNames.add(new SocketData("Hello", "IQDE554", managerName));
        expressionSystemNames.add(new SocketData("SomethingElse", "IQDE61232", managerName));
        expressionSystemNames.add(new SocketData("Yes123", "IQDE3", managerName));

        DigitalFormula expression = new DigitalFormula("IQDE321", null, expressionSystemNames);
        assertNotNull( expression, "exists");
        assertEquals( 5, expression.getChildCount(), "expression has 5 female sockets");

        for (int i=0; i < 5; i++) {
            SocketData socketData = expressionSystemNames.get(i);
            assertEquals( socketData._socketName, expression.getChild(i).getName(),
                () -> "expression female socket name is "+socketData._socketName);
            assertEquals( "jmri.jmrit.logixng.implementation.DefaultFemaleGenericExpressionSocket",
                expression.getChild(i).getClass().getName(),
                "expression female socket is of correct class");
            assertFalse( expression.getChild(i).isConnected(),
                "expression female socket is not connected");
        }

        // Setup action. This connects the child actions to this action
        expression.setup();

        for (int i=0; i < 5; i++) {
            SocketData socketData = expressionSystemNames.get(i);
            assertEquals( socketData._socketName, expression.getChild(i).getName(),
                () -> "expression female socket name is "+socketData._socketName);

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
        List<SocketData> expressionSystemNames = new ArrayList<>();
        expressionSystemNames.add(new SocketData("XYZ123", "IQDE52", ""));

        jmri.jmrit.logixng.expressions.DigitalFormula expression = new jmri.jmrit.logixng.expressions.DigitalFormula("IQDE321", null, expressionSystemNames);

        java.lang.reflect.Method method =
                expression.getClass().getDeclaredMethod("setExpressionSystemNames", new Class<?>[]{List.class});
        method.setAccessible(true);

        InvocationTargetException e = assertThrows( InvocationTargetException.class,
            () -> method.invoke(expression, new Object[]{null}));
        RuntimeException ex = assertInstanceOf( RuntimeException.class, e.getCause());
        assertEquals( "expression system names cannot be set more than once",
            ex.getMessage(), "Exception message is correct");
    }

    @Test
    public void testSetChildCount() throws SocketAlreadyConnectedException {
        DigitalFormula a = (DigitalFormula)_base;
        AtomicBoolean ab = new AtomicBoolean(false);

        _base.addPropertyChangeListener((PropertyChangeEvent evt) -> {
            ab.set(true);
        });

        a.setChildCount(1);
        assertEquals( 1, a.getChildCount(), "numChilds are correct");

        // Test increase num children
        ab.set(false);
        a.setChildCount(a.getChildCount()+1);
        assertEquals( 2, a.getChildCount(), "numChilds are correct");
        assertTrue( ab.get(), "PropertyChangeEvent fired");

        // Test decrease num children
        ab.set(false);
        assertTrue( a.getChildCount() > 1, "We have least two children");
        a.setChildCount(1);
        assertEquals( 1, a.getChildCount(), "numChilds are correct");
        assertTrue( ab.get(), "PropertyChangeEvent fired");

        // Test decrease num children when all children are connected
        ab.set(false);
        a.getChild(0).disconnect();
        a.getChild(0).connect(getConnectableChild());
        a.getChild(1).disconnect();
        a.getChild(1).connect(getConnectableChild());
        a.getChild(2).disconnect();
        a.getChild(2).connect(getConnectableChild());
        assertEquals( 4, a.getChildCount(), "numChilds are correct");
        a.setChildCount(2);
        assertEquals( 2, a.getChildCount(), "numChilds are correct");
        assertTrue( ab.get(), "PropertyChangeEvent fired");

        // Test setChildCount to same number of children as before
        ab.set(false);
        a.setChildCount(2);
        assertEquals( 2, a.getChildCount(), "numChilds are correct");
        assertFalse( ab.get(), "PropertyChangeEvent not fired");
    }

    @Test
    public void testFormula() throws ParserException, SocketAlreadyConnectedException {
        DigitalFormula a = (DigitalFormula)_base;

        a.getChild(0).disconnect();
        a.getChild(0).connect(getConnectableChild());
        a.getChild(0).setName("Abc");
        a.getChild(1).disconnect();
        a.getChild(1).connect(getConnectableChild());
        a.getChild(1).setName("Xyz");

        a.setFormula("Xyz + Abc");
        assertEquals( "Xyz + Abc", a.getFormula(), "Formula is correct");

        a.setFormula("Abc - Xyz");
        assertEquals( "Abc - Xyz", a.getFormula(), "Formula is correct");
    }

    @Test
    public void testGetChild() throws JmriException {
        DigitalFormula expression2 = new DigitalFormula("IQDE321", null);
//        expression2.setFormula("R1");

        for (int i=0; i < 3; i++) {
            assertEquals( i+1, expression2.getChildCount(),
                "getChildCount() returns "+i);

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
        assertSame( LogixNG_Category.COMMON, _base.getCategory(), "Category matches");
    }

    // Test the methods connected(FemaleSocket) and getExpressionSystemName(int)
    @Test
    public void testConnected_getExpressionSystemName() throws SocketAlreadyConnectedException {
        DigitalFormula expression = new DigitalFormula("IQDE121", null);

        ExpressionMemory expressionMemory = new ExpressionMemory("IQDE122", null);
        MaleSocket maleSAMSocket =
                InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expressionMemory);

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
        DigitalFormula expression = new DigitalFormula("IQDE321", null);
        assertEquals( "Digital Formula", expression.getShortDescription(), "strings matches");
        assertEquals( "Digital Formula: empty", expression.getLongDescription(), "strings matches");
    }

    @Test
    public void testEvaluateEmptyFormula() throws ParserException, JmriException {
        DigitalFormula expression = new DigitalFormula("IQDE321", null);
        expression.setFormula("");
        assertFalse( expression.evaluate(), "Empty formula returns false");
    }
/*
    private void testValidate(boolean expectedResult, String formula, List<DigitalExpressionBean> conditionalVariablesList) throws Exception {
        Formula ix1 = new Formula("IQDE321", "IXIC 1");
        ix1.setFormula("R1");

        int count = 0;
        List<ExpressionEntry> expressionEntryList = new ArrayList<>();
        for (DigitalExpressionBean expressionFormula : conditionalVariablesList) {
            String socketName = "E"+Integer.toString(count++);
            FemaleDigitalExpressionSocket socket =
                    InstanceManager.getDefault(DigitalExpressionManager.class)
                            .createFemaleSocket(null, this, socketName);
            socket.connect((MaleSocket) expressionFormula);
            expressionEntryList.add(new ExpressionEntry(socket, socketName));
        }

        if (expectedResult) {
            Assert.assertTrue("validateFormula() returns null for '"+formula+"'",
                    ix1.validateFormula(formula, expressionEntryList) == null);
        } else {
            Assert.assertTrue("validateFormula() returns error message for '"+formula+"'",
                    ix1.validateFormula(formula, expressionEntryList) != null);
        }
    }

    private void testCalculate(int expectedResult, String formula,
            List<DigitalExpressionBean> conditionalVariablesList, String errorMessage)
            throws Exception {

        Formula ix1 = new Formula("IQDE321", "IXIC 1");
        ix1.setFormula(formula);

//        for (int i=0; i < ix1.getChildCount(); i++) {
//            ix1.getChild(i).disconnect();
//        }

        ix1.setChildCount(conditionalVariablesList.size());

        for (int i=0; i < conditionalVariablesList.size(); i++) {
            ix1.getChild(i).connect((MaleSocket)conditionalVariablesList.get(i));
        }

        switch (expectedResult) {
            case Formula.FALSE:
                Assert.assertFalse("validateFormula() returns FALSE for '"+formula+"'",
                        ix1.evaluate());
                break;

            case Formula.TRUE:
//                System.err.format("formula: %s%n", formula);
//                System.err.format("variable: %b%n", conditionalVariablesList.get(0).evaluate(isCompleted));
                Assert.assertTrue("validateFormula() returns TRUE for '"+formula+"'",
                        ix1.evaluate());
                break;

            default:
                throw new RuntimeException(String.format("Unknown expected result: %d", expectedResult));
        }

        if (! errorMessage.isEmpty()) {
            jmri.util.JUnitAppender.assertErrorMessageStartsWith(errorMessage);
        }
    }

    @Test
    public void testValidate() throws Exception {
        DigitalExpressionBean[] conditionalVariables_Empty = { };
        List<DigitalExpressionBean> conditionalVariablesList_Empty = Arrays.asList(conditionalVariables_Empty);

        DigitalExpressionBean trueExpression =
                InstanceManager.getDefault(
                        DigitalExpressionManager.class).registerExpression(
                                new True(InstanceManager.getDefault(DigitalExpressionManager.class).getAutoSystemName(), null));
//        DigitalExpressionBean falseExpression = InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(new False(conditionalNG));

        DigitalExpressionBean[] conditionalVariables_True
                = { trueExpression };
        List<DigitalExpressionBean> conditionalVariablesList_True = Arrays.asList(conditionalVariables_True);

        DigitalExpressionBean[] conditionalVariables_TrueTrueTrue
                = { trueExpression
                        , trueExpression
                        , trueExpression };
        List<DigitalExpressionBean> conditionalVariablesList_TrueTrueTrue = Arrays.asList(conditionalVariables_TrueTrueTrue);

        // Test empty formula string
        testValidate(EXPECT_FAILURE, "", conditionalVariablesList_Empty);

        testValidate(EXPECT_SUCCESS, "R1", conditionalVariablesList_True);
        testValidate(EXPECT_FAILURE, "R2", conditionalVariablesList_True);

        // Test parentheses
        testValidate(EXPECT_SUCCESS, "([{R1)}]", conditionalVariablesList_True);
        testValidate(EXPECT_FAILURE, "(R2", conditionalVariablesList_True);
        testValidate(EXPECT_FAILURE, "R2)", conditionalVariablesList_True);

        // Test several items
        testValidate(EXPECT_FAILURE, "R1 and R2 and R3", conditionalVariablesList_True);
        testValidate(EXPECT_FAILURE, "R1", conditionalVariablesList_TrueTrueTrue);
        testValidate(EXPECT_SUCCESS, "R1 and R2 and R3", conditionalVariablesList_TrueTrueTrue);

        // Test uppercase and lowercase
        testValidate(EXPECT_SUCCESS, "R2 AND R1 or R3", conditionalVariablesList_TrueTrueTrue);

        // Test several items and parenthese
        testValidate(EXPECT_SUCCESS, "(R1 and R3) and not R2", conditionalVariablesList_TrueTrueTrue);
        testValidate(EXPECT_FAILURE, "(R1 and) R3 and not R2", conditionalVariablesList_TrueTrueTrue);
        testValidate(EXPECT_FAILURE, "R1( and R3) and not R2", conditionalVariablesList_TrueTrueTrue);
        testValidate(EXPECT_FAILURE, "R1 (and R3 and) not R2", conditionalVariablesList_TrueTrueTrue);
        testValidate(EXPECT_FAILURE, "(R1 and R3) and not R2)", conditionalVariablesList_TrueTrueTrue);
        testValidate(EXPECT_SUCCESS, "(R1 and (R3) and not R2)", conditionalVariablesList_TrueTrueTrue);

        // Test invalid combinations
        testValidate(EXPECT_FAILURE, "R1 and or R3 and R2", conditionalVariablesList_TrueTrueTrue);
        testValidate(EXPECT_FAILURE, "R1 or or R3 and R2", conditionalVariablesList_TrueTrueTrue);
        testValidate(EXPECT_FAILURE, "R1 or and R3 and R2", conditionalVariablesList_TrueTrueTrue);
        testValidate(EXPECT_FAILURE, "R1 not R3 and R2", conditionalVariablesList_TrueTrueTrue);
        testValidate(EXPECT_FAILURE, "and R1 not R3 and R2", conditionalVariablesList_TrueTrueTrue);
        testValidate(EXPECT_FAILURE, "R1 or R3 and R2 or", conditionalVariablesList_TrueTrueTrue);
    }

    @Test
    public void testCalculate() throws Exception {
        DigitalExpressionBean[] conditionalVariables_Empty = { };
        List<DigitalExpressionBean> conditionalVariablesList_Empty = Arrays.asList(conditionalVariables_Empty);

        DigitalExpressionBean trueExpression =
                InstanceManager.getDefault(
                        DigitalExpressionManager.class).registerExpression(
                                new True(InstanceManager.getDefault(DigitalExpressionManager.class).getAutoSystemName(), null));
        DigitalExpressionBean falseExpression =
                InstanceManager.getDefault(
                        DigitalExpressionManager.class).registerExpression(
                                new False(InstanceManager.getDefault(DigitalExpressionManager.class).getAutoSystemName(), null));

        DigitalExpressionBean[] conditionalVariables_True
                = { trueExpression };
        List<DigitalExpressionBean> conditionalVariablesList_True = Arrays.asList(conditionalVariables_True);

        DigitalExpressionBean[] conditionalVariables_False
                = { falseExpression };
        List<DigitalExpressionBean> conditionalVariablesList_False = Arrays.asList(conditionalVariables_False);

        DigitalExpressionBean[] conditionalVariables_TrueTrueTrue
                = { trueExpression
                        , trueExpression
                        , trueExpression };
        List<DigitalExpressionBean> conditionalVariablesList_TrueTrueTrue = Arrays.asList(conditionalVariables_TrueTrueTrue);

        // Test with two digit variable numbers
        DigitalExpressionBean[] conditionalVariables_TrueTrueFalseTrueTrueFalseTrueTrueFalseTrueTrueFalse
                = {trueExpression
                        , trueExpression
                        , falseExpression
                        , trueExpression
                        , trueExpression
                        , falseExpression
                        , trueExpression
                        , trueExpression
                        , falseExpression
                        , trueExpression
                        , trueExpression
                        , falseExpression };
        List<DigitalExpressionBean> conditionalVariablesList_TrueTrueFalseTrueTrueFalseTrueTrueFalseTrueTrueFalse =
                Arrays.asList(conditionalVariables_TrueTrueFalseTrueTrueFalseTrueTrueFalseTrueTrueFalse);


        // Test empty formula string
        testCalculate(Formula.FALSE, "", conditionalVariablesList_Empty, "");
//        testCalculate(Formula.FALSE, "", conditionalVariablesList_True,
//                "IXIC 1 parseCalculation error formula= , ex= java.lang.StringIndexOutOfBoundsException");
        testCalculate(Formula.FALSE, "", conditionalVariablesList_True, "");

        // Test illegal number
        testCalculate(Formula.FALSE, "R#", conditionalVariablesList_True,
                "IXIC 1 parseCalculation error formula= R#, ex= java.lang.NumberFormatException");
        testCalculate(Formula.FALSE, "R-", conditionalVariablesList_True,
                "IXIC 1 parseCalculation error formula= R-, ex= java.lang.NumberFormatException");
        testCalculate(Formula.FALSE, "Ra", conditionalVariablesList_True,
                "IXIC 1 parseCalculation error formula= Ra, ex= java.lang.NumberFormatException");

        // Test single condition
        testCalculate(Formula.TRUE, "R1", conditionalVariablesList_True, "");
        testCalculate(Formula.FALSE, "R1", conditionalVariablesList_False, "");
        testCalculate(Formula.FALSE, "not R1", conditionalVariablesList_True, "");
        testCalculate(Formula.TRUE, "not R1", conditionalVariablesList_False, "");

        // Test single item but wrong item (R2 instead of R1)
//        testCalculate(Formula.FALSE, "R2)", conditionalVariablesList_True,
//                "IXIC 1 parseCalculation error formula= R2), ex= java.lang.ArrayIndexOutOfBoundsException");

        // Test two digit variable numbers
        testCalculate(Formula.TRUE, "R3 and R12 or R5 and R10",
                conditionalVariablesList_TrueTrueFalseTrueTrueFalseTrueTrueFalseTrueTrueFalse, "");
        testCalculate(Formula.FALSE, "R3 and (R12 or R5) and R10",
                conditionalVariablesList_TrueTrueFalseTrueTrueFalseTrueTrueFalseTrueTrueFalse, "");
        testCalculate(Formula.FALSE, "R12 and R10",
                conditionalVariablesList_TrueTrueFalseTrueTrueFalseTrueTrueFalseTrueTrueFalse, "");
        testCalculate(Formula.TRUE, "R12 or R10",
                conditionalVariablesList_TrueTrueFalseTrueTrueFalseTrueTrueFalseTrueTrueFalse, "");
        testCalculate(Formula.FALSE, "not (R12 or R10)",
                conditionalVariablesList_TrueTrueFalseTrueTrueFalseTrueTrueFalseTrueTrueFalse, "");

        // Test parentheses
        testCalculate(Formula.TRUE, "([{R1)}]", conditionalVariablesList_True, "");
//        testCalculate(Formula.FALSE, "(R2", conditionalVariablesList_True,
//                "IXIC 1 parseCalculation error formula= (R2, ex= java.lang.ArrayIndexOutOfBoundsException");

        // Test several items
        testCalculate(Formula.FALSE, "R1 and R2 and R3", conditionalVariablesList_True,
                "IXIC 1 parseCalculation error formula= R1 and R2 and R3, ex= java.lang.IndexOutOfBoundsException");
        testCalculate(Formula.TRUE, "R1", conditionalVariablesList_TrueTrueTrue, "");
        testCalculate(Formula.TRUE, "R2", conditionalVariablesList_TrueTrueTrue, "");
        testCalculate(Formula.TRUE, "R3", conditionalVariablesList_TrueTrueTrue, "");
        testCalculate(Formula.TRUE, "R1 and R2 and R3", conditionalVariablesList_TrueTrueTrue, "");
        testCalculate(Formula.TRUE, "R2 AND R1 or R3", conditionalVariablesList_TrueTrueTrue, "");

        // Test invalid combinations of and, or, not
        testCalculate(Formula.FALSE, "R1 and or R3 and R2", conditionalVariablesList_TrueTrueTrue,
                "IXIC 1 parseCalculation error formula= R1 and or R3 and R2, ex= jmri.JmriException: Unexpected operator or characters < ORR3ANDR2 >");
        testCalculate(Formula.FALSE, "R1 or or R3 and R2", conditionalVariablesList_TrueTrueTrue,
                "IXIC 1 parseCalculation error formula= R1 or or R3 and R2, ex= jmri.JmriException: Unexpected operator or characters < ORR3ANDR2 >");
        testCalculate(Formula.FALSE, "R1 or and R3 and R2", conditionalVariablesList_TrueTrueTrue,
                "IXIC 1 parseCalculation error formula= R1 or and R3 and R2, ex= jmri.JmriException: Unexpected operator or characters < ANDR3ANDR2 >");
        testCalculate(Formula.FALSE, "R1 not R3 and R2", conditionalVariablesList_TrueTrueTrue,
                "IXIC 1 parseCalculation error formula= R1 not R3 and R2, ex= jmri.JmriException: Could not find expected operator < NOTR3ANDR2 >");
        testCalculate(Formula.FALSE, "and R1 not R3 and R2", conditionalVariablesList_TrueTrueTrue,
                "IXIC 1 parseCalculation error formula= and R1 not R3 and R2, ex= jmri.JmriException: Unexpected operator or characters < ANDR1NOTR3ANDR2 >");
        testCalculate(Formula.FALSE, "R1 or R3 and R2 or", conditionalVariablesList_TrueTrueTrue,
                "IXIC 1 parseCalculation error formula= R1 or R3 and R2 or, ex= java.lang.StringIndexOutOfBoundsException");

        // Test several items and parenthese
        testCalculate(Formula.TRUE, "(R1 and R3) and R2", conditionalVariablesList_TrueTrueTrue, "");
        testCalculate(Formula.FALSE, "(R1 and R3) and not R2", conditionalVariablesList_TrueTrueTrue, "");
        testCalculate(Formula.FALSE, "(R1 and) R3 and not R2", conditionalVariablesList_TrueTrueTrue,
                "IXIC 1 parseCalculation error formula= (R1 and) R3 and not R2, ex= jmri.JmriException: Unexpected operator or characters < )R3ANDNOTR2 >");
        testCalculate(Formula.FALSE, "R1( and R3) and not R2", conditionalVariablesList_TrueTrueTrue,
                "IXIC 1 parseCalculation error formula= R1( and R3) and not R2, ex= jmri.JmriException: Could not find expected operator < (ANDR3)ANDNOTR2 >");
        testCalculate(Formula.FALSE, "R1 (and R3 and) not R2", conditionalVariablesList_TrueTrueTrue,
                "IXIC 1 parseCalculation error formula= R1 (and R3 and) not R2, ex= jmri.JmriException: Could not find expected operator < (ANDR3AND)NOTR2 >");
        testCalculate(Formula.FALSE, "(R1 and R3) and not R2)", conditionalVariablesList_TrueTrueTrue, "");
        testCalculate(Formula.TRUE, "(R1 and (R3) and R2)", conditionalVariablesList_TrueTrueTrue, "");
        testCalculate(Formula.FALSE, "(R1 and (R3) and not R2)", conditionalVariablesList_TrueTrueTrue, "");
    }
*/

    @Before
    @BeforeEach
    public void setUp() throws JmriException {
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

        expressionFormula = new DigitalFormula("IQDE321", null);
//        expressionFormula.setFormula("1");
//        expressionFormula.setFormula("true");
        expressionFormula.setFormula("E1");
        MaleSocket maleSocketExpressionFormula =
                InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expressionFormula);
        ifThenElse.getChild(0).connect(maleSocketExpressionFormula);

        DigitalExpressionBean childExpression = new True("IQDE322", null);
        MaleSocket maleSocketChild =
                InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(childExpression);
        maleSocketExpressionFormula.getChild(0).connect(maleSocketChild);

        _base = expressionFormula;
        _baseMaleSocket = maleSocketExpressionFormula;

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
