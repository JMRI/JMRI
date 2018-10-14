package jmri.jmrit.operations.routes;

import jmri.jmrit.operations.locations.Location;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class RouteLocationTest {

    @Test
    public void testCTor() {
        Location l = new Location("Test id", "Test Name");
        RouteLocation t = new RouteLocation("Test",l);
        Assert.assertNotNull("exists",t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(RouteLocationTest.class);

}
