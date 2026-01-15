package jmri.jmrit.logixng.util.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import jmri.JmriException;
import jmri.jmrit.logixng.SymbolTable;
import jmri.jmrit.logixng.implementation.DefaultConditionalNG;
import jmri.jmrit.logixng.implementation.DefaultSymbolTable;
import jmri.jmrit.logixng.util.LogixNG_Thread;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test ExpressionNodeArithmeticOperator
 * 
 * @author Daniel Bergqvist 2019
 */
public class ExpressionNodeArithmeticOperatorTest {

    @Test
    public void testCtor() throws ParserException {

        ExpressionNode exprTrue = new ExpressionNodeTrue();

        Token token = new Token(TokenType.NONE, "1", 0);
        ExpressionNodeFloatingNumber expressionNumber = new ExpressionNodeFloatingNumber(token);
        ExpressionNodeArithmeticOperator t = new ExpressionNodeArithmeticOperator(TokenType.ADD, null, expressionNumber);
        assertNotNull( t, "exists");


        // Test right side is null
        IllegalArgumentException e = assertThrows( IllegalArgumentException.class, () -> {
            var enao = new ExpressionNodeArithmeticOperator(TokenType.ADD, null, null);
            fail("ahould have thrown, created " + enao);
        }, "exception is thrown");
        assertNotNull(e);

        // Test invalid token
        e = assertThrows( IllegalArgumentException.class, () -> {
            var enao = new ExpressionNodeArithmeticOperator(TokenType.BINARY_AND, null, null);
            fail("ahould have thrown, created " + enao);
        }, "exception is thrown");
        assertNotNull(e);

        // MULTIPLY requires two operands
        e = assertThrows( IllegalArgumentException.class, () -> {
            var enao = new ExpressionNodeArithmeticOperator(TokenType.MULTIPLY, null, exprTrue);
            fail("ahould have thrown, created " + enao);
        }, "exception is thrown");
        assertNotNull(e);

        // DIVIDE requires two operands
        e = assertThrows( IllegalArgumentException.class, () -> {
            var enao = new ExpressionNodeArithmeticOperator(TokenType.DIVIDE, null, exprTrue);
            fail("ahould have thrown, created " + enao);
        }, "exception is thrown");
        assertNotNull(e);
    }

    @Test
    public void testCalculate() throws JmriException {

        ExpressionNode expr12_34 = new ExpressionNodeFloatingNumber(new Token(TokenType.NONE, "12.34", 0));
        ExpressionNode expr25_46 = new ExpressionNodeFloatingNumber(new Token(TokenType.NONE, "25.46", 0));
        ExpressionNode expr12 = new ExpressionNodeIntegerNumber(new Token(TokenType.NONE, "12", 0));
        ExpressionNode expr235 = new ExpressionNodeIntegerNumber(new Token(TokenType.NONE, "235", 0));

        SymbolTable symbolTable = new DefaultSymbolTable(new DefaultConditionalNG("IQC1", null));

        assertEquals( 37.8,
                (double)new ExpressionNodeArithmeticOperator(TokenType.ADD, expr12_34, expr25_46).calculate(symbolTable),
                0.00000001, "calculate() gives the correct value");
        assertEquals( 247,
                (long)new ExpressionNodeArithmeticOperator(TokenType.ADD, expr12, expr235).calculate(symbolTable),
                "calculate() gives the correct value");
        assertEquals( 24.34,
                (double)new ExpressionNodeArithmeticOperator(TokenType.ADD, expr12, expr12_34).calculate(symbolTable),
                0.00000001, "calculate() gives the correct value");

        assertEquals( -13.12,
                (double)new ExpressionNodeArithmeticOperator(TokenType.SUBTRACKT, expr12_34, expr25_46).calculate(symbolTable),
                0.00000001, "calculate() gives the correct value");
        assertEquals( -223,
                (long)new ExpressionNodeArithmeticOperator(TokenType.SUBTRACKT, expr12, expr235).calculate(symbolTable),
                "calculate() gives the correct value");
        assertEquals( -0.34,
                (double)new ExpressionNodeArithmeticOperator(TokenType.SUBTRACKT, expr12, expr12_34).calculate(symbolTable),
                0.00000001, "calculate() gives the correct value");

        assertEquals( 314.1764,
                (double)new ExpressionNodeArithmeticOperator(TokenType.MULTIPLY, expr12_34, expr25_46).calculate(symbolTable),
                0.00000001, "calculate() gives the correct value");
        assertEquals( 2820,
                (long)new ExpressionNodeArithmeticOperator(TokenType.MULTIPLY, expr12, expr235).calculate(symbolTable),
                "calculate() gives the correct value");
        assertEquals( 148.08,
                (double)new ExpressionNodeArithmeticOperator(TokenType.MULTIPLY, expr12, expr12_34).calculate(symbolTable),
                0.00000001, "calculate() gives the correct value");

        assertEquals( 0.4846818538884525,
                (double)new ExpressionNodeArithmeticOperator(TokenType.DIVIDE, expr12_34, expr25_46).calculate(symbolTable),
                0.00000001, "calculate() gives the correct value");
        assertEquals( 19,
                (long)new ExpressionNodeArithmeticOperator(TokenType.DIVIDE, expr235, expr12).calculate(symbolTable),
                "calculate() gives the correct value");
        assertEquals( 0.9724473257698542,
                (double)new ExpressionNodeArithmeticOperator(TokenType.DIVIDE, expr12, expr12_34).calculate(symbolTable),
                0.00000001, "calculate() gives the correct value");


        // MODULO requires two integer operands
        CalculateException e = assertThrows( CalculateException.class, () ->
            new ExpressionNodeArithmeticOperator(TokenType.MODULO, expr12_34, expr25_46).calculate(symbolTable),
                "exception is thrown");
        assertNotNull(e);

        assertEquals( 7,
                (long)new ExpressionNodeArithmeticOperator(TokenType.MODULO, expr235, expr12).calculate(symbolTable),
                "calculate() gives the correct value");

        // MODULO requires two integer operands
        e = assertThrows( CalculateException.class, () ->
            new ExpressionNodeArithmeticOperator(TokenType.MODULO, expr12, expr12_34).calculate(symbolTable),
                "exception is thrown");
        assertNotNull(e);


        // Test unsupported token type
        e = assertThrows( CalculateException.class, () -> {
            ExpressionNode en = new ExpressionNodeArithmeticOperator(TokenType.DIVIDE, expr12, expr12_34);
            jmri.util.ReflectionUtilScaffold.setField(en, "_tokenType", TokenType.COMMA);
            en.calculate(symbolTable);
        }, "exception is thrown");
        assertNotNull(e);
    }

    @Test
    public void testGetDefinitionString() {

        ExpressionNode expr12_34 = new ExpressionNodeFloatingNumber(new Token(TokenType.NONE, "12.34", 0));
        ExpressionNode expr25_46 = new ExpressionNodeFloatingNumber(new Token(TokenType.NONE, "25.46", 0));
        ExpressionNode expr12 = new ExpressionNodeFloatingNumber(new Token(TokenType.NONE, "12", 0));
        ExpressionNode expr23 = new ExpressionNodeFloatingNumber(new Token(TokenType.NONE, "23", 0));

        assertEquals( "+(FloatNumber:12.34)",
                new ExpressionNodeArithmeticOperator(TokenType.ADD, null, expr12_34)
                        .getDefinitionString(), "getDefinitionString() gives the correct value");
        assertEquals( "-(FloatNumber:12)",
                new ExpressionNodeArithmeticOperator(TokenType.SUBTRACKT, null, expr12)
                        .getDefinitionString(), "getDefinitionString() gives the correct value");

        assertEquals( "(FloatNumber:12.34)+(FloatNumber:25.46)",
                new ExpressionNodeArithmeticOperator(TokenType.ADD, expr12_34, expr25_46)
                        .getDefinitionString(), "getDefinitionString() gives the correct value");
        assertEquals( "(FloatNumber:12.34)+(FloatNumber:12)",
                new ExpressionNodeArithmeticOperator(TokenType.ADD, expr12_34, expr12)
                        .getDefinitionString(), "getDefinitionString() gives the correct value");
        assertEquals( "(FloatNumber:12)+(FloatNumber:23)",
                new ExpressionNodeArithmeticOperator(TokenType.ADD, expr12, expr23)
                        .getDefinitionString(), "getDefinitionString() gives the correct value");
        assertEquals( "(FloatNumber:12)+(FloatNumber:12.34)",
                new ExpressionNodeArithmeticOperator(TokenType.ADD, expr12, expr12_34)
                        .getDefinitionString(), "getDefinitionString() gives the correct value");

        assertEquals( "(FloatNumber:12.34)-(FloatNumber:25.46)",
                new ExpressionNodeArithmeticOperator(TokenType.SUBTRACKT, expr12_34, expr25_46)
                        .getDefinitionString(), "getDefinitionString() gives the correct value");
        assertEquals( "(FloatNumber:12.34)-(FloatNumber:12)",
                new ExpressionNodeArithmeticOperator(TokenType.SUBTRACKT, expr12_34, expr12)
                        .getDefinitionString(), "getDefinitionString() gives the correct value");
        assertEquals( "(FloatNumber:12)-(FloatNumber:23)",
                new ExpressionNodeArithmeticOperator(TokenType.SUBTRACKT, expr12, expr23)
                        .getDefinitionString(), "getDefinitionString() gives the correct value");
        assertEquals( "(FloatNumber:12)-(FloatNumber:12.34)",
                new ExpressionNodeArithmeticOperator(TokenType.SUBTRACKT, expr12, expr12_34)
                        .getDefinitionString(), "getDefinitionString() gives the correct value");

        assertEquals( "(FloatNumber:12.34)*(FloatNumber:25.46)",
                new ExpressionNodeArithmeticOperator(TokenType.MULTIPLY, expr12_34, expr25_46)
                        .getDefinitionString(), "getDefinitionString() gives the correct value");
        assertEquals( "(FloatNumber:12.34)*(FloatNumber:12)",
                new ExpressionNodeArithmeticOperator(TokenType.MULTIPLY, expr12_34, expr12)
                        .getDefinitionString(), "getDefinitionString() gives the correct value");
        assertEquals( "(FloatNumber:12)*(FloatNumber:23)",
                new ExpressionNodeArithmeticOperator(TokenType.MULTIPLY, expr12, expr23)
                        .getDefinitionString(), "getDefinitionString() gives the correct value");
        assertEquals( "(FloatNumber:12)*(FloatNumber:12.34)",
                new ExpressionNodeArithmeticOperator(TokenType.MULTIPLY, expr12, expr12_34)
                        .getDefinitionString(), "getDefinitionString() gives the correct value");

        assertEquals( "(FloatNumber:12.34)/(FloatNumber:25.46)",
                new ExpressionNodeArithmeticOperator(TokenType.DIVIDE, expr12_34, expr25_46)
                        .getDefinitionString(), "getDefinitionString() gives the correct value");
        assertEquals( "(FloatNumber:12.34)/(FloatNumber:12)",
                new ExpressionNodeArithmeticOperator(TokenType.DIVIDE, expr12_34, expr12)
                        .getDefinitionString(), "getDefinitionString() gives the correct value");
        assertEquals( "(FloatNumber:12)/(FloatNumber:23)",
                new ExpressionNodeArithmeticOperator(TokenType.DIVIDE, expr12, expr23)
                        .getDefinitionString(), "getDefinitionString() gives the correct value");
        assertEquals( "(FloatNumber:12)/(FloatNumber:12.34)",
                new ExpressionNodeArithmeticOperator(TokenType.DIVIDE, expr12, expr12_34)
                        .getDefinitionString(), "getDefinitionString() gives the correct value");

        assertEquals( "(FloatNumber:12.34)%(FloatNumber:25.46)",
                new ExpressionNodeArithmeticOperator(TokenType.MODULO, expr12_34, expr25_46)
                        .getDefinitionString(), "getDefinitionString() gives the correct value");
        assertEquals( "(FloatNumber:12.34)%(FloatNumber:12)",
                new ExpressionNodeArithmeticOperator(TokenType.MODULO, expr12_34, expr12)
                        .getDefinitionString(), "getDefinitionString() gives the correct value");
        assertEquals( "(FloatNumber:12)%(FloatNumber:23)",
                new ExpressionNodeArithmeticOperator(TokenType.MODULO, expr12, expr23)
                        .getDefinitionString(), "getDefinitionString() gives the correct value");
        assertEquals( "(FloatNumber:12)%(FloatNumber:12.34)",
                new ExpressionNodeArithmeticOperator(TokenType.MODULO, expr12, expr12_34)
                        .getDefinitionString(), "getDefinitionString() gives the correct value");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.tearDown();
    }

}
