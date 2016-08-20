//LocationTableFrameTest.java
package jmri.jmrit.operations.locations;

import jmri.jmrit.operations.OperationsSwingTestCase;
import jmri.util.JmriJFrame;
import junit.extensions.jfcunit.eventdata.MouseEventData;
import org.junit.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests for the Operations Locations GUI class
 *
 * @author	Dan Boudreau Copyright (C) 2009
 */
public class LocationTableFrameTest extends OperationsSwingTestCase {

    final static int ALL = Track.EAST + Track.WEST + Track.NORTH + Track.SOUTH;

    public void testLocationsTableFrame() {

        LocationsTableFrame f = new LocationsTableFrame();

        // should be 5 rows
        Assert.assertEquals("number of rows", 5, f.locationsModel.getRowCount());
        // default is sort by name
        Assert.assertEquals("1st loc", "Test Loc A", f.locationsModel.getValueAt(0, LocationsTableModel.NAMECOLUMN));
        Assert.assertEquals("2nd loc", "Test Loc B", f.locationsModel.getValueAt(1, LocationsTableModel.NAMECOLUMN));
        Assert.assertEquals("3rd loc", "Test Loc C", f.locationsModel.getValueAt(2, LocationsTableModel.NAMECOLUMN));
        Assert.assertEquals("4th loc", "Test Loc D", f.locationsModel.getValueAt(3, LocationsTableModel.NAMECOLUMN));
        Assert.assertEquals("5th loc", "Test Loc E", f.locationsModel.getValueAt(4, LocationsTableModel.NAMECOLUMN));

        // check location lengths
        Assert.assertEquals("1st loc length", "1005", f.locationsModel.getValueAt(0, LocationsTableModel.LENGTHCOLUMN));
        Assert.assertEquals("2nd loc length", "1004", f.locationsModel.getValueAt(1, LocationsTableModel.LENGTHCOLUMN));
        Assert.assertEquals("3rd loc length", "1003", f.locationsModel.getValueAt(2, LocationsTableModel.LENGTHCOLUMN));
        Assert.assertEquals("4th loc length", "1002", f.locationsModel.getValueAt(3, LocationsTableModel.LENGTHCOLUMN));
        Assert.assertEquals("5th loc length", "1001", f.locationsModel.getValueAt(4, LocationsTableModel.LENGTHCOLUMN));

        // create edit location frame
        f.locationsModel.setValueAt(null, 2, LocationsTableModel.EDITCOLUMN);

        // create add location frame by clicking add button
        getHelper().enterClickAndLeave(new MouseEventData(this, f.addButton));

        // confirm location add frame creation
        JmriJFrame lef = JmriJFrame.getFrame("Add Location");
        Assert.assertNotNull(lef);

        // confirm location edit frame creation
        JmriJFrame lef2 = JmriJFrame.getFrame("Edit Location");
        Assert.assertNotNull(lef2);

        // close windows
        lef.dispose();
        f.dispose();

        Assert.assertNull(JmriJFrame.getFrame("Add Location"));
        Assert.assertNull(JmriJFrame.getFrame("Edit Location"));

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

    public LocationTableFrameTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", LocationTableFrameTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(LocationTableFrameTest.class);
        return suite;
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
}
