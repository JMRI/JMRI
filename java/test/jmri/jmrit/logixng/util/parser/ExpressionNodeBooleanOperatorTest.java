package jmri.jmrit.logixng.util.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
 * Test ParsedExpression
 *
 * @author Daniel Bergqvist 2019
 */
public class ExpressionNodeBooleanOperatorTest {

    @Test
    public void testCtor() throws ParserException {

        ExpressionNode exprTrue = new ExpressionNodeTrue();
        ExpressionNode exprFalse = new ExpressionNodeFalse();

        Token token = new Token(TokenType.NONE, "1", 0);
        ExpressionNodeFloatingNumber expressionNumber = new ExpressionNodeFloatingNumber(token);
        ExpressionNodeBooleanOperator t = new ExpressionNodeBooleanOperator(TokenType.BOOLEAN_NOT, null, expressionNumber);
        assertNotNull( t, "exists");


        // Test right side is null
        IllegalArgumentException e = assertThrows( IllegalArgumentException.class, () -> {
            var enbo = new ExpressionNodeBooleanOperator(TokenType.BOOLEAN_NOT, null, null);
            fail("should have thrown, not created " + enbo);
        }, "exception is thrown");
        assertNotNull(e);

        // Test invalid token
        e = assertThrows( IllegalArgumentException.class, () -> {
            var enbo = new ExpressionNodeBooleanOperator(TokenType.BINARY_AND, null, null);
            fail("should have thrown, not created " + enbo);
        }, "exception is thrown");
        assertNotNull(e);

        // AND requires two operands
        e = assertThrows( IllegalArgumentException.class, () -> {
            var enbo = new ExpressionNodeBooleanOperator(TokenType.BOOLEAN_AND, null, exprTrue);
            fail("should have thrown, not created " + enbo);
        }, "exception is thrown");
        assertNotNull(e);

        // OR requires two operands
        e = assertThrows( IllegalArgumentException.class, () -> {
            var enbo = new ExpressionNodeBooleanOperator(TokenType.BOOLEAN_OR, null, exprTrue);
            fail("should have thrown, not created " + enbo);
        }, "exception is thrown");
        assertNotNull(e);

        // NOT requires only one operands
        e = assertThrows( IllegalArgumentException.class, () -> {
            var enbo = new ExpressionNodeBooleanOperator(TokenType.BOOLEAN_NOT, exprFalse, exprTrue);
            fail("should have thrown, not created " + enbo);
        }, "exception is thrown");
        assertNotNull(e);

        // BINARY_AND is an unsupported operator
        e = assertThrows( IllegalArgumentException.class, () -> {
            var enbo = new ExpressionNodeBooleanOperator(TokenType.BINARY_AND, exprFalse, exprTrue);
            fail("should have thrown, not created " + enbo);
        }, "exception is thrown");
        assertNotNull(e);
    }

    @Test
    public void testCalculate() throws JmriException {

        ExpressionNode exprTrue1 = new ExpressionNodeTrue();
        ExpressionNode exprTrue2 = new ExpressionNodeTrue();
        ExpressionNode exprTrue3 = new ExpressionNodeIntegerNumber(new Token(TokenType.NONE, "1", 0));
        ExpressionNode exprFalse1 = new ExpressionNodeFalse();
        ExpressionNode exprFalse2 = new ExpressionNodeFalse();
        ExpressionNode exprFalse3 = new ExpressionNodeIntegerNumber(new Token(TokenType.NONE, "0", 0));
        ExpressionNode expr12_34 = new ExpressionNodeFloatingNumber(new Token(TokenType.NONE, "12.34", 0));
        ExpressionNode expr25_46 = new ExpressionNodeFloatingNumber(new Token(TokenType.NONE, "25.46", 0));


        SymbolTable symbolTable = new DefaultSymbolTable(new DefaultConditionalNG("IQC1", null));

        assertFalse( (Boolean)new ExpressionNodeBooleanOperator(TokenType.BOOLEAN_NOT, null, exprTrue1).calculate(symbolTable),
                "calculate() gives the correct value");
        assertTrue( (Boolean)new ExpressionNodeBooleanOperator(TokenType.BOOLEAN_NOT, null, exprFalse1).calculate(symbolTable),
                "calculate() gives the correct value");

        assertTrue( (Boolean)new ExpressionNodeBooleanOperator(TokenType.BOOLEAN_AND, exprTrue1, exprTrue2).calculate(symbolTable),
                "calculate() gives the correct value");
        assertFalse( (Boolean)new ExpressionNodeBooleanOperator(TokenType.BOOLEAN_AND, exprTrue1, exprFalse1).calculate(symbolTable),
                "calculate() gives the correct value");
        assertFalse( (Boolean)new ExpressionNodeBooleanOperator(TokenType.BOOLEAN_AND, exprFalse1, exprFalse2).calculate(symbolTable),
                "calculate() gives the correct value");
        assertFalse( (Boolean)new ExpressionNodeBooleanOperator(TokenType.BOOLEAN_AND, exprFalse1, exprTrue1).calculate(symbolTable),
                "calculate() gives the correct value");

        assertTrue( (Boolean)new ExpressionNodeBooleanOperator(TokenType.BOOLEAN_OR, exprTrue1, exprTrue2).calculate(symbolTable),
                "calculate() gives the correct value");
        assertTrue( (Boolean)new ExpressionNodeBooleanOperator(TokenType.BOOLEAN_OR, exprTrue1, exprFalse1).calculate(symbolTable),
                "calculate() gives the correct value");
        assertFalse( (Boolean)new ExpressionNodeBooleanOperator(TokenType.BOOLEAN_OR, exprFalse1, exprFalse1).calculate(symbolTable),
                "calculate() gives the correct value");
        assertTrue( (Boolean)new ExpressionNodeBooleanOperator(TokenType.BOOLEAN_OR, exprFalse1, exprTrue1).calculate(symbolTable),
                "calculate() gives the correct value");

        // Test non boolean operands
        assertTrue( (Boolean)new ExpressionNodeBooleanOperator(TokenType.BOOLEAN_OR, exprTrue3, exprTrue2).calculate(symbolTable),
                "calculate() gives the correct value");
        assertTrue( (Boolean)new ExpressionNodeBooleanOperator(TokenType.BOOLEAN_OR, exprTrue3, exprFalse3).calculate(symbolTable),
                "calculate() gives the correct value");
        assertFalse( (Boolean)new ExpressionNodeBooleanOperator(TokenType.BOOLEAN_OR, exprFalse1, exprFalse3).calculate(symbolTable),
                "calculate() gives the correct value");
        assertTrue( (Boolean)new ExpressionNodeBooleanOperator(TokenType.BOOLEAN_OR, exprFalse3, exprTrue3).calculate(symbolTable),
                "calculate() gives the correct value");


        // ExpressionNodeBooleanOperator requires two operands that can be booleans
        CalculateException e = assertThrows( CalculateException.class, () ->
            new ExpressionNodeBooleanOperator(TokenType.BOOLEAN_OR, exprFalse1, expr25_46).calculate(symbolTable),
                "exception is thrown");
        assertNotNull(e);

        // ExpressionNodeBooleanOperator requires two operands that can be booleans
        e = assertThrows( CalculateException.class, () ->
            new ExpressionNodeBooleanOperator(TokenType.BOOLEAN_OR, expr12_34, exprFalse1).calculate(symbolTable),
                "exception is thrown");
        assertNotNull(e);

        // Test unsupported token type
        e = assertThrows( CalculateException.class, () -> {
            ExpressionNode en = new ExpressionNodeBooleanOperator(TokenType.BOOLEAN_OR, exprTrue1, exprTrue2);
            jmri.util.ReflectionUtilScaffold.setField(en, "_tokenType", TokenType.COMMA);
            en.calculate(symbolTable);
        }, "exception is thrown");
        assertNotNull(e);
    }

    @Test
    public void testGetDefinitionString() {

        ExpressionNode exprTrue1 = new ExpressionNodeTrue();
        ExpressionNode exprTrue2 = new ExpressionNodeTrue();
        ExpressionNode exprFalse1 = new ExpressionNodeFalse();
        ExpressionNode exprFalse2 = new ExpressionNodeFalse();

        assertEquals( "!(true)",
                new ExpressionNodeBooleanOperator(TokenType.BOOLEAN_NOT, null, exprTrue1)
                        .getDefinitionString(), "getDefinitionString() gives the correct value");
        assertEquals( "!(false)",
                new ExpressionNodeBooleanOperator(TokenType.BOOLEAN_NOT, null, exprFalse1)
                        .getDefinitionString(), "getDefinitionString() gives the correct value");

        assertEquals( "(true)&&(true)",
                new ExpressionNodeBooleanOperator(TokenType.BOOLEAN_AND, exprTrue1, exprTrue2)
                        .getDefinitionString(), "getDefinitionString() gives the correct value");
        assertEquals( "(true)&&(false)",
                new ExpressionNodeBooleanOperator(TokenType.BOOLEAN_AND, exprTrue1, exprFalse1)
                        .getDefinitionString(), "getDefinitionString() gives the correct value");
        assertEquals( "(false)&&(false)",
                new ExpressionNodeBooleanOperator(TokenType.BOOLEAN_AND, exprFalse1, exprFalse2)
                        .getDefinitionString(), "getDefinitionString() gives the correct value");
        assertEquals( "(false)&&(true)",
                new ExpressionNodeBooleanOperator(TokenType.BOOLEAN_AND, exprFalse1, exprTrue1)
                        .getDefinitionString(), "getDefinitionString() gives the correct value");

        assertEquals( "(true)||(true)",
                new ExpressionNodeBooleanOperator(TokenType.BOOLEAN_OR, exprTrue1, exprTrue2)
                        .getDefinitionString(), "getDefinitionString() gives the correct value");
        assertEquals( "(true)||(false)",
                new ExpressionNodeBooleanOperator(TokenType.BOOLEAN_OR, exprTrue1, exprFalse1)
                        .getDefinitionString(), "getDefinitionString() gives the correct value");
        assertEquals( "(false)||(false)",
                new ExpressionNodeBooleanOperator(TokenType.BOOLEAN_OR, exprFalse1, exprFalse2)
                        .getDefinitionString(), "getDefinitionString() gives the correct value");
        assertEquals( "(false)||(true)",
                new ExpressionNodeBooleanOperator(TokenType.BOOLEAN_OR, exprFalse1, exprTrue1)
                        .getDefinitionString(), "getDefinitionString() gives the correct value");

        assertEquals( "(true)^^(true)",
                new ExpressionNodeBooleanOperator(TokenType.BOOLEAN_XOR, exprTrue1, exprTrue2)
                        .getDefinitionString(), "getDefinitionString() gives the correct value");
        assertEquals( "(true)^^(false)",
                new ExpressionNodeBooleanOperator(TokenType.BOOLEAN_XOR, exprTrue1, exprFalse1)
                        .getDefinitionString(), "getDefinitionString() gives the correct value");
        assertEquals( "(false)^^(false)",
                new ExpressionNodeBooleanOperator(TokenType.BOOLEAN_XOR, exprFalse1, exprFalse2)
                        .getDefinitionString(), "getDefinitionString() gives the correct value");
        assertEquals( "(false)^^(true)",
                new ExpressionNodeBooleanOperator(TokenType.BOOLEAN_XOR, exprFalse1, exprTrue1)
                        .getDefinitionString(), "getDefinitionString() gives the correct value");


        // Test unsupported token type
        UnsupportedOperationException e = assertThrows( UnsupportedOperationException.class, () -> {
            ExpressionNode en = new ExpressionNodeBooleanOperator(TokenType.BOOLEAN_OR, exprTrue1, exprTrue2);
            jmri.util.ReflectionUtilScaffold.setField(en, "_tokenType", TokenType.COMMA);
            en.getDefinitionString();
        }, "exception is thrown");
        assertNotNull(e);
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
