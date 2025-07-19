package jmri.jmrit.operations.rollingstock.cars.tools;

import java.awt.GraphicsEnvironment;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.Assume;

import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.rollingstock.cars.gui.CarSetFrame;


/**
 *
 * @author Daniel Boudreau Copyright (C) 2023
 */
public class CarRoutingReportActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        CarSetFrame f = new CarSetFrame();
        CarRoutingReportAction t = new CarRoutingReportAction(f, false);
        Assert.assertNotNull("exists",t);
    }
}
