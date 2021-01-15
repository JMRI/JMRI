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
        Assert.assertNotNull("exists", t);
        t = new ExpressionNodeTernaryOperator(expressionNumber, null, expressionNumber);
        Assert.assertNotNull("exists", t);
        t = new ExpressionNodeTernaryOperator(expressionNumber, expressionNumber, null);
        Assert.assertNotNull("exists", t);
        t = new ExpressionNodeTernaryOperator(expressionNumber, expressionNumber, expressionNumber);
        Assert.assertNotNull("exists", t);
        
        AtomicBoolean hasThrown = new AtomicBoolean(false);
        
        // Left side must not be null
        try {
            new ExpressionNodeTernaryOperator(null, null, null);
        } catch (IllegalArgumentException e) {
            hasThrown.set(true);
        }
        Assert.assertTrue("exception is thrown", hasThrown.get());
        
        // Left side must not be null
        hasThrown.set(false);
        try {
            new ExpressionNodeTernaryOperator(null, expressionNumber, null);
        } catch (IllegalArgumentException e) {
            hasThrown.set(true);
        }
        Assert.assertTrue("exception is thrown", hasThrown.get());
        
        // Left side must not be null
        hasThrown.set(false);
        try {
            new ExpressionNodeTernaryOperator(null, null, expressionNumber);
        } catch (IllegalArgumentException e) {
            hasThrown.set(true);
        }
        Assert.assertTrue("exception is thrown", hasThrown.get());
        
        // Left side must not be null
        hasThrown.set(false);
        try {
            new ExpressionNodeTernaryOperator(null, expressionNumber, expressionNumber);
        } catch (IllegalArgumentException e) {
            hasThrown.set(true);
        }
        Assert.assertTrue("exception is thrown", hasThrown.get());
    }
    
    @Test
    public void testCalculate() throws Exception {
        
        ExpressionNode exprTrue = new ExpressionNodeTrue();
        ExpressionNode exprFalse = new ExpressionNodeFalse();
        
        ExpressionNode expr0 = new ExpressionNodeIntegerNumber(new Token(TokenType.NONE, "0", 0));
        ExpressionNode expr1 = new ExpressionNodeIntegerNumber(new Token(TokenType.NONE, "1", 0));
        ExpressionNode expr12_34 = new ExpressionNodeFloatingNumber(new Token(TokenType.NONE, "12.34", 0));
        ExpressionNode expr25_46 = new ExpressionNodeFloatingNumber(new Token(TokenType.NONE, "25.46", 0));
        
        SymbolTable symbolTable = new DefaultSymbolTable(new DefaultConditionalNG("IQC1", null));
        
        Assert.assertEquals("calculate() gives the correct value",
                12.34,
                (double)new ExpressionNodeTernaryOperator(expr1, expr12_34, expr25_46).calculate(symbolTable),
                0.00000001);
        Assert.assertEquals("calculate() gives the correct value",
                25.46,
                (double)new ExpressionNodeTernaryOperator(expr0, expr12_34, expr25_46).calculate(symbolTable),
                0.00000001);
        
        Assert.assertEquals("calculate() gives the correct value",
                12.34,
                (double)new ExpressionNodeTernaryOperator(exprTrue, expr12_34, expr25_46).calculate(symbolTable),
                0.00000001);
        Assert.assertEquals("calculate() gives the correct value",
                25.46,
                (double)new ExpressionNodeTernaryOperator(exprFalse, expr12_34, expr25_46).calculate(symbolTable),
                0.00000001);
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
