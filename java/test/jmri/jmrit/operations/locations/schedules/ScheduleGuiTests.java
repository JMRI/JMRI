package jmri.jmrit.operations.locations.schedules;

import java.awt.GraphicsEnvironment;
import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.Track;
import jmri.util.JUnitOperationsUtil;
import jmri.util.JUnitUtil;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

/**
 * Tests for the Operations Schedules class
 *
 * @author Dan Boudreau Copyright (C) 2016
 */
public class ScheduleGuiTests extends OperationsTestCase {

    @Test
    public void testScheduleCopyFrame() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ScheduleCopyFrame f = new ScheduleCopyFrame();
        Assert.assertNotNull(f);

        // TODO improve test
        JUnitUtil.dispose(f);
    }

    @Test
    public void testScheduleOptionsFrame() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JUnitOperationsUtil.loadFiveLocations();
        LocationManager lManager = InstanceManager.getDefault(LocationManager.class);
        Track track = lManager.getLocationByName("Test Loc E").getTrackByName("Test Track", null);
        ScheduleManager sManager = InstanceManager.getDefault(ScheduleManager.class);
        Schedule schedule = sManager.newSchedule("test schedule");
        ScheduleEditFrame f = new ScheduleEditFrame(schedule, track);
        Assert.assertNotNull(f);

        // TODO improve test
        ScheduleOptionsFrame sf = new ScheduleOptionsFrame(f);
        Assert.assertNotNull(sf);
        JUnitUtil.dispose(sf);
        JUnitUtil.dispose(f);
    }

    @Test
    public void testSchedulesByLoadFrame() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SchedulesByLoadFrame f = new SchedulesByLoadFrame();
        Assert.assertNotNull(f);

        // TODO improve test
        JUnitUtil.dispose(f);
    }

    @Test
    public void testSchedulesTableFrame() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SchedulesTableFrame f = new SchedulesTableFrame();
        Assert.assertNotNull(f);

        // TODO improve test
        JUnitUtil.dispose(f);
    }
}
