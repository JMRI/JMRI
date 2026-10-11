package jmri.jmrit.logixng.util.parser.functions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.*;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.jmrit.logixng.SymbolTable;
import jmri.jmrit.logixng.implementation.DefaultConditionalNG;
import jmri.jmrit.logixng.implementation.DefaultSymbolTable;
import jmri.jmrit.logixng.util.LogixNG_Thread;
import jmri.jmrit.logixng.util.parser.*;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Test ConvertFunctions
 *
 * @author Daniel Bergqvist 2020
 */
public class ConvertFunctionsTest {

    private final ExpressionNode expr_boolean_true = new ExpressionNodeTrue();
    private final ExpressionNode expr_str_HELLO = new ExpressionNodeString(new Token(TokenType.NONE, "hello", 0));
    private final ExpressionNode expr_str_RAD = new ExpressionNodeString(new Token(TokenType.NONE, "rad", 0));
    private final ExpressionNode expr_str_DEG = new ExpressionNodeString(new Token(TokenType.NONE, "deg", 0));
    private final ExpressionNode expr_str_0_34 = new ExpressionNodeString(new Token(TokenType.NONE, "0.34", 0));
    private final ExpressionNode expr0 = new ExpressionNodeFloatingNumber(new Token(TokenType.NONE, "0", 0));
    private final ExpressionNode expr0_34 = new ExpressionNodeFloatingNumber(new Token(TokenType.NONE, "0.34", 0));
    private final ExpressionNode expr0_95 = new ExpressionNodeFloatingNumber(new Token(TokenType.NONE, "0.95", 0));
    private final ExpressionNode expr12_34 = new ExpressionNodeFloatingNumber(new Token(TokenType.NONE, "12.34", 0));
    private final ExpressionNode expr25_46 = new ExpressionNodeFloatingNumber(new Token(TokenType.NONE, "25.46", 0));
    private final ExpressionNode expr12 = new ExpressionNodeIntegerNumber(new Token(TokenType.NONE, "12", 0));
    private final ExpressionNode expr23 = new ExpressionNodeIntegerNumber(new Token(TokenType.NONE, "23", 0));
    private final ExpressionNode expr2FA5 = new ExpressionNodeString(new Token(TokenType.NONE, "2FA5", 0));
    private final ExpressionNode exprC352 = new ExpressionNodeString(new Token(TokenType.NONE, "c352", 0));
    private final ExpressionNode exprTrue = new ExpressionNodeTrue();
    private final ExpressionNode exprFalse = new ExpressionNodeFalse();


    private List<ExpressionNode> getParameterList(ExpressionNode... exprNodes) {
        List<ExpressionNode> list = new ArrayList<>();
        Collections.addAll(list, exprNodes);
        return list;
    }

    @Test
    public void testBundle() {
        assertEquals( "Function \"sin\" has wrong number of parameters",
                Bundle.getMessage("WrongNumberOfParameters1", "sin"),
                "strings are equal");
        assertEquals( "Function \"sin\" has wrong number of parameters",
                Bundle.getMessage(Locale.CANADA, "WrongNumberOfParameters1", "sin"),
                "strings are equal");
        // Test Bundle.retry(Locale, String)
        assertEquals( "Item",Bundle.getMessage("CategoryItem"), "strings matches");

        assertNotNull(Bundle.getMessage("Convert.isInt"));
        assertNotNull(Bundle.getMessage("Convert.isFloat"));
        assertNotNull(Bundle.getMessage("Convert.bool"));
        assertNotNull(Bundle.getMessage("Convert.int"));
        assertNotNull(Bundle.getMessage("Convert.float"));
        assertNotNull(Bundle.getMessage("Convert.str_Descr"));
        assertNotNull(Bundle.getMessage("Convert.hex2dec"));
    }

    @Test
    public void testIsIntFunction() throws JmriException {
        Function isIntFunction = InstanceManager.getDefault(FunctionManager.class).get("isInt");
        assertEquals( "isInt", isIntFunction.getName(), "strings matches");

        SymbolTable symbolTable = new DefaultSymbolTable(new DefaultConditionalNG("IQC1", null));

        // Test unsupported token type
        WrongNumberOfParametersException e = assertThrows( WrongNumberOfParametersException.class, () ->
            isIntFunction.calculate(symbolTable, getParameterList()),
                    "exception is thrown");
        assertNotNull(e);

        assertFalse((boolean)isIntFunction.calculate(symbolTable, getParameterList(expr_boolean_true)));
        assertFalse((boolean)isIntFunction.calculate(symbolTable, getParameterList(expr_str_HELLO)));
        JUnitAppender.assertWarnMessage("the string \"hello\" cannot be converted to a number");

        assertFalse((boolean)isIntFunction.calculate(symbolTable, getParameterList(expr_str_RAD)));
        JUnitAppender.assertWarnMessage("the string \"rad\" cannot be converted to a number");

        assertFalse((boolean)isIntFunction.calculate(symbolTable, getParameterList(expr_str_DEG)));
        JUnitAppender.assertWarnMessage("the string \"deg\" cannot be converted to a number");

        assertFalse((boolean)isIntFunction.calculate(symbolTable, getParameterList(expr_str_0_34)));
        JUnitAppender.assertWarnMessage("the string \"0.34\" cannot be converted to a number");

        assertFalse((boolean)isIntFunction.calculate(symbolTable, getParameterList(expr0_34)));
        assertFalse((boolean)isIntFunction.calculate(symbolTable, getParameterList(expr0_95)));
        assertFalse((boolean)isIntFunction.calculate(symbolTable, getParameterList(expr12_34)));
        assertFalse((boolean)isIntFunction.calculate(symbolTable, getParameterList(expr25_46)));
        assertTrue((boolean)isIntFunction.calculate(symbolTable, getParameterList(expr12)));
        assertTrue((boolean)isIntFunction.calculate(symbolTable, getParameterList(expr23)));
        assertFalse((boolean)isIntFunction.calculate(symbolTable, getParameterList(expr2FA5)));
        JUnitAppender.assertWarnMessage("the string \"2FA5\" cannot be converted to a number");

        assertFalse((boolean)isIntFunction.calculate(symbolTable, getParameterList(exprC352)));
        JUnitAppender.assertWarnMessage("the string \"c352\" cannot be converted to a number");

        // Test wrong number of parameters
        e = assertThrows( WrongNumberOfParametersException.class, () ->
            isIntFunction.calculate(symbolTable, getParameterList(expr12_34, expr25_46)));
        assertNotNull(e);
    }

    @Test
    public void testIsFloatFunction() throws JmriException {
        Function isFloatFunction = InstanceManager.getDefault(FunctionManager.class).get("isFloat");
        assertEquals( "isFloat", isFloatFunction.getName(), "strings matches");

        SymbolTable symbolTable = new DefaultSymbolTable(new DefaultConditionalNG("IQC1", null));

        // Test unsupported token type
        WrongNumberOfParametersException e = assertThrows( WrongNumberOfParametersException.class, () ->
            isFloatFunction.calculate(symbolTable, getParameterList()),
                "exception is thrown");
        assertNotNull(e);

        assertFalse((boolean)isFloatFunction.calculate(symbolTable, getParameterList(expr_boolean_true)));
        assertFalse((boolean)isFloatFunction.calculate(symbolTable, getParameterList(expr_str_HELLO)));
        JUnitAppender.assertWarnMessageStartingWith("the string \"hello\" cannot be converted to a number");

        assertFalse((boolean)isFloatFunction.calculate(symbolTable, getParameterList(expr_str_RAD)));
        JUnitAppender.assertWarnMessageStartingWith("the string \"rad\" cannot be converted to a number");

        assertFalse((boolean)isFloatFunction.calculate(symbolTable, getParameterList(expr_str_DEG)));
        JUnitAppender.assertWarnMessageStartingWith("the string \"deg\" cannot be converted to a number");

        assertTrue((boolean)isFloatFunction.calculate(symbolTable, getParameterList(expr_str_0_34)));
        assertTrue((boolean)isFloatFunction.calculate(symbolTable, getParameterList(expr0_34)));
        assertTrue((boolean)isFloatFunction.calculate(symbolTable, getParameterList(expr0_95)));
        assertTrue((boolean)isFloatFunction.calculate(symbolTable, getParameterList(expr12_34)));
        assertTrue((boolean)isFloatFunction.calculate(symbolTable, getParameterList(expr25_46)));
        assertTrue((boolean)isFloatFunction.calculate(symbolTable, getParameterList(expr12)));
        assertTrue((boolean)isFloatFunction.calculate(symbolTable, getParameterList(expr23)));
        assertFalse((boolean)isFloatFunction.calculate(symbolTable, getParameterList(expr2FA5)));
        JUnitAppender.assertWarnMessageStartingWith("the string \"2FA5\" cannot be converted to a number");

        assertFalse((boolean)isFloatFunction.calculate(symbolTable, getParameterList(exprC352)));
        JUnitAppender.assertWarnMessageStartingWith("the string \"c352\" cannot be converted to a number");

        // Test wrong number of parameters
        //e = assertThrows ( WrongNumberOfParametersException.class, () ->
            isFloatFunction.calculate(symbolTable, getParameterList(expr12_34, expr25_46));
        //assertNotNull(e);
    }

    @Test
    public void testBoolFunction() throws JmriException {
        Function boolFunction = InstanceManager.getDefault(FunctionManager.class).get("bool");
        assertEquals( "bool", boolFunction.getName(), "strings matches");


        SymbolTable symbolTable = new DefaultSymbolTable(new DefaultConditionalNG("IQC1", null));

        // Test unsupported token type
        WrongNumberOfParametersException e = assertThrows( WrongNumberOfParametersException.class, () ->
            boolFunction.calculate(symbolTable, getParameterList()),
                "exception is thrown");
        assertNotNull(e);


        assertTrue( (boolean)boolFunction.calculate(symbolTable, getParameterList(expr12)), "result is true");
        assertTrue( (boolean)boolFunction.calculate(symbolTable, getParameterList(expr12_34)), "result is true");
        assertFalse( (boolean)boolFunction.calculate(symbolTable, getParameterList(expr0)), "result is false");
        assertFalse( (boolean)boolFunction.calculate(symbolTable, getParameterList(expr0_34)), "result is false");
        assertTrue( (boolean)boolFunction.calculate(symbolTable, getParameterList(exprTrue)), "result is true");
        assertFalse( (boolean)boolFunction.calculate(symbolTable, getParameterList(exprFalse)), "result is false");

        // Test unsupported token type
        //e = assertThrows( WrongNumberOfParametersException.class, () ->
            boolFunction.calculate(symbolTable, getParameterList(expr12_34, expr25_46));
        //        "exception is thrown");
        //assertNotNull(e);

        // Test array
        IllegalArgumentException ex = assertThrows( IllegalArgumentException.class, () ->
            boolFunction.calculate(symbolTable, getParameterList(
                    new ExpressionNodeConstantScaffold(new String[]{"Red", "Green"}))),
                "exception is thrown");
        assertNotNull(ex);
    }

    @Test
    public void testBoolJythonFunction() throws JmriException {
        Function boolJythonFunction = InstanceManager.getDefault(FunctionManager.class).get("boolJython");
        assertEquals( "boolJython", boolJythonFunction.getName(), "strings matches");


        SymbolTable symbolTable = new DefaultSymbolTable(new DefaultConditionalNG("IQC1", null));

        // Test unsupported token type
        WrongNumberOfParametersException e = assertThrows( WrongNumberOfParametersException.class, () ->
            boolJythonFunction.calculate(symbolTable, getParameterList()),
                "exception is thrown");
        assertNotNull(e);

        assertTrue( (boolean)boolJythonFunction.calculate(symbolTable, getParameterList(expr12)), "result is true");
        assertTrue( (boolean)boolJythonFunction.calculate(symbolTable, getParameterList(expr12_34)), "result is true");
        assertFalse( (boolean)boolJythonFunction.calculate(symbolTable, getParameterList(expr0)), "result is false");
        assertFalse( (boolean)boolJythonFunction.calculate(symbolTable, getParameterList(expr0_34)), "result is false");
        assertTrue( (boolean)boolJythonFunction.calculate(symbolTable, getParameterList(exprTrue)), "result is true");
        assertFalse( (boolean)boolJythonFunction.calculate(symbolTable, getParameterList(exprFalse)), "result is false");

        assertFalse( (boolean)boolJythonFunction.calculate(symbolTable, getParameterList(
                new ExpressionNodeConstantScaffold(new String[]{}))), "result is false");
        assertTrue( (boolean)boolJythonFunction.calculate(symbolTable, getParameterList(
                new ExpressionNodeConstantScaffold(new String[]{"Red", "Green"}))), "result is true");

        List<String> list = new ArrayList<>();
        assertFalse( (boolean)boolJythonFunction.calculate(symbolTable, getParameterList(
                new ExpressionNodeConstantScaffold(list))), "result is false");
        list.add("Blue");
        assertTrue( (boolean)boolJythonFunction.calculate(symbolTable, getParameterList(
                new ExpressionNodeConstantScaffold(list))), "result is true");

        Set<String> set = new HashSet<>();
        assertFalse( (boolean)boolJythonFunction.calculate(symbolTable, getParameterList(
                new ExpressionNodeConstantScaffold(set))), "result is false");
        set.add("Green");
        assertTrue( (boolean)boolJythonFunction.calculate(symbolTable, getParameterList(
                new ExpressionNodeConstantScaffold(set))), "result is true");

        Map<String, Integer> map = new HashMap<>();
        assertFalse( (boolean)boolJythonFunction.calculate(symbolTable, getParameterList(
                new ExpressionNodeConstantScaffold(map))), "result is false");
        map.put("Red", 2);
        assertTrue( (boolean)boolJythonFunction.calculate(symbolTable, getParameterList(
                new ExpressionNodeConstantScaffold(map))), "result is true");

        // Test unsupported token type
        //e = assertThrows( WrongNumberOfParametersException.class, () ->
            boolJythonFunction.calculate(symbolTable, getParameterList(expr12_34, expr25_46));
        //        "exception is thrown");
        //assertNotNull(e);

    }

    @Test
    public void testIntFunction() throws JmriException {
        Function intFunction = InstanceManager.getDefault(FunctionManager.class).get("int");
        assertEquals( "int", intFunction.getName(), "strings matches");


        SymbolTable symbolTable = new DefaultSymbolTable(new DefaultConditionalNG("IQC1", null));

        // Test unsupported token type
        WrongNumberOfParametersException e = assertThrows( WrongNumberOfParametersException.class, () ->
            intFunction.calculate(symbolTable, getParameterList()),
                "exception is thrown");
        assertNotNull(e);

        assertEquals( 12L, intFunction.calculate(symbolTable, getParameterList(expr12_34)),
                "numbers are equal");

        // Test unsupported token type
        e = assertThrows( WrongNumberOfParametersException.class, () ->
            intFunction.calculate(symbolTable, getParameterList(expr12_34, expr25_46)),
                "exception is thrown");
        assertNotNull(e);

    }

    @Test
    public void testFloatFunction() throws JmriException {
        Function floatFunction = InstanceManager.getDefault(FunctionManager.class).get("float");
        assertEquals( "float", floatFunction.getName(), "strings matches");


        SymbolTable symbolTable = new DefaultSymbolTable(new DefaultConditionalNG("IQC1", null));

        // Test unsupported token type
        WrongNumberOfParametersException e = assertThrows( WrongNumberOfParametersException.class, () ->
            floatFunction.calculate(symbolTable, getParameterList()),
                "exception is thrown");
        assertNotNull(e);

        assertEquals( 12.34, floatFunction.calculate(symbolTable, getParameterList(expr12_34)),
                "numbers are equal");

        // Test unsupported token type
        // e = assertThrows( WrongNumberOfParametersException.class, () ->
            floatFunction.calculate(symbolTable, getParameterList(expr12_34, expr25_46));
        //        "exception is thrown");
        // assertNotNull(e);

    }

    @Test
    public void testStrFunction() throws JmriException {
        Function strFunction = InstanceManager.getDefault(FunctionManager.class).get("str");
        assertEquals( "str", strFunction.getName(), "strings matches");


        SymbolTable symbolTable = new DefaultSymbolTable(new DefaultConditionalNG("IQC1", null));

        // Test unsupported token type
        WrongNumberOfParametersException e = assertThrows( WrongNumberOfParametersException.class, () ->
            strFunction.calculate(symbolTable, getParameterList()),
                "exception is thrown");
        assertNotNull(e);

        assertEquals( "12", strFunction.calculate(symbolTable, getParameterList(expr12)),
                "strings are equal");
        assertEquals( "12.34", strFunction.calculate(symbolTable, getParameterList(expr12_34)),
                "strings are equal");

        assertEquals( "Blue",
                strFunction.calculate(symbolTable, getParameterList(new ExpressionNodeConstantScaffold("Blue"))),
                "Strings are equal");
        assertTrue(((String)strFunction.calculate(symbolTable, getParameterList(
                new ExpressionNodeConstantScaffold(new String[0])))).startsWith("[Ljava.lang.String;@"));
        assertTrue(((String)strFunction.calculate(symbolTable, getParameterList(
                new ExpressionNodeConstantScaffold(new String[]{"Blue", "Red"})))).startsWith("[Ljava.lang.String;@"));

        List<String> list = new ArrayList<>();
        assertEquals( "[]", strFunction.calculate(symbolTable, getParameterList(
                new ExpressionNodeConstantScaffold(list))),
                "Strings are equal");
        list.add("Blue");
        list.add("Green");
        assertEquals( "[Blue, Green]", strFunction.calculate(symbolTable, getParameterList(
                new ExpressionNodeConstantScaffold(list))), "Strings are equal");

        Set<String> set = new HashSet<>();
        assertEquals( "[]", strFunction.calculate(symbolTable, getParameterList(
                new ExpressionNodeConstantScaffold(set))), "Strings are equal");
        set.add("Green");
        set.add("Yellow");
        assertEquals( "[Yellow, Green]", strFunction.calculate(symbolTable, getParameterList(
                new ExpressionNodeConstantScaffold(set))), "Strings are equal");

        Map<String, Integer> map = new HashMap<>();
        assertEquals( "{}", strFunction.calculate(symbolTable, getParameterList(
                new ExpressionNodeConstantScaffold(map))), "Strings are equal");
        map.put("Red", 2);
        map.put("Green", 4);
        assertEquals( "{Red=2, Green=4}", strFunction.calculate(symbolTable, getParameterList(
                new ExpressionNodeConstantScaffold(map))), "Strings are equal");

        // Test unsupported token type
        //e = assertThrows( WrongNumberOfParametersException.class, () ->
            strFunction.calculate(symbolTable, getParameterList(expr12_34, expr25_46));
        //        "exception is thrown");
        // assertNotNull(e);
    }

    @Test
    public void testHex2DecFunction() throws JmriException {
        Function hex2DecFunction = InstanceManager.getDefault(FunctionManager.class).get("hex2dec");
        assertEquals( "hex2dec", hex2DecFunction.getName(), "strings matches");


        SymbolTable symbolTable = new DefaultSymbolTable(new DefaultConditionalNG("IQC1", null));

        // Test unsupported token type
        WrongNumberOfParametersException e = assertThrows( WrongNumberOfParametersException.class, () ->
            hex2DecFunction.calculate(symbolTable, getParameterList()),
                "exception is thrown");
        assertNotNull(e);

        assertEquals( 18L, hex2DecFunction.calculate(symbolTable, getParameterList(expr12)), "numbers are equal");
        assertEquals( 35L, hex2DecFunction.calculate(symbolTable, getParameterList(expr23)), "numbers are equal");
        assertEquals( 12197L, hex2DecFunction.calculate(symbolTable, getParameterList(expr2FA5)), "numbers are equal");
        assertEquals( 50002L, hex2DecFunction.calculate(symbolTable, getParameterList(exprC352)), "numbers are equal");

        // Test unsupported token type
        e = assertThrows( WrongNumberOfParametersException.class, () ->
            hex2DecFunction.calculate(symbolTable, getParameterList(expr12_34, expr25_46)),
                "exception is thrown");
        assertNotNull(e);
    }

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
