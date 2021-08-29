package jmri.jmrit.operations.trains;

import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.text.MessageFormat;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.operators.JRadioButtonOperator;
import org.netbeans.jemmy.operators.JTableOperator;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.routes.RouteManager;
import jmri.jmrit.operations.setup.Setup;
import jmri.util.JUnitOperationsUtil;
import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;
import jmri.util.junit.rules.RetryRule;
import jmri.util.swing.JemmyUtil;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
@Timeout(10)
public class TrainsTableFrameTest extends OperationsTestCase {

    public RetryRule retryRule = new RetryRule(3); // first, plus three retries

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        TrainsTableFrame t = new TrainsTableFrame();
        Assert.assertNotNull("exists", t);
        JUnitUtil.dispose(t);
        JUnitOperationsUtil.checkOperationsShutDownTask();
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
        JemmyUtil.enterClickAndLeave(f.showAllBox);
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
        JUnitOperationsUtil.checkOperationsShutDownTask();
    }

    @Test
    public void testTableEditButton() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JUnitOperationsUtil.loadTrains();

        TrainsTableAction a = new TrainsTableAction();
        a.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));

        TrainsTableFrame tef = (TrainsTableFrame) JmriJFrame.getFrame(Bundle.getMessage("TitleTrainsTable"));
        Assert.assertNotNull(tef);

        // Find new table window
        JFrameOperator jfo = new JFrameOperator(tef);

        // Open train edit window
        JTableOperator tbl = new JTableOperator(jfo);
        tbl.clickOnCell(0, tbl.findColumn(Bundle.getMessage("ButtonEdit")));

        new JFrameOperator("Edit Train");
        // find the edit window
        JmriJFrame et = JmriJFrame.getFrame("Edit Train");
        Assert.assertNotNull(et);

        // kill panels
        JUnitUtil.dispose(tef);
        // should have disposed the edit train window
        et = JmriJFrame.getFrame("Edit Train");
        Assert.assertNull(et);
    }

    @Test
    public void testTableBuildCheckBox() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JUnitOperationsUtil.loadTrains();
        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);

        Train train0 = tmanager.getTrainByName("Test_Train 0");
        Assert.assertNotNull(train0);
        Assert.assertTrue("default selected", train0.isBuildEnabled());
        Assert.assertEquals("Number of trains", 5, tmanager.getNumEntries());

        // improve test coverage by showing built dates
        train0.setBuiltEndYear("1956");

        TrainsTableAction a = new TrainsTableAction();
        a.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));

        TrainsTableFrame tef = (TrainsTableFrame) JmriJFrame.getFrame(Bundle.getMessage("TitleTrainsTable"));
        Assert.assertNotNull(tef);

        // Find new table window
        JFrameOperator jfo = new JFrameOperator(tef);

        // Open train edit window
        JTableOperator tbl = new JTableOperator(jfo);
        tbl.clickOnCell(0, tbl.findColumn(Bundle.getMessage("Build")));
        Assert.assertFalse("build deselected", train0.isBuildEnabled());

        JemmyUtil.enterClickAndLeave(tef.showAllBox);
        Assert.assertEquals("table size", 4, tef.trainsTable.getRowCount());

        // kill panels
        JUnitUtil.dispose(tef);
    }

    @Test
    public void testTableBuildButton() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JUnitOperationsUtil.loadTrains();
        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);

        Train train0 = tmanager.getTrainByName("Test_Train 0");
        Assert.assertNotNull(train0);
        Assert.assertFalse("train build status", train0.isBuilt());

        Train train1 = tmanager.getTrainByName("Test_Train 1");
        Assert.assertNotNull(train1);
        Assert.assertFalse("train build status", train1.isBuilt());

        Train train2 = tmanager.getTrainByName("Test_Train 2");
        Assert.assertNotNull(train2);
        Assert.assertFalse("train build status", train2.isBuilt());

        Route route = JUnitOperationsUtil.createFiveLocationRoute();
        train0.setRoute(route);

        // improve test coverage by showing built dates
        train0.setBuiltStartYear("1956");

        // improve test coverage by showing road names
        train0.setRoadOption(Train.INCLUDE_ROADS);
        String[] roads = { "SP", "UP" };
        train0.setRoadNames(roads);

        // improve test coverage by showing load names
        train0.setLoadOption(Train.EXCLUDE_LOADS);
        String[] loads = { "Coal", "Boxes" };
        train0.setLoadNames(loads);

        // improve test coverage by showing owner names
        train0.setOwnerOption(Train.EXCLUDE_OWNERS);
        String[] owners = { "DAB", "AAR" };
        train0.setOwnerNames(owners);

        TrainsTableAction a = new TrainsTableAction();
        a.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));

        TrainsTableFrame tef = (TrainsTableFrame) JmriJFrame.getFrame(Bundle.getMessage("TitleTrainsTable"));
        Assert.assertNotNull(tef);

        // Find new table window by name
        JFrameOperator jfo = new JFrameOperator(tef);

        // build train 0
        JTableOperator tbl = new JTableOperator(jfo);
        tbl.clickOnCell(0, tbl.findColumn(Bundle.getMessage("Function"))); // build button

        jmri.util.JUnitUtil.waitFor(() -> {
            return train0.isBuilt();
        }, "wait train to build");
        Assert.assertTrue("train build status", train0.isBuilt());

        // build train 1, error no route!
        JemmyUtil.clickOnCellThreadSafe(tbl, 1, Bundle.getMessage("Function"));

        // a popup warning that the train doesn't have a route should appear
        JemmyUtil.pressDialogButton(MessageFormat.format(Bundle.getMessage("buildErrorMsg"),
                new Object[] { train1.getName(), train1.getDescription() }), Bundle.getMessage("ButtonOK"));

        // try to move train 2, error not built
        JemmyUtil.clickOnCellThreadSafe(tbl, 2, Bundle.getMessage("Action"));

        // a popup warning that the train isn't built should appear
        JemmyUtil.pressDialogButton(Bundle.getMessage("CanNotPerformAction"), Bundle.getMessage("ButtonOK"));

        // test action button "Move"
        tbl.clickOnCell(0, tbl.findColumn(Bundle.getMessage("Action"))); // move button
        Assert.assertEquals("move train", "Boston", train0.getCurrentLocationName());

        // now test "Terminate"
        JRadioButtonOperator jbo = new JRadioButtonOperator(jfo, Bundle.getMessage("Terminate"));
        jbo.doClick();
        // test action button "Terminate"
        JemmyUtil.clickOnCellThreadSafe(tbl, 0, Bundle.getMessage("Action"));

        // a popup warning that Manifest hasn't been printed should appear
        JemmyUtil.pressDialogButton(MessageFormat.format(Bundle.getMessage("DoYouWantToTermiate"), new Object[] { train0.getName() }), Bundle.getMessage("ButtonYes"));

        Assert.assertEquals("terminate train", "", train0.getCurrentLocationName());
        Assert.assertFalse("train build status", train0.isBuilt());

        // kill panels
        JUnitUtil.dispose(tef);
    }

    @Test
    public void testTableConductorButton() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JUnitOperationsUtil.loadTrains();
        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);

        Train train0 = tmanager.getTrainByName("Test_Train 0");
        Assert.assertNotNull(train0);
        Assert.assertFalse("train build status", train0.isBuilt());

        Route route = JUnitOperationsUtil.createFiveLocationRoute();
        train0.setRoute(route);

        TrainsTableAction a = new TrainsTableAction();
        a.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));

        TrainsTableFrame tef = (TrainsTableFrame) JmriJFrame.getFrame(Bundle.getMessage("TitleTrainsTable"));
        Assert.assertNotNull(tef);

        // Find new table window
        JFrameOperator jfo = new JFrameOperator(tef);

        // build train 0
        JTableOperator tbl = new JTableOperator(jfo);
        tbl.clickOnCell(0, tbl.findColumn(Bundle.getMessage("Function"))); // build button

        jmri.util.JUnitUtil.waitFor(() -> {
            return train0.isBuilt();
        }, "wait train to build");
        Assert.assertTrue("train build status", train0.isBuilt());

        // test action button "Conductor"
        JRadioButtonOperator jrbo = new JRadioButtonOperator(jfo, Bundle.getMessage("Conductor"));
        jrbo.doClick();
        tbl.clickOnCell(0, tbl.findColumn(Bundle.getMessage("Action"))); // Conductor button

        // Find conductor window by name
        JFrameOperator jfoc = new JFrameOperator(
                Bundle.getMessage("TitleTrainConductor") + " (" + train0.getName() + ")");
        // Move train using conductor window
        JButtonOperator jbo = new JButtonOperator(jfoc, Bundle.getMessage("Move"));
        jbo.doClick();
        Assert.assertEquals("Train moved", "Boston", train0.getCurrentLocationName());

        // kill panels
        jfoc.dispose();
        JUnitUtil.dispose(tef);
    }

    /**
     * Test table reset button. Train departs and terminates into staging.
     */
    @Test
    public void testTableResetActionButton() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JUnitOperationsUtil.initOperationsData();
        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);

        Setup.setStagingTrackImmediatelyAvail(true);

        Train train = tmanager.getTrainByName("STF");
        Assert.assertNotNull(train);
        Assert.assertFalse("train build status", train.isBuilt());

        // improve test coverage by showing built dates
        train.setBuiltStartYear("1956");
        train.setBuiltEndYear("2021");

        // improve test coverage by using table colors
        tmanager.setRowColorsManual(false);
        tmanager.setRowColorNameForBuilt("Blue");
        train.setRowColorNameReset("Yellow");

        TrainsTableAction a = new TrainsTableAction();
        a.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));

        TrainsTableFrame tef = (TrainsTableFrame) JmriJFrame.getFrame(Bundle.getMessage("TitleTrainsTable"));
        Assert.assertNotNull(tef);

        JFrameOperator jfo = new JFrameOperator(tef);

        // build train
        JTableOperator tbl = new JTableOperator(jfo);
        tbl.clickOnCell(0, tbl.findColumn(Bundle.getMessage("Function"))); // build button

        jmri.util.JUnitUtil.waitFor(() -> {
            return train.isBuilt();
        }, "wait train to build");
        Assert.assertTrue("train build status", train.isBuilt());

        // test action button "Reset"
        JRadioButtonOperator jbo = new JRadioButtonOperator(jfo, Bundle.getMessage("Reset"));
        jbo.doClick();
        tbl.clickOnCell(0, tbl.findColumn(Bundle.getMessage("Action"))); // Reset button
        Assert.assertFalse("train build status", train.isBuilt());

        // for test coverage reset train 1
        tbl.clickOnCell(1, tbl.findColumn(Bundle.getMessage("Action"))); // Reset button

        // build again
        tbl.clickOnCell(0, tbl.findColumn(Bundle.getMessage("Function"))); // build button

        jmri.util.JUnitUtil.waitFor(() -> {
            return train.isBuilt();
        }, "wait train to build");

        train.move(); // can't reset a train once it departs

        JemmyUtil.clickOnCellThreadSafe(tbl, 0 , Bundle.getMessage("Action"));

        // a popup warning can't reset train should appear
        JemmyUtil.pressDialogButton(Bundle.getMessage("CanNotResetTrain"), Bundle.getMessage("ButtonOK"));
        Assert.assertTrue("train build status", train.isBuilt());

        // kill panels
        JUnitUtil.dispose(tef);
    }

    @Test
    public void testOptionalColumns() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JUnitOperationsUtil.loadTrains();
        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);
        Train train0 = tmanager.getTrainByName("Test_Train 0");
        Assert.assertNotNull(train0);

        TrainsTableFrame tef = new TrainsTableFrame();
        JFrameOperator jfo = new JFrameOperator(tef);
        JTableOperator tbl = new JTableOperator(jfo);
        Assert.assertEquals("column not found", -1, tbl.findColumn(Bundle.getMessage("Built")));
        Assert.assertEquals("column not found", -1, tbl.findColumn(Bundle.getMessage("Load")));
        Assert.assertEquals("column not found", -1, tbl.findColumn(Bundle.getMessage("Road")));
        Assert.assertEquals("column not found", -1, tbl.findColumn(Bundle.getMessage("Owner")));

        // show built dates
        train0.setBuiltStartYear("1956");
        train0.setBuiltEndYear("2021");
        Assert.assertEquals("column found", 5, tbl.findColumn(Bundle.getMessage("Built")));

        // show roads
        train0.setRoadOption(Train.INCLUDE_ROADS);
        String[] roads = { "SP", "UP" };
        train0.setRoadNames(roads);
        Assert.assertEquals("column found", 6, tbl.findColumn(Bundle.getMessage("Road")));

        // show load names
        train0.setLoadOption(Train.EXCLUDE_LOADS);
        String[] loads = { "Coal", "Boxes" };
        train0.setLoadNames(loads);
        Assert.assertEquals("column found", 7, tbl.findColumn(Bundle.getMessage("Load")));

        // show owner names
        train0.setOwnerOption(Train.EXCLUDE_OWNERS);
        String[] owners = { "DAB", "AAR" };
        train0.setOwnerNames(owners);
        Assert.assertEquals("column found", 8, tbl.findColumn(Bundle.getMessage("Owner")));

        // kill panels
        JUnitUtil.dispose(tef);
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
        JUnitOperationsUtil.checkOperationsShutDownTask();
    }

    @Test
    public void testBuildButtonFailure() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        JUnitOperationsUtil.loadTrains();
        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);
        
        // increase test coverage
        tmanager.setRowColorsManual(false);

        TrainsTableFrame f = new TrainsTableFrame();
        f.setLocation(10, 20);

        // confirm defaults
        for (Train train : tmanager.getTrainsByNameList()) {
            Assert.assertFalse(train.isBuilt());
            Assert.assertTrue(train.isBuildEnabled());
            Assert.assertFalse(train.isBuildFailed());
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
            Assert.assertTrue(train.isBuildFailed());
        }

        JUnitUtil.dispose(f);
        JUnitOperationsUtil.checkOperationsShutDownTask();
    }

    @Test
    public void testBuildButtonSuccess() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        JUnitOperationsUtil.loadTrains();
        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);
        
        // increase test coverage
        tmanager.setRowColorsManual(false);

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
            Assert.assertFalse(train.isBuildFailed());
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
            Assert.assertFalse(train.isBuildFailed());
        }

        // dialog window asks if user wants to terminate train, answer no
        JemmyUtil.enterClickAndLeaveThreadSafe(ttf.terminateButton);

        for (Train train : tmanager.getTrainsByNameList()) {
            JemmyUtil.pressDialogButton(MessageFormat.format(Bundle.getMessage("TerminateTrain"),
                    new Object[] { train.getName(), train.getDescription() }), Bundle.getMessage("ButtonNo"));
        }
        
        JemmyUtil.waitFor(ttf);

        // confirm no change in status
        for (Train train : tmanager.getTrainsByNameList()) {
            Assert.assertTrue(train.isBuilt());
            Assert.assertTrue(train.isBuildEnabled());
            Assert.assertFalse(train.isBuildFailed());
        }

        // dialog window asks if user wants to terminate train, answer yes
        JemmyUtil.enterClickAndLeaveThreadSafe(ttf.terminateButton);

        for (Train train : tmanager.getTrainsByNameList()) {
            JemmyUtil.pressDialogButton(MessageFormat.format(Bundle.getMessage("TerminateTrain"),
                    new Object[] { train.getName(), train.getDescription() }), Bundle.getMessage("ButtonYes"));
        }
        
        JemmyUtil.waitFor(ttf);

        // confirm all trains terminated
        for (Train train : tmanager.getTrainsByNameList()) {
            Assert.assertFalse(train.isBuilt());
            Assert.assertTrue(train.isBuildEnabled());
            Assert.assertFalse(train.isBuildFailed());
        }

        JUnitUtil.dispose(ttf);
        JUnitOperationsUtil.checkOperationsShutDownTask();
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
            Assert.assertFalse(train.isBuildFailed());
        }

        JemmyUtil.enterClickAndLeave(f.terminateButton);

        JUnitUtil.dispose(f);
        JUnitOperationsUtil.checkOperationsShutDownTask();
    }

    // private final static Logger log =
    // LoggerFactory.getLogger(TrainsTableFrameTest.class);

}
