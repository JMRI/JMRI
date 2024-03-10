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
        Function lengthFunction = InstanceManager.getDefault(FunctionManager.class).get("length");
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
                (int)lengthFunction.calculate(symbolTable, getParameterList(new ExpressionNodeConstantScaffold(
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
                (int)lengthFunction.calculate(symbolTable, getParameterList(new ExpressionNodeConstantScaffold(list))));

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
                (int)lengthFunction.calculate(symbolTable, getParameterList(new ExpressionNodeConstantScaffold(set))));

        Map<String, Integer> map = new HashMap<>();
        map.put("Hello",72);
        map.put("Something", 33);
        Assert.assertEquals("Strings are equal", 2,
                (int)lengthFunction.calculate(symbolTable, getParameterList(new ExpressionNodeConstantScaffold(map))));
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
