package jmri.jmrit.operations.rollingstock.cars.tools;

import org.junit.Assert;
import org.junit.jupiter.api.*;

import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.rollingstock.cars.CarsTableModel;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class ResetCheckboxesCarsTableActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        CarsTableModel carsTableModel = new CarsTableModel(false, "test", "test");
        ResetCheckboxesCarsTableAction t = new ResetCheckboxesCarsTableAction(carsTableModel);
        Assert.assertNotNull("exists",t);
    }

    // private final static Logger log = LoggerFactory.getLogger(ResetCheckboxesCarsTableActionTest.class);

}
