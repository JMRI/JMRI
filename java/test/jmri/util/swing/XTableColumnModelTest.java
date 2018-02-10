package jmri.util.swing;

import java.util.Enumeration;
import javax.swing.table.TableColumn;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Randall Wood Copyright 2018
 */
public class XTableColumnModelTest {

    private static final String COLUMN1 = "column1";
    private static final String COLUMN2 = "column2";
    private static final String COLUMN3 = "column3";

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    /**
     * Test of setColumnVisible method, of class XTableColumnModel.
     */
    @Test
    public void testSetColumnVisible() {
        XTableColumnModel instance = testModel();
        TableColumn column = instance.getColumn(0);
        Assert.assertTrue(instance.isColumnVisible(column));
        instance.setColumnVisible(column, false);
        Assert.assertFalse(instance.isColumnVisible(column));
        instance.setColumnVisible(column, true);
        Assert.assertTrue(instance.isColumnVisible(column));
    }

    /**
     * Test of setAllColumnsVisible method, of class XTableColumnModel.
     */
    @Test
    public void testSetAllColumnsVisible() {
        XTableColumnModel instance = testModel();
        TableColumn column1 = instance.getColumn(0);
        TableColumn column2 = instance.getColumn(1);
        TableColumn column3 = instance.getColumn(2);
        instance.setColumnVisible(column1, false);
        instance.setColumnVisible(column2, false);
        Assert.assertFalse(instance.isColumnVisible(column1));
        Assert.assertFalse(instance.isColumnVisible(column2));
        Assert.assertTrue(instance.isColumnVisible(column3));
        instance.setAllColumnsVisible();
        Assert.assertTrue(instance.isColumnVisible(column1));
        Assert.assertTrue(instance.isColumnVisible(column2));
        Assert.assertTrue(instance.isColumnVisible(column3));
    }

    /**
     * Test of getColumnByModelIndex method, of class XTableColumnModel.
     */
    @Test
    public void testGetColumnByModelIndex() {
        // all columns have modelIndex 0 until associated with columns from a
        // data model or explicitly set (as in this test)
        XTableColumnModel instance = testModel();
        TableColumn column1 = instance.getColumn(0);
        TableColumn column2 = instance.getColumn(1);
        TableColumn column3 = instance.getColumn(2);
        Assert.assertEquals(0, column1.getModelIndex());
        Assert.assertEquals(0, column2.getModelIndex());
        Assert.assertEquals(0, column3.getModelIndex());
        Assert.assertEquals(column1, instance.getColumnByModelIndex(0));
        Assert.assertNull(instance.getColumnByModelIndex(1));
        Assert.assertNull(instance.getColumnByModelIndex(2));
        Assert.assertNull(instance.getColumnByModelIndex(3));
        column1.setModelIndex(3);
        Assert.assertEquals(column1, instance.getColumnByModelIndex(3));
        Assert.assertEquals(column2, instance.getColumnByModelIndex(0));
        Assert.assertNull(instance.getColumnByModelIndex(1));
        Assert.assertNull(instance.getColumnByModelIndex(2));
        column2.setModelIndex(1);
        Assert.assertEquals(column1, instance.getColumnByModelIndex(3));
        Assert.assertEquals(column2, instance.getColumnByModelIndex(1));
        Assert.assertEquals(column3, instance.getColumnByModelIndex(0));
        Assert.assertNull(instance.getColumnByModelIndex(2));
        column1.setModelIndex(0);
        column2.setModelIndex(1);
        column3.setModelIndex(2);
        Assert.assertEquals(column1, instance.getColumnByModelIndex(0));
        Assert.assertEquals(column2, instance.getColumnByModelIndex(1));
        Assert.assertEquals(column3, instance.getColumnByModelIndex(2));
        Assert.assertNull(instance.getColumnByModelIndex(3));
    }

    /**
     * Test of isColumnVisible method, of class XTableColumnModel.
     */
    @Test
    public void testIsColumnVisible() {
        TableColumn column = new TableColumn();
        XTableColumnModel instance = new XTableColumnModel();
        instance.addColumn(column);
        Assert.assertTrue(instance.isColumnVisible(column));
        instance.setColumnVisible(column, false);
        Assert.assertFalse(instance.isColumnVisible(column));
        instance.setColumnVisible(column, true);
        Assert.assertTrue(instance.isColumnVisible(column));
    }

    /**
     * Test of addColumn method, of class XTableColumnModel.
     */
    @Test
    public void testAddColumn() {
        TableColumn column = new TableColumn();
        column.setIdentifier(COLUMN1);
        XTableColumnModel instance = new XTableColumnModel();
        Assert.assertEquals(0, instance.getColumnCount());
        Assert.assertEquals(0, instance.getColumnCount(false));
        try {
            instance.getColumnIndex(COLUMN1);
            Assert.fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            // passes if caught
        }
        instance.addColumn(column);
        Assert.assertEquals(1, instance.getColumnCount());
        Assert.assertEquals(1, instance.getColumnCount(false));
        Assert.assertEquals(0, instance.getColumnIndex(COLUMN1));
    }

    /**
     * Test of removeColumn method, of class XTableColumnModel.
     */
    @Test
    public void testRemoveColumn() {
        TableColumn column = new TableColumn();
        XTableColumnModel instance = new XTableColumnModel();
        instance.addColumn(column);
        Assert.assertEquals(1, instance.getColumnCount());
        Assert.assertEquals(1, instance.getColumnCount(false));
        instance.removeColumn(column);
        Assert.assertEquals(0, instance.getColumnCount());
        Assert.assertEquals(0, instance.getColumnCount(false));
    }

    /**
     * Test of moveColumn method, of class XTableColumnModel.
     */
    @Test
    public void testMoveColumn_int_int() {
        XTableColumnModel instance = testModel();
        TableColumn column1 = instance.getColumn(0);
        TableColumn column2 = instance.getColumn(1);
        TableColumn column3 = instance.getColumn(2);
        instance.moveColumn(0, 0);
        Assert.assertEquals(column1, instance.getColumn(0));
        Assert.assertEquals(column2, instance.getColumn(1));
        Assert.assertEquals(column3, instance.getColumn(2));
        instance.moveColumn(0, 2);
        Assert.assertEquals(column2, instance.getColumn(0));
        Assert.assertEquals(column3, instance.getColumn(1));
        Assert.assertEquals(column1, instance.getColumn(2));
        instance.setColumnVisible(column1, false);
        try {
            instance.moveColumn(2, 0);
            Assert.fail("Expected IllegalArgumentException not thrown");
        } catch (IllegalArgumentException ex) {
            // passes
        } catch (Exception ex) {
            Assert.fail("Unexpected Exception thrown: " + ex.getMessage());
        }
        Assert.assertEquals(column2, instance.getColumn(0));
        Assert.assertEquals(column3, instance.getColumn(1));
        try {
            instance.getColumn(2);
            Assert.fail("Expected ArrayIndexOutOfBoundsException not thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            // pass
        } catch (Exception ex) {
            Assert.fail("Unexpected Exception thrown: " + ex.getMessage());
        }
        instance.setColumnVisible(column1, true);
        instance.moveColumn(2, 0);
        Assert.assertEquals(column1, instance.getColumn(0));
        Assert.assertEquals(column2, instance.getColumn(1));
        Assert.assertEquals(column3, instance.getColumn(2));
    }

    /**
     * Test of moveColumn method, of class XTableColumnModel.
     */
    @Test
    public void testMoveColumn_3args() {
        XTableColumnModel instance = testModel();
        TableColumn column1 = instance.getColumn(0);
        TableColumn column2 = instance.getColumn(1);
        TableColumn column3 = instance.getColumn(2);
        boolean onlyVisible = true;
        //
        // move only visible columns (third arg is true)
        //
        instance.moveColumn(0, 0, onlyVisible);
        Assert.assertEquals(column1, instance.getColumn(0));
        Assert.assertEquals(column2, instance.getColumn(1));
        Assert.assertEquals(column3, instance.getColumn(2));
        instance.moveColumn(0, 2, onlyVisible);
        Assert.assertEquals(column2, instance.getColumn(0));
        Assert.assertEquals(column3, instance.getColumn(1));
        Assert.assertEquals(column1, instance.getColumn(2));
        instance.setColumnVisible(column1, false);
        try {
            instance.moveColumn(2, 0, onlyVisible);
            Assert.fail("Expected IllegalArgumentException not thrown");
        } catch (IllegalArgumentException ex) {
            // passes
        } catch (Exception ex) {
            Assert.fail("Unexpected Exception thrown: " + ex.getMessage());
        }
        Assert.assertEquals(column2, instance.getColumn(0));
        Assert.assertEquals(column3, instance.getColumn(1));
        try {
            instance.getColumn(2);
            Assert.fail("Expected ArrayIndexOutOfBoundsException not thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            // pass
        } catch (Exception ex) {
            Assert.fail("Unexpected Exception thrown: " + ex.getMessage());
        }
        instance.setColumnVisible(column1, true);
        instance.moveColumn(2, 0, onlyVisible);
        Assert.assertEquals(column1, instance.getColumn(0));
        Assert.assertEquals(column2, instance.getColumn(1));
        Assert.assertEquals(column3, instance.getColumn(2));
        //
        // move hidden or visible columns (third arg is false)
        //
        onlyVisible = false;
        instance.moveColumn(0, 0, onlyVisible);
        Assert.assertEquals(column1, instance.getColumn(0));
        Assert.assertEquals(column2, instance.getColumn(1));
        Assert.assertEquals(column3, instance.getColumn(2));
        instance.moveColumn(0, 2, onlyVisible);
        Assert.assertEquals(column2, instance.getColumn(0));
        Assert.assertEquals(column3, instance.getColumn(1));
        Assert.assertEquals(column1, instance.getColumn(2));
        instance.setColumnVisible(column1, false);
        instance.moveColumn(2, 0, onlyVisible);
        Assert.assertEquals(column2, instance.getColumn(0));
        Assert.assertEquals(column3, instance.getColumn(1));
        Assert.assertEquals(column1, instance.getColumn(0, onlyVisible));
        Assert.assertEquals(column2, instance.getColumn(1, onlyVisible));
        Assert.assertEquals(column3, instance.getColumn(2, onlyVisible));
        try {
            instance.getColumn(2, !onlyVisible);
            Assert.fail("Expected ArrayIndexOutOfBoundsException not thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            // pass
        } catch (Exception ex) {
            Assert.fail("Unexpected Exception thrown: " + ex.getMessage());
        }
        instance.setColumnVisible(column1, true);
        Assert.assertEquals(column1, instance.getColumn(0));
        Assert.assertEquals(column2, instance.getColumn(1));
        Assert.assertEquals(column3, instance.getColumn(2));
        instance.moveColumn(2, 0, onlyVisible);
        Assert.assertEquals(column3, instance.getColumn(0));
        Assert.assertEquals(column1, instance.getColumn(1));
        Assert.assertEquals(column2, instance.getColumn(2));
    }

    /**
     * Test of getColumnCount method, of class XTableColumnModel.
     */
    @Test
    public void testGetColumnCount() {
        XTableColumnModel instance = testModel();
        Assert.assertEquals(3, instance.getColumnCount(true));
        Assert.assertEquals(3, instance.getColumnCount(false));
        instance.setColumnVisible(instance.getColumn(0), false);
        Assert.assertEquals(2, instance.getColumnCount(true));
        Assert.assertEquals(3, instance.getColumnCount(false));
    }

    /**
     * Test of getColumns method, of class XTableColumnModel.
     */
    @Test
    public void testGetColumns() {
        XTableColumnModel instance = testModel();
        // all columns visible
        int count = 0;
        Enumeration<TableColumn> e = instance.getColumns(true);
        while (e.hasMoreElements()) {
            e.nextElement();
            count++;
        }
        Assert.assertEquals(3, count);
        // hide one column
        count = 0;
        instance.setColumnVisible(instance.getColumn(0), false);
        e = instance.getColumns(true);
        while (e.hasMoreElements()) {
            e.nextElement();
            count++;
        }
        Assert.assertEquals(2, count);
        count = 0;
        e = instance.getColumns(false);
        while (e.hasMoreElements()) {
            e.nextElement();
            count++;
        }
        Assert.assertEquals(3, count);
    }

    /**
     * Test of getColumnIndex method, of class XTableColumnModel.
     */
    @Test
    public void testGetColumnIndex() {
        XTableColumnModel instance = testModel();
        TableColumn column1 = instance.getColumn(0);
        Assert.assertEquals(0, instance.getColumnIndex(COLUMN1, true));
        Assert.assertEquals(0, instance.getColumnIndex(COLUMN1, false));
        instance.setColumnVisible(column1, false);
        try {
            // throws exception if requiring visible column
            instance.getColumnIndex(COLUMN1, true);
            Assert.fail("Expected IllegalArgumentException not thrown");
        } catch (IllegalArgumentException ex) {
            // pass
        } catch (Exception ex) {
            Assert.fail("Unexpected Exception thrown: " + ex.getMessage());
        }
        // returns index if allowing hidden column
        Assert.assertEquals(0, instance.getColumnIndex(COLUMN1, false));
    }

    /**
     * Test of getColumn method, of class XTableColumnModel.
     */
    @Test
    public void testGetColumn() {
        XTableColumnModel instance = testModel();
        TableColumn column1 = instance.getColumn(0);
        TableColumn column2 = instance.getColumn(0);
        instance.setColumnVisible(column1, false);
        // because column1 is hidden, requesting the first visible column should
        // return column2 when onlyVisible is true
        Assert.assertNotEquals(column2, instance.getColumn(0, true));
        Assert.assertEquals(instance.getColumn(0), instance.getColumn(0, true));
        // when onlyVisible is false column1 is still first column
        Assert.assertEquals(column1, instance.getColumn(0, false));
        Assert.assertNotEquals(instance.getColumn(0), instance.getColumn(0, false));
    }

    /**
     * Create model with three columns, all visible.
     *
     * @return the new model
     */
    private XTableColumnModel testModel() {
        XTableColumnModel instance = new XTableColumnModel();
        TableColumn column1 = new TableColumn();
        column1.setIdentifier(COLUMN1);
        TableColumn column2 = new TableColumn();
        column2.setIdentifier(COLUMN2);
        TableColumn column3 = new TableColumn();
        column3.setIdentifier(COLUMN3);
        instance.addColumn(column1);
        instance.addColumn(column2);
        instance.addColumn(column3);
        return instance;
    }
}
