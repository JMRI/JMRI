package jmri.jmrit.consisttool;

import jmri.ConsistManager;
import jmri.InstanceManager;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of ConsistDataModel
 *
 * @author	Paul Bender Copyright (C) 2015,2016
 */
public class ConsistDataModelTest {

    private ConsistDataModel model;

    @Test
    public void testCtor() {
        Assert.assertNotNull("exists", model);
    }

    @Test
    public void testGetRowCountNullConsist() {
        Assert.assertEquals("consist table model size",0, model.getRowCount());
    }

    @Test
    public void testGetColumnCount() {
        Assert.assertEquals("consist column count",4, model.getColumnCount());
    }

    @Test
    public void testGetColumnNameAddress() {
        // this uses an internal detail (The private column number).  If the
        // column numbering changes, the test may need to change.
        Assert.assertEquals("address column name",Bundle.getMessage("AddressColumnLabel"), model.getColumnName(0));
    }

    @Test
    public void testGetColumnNameRoster() {
        // this uses an internal detail (The private column number).  If the
        // column numbering changes, the test may need to change.
        Assert.assertEquals("roster column name",Bundle.getMessage("RosterColumnLabel"), model.getColumnName(1));
    }

    @Test
    public void testGetColumnNameDirection() {
        // this uses an internal detail (The private column number).  If the
        // column numbering changes, the test may need to change.
        Assert.assertEquals("Direction column name",Bundle.getMessage("DirectionColumnLabel"), model.getColumnName(2));
    }

    @Test
    public void testGetColumnNameOther() {
        // this uses an internal detail (The private column number).  If the
        // column numbering changes, the test may need to change.
        Assert.assertEquals("other column name","", model.getColumnName(3));
    }


    @Test
    public void testGetColumnClassAddress() {
        // this uses an internal detail (The private column number).  If the
        // column numbering changes, the test may need to change.
        Assert.assertEquals("address column class",java.lang.String.class, model.getColumnClass(0));
    }

    @Test
    public void testGetColumnClassRoster() {
        // this uses an internal detail (The private column number).  If the
        // column numbering changes, the test may need to change.
        Assert.assertEquals("roster column class",String.class, model.getColumnClass(1));
    }

    @Test
    public void testGetColumnClassDirection() {
        // this uses an internal detail (The private column number).  If the
        // column numbering changes, the test may need to change.
        Assert.assertEquals("direction column class",Boolean.class, model.getColumnClass(2));
    }

    @Test
    public void testGetColumnClassDelete() {
        // this uses an internal detail (The private column number).  If the
        // column numbering changes, the test may need to change.
        Assert.assertEquals("delete column class",javax.swing.JButton.class, model.getColumnClass(3));
    }

    @Test
    public void testIsCellEditableDeleteColumn() {
        // this uses an internal detail (The private column number).  If the
        // column numbering changes, the test may need to change.
        Assert.assertTrue("delete column Editable",model.isCellEditable(0,3));
        Assert.assertTrue("delete column Editable",model.isCellEditable(2,3));
    }

    @Test
    public void testIsCellEditableDirectionColumn() {
        // this uses an internal detail (The private column number).  If the
        // column numbering changes, the test may need to change.
        Assert.assertFalse("Direction column (row 0) Editable",model.isCellEditable(0,2));
        Assert.assertTrue("Direciton column (not row 0) Editable",model.isCellEditable(2,2));
    }

    @Test
    public void testIsCellEditableRosterColumn() {
        // this uses an internal detail (The private column number).  If the
        // column numbering changes, the test may need to change.
        Assert.assertFalse("Roster column Editable",model.isCellEditable(0,1));
        Assert.assertFalse("Roster column Editable",model.isCellEditable(2,1));
    }

    @Test
    public void testIsCellEditableAddressColumn() {
        // this uses an internal detail (The private column number).  If the
        // column numbering changes, the test may need to change.
        Assert.assertFalse("Address column Editable",model.isCellEditable(0,0));
        Assert.assertFalse("Address column Editable",model.isCellEditable(2,0));
    }

    @Test
    public void testGetValueAtNullConsist() {
        // this uses an internal detail (The private column number).  If the
        // column numbering changes, the test may need to change.
        Assert.assertNull("Address",model.getValueAt(0,0));
        Assert.assertNull("Address",model.getValueAt(2,0));
        Assert.assertNull("Roster",model.getValueAt(0,1));
        Assert.assertNull("Roster",model.getValueAt(2,1));
        Assert.assertNull("Direction",model.getValueAt(0,2));
        Assert.assertNull("Direction",model.getValueAt(2,2));
        Assert.assertNull("Delete",model.getValueAt(0,3));
        Assert.assertNull("Delete",model.getValueAt(2,3));
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        InstanceManager.setDefault(ConsistManager.class, new TestConsistManager());
        model = new ConsistDataModel();
    }

    @After
    public void tearDown() {
        model = null;
        JUnitUtil.tearDown();
    }


}
