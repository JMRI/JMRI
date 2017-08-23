package jmri.jmrit.operations.locations.schedules;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import java.awt.GraphicsEnvironment;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.Track;

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
        t.dispose();
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(ScheduleOptionsActionTest.class.getName());
}
