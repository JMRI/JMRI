package jmri.jmrit.operations.rollingstock.cars.tools;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.rollingstock.cars.CarSetFrame;

/**
 *
 * @author Daniel Boudreau Copyright (C) 2023
 */
public class CarRoutingReportActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        CarSetFrame f = new CarSetFrame();
        CarRoutingReportAction t = new CarRoutingReportAction(f, false);
        Assert.assertNotNull("exists",t);
    }
}
