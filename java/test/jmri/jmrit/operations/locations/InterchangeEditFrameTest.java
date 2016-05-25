//InterchangeEditFrameTest.java
package jmri.jmrit.operations.locations;

import jmri.jmrit.operations.OperationsSwingTestCase;
import jmri.jmrit.operations.rollingstock.cars.CarRoads;
import junit.extensions.jfcunit.eventdata.MouseEventData;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests for the Operations Locations GUI class
 *
 * @author	Dan Boudreau Copyright (C) 2009
 */
public class InterchangeEditFrameTest extends OperationsSwingTestCase {

    final static int ALL = Track.EAST + Track.WEST + Track.NORTH + Track.SOUTH;

    public void testInterchangeEditFrame() {
        // add UP road name
        CarRoads cr = CarRoads.instance();
        cr.addName("UP");

        LocationManager lManager = LocationManager.instance();
        Location l3 = lManager.newLocation("Test Loc C");
        l3.setLength(1003);

        Location l = lManager.getLocationByName("Test Loc C");
        Assert.assertNotNull("Test Loc C", l);
        InterchangeEditFrame f = new InterchangeEditFrame();
        f.setTitle("Test Interchange Add Frame");
        f.setLocation(0, 0);	// entire panel must be visible for tests to work properly
        f.initComponents(l, null);

        // create two interchange tracks
        f.trackNameTextField.setText("new interchange track");
        f.trackLengthTextField.setText("321");
        getHelper().enterClickAndLeave(new MouseEventData(this, f.addTrackButton));

        f.trackNameTextField.setText("2nd interchange track");
        f.trackLengthTextField.setText("4331");
        getHelper().enterClickAndLeave(new MouseEventData(this, f.addTrackButton));

        // deselect east and south check boxes
        getHelper().enterClickAndLeave(new MouseEventData(this, f.eastCheckBox));
        getHelper().enterClickAndLeave(new MouseEventData(this, f.southCheckBox));

        getHelper().enterClickAndLeave(new MouseEventData(this, f.saveTrackButton));

        Track t = l.getTrackByName("new interchange track", Track.INTERCHANGE);
        Assert.assertNotNull("new interchange track", t);
        Assert.assertEquals("interchange track length", 321, t.getLength());
        // check that the defaults are correct
        Assert.assertEquals("all directions", ALL, t.getTrainDirections());
        Assert.assertEquals("all roads", Track.ALL_ROADS, t.getRoadOption());

        t = l.getTrackByName("2nd interchange track", Track.INTERCHANGE);
        Assert.assertNotNull("2nd interchange track", t);
        Assert.assertEquals("2nd interchange track length", 4331, t.getLength());
        Assert.assertEquals("west and north", Track.NORTH + Track.WEST, t.getTrainDirections());

        // check track accepts Boxcars
        Assert.assertTrue("2nd interchange track accepts Boxcars", t.acceptsTypeName("Boxcar"));
        // test clear car types button
        getHelper().enterClickAndLeave(new MouseEventData(this, f.clearButton));
        getHelper().enterClickAndLeave(new MouseEventData(this, f.saveTrackButton));
        Assert.assertFalse("2nd interchange track doesn't accept Boxcars", t.acceptsTypeName("Boxcar"));

        getHelper().enterClickAndLeave(new MouseEventData(this, f.setButton));
        getHelper().enterClickAndLeave(new MouseEventData(this, f.saveTrackButton));
        Assert.assertTrue("2nd interchange track accepts Boxcars again", t.acceptsTypeName("Boxcar"));

        f.dispose();

        // now reload
        Location l2 = lManager.getLocationByName("Test Loc C");
        Assert.assertNotNull("Location Test Loc C", l2);

        LocationEditFrame fl = new LocationEditFrame(l2);
        fl.setTitle("Test Edit Location Frame");

        // check location name
        Assert.assertEquals("name", "Test Loc C", fl.locationNameTextField.getText());

        Assert.assertEquals("number of interchanges", 2, fl.interchangeModel.getRowCount());
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

    public InterchangeEditFrameTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", InterchangeEditFrameTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(InterchangeEditFrameTest.class);
        return suite;
    }

    // The minimal setup for log4J
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
}
