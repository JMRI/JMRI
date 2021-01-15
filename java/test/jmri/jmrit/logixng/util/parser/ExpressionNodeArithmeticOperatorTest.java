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
        Assert.assertNotNull("exists", t);
        
        AtomicBoolean hasThrown = new AtomicBoolean(false);
        
        // Test right side is null
        try {
            new ExpressionNodeArithmeticOperator(TokenType.ADD, null, null);
        } catch (IllegalArgumentException e) {
            hasThrown.set(true);
        }
        Assert.assertTrue("exception is thrown", hasThrown.get());
        
        // Test invalid token
        hasThrown.set(false);
        try {
            new ExpressionNodeArithmeticOperator(TokenType.BINARY_AND, null, null);
        } catch (IllegalArgumentException e) {
            hasThrown.set(true);
        }
        Assert.assertTrue("exception is thrown", hasThrown.get());
        
        // MULTIPLY requires two operands
        hasThrown.set(false);
        try {
            new ExpressionNodeArithmeticOperator(TokenType.MULTIPLY, null, exprTrue);
        } catch (IllegalArgumentException e) {
            hasThrown.set(true);
        }
        Assert.assertTrue("exception is thrown", hasThrown.get());
        
        // DIVIDE requires two operands
        hasThrown.set(false);
        try {
            new ExpressionNodeArithmeticOperator(TokenType.DIVIDE, null, exprTrue);
        } catch (IllegalArgumentException e) {
            hasThrown.set(true);
        }
        Assert.assertTrue("exception is thrown", hasThrown.get());
    }
    
    @Test
    public void testCalculate() throws Exception {
        
        ExpressionNode expr12_34 = new ExpressionNodeFloatingNumber(new Token(TokenType.NONE, "12.34", 0));
        ExpressionNode expr25_46 = new ExpressionNodeFloatingNumber(new Token(TokenType.NONE, "25.46", 0));
        ExpressionNode expr12 = new ExpressionNodeIntegerNumber(new Token(TokenType.NONE, "12", 0));
        ExpressionNode expr235 = new ExpressionNodeIntegerNumber(new Token(TokenType.NONE, "235", 0));
        
        SymbolTable symbolTable = new DefaultSymbolTable(new DefaultConditionalNG("IQC1", null));
        
        Assert.assertEquals("calculate() gives the correct value",
                37.8,
                (double)new ExpressionNodeArithmeticOperator(TokenType.ADD, expr12_34, expr25_46).calculate(symbolTable),
                0.00000001);
        Assert.assertEquals("calculate() gives the correct value",
                247,
                (long)new ExpressionNodeArithmeticOperator(TokenType.ADD, expr12, expr235).calculate(symbolTable));
        Assert.assertEquals("calculate() gives the correct value",
                24.34,
                (double)new ExpressionNodeArithmeticOperator(TokenType.ADD, expr12, expr12_34).calculate(symbolTable),
                0.00000001);
        
        Assert.assertEquals("calculate() gives the correct value",
                -13.12,
                (double)new ExpressionNodeArithmeticOperator(TokenType.SUBTRACKT, expr12_34, expr25_46).calculate(symbolTable),
                0.00000001);
        Assert.assertEquals("calculate() gives the correct value",
                -223,
                (long)new ExpressionNodeArithmeticOperator(TokenType.SUBTRACKT, expr12, expr235).calculate(symbolTable));
        Assert.assertEquals("calculate() gives the correct value",
                -0.34,
                (double)new ExpressionNodeArithmeticOperator(TokenType.SUBTRACKT, expr12, expr12_34).calculate(symbolTable),
                0.00000001);
        
        Assert.assertEquals("calculate() gives the correct value",
                314.1764,
                (double)new ExpressionNodeArithmeticOperator(TokenType.MULTIPLY, expr12_34, expr25_46).calculate(symbolTable),
                0.00000001);
        Assert.assertEquals("calculate() gives the correct value",
                2820,
                (long)new ExpressionNodeArithmeticOperator(TokenType.MULTIPLY, expr12, expr235).calculate(symbolTable));
        Assert.assertEquals("calculate() gives the correct value",
                148.08,
                (double)new ExpressionNodeArithmeticOperator(TokenType.MULTIPLY, expr12, expr12_34).calculate(symbolTable),
                0.00000001);
        
        Assert.assertEquals("calculate() gives the correct value",
                0.4846818538884525,
                (double)new ExpressionNodeArithmeticOperator(TokenType.DIVIDE, expr12_34, expr25_46).calculate(symbolTable),
                0.00000001);
        Assert.assertEquals("calculate() gives the correct value",
                19,
                (long)new ExpressionNodeArithmeticOperator(TokenType.DIVIDE, expr235, expr12).calculate(symbolTable));
        Assert.assertEquals("calculate() gives the correct value",
                0.9724473257698542,
                (double)new ExpressionNodeArithmeticOperator(TokenType.DIVIDE, expr12, expr12_34).calculate(symbolTable),
                0.00000001);
        
        AtomicBoolean hasThrown = new AtomicBoolean(false);
        
        // MODULO requires two integer operands
        hasThrown.set(false);
        try {
            new ExpressionNodeArithmeticOperator(TokenType.MODULO, expr12_34, expr25_46).calculate(symbolTable);
        } catch (CalculateException e) {
            hasThrown.set(true);
        }
        Assert.assertTrue("exception is thrown", hasThrown.get());
        
        Assert.assertEquals("calculate() gives the correct value",
                7,
                (long)new ExpressionNodeArithmeticOperator(TokenType.MODULO, expr235, expr12).calculate(symbolTable));
        
        // MODULO requires two integer operands
        hasThrown.set(false);
        try {
            new ExpressionNodeArithmeticOperator(TokenType.MODULO, expr12, expr12_34).calculate(symbolTable);
        } catch (CalculateException e) {
            hasThrown.set(true);
        }
        Assert.assertTrue("exception is thrown", hasThrown.get());
        
        // Test unsupported token type
        hasThrown.set(false);
        try {
            ExpressionNode en = new ExpressionNodeArithmeticOperator(TokenType.DIVIDE, expr12, expr12_34);
            jmri.util.ReflectionUtilScaffold.setField(en, "_tokenType", TokenType.COMMA);
            en.calculate(symbolTable);
        } catch (CalculateException e) {
            hasThrown.set(true);
        }
        Assert.assertTrue("exception is thrown", hasThrown.get());
    }
    
    @Test
    public void testGetDefinitionString() throws Exception {
        
        ExpressionNode expr12_34 = new ExpressionNodeFloatingNumber(new Token(TokenType.NONE, "12.34", 0));
        ExpressionNode expr25_46 = new ExpressionNodeFloatingNumber(new Token(TokenType.NONE, "25.46", 0));
        ExpressionNode expr12 = new ExpressionNodeFloatingNumber(new Token(TokenType.NONE, "12", 0));
        ExpressionNode expr23 = new ExpressionNodeFloatingNumber(new Token(TokenType.NONE, "23", 0));
        
        Assert.assertEquals("getDefinitionString() gives the correct value",
                "(null)+(FloatNumber:12.34)",
                new ExpressionNodeArithmeticOperator(TokenType.ADD, null, expr12_34)
                        .getDefinitionString());
        Assert.assertEquals("calculate gives the correct value",
                "(null)-(FloatNumber:12)",
                new ExpressionNodeArithmeticOperator(TokenType.SUBTRACKT, null, expr12)
                        .getDefinitionString());
        
        Assert.assertEquals("getDefinitionString() gives the correct value",
                "(FloatNumber:12.34)+(FloatNumber:25.46)",
                new ExpressionNodeArithmeticOperator(TokenType.ADD, expr12_34, expr25_46)
                        .getDefinitionString());
        Assert.assertEquals("getDefinitionString() gives the correct value",
                "(FloatNumber:12.34)+(FloatNumber:12)",
                new ExpressionNodeArithmeticOperator(TokenType.ADD, expr12_34, expr12)
                        .getDefinitionString());
        Assert.assertEquals("getDefinitionString() gives the correct value",
                "(FloatNumber:12)+(FloatNumber:23)",
                new ExpressionNodeArithmeticOperator(TokenType.ADD, expr12, expr23)
                        .getDefinitionString());
        Assert.assertEquals("getDefinitionString() gives the correct value",
                "(FloatNumber:12)+(FloatNumber:12.34)",
                new ExpressionNodeArithmeticOperator(TokenType.ADD, expr12, expr12_34)
                        .getDefinitionString());
        
        Assert.assertEquals("getDefinitionString() gives the correct value",
                "(FloatNumber:12.34)-(FloatNumber:25.46)",
                new ExpressionNodeArithmeticOperator(TokenType.SUBTRACKT, expr12_34, expr25_46)
                        .getDefinitionString());
        Assert.assertEquals("getDefinitionString() gives the correct value",
                "(FloatNumber:12.34)-(FloatNumber:12)",
                new ExpressionNodeArithmeticOperator(TokenType.SUBTRACKT, expr12_34, expr12)
                        .getDefinitionString());
        Assert.assertEquals("getDefinitionString() gives the correct value",
                "(FloatNumber:12)-(FloatNumber:23)",
                new ExpressionNodeArithmeticOperator(TokenType.SUBTRACKT, expr12, expr23)
                        .getDefinitionString());
        Assert.assertEquals("getDefinitionString() gives the correct value",
                "(FloatNumber:12)-(FloatNumber:12.34)",
                new ExpressionNodeArithmeticOperator(TokenType.SUBTRACKT, expr12, expr12_34)
                        .getDefinitionString());
        
        Assert.assertEquals("getDefinitionString() gives the correct value",
                "(FloatNumber:12.34)*(FloatNumber:25.46)",
                new ExpressionNodeArithmeticOperator(TokenType.MULTIPLY, expr12_34, expr25_46)
                        .getDefinitionString());
        Assert.assertEquals("getDefinitionString() gives the correct value",
                "(FloatNumber:12.34)*(FloatNumber:12)",
                new ExpressionNodeArithmeticOperator(TokenType.MULTIPLY, expr12_34, expr12)
                        .getDefinitionString());
        Assert.assertEquals("getDefinitionString() gives the correct value",
                "(FloatNumber:12)*(FloatNumber:23)",
                new ExpressionNodeArithmeticOperator(TokenType.MULTIPLY, expr12, expr23)
                        .getDefinitionString());
        Assert.assertEquals("getDefinitionString() gives the correct value",
                "(FloatNumber:12)*(FloatNumber:12.34)",
                new ExpressionNodeArithmeticOperator(TokenType.MULTIPLY, expr12, expr12_34)
                        .getDefinitionString());
        
        Assert.assertEquals("getDefinitionString() gives the correct value",
                "(FloatNumber:12.34)/(FloatNumber:25.46)",
                new ExpressionNodeArithmeticOperator(TokenType.DIVIDE, expr12_34, expr25_46)
                        .getDefinitionString());
        Assert.assertEquals("getDefinitionString() gives the correct value",
                "(FloatNumber:12.34)/(FloatNumber:12)",
                new ExpressionNodeArithmeticOperator(TokenType.DIVIDE, expr12_34, expr12)
                        .getDefinitionString());
        Assert.assertEquals("getDefinitionString() gives the correct value",
                "(FloatNumber:12)/(FloatNumber:23)",
                new ExpressionNodeArithmeticOperator(TokenType.DIVIDE, expr12, expr23)
                        .getDefinitionString());
        Assert.assertEquals("getDefinitionString() gives the correct value",
                "(FloatNumber:12)/(FloatNumber:12.34)",
                new ExpressionNodeArithmeticOperator(TokenType.DIVIDE, expr12, expr12_34)
                        .getDefinitionString());
        
        Assert.assertEquals("getDefinitionString() gives the correct value",
                "(FloatNumber:12.34)%(FloatNumber:25.46)",
                new ExpressionNodeArithmeticOperator(TokenType.MODULO, expr12_34, expr25_46)
                        .getDefinitionString());
        Assert.assertEquals("getDefinitionString() gives the correct value",
                "(FloatNumber:12.34)%(FloatNumber:12)",
                new ExpressionNodeArithmeticOperator(TokenType.MODULO, expr12_34, expr12)
                        .getDefinitionString());
        Assert.assertEquals("getDefinitionString() gives the correct value",
                "(FloatNumber:12)%(FloatNumber:23)",
                new ExpressionNodeArithmeticOperator(TokenType.MODULO, expr12, expr23)
                        .getDefinitionString());
        Assert.assertEquals("getDefinitionString() gives the correct value",
                "(FloatNumber:12)%(FloatNumber:12.34)",
                new ExpressionNodeArithmeticOperator(TokenType.MODULO, expr12, expr12_34)
                        .getDefinitionString());
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
