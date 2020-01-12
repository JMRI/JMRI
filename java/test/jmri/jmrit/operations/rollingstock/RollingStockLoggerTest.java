package jmri.jmrit.operations.rollingstock;

import java.io.File;
import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.jmrit.operations.rollingstock.cars.CarTypes;
import jmri.jmrit.operations.setup.Setup;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class RollingStockLoggerTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        RollingStockLogger t = new RollingStockLogger();
        Assert.assertNotNull("exists",t);
    }
    
    // test creation
    @Test
    public void testCreate() {
        // load a car
        CarManager manager = InstanceManager.getDefault(CarManager.class);
        Car c1 = manager.newRS("CP", "1");
        CarTypes ct = InstanceManager.getDefault(CarTypes.class);
        ct.addName("Boxcar");
        c1.setTypeName("Boxcar");
        c1.setLength("40");
        // turn on logging
        Setup.setCarLoggerEnabled(true);
        // log is created after a car is placed
        File file = new File(InstanceManager.getDefault(RollingStockLogger.class).getFullLoggerFileName());
        Assert.assertFalse("file exists", file.exists());
        // place car
        Location l1 = new Location("id1", "Logger location B");
        Track l1t1 = l1.addTrack("Logger track A", Track.SPUR);
        l1t1.setLength(100);
        Assert.assertEquals("place c1", Track.OKAY, c1.setLocation(l1, l1t1));

        // confirm creation of directory
        File dir = new File(InstanceManager.getDefault(RollingStockLogger.class).getDirectoryName());
        Assert.assertTrue("directory exists", dir.exists());
        Assert.assertTrue("file exists", file.exists());
    }

    // private final static Logger log = LoggerFactory.getLogger(RollingStockLoggerTest.class);

}
