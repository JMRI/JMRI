package jmri.jmrit.logixng.util.parser.functions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

import jmri.InstanceManager;
import jmri.jmrit.logixng.SymbolTable;
import jmri.jmrit.logixng.implementation.DefaultConditionalNG;
import jmri.jmrit.logixng.implementation.DefaultSymbolTable;
import jmri.jmrit.logixng.util.LogixNG_Thread;
import jmri.jmrit.logixng.util.parser.*;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test ParsedExpression
 *
 * @author Daniel Bergqvist 2019
 */
public class MathFunctionsTest {

    ExpressionNode expr_boolean_true = new ExpressionNodeTrue();
    ExpressionNode expr_str_HELLO = new ExpressionNodeString(new Token(TokenType.NONE, "hello", 0));
    ExpressionNode expr_str_RAD = new ExpressionNodeString(new Token(TokenType.NONE, "rad", 0));
    ExpressionNode expr_str_DEG = new ExpressionNodeString(new Token(TokenType.NONE, "deg", 0));
    ExpressionNode expr0_34 = new ExpressionNodeFloatingNumber(new Token(TokenType.NONE, "0.34", 0));
    ExpressionNode expr0_95 = new ExpressionNodeFloatingNumber(new Token(TokenType.NONE, "0.95", 0));
    ExpressionNode expr12_34 = new ExpressionNodeFloatingNumber(new Token(TokenType.NONE, "12.34", 0));
    ExpressionNode expr12 = new ExpressionNodeIntegerNumber(new Token(TokenType.NONE, "12", 0));
    ExpressionNode expr23 = new ExpressionNodeIntegerNumber(new Token(TokenType.NONE, "23", 0));
    ExpressionNode exprNeg0_34 = new ExpressionNodeFloatingNumber(new Token(TokenType.NONE, "-0.34", 0));
    ExpressionNode exprNeg0_95 = new ExpressionNodeFloatingNumber(new Token(TokenType.NONE, "-0.95", 0));
    ExpressionNode exprNeg12_34 = new ExpressionNodeFloatingNumber(new Token(TokenType.NONE, "-12.34", 0));

    ExpressionNode exprPi2 = new ExpressionNodeFloatingNumber(new Token(TokenType.NONE, Double.toString(1.0/2*Math.PI), 0));
    ExpressionNode exprPi = new ExpressionNodeFloatingNumber(new Token(TokenType.NONE, Double.toString(Math.PI), 0));
    ExpressionNode expr3Pi2 = new ExpressionNodeFloatingNumber(new Token(TokenType.NONE, Double.toString(3.0/2*Math.PI), 0));
    ExpressionNode expr2Pi = new ExpressionNodeFloatingNumber(new Token(TokenType.NONE, Double.toString(2*Math.PI), 0));

    ExpressionNode expr0 = new ExpressionNodeFloatingNumber(new Token(TokenType.NONE, "0", 0));
    ExpressionNode expr90 = new ExpressionNodeFloatingNumber(new Token(TokenType.NONE, "90", 0));
    ExpressionNode expr180 = new ExpressionNodeFloatingNumber(new Token(TokenType.NONE, "180", 0));
    ExpressionNode expr270 = new ExpressionNodeFloatingNumber(new Token(TokenType.NONE, "270", 0));
    ExpressionNode expr360 = new ExpressionNodeFloatingNumber(new Token(TokenType.NONE, "360", 0));

    private List<ExpressionNode> getParameterList(ExpressionNode... exprNodes) {
        List<ExpressionNode> list = new ArrayList<>();
        Collections.addAll(list, exprNodes);
        return list;
    }

    @Test
    public void testBundle() {
        Assert.assertEquals("strings are equal",
                "Function \"sin\" has wrong number of parameters",
                Bundle.getMessage("WrongNumberOfParameters1", "sin"));
        Assert.assertEquals("strings are equal",
                "Function \"sin\" has wrong number of parameters",
                Bundle.getMessage(Locale.CANADA, "WrongNumberOfParameters1", "sin"));
        // Test Bundle.retry(Locale, String)
        Assert.assertEquals("strings matches","Item",Bundle.getMessage("CategoryItem"));    }

    @Test
    public void testSinFunction() throws Exception {
        Function sinFunction = InstanceManager.getDefault(FunctionManager.class).get("sin");
        Assert.assertEquals("strings matches", "sin", sinFunction.getName());

        SymbolTable symbolTable = new DefaultSymbolTable(new DefaultConditionalNG("IQC1", null));

        // Test sin(x)
        Assert.assertEquals("numbers are equal", (Double)0.3334870921408144, (Double)sinFunction.calculate(symbolTable, getParameterList(expr0_34)), 0.0000001d);
        Assert.assertEquals("numbers are equal", (Double)0.8134155047893737, (Double)sinFunction.calculate(symbolTable, getParameterList(expr0_95)), 0.0000001d);
        Assert.assertEquals("numbers are equal", (Double)(-0.22444221895185537), (Double)sinFunction.calculate(symbolTable, getParameterList(expr12_34)), 0.0000001d);

        // Test sin(x) with a string as parameter
        Assert.assertEquals("numbers are equal", (Double)0.3334870921408144, (Double)sinFunction.calculate(symbolTable, getParameterList(expr0_34)), 0.0000001d);

        AtomicBoolean hasThrown = new AtomicBoolean(false);

        // Test sin(x,"rad"), sin(x,"deg"), sin(x,"hello"), sin(x, true)
        Assert.assertEquals("numbers are equal", (Double)0.3334870921408144, (Double)sinFunction.calculate(symbolTable, getParameterList(expr0_34, expr_str_RAD)), 0.0000001d);
        Assert.assertEquals("numbers are equal", (Double)0.21371244079399437, (Double)sinFunction.calculate(symbolTable, getParameterList(expr12_34, expr_str_DEG)), 0.0000001d);
        hasThrown.set(false);
        try {
            sinFunction.calculate(symbolTable, getParameterList(expr12_34, expr_str_HELLO));
        } catch (CalculateException e) {
            hasThrown.set(true);
        }
        Assert.assertTrue("exception is thrown", hasThrown.get());
        hasThrown.set(false);
        try {
            sinFunction.calculate(symbolTable, getParameterList(expr12_34, expr_boolean_true));
        } catch (CalculateException e) {
            hasThrown.set(true);
        }
        Assert.assertTrue("exception is thrown", hasThrown.get());

        // Test sin(x,"deg", 12, 23)
        Assert.assertEquals("numbers are equal", (Double)18.675418424366967, (Double)sinFunction.calculate(symbolTable, getParameterList(expr12_34, expr_str_DEG, expr12, expr23)), 0.0000001d);

        Assert.assertEquals("numbers are equal", (Double)17.5, (Double)sinFunction.calculate(symbolTable, getParameterList(expr0, expr_str_RAD, expr12, expr23)), 0.0000001d);
        Assert.assertEquals("numbers are equal", (Double)23.0, (Double)sinFunction.calculate(symbolTable, getParameterList(exprPi2, expr_str_RAD, expr12, expr23)), 0.0000001d);
        Assert.assertEquals("numbers are equal", (Double)17.5, (Double)sinFunction.calculate(symbolTable, getParameterList(exprPi, expr_str_RAD, expr12, expr23)), 0.0000001d);
        Assert.assertEquals("numbers are equal", (Double)12.0, (Double)sinFunction.calculate(symbolTable, getParameterList(expr3Pi2, expr_str_RAD, expr12, expr23)), 0.0000001d);
        Assert.assertEquals("numbers are equal", (Double)17.5, (Double)sinFunction.calculate(symbolTable, getParameterList(expr2Pi, expr_str_RAD, expr12, expr23)), 0.0000001d);

        Assert.assertEquals("numbers are equal", (Double)17.5, (Double)sinFunction.calculate(symbolTable, getParameterList(expr0, expr_str_DEG, expr12, expr23)), 0.0000001d);
        Assert.assertEquals("numbers are equal", (Double)23.0, (Double)sinFunction.calculate(symbolTable, getParameterList(expr90, expr_str_DEG, expr12, expr23)), 0.0000001d);
        Assert.assertEquals("numbers are equal", (Double)17.5, (Double)sinFunction.calculate(symbolTable, getParameterList(expr180, expr_str_DEG, expr12, expr23)), 0.0000001d);
        Assert.assertEquals("numbers are equal", (Double)12.0, (Double)sinFunction.calculate(symbolTable, getParameterList(expr270, expr_str_DEG, expr12, expr23)), 0.0000001d);
        Assert.assertEquals("numbers are equal", (Double)17.5, (Double)sinFunction.calculate(symbolTable, getParameterList(expr360, expr_str_DEG, expr12, expr23)), 0.0000001d);

        // Test sin()
        hasThrown.set(false);
        try {
            sinFunction.calculate(symbolTable, getParameterList());
        } catch (WrongNumberOfParametersException e) {
            hasThrown.set(true);
        }
        Assert.assertTrue("exception is thrown", hasThrown.get());
    }

    @Test
    public void testCosFunction() throws Exception {
        Function cosFunction = InstanceManager.getDefault(FunctionManager.class).get("cos");
        Assert.assertEquals("strings matches", "cos", cosFunction.getName());

        SymbolTable symbolTable = new DefaultSymbolTable(new DefaultConditionalNG("IQC1", null));

        // Test cos(x)
        Assert.assertEquals("numbers are equal", (Double)0.9427546655283462, (Double)cosFunction.calculate(symbolTable, getParameterList(expr0_34)), 0.0000001d);
        Assert.assertEquals("numbers are equal", (Double)0.5816830894638836, (Double)cosFunction.calculate(symbolTable, getParameterList(expr0_95)), 0.0000001d);
        Assert.assertEquals("numbers are equal", (Double)(0.9744873987650982), (Double)cosFunction.calculate(symbolTable, getParameterList(expr12_34)), 0.0000001d);

        // Test cos(x) with a string as parameter
        Assert.assertEquals("numbers are equal", (Double)0.9427546655283462, (Double)cosFunction.calculate(symbolTable, getParameterList(expr0_34)), 0.0000001d);

        AtomicBoolean hasThrown = new AtomicBoolean(false);

        // Test cos(x,"rad"), cos(x,"deg"), cos(x,"hello"), cos(x, true)
        Assert.assertEquals("numbers are equal", (Double)0.9427546655283462, (Double)cosFunction.calculate(symbolTable, getParameterList(expr0_34, expr_str_RAD)), 0.0000001d);
        Assert.assertEquals("numbers are equal", (Double)0.976896613081381, (Double)cosFunction.calculate(symbolTable, getParameterList(expr12_34, expr_str_DEG)), 0.0000001d);
        hasThrown.set(false);
        try {
            cosFunction.calculate(symbolTable, getParameterList(expr12_34, expr_str_HELLO));
        } catch (CalculateException e) {
            hasThrown.set(true);
        }
        Assert.assertTrue("exception is thrown", hasThrown.get());
        hasThrown.set(false);
        try {
            cosFunction.calculate(symbolTable, getParameterList(expr12_34, expr_boolean_true));
        } catch (CalculateException e) {
            hasThrown.set(true);
        }
        Assert.assertTrue("exception is thrown", hasThrown.get());

        // Test cos(x,"deg", 12, 23)
        Assert.assertEquals("numbers are equal", (Double)22.872931371947594, (Double)cosFunction.calculate(symbolTable, getParameterList(expr12_34, expr_str_DEG, expr12, expr23)), 0.0000001d);

        Assert.assertEquals("numbers are equal", (Double)23.0, (Double)cosFunction.calculate(symbolTable, getParameterList(expr0, expr_str_RAD, expr12, expr23)), 0.0000001d);
        Assert.assertEquals("numbers are equal", (Double)17.5, (Double)cosFunction.calculate(symbolTable, getParameterList(exprPi2, expr_str_RAD, expr12, expr23)), 0.0000001d);
        Assert.assertEquals("numbers are equal", (Double)12.0, (Double)cosFunction.calculate(symbolTable, getParameterList(exprPi, expr_str_RAD, expr12, expr23)), 0.0000001d);
        Assert.assertEquals("numbers are equal", (Double)17.5, (Double)cosFunction.calculate(symbolTable, getParameterList(expr3Pi2, expr_str_RAD, expr12, expr23)), 0.0000001d);
        Assert.assertEquals("numbers are equal", (Double)23.0, (Double)cosFunction.calculate(symbolTable, getParameterList(expr2Pi, expr_str_RAD, expr12, expr23)), 0.0000001d);

        Assert.assertEquals("numbers are equal", (Double)23.0, (Double)cosFunction.calculate(symbolTable, getParameterList(expr0, expr_str_DEG, expr12, expr23)), 0.0000001d);
        Assert.assertEquals("numbers are equal", (Double)17.5, (Double)cosFunction.calculate(symbolTable, getParameterList(expr90, expr_str_DEG, expr12, expr23)), 0.0000001d);
        Assert.assertEquals("numbers are equal", (Double)12.0, (Double)cosFunction.calculate(symbolTable, getParameterList(expr180, expr_str_DEG, expr12, expr23)), 0.0000001d);
        Assert.assertEquals("numbers are equal", (Double)17.5, (Double)cosFunction.calculate(symbolTable, getParameterList(expr270, expr_str_DEG, expr12, expr23)), 0.0000001d);
        Assert.assertEquals("numbers are equal", (Double)23.0, (Double)cosFunction.calculate(symbolTable, getParameterList(expr360, expr_str_DEG, expr12, expr23)), 0.0000001d);

        // Test cos()
        hasThrown.set(false);
        try {
            cosFunction.calculate(symbolTable, getParameterList());
        } catch (WrongNumberOfParametersException e) {
            hasThrown.set(true);
        }
        Assert.assertTrue("exception is thrown", hasThrown.get());
    }

    @Test
    public void testTanFunction() throws Exception {
        Function tanFunction = InstanceManager.getDefault(FunctionManager.class).get("tan");
        Assert.assertEquals("strings matches", "tan", tanFunction.getName());

        SymbolTable symbolTable = new DefaultSymbolTable(new DefaultConditionalNG("IQC1", null));

        // Test tan(x)
        Assert.assertEquals("numbers are equal", (Double)0.35373687803912257, (Double)tanFunction.calculate(symbolTable, getParameterList(expr0_34)), 0.0000001d);
        Assert.assertEquals("numbers are equal", (Double)1.398382589287699, (Double)tanFunction.calculate(symbolTable, getParameterList(expr0_95)), 0.0000001d);
        Assert.assertEquals("numbers are equal", (Double)(-0.23031823627096235), (Double)tanFunction.calculate(symbolTable, getParameterList(expr12_34)), 0.0000001d);

        // Test tan(x) with a string as parameter
        Assert.assertEquals("numbers are equal", (Double)0.35373687803912257, (Double)tanFunction.calculate(symbolTable, getParameterList(expr0_34)), 0.0000001d);

        AtomicBoolean hasThrown = new AtomicBoolean(false);

        // Test tan(x,"rad"), tan(x,"deg"), tan(x,"hello"), tan(x, true)
        Assert.assertEquals("numbers are equal", (Double)0.35373687803912257, (Double)tanFunction.calculate(symbolTable, getParameterList(expr0_34, expr_str_RAD)), 0.0000001d);
        Assert.assertEquals("numbers are equal", (Double)0.21876669233184345, (Double)tanFunction.calculate(symbolTable, getParameterList(expr12_34, expr_str_DEG)), 0.0000001d);
        hasThrown.set(false);
        try {
            tanFunction.calculate(symbolTable, getParameterList(expr12_34, expr_str_HELLO));
        } catch (CalculateException e) {
            hasThrown.set(true);
        }
        Assert.assertTrue("exception is thrown", hasThrown.get());
        hasThrown.set(false);
        try {
            tanFunction.calculate(symbolTable, getParameterList(expr12_34, expr_boolean_true));
        } catch (CalculateException e) {
            hasThrown.set(true);
        }
        Assert.assertTrue("exception is thrown", hasThrown.get());

        // Test tan()
        hasThrown.set(false);
        try {
            tanFunction.calculate(symbolTable, getParameterList());
        } catch (WrongNumberOfParametersException e) {
            hasThrown.set(true);
        }
        Assert.assertTrue("exception is thrown", hasThrown.get());
    }

    @Test
    public void testArctanFunction() throws Exception {
        Function atanFunction = InstanceManager.getDefault(FunctionManager.class).get("atan");
        Assert.assertEquals("strings matches", "atan", atanFunction.getName());

        SymbolTable symbolTable = new DefaultSymbolTable(new DefaultConditionalNG("IQC1", null));

        // Test atan(x)
        Assert.assertEquals("numbers are equal", (Double)0.3277385067805555, (Double)atanFunction.calculate(symbolTable, getParameterList(expr0_34)), 0.0000001d);
        Assert.assertEquals("numbers are equal", (Double)0.7597627548757708, (Double)atanFunction.calculate(symbolTable, getParameterList(expr0_95)), 0.0000001d);
        Assert.assertEquals("numbers are equal", (Double)1.4899357456343294, (Double)atanFunction.calculate(symbolTable, getParameterList(expr12_34)), 0.0000001d);

        // Test atan(x) with a string as parameter
        Assert.assertEquals("numbers are equal", (Double)0.3277385067805555, (Double)atanFunction.calculate(symbolTable, getParameterList(expr0_34)), 0.0000001d);

        AtomicBoolean hasThrown = new AtomicBoolean(false);

        // Test atan(x,"rad"), atan(x,"deg"), atan(x,"hello"), atan(x, true)
        Assert.assertEquals("numbers are equal", (Double)0.3277385067805555, (Double)atanFunction.calculate(symbolTable, getParameterList(expr0_34, expr_str_RAD)), 0.0000001d);
        Assert.assertEquals("numbers are equal", (Double)85.36702997052444, (Double)atanFunction.calculate(symbolTable, getParameterList(expr12_34, expr_str_DEG)), 0.0000001d);
        hasThrown.set(false);
        try {
            atanFunction.calculate(symbolTable, getParameterList(expr12_34, expr_str_HELLO));
        } catch (CalculateException e) {
            hasThrown.set(true);
        }
        Assert.assertTrue("exception is thrown", hasThrown.get());
        hasThrown.set(false);
        try {
            atanFunction.calculate(symbolTable, getParameterList(expr12_34, expr_boolean_true));
        } catch (CalculateException e) {
            hasThrown.set(true);
        }
        Assert.assertTrue("exception is thrown", hasThrown.get());

        // Test atan()
        hasThrown.set(false);
        try {
            atanFunction.calculate(symbolTable, getParameterList());
        } catch (WrongNumberOfParametersException e) {
            hasThrown.set(true);
        }
        Assert.assertTrue("exception is thrown", hasThrown.get());
    }

    @Test
    public void testAbsFunction() throws Exception {
        Function absFunction = InstanceManager.getDefault(FunctionManager.class).get("abs");
        Assert.assertEquals("strings matches", "abs", absFunction.getName());

        SymbolTable symbolTable = new DefaultSymbolTable(new DefaultConditionalNG("IQC1", null));

        Assert.assertEquals("numbers are equal", (Double)0.34, (Double)absFunction.calculate(symbolTable, getParameterList(expr0_34)), 0.0000001d);
        Assert.assertEquals("numbers are equal", (Double)0.95, (Double)absFunction.calculate(symbolTable, getParameterList(expr0_95)), 0.0000001d);
        Assert.assertEquals("numbers are equal", (Double)(12.34), (Double)absFunction.calculate(symbolTable, getParameterList(expr12_34)), 0.0000001d);

        Assert.assertEquals("numbers are equal", (Double)0.34, (Double)absFunction.calculate(symbolTable, getParameterList(exprNeg0_34)), 0.0000001d);
        Assert.assertEquals("numbers are equal", (Double)0.95, (Double)absFunction.calculate(symbolTable, getParameterList(exprNeg0_95)), 0.0000001d);
        Assert.assertEquals("numbers are equal", (Double)(12.34), (Double)absFunction.calculate(symbolTable, getParameterList(exprNeg12_34)), 0.0000001d);
    }

    @Test
    public void testRandomFunction() throws Exception {
        Function randomFunction = InstanceManager.getDefault(FunctionManager.class).get("random");
        Assert.assertEquals("strings matches", "random", randomFunction.getName());

        SymbolTable symbolTable = new DefaultSymbolTable(new DefaultConditionalNG("IQC1", null));

        // Test random()
        for (int i=0; i < 100; i++) {
            double value = (double) randomFunction.calculate(symbolTable, getParameterList());
            Assert.assertTrue(value >= 0.0);
            Assert.assertTrue(value < 1.0);
        }

        // Test random(max)
        for (int max=1; max < 1000; max += 100) {
            for (int i=0; i < 100; i++) {
                double value = (double) randomFunction.calculate(symbolTable, getParameterList(
                        new ExpressionNodeFloatingNumber(new Token(TokenType.NONE, Integer.toString(max), 0))));
                Assert.assertTrue(value >= 0.0);
                Assert.assertTrue(value < max);
            }
        }

        // Test random(min, max)
        for (int min=1; min < 1000; min += 100) {
            for (int size=1; size < 1000; size += 100) {
                for (int i=0; i < 100; i++) {
                    double value = (double) randomFunction.calculate(symbolTable, getParameterList(
                            new ExpressionNodeFloatingNumber(new Token(TokenType.NONE, Integer.toString(min), 0)),
                            new ExpressionNodeFloatingNumber(new Token(TokenType.NONE, Integer.toString(min+size), 0))));
                    Assert.assertTrue(value >= min);
                    Assert.assertTrue(value < min+size);
                }
            }
        }
    }

    @Test
    public void testSqrFunction() throws Exception {
        Function sqrFunction = InstanceManager.getDefault(FunctionManager.class).get("sqr");
        Assert.assertEquals("strings matches", "sqr", sqrFunction.getName());

        SymbolTable symbolTable = new DefaultSymbolTable(new DefaultConditionalNG("IQC1", null));

        Assert.assertEquals("numbers are equal", 144L, (long)(Long)sqrFunction.calculate(symbolTable, getParameterList(expr12)));
        Assert.assertEquals("numbers are equal", 529, (long)(Long)sqrFunction.calculate(symbolTable, getParameterList(expr23)));

        Assert.assertEquals("numbers are equal", (Double)0.1156, (Double)sqrFunction.calculate(symbolTable, getParameterList(expr0_34)), 0.0000001d);
        Assert.assertEquals("numbers are equal", (Double)0.9025, (Double)sqrFunction.calculate(symbolTable, getParameterList(expr0_95)), 0.0000001d);
        Assert.assertEquals("numbers are equal", (Double)152.2756, (Double)sqrFunction.calculate(symbolTable, getParameterList(expr12_34)), 0.0000001d);

        Assert.assertEquals("numbers are equal", (Double)0.1156, (Double)sqrFunction.calculate(symbolTable, getParameterList(exprNeg0_34)), 0.0000001d);
        Assert.assertEquals("numbers are equal", (Double)0.9025, (Double)sqrFunction.calculate(symbolTable, getParameterList(exprNeg0_95)), 0.0000001d);
        Assert.assertEquals("numbers are equal", (Double)152.2756, (Double)sqrFunction.calculate(symbolTable, getParameterList(exprNeg12_34)), 0.0000001d);
    }

    @Test
    public void testSqrtFunction() throws Exception {
        Function sqrtFunction = InstanceManager.getDefault(FunctionManager.class).get("sqrt");
        Assert.assertEquals("strings matches", "sqrt", sqrtFunction.getName());

        SymbolTable symbolTable = new DefaultSymbolTable(new DefaultConditionalNG("IQC1", null));

        Assert.assertEquals("numbers are equal", (Double)3.4641016151377544, (Double)sqrtFunction.calculate(symbolTable, getParameterList(expr12)), 0.0000001d);
        Assert.assertEquals("numbers are equal", (Double)4.795831523312719, (Double)sqrtFunction.calculate(symbolTable, getParameterList(expr23)), 0.0000001d);

        Assert.assertEquals("numbers are equal", (Double)0.5830951894845301, (Double)sqrtFunction.calculate(symbolTable, getParameterList(expr0_34)), 0.0000001d);
        Assert.assertEquals("numbers are equal", (Double)0.9746794344808963, (Double)sqrtFunction.calculate(symbolTable, getParameterList(expr0_95)), 0.0000001d);
        Assert.assertEquals("numbers are equal", (Double)3.5128336140500593, (Double)sqrtFunction.calculate(symbolTable, getParameterList(expr12_34)), 0.0000001d);

        Assert.assertEquals("numbers are equal", Double.NaN, (Double)sqrtFunction.calculate(symbolTable, getParameterList(exprNeg0_34)), 0.0000001d);
        Assert.assertEquals("numbers are equal", Double.NaN, (Double)sqrtFunction.calculate(symbolTable, getParameterList(exprNeg0_95)), 0.0000001d);
        Assert.assertEquals("numbers are equal", Double.NaN, (Double)sqrtFunction.calculate(symbolTable, getParameterList(exprNeg12_34)), 0.0000001d);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
