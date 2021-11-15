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
 * Test Category
 * 
 * @author Daniel Bergqvist 2018
 */
public class TableTest {

    private void expectException(Runnable r, Class<? extends Exception> exceptionClass, String errorMessage) {
        boolean exceptionThrown = false;
        try {
            r.run();
        } catch (Exception e) {
            Assert.assertTrue("Exception is correct", e.getClass() == exceptionClass);
            Assert.assertEquals("Exception message is correct", errorMessage, e.getMessage());
            exceptionThrown = true;
        }
        Assert.assertTrue("Exception is thrown", exceptionThrown);
    }
    
    @Test
    @SuppressWarnings("ResultOfMethodCallIgnored")  // This method test thrown exceptions
    public void testExceptions() {
        Table t = new MyTable();
        
        expectException(() -> {
            t.getCell("Bad row", "Seventh column");
        }, Table.RowNotFoundException.class, "Row \"Bad row\" is not found");
        
        expectException(() -> {
            t.getCell("Second row", "Bad column");
        }, Table.ColumnNotFoundException.class, "Column \"Bad column\" is not found");
        
        expectException(() -> {
            t.setCell("Hello", "Bad row", "Seventh column");
        }, Table.RowNotFoundException.class, "Row \"Bad row\" is not found");
        
        expectException(() -> {
            t.setCell("Hello", "Second row", "Bad column");
        }, Table.ColumnNotFoundException.class, "Column \"Bad column\" is not found");
    }
    
    @Test
    public void testTable() {
        Table t = new MyTable();
        
        Assert.assertTrue("Item is null", null == t.getCell("Second row", "Seventh column"));
        
        t.setCell("Hello", "Third row");
        Assert.assertTrue("Item has correct value", "Hello" == t.getCell("Third row"));
        Assert.assertTrue("Item has correct value", "Hello" == t.getCell("Third row", "First column"));
        
        t.setCell("Hello again", "Second row", "Seventh column");
        Assert.assertTrue("Item has correct value", "Hello again" == t.getCell("Second row", "Seventh column"));
        
        Integer i = 15;
        t.setCell(i, "Second row", "Seventh column");
        Assert.assertTrue("Item has correct value", t.getCell("Second row", "Seventh column").equals(15));
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
            if (i == null) throw new RowNotFoundException(rowName);
            return i;
        }

        @Override
        public int getColumnNumber(String columnName) {
            Integer i = columnHeaders.get(columnName);
            if (i == null) throw new ColumnNotFoundException(columnName);
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
