package jmri.jmrit.operations.locations.gui;

import java.awt.GraphicsEnvironment;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.locations.*;
import jmri.jmrit.operations.rollingstock.cars.CarRoads;
import jmri.jmrit.operations.rollingstock.cars.CarTypes;
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
public class InterchangeEditFrameTest extends OperationsTestCase {

    final static int ALL = Track.EAST + Track.WEST + Track.NORTH + Track.SOUTH;
    private LocationManager lManager = null;
    private Location l = null;

    @Test
    public void testAddInterchange() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        InterchangeEditFrame f = new InterchangeEditFrame();
        f.setTitle("Test Interchange Add Frame");
        f.setLocation(0, 0); // entire panel must be visible for tests to work properly
        f.initComponents(l, null);

        // create one interchange track
        f.trackNameTextField.setText("new interchange track");
        f.trackLengthTextField.setText("321");
        JemmyUtil.enterClickAndLeave(f.addTrackButton);

        Track t = l.getTrackByName("new interchange track", Track.INTERCHANGE);
        Assert.assertNotNull("new interchange track", t);
        Assert.assertEquals("interchange track length", 321, t.getLength());
        // check that the defaults are correct
        Assert.assertEquals("all directions", ALL, t.getTrainDirections());
        Assert.assertEquals("all roads", Track.ALL_ROADS, t.getRoadOption());

        JUnitUtil.dispose(f);
    }

    @Test
    public void testSetDirectionUsingCheckbox() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        InterchangeEditFrame f = new InterchangeEditFrame();
        f.setTitle("Test Interchange Add Frame");
        f.setLocation(0, 0); // entire panel must be visible for tests to work properly
        f.initComponents(l, null);

        // create one interchange tracks
        f.trackNameTextField.setText("2nd interchange track");
        f.trackLengthTextField.setText("4331");
        JemmyUtil.enterClickAndLeave(f.addTrackButton);
        Track t = l.getTrackByName("2nd interchange track", Track.INTERCHANGE);
        Assert.assertNotNull("2nd interchange track", t);
        Assert.assertEquals("2nd interchange track length", 4331, t.getLength());
        Assert.assertEquals("Direction All before change", ALL, t.getTrainDirections());

        // deselect east and south check boxes
        JemmyUtil.enterClickAndLeave(f.eastCheckBox);
        JemmyUtil.enterClickAndLeave(f.southCheckBox);

        JemmyUtil.enterClickAndLeave(f.saveTrackButton);

        Assert.assertEquals("west and north", Track.NORTH + Track.WEST, t.getTrainDirections());

        JUnitUtil.dispose(f);
    }

    @Test
    public void testSetAcceptedCarTypes() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        InterchangeEditFrame f = new InterchangeEditFrame();
        f.setTitle("Test Interchange Add Frame");
        f.setLocation(0, 0); // entire panel must be visible for tests to work properly
        f.initComponents(l, null);

        // create one interchange tracks
        f.trackNameTextField.setText("2nd interchange track");
        f.trackLengthTextField.setText("4331");
        JemmyUtil.enterClickAndLeave(f.addTrackButton);

        Track t = l.getTrackByName("2nd interchange track", Track.INTERCHANGE);

        // check track accepts Boxcars
        Assert.assertTrue("2nd interchange track accepts Boxcars", t.isTypeNameAccepted("Boxcar"));
        // test clear car types button
        JemmyUtil.enterClickAndLeave(f.clearButton);
        JemmyUtil.enterClickAndLeave(f.saveTrackButton);
        Assert.assertFalse("2nd interchange track doesn't accept Boxcars", t.isTypeNameAccepted("Boxcar"));

        JemmyUtil.enterClickAndLeave(f.setButton);
        JemmyUtil.enterClickAndLeave(f.saveTrackButton);
        Assert.assertTrue("2nd interchange track accepts Boxcars again", t.isTypeNameAccepted("Boxcar"));

        JUnitUtil.dispose(f);
    }

    @Test
    public void testAddCloseAndRestore() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        InterchangeEditFrame f = new InterchangeEditFrame();
        f.setTitle("Test Interchange Add Frame");
        f.setLocation(0, 0); // entire panel must be visible for tests to work properly
        f.initComponents(l, null);

        // create two interchange tracks
        f.trackNameTextField.setText("new interchange track");
        f.trackLengthTextField.setText("321");
        JemmyUtil.enterClickAndLeave(f.addTrackButton);

        f.trackNameTextField.setText("2nd interchange track");
        f.trackLengthTextField.setText("4331");
        JemmyUtil.enterClickAndLeave(f.addTrackButton);

        // deselect east and south check boxes
        JemmyUtil.enterClickAndLeave(f.eastCheckBox);
        JemmyUtil.enterClickAndLeave(f.southCheckBox);

        JemmyUtil.enterClickAndLeave(f.saveTrackButton);

        JUnitUtil.dispose(f);

        // now reload
        Location l2 = lManager.getLocationByName("Test Loc C");
        Assert.assertNotNull("Location Test Loc C", l2);

        LocationEditFrame fl = new LocationEditFrame(l2);
        fl.setTitle("Test Edit Location Frame");

        // check location name
        Assert.assertEquals("name", "Test Loc C", fl.locationNameTextField.getText());

        Assert.assertEquals("number of interchanges", 2, fl.interchangeModel.getRowCount());
        Assert.assertEquals("number of staging tracks", 0, fl.stagingModel.getRowCount());

        JUnitUtil.dispose(fl);
    }
    
    @Test
    public void testTrainDropIds() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Track t = l.addTrack("Test Yard Drop Ids", Track.INTERCHANGE);
        InterchangeEditFrame f = new InterchangeEditFrame();
        f.initComponents(l, t);
        
        JemmyUtil.enterClickAndLeave(f.trainDrop);
        JemmyUtil.enterClickAndLeave(f.addDropButton);
        
        Assert.assertEquals("one drop id", 1, t.getDropIds().length);
        Assert.assertEquals("drop id", "1", t.getDropIds()[0]);
        
        JemmyUtil.enterClickAndLeave(f.deleteDropButton);
        Assert.assertEquals("no drop ids", 0, t.getDropIds().length);
        
        Assert.assertEquals("option", Track.TRAINS, t.getDropOption());
        JemmyUtil.enterClickAndLeave(f.anyDrops);
        Assert.assertEquals("option", Track.ANY, t.getDropOption());
        JemmyUtil.enterClickAndLeave(f.excludeTrainDrop);
        Assert.assertEquals("option", Track.EXCLUDE_TRAINS, t.getDropOption());
        
        JUnitUtil.dispose(f);
    }
    
    @Test
    public void testTrainPickupIds() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Track t = l.addTrack("Test Yard Pickup Ids", Track.INTERCHANGE);
        InterchangeEditFrame f = new InterchangeEditFrame();
        f.initComponents(l, t);
        
        JemmyUtil.enterClickAndLeave(f.trainPickup);
        // add does nothing, nothing selected
        JemmyUtil.enterClickAndLeave(f.addPickupButton);
        
        JemmyUtil.enterClickAndLeave(f.addPickupButton);
        Assert.assertEquals("one pickup id", 1, t.getPickupIds().length);
        Assert.assertEquals("pickup id", "1", t.getPickupIds()[0]);
        
        JemmyUtil.enterClickAndLeave(f.deletePickupButton);
        Assert.assertEquals("no pickup ids", 0, t.getPickupIds().length);
        
        Assert.assertEquals("option", Track.TRAINS, t.getPickupOption());
        JemmyUtil.enterClickAndLeave(f.anyPickups);
        Assert.assertEquals("option", Track.ANY, t.getPickupOption());
        JemmyUtil.enterClickAndLeave(f.excludeTrainPickup);
        Assert.assertEquals("option", Track.EXCLUDE_TRAINS, t.getPickupOption());
        
        JUnitUtil.dispose(f);
    }
    
    @Test
    public void testRouteDropIds() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Track t = l.addTrack("Test Yard Drop Ids", Track.INTERCHANGE);
        InterchangeEditFrame f = new InterchangeEditFrame();
        f.initComponents(l, t);
        
        JemmyUtil.enterClickAndLeave(f.routeDrop);
        JemmyUtil.enterClickAndLeave(f.addDropButton);

        Assert.assertEquals("one drop id", 1, t.getDropIds().length);
        Assert.assertEquals("drop id", "1", t.getDropIds()[0]);
        
        JemmyUtil.enterClickAndLeave(f.deleteDropButton);
        Assert.assertEquals("no drop ids", 0, t.getDropIds().length);
        
        Assert.assertEquals("option", Track.ROUTES, t.getDropOption());
        JemmyUtil.enterClickAndLeave(f.anyDrops);
        Assert.assertEquals("option", Track.ANY, t.getDropOption());
        JemmyUtil.enterClickAndLeave(f.excludeRouteDrop);
        Assert.assertEquals("option", Track.EXCLUDE_ROUTES, t.getDropOption());
        
        JUnitUtil.dispose(f);
    }
    
    @Test
    public void testRoutePickupIds() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Track t = l.addTrack("Test Yard Pickup Ids", Track.INTERCHANGE);
        InterchangeEditFrame f = new InterchangeEditFrame();
        f.initComponents(l, t);
        
        JemmyUtil.enterClickAndLeave(f.routePickup);
        JemmyUtil.enterClickAndLeave(f.addPickupButton);
        
        Assert.assertEquals("one pickup id", 1, t.getPickupIds().length);
        Assert.assertEquals("pickup id", "1", t.getPickupIds()[0]);
        
        JemmyUtil.enterClickAndLeave(f.deletePickupButton);
        Assert.assertEquals("no pickup ids", 0, t.getPickupIds().length);
        
        Assert.assertEquals("option", Track.ROUTES, t.getPickupOption());
        JemmyUtil.enterClickAndLeave(f.anyPickups);
        Assert.assertEquals("option", Track.ANY, t.getPickupOption());
        JemmyUtil.enterClickAndLeave(f.excludeRoutePickup);
        Assert.assertEquals("option", Track.EXCLUDE_ROUTES, t.getPickupOption());

        
        JUnitUtil.dispose(f);
    }
    
    @Test
    public void testRoutePickupError() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Track t = l.addTrack("Test Yard Pickup Error", Track.INTERCHANGE);
        InterchangeEditFrame f = new InterchangeEditFrame();
        f.initComponents(l, t);
        
        // confirm default
        Assert.assertTrue(f.autoPickupCheckBox.isSelected());
        JemmyUtil.enterClickAndLeave(f.autoPickupCheckBox);
        
        // create a route not serviced by this location
        Location locB = lManager.getLocationByName("Test Loc B"); 
        RouteManager routeManager = InstanceManager.getDefault(RouteManager.class);
        Route routeB = routeManager.newRoute("Bad Route B");
        routeB.addLocation(locB);
        
        JemmyUtil.enterClickAndLeave(f.routePickup);
        f.comboBoxPickupRoutes.setSelectedItem(routeB);
        // try to add a route not serviced by this track
        JemmyUtil.enterClickAndLeaveThreadSafe(f.addPickupButton);
        JemmyUtil.pressDialogButton(f, Bundle.getMessage("ErrorTitle"), Bundle.getMessage("ButtonOK"));
        
        JUnitUtil.dispose(f);
    }
    
    @Test
    public void testRouteDropError() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Track t = l.addTrack("Test Yard Drop Error", Track.INTERCHANGE);
        InterchangeEditFrame f = new InterchangeEditFrame();
        f.initComponents(l, t);
        
        // confirm default
        Assert.assertTrue(f.autoDropCheckBox.isSelected());
        JemmyUtil.enterClickAndLeave(f.autoDropCheckBox);
        
        // create a route not serviced by this location
        Location locB = lManager.getLocationByName("Test Loc B"); 
        RouteManager routeManager = InstanceManager.getDefault(RouteManager.class);
        Route routeB = routeManager.newRoute("Bad Route B");
        routeB.addLocation(locB);
        
        JemmyUtil.enterClickAndLeave(f.routeDrop);
        f.comboBoxDropRoutes.setSelectedItem(routeB);
        // try to add a route not serviced by this track
        JemmyUtil.enterClickAndLeaveThreadSafe(f.addDropButton);
        JemmyUtil.pressDialogButton(f, Bundle.getMessage("ErrorTitle"), Bundle.getMessage("ButtonOK"));
        
        JUnitUtil.dispose(f);
    }
    
    @Test
    public void testTrainPickupError() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Track t = l.addTrack("Test Yard Pickup Error", Track.INTERCHANGE);
        InterchangeEditFrame f = new InterchangeEditFrame();
        f.initComponents(l, t);
        
        // confirm default
        Assert.assertTrue(f.autoPickupCheckBox.isSelected());
        JemmyUtil.enterClickAndLeave(f.autoPickupCheckBox);
        
        // create a route not serviced by this location
        Location locB = lManager.getLocationByName("Test Loc B"); 
        RouteManager routeManager = InstanceManager.getDefault(RouteManager.class);
        Route routeB = routeManager.newRoute("Bad Route B");
        routeB.addLocation(locB);
        // now the train with this route
        TrainManager trainManager = InstanceManager.getDefault(TrainManager.class);
        Train trainB = trainManager.newTrain("New Test Train B");
        trainB.setRoute(routeB);
        
        JemmyUtil.enterClickAndLeave(f.trainPickup);
        f.comboBoxPickupTrains.setSelectedItem(trainB);
        // try to add a train not serviced by this track
        JemmyUtil.enterClickAndLeaveThreadSafe(f.addPickupButton);
        JemmyUtil.pressDialogButton(f, Bundle.getMessage("ErrorTitle"), Bundle.getMessage("ButtonOK"));
        
        JUnitUtil.dispose(f);
    }
    
    @Test
    public void testTrainDropError() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Track t = l.addTrack("Test Yard Drop Error", Track.INTERCHANGE);
        InterchangeEditFrame f = new InterchangeEditFrame();
        f.initComponents(l, t);
        
        // confirm default
        Assert.assertTrue(f.autoDropCheckBox.isSelected());
        JemmyUtil.enterClickAndLeave(f.autoDropCheckBox);
        
        // create a route not serviced by this location
        Location locB = lManager.getLocationByName("Test Loc B"); 
        RouteManager routeManager = InstanceManager.getDefault(RouteManager.class);
        Route routeB = routeManager.newRoute("Bad Route B");
        routeB.addLocation(locB);
        // now the train with this route
        TrainManager trainManager = InstanceManager.getDefault(TrainManager.class);
        Train trainB = trainManager.newTrain("New Test Train B");
        trainB.setRoute(routeB);
        
        JemmyUtil.enterClickAndLeave(f.trainDrop);
        f.comboBoxDropTrains.setSelectedItem(trainB);
        // try to add a train not serviced by this track
        JemmyUtil.enterClickAndLeaveThreadSafe(f.addDropButton);
        JemmyUtil.pressDialogButton(f, Bundle.getMessage("ErrorTitle"), Bundle.getMessage("ButtonOK"));
        
        JUnitUtil.dispose(f);
    }
    
    @Test
    public void testCloseWindowOnSave() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Track t = l.addTrack("Test Close", Track.INTERCHANGE);
        InterchangeEditFrame f = new InterchangeEditFrame();
        f.initComponents(l, t);
        JUnitOperationsUtil.testCloseWindowOnSave(f.getTitle());
    }



    // Ensure minimal setup for log4J
    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
        CarTypes ct = InstanceManager.getDefault(CarTypes.class);
        ct.addName("Boxcar");

        JUnitOperationsUtil.loadFiveLocations();

        // add UP road name
        CarRoads cr = InstanceManager.getDefault(CarRoads.class);
        cr.addName("UP");

        lManager = InstanceManager.getDefault(LocationManager.class);
        l = lManager.getLocationByName("Test Loc C");
        
        JUnitOperationsUtil.loadTrain(l);
       
    }
}
