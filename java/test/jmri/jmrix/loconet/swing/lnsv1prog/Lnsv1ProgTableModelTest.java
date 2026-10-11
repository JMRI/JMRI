package jmri.jmrix.loconet.swing.lnsv1prog;

import jmri.jmrix.loconet.Lnsv1DevicesManager;
import jmri.jmrix.loconet.LocoNetMessage;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import jmri.util.JUnitUtil;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

class Lnsv1ProgTableModelTest {

    private LocoNetSystemConnectionMemo memo;
    private Lnsv1ProgTableModel lptm;

    @Test
    public void testCTor() {
        Assertions.assertNotNull(lptm, "Lnsv1Prog model exists");
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
        Assertions.assertEquals("CV last read", lptm.getColumnName(4), "Read Column name");
    }

    @Test
    void testGetColumnClass() {
        Assertions.assertEquals(Integer.class, lptm.getColumnClass(Lnsv1ProgTableModel.VALUE_COLUMN), "VALUE_COLUMN class");
        Assertions.assertEquals(javax.swing.JButton.class, lptm.getColumnClass(Lnsv1ProgTableModel.OPENPRGMRBUTTON_COLUMN), "OPENPRGMRBUTTONCOLUMN class");
    }

    @Test
    void tastIsCellEditable() {
        Assertions.assertTrue(lptm.isCellEditable(0, Lnsv1ProgTableModel.OPENPRGMRBUTTON_COLUMN), "OPENPRGMRBUTTON_COLUMN is editable");
        Assertions.assertFalse(lptm.isCellEditable(0, Lnsv1ProgTableModel.VALUE_COLUMN), "VALUE_COLUMN is not editable");
    }

    @Test
    void testGetColumnCount() {
        Assertions.assertEquals(10, lptm.getColumnCount(), "ProgTable column count");
    }

    @Test
    @DisabledIfSystemProperty( named = "java.awt.headless", matches = "true" )
    void testGetSetValueAt() {
        Lnsv1DevicesManager lcdm = new Lnsv1DevicesManager(memo);
        memo.setLnsv1DevicesManager(lcdm);
        // in setup, produces error output if here: jmri.InstanceManager.setDefault(RosterConfigManager.class, new RosterConfigManager());
        Assertions.assertEquals(0, lcdm.getDeviceCount(), "Lnsv1DeviceManager List empty");
        lcdm.message(new LocoNetMessage(new int[] {0xE5, 0x10, 0x51, 0x50, 0x01, 0x02, 0x02, 0x33, 0x02, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x18}));
        // should add 1 row to table
        Assertions.assertEquals(1, lcdm.getDeviceCount(), "Lnsv1DeviceManager List 1 item");
        Assertions.assertEquals(1, (int)lptm.getValueAt(0, Lnsv1ProgTableModel.COUNT_COLUMN), "getValue in cell 0,COUNT_COLUMN");
        Assertions.assertEquals(0, (int)lptm.getValueAt(0, Lnsv1ProgTableModel.VALUE_COLUMN), "getValue in cell 0,VALUE_COLUMN");
        Assertions.assertEquals("81/1", lptm.getValueAt(0, Lnsv1ProgTableModel.MODADDRSPLIT_COLUMN), "getValue in cell 0,MODADDRSPLIT_COLUMN");
        // Roster should be loaded for match
        Assertions.assertEquals(179, lptm.getValueAt(0, Lnsv1ProgTableModel.CV_COLUMN), "getValue in cell 0,CV_COLUMN");
        Assertions.assertEquals(Bundle.getMessage("ButtonNoMatchInRoster"), lptm.getValueAt(0, Lnsv1ProgTableModel.OPENPRGMRBUTTON_COLUMN), "getValue in cell 0,OPENPRGMRBUTTONCOLUMN");
        lptm.setValueAt(5,0, Lnsv1ProgTableModel.OPENPRGMRBUTTON_COLUMN); // click should see na action

        lcdm.message(new LocoNetMessage(new int[]{0xE5, 0x10, 0x04, 0x50, 0x01, 0x00, 0x02, 0x4A, 0x54, 0x00, 0x10, 0x02, 0x48, 0x00, 0x00, 0x05}));
        // should add row 2 to table, no match in roster
        Assertions.assertEquals(2, (int)lptm.getValueAt(1, Lnsv1ProgTableModel.COUNT_COLUMN), "getValue in cell 1,COUNT_COLUMN");
        Assertions.assertEquals(Bundle.getMessage("ButtonNoMatchInRoster"), lptm.getValueAt(1, Lnsv1ProgTableModel.OPENPRGMRBUTTON_COLUMN), "getValue in cell 1,OPENPRGMRBUTTONCOLUMN");
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
        lptm = new Lnsv1ProgTableModel(new Lnsv1ProgPane(), memo);
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
