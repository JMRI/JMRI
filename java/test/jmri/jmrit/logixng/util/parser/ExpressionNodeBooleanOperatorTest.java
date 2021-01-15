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
public class ExpressionNodeBooleanOperatorTest {

    @Test
    public void testCtor() throws ParserException {
        
        ExpressionNode exprTrue = new ExpressionNodeTrue();
        ExpressionNode exprFalse = new ExpressionNodeFalse();
        
        Token token = new Token(TokenType.NONE, "1", 0);
        ExpressionNodeFloatingNumber expressionNumber = new ExpressionNodeFloatingNumber(token);
        ExpressionNodeBooleanOperator t = new ExpressionNodeBooleanOperator(TokenType.BOOLEAN_NOT, null, expressionNumber);
        Assert.assertNotNull("exists", t);
        
        AtomicBoolean hasThrown = new AtomicBoolean(false);
        
        // Test right side is null
        try {
            new ExpressionNodeBooleanOperator(TokenType.BOOLEAN_NOT, null, null);
        } catch (IllegalArgumentException e) {
            hasThrown.set(true);
        }
        Assert.assertTrue("exception is thrown", hasThrown.get());
        
        // Test invalid token
        try {
            new ExpressionNodeBooleanOperator(TokenType.BINARY_AND, null, null);
        } catch (IllegalArgumentException e) {
            hasThrown.set(true);
        }
        Assert.assertTrue("exception is thrown", hasThrown.get());
        
        // AND requires two operands
        try {
            new ExpressionNodeBooleanOperator(TokenType.BOOLEAN_AND, null, exprTrue);
        } catch (IllegalArgumentException e) {
            hasThrown.set(true);
        }
        Assert.assertTrue("exception is thrown", hasThrown.get());
        
        // OR requires two operands
        hasThrown.set(false);
        try {
            new ExpressionNodeBooleanOperator(TokenType.BOOLEAN_OR, null, exprTrue);
        } catch (IllegalArgumentException e) {
            hasThrown.set(true);
        }
        Assert.assertTrue("exception is thrown", hasThrown.get());
        
        // NOT requires only one operands
        hasThrown.set(false);
        try {
            new ExpressionNodeBooleanOperator(TokenType.BOOLEAN_NOT, exprFalse, exprTrue);
        } catch (IllegalArgumentException e) {
            hasThrown.set(true);
        }
        Assert.assertTrue("exception is thrown", hasThrown.get());
        
        // BINARY_AND is an unsupported operator
        hasThrown.set(false);
        try {
            new ExpressionNodeBooleanOperator(TokenType.BINARY_AND, exprFalse, exprTrue);
        } catch (IllegalArgumentException e) {
            hasThrown.set(true);
        }
        Assert.assertTrue("exception is thrown", hasThrown.get());
    }
    
    @Test
    public void testCalculate() throws Exception {
        
        ExpressionNode exprTrue1 = new ExpressionNodeTrue();
        ExpressionNode exprTrue2 = new ExpressionNodeTrue();
        ExpressionNode exprTrue3 = new ExpressionNodeIntegerNumber(new Token(TokenType.NONE, "1", 0));
        ExpressionNode exprFalse1 = new ExpressionNodeFalse();
        ExpressionNode exprFalse2 = new ExpressionNodeFalse();
        ExpressionNode exprFalse3 = new ExpressionNodeIntegerNumber(new Token(TokenType.NONE, "0", 0));
        ExpressionNode expr12_34 = new ExpressionNodeFloatingNumber(new Token(TokenType.NONE, "12.34", 0));
        ExpressionNode expr25_46 = new ExpressionNodeFloatingNumber(new Token(TokenType.NONE, "25.46", 0));
       
        
        SymbolTable symbolTable = new DefaultSymbolTable(new DefaultConditionalNG("IQC1", null));
        
        Assert.assertFalse("calculate() gives the correct value",
                (Boolean)new ExpressionNodeBooleanOperator(TokenType.BOOLEAN_NOT, null, exprTrue1).calculate(symbolTable));
        Assert.assertTrue("calculate() gives the correct value",
                (Boolean)new ExpressionNodeBooleanOperator(TokenType.BOOLEAN_NOT, null, exprFalse1).calculate(symbolTable));
        
        Assert.assertTrue("calculate() gives the correct value",
                (Boolean)new ExpressionNodeBooleanOperator(TokenType.BOOLEAN_AND, exprTrue1, exprTrue2).calculate(symbolTable));
        Assert.assertFalse("calculate() gives the correct value",
                (Boolean)new ExpressionNodeBooleanOperator(TokenType.BOOLEAN_AND, exprTrue1, exprFalse1).calculate(symbolTable));
        Assert.assertFalse("calculate() gives the correct value",
                (Boolean)new ExpressionNodeBooleanOperator(TokenType.BOOLEAN_AND, exprFalse1, exprFalse2).calculate(symbolTable));
        Assert.assertFalse("calculate() gives the correct value",
                (Boolean)new ExpressionNodeBooleanOperator(TokenType.BOOLEAN_AND, exprFalse1, exprTrue1).calculate(symbolTable));
        
        Assert.assertTrue("calculate() gives the correct value",
                (Boolean)new ExpressionNodeBooleanOperator(TokenType.BOOLEAN_OR, exprTrue1, exprTrue2).calculate(symbolTable));
        Assert.assertTrue("calculate() gives the correct value",
                (Boolean)new ExpressionNodeBooleanOperator(TokenType.BOOLEAN_OR, exprTrue1, exprFalse1).calculate(symbolTable));
        Assert.assertFalse("calculate() gives the correct value",
                (Boolean)new ExpressionNodeBooleanOperator(TokenType.BOOLEAN_OR, exprFalse1, exprFalse1).calculate(symbolTable));
        Assert.assertTrue("calculate() gives the correct value",
                (Boolean)new ExpressionNodeBooleanOperator(TokenType.BOOLEAN_OR, exprFalse1, exprTrue1).calculate(symbolTable));
        
        // Test non boolean operands
        Assert.assertTrue("calculate() gives the correct value",
                (Boolean)new ExpressionNodeBooleanOperator(TokenType.BOOLEAN_OR, exprTrue3, exprTrue2).calculate(symbolTable));
        Assert.assertTrue("calculate() gives the correct value",
                (Boolean)new ExpressionNodeBooleanOperator(TokenType.BOOLEAN_OR, exprTrue3, exprFalse3).calculate(symbolTable));
        Assert.assertFalse("calculate() gives the correct value",
                (Boolean)new ExpressionNodeBooleanOperator(TokenType.BOOLEAN_OR, exprFalse1, exprFalse3).calculate(symbolTable));
        Assert.assertTrue("calculate() gives the correct value",
                (Boolean)new ExpressionNodeBooleanOperator(TokenType.BOOLEAN_OR, exprFalse3, exprTrue3).calculate(symbolTable));
        
        AtomicBoolean hasThrown = new AtomicBoolean(false);
        
        // ExpressionNodeBooleanOperator requires two operands that can be booleans
        hasThrown.set(false);
        try {
            new ExpressionNodeBooleanOperator(TokenType.BOOLEAN_OR, exprFalse1, expr25_46).calculate(symbolTable);
        } catch (CalculateException e) {
            hasThrown.set(true);
        }
        Assert.assertTrue("exception is thrown", hasThrown.get());
        
        // ExpressionNodeBooleanOperator requires two operands that can be booleans
        hasThrown.set(false);
        try {
            new ExpressionNodeBooleanOperator(TokenType.BOOLEAN_OR, expr12_34, exprFalse1).calculate(symbolTable);
        } catch (CalculateException e) {
            hasThrown.set(true);
        }
        Assert.assertTrue("exception is thrown", hasThrown.get());
        
        // Test unsupported token type
        hasThrown.set(false);
        try {
            ExpressionNode en = new ExpressionNodeBooleanOperator(TokenType.BOOLEAN_OR, exprTrue1, exprTrue2);
            jmri.util.ReflectionUtilScaffold.setField(en, "_tokenType", TokenType.COMMA);
            en.calculate(symbolTable);
        } catch (CalculateException e) {
            hasThrown.set(true);
        }
        Assert.assertTrue("exception is thrown", hasThrown.get());
    }
    
    @Test
    public void testGetDefinitionString() throws ParserException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        
        ExpressionNode exprTrue1 = new ExpressionNodeTrue();
        ExpressionNode exprTrue2 = new ExpressionNodeTrue();
        ExpressionNode exprFalse1 = new ExpressionNodeFalse();
        ExpressionNode exprFalse2 = new ExpressionNodeFalse();
        
        Assert.assertEquals("getDefinitionString() gives the correct value",
                "!(true)",
                new ExpressionNodeBooleanOperator(TokenType.BOOLEAN_NOT, null, exprTrue1)
                        .getDefinitionString());
        Assert.assertEquals("calculate gives the correct value",
                "!(false)",
                new ExpressionNodeBooleanOperator(TokenType.BOOLEAN_NOT, null, exprFalse1)
                        .getDefinitionString());
        
        Assert.assertEquals("getDefinitionString() gives the correct value",
                "(true)&&(true)",
                new ExpressionNodeBooleanOperator(TokenType.BOOLEAN_AND, exprTrue1, exprTrue2)
                        .getDefinitionString());
        Assert.assertEquals("getDefinitionString() gives the correct value",
                "(true)&&(false)",
                new ExpressionNodeBooleanOperator(TokenType.BOOLEAN_AND, exprTrue1, exprFalse1)
                        .getDefinitionString());
        Assert.assertEquals("getDefinitionString() gives the correct value",
                "(false)&&(false)",
                new ExpressionNodeBooleanOperator(TokenType.BOOLEAN_AND, exprFalse1, exprFalse2)
                        .getDefinitionString());
        Assert.assertEquals("getDefinitionString() gives the correct value",
                "(false)&&(true)",
                new ExpressionNodeBooleanOperator(TokenType.BOOLEAN_AND, exprFalse1, exprTrue1)
                        .getDefinitionString());
        
        Assert.assertEquals("getDefinitionString() gives the correct value",
                "(true)||(true)",
                new ExpressionNodeBooleanOperator(TokenType.BOOLEAN_OR, exprTrue1, exprTrue2)
                        .getDefinitionString());
        Assert.assertEquals("getDefinitionString() gives the correct value",
                "(true)||(false)",
                new ExpressionNodeBooleanOperator(TokenType.BOOLEAN_OR, exprTrue1, exprFalse1)
                        .getDefinitionString());
        Assert.assertEquals("getDefinitionString() gives the correct value",
                "(false)||(false)",
                new ExpressionNodeBooleanOperator(TokenType.BOOLEAN_OR, exprFalse1, exprFalse2)
                        .getDefinitionString());
        Assert.assertEquals("getDefinitionString() gives the correct value",
                "(false)||(true)",
                new ExpressionNodeBooleanOperator(TokenType.BOOLEAN_OR, exprFalse1, exprTrue1)
                        .getDefinitionString());
        
        AtomicBoolean hasThrown = new AtomicBoolean(false);
        
        // Test unsupported token type
        hasThrown.set(false);
        try {
            ExpressionNode en = new ExpressionNodeBooleanOperator(TokenType.BOOLEAN_OR, exprTrue1, exprTrue2);
            jmri.util.ReflectionUtilScaffold.setField(en, "_tokenType", TokenType.COMMA);
            en.getDefinitionString();
        } catch (UnsupportedOperationException e) {
            hasThrown.set(true);
        }
        Assert.assertTrue("exception is thrown", hasThrown.get());
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
