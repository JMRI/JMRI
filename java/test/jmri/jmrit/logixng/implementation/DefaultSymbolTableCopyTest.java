package jmri.jmrit.logixng.implementation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import jmri.JmriException;
import jmri.jmrit.logixng.SymbolTable;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test that a copy of a symbol table keeps its variables when new variables
 * are added to the copy, as ExecuteDelayed and similar actions do.
 *
 * @author Keith Bruce 2026
 */
public class DefaultSymbolTableCopyTest {

    @Test
    public void testAddSymbolToCopy() throws JmriException {
        DefaultSymbolTable symbolTable = new DefaultSymbolTable();
        symbolTable.createSymbols(List.of(
                new SymbolTable.VariableData("x", SymbolTable.InitialValueType.String, "X"),
                new SymbolTable.VariableData("z", SymbolTable.InitialValueType.String, "Z")));

        DefaultSymbolTable copy = new DefaultSymbolTable(symbolTable);
        assertEquals("X", copy.getValue("x"));
        assertEquals("Z", copy.getValue("z"));

        // As an item with a local variable inside ExecuteDelayed does
        copy.createSymbols(List.of(
                new SymbolTable.VariableData("y", SymbolTable.InitialValueType.String, "Y")));
        assertEquals("X", copy.getValue("x"), "x is not overwritten by y");
        assertEquals("Z", copy.getValue("z"), "z is kept when the stack grows");
        assertEquals("Y", copy.getValue("y"));
        assertEquals(3, copy.getStack().getCount(), "y is added after the two copied values");

        copy.setValue("y", "New Y");
        assertEquals("X", copy.getValue("x"));
        assertEquals("Z", copy.getValue("z"));
        assertEquals("New Y", copy.getValue("y"));

        // The original symbol table is not changed
        assertEquals("X", symbolTable.getValue("x"));
        assertEquals("Z", symbolTable.getValue("z"));
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initLogixNGManager();
    }

    @AfterEach
    public void tearDown() {
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
