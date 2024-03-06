package jmri.jmrit.logixng.util.parser.functions;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import jmri.jmrit.logixng.SymbolTable;
import jmri.jmrit.logixng.implementation.DefaultConditionalNG;
import jmri.jmrit.logixng.implementation.DefaultSymbolTable;
import jmri.jmrit.logixng.util.LogixNG_Thread;
import jmri.jmrit.logixng.util.parser.ExpressionNode;
import jmri.jmrit.logixng.util.parser.ExpressionNodeString;
import jmri.jmrit.logixng.util.parser.Token;
import jmri.jmrit.logixng.util.parser.TokenType;
import jmri.jmrit.logixng.util.parser.WrongNumberOfParametersException;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test ParsedExpression
 *
 * @author Daniel Bergqvist 2024
 */
public class CommonFunctionsTest {


    private List<ExpressionNode> getParameterList(ExpressionNode... exprNodes) {
        List<ExpressionNode> list = new ArrayList<>();
        Collections.addAll(list, exprNodes);
        return list;
    }

    @Test
    public void testLengthFunction() throws Exception {
        CommonFunctions.LengthFunction lengthFunction = new CommonFunctions.LengthFunction();
        Assert.assertEquals("strings matches", "length", lengthFunction.getName());

        AtomicBoolean hasThrown = new AtomicBoolean(false);

        SymbolTable symbolTable = new DefaultSymbolTable(new DefaultConditionalNG("IQC1", null));

        // Test unsupported token type
        hasThrown.set(false);
        try {
            lengthFunction.calculate(symbolTable, getParameterList());
        } catch (WrongNumberOfParametersException e) {
            hasThrown.set(true);
        }
        Assert.assertTrue("exception is thrown", hasThrown.get());

        Assert.assertEquals("Values are equal", 8,
                (int)lengthFunction.calculate(symbolTable, getParameterList(
                        new ExpressionNodeString(new Token(TokenType.NONE, "A string", 0)))));

        Assert.assertEquals("Strings are equal", 4,
                (int)lengthFunction.calculate(symbolTable, getParameterList(
                        new ExpressionNodeConstant(
                                new String[]{"Red", "Green", "Blue", "Yellow"}))));

        List<String> list = new ArrayList<>();
        list.add("A");
        list.add("x");
        list.add("Hello");
        list.add("Something");
        list.add("EE");
        list.add("H");
        list.add("III");
        Assert.assertEquals("Strings are equal", 7,
                (int)lengthFunction.calculate(symbolTable, getParameterList(
                        new ExpressionNodeConstant(list))));

        Set<String> set = new HashSet<>();
        set.add("A");
        set.add("x");
        set.add("Hello");
        set.add("Something");
        set.add("EE");
        set.add("12");
        set.add("32");
        set.add("Jjjj");
        set.add("H");
        set.add("III");
        Assert.assertEquals("Strings are equal", 10,
                (int)lengthFunction.calculate(symbolTable, getParameterList(
                        new ExpressionNodeConstant(set))));

        Map<String, Integer> map = new HashMap<>();
        map.put("Hello",72);
        map.put("Something", 33);
        Assert.assertEquals("Strings are equal", 2,
                (int)lengthFunction.calculate(symbolTable, getParameterList(
                        new ExpressionNodeConstant(map))));
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


    public static class ExpressionNodeConstant implements ExpressionNode {

        private final Object _value;

        public ExpressionNodeConstant(Object value) {
            _value = value;
        }

        @Override
        public Object calculate(SymbolTable symbolTable) {
            return _value;
        }

        /** {@inheritDoc} */
        @Override
        public String getDefinitionString() {
            return null;    // This value is never used
        }

    }

}
