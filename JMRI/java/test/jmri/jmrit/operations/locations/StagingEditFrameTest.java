//StagingEditFrameTest.java
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
public class StagingEditFrameTest extends OperationsSwingTestCase {

    final static int ALL = Track.EAST + Track.WEST + Track.NORTH + Track.SOUTH;

    LocationManager lManager = null; // set in setUp, dispose in tearDown
    Location l = null;  // set in setUp, dispose in tearDown

    /**
     * Staging tracks needs its own location
     */
    @Test
    public void testAddStagingTrackDefaults() {
        if (GraphicsEnvironment.isHeadless()) {
            return; // can't use Assume in TestCase subclasses
        }
        StagingEditFrame f = new StagingEditFrame();
        f.setTitle("Test Staging Add Frame");
        f.setLocation(0, 0);	// entire panel must be visible for tests to work properly
        f.initComponents(l, null);

        // create one staging tracks
        f.trackNameTextField.setText("new staging track");
        f.trackLengthTextField.setText("34");
        enterClickAndLeave(f.addTrackButton);
        Track t = l.getTrackByName("new staging track", null);
        Assert.assertNotNull("new staging track", t);
        Assert.assertEquals("staging track length", 34, t.getLength());

        // check that the defaults are correct
        Assert.assertEquals("all directions", ALL, t.getTrainDirections());
        Assert.assertEquals("all roads", Track.ALL_ROADS, t.getRoadOption());

        // add a second track
        f.trackNameTextField.setText("2nd staging track");
        f.trackLengthTextField.setText("3456");
        enterClickAndLeave(f.addTrackButton);

        t = l.getTrackByName("2nd staging track", null);
        Assert.assertNotNull("2nd staging track", t);
        Assert.assertEquals("2nd staging track length", 3456, t.getLength());
        // check that the defaults are correct
        Assert.assertEquals("all directions", ALL, t.getTrainDirections());
        Assert.assertEquals("all roads", Track.ALL_ROADS, t.getRoadOption());

        // add a third track
        f.trackNameTextField.setText("3rd staging track");
        f.trackLengthTextField.setText("1");
        enterClickAndLeave(f.addTrackButton);

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
        if (GraphicsEnvironment.isHeadless()) {
            return; // can't use Assume in TestCase subclasses
        }
        StagingEditFrame f = new StagingEditFrame();
        f.setTitle("Test Staging Add Frame");
        f.setLocation(0, 0);	// entire panel must be visible for tests to work properly
        f.initComponents(l, null);

        f.trackNameTextField.setText("4th staging track");
        f.trackLengthTextField.setText("12");
        enterClickAndLeave(f.addTrackButton);

        Track t = l.getTrackByName("4th staging track", null);
        Assert.assertNotNull("4th staging track", t);
        Assert.assertEquals("4th staging track length", 12, t.getLength());
        Assert.assertEquals("Direction All before Change", ALL, t.getTrainDirections());

        // deselect east, west and south check boxes
        enterClickAndLeave(f.northCheckBox);
        enterClickAndLeave(f.westCheckBox);
        enterClickAndLeave(f.southCheckBox);

        enterClickAndLeave(f.saveTrackButton);

        Assert.assertEquals("only east", Track.EAST, t.getTrainDirections());

        JUnitUtil.dispose(f);
    }

    @Test
    public void testAddCloseAndReload() {
        if (GraphicsEnvironment.isHeadless()) {
            return; // can't use Assume in TestCase subclasses
        }
        StagingEditFrame f = new StagingEditFrame();
        f.setTitle("Test Staging Add Frame");
        f.setLocation(0, 0);	// entire panel must be visible for tests to work properly
        f.initComponents(l, null);

        // create four staging tracks
        f.trackNameTextField.setText("new staging track");
        f.trackLengthTextField.setText("34");
        enterClickAndLeave(f.addTrackButton);

        f.trackNameTextField.setText("2nd staging track");
        f.trackLengthTextField.setText("3456");
        enterClickAndLeave(f.addTrackButton);

        f.trackNameTextField.setText("3rd staging track");
        f.trackLengthTextField.setText("1");
        enterClickAndLeave(f.addTrackButton);

        f.trackNameTextField.setText("4th staging track");
        f.trackLengthTextField.setText("12");
        enterClickAndLeave(f.addTrackButton);

        // deselect east, west and south check boxes
        enterClickAndLeave(f.northCheckBox);
        enterClickAndLeave(f.westCheckBox);
        enterClickAndLeave(f.southCheckBox);

        enterClickAndLeave(f.saveTrackButton);

        JUnitUtil.dispose(f);

        Location l2 = lManager.getLocationByName("Test Loc A");
        Assert.assertNotNull("Test Loc A", l2);

        LocationEditFrame fl = new LocationEditFrame(l2);
        fl.setTitle("Test Edit Location Frame Staging");

        // check location name
        Assert.assertEquals("name", "Test Loc A", fl.locationNameTextField.getText());

        Assert.assertEquals("number of sidings", 0, fl.spurModel.getRowCount());
        Assert.assertEquals("number of interchanges", 0, fl.interchangeModel.getRowCount());
        Assert.assertEquals("number of yards", 0, fl.yardModel.getRowCount());
        Assert.assertEquals("number of staging tracks", 4, fl.stagingModel.getRowCount());

        // is the staging only button selected?
        Assert.assertTrue("staging selected", fl.stageRadioButton.isSelected());

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
        l = lManager.getLocationByName("Test Loc A");
        Assert.assertNotNull("Test Loc A", l);

        jmri.jmrit.operations.setup.Setup.setRfidEnabled(false); // turn off the ID Tag Reader field by default.
    }

    @Override
    @After
    public void tearDown() throws Exception {
        super.tearDown();

        lManager = null;
        l = null;
    }
}
