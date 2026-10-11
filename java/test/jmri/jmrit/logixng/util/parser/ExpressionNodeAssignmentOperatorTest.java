package jmri.jmrit.logixng.util.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.*;

import jmri.JmriException;
import jmri.jmrit.logixng.SymbolTable;
import jmri.jmrit.logixng.implementation.DefaultConditionalNG;
import jmri.jmrit.logixng.implementation.DefaultSymbolTable;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
        assertNotNull( t, "exists");


        // Left side must be assignable
        IllegalArgumentException e = assertThrows( IllegalArgumentException.class, () -> {
            var enao = new ExpressionNodeAssignmentOperator(TokenType.ASSIGN, expressionNumber, null);
            fail("should have thrown, not created " + enao);
        }, "exception is thrown");
        assertNotNull(e);

        // Left side must not be null
        e = assertThrows( IllegalArgumentException.class, () -> {
            var enao = new ExpressionNodeAssignmentOperator(TokenType.ASSIGN, null, null);
            fail("should have thrown, not created " + enao);
        }, "exception is thrown");
        assertNotNull(e);

        // Left side must not be null
        e = assertThrows( IllegalArgumentException.class, () -> {
            var enao = new ExpressionNodeAssignmentOperator(TokenType.ASSIGN, null, expressionNumber);
            fail("should have thrown, not created " + enao);
        }, "exception is thrown");
        assertNotNull(e);
    }

    private Object getVariable(SymbolTable symbolTable, String name) {
        return symbolTable.getValue(name);
    }

    private void setVariable(SymbolTable symbolTable, String name, Object value) {
        symbolTable.setValue(name, value);
    }

    @Test
    public void testCalculate() throws JmriException {

        SymbolTable symbolTable = new DefaultSymbolTable(new DefaultConditionalNG("IQC1", null));

        List<SymbolTable.VariableData> localVariables = new ArrayList<>();
        localVariables.add(new SymbolTable.VariableData("myVar", SymbolTable.InitialValueType.FloatingNumber, "42.11"));
        symbolTable.createSymbols(localVariables);

        Map<String, Variable> variables = new HashMap<>();
        ExpressionNodeIdentifier exprIdent = new ExpressionNodeIdentifier(new Token(TokenType.NONE, "myVar", 0), variables);

        ExpressionNode expr25_46 = new ExpressionNodeFloatingNumber(new Token(TokenType.NONE, "25.46", 0));
        ExpressionNode expr12 = new ExpressionNodeIntegerNumber(new Token(TokenType.NONE, "12", 0));

        setVariable(symbolTable, "myVar", 42.11);
        assertEquals( 25.46,
                (double)new ExpressionNodeAssignmentOperator(TokenType.ASSIGN, exprIdent, expr25_46).calculate(symbolTable),
                0.00000001, "calculate() gives the correct value");
        assertEquals( 25.46, (Double)getVariable(symbolTable, "myVar"), 0.00000001, "The variable has correct value");

        setVariable(symbolTable, "myVar", 42.11);
        assertEquals( 67.57,
                (double)new ExpressionNodeAssignmentOperator(TokenType.ASSIGN_ADD, exprIdent, expr25_46).calculate(symbolTable),
                0.00000001, "calculate() gives the correct value");
        assertEquals( 67.57, (Double)getVariable(symbolTable, "myVar"), 0.00000001, "The variable has correct value");

        setVariable(symbolTable, "myVar", 42.11);
        assertEquals( 16.65,
                (double)new ExpressionNodeAssignmentOperator(TokenType.ASSIGN_SUBTRACKT, exprIdent, expr25_46).calculate(symbolTable),
                0.00000001, "calculate() gives the correct value");
        assertEquals( 16.65, (Double)getVariable(symbolTable, "myVar"), 0.00000001, "The variable has correct value");

        setVariable(symbolTable, "myVar", 42.11);
        assertEquals( 1072.1206,
                (double)new ExpressionNodeAssignmentOperator(TokenType.ASSIGN_MULTIPLY, exprIdent, expr25_46).calculate(symbolTable),
                0.00000001, "calculate() gives the correct value");
        assertEquals( 1072.1206, (Double)getVariable(symbolTable, "myVar"), 0.00000001, "The variable has correct value");

        setVariable(symbolTable, "myVar", 42.11);
        assertEquals( 1.653967,
                (double)new ExpressionNodeAssignmentOperator(TokenType.ASSIGN_DIVIDE, exprIdent, expr25_46).calculate(symbolTable),
                0.00000001, "calculate() gives the correct value");
        assertEquals( 1.653967, (Double)getVariable(symbolTable, "myVar"), 0.00000001, "The variable has correct value");

        setVariable(symbolTable, "myVar", 532);
        assertEquals( 4,
                (long)new ExpressionNodeAssignmentOperator(TokenType.ASSIGN_MODULO, exprIdent, expr12).calculate(symbolTable),
                "calculate() gives the correct value");
        assertEquals( 4, (long)getVariable(symbolTable, "myVar"), "The variable has correct value");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.tearDown();
    }

}
