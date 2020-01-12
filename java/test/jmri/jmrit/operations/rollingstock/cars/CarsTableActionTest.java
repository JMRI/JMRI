package jmri.jmrit.operations.rollingstock.cars;

import jmri.jmrit.operations.OperationsTestCase;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class CarsTableActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        CarsTableAction t = new CarsTableAction();
        Assert.assertNotNull("exists",t);
    }

    // private final static Logger log = LoggerFactory.getLogger(CarsTableActionTest.class);

}
