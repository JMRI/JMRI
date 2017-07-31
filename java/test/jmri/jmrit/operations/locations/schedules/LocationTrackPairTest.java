package jmri.jmrit.operations.locations.schedules;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.Track;

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
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(LocationTrackPairTest.class.getName());

}
