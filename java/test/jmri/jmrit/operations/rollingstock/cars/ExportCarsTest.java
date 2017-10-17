package jmri.jmrit.operations.rollingstock.cars;

import java.util.List;
import jmri.InstanceManager;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class ExportCarsTest {

    @Test
    public void testCTor() {
        CarManager manager = InstanceManager.getDefault(CarManager.class);
        List<Car> carList = manager.getByIdList();
        ExportCars t = new ExportCars(carList);
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

    // private final static Logger log = LoggerFactory.getLogger(ExportCarsTest.class);
}
