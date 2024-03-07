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
public class JavaFunctionsTest {


    private List<ExpressionNode> getParameterList(ExpressionNode... exprNodes) {
        List<ExpressionNode> list = new ArrayList<>();
        Collections.addAll(list, exprNodes);
        return list;
    }

    @Test
    public void testNewFunction() throws Exception {
        Function newFunction = InstanceManager.getDefault(FunctionManager.class).get("new");
        Assert.assertEquals("strings matches", "new", newFunction.getName());

        AtomicBoolean hasThrown = new AtomicBoolean(false);

        SymbolTable symbolTable = new DefaultSymbolTable(new DefaultConditionalNG("IQC1", null));

        // Test unsupported token type
        hasThrown.set(false);
        try {
            newFunction.calculate(symbolTable, getParameterList());
        } catch (WrongNumberOfParametersException e) {
            hasThrown.set(true);
        }
        Assert.assertTrue("exception is thrown", hasThrown.get());

        Object obj = newFunction.calculate(symbolTable, getParameterList(
                new ExpressionNodeString(new Token(TokenType.NONE, "jmri.jmrit.logixng.util.parser.functions.ExpressionNodeConstantScaffold", 0)),
                new ExpressionNodeString(new Token(TokenType.NONE, "Parameter", 0))
                ));
        Assert.assertNotNull(obj);
        Assert.assertEquals("jmri.jmrit.logixng.util.parser.functions.ExpressionNodeConstantScaffold", obj.getClass().getName());

        obj = newFunction.calculate(symbolTable, getParameterList(
                new ExpressionNodeString(new Token(TokenType.NONE, "java.lang.StringBuilder", 0))));
        Assert.assertNotNull(obj);
        Assert.assertEquals("java.lang.StringBuilder", obj.getClass().getName());
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
