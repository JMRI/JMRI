package jmri.jmrix.cmri.serial.nodeconfigmanager;

import java.awt.GraphicsEnvironment;

import jmri.jmrix.cmri.CMRISystemConnectionMemo;
import jmri.jmrix.cmri.serial.SerialNode;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

/**
 * Test simple functioning of NodeConfigFrame
 * Copied from NodeConfig
 *
 * @author Paul Bender Copyright (C) 2016
 * @author Chuck Catania Copyright (C) 2017
 */
public class NodeConfigManagerFrameTest extends jmri.util.JmriJFrameTestBase {

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();
        if (!GraphicsEnvironment.isHeadless()) {
            frame = new NodeConfigManagerFrame(new CMRISystemConnectionMemo());
        }
    }

    @Test
    @DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
    public void testEsp32NodeCards() {
        NodeConfigManagerFrame f = (NodeConfigManagerFrame) frame;
        NodeConfigManagerFrame.CPnodeConfigModel m = f.new CPnodeConfigModel();
        f.nodeType = SerialNode.CPNODE;
        Assertions.assertEquals(16, m.getRowCount());
        f.nodeType = SerialNode.ESP32NODE;
        f.cpNodeOnboard = 0;
        Assertions.assertEquals(64, m.getRowCount());
        int col = NodeConfigManagerFrame.CPnodeConfigModel.CARDNUM_COLUMN;
        Assertions.assertEquals("20", m.getValueAt(0, col).toString().trim().substring(0, 2));
        Assertions.assertTrue(m.getValueAt(16, col).toString().contains("40") && m.getValueAt(16, col).toString().contains("ch 0-7"));
        Assertions.assertTrue(m.getValueAt(31, col).toString().contains("ch 8-15"));
        Assertions.assertTrue(m.getValueAt(30, col).toString().contains("47"));
        Assertions.assertEquals("logic 1-8", m.getValueAt(32, col).toString().trim());   // card 32: CT n257-n264
        Assertions.assertEquals("logic 9-16", m.getValueAt(33, col).toString().trim());
        Assertions.assertEquals("logic 249-256", m.getValueAt(63, col).toString().trim());  // card 63: CT n505-n512

        // fixed numbers: cards 0-15 Output or Input (never No Card); 16-63 always Output, not editable
        int type = NodeConfigManagerFrame.CPnodeConfigModel.CARDTYPE_COLUMN;
        String in = Bundle.getMessage("CardTypeInput");
        String out = Bundle.getMessage("CardTypeOutput");
        String none = Bundle.getMessage("CardTypeNone");
        for (int r = 0; r < 16; r++) {
            m.setValueAt(out, r, type);
        }
        m.setValueAt(in, 2, type);
        Assertions.assertEquals(in, m.getValueAt(2, type).toString().trim());
        Assertions.assertEquals(out, m.getValueAt(3, type).toString().trim());
        m.setValueAt(none, 3, type);
        Assertions.assertEquals(out, m.getValueAt(3, type).toString().trim());
        Assertions.assertTrue(m.isCellEditable(15, type));
        Assertions.assertFalse(m.isCellEditable(16, type));
        Assertions.assertFalse(m.isCellEditable(53, type));
        String was16 = m.getValueAt(16, type).toString(), was40 = m.getValueAt(40, type).toString();
        m.setValueAt(in, 16, type);     // 0x40 ch 0-7: can't be changed
        Assertions.assertEquals(was16, m.getValueAt(16, type).toString());
        m.setValueAt(out.equals(was40.trim()) ? none : out, 40, type);   // signal logic: can't be changed
        Assertions.assertEquals(was40, m.getValueAt(40, type).toString());
    }

    @AfterEach
    @Override
    public void tearDown() {
        jmri.util.JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        super.tearDown();
    }
}
