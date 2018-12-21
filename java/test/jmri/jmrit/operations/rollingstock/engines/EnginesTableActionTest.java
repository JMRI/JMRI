package jmri.jmrit.operations.rollingstock.engines;

import jmri.jmrit.operations.OperationsTestCase;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class EnginesTableActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        EnginesTableAction t = new EnginesTableAction();
        Assert.assertNotNull("exists",t);
    }

    // private final static Logger log = LoggerFactory.getLogger(EnginesTableActionTest.class);

}
