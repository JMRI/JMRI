package jmri.jmrit.logixng.util.parser;

import java.util.concurrent.atomic.AtomicBoolean;

import jmri.jmrit.logixng.SymbolTable;
import jmri.jmrit.logixng.implementation.DefaultConditionalNG;
import jmri.jmrit.logixng.implementation.DefaultSymbolTable;
import jmri.jmrit.logixng.util.LogixNG_Thread;
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
public class ExpressionNodeComparingOperatorTest {

    @Test
    public void testCtor() throws ParserException {
        
        ExpressionNode exprTrue = new ExpressionNodeTrue();
        
        Token token = new Token(TokenType.NONE, "1", 0);
        ExpressionNodeFloatingNumber expressionNumber = new ExpressionNodeFloatingNumber(token);
        ExpressionNodeComparingOperator t = new ExpressionNodeComparingOperator(TokenType.EQUAL, expressionNumber, expressionNumber);
        Assert.assertNotNull("exists", t);
        
        AtomicBoolean hasThrown = new AtomicBoolean(false);
        
        // Test invalid token
        hasThrown.set(false);
        try {
            new ExpressionNodeComparingOperator(TokenType.BINARY_AND, null, null);
        } catch (IllegalArgumentException e) {
            hasThrown.set(true);
        }
        Assert.assertTrue("exception is thrown", hasThrown.get());
        
        // Two operands are required
        hasThrown.set(false);
        try {
            new ExpressionNodeComparingOperator(TokenType.EQUAL, null, exprTrue);
        } catch (IllegalArgumentException e) {
            hasThrown.set(true);
        }
        Assert.assertTrue("exception is thrown", hasThrown.get());
        
        // Two operands are required
        hasThrown.set(false);
        try {
            new ExpressionNodeComparingOperator(TokenType.EQUAL, exprTrue, null);
        } catch (IllegalArgumentException e) {
            hasThrown.set(true);
        }
        Assert.assertTrue("exception is thrown", hasThrown.get());
    }
    
    @Test
    public void testCalculate() throws Exception {
        
        ExpressionNode expr12_34 = new ExpressionNodeFloatingNumber(new Token(TokenType.NONE, "12.34", 0));
        ExpressionNode expr25_46 = new ExpressionNodeFloatingNumber(new Token(TokenType.NONE, "25.46", 0));
        ExpressionNode exprHello = new ExpressionNodeString(new Token(TokenType.NONE, "Hello", 0));
        ExpressionNode exprWorld = new ExpressionNodeString(new Token(TokenType.NONE, "World", 0));
        ExpressionNode exprNull = new ExpressionNodeString(new Token(TokenType.NONE, null, 0));
        
        
        SymbolTable symbolTable = new DefaultSymbolTable(new DefaultConditionalNG("IQC1", null));
        
        Assert.assertTrue("calculate() gives the correct value",
                (boolean)new ExpressionNodeComparingOperator(TokenType.EQUAL, expr12_34, expr12_34).calculate(symbolTable));
        Assert.assertFalse("calculate() gives the correct value",
                (boolean)new ExpressionNodeComparingOperator(TokenType.NOT_EQUAL, expr12_34, expr12_34).calculate(symbolTable));
        Assert.assertFalse("calculate() gives the correct value",
                (boolean)new ExpressionNodeComparingOperator(TokenType.LESS_THAN, expr12_34, expr12_34).calculate(symbolTable));
        Assert.assertTrue("calculate() gives the correct value",
                (boolean)new ExpressionNodeComparingOperator(TokenType.LESS_OR_EQUAL, expr12_34, expr12_34).calculate(symbolTable));
        Assert.assertFalse("calculate() gives the correct value",
                (boolean)new ExpressionNodeComparingOperator(TokenType.GREATER_THAN, expr12_34, expr12_34).calculate(symbolTable));
        Assert.assertTrue("calculate() gives the correct value",
                (boolean)new ExpressionNodeComparingOperator(TokenType.GREATER_OR_EQUAL, expr12_34, expr12_34).calculate(symbolTable));
        
        Assert.assertTrue("calculate() gives the correct value",
                (boolean)new ExpressionNodeComparingOperator(TokenType.EQUAL, exprHello, exprHello).calculate(symbolTable));
        Assert.assertFalse("calculate() gives the correct value",
                (boolean)new ExpressionNodeComparingOperator(TokenType.NOT_EQUAL, exprHello, exprHello).calculate(symbolTable));
        Assert.assertFalse("calculate() gives the correct value",
                (boolean)new ExpressionNodeComparingOperator(TokenType.LESS_THAN, exprHello, exprHello).calculate(symbolTable));
        Assert.assertTrue("calculate() gives the correct value",
                (boolean)new ExpressionNodeComparingOperator(TokenType.LESS_OR_EQUAL, exprHello, exprHello).calculate(symbolTable));
        Assert.assertFalse("calculate() gives the correct value",
                (boolean)new ExpressionNodeComparingOperator(TokenType.GREATER_THAN, exprHello, exprHello).calculate(symbolTable));
        Assert.assertTrue("calculate() gives the correct value",
                (boolean)new ExpressionNodeComparingOperator(TokenType.GREATER_OR_EQUAL, exprHello, exprHello).calculate(symbolTable));
        
        Assert.assertTrue("calculate() gives the correct value",
                (boolean)new ExpressionNodeComparingOperator(TokenType.EQUAL, exprNull, exprNull).calculate(symbolTable));
        Assert.assertFalse("calculate() gives the correct value",
                (boolean)new ExpressionNodeComparingOperator(TokenType.NOT_EQUAL, exprNull, exprNull).calculate(symbolTable));
        Assert.assertFalse("calculate() gives the correct value",
                (boolean)new ExpressionNodeComparingOperator(TokenType.LESS_THAN, exprNull, exprNull).calculate(symbolTable));
        Assert.assertTrue("calculate() gives the correct value",
                (boolean)new ExpressionNodeComparingOperator(TokenType.LESS_OR_EQUAL, exprNull, exprNull).calculate(symbolTable));
        Assert.assertFalse("calculate() gives the correct value",
                (boolean)new ExpressionNodeComparingOperator(TokenType.GREATER_THAN, exprNull, exprNull).calculate(symbolTable));
        Assert.assertTrue("calculate() gives the correct value",
                (boolean)new ExpressionNodeComparingOperator(TokenType.GREATER_OR_EQUAL, exprNull, exprNull).calculate(symbolTable));
        
        
        Assert.assertFalse("calculate() gives the correct value",
                (boolean)new ExpressionNodeComparingOperator(TokenType.EQUAL, expr12_34, expr25_46).calculate(symbolTable));
        Assert.assertTrue("calculate() gives the correct value",
                (boolean)new ExpressionNodeComparingOperator(TokenType.NOT_EQUAL, expr12_34, expr25_46).calculate(symbolTable));
        Assert.assertTrue("calculate() gives the correct value",
                (boolean)new ExpressionNodeComparingOperator(TokenType.LESS_THAN, expr12_34, expr25_46).calculate(symbolTable));
        Assert.assertTrue("calculate() gives the correct value",
                (boolean)new ExpressionNodeComparingOperator(TokenType.LESS_OR_EQUAL, expr12_34, expr25_46).calculate(symbolTable));
        Assert.assertFalse("calculate() gives the correct value",
                (boolean)new ExpressionNodeComparingOperator(TokenType.GREATER_THAN, expr12_34, expr25_46).calculate(symbolTable));
        Assert.assertFalse("calculate() gives the correct value",
                (boolean)new ExpressionNodeComparingOperator(TokenType.GREATER_OR_EQUAL, expr12_34, expr25_46).calculate(symbolTable));
        
        Assert.assertFalse("calculate() gives the correct value",
                (boolean)new ExpressionNodeComparingOperator(TokenType.EQUAL, expr25_46, expr12_34).calculate(symbolTable));
        Assert.assertTrue("calculate() gives the correct value",
                (boolean)new ExpressionNodeComparingOperator(TokenType.NOT_EQUAL, expr25_46, expr12_34).calculate(symbolTable));
        Assert.assertFalse("calculate() gives the correct value",
                (boolean)new ExpressionNodeComparingOperator(TokenType.LESS_THAN, expr25_46, expr12_34).calculate(symbolTable));
        Assert.assertFalse("calculate() gives the correct value",
                (boolean)new ExpressionNodeComparingOperator(TokenType.LESS_OR_EQUAL, expr25_46, expr12_34).calculate(symbolTable));
        Assert.assertTrue("calculate() gives the correct value",
                (boolean)new ExpressionNodeComparingOperator(TokenType.GREATER_THAN, expr25_46, expr12_34).calculate(symbolTable));
        Assert.assertTrue("calculate() gives the correct value",
                (boolean)new ExpressionNodeComparingOperator(TokenType.GREATER_OR_EQUAL, expr25_46, expr12_34).calculate(symbolTable));
        
        
        Assert.assertFalse("calculate() gives the correct value",
                (boolean)new ExpressionNodeComparingOperator(TokenType.EQUAL, expr12_34, exprHello).calculate(symbolTable));
        Assert.assertTrue("calculate() gives the correct value",
                (boolean)new ExpressionNodeComparingOperator(TokenType.NOT_EQUAL, expr12_34, exprHello).calculate(symbolTable));
        Assert.assertTrue("calculate() gives the correct value",
                (boolean)new ExpressionNodeComparingOperator(TokenType.LESS_THAN, expr12_34, exprHello).calculate(symbolTable));
        Assert.assertTrue("calculate() gives the correct value",
                (boolean)new ExpressionNodeComparingOperator(TokenType.LESS_OR_EQUAL, expr12_34, exprHello).calculate(symbolTable));
        Assert.assertFalse("calculate() gives the correct value",
                (boolean)new ExpressionNodeComparingOperator(TokenType.GREATER_THAN, expr12_34, exprHello).calculate(symbolTable));
        Assert.assertFalse("calculate() gives the correct value",
                (boolean)new ExpressionNodeComparingOperator(TokenType.GREATER_OR_EQUAL, expr12_34, exprHello).calculate(symbolTable));
        
        Assert.assertFalse("calculate() gives the correct value",
                (boolean)new ExpressionNodeComparingOperator(TokenType.EQUAL, exprHello, expr12_34).calculate(symbolTable));
        Assert.assertTrue("calculate() gives the correct value",
                (boolean)new ExpressionNodeComparingOperator(TokenType.NOT_EQUAL, exprHello, expr12_34).calculate(symbolTable));
        Assert.assertFalse("calculate() gives the correct value",
                (boolean)new ExpressionNodeComparingOperator(TokenType.LESS_THAN, exprHello, expr12_34).calculate(symbolTable));
        Assert.assertFalse("calculate() gives the correct value",
                (boolean)new ExpressionNodeComparingOperator(TokenType.LESS_OR_EQUAL, exprHello, expr12_34).calculate(symbolTable));
        Assert.assertTrue("calculate() gives the correct value",
                (boolean)new ExpressionNodeComparingOperator(TokenType.GREATER_THAN, exprHello, expr12_34).calculate(symbolTable));
        Assert.assertTrue("calculate() gives the correct value",
                (boolean)new ExpressionNodeComparingOperator(TokenType.GREATER_OR_EQUAL, exprHello, expr12_34).calculate(symbolTable));
        
        
        Assert.assertFalse("calculate() gives the correct value",
                (boolean)new ExpressionNodeComparingOperator(TokenType.EQUAL, exprHello, exprWorld).calculate(symbolTable));
        Assert.assertTrue("calculate() gives the correct value",
                (boolean)new ExpressionNodeComparingOperator(TokenType.NOT_EQUAL, exprHello, exprWorld).calculate(symbolTable));
        Assert.assertTrue("calculate() gives the correct value",
                (boolean)new ExpressionNodeComparingOperator(TokenType.LESS_THAN, exprHello, exprWorld).calculate(symbolTable));
        Assert.assertTrue("calculate() gives the correct value",
                (boolean)new ExpressionNodeComparingOperator(TokenType.LESS_OR_EQUAL, exprHello, exprWorld).calculate(symbolTable));
        Assert.assertFalse("calculate() gives the correct value",
                (boolean)new ExpressionNodeComparingOperator(TokenType.GREATER_THAN, exprHello, exprWorld).calculate(symbolTable));
        Assert.assertFalse("calculate() gives the correct value",
                (boolean)new ExpressionNodeComparingOperator(TokenType.GREATER_OR_EQUAL, exprHello, exprWorld).calculate(symbolTable));
        
        Assert.assertFalse("calculate() gives the correct value",
                (boolean)new ExpressionNodeComparingOperator(TokenType.EQUAL, exprWorld, exprHello).calculate(symbolTable));
        Assert.assertTrue("calculate() gives the correct value",
                (boolean)new ExpressionNodeComparingOperator(TokenType.NOT_EQUAL, exprWorld, exprHello).calculate(symbolTable));
        Assert.assertFalse("calculate() gives the correct value",
                (boolean)new ExpressionNodeComparingOperator(TokenType.LESS_THAN, exprWorld, exprHello).calculate(symbolTable));
        Assert.assertFalse("calculate() gives the correct value",
                (boolean)new ExpressionNodeComparingOperator(TokenType.LESS_OR_EQUAL, exprWorld, exprHello).calculate(symbolTable));
        Assert.assertTrue("calculate() gives the correct value",
                (boolean)new ExpressionNodeComparingOperator(TokenType.GREATER_THAN, exprWorld, exprHello).calculate(symbolTable));
        Assert.assertTrue("calculate() gives the correct value",
                (boolean)new ExpressionNodeComparingOperator(TokenType.GREATER_OR_EQUAL, exprWorld, exprHello).calculate(symbolTable));
        
        
        Assert.assertFalse("calculate() gives the correct value",
                (boolean)new ExpressionNodeComparingOperator(TokenType.EQUAL, expr12_34, exprNull).calculate(symbolTable));
        Assert.assertTrue("calculate() gives the correct value",
                (boolean)new ExpressionNodeComparingOperator(TokenType.NOT_EQUAL, expr12_34, exprNull).calculate(symbolTable));
        Assert.assertFalse("calculate() gives the correct value",
                (boolean)new ExpressionNodeComparingOperator(TokenType.LESS_THAN, expr12_34, exprNull).calculate(symbolTable));
        Assert.assertFalse("calculate() gives the correct value",
                (boolean)new ExpressionNodeComparingOperator(TokenType.LESS_OR_EQUAL, expr12_34, exprNull).calculate(symbolTable));
        Assert.assertTrue("calculate() gives the correct value",
                (boolean)new ExpressionNodeComparingOperator(TokenType.GREATER_THAN, expr12_34, exprNull).calculate(symbolTable));
        Assert.assertTrue("calculate() gives the correct value",
                (boolean)new ExpressionNodeComparingOperator(TokenType.GREATER_OR_EQUAL, expr12_34, exprNull).calculate(symbolTable));
        
        
        Assert.assertFalse("calculate() gives the correct value",
                (boolean)new ExpressionNodeComparingOperator(TokenType.EQUAL, exprHello, exprNull).calculate(symbolTable));
        Assert.assertTrue("calculate() gives the correct value",
                (boolean)new ExpressionNodeComparingOperator(TokenType.NOT_EQUAL, exprHello, exprNull).calculate(symbolTable));
        Assert.assertFalse("calculate() gives the correct value",
                (boolean)new ExpressionNodeComparingOperator(TokenType.LESS_THAN, exprHello, exprNull).calculate(symbolTable));
        Assert.assertFalse("calculate() gives the correct value",
                (boolean)new ExpressionNodeComparingOperator(TokenType.LESS_OR_EQUAL, exprHello, exprNull).calculate(symbolTable));
        Assert.assertTrue("calculate() gives the correct value",
                (boolean)new ExpressionNodeComparingOperator(TokenType.GREATER_THAN, exprHello, exprNull).calculate(symbolTable));
        Assert.assertTrue("calculate() gives the correct value",
                (boolean)new ExpressionNodeComparingOperator(TokenType.GREATER_OR_EQUAL, exprHello, exprNull).calculate(symbolTable));
        
        
        Assert.assertFalse("calculate() gives the correct value",
                (boolean)new ExpressionNodeComparingOperator(TokenType.EQUAL, exprNull, expr12_34).calculate(symbolTable));
        Assert.assertTrue("calculate() gives the correct value",
                (boolean)new ExpressionNodeComparingOperator(TokenType.NOT_EQUAL, exprNull, expr12_34).calculate(symbolTable));
        Assert.assertTrue("calculate() gives the correct value",
                (boolean)new ExpressionNodeComparingOperator(TokenType.LESS_THAN, exprNull, expr12_34).calculate(symbolTable));
        Assert.assertTrue("calculate() gives the correct value",
                (boolean)new ExpressionNodeComparingOperator(TokenType.LESS_OR_EQUAL, exprNull, expr12_34).calculate(symbolTable));
        Assert.assertFalse("calculate() gives the correct value",
                (boolean)new ExpressionNodeComparingOperator(TokenType.GREATER_THAN, exprNull, expr12_34).calculate(symbolTable));
        Assert.assertFalse("calculate() gives the correct value",
                (boolean)new ExpressionNodeComparingOperator(TokenType.GREATER_OR_EQUAL, exprNull, expr12_34).calculate(symbolTable));
        
        
        Assert.assertFalse("calculate() gives the correct value",
                (boolean)new ExpressionNodeComparingOperator(TokenType.EQUAL, exprNull, exprHello).calculate(symbolTable));
        Assert.assertTrue("calculate() gives the correct value",
                (boolean)new ExpressionNodeComparingOperator(TokenType.NOT_EQUAL, exprNull, exprHello).calculate(symbolTable));
        Assert.assertTrue("calculate() gives the correct value",
                (boolean)new ExpressionNodeComparingOperator(TokenType.LESS_THAN, exprNull, exprHello).calculate(symbolTable));
        Assert.assertTrue("calculate() gives the correct value",
                (boolean)new ExpressionNodeComparingOperator(TokenType.LESS_OR_EQUAL, exprNull, exprHello).calculate(symbolTable));
        Assert.assertFalse("calculate() gives the correct value",
                (boolean)new ExpressionNodeComparingOperator(TokenType.GREATER_THAN, exprNull, exprHello).calculate(symbolTable));
        Assert.assertFalse("calculate() gives the correct value",
                (boolean)new ExpressionNodeComparingOperator(TokenType.GREATER_OR_EQUAL, exprNull, exprHello).calculate(symbolTable));
    }
    
    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.tearDown();
    }
    
}
