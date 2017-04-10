package jmri.jmrit.operations.routes;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrit.operations.locations.Location;

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
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(RouteLocationTest.class.getName());

}
