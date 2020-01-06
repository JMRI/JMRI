package jmri.jmrit.beantable.oblock;

import java.awt.*;

import jmri.Block;
import jmri.InstanceManager;
import jmri.Path;
import jmri.Sensor;
import jmri.implementation.AbstractSensor;
import jmri.jmrit.logix.OBlockManager;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JFrameOperator;

import javax.swing.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class TableFramesTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        TableFrames t = new TableFrames();
        Assert.assertNotNull("exists", t);
        t.initComponents();
    }

    @Test
    public void testImport() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
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
        // neither works, so suppressed the Import Ready dialog for now
        //Container pane = JUnitUtil.findContainer(Bundle.getMessage("MessageTitle"));
        //Assert.assertNotNull("Import complete dialog", pane);
        //new JButtonOperator(new JFrameOperator((JFrame) pane), Bundle.getMessage("ButtonOK")).doClick();
        // check import result

        Assert.assertNotNull("Imported OBlock", InstanceManager.getDefault(OBlockManager.class).getOBlock("OB0001"));
        jmri.util.JUnitAppender.assertWarnMessage("Portal IP0001-0002 needs an OBlock on each side");
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.resetWindows(false,false);
        JUnitUtil.tearDown();
    }

    //private final static Logger log = LoggerFactory.getLogger(TableFramesTest.class);

}
