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
 * Test ExpressionNodeTernaryOperator
 * 
 * @author Daniel Bergqvist 2020
 */
public class ExpressionNodeTernaryOperatorTest {

    @Test
    public void testCtor() throws ParserException {

        Token token = new Token(TokenType.NONE, "1", 0);
        ExpressionNodeFloatingNumber expressionNumber = new ExpressionNodeFloatingNumber(token);
        ExpressionNodeTernaryOperator t = new ExpressionNodeTernaryOperator(expressionNumber, null, null);
        assertNotNull( t, "exists");
        t = new ExpressionNodeTernaryOperator(expressionNumber, null, expressionNumber);
        assertNotNull( t, "exists");
        t = new ExpressionNodeTernaryOperator(expressionNumber, expressionNumber, null);
        assertNotNull( t, "exists");
        t = new ExpressionNodeTernaryOperator(expressionNumber, expressionNumber, expressionNumber);
        assertNotNull( t, "exists");


        // Left side must not be null
        IllegalArgumentException e = assertThrows( IllegalArgumentException.class, () -> {
            var ento = new ExpressionNodeTernaryOperator(null, null, null);
            fail("should have thrown, created " + ento);
        }, "exception is thrown");
        assertNotNull(e);

        // Left side must not be null
        e = assertThrows( IllegalArgumentException.class, () -> {
            var ento = new ExpressionNodeTernaryOperator(null, expressionNumber, null);
            fail("should have thrown, created " + ento);
        }, "exception is thrown");
        assertNotNull(e);

        // Left side must not be null
        e = assertThrows( IllegalArgumentException.class, () -> {
            var ento = new ExpressionNodeTernaryOperator(null, null, expressionNumber);
            fail("should have thrown, created " + ento);
        }, "exception is thrown");
        assertNotNull(e);

        // Left side must not be null
        e = assertThrows( IllegalArgumentException.class, () -> {
            var ento = new ExpressionNodeTernaryOperator(null, expressionNumber, expressionNumber);
            fail("should have thrown, created " + ento);
        }, "exception is thrown");
        assertNotNull(e);
    }

    @Test
    public void testCalculate() throws JmriException {

        ExpressionNode exprTrue = new ExpressionNodeTrue();
        ExpressionNode exprFalse = new ExpressionNodeFalse();

        ExpressionNode expr0 = new ExpressionNodeIntegerNumber(new Token(TokenType.NONE, "0", 0));
        ExpressionNode expr1 = new ExpressionNodeIntegerNumber(new Token(TokenType.NONE, "1", 0));
        ExpressionNode expr12_34 = new ExpressionNodeFloatingNumber(new Token(TokenType.NONE, "12.34", 0));
        ExpressionNode expr25_46 = new ExpressionNodeFloatingNumber(new Token(TokenType.NONE, "25.46", 0));

        SymbolTable symbolTable = new DefaultSymbolTable(new DefaultConditionalNG("IQC1", null));

        assertEquals( 12.34,
                (double)new ExpressionNodeTernaryOperator(expr1, expr12_34, expr25_46).calculate(symbolTable),
                0.00000001, "calculate() gives the correct value");
        assertEquals( 25.46,
                (double)new ExpressionNodeTernaryOperator(expr0, expr12_34, expr25_46).calculate(symbolTable),
                0.00000001, "calculate() gives the correct value");

        assertEquals( 12.34,
                (double)new ExpressionNodeTernaryOperator(exprTrue, expr12_34, expr25_46).calculate(symbolTable),
                0.00000001, "calculate() gives the correct value");
        assertEquals( 25.46,
                (double)new ExpressionNodeTernaryOperator(exprFalse, expr12_34, expr25_46).calculate(symbolTable),
                0.00000001, "calculate() gives the correct value");
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
