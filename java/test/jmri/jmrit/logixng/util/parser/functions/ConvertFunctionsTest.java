package jmri.jmrit.logixng.util.parser.functions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

import jmri.jmrit.logixng.SymbolTable;
import jmri.jmrit.logixng.implementation.DefaultConditionalNG;
import jmri.jmrit.logixng.implementation.DefaultSymbolTable;
import jmri.jmrit.logixng.util.LogixNG_Thread;
import jmri.jmrit.logixng.util.parser.ExpressionNode;
import jmri.jmrit.logixng.util.parser.ExpressionNodeFloatingNumber;
import jmri.jmrit.logixng.util.parser.ExpressionNodeString;
import jmri.jmrit.logixng.util.parser.ExpressionNodeTrue;
import jmri.jmrit.logixng.util.parser.Token;
import jmri.jmrit.logixng.util.parser.TokenType;
import jmri.jmrit.logixng.util.parser.WrongNumberOfParametersException;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test ConvertFunctions
 * 
 * @author Daniel Bergqvist 2020
 */
public class ConvertFunctionsTest {

    ExpressionNode expr_boolean_true = new ExpressionNodeTrue();
    ExpressionNode expr_str_HELLO = new ExpressionNodeString(new Token(TokenType.NONE, "hello", 0));
    ExpressionNode expr_str_RAD = new ExpressionNodeString(new Token(TokenType.NONE, "rad", 0));
    ExpressionNode expr_str_DEG = new ExpressionNodeString(new Token(TokenType.NONE, "deg", 0));
    ExpressionNode expr_str_0_34 = new ExpressionNodeString(new Token(TokenType.NONE, "0.34", 0));
    ExpressionNode expr0_34 = new ExpressionNodeFloatingNumber(new Token(TokenType.NONE, "0.34", 0));
    ExpressionNode expr0_95 = new ExpressionNodeFloatingNumber(new Token(TokenType.NONE, "0.95", 0));
    ExpressionNode expr12_34 = new ExpressionNodeFloatingNumber(new Token(TokenType.NONE, "12.34", 0));
    ExpressionNode expr25_46 = new ExpressionNodeFloatingNumber(new Token(TokenType.NONE, "25.46", 0));
    ExpressionNode expr12 = new ExpressionNodeFloatingNumber(new Token(TokenType.NONE, "12", 0));
    ExpressionNode expr23 = new ExpressionNodeFloatingNumber(new Token(TokenType.NONE, "23", 0));
    
    
    private List<ExpressionNode> getParameterList(ExpressionNode... exprNodes) {
        List<ExpressionNode> list = new ArrayList<>();
        Collections.addAll(list, exprNodes);
        return list;
    }
    
    @Test
    public void testBundle() {
        Assert.assertEquals("strings are equal",
                "Function \"sin\" has wrong number of parameters",
                Bundle.getMessage("WrongNumberOfParameters1", "sin"));
        Assert.assertEquals("strings are equal",
                "Function \"sin\" has wrong number of parameters",
                Bundle.getMessage(Locale.CANADA, "WrongNumberOfParameters1", "sin"));
        // Test Bundle.retry(Locale, String)
        Assert.assertEquals("strings matches","Item",Bundle.getMessage("CategoryItem"));
    }
    
    @Test
    public void testIntFunction() throws Exception {
        ConvertFunctions.IntFunction intFunction = new ConvertFunctions.IntFunction();
        Assert.assertEquals("strings matches", "int", intFunction.getName());
        Assert.assertEquals("strings matches", "int", new ConvertFunctions.IntFunction().getName());
        
        AtomicBoolean hasThrown = new AtomicBoolean(false);
        
        SymbolTable symbolTable = new DefaultSymbolTable(new DefaultConditionalNG("IQC1", null));
        
        // Test unsupported token type
        hasThrown.set(false);
        try {
            intFunction.calculate(symbolTable, getParameterList());
        } catch (WrongNumberOfParametersException e) {
            hasThrown.set(true);
        }
        Assert.assertTrue("exception is thrown", hasThrown.get());
        
        Assert.assertEquals("numbers are equal", 12, intFunction.calculate(symbolTable, getParameterList(expr12_34)));
        
        // Test unsupported token type
        hasThrown.set(false);
        try {
            intFunction.calculate(symbolTable, getParameterList(expr12_34, expr25_46));
        } catch (WrongNumberOfParametersException e) {
            hasThrown.set(true);
        }
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
