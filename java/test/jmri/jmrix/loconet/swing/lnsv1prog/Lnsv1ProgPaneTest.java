package jmri.jmrix.loconet.swing.lnsv1prog;

import jmri.jmrit.roster.RosterConfigManager;
import jmri.jmrix.loconet.*;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;
import org.junit.jupiter.api.*;
//import org.netbeans.jemmy.operators.JButtonOperator;
//import org.netbeans.jemmy.operators.JDialogOperator;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Test of the LNSV1 Programming Pane tool.
 *
 * @author Egbert Broerse   Copyright 2021, 2025
 * @author Paul Bender Copyright (C) 2017
 */
@Timeout(30)
public class Lnsv1ProgPaneTest extends jmri.util.swing.JmriPanelTest {

    private LocoNetSystemConnectionMemo memo;
    private Lnsv1ProgPane lnPanel;

    @Test
    @Override
    @DisabledIfHeadless
    public void testCTor() {
        Lnsv1ProgPane p = new Lnsv1ProgPane();
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
        Lnsv1ProgPane p = new Lnsv1ProgPane();
        Assertions.assertTrue(p.isVisible(), "LNSV1 running = visible");
        assertNotNull(p, "exists");
        p.dispose();
        Assertions.assertFalse(p.isVisible(), "disposed = invisible");
    }

    @Test
    @DisabledIfHeadless
    public void testPanel() {
        Lnsv1DevicesManager lsv1dm = memo.getLnsv1DevicesManager();
        lnPanel.initComponents(memo); // set up stuff
        panel.initComponents();
        // test list
        Assertions.assertNull(lnPanel.getModule(0), "Get Lnsv1Module 0 from empty list");

        ThreadingUtil.runOnGUI( () -> lnPanel.probeAllButtonActionPerformed()); // no replies in a test

//        JUnitUtil.waitFor(()-> !(dialog_thread1.isAlive()), "BroadcastAll Warning Dialog closed");
//        Assertions.assertEquals("locked", lnPanel.getAddressEntry(), "AddressField locked");

        // TODO Press Set All Addresses button after filling in low and high address fields

        lnPanel.setCvFields(2, 14);
        //Assertions.assertEquals("1", lnPanel.getAddressEntry(), "AddressField unlocked");

        lsv1dm.message(new LocoNetMessage(new int[] {0xE5, 0x10, 0x51, 0x50, 0x01, 0x02, 0x02, 0x33, 0x02, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x18}));
        Assertions.assertEquals(1, lsv1dm.getDeviceCount(), "modules in table after probe");
//        Assertions.assertEquals(5033, lnPanel.getModule(0).getProductID(), "ProductID of Lnsv1Module at index 0 after adding");
//        // add same module to monitor, adding it to table and filling in texts entry fields
//        lnPanel.message(new LocoNetMessage(new int[]{0xE5, 0x0F, 0x05, 0x49, 0x4B, 0x1F, 0x11, 0x29, 0x13, 0x00, 0x00, 0x08, 0x00, 0x00, 0x4D}));
//        Assertions.assertEquals("", lnPanel.getArticleEntry(), "Article field still empty");
//
//        lnPanel.copyEntry(8888, 77);
//        Assertions.assertEquals("8888", lnPanel.getArticleEntry(), "ProductID copied to Article field");
//
//        ThreadingUtil.runOnGUI( () -> lnPanel.modProgButtonActionPerformed());
//        Assertions.assertEquals("locked", lnPanel.getAddressEntry(), "AddressField locked");
//
        // TODO Press Read button
//        lnPanel.setCvFields(1, 100); // enter some values for read
//        lnPanel.readButtonActionPerformed();
//        // no feedback, just confirm no exception is thrown
//        ThreadingUtil.runOnGUI( () -> lnPanel.writeButtonActionPerformed());
//        LocoNetMessage l = new LocoNetMessage(new int[]{0xB4, 0x6D, 0x7F, 0x59}); // OPC_LONG_ACK
//        lnPanel.message(l); // check in monitor
//        Assertions.assertEquals("confirmed by module 77\n",
//                lnPanel.getMonitorContents().substring(lnPanel.getMonitorContents().length() - 23), "Write confirmed in Monitor");
//
//        ThreadingUtil.runOnGUI( () -> lnPanel.modProgButtonActionPerformed()); // to turn programming off
//        Assertions.assertEquals("77", lnPanel.getAddressEntry(), "AddressField 77");
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

        jmri.InstanceManager.setDefault(LocoNetSystemConnectionMemo.class, memo);
        jmri.InstanceManager.setDefault(RosterConfigManager.class, new RosterConfigManager());

        // pane for Lnsv1Prog
        panel = lnPanel = new Lnsv1ProgPane();
        helpTarget = "package.jmri.jmrix.loconet.swing.lnsv1prog.Lnsv1ProgPane";
        title = Bundle.getMessage("MenuItemLnsv1Prog");
    }

    @Override
    @AfterEach
    public void tearDown() {
        panel = null;
        memo.dispose();

        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(Lnsv1ProgPane.class);

}
