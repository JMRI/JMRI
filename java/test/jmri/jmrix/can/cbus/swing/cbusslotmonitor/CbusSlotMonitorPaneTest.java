package jmri.jmrix.can.cbus.swing.cbusslotmonitor;

import javax.swing.JPopupMenu;

import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.jmrix.can.cbus.CbusConstants;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import org.netbeans.jemmy.operators.*;

/**
 * Test simple functioning of CbusSlotMonitorPane.
 *
 * @author Paul Bender Copyright (C) 2016
 * @author Steve Young Copyright (C) 2019
 */
public class CbusSlotMonitorPaneTest extends jmri.util.swing.JmriPanelTest {

    @DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
    @Test
    public void testTableUpdates() {
        
        CbusSlotMonitorPane smPanel = new CbusSlotMonitorPane();
        smPanel.initComponents(memo);
        
        Assert.assertNotNull("exists",smPanel);
        
        jmri.util.JmriJFrame f = new jmri.util.JmriJFrame();
        f.add(smPanel);
        f.setTitle(smPanel.getTitle());
        
        java.util.List<javax.swing.JMenu> list = smPanel.getMenus();
        javax.swing.JMenuBar bar = f.getJMenuBar();
        if (bar == null) {
            bar = new javax.swing.JMenuBar();
        }
        for (javax.swing.JMenu menu : list) {
            bar.add(menu);
        }
        f.setJMenuBar(bar);
        
        f.pack();
        f.setVisible(true);
        
        
        CanReply r = new CanReply();
        r.setHeader(tcis.getCanid());
        r.setNumDataElements(3);
        r.setElement(0, CbusConstants.CBUS_RLOC); 
        r.setElement(1, 0x00);
        r.setElement(2, 0x03);
        
        // send the CanReply to the SlotMon Data Model
        smPanel.slotModel.reply(r);
        
        JFrameOperator jfo = new JFrameOperator(f);
        
        JTableOperator tbl = new JTableOperator(jfo, 0);
        tbl.waitCell("",0,0); // session null in column 1
        tbl.waitCell("3",0,1); // loco id in col 2
        
        CanReply ra = new CanReply();
        ra.setHeader(tcis.getCanid());
        ra.setNumDataElements(8);
        ra.setElement(0, CbusConstants.CBUS_PLOC);
        ra.setElement(1, 0x01); // session
        ra.setElement(2, 0x00); // addr hi
        ra.setElement(3, 0x03); // addr lo
        ra.setElement(4, 0xa7);
        ra.setElement(5, 0xa2);
        ra.setElement(6, 0x7b);
        ra.setElement(7, 0x00);
        smPanel.slotModel.reply(ra);
        
        tbl.waitCell("1",0,0); // session 1 in column 1
        tbl.waitCell("3",0,1); // loco id in col 2
        tbl.waitCell("39",0,3); // speed in col 4
        tbl.waitCell(Bundle.getMessage("FWD"),0,4); // direction in col 5
        tbl.waitCell("2 5 6 8",0,5); // speed in col 6
        
        JMenuBarOperator mainbar = new JMenuBarOperator(jfo);
        mainbar.pushMenu(Bundle.getMessage("SessCol")); // stops at top level
        JMenuOperator jmo = new JMenuOperator(mainbar, Bundle.getMessage("SessCol"));
        JPopupMenu jpm = jmo.getPopupMenu();
        JMenuItemOperator jmio = new JMenuItemOperator(
            new JPopupMenuOperator(jpm),Bundle.getMessage("Long")); // Long DCC Address
        jmio.push();
        
        tbl.waitCell("39",0,4); // Now speed in col 5
        
        // jmri.util.swing.JemmyUtil.pressButton(new JFrameOperator(f),("Pause Test"));
        
        smPanel.dispose();
        
    }
    
    private CanSystemConnectionMemo memo;
    private TrafficControllerScaffold tcis;

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        
        memo = new CanSystemConnectionMemo();
        tcis = new TrafficControllerScaffold();
        memo.setTrafficController(tcis);
        panel = new CbusSlotMonitorPane();
        title = Bundle.getMessage("MenuItemCbusSlotMonitor");
        helpTarget = "package.jmri.jmrix.can.cbus.swing.cbusslotmonitor.CbusSlotMonitorPane";
    }

    @Override
    @AfterEach
    public void tearDown() { 
        memo.dispose();
        memo=null;
        tcis.terminateThreads();
        tcis=null;
        JUnitUtil.tearDown();
    }

}
