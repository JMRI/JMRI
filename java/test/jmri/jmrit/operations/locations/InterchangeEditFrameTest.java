//InterchangeEditFrameTest.java
package jmri.jmrit.operations.locations;

import java.awt.GraphicsEnvironment;
import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsSwingTestCase;
import jmri.jmrit.operations.rollingstock.cars.CarRoads;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the Operations Locations GUI class
 *
 * @author	Dan Boudreau Copyright (C) 2009
 */
public class InterchangeEditFrameTest extends OperationsSwingTestCase {

    final static int ALL = Track.EAST + Track.WEST + Track.NORTH + Track.SOUTH;
    private LocationManager lManager = null;
    private Location l = null;

    @Test
    public void testAddInterchange() {
        if (GraphicsEnvironment.isHeadless()) {
            return; // can't use Assume in TestCase subclasses
        }
        InterchangeEditFrame f = new InterchangeEditFrame();
        f.setTitle("Test Interchange Add Frame");
        f.setLocation(0, 0);	// entire panel must be visible for tests to work properly
        f.initComponents(l, null);

        // create one interchange track
        f.trackNameTextField.setText("new interchange track");
        f.trackLengthTextField.setText("321");
        enterClickAndLeave(f.addTrackButton);

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
        if (GraphicsEnvironment.isHeadless()) {
            return; // can't use Assume in TestCase subclasses
        }
        InterchangeEditFrame f = new InterchangeEditFrame();
        f.setTitle("Test Interchange Add Frame");
        f.setLocation(0, 0);	// entire panel must be visible for tests to work properly
        f.initComponents(l, null);

        // create one interchange tracks
        f.trackNameTextField.setText("2nd interchange track");
        f.trackLengthTextField.setText("4331");
        enterClickAndLeave(f.addTrackButton);
        Track t = l.getTrackByName("2nd interchange track", Track.INTERCHANGE);
        Assert.assertNotNull("2nd interchange track", t);
        Assert.assertEquals("2nd interchange track length", 4331, t.getLength());
        Assert.assertEquals("Direction All before change", ALL, t.getTrainDirections());

        // deselect east and south check boxes
        enterClickAndLeave(f.eastCheckBox);
        enterClickAndLeave(f.southCheckBox);

        enterClickAndLeave(f.saveTrackButton);

        Assert.assertEquals("west and north", Track.NORTH + Track.WEST, t.getTrainDirections());

        JUnitUtil.dispose(f);
    }

    @Test
    public void testSetAcceptedCarTypes() {
        if (GraphicsEnvironment.isHeadless()) {
            return; // can't use Assume in TestCase subclasses
        }
        InterchangeEditFrame f = new InterchangeEditFrame();
        f.setTitle("Test Interchange Add Frame");
        f.setLocation(0, 0);	// entire panel must be visible for tests to work properly
        f.initComponents(l, null);

        // create one interchange tracks
        f.trackNameTextField.setText("2nd interchange track");
        f.trackLengthTextField.setText("4331");
        enterClickAndLeave(f.addTrackButton);

        Track t = l.getTrackByName("2nd interchange track", Track.INTERCHANGE);

        // check track accepts Boxcars
        Assert.assertTrue("2nd interchange track accepts Boxcars", t.acceptsTypeName("Boxcar"));
        // test clear car types button
        enterClickAndLeave(f.clearButton);
        enterClickAndLeave(f.saveTrackButton);
        Assert.assertFalse("2nd interchange track doesn't accept Boxcars", t.acceptsTypeName("Boxcar"));

        enterClickAndLeave(f.setButton);
        enterClickAndLeave(f.saveTrackButton);
        Assert.assertTrue("2nd interchange track accepts Boxcars again", t.acceptsTypeName("Boxcar"));

        JUnitUtil.dispose(f);
    }

    @Test
    public void testAddCloseAndRestore() {
        if (GraphicsEnvironment.isHeadless()) {
            return; // can't use Assume in TestCase subclasses
        }
        InterchangeEditFrame f = new InterchangeEditFrame();
        f.setTitle("Test Interchange Add Frame");
        f.setLocation(0, 0);	// entire panel must be visible for tests to work properly
        f.initComponents(l, null);

        // create two interchange tracks
        f.trackNameTextField.setText("new interchange track");
        f.trackLengthTextField.setText("321");
        enterClickAndLeave(f.addTrackButton);

        f.trackNameTextField.setText("2nd interchange track");
        f.trackLengthTextField.setText("4331");
        enterClickAndLeave(f.addTrackButton);

        // deselect east and south check boxes
        enterClickAndLeave(f.eastCheckBox);
        enterClickAndLeave(f.southCheckBox);

        enterClickAndLeave(f.saveTrackButton);

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

    private void loadLocations() {
        // create 5 locations
        LocationManager lManager = InstanceManager.getDefault(LocationManager.class);
        Location l1 = lManager.newLocation("Test Loc E");
        l1.setLength(1001);
        Location l2 = lManager.newLocation("Test Loc D");
        l2.setLength(1002);
        Location l3 = lManager.newLocation("Test Loc C");
        l3.setLength(1003);
        Location l4 = lManager.newLocation("Test Loc B");
        l4.setLength(1004);
        Location l5 = lManager.newLocation("Test Loc A");
        l5.setLength(1005);

    }

    // Ensure minimal setup for log4J
    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        loadLocations();

        // add UP road name
        CarRoads cr = InstanceManager.getDefault(CarRoads.class);
        cr.addName("UP");

        lManager = InstanceManager.getDefault(LocationManager.class);
        l = lManager.getLocationByName("Test Loc C");
    }

    // The minimal setup for log4J
    @Override
    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }
}
