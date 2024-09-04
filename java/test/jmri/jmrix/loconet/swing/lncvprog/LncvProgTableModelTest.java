package jmri.jmrix.loconet.swing.lncvprog;

import jmri.jmrit.roster.RosterConfigManager;
import jmri.jmrix.loconet.LncvDevicesManager;
import jmri.jmrix.loconet.LocoNetMessage;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

class LncvProgTableModelTest {

    private LocoNetSystemConnectionMemo memo;
    private LncvProgTableModel lptm;

    @Test
    public void testCTor() {
        Assertions.assertNotNull(lptm, "LncvProg model exists");
    }

    @Test
    void testInitTable() {
        javax.swing.JTable table = new javax.swing.JTable(lptm);
        lptm.initTable(table); // one would expect 8 but is 15
        Assertions.assertEquals(15, table.getColumn("#").getMaxWidth(), "IDColumn width");
    }

    @Test
    void testGetColumnName() {
        lptm.initTable(new javax.swing.JTable(lptm));
        Assertions.assertEquals("#", lptm.getColumnName(0), "IDColumn name");
    }

    @Test
    void testGetColumnClass() {
        Assertions.assertEquals(Integer.class, lptm.getColumnClass(LncvProgTableModel.VALUE_COLUMN), "VALUE_COLUMN class");
        Assertions.assertEquals(javax.swing.JButton.class, lptm.getColumnClass(LncvProgTableModel.OPENPRGMRBUTTONCOLUMN), "OPENPRGMRBUTTONCOLUMN class");
    }

    @Test
    void tastIsCellEditable() {
        Assertions.assertTrue(lptm.isCellEditable(0, LncvProgTableModel.OPENPRGMRBUTTONCOLUMN), "OPENPRGMRBUTTONCOLUMN is editable");
        Assertions.assertFalse(lptm.isCellEditable(0, LncvProgTableModel.VALUE_COLUMN), "VALUE_COLUMN is not editable");
    }

    @Test
    void testGetColumnCount() {
        Assertions.assertEquals(8, lptm.getColumnCount(), "ProgTable column count");
    }

    @Test
    @DisabledIfSystemProperty( named = "java.awt.headless", matches = "true" )
    void testGetSetValueAt() {
        LncvDevicesManager lcdm = new LncvDevicesManager(memo);
        memo.setLncvDevicesManager(lcdm);
        jmri.InstanceManager.setDefault(jmri.jmrit.roster.RosterConfigManager.class, new RosterConfigManager());
        Assertions.assertEquals(0, lcdm.getDeviceCount(), "LncvDeviceManager List empty");
        lcdm.message(new LocoNetMessage(new int[] {0xE5, 0x0F, 0x05, 0x49, 0x4B, 0x1F, 0x11, 0x29, 0x13, 0x00, 0x00, 0x08, 0x00, 0x00, 0x4D}));
        // should add 1 row to table
        Assertions.assertEquals(1, (int)lptm.getValueAt(0, LncvProgTableModel.COUNT_COLUMN), "getValue in cell 0,COUNT_COLUMN");
        Assertions.assertEquals(136, (int)lptm.getValueAt(0, LncvProgTableModel.VALUE_COLUMN), "getValue in cell 0,VALUE_COLUMN");
        Assertions.assertEquals(5033, (int)lptm.getValueAt(0, LncvProgTableModel.ARTICLE_COLUMN), "getValue in cell 0,ARTICLE_COLUMN");
        // Roster should be loaded for match
        Assertions.assertEquals("DR5033", lptm.getValueAt(0, LncvProgTableModel.DEVICENAMECOLUMN), "getValue in cell 0,DEVICENAMECOLUMN");
        Assertions.assertEquals(Bundle.getMessage("ButtonCreateEntry"), lptm.getValueAt(0, LncvProgTableModel.OPENPRGMRBUTTONCOLUMN), "getValue in cell 0,OPENPRGMRBUTTONCOLUMN");
        lptm.setValueAt(5,0, LncvProgTableModel.OPENPRGMRBUTTONCOLUMN); // click should see na action

        lcdm.message(new LocoNetMessage(new int[] {0xE5, 0x0F, 0x05, 0x49, 0x4B, 0x1F, 0x11, 0x29, 0x00, 0x00, 0x00, 0x08, 0x00, 0x00, 0x4D}));
        // should add row 2 to table, unknown article number 0029, no match
        Assertions.assertEquals(2, (int)lptm.getValueAt(1, LncvProgTableModel.COUNT_COLUMN), "getValue in cell 1,COUNT_COLUMN");
        Assertions.assertEquals(Bundle.getMessage("ButtonNoMatchInRoster"), lptm.getValueAt(1, LncvProgTableModel.OPENPRGMRBUTTONCOLUMN), "getValue in cell 1,OPENPRGMRBUTTONCOLUMN");
    }

//    @Test
//    void openPaneOpsProgFrame() {
//    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initRosterConfigManager();
        memo = new LocoNetSystemConnectionMemo();
        jmri.InstanceManager.setDefault( LocoNetSystemConnectionMemo.class, memo);
        lptm = new LncvProgTableModel(new LncvProgPane(), memo);
    }

    @AfterEach
    public void tearDown() {
        lptm.dispose();
        lptm = null;
        memo.dispose();
        memo = null;
        JUnitUtil.tearDown();
    }

}
