package jmri.jmrit.beantable.oblock;

import java.awt.*;

import jmri.Block;
import jmri.InstanceManager;
import jmri.Path;
import jmri.implementation.AbstractSensor;
import jmri.jmrit.logix.OBlockManager;
import jmri.util.JUnitUtil;

import jmri.util.gui.GuiLafPreferencesManager;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class TableFramesTest {

    @Test
    public void testCTor() {
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless());
        InstanceManager.getDefault(GuiLafPreferencesManager.class).setOblockEditTabbed(false);
        TableFrames tf = new TableFrames();
        Assertions.assertNotNull(tf, "exists");
        tf.initComponents();
    }

    @Test
    public void testCTorTabbed() {
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless());
        InstanceManager.getDefault(GuiLafPreferencesManager.class).setOblockEditTabbed(true);
        TableFrames tf = new TableFrames();
        Assertions.assertNotNull(tf, "exists");
    }

    @Test
    public void testImport() {
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless());
        // use original _desktop interface
        InstanceManager.getDefault(GuiLafPreferencesManager.class).setOblockEditTabbed(false);

        TableFrames t = new TableFrames();
        t.initComponents();
        // mute warnings
        t.setShowWarnings("No");
        // set up Block to import
        Block b1 = InstanceManager.getDefault(jmri.BlockManager.class).provideBlock("IB:AUTO:0001");
        b1.setUserName("block 1");
        b1.setLength(120);
        b1.setCurvature(21);
        Block b2 = InstanceManager.getDefault(jmri.BlockManager.class).provideBlock("IB:AUTO:0002");
        b2.setUserName("block 2");
        b2.setLength(100);
        b1.addPath(new Path(b2, 64, 128));
        b2.addPath(new Path(b1, 128, 64));
        new AbstractSensor("IS1") {
            @Override
            public void requestUpdateFromLayout() {
            }
        };
        b1.setSensor("IS1");
        // call import method
        t.importBlocks();
        // find + close Message Dialog "Finished"
        new org.netbeans.jemmy.QueueTool().waitEmpty();
        // create a thread that waits to close the dialog box opened later
//        Thread thr = new Thread(() -> {
//            // constructor for jdo will wait until the dialog is visible
//            JDialogOperator jdo = new JDialogOperator(Bundle.getMessage("MessageTitle"));
//            new JButtonOperator(jdo, Bundle.getMessage("ButtonOK")).doClick();
//        });
        // neither works, so suppress the Import Ready dialog for now
        //Container pane = JUnitUtil.findContainer(Bundle.getMessage("MessageTitle"));
        //Assert.assertNotNull("Import complete dialog", pane);
        //new JButtonOperator(new JFrameOperator((JFrame) pane), Bundle.getMessage("ButtonOK")).doClick();
        // check import result

        Assertions.assertNotNull(InstanceManager.getDefault(OBlockManager.class).getOBlock("OB0001"), "Imported OBlock");
        //2 x WARN  - Portal IP0001-0002 needs an OBlock on each side [main] jmrit.beantable.oblock.SignalTableModel.makeList()
        jmri.util.JUnitAppender.assertWarnMessage("Portal IP0001-0002 needs an OBlock on each side");
        jmri.util.JUnitAppender.assertWarnMessage("Portal IP0001-0002 needs an OBlock on each side");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.resetWindows(false,false);
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.deregisterEditorManagerShutdownTask();
        JUnitUtil.tearDown();
    }

    //private final static Logger log = LoggerFactory.getLogger(TableFramesTest.class);

}
