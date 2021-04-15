package jmri.jmrix.loconet.swing.lncvprog;

import jmri.jmrit.roster.RosterConfigManager;
import jmri.jmrix.loconet.*;
import jmri.util.JUnitUtil;
import org.junit.jupiter.api.*;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;

import java.awt.*;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Test of the LNCV Programming Pane tool.
 *
 * @author Egbert Broerse   Copyright 2021
 * @author Paul Bender Copyright (C) 2017
 */
public class LncvProgPaneTest extends jmri.util.swing.JmriPanelTest {

    LocoNetSystemConnectionMemo memo;
    LncvProgPane panel;

    @Test
    public void testCTor() {
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless());
        LncvProgPane p = new LncvProgPane();
        assertNotNull(p, "exists");
    }

    @Override
    @Test
    public void testInitComponents() throws Exception{
        // for now, just makes sure there isn't an exception.
        panel.initComponents(memo);
    }

    @Test
    @Override
    public void testGetHelpTarget(){
        Assertions.assertEquals(helpTarget, panel.getHelpTarget(), "help target");
    }

    @Test
    @Override
    public void testGetTitle(){
        Assertions.assertEquals(title, panel.getTitle(), "title");
    }

    @Test
    public void testDispose() {
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless());
        LncvProgPane p = new LncvProgPane();
        Assertions.assertTrue(p.isVisible(), "LNCV running = visible");
        assertNotNull(p, "exists");
        p.dispose();
        Assertions.assertFalse(p.isVisible(), "disposed = invisible");
    }

    @Test
    public void testPanel() {
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless());
        LncvDevicesManager lcdm = memo.getLncvDevicesManager();
        panel.initComponents(memo); // set up stuff
        panel.initComponents();
        // test list
        Assertions.assertNull(panel.getModule(0), "Get LncvModule 0 from empty list");

        Thread dialog_thread1 = new Thread(() -> {
            JDialogOperator jfo = new JDialogOperator(Bundle.getMessage("WarningTitle") );
            new JButtonOperator(jfo, Bundle.getMessage("ButtonProceed")).doClick();
        });
        dialog_thread1.setName("BroadcastAll Dialog button Proceed clicked");
        dialog_thread1.start();

        panel.allProgButtonActionPerformed();

        JUnitUtil.waitFor(()-> !(dialog_thread1.isAlive()), "BroadcastAll Warning Dialog closed");
        Assertions.assertEquals("locked", panel.getAddressEntry(), "AddressField locked");

        panel.allProgButtonActionPerformed(); // second click "Stop" will release buttons
        Assertions.assertEquals("1", panel.getAddressEntry(), "AddressField unlocked");

        lcdm.message(new LocoNetMessage(new int[]{0xE5, 0x0F, 0x05, 0x49, 0x4B, 0x1F, 0x11, 0x29, 0x13, 0x00, 0x00, 0x08, 0x00, 0x00, 0x4D}));
        Assertions.assertEquals(1, lcdm.getDeviceCount(), "modules in devman list after add");
        Assertions.assertEquals(5033, panel.getModule(0).getProductID(), "ProductID of LncvModule at index 0 after adding");
        // add same module to monitor, adding it to table and filling in texts entry fields
        panel.message(new LocoNetMessage(new int[]{0xE5, 0x0F, 0x05, 0x49, 0x4B, 0x1F, 0x11, 0x29, 0x13, 0x00, 0x00, 0x08, 0x00, 0x00, 0x4D}));
        Assertions.assertEquals("", panel.getArticleEntry(), "Article field still empty");

        panel.copyEntry(8888, 77);
        Assertions.assertEquals("8888", panel.getArticleEntry(), "ProductID copied to Article field");

        panel.modProgButtonActionPerformed();
        Assertions.assertEquals("locked", panel.getAddressEntry(), "AddressField locked");

        panel.setCvFields(1, 100); // enter some values for read
        panel.readButtonActionPerformed();
        // no feedback, just confirm no exception is thrown
        panel.writeButtonActionPerformed();
        LocoNetMessage l = new LocoNetMessage(new int[]{0xB4, 0x6D, 0x7F, 0x59}); // OPC_LONG_ACK
        panel.message(l); // check in monitor
        Assertions.assertEquals("confirmed by module 77\n", panel.getMonitorContents().substring(panel.getMonitorContents().length() - 23), "Write confirmed in Monitor");

        panel.modProgButtonActionPerformed(); // to turn programming off
        Assertions.assertEquals("77", panel.getAddressEntry(), "AddressField 77");
    }

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initDefaultUserMessagePreferences();

        // prepare an interface, register
        LnTrafficController lnis = new LocoNetInterfaceScaffold();
        SlotManager slotmanager = new SlotManager(lnis);
        memo = new LocoNetSystemConnectionMemo(lnis, slotmanager);

        jmri.InstanceManager.setDefault(jmri.jmrix.loconet.LocoNetSystemConnectionMemo.class, memo);
        jmri.InstanceManager.setDefault(jmri.jmrit.roster.RosterConfigManager.class, new RosterConfigManager());

        // pane for LncvProg
        panel = new LncvProgPane();
        helpTarget = "package.jmri.jmrix.loconet.swing.lncvprog.LncvProgPane";
        title = Bundle.getMessage("MenuItemLncvProg");
    }

    @Override
    @AfterEach
    public void tearDown() {
        panel = null;

        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(LncvProgPane.class);

}
