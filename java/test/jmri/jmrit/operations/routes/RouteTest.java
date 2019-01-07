package jmri.jmrit.operations.routes;

import jmri.jmrit.operations.OperationsTestCase;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class RouteTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Route t = new Route("Test Route","Test ID");
        Assert.assertNotNull("exists",t);
    }

    // private final static Logger log = LoggerFactory.getLogger(RouteTest.class);

}
