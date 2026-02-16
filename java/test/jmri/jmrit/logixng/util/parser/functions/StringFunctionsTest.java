package jmri.jmrit.logixng.util.parser.functions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.*;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.jmrit.logixng.SymbolTable;
import jmri.jmrit.logixng.implementation.DefaultConditionalNG;
import jmri.jmrit.logixng.implementation.DefaultSymbolTable;
import jmri.jmrit.logixng.util.LogixNG_Thread;
import jmri.jmrit.logixng.util.parser.*;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test ParsedExpression
 *
 * @author Daniel Bergqvist 2020
 */
public class StringFunctionsTest {

    private final ExpressionNode expr_str_HELLO = new ExpressionNodeString(new Token(TokenType.NONE, "hello", 0));
    private final ExpressionNode expr0_95 = new ExpressionNodeFloatingNumber(new Token(TokenType.NONE, "0.95", 0));
    private final ExpressionNode expr12 = new ExpressionNodeIntegerNumber(new Token(TokenType.NONE, "12", 0));


    private List<ExpressionNode> getParameterList(ExpressionNode... exprNodes) {
        List<ExpressionNode> list = new ArrayList<>();
        Collections.addAll(list, exprNodes);
        return list;
    }

    @Test
    public void testFormatFunction() throws JmriException {
        Function formatFunction = InstanceManager.getDefault(FunctionManager.class).get("format");
        assertEquals( "format", formatFunction.getName(), "strings matches");

        SymbolTable symbolTable = new DefaultSymbolTable(new DefaultConditionalNG("IQC1", null));

        // Test unsupported token type
        WrongNumberOfParametersException e = assertThrows( WrongNumberOfParametersException.class, () ->
            formatFunction.calculate(symbolTable, getParameterList()),
                "exception is thrown");
        assertNotNull(e);

        assertEquals( "A format string",
                formatFunction.calculate(symbolTable, getParameterList(new ExpressionNodeString(
                        new Token(TokenType.NONE, "A format string", 0)))),
                "Strings are equal");

        assertEquals( "Number: 12, String: hello",
                formatFunction.calculate(symbolTable, getParameterList(new ExpressionNodeString(
                        new Token(TokenType.NONE, "Number: %d, String: %s", 0)),
                        expr12, expr_str_HELLO)),
                "Strings are equal");

        assertEquals( "Number: 012, String:   hello, Float:  0.9500",
                formatFunction.calculate(symbolTable, getParameterList(new ExpressionNodeString(
                        new Token(TokenType.NONE, "Number: %03d, String: %7s, Float: %7.4f", 0)),
                        expr12, expr_str_HELLO, expr0_95)),
                "Strings are equal");
    }

    @Test
    public void testStrLenFunction() throws JmriException {
        Function strlenFunction = InstanceManager.getDefault(FunctionManager.class).get("strlen");
        assertEquals( "strlen", strlenFunction.getName(), "strings matches");

        SymbolTable symbolTable = new DefaultSymbolTable(new DefaultConditionalNG("IQC1", null));

        // Test unsupported token type
        WrongNumberOfParametersException e = assertThrows(WrongNumberOfParametersException.class, () ->
            strlenFunction.calculate(symbolTable, getParameterList()),
                "exception is thrown");
        assertNotNull(e);


        assertEquals( 8,
                (int)strlenFunction.calculate(symbolTable, getParameterList(
                        new ExpressionNodeString(new Token(TokenType.NONE, "A string", 0)))),
                "Values are equal");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initTimeProviderManager();
    }

    @AfterEach
    public void tearDown() {
        LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
