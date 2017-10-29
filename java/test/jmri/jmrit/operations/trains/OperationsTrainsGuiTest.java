package jmri.jmrit.operations.trains;

import java.awt.GraphicsEnvironment;
import java.util.List;
import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsSwingTestCase;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.jmrit.operations.rollingstock.engines.Engine;
import jmri.jmrit.operations.rollingstock.engines.EngineManager;
import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.routes.RouteManager;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.trains.timetable.TrainsScheduleTableFrame;
import jmri.jmrit.operations.trains.tools.TrainByCarTypeFrame;
import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;
import jmri.util.ThreadingUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Tests for the Operations Trains GUI class
 *
 * @author Dan Boudreau Copyright (C) 2009
 */
public class OperationsTrainsGuiTest extends OperationsSwingTestCase {

//    private final int DIRECTION_ALL = Location.EAST + Location.WEST + Location.NORTH + Location.SOUTH;

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
        JmriJFrame tef = JmriJFrame.getFrame("Add Train");
        Assert.assertNotNull("train edit frame", tef);

        // create the TrainSwichListEditFrame
        enterClickAndLeave(f.switchListsButton);

        // confirm panel creation
        JmriJFrame tsle = JmriJFrame.getFrame("Switch Lists by Location");
        Assert.assertNotNull("train switchlist edit frame", tsle);

        // kill panels
        JUnitUtil.dispose(tef);
        JUnitUtil.dispose(tsle);
        JUnitUtil.dispose(f);
    }

    /**
     * This test relies on OperationsTrainsTest having been run to initialize
     * the train fields.
     */
    @Test
    public void testTrainEditFrame() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        TrainEditFrame trainEditFrame = new TrainEditFrame(null);
        trainEditFrame.setTitle("Test Edit Train Frame");
        ThreadingUtil.runOnGUI(() -> {
            // fill in name and description fields
            trainEditFrame.trainNameTextField.setText("Test Train Name");
            trainEditFrame.trainDescriptionTextField.setText("Test Train Description");
            trainEditFrame.commentTextArea.setText("Test Train Comment");
        });
        enterClickAndLeave(trainEditFrame.addTrainButton);

        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);
        Train t = tmanager.getTrainByName("Test Train Name");

        // test defaults
        Assert.assertEquals("train name", "Test Train Name", t.getName());
        Assert.assertEquals("train description", "Test Train Description", t.getDescription());
        Assert.assertEquals("train comment", "Test Train Comment", t.getComment());
        Assert.assertEquals("train depart time", "00:00", t.getDepartureTime());
        Assert.assertEquals("train route", null, t.getRoute());
        Assert.assertTrue("train accepts car type Boxcar", t.acceptsTypeName("Boxcar"));
        Assert.assertEquals("train roads", Train.ALL_ROADS, t.getRoadOption());
        Assert.assertEquals("train requirements", Train.NO_CABOOSE_OR_FRED, t.getRequirements());

        // test departure time fields
        trainEditFrame.hourBox.setSelectedItem("15");
        trainEditFrame.minuteBox.setSelectedItem("45");
        // shouldn't change until Save
        Assert.assertEquals("train comment", "00:00", t.getDepartureTime());
        enterClickAndLeave(trainEditFrame.saveTrainButton);

        // clear no route dialogue box
        pressDialogButton(trainEditFrame, Bundle.getMessage("TrainNoRoute"), "OK");

        Assert.assertEquals("train comment", "15:45", t.getDepartureTime());

        // test route field, 5 routes and a blank
        Assert.assertEquals("Route Combobox item count", 6, trainEditFrame.routeBox.getItemCount());
        ThreadingUtil.runOnGUI(() -> {
            trainEditFrame.routeBox.setSelectedIndex(3); // the 3rd item should be "Test Route C"
        });
        Assert.assertEquals("train route 2", "Test Route C", t.getRoute().getName());
        // test route edit button
        enterClickAndLeave(trainEditFrame.editButton);

        // confirm panel creation
        JmriJFrame ref = JmriJFrame.getFrame("Edit Route");
        Assert.assertNotNull("route add frame", ref);

        // increase screen size so clear and set buttons are shown
        ThreadingUtil.runOnGUI(() -> {
            trainEditFrame.setLocation(10, 0);
            trainEditFrame.setSize(trainEditFrame.getWidth(), trainEditFrame.getHeight() + 200);
        });

        // test car types using the clear and set buttons
        enterClickAndLeave(trainEditFrame.clearButton);

        Assert.assertFalse("train accepts car type Boxcar", t.acceptsTypeName("Boxcar"));
        enterClickAndLeave(trainEditFrame.setButton);

        Assert.assertTrue("train accepts car type Boxcar", t.acceptsTypeName("Boxcar"));

        // test engine fields
        Assert.assertEquals("number of engines", "0", t.getNumberEngines());
        Assert.assertEquals("engine model", "", t.getEngineModel());
        Assert.assertEquals("engine road", "", t.getEngineRoad());
        // now change them
        trainEditFrame.numEnginesBox.setSelectedItem("3");
        trainEditFrame.modelEngineBox.setSelectedItem("FT");
        trainEditFrame.roadEngineBox.setSelectedItem("UP");
        // shouldn't change until Save
        Assert.assertEquals("number of engines 1", "0", t.getNumberEngines());
        Assert.assertEquals("engine model 1", "", t.getEngineModel());
        Assert.assertEquals("engine road 1", "", t.getEngineRoad());
        enterClickAndLeave(trainEditFrame.saveTrainButton);

        Assert.assertEquals("number of engines 2", "3", t.getNumberEngines());
        Assert.assertEquals("engine model 2", "FT", t.getEngineModel());
        Assert.assertEquals("engine road 2", "UP", t.getEngineRoad());

        // test caboose and FRED buttons and fields
        // require a car with FRED
        enterClickAndLeave(trainEditFrame.fredRadioButton);

        // shouldn't change until Save
        Assert.assertEquals("train requirements 1", Train.NO_CABOOSE_OR_FRED, t.getRequirements());
        enterClickAndLeave(trainEditFrame.saveTrainButton);

        Assert.assertEquals("train requirements 2", Train.FRED, t.getRequirements());
        enterClickAndLeave(trainEditFrame.cabooseRadioButton);

        enterClickAndLeave(trainEditFrame.saveTrainButton);

        Assert.assertEquals("train requirements 3", Train.CABOOSE, t.getRequirements());
        Assert.assertEquals("caboose road 1", "", t.getCabooseRoad());
        // shouldn't change until Save
        trainEditFrame.roadCabooseBox.setSelectedItem("NH");
        Assert.assertEquals("caboose road 2", "", t.getCabooseRoad());
        enterClickAndLeave(trainEditFrame.saveTrainButton);

        Assert.assertEquals("caboose road 3", "NH", t.getCabooseRoad());
        enterClickAndLeave(trainEditFrame.noneRadioButton);

        enterClickAndLeave(trainEditFrame.saveTrainButton);

        Assert.assertEquals("train requirements 4", Train.NO_CABOOSE_OR_FRED, t.getRequirements());

        // test frame size and location
        ThreadingUtil.runOnGUI(() -> {
            trainEditFrame.setSize(650, 600);
            trainEditFrame.setLocation(25, 30);
        });
        enterClickAndLeave(trainEditFrame.saveTrainButton);

        // test delete button
        // the delete opens a dialog window to confirm the delete
        enterClickAndLeave(trainEditFrame.deleteTrainButton);

        // don't delete, we need this train for the next two tests
        // testTrainBuildOptionFrame() and testTrainEditFrameRead()
        pressDialogButton(trainEditFrame, Bundle.getMessage("deleteTrain"), Bundle.getMessage("ButtonNo"));

        ThreadingUtil.runOnGUI(() -> {
            JUnitUtil.dispose(ref);
            JUnitUtil.dispose(trainEditFrame);
        });

        // now reload the window
        Train t2 = tmanager.getTrainByName("Test Train Name");
        Assert.assertNotNull(t);

        // change the train so it doesn't match the add test
        ThreadingUtil.runOnGUI(() -> {
            t2.setRequirements(Train.CABOOSE);
            t2.setCabooseRoad("CP");

        });

        TrainEditFrame f = new TrainEditFrame(t2);
        f.setTitle("Test Edit Train Frame");

        Assert.assertEquals("train name", "Test Train Name", f.trainNameTextField.getText());
        Assert.assertEquals("train description", "Test Train Description", f.trainDescriptionTextField
                .getText());
        Assert.assertEquals("train comment", "Test Train Comment", f.commentTextArea.getText());
        Assert.assertEquals("train depart hour", "15", f.hourBox.getSelectedItem());
        Assert.assertEquals("train depart minute", "45", f.minuteBox.getSelectedItem());
        Assert.assertEquals("train route", t.getRoute(), f.routeBox.getSelectedItem());
        Assert.assertEquals("number of engines", "3", f.numEnginesBox.getSelectedItem());
        Assert.assertEquals("engine model", "FT", f.modelEngineBox.getSelectedItem());
        Assert.assertEquals("engine road", "UP", f.roadEngineBox.getSelectedItem());
        Assert.assertEquals("caboose road", "CP", f.roadCabooseBox.getSelectedItem());
        // check radio buttons
        Assert.assertTrue("caboose selected", f.cabooseRadioButton.isSelected());
        Assert.assertFalse("none selected", f.noneRadioButton.isSelected());
        Assert.assertFalse("FRED selected", f.fredRadioButton.isSelected());

        ThreadingUtil.runOnGUI(() -> {
            JUnitUtil.dispose(f);
        });
    }

    @Test
    public void testTrainEditFrameBuildOptionFrame() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // test build options
        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);
        Train t = tmanager.newTrain("Test Train New Name");

        // Add a route to this train
        Route route = InstanceManager.getDefault(RouteManager.class).newRoute("Test Train Route");
        route.addLocation(InstanceManager.getDefault(LocationManager.class).newLocation("Test Train Location A"));
        route.addLocation(InstanceManager.getDefault(LocationManager.class).newLocation("Test Train Location B"));
        route.addLocation(InstanceManager.getDefault(LocationManager.class).newLocation("Test Train Location C"));
        t.setRoute(route);

        TrainEditFrame trainEditFrame = new TrainEditFrame(t);
        trainEditFrame.setLocation(0, 0); // entire panel must be visible for tests to work properly
        trainEditFrame.setTitle("Test Build Options Train Frame");

        TrainEditBuildOptionsFrame f = new TrainEditBuildOptionsFrame();
        f.setLocation(0, 0); // entire panel must be visible for tests to work properly
        f.initComponents(trainEditFrame);
        f.setTitle("Test Train Build Options");

        // confirm defaults
        Assert.assertEquals("Build normal", false, t.isBuildTrainNormalEnabled());
        Assert.assertEquals("send to terminal", false, t.isSendCarsToTerminalEnabled());
        Assert.assertEquals("return to staging", false, t.isAllowReturnToStagingEnabled());
        Assert.assertEquals("allow local moves", true, t.isAllowLocalMovesEnabled());
        Assert.assertEquals("allow through cars", true, t.isAllowThroughCarsEnabled());

        // test options
        enterClickAndLeave(f.buildNormalCheckBox);
        enterClickAndLeave(f.saveTrainButton);

        Assert.assertEquals("Build normal", true, t.isBuildTrainNormalEnabled());
        Assert.assertEquals("send to terminal", false, t.isSendCarsToTerminalEnabled());
        Assert.assertEquals("return to staging", false, t.isAllowReturnToStagingEnabled());
        Assert.assertEquals("allow local moves", true, t.isAllowLocalMovesEnabled());
        Assert.assertEquals("allow through cars", true, t.isAllowThroughCarsEnabled());

        enterClickAndLeave(f.sendToTerminalCheckBox);
        enterClickAndLeave(f.saveTrainButton);

        Assert.assertEquals("Build normal", true, t.isBuildTrainNormalEnabled());
        Assert.assertEquals("send to terminal", true, t.isSendCarsToTerminalEnabled());
        Assert.assertEquals("return to staging", false, t.isAllowReturnToStagingEnabled());
        Assert.assertEquals("allow local moves", true, t.isAllowLocalMovesEnabled());
        Assert.assertEquals("allow through cars", true, t.isAllowThroughCarsEnabled());

        enterClickAndLeave(f.returnStagingCheckBox);
        enterClickAndLeave(f.saveTrainButton);

        Assert.assertEquals("Build normal", true, t.isBuildTrainNormalEnabled());
        Assert.assertEquals("send to terminal", true, t.isSendCarsToTerminalEnabled());
        // the return to staging checkbox should be disabled
        Assert.assertEquals("return to staging", false, t.isAllowReturnToStagingEnabled());
        Assert.assertEquals("allow local moves", true, t.isAllowLocalMovesEnabled());
        Assert.assertEquals("allow through cars", true, t.isAllowThroughCarsEnabled());

        enterClickAndLeave(f.allowLocalMovesCheckBox);
        enterClickAndLeave(f.saveTrainButton);

        Assert.assertEquals("Build normal", true, t.isBuildTrainNormalEnabled());
        Assert.assertEquals("send to terminal", true, t.isSendCarsToTerminalEnabled());
        Assert.assertEquals("return to staging", false, t.isAllowReturnToStagingEnabled());
        Assert.assertEquals("allow local moves", false, t.isAllowLocalMovesEnabled());
        Assert.assertEquals("allow through cars", true, t.isAllowThroughCarsEnabled());

        enterClickAndLeave(f.allowThroughCarsCheckBox);
        enterClickAndLeave(f.saveTrainButton);

        Assert.assertEquals("Build normal", true, t.isBuildTrainNormalEnabled());
        Assert.assertEquals("send to terminal", true, t.isSendCarsToTerminalEnabled());
        Assert.assertEquals("return to staging", false, t.isAllowReturnToStagingEnabled());
        Assert.assertEquals("allow local moves", false, t.isAllowLocalMovesEnabled());
        Assert.assertEquals("allow through cars", false, t.isAllowThroughCarsEnabled());

        // test car owner options
        enterClickAndLeave(f.ownerNameExclude);

        Assert.assertEquals("train car owner exclude", Train.EXCLUDE_OWNERS, t.getOwnerOption());
        enterClickAndLeave(f.ownerNameInclude);

        Assert.assertEquals("train car owner include", Train.INCLUDE_OWNERS, t.getOwnerOption());
        enterClickAndLeave(f.ownerNameAll);

        Assert.assertEquals("train car owner all", Train.ALL_OWNERS, t.getOwnerOption());

        // test car date options
        enterClickAndLeave(f.builtDateAfter);

        f.builtAfterTextField.setText("1956");
        enterClickAndLeave(f.saveTrainButton);

        Assert.assertEquals("train car built after", "1956", t.getBuiltStartYear());

        enterClickAndLeave(f.builtDateBefore);

        f.builtBeforeTextField.setText("2010");
        enterClickAndLeave(f.saveTrainButton);

        Assert.assertEquals("train car built before", "2010", t.getBuiltEndYear());

        enterClickAndLeave(f.builtDateRange);

        f.builtAfterTextField.setText("1888");
        f.builtBeforeTextField.setText("2000");
        enterClickAndLeave(f.saveTrainButton);

        Assert.assertEquals("train car built after range", "1888", t.getBuiltStartYear());
        Assert.assertEquals("train car built before range", "2000", t.getBuiltEndYear());

        enterClickAndLeave(f.builtDateAll);
        enterClickAndLeave(f.saveTrainButton);

        Assert.assertEquals("train car built after all", "", t.getBuiltStartYear());
        Assert.assertEquals("train car built before all", "", t.getBuiltEndYear());

        // test optional loco and caboose changes
        enterClickAndLeave(f.change1Engine);
        enterClickAndLeave(f.saveTrainButton);

        // clear dialogue box
        pressDialogButton(f, Bundle.getMessage("CanNotSave"), "OK");

        Assert.assertEquals("loco 1 change", Train.CHANGE_ENGINES, t.getSecondLegOptions());
        Assert.assertEquals("loco 1 departure name", "", t.getSecondLegStartLocationName());

        f.routePickup1Box.setSelectedIndex(1); // should be "Test Train Location A"
        f.numEngines1Box.setSelectedIndex(3); // should be 3 locos
        f.modelEngine1Box.setSelectedItem("FT");
        f.roadEngine1Box.setSelectedItem("UP");

        enterClickAndLeave(f.saveTrainButton);

        Assert.assertEquals("loco 1 change", Train.CHANGE_ENGINES, t.getSecondLegOptions());
        Assert.assertEquals("loco 1 departure name", "Test Train Location A", t
                .getSecondLegStartLocationName());
        Assert.assertEquals("loco 1 number of engines", "3", t.getSecondLegNumberEngines());
        Assert.assertEquals("loco 1 model", "FT", t.getSecondLegEngineModel());
        Assert.assertEquals("loco 1 road", "UP", t.getSecondLegEngineRoad());

        enterClickAndLeave(f.modify1Caboose);

        f.routePickup1Box.setSelectedIndex(0);
        f.roadCaboose1Box.setSelectedItem("NH");
        enterClickAndLeave(f.saveTrainButton);

        // clear dialogue box
        pressDialogButton(f, Bundle.getMessage("CanNotSave"), "OK");

        Assert.assertEquals("caboose 1 change", Train.ADD_CABOOSE, t.getSecondLegOptions());

        f.routePickup1Box.setSelectedIndex(2);
        enterClickAndLeave(f.saveTrainButton);

        Assert.assertEquals("caboose 1 road", "NH", t.getSecondLegCabooseRoad());

        enterClickAndLeave(f.helper1Service);

        f.routePickup1Box.setSelectedIndex(0);
        enterClickAndLeave(f.saveTrainButton);

        // clear dialogue box
        pressDialogButton(f, Bundle.getMessage("CanNotSave"), "OK");

        Assert.assertEquals("helper 1 change", Train.HELPER_ENGINES, t.getSecondLegOptions());

        f.routePickup1Box.setSelectedIndex(2); // Should be "Test Train Location B"
        f.routeDrop1Box.setSelectedIndex(3); // Should be "Test Train Location C"
        enterClickAndLeave(f.saveTrainButton);

        Assert.assertEquals("Helper 1 start location name", "Test Train Location B", t
                .getSecondLegStartLocationName());
        Assert.assertEquals("Helper 1 end location name", "Test Train Location C", t
                .getSecondLegEndLocationName());

        enterClickAndLeave(f.none1);
        enterClickAndLeave(f.saveTrainButton);

        Assert.assertEquals("none 1", 0, t.getSecondLegOptions());

        // now do the second set of locos and cabooses
        enterClickAndLeave(f.change2Engine);
        enterClickAndLeave(f.saveTrainButton);

        // clear dialogue box
        pressDialogButton(f, Bundle.getMessage("CanNotSave"), "OK");

        Assert.assertEquals("loco 2 change", Train.CHANGE_ENGINES, t.getThirdLegOptions());
        Assert.assertEquals("loco 2 departure name", "", t.getThirdLegStartLocationName());

        f.routePickup2Box.setSelectedIndex(1); // should be "Test Train Location A"
        f.numEngines2Box.setSelectedIndex(3); // should be 3 locos
        f.modelEngine2Box.setSelectedItem("FT");
        f.roadEngine2Box.setSelectedItem("UP");

        enterClickAndLeave(f.saveTrainButton);

        Assert.assertEquals("loco 2 change", Train.CHANGE_ENGINES, t.getThirdLegOptions());
        Assert.assertEquals("loco 2 departure name", "Test Train Location A", t
                .getThirdLegStartLocationName());
        Assert.assertEquals("loco 2 number of engines", "3", t.getThirdLegNumberEngines());
        Assert.assertEquals("loco 2 model", "FT", t.getThirdLegEngineModel());
        Assert.assertEquals("loco 2 road", "UP", t.getThirdLegEngineRoad());

        enterClickAndLeave(f.modify2Caboose);

        f.routePickup2Box.setSelectedIndex(0);
        f.roadCaboose2Box.setSelectedItem("NH");
        enterClickAndLeave(f.saveTrainButton);

        // clear dialogue box
        pressDialogButton(f, Bundle.getMessage("CanNotSave"), "OK");

        Assert.assertEquals("caboose 2 change", Train.ADD_CABOOSE, t.getThirdLegOptions());

        f.routePickup2Box.setSelectedIndex(2);
        enterClickAndLeave(f.saveTrainButton);

        Assert.assertEquals("caboose 2 road", "NH", t.getThirdLegCabooseRoad());

        enterClickAndLeave(f.helper2Service);

        f.routePickup2Box.setSelectedIndex(0);
        enterClickAndLeave(f.saveTrainButton);

        // clear dialogue box
        pressDialogButton(f, Bundle.getMessage("CanNotSave"), "OK");

        Assert.assertEquals("helper 2 change", Train.HELPER_ENGINES, t.getThirdLegOptions());

        f.routePickup2Box.setSelectedIndex(2); // Should be "Test Train Location B"
        f.routeDrop2Box.setSelectedIndex(3); // Should be "Test Train Location C"
        enterClickAndLeave(f.saveTrainButton);

        Assert.assertEquals("Helper 2 start location name", "Test Train Location B", t
                .getThirdLegStartLocationName());
        Assert.assertEquals("Helper 2 end location name", "Test Train Location C", t
                .getThirdLegEndLocationName());

        enterClickAndLeave(f.none2);
        enterClickAndLeave(f.saveTrainButton);

        Assert.assertEquals("none 2", 0, t.getThirdLegOptions());

        JUnitUtil.dispose(trainEditFrame);
        JUnitUtil.dispose(f);
    }

    @Test
    public void testTrainSwitchListEditFrame() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // check defaults
        Assert.assertTrue("All Trains", Setup.isSwitchListAllTrainsEnabled());
        Assert.assertTrue("Page per Train", Setup.getSwitchListPageFormat().equals(Setup.PAGE_NORMAL));
        Assert.assertTrue("Real Time", Setup.isSwitchListRealTime());

        TrainSwitchListEditFrame f = new TrainSwitchListEditFrame();
        ThreadingUtil.runOnGUI(() -> {
            f.initComponents();
        });

        LocationManager lmanager = InstanceManager.getDefault(LocationManager.class);
        List<Location> locations = lmanager.getLocationsByNameList();

        // default switch list will print all locations
        for (int i = 0; i < locations.size(); i++) {
            Location l = locations.get(i);
            Assert.assertTrue("print switchlist 1", l.isSwitchListEnabled());
        }
        // now clear all locations
        enterClickAndLeave(f.clearButton);

        enterClickAndLeave(f.saveButton);

        for (int i = 0; i < locations.size(); i++) {
            Location l = locations.get(i);
            Assert.assertFalse("print switchlist 2", l.isSwitchListEnabled());
        }
        // now set all locations
        enterClickAndLeave(f.setButton);
        enterClickAndLeave(f.saveButton);

        for (int i = 0; i < locations.size(); i++) {
            Location l = locations.get(i);
            Assert.assertTrue("print switchlist 3", l.isSwitchListEnabled());
        }

        // test the two check box options
        enterClickAndLeave(f.switchListRealTimeCheckBox);
        enterClickAndLeave(f.saveButton);

        Assert.assertTrue("All Trains", Setup.isSwitchListAllTrainsEnabled());
        Assert.assertTrue("Page per Train", Setup.getSwitchListPageFormat().equals(Setup.PAGE_NORMAL));
        Assert.assertFalse("Real Time", Setup.isSwitchListRealTime());

        enterClickAndLeave(f.switchListAllTrainsCheckBox);
        enterClickAndLeave(f.saveButton);

        Assert.assertFalse("All Trains", Setup.isSwitchListAllTrainsEnabled());
        Assert.assertTrue("Page per Train", Setup.getSwitchListPageFormat().equals(Setup.PAGE_NORMAL));
        Assert.assertFalse("Real Time", Setup.isSwitchListRealTime());

        // TODO add test for combo box
        //		enterClickAndLeave(f.switchListPageComboBox);
        //		enterClickAndLeave(f.saveButton);
        //		Assert.assertFalse("All Trains", Setup.isSwitchListAllTrainsEnabled());
        //		Assert.assertTrue("Page per Train", Setup.isSwitchListPagePerTrainEnabled());
        //		Assert.assertFalse("Real Time", Setup.isSwitchListRealTime());
        ThreadingUtil.runOnGUI(() -> {
            JUnitUtil.dispose(f);
        });
    }

    /**
     * Test that delete train works
     */
    @Test
    public void testTrainEditFrameDelete() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);
        Train t = tmanager.getTrainByName("Test_Train 1");
        Assert.assertNotNull(t);

        TrainEditFrame trainEditFrame = new TrainEditFrame(t);
        trainEditFrame.setTitle("Test Delete Train Frame");

        enterClickAndLeave(trainEditFrame.deleteTrainButton);

        // And now press the confirmation button
        pressDialogButton(trainEditFrame, Bundle.getMessage("deleteTrain"), Bundle.getMessage("ButtonYes"));

        t = tmanager.getTrainByName("Test_Train 1");
        Assert.assertNull("train deleted", t);

        // Now add it back
        enterClickAndLeave(trainEditFrame.addTrainButton);

        t = tmanager.getTrainByName("Test_Train 1");
        Assert.assertNotNull("train added", t);

        JUnitUtil.dispose(trainEditFrame);
    }

    @Test
    public void testTrainByCarTypeFrame() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);
        Train train = tmanager.getTrainByName("Test Train Name");
        TrainByCarTypeFrame f = new TrainByCarTypeFrame();
        f.initComponents(train);

        Assert.assertNotNull("frame exists", f);
        JUnitUtil.dispose(f);
    }

    @Test
    public void testTrainsScheduleTableFrame() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        TrainsScheduleTableFrame f = new TrainsScheduleTableFrame();
        ThreadingUtil.runOnGUI(() -> {
            f.setVisible(true);
        });

        Assert.assertNotNull("frame exists", f);
        ThreadingUtil.runOnGUI(() -> {
            JUnitUtil.dispose(f);
        });
    }

    @Test
    @Ignore("commented out in JUnit3")
    public void testTrainTestPanel() {
        // confirm panel creation
        JmriJFrame f = JmriJFrame.getFrame("Train Test Panel");
        Assert.assertNotNull(f);

    }

    // Ensure minimal setup for log4J
    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        loadTrains();
    }

    private void loadTrains() {
        // Add some cars for the various tests in this suite
        CarManager cm = InstanceManager.getDefault(CarManager.class);
        // add caboose to the roster
        Car c = cm.newCar("NH", "687");
        c.setCaboose(true);
        c = cm.newCar("CP", "435");
        c.setCaboose(true);

        // load engines
        EngineManager emanager = InstanceManager.getDefault(EngineManager.class);
        Engine e1 = emanager.newEngine("E", "1");
        e1.setModel("GP40");
        Engine e2 = emanager.newEngine("E", "2");
        e2.setModel("GP40");
        Engine e3 = emanager.newEngine("UP", "3");
        e3.setModel("GP40");
        Engine e4 = emanager.newEngine("UP", "4");
        e4.setModel("FT");

        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);
        // turn off build fail messages
        tmanager.setBuildMessagesEnabled(true);
        // turn off print preview
        tmanager.setPrintPreviewEnabled(false);

        // load 5 trains
        for (int i = 0; i < 5; i++) {
            tmanager.newTrain("Test_Train " + i);
        }

        // load 6 locations
        for (int i = 0; i < 6; i++) {
            InstanceManager.getDefault(LocationManager.class).newLocation("Test_Location " + i);
        }

        // load 5 routes
        InstanceManager.getDefault(RouteManager.class).newRoute("Test Route A");
        InstanceManager.getDefault(RouteManager.class).newRoute("Test Route B");
        InstanceManager.getDefault(RouteManager.class).newRoute("Test Route C");
        InstanceManager.getDefault(RouteManager.class).newRoute("Test Route D");
        InstanceManager.getDefault(RouteManager.class).newRoute("Test Route E");
    }

    @Override
    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }
}
