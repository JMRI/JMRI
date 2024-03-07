package jmri.jmrit.logixng.util.parser.functions;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import jmri.InstanceManager;
import jmri.jmrit.logixng.SymbolTable;
import jmri.jmrit.logixng.implementation.DefaultConditionalNG;
import jmri.jmrit.logixng.implementation.DefaultSymbolTable;
import jmri.jmrit.logixng.util.LogixNG_Thread;
import jmri.jmrit.logixng.util.parser.*;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test ParsedExpression
 *
 * @author Daniel Bergqvist 2020
 */
public class StringFunctionsTest {

    ExpressionNode expr_str_HELLO = new ExpressionNodeString(new Token(TokenType.NONE, "hello", 0));
    ExpressionNode expr0_95 = new ExpressionNodeFloatingNumber(new Token(TokenType.NONE, "0.95", 0));
    ExpressionNode expr12 = new ExpressionNodeIntegerNumber(new Token(TokenType.NONE, "12", 0));


    private List<ExpressionNode> getParameterList(ExpressionNode... exprNodes) {
        List<ExpressionNode> list = new ArrayList<>();
        Collections.addAll(list, exprNodes);
        return list;
    }

    @Test
    public void testFormatFunction() throws Exception {
        Function formatFunction = InstanceManager.getDefault(FunctionManager.class).get("format");
        Assert.assertEquals("strings matches", "format", formatFunction.getName());

        AtomicBoolean hasThrown = new AtomicBoolean(false);

        SymbolTable symbolTable = new DefaultSymbolTable(new DefaultConditionalNG("IQC1", null));

        // Test unsupported token type
        hasThrown.set(false);
        try {
            formatFunction.calculate(symbolTable, getParameterList());
        } catch (WrongNumberOfParametersException e) {
            hasThrown.set(true);
        }
        Assert.assertTrue("exception is thrown", hasThrown.get());

        Assert.assertEquals("Strings are equal", "A format string",
                formatFunction.calculate(symbolTable, getParameterList(new ExpressionNodeString(
                        new Token(TokenType.NONE, "A format string", 0)))));

        Assert.assertEquals("Strings are equal", "Number: 12, String: hello",
                formatFunction.calculate(symbolTable, getParameterList(new ExpressionNodeString(
                        new Token(TokenType.NONE, "Number: %d, String: %s", 0)),
                        expr12, expr_str_HELLO)));

        Assert.assertEquals("Strings are equal", "Number: 012, String:   hello, Float:  0.9500",
                formatFunction.calculate(symbolTable, getParameterList(new ExpressionNodeString(
                        new Token(TokenType.NONE, "Number: %03d, String: %7s, Float: %7.4f", 0)),
                        expr12, expr_str_HELLO, expr0_95)));
    }

    @Test
    public void testStrLenFunction() throws Exception {
        Function strlenFunction = InstanceManager.getDefault(FunctionManager.class).get("strlen");
        Assert.assertEquals("strings matches", "strlen", strlenFunction.getName());

        AtomicBoolean hasThrown = new AtomicBoolean(false);

        SymbolTable symbolTable = new DefaultSymbolTable(new DefaultConditionalNG("IQC1", null));

        // Test unsupported token type
        hasThrown.set(false);
        try {
            strlenFunction.calculate(symbolTable, getParameterList());
        } catch (WrongNumberOfParametersException e) {
            hasThrown.set(true);
        }
        Assert.assertTrue("exception is thrown", hasThrown.get());

        Assert.assertEquals("Values are equal", 8,
                (int)strlenFunction.calculate(symbolTable, getParameterList(
                        new ExpressionNodeString(new Token(TokenType.NONE, "A string", 0)))));
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
