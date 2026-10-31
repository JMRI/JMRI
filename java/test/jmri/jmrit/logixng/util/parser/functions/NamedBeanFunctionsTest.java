package jmri.jmrit.logixng.util.parser.functions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.implementation.*;
import jmri.jmrit.logixng.util.LogixNG_Thread;
import jmri.jmrit.logixng.util.parser.*;
import jmri.util.JUnitUtil;

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
    private final ExpressionNode exprReferenceIM1 = new ExpressionNodeString(new Token(TokenType.NONE, "{IM1}", 0));


    private List<ExpressionNode> getParameterList(ExpressionNode... exprNodes) {
        List<ExpressionNode> list = new ArrayList<>();
        Collections.addAll(list, exprNodes);
        return list;
    }

    @Test
    public void testGetLogixNGTableFunction() throws JmriException {
        Function getLogixNGTableFunction = InstanceManager.getDefault(FunctionManager.class).get("getLogixNGTable");
        assertEquals( "getLogixNGTable", getLogixNGTableFunction.getName(), "strings matches");
        assertNotNull( getLogixNGTableFunction.getDescription(), "Function has description");

        SymbolTable symbolTable = new DefaultSymbolTable(new DefaultConditionalNG("IQC1", null));

        NamedTable table = new DefaultInternalNamedTable("IQT1", "My table", 2, 3);
        InstanceManager.getDefault(NamedTableManager.class).register(table);

        Object myTable = getLogixNGTableFunction.calculate(symbolTable, getParameterList(exprLogixNGTableIQT1));
        assertNotNull(myTable);
        assertEquals("jmri.jmrit.logixng.implementation.DefaultInternalNamedTable", myTable.getClass().getName());
        assertEquals("IQT1", ((NamedBean)myTable).getSystemName());
        assertEquals("My table", ((NamedBean)myTable).getUserName());

        myTable = getLogixNGTableFunction.calculate(symbolTable, getParameterList(exprLogixNGTableMyTable));
        assertNotNull(myTable);
        assertEquals("jmri.jmrit.logixng.implementation.DefaultInternalNamedTable", myTable.getClass().getName());
        assertEquals("IQT1", ((NamedBean)myTable).getSystemName());
        assertEquals("My table", ((NamedBean)myTable).getUserName());
    }

    @Test
    public void testGetSetMemoryFunction() throws JmriException {
        Function readMemoryFunction = InstanceManager.getDefault(FunctionManager.class).get("readMemory");
        assertEquals( "readMemory", readMemoryFunction.getName(), "strings matches");
        assertNotNull( readMemoryFunction.getDescription(), "Function has description");

        Function writeMemoryFunction = InstanceManager.getDefault(FunctionManager.class).get("writeMemory");
        assertEquals( "writeMemory", writeMemoryFunction.getName(), "strings matches");
        assertNotNull( writeMemoryFunction.getDescription(), "Function has description");


        SymbolTable symbolTable = new DefaultSymbolTable(new DefaultConditionalNG("IQC1", null));

        WrongNumberOfParametersException e = assertThrows( WrongNumberOfParametersException.class, () ->
            readMemoryFunction.calculate(symbolTable, getParameterList()),
                "exception is thrown");
        assertNotNull(e);

        assertNull( readMemoryFunction.calculate(symbolTable, getParameterList(exprMemoryIM1)),
                "Memory has correct value");

        assertNull( readMemoryFunction.calculate(symbolTable, getParameterList(exprMyMemory1)),
                "Memory has correct value");

        MyMemory memory = new MyMemory();
        InstanceManager.getDefault(MemoryManager.class).register(memory);

        memory._lastValue = null;
        memory._value = "Green";
        assertEquals( "Green",
                readMemoryFunction.calculate(symbolTable, getParameterList(exprMemoryIM1)),
                "Memory has correct value");
        assertNull( memory._lastValue, "Memory is not set");

        memory._value = "Green";
        assertEquals( "Green",
                readMemoryFunction.calculate(symbolTable, getParameterList(exprMyMemory1)),
                "Memory has correct value");
        assertNull( memory._lastValue, "Memory is not set");

        memory._value = "Red";
        assertEquals( "Red",
                readMemoryFunction.calculate(symbolTable, getParameterList(exprMemoryIM1)),
                "Memory has correct value");
        assertNull( memory._lastValue, "Memory is not set");

        memory._value = "Red";
        assertEquals( "Red",
                readMemoryFunction.calculate(symbolTable, getParameterList(exprMyMemory1)),
                "Memory has correct value");
        assertNull( memory._lastValue, "Memory is not set");

        memory._lastValue = null;
        memory._value = null;
        assertEquals( "Green",
                writeMemoryFunction.calculate(symbolTable,
                        getParameterList(exprMemoryIM1, exprGreen)),
                "Memory has correct value");
        assertEquals( "Green", memory._lastValue, "Memory is set");

        memory._lastValue = null;
        memory._value = null;
        assertEquals( "Green",
                writeMemoryFunction.calculate(symbolTable,
                        getParameterList(exprMyMemory1, exprGreen)),
                "Memory has correct value");
        assertEquals( "Green", memory._lastValue, "Memory is set");

        memory._lastValue = null;
        memory._value = null;
        assertEquals( "Red",
                writeMemoryFunction.calculate(symbolTable,
                        getParameterList(exprMemoryIM1, exprRed)),
                "Memory has correct value");
        assertEquals( "Red", memory._lastValue, "Memory is set");

        memory._lastValue = null;
        memory._value = null;
        assertEquals( "Red",
                writeMemoryFunction.calculate(symbolTable,
                        getParameterList(exprMyMemory1, exprRed)),
                "Memory has correct value");
        assertEquals( "Red", memory._lastValue, "Memory is set");
    }

    @Test
    public void testEvaluateMemoryFunction() throws JmriException {
        Function evaluateMemoryFunction = InstanceManager.getDefault(FunctionManager.class).get("evaluateMemory");
        assertEquals( "evaluateMemory", evaluateMemoryFunction.getName(), "strings matches");
        assertNotNull( evaluateMemoryFunction.getDescription(), "Function has description");


        SymbolTable symbolTable = new DefaultSymbolTable(new DefaultConditionalNG("IQC1", null));

        WrongNumberOfParametersException e = assertThrows( WrongNumberOfParametersException.class, () ->
            evaluateMemoryFunction.calculate(symbolTable, getParameterList()),
                "exception is thrown");
        assertNotNull(e);

        assertNull( evaluateMemoryFunction.calculate(symbolTable, getParameterList(exprMemoryIM1)),
                "Memory has correct value");

        assertNull(
                evaluateMemoryFunction.calculate(symbolTable, getParameterList(exprMyMemory1)),
                "Memory has correct value");

        MyMemory memory = new MyMemory();
        InstanceManager.getDefault(MemoryManager.class).register(memory);

        MyMemory otherMemory = new MyMemory("IM2", "My other memory");
        otherMemory._value = "Other memory value";
        InstanceManager.getDefault(MemoryManager.class).register(otherMemory);

        memory._lastValue = null;
        memory._value = "Green";
        assertEquals( "Green",
                evaluateMemoryFunction.calculate(symbolTable, getParameterList(exprMemoryIM1)),
                "Memory has correct value");
        assertNull( memory._lastValue, "Memory is not set");

        memory._value = "Green";
        assertEquals( "Green",
                evaluateMemoryFunction.calculate(symbolTable, getParameterList(exprMyMemory1)),
                "Memory has correct value");
        assertNull( memory._lastValue, "Memory is not set");

        memory._value = "Red";
        assertEquals( "Red",
                evaluateMemoryFunction.calculate(symbolTable, getParameterList(exprMemoryIM1)),
                "Memory has correct value");
        assertNull( memory._lastValue, "Memory is not set");

        memory._value = "Red";
        assertEquals( "Red",
                evaluateMemoryFunction.calculate(symbolTable, getParameterList(exprMyMemory1)),
                "Memory has correct value");
        assertNull( memory._lastValue, "Memory is not set");

        memory._value = "{IM2}";
        assertEquals( "Other memory value",
                evaluateMemoryFunction.calculate(symbolTable, getParameterList(exprMyMemory1)),
                "Memory has correct value");
        assertNull( memory._lastValue, "Memory is not set");

        memory._value = "{My other memory}";
        assertEquals( "Other memory value",
                evaluateMemoryFunction.calculate(symbolTable, getParameterList(exprMyMemory1)),
                "Memory has correct value");
        assertNull( memory._lastValue, "Memory is not set");
    }

    @Test
    public void testEvaluateReferenceFunction() throws JmriException {
        Function evaluateReferenceFunction = InstanceManager.getDefault(FunctionManager.class).get("evaluateReference");
        assertEquals( "evaluateReference", evaluateReferenceFunction.getName(), "strings matches");
        assertNotNull( evaluateReferenceFunction.getDescription(), "Function has description");


        SymbolTable symbolTable = new DefaultSymbolTable(new DefaultConditionalNG("IQC1", null));

        WrongNumberOfParametersException e = assertThrows( WrongNumberOfParametersException.class, () ->
            evaluateReferenceFunction.calculate(symbolTable, getParameterList()),
                "exception is thrown");
        assertNotNull(e);

        MyMemory memory = new MyMemory();
        InstanceManager.getDefault(MemoryManager.class).register(memory);
        memory._value = "Some value";

        MyMemory otherMemory = new MyMemory("IM2", "My other memory");
        otherMemory._value = "Other memory value";
        InstanceManager.getDefault(MemoryManager.class).register(otherMemory);

        assertEquals( "Some value",
                evaluateReferenceFunction.calculate(symbolTable, getParameterList(exprReferenceIM1)),
                "Reference has correct value");

        memory._lastValue = null;
        memory._value = "Green";
        assertEquals( "Green",
                evaluateReferenceFunction.calculate(symbolTable, getParameterList(exprReferenceIM1)),
                "Reference has correct value");
        assertNull( memory._lastValue, "Memory is not set");

        memory._value = "Green";
        assertEquals( "Green",
                evaluateReferenceFunction.calculate(symbolTable, getParameterList(exprReferenceIM1)),
                "Reference has correct value");
        assertNull( memory._lastValue, "Memory is not set");

        memory._value = "Red";
        assertEquals( "Red",
                evaluateReferenceFunction.calculate(symbolTable, getParameterList(exprReferenceIM1)),
                "Reference has correct value");
        assertNull( memory._lastValue, "Memory is not set");

        memory._value = "Red";
        assertEquals( "Red",
                evaluateReferenceFunction.calculate(symbolTable, getParameterList(exprReferenceIM1)),
                "Reference has correct value");
        assertNull( memory._lastValue, "Memory is not set");
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
