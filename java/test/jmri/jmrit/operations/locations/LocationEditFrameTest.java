package jmri.jmrit.operations.locations;

import java.awt.GraphicsEnvironment;
import java.text.MessageFormat;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.Test;
import org.netbeans.jemmy.operators.*;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.locations.schedules.Schedule;
import jmri.jmrit.operations.locations.schedules.ScheduleManager;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.routes.RouteManager;
import jmri.jmrit.operations.setup.Setup;
import jmri.util.*;
import jmri.util.swing.JemmyUtil;

/**
 * Tests for the Operations Locations GUI class
 *
 * @author Dan Boudreau Copyright (C) 2009
 */
public class LocationEditFrameTest extends OperationsTestCase {

    final static int ALL = Track.EAST + Track.WEST + Track.NORTH + Track.SOUTH;

    @Test
    public void testAddTracks() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        LocationEditFrame f = new LocationEditFrame(null);
        f.setTitle("Test Add Location Frame");

        f.locationNameTextField.setText("New Test Location");
        JemmyUtil.enterClickAndLeave(f.addLocationButton);

        LocationManager lManager = InstanceManager.getDefault(LocationManager.class);
        Assert.assertEquals("should be 1 locations", 1, lManager.getLocationsByNameList().size());
        Location newLoc = lManager.getLocationByName("New Test Location");

        Assert.assertNotNull(newLoc);

        // add a spur track
        JemmyUtil.enterClickAndLeave(f.addSpurButton);

        // add an interchange track
        JemmyUtil.enterClickAndLeave(f.interchangeRadioButton);
        JemmyUtil.enterClickAndLeave(f.addInterchangeButton);

        // add a staging track
        JemmyUtil.enterClickAndLeave(f.stagingRadioButton);
        JemmyUtil.enterClickAndLeave(f.addStagingButton);

        // add a yard track
        JemmyUtil.enterClickAndLeave(f.yardRadioButton);
        JemmyUtil.enterClickAndLeave(f.addYardButton);

        // confirm that all four add track windows exist
        JUnitUtil.waitFor(() -> {
            return JmriJFrame.getFrame(Bundle.getMessage("AddSpur")) != null;
        }, "lef not null");
        JmriJFrame tef = JmriJFrame.getFrame(Bundle.getMessage("AddSpur"));
        Assert.assertNotNull(tef);
        tef = JmriJFrame.getFrame(Bundle.getMessage("AddInterchange"));
        Assert.assertNotNull(tef);
        tef = JmriJFrame.getFrame(Bundle.getMessage("AddStaging"));
        Assert.assertNotNull(tef);
        tef = JmriJFrame.getFrame(Bundle.getMessage("AddYard"));
        Assert.assertNotNull(tef);

        JUnitUtil.dispose(f);
        // confirm add windows disposed
        tef = JmriJFrame.getFrame(Bundle.getMessage("AddSpur"));
        Assert.assertNull(tef);
        tef = JmriJFrame.getFrame(Bundle.getMessage("AddInterchange"));
        Assert.assertNull(tef);
        tef = JmriJFrame.getFrame(Bundle.getMessage("AddStaging"));
        Assert.assertNull(tef);
        tef = JmriJFrame.getFrame(Bundle.getMessage("AddYard"));
        Assert.assertNull(tef);
    }

    @Test
    public void testDeleteButton() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        LocationEditFrame f = new LocationEditFrame(null);
        f.setTitle("Test Add Location Frame");

        f.locationNameTextField.setText("New Test Location");
        JemmyUtil.enterClickAndLeave(f.addLocationButton);

        LocationManager lManager = InstanceManager.getDefault(LocationManager.class);
        Assert.assertEquals("should be 1 locations", 1, lManager.getLocationsByNameList().size());
        Location newLoc = lManager.getLocationByName("New Test Location");

        Assert.assertNotNull(newLoc);

        // add a spur track
        JemmyUtil.enterClickAndLeave(f.addSpurButton);

        // add an interchange track
        JemmyUtil.enterClickAndLeave(f.interchangeRadioButton);
        JemmyUtil.enterClickAndLeave(f.addInterchangeButton);

        // add a staging track
        JemmyUtil.enterClickAndLeave(f.stagingRadioButton);
        JemmyUtil.enterClickAndLeave(f.addStagingButton);

        // add a yard track
        JemmyUtil.enterClickAndLeave(f.yardRadioButton);
        JemmyUtil.enterClickAndLeave(f.addYardButton);

        // confirm that all four add track windows exist
        JUnitUtil.waitFor(() -> {
            return JmriJFrame.getFrame(Bundle.getMessage("AddSpur")) != null;
        }, "lef not null");
        JmriJFrame tef = JmriJFrame.getFrame(Bundle.getMessage("AddSpur"));
        Assert.assertNotNull(tef);
        tef = JmriJFrame.getFrame(Bundle.getMessage("AddInterchange"));
        Assert.assertNotNull(tef);
        tef = JmriJFrame.getFrame(Bundle.getMessage("AddStaging"));
        Assert.assertNotNull(tef);
        tef = JmriJFrame.getFrame(Bundle.getMessage("AddYard"));
        Assert.assertNotNull(tef);

        JemmyUtil.enterClickAndLeaveThreadSafe(f.deleteLocationButton);
        // confirm delete dialog window should appear, try no
        JemmyUtil.pressDialogButton(f, Bundle.getMessage("deletelocation?"), Bundle.getMessage("ButtonNo"));
        JemmyUtil.waitFor(f);
        Assert.assertEquals("should be 1 locations", 1, lManager.getLocationsByNameList().size());
        
        JemmyUtil.enterClickAndLeaveThreadSafe(f.deleteLocationButton);
        // confirm delete dialog window should appear, try yes
        JemmyUtil.pressDialogButton(f, Bundle.getMessage("deletelocation?"), Bundle.getMessage("ButtonYes"));
        JemmyUtil.waitFor(f);
        JUnitUtil.waitFor(() -> {
            return JmriJFrame.getFrame(Bundle.getMessage("AddSpur")) == null;
        }, "lef null");

        // confirm add windows disposed
        tef = JmriJFrame.getFrame(Bundle.getMessage("AddSpur"));
        Assert.assertNull(tef);
        tef = JmriJFrame.getFrame(Bundle.getMessage("AddInterchange"));
        Assert.assertNull(tef);
        tef = JmriJFrame.getFrame(Bundle.getMessage("AddStaging"));
        Assert.assertNull(tef);
        tef = JmriJFrame.getFrame(Bundle.getMessage("AddYard"));
        Assert.assertNull(tef);

        JUnitUtil.dispose(f);
    }
    
    @Test
    public void testCarsDeleteButton() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JUnitOperationsUtil.createOneNormalLocation("Test Location");

        LocationManager lManager = InstanceManager.getDefault(LocationManager.class);
        Assert.assertEquals("should be 1 locations", 1, lManager.getLocationsByNameList().size());
        Location loc = lManager.getLocationByName("Test Location");
        Assert.assertNotNull(loc);
        
        Track track = loc.getTrackByName("Test Location Spur 1", null);
        Assert.assertNotNull(track);
        
        Car c1 = JUnitOperationsUtil.createAndPlaceCar("DB", "001", "Boxcar", "40", track, 0);

        LocationEditFrame f = new LocationEditFrame(loc);
        Assert.assertNotNull(f);
        
        JemmyUtil.enterClickAndLeaveThreadSafe(f.deleteLocationButton);
        // confirm delete dialog window should appear, try no
        JemmyUtil.pressDialogButton(f, Bundle.getMessage("deletelocation?"), Bundle.getMessage("ButtonNo"));
        JemmyUtil.waitFor(f);
        Assert.assertEquals("should be 1 locations", 1, lManager.getLocationsByNameList().size());
        Assert.assertEquals("Car's track", track, c1.getTrack());
        Assert.assertEquals("Car's location", loc, c1.getLocation());
        
        JemmyUtil.enterClickAndLeaveThreadSafe(f.deleteLocationButton);
        // confirm delete dialog window should appear, try yes
        JemmyUtil.pressDialogButton(f, Bundle.getMessage("deletelocation?"), Bundle.getMessage("ButtonYes"));
        JemmyUtil.waitFor(f);
        Assert.assertEquals("should be 0 locations", 0, lManager.getLocationsByNameList().size());
        Assert.assertEquals("Car's track", null, c1.getTrack());
        Assert.assertEquals("Car's location", null, c1.getLocation());
        
        JUnitUtil.dispose(f);
    }
    
    @Test
    public void testRouteDeleteButton() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        LocationEditFrame f = new LocationEditFrame(null);
        f.setTitle("Test Delete Location Frame");

        f.locationNameTextField.setText("Test Location");
        JemmyUtil.enterClickAndLeave(f.addLocationButton);

        LocationManager lManager = InstanceManager.getDefault(LocationManager.class);
        Assert.assertEquals("should be 1 locations", 1, lManager.getLocationsByNameList().size());
        Location newLoc = lManager.getLocationByName("Test Location");

        Assert.assertNotNull(newLoc);
        
        // add location to a route
        RouteManager routeManager = InstanceManager.getDefault(RouteManager.class);
        Route route = routeManager.newRoute("Test Route");
        route.addLocation(newLoc);

        JemmyUtil.enterClickAndLeaveThreadSafe(f.deleteLocationButton);
        // Can not delete warning window should appear
        JemmyUtil.pressDialogButton(f, Bundle.getMessage("CanNotDeleteLocation"), Bundle.getMessage("ButtonOK"));
        JemmyUtil.waitFor(f);

        JUnitUtil.dispose(f);
    }

    @Test
    public void testAddDeleteSaveButtons() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JUnitOperationsUtil.loadFiveLocations();

        LocationEditFrame f = new LocationEditFrame(null);
        f.setTitle("Test Add Location Frame");

        f.locationNameTextField.setText("New Test Location");
        JemmyUtil.enterClickAndLeave(f.addLocationButton);

        LocationManager lManager = InstanceManager.getDefault(LocationManager.class);
        Assert.assertEquals("should be 6 locations", 6, lManager.getLocationsByNameList().size());
        Location newLoc = lManager.getLocationByName("New Test Location");

        Assert.assertNotNull(newLoc);

        f.locationNameTextField.setText("Newer Test Location");
        JemmyUtil.enterClickAndLeave(f.saveLocationButton);

        Assert.assertEquals("changed location name", "Newer Test Location", newLoc.getName());

        // test delete button
        JemmyUtil.enterClickAndLeaveThreadSafe(f.deleteLocationButton);
        Assert.assertEquals("should be 6 locations", 6, lManager.getLocationsByNameList().size());
        // confirm delete dialog window should appear
        JemmyUtil.pressDialogButton(f, Bundle.getMessage("deletelocation?"), Bundle.getMessage("ButtonYes"));
        JemmyUtil.waitFor(f);
        // location now deleted
        Assert.assertEquals("should be 5 locations", 5, lManager.getLocationsByNameList().size());

        JUnitUtil.dispose(f);
    }

    @Test
    public void testLocationName() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        LocationEditFrame f = new LocationEditFrame(null);
        f.locationNameTextField.setText("New Test Location");
        JemmyUtil.enterClickAndLeave(f.addLocationButton);

        LocationManager lManager = InstanceManager.getDefault(LocationManager.class);
        Assert.assertEquals("should be 1 locations", 1, lManager.getLocationsByNameList().size());
        Location newLoc = lManager.getLocationByName("New Test Location");
        Assert.assertNotNull(newLoc);

        // test name too long error
        f.locationNameTextField.setText("abcdefghijklmnopqrstuvwxyz");
        JemmyUtil.enterClickAndLeaveThreadSafe(f.saveLocationButton);
        String title =
                MessageFormat.format(Bundle.getMessage("CanNotLocation"), new Object[]{Bundle.getMessage("save")});
        JemmyUtil.pressDialogButton(f, title, Bundle.getMessage("ButtonOK"));

        // test hyphen feature (this creates a new location)
        f.locationNameTextField.setText("abcdefghijkl-mnopqrstuvwxyz");
        JemmyUtil.enterClickAndLeave(f.saveLocationButton);

        // test hyphen error
        f.locationNameTextField.setText("-");
        JemmyUtil.enterClickAndLeaveThreadSafe(f.saveLocationButton);
        JemmyUtil.pressDialogButton(f, title, Bundle.getMessage("ButtonOK"));

        // test blank name error
        f.locationNameTextField.setText("");
        JemmyUtil.enterClickAndLeaveThreadSafe(f.saveLocationButton);
        JemmyUtil.pressDialogButton(f, title, Bundle.getMessage("ButtonOK"));

        // test reserved character error
        f.locationNameTextField.setText("Bad.name");
        JemmyUtil.enterClickAndLeaveThreadSafe(f.saveLocationButton);
        JemmyUtil.pressDialogButton(f, title, Bundle.getMessage("ButtonOK"));

        // test same name error
        lManager.newLocation("Same Name");
        f.locationNameTextField.setText("Same Name");
        JemmyUtil.enterClickAndLeaveThreadSafe(f.saveLocationButton);
        JemmyUtil.pressDialogButton(f, title, Bundle.getMessage("ButtonOK"));

        Assert.assertEquals("should be 2 locations", 2, lManager.getLocationsByNameList().size());

        JUnitUtil.dispose(f);
    }

    @Test
    public void testTrainDirections() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        // improve test coverage
        Setup.setRfidEnabled(true);

        LocationEditFrame f = new LocationEditFrame(null);
        f.locationNameTextField.setText("New Test Location");
        JemmyUtil.enterClickAndLeave(f.addLocationButton);

        LocationManager lManager = InstanceManager.getDefault(LocationManager.class);
        Assert.assertEquals("should be 1 locations", 1, lManager.getLocationsByNameList().size());
        Location newLoc = lManager.getLocationByName("New Test Location");
        Assert.assertNotNull(newLoc);

        Assert.assertEquals("Train directions", Location.EAST + Location.WEST + Location.SOUTH + Location.NORTH,
                newLoc.getTrainDirections());

        JemmyUtil.enterClickAndLeave(f.eastCheckBox);
        Assert.assertEquals("Train directions", Location.WEST + Location.SOUTH + Location.NORTH,
                newLoc.getTrainDirections());

        JemmyUtil.enterClickAndLeave(f.westCheckBox);
        Assert.assertEquals("Train directions", Location.SOUTH + Location.NORTH, newLoc.getTrainDirections());

        JemmyUtil.enterClickAndLeave(f.northCheckBox);
        Assert.assertEquals("Train directions", Location.SOUTH, newLoc.getTrainDirections());

        JemmyUtil.enterClickAndLeave(f.southCheckBox);
        Assert.assertEquals("Train directions", 0, newLoc.getTrainDirections());

        JemmyUtil.enterClickAndLeave(f.eastCheckBox);
        Assert.assertEquals("Train directions", Location.EAST, newLoc.getTrainDirections());

        JUnitUtil.dispose(f);
    }

    @Test
    public void testSelectButtons() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        LocationEditFrame f = new LocationEditFrame(null);
        f.locationNameTextField.setText("New Test Location");
        JemmyUtil.enterClickAndLeave(f.addLocationButton);

        LocationManager lManager = InstanceManager.getDefault(LocationManager.class);
        Assert.assertEquals("should be 1 locations", 1, lManager.getLocationsByNameList().size());
        Location newLoc = lManager.getLocationByName("New Test Location");
        Assert.assertNotNull(newLoc);

        Assert.assertTrue("Location accepts all types", newLoc.acceptsTypeName("Boxcar"));
        JemmyUtil.enterClickAndLeave(f.clearButton);
        Assert.assertFalse("Location doesn't accepts all types", newLoc.acceptsTypeName("Boxcar"));
        JemmyUtil.enterClickAndLeave(f.setButton);
        Assert.assertTrue("Location accepts all types", newLoc.acceptsTypeName("Boxcar"));

        JemmyUtil.enterClickAndLeaveThreadSafe(f.autoSelectButton);
        JemmyUtil.pressDialogButton(f, Bundle.getMessage("autoSelectLocations?"), Bundle.getMessage("ButtonNo"));
        Assert.assertTrue("Location accepts all types", newLoc.acceptsTypeName("Boxcar"));

        JemmyUtil.enterClickAndLeaveThreadSafe(f.autoSelectButton);
        JemmyUtil.pressDialogButton(f, Bundle.getMessage("autoSelectLocations?"), Bundle.getMessage("ButtonYes"));
        Assert.assertFalse("Location doesn't accepts all types", newLoc.acceptsTypeName("Boxcar"));

        JUnitUtil.dispose(f);
    }

    @Test
    public void testTypeCheckbox() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        LocationEditFrame f = new LocationEditFrame(null);
        f.locationNameTextField.setText("New Test Location");
        JemmyUtil.enterClickAndLeave(f.addLocationButton);

        LocationManager lManager = InstanceManager.getDefault(LocationManager.class);
        Assert.assertEquals("should be 1 locations", 1, lManager.getLocationsByNameList().size());
        Location newLoc = lManager.getLocationByName("New Test Location");
        Assert.assertNotNull(newLoc);

        Assert.assertTrue("Location accepts Boxcar", newLoc.acceptsTypeName("Boxcar"));

        JFrameOperator jfo = new JFrameOperator(f);
        JCheckBoxOperator jbo = new JCheckBoxOperator(jfo, "Boxcar");

        jbo.doClick();
        Assert.assertFalse("Location doesn't accepts Boxcar", newLoc.acceptsTypeName("Boxcar"));

        jbo.doClick();
        Assert.assertTrue("Location accepts Boxcar", newLoc.acceptsTypeName("Boxcar"));

        JUnitUtil.dispose(f);
    }

    @Test
    public void testEditStaging() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JUnitOperationsUtil.createFourStagingLocations();

        LocationManager lManager = InstanceManager.getDefault(LocationManager.class);
        Assert.assertEquals("should be 4 locations", 4, lManager.getLocationsByNameList().size());
        Location loc = lManager.getLocationByName("North End Staging");
        Assert.assertNotNull(loc);

        LocationEditFrame f = new LocationEditFrame(loc);
        Assert.assertNotNull(f);

        JFrameOperator jfo = new JFrameOperator(f);
        JTableOperator tbl = new JTableOperator(jfo);
        tbl.clickOnCell(1, tbl.findColumn(Bundle.getMessage("ButtonEdit")));

        // confirm edit staging track window exists
        JUnitUtil.waitFor(() -> {
            return JmriJFrame.getFrame(Bundle.getMessage("EditStaging", loc.getName())) != null;
        }, "esf not null");
        JmriJFrame tef = JmriJFrame.getFrame(Bundle.getMessage("EditStaging", loc.getName()));
        Assert.assertNotNull(tef);

        Track t = loc.getTracksList().get(0);
        Assert.assertNotNull(t);
        t.setLength(350); // change track length to create property change

        TrackEditFrame tefc = (TrackEditFrame) tef;
        JUnitUtil.waitFor(() -> {
            return tefc.trackLengthTextField.getText().equals("350");
        }, "Field updated");
        Assert.assertEquals("Track Length", "350", tefc.trackLengthTextField.getText());

        JUnitUtil.dispose(f);
        tef = JmriJFrame.getFrame(Bundle.getMessage("EditStaging", loc.getName()));
        Assert.assertNull(tef);
    }
    
    @Test
    public void testStagingTable() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JUnitOperationsUtil.createTwoStagingLocations();

        LocationManager lManager = InstanceManager.getDefault(LocationManager.class);
        Location loc = lManager.getLocationByName("North End Staging");
        Assert.assertNotNull(loc);
        
        // turn on id column
        InstanceManager.getDefault(LocationManager.class).setShowIdEnabled(true);

        LocationEditFrame f = new LocationEditFrame(loc);
        Assert.assertNotNull(f);
        f.setSize(1200, f.getHeight()); // need full width

        JFrameOperator jfo = new JFrameOperator(f);
        JTableOperator tbl = new JTableOperator(jfo);
        
        Setup.setCarRoutingViaStagingEnabled(true);
        
        Assert.assertEquals("Confirm number of columns", 14, tbl.getColumnCount());
        Assert.assertEquals("Column doesn't exist", -1, tbl.findColumn(Bundle.getMessage("Moves")));
        Assert.assertEquals("Column doesn't exist", -1, tbl.findColumn(Bundle.getMessage("Hold")));
        Assert.assertEquals("Column doesn't exist", -1, tbl.findColumn(Bundle.getMessage("Schedule")));
        Assert.assertEquals("Column doesn't exist", -1, tbl.findColumn(Bundle.getMessage("Road")));
        Assert.assertEquals("Column doesn't exist", -1, tbl.findColumn(Bundle.getMessage("Load")));
        Assert.assertEquals("Column doesn't exist", -1, tbl.findColumn(Bundle.getMessage("Restrictions")));
        Assert.assertEquals("Column doesn't exist", -1, tbl.findColumn(Bundle.getMessage("Pool")));
        Assert.assertEquals("Column doesn't exist", -1, tbl.findColumn(Bundle.getMessage("ServiceOrder")));
        Assert.assertEquals("Column doesn't exist", -1, tbl.findColumn(Bundle.getMessage("PlanPickUp")));
        Assert.assertEquals("Column doesn't exist", -1, tbl.findColumn(Bundle.getMessage("Dest")));
        Assert.assertEquals("Column doesn't exist", -1, tbl.findColumn(Bundle.getMessage("Ship")));
        Assert.assertEquals("Column doesn't exist", -1, tbl.findColumn(Bundle.getMessage("AbbrevationDirection")));
        
        // confirm comment column is visible
        Assert.assertEquals("Column exists", 12, tbl.findColumn(Bundle.getMessage("Comment")));

        // confirm columns unique to staging are visible
        Assert.assertEquals("Column exists", 9, tbl.findColumn(Bundle.getMessage("LoadDefaultAbv")));
        Assert.assertEquals("Column exists", 10, tbl.findColumn(Bundle.getMessage("LoadCustomAbv")));
               
        // add track moves column
        Setup.setShowTrackMovesEnabled(true);
        Assert.assertEquals("Confirm number of columns", 15, tbl.getColumnCount());
        Assert.assertEquals("Column exists", 5, tbl.findColumn(Bundle.getMessage("Moves")));
        
        // test 2nd row
        Track track = loc.getTrackByName("North End 2", Track.STAGING);
        Assert.assertNotNull(track);
        
        // test change moves value
        tbl.setValueAt(23, 1, tbl.findColumn(Bundle.getMessage("Moves")));
        Assert.assertEquals("moves", 23, track.getMoves());
        
        // test routed
        Assert.assertEquals("Column exists", 12, tbl.findColumn(Bundle.getMessage("Routed")));
        Assert.assertFalse("Routed", track.isOnlyCarsWithFinalDestinationEnabled());
        tbl.clickOnCell(1, tbl.findColumn(Bundle.getMessage("Routed")));
        Assert.assertTrue("Routed", track.isOnlyCarsWithFinalDestinationEnabled());

        // add road restriction
        track.setRoadOption(Track.EXCLUDE_ROADS);
        Assert.assertEquals("Confirm number of columns", 16, tbl.getColumnCount());
        Assert.assertEquals("Column exists", 10, tbl.findColumn(Bundle.getMessage("Road")));

        // add load restriction
        track.setLoadOption(Track.EXCLUDE_LOADS);
        Assert.assertEquals("Confirm number of columns", 17, tbl.getColumnCount());
        Assert.assertEquals("Column exists", 11, tbl.findColumn(Bundle.getMessage("Load")));

        // add train restriction
        track.setPickupOption(Track.EXCLUDE_TRAINS);
        Assert.assertEquals("Confirm number of columns", 18, tbl.getColumnCount());
        Assert.assertEquals("Column exists", 14, tbl.findColumn(Bundle.getMessage("Restrictions")));
        
        // add pool
        Pool pool = loc.addPool("Pool One");
        track.setPool(pool);
        Assert.assertEquals("Confirm number of columns", 19, tbl.getColumnCount());
        Assert.assertEquals("Column exists", 16, tbl.findColumn(Bundle.getMessage("Pool")));
        
        // destination restrictions
        track.setDestinationOption(Track.EXCLUDE_DESTINATIONS);
        Assert.assertEquals("Confirm number of columns", 20, tbl.getColumnCount());
        Assert.assertEquals("Column exists", 15, tbl.findColumn(Bundle.getMessage("Dest")));
        
        // ship load restrictions
        track.setShipLoadOption(Track.INCLUDE_LOADS);
        Assert.assertEquals("Confirm number of columns", 21, tbl.getColumnCount());
        Assert.assertEquals("Column exists", 14, tbl.findColumn(Bundle.getMessage("Ship")));

        // test custom load status field
        Assert.assertEquals("Custom load status", "",
                tbl.getValueAt(1, tbl.findColumn(Bundle.getMessage("LoadCustomAbv"))));

        // blocking option
        track.setBlockCarsEnabled(true);
        Assert.assertEquals("Custom load status", Bundle.getMessage("ABV_CarBlocking"),
                tbl.getValueAt(1, tbl.findColumn(Bundle.getMessage("LoadCustomAbv"))));
        track.setBlockCarsEnabled(false);

        // custom load any spur option
        track.setAddCustomLoadsAnySpurEnabled(true);
        Assert.assertEquals("Custom load status", Bundle.getMessage("ABV_GenerateCustomLoadAnySpur") + " ",
                tbl.getValueAt(1, tbl.findColumn(Bundle.getMessage("LoadCustomAbv"))));

        // staging option
        track.setAddCustomLoadsAnyStagingTrackEnabled(true);
        Assert.assertEquals("Custom load status",
                Bundle.getMessage("ABV_GenerateCustomLoadAnySpur") +
                        " " +
                        Bundle.getMessage("ABV_GereateCustomLoadStaging"),
                tbl.getValueAt(1, tbl.findColumn(Bundle.getMessage("LoadCustomAbv"))));

        // remove custom load option
        track.setRemoveCustomLoadsEnabled(true);
        Assert.assertEquals("Custom load status",
                Bundle.getMessage("ABV_EmptyCustomLoads") +
                        " " +
                Bundle.getMessage("ABV_GenerateCustomLoadAnySpur") +
                        " " +
                        Bundle.getMessage("ABV_GereateCustomLoadStaging"),
                tbl.getValueAt(1, tbl.findColumn(Bundle.getMessage("LoadCustomAbv"))));

        // add custom load option
        track.setAddCustomLoadsEnabled(true);
        Assert.assertEquals("Custom load status",
                Bundle.getMessage("ABV_EmptyCustomLoads") +
                        " " +
                        Bundle.getMessage("ABV_GenerateCustomLoad") +
                        " " +
                        Bundle.getMessage("ABV_GenerateCustomLoadAnySpur") +
                        " " +
                        Bundle.getMessage("ABV_GereateCustomLoadStaging"),
                tbl.getValueAt(1, tbl.findColumn(Bundle.getMessage("LoadCustomAbv"))));

        // test default load status field
        Assert.assertEquals("Default load status", "",
                tbl.getValueAt(1, tbl.findColumn(Bundle.getMessage("LoadDefaultAbv"))));

        // swap default load
        track.setLoadSwapEnabled(true);
        Assert.assertEquals("Default load status", Bundle.getMessage("ABV_SwapDefaultLoads"),
                tbl.getValueAt(1, tbl.findColumn(Bundle.getMessage("LoadDefaultAbv"))));

        // change load to empty
        track.setLoadEmptyEnabled(true);
        Assert.assertEquals("Default load status", Bundle.getMessage("ABV_EmptyDefaultLoads"),
                tbl.getValueAt(1, tbl.findColumn(Bundle.getMessage("LoadDefaultAbv"))));

        JUnitUtil.dispose(f);
    }

    @Test
    public void testEditSpur() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JUnitOperationsUtil.createOneNormalLocation("Test Location");

        LocationManager lManager = InstanceManager.getDefault(LocationManager.class);
        Assert.assertEquals("should be 1 locations", 1, lManager.getLocationsByNameList().size());
        Location loc = lManager.getLocationByName("Test Location");
        Assert.assertNotNull(loc);

        LocationEditFrame f = new LocationEditFrame(loc);
        Assert.assertNotNull(f);

        JFrameOperator jfo = new JFrameOperator(f);
        JTableOperator tbl = new JTableOperator(jfo);
        tbl.clickOnCell(1, tbl.findColumn(Bundle.getMessage("ButtonEdit")));

        // confirm edit spur track window exists
        JUnitUtil.waitFor(() -> {
            return JmriJFrame.getFrame(Bundle.getMessage("EditSpur", loc.getName())) != null;
        }, "esf not null");
        JmriJFrame tef = JmriJFrame.getFrame(Bundle.getMessage("EditSpur", loc.getName()));
        Assert.assertNotNull(tef);

        Track t = loc.getTrackByName("Test Location Spur 2", null);
        Assert.assertNotNull(t);
        t.setLength(222); // change track length to create property change

        TrackEditFrame tefc = (TrackEditFrame) tef;
        JUnitUtil.waitFor(() -> {
            return tefc.trackLengthTextField.getText().equals("222");
        }, "Field updated");
        Assert.assertEquals("Track Length", "222", tefc.trackLengthTextField.getText());

        JUnitUtil.dispose(f);
        tef = JmriJFrame.getFrame(Bundle.getMessage("EditSpur", loc.getName()));
        Assert.assertNull(tef);
    }

    @Test
    public void testSpurTable() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Location loc = JUnitOperationsUtil.createOneNormalLocation("Test Location");
        Assert.assertNotNull(loc);
        
        // turn on id column
        InstanceManager.getDefault(LocationManager.class).setShowIdEnabled(true);
        
        // add a 3rd track for coverage
        Track track3 = loc.addTrack("Test Location Spur 3", Track.SPUR);

        LocationEditFrame f = new LocationEditFrame(loc);
        Assert.assertNotNull(f);
        f.setSize(1500, f.getHeight()); // need full width

        JFrameOperator jfo = new JFrameOperator(f);
        JTableOperator tbl = new JTableOperator(jfo);

        Assert.assertEquals("Confirm number of columns", 10, tbl.getColumnCount());
        Assert.assertEquals("Confirm number of rows", 3, tbl.getRowCount());
        Assert.assertEquals("Column doesn't exist", -1, tbl.findColumn(Bundle.getMessage("Moves")));
        Assert.assertEquals("Column doesn't exist", -1, tbl.findColumn(Bundle.getMessage("Hold")));
        Assert.assertEquals("Column doesn't exist", -1, tbl.findColumn(Bundle.getMessage("Schedule")));
        Assert.assertEquals("Column doesn't exist", -1, tbl.findColumn(Bundle.getMessage("Road")));
        Assert.assertEquals("Column doesn't exist", -1, tbl.findColumn(Bundle.getMessage("Load")));
        Assert.assertEquals("Column doesn't exist", -1, tbl.findColumn(Bundle.getMessage("Restrictions")));
        Assert.assertEquals("Column doesn't exist", -1, tbl.findColumn(Bundle.getMessage("Pool")));
        Assert.assertEquals("Column doesn't exist", -1, tbl.findColumn(Bundle.getMessage("ServiceOrder")));
        Assert.assertEquals("Column doesn't exist", -1, tbl.findColumn(Bundle.getMessage("PlanPickUp")));
        Assert.assertEquals("Column doesn't exist", -1, tbl.findColumn(Bundle.getMessage("Dest")));
        Assert.assertEquals("Column doesn't exist", -1, tbl.findColumn(Bundle.getMessage("Routed")));
        Assert.assertEquals("Column doesn't exist", -1, tbl.findColumn(Bundle.getMessage("AbbrevationDirection")));
        Assert.assertEquals("Column doesn't exist", -1, tbl.findColumn(Bundle.getMessage("AlternateTrack")));
        
        // add track moves column
        Setup.setShowTrackMovesEnabled(true);
        Assert.assertEquals("Confirm number of columns", 11, tbl.getColumnCount());
        Assert.assertEquals("Column exists", 5, tbl.findColumn(Bundle.getMessage("Moves")));
        
        // add schedule column
        Track track = loc.getTrackByName("Test Location Spur 1", Track.SPUR);
        Assert.assertNotNull(track);

        ScheduleManager sm = InstanceManager.getDefault(ScheduleManager.class);
        Schedule s = sm.newSchedule("Test Schedule");
        track.setSchedule(s);

        Assert.assertEquals("Confirm number of columns", 12, tbl.getColumnCount());
        Assert.assertEquals("Column doesn't exists", -1, tbl.findColumn(Bundle.getMessage("Hold")));
        Assert.assertEquals("Column exists", 10, tbl.findColumn(Bundle.getMessage("Schedule")));

        // adding a schedule and an alternate adds the hold column
        Track altTrack = loc.getTrackByName("Test Location Yard 1", Track.YARD);
        Assert.assertNotNull(altTrack);
        track.setAlternateTrack(altTrack);

        Assert.assertEquals("Confirm number of columns", 14, tbl.getColumnCount());
        Assert.assertEquals("Column exists", 11, tbl.findColumn(Bundle.getMessage("Hold")));
        Assert.assertEquals("Column exists", 12, tbl.findColumn(Bundle.getMessage("AlternateTrack")));
        
        // test change moves value
        tbl.setValueAt(5, 0, tbl.findColumn(Bundle.getMessage("Moves")));
        Assert.assertEquals("moves", 5, track.getMoves());
        
        // test hold checkbox
        Assert.assertFalse("Hold", track.isHoldCarsWithCustomLoadsEnabled());
        tbl.clickOnCell(0, tbl.findColumn(Bundle.getMessage("Hold")));
        Assert.assertTrue("Hold", track.isHoldCarsWithCustomLoadsEnabled());

        // add road restriction
        track.setRoadOption(Track.EXCLUDE_ROADS);
        Assert.assertEquals("Confirm number of columns", 15, tbl.getColumnCount());
        // hold moves to column 12
        Assert.assertEquals("Column exists", 11, tbl.findColumn(Bundle.getMessage("Road")));

        // add load restriction
        track.setLoadOption(Track.EXCLUDE_LOADS);
        Assert.assertEquals("Confirm number of columns", 16, tbl.getColumnCount());
        // hold moves to column 13
        Assert.assertEquals("Column exists", 12, tbl.findColumn(Bundle.getMessage("Load")));

        // add train restriction
        track.setDropOption(Track.EXCLUDE_TRAINS);
        Assert.assertEquals("Confirm number of columns", 17, tbl.getColumnCount());
        Assert.assertEquals("Column exists", 13, tbl.findColumn(Bundle.getMessage("Restrictions")));
        
        // add pool
        Pool pool = loc.addPool("Pool One");
        track.setPool(pool);
        Assert.assertEquals("Confirm number of columns", 18, tbl.getColumnCount());
        Assert.assertEquals("Column exists", 15, tbl.findColumn(Bundle.getMessage("Pool")));
        
        // test load change
        track.setDisableLoadChangeEnabled(true);
        Assert.assertEquals("Confirm number of columns", 19, tbl.getColumnCount());
        Assert.assertEquals("Column exists", 13, tbl.findColumn(Bundle.getMessage("DisableLoadChange")));
        tbl.clickOnCell(0, tbl.findColumn(Bundle.getMessage("DisableLoadChange")));
        Assert.assertFalse("Load change", track.isDisableLoadChangeEnabled());

        // column should disappear
        Assert.assertEquals("Confirm number of columns", 18, tbl.getColumnCount());
        Assert.assertEquals("Column exists", -1, tbl.findColumn(Bundle.getMessage("DisableLoadChange")));

        // test track directions
        track.setTrainDirections(Track.EAST);
        Assert.assertEquals("Confirm number of columns", 19, tbl.getColumnCount());
        Assert.assertEquals("Column exists", 17, tbl.findColumn(Bundle.getMessage("AbbrevationDirection")));

        // no schedule, no hold column
        track.setSchedule(null);
        Assert.assertEquals("Confirm number of columns", 17, tbl.getColumnCount());
        Assert.assertEquals("Column doesn't exists", -1, tbl.findColumn(Bundle.getMessage("Hold")));
        Assert.assertEquals("Column doesn't exists", -1, tbl.findColumn(Bundle.getMessage("Schedule")));

        // test track length
        Assert.assertEquals("zero length", 0, tbl.getValueAt(2, tbl.findColumn(Bundle.getMessage("Length"))));
        track3.setLength(124);
        Assert.assertEquals("new length", 124, tbl.getValueAt(2, tbl.findColumn(Bundle.getMessage("Length"))));
        
        // test adding a new track
        loc.addTrack("Test Location Spur 4", Track.SPUR);
        Assert.assertEquals("Confirm number of rows", 4, tbl.getRowCount());
        
        JUnitUtil.dispose(f);
    }

    @Test
    public void testEditInterchange() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JUnitOperationsUtil.createOneNormalLocation("Test Location");

        LocationManager lManager = InstanceManager.getDefault(LocationManager.class);
        Assert.assertEquals("should be 1 locations", 1, lManager.getLocationsByNameList().size());
        Location loc = lManager.getLocationByName("Test Location");
        Assert.assertNotNull(loc);

        LocationEditFrame f = new LocationEditFrame(loc);
        Assert.assertNotNull(f);

        JemmyUtil.enterClickAndLeave(f.interchangeRadioButton);

        JFrameOperator jfo = new JFrameOperator(f);
        JTableOperator tbl = new JTableOperator(jfo);
        tbl.clickOnCell(1, tbl.findColumn(Bundle.getMessage("ButtonEdit")));

        // confirm edit interchange track window exists
        JUnitUtil.waitFor(() -> {
            return JmriJFrame.getFrame(Bundle.getMessage("EditInterchange", loc.getName())) != null;
        }, "esf not null");
        JmriJFrame tef = JmriJFrame.getFrame(Bundle.getMessage("EditInterchange", loc.getName()));
        Assert.assertNotNull(tef);

        Track t = loc.getTrackByName("Test Location Interchange 2", null);
        Assert.assertNotNull(t);
        t.setLength(222); // change track length to create property change

        TrackEditFrame tefc = (TrackEditFrame) tef;
        JUnitUtil.waitFor(() -> {
            return tefc.trackLengthTextField.getText().equals("222");
        }, "Field updated");
        Assert.assertEquals("Track Length", "222", tefc.trackLengthTextField.getText());

        JUnitUtil.dispose(f);
        tef = JmriJFrame.getFrame(Bundle.getMessage("EditInterchange", loc.getName()));
        Assert.assertNull(tef);
    }
    
    @Test
    public void testInterchangeTable() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Location loc = JUnitOperationsUtil.createOneNormalLocation("Test Location");
        Assert.assertNotNull(loc);
        
        // turn on id column
        InstanceManager.getDefault(LocationManager.class).setShowIdEnabled(true);

        LocationEditFrame f = new LocationEditFrame(loc);
        Assert.assertNotNull(f);
        JemmyUtil.enterClickAndLeave(f.interchangeRadioButton);
        f.setSize(1200, f.getHeight()); // need full width

        JFrameOperator jfo = new JFrameOperator(f);
        JTableOperator tbl = new JTableOperator(jfo);

        Assert.assertEquals("Confirm number of columns", 11, tbl.getColumnCount());
        Assert.assertEquals("Column doesn't exist", -1, tbl.findColumn(Bundle.getMessage("Moves")));
        Assert.assertEquals("Column doesn't exist", -1, tbl.findColumn(Bundle.getMessage("Hold")));
        Assert.assertEquals("Column doesn't exist", -1, tbl.findColumn(Bundle.getMessage("Schedule")));
        Assert.assertEquals("Column doesn't exist", -1, tbl.findColumn(Bundle.getMessage("Road")));
        Assert.assertEquals("Column doesn't exist", -1, tbl.findColumn(Bundle.getMessage("Load")));
        Assert.assertEquals("Column doesn't exist", -1, tbl.findColumn(Bundle.getMessage("Restrictions")));
        Assert.assertEquals("Column doesn't exist", -1, tbl.findColumn(Bundle.getMessage("Pool")));
        Assert.assertEquals("Column doesn't exist", -1, tbl.findColumn(Bundle.getMessage("ServiceOrder")));
        Assert.assertEquals("Column doesn't exist", -1, tbl.findColumn(Bundle.getMessage("PlanPickUp")));
        Assert.assertEquals("Column doesn't exist", -1, tbl.findColumn(Bundle.getMessage("Dest")));
        Assert.assertEquals("Column doesn't exist", -1, tbl.findColumn(Bundle.getMessage("AbbrevationDirection")));
               
        // add track moves column
        Setup.setShowTrackMovesEnabled(true);
        Assert.assertEquals("Confirm number of columns", 12, tbl.getColumnCount());
        Assert.assertEquals("Column exists", 5, tbl.findColumn(Bundle.getMessage("Moves")));
        
        // test 2nd row
        Track track = loc.getTrackByName("Test Location Interchange 2", Track.INTERCHANGE);
        Assert.assertNotNull(track);
        
        // test change moves value
        tbl.setValueAt(6, 1, tbl.findColumn(Bundle.getMessage("Moves")));
        Assert.assertEquals("moves", 6, track.getMoves());
        
        // test routed
        Assert.assertEquals("Column exists", 10, tbl.findColumn(Bundle.getMessage("Routed")));
        Assert.assertFalse("Routed", track.isOnlyCarsWithFinalDestinationEnabled());
        tbl.clickOnCell(1, tbl.findColumn(Bundle.getMessage("Routed")));
        Assert.assertTrue("Routed", track.isOnlyCarsWithFinalDestinationEnabled());

        // add road restriction
        track.setRoadOption(Track.EXCLUDE_ROADS);
        Assert.assertEquals("Confirm number of columns", 13, tbl.getColumnCount());
        Assert.assertEquals("Column exists", 10, tbl.findColumn(Bundle.getMessage("Road")));

        // add load restriction
        track.setLoadOption(Track.EXCLUDE_LOADS);
        Assert.assertEquals("Confirm number of columns", 14, tbl.getColumnCount());
        Assert.assertEquals("Column exists", 11, tbl.findColumn(Bundle.getMessage("Load")));

        // add train restriction
        track.setPickupOption(Track.EXCLUDE_TRAINS);
        Assert.assertEquals("Confirm number of columns", 15, tbl.getColumnCount());
        Assert.assertEquals("Column exists", 12, tbl.findColumn(Bundle.getMessage("Restrictions")));
        
        // add pool
        Pool pool = loc.addPool("Pool One");
        track.setPool(pool);
        Assert.assertEquals("Confirm number of columns", 16, tbl.getColumnCount());
        Assert.assertEquals("Column exists", 14, tbl.findColumn(Bundle.getMessage("Pool")));
        
        // add service order
        track.setServiceOrder(Track.FIFO);
        Assert.assertEquals("Confirm number of columns", 17, tbl.getColumnCount());
        Assert.assertEquals("Column exists", 15, tbl.findColumn(Bundle.getMessage("ServiceOrder")));

        // add planned pick ups
        track.setIgnoreUsedLengthPercentage(50);
        Assert.assertEquals("Confirm number of columns", 18, tbl.getColumnCount());
        Assert.assertEquals("Column exists", 15, tbl.findColumn(Bundle.getMessage("PlanPickUp")));
        
        // destination restrictions
        track.setDestinationOption(Track.EXCLUDE_DESTINATIONS);
        Assert.assertEquals("Confirm number of columns", 19, tbl.getColumnCount());
        Assert.assertEquals("Column exists", 13, tbl.findColumn(Bundle.getMessage("Dest")));

        JUnitUtil.dispose(f);
    }

    @Test
    public void testEditYard() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JUnitOperationsUtil.createOneNormalLocation("Test Location");

        LocationManager lManager = InstanceManager.getDefault(LocationManager.class);
        Assert.assertEquals("should be 1 locations", 1, lManager.getLocationsByNameList().size());
        Location loc = lManager.getLocationByName("Test Location");
        Assert.assertNotNull(loc);

        LocationEditFrame f = new LocationEditFrame(loc);
        Assert.assertNotNull(f);

        JemmyUtil.enterClickAndLeave(f.yardRadioButton);

        JFrameOperator jfo = new JFrameOperator(f);
        JTableOperator tbl = new JTableOperator(jfo);
        tbl.clickOnCell(1, tbl.findColumn(Bundle.getMessage("ButtonEdit")));

        // confirm edit interchange track window exists
        JUnitUtil.waitFor(() -> {
            return JmriJFrame.getFrame(Bundle.getMessage("EditYard", loc.getName())) != null;
        }, "esf not null");
        JmriJFrame tef = JmriJFrame.getFrame(Bundle.getMessage("EditYard", loc.getName()));
        Assert.assertNotNull(tef);

        Track t = loc.getTrackByName("Test Location Yard 2", null);
        Assert.assertNotNull(t);
        t.setLength(222); // change track length to create property change

        TrackEditFrame tefc = (TrackEditFrame) tef;
        JUnitUtil.waitFor(() -> {
            return tefc.trackLengthTextField.getText().equals("222");
        }, "Field updated");
        Assert.assertEquals("Track Length", "222", tefc.trackLengthTextField.getText());

        JUnitUtil.dispose(f);
        tef = JmriJFrame.getFrame(Bundle.getMessage("EditYard", loc.getName()));
        Assert.assertNull(tef);
    }
    
    @Test
    public void testCommentbuttons() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JUnitOperationsUtil.createOneNormalLocation("Test Location");

        LocationManager lManager = InstanceManager.getDefault(LocationManager.class);
        Assert.assertEquals("should be 1 locations", 1, lManager.getLocationsByNameList().size());
        Location loc = lManager.getLocationByName("Test Location");
        Assert.assertNotNull(loc);

        Track test = loc.addTrack("Test Spur", Track.SPUR);

        LocationEditFrame f = new LocationEditFrame(loc);
        Assert.assertNotNull(f);
        f.setSize(1200, f.getHeight()); // need full width

        JFrameOperator jfo = new JFrameOperator(f);
        JTableOperator tbl = new JTableOperator(jfo);

        Assert.assertEquals("Confirm number of columns", 9, tbl.getColumnCount());
        Assert.assertEquals("Confirm number of rows", 3, tbl.getRowCount());

        // add manifest comment, comment column should appear
        test.setCommentBoth("Test both comment");
        Assert.assertEquals("Confirm number of columns", 10, tbl.getColumnCount());
        test.setCommentBoth("");
        Assert.assertEquals("Confirm number of columns", 9, tbl.getColumnCount());

        test.setCommentPickup("Test pick up comment");
        Assert.assertEquals("Confirm number of columns", 10, tbl.getColumnCount());
        test.setCommentPickup("");
        Assert.assertEquals("Confirm number of columns", 9, tbl.getColumnCount());

        test.setCommentSetout("Test set out comment");
        Assert.assertEquals("Confirm number of columns", 10, tbl.getColumnCount());

        // test add comment button
        tbl.clickOnCell(1, tbl.findColumn(Bundle.getMessage("Comment")));

        // confirm edit track manifest comments window exists
        JUnitUtil.waitFor(() -> {
            return JmriJFrame.getFrame("Test Location Spur 2") != null;
        }, "esf not null");
        JmriJFrame tef = JmriJFrame.getFrame("Test Location Spur 2");
        Assert.assertNotNull(tef);

        test.setCommentSetout("");
        Assert.assertEquals("Confirm number of columns", 9, tbl.getColumnCount());

        JUnitUtil.dispose(tef);
        JUnitUtil.dispose(f);
    }

    @Test
    public void testCloseWindowOnSave() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Location loc = JUnitOperationsUtil.createOneNormalLocation("Test Location");
        LocationEditFrame f = new LocationEditFrame(loc);
        JUnitOperationsUtil.testCloseWindowOnSave(f.getTitle());
    }
}
