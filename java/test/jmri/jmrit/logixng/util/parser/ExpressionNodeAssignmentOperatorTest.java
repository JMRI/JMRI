package jmri.jmrit.logixng.util.parser;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.jmrit.logixng.LogixNG_Manager;
import jmri.jmrit.logixng.SymbolTable;
import jmri.jmrit.logixng.implementation.DefaultSymbolTable;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test ExpressionNodeAssignmentOperator
 * 
 * @author Daniel Bergqvist 2020
 */
public class ExpressionNodeAssignmentOperatorTest {
    
    @Test
    public void testCtor() throws ParserException {
        
        Map<String, Variable> variables = new HashMap<>();
        Token token = new Token(TokenType.NONE, "1", 0);
        ExpressionNodeIdentifier exprIdent = new ExpressionNodeIdentifier(new Token(TokenType.NONE, "myVar", 0), variables);
        ExpressionNodeFloatingNumber expressionNumber = new ExpressionNodeFloatingNumber(token);
        ExpressionNodeAssignmentOperator t = new ExpressionNodeAssignmentOperator(TokenType.ASSIGN, exprIdent, null);
        Assert.assertNotNull("exists", t);
        
        AtomicBoolean hasThrown = new AtomicBoolean(false);
        
        // Left side must be assignable
        try {
            new ExpressionNodeAssignmentOperator(TokenType.ASSIGN, expressionNumber, null);
        } catch (IllegalArgumentException e) {
            hasThrown.set(true);
        }
        Assert.assertTrue("exception is thrown", hasThrown.get());
        
        // Left side must not be null
        try {
            new ExpressionNodeAssignmentOperator(TokenType.ASSIGN, null, null);
        } catch (IllegalArgumentException e) {
            hasThrown.set(true);
        }
        Assert.assertTrue("exception is thrown", hasThrown.get());
        
        // Left side must not be null
        hasThrown.set(false);
        try {
            new ExpressionNodeAssignmentOperator(TokenType.ASSIGN, null, expressionNumber);
        } catch (IllegalArgumentException e) {
            hasThrown.set(true);
        }
        Assert.assertTrue("exception is thrown", hasThrown.get());
    }
    
    private Object getVariable(String name) {
        return InstanceManager.getDefault(LogixNG_Manager.class)
                .getSymbolTable().getValue(name);
    }
    
    private void setVariable(String name, Object value) {
        InstanceManager.getDefault(LogixNG_Manager.class)
                .getSymbolTable().setValue(name, value);
    }
    
    @Test
    public void testCalculate() throws Exception {
        
        Map<String, Variable> variables = new HashMap<>();
        ExpressionNodeIdentifier exprIdent = new ExpressionNodeIdentifier(new Token(TokenType.NONE, "myVar", 0), variables);
        
        ExpressionNode expr25_46 = new ExpressionNodeFloatingNumber(new Token(TokenType.NONE, "25.46", 0));
        ExpressionNode expr12 = new ExpressionNodeIntegerNumber(new Token(TokenType.NONE, "12", 0));
        
        setVariable("myVar", 42.11);
        Assert.assertEquals("calculate() gives the correct value",
                25.46,
                (double)new ExpressionNodeAssignmentOperator(TokenType.ASSIGN, exprIdent, expr25_46).calculate(),
                0.00000001);
        Assert.assertEquals("The variable has correct value", 25.46, (Double)getVariable("myVar"), 0.00000001);
        
        setVariable("myVar", 42.11);
        Assert.assertEquals("calculate() gives the correct value",
                67.57,
                (double)new ExpressionNodeAssignmentOperator(TokenType.ASSIGN_ADD, exprIdent, expr25_46).calculate(),
                0.00000001);
        Assert.assertEquals("The variable has correct value", 67.57, (Double)getVariable("myVar"), 0.00000001);
        
        setVariable("myVar", 42.11);
        Assert.assertEquals("calculate() gives the correct value",
                16.65,
                (double)new ExpressionNodeAssignmentOperator(TokenType.ASSIGN_SUBTRACKT, exprIdent, expr25_46).calculate(),
                0.00000001);
        Assert.assertEquals("The variable has correct value", 16.65, (Double)getVariable("myVar"), 0.00000001);
        
        setVariable("myVar", 42.11);
        Assert.assertEquals("calculate() gives the correct value",
                1072.1206,
                (double)new ExpressionNodeAssignmentOperator(TokenType.ASSIGN_MULTIPLY, exprIdent, expr25_46).calculate(),
                0.00000001);
        Assert.assertEquals("The variable has correct value", 1072.1206, (Double)getVariable("myVar"), 0.00000001);
        
        setVariable("myVar", 42.11);
        Assert.assertEquals("calculate() gives the correct value",
                1.653967,
                (double)new ExpressionNodeAssignmentOperator(TokenType.ASSIGN_DIVIDE, exprIdent, expr25_46).calculate(),
                0.00000001);
        Assert.assertEquals("The variable has correct value", 1.653967, (Double)getVariable("myVar"), 0.00000001);
        
        setVariable("myVar", 532);
        Assert.assertEquals("calculate() gives the correct value",
                4,
                (long)new ExpressionNodeAssignmentOperator(TokenType.ASSIGN_MODULO, exprIdent, expr12).calculate());
        Assert.assertEquals("The variable has correct value", 4, (long)getVariable("myVar"));
    }
    
    // The minimal setup for log4J
    @Before
    public void setUp() throws JmriException {
        JUnitUtil.setUp();
        
        List<SymbolTable.VariableData> localVariables = new ArrayList<>();
        localVariables.add(new SymbolTable.VariableData("myVar", SymbolTable.InitialValueType.FloatingNumber, "42.11"));
        InstanceManager.getDefault(LogixNG_Manager.class)
                .setSymbolTable(new DefaultSymbolTable());
        InstanceManager.getDefault(LogixNG_Manager.class)
                .getSymbolTable().createSymbols(localVariables);
    }

    @After
    public void tearDown() {
        jmri.jmrit.logixng.util.LogixNG_ThreadingUtil.stopLogixNGThread();
        JUnitUtil.tearDown();
    }
    
}
