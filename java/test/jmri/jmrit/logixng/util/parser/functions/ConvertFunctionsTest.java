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
import jmri.jmrit.logixng.util.parser.ExpressionNodeIntegerNumber;
import jmri.jmrit.logixng.util.parser.ExpressionNodeFloatingNumber;
import jmri.jmrit.logixng.util.parser.ExpressionNodeString;
import jmri.jmrit.logixng.util.parser.ExpressionNodeTrue;
import jmri.jmrit.logixng.util.parser.Token;
import jmri.jmrit.logixng.util.parser.TokenType;
import jmri.jmrit.logixng.util.parser.WrongNumberOfParametersException;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

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
    ExpressionNode expr12 = new ExpressionNodeIntegerNumber(new Token(TokenType.NONE, "12", 0));
    ExpressionNode expr23 = new ExpressionNodeIntegerNumber(new Token(TokenType.NONE, "23", 0));
    ExpressionNode expr2FA5 = new ExpressionNodeString(new Token(TokenType.NONE, "2FA5", 0));
    ExpressionNode exprC352 = new ExpressionNodeString(new Token(TokenType.NONE, "c352", 0));
    
    
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
        
        Assert.assertNotNull(Bundle.getMessage("Convert.isInt"));
        Assert.assertNotNull(Bundle.getMessage("Convert.isFloat"));
        Assert.assertNotNull(Bundle.getMessage("Convert.int"));
        Assert.assertNotNull(Bundle.getMessage("Convert.float"));
        Assert.assertNotNull(Bundle.getMessage("Convert.str_Descr"));
        Assert.assertNotNull(Bundle.getMessage("Convert.hex2dec"));
    }
    
    @Test
    public void testIsIntFunction() throws Exception {
        ConvertFunctions.IsIntFunction isIntFunction = new ConvertFunctions.IsIntFunction();
        Assert.assertEquals("strings matches", "isInt", isIntFunction.getName());
        Assert.assertEquals("strings matches", "isInt", new ConvertFunctions.IsIntFunction().getName());
        
        AtomicBoolean hasThrown = new AtomicBoolean(false);
        
        SymbolTable symbolTable = new DefaultSymbolTable(new DefaultConditionalNG("IQC1", null));
        
        // Test unsupported token type
        hasThrown.set(false);
        try {
            isIntFunction.calculate(symbolTable, getParameterList());
        } catch (WrongNumberOfParametersException e) {
            hasThrown.set(true);
        }
        Assert.assertTrue("exception is thrown", hasThrown.get());
        
        Assert.assertFalse((boolean)isIntFunction.calculate(symbolTable, getParameterList(expr_boolean_true)));
        Assert.assertFalse((boolean)isIntFunction.calculate(symbolTable, getParameterList(expr_str_HELLO)));
        JUnitAppender.suppressWarnMessageStartsWith("the string \"hello\" cannot be converted to a number");
        
        Assert.assertFalse((boolean)isIntFunction.calculate(symbolTable, getParameterList(expr_str_RAD)));
        JUnitAppender.suppressWarnMessageStartsWith("the string \"rad\" cannot be converted to a number");
        
        Assert.assertFalse((boolean)isIntFunction.calculate(symbolTable, getParameterList(expr_str_DEG)));
        JUnitAppender.suppressWarnMessageStartsWith("the string \"deg\" cannot be converted to a number");
        
        Assert.assertFalse((boolean)isIntFunction.calculate(symbolTable, getParameterList(expr_str_0_34)));
        JUnitAppender.suppressWarnMessageStartsWith("the string \"0.34\" cannot be converted to a number");
        
        Assert.assertFalse((boolean)isIntFunction.calculate(symbolTable, getParameterList(expr0_34)));
        Assert.assertFalse((boolean)isIntFunction.calculate(symbolTable, getParameterList(expr0_95)));
        Assert.assertFalse((boolean)isIntFunction.calculate(symbolTable, getParameterList(expr12_34)));
        Assert.assertFalse((boolean)isIntFunction.calculate(symbolTable, getParameterList(expr25_46)));
        Assert.assertTrue((boolean)isIntFunction.calculate(symbolTable, getParameterList(expr12)));
        Assert.assertTrue((boolean)isIntFunction.calculate(symbolTable, getParameterList(expr23)));
        Assert.assertFalse((boolean)isIntFunction.calculate(symbolTable, getParameterList(expr2FA5)));
        JUnitAppender.suppressWarnMessageStartsWith("the string \"2FA5\" cannot be converted to a number");
        
        Assert.assertFalse((boolean)isIntFunction.calculate(symbolTable, getParameterList(exprC352)));
        JUnitAppender.suppressWarnMessageStartsWith("the string \"c352\" cannot be converted to a number");
        
        // Test wrong number of parameters
        hasThrown.set(false);
        try {
            isIntFunction.calculate(symbolTable, getParameterList(expr12_34, expr25_46));
        } catch (WrongNumberOfParametersException e) {
            hasThrown.set(true);
        }
    }
    
    @Test
    public void testIsFloatFunction() throws Exception {
        ConvertFunctions.IsFloatFunction isFloatFunction = new ConvertFunctions.IsFloatFunction();
        Assert.assertEquals("strings matches", "isFloat", isFloatFunction.getName());
        Assert.assertEquals("strings matches", "isFloat", new ConvertFunctions.IsFloatFunction().getName());
        
        AtomicBoolean hasThrown = new AtomicBoolean(false);
        
        SymbolTable symbolTable = new DefaultSymbolTable(new DefaultConditionalNG("IQC1", null));
        
        // Test unsupported token type
        hasThrown.set(false);
        try {
            isFloatFunction.calculate(symbolTable, getParameterList());
        } catch (WrongNumberOfParametersException e) {
            hasThrown.set(true);
        }
        Assert.assertTrue("exception is thrown", hasThrown.get());
        
        Assert.assertFalse((boolean)isFloatFunction.calculate(symbolTable, getParameterList(expr_boolean_true)));
        Assert.assertFalse((boolean)isFloatFunction.calculate(symbolTable, getParameterList(expr_str_HELLO)));
        JUnitAppender.assertWarnMessageStartingWith("the string \"hello\" cannot be converted to a number");
        
        Assert.assertFalse((boolean)isFloatFunction.calculate(symbolTable, getParameterList(expr_str_RAD)));
        JUnitAppender.assertWarnMessageStartingWith("the string \"rad\" cannot be converted to a number");
        
        Assert.assertFalse((boolean)isFloatFunction.calculate(symbolTable, getParameterList(expr_str_DEG)));
        JUnitAppender.assertWarnMessageStartingWith("the string \"deg\" cannot be converted to a number");
        
        Assert.assertTrue((boolean)isFloatFunction.calculate(symbolTable, getParameterList(expr_str_0_34)));
        Assert.assertTrue((boolean)isFloatFunction.calculate(symbolTable, getParameterList(expr0_34)));
        Assert.assertTrue((boolean)isFloatFunction.calculate(symbolTable, getParameterList(expr0_95)));
        Assert.assertTrue((boolean)isFloatFunction.calculate(symbolTable, getParameterList(expr12_34)));
        Assert.assertTrue((boolean)isFloatFunction.calculate(symbolTable, getParameterList(expr25_46)));
        Assert.assertTrue((boolean)isFloatFunction.calculate(symbolTable, getParameterList(expr12)));
        Assert.assertTrue((boolean)isFloatFunction.calculate(symbolTable, getParameterList(expr23)));
        Assert.assertFalse((boolean)isFloatFunction.calculate(symbolTable, getParameterList(expr2FA5)));
        JUnitAppender.assertWarnMessageStartingWith("the string \"2FA5\" cannot be converted to a number");
        
        Assert.assertFalse((boolean)isFloatFunction.calculate(symbolTable, getParameterList(exprC352)));
        JUnitAppender.assertWarnMessageStartingWith("the string \"c352\" cannot be converted to a number");
        
        // Test wrong number of parameters
        hasThrown.set(false);
        try {
            isFloatFunction.calculate(symbolTable, getParameterList(expr12_34, expr25_46));
        } catch (WrongNumberOfParametersException e) {
            hasThrown.set(true);
        }
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
    
    @Test
    public void testHex2DecFunction() throws Exception {
        ConvertFunctions.Hex2DecFunction hex2DecFunction = new ConvertFunctions.Hex2DecFunction();
        Assert.assertEquals("strings matches", "hex2dec", hex2DecFunction.getName());
        Assert.assertEquals("strings matches", "hex2dec", new ConvertFunctions.Hex2DecFunction().getName());
        
        AtomicBoolean hasThrown = new AtomicBoolean(false);
        
        SymbolTable symbolTable = new DefaultSymbolTable(new DefaultConditionalNG("IQC1", null));
        
        // Test unsupported token type
        hasThrown.set(false);
        try {
            hex2DecFunction.calculate(symbolTable, getParameterList());
        } catch (WrongNumberOfParametersException e) {
            hasThrown.set(true);
        }
        Assert.assertTrue("exception is thrown", hasThrown.get());
        
        Assert.assertEquals("numbers are equal", 18L, hex2DecFunction.calculate(symbolTable, getParameterList(expr12)));
        Assert.assertEquals("numbers are equal", 35L, hex2DecFunction.calculate(symbolTable, getParameterList(expr23)));
        Assert.assertEquals("numbers are equal", 12197L, hex2DecFunction.calculate(symbolTable, getParameterList(expr2FA5)));
        Assert.assertEquals("numbers are equal", 50002L, hex2DecFunction.calculate(symbolTable, getParameterList(exprC352)));
        
        // Test unsupported token type
        hasThrown.set(false);
        try {
            hex2DecFunction.calculate(symbolTable, getParameterList(expr12_34, expr25_46));
        } catch (WrongNumberOfParametersException e) {
            hasThrown.set(true);
        }
    }
    
    // The minimal setup for log4J
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
