package jmri.jmrit.logixng;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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
    
    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initLogixNGManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
