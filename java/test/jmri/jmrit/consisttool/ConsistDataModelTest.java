package jmri.jmrit.consisttool;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jmri.ConsistManager;
import jmri.InstanceManager;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Test simple functioning of ConsistDataModel
 *
 * @author Paul Bender Copyright (C) 2015,2016
 */
public class ConsistDataModelTest {

    private ConsistDataModel model;

    @Test
    public void testCtor() {
        assertNotNull(model, "exists");
    }

    @Test
    public void testGetRowCountNullConsist() {
        assertEquals(0, model.getRowCount(), "consist table model size");
    }

    @Test
    public void testGetColumnCount() {
        assertEquals(4, model.getColumnCount(), "consist column count");
    }

    @Test
    public void testGetColumnNameAddress() {
        // this uses an internal detail (The private column number).  If the
        // column numbering changes, the test may need to change.
        assertEquals(Bundle.getMessage("AddressColumnLabel"), model.getColumnName(0),
            "address column name");
    }

    @Test
    public void testGetColumnNameRoster() {
        // this uses an internal detail (The private column number).  If the
        // column numbering changes, the test may need to change.
        assertEquals(Bundle.getMessage("RosterColumnLabel"), model.getColumnName(1),
            "roster column name");
    }

    @Test
    public void testGetColumnNameDirection() {
        // this uses an internal detail (The private column number).  If the
        // column numbering changes, the test may need to change.
        assertEquals(Bundle.getMessage("DirectionColumnLabel"), model.getColumnName(2),
            "Direction column name");
    }

    @Test
    public void testGetColumnNameOther() {
        // this uses an internal detail (The private column number).  If the
        // column numbering changes, the test may need to change.
        assertEquals("", model.getColumnName(3), "other column name");
    }

    @Test
    public void testGetColumnClassAddress() {
        // this uses an internal detail (The private column number).  If the
        // column numbering changes, the test may need to change.
        assertEquals(java.lang.String.class, model.getColumnClass(0), "address column class");
    }

    @Test
    public void testGetColumnClassRoster() {
        // this uses an internal detail (The private column number).  If the
        // column numbering changes, the test may need to change.
        assertEquals(String.class, model.getColumnClass(1), "roster column class");
    }

    @Test
    public void testGetColumnClassDirection() {
        // this uses an internal detail (The private column number).  If the
        // column numbering changes, the test may need to change.
        assertEquals(Boolean.class, model.getColumnClass(2), "direction column class");
    }

    @Test
    public void testGetColumnClassDelete() {
        // this uses an internal detail (The private column number).  If the
        // column numbering changes, the test may need to change.
        assertEquals(javax.swing.JButton.class, model.getColumnClass(3), "delete column class");
    }

    @Test
    public void testIsCellEditableDeleteColumn() {
        // this uses an internal detail (The private column number).  If the
        // column numbering changes, the test may need to change.
        assertTrue(model.isCellEditable(0,3), "delete column Editable");
        assertTrue(model.isCellEditable(2,3), "delete column Editable");
    }

    @Test
    public void testIsCellEditableDirectionColumn() {
        // this uses an internal detail (The private column number).  If the
        // column numbering changes, the test may need to change.
        assertFalse(model.isCellEditable(0,2), "Direction column (row 0) Editable");
        assertTrue(model.isCellEditable(2,2), "Direciton column (not row 0) Editable");
    }

    @Test
    public void testIsCellEditableRosterColumn() {
        // this uses an internal detail (The private column number).  If the
        // column numbering changes, the test may need to change.
        assertFalse(model.isCellEditable(0,1), "Roster column Editable");
        assertFalse(model.isCellEditable(2,1), "Roster column Editable");
    }

    @Test
    public void testIsCellEditableAddressColumn() {
        // this uses an internal detail (The private column number).  If the
        // column numbering changes, the test may need to change.
        assertFalse(model.isCellEditable(0,0), "Address column Editable");
        assertFalse(model.isCellEditable(2,0), "Address column Editable");
    }

    @Test
    public void testGetValueAtNullConsist() {
        // this uses an internal detail (The private column number).  If the
        // column numbering changes, the test may need to change.
        assertNull(model.getValueAt(0,0), "Address");
        assertNull(model.getValueAt(2,0), "Address");
        assertNull(model.getValueAt(0,1), "Roster");
        assertNull(model.getValueAt(2,1), "Roster");
        assertNull(model.getValueAt(0,2), "Direction");
        assertNull(model.getValueAt(2,2), "Direction");
        assertNull(model.getValueAt(0,3), "Delete");
        assertNull(model.getValueAt(2,3), "Delete");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        InstanceManager.setDefault(ConsistManager.class, new TestConsistManager());
        model = new ConsistDataModel();
    }

    @AfterEach
    public void tearDown() {
        model = null;
        JUnitUtil.tearDown();
    }


}
