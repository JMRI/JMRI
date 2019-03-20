package jmri.jmrit.operations.locations;

import java.awt.GraphicsEnvironment;
import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
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
public class YardEditFrameTest extends OperationsTestCase {

    final static int ALL = Track.EAST + Track.WEST + Track.NORTH + Track.SOUTH;
    private LocationManager lManager;
    private Location l;

    @Test
    public void testCreateYardTrackDefault() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        YardEditFrame f = new YardEditFrame();
        f.setTitle("Test Yard Add Frame");
        f.initComponents(l, null);

        // create a yard track with length 43.
        f.trackNameTextField.setText("new yard track");
        f.trackLengthTextField.setText("43");
        JemmyUtil.enterClickAndLeave(f.addTrackButton);

        Track t = l.getTrackByName("new yard track", null);
        Assert.assertNotNull("new yard track", t);
        Assert.assertEquals("yard track length", 43, t.getLength());
        // check that the defaults are correct
        Assert.assertEquals("all directions", ALL, t.getTrainDirections());
        Assert.assertEquals("all roads", Track.ALL_ROADS, t.getRoadOption());

        // add a second track with length 6543.
        f.trackNameTextField.setText("2nd yard track");
        f.trackLengthTextField.setText("6543");
        JemmyUtil.enterClickAndLeave(f.addTrackButton);

        t = l.getTrackByName("2nd yard track", null);
        Assert.assertNotNull("2nd yard track", t);
        Assert.assertEquals("2nd yard track length", 6543, t.getLength());
        // check that the defaults are correct
        Assert.assertEquals("all directions", ALL, t.getTrainDirections());
        Assert.assertEquals("all roads", Track.ALL_ROADS, t.getRoadOption());

        // add A third track with length 1.
        f.trackNameTextField.setText("3rd yard track");
        f.trackLengthTextField.setText("1");
        JemmyUtil.enterClickAndLeave(f.addTrackButton);

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
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        YardEditFrame f = new YardEditFrame();
        f.setTitle("Test Yard Direction Set Frame");
        f.initComponents(l, null);

        f.trackNameTextField.setText("4th yard track");
        f.trackLengthTextField.setText("21");
        JemmyUtil.enterClickAndLeave(f.addTrackButton);

        Track t = l.getTrackByName("4th yard track", null);
        Assert.assertNotNull("4th yard track", t);
        Assert.assertEquals("4th yard track length", 21, t.getLength());
        Assert.assertEquals("Direction all before change", ALL, t.getTrainDirections());

        // deselect east, west and south check boxes
        JemmyUtil.enterClickAndLeave(f.eastCheckBox);
        JemmyUtil.enterClickAndLeave(f.westCheckBox);
        JemmyUtil.enterClickAndLeave(f.southCheckBox);

        JemmyUtil.enterClickAndLeave(f.saveTrackButton);

        Assert.assertEquals("only north", Track.NORTH, t.getTrainDirections());

        // clean up the frame
        f.setVisible(false);
        JUnitUtil.dispose(f);

    }

    @Test
    public void testCreateTracksAndReloadFrame() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        YardEditFrame f = new YardEditFrame();
        f.setTitle("Test Yard Create Frame");
        f.initComponents(l, null);

        // create four yard tracks
        f.trackNameTextField.setText("new yard track");
        f.trackLengthTextField.setText("43");
        JemmyUtil.enterClickAndLeave(f.addTrackButton);

        f.trackNameTextField.setText("2nd yard track");
        f.trackLengthTextField.setText("6543");
        JemmyUtil.enterClickAndLeave(f.addTrackButton);

        f.trackNameTextField.setText("3rd yard track");
        f.trackLengthTextField.setText("1");
        JemmyUtil.enterClickAndLeave(f.addTrackButton);

        f.trackNameTextField.setText("4th yard track");
        f.trackLengthTextField.setText("21");
        JemmyUtil.enterClickAndLeave(f.addTrackButton);

        // deselect east, west and south check boxes
        JemmyUtil.enterClickAndLeave(f.eastCheckBox);
        JemmyUtil.enterClickAndLeave(f.westCheckBox);
        JemmyUtil.enterClickAndLeave(f.southCheckBox);

        JemmyUtil.enterClickAndLeave(f.saveTrackButton);

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

    // Ensure minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        super.setUp();

        JUnitOperationsUtil.loadFiveLocations();

        lManager = InstanceManager.getDefault(LocationManager.class);
        l = lManager.getLocationByName("Test Loc C");
        
        JUnitOperationsUtil.loadTrain(l);
    }
}
