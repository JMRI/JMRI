package jmri.jmrit.logixng;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.*;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.jmrit.logixng.SymbolTable.InitialValueType;
import jmri.jmrit.logixng.implementation.DefaultConditionalNG;
import jmri.jmrit.logixng.implementation.DefaultSymbolTable;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test SymbolTable
 *
 * @author Daniel Bergqvist 2021
 */
public class SymbolTableTest {

    @Test
    public void testValidateName() {
        // Valid names
        assertTrue(SymbolTable.validateName("Abc"));
        assertTrue(SymbolTable.validateName("abc"));
        assertTrue(SymbolTable.validateName("Abc123"));
        assertTrue(SymbolTable.validateName("A123bc"));
        assertTrue(SymbolTable.validateName("Abc___"));
        assertTrue(SymbolTable.validateName("Abc___fsdffs"));
        assertTrue(SymbolTable.validateName("Abc3123__2341fsdf"));

        // Invalid names
        assertFalse(SymbolTable.validateName("12Abc"));  // Starts with a digit
        assertFalse(SymbolTable.validateName("_Abc"));   // Starts with an underscore
        assertFalse(SymbolTable.validateName(" Abc"));   // Starts with a non letter
        assertFalse(SymbolTable.validateName("A bc"));   // Has a character that's not letter, digit or underscore
        assertFalse(SymbolTable.validateName("A{bc"));   // Has a character that's not letter, digit or underscore
        assertFalse(SymbolTable.validateName("A+bc"));   // Has a character that's not letter, digit or underscore
    }

    private SymbolTable createLocalVariable(InitialValueType type, String initialValue)
            throws JmriException {
        List<SymbolTable.VariableData> localVariables = new ArrayList<>();
        localVariables.add(new SymbolTable.VariableData("myVar", type, initialValue));
        SymbolTable symbolTable = new DefaultSymbolTable(new DefaultConditionalNG("IQC1", null));
        symbolTable.createSymbols(localVariables);
        return symbolTable;
    }

    @Test
    public void testLocalVariables() throws JmriException {

        // Test strings
        SymbolTable symbolTable = createLocalVariable(InitialValueType.String, "Hello");
        assertEquals( "Hello", symbolTable.getValue("myVar"),
                "variable has correct value");

        symbolTable.setValue("myVar", 25.3);
        assertEquals( 25.3, (double)symbolTable.getValue("myVar"), 0.00000001,
                "variable has correct value");

        symbolTable.setValue("myVar", "34");
        assertEquals( "34", symbolTable.getValue("myVar"),
                "variable has correct value");

        symbolTable.setValue("myVar", 12);
        assertEquals( 12, (int)symbolTable.getValue("myVar"),
                "variable has correct value");

        // Test integers
        symbolTable = createLocalVariable(InitialValueType.FloatingNumber, "42.11");
        assertEquals( 42.11, (double)symbolTable.getValue("myVar"), 0.00000001,
                "variable has correct value");

        symbolTable.setValue("myVar", 25.3);
        assertEquals( 25.3, (double)symbolTable.getValue("myVar"), 0.00000001,
                "variable has correct value");

        symbolTable.setValue("myVar", "34");
        assertEquals( "34", symbolTable.getValue("myVar"),
                "variable has correct value");

        symbolTable.setValue("myVar", 12);
        assertEquals( 12, (int)symbolTable.getValue("myVar"),
                "variable has correct value");

        // Test floating point numbers
        symbolTable = createLocalVariable(InitialValueType.FloatingNumber, "42.11");
        assertEquals( 42.11, (double)symbolTable.getValue("myVar"), 0.00000001,
                "variable has correct value");

        symbolTable.setValue("myVar", 25.3);
        assertEquals( 25.3, (double)symbolTable.getValue("myVar"), 0.00000001,
                "variable has correct value");

        symbolTable.setValue("myVar", "34");
        assertEquals( "34", symbolTable.getValue("myVar"),
                "variable has correct value");

        symbolTable.setValue("myVar", 12);
        assertEquals( 12, (int)symbolTable.getValue("myVar"),
                "variable has correct value");
    }

    @Test
    public void testLocalVariablesStrictTyping() throws JmriException {

        LogixNGPreferences prefs = InstanceManager.getDefault(LogixNGPreferences.class);
        prefs.setStrictTypingLocalVariables(true);

        // Test strings
        SymbolTable symbolTable = createLocalVariable(InitialValueType.String, "Hello");
        assertEquals( "Hello", symbolTable.getValue("myVar"),
                "variable has correct value");

        symbolTable.setValue("myVar", 25.3);
        assertEquals( "25.3", symbolTable.getValue("myVar"),
                "variable has correct value");

        symbolTable.setValue("myVar", "34");
        assertEquals( "34", symbolTable.getValue("myVar"),
                "variable has correct value");

        symbolTable.setValue("myVar", 12);
        assertEquals( "12", symbolTable.getValue("myVar"),
                "variable has correct value");

        // Test booleans
        symbolTable = createLocalVariable(InitialValueType.Boolean, "True");
        assertTrue( (boolean)symbolTable.getValue("myVar"),
                "variable has correct value");

        symbolTable = createLocalVariable(InitialValueType.Boolean, "False");
        assertFalse( (boolean)symbolTable.getValue("myVar"),
                "variable has correct value");

        symbolTable = createLocalVariable(InitialValueType.Boolean, "true");
        assertTrue( (boolean)symbolTable.getValue("myVar"),
                "variable has correct value");

        symbolTable = createLocalVariable(InitialValueType.Boolean, "false");
        assertFalse( (boolean)symbolTable.getValue("myVar"),
                "variable has correct value");

        // Test integers
        SymbolTable symbolTable2 = createLocalVariable(InitialValueType.Integer, "42");
        assertEquals( 42, (long)symbolTable2.getValue("myVar"), 0.00000001,
                "variable has correct value");

        NumberFormatException ex = assertThrows(NumberFormatException.class, () -> {
            symbolTable2.setValue("myVar", 25.3);
        });
        assertNotNull(ex);

        symbolTable2.setValue("myVar", "34");
        assertEquals( 34, (long)symbolTable2.getValue("myVar"),
                "variable has correct value");

        symbolTable2.setValue("myVar", 12);
        assertEquals( 12, (long)symbolTable2.getValue("myVar"),
                "variable has correct value");

        // Test floating point numbers
        symbolTable = createLocalVariable(InitialValueType.FloatingNumber, "42.11");
        assertEquals( 42.11, (double)symbolTable.getValue("myVar"), 0.00000001,
                "variable has correct value");

        symbolTable.setValue("myVar", 25.3);
        assertEquals( 25.3, (double)symbolTable.getValue("myVar"), 0.00000001,
                "variable has correct value");

        symbolTable.setValue("myVar", "34");
        assertEquals( 34.0, (double)symbolTable.getValue("myVar"), 0.00000001,
                "variable has correct value");

        symbolTable.setValue("myVar", 12);
        assertEquals( 12.0, (double)symbolTable.getValue("myVar"), 0.00000001,
                "variable has correct value");
    }

    @Test
    public void testInitializeLocalVariables() throws JmriException {
        for (InitialValueType type : InitialValueType.values()) {
            if (null == type) {
                // passing null as 1st createLocalVariable Arg rather than type
                // avoids a Spotbug even though we know it's null . . . .
                SymbolTable symbolTable = createLocalVariable( null, null);
                assertNull( symbolTable.getValue("myVar"), "variable is null");
            } else {
                IllegalArgumentException ex;

                SymbolTable symbolTable;

                switch (type) {
                    case None:
                        symbolTable = createLocalVariable(type, null);
                        assertNull( symbolTable.getValue("myVar"), "None variable is null");
                        break;

                    case String:
                        symbolTable = createLocalVariable(type, null);
                        assertNull( symbolTable.getValue("myVar"), "String variable is null");
                        break;

                    case Boolean:
                        ex = assertThrows(IllegalArgumentException.class, () -> {
                            createLocalVariable(type, null);
                        });
                        assertEquals("Initial data is null for local variable \"myVar\". Can't set value to boolean.",
                            ex.getMessage());
                        break;

                    case Integer:
                        ex = assertThrows(IllegalArgumentException.class, () -> {
                            createLocalVariable(type, null);
                        });
                        assertEquals("Initial data is null for local variable \"myVar\". Can't set value to integer.",
                                ex.getMessage());
                        break;

                    case FloatingNumber:
                        ex = assertThrows(IllegalArgumentException.class, () -> {
                            createLocalVariable(type, null);
                        });
                        assertEquals("Initial data is null for local variable \"myVar\". Can't set value to floating number.",
                                ex.getMessage());
                        break;

                    case Map:
                        symbolTable = createLocalVariable(type, null);
                        Map<?,?> map = assertInstanceOf( Map.class, symbolTable.getValue("myVar"), "variable is a map");
                        assertTrue( map.isEmpty(), "map is empty");
                        break;

                    case Array:
                        symbolTable = createLocalVariable(type, null);
                        List<?> list = assertInstanceOf( List.class, symbolTable.getValue("myVar"), "variable is a list");
                        assertTrue( list.isEmpty(), "list is empty");
                        break;

                    case Object:
                        symbolTable = createLocalVariable(type, null);
                        assertNull( symbolTable.getValue("myVar"), "Object variable is null");
                        break;

                    default:
                        ex = assertThrows(IllegalArgumentException.class, () -> {
                            createLocalVariable(type, null);
                        });
                        assertEquals(String.format(
                                "Initial data is null for local variable \"myVar\". Can't set value from %s.",
                                type.toString().toLowerCase()),
                                ex.getMessage());
                }
                assertTrue( JUnitAppender.getBacklog().isEmpty(), "backlog is empty");
            }
        }
    }

    @Test
    public void testInitializeGlobalVariables() throws JmriException {
        GlobalVariableManager mgr = InstanceManager.getDefault(GlobalVariableManager.class);

        for (InitialValueType type : InitialValueType.values()) {

            GlobalVariable globalVariable = mgr.createGlobalVariable("myVar");
            globalVariable.setInitialValueType(type);
            globalVariable.setInitialValueData(null);

            if (null == type) {
                globalVariable.initialize();
                // passing null as 1st createLocalVariable Arg rather than type
                // avoids a Spotbug even though we know it's null . . . .
                SymbolTable symbolTable = createLocalVariable(null, null);
                assertNull( symbolTable.getValue("myVar"), "variable is null");
            } else {
                IllegalArgumentException ex;

                switch (type) {
                    case None:
                        globalVariable.initialize();
                        assertNull( globalVariable.getValue(), "None variable is null");
                        break;

                    case String:
                        globalVariable.initialize();
                        assertNull( globalVariable.getValue(), "String variable is null");
                        break;

                    case Boolean:
                        ex = assertThrows(IllegalArgumentException.class, () -> {
                            globalVariable.initialize();
                        });
                        assertEquals("Initial data is null for global variable \"myVar\". Can't set value to boolean.",
                                ex.getMessage());
                        break;

                    case Integer:
                        ex = assertThrows(IllegalArgumentException.class, () -> {
                            globalVariable.initialize();
                        });
                        assertEquals("Initial data is null for global variable \"myVar\". Can't set value to integer.",
                                ex.getMessage());
                        break;

                    case FloatingNumber:
                        ex = assertThrows(IllegalArgumentException.class, () -> {
                            globalVariable.initialize();
                        });
                        assertEquals("Initial data is null for global variable \"myVar\". Can't set value to floating number.",
                                ex.getMessage());
                        break;

                    case Map:
                        globalVariable.initialize();
                        Map<?,?> map = assertInstanceOf( Map.class, globalVariable.getValue(), "variable is a map");
                        assertTrue( map.isEmpty(), "map is empty");
                        break;

                    case Array:
                        globalVariable.initialize();
                        List<?> list = assertInstanceOf( List.class, globalVariable.getValue(), "variable is a list");
                        assertTrue( list.isEmpty(), "list is empty");
                        break;

                    case Object:
                        globalVariable.initialize();
                        assertNull( globalVariable.getValue(), "Object variable is null");
                        break;

                    default:
                        ex = assertThrows(IllegalArgumentException.class, () -> {
                            globalVariable.initialize();
                        });
                        assertEquals(String.format(
                                "Initial data is null for global variable \"myVar\". Can't set value from %s.",
                                type.toString().toLowerCase()),
                                ex.getMessage());
                }
                assertTrue( JUnitAppender.getBacklog().isEmpty(), "backlog is empty");
            }
            mgr.deleteGlobalVariable(globalVariable);
        }
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initLogixNGManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
