package jmri.jmrit.operations.locations;

import java.util.List;
import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.OpsPropertyChangeListener;
import jmri.jmrit.operations.rollingstock.cars.Car;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the Operations Pool class Last manually cross-checked on ?????
 *
 * Still to do: ?????????????? Nees to be updated for Pool class ScheduleItem:
 * XML read/write Schedule: Register, List, XML read/write Track:
 * AcceptsDropTrain, AcceptsDropRoute Track: AcceptsPickupTrain,
 * AcceptsPickupRoute Track: CheckScheduleValid Track: XML read/write Location:
 * Track support <-- I am here Location: XML read/write
 *
 * @author Gregory Madsen Copyright (C) 2012, based on OperationsLocationTest
 * class
 */
public class OperationsPoolTest extends OperationsTestCase {

    Pool p = null;
    Track t1 = null;
    Track t2 = null;

	// test Pool Class (part one)
    // test Pool creation in different ways
    // public void testCreateDefault()
    // {
    // // There is currently no default constructor...
    // }
    @Test
    public void testCreateNormal() {
        p = new Pool("Id1", "Name1");
        Assert.assertEquals("Pool Id", "Id1", p.getId());
        Assert.assertEquals("Pool Name", "Name1", p.getName());
        Assert.assertEquals("Pool Size", 0, p.getSize());

    }

    // test property set
    @Test
    public void testProperties() {
        p = new Pool("Id2", "Name2");

        p.setName("NewName");
        Assert.assertEquals("setName", "NewName", p.getName());
    }

    // test PropertyChanged event
    @Test
    public void testPropertyChangedEvent() {
        p = new Pool("Id3", "Name3");
        // Boolean fired = false;

        OpsPropertyChangeListener opcl = new OpsPropertyChangeListener();

        p.addPropertyChangeListener(opcl);

        // Set with different name, should fire
        p.setName("NewName");
        Assert.assertTrue("Name changed event fired", opcl.isFired());
        Assert.assertEquals("Name changed name", "Name", opcl.getEvent().getPropertyName());

        opcl.reset();

        // Set with same name, should NOT fire.
        p.setName("NewName");
        Assert.assertFalse("Name changed event not fired", opcl.isFired());
        Assert.assertNull("No event", opcl.getEvent());
    }

    // test methods
    @Test
    public void testToString() {
        p = new Pool("Id4", "Name4");

        Assert.assertEquals("ToString()", "Name4", p.toString());
    }

    @Test
    public void testAddTrack() {
        t1 = new Track("Id1", "Track1", "Type1", null);
        p = new Pool("P1", "Pool1");
        OpsPropertyChangeListener opcl = new OpsPropertyChangeListener();
        p.addPropertyChangeListener(opcl);

        p.add(t1);
        Assert.assertEquals("Added one track", 1, p.getSize());
        Assert.assertTrue("Was fired", opcl.isFired());
        Assert.assertEquals("List changed", "poolListChange", opcl.getEvent().getPropertyName());

        // Try to add it again
        opcl.reset();
        p.add(t1);

        Assert.assertEquals("Added same track", 1, p.getSize());
        Assert.assertFalse("Was not fired", opcl.isFired());
        Assert.assertNull("No event", opcl.getEvent());
    }

    @Test
    public void testRemoveTrack() {
        t1 = new Track("Id1", "Track1", "Type1", null);
        p = new Pool("P1", "Pool1");
        OpsPropertyChangeListener opcl = new OpsPropertyChangeListener();
        p.addPropertyChangeListener(opcl);

        p.add(t1);
        opcl.reset();

        p.remove(t1);

        Assert.assertEquals("Removed one track", 0, p.getSize());
        Assert.assertTrue("Was fired", opcl.isFired());
        Assert.assertEquals("List changed", "poolListChange", opcl.getEvent().getPropertyName());

        // Try to remove it when it is not there.
        opcl.reset();
        p.remove(t1);

        Assert.assertEquals("Removed same track", 0, p.getSize());
        Assert.assertFalse("Was not fired", opcl.isFired());
        Assert.assertNull("No event", opcl.getEvent());
    }

    private Pool Create2TrackPool() {
        // Helper method to avoid duplicate code.
        // This could be generalized if need be.
        t1 = new Track("Id1", "Track1", "Type1", null);
        t1.setLength(100);
        t1.setMinimumLength(50);

        t2 = new Track("Id2", "Track2", "Type2", null);
        t2.setLength(120);
        t2.setMinimumLength(40);

        p = new Pool("P1", "Pool1");

        p.add(t1);
        p.add(t2);

        return p;
    }

    @Test
    public void testGetTracks() {
        Create2TrackPool();

        Assert.assertEquals("Added two tracks", 2, p.getTracks().size());

        Assert.assertEquals("First track", t1, p.getTracks().get(0));
        Assert.assertEquals("Second track", t2, p.getTracks().get(1));
    }

    @Test
    public void testTrackListIsCopy() {
        // Make sure the collection is not the internal one.
        Create2TrackPool();

        List<Track> lt = p.getTracks();
        Assert.assertEquals("Returned size", 2, lt.size());

        // Clear the returned list
        lt.clear();

        Assert.assertEquals("Original list size is unchanged", 2, p.getTracks().size());
    }

    @Test
    public void testLengthenTrack1() {
		// Track 1 is 100 feet, minimum 50, available 50
        // Track 2 is 120 feet, minimum 40, available 80

        // Request Track 1 to be 120 feet, which will take 20 feet from Track 2
        Create2TrackPool();

        Boolean ok = p.requestTrackLength(t1, 120);
        Assert.assertTrue("Requested Track 1 120", ok);

        Assert.assertEquals("Length 1", 120, t1.getLength());
        Assert.assertEquals("Length 2", 100, t2.getLength());
    }

    @Test
    public void testLengthenTrack1Maximum() {
		// Track 1 is 100 feet, minimum 50, available 50
        // Track 2 is 120 feet, minimum 40, available 80

        // Request Track 1 to be 180 feet, which will take all available 80 feet
        // from Track 2
        Create2TrackPool();

        Boolean ok = p.requestTrackLength(t1, 180);
        Assert.assertTrue("Requested Track 1 180", ok);

        Assert.assertEquals("Length 1", 180, t1.getLength());
        Assert.assertEquals("Length 2", 40, t2.getLength());
    }

    @Test
    public void testLengthenTrack1TooLong() {
		// Track 1 is 100 feet, minimum 50, available 50
        // Track 2 is 120 feet, minimum 40, available 80

        // Request Track 1 to be 181 feet, which will fail as there is only 80
        // feet available from Track 2
        Create2TrackPool();

        Boolean ok = p.requestTrackLength(t1, 181);
        Assert.assertFalse("Requested Track 1 181", ok);

        // Assert.assertEquals("Length 1", 180, t1.getLength());
        // Assert.assertEquals("Length 2", 40, t2.getLength());
    }

    @Test
    public void testShortenTrack1() {
		// Track 1 is 100 feet, minimum 50, available 50
        // Track 2 is 120 feet, minimum 40, available 80

        // Request Track 1 to be 80 feet, which will give 20 feet to Track 2
        Create2TrackPool();

        Boolean ok = p.requestTrackLength(t1, 80);
        Assert.assertTrue("Requested Track 1 80", ok);

        Assert.assertEquals("Length 1", 80, t1.getLength());
        Assert.assertEquals("Length 2", 140, t2.getLength());
    }

    @Test
    public void testShortenTrack1BelowMinimum() {
		// Track 1 is 100 feet, minimum 50, available 50
        // Track 2 is 120 feet, minimum 40, available 80

        // Request Track 1 to be 20 feet, which will give 80 feet to Track 2
        Create2TrackPool();

        Boolean ok = p.requestTrackLength(t1, 20);
        Assert.assertTrue("Requested Track 1 20", ok);

        Assert.assertEquals("Length 1", 20, t1.getLength());
        Assert.assertEquals("Length 2", 200, t2.getLength());
    }

    @Test
    public void testTrackPools() {
        LocationManager locMan = new LocationManager();
        Location l = locMan.newLocation("TestTrackPoolsLocation");
        Track t1 = l.addTrack("Yard 1", Track.YARD);
        Track t2 = l.addTrack("Yard 2", Track.YARD);
        Track t3 = l.addTrack("Siding 1", Track.SPUR);
        Track t4 = l.addTrack("Siding 2", Track.SPUR);
        Track t5 = l.addTrack("Interchange 1", Track.INTERCHANGE);
        Track t6 = l.addTrack("Interchange 2", Track.INTERCHANGE);
        Track t7 = l.addTrack("Interchange 3", Track.INTERCHANGE);

        // create two pools
        Pool pool1 = l.addPool("Pool One");
        Pool pool2 = l.addPool("Pool Two");

        t1.setPool(pool1);
        t3.setPool(pool1);
        t7.setPool(pool1);

        t2.setPool(pool2);
        t4.setPool(pool2);
        t5.setPool(pool2);
        t6.setPool(pool2);

        // only give one track in the pool some length
        t3.setLength(100);
        t5.setLength(200);

        // set minimums
        t2.setMinimumLength(50);
        t5.setMinimumLength(100);

        Car c1 = new Car("C", "1");
        c1.setLength("40");
        c1.setTypeName("Boxcar"); // location and track defaults should support type Boxcar

        Car c2 = new Car("C", "2");
        c2.setLength("25");
        c2.setTypeName("Boxcar"); // location and track defaults should support type Boxcar

        Car c3 = new Car("C", "3");
        c3.setLength("32");
        c3.setTypeName("Boxcar"); // location and track defaults should support type Boxcar

        // now place cars and see if track lengths adjust correctly
        Assert.assertEquals("Place c1", Track.OKAY, c1.setLocation(l, t1));
        Assert.assertEquals("track length", 40 + Car.COUPLERS, t1.getLength());
        Assert.assertEquals("track length", 100 - (40 + Car.COUPLERS), t3.getLength());
        Assert.assertEquals("track length", 0, t7.getLength());

        Assert.assertEquals("Place c2", Track.OKAY, c2.setLocation(l, t7));
        Assert.assertEquals("track length", 40 + Car.COUPLERS, t1.getLength());
        Assert.assertEquals("track length", 25 + Car.COUPLERS, t7.getLength());
        Assert.assertEquals("track length", 100 - (40 + Car.COUPLERS) - (25 + Car.COUPLERS), t3.getLength());

        // not able to place c3, not enough available track length
        String status = c3.setLocation(l, t1);
        // Assert.assertEquals("Place c3", Track.LENGTH + " "+(32+Car.COUPLER)+" " +
        // Setup.getLengthUnit().toLowerCase(), c3.setLocation(l, t1));
        Assert.assertTrue("Length issue", status.startsWith(Track.LENGTH));
        Assert.assertEquals("track length", 40 + Car.COUPLERS + 100 - (40 + Car.COUPLERS) - (25 + Car.COUPLERS), t1
                .getLength());
        Assert.assertEquals("track length", 25 + Car.COUPLERS, t7.getLength());
        Assert.assertEquals("track length", 0, t3.getLength());

        // now test the minimum track length pool feature
        // tracks t2 t4 t5 and t6 in the same pool
        Assert.assertEquals("Place c1", Track.OKAY, c1.setLocation(l, t2));
        Assert.assertEquals("track length", 40 + Car.COUPLERS, t2.getLength());
        Assert.assertEquals("track length", 200 - (40 + Car.COUPLERS), t5.getLength());
        Assert.assertEquals("track length", 0, t4.getLength());
        Assert.assertEquals("track length", 0, t6.getLength());

        Assert.assertEquals("Place c2", Track.OKAY, c2.setLocation(l, t2));
        Assert.assertEquals("track length", 40 + Car.COUPLERS + 25 + Car.COUPLERS, t2.getLength());
        Assert.assertEquals("track length", 200 - (40 + Car.COUPLERS + 25 + Car.COUPLERS), t5.getLength());
        Assert.assertEquals("track length", 0, t4.getLength());
        Assert.assertEquals("track length", 0, t6.getLength());

        // not able to place c3, not enough available track length because of minimum for t5
        status = c3.setLocation(l, t2);
        // Assert.assertEquals("Place c3", Track.LENGTH + " "+(32+Car.COUPLER)+" " +
        // Setup.getLengthUnit().toLowerCase(), c3.setLocation(l, t2));
        Assert.assertTrue("Length issue", status.startsWith(Track.LENGTH));
        Assert.assertEquals("track length", 100, t2.getLength());
        Assert.assertEquals("track length", 100, t5.getLength()); // minimum track length
        Assert.assertEquals("track length", 0, t4.getLength());
        Assert.assertEquals("track length", 0, t6.getLength());

        // now give t6 some length so the set location will work
        t6.setLength(50);

        Assert.assertEquals("Place c3", Track.OKAY, c3.setLocation(l, t2));
        Assert.assertEquals("track length", 40 + Car.COUPLERS + 25 + Car.COUPLERS + 32 + Car.COUPLERS, t2.getLength());
        Assert.assertEquals("track length", 100, t5.getLength()); // minimum track length
        Assert.assertEquals("track length", 0, t4.getLength());
        Assert.assertEquals("track length", 150 - (40 + Car.COUPLERS + 25 + Car.COUPLERS + 32 + Car.COUPLERS), t6
                .getLength());

        // now move the cars on t2 to t4 to test the minimum for t2
        c1.setLocation(null, null); // release the used track. TODO requestTrackLength() should have checked
        // to see if car's track was part of pool

        Assert.assertEquals("Place c1", Track.OKAY, c1.setLocation(l, t4));
        Assert.assertEquals("track length", 25 + Car.COUPLERS + 32 + Car.COUPLERS, t2.getLength());
        Assert.assertEquals("track length", 100, t5.getLength()); // minimum track length
        Assert.assertEquals("track length", 40 + Car.COUPLERS, t4.getLength());
        // 250 feet total track length in pool, 100 minimum
        Assert.assertEquals("track length", (250 - 100) - (40 + Car.COUPLERS + 25 + Car.COUPLERS + 32 + Car.COUPLERS), t6
                .getLength());

        c2.setLocation(null, null);

        // latest code change 4/4/2013 provides enough track length for c2
        Assert.assertEquals("Place c2", Track.OKAY, c2.setLocation(l, t4));
        Assert.assertEquals("track length", 50, t2.getLength()); // minimum track length
        Assert.assertEquals("track length", 100, t5.getLength()); // minimum track length
        Assert.assertEquals("track length", 25 + Car.COUPLERS + 40 + Car.COUPLERS, t4.getLength());
        // 250 feet total track length in pool, 150 minimum
        Assert.assertEquals("track length", (250 - 150) - (40 + Car.COUPLERS + 25 + Car.COUPLERS), t6.getLength());
    }

    @Override
    @Before
    public void setUp() {
        super.setUp();
        InstanceManager.getDefault(jmri.jmrit.operations.rollingstock.cars.CarTypes.class).addName("Boxcar");
    }
}
