package jmri.jmrit.whereused;

import java.awt.GraphicsEnvironment;
import jmri.*;
import jmri.jmrit.logix.OBlock;
import jmri.jmrit.logix.OBlockManager;
import jmri.util.JUnitUtil;
import org.junit.*;
import org.netbeans.jemmy.operators.*;

/**
 * Tests for the WhereUsedFrame Class
 * @author Dave Sand Copyright (C) 2020
 */
public class WhereUsedFrameTest {

    @Test
    public void testFrame() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        WhereUsedFrame frame = new WhereUsedFrame();
        Assert.assertNotNull(frame);
        frame.setVisible(true);
        JFrameOperator jfo = new JFrameOperator(Bundle.getMessage("TitleWhereUsed"));  // NOI18N
        Assert.assertNotNull(jfo);

        // For each item type, create the bean combo box and verify it.  Build the report using a direct call.
        JComboBoxOperator jcoType = new JComboBoxOperator(jfo, 0);

        // Sensor
        jcoType.selectItem(1);
        String mgr = frame._itemNameBox.getManager().getClass().getSimpleName();
        Assert.assertEquals(mgr, "ProxySensorManager");
        Sensor sensor = InstanceManager.getDefault(SensorManager.class).getSensor("S-Main");
        frame.buildWhereUsedListing(WhereUsedFrame.ItemType.SENSOR, sensor);

        // Turnout
        jcoType.selectItem(2);
        mgr = frame._itemNameBox.getManager().getClass().getSimpleName();
        Assert.assertEquals(mgr, "ProxyTurnoutManager");
        Turnout turnout = InstanceManager.getDefault(TurnoutManager.class).getTurnout("LE Left");
        frame.buildWhereUsedListing(WhereUsedFrame.ItemType.TURNOUT, turnout);

        // Light
        jcoType.selectItem(3);
        mgr = frame._itemNameBox.getManager().getClass().getSimpleName();
        Assert.assertEquals(mgr, "ProxyLightManager");
        Light light = InstanceManager.getDefault(LightManager.class).getLight("L-Sensor Control");
        frame.buildWhereUsedListing(WhereUsedFrame.ItemType.LIGHT, light);

        // Signal Head
        jcoType.selectItem(4);
        mgr = frame._itemNameBox.getManager().getClass().getSimpleName();
        Assert.assertEquals(mgr, "AbstractSignalHeadManager");
        SignalHead signalHead = InstanceManager.getDefault(SignalHeadManager.class).getSignalHead("Left-AU");
        frame.buildWhereUsedListing(WhereUsedFrame.ItemType.SIGNALHEAD, signalHead);

        // Signal Mast
        jcoType.selectItem(5);
        mgr = frame._itemNameBox.getManager().getClass().getSimpleName();
        Assert.assertEquals(mgr, "DefaultSignalMastManager");
        SignalMast signalMast = InstanceManager.getDefault(SignalMastManager.class).getSignalMast("Left-A");
        frame.buildWhereUsedListing(WhereUsedFrame.ItemType.SIGNALMAST, signalMast);

        // Memory
        jcoType.selectItem(6);
        mgr = frame._itemNameBox.getManager().getClass().getSimpleName();
        Assert.assertEquals(mgr, "DefaultMemoryManager");
        Memory memory = InstanceManager.getDefault(MemoryManager.class).getMemory("BlockMemory");
        frame.buildWhereUsedListing(WhereUsedFrame.ItemType.MEMORY, memory);

        // Block
        jcoType.selectItem(7);
        mgr = frame._itemNameBox.getManager().getClass().getSimpleName();
        Assert.assertEquals(mgr, "BlockManager");
        Block block = InstanceManager.getDefault(BlockManager.class).getBlock("B-Main");
        frame.buildWhereUsedListing(WhereUsedFrame.ItemType.BLOCK, block);

        // OBlock
        jcoType.selectItem(8);
        mgr = frame._itemNameBox.getManager().getClass().getSimpleName();
        Assert.assertEquals(mgr, "OBlockManager");
        OBlock oblock = InstanceManager.getDefault(OBlockManager.class).getOBlock("OB::Main");
        frame.buildWhereUsedListing(WhereUsedFrame.ItemType.OBLOCK, oblock);

        JUnitUtil.dispose(frame);
    }

    @Test
    public void testSave() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        WhereUsedFrame frame = new WhereUsedFrame();
        Assert.assertNotNull(frame);
        frame.setVisible(true);

        // Cancel save request
        Thread cancelFile = createModalDialogOperatorThread(Bundle.getMessage("SaveDialogTitle"), Bundle.getMessage("ButtonCancel"), "cancelFile");  // NOI18N
        frame.saveWhereUsedPressed();
        JUnitUtil.waitFor(()->{return !(cancelFile.isAlive());}, "cancelFile finished");  // NOI18N

        // Complete save request
        Thread saveFile = createModalDialogOperatorThread(Bundle.getMessage("SaveDialogTitle"), Bundle.getMessage("ButtonSave"), "saveFile");  // NOI18N
        frame.saveWhereUsedPressed();
        JUnitUtil.waitFor(()->{return !(saveFile.isAlive());}, "saveFile finished");  // NOI18N

        // Replace duplicate file
        Thread saveFile2 = createModalDialogOperatorThread(Bundle.getMessage("SaveDialogTitle"), Bundle.getMessage("ButtonSave"), "saveFile2");  // NOI18N
        Thread replaceFile = createModalDialogOperatorThread(Bundle.getMessage("SaveDuplicateTitle"), Bundle.getMessage("SaveDuplicateReplace"), "replaceFile");  // NOI18N
        frame.saveWhereUsedPressed();
        JUnitUtil.waitFor(()->{return !(saveFile2.isAlive());}, "saveFile2 finished");  // NOI18N
        JUnitUtil.waitFor(()->{return !(replaceFile.isAlive());}, "replaceFile finished");  // NOI18N

        // Append duplicate file
        Thread saveFile3 = createModalDialogOperatorThread(Bundle.getMessage("SaveDialogTitle"), Bundle.getMessage("ButtonSave"), "saveFile3");  // NOI18N
        Thread appendFile = createModalDialogOperatorThread(Bundle.getMessage("SaveDuplicateTitle"), Bundle.getMessage("SaveDuplicateAppend"), "appendFile");  // NOI18N
        frame.saveWhereUsedPressed();
        JUnitUtil.waitFor(()->{return !(saveFile3.isAlive());}, "saveFile3 finished");  // NOI18N
        JUnitUtil.waitFor(()->{return !(appendFile.isAlive());}, "appendFile finished");  // NOI18N

        // Cancel duplicate file
        Thread saveFile4 = createModalDialogOperatorThread(Bundle.getMessage("SaveDialogTitle"), Bundle.getMessage("ButtonSave"), "saveFile4");  // NOI18N
        Thread cancelFile2 = createModalDialogOperatorThread(Bundle.getMessage("SaveDuplicateTitle"), Bundle.getMessage("ButtonCancel"), "cancelFile2");  // NOI18N
        frame.saveWhereUsedPressed();
        JUnitUtil.waitFor(()->{return !(saveFile4.isAlive());}, "saveFile4 finished");  // NOI18N
        JUnitUtil.waitFor(()->{return !(cancelFile2.isAlive());}, "cancelFile2 finished");  // NOI18N

        JUnitUtil.dispose(frame);
    }

    Thread createModalDialogOperatorThread(String dialogTitle, String buttonText, String threadName) {
        Thread t = new Thread(() -> {
            // constructor for jdo will wait until the dialog is visible
            JDialogOperator jdo = new JDialogOperator(dialogTitle);
            JButtonOperator jbo = new JButtonOperator(jdo, buttonText);
            jbo.pushNoBlock();
        });
        t.setName(dialogTitle + " Close Dialog Thread: " + threadName);  // NOI18N
        t.start();
        return t;
    }

    @Before
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initRosterConfigManager();
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        jmri.configurexml.ConfigXmlManager cm = new jmri.configurexml.ConfigXmlManager();
        java.io.File f = new java.io.File("java/test/jmri/jmrit/whereused/load/WhereUsedTesting.xml");  // NOI18N
        cm.load(f);
   }

    @After
    public  void tearDown() {
        JUnitUtil.tearDown();
    }

//     private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(WhereUsedFrameTest.class);
}
