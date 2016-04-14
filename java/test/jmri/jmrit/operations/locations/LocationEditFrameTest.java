//LocationEditFrameTest.java
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
public class LocationEditFrameTest extends OperationsSwingTestCase {

    final static int ALL = Track.EAST + Track.WEST + Track.NORTH + Track.SOUTH;

    public void testLocationEditFrame() {
        loadLocations();
        
        LocationEditFrame f = new LocationEditFrame(null);
        f.setTitle("Test Add Location Frame");

        f.locationNameTextField.setText("New Test Location");
        //f.addLocationButton.doClick();
        getHelper().enterClickAndLeave(new MouseEventData(this, f.addLocationButton));

        LocationManager lManager = LocationManager.instance();
        Assert.assertEquals("should be 6 locations", 6, lManager.getLocationsByNameList().size());
        Location newLoc = lManager.getLocationByName("New Test Location");

        Assert.assertNotNull(newLoc);

        // add a yard track
        getHelper().enterClickAndLeave(new MouseEventData(this, f.addYardButton));

        // add an interchange track
        getHelper().enterClickAndLeave(new MouseEventData(this, f.addInterchangeButton));

        // add a staging track
        getHelper().enterClickAndLeave(new MouseEventData(this, f.addStagingButton));

        // add a yard track
        getHelper().enterClickAndLeave(new MouseEventData(this, f.addYardButton));

        f.locationNameTextField.setText("Newer Test Location");
        getHelper().enterClickAndLeave(new MouseEventData(this, f.saveLocationButton));

        Assert.assertEquals("changed location name", "Newer Test Location", newLoc.getName());

        // test delete button
        getHelper().enterClickAndLeave(new MouseEventData(this, f.deleteLocationButton));
        Assert.assertEquals("should be 6 locations", 6, lManager.getLocationsByNameList().size());
        // confirm delete dialog window should appear
        pressDialogButton(f, "Yes");
        // location now deleted
        Assert.assertEquals("should be 5 locations", 5, lManager.getLocationsByNameList().size());

        f.dispose();
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

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public LocationEditFrameTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", LocationEditFrameTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(LocationEditFrameTest.class);
        return suite;
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
}
