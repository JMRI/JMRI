package jmri.jmrit.operations.rollingstock;

import java.io.File;
import java.util.Locale;
import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.jmrit.operations.rollingstock.cars.CarTypes;
import jmri.jmrit.operations.setup.Setup;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the Operations Logger class
 *
 * @author	Dan Boudreau Copyright (C) 2010
 *
 */
public class OperationsLoggerTest extends OperationsTestCase {

    // test creation
    @Test
    public void testCreate() {
        // load a car
        CarManager manager = InstanceManager.getDefault(CarManager.class);
        Car c1 = manager.newCar("CP", "1");
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

        // now delete file
        Assert.assertTrue("delete file", file.delete());
        // now delete directory
        dir.delete();
        //Assert.assertTrue("delete directory", dir.delete()); TODO fails on some machines?
        Setup.setCarLoggerEnabled(false);

    }

    // Ensure minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        super.setUp();
    }
    
    Locale defaultLocale = Locale.getDefault();

    // The minimal setup for log4J
    @Override
    @After
    public void tearDown() {
        super.tearDown();
    }
}
