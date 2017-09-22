package jmri.jmrit.operations.locations.schedules;

import java.awt.GraphicsEnvironment;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.Track;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class ScheduleOptionsActionTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Location l = new Location("Location Test Attridutes id", "Location Test Name");
        Track trk = new Track("Test id", "Test Name", "Test Type", l);
        ScheduleEditFrame t = new ScheduleEditFrame(new Schedule("Test id", "Test Name"), trk);
        ScheduleOptionsAction a = new ScheduleOptionsAction(t);
        Assert.assertNotNull("exists", a);
        JUnitUtil.dispose(t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(ScheduleOptionsActionTest.class);
}
