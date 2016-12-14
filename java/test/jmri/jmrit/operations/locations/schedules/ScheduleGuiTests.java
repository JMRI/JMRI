package jmri.jmrit.operations.locations.schedules;

import java.awt.GraphicsEnvironment;
import jmri.jmrit.operations.OperationsSwingTestCase;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.Track;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.junit.Assert;

/**
 * Tests for the Operations Schedules class
 *
 * @author Dan Boudreau Copyright (C) 2016
 */
public class ScheduleGuiTests extends OperationsSwingTestCase {

    public void testScheduleCopyFrame() {
        if (GraphicsEnvironment.isHeadless()) {
            return; // can't use Assume in TestCase subclasses
        }
        ScheduleCopyFrame f = new ScheduleCopyFrame();
        Assert.assertNotNull(f);

        // TODO improve test
        f.dispose();
    }

    public void testScheduleOptionsFrame() {
        if (GraphicsEnvironment.isHeadless()) {
            return; // can't use Assume in TestCase subclasses
        }
        LocationManager lManager = LocationManager.instance();
        Track track = lManager.getLocationByName("Test Loc E").getTrackByName("Test Track", null);
        ScheduleManager sManager = ScheduleManager.instance();
        Schedule schedule = sManager.newSchedule("test schedule");
        ScheduleEditFrame f = new ScheduleEditFrame(schedule, track);
        Assert.assertNotNull(f);

        // TODO improve test

        ScheduleOptionsFrame sf = new ScheduleOptionsFrame(f);
        Assert.assertNotNull(sf);
        sf.dispose();
        f.dispose();
    }

    public void testSchedulesByLoadFrame() {
        if (GraphicsEnvironment.isHeadless()) {
            return; // can't use Assume in TestCase subclasses
        }
        SchedulesByLoadFrame f = new SchedulesByLoadFrame();
        Assert.assertNotNull(f);

        // TODO improve test
        f.dispose();
    }

    public void testSchedulesTableFrame() {
        if (GraphicsEnvironment.isHeadless()) {
            return; // can't use Assume in TestCase subclasses
        }
        SchedulesTableFrame f = new SchedulesTableFrame();
        Assert.assertNotNull(f);

        // TODO improve test
        f.dispose();
    }

    private void loadLocations() {
        // create 5 locations
        LocationManager lManager = LocationManager.instance();
        Location l1 = lManager.newLocation("Test Loc E");
        l1.addTrack("Test Track", Track.SPUR);
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

    public ScheduleGuiTests(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", ScheduleGuiTests.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(ScheduleGuiTests.class);
        return suite;
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
}
