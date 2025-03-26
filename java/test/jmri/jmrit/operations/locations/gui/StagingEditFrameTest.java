package jmri.jmrit.operations.locations.gui;

import java.awt.GraphicsEnvironment;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.*;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.locations.*;
import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.routes.RouteManager;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.util.JUnitOperationsUtil;
import jmri.util.JUnitUtil;
import jmri.util.swing.JemmyUtil;

/**
 * Tests for the Operations Locations GUI class
 *
 * @author Dan Boudreau Copyright (C) 2009
 */
public class StagingEditFrameTest extends OperationsTestCase {

    final static int ALL = Track.EAST + Track.WEST + Track.NORTH + Track.SOUTH;

    LocationManager lManager = null; // set in setUp, dispose in tearDown
    Location l = null;  // set in setUp, dispose in tearDown

    /**
     * Staging tracks needs its own location
     */
    @Test
    public void testAddStagingTrackDefaults() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        StagingEditFrame f = new StagingEditFrame();
        f.setTitle("Test Staging Add Frame");
        f.setLocation(0, 0); // entire panel must be visible for tests to work properly
        f.initComponents(l, null);

        // create one staging tracks
        f.trackNameTextField.setText("new staging track");
        f.trackLengthTextField.setText("34");
        JemmyUtil.enterClickAndLeave(f.addTrackButton);
        Track t = l.getTrackByName("new staging track", null);
        Assert.assertNotNull("new staging track", t);
        Assert.assertEquals("staging track length", 34, t.getLength());

        // check that the defaults are correct
        Assert.assertEquals("all directions", ALL, t.getTrainDirections());
        Assert.assertEquals("all roads", Track.ALL_ROADS, t.getRoadOption());

        // add a second track
        f.trackNameTextField.setText("2nd staging track");
        f.trackLengthTextField.setText("3456");
        JemmyUtil.enterClickAndLeave(f.addTrackButton);

        t = l.getTrackByName("2nd staging track", null);
        Assert.assertNotNull("2nd staging track", t);
        Assert.assertEquals("2nd staging track length", 3456, t.getLength());
        // check that the defaults are correct
        Assert.assertEquals("all directions", ALL, t.getTrainDirections());
        Assert.assertEquals("all roads", Track.ALL_ROADS, t.getRoadOption());

        // add a third track
        f.trackNameTextField.setText("3rd staging track");
        f.trackLengthTextField.setText("1");
        JemmyUtil.enterClickAndLeave(f.addTrackButton);

        JUnitUtil.dispose(f);

        t = l.getTrackByName("3rd staging track", null);
        Assert.assertNotNull("3rd staging track", t);
        Assert.assertEquals("3rd staging track length", 1, t.getLength());
        // check that the defaults are correct
        Assert.assertEquals("all directions", ALL, t.getTrainDirections());
        Assert.assertEquals("all roads", Track.ALL_ROADS, t.getRoadOption());

        JUnitUtil.dispose(f);
    }

    @Test
    public void testSetDirectionUsingChceckbox() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        StagingEditFrame f = new StagingEditFrame();
        f.setTitle("Test Staging Add Frame");
        f.setLocation(0, 0); // entire panel must be visible for tests to work properly
        f.initComponents(l, null);

        f.trackNameTextField.setText("4th staging track");
        f.trackLengthTextField.setText("12");
        JemmyUtil.enterClickAndLeave(f.addTrackButton);

        Track t = l.getTrackByName("4th staging track", null);
        Assert.assertNotNull("4th staging track", t);
        Assert.assertEquals("4th staging track length", 12, t.getLength());
        Assert.assertEquals("Direction All before Change", ALL, t.getTrainDirections());

        // deselect east, west and south check boxes
        JemmyUtil.enterClickAndLeave(f.northCheckBox);
        JemmyUtil.enterClickAndLeave(f.westCheckBox);
        JemmyUtil.enterClickAndLeave(f.southCheckBox);

        JemmyUtil.enterClickAndLeave(f.saveTrackButton);

        Assert.assertEquals("only east", Track.EAST, t.getTrainDirections());

        JUnitUtil.dispose(f);
    }

    @Test
    public void testAddCloseAndReload() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        StagingEditFrame f = new StagingEditFrame();
        f.setTitle("Test Staging Add Frame");
        f.setLocation(0, 0); // entire panel must be visible for tests to work properly
        f.initComponents(l, null);

        // create four staging tracks
        f.trackNameTextField.setText("new staging track");
        f.trackLengthTextField.setText("34");
        JemmyUtil.enterClickAndLeave(f.addTrackButton);

        f.trackNameTextField.setText("2nd staging track");
        f.trackLengthTextField.setText("3456");
        JemmyUtil.enterClickAndLeave(f.addTrackButton);

        f.trackNameTextField.setText("3rd staging track");
        f.trackLengthTextField.setText("1");
        JemmyUtil.enterClickAndLeave(f.addTrackButton);

        f.trackNameTextField.setText("4th staging track");
        f.trackLengthTextField.setText("12");
        JemmyUtil.enterClickAndLeave(f.addTrackButton);

        // deselect east, west and south check boxes
        JemmyUtil.enterClickAndLeave(f.northCheckBox);
        JemmyUtil.enterClickAndLeave(f.westCheckBox);
        JemmyUtil.enterClickAndLeave(f.southCheckBox);

        JemmyUtil.enterClickAndLeave(f.saveTrackButton);

        JUnitUtil.dispose(f);

        Location l2 = lManager.getLocationByName("Test Loc C");
        Assert.assertNotNull("Test Loc C", l2);

        LocationEditFrame fl = new LocationEditFrame(l2);
        fl.setTitle("Test Edit Location Frame Staging");

        // check location name
        Assert.assertEquals("name", "Test Loc C", fl.locationNameTextField.getText());

        Assert.assertEquals("number of spurs", 0, fl.spurModel.getRowCount());
        Assert.assertEquals("number of interchanges", 0, fl.interchangeModel.getRowCount());
        Assert.assertEquals("number of yards", 0, fl.yardModel.getRowCount());
        Assert.assertEquals("number of staging tracks", 4, fl.stagingModel.getRowCount());

        // is the staging only button selected?
        Assert.assertTrue("staging selected", fl.stagingRadioButton.isSelected());

        JUnitUtil.dispose(fl);
    }
    
    @Test
    public void testRouteComboboxes() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        
        Track t = l.addTrack("Test staging track", Track.STAGING);
        t.setLength(44);
        Assert.assertNotNull(t);
        
        StagingEditFrame f = new StagingEditFrame();
        f.setLocation(0, 0); // entire panel must be visible for tests to work properly
        f.initComponents(t);  
        
        JemmyUtil.enterClickAndLeave(f.routeDrop);
        JemmyUtil.enterClickAndLeave(f.routePickup);
        
        Assert.assertEquals("Route combobox updated", 1, f.comboBoxDropRoutes.getItemCount());
        Assert.assertEquals("Route combobox updated", 1, f.comboBoxPickupRoutes.getItemCount());
        
        // create a new route
        RouteManager routeManager = InstanceManager.getDefault(RouteManager.class);
        Route route = routeManager.newRoute("New Route Train A");
        route.addLocation(l);
        
        Location locB = lManager.getLocationByName("Test Loc B");     
        Route routeB = routeManager.newRoute("New Route B");
        routeB.addLocation(locB);
        routeB.addLocation(l);
        
        Route routeC = routeManager.newRoute("New Route C");
        routeC.addLocation(l);
        routeC.addLocation(locB);
       
        // a route not serviced by this location
        Route routeD = routeManager.newRoute("New Route D");
        routeD.addLocation(locB);
        
        // confirm default
        Assert.assertTrue(f.autoDropCheckBox.isSelected());
        Assert.assertTrue(f.autoPickupCheckBox.isSelected());
        
        JemmyUtil.enterClickAndLeave(f.autoDropCheckBox);
        JemmyUtil.enterClickAndLeave(f.autoPickupCheckBox);
        
        // confirm 
        Assert.assertEquals("Route combobox updated", 6, f.comboBoxDropRoutes.getItemCount());
        Assert.assertEquals("Route combobox updated", 6, f.comboBoxPickupRoutes.getItemCount());
        
        JemmyUtil.enterClickAndLeave(f.autoDropCheckBox);
        
        // confirm 
        Assert.assertEquals("Route combobox updated", 3, f.comboBoxDropRoutes.getItemCount());
        Assert.assertEquals("Route combobox updated", 6, f.comboBoxPickupRoutes.getItemCount());

        JemmyUtil.enterClickAndLeave(f.autoPickupCheckBox);
        
        // confirm 
        Assert.assertEquals("Route combobox updated", 3, f.comboBoxDropRoutes.getItemCount());
        Assert.assertEquals("Route combobox updated", 4, f.comboBoxPickupRoutes.getItemCount());
        
        JUnitUtil.dispose(f);
    }
    
    @Test
    public void testTrainComboboxes() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        
        Track t = l.addTrack("Test staging track", Track.STAGING);
        t.setLength(44);
        Assert.assertNotNull(t);
        
        StagingEditFrame f = new StagingEditFrame();
        f.setLocation(0, 0); // entire panel must be visible for tests to work properly
        f.initComponents(t);  
        
        JemmyUtil.enterClickAndLeave(f.trainDrop);
        JemmyUtil.enterClickAndLeave(f.trainPickup);
        
        // "Test Train A" services this staging track.
        Assert.assertEquals("Train combobox updated", 1, f.comboBoxDropTrains.getItemCount());
        Assert.assertEquals("Train combobox updated", 1, f.comboBoxPickupTrains.getItemCount());
        
        // create a new route and train
        RouteManager routeManager = InstanceManager.getDefault(RouteManager.class);
        Route route = routeManager.newRoute("New Route A");
        route.addLocation(l);
        TrainManager trainManager = InstanceManager.getDefault(TrainManager.class);
        Train trainA = trainManager.newTrain("New Test Train A");
        trainA.setRoute(route);
        
        // this train terminates
        Location locB = lManager.getLocationByName("Test Loc B");     
        Route routeB = routeManager.newRoute("New Route B");
        routeB.addLocation(locB);
        routeB.addLocation(l);
        Train trainB = trainManager.newTrain("New Test Train B");
        trainB.setRoute(routeB);
        
        // this train departs
        Route routeC = routeManager.newRoute("New Route C");
        routeC.addLocation(l);
        routeC.addLocation(locB);  
        Train trainC = trainManager.newTrain("New Test Train C");
        trainC.setRoute(routeC);
        
        // a route not serviced by this location
        Route routeD = routeManager.newRoute("New Route D");
        routeD.addLocation(locB);
        Train trainD = trainManager.newTrain("New Test Train D");
        trainD.setRoute(routeD);
        
        // confirm default
        Assert.assertTrue(f.autoDropCheckBox.isSelected());
        Assert.assertTrue(f.autoPickupCheckBox.isSelected());
        
        JemmyUtil.enterClickAndLeave(f.autoDropCheckBox);
        JemmyUtil.enterClickAndLeave(f.autoPickupCheckBox);
        
        // confirm 
        Assert.assertEquals("Train combobox updated", 6, f.comboBoxDropTrains.getItemCount());
        Assert.assertEquals("Train combobox updated", 6, f.comboBoxPickupTrains.getItemCount());
        
        JemmyUtil.enterClickAndLeave(f.autoDropCheckBox);
        
        // confirm 
        Assert.assertEquals("Train combobox updated", 3, f.comboBoxDropTrains.getItemCount());
        Assert.assertEquals("Train combobox updated", 6, f.comboBoxPickupTrains.getItemCount());

        JemmyUtil.enterClickAndLeave(f.autoPickupCheckBox);
        
        // confirm 
        Assert.assertEquals("Train combobox updated", 3, f.comboBoxDropTrains.getItemCount());
        Assert.assertEquals("Train combobox updated", 3, f.comboBoxPickupTrains.getItemCount());
        
        JUnitUtil.dispose(f);
    }
    
    @Test
    public void testCheckboxes() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        
        Track t = l.addTrack("Test staging track", Track.STAGING);
        t.setLength(44);
        Assert.assertNotNull(t);
        
        StagingEditFrame f = new StagingEditFrame();
        f.setLocation(0, 0); // entire panel must be visible for tests to work properly
        f.initComponents(t);  
           
        Assert.assertFalse(t.isBlockCarsEnabled());
        JemmyUtil.enterClickAndLeave(f.blockCarsCheckBox);
        JemmyUtil.enterClickAndLeave(f.saveTrackButton);
        Assert.assertTrue("Car blocking enabled", t.isBlockCarsEnabled());
        
        // check defaults
        Assert.assertFalse(t.isAddCustomLoadsAnySpurEnabled());
        Assert.assertFalse(t.isAddCustomLoadsEnabled());
        Assert.assertFalse(t.isAddCustomLoadsAnyStagingTrackEnabled());
        Assert.assertFalse(t.isLoadEmptyEnabled());
        Assert.assertFalse(t.isLoadSwapEnabled());
        
        JemmyUtil.enterClickAndLeave(f.loadAnyCheckBox);
        JemmyUtil.enterClickAndLeave(f.saveTrackButton);
        
        Assert.assertTrue(t.isAddCustomLoadsAnySpurEnabled());
        Assert.assertFalse(t.isAddCustomLoadsEnabled());
        Assert.assertFalse(t.isAddCustomLoadsAnyStagingTrackEnabled());
        Assert.assertFalse("Car blocking disabled", t.isBlockCarsEnabled());
        
        JemmyUtil.enterClickAndLeave(f.loadCheckBox);
        JemmyUtil.enterClickAndLeave(f.saveTrackButton);
        
        Assert.assertFalse(t.isAddCustomLoadsAnySpurEnabled());
        Assert.assertTrue(t.isAddCustomLoadsEnabled());
        Assert.assertFalse(t.isAddCustomLoadsAnyStagingTrackEnabled());

        JemmyUtil.enterClickAndLeave(f.loadAnyStagingCheckBox);
        JemmyUtil.enterClickAndLeave(f.saveTrackButton);
        
        Assert.assertFalse(t.isAddCustomLoadsAnySpurEnabled());
        Assert.assertTrue(t.isAddCustomLoadsEnabled());
        Assert.assertTrue(t.isAddCustomLoadsAnyStagingTrackEnabled());
        
        // test block cars checkbox enable
        Assert.assertFalse(f.blockCarsCheckBox.isEnabled());
        JemmyUtil.enterClickAndLeave(f.loadAnyStagingCheckBox);
        Assert.assertFalse(f.blockCarsCheckBox.isEnabled());
        JemmyUtil.enterClickAndLeave(f.loadCheckBox);
        Assert.assertTrue(f.blockCarsCheckBox.isEnabled());
        
        JemmyUtil.enterClickAndLeave(f.loadAnyStagingCheckBox);
        Assert.assertFalse(f.blockCarsCheckBox.isEnabled());
        JemmyUtil.enterClickAndLeave(f.loadAnyStagingCheckBox);
        Assert.assertTrue(f.blockCarsCheckBox.isEnabled());
        
        JemmyUtil.enterClickAndLeave(f.loadAnyCheckBox);
        Assert.assertFalse(f.blockCarsCheckBox.isEnabled());
        JemmyUtil.enterClickAndLeave(f.loadAnyCheckBox);
        Assert.assertTrue(f.blockCarsCheckBox.isEnabled());
        
        // test empty load and swap checkboxes
        JemmyUtil.enterClickAndLeave(f.emptyCheckBox);
        JemmyUtil.enterClickAndLeave(f.saveTrackButton);
        
        Assert.assertTrue(t.isLoadEmptyEnabled());
        Assert.assertFalse(t.isLoadSwapEnabled());
       
        JemmyUtil.enterClickAndLeave(f.swapLoadsCheckBox);
        JemmyUtil.enterClickAndLeave(f.saveTrackButton);
        
        Assert.assertFalse(t.isLoadEmptyEnabled());
        Assert.assertTrue(t.isLoadSwapEnabled());
        
        JUnitUtil.dispose(f);
    }
    
    @Test
    public void testBlockCheckbox() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        
        Track t = l.addTrack("Test staging track", Track.STAGING);
        t.setLength(44);
        Assert.assertNotNull(t);
        
        // blocking should be disabled if any of the generate feature is enabled
        t.setAddCustomLoadsEnabled(true);
        t.setBlockCarsEnabled(true);
        
        StagingEditFrame f = new StagingEditFrame();
        f.setLocation(0, 0); // entire panel must be visible for tests to work properly
        f.initComponents(t);  
           
        Assert.assertFalse(f.blockCarsCheckBox.isEnabled());
        Assert.assertFalse(f.blockCarsCheckBox.isSelected());
        
        JUnitUtil.dispose(f);
    }
        
    @Test
    public void testCloseWindowOnSave() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Track t = l.addTrack("Test Close", Track.STAGING);
        StagingEditFrame f = new StagingEditFrame();
        f.initComponents(l, t);
        JUnitOperationsUtil.testCloseWindowOnSave(f.getTitle());
    }

    // Ensure minimal setup for log4J
    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();

        JUnitOperationsUtil.loadFiveLocations();
        
        lManager = InstanceManager.getDefault(LocationManager.class);
        l = lManager.getLocationByName("Test Loc C");
        
        JUnitOperationsUtil.loadTrain(l);

        jmri.jmrit.operations.setup.Setup.setRfidEnabled(false); // turn off the ID Tag Reader field by default.
    }

    @Override
    @AfterEach
    public void tearDown() {
        super.tearDown();

        lManager = null;
        l = null;
    }
}
