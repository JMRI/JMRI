//YardEditFrameTest.java
package jmri.jmrit.operations.locations;

import java.awt.GraphicsEnvironment;
import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsSwingTestCase;
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
public class YardEditFrameTest extends OperationsSwingTestCase {

    final static int ALL = Track.EAST + Track.WEST + Track.NORTH + Track.SOUTH;
    private LocationManager lManager = InstanceManager.getDefault(LocationManager.class);
    private Location l = lManager.getLocationByName("Test Loc C");

    @Test
    public void testCreateYardTrackDefault() {
        if (GraphicsEnvironment.isHeadless()) {
            return; // can't use Assume in TestCase subclasses
        }
        YardEditFrame f = new YardEditFrame();
        f.setTitle("Test Yard Add Frame");
        f.initComponents(l, null);

        // create a yard track with length 43.
        f.trackNameTextField.setText("new yard track");
        f.trackLengthTextField.setText("43");
        enterClickAndLeave(f.addTrackButton);

        Track t = l.getTrackByName("new yard track", null);
        Assert.assertNotNull("new yard track", t);
        Assert.assertEquals("yard track length", 43, t.getLength());
        // check that the defaults are correct
        Assert.assertEquals("all directions", ALL, t.getTrainDirections());
        Assert.assertEquals("all roads", Track.ALL_ROADS, t.getRoadOption());

        // add a second track with length 6543.
        f.trackNameTextField.setText("2nd yard track");
        f.trackLengthTextField.setText("6543");
        enterClickAndLeave(f.addTrackButton);

        t = l.getTrackByName("2nd yard track", null);
        Assert.assertNotNull("2nd yard track", t);
        Assert.assertEquals("2nd yard track length", 6543, t.getLength());
        // check that the defaults are correct
        Assert.assertEquals("all directions", ALL, t.getTrainDirections());
        Assert.assertEquals("all roads", Track.ALL_ROADS, t.getRoadOption());

        // add A third track with length 1.
        f.trackNameTextField.setText("3rd yard track");
        f.trackLengthTextField.setText("1");
        enterClickAndLeave(f.addTrackButton);

        t = l.getTrackByName("3rd yard track", null);
        Assert.assertNotNull("3rd yard track", t);
        Assert.assertEquals("3rd yard track length", 1, t.getLength());
        // check that the defaults are correct
        Assert.assertEquals("all directions", ALL, t.getTrainDirections());
        Assert.assertEquals("all roads", Track.ALL_ROADS, t.getRoadOption());

        // clean up the frame
        f.setVisible(false);
        JUnitUtil.dispose(f);
    }

    @Test
    public void testSetDirectionUsingCheckbox() {
        if (GraphicsEnvironment.isHeadless()) {
            return; // can't use Assume in TestCase subclasses
        }
        YardEditFrame f = new YardEditFrame();
        f.setTitle("Test Yard Direction Set Frame");
        f.initComponents(l, null);

        f.trackNameTextField.setText("4th yard track");
        f.trackLengthTextField.setText("21");
        enterClickAndLeave(f.addTrackButton);

        Track t = l.getTrackByName("4th yard track", null);
        Assert.assertNotNull("4th yard track", t);
        Assert.assertEquals("4th yard track length", 21, t.getLength());
        Assert.assertEquals("Direction all before change", ALL, t.getTrainDirections());

        // deselect east, west and south check boxes
        enterClickAndLeave(f.eastCheckBox);
        enterClickAndLeave(f.westCheckBox);
        enterClickAndLeave(f.southCheckBox);

        enterClickAndLeave(f.saveTrackButton);

        Assert.assertEquals("only north", Track.NORTH, t.getTrainDirections());

        // clean up the frame
        f.setVisible(false);
        JUnitUtil.dispose(f);

    }

    @Test
    public void testCreateTracksAndReloadFrame() {
        if (GraphicsEnvironment.isHeadless()) {
            return; // can't use Assume in TestCase subclasses
        }
        YardEditFrame f = new YardEditFrame();
        f.setTitle("Test Yard Create Frame");
        f.initComponents(l, null);

        // create four yard tracks
        f.trackNameTextField.setText("new yard track");
        f.trackLengthTextField.setText("43");
        enterClickAndLeave(f.addTrackButton);

        f.trackNameTextField.setText("2nd yard track");
        f.trackLengthTextField.setText("6543");
        enterClickAndLeave(f.addTrackButton);

        f.trackNameTextField.setText("3rd yard track");
        f.trackLengthTextField.setText("1");
        enterClickAndLeave(f.addTrackButton);

        f.trackNameTextField.setText("4th yard track");
        f.trackLengthTextField.setText("21");
        enterClickAndLeave(f.addTrackButton);

        // deselect east, west and south check boxes
        enterClickAndLeave(f.eastCheckBox);
        enterClickAndLeave(f.westCheckBox);
        enterClickAndLeave(f.southCheckBox);

        enterClickAndLeave(f.saveTrackButton);

        // clean up the frame
        f.setVisible(false);
        JUnitUtil.dispose(f);

        // now reload
        Location l2 = lManager.getLocationByName("Test Loc C");
        Assert.assertNotNull("Location Test Loc C", l2);

        LocationEditFrame fl = new LocationEditFrame(l2);
        fl.setTitle("Test Edit Restored Location Frame");

        // check location name
        Assert.assertEquals("name", "Test Loc C", fl.locationNameTextField.getText());

        Assert.assertEquals("number of yards", 4, fl.yardModel.getRowCount());
        Assert.assertEquals("number of staging tracks", 0, fl.stagingModel.getRowCount());

        // clean up the frame
        fl.setVisible(false);
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

        lManager = InstanceManager.getDefault(LocationManager.class);
        l = lManager.getLocationByName("Test Loc C");
    }

    @Override
    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }
}
