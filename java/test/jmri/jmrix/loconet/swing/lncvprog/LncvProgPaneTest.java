package jmri.jmrix.loconet.swing.lncvprog;

import jmri.jmrit.roster.RosterConfigManager;
import jmri.jmrix.loconet.*;
import jmri.jmrix.loconet.downloader.LoaderPane;
import jmri.util.JUnitUtil;
import org.junit.Assert;
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
        ((LncvProgPane) panel).initComponents(memo);
    }

    @Test
    @Override
    public void testGetHelpTarget(){
        Assert.assertEquals("help target", helpTarget, panel.getHelpTarget());
    }

    @Test
    @Override
    public void testGetTitle(){
        Assert.assertEquals("title", title, panel.getTitle());
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
        Assert.assertNull("Get LncvModule 0 from empty list", panel.getModule(0));

        Thread dialog_thread1 = new Thread(() -> {
            JDialogOperator jfo = new JDialogOperator(Bundle.getMessage("WarningTitle") );
            new JButtonOperator(jfo, Bundle.getMessage("ButtonProceed")).doClick();
        });
        dialog_thread1.setName("BroadcastAll Dialog button Proceed clicked");
        dialog_thread1.start();

        panel.allProgButtonActionPerformed();

        JUnitUtil.waitFor(()-> !(dialog_thread1.isAlive()), "BroadcastAll Warning Dialog closed");
        Assert.assertEquals("AddressField locked", "locked", panel.getAddressEntry());

        panel.allProgButtonActionPerformed(); // second click "Stop" will release buttons
        Assert.assertEquals("AddressField unlocked", "1", panel.getAddressEntry());

        lcdm.message(new LocoNetMessage(new int[]{0xE5, 0x0F, 0x05, 0x49, 0x4B, 0x1F, 0x11, 0x29, 0x13, 0x00, 0x00, 0x08, 0x00, 0x00, 0x4D}));
        Assert.assertEquals("modules in devman list after add", 1, lcdm.getDeviceCount());
        Assert.assertEquals("ProductID of LncvModule at index 0 after adding", 5033, panel.getModule(0).getProductID());
        // add same module to monitor, adding it to table and filling in texts entry fields
        panel.message(new LocoNetMessage(new int[]{0xE5, 0x0F, 0x05, 0x49, 0x4B, 0x1F, 0x11, 0x29, 0x13, 0x00, 0x00, 0x08, 0x00, 0x00, 0x4D}));
        Assert.assertEquals("Article field still empty", "", panel.getArticleEntry());

        panel.copyEntry(88, 77);
        Assert.assertEquals("ProductID copied to Article field", "88", panel.getArticleEntry());

//        Thread dialog_thread2 = new Thread(() -> {
//            JDialogOperator jfo = new JDialogOperator("Waarschuwing");//Bundle.getMessage("WarningTitle") );
//            new JButtonOperator(jfo, Bundle.getMessage("ButtonProceed")).doClick();
//        });
//        dialog_thread2.setName("BroadcastAll Dialog button Proceed clicked");
//        dialog_thread2.start();

        panel.modProgButtonActionPerformed();
        Assert.assertEquals("AddressField locked", "locked", panel.getAddressEntry());

//        JUnitUtil.waitFor(()-> !(dialog_thread2.isAlive()), "BroadcastAll Warning Dialog closed");

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
