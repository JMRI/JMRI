package jmri.jmrit.operations.locations.schedules.tools;

import java.awt.GraphicsEnvironment;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.Test;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.locations.*;
import jmri.jmrit.operations.locations.schedules.*;
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
    
    @Test
    public void testCloseWindowOnSave() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Location loc = JUnitOperationsUtil.createOneNormalLocation("Test");
        Track track = loc.getTrackByName("Test Spur 1", null);
        ScheduleManager sManager = InstanceManager.getDefault(ScheduleManager.class);
        Schedule schedule = sManager.newSchedule("test schedule");
        ScheduleEditFrame sef = new ScheduleEditFrame(schedule, track);
        ScheduleOptionsFrame f = new ScheduleOptionsFrame(sef);
        JUnitOperationsUtil.testCloseWindowOnSave(f.getTitle());
    }


    // private final static Logger log = LoggerFactory.getLogger(ScheduleOptionsFrameTest.class);
}
