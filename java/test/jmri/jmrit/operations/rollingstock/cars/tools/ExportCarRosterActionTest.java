package jmri.jmrit.operations.rollingstock.cars.tools;

import java.awt.GraphicsEnvironment;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.Assume;

import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.rollingstock.cars.CarsTableFrame;
import jmri.util.JUnitOperationsUtil;
import jmri.util.JUnitUtil;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class ExportCarRosterActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        CarsTableFrame ctf = new CarsTableFrame(true, null, null);
        ExportCarRosterAction t = new ExportCarRosterAction(ctf);
        Assert.assertNotNull("exists",t);
        JUnitUtil.dispose(ctf);
        JUnitOperationsUtil.checkOperationsShutDownTask();
    }

    // private final static Logger log = LoggerFactory.getLogger(ExportCarRosterActionTest.class);

}
