//YardEditFrameTest.java
package jmri.jmrit.operations.locations;

import jmri.jmrit.operations.OperationsSwingTestCase;
import junit.extensions.jfcunit.eventdata.MouseEventData;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests for the Operations Locations GUI class
 *
 * @author	Dan Boudreau Copyright (C) 2009
 */
public class YardEditFrameTest extends OperationsSwingTestCase {

    final static int ALL = Track.EAST + Track.WEST + Track.NORTH + Track.SOUTH;

    public void testYardEditFrame() {
        LocationManager lManager = LocationManager.instance();
        Location l = lManager.getLocationByName("Test Loc C");
        YardEditFrame f = new YardEditFrame();
        f.setTitle("Test Yard Add Frame");
        f.initComponents(l, null);

        // create four yard tracks
        f.trackNameTextField.setText("new yard track");
        f.trackLengthTextField.setText("43");
        getHelper().enterClickAndLeave(new MouseEventData(this, f.addTrackButton));

        f.trackNameTextField.setText("2nd yard track");
        f.trackLengthTextField.setText("6543");
        getHelper().enterClickAndLeave(new MouseEventData(this, f.addTrackButton));

        f.trackNameTextField.setText("3rd yard track");
        f.trackLengthTextField.setText("1");
        getHelper().enterClickAndLeave(new MouseEventData(this, f.addTrackButton));

        f.trackNameTextField.setText("4th yard track");
        f.trackLengthTextField.setText("21");
        getHelper().enterClickAndLeave(new MouseEventData(this, f.addTrackButton));

        // deselect east, west and south check boxes
        getHelper().enterClickAndLeave(new MouseEventData(this, f.eastCheckBox));
        getHelper().enterClickAndLeave(new MouseEventData(this, f.westCheckBox));
        getHelper().enterClickAndLeave(new MouseEventData(this, f.southCheckBox));

        getHelper().enterClickAndLeave(new MouseEventData(this, f.saveTrackButton));

        Track t = l.getTrackByName("new yard track", null);
        Assert.assertNotNull("new yard track", t);
        Assert.assertEquals("yard track length", 43, t.getLength());
        // check that the defaults are correct
        Assert.assertEquals("all directions", ALL, t.getTrainDirections());
        Assert.assertEquals("all roads", Track.ALL_ROADS, t.getRoadOption());

        t = l.getTrackByName("2nd yard track", null);
        Assert.assertNotNull("2nd yard track", t);
        Assert.assertEquals("2nd yard track length", 6543, t.getLength());
        // check that the defaults are correct
        Assert.assertEquals("all directions", ALL, t.getTrainDirections());
        Assert.assertEquals("all roads", Track.ALL_ROADS, t.getRoadOption());

        t = l.getTrackByName("3rd yard track", null);
        Assert.assertNotNull("3rd yard track", t);
        Assert.assertEquals("3rd yard track length", 1, t.getLength());
        // check that the defaults are correct
        Assert.assertEquals("all directions", ALL, t.getTrainDirections());
        Assert.assertEquals("all roads", Track.ALL_ROADS, t.getRoadOption());

        t = l.getTrackByName("4th yard track", null);
        Assert.assertNotNull("4th yard track", t);
        Assert.assertEquals("4th yard track length", 21, t.getLength());
        Assert.assertEquals("only north", Track.NORTH, t.getTrainDirections());

        f.dispose();

        // now reload
        Location l2 = lManager.getLocationByName("Test Loc C");
        Assert.assertNotNull("Location Test Loc C", l2);

        LocationEditFrame fl = new LocationEditFrame(l2);
        fl.setTitle("Test Edit Location Frame");

        // check location name
        Assert.assertEquals("name", "Test Loc C", fl.locationNameTextField.getText());

        Assert.assertEquals("number of yards", 4, fl.yardModel.getRowCount());
        Assert.assertEquals("number of staging tracks", 0, fl.stagingModel.getRowCount());

        fl.dispose();
    }

    private void loadLocations() {
        // create 5 locations
        LocationManager lManager = LocationManager.instance();
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
    protected void setUp() throws Exception {
        super.setUp();

        loadLocations();
    }

    public YardEditFrameTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", YardEditFrameTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(YardEditFrameTest.class);
        return suite;
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
}
