package jmri.jmrit.logixng;

import java.util.*;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.jmrit.logixng.SymbolTable.InitialValueType;
import jmri.jmrit.logixng.implementation.DefaultConditionalNG;
import jmri.jmrit.logixng.implementation.DefaultSymbolTable;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
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
        Assert.assertTrue(SymbolTable.validateName("Abc"));
        Assert.assertTrue(SymbolTable.validateName("abc"));
        Assert.assertTrue(SymbolTable.validateName("Abc123"));
        Assert.assertTrue(SymbolTable.validateName("A123bc"));
        Assert.assertTrue(SymbolTable.validateName("Abc___"));
        Assert.assertTrue(SymbolTable.validateName("Abc___fsdffs"));
        Assert.assertTrue(SymbolTable.validateName("Abc3123__2341fsdf"));

        // Invalid names
        Assert.assertFalse(SymbolTable.validateName("12Abc"));  // Starts with a digit
        Assert.assertFalse(SymbolTable.validateName("_Abc"));   // Starts with an underscore
        Assert.assertFalse(SymbolTable.validateName(" Abc"));   // Starts with a non letter
        Assert.assertFalse(SymbolTable.validateName("A bc"));   // Has a character that's not letter, digit or underscore
        Assert.assertFalse(SymbolTable.validateName("A{bc"));   // Has a character that's not letter, digit or underscore
        Assert.assertFalse(SymbolTable.validateName("A+bc"));   // Has a character that's not letter, digit or underscore
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
        Assert.assertEquals("variable has correct value",
                "Hello", symbolTable.getValue("myVar"));

        symbolTable.setValue("myVar", 25.3);
        Assert.assertEquals("variable has correct value",
                25.3, (double)symbolTable.getValue("myVar"), 0.00000001);

        symbolTable.setValue("myVar", "34");
        Assert.assertEquals("variable has correct value",
                "34", symbolTable.getValue("myVar"));

        symbolTable.setValue("myVar", 12);
        Assert.assertEquals("variable has correct value",
                12, (int)symbolTable.getValue("myVar"));

        // Test integers
        symbolTable = createLocalVariable(InitialValueType.FloatingNumber, "42.11");
        Assert.assertEquals("variable has correct value",
                42.11, (double)symbolTable.getValue("myVar"), 0.00000001);

        symbolTable.setValue("myVar", 25.3);
        Assert.assertEquals("variable has correct value",
                25.3, (double)symbolTable.getValue("myVar"), 0.00000001);

        symbolTable.setValue("myVar", "34");
        Assert.assertEquals("variable has correct value",
                "34", symbolTable.getValue("myVar"));

        symbolTable.setValue("myVar", 12);
        Assert.assertEquals("variable has correct value",
                12, (int)symbolTable.getValue("myVar"));

        // Test floating point numbers
        symbolTable = createLocalVariable(InitialValueType.FloatingNumber, "42.11");
        Assert.assertEquals("variable has correct value",
                42.11, (double)symbolTable.getValue("myVar"), 0.00000001);

        symbolTable.setValue("myVar", 25.3);
        Assert.assertEquals("variable has correct value",
                25.3, (double)symbolTable.getValue("myVar"), 0.00000001);

        symbolTable.setValue("myVar", "34");
        Assert.assertEquals("variable has correct value",
                "34", symbolTable.getValue("myVar"));

        symbolTable.setValue("myVar", 12);
        Assert.assertEquals("variable has correct value",
                12, (int)symbolTable.getValue("myVar"));
    }

    @Test
    public void testLocalVariablesStrictTyping() throws JmriException {

        LogixNGPreferences prefs = InstanceManager.getDefault(LogixNGPreferences.class);
        prefs.setStrictTypingLocalVariables(true);

        // Test strings
        SymbolTable symbolTable = createLocalVariable(InitialValueType.String, "Hello");
        Assert.assertEquals("variable has correct value",
                "Hello", symbolTable.getValue("myVar"));

        symbolTable.setValue("myVar", 25.3);
        Assert.assertEquals("variable has correct value",
                "25.3", symbolTable.getValue("myVar"));

        symbolTable.setValue("myVar", "34");
        Assert.assertEquals("variable has correct value",
                "34", symbolTable.getValue("myVar"));

        symbolTable.setValue("myVar", 12);
        Assert.assertEquals("variable has correct value",
                "12", symbolTable.getValue("myVar"));

        // Test booleans
        symbolTable = createLocalVariable(InitialValueType.Boolean, "True");
        Assert.assertTrue("variable has correct value",
                (boolean)symbolTable.getValue("myVar"));

        symbolTable = createLocalVariable(InitialValueType.Boolean, "False");
        Assert.assertFalse("variable has correct value",
                (boolean)symbolTable.getValue("myVar"));

        symbolTable = createLocalVariable(InitialValueType.Boolean, "true");
        Assert.assertTrue("variable has correct value",
                (boolean)symbolTable.getValue("myVar"));

        symbolTable = createLocalVariable(InitialValueType.Boolean, "false");
        Assert.assertFalse("variable has correct value",
                (boolean)symbolTable.getValue("myVar"));

        // Test integers
        SymbolTable symbolTable2 = createLocalVariable(InitialValueType.Integer, "42");
        Assert.assertEquals("variable has correct value",
                42, (long)symbolTable2.getValue("myVar"), 0.00000001);

        Assertions.assertThrows(NumberFormatException.class, () -> {
            symbolTable2.setValue("myVar", 25.3);
        });

        symbolTable2.setValue("myVar", "34");
        Assert.assertEquals("variable has correct value",
                34, (long)symbolTable2.getValue("myVar"));

        symbolTable2.setValue("myVar", 12);
        Assert.assertEquals("variable has correct value",
                12, (long)symbolTable2.getValue("myVar"));

        // Test floating point numbers
        symbolTable = createLocalVariable(InitialValueType.FloatingNumber, "42.11");
        Assert.assertEquals("variable has correct value",
                42.11, (double)symbolTable.getValue("myVar"), 0.00000001);

        symbolTable.setValue("myVar", 25.3);
        Assert.assertEquals("variable has correct value",
                25.3, (double)symbolTable.getValue("myVar"), 0.00000001);

        symbolTable.setValue("myVar", "34");
        Assert.assertEquals("variable has correct value",
                34.0, (double)symbolTable.getValue("myVar"), 0.00000001);

        symbolTable.setValue("myVar", 12);
        Assert.assertEquals("variable has correct value",
                12.0, (double)symbolTable.getValue("myVar"), 0.00000001);
    }

    @Test
    public void testInitializeLocalVariables() throws JmriException {
        for (InitialValueType type : InitialValueType.values()) {
            if (null == type) {
                SymbolTable symbolTable = createLocalVariable(type, null);
                Assert.assertNull("variable is null", symbolTable.getValue("myVar"));
            } else {
                IllegalArgumentException ex;

                SymbolTable symbolTable;

                switch (type) {
                    case None:
                        symbolTable = createLocalVariable(type, null);
                        Assert.assertNull("variable is null", symbolTable.getValue("myVar"));
                        break;

                    case String:
                        symbolTable = createLocalVariable(type, null);
                        Assert.assertNull("variable is null", symbolTable.getValue("myVar"));
                        break;

                    case Boolean:
                        ex = Assertions.assertThrows(IllegalArgumentException.class, () -> {
                            createLocalVariable(type, null);
                        });
                        Assert.assertEquals("Initial data is null for local variable \"myVar\". Can't set value to boolean.", ex.getMessage());
                        break;

                    case Integer:
                        ex = Assertions.assertThrows(IllegalArgumentException.class, () -> {
                            createLocalVariable(type, null);
                        });
                        Assert.assertEquals("Initial data is null for local variable \"myVar\". Can't set value to integer.", ex.getMessage());
                        break;

                    case FloatingNumber:
                        ex = Assertions.assertThrows(IllegalArgumentException.class, () -> {
                            createLocalVariable(type, null);
                        });
                        Assert.assertEquals("Initial data is null for local variable \"myVar\". Can't set value to floating number.", ex.getMessage());
                        break;

                    case Map:
                        symbolTable = createLocalVariable(type, null);
                        Assert.assertTrue("variable is a map", symbolTable.getValue("myVar") instanceof Map);
                        Assert.assertTrue("map is empty", ((Map)symbolTable.getValue("myVar")).isEmpty());
                        break;

                    case Array:
                        symbolTable = createLocalVariable(type, null);
                        Assert.assertTrue("variable is a list", symbolTable.getValue("myVar") instanceof List);
                        Assert.assertTrue("list is empty", ((List)symbolTable.getValue("myVar")).isEmpty());
                        break;

                    default:
                        ex = Assertions.assertThrows(IllegalArgumentException.class, () -> {
                            createLocalVariable(type, null);
                        });
                        Assert.assertEquals(String.format(
                                "Initial data is null for local variable \"myVar\". Can't set value from %s.",
                                type.toString().toLowerCase()),
                                ex.getMessage());
                }
                Assert.assertTrue("backlog is empty", JUnitAppender.getBacklog().isEmpty());
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
                SymbolTable symbolTable = createLocalVariable(type, null);
                Assert.assertNull("variable is null", symbolTable.getValue("myVar"));
            } else {
                IllegalArgumentException ex;

                switch (type) {
                    case None:
                        globalVariable.initialize();
                        Assert.assertNull("variable is null", globalVariable.getValue());
                        break;

                    case String:
                        globalVariable.initialize();
                        Assert.assertNull("variable is null", globalVariable.getValue());
                        break;

                    case Boolean:
                        ex = Assertions.assertThrows(IllegalArgumentException.class, () -> {
                            globalVariable.initialize();
                        });
                        Assert.assertEquals("Initial data is null for global variable \"myVar\". Can't set value to boolean.", ex.getMessage());
                        break;

                    case Integer:
                        ex = Assertions.assertThrows(IllegalArgumentException.class, () -> {
                            globalVariable.initialize();
                        });
                        Assert.assertEquals("Initial data is null for global variable \"myVar\". Can't set value to integer.", ex.getMessage());
                        break;

                    case FloatingNumber:
                        ex = Assertions.assertThrows(IllegalArgumentException.class, () -> {
                            globalVariable.initialize();
                        });
                        Assert.assertEquals("Initial data is null for global variable \"myVar\". Can't set value to floating number.", ex.getMessage());
                        break;

                    case Map:
                        globalVariable.initialize();
                        Assert.assertTrue("variable is a map", globalVariable.getValue() instanceof Map);
                        Assert.assertTrue("map is empty", ((Map)globalVariable.getValue()).isEmpty());
                        break;

                    case Array:
                        globalVariable.initialize();
                        Assert.assertTrue("variable is a list", globalVariable.getValue() instanceof List);
                        Assert.assertTrue("list is empty", ((List)globalVariable.getValue()).isEmpty());
                        break;

                    default:
                        ex = Assertions.assertThrows(IllegalArgumentException.class, () -> {
                            globalVariable.initialize();
                        });
                        Assert.assertEquals(String.format(
                                "Initial data is null for global variable \"myVar\". Can't set value from %s.",
                                type.toString().toLowerCase()),
                                ex.getMessage());
                }
                Assert.assertTrue("backlog is empty", JUnitAppender.getBacklog().isEmpty());
            }
            mgr.deleteGlobalVariable(globalVariable);
        }
    }

    // The minimal setup for log4J
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
