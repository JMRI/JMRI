package jmri.jmrit.operations.rollingstock;

import java.awt.GraphicsEnvironment;
import jmri.jmrit.operations.rollingstock.cars.Car;
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
public class RollingStockSetFrameTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        RollingStockSetFrame <Car> t = new RollingStockSetFrame<Car>();
        Assert.assertNotNull("exists",t);
        JUnitUtil.dispose(t);
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

    // private final static Logger log = LoggerFactory.getLogger(RollingStockSetFrameTest.class);

}
