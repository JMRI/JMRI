package jmri.jmrit.operations.rollingstock;

import java.io.File;
import java.util.Locale;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.jmrit.operations.setup.Setup;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests for the Operations Logger class
 *
 * @author	Dan Boudreau Copyright (C) 2010
 *
 */
public class OperationsLoggerTest extends OperationsTestCase {

    // test creation
    public void testCreate() {
        // load a car
        CarManager manager = CarManager.instance();
        Car c1 = manager.newCar("CP", "1");
        c1.setTypeName("Boxcar");
        c1.setLength("40");
        // turn on logging
        Setup.setCarLoggerEnabled(true);
        // log is created after a car is placed
        File file = new File(RollingStockLogger.instance().getFullLoggerFileName());
        Assert.assertFalse("file exists", file.exists());
        // place car
        Location l1 = new Location("id1", "Logger location B");
        Track l1t1 = l1.addTrack("Logger track A", Track.SPUR);
        l1t1.setLength(100);
        Assert.assertEquals("place c1", Track.OKAY, c1.setLocation(l1, l1t1));

        // confirm creation of directory
        File dir = new File(RollingStockLogger.instance().getDirectoryName());
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
    protected void setUp() throws Exception {
        super.setUp();
    }

    public OperationsLoggerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", OperationsLoggerTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(OperationsLoggerTest.class);
        return suite;
    }

    Locale defaultLocale = Locale.getDefault();

    // The minimal setup for log4J
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
}
