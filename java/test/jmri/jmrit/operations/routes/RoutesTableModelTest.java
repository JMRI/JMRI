package jmri.jmrit.operations.routes;

import jmri.jmrit.operations.OperationsTestCase;
import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class RoutesTableModelTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        RoutesTableModel t = new RoutesTableModel();
        Assert.assertNotNull("exists",t);
    }

    // private final static Logger log = LoggerFactory.getLogger(RoutesTableModelTest.class);

}
