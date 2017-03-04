package jmri.jmrit.operations.locations.schedules;

import java.awt.GraphicsEnvironment;
import jmri.jmrit.operations.OperationsSwingTestCase;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.Track;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the Operations Schedules class
 *
 * @author Dan Boudreau Copyright (C) 2016
 */
public class ScheduleGuiTests extends OperationsSwingTestCase {

    @Test
    public void testScheduleCopyFrame() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ScheduleCopyFrame f = new ScheduleCopyFrame();
        Assert.assertNotNull(f);

        // TODO improve test
        f.dispose();
    }

    @Test
    public void testScheduleOptionsFrame() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
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

    @Test
    public void testSchedulesByLoadFrame() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SchedulesByLoadFrame f = new SchedulesByLoadFrame();
        Assert.assertNotNull(f);

        // TODO improve test
        f.dispose();
    }

    @Test
    public void testSchedulesTableFrame() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
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
    @Before
    public void setUp() throws Exception {
        super.setUp();

        loadLocations();
    }

    @Override
    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }
}
