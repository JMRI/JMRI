package jmri.jmrit.operations.trains;

import java.awt.GraphicsEnvironment;
import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsSwingTestCase;
import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class TrainsTableFrameTest extends OperationsSwingTestCase {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        TrainsTableFrame t = new TrainsTableFrame();
        Assert.assertNotNull("exists",t);
        JUnitUtil.dispose(t);
    }
    
    @Test
    public void testTrainsTableFrame() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);

        TrainsTableFrame f = new TrainsTableFrame();
        f.setLocation(10, 20);

        enterClickAndLeave(f.saveButton);

        Assert.assertEquals("sort by name", TrainsTableModel.TIMECOLUMNNAME, f.getSortBy());
        Assert.assertTrue("Build Messages", tmanager.isBuildMessagesEnabled());
        Assert.assertFalse("Build Report", tmanager.isBuildReportEnabled());
        Assert.assertFalse("Print Review", tmanager.isPrintPreviewEnabled());

        enterClickAndLeave(f.showTime);
        enterClickAndLeave(f.buildMsgBox);
        enterClickAndLeave(f.buildReportBox);
        enterClickAndLeave(f.saveButton);

        Assert.assertFalse("Build Messages 2", tmanager.isBuildMessagesEnabled());
        Assert.assertTrue("Build Report 2", tmanager.isBuildReportEnabled());
        Assert.assertFalse("Print Review 2", tmanager.isPrintPreviewEnabled());

        enterClickAndLeave(f.showId);
        enterClickAndLeave(f.buildMsgBox);
        enterClickAndLeave(f.printPreviewBox);
        enterClickAndLeave(f.saveButton);

        Assert.assertTrue("Build Messages 3", tmanager.isBuildMessagesEnabled());
        Assert.assertTrue("Build Report 3", tmanager.isBuildReportEnabled());
        Assert.assertTrue("Print Review 3", tmanager.isPrintPreviewEnabled());

        // create the TrainEditFrame
        enterClickAndLeave(f.addButton);

        // confirm panel creation
        JmriJFrame tef = JmriJFrame.getFrame(Bundle.getMessage("TitleTrainAdd"));
        Assert.assertNotNull("train edit frame", tef);

        // create the TrainSwichListEditFrame
        enterClickAndLeave(f.switchListsButton);

        // confirm panel creation
        JmriJFrame tsle = JmriJFrame.getFrame(Bundle.getMessage("TitleSwitchLists"));
        Assert.assertNotNull("train switchlist edit frame", tsle);

        // kill panels
        JUnitUtil.dispose(tef);
        JUnitUtil.dispose(tsle);
        JUnitUtil.dispose(f);
    }


    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        loadTrains();
    }
    
     @Override
     @After
     public void tearDown() throws Exception {
         super.tearDown();
     }

    // private final static Logger log = LoggerFactory.getLogger(TrainsTableFrameTest.class);

}
