package jmri.jmrit.operations.routes;

import jmri.jmrit.operations.OperationsTestCase;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class RouteEditTableModelTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        RouteEditTableModel t = new RouteEditTableModel();
        Assert.assertNotNull("exists",t);
    }

    // private final static Logger log = LoggerFactory.getLogger(RouteEditTableModelTest.class);

}
