package jmri.jmrit.operations.rollingstock.cars.tools;

import java.awt.GraphicsEnvironment;
import jmri.jmrit.operations.rollingstock.cars.CarsTableFrame;
import jmri.jmrit.operations.rollingstock.cars.tools.ExportCarRosterAction;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class ExportCarRosterActionTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        CarsTableFrame ctf = new CarsTableFrame(true, null, null);
        ExportCarRosterAction t = new ExportCarRosterAction("Test Action",ctf);
        Assert.assertNotNull("exists",t);
        JUnitUtil.dispose(ctf);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(ExportCarRosterActionTest.class);

}
