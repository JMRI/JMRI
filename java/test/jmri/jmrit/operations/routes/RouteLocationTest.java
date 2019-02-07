package jmri.jmrit.operations.routes;

import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.locations.Location;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class RouteLocationTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Location l = new Location("Test id", "Test Name");
        RouteLocation t = new RouteLocation("Test",l);
        Assert.assertNotNull("exists",t);
    }

    // private final static Logger log = LoggerFactory.getLogger(RouteLocationTest.class);

}
