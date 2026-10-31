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
 * @author Daniel Bergqvist 2024
 */
public class JavaFunctionsTest {


    private List<ExpressionNode> getParameterList(ExpressionNode... exprNodes) {
        List<ExpressionNode> list = new ArrayList<>();
        Collections.addAll(list, exprNodes);
        return list;
    }

    @Test
    public void testNewFunction() throws JmriException {
        Function newFunction = InstanceManager.getDefault(FunctionManager.class).get("new");
        assertEquals( "new", newFunction.getName(), "strings matches");


        SymbolTable symbolTable = new DefaultSymbolTable(new DefaultConditionalNG("IQC1", null));

        // Test unsupported token type
        WrongNumberOfParametersException e = assertThrows( WrongNumberOfParametersException.class, () ->
            newFunction.calculate(symbolTable, getParameterList()), "exception is thrown");
        assertNotNull(e);

        Object obj = newFunction.calculate(symbolTable, getParameterList(
                new ExpressionNodeString(new Token(TokenType.NONE, "jmri.jmrit.logixng.util.parser.functions.ExpressionNodeConstantScaffold", 0)),
                new ExpressionNodeString(new Token(TokenType.NONE, "Parameter", 0))
                ));
        assertNotNull(obj);
        assertEquals("jmri.jmrit.logixng.util.parser.functions.ExpressionNodeConstantScaffold", obj.getClass().getName());

        obj = newFunction.calculate(symbolTable, getParameterList(
                new ExpressionNodeString(new Token(TokenType.NONE, "java.lang.StringBuilder", 0))));
        assertNotNull(obj);
        assertEquals("java.lang.StringBuilder", obj.getClass().getName());
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
