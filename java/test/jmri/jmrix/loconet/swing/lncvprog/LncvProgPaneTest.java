package jmri.jmrix.loconet.swing.lncvprog;

import jmri.jmrit.roster.RosterConfigManager;
import jmri.jmrix.loconet.*;
import jmri.util.ThreadingUtil;
import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Test of the LNCV Programming Pane tool.
 *
 * @author Egbert Broerse   Copyright 2021
 * @author Paul Bender Copyright (C) 2017
 */
@Timeout(30)
public class LncvProgPaneTest extends jmri.util.swing.JmriPanelTest {

    private LocoNetSystemConnectionMemo memo;
    private LncvProgPane lnPanel;

    @Test
    @Override
    @DisabledIfHeadless
    public void testCTor() {
        LncvProgPane p = new LncvProgPane();
        assertNotNull(p, "exists");
    }

    @Test
    @Override
    public void testInitComponents() throws Exception{
        // for now, just makes sure there isn't an exception.
        lnPanel.initComponents(memo);
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
    @DisabledIfHeadless
    public void testDispose() {
        LncvProgPane p = new LncvProgPane();
        Assertions.assertTrue(p.isVisible(), "LNCV running = visible");
        assertNotNull(p, "exists");
        p.dispose();
        Assertions.assertFalse(p.isVisible(), "disposed = invisible");
    }

    @Test
    @DisabledIfHeadless
    public void testPanel() {
        LncvDevicesManager lcdm = memo.getLncvDevicesManager();
        lnPanel.initComponents(memo); // set up stuff
        panel.initComponents();
        // test list
        Assertions.assertNull(lnPanel.getModule(0), "Get LncvModule 0 from empty list");

        Thread dialog_thread1 = new Thread(() -> {
            JDialogOperator jfo = new JDialogOperator(Bundle.getMessage("WarningTitle") );
            new JButtonOperator(jfo, Bundle.getMessage("ButtonProceed")).doClick();
        });
        dialog_thread1.setName("BroadcastAll Dialog button Proceed clicked");
        dialog_thread1.start();

        ThreadingUtil.runOnGUI( () -> lnPanel.allProgButtonActionPerformed());

        JUnitUtil.waitFor(()-> !(dialog_thread1.isAlive()), "BroadcastAll Warning Dialog closed");
        Assertions.assertEquals("locked", lnPanel.getAddressEntry(), "AddressField locked");

        ThreadingUtil.runOnGUI( () -> lnPanel.allProgButtonActionPerformed()); // second click "Stop" will release buttons
        Assertions.assertEquals("1", lnPanel.getAddressEntry(), "AddressField unlocked");

        lcdm.message(new LocoNetMessage(new int[]{0xE5, 0x0F, 0x05, 0x49, 0x4B, 0x1F, 0x11, 0x29, 0x13, 0x00, 0x00, 0x08, 0x00, 0x00, 0x4D}));
        Assertions.assertEquals(1, lcdm.getDeviceCount(), "modules in devman list after add");
        Assertions.assertEquals(5033, lnPanel.getModule(0).getProductID(), "ProductID of LncvModule at index 0 after adding");
        // add same module to monitor, adding it to table and filling in texts entry fields
        lnPanel.message(new LocoNetMessage(new int[]{0xE5, 0x0F, 0x05, 0x49, 0x4B, 0x1F, 0x11, 0x29, 0x13, 0x00, 0x00, 0x08, 0x00, 0x00, 0x4D}));
        Assertions.assertEquals("", lnPanel.getArticleEntry(), "Article field still empty");

        lnPanel.copyEntry(8888, 77);
        Assertions.assertEquals("8888", lnPanel.getArticleEntry(), "ProductID copied to Article field");

        ThreadingUtil.runOnGUI( () -> lnPanel.modProgButtonActionPerformed());
        Assertions.assertEquals("locked", lnPanel.getAddressEntry(), "AddressField locked");

        lnPanel.setCvFields(1, 100); // enter some values for read
        lnPanel.readButtonActionPerformed();
        // no feedback, just confirm no exception is thrown
        ThreadingUtil.runOnGUI( () -> lnPanel.writeButtonActionPerformed());
        LocoNetMessage l = new LocoNetMessage(new int[]{0xB4, 0x6D, 0x7F, 0x59}); // OPC_LONG_ACK
        lnPanel.message(l); // check in monitor
        Assertions.assertEquals("confirmed by module 77\n",
                lnPanel.getMonitorContents().substring(lnPanel.getMonitorContents().length() - 23), "Write confirmed in Monitor");

        ThreadingUtil.runOnGUI( () -> lnPanel.modProgButtonActionPerformed()); // to turn programming off
        Assertions.assertEquals("77", lnPanel.getAddressEntry(), "AddressField 77");
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
        panel = lnPanel = new LncvProgPane();
        helpTarget = "package.jmri.jmrix.loconet.swing.lncvprog.LncvProgPane";
        title = Bundle.getMessage("MenuItemLncvProg");
    }

    @Override
    @AfterEach
    public void tearDown() {
        panel = null;
        memo.dispose();

        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(LncvProgPane.class);

}
