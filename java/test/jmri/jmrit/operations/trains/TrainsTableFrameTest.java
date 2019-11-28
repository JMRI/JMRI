package jmri.jmrit.operations.trains;

import java.awt.GraphicsEnvironment;
import java.text.MessageFormat;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.routes.RouteManager;
import jmri.util.JUnitOperationsUtil;
import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;
import jmri.util.swing.JemmyUtil;

import org.junit.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class TrainsTableFrameTest extends OperationsTestCase {

    @Rule
    public org.junit.rules.Timeout globalTimeout = org.junit.rules.Timeout.seconds(10);

    @Rule
    public jmri.util.junit.rules.RetryRule retryRule = new jmri.util.junit.rules.RetryRule(3); // first, plus three retries

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        TrainsTableFrame t = new TrainsTableFrame();
        Assert.assertNotNull("exists", t);
        JUnitUtil.dispose(t);
    }

    @Test
    public void testTrainsTableFrame() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        JUnitOperationsUtil.loadTrains();
        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);

        TrainsTableFrame f = new TrainsTableFrame();
        f.setLocation(10, 20);

        JemmyUtil.enterClickAndLeave(f.saveButton);

        Assert.assertEquals("sort by name", TrainsTableModel.TIMECOLUMNNAME, f.getSortBy());
        Assert.assertTrue("Build Messages", tmanager.isBuildMessagesEnabled());
        Assert.assertFalse("Build Report", tmanager.isBuildReportEnabled());
        Assert.assertFalse("Print Review", tmanager.isPrintPreviewEnabled());

        JemmyUtil.enterClickAndLeave(f.showTime);
        JemmyUtil.enterClickAndLeave(f.buildMsgBox);
        JemmyUtil.enterClickAndLeave(f.buildReportBox);
        JemmyUtil.enterClickAndLeave(f.saveButton);

        Assert.assertFalse("Build Messages 2", tmanager.isBuildMessagesEnabled());
        Assert.assertTrue("Build Report 2", tmanager.isBuildReportEnabled());
        Assert.assertFalse("Print Review 2", tmanager.isPrintPreviewEnabled());

        JemmyUtil.enterClickAndLeave(f.showId);
        JemmyUtil.enterClickAndLeave(f.buildMsgBox);
        JemmyUtil.enterClickAndLeave(f.printPreviewBox);
        JemmyUtil.enterClickAndLeave(f.saveButton);

        Assert.assertTrue("Build Messages 3", tmanager.isBuildMessagesEnabled());
        Assert.assertTrue("Build Report 3", tmanager.isBuildReportEnabled());
        Assert.assertTrue("Print Review 3", tmanager.isPrintPreviewEnabled());

        // create the TrainEditFrame
        JemmyUtil.enterClickAndLeave(f.addButton);

        // confirm panel creation
        JmriJFrame tef = JmriJFrame.getFrame(Bundle.getMessage("TitleTrainAdd"));
        Assert.assertNotNull("train edit frame", tef);

        // create the TrainSwichListEditFrame
        JemmyUtil.enterClickAndLeave(f.switchListsButton);

        // confirm panel creation
        JmriJFrame tsle = JmriJFrame.getFrame(Bundle.getMessage("TitleSwitchLists"));
        Assert.assertNotNull("train switchlist edit frame", tsle);

        // kill panels
        JUnitUtil.dispose(tef);
        JUnitUtil.dispose(tsle);
        JUnitUtil.dispose(f);
    }

    @Test
    public void testRadioButtons() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        JUnitOperationsUtil.loadTrains();

        TrainsTableFrame f = new TrainsTableFrame();
        f.setLocation(10, 20);

        JemmyUtil.enterClickAndLeave(f.resetRB);
        JemmyUtil.enterClickAndLeave(f.terminateRB);
        JemmyUtil.enterClickAndLeave(f.conductorRB);
        JemmyUtil.enterClickAndLeave(f.moveRB);

        JUnitUtil.dispose(f);
    }

    @Test
    public void testBuildButtonFailure() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        JUnitOperationsUtil.loadTrains();
        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);

        TrainsTableFrame f = new TrainsTableFrame();
        f.setLocation(10, 20);

        // confirm defaults
        for (Train train : tmanager.getTrainsByNameList()) {
            Assert.assertFalse(train.isBuilt());
            Assert.assertTrue(train.isBuildEnabled());
            Assert.assertFalse(train.getBuildFailed());
        }

        // must disable build failure messages or thread lock
        tmanager.setBuildMessagesEnabled(false);

        JemmyUtil.enterClickAndLeave(f.buildButton);

        // need to wait for builds to complete
        Thread build = JUnitUtil.getThreadByName("Build Trains");
        if (build != null) {
            try {
                build.join();
            } catch (InterruptedException e) {
                // do nothing
            }
        }

        // confirm build failed for all trains
        for (Train train : tmanager.getTrainsByNameList()) {
            Assert.assertFalse(train.isBuilt());
            Assert.assertTrue(train.isBuildEnabled());
            Assert.assertTrue(train.getBuildFailed());
        }

        JUnitUtil.dispose(f);
    }

    @Test
    public void testBuildButtonSuccess() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        JUnitOperationsUtil.loadTrains();
        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);

        TrainsTableFrame ttf = new TrainsTableFrame();
        ttf.setLocation(10, 20);

        Route route = InstanceManager.getDefault(RouteManager.class).getRouteByName("Test Route D");
        Assert.assertNotNull(route);
        Location location = InstanceManager.getDefault(LocationManager.class).getLocationByName("Test_Location 1");
        route.addLocation(location);

        // confirm defaults
        for (Train train : tmanager.getTrainsByNameList()) {
            Assert.assertFalse(train.isBuilt());
            Assert.assertTrue(train.isBuildEnabled());
            Assert.assertFalse(train.getBuildFailed());
            train.setRoute(route);
        }

        // must disable build failure messages or thread lock
        tmanager.setBuildMessagesEnabled(false);

        JemmyUtil.enterClickAndLeave(ttf.buildButton);

        // need to wait for builds to complete
        Thread build = JUnitUtil.getThreadByName("Build Trains");
        if (build != null) {
            try {
                build.join();
            } catch (InterruptedException e) {
                // do nothing
            }
        }

        // confirm build succeeded for all trains
        for (Train train : tmanager.getTrainsByNameList()) {
            Assert.assertTrue(train.isBuilt());
            Assert.assertTrue(train.isBuildEnabled());
            Assert.assertFalse(train.getBuildFailed());
        }
        
        // dialog window asks if user wants to terminate train, answer no
        JemmyUtil.enterClickAndLeave(ttf.terminateButton);

        for (Train train : tmanager.getTrainsByNameList()) {
            JemmyUtil.pressDialogButton(MessageFormat.format(Bundle
                    .getMessage("TerminateTrain"),
                    new Object[]{train.getName(), train.getDescription()}), Bundle.getMessage("ButtonNo"));
        }
        
        // confirm no change in status
        for (Train train : tmanager.getTrainsByNameList()) {
            Assert.assertTrue(train.isBuilt());
            Assert.assertTrue(train.isBuildEnabled());
            Assert.assertFalse(train.getBuildFailed());
        }

     // dialog window asks if user wants to terminate train, answer yes
        JemmyUtil.enterClickAndLeave(ttf.terminateButton);

        for (Train train : tmanager.getTrainsByNameList()) {
            JemmyUtil.pressDialogButton(MessageFormat.format(Bundle
                    .getMessage("TerminateTrain"),
                    new Object[]{train.getName(), train.getDescription()}), Bundle.getMessage("ButtonYes"));
        }
        
        // confirm all trains terminated
        for (Train train : tmanager.getTrainsByNameList()) {
            Assert.assertFalse(train.isBuilt());
            Assert.assertTrue(train.isBuildEnabled());
            Assert.assertFalse(train.getBuildFailed());
        }

        JUnitUtil.dispose(ttf);
    }

    @Test
    public void testTerminateButton() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        JUnitOperationsUtil.loadTrains();
        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);

        TrainsTableFrame f = new TrainsTableFrame();
        f.setLocation(10, 20);

        // confirm defaults
        for (Train train : tmanager.getTrainsByNameList()) {
            Assert.assertFalse(train.isBuilt());
            Assert.assertTrue(train.isBuildEnabled());
            Assert.assertFalse(train.getBuildFailed());
        }

        JemmyUtil.enterClickAndLeave(f.terminateButton);

        JUnitUtil.dispose(f);
    }

    // private final static Logger log = LoggerFactory.getLogger(TrainsTableFrameTest.class);

}
