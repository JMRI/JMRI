package jmri.jmrit.logixng;

import java.util.ArrayList;
import java.util.List;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.jmrit.logixng.SymbolTable.InitialValueType;
import jmri.jmrit.logixng.implementation.DefaultConditionalNG;
import jmri.jmrit.logixng.implementation.DefaultSymbolTable;
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
