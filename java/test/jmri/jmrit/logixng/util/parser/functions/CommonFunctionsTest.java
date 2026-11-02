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
public class CommonFunctionsTest {


    private List<ExpressionNode> getParameterList(ExpressionNode... exprNodes) {
        List<ExpressionNode> list = new ArrayList<>();
        Collections.addAll(list, exprNodes);
        return list;
    }

    @Test
    public void testLengthFunction() throws JmriException {
        Function lengthFunction = InstanceManager.getDefault(FunctionManager.class).get("length");
        assertEquals( "length", lengthFunction.getName(), "strings matches");


        SymbolTable symbolTable = new DefaultSymbolTable(new DefaultConditionalNG("IQC1", null));

        // Test unsupported token type
        WrongNumberOfParametersException e = assertThrows( WrongNumberOfParametersException.class, () ->
            lengthFunction.calculate(symbolTable, getParameterList()),
                    "exception is thrown");
        assertNotNull(e);

        assertEquals( 8,
                (int)lengthFunction.calculate(symbolTable, getParameterList(
                        new ExpressionNodeString(new Token(TokenType.NONE, "A string", 0)))),
                "Values are equal");

        assertEquals( 4,
                (int)lengthFunction.calculate(symbolTable, getParameterList(new ExpressionNodeConstantScaffold(
                                new String[]{"Red", "Green", "Blue", "Yellow"}))),
                "Strings are equal");

        List<String> list = new ArrayList<>();
        list.add("A");
        list.add("x");
        list.add("Hello");
        list.add("Something");
        list.add("EE");
        list.add("H");
        list.add("III");
        assertEquals( 7,
                (int)lengthFunction.calculate(symbolTable, getParameterList(new ExpressionNodeConstantScaffold(list))),
                "Strings are equal");

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
        assertEquals( 10,
                (int)lengthFunction.calculate(symbolTable, getParameterList(new ExpressionNodeConstantScaffold(set))),
                "Strings are equal");

        Map<String, Integer> map = new HashMap<>();
        map.put("Hello",72);
        map.put("Something", 33);
        assertEquals( 2,
                (int)lengthFunction.calculate(symbolTable, getParameterList(new ExpressionNodeConstantScaffold(map))),
                "Strings are equal");
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
