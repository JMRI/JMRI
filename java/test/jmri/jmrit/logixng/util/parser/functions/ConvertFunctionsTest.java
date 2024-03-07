package jmri.jmrit.logixng.util.parser.functions;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import jmri.InstanceManager;
import jmri.jmrit.logixng.SymbolTable;
import jmri.jmrit.logixng.implementation.DefaultConditionalNG;
import jmri.jmrit.logixng.implementation.DefaultSymbolTable;
import jmri.jmrit.logixng.util.LogixNG_Thread;
import jmri.jmrit.logixng.util.parser.*;
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
    ExpressionNode expr0 = new ExpressionNodeFloatingNumber(new Token(TokenType.NONE, "0", 0));
    ExpressionNode expr0_34 = new ExpressionNodeFloatingNumber(new Token(TokenType.NONE, "0.34", 0));
    ExpressionNode expr0_95 = new ExpressionNodeFloatingNumber(new Token(TokenType.NONE, "0.95", 0));
    ExpressionNode expr12_34 = new ExpressionNodeFloatingNumber(new Token(TokenType.NONE, "12.34", 0));
    ExpressionNode expr25_46 = new ExpressionNodeFloatingNumber(new Token(TokenType.NONE, "25.46", 0));
    ExpressionNode expr12 = new ExpressionNodeIntegerNumber(new Token(TokenType.NONE, "12", 0));
    ExpressionNode expr23 = new ExpressionNodeIntegerNumber(new Token(TokenType.NONE, "23", 0));
    ExpressionNode expr2FA5 = new ExpressionNodeString(new Token(TokenType.NONE, "2FA5", 0));
    ExpressionNode exprC352 = new ExpressionNodeString(new Token(TokenType.NONE, "c352", 0));
    ExpressionNode exprTrue = new ExpressionNodeTrue();
    ExpressionNode exprFalse = new ExpressionNodeFalse();


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
        Assert.assertNotNull(Bundle.getMessage("Convert.bool"));
        Assert.assertNotNull(Bundle.getMessage("Convert.int"));
        Assert.assertNotNull(Bundle.getMessage("Convert.float"));
        Assert.assertNotNull(Bundle.getMessage("Convert.str_Descr"));
        Assert.assertNotNull(Bundle.getMessage("Convert.hex2dec"));
    }

    @Test
    public void testIsIntFunction() throws Exception {
        Function isIntFunction = InstanceManager.getDefault(FunctionManager.class).get("isInt");
        Assert.assertEquals("strings matches", "isInt", isIntFunction.getName());

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
        JUnitAppender.assertWarnMessage("the string \"hello\" cannot be converted to a number");

        Assert.assertFalse((boolean)isIntFunction.calculate(symbolTable, getParameterList(expr_str_RAD)));
        JUnitAppender.assertWarnMessage("the string \"rad\" cannot be converted to a number");

        Assert.assertFalse((boolean)isIntFunction.calculate(symbolTable, getParameterList(expr_str_DEG)));
        JUnitAppender.assertWarnMessage("the string \"deg\" cannot be converted to a number");

        Assert.assertFalse((boolean)isIntFunction.calculate(symbolTable, getParameterList(expr_str_0_34)));
        JUnitAppender.assertWarnMessage("the string \"0.34\" cannot be converted to a number");

        Assert.assertFalse((boolean)isIntFunction.calculate(symbolTable, getParameterList(expr0_34)));
        Assert.assertFalse((boolean)isIntFunction.calculate(symbolTable, getParameterList(expr0_95)));
        Assert.assertFalse((boolean)isIntFunction.calculate(symbolTable, getParameterList(expr12_34)));
        Assert.assertFalse((boolean)isIntFunction.calculate(symbolTable, getParameterList(expr25_46)));
        Assert.assertTrue((boolean)isIntFunction.calculate(symbolTable, getParameterList(expr12)));
        Assert.assertTrue((boolean)isIntFunction.calculate(symbolTable, getParameterList(expr23)));
        Assert.assertFalse((boolean)isIntFunction.calculate(symbolTable, getParameterList(expr2FA5)));
        JUnitAppender.assertWarnMessage("the string \"2FA5\" cannot be converted to a number");

        Assert.assertFalse((boolean)isIntFunction.calculate(symbolTable, getParameterList(exprC352)));
        JUnitAppender.assertWarnMessage("the string \"c352\" cannot be converted to a number");

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
        Function isFloatFunction = InstanceManager.getDefault(FunctionManager.class).get("isFloat");
        Assert.assertEquals("strings matches", "isFloat", isFloatFunction.getName());

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
    public void testBoolFunction() throws Exception {
        Function boolFunction = InstanceManager.getDefault(FunctionManager.class).get("bool");
        Assert.assertEquals("strings matches", "bool", boolFunction.getName());

        AtomicBoolean hasThrown = new AtomicBoolean(false);

        SymbolTable symbolTable = new DefaultSymbolTable(new DefaultConditionalNG("IQC1", null));

        // Test unsupported token type
        hasThrown.set(false);
        try {
            boolFunction.calculate(symbolTable, getParameterList());
        } catch (WrongNumberOfParametersException e) {
            hasThrown.set(true);
        }
        Assert.assertTrue("exception is thrown", hasThrown.get());

        Assert.assertTrue("result is true", (boolean)boolFunction.calculate(symbolTable, getParameterList(expr12)));
        Assert.assertTrue("result is true", (boolean)boolFunction.calculate(symbolTable, getParameterList(expr12_34)));
        Assert.assertFalse("result is false", (boolean)boolFunction.calculate(symbolTable, getParameterList(expr0)));
        Assert.assertFalse("result is false", (boolean)boolFunction.calculate(symbolTable, getParameterList(expr0_34)));
        Assert.assertTrue("result is true", (boolean)boolFunction.calculate(symbolTable, getParameterList(exprTrue)));
        Assert.assertFalse("result is false", (boolean)boolFunction.calculate(symbolTable, getParameterList(exprFalse)));

        // Test unsupported token type
        hasThrown.set(false);
        try {
            boolFunction.calculate(symbolTable, getParameterList(expr12_34, expr25_46));
        } catch (WrongNumberOfParametersException e) {
            hasThrown.set(true);
        }

        // Test array
        hasThrown.set(false);
        try {
            boolFunction.calculate(symbolTable, getParameterList(
                    new ExpressionNodeConstantScaffold(new String[]{"Red", "Green"})));
        } catch (IllegalArgumentException e) {
            hasThrown.set(true);
        }
        Assert.assertTrue("exception is thrown", hasThrown.get());
    }

    @Test
    public void testBoolJythonFunction() throws Exception {
        Function boolJythonFunction = InstanceManager.getDefault(FunctionManager.class).get("boolJython");
        Assert.assertEquals("strings matches", "boolJython", boolJythonFunction.getName());

        AtomicBoolean hasThrown = new AtomicBoolean(false);

        SymbolTable symbolTable = new DefaultSymbolTable(new DefaultConditionalNG("IQC1", null));

        // Test unsupported token type
        hasThrown.set(false);
        try {
            boolJythonFunction.calculate(symbolTable, getParameterList());
        } catch (WrongNumberOfParametersException e) {
            hasThrown.set(true);
        }
        Assert.assertTrue("exception is thrown", hasThrown.get());

        Assert.assertTrue("result is true", (boolean)boolJythonFunction.calculate(symbolTable, getParameterList(expr12)));
        Assert.assertTrue("result is true", (boolean)boolJythonFunction.calculate(symbolTable, getParameterList(expr12_34)));
        Assert.assertFalse("result is false", (boolean)boolJythonFunction.calculate(symbolTable, getParameterList(expr0)));
        Assert.assertFalse("result is false", (boolean)boolJythonFunction.calculate(symbolTable, getParameterList(expr0_34)));
        Assert.assertTrue("result is true", (boolean)boolJythonFunction.calculate(symbolTable, getParameterList(exprTrue)));
        Assert.assertFalse("result is false", (boolean)boolJythonFunction.calculate(symbolTable, getParameterList(exprFalse)));

        Assert.assertFalse("result is false", (boolean)boolJythonFunction.calculate(symbolTable, getParameterList(
                new ExpressionNodeConstantScaffold(new String[]{}))));
        Assert.assertTrue("result is true", (boolean)boolJythonFunction.calculate(symbolTable, getParameterList(
                new ExpressionNodeConstantScaffold(new String[]{"Red", "Green"}))));

        List<String> list = new ArrayList<>();
        Assert.assertFalse("result is false", (boolean)boolJythonFunction.calculate(symbolTable, getParameterList(
                new ExpressionNodeConstantScaffold(list))));
        list.add("Blue");
        Assert.assertTrue("result is true", (boolean)boolJythonFunction.calculate(symbolTable, getParameterList(
                new ExpressionNodeConstantScaffold(list))));

        Set<String> set = new HashSet<>();
        Assert.assertFalse("result is false", (boolean)boolJythonFunction.calculate(symbolTable, getParameterList(
                new ExpressionNodeConstantScaffold(set))));
        set.add("Green");
        Assert.assertTrue("result is true", (boolean)boolJythonFunction.calculate(symbolTable, getParameterList(
                new ExpressionNodeConstantScaffold(set))));

        Map<String, Integer> map = new HashMap<>();
        Assert.assertFalse("result is false", (boolean)boolJythonFunction.calculate(symbolTable, getParameterList(
                new ExpressionNodeConstantScaffold(map))));
        map.put("Red", 2);
        Assert.assertTrue("result is true", (boolean)boolJythonFunction.calculate(symbolTable, getParameterList(
                new ExpressionNodeConstantScaffold(map))));

        // Test unsupported token type
        hasThrown.set(false);
        try {
            boolJythonFunction.calculate(symbolTable, getParameterList(expr12_34, expr25_46));
        } catch (WrongNumberOfParametersException e) {
            hasThrown.set(true);
        }
    }

    @Test
    public void testIntFunction() throws Exception {
        Function intFunction = InstanceManager.getDefault(FunctionManager.class).get("int");
        Assert.assertEquals("strings matches", "int", intFunction.getName());

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

        Assert.assertEquals("numbers are equal", 12L, intFunction.calculate(symbolTable, getParameterList(expr12_34)));

        // Test unsupported token type
        hasThrown.set(false);
        try {
            intFunction.calculate(symbolTable, getParameterList(expr12_34, expr25_46));
        } catch (WrongNumberOfParametersException e) {
            hasThrown.set(true);
        }
    }

    @Test
    public void testFloatFunction() throws Exception {
        Function floatFunction = InstanceManager.getDefault(FunctionManager.class).get("float");
        Assert.assertEquals("strings matches", "float", floatFunction.getName());

        AtomicBoolean hasThrown = new AtomicBoolean(false);

        SymbolTable symbolTable = new DefaultSymbolTable(new DefaultConditionalNG("IQC1", null));

        // Test unsupported token type
        hasThrown.set(false);
        try {
            floatFunction.calculate(symbolTable, getParameterList());
        } catch (WrongNumberOfParametersException e) {
            hasThrown.set(true);
        }
        Assert.assertTrue("exception is thrown", hasThrown.get());

        Assert.assertEquals("numbers are equal", 12.34, floatFunction.calculate(symbolTable, getParameterList(expr12_34)));

        // Test unsupported token type
        hasThrown.set(false);
        try {
            floatFunction.calculate(symbolTable, getParameterList(expr12_34, expr25_46));
        } catch (WrongNumberOfParametersException e) {
            hasThrown.set(true);
        }
    }

    @Test
    public void testStrFunction() throws Exception {
        Function strFunction = InstanceManager.getDefault(FunctionManager.class).get("str");
        Assert.assertEquals("strings matches", "str", strFunction.getName());

        AtomicBoolean hasThrown = new AtomicBoolean(false);

        SymbolTable symbolTable = new DefaultSymbolTable(new DefaultConditionalNG("IQC1", null));

        // Test unsupported token type
        hasThrown.set(false);
        try {
            strFunction.calculate(symbolTable, getParameterList());
        } catch (WrongNumberOfParametersException e) {
            hasThrown.set(true);
        }
        Assert.assertTrue("exception is thrown", hasThrown.get());

        Assert.assertEquals("strings are equal", "12", strFunction.calculate(symbolTable, getParameterList(expr12)));
        Assert.assertEquals("strings are equal", "12.34", strFunction.calculate(symbolTable, getParameterList(expr12_34)));

        Assert.assertEquals("Strings are equal", "Blue",
                strFunction.calculate(symbolTable, getParameterList(new ExpressionNodeConstantScaffold("Blue"))));
        Assert.assertTrue(((String)strFunction.calculate(symbolTable, getParameterList(
                new ExpressionNodeConstantScaffold(new String[0])))).startsWith("[Ljava.lang.String;@"));
        Assert.assertTrue(((String)strFunction.calculate(symbolTable, getParameterList(
                new ExpressionNodeConstantScaffold(new String[]{"Blue", "Red"})))).startsWith("[Ljava.lang.String;@"));

        List<String> list = new ArrayList<>();
        Assert.assertEquals("Strings are equal", "[]", strFunction.calculate(symbolTable, getParameterList(
                new ExpressionNodeConstantScaffold(list))));
        list.add("Blue");
        list.add("Green");
        Assert.assertEquals("Strings are equal", "[Blue, Green]", strFunction.calculate(symbolTable, getParameterList(
                new ExpressionNodeConstantScaffold(list))));

        Set<String> set = new HashSet<>();
        Assert.assertEquals("Strings are equal", "[]", strFunction.calculate(symbolTable, getParameterList(
                new ExpressionNodeConstantScaffold(set))));
        set.add("Green");
        set.add("Yellow");
        Assert.assertEquals("Strings are equal", "[Yellow, Green]", strFunction.calculate(symbolTable, getParameterList(
                new ExpressionNodeConstantScaffold(set))));

        Map<String, Integer> map = new HashMap<>();
        Assert.assertEquals("Strings are equal", "{}", strFunction.calculate(symbolTable, getParameterList(
                new ExpressionNodeConstantScaffold(map))));
        map.put("Red", 2);
        map.put("Green", 4);
        Assert.assertEquals("Strings are equal", "{Red=2, Green=4}", strFunction.calculate(symbolTable, getParameterList(
                new ExpressionNodeConstantScaffold(map))));

        // Test unsupported token type
        hasThrown.set(false);
        try {
            strFunction.calculate(symbolTable, getParameterList(expr12_34, expr25_46));
        } catch (WrongNumberOfParametersException e) {
            hasThrown.set(true);
        }
    }

    @Test
    public void testHex2DecFunction() throws Exception {
        Function hex2DecFunction = InstanceManager.getDefault(FunctionManager.class).get("hex2dec");
        Assert.assertEquals("strings matches", "hex2dec", hex2DecFunction.getName());

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
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
