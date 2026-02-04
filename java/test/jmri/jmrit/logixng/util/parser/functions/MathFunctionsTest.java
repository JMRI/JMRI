package jmri.jmrit.logixng.util.parser.functions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.jmrit.logixng.SymbolTable;
import jmri.jmrit.logixng.implementation.DefaultConditionalNG;
import jmri.jmrit.logixng.implementation.DefaultSymbolTable;
import jmri.jmrit.logixng.util.LogixNG_Thread;
import jmri.jmrit.logixng.util.parser.*;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test ParsedExpression
 *
 * @author Daniel Bergqvist 2019
 */
public class MathFunctionsTest {

    private final ExpressionNode expr_boolean_true = new ExpressionNodeTrue();
    private final ExpressionNode expr_str_HELLO = new ExpressionNodeString(new Token(TokenType.NONE, "hello", 0));
    private final ExpressionNode expr_str_RAD = new ExpressionNodeString(new Token(TokenType.NONE, "rad", 0));
    private final ExpressionNode expr_str_DEG = new ExpressionNodeString(new Token(TokenType.NONE, "deg", 0));
    private final ExpressionNode expr0_34 = new ExpressionNodeFloatingNumber(new Token(TokenType.NONE, "0.34", 0));
    private final ExpressionNode expr0_95 = new ExpressionNodeFloatingNumber(new Token(TokenType.NONE, "0.95", 0));
    private final ExpressionNode expr12_34 = new ExpressionNodeFloatingNumber(new Token(TokenType.NONE, "12.34", 0));
    private final ExpressionNode expr12_3456789 = new ExpressionNodeFloatingNumber(new Token(TokenType.NONE, "12.3456789", 0));
    private final ExpressionNode expr1234567 = new ExpressionNodeFloatingNumber(new Token(TokenType.NONE, "1234567", 0));
    private final ExpressionNode expr12 = new ExpressionNodeIntegerNumber(new Token(TokenType.NONE, "12", 0));
    private final ExpressionNode expr23 = new ExpressionNodeIntegerNumber(new Token(TokenType.NONE, "23", 0));
    private final ExpressionNode exprNeg0_34 = new ExpressionNodeFloatingNumber(new Token(TokenType.NONE, "-0.34", 0));
    private final ExpressionNode exprNeg0_95 = new ExpressionNodeFloatingNumber(new Token(TokenType.NONE, "-0.95", 0));
    private final ExpressionNode exprNeg12_34 = new ExpressionNodeFloatingNumber(new Token(TokenType.NONE, "-12.34", 0));

    private final ExpressionNode exprPi2 = new ExpressionNodeFloatingNumber(new Token(TokenType.NONE, Double.toString(1.0/2*Math.PI), 0));
    private final ExpressionNode exprPi = new ExpressionNodeFloatingNumber(new Token(TokenType.NONE, Double.toString(Math.PI), 0));
    private final ExpressionNode expr3Pi2 = new ExpressionNodeFloatingNumber(new Token(TokenType.NONE, Double.toString(3.0/2*Math.PI), 0));
    private final ExpressionNode expr2Pi = new ExpressionNodeFloatingNumber(new Token(TokenType.NONE, Double.toString(2*Math.PI), 0));

    private final ExpressionNode expr0 = new ExpressionNodeFloatingNumber(new Token(TokenType.NONE, "0", 0));
    private final ExpressionNode expr90 = new ExpressionNodeFloatingNumber(new Token(TokenType.NONE, "90", 0));
    private final ExpressionNode expr180 = new ExpressionNodeFloatingNumber(new Token(TokenType.NONE, "180", 0));
    private final ExpressionNode expr270 = new ExpressionNodeFloatingNumber(new Token(TokenType.NONE, "270", 0));
    private final ExpressionNode expr360 = new ExpressionNodeFloatingNumber(new Token(TokenType.NONE, "360", 0));

    private final ExpressionNode expr1 = new ExpressionNodeFloatingNumber(new Token(TokenType.NONE, "1", 0));
    private final ExpressionNode expr2 = new ExpressionNodeFloatingNumber(new Token(TokenType.NONE, "2", 0));
    private final ExpressionNode expr3 = new ExpressionNodeFloatingNumber(new Token(TokenType.NONE, "3", 0));

    private final ExpressionNode exprNeg1 = new ExpressionNodeFloatingNumber(new Token(TokenType.NONE, "-1", 0));
    private final ExpressionNode exprNeg2 = new ExpressionNodeFloatingNumber(new Token(TokenType.NONE, "-2", 0));
    private final ExpressionNode exprNeg3 = new ExpressionNodeFloatingNumber(new Token(TokenType.NONE, "-3", 0));


    private ExpressionNode getExprNode(String value) {
        return new ExpressionNodeFloatingNumber(new Token(TokenType.NONE, value, 0));
    }

    private List<ExpressionNode> getParameterList(ExpressionNode... exprNodes) {
        List<ExpressionNode> list = new ArrayList<>();
        Collections.addAll(list, exprNodes);
        return list;
    }

    @Test
    public void testBundle() {
        assertEquals( "Function \"sin\" has wrong number of parameters",
                Bundle.getMessage("WrongNumberOfParameters1", "sin"),
                "strings are equal");
        assertEquals( "Function \"sin\" has wrong number of parameters",
                Bundle.getMessage(Locale.CANADA, "WrongNumberOfParameters1", "sin"),
                "strings are equal 2");
        // Test Bundle.retry(Locale, String)
        assertEquals("Item",Bundle.getMessage("CategoryItem"), "string matches");
    }

    @Test
    public void testSinFunction() throws JmriException {
        Function sinFunction = InstanceManager.getDefault(FunctionManager.class).get("sin");
        assertEquals( "sin", sinFunction.getName(), "strings matches");

        SymbolTable symbolTable = new DefaultSymbolTable(new DefaultConditionalNG("IQC1", null));

        // Test sin(x)
        assertEquals( (Double)0.3334870921408144,
            (Double)sinFunction.calculate(symbolTable, getParameterList(expr0_34)), 0.0000001d,
            "numbers are equal");
        assertEquals( (Double)0.8134155047893737,
            (Double)sinFunction.calculate(symbolTable, getParameterList(expr0_95)), 0.0000001d,
            "numbers are equal 2");
        assertEquals( (Double)(-0.22444221895185537),
            (Double)sinFunction.calculate(symbolTable, getParameterList(expr12_34)), 0.0000001d,
            "numbers are equal 3");

        // Test sin(x) with a string as parameter
        assertEquals( (Double)0.3334870921408144,
            (Double)sinFunction.calculate(symbolTable, getParameterList(expr0_34)), 0.0000001d,
            "numbers are equal 4");

        // Test sin(x,"rad"), sin(x,"deg"), sin(x,"hello"), sin(x, true)
        assertEquals( (Double)0.3334870921408144,
            (Double)sinFunction.calculate(symbolTable, getParameterList(expr0_34, expr_str_RAD)), 0.0000001d,
            "numbers are equal 5");
        assertEquals( (Double)0.21371244079399437,
            (Double)sinFunction.calculate(symbolTable, getParameterList(expr12_34, expr_str_DEG)), 0.0000001d,
            "numbers are equal 6");

        CalculateException e = assertThrows( CalculateException.class,
            () -> sinFunction.calculate(symbolTable, getParameterList(expr12_34, expr_str_HELLO)),
            "exception is thrown");
        assertNotNull(e);

        e = assertThrows( CalculateException.class,
            () -> sinFunction.calculate(symbolTable, getParameterList(expr12_34, expr_boolean_true)),
            "exception is thrown 2");
        assertNotNull(e);

        // Test sin(x,"deg", 12, 23)
        assertEquals( (Double)18.675418424366967,
            (Double)sinFunction.calculate(symbolTable, getParameterList(expr12_34, expr_str_DEG, expr12, expr23)), 0.0000001d,
            "numbers are equal 7");

        assertEquals( (Double)17.5,
            (Double)sinFunction.calculate(symbolTable, getParameterList(expr0, expr_str_RAD, expr12, expr23)), 0.0000001d,
            "numbers are equal 8");
        assertEquals( (Double)23.0,
            (Double)sinFunction.calculate(symbolTable, getParameterList(exprPi2, expr_str_RAD, expr12, expr23)), 0.0000001d,
            "numbers are equal 8");
        assertEquals( (Double)17.5,
            (Double)sinFunction.calculate(symbolTable, getParameterList(exprPi, expr_str_RAD, expr12, expr23)), 0.0000001d,
            "numbers are equal 9");
        assertEquals( (Double)12.0,
            (Double)sinFunction.calculate(symbolTable, getParameterList(expr3Pi2, expr_str_RAD, expr12, expr23)), 0.0000001d,
            "numbers are equal 10");
        assertEquals( (Double)17.5,
            (Double)sinFunction.calculate(symbolTable, getParameterList(expr2Pi, expr_str_RAD, expr12, expr23)), 0.0000001d,
            "numbers are equal 11");

        assertEquals( (Double)17.5,
            (Double)sinFunction.calculate(symbolTable, getParameterList(expr0, expr_str_DEG, expr12, expr23)), 0.0000001d,
            "numbers are equal 12");
        assertEquals( (Double)23.0,
            (Double)sinFunction.calculate(symbolTable, getParameterList(expr90, expr_str_DEG, expr12, expr23)), 0.0000001d,
            "numbers are equal 13");
        assertEquals( (Double)17.5,
            (Double)sinFunction.calculate(symbolTable, getParameterList(expr180, expr_str_DEG, expr12, expr23)), 0.0000001d,
            "numbers are equal 14");
        assertEquals( (Double)12.0,
            (Double)sinFunction.calculate(symbolTable, getParameterList(expr270, expr_str_DEG, expr12, expr23)), 0.0000001d,
            "numbers are equal 15");
        assertEquals( (Double)17.5,
            (Double)sinFunction.calculate(symbolTable, getParameterList(expr360, expr_str_DEG, expr12, expr23)), 0.0000001d,
            "numbers are equal 16");

        // Test sin()
        WrongNumberOfParametersException ex = assertThrows( WrongNumberOfParametersException.class,
            () -> sinFunction.calculate(symbolTable, getParameterList()),
            "exception is thrown 3");
        assertNotNull(ex);
    }

    @Test
    public void testCosFunction() throws JmriException {
        Function cosFunction = InstanceManager.getDefault(FunctionManager.class).get("cos");
        assertEquals( "cos", cosFunction.getName(), "string matches");

        SymbolTable symbolTable = new DefaultSymbolTable(new DefaultConditionalNG("IQC1", null));

        // Test cos(x)
        assertEquals( (Double)0.9427546655283462,
            (Double)cosFunction.calculate(symbolTable, getParameterList(expr0_34)), 0.0000001d,
            "numbers are equal");
        assertEquals( (Double)0.5816830894638836,
            (Double)cosFunction.calculate(symbolTable, getParameterList(expr0_95)), 0.0000001d,
            "numbers are equal 2");
        assertEquals( (Double)(0.9744873987650982),
            (Double)cosFunction.calculate(symbolTable, getParameterList(expr12_34)), 0.0000001d,
            "numbers are equal 3");

        // Test cos(x) with a string as parameter
        assertEquals( (Double)0.9427546655283462,
            (Double)cosFunction.calculate(symbolTable, getParameterList(expr0_34)), 0.0000001d,
            "numbers are equal 4");

        // Test cos(x,"rad"), cos(x,"deg"), cos(x,"hello"), cos(x, true)
        assertEquals( (Double)0.9427546655283462,
            (Double)cosFunction.calculate(symbolTable, getParameterList(expr0_34, expr_str_RAD)), 0.0000001d,
            "numbers are equal 5");
        assertEquals( (Double)0.976896613081381,
            (Double)cosFunction.calculate(symbolTable, getParameterList(expr12_34, expr_str_DEG)), 0.0000001d,
            "numbers are equal 6");

        CalculateException e = assertThrows( CalculateException.class,
            () -> cosFunction.calculate(symbolTable, getParameterList(expr12_34, expr_str_HELLO)),
            "exception is thrown");
        assertNotNull(e);

        e = assertThrows( CalculateException.class,
            () -> cosFunction.calculate(symbolTable, getParameterList(expr12_34, expr_boolean_true)),
            "exception is thrown 2");
        assertNotNull(e);

        // Test cos(x,"deg", 12, 23)
        assertEquals( (Double)22.872931371947594,
            (Double)cosFunction.calculate(symbolTable, getParameterList(expr12_34, expr_str_DEG, expr12, expr23)), 0.0000001d,
            "numbers are equal 7");

        assertEquals( (Double)23.0,
            (Double)cosFunction.calculate(symbolTable, getParameterList(expr0, expr_str_RAD, expr12, expr23)), 0.0000001d,
            "numbers are equal 8");
        assertEquals( (Double)17.5,
            (Double)cosFunction.calculate(symbolTable, getParameterList(exprPi2, expr_str_RAD, expr12, expr23)), 0.0000001d,
            "numbers are equal 9");
        assertEquals( (Double)12.0,
            (Double)cosFunction.calculate(symbolTable, getParameterList(exprPi, expr_str_RAD, expr12, expr23)), 0.0000001d,
            "numbers are equal 10");
        assertEquals( (Double)17.5,
            (Double)cosFunction.calculate(symbolTable, getParameterList(expr3Pi2, expr_str_RAD, expr12, expr23)), 0.0000001d,
            "numbers are equal 11");
        assertEquals( (Double)23.0,
            (Double)cosFunction.calculate(symbolTable, getParameterList(expr2Pi, expr_str_RAD, expr12, expr23)), 0.0000001d,
            "numbers are equal 12");

        assertEquals( (Double)23.0,
            (Double)cosFunction.calculate(symbolTable, getParameterList(expr0, expr_str_DEG, expr12, expr23)), 0.0000001d,
            "numbers are equal 13");
        assertEquals( (Double)17.5,
            (Double)cosFunction.calculate(symbolTable, getParameterList(expr90, expr_str_DEG, expr12, expr23)), 0.0000001d,
            "numbers are equal 14");
        assertEquals( (Double)12.0,
            (Double)cosFunction.calculate(symbolTable, getParameterList(expr180, expr_str_DEG, expr12, expr23)), 0.0000001d,
            "numbers are equal 15");
        assertEquals( (Double)17.5,
            (Double)cosFunction.calculate(symbolTable, getParameterList(expr270, expr_str_DEG, expr12, expr23)), 0.0000001d,
            "numbers are equal 16");
        assertEquals( (Double)23.0,
            (Double)cosFunction.calculate(symbolTable, getParameterList(expr360, expr_str_DEG, expr12, expr23)), 0.0000001d,
            "numbers are equal 17");

        // Test cos()
        WrongNumberOfParametersException ex = assertThrows( WrongNumberOfParametersException.class,
            () -> cosFunction.calculate(symbolTable, getParameterList()),
            "exception is thrown");
        assertNotNull(ex);

    }

    @Test
    public void testTanFunction() throws JmriException {
        Function tanFunction = InstanceManager.getDefault(FunctionManager.class).get("tan");
        assertEquals( "tan", tanFunction.getName(), "strings matches");

        SymbolTable symbolTable = new DefaultSymbolTable(new DefaultConditionalNG("IQC1", null));

        // Test tan(x)
        assertEquals( (Double)0.35373687803912257,
            (Double)tanFunction.calculate(symbolTable, getParameterList(expr0_34)), 0.0000001d,
            "numbers are equal");
        assertEquals( (Double)1.398382589287699,
            (Double)tanFunction.calculate(symbolTable, getParameterList(expr0_95)), 0.0000001d,
            "numbers are equal 2");
        assertEquals( (Double)(-0.23031823627096235),
            (Double)tanFunction.calculate(symbolTable, getParameterList(expr12_34)), 0.0000001d,
            "numbers are equal 3");

        // Test tan(x) with a string as parameter
        assertEquals( (Double)0.35373687803912257,
            (Double)tanFunction.calculate(symbolTable, getParameterList(expr0_34)), 0.0000001d,
            "numbers are equal 4");


        // Test tan(x,"rad"), tan(x,"deg"), tan(x,"hello"), tan(x, true)
        assertEquals( (Double)0.35373687803912257,
            (Double)tanFunction.calculate(symbolTable, getParameterList(expr0_34, expr_str_RAD)), 0.0000001d,
            "numbers are equal 5");
        assertEquals( (Double)0.21876669233184345,
            (Double)tanFunction.calculate(symbolTable, getParameterList(expr12_34, expr_str_DEG)), 0.0000001d,
            "numbers are equal 6");

        CalculateException e = assertThrows( CalculateException.class,
            () -> tanFunction.calculate(symbolTable, getParameterList(expr12_34, expr_str_HELLO)),
            "exception is thrown");
        assertNotNull(e);

        e = assertThrows( CalculateException.class,
            () -> tanFunction.calculate(symbolTable, getParameterList(expr12_34, expr_boolean_true)),
            "exception is thrown");
        assertNotNull(e);

        // Test tan()
        WrongNumberOfParametersException ex = assertThrows( WrongNumberOfParametersException.class,
            () -> tanFunction.calculate(symbolTable, getParameterList()),
            "exception is thrown");
        assertNotNull(ex);
    }

    @Test
    public void testArctanFunction() throws JmriException {
        Function atanFunction = InstanceManager.getDefault(FunctionManager.class).get("atan");
        assertEquals( "atan", atanFunction.getName(), "string matches");

        SymbolTable symbolTable = new DefaultSymbolTable(new DefaultConditionalNG("IQC1", null));

        // Test atan(x)
        assertEquals( (Double)0.3277385067805555,
            (Double)atanFunction.calculate(symbolTable, getParameterList(expr0_34)), 0.0000001d,
            "numbers are equal");
        assertEquals( (Double)0.7597627548757708,
            (Double)atanFunction.calculate(symbolTable, getParameterList(expr0_95)), 0.0000001d,
            "numbers are equal 2");
        assertEquals( (Double)1.4899357456343294,
            (Double)atanFunction.calculate(symbolTable, getParameterList(expr12_34)), 0.0000001d,
            "numbers are equal 3");

        // Test atan(x) with a string as parameter
        assertEquals( (Double)0.3277385067805555,
            (Double)atanFunction.calculate(symbolTable, getParameterList(expr0_34)), 0.0000001d,
            "numbers are equal 4");

        // Test atan(x,"rad"), atan(x,"deg"), atan(x,"hello"), atan(x, true)
        assertEquals( (Double)0.3277385067805555,
            (Double)atanFunction.calculate(symbolTable, getParameterList(expr0_34, expr_str_RAD)), 0.0000001d,
            "numbers are equal 5");
        assertEquals( (Double)85.36702997052444, (Double)atanFunction.calculate(symbolTable, getParameterList(expr12_34, expr_str_DEG)), 0.0000001d,
                "numbers are equal 6");
        CalculateException e = assertThrows( CalculateException.class,
            () -> atanFunction.calculate(symbolTable, getParameterList(expr12_34, expr_str_HELLO)),
            "exception is thrown");
        assertNotNull(e);

        e = assertThrows( CalculateException.class,
            () -> atanFunction.calculate(symbolTable, getParameterList(expr12_34, expr_boolean_true)),
            "exception is thrown");
        assertNotNull(e);

        // Test atan()
        WrongNumberOfParametersException ex = assertThrows( WrongNumberOfParametersException.class,
            () -> atanFunction.calculate(symbolTable, getParameterList()),
            "exception is thrown");
        assertNotNull(ex);
    }

    @Test
    public void testAbsFunction() throws JmriException {
        Function absFunction = InstanceManager.getDefault(FunctionManager.class).get("abs");
        assertEquals( "abs", absFunction.getName(), "string matches");

        SymbolTable symbolTable = new DefaultSymbolTable(new DefaultConditionalNG("IQC1", null));

        assertEquals( (Double)0.34,
            (Double)absFunction.calculate(symbolTable, getParameterList(expr0_34)), 0.0000001d,
            "numbers are equal");
        assertEquals( (Double)0.95,
            (Double)absFunction.calculate(symbolTable, getParameterList(expr0_95)), 0.0000001d,
            "numbers are equal 2");
        assertEquals( (Double)(12.34),
            (Double)absFunction.calculate(symbolTable, getParameterList(expr12_34)), 0.0000001d,
            "numbers are equal 3");

        assertEquals( (Double)0.34,
            (Double)absFunction.calculate(symbolTable, getParameterList(exprNeg0_34)), 0.0000001d,
            "numbers are equal 4");
        assertEquals( (Double)0.95,
            (Double)absFunction.calculate(symbolTable, getParameterList(exprNeg0_95)), 0.0000001d,
            "numbers are equal 5");
        assertEquals( (Double)(12.34),
            (Double)absFunction.calculate(symbolTable, getParameterList(exprNeg12_34)), 0.0000001d,
            "numbers are equal 6");
    }

    @Test
    public void testRandomFunction() throws JmriException {
        Function randomFunction = InstanceManager.getDefault(FunctionManager.class).get("random");
        assertEquals( "random", randomFunction.getName(), "string matches");

        SymbolTable symbolTable = new DefaultSymbolTable(new DefaultConditionalNG("IQC1", null));

        // Test random()
        for (int i=0; i < 100; i++) {
            double value = (double) randomFunction.calculate(symbolTable, getParameterList());
            assertTrue(value >= 0.0);
            assertTrue(value < 1.0);
        }

        // Test random(max)
        for (int max=1; max < 1000; max += 100) {
            for (int i=0; i < 100; i++) {
                double value = (double) randomFunction.calculate(symbolTable, getParameterList(
                        new ExpressionNodeFloatingNumber(new Token(TokenType.NONE, Integer.toString(max), 0))));
                assertTrue(value >= 0.0);
                assertTrue(value < max);
            }
        }

        // Test random(min, max)
        for (int min=1; min < 1000; min += 100) {
            for (int size=1; size < 1000; size += 100) {
                for (int i=0; i < 100; i++) {
                    double value = (double) randomFunction.calculate(symbolTable, getParameterList(
                            new ExpressionNodeFloatingNumber(new Token(TokenType.NONE, Integer.toString(min), 0)),
                            new ExpressionNodeFloatingNumber(new Token(TokenType.NONE, Integer.toString(min+size), 0))));
                    assertTrue(value >= min);
                    assertTrue(value < min+size);
                }
            }
        }
    }

    @Test
    public void testSqrFunction() throws JmriException {
        Function sqrFunction = InstanceManager.getDefault(FunctionManager.class).get("sqr");
        assertEquals( "sqr", sqrFunction.getName(), "string matches");

        SymbolTable symbolTable = new DefaultSymbolTable(new DefaultConditionalNG("IQC1", null));

        assertEquals( 144L,
            (long)(Long)sqrFunction.calculate(symbolTable, getParameterList(expr12)),
            "numbers are equal");
        assertEquals( 529, (long)(Long)sqrFunction.calculate(symbolTable, getParameterList(expr23)),
            "numbers are equal 2");

        assertEquals( (Double)0.1156, (Double)sqrFunction.calculate(symbolTable, getParameterList(expr0_34)), 0.0000001d,
            "numbers are equal 3");
        assertEquals( (Double)0.9025, (Double)sqrFunction.calculate(symbolTable, getParameterList(expr0_95)), 0.0000001d,
            "numbers are equal 4");
        assertEquals( (Double)152.2756, (Double)sqrFunction.calculate(symbolTable, getParameterList(expr12_34)), 0.0000001d,
            "numbers are equal 5");

        assertEquals( (Double)0.1156, (Double)sqrFunction.calculate(symbolTable, getParameterList(exprNeg0_34)), 0.0000001d,
            "numbers are equal 6");
        assertEquals( (Double)0.9025, (Double)sqrFunction.calculate(symbolTable, getParameterList(exprNeg0_95)), 0.0000001d,
            "numbers are equal 7");
        assertEquals( (Double)152.2756, (Double)sqrFunction.calculate(symbolTable, getParameterList(exprNeg12_34)), 0.0000001d,
            "numbers are equal 8");
    }

    @Test
    public void testSqrtFunction() throws JmriException {
        Function sqrtFunction = InstanceManager.getDefault(FunctionManager.class).get("sqrt");
        assertEquals( "sqrt", sqrtFunction.getName(), "string matches");

        SymbolTable symbolTable = new DefaultSymbolTable(new DefaultConditionalNG("IQC1", null));

        assertEquals( (Double)3.4641016151377544,
            (Double)sqrtFunction.calculate(symbolTable, getParameterList(expr12)), 0.0000001d,
            "numbers are equal");
        assertEquals( (Double)4.795831523312719,
            (Double)sqrtFunction.calculate(symbolTable, getParameterList(expr23)), 0.0000001d,
            "numbers are equal 2");

        assertEquals( (Double)0.5830951894845301,
            (Double)sqrtFunction.calculate(symbolTable, getParameterList(expr0_34)), 0.0000001d,
            "numbers are equal 3");
        assertEquals( (Double)0.9746794344808963,
            (Double)sqrtFunction.calculate(symbolTable, getParameterList(expr0_95)), 0.0000001d,
            "numbers are equal 4");
        assertEquals( (Double)3.5128336140500593,
            (Double)sqrtFunction.calculate(symbolTable, getParameterList(expr12_34)), 0.0000001d,
            "numbers are equal 5");

        assertEquals( Double.NaN,
            (Double)sqrtFunction.calculate(symbolTable, getParameterList(exprNeg0_34)), 0.0000001d,
            "numbers are equal 6");
        assertEquals( Double.NaN,
            (Double)sqrtFunction.calculate(symbolTable, getParameterList(exprNeg0_95)), 0.0000001d,
            "numbers are equal 7");
        assertEquals( Double.NaN,
            (Double)sqrtFunction.calculate(symbolTable, getParameterList(exprNeg12_34)), 0.0000001d,
            "numbers are equal 8");
    }

    @Test
    public void testRoundFunction() throws JmriException {
        Function roundFunction = InstanceManager.getDefault(FunctionManager.class).get("round");
        assertEquals( "round", roundFunction.getName(), "string matches");

        SymbolTable symbolTable = new DefaultSymbolTable(new DefaultConditionalNG("IQC1", null));

        // Test round(x)
        assertEquals( (Double)0.0,
            (Double)roundFunction.calculate(symbolTable, getParameterList(expr0_34)), 0.0000001d,
            "numbers are equal");
        assertEquals( (Double)1.0,
            (Double)roundFunction.calculate(symbolTable, getParameterList(expr0_95)), 0.0000001d,
            "numbers are equal 2");
        assertEquals( (Double)12.0,
            (Double)roundFunction.calculate(symbolTable, getParameterList(expr12_3456789)), 0.0000001d,
            "numbers are equal 3");
        assertEquals( (Double)1.0,
            (Double)roundFunction.calculate(symbolTable, getParameterList(getExprNode("1.4"))), 0.0000001d,
            "numbers are equal 4");
        assertEquals( (Double)2.0,
            (Double)roundFunction.calculate(symbolTable, getParameterList(getExprNode("1.5"))), 0.0000001d,
            "numbers are equal 5");
        assertEquals( (Double)2.0,
            (Double)roundFunction.calculate(symbolTable, getParameterList(getExprNode("1.6"))), 0.0000001d,
            "numbers are equal 6");

        // Test round(x) with number of decimals
        assertEquals( (Double)12.0,
                (Double)roundFunction.calculate(symbolTable, getParameterList(expr12_3456789, expr0)), 0.0000001d,
            "numbers are equal 6a");
        assertEquals( (Double)12.3,
                (Double)roundFunction.calculate(symbolTable, getParameterList(expr12_3456789, expr1)), 0.0000001d,
            "numbers are equal 6b");
        assertEquals( (Double)12.35,
                (Double)roundFunction.calculate(symbolTable, getParameterList(expr12_3456789, expr2)), 0.0000001d,
            "numbers are equal 6c");
        assertEquals( (Double)12.346,
                (Double)roundFunction.calculate(symbolTable, getParameterList(expr12_3456789, expr3)), 0.0000001d,
            "numbers are equal 6d");
        assertEquals( (Double)10.0,
                (Double)roundFunction.calculate(symbolTable, getParameterList(expr12_3456789, exprNeg1)), 0.0000001d,
            "numbers are equal 6e");
        assertEquals( (Double)0.0,
                (Double)roundFunction.calculate(symbolTable, getParameterList(expr12_3456789, exprNeg2)), 0.0000001d,
            "numbers are equal 6f");
        assertEquals( (Double)0.0,
                (Double)roundFunction.calculate(symbolTable, getParameterList(expr12_3456789, exprNeg3)), 0.0000001d,
            "numbers are equal 6g");
        assertEquals( (Double)1234567.0,
                (Double)roundFunction.calculate(symbolTable, getParameterList(expr1234567)), 0.0000001d,
            "numbers are equal 6h");
        assertEquals( (Double)1234567.0,
                (Double)roundFunction.calculate(symbolTable, getParameterList(expr1234567, expr0)), 0.0000001d,
            "numbers are equal 6i");
        assertEquals( (Double)1234570.0,
                (Double)roundFunction.calculate(symbolTable, getParameterList(expr1234567, exprNeg1)), 0.0000001d,
            "numbers are equal 7");
        assertEquals( (Double)1234600.0,
                (Double)roundFunction.calculate(symbolTable, getParameterList(expr1234567, exprNeg2)), 0.0000001d,
            "numbers are equal 8");
        assertEquals( (Double)1235000.0,
                (Double)roundFunction.calculate(symbolTable, getParameterList(expr1234567, exprNeg3)), 0.0000001d,
            "numbers are equal 9");

        assertEquals( (Double)1.0,
            (Double)roundFunction.calculate(symbolTable, getParameterList(getExprNode("1.4"), getExprNode("0"))), 0.0000001d,
            "numbers are equal 10");
        assertEquals( (Double)2.0,
            (Double)roundFunction.calculate(symbolTable, getParameterList(getExprNode("1.5"), getExprNode("0"))), 0.0000001d,
            "numbers are equal 11");
        assertEquals( (Double)2.0,
            (Double)roundFunction.calculate(symbolTable, getParameterList(getExprNode("1.6"), getExprNode("0"))), 0.0000001d,
            "numbers are equal 12");
        assertEquals( (Double)1.00,
            (Double)roundFunction.calculate(symbolTable, getParameterList(getExprNode("1.004"), getExprNode("2"))), 0.0000001d,
            "numbers are equal 13");
        assertEquals( (Double)1.01,
            (Double)roundFunction.calculate(symbolTable, getParameterList(getExprNode("1.00500001"), getExprNode("2"))), 0.0000001d,
            "numbers are equal 14");
        assertEquals( (Double)1.01,
            (Double)roundFunction.calculate(symbolTable, getParameterList(getExprNode("1.006"), getExprNode("2"))), 0.0000001d,
            "numbers are equal 15");
        assertEquals( (Double)100.0,
            (Double)roundFunction.calculate(symbolTable, getParameterList(getExprNode("140"), getExprNode("-2"))), 0.0000001d,
            "numbers are equal 16");
        assertEquals( (Double)200.0,
            (Double)roundFunction.calculate(symbolTable, getParameterList(getExprNode("150"), getExprNode("-2"))), 0.0000001d,
            "numbers are equal 17");
        assertEquals( (Double)200.0,
            (Double)roundFunction.calculate(symbolTable, getParameterList(getExprNode("160"), getExprNode("-2"))), 0.0000001d,
            "numbers are equal 18");
    }

    @Test
    public void testCeilFunction() throws JmriException {
        Function ceilFunction = InstanceManager.getDefault(FunctionManager.class).get("ceil");
        assertEquals( "ceil", ceilFunction.getName(), "string matches");

        SymbolTable symbolTable = new DefaultSymbolTable(new DefaultConditionalNG("IQC1", null));

        // Test ceil(x)
        assertEquals( (Double)1.0, (Double)ceilFunction.calculate(symbolTable, getParameterList(expr0_34)), 0.0000001d, "numbers are equal");
        assertEquals( (Double)1.0, (Double)ceilFunction.calculate(symbolTable, getParameterList(expr0_95)), 0.0000001d, "numbers are equal");
        assertEquals( (Double)13.0, (Double)ceilFunction.calculate(symbolTable, getParameterList(expr12_3456789)), 0.0000001d, "numbers are equal");
        assertEquals( (Double)2.0, (Double)ceilFunction.calculate(symbolTable, getParameterList(getExprNode("1.4"))), 0.0000001d, "numbers are equal");
        assertEquals( (Double)2.0, (Double)ceilFunction.calculate(symbolTable, getParameterList(getExprNode("1.5"))), 0.0000001d, "numbers are equal");
        assertEquals( (Double)2.0, (Double)ceilFunction.calculate(symbolTable, getParameterList(getExprNode("1.6"))), 0.0000001d, "numbers are equal");

        // Test ceil(x) with number of decimals
        assertEquals( (Double)13.0, (Double)ceilFunction.calculate(symbolTable, getParameterList(expr12_3456789, expr0)), 0.0000001d, "numbers are equal");
        assertEquals( (Double)12.4, (Double)ceilFunction.calculate(symbolTable, getParameterList(expr12_3456789, expr1)), 0.0000001d, "numbers are equal");
        assertEquals( (Double)12.35, (Double)ceilFunction.calculate(symbolTable, getParameterList(expr12_3456789, expr2)), 0.0000001d, "numbers are equal");
        assertEquals( (Double)12.346, (Double)ceilFunction.calculate(symbolTable, getParameterList(expr12_3456789, expr3)), 0.0000001d, "numbers are equal");
        assertEquals( (Double)20.0, (Double)ceilFunction.calculate(symbolTable, getParameterList(expr12_3456789, exprNeg1)), 0.0000001d, "numbers are equal");
        assertEquals( (Double)100.0, (Double)ceilFunction.calculate(symbolTable, getParameterList(expr12_3456789, exprNeg2)), 0.0000001d, "numbers are equal");
        assertEquals( (Double)1000.0, (Double)ceilFunction.calculate(symbolTable, getParameterList(expr12_3456789, exprNeg3)), 0.0000001d, "numbers are equal");
        assertEquals( (Double)1234567.0, (Double)ceilFunction.calculate(symbolTable, getParameterList(expr1234567)), 0.0000001d, "numbers are equal");
        assertEquals( (Double)1234567.0, (Double)ceilFunction.calculate(symbolTable, getParameterList(expr1234567, expr0)), 0.0000001d, "numbers are equal");
        assertEquals( (Double)1234570.0, (Double)ceilFunction.calculate(symbolTable, getParameterList(expr1234567, exprNeg1)), 0.0000001d, "numbers are equal");
        assertEquals( (Double)1234600.0, (Double)ceilFunction.calculate(symbolTable, getParameterList(expr1234567, exprNeg2)), 0.0000001d, "numbers are equal");
        assertEquals( (Double)1235000.0, (Double)ceilFunction.calculate(symbolTable, getParameterList(expr1234567, exprNeg3)), 0.0000001d, "numbers are equal");

        assertEquals( (Double)2.0, (Double)ceilFunction.calculate(symbolTable, getParameterList(getExprNode("1.4"), getExprNode("0"))), 0.0000001d, "numbers are equal");
        assertEquals( (Double)2.0, (Double)ceilFunction.calculate(symbolTable, getParameterList(getExprNode("1.5"), getExprNode("0"))), 0.0000001d, "numbers are equal");
        assertEquals( (Double)2.0, (Double)ceilFunction.calculate(symbolTable, getParameterList(getExprNode("1.6"), getExprNode("0"))), 0.0000001d, "numbers are equal");
        assertEquals( (Double)1.01, (Double)ceilFunction.calculate(symbolTable, getParameterList(getExprNode("1.004"), getExprNode("2"))), 0.0000001d, "numbers are equal");
        assertEquals( (Double)1.01, (Double)ceilFunction.calculate(symbolTable, getParameterList(getExprNode("1.00500001"), getExprNode("2"))), 0.0000001d, "numbers are equal");
        assertEquals( (Double)1.01, (Double)ceilFunction.calculate(symbolTable, getParameterList(getExprNode("1.006"), getExprNode("2"))), 0.0000001d, "numbers are equal");
        assertEquals( (Double)200.0, (Double)ceilFunction.calculate(symbolTable, getParameterList(getExprNode("140"), getExprNode("-2"))), 0.0000001d, "numbers are equal");
        assertEquals( (Double)200.0, (Double)ceilFunction.calculate(symbolTable, getParameterList(getExprNode("150"), getExprNode("-2"))), 0.0000001d, "numbers are equal");
        assertEquals( (Double)200.0, (Double)ceilFunction.calculate(symbolTable, getParameterList(getExprNode("160"), getExprNode("-2"))), 0.0000001d, "numbers are equal");
    }

    @Test
    public void testFloorFunction() throws JmriException {
        Function floorFunction = InstanceManager.getDefault(FunctionManager.class).get("floor");
        assertEquals( "floor", floorFunction.getName(), "string matches");

        SymbolTable symbolTable = new DefaultSymbolTable(new DefaultConditionalNG("IQC1", null));

        // Test floor(x)
        assertEquals( (Double)0.0, (Double)floorFunction.calculate(symbolTable, getParameterList(expr0_34)), 0.0000001d, "numbers are equal");
        assertEquals( (Double)0.0, (Double)floorFunction.calculate(symbolTable, getParameterList(expr0_95)), 0.0000001d, "numbers are equal");
        assertEquals( (Double)12.0, (Double)floorFunction.calculate(symbolTable, getParameterList(expr12_3456789)), 0.0000001d, "numbers are equal");
        assertEquals( (Double)1.0, (Double)floorFunction.calculate(symbolTable, getParameterList(getExprNode("1.4"))), 0.0000001d, "numbers are equal");
        assertEquals( (Double)1.0, (Double)floorFunction.calculate(symbolTable, getParameterList(getExprNode("1.5"))), 0.0000001d, "numbers are equal");
        assertEquals( (Double)1.0, (Double)floorFunction.calculate(symbolTable, getParameterList(getExprNode("1.6"))), 0.0000001d, "numbers are equal");

        // Test floor(x) with number of decimals
        assertEquals( (Double)12.0, (Double)floorFunction.calculate(symbolTable, getParameterList(expr12_3456789, expr0)), 0.0000001d, "numbers are equal");
        assertEquals( (Double)12.3, (Double)floorFunction.calculate(symbolTable, getParameterList(expr12_3456789, expr1)), 0.0000001d, "numbers are equal");
        assertEquals( (Double)12.34, (Double)floorFunction.calculate(symbolTable, getParameterList(expr12_3456789, expr2)), 0.0000001d, "numbers are equal");
        assertEquals( (Double)12.345, (Double)floorFunction.calculate(symbolTable, getParameterList(expr12_3456789, expr3)), 0.0000001d, "numbers are equal");
        assertEquals( (Double)10.0, (Double)floorFunction.calculate(symbolTable, getParameterList(expr12_3456789, exprNeg1)), 0.0000001d, "numbers are equal");
        assertEquals( (Double)0.0, (Double)floorFunction.calculate(symbolTable, getParameterList(expr12_3456789, exprNeg2)), 0.0000001d, "numbers are equal");
        assertEquals( (Double)0.0, (Double)floorFunction.calculate(symbolTable, getParameterList(expr12_3456789, exprNeg3)), 0.0000001d, "numbers are equal");
        assertEquals( (Double)1234567.0, (Double)floorFunction.calculate(symbolTable, getParameterList(expr1234567)), 0.0000001d, "numbers are equal");
        assertEquals( (Double)1234567.0, (Double)floorFunction.calculate(symbolTable, getParameterList(expr1234567, expr0)), 0.0000001d, "numbers are equal");
        assertEquals( (Double)1234560.0, (Double)floorFunction.calculate(symbolTable, getParameterList(expr1234567, exprNeg1)), 0.0000001d, "numbers are equal");
        assertEquals( (Double)1234500.0, (Double)floorFunction.calculate(symbolTable, getParameterList(expr1234567, exprNeg2)), 0.0000001d, "numbers are equal");
        assertEquals( (Double)1234000.0, (Double)floorFunction.calculate(symbolTable, getParameterList(expr1234567, exprNeg3)), 0.0000001d, "numbers are equal");

        assertEquals( (Double)1.0, (Double)floorFunction.calculate(symbolTable, getParameterList(getExprNode("1.4"), getExprNode("0"))), 0.0000001d, "numbers are equal");
        assertEquals( (Double)1.0, (Double)floorFunction.calculate(symbolTable, getParameterList(getExprNode("1.5"), getExprNode("0"))), 0.0000001d, "numbers are equal");
        assertEquals( (Double)1.0, (Double)floorFunction.calculate(symbolTable, getParameterList(getExprNode("1.6"), getExprNode("0"))), 0.0000001d, "numbers are equal");
        assertEquals( (Double)1.00, (Double)floorFunction.calculate(symbolTable, getParameterList(getExprNode("1.004"), getExprNode("2"))), 0.0000001d, "numbers are equal");
        assertEquals( (Double)1.00, (Double)floorFunction.calculate(symbolTable, getParameterList(getExprNode("1.00500001"), getExprNode("2"))), 0.0000001d, "numbers are equal");
        assertEquals( (Double)1.00, (Double)floorFunction.calculate(symbolTable, getParameterList(getExprNode("1.006"), getExprNode("2"))), 0.0000001d, "numbers are equal");
        assertEquals( (Double)100.0, (Double)floorFunction.calculate(symbolTable, getParameterList(getExprNode("140"), getExprNode("-2"))), 0.0000001d, "numbers are equal");
        assertEquals( (Double)100.0, (Double)floorFunction.calculate(symbolTable, getParameterList(getExprNode("150"), getExprNode("-2"))), 0.0000001d, "numbers are equal");
        assertEquals( (Double)100.0, (Double)floorFunction.calculate(symbolTable, getParameterList(getExprNode("160"), getExprNode("-2"))), 0.0000001d, "numbers are equal");
    }

    // The minimal setup for log4J
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
