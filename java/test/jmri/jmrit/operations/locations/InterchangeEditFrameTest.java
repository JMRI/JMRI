package jmri.jmrit.operations.locations;

import java.awt.GraphicsEnvironment;
import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.rollingstock.cars.CarRoads;
import jmri.jmrit.operations.rollingstock.cars.CarTypes;
import jmri.util.JUnitOperationsUtil;
import jmri.util.JUnitUtil;
import jmri.util.swing.JemmyUtil;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the Operations Locations GUI class
 *
 * @author	Dan Boudreau Copyright (C) 2009
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
        f.setLocation(0, 0);	// entire panel must be visible for tests to work properly
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
        f.setLocation(0, 0);	// entire panel must be visible for tests to work properly
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
        f.setLocation(0, 0);	// entire panel must be visible for tests to work properly
        f.initComponents(l, null);

        // create one interchange tracks
        f.trackNameTextField.setText("2nd interchange track");
        f.trackLengthTextField.setText("4331");
        JemmyUtil.enterClickAndLeave(f.addTrackButton);

        Track t = l.getTrackByName("2nd interchange track", Track.INTERCHANGE);

        // check track accepts Boxcars
        Assert.assertTrue("2nd interchange track accepts Boxcars", t.acceptsTypeName("Boxcar"));
        // test clear car types button
        JemmyUtil.enterClickAndLeave(f.clearButton);
        JemmyUtil.enterClickAndLeave(f.saveTrackButton);
        Assert.assertFalse("2nd interchange track doesn't accept Boxcars", t.acceptsTypeName("Boxcar"));

        JemmyUtil.enterClickAndLeave(f.setButton);
        JemmyUtil.enterClickAndLeave(f.saveTrackButton);
        Assert.assertTrue("2nd interchange track accepts Boxcars again", t.acceptsTypeName("Boxcar"));

        JUnitUtil.dispose(f);
    }

    @Test
    public void testAddCloseAndRestore() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        InterchangeEditFrame f = new InterchangeEditFrame();
        f.setTitle("Test Interchange Add Frame");
        f.setLocation(0, 0);	// entire panel must be visible for tests to work properly
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

    // Ensure minimal setup for log4J
    @Override
    @Before
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
