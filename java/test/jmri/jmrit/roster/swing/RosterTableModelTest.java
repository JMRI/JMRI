package jmri.jmrit.roster.swing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.roster.RosterEntryImplementations;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for the roster.swing.RosterTableModel class.
 *
 * @author Bob Jacobsen Copyright (C) 2009
 */
public class RosterTableModelTest {

    @Test
    public void testTableLength() {
        RosterTableModel t = new RosterTableModel();

        assertEquals(3, t.getRowCount());
        t.dispose();
    }

    @Test
    public void testColumnCount() {
        RosterTableModel t = new RosterTableModel(true); // set Editable

        // hard-coded value is number of columns expected
        // 14 normal columns + 4 attribute columns
        assertEquals(18, t.getColumnCount());
        
        
        assertTrue(t.isCellEditable(0, RosterTableModel.IDCOL));
        assertFalse(t.isCellEditable(0, RosterTableModel.ADDRESSCOL));
        assertFalse(t.isCellEditable(0, RosterTableModel.ICONCOL));
        assertFalse(t.isCellEditable(0, RosterTableModel.DECODERMFGCOL));
        assertFalse(t.isCellEditable(0, RosterTableModel.DECODERFAMILYCOL));
        assertFalse(t.isCellEditable(0, RosterTableModel.DECODERMODELCOL));
        assertTrue(t.isCellEditable(0, RosterTableModel.ROADNAMECOL));
        assertTrue(t.isCellEditable(0, RosterTableModel.ROADNUMBERCOL));
        assertTrue(t.isCellEditable(0, RosterTableModel.MFGCOL));
        assertTrue(t.isCellEditable(0, RosterTableModel.MODELCOL));
        assertTrue(t.isCellEditable(0, RosterTableModel.OWNERCOL));
        assertFalse(t.isCellEditable(0, RosterTableModel.DATEUPDATECOL));
        assertFalse(t.isCellEditable(0, RosterTableModel.PROTOCOL));

        assertTrue(t.isCellEditable(0, RosterTableModel.NUMCOL));
        assertTrue(t.isCellEditable(0, RosterTableModel.NUMCOL+1));
        assertTrue(t.isCellEditable(0, RosterTableModel.NUMCOL+2));
        assertTrue(t.isCellEditable(0, RosterTableModel.NUMCOL+3));
        
        t.dispose();
    }

    @Test
    public void testColumnName() {
        RosterTableModel t = new RosterTableModel(null); // no rosterGroup

        assertEquals(Bundle.getMessage("FieldID"), t.getColumnName( RosterTableModel.IDCOL));
        assertEquals(Bundle.getMessage("FieldDCCAddress"), t.getColumnName(RosterTableModel.ADDRESSCOL));
        assertEquals(Bundle.getMessage("FieldIcon"), t.getColumnName(RosterTableModel.ICONCOL));
        assertEquals(Bundle.getMessage("FieldDecoderModel"), t.getColumnName(RosterTableModel.DECODERMODELCOL));
        assertEquals(Bundle.getMessage("FieldRoadName"), t.getColumnName(RosterTableModel.ROADNAMECOL));
        assertEquals(Bundle.getMessage("FieldRoadNumber"), t.getColumnName(RosterTableModel.ROADNUMBERCOL));
        assertEquals(Bundle.getMessage("FieldManufacturer"), t.getColumnName(RosterTableModel.MFGCOL));
        assertEquals(Bundle.getMessage("FieldModel"), t.getColumnName(RosterTableModel.MODELCOL));
        assertEquals(Bundle.getMessage("FieldOwner"), t.getColumnName(RosterTableModel.OWNERCOL));
        assertEquals(Bundle.getMessage("FieldDateUpdated"), t.getColumnName(RosterTableModel.DATEUPDATECOL));
        assertEquals(Bundle.getMessage("FieldProtocol"), t.getColumnName(RosterTableModel.PROTOCOL));

        // the roster entries have 4 attributes
        assertEquals("key a", t.getColumnName(RosterTableModel.NUMCOL));
        assertEquals("key b", t.getColumnName(RosterTableModel.NUMCOL+1));
        assertEquals("key c", t.getColumnName(RosterTableModel.NUMCOL+2));
        assertEquals("key d", t.getColumnName(RosterTableModel.NUMCOL+3));

        assertEquals("<UNKNOWN>", t.getColumnName(RosterTableModel.NUMCOL+4));
        
        t.dispose();
    }

    @Test
    public void testGetValueAt() {
        RosterTableModel t = new RosterTableModel();

        assertEquals("id 1", t.getValueAt(0, RosterTableModel.IDCOL));
        assertEquals(12, (int)t.getValueAt(0, RosterTableModel.ADDRESSCOL));
        assertEquals("33", t.getValueAt(0, RosterTableModel.DECODERMODELCOL));

        assertEquals("id 2", t.getValueAt(1, RosterTableModel.IDCOL));
        assertEquals(13,(int) t.getValueAt(1, RosterTableModel.ADDRESSCOL));
        assertEquals("34", t.getValueAt(1, RosterTableModel.DECODERMODELCOL));

        assertEquals("id 3", t.getValueAt(2, RosterTableModel.IDCOL));
        assertEquals(14, (int)t.getValueAt(2, RosterTableModel.ADDRESSCOL));
        assertEquals("35", t.getValueAt(2, RosterTableModel.DECODERMODELCOL));
 
        assertEquals("DCC Long", t.getValueAt(2, RosterTableModel.PROTOCOL));
 
 
        assertEquals("value 11", t.getValueAt(1, RosterTableModel.NUMCOL));
        assertEquals("value 12", t.getValueAt(1, RosterTableModel.NUMCOL+1));
        assertEquals("value 13", t.getValueAt(1, RosterTableModel.NUMCOL+2));
        assertEquals("value 14", t.getValueAt(1, RosterTableModel.NUMCOL+3));

        t.dispose();
    }

    @Test
    public void testSetValueAt() {
        RosterTableModel t = new RosterTableModel(true); // editable
        assertEquals("id 1", t.getValueAt(0, RosterTableModel.IDCOL));
        t.setValueAt("A New Id 1", 0, RosterTableModel.IDCOL);
        t.setValueAt("A New Id 1", 0, RosterTableModel.IDCOL);
        assertEquals("A New Id 1", t.getValueAt(0, RosterTableModel.IDCOL));
        
        assertEquals("SP", t.getValueAt(0, RosterTableModel.ROADNAMECOL));
        t.setValueAt("A New RoadName", 0, RosterTableModel.ROADNAMECOL);
        assertEquals("A New RoadName", t.getValueAt(0, RosterTableModel.ROADNAMECOL));
        
        assertEquals("431", t.getValueAt(0, RosterTableModel.ROADNUMBERCOL));
        t.setValueAt("99", 0, RosterTableModel.ROADNUMBERCOL);
        assertEquals("99", t.getValueAt(0, RosterTableModel.ROADNUMBERCOL));
        
        assertEquals("Athearn", t.getValueAt(0, RosterTableModel.MFGCOL));
        t.setValueAt("My MFG", 0, RosterTableModel.MFGCOL);
        assertEquals("My MFG", t.getValueAt(0, RosterTableModel.MFGCOL));
        
        assertEquals("", t.getValueAt(0, RosterTableModel.MODELCOL));
        t.setValueAt("Model Ref", 0, RosterTableModel.MODELCOL);
        assertEquals("Model Ref", t.getValueAt(0, RosterTableModel.MODELCOL));
        
        assertEquals("", t.getValueAt(0, RosterTableModel.OWNERCOL));
        t.setValueAt("Its my train!", 0, RosterTableModel.OWNERCOL);
        assertEquals("Its my train!", t.getValueAt(0, RosterTableModel.OWNERCOL));
        
        
        assertEquals("value 1", t.getValueAt(0, RosterTableModel.NUMCOL));
        t.setValueAt("new value 1", 0, RosterTableModel.NUMCOL);
        assertEquals("new value 1", t.getValueAt(0, RosterTableModel.NUMCOL));
        
        assertEquals("", t.getValueAt(0, RosterTableModel.NUMCOL+1));
        t.setValueAt("new value B1", 0, RosterTableModel.NUMCOL+1);
        assertEquals("new value B1", t.getValueAt(0, RosterTableModel.NUMCOL+1));
        
        assertEquals("", t.getValueAt(0, RosterTableModel.NUMCOL+2));
        t.setValueAt("new value C2", 0, RosterTableModel.NUMCOL+2);
        assertEquals("new value C2", t.getValueAt(0, RosterTableModel.NUMCOL+2));
        
        t.setValueAt("", 0, RosterTableModel.NUMCOL+2);
        assertEquals("", t.getValueAt(0, RosterTableModel.NUMCOL+2));
        
        assertEquals("", t.getValueAt(0, RosterTableModel.NUMCOL+3));
        t.setValueAt("new value D3", 0, RosterTableModel.NUMCOL+3);
        assertEquals("new value D3", t.getValueAt(0, RosterTableModel.NUMCOL+3));
        
        t.setValueAt(null, 0, RosterTableModel.NUMCOL+3);
        assertEquals("", t.getValueAt(0, RosterTableModel.NUMCOL+3));

    }

    @Test
    public void testLastOperatedAttributes() {
        Roster.getDefault().getEntry(0).deleteAttribute("key a");
        Roster.getDefault().getEntry(0).putAttribute(RosterEntry.ATTRIBUTE_LAST_OPERATED, "2022-10-31T06:22:00.000+00:00");
        Roster.getDefault().getEntry(0).putAttribute(RosterEntry.ATTRIBUTE_OPERATING_DURATION, "54321");
        
        Roster.getDefault().getEntry(2).putAttribute(RosterEntry.ATTRIBUTE_LAST_OPERATED, "Not a Date / Time");
        
        Roster.getDefault().getEntry(1).deleteAttribute("key a");
        Roster.getDefault().getEntry(1).deleteAttribute("key b");
        Roster.getDefault().getEntry(1).deleteAttribute("key c");
        Roster.getDefault().getEntry(1).deleteAttribute("key d");
        
        RosterTableModel t = new RosterTableModel(true); // set Editable

        // hard-coded value is number of columns expected
        // 14 normal columns + 2 attribute columns
        assertEquals(16, t.getColumnCount());
        assertTrue(java.util.Date.class == t.getColumnClass(RosterTableModel.NUMCOL));

        assertNotNull(t.getValueAt(0, RosterTableModel.NUMCOL));
        assertNull(t.getValueAt(1, RosterTableModel.NUMCOL));
        assertNull(t.getValueAt(2, RosterTableModel.NUMCOL));
 
        assertEquals(Bundle.getMessage(RosterEntry.ATTRIBUTE_LAST_OPERATED),t.getColumnName(RosterTableModel.NUMCOL));
        assertEquals(Bundle.getMessage(RosterEntry.ATTRIBUTE_OPERATING_DURATION),t.getColumnName(RosterTableModel.NUMCOL+1));

        
        t.dispose();
        
        
    }

    // create a standard test roster
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();

        JUnitUtil.resetProfileManager();
        JUnitUtil.initRosterConfigManager();

        // first entry
        RosterEntry r = RosterEntryImplementations.id1();
        Roster.getDefault().addEntry(r);
        r.putAttribute("key a", "value 1");

        r = RosterEntryImplementations.id2();
        Roster.getDefault().addEntry(r);
        r.putAttribute("key a", "value 11");
        r.putAttribute("key b", "value 12");
        r.putAttribute("key c", "value 13");
        r.putAttribute("key d", "value 14");

        r = RosterEntryImplementations.id3();
        Roster.getDefault().addEntry(r);

    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
