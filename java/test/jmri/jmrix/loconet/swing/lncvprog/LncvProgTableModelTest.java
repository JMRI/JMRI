package jmri.jmrix.loconet.swing.lncvprog;

import jmri.jmrit.roster.RosterConfigManager;
import jmri.jmrix.loconet.LncvDevicesManager;
import jmri.jmrix.loconet.LocoNetMessage;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import jmri.util.JUnitUtil;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.*;

class LncvProgTableModelTest {

    LocoNetSystemConnectionMemo memo;
    LncvProgTableModel lptm;

    @Test
    public void testCTor() {
        Assert.assertNotNull("LncvProg model exists", lptm);
    }

    @Test
    void testInitTable() {
        JTable table = new JTable(lptm);
        lptm.initTable(table); // one would expect 8 but is 15
        Assert.assertEquals("IDColumn width", 15, table.getColumn("#").getMaxWidth());
    }

    @Test
    void testGetColumnName() {
        lptm.initTable(new javax.swing.JTable(lptm));
        Assert.assertEquals("IDColumn name", "#", lptm.getColumnName(0));
    }

    @Test
    void testGetColumnClass() {
        Assert.assertEquals("VALUE_COLUMN class", Integer.class, lptm.getColumnClass(LncvProgTableModel.VALUE_COLUMN));
        Assert.assertEquals("OPENPRGMRBUTTONCOLUMN class", JButton.class, lptm.getColumnClass(LncvProgTableModel.OPENPRGMRBUTTONCOLUMN));
    }

    @Test
    void tastIsCellEditable() {
        Assert.assertEquals("OPENPRGMRBUTTONCOLUMN is editable", true, lptm.isCellEditable(0, LncvProgTableModel.OPENPRGMRBUTTONCOLUMN));
        Assert.assertEquals("VALUE_COLUMN is not editable", false, lptm.isCellEditable(0, LncvProgTableModel.VALUE_COLUMN));
    }

    @Test
    void testGetColumnCount() {
        Assert.assertEquals("ProgTable column count", 8, lptm.getColumnCount());
    }

    @Test
    void testGetSetValueAt() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LncvDevicesManager lcdm = new LncvDevicesManager(memo);
        memo.setLncvDevicesManager(lcdm);
        jmri.InstanceManager.setDefault(jmri.jmrit.roster.RosterConfigManager.class, new RosterConfigManager());
        Assert.assertEquals("LncvDeviceManager List empty", 0, lcdm.getDeviceCount());
        lcdm.message(new LocoNetMessage(new int[] {0xE5, 0x0F, 0x05, 0x49, 0x4B, 0x1F, 0x11, 0x29, 0x13, 0x00, 0x00, 0x08, 0x00, 0x00, 0x4D}));
        // should add 1 row to table
        Assert.assertEquals("getValue in cell 0,COUNT_COLUMN", 1, lptm.getValueAt(0, LncvProgTableModel.COUNT_COLUMN));
        Assert.assertEquals("getValue in cell 0,VALUE_COLUMN", 136, lptm.getValueAt(0, LncvProgTableModel.VALUE_COLUMN));
        Assert.assertEquals("getValue in cell 0,ARTICLE_COLUMN", 5033, lptm.getValueAt(0, LncvProgTableModel.ARTICLE_COLUMN));
        // Roster should be loaded for match
        Assert.assertEquals("getValue in cell 0,DEVICENAMECOLUMN", "DR5033", lptm.getValueAt(0, LncvProgTableModel.DEVICENAMECOLUMN));
        Assert.assertEquals("getValue in cell 0,OPENPRGMRBUTTONCOLUMN", Bundle.getMessage("ButtonCreateEntry"), lptm.getValueAt(0, LncvProgTableModel.OPENPRGMRBUTTONCOLUMN));
        lptm.setValueAt(5,0, LncvProgTableModel.OPENPRGMRBUTTONCOLUMN); // click should see na action

        lcdm.message(new LocoNetMessage(new int[] {0xE5, 0x0F, 0x05, 0x49, 0x4B, 0x1F, 0x11, 0x29, 0x00, 0x00, 0x00, 0x08, 0x00, 0x00, 0x4D}));
        // should add row 2 to table, unknown article number 0029, no match
        Assert.assertEquals("getValue in cell 1,COUNT_COLUMN", 2, lptm.getValueAt(1, LncvProgTableModel.COUNT_COLUMN));
        Assert.assertEquals("getValue in cell 1,OPENPRGMRBUTTONCOLUMN", Bundle.getMessage("ButtonNoMatchInRoster"), lptm.getValueAt(1, LncvProgTableModel.OPENPRGMRBUTTONCOLUMN));
    }

//    @Test
//    void openPaneOpsProgFrame() {
//    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        JUnitUtil.initRosterConfigManager();
        memo = new jmri.jmrix.loconet.LocoNetSystemConnectionMemo();
        jmri.InstanceManager.setDefault(jmri.jmrix.loconet.LocoNetSystemConnectionMemo.class, memo);
        lptm = new LncvProgTableModel(new LncvProgPane(), memo);
    }

    @AfterEach
    public void tearDown() {
        memo = null;
        JUnitUtil.tearDown();
    }

}
