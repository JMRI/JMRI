package jmri.jmrit.operations.trains;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarManager;

/**
 * Tests for the TrainBuilderBase class
 *
 * @author Daniel Boudreau Copyright (C) 2021
 */
public class TrainBuilderBaseTest extends OperationsTestCase {
    
    private LocationManager lmanager;
    private CarManager cmanager;
    TrainBuilderBase tbb;
    
    // NOTE: this test uses reflection to test a protected method.
    java.lang.reflect.Method sortCarsOnFifoLifoTracks = null;

    @Test
    public void testCtor() {
        TrainBuilderBase tb = new TrainBuilderBase();
        Assert.assertNotNull("Train Builder Constructor", tb);
    }
    
    // test protected method sortCarsOnFifoLifoTracks
    @Test
    public void testCarOrderNORMAL() {
        String carTypes[] = Bundle.getMessage("carTypeNames").split(",");
        // start by creating a track
        Location A = lmanager.newLocation("A");
        Track interchangeTrack = A.addTrack("track", Track.INTERCHANGE);
        interchangeTrack.setLength(1000);
        interchangeTrack.setServiceOrder(Track.NORMAL);

        java.util.Calendar cal = java.util.Calendar.getInstance();
        java.util.Date date = cal.getTime();

        // place 3 cars on the track.
        Car a = cmanager.newRS("ABC", "123");
        a.setTypeName(carTypes[1]);
        a.setLength("50");
        a.setLastDate(date);
        a.setLocation(A, interchangeTrack);

        cal.add(java.util.Calendar.MINUTE, 2);
        date = cal.getTime();

        Car b = cmanager.newRS("ABC", "321");
        b.setTypeName(carTypes[1]);
        b.setLength("50");
        b.setLastDate(date);
        b.setLocation(A, interchangeTrack);

        cal.add(java.util.Calendar.MINUTE, 2);
        date = cal.getTime();

        Car c = cmanager.newRS("ABC", "111");
        c.setTypeName(carTypes[1]);
        c.setLength("50");
        c.setLastDate(date);
        c.setLocation(A, interchangeTrack);

        // and set the car list up.
        tbb._carList = new java.util.ArrayList<>();
        tbb._carList.add(a);
        tbb._carList.add(b);
        tbb._carList.add(c);

        try {
            // with Track.NORMAL order, the car you ask for is the
            // car you get.
            sortCarsOnFifoLifoTracks.invoke(tbb);
            Assert.assertEquals("NORMAL Order, 123 first", a, tbb._carList.get(0));
            Assert.assertEquals("NORMAL Order, 321 second", b, tbb._carList.get(1));
            Assert.assertEquals("NORMAL Order, 111 last", c, tbb._carList.get(2));
        } catch (java.lang.IllegalAccessException iae) {
            Assert.fail("Could not access method getCarOrder in TrackBuilder class");
        } catch (java.lang.reflect.InvocationTargetException ite) {
            Throwable cause = ite.getCause();
            Assert.fail("getCarOrder  executon failed reason: " + cause.getMessage());
        }
    }

    @Test
    public void testCarOrderFIFO() {
        String carTypes[] = Bundle.getMessage("carTypeNames").split(",");
        // start by creating a track
        Location A = lmanager.newLocation("A");
        Track interchangeTrack = A.addTrack("track", Track.INTERCHANGE);
        interchangeTrack.setLength(1000);
        interchangeTrack.setServiceOrder(Track.FIFO);

        java.util.Calendar cal = java.util.Calendar.getInstance();
        java.util.Date date = cal.getTime();

        // and placing 3 cars on the track.
        Car a = cmanager.newRS("ABC", "123");
        a.setTypeName(carTypes[1]);
        a.setLength("50");
        a.setLastDate(date);
        a.setLocation(A, interchangeTrack);

        cal.add(java.util.Calendar.MINUTE, 2);
        date = cal.getTime();

        Car b = cmanager.newRS("ABC", "321");
        b.setTypeName(carTypes[1]);
        b.setLength("50");
        b.setLastDate(date);
        b.setLocation(A, interchangeTrack);

        cal.add(java.util.Calendar.MINUTE, 2);
        date = cal.getTime();

        Car c = cmanager.newRS("ABC", "111");
        c.setTypeName(carTypes[1]);
        c.setLength("50");
        c.setLastDate(date);
        c.setLocation(A, interchangeTrack);

        // and set the car list up.
        tbb._carList = new java.util.ArrayList<>();
        tbb._carList.add(c);
        tbb._carList.add(a);
        tbb._carList.add(b);

        try {
            // FIFO
            sortCarsOnFifoLifoTracks.invoke(tbb);
            Assert.assertEquals("FIFO Order, 123 first", a, tbb._carList.get(0));
            Assert.assertEquals("FIFO Order, 321 second", b, tbb._carList.get(1));
            Assert.assertEquals("FIFO Order, 111 last", c, tbb._carList.get(2));
        } catch (java.lang.IllegalAccessException iae) {
            Assert.fail("Could not access method getCarOrder in TrackBuilder class");
        } catch (java.lang.reflect.InvocationTargetException ite) {
            Throwable cause = ite.getCause();
            Assert.fail("getCarOrder  executon failed reason: " + cause.getMessage());
        }
    }

    @Test
    public void testCarOrderLIFO() {
        String carTypes[] = Bundle.getMessage("carTypeNames").split(",");
        // start by creating a track
        Location A = lmanager.newLocation("A");
        Track interchangeTrack = A.addTrack("track", Track.INTERCHANGE);
        interchangeTrack.setLength(1000);
        interchangeTrack.setServiceOrder(Track.LIFO);

        java.util.Calendar cal = java.util.Calendar.getInstance();
        java.util.Date date = cal.getTime();

        // and placing 3 cars on the track.
        Car a = cmanager.newRS("ABC", "123");
        a.setTypeName(carTypes[1]);
        a.setLength("50");
        a.setLastDate(date);
        a.setLocation(A, interchangeTrack);

        cal.add(java.util.Calendar.MINUTE, 2);
        date = cal.getTime();

        Car b = cmanager.newRS("ABC", "321");
        b.setTypeName(carTypes[1]);
        b.setLength("50");
        b.setLastDate(date);
        b.setLocation(A, interchangeTrack);

        cal.add(java.util.Calendar.MINUTE, 2);
        date = cal.getTime();

        Car c = cmanager.newRS("ABC", "111");
        c.setTypeName(carTypes[1]);
        c.setLength("50");
        c.setLastDate(date);
        c.setLocation(A, interchangeTrack);

        // and set the car list up.
        tbb._carList = new java.util.ArrayList<>();
        tbb._carList.add(c);
        tbb._carList.add(a);
        tbb._carList.add(b);

        try {
            // LIFO
            sortCarsOnFifoLifoTracks.invoke(tbb);
            Assert.assertEquals("LIFO Order, 123 first", c, tbb._carList.get(0));
            Assert.assertEquals("LIFO Order, 321 second", b, tbb._carList.get(1));
            Assert.assertEquals("LIFO Order, 111 last", a, tbb._carList.get(2));
        } catch (java.lang.IllegalAccessException iae) {
            Assert.fail("Could not access method getCarOrder in TrackBuilder class");
        } catch (java.lang.reflect.InvocationTargetException ite) {
            Throwable cause = ite.getCause();
            Assert.fail("getCarOrder  executon failed reason: " + cause.getMessage());
        }
    }
    
    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();

        // setup managers
        lmanager = InstanceManager.getDefault(LocationManager.class);
        cmanager = InstanceManager.getDefault(CarManager.class);
        
        tbb = new TrainBuilderBase();
        
        try {
            sortCarsOnFifoLifoTracks = tbb.getClass().getDeclaredMethod("sortCarsOnFifoLifoTracks");
        } catch (java.lang.NoSuchMethodException nsm) {
            Assert.fail("Could not find method sortCarsOnFifoLifoTracks in TrackBuilder class: ");
        }

        // override the default permissions.
        Assert.assertNotNull(sortCarsOnFifoLifoTracks);
        sortCarsOnFifoLifoTracks.setAccessible(true);
    }
}
