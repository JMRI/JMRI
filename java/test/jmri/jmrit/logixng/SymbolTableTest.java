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
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.tearDown();
    }
    
    
    private static class MyTable implements Table {

        private static final int NUM_ROWS = 3;
        private static final int NUM_COLUMNS = 8;
        private final Object[][] _data = new Object[NUM_ROWS+1][NUM_COLUMNS+1];
        private final Map<String, Integer> rowHeaders = new HashMap<>();
        private final Map<String, Integer> columnHeaders = new HashMap<>();
        
        public MyTable() {
            _data[1][0] = "First row";
            _data[2][0] = "Second row";
            _data[3][0] = "Third row";
            _data[0][1] = "First column";
            _data[0][2] = "Second column";
            _data[0][3] = "Third column";
            _data[0][4] = "Forth column";
            _data[0][5] = "Fifth column";
            _data[0][6] = "Sixth column";
            _data[0][7] = "Seventh column";
            _data[0][8] = "Eighth column";
            for (int i=1; i <= NUM_ROWS; i++) rowHeaders.put(_data[i][0].toString(), i);
            for (int i=1; i <= NUM_COLUMNS; i++) columnHeaders.put(_data[0][i].toString(), i);
        }
        
        @Override
        public Object getCell(int row, int column) {
            return _data[row][column];
        }

        @Override
        public void setCell(Object value, int row, int column) {
            _data[row][column] = value;
        }

        @Override
        public int numRows() {
            return NUM_ROWS;
        }

        @Override
        public int numColumns() {
            return NUM_COLUMNS;
        }

        @Override
        public int getRowNumber(String rowName) {
            Integer i = rowHeaders.get(rowName);
            return i != null ? i : -1;
        }

        @Override
        public int getColumnNumber(String columnName) {
            Integer i = columnHeaders.get(columnName);
            if (i == null) {
                try {
                    i = Integer.parseInt(columnName);
                } catch (NumberFormatException e) {
                    return -1;
                }
            }
            return i;
        }

        @Override
        public void storeTableAsCSV(File file) throws FileNotFoundException {
            throw new UnsupportedOperationException("44Not supported.");
        }

        @Override
        public void storeTableAsCSV(File file, String systemName, String userName) throws FileNotFoundException {
            throw new UnsupportedOperationException("55Not supported.");
        }
        
    }
    
}
