package jmri.jmrit.logixng.util.parser.functions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.implementation.*;
import jmri.jmrit.logixng.util.LogixNG_Thread;
import jmri.jmrit.logixng.util.parser.*;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Test LayoutFunctions
 *
 * @author Daniel Bergqvist 2021
 */
public class NamedBeanFunctionsTest {

    private final ExpressionNode exprLogixNGTableIQT1 = new ExpressionNodeString(new Token(TokenType.NONE, "IQT1", 0));
    private final ExpressionNode exprLogixNGTableMyTable = new ExpressionNodeString(new Token(TokenType.NONE, "My table", 0));

    private final ExpressionNode exprMemoryIM1 = new ExpressionNodeString(new Token(TokenType.NONE, "IM1", 0));
    private final ExpressionNode exprMyMemory1 = new ExpressionNodeString(new Token(TokenType.NONE, "My memory 1", 0));
    private final ExpressionNode exprRed = new ExpressionNodeString(new Token(TokenType.NONE, "Red", 0));
    private final ExpressionNode exprGreen = new ExpressionNodeString(new Token(TokenType.NONE, "Green", 0));


    private List<ExpressionNode> getParameterList(ExpressionNode... exprNodes) {
        List<ExpressionNode> list = new ArrayList<>();
        Collections.addAll(list, exprNodes);
        return list;
    }

    @Test
    public void testGetLogixNGTableFunction() throws Exception {
        Function getLogixNGTableFunction = InstanceManager.getDefault(FunctionManager.class).get("getLogixNGTable");
        Assert.assertEquals("strings matches", "getLogixNGTable", getLogixNGTableFunction.getName());
        Assert.assertNotNull("Function has description", getLogixNGTableFunction.getDescription());

        SymbolTable symbolTable = new DefaultSymbolTable(new DefaultConditionalNG("IQC1", null));

        NamedTable table = new DefaultInternalNamedTable("IQT1", "My table", 2, 3);
        InstanceManager.getDefault(NamedTableManager.class).register(table);

        Object myTable = getLogixNGTableFunction.calculate(symbolTable, getParameterList(exprLogixNGTableIQT1));
        Assert.assertNotNull(myTable);
        Assert.assertEquals("jmri.jmrit.logixng.implementation.DefaultInternalNamedTable", myTable.getClass().getName());
        Assert.assertEquals("IQT1", ((NamedBean)myTable).getSystemName());
        Assert.assertEquals("My table", ((NamedBean)myTable).getUserName());

        myTable = getLogixNGTableFunction.calculate(symbolTable, getParameterList(exprLogixNGTableMyTable));
        Assert.assertNotNull(myTable);
        Assert.assertEquals("jmri.jmrit.logixng.implementation.DefaultInternalNamedTable", myTable.getClass().getName());
        Assert.assertEquals("IQT1", ((NamedBean)myTable).getSystemName());
        Assert.assertEquals("My table", ((NamedBean)myTable).getUserName());
    }

    @Test
    public void testGetSetMemoryFunction() throws Exception {
        Function readMemoryFunction = InstanceManager.getDefault(FunctionManager.class).get("readMemory");
        Assert.assertEquals("strings matches", "readMemory", readMemoryFunction.getName());
        Assert.assertNotNull("Function has description", readMemoryFunction.getDescription());

        Function writeMemoryFunction = InstanceManager.getDefault(FunctionManager.class).get("writeMemory");
        Assert.assertEquals("strings matches", "writeMemory", writeMemoryFunction.getName());
        Assert.assertNotNull("Function has description", writeMemoryFunction.getDescription());

        AtomicBoolean hasThrown = new AtomicBoolean(false);

        SymbolTable symbolTable = new DefaultSymbolTable(new DefaultConditionalNG("IQC1", null));

        hasThrown.set(false);
        try {
            readMemoryFunction.calculate(symbolTable, getParameterList());
        } catch (WrongNumberOfParametersException e) {
            hasThrown.set(true);
        }
        Assert.assertTrue("exception is thrown", hasThrown.get());

        Assert.assertEquals("Memory has correct value", null,
                readMemoryFunction.calculate(symbolTable, getParameterList(exprMemoryIM1)));

        Assert.assertEquals("Memory has correct value", null,
                readMemoryFunction.calculate(symbolTable, getParameterList(exprMyMemory1)));

        MyMemory memory = new MyMemory();
        InstanceManager.getDefault(MemoryManager.class).register(memory);

        memory._lastValue = null;
        memory._value = "Green";
        Assert.assertEquals("Memory has correct value", "Green",
                readMemoryFunction.calculate(symbolTable, getParameterList(exprMemoryIM1)));
        Assert.assertNull("Memory is not set", memory._lastValue);

        memory._value = "Green";
        Assert.assertEquals("Memory has correct value", "Green",
                readMemoryFunction.calculate(symbolTable, getParameterList(exprMyMemory1)));
        Assert.assertNull("Memory is not set", memory._lastValue);

        memory._value = "Red";
        Assert.assertEquals("Memory has correct value", "Red",
                readMemoryFunction.calculate(symbolTable, getParameterList(exprMemoryIM1)));
        Assert.assertNull("Memory is not set", memory._lastValue);

        memory._value = "Red";
        Assert.assertEquals("Memory has correct value", "Red",
                readMemoryFunction.calculate(symbolTable, getParameterList(exprMyMemory1)));
        Assert.assertNull("Memory is not set", memory._lastValue);

        memory._lastValue = null;
        memory._value = null;
        Assert.assertEquals("Memory has correct value",
                "Green",
                writeMemoryFunction.calculate(symbolTable,
                        getParameterList(exprMemoryIM1, exprGreen)));
        Assert.assertEquals("Memory is set", "Green", memory._lastValue);

        memory._lastValue = null;
        memory._value = null;
        Assert.assertEquals("Memory has correct value",
                "Green",
                writeMemoryFunction.calculate(symbolTable,
                        getParameterList(exprMyMemory1, exprGreen)));
        Assert.assertEquals("Memory is set", "Green", memory._lastValue);

        memory._lastValue = null;
        memory._value = null;
        Assert.assertEquals("Memory has correct value",
                "Red",
                writeMemoryFunction.calculate(symbolTable,
                        getParameterList(exprMemoryIM1, exprRed)));
        Assert.assertEquals("Memory is set", "Red", memory._lastValue);

        memory._lastValue = null;
        memory._value = null;
        Assert.assertEquals("Memory has correct value",
                "Red",
                writeMemoryFunction.calculate(symbolTable,
                        getParameterList(exprMyMemory1, exprRed)));
        Assert.assertEquals("Memory is set", "Red", memory._lastValue);
    }

    @Test
    public void testEvaluateMemoryFunction() throws Exception {
        Function evaluateMemoryFunction = InstanceManager.getDefault(FunctionManager.class).get("evaluateMemory");
        Assert.assertEquals("strings matches", "evaluateMemory", evaluateMemoryFunction.getName());
        Assert.assertNotNull("Function has description", evaluateMemoryFunction.getDescription());

        AtomicBoolean hasThrown = new AtomicBoolean(false);

        SymbolTable symbolTable = new DefaultSymbolTable(new DefaultConditionalNG("IQC1", null));

        hasThrown.set(false);
        try {
            evaluateMemoryFunction.calculate(symbolTable, getParameterList());
        } catch (WrongNumberOfParametersException e) {
            hasThrown.set(true);
        }
        Assert.assertTrue("exception is thrown", hasThrown.get());

        Assert.assertEquals("Memory has correct value", null,
                evaluateMemoryFunction.calculate(symbolTable, getParameterList(exprMemoryIM1)));

        Assert.assertEquals("Memory has correct value", null,
                evaluateMemoryFunction.calculate(symbolTable, getParameterList(exprMyMemory1)));

        MyMemory memory = new MyMemory();
        InstanceManager.getDefault(MemoryManager.class).register(memory);

        MyMemory otherMemory = new MyMemory("IM2", "My other memory");
        otherMemory._value = "Other memory value";
        InstanceManager.getDefault(MemoryManager.class).register(otherMemory);

        memory._lastValue = null;
        memory._value = "Green";
        Assert.assertEquals("Memory has correct value", "Green",
                evaluateMemoryFunction.calculate(symbolTable, getParameterList(exprMemoryIM1)));
        Assert.assertNull("Memory is not set", memory._lastValue);

        memory._value = "Green";
        Assert.assertEquals("Memory has correct value", "Green",
                evaluateMemoryFunction.calculate(symbolTable, getParameterList(exprMyMemory1)));
        Assert.assertNull("Memory is not set", memory._lastValue);

        memory._value = "Red";
        Assert.assertEquals("Memory has correct value", "Red",
                evaluateMemoryFunction.calculate(symbolTable, getParameterList(exprMemoryIM1)));
        Assert.assertNull("Memory is not set", memory._lastValue);

        memory._value = "Red";
        Assert.assertEquals("Memory has correct value", "Red",
                evaluateMemoryFunction.calculate(symbolTable, getParameterList(exprMyMemory1)));
        Assert.assertNull("Memory is not set", memory._lastValue);

        memory._value = "{IM2}";
        Assert.assertEquals("Memory has correct value", "Other memory value",
                evaluateMemoryFunction.calculate(symbolTable, getParameterList(exprMyMemory1)));
        Assert.assertNull("Memory is not set", memory._lastValue);

        memory._value = "{My other memory}";
        Assert.assertEquals("Memory has correct value", "Other memory value",
                evaluateMemoryFunction.calculate(symbolTable, getParameterList(exprMyMemory1)));
        Assert.assertNull("Memory is not set", memory._lastValue);
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


    private static class MyMemory extends jmri.implementation.AbstractMemory {

        Object _lastValue = null;
        Object _value = null;

        MyMemory() {
            super("IM1", "My memory 1");
        }

        MyMemory(String sys, String user) {
            super(sys, user);
        }

        /** {@inheritDoc} */
        @Override
        public Object getValue() {
            return _value;
        }

        @Override
        public void setValue(Object value) {
            _lastValue = value;
            _value = null;
        }

        @Override
        public int getState() {
            return 0;
        }

        @Override
        public void setState(int s) throws JmriException {
            // Do nothing
        }

    }

}
