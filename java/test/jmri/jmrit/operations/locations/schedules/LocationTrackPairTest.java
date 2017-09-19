package jmri.jmrit.operations.locations.schedules;

import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.Track;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class LocationTrackPairTest {

    @Test
    public void testCTor() {
        Location l = new Location("Location Test Attridutes id", "Location Test Name");
        Track trk = new Track("Test id", "Test Name", "Test Type", l);
        LocationTrackPair t = new LocationTrackPair(l,trk);
        Assert.assertNotNull("exists",t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(LocationTrackPairTest.class);

}
