package jmri.jmrit.operations.rollingstock.cars.tools;

import jmri.jmrit.operations.OperationsTestCase;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class ResetCheckboxesCarsTableActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        ResetCheckboxesCarsTableAction t = new ResetCheckboxesCarsTableAction("Test");
        Assert.assertNotNull("exists",t);
    }

    // private final static Logger log = LoggerFactory.getLogger(ResetCheckboxesCarsTableActionTest.class);

}
