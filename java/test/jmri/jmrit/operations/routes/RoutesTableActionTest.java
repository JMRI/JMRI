package jmri.jmrit.operations.routes;

import jmri.jmrit.operations.OperationsTestCase;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class RoutesTableActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        RoutesTableAction t = new RoutesTableAction();
        Assert.assertNotNull("exists",t);
    }

    // private final static Logger log = LoggerFactory.getLogger(RoutesTableActionTest.class);

}
