package jmri.jmrit.operations.rollingstock.cars;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import jmri.jmrit.operations.rollingstock.RollingStock;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class ExportCarsTest {

    @Test
    public void testCTor() {
        CarManager manager = CarManager.instance();
        List<RollingStock> carList = manager.getByIdList();
        ExportCars t = new ExportCars(carList);
        Assert.assertNotNull("exists",t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(ExportCarsTest.class.getName());

}
