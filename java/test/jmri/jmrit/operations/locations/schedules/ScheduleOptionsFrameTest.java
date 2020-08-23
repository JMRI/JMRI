package jmri.jmrit.operations.locations.schedules;

import java.awt.GraphicsEnvironment;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.Assume;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.Track;
import jmri.util.JUnitOperationsUtil;
import jmri.util.JUnitUtil;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class ScheduleOptionsFrameTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Location l = new Location("Location Test Attridutes id", "Location Test Name");
        Track trk = new Track("Test id", "Test Name", "Test Type", l);
        ScheduleEditFrame t = new ScheduleEditFrame(new Schedule("Test id", "Test Name"), trk);
        ScheduleOptionsFrame a = new ScheduleOptionsFrame(t);
        Assert.assertNotNull("exists", a);
        JUnitUtil.dispose(t);
        JUnitUtil.dispose(a);
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

    // private final static Logger log = LoggerFactory.getLogger(ScheduleOptionsFrameTest.class);
}
