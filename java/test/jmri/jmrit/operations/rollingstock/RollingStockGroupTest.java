package jmri.jmrit.operations.rollingstock;

import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.engines.Engine;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class RollingStockGroupTest {

    @Test
    public void testCTor() {
        RollingStockGroup <Car> t = new RollingStockGroup<Car>("Test");
        Assert.assertNotNull("exists", t);
    }
    
    @Test
    public void testCTor2() {
        RollingStockGroup <Engine> t = new RollingStockGroup<Engine>("Test");
        Assert.assertNotNull("exists", t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(RollingStockGroupTest.class);

}
