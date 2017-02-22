//SidingEditFrameTest.java
package jmri.jmrit.operations.locations;

import java.awt.GraphicsEnvironment;
import jmri.jmrit.operations.OperationsSwingTestCase;
import jmri.util.JmriJFrame;
import junit.extensions.jfcunit.eventdata.MouseEventData;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.junit.Assert;

/**
 * Tests for the Operations Locations GUI class
 *
 * @author	Dan Boudreau Copyright (C) 2009
 */
public class SidingEditFrameTest extends OperationsSwingTestCase {

    final static int ALL = Track.EAST + Track.WEST + Track.NORTH + Track.SOUTH;
    private LocationManager lManager = null;
    private Location l = null;

    public void testAddSidingDefaults() {
        if (GraphicsEnvironment.isHeadless()) {
            return; // can't use Assume in TestCase subclasses
        }
        LocationManager lManager = LocationManager.instance();
        Location l = lManager.getLocationByName("Test Loc C");
        SpurEditFrame f = new SpurEditFrame();
        f.setTitle("Test Siding Add Frame");
        f.setLocation(0, 0);	// entire panel must be visible for tests to work properly
        f.initComponents(l, null);

        // create one siding tracks
        f.trackNameTextField.setText("new siding track");
        f.trackLengthTextField.setText("1223");
        getHelper().enterClickAndLeave(new MouseEventData(this, f.addTrackButton,1000l));
        sleep(1);   // for slow machines

        Track t = l.getTrackByName("new siding track", null);
        Assert.assertNotNull("new siding track", t);
        Assert.assertEquals("siding track length", 1223, t.getLength());
        // check that the defaults are correct
        Assert.assertEquals("all directions", ALL, t.getTrainDirections());
        Assert.assertEquals("all roads", Track.ALL_ROADS, t.getRoadOption());

        // create a second siding
        f.trackNameTextField.setText("2nd siding track");
        f.trackLengthTextField.setText("9999");
        getHelper().enterClickAndLeave(new MouseEventData(this, f.addTrackButton,1000l));

        sleep(1);   // for slow machines

        t = l.getTrackByName("2nd siding track", null);
        Assert.assertNotNull("2nd siding track", t);
        Assert.assertEquals("2nd siding track length", 9999, t.getLength());
        // check that the defaults are correct
        Assert.assertEquals("all directions", ALL, t.getTrainDirections());
        Assert.assertEquals("all roads", Track.ALL_ROADS, t.getRoadOption());

        // kill all frames
        f.dispose();
    }


    public void testSetDirectionUsingCheckbox() {
        if (GraphicsEnvironment.isHeadless()) {
            return; // can't use Assume in TestCase subclasses
        }
        SpurEditFrame f = new SpurEditFrame();
        f.setTitle("Test Siding Add Frame");
        f.setLocation(0, 0);	// entire panel must be visible for tests to work properly
        f.initComponents(l, null);

        f.trackNameTextField.setText("3rd siding track");
        f.trackLengthTextField.setText("1010");
        getHelper().enterClickAndLeave(new MouseEventData(this, f.addTrackButton,1000l));

        sleep(1);   // for slow machines

        Track t = l.getTrackByName("3rd siding track", null);
        Assert.assertNotNull("3rd siding track", t);
        Assert.assertEquals("3rd siding track length", 1010, t.getLength());
        Assert.assertEquals("Direction All before change", ALL , t.getTrainDirections());

        // deselect east, west and north check boxes
        getHelper().enterClickAndLeave(new MouseEventData(this, f.eastCheckBox,1000l));
        getHelper().enterClickAndLeave(new MouseEventData(this, f.westCheckBox,1000l));
        getHelper().enterClickAndLeave(new MouseEventData(this, f.northCheckBox,1000l));

        getHelper().enterClickAndLeave(new MouseEventData(this, f.saveTrackButton,1000l));

        sleep(1);   // for slow machines

        Assert.assertEquals("only south", Track.SOUTH, t.getTrainDirections());

        // kill all frames
        f.dispose();
    }

    public void testAddScheduleButton() {
        if (GraphicsEnvironment.isHeadless()) {
            return; // can't use Assume in TestCase subclasses
        }
        SpurEditFrame f = new SpurEditFrame();
        f.setTitle("Test Siding Add Frame");
        f.setLocation(0, 0);	// entire panel must be visible for tests to work properly
        f.initComponents(l, null);

        f.trackNameTextField.setText("3rd siding track");
        f.trackLengthTextField.setText("1010");
        getHelper().enterClickAndLeave(new MouseEventData(this, f.addTrackButton,1000l));

        sleep(1);   // for slow machines

        // create the schedule edit frame
        getHelper().enterClickAndLeave(new MouseEventData(this, f.editScheduleButton,1000l));

        sleep(1);   // for slow machines

        // confirm schedule add frame creation
        JmriJFrame sef = JmriJFrame.getFrame("Add Schedule for Spur \"3rd siding track\"");
        Assert.assertNotNull(sef);

        // kill all frames
        f.dispose();
        sef.dispose();
    }

    public void testAddCloseAndRestore() {
        if (GraphicsEnvironment.isHeadless()) {
            return; // can't use Assume in TestCase subclasses
        }
        SpurEditFrame f = new SpurEditFrame();
        f.setTitle("Test Siding Add Frame");
        f.setLocation(0, 0);	// entire panel must be visible for tests to work properly
        f.initComponents(l, null);

        // create three siding tracks
        f.trackNameTextField.setText("new siding track");
        f.trackLengthTextField.setText("1223");
        getHelper().enterClickAndLeave(new MouseEventData(this, f.addTrackButton,1000l));

        f.trackNameTextField.setText("2nd siding track");
        f.trackLengthTextField.setText("9999");
        getHelper().enterClickAndLeave(new MouseEventData(this, f.addTrackButton,1000l));

        f.trackNameTextField.setText("3rd siding track");
        f.trackLengthTextField.setText("1010");
        getHelper().enterClickAndLeave(new MouseEventData(this, f.addTrackButton,1000l));

        // deselect east, west and north check boxes
        getHelper().enterClickAndLeave(new MouseEventData(this, f.eastCheckBox,1000l));
        getHelper().enterClickAndLeave(new MouseEventData(this, f.westCheckBox,1000l));
        getHelper().enterClickAndLeave(new MouseEventData(this, f.northCheckBox,1000l));

        getHelper().enterClickAndLeave(new MouseEventData(this, f.saveTrackButton,1000l));

        // create the schedule edit frame
        getHelper().enterClickAndLeave(new MouseEventData(this, f.editScheduleButton,1000l));

        // confirm schedule add frame creation
        JmriJFrame sef = JmriJFrame.getFrame("Add Schedule for Spur \"3rd siding track\"");
        Assert.assertNotNull(sef);

        // kill all frames
        f.dispose();
        sef.dispose();

        // now reload
        Location l2 = lManager.getLocationByName("Test Loc C");
        Assert.assertNotNull("Location Test Loc C", l2);

        LocationEditFrame fl = new LocationEditFrame(l2);
        fl.setTitle("Test Edit Location Frame");

        // check location name
        Assert.assertEquals("name", "Test Loc C", fl.locationNameTextField.getText());

        Assert.assertEquals("number of sidings", 3, fl.spurModel.getRowCount());
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
        lManager = LocationManager.instance();
        l = lManager.getLocationByName("Test Loc C");
    }

    public SidingEditFrameTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", SidingEditFrameTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(SidingEditFrameTest.class);
        return suite;
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
}
