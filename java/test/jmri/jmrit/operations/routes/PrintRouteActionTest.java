package jmri.jmrit.operations.routes;

import java.awt.GraphicsEnvironment;
import jmri.jmrit.operations.OperationsTestCase;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class PrintRouteActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Route r = new Route("Test Route","Test ID");
        PrintRouteAction t = new PrintRouteAction("Test Action",true,r);
        Assert.assertNotNull("exists",t);
    }

    // private final static Logger log = LoggerFactory.getLogger(PrintRouteActionTest.class);

}
