package jmri.jmrit.roster.swing;

import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.roster.RosterEntryImplementations;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests for the roster.swing.RosterTableModel class.
 *
 * @author Bob Jacobsen Copyright (C) 2009
 */
public class RosterTableModelTest {

    @Test
    public void testTableLength() throws Exception {
        RosterTableModel t = new RosterTableModel();

        Assertions.assertEquals(3, t.getRowCount());
        t.dispose();
    }

    @Test
    public void testColumnCount() throws Exception {
        RosterTableModel t = new RosterTableModel(true); // set Editable

        // hard-coded value is number of columns expected
        // 11 normal columns + 4 attribute columns
        Assert.assertEquals(15, t.getColumnCount());
        
        
        Assertions.assertTrue(t.isCellEditable(0, RosterTableModel.IDCOL));
        Assertions.assertFalse(t.isCellEditable(0, RosterTableModel.ADDRESSCOL));
        Assertions.assertFalse(t.isCellEditable(0, RosterTableModel.ICONCOL));
        Assertions.assertFalse(t.isCellEditable(0, RosterTableModel.DECODERCOL));
        Assertions.assertTrue(t.isCellEditable(0, RosterTableModel.ROADNAMECOL));
        Assertions.assertTrue(t.isCellEditable(0, RosterTableModel.ROADNUMBERCOL));
        Assertions.assertTrue(t.isCellEditable(0, RosterTableModel.MFGCOL));
        Assertions.assertTrue(t.isCellEditable(0, RosterTableModel.MODELCOL));
        Assertions.assertTrue(t.isCellEditable(0, RosterTableModel.OWNERCOL));
        Assertions.assertFalse(t.isCellEditable(0, RosterTableModel.DATEUPDATECOL));
        Assertions.assertFalse(t.isCellEditable(0, RosterTableModel.PROTOCOL));
        
        Assertions.assertTrue(t.isCellEditable(0, RosterTableModel.NUMCOL));
        Assertions.assertTrue(t.isCellEditable(0, RosterTableModel.NUMCOL+1));
        Assertions.assertTrue(t.isCellEditable(0, RosterTableModel.NUMCOL+2));
        Assertions.assertTrue(t.isCellEditable(0, RosterTableModel.NUMCOL+3));
        
        t.dispose();
    }

    @Test
    public void testColumnName() throws Exception {
        RosterTableModel t = new RosterTableModel(null); // no rosterGroup

        Assert.assertEquals(Bundle.getMessage("FieldID"), t.getColumnName( RosterTableModel.IDCOL));
        Assert.assertEquals(Bundle.getMessage("FieldDCCAddress"), t.getColumnName(RosterTableModel.ADDRESSCOL));
        Assert.assertEquals(Bundle.getMessage("FieldIcon"), t.getColumnName(RosterTableModel.ICONCOL));
        Assert.assertEquals(Bundle.getMessage("FieldDecoderModel"), t.getColumnName(RosterTableModel.DECODERCOL));
        Assert.assertEquals(Bundle.getMessage("FieldRoadName"), t.getColumnName(RosterTableModel.ROADNAMECOL));
        Assert.assertEquals(Bundle.getMessage("FieldRoadNumber"), t.getColumnName(RosterTableModel.ROADNUMBERCOL));
        Assert.assertEquals(Bundle.getMessage("FieldManufacturer"), t.getColumnName(RosterTableModel.MFGCOL));
        Assert.assertEquals(Bundle.getMessage("FieldModel"), t.getColumnName(RosterTableModel.MODELCOL));
        Assert.assertEquals(Bundle.getMessage("FieldOwner"), t.getColumnName(RosterTableModel.OWNERCOL));
        Assert.assertEquals(Bundle.getMessage("FieldDateUpdated"), t.getColumnName(RosterTableModel.DATEUPDATECOL));
        Assert.assertEquals(Bundle.getMessage("FieldProtocol"), t.getColumnName(RosterTableModel.PROTOCOL));

        // the roster entries have 4 attributes
        Assert.assertEquals("key a", t.getColumnName(RosterTableModel.NUMCOL));
        Assert.assertEquals("key b", t.getColumnName(RosterTableModel.NUMCOL+1));
        Assert.assertEquals("key c", t.getColumnName(RosterTableModel.NUMCOL+2));
        Assert.assertEquals("key d", t.getColumnName(RosterTableModel.NUMCOL+3));

        Assert.assertEquals("<UNKNOWN>", t.getColumnName(RosterTableModel.NUMCOL+4));
        
        t.dispose();
    }

    @Test
    public void testGetValueAt() {
        RosterTableModel t = new RosterTableModel();

        Assert.assertEquals("id 1", t.getValueAt(0, RosterTableModel.IDCOL));
        Assert.assertEquals(12, (int)t.getValueAt(0, RosterTableModel.ADDRESSCOL));
        Assert.assertEquals("33", t.getValueAt(0, RosterTableModel.DECODERCOL));

        Assert.assertEquals("id 2", t.getValueAt(1, RosterTableModel.IDCOL));
        Assert.assertEquals(13,(int) t.getValueAt(1, RosterTableModel.ADDRESSCOL));
        Assert.assertEquals("34", t.getValueAt(1, RosterTableModel.DECODERCOL));

        Assert.assertEquals("id 3", t.getValueAt(2, RosterTableModel.IDCOL));
        Assert.assertEquals(14, (int)t.getValueAt(2, RosterTableModel.ADDRESSCOL));
        Assert.assertEquals("35", t.getValueAt(2, RosterTableModel.DECODERCOL));
        
        Assert.assertEquals("DCC Long", t.getValueAt(2, RosterTableModel.PROTOCOL));
        
        
        Assert.assertEquals("value 11", t.getValueAt(1, RosterTableModel.NUMCOL));
        Assert.assertEquals("value 12", t.getValueAt(1, RosterTableModel.NUMCOL+1));
        Assert.assertEquals("value 13", t.getValueAt(1, RosterTableModel.NUMCOL+2));
        Assert.assertEquals("value 14", t.getValueAt(1, RosterTableModel.NUMCOL+3));

        t.dispose();
    }
    
    @Test
    public void testSetValueAt() {
        RosterTableModel t = new RosterTableModel(true); // editable
        Assert.assertEquals("id 1", t.getValueAt(0, RosterTableModel.IDCOL));
        t.setValueAt("A New Id 1", 0, RosterTableModel.IDCOL);
        t.setValueAt("A New Id 1", 0, RosterTableModel.IDCOL);
        Assert.assertEquals("A New Id 1", t.getValueAt(0, RosterTableModel.IDCOL));
        
        Assert.assertEquals("SP", t.getValueAt(0, RosterTableModel.ROADNAMECOL));
        t.setValueAt("A New RoadName", 0, RosterTableModel.ROADNAMECOL);
        Assert.assertEquals("A New RoadName", t.getValueAt(0, RosterTableModel.ROADNAMECOL));
        
        Assert.assertEquals("431", t.getValueAt(0, RosterTableModel.ROADNUMBERCOL));
        t.setValueAt("99", 0, RosterTableModel.ROADNUMBERCOL);
        Assert.assertEquals("99", t.getValueAt(0, RosterTableModel.ROADNUMBERCOL));
        
        Assert.assertEquals("Athearn", t.getValueAt(0, RosterTableModel.MFGCOL));
        t.setValueAt("My MFG", 0, RosterTableModel.MFGCOL);
        Assert.assertEquals("My MFG", t.getValueAt(0, RosterTableModel.MFGCOL));
        
        Assert.assertEquals("", t.getValueAt(0, RosterTableModel.MODELCOL));
        t.setValueAt("Model Ref", 0, RosterTableModel.MODELCOL);
        Assert.assertEquals("Model Ref", t.getValueAt(0, RosterTableModel.MODELCOL));
        
        Assert.assertEquals("", t.getValueAt(0, RosterTableModel.OWNERCOL));
        t.setValueAt("Its my train!", 0, RosterTableModel.OWNERCOL);
        Assert.assertEquals("Its my train!", t.getValueAt(0, RosterTableModel.OWNERCOL));
        
        
        Assert.assertEquals("value 1", t.getValueAt(0, RosterTableModel.NUMCOL));
        t.setValueAt("new value 1", 0, RosterTableModel.NUMCOL);
        Assert.assertEquals("new value 1", t.getValueAt(0, RosterTableModel.NUMCOL));
        
        Assert.assertEquals("", t.getValueAt(0, RosterTableModel.NUMCOL+1));
        t.setValueAt("new value B1", 0, RosterTableModel.NUMCOL+1);
        Assert.assertEquals("new value B1", t.getValueAt(0, RosterTableModel.NUMCOL+1));
        
        Assert.assertEquals("", t.getValueAt(0, RosterTableModel.NUMCOL+2));
        t.setValueAt("new value C2", 0, RosterTableModel.NUMCOL+2);
        Assert.assertEquals("new value C2", t.getValueAt(0, RosterTableModel.NUMCOL+2));
        
        t.setValueAt("", 0, RosterTableModel.NUMCOL+2);
        Assert.assertEquals("", t.getValueAt(0, RosterTableModel.NUMCOL+2));
        
        Assert.assertEquals("", t.getValueAt(0, RosterTableModel.NUMCOL+3));
        t.setValueAt("new value D3", 0, RosterTableModel.NUMCOL+3);
        Assert.assertEquals("new value D3", t.getValueAt(0, RosterTableModel.NUMCOL+3));
        
        t.setValueAt(null, 0, RosterTableModel.NUMCOL+3);
        Assert.assertEquals("", t.getValueAt(0, RosterTableModel.NUMCOL+3));

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
        // 11 normal columns + 2 attribute columns
        Assert.assertEquals(13, t.getColumnCount());
        Assert.assertTrue(java.util.Date.class == t.getColumnClass(RosterTableModel.NUMCOL));

        Assert.assertNotNull(t.getValueAt(0, RosterTableModel.NUMCOL));
        Assert.assertNull(t.getValueAt(1, RosterTableModel.NUMCOL));
        Assert.assertNull(t.getValueAt(2, RosterTableModel.NUMCOL));
        
        Assert.assertEquals(Bundle.getMessage(RosterEntry.ATTRIBUTE_LAST_OPERATED),t.getColumnName(RosterTableModel.NUMCOL));
        Assert.assertEquals(Bundle.getMessage(RosterEntry.ATTRIBUTE_OPERATING_DURATION),t.getColumnName(RosterTableModel.NUMCOL+1));

        
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
