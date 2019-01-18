package jmri.jmrit.operations;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class OperationsMenuTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        OperationsMenu t = new OperationsMenu();
        Assert.assertNotNull("exists",t);
    }

    // private final static Logger log = LoggerFactory.getLogger(OperationsMenuTest.class);

}
