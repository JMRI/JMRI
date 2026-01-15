package jmri.jmrit.logixng.util.parser;

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
public class ExpressionNodeComparingOperatorTest {

    @Test
    public void testCtor() throws ParserException {

        ExpressionNode exprTrue = new ExpressionNodeTrue();

        Token token = new Token(TokenType.NONE, "1", 0);
        ExpressionNodeFloatingNumber expressionNumber = new ExpressionNodeFloatingNumber(token);
        ExpressionNodeComparingOperator t = new ExpressionNodeComparingOperator(TokenType.EQUAL, expressionNumber, expressionNumber);
        assertNotNull( t, "exists");


        // Test invalid token
        IllegalArgumentException e = assertThrows( IllegalArgumentException.class, () -> {
            var enco = new ExpressionNodeComparingOperator(TokenType.BINARY_AND, null, null);
            fail("should have thrown, not created " + enco);
        }, "exception is thrown");
        assertNotNull(e);

        // Two operands are required
        e = assertThrows( IllegalArgumentException.class, () -> {
            var enco = new ExpressionNodeComparingOperator(TokenType.EQUAL, null, exprTrue);
            fail("should have thrown, not created " + enco);
        }, "exception is thrown");
        assertNotNull(e);

        // Two operands are required
        e = assertThrows( IllegalArgumentException.class, () -> {
            var enco = new ExpressionNodeComparingOperator(TokenType.EQUAL, exprTrue, null);
            fail("should have thrown, not created " + enco);
        }, "exception is thrown");
        assertNotNull(e);

    }

    @Test
    public void testCalculate() throws JmriException {
        
        ExpressionNode expr12_34 = new ExpressionNodeFloatingNumber(new Token(TokenType.NONE, "12.34", 0));
        ExpressionNode expr25_46 = new ExpressionNodeFloatingNumber(new Token(TokenType.NONE, "25.46", 0));
        ExpressionNode exprHello = new ExpressionNodeString(new Token(TokenType.NONE, "Hello", 0));
        ExpressionNode exprWorld = new ExpressionNodeString(new Token(TokenType.NONE, "World", 0));
        ExpressionNode exprNull = new ExpressionNodeString(new Token(TokenType.NONE, null, 0));
        
        
        SymbolTable symbolTable = new DefaultSymbolTable(new DefaultConditionalNG("IQC1", null));
        
        assertTrue( (boolean)new ExpressionNodeComparingOperator(TokenType.EQUAL, expr12_34, expr12_34).calculate(symbolTable),
                "calculate() gives the correct value");
        assertFalse( (boolean)new ExpressionNodeComparingOperator(TokenType.NOT_EQUAL, expr12_34, expr12_34).calculate(symbolTable),
                "calculate() gives the correct value");
        assertFalse( (boolean)new ExpressionNodeComparingOperator(TokenType.LESS_THAN, expr12_34, expr12_34).calculate(symbolTable),
                "calculate() gives the correct value");
        assertTrue( (boolean)new ExpressionNodeComparingOperator(TokenType.LESS_OR_EQUAL, expr12_34, expr12_34).calculate(symbolTable),
                "calculate() gives the correct value");
        assertFalse( (boolean)new ExpressionNodeComparingOperator(TokenType.GREATER_THAN, expr12_34, expr12_34).calculate(symbolTable),
                "calculate() gives the correct value");
        assertTrue( (boolean)new ExpressionNodeComparingOperator(TokenType.GREATER_OR_EQUAL, expr12_34, expr12_34).calculate(symbolTable),
                "calculate() gives the correct value");

        assertTrue( (boolean)new ExpressionNodeComparingOperator(TokenType.EQUAL, exprHello, exprHello).calculate(symbolTable),
                "calculate() gives the correct value");
        assertFalse( (boolean)new ExpressionNodeComparingOperator(TokenType.NOT_EQUAL, exprHello, exprHello).calculate(symbolTable),
                "calculate() gives the correct value");
        assertFalse( (boolean)new ExpressionNodeComparingOperator(TokenType.LESS_THAN, exprHello, exprHello).calculate(symbolTable),
                "calculate() gives the correct value");
        assertTrue( (boolean)new ExpressionNodeComparingOperator(TokenType.LESS_OR_EQUAL, exprHello, exprHello).calculate(symbolTable),
                "calculate() gives the correct value");
        assertFalse( (boolean)new ExpressionNodeComparingOperator(TokenType.GREATER_THAN, exprHello, exprHello).calculate(symbolTable),
                "calculate() gives the correct value");
        assertTrue( (boolean)new ExpressionNodeComparingOperator(TokenType.GREATER_OR_EQUAL, exprHello, exprHello).calculate(symbolTable),
                "calculate() gives the correct value");

        assertTrue( (boolean)new ExpressionNodeComparingOperator(TokenType.EQUAL, exprNull, exprNull).calculate(symbolTable),
                "calculate() gives the correct value");
        assertFalse( (boolean)new ExpressionNodeComparingOperator(TokenType.NOT_EQUAL, exprNull, exprNull).calculate(symbolTable),
                "calculate() gives the correct value");
        assertFalse( (boolean)new ExpressionNodeComparingOperator(TokenType.LESS_THAN, exprNull, exprNull).calculate(symbolTable),
                "calculate() gives the correct value");
        assertTrue( (boolean)new ExpressionNodeComparingOperator(TokenType.LESS_OR_EQUAL, exprNull, exprNull).calculate(symbolTable),
                "calculate() gives the correct value");
        assertFalse( (boolean)new ExpressionNodeComparingOperator(TokenType.GREATER_THAN, exprNull, exprNull).calculate(symbolTable),
                "calculate() gives the correct value");
        assertTrue( (boolean)new ExpressionNodeComparingOperator(TokenType.GREATER_OR_EQUAL, exprNull, exprNull).calculate(symbolTable),
                "calculate() gives the correct value");


        assertFalse( (boolean)new ExpressionNodeComparingOperator(TokenType.EQUAL, expr12_34, expr25_46).calculate(symbolTable),
                "calculate() gives the correct value");
        assertTrue( (boolean)new ExpressionNodeComparingOperator(TokenType.NOT_EQUAL, expr12_34, expr25_46).calculate(symbolTable),
                "calculate() gives the correct value");
        assertTrue( (boolean)new ExpressionNodeComparingOperator(TokenType.LESS_THAN, expr12_34, expr25_46).calculate(symbolTable),
                "calculate() gives the correct value");
        assertTrue( (boolean)new ExpressionNodeComparingOperator(TokenType.LESS_OR_EQUAL, expr12_34, expr25_46).calculate(symbolTable),
                "calculate() gives the correct value");
        assertFalse( (boolean)new ExpressionNodeComparingOperator(TokenType.GREATER_THAN, expr12_34, expr25_46).calculate(symbolTable),
                "calculate() gives the correct value");
        assertFalse( (boolean)new ExpressionNodeComparingOperator(TokenType.GREATER_OR_EQUAL, expr12_34, expr25_46).calculate(symbolTable),
                "calculate() gives the correct value");

        assertFalse( (boolean)new ExpressionNodeComparingOperator(TokenType.EQUAL, expr25_46, expr12_34).calculate(symbolTable),
                "calculate() gives the correct value");
        assertTrue( (boolean)new ExpressionNodeComparingOperator(TokenType.NOT_EQUAL, expr25_46, expr12_34).calculate(symbolTable),
                "calculate() gives the correct value");
        assertFalse( (boolean)new ExpressionNodeComparingOperator(TokenType.LESS_THAN, expr25_46, expr12_34).calculate(symbolTable),
                "calculate() gives the correct value");
        assertFalse( (boolean)new ExpressionNodeComparingOperator(TokenType.LESS_OR_EQUAL, expr25_46, expr12_34).calculate(symbolTable),
                "calculate() gives the correct value");
        assertTrue( (boolean)new ExpressionNodeComparingOperator(TokenType.GREATER_THAN, expr25_46, expr12_34).calculate(symbolTable),
                "calculate() gives the correct value");
        assertTrue( (boolean)new ExpressionNodeComparingOperator(TokenType.GREATER_OR_EQUAL, expr25_46, expr12_34).calculate(symbolTable),
                "calculate() gives the correct value");


        assertFalse( (boolean)new ExpressionNodeComparingOperator(TokenType.EQUAL, expr12_34, exprHello).calculate(symbolTable),
                "calculate() gives the correct value");
        assertTrue( (boolean)new ExpressionNodeComparingOperator(TokenType.NOT_EQUAL, expr12_34, exprHello).calculate(symbolTable),
                "calculate() gives the correct value");
        assertTrue( (boolean)new ExpressionNodeComparingOperator(TokenType.LESS_THAN, expr12_34, exprHello).calculate(symbolTable),
                "calculate() gives the correct value");
        assertTrue( (boolean)new ExpressionNodeComparingOperator(TokenType.LESS_OR_EQUAL, expr12_34, exprHello).calculate(symbolTable),
                "calculate() gives the correct value");
        assertFalse( (boolean)new ExpressionNodeComparingOperator(TokenType.GREATER_THAN, expr12_34, exprHello).calculate(symbolTable),
                "calculate() gives the correct value");
        assertFalse( (boolean)new ExpressionNodeComparingOperator(TokenType.GREATER_OR_EQUAL, expr12_34, exprHello).calculate(symbolTable),
                "calculate() gives the correct value");

        assertFalse( (boolean)new ExpressionNodeComparingOperator(TokenType.EQUAL, exprHello, expr12_34).calculate(symbolTable),
                "calculate() gives the correct value");
        assertTrue( (boolean)new ExpressionNodeComparingOperator(TokenType.NOT_EQUAL, exprHello, expr12_34).calculate(symbolTable),
                "calculate() gives the correct value");
        assertFalse( (boolean)new ExpressionNodeComparingOperator(TokenType.LESS_THAN, exprHello, expr12_34).calculate(symbolTable),
                "calculate() gives the correct value");
        assertFalse( (boolean)new ExpressionNodeComparingOperator(TokenType.LESS_OR_EQUAL, exprHello, expr12_34).calculate(symbolTable),
                "calculate() gives the correct value");
        assertTrue( (boolean)new ExpressionNodeComparingOperator(TokenType.GREATER_THAN, exprHello, expr12_34).calculate(symbolTable),
                "calculate() gives the correct value");
        assertTrue( (boolean)new ExpressionNodeComparingOperator(TokenType.GREATER_OR_EQUAL, exprHello, expr12_34).calculate(symbolTable),
                "calculate() gives the correct value");


        assertFalse( (boolean)new ExpressionNodeComparingOperator(TokenType.EQUAL, exprHello, exprWorld).calculate(symbolTable),
                "calculate() gives the correct value");
        assertTrue( (boolean)new ExpressionNodeComparingOperator(TokenType.NOT_EQUAL, exprHello, exprWorld).calculate(symbolTable),
                "calculate() gives the correct value");
        assertTrue( (boolean)new ExpressionNodeComparingOperator(TokenType.LESS_THAN, exprHello, exprWorld).calculate(symbolTable),
                "calculate() gives the correct value");
        assertTrue( (boolean)new ExpressionNodeComparingOperator(TokenType.LESS_OR_EQUAL, exprHello, exprWorld).calculate(symbolTable),
                "calculate() gives the correct value");
        assertFalse( (boolean)new ExpressionNodeComparingOperator(TokenType.GREATER_THAN, exprHello, exprWorld).calculate(symbolTable),
                "calculate() gives the correct value");
        assertFalse( (boolean)new ExpressionNodeComparingOperator(TokenType.GREATER_OR_EQUAL, exprHello, exprWorld).calculate(symbolTable),
                "calculate() gives the correct value");
        
        assertFalse( (boolean)new ExpressionNodeComparingOperator(TokenType.EQUAL, exprWorld, exprHello).calculate(symbolTable),
                "calculate() gives the correct value");
        assertTrue( (boolean)new ExpressionNodeComparingOperator(TokenType.NOT_EQUAL, exprWorld, exprHello).calculate(symbolTable),
                "calculate() gives the correct value");
        assertFalse( (boolean)new ExpressionNodeComparingOperator(TokenType.LESS_THAN, exprWorld, exprHello).calculate(symbolTable),
                "calculate() gives the correct value");
        assertFalse( (boolean)new ExpressionNodeComparingOperator(TokenType.LESS_OR_EQUAL, exprWorld, exprHello).calculate(symbolTable),
                "calculate() gives the correct value");
        assertTrue( (boolean)new ExpressionNodeComparingOperator(TokenType.GREATER_THAN, exprWorld, exprHello).calculate(symbolTable),
                "calculate() gives the correct value");
        assertTrue( (boolean)new ExpressionNodeComparingOperator(TokenType.GREATER_OR_EQUAL, exprWorld, exprHello).calculate(symbolTable),
                "calculate() gives the correct value");


        assertFalse( (boolean)new ExpressionNodeComparingOperator(TokenType.EQUAL, expr12_34, exprNull).calculate(symbolTable),
                "calculate() gives the correct value");
        assertTrue( (boolean)new ExpressionNodeComparingOperator(TokenType.NOT_EQUAL, expr12_34, exprNull).calculate(symbolTable),
                "calculate() gives the correct value");
        assertFalse( (boolean)new ExpressionNodeComparingOperator(TokenType.LESS_THAN, expr12_34, exprNull).calculate(symbolTable),
                "calculate() gives the correct value");
        assertFalse( (boolean)new ExpressionNodeComparingOperator(TokenType.LESS_OR_EQUAL, expr12_34, exprNull).calculate(symbolTable),
                "calculate() gives the correct value");
        assertTrue( (boolean)new ExpressionNodeComparingOperator(TokenType.GREATER_THAN, expr12_34, exprNull).calculate(symbolTable),
                "calculate() gives the correct value");
        assertTrue( (boolean)new ExpressionNodeComparingOperator(TokenType.GREATER_OR_EQUAL, expr12_34, exprNull).calculate(symbolTable),
                "calculate() gives the correct value");


        assertFalse( (boolean)new ExpressionNodeComparingOperator(TokenType.EQUAL, exprHello, exprNull).calculate(symbolTable),
                "calculate() gives the correct value");
        assertTrue( (boolean)new ExpressionNodeComparingOperator(TokenType.NOT_EQUAL, exprHello, exprNull).calculate(symbolTable),
                "calculate() gives the correct value");
        assertFalse( (boolean)new ExpressionNodeComparingOperator(TokenType.LESS_THAN, exprHello, exprNull).calculate(symbolTable),
                "calculate() gives the correct value");
        assertFalse( (boolean)new ExpressionNodeComparingOperator(TokenType.LESS_OR_EQUAL, exprHello, exprNull).calculate(symbolTable),
                "calculate() gives the correct value");
        assertTrue( (boolean)new ExpressionNodeComparingOperator(TokenType.GREATER_THAN, exprHello, exprNull).calculate(symbolTable),
                "calculate() gives the correct value");
        assertTrue( (boolean)new ExpressionNodeComparingOperator(TokenType.GREATER_OR_EQUAL, exprHello, exprNull).calculate(symbolTable),
                "calculate() gives the correct value");


        assertFalse( (boolean)new ExpressionNodeComparingOperator(TokenType.EQUAL, exprNull, expr12_34).calculate(symbolTable),
                "calculate() gives the correct value");
        assertTrue( (boolean)new ExpressionNodeComparingOperator(TokenType.NOT_EQUAL, exprNull, expr12_34).calculate(symbolTable),
                "calculate() gives the correct value");
        assertTrue( (boolean)new ExpressionNodeComparingOperator(TokenType.LESS_THAN, exprNull, expr12_34).calculate(symbolTable),
                "calculate() gives the correct value");
        assertTrue( (boolean)new ExpressionNodeComparingOperator(TokenType.LESS_OR_EQUAL, exprNull, expr12_34).calculate(symbolTable),
                "calculate() gives the correct value");
        assertFalse( (boolean)new ExpressionNodeComparingOperator(TokenType.GREATER_THAN, exprNull, expr12_34).calculate(symbolTable),
                "calculate() gives the correct value");
        assertFalse( (boolean)new ExpressionNodeComparingOperator(TokenType.GREATER_OR_EQUAL, exprNull, expr12_34).calculate(symbolTable),
                "calculate() gives the correct value");


        assertFalse( (boolean)new ExpressionNodeComparingOperator(TokenType.EQUAL, exprNull, exprHello).calculate(symbolTable),
                "calculate() gives the correct value");
        assertTrue( (boolean)new ExpressionNodeComparingOperator(TokenType.NOT_EQUAL, exprNull, exprHello).calculate(symbolTable),
                "calculate() gives the correct value");
        assertTrue( (boolean)new ExpressionNodeComparingOperator(TokenType.LESS_THAN, exprNull, exprHello).calculate(symbolTable),
                "calculate() gives the correct value");
        assertTrue( (boolean)new ExpressionNodeComparingOperator(TokenType.LESS_OR_EQUAL, exprNull, exprHello).calculate(symbolTable),
                "calculate() gives the correct value");
        assertFalse( (boolean)new ExpressionNodeComparingOperator(TokenType.GREATER_THAN, exprNull, exprHello).calculate(symbolTable),
                "calculate() gives the correct value");
        assertFalse( (boolean)new ExpressionNodeComparingOperator(TokenType.GREATER_OR_EQUAL, exprNull, exprHello).calculate(symbolTable),
                "calculate() gives the correct value");
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
