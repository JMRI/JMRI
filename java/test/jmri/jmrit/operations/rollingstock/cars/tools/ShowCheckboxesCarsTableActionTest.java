package jmri.jmrit.operations.rollingstock.cars.tools;

import jmri.jmrit.operations.OperationsTestCase;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class ShowCheckboxesCarsTableActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        ShowCheckboxesCarsTableAction t = new ShowCheckboxesCarsTableAction("Test");
        Assert.assertNotNull("exists",t);
    }

    // private final static Logger log = LoggerFactory.getLogger(ShowCheckboxesCarsTableActionTest.class);

}
