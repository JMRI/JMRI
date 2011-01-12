//OperationsCarRouterTest.java

package jmri.jmrit.operations.router;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.Schedule;
import jmri.jmrit.operations.locations.ScheduleItem;
import jmri.jmrit.operations.locations.ScheduleManager;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.jmrit.operations.rollingstock.cars.CarTypes;
import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.setup.Setup;

import jmri.jmrit.operations.routes.RouteManager;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.jmrit.operations.trains.Train;
import java.util.List;
import java.util.Locale;

/**
 * Tests for the Operations Router class
 *  
 * @author	Daniel Boudreau Copyright (C) 2010
 * @version $Revision: 1.10 $
 */
public class OperationsCarRouterTest extends TestCase {
	
	private final int DIRECTION_ALL = Location.EAST+Location.WEST+Location.NORTH+Location.SOUTH;
	
	public void testCarRoutingDefaults(){
		Assert.assertTrue("Default car routing true", Setup.isCarRoutingEnabled());
		Assert.assertFalse("Default routing through staging", Setup.isCarRoutingViaStagingEnabled());
	}
	
	/** Test car routing.  First set of tests confirm proper operation of just one location.
	 * The next set of tests confirms operation using one train and two locations.
	 * When this test was written, routing up to 5 trains and 6 locations was supported.
	 * 
	 */
	public void testCarRouting(){
		// Need to clear out TrainManager global variables
		TrainManager.instance().dispose();
		LocationManager.instance().dispose();
		// now load up the managers
		TrainManager tmanager = TrainManager.instance();
		RouteManager rmanager = RouteManager.instance();
		LocationManager lmanager = LocationManager.instance();
		Router router = Router.instance();
		CarManager cmanager = CarManager.instance();
		CarTypes ct = CarTypes.instance();
		
		// register the car and engine types used
		ct.addName("Boxcar");
		ct.addName("Caboose");
		ct.addName("Flat");
		
		// create 6 locations and tracks
		Location Acton = lmanager.newLocation("Acton MA");
		Assert.assertEquals("Location 1 Name", "Acton MA", Acton.getName());
		Assert.assertEquals("Location 1 Initial Length", 0, Acton.getLength());

		Track AS1 = Acton.addTrack("Acton Siding 1", Track.SIDING);
		AS1.setLength(300);
		Assert.assertEquals("Location AS1 Name", "Acton Siding 1", AS1.getName());
		Assert.assertEquals("Location AS1 Length", 300, AS1.getLength());
		
		Track AS2 = Acton.addTrack("Acton Siding 2", Track.SIDING);
		AS2.setLength(300);
		Assert.assertEquals("Location AS2 Name", "Acton Siding 2", AS2.getName());
		Assert.assertEquals("Location AS2 Length", 300, AS2.getLength());
		
		Track AY = Acton.addTrack("Acton Yard", Track.YARD);
		AY.setLength(400);
		Assert.assertEquals("Location AY Name", "Acton Yard", AY.getName());
		Assert.assertEquals("Location AY Length", 400, AY.getLength());
		
		Track AI = Acton.addTrack("Acton Interchange", Track.INTERCHANGE);
		AI.setLength(500);
		Assert.assertEquals("Track AI Name", "Acton Interchange", AI.getName());
		Assert.assertEquals("Track AI Length", 500, AI.getLength());
		Assert.assertEquals("Track AI Train Directions", DIRECTION_ALL, AI.getTrainDirections());
		
		Location Bedford = lmanager.newLocation("Bedford MA");
		Assert.assertEquals("Location 1 Name", "Bedford MA", Bedford.getName());
		Assert.assertEquals("Location 1 Initial Length", 0, Bedford.getLength());

		Track BS1 = Bedford.addTrack("Bedford Siding 1", Track.SIDING);
		BS1.setLength(300);
		Assert.assertEquals("Location BS1 Name", "Bedford Siding 1", BS1.getName());
		Assert.assertEquals("Location BS1 Length", 300, BS1.getLength());
		
		Track BS2 = Bedford.addTrack("Bedford Siding 2", Track.SIDING);
		BS2.setLength(300);
		Assert.assertEquals("Location BS2 Name", "Bedford Siding 2", BS2.getName());
		Assert.assertEquals("Location BS2 Length", 300, BS2.getLength());
		
		Track BY = Bedford.addTrack("Bedford Yard", Track.YARD);
		BY.setLength(400);
		Assert.assertEquals("Location BY Name", "Bedford Yard", BY.getName());
		Assert.assertEquals("Location BY Length", 400, BY.getLength());
		
		Track BI = Bedford.addTrack("Bedford Interchange", Track.INTERCHANGE);
		BI.setLength(500);
		Assert.assertEquals("Track BI Name", "Bedford Interchange", BI.getName());
		Assert.assertEquals("Track BI Length", 500, BI.getLength());
		
		Location Clinton = lmanager.newLocation("Clinton MA");
		Assert.assertEquals("Location 1 Name", "Clinton MA", Clinton.getName());
		Assert.assertEquals("Location 1 Initial Length", 0, Clinton.getLength());

		Track CS1 = Clinton.addTrack("Clinton Siding 1", Track.SIDING);
		CS1.setLength(300);
		Assert.assertEquals("Location CS1 Name", "Clinton Siding 1", CS1.getName());
		Assert.assertEquals("Location CS1 Length", 300, CS1.getLength());
		
		Track CS2 = Clinton.addTrack("Clinton Siding 2", Track.SIDING);
		CS2.setLength(300);
		Assert.assertEquals("Location CS2 Name", "Clinton Siding 2", CS2.getName());
		Assert.assertEquals("Location CS2 Length", 300, BS2.getLength());
		
		Track CY = Clinton.addTrack("Clinton Yard", Track.YARD);
		CY.setLength(400);
		Assert.assertEquals("Location CY Name", "Clinton Yard", CY.getName());
		Assert.assertEquals("Location CY Length", 400, CY.getLength());
		
		Track CI = Clinton.addTrack("Clinton Interchange", Track.INTERCHANGE);
		CI.setLength(500);
		Assert.assertEquals("Track CI Name", "Clinton Interchange", CI.getName());
		Assert.assertEquals("Track CI Length", 500, CI.getLength());
		
		Location Danbury = lmanager.newLocation("Danbury MA");
		Track DS1 = Danbury.addTrack("Danbury Siding 1", Track.SIDING);
		DS1.setLength(300);
		Track DS2 = Danbury.addTrack("Danbury Siding 2", Track.SIDING);
		DS2.setLength(300);		
		Track DY = Danbury.addTrack("Danbury Yard", Track.YARD);
		DY.setLength(400);		
		Track DI = Danbury.addTrack("Danbury Interchange", Track.INTERCHANGE);
		DI.setLength(500);
		
		Location Essex = lmanager.newLocation("Essex MA");
		Track ES1 = Essex.addTrack("Essex Siding 1", Track.SIDING);
		ES1.setLength(300);	
		Track ES2 = Essex.addTrack("Essex Siding 2", Track.SIDING);
		ES2.setLength(300);		
		Track EY = Essex.addTrack("Essex Yard", Track.YARD);
		EY.setLength(400);		
		Track EI = Essex.addTrack("Essex Interchange", Track.INTERCHANGE);
		EI.setLength(500);
		
		Location Foxboro = lmanager.newLocation("Foxboro MA");
		Track FS1 = Foxboro.addTrack("Foxboro Siding 1", Track.SIDING);
		FS1.setLength(300);	
		Track FS2 = Foxboro.addTrack("Foxboro Siding 2", Track.SIDING);
		FS2.setLength(300);		
		Track FY = Foxboro.addTrack("Foxboro Yard", Track.YARD);
		FY.setLength(400);		
		Track FI = Foxboro.addTrack("Foxboro Interchange", Track.INTERCHANGE);
		FI.setLength(500);
				
		// create 2 cars
		Car c3 = cmanager.newCar("BA", "3");
		c3.setType("Boxcar");
		c3.setLength("40");
		c3.setOwner("DAB");
		c3.setBuilt("1984");
		Assert.assertEquals("Box Car 3 Length", "40", c3.getLength());
		
		Car c4 = cmanager.newCar("BB", "4");
		c4.setType("Flat");
		c4.setLength("40");
		c4.setOwner("AT");
		c4.setBuilt("1-86");
		Assert.assertEquals("Box Car 4 Length", "40", c4.getLength());
		
		Assert.assertEquals("place car at BI", Car.OKAY, c3.setLocation(Acton, AS1));
		Assert.assertTrue("Try routing no next destination", router.setDestination(c3));
		Assert.assertEquals("Check car's destination", "", c3.getDestinationName());
		
		Assert.assertEquals("place car at Acton", Car.OKAY, c4.setLocation(Acton, AS1));
		Assert.assertTrue("Try routing no next destination", router.setDestination(c4));
		Assert.assertEquals("Check car's destination", "", c4.getDestinationName());
		
		// first try car routing with just one location
		c3.setNextDestination(Acton);
		Assert.assertTrue("Try routing next destination equal to current", router.setDestination(c3));
		Assert.assertEquals("Check car's destination", "", c3.getDestinationName());
		
		// now try with next track not equal to current
		c3.setNextDestination(Acton);
		c3.setNextDestTrack(AS2);
		Assert.assertFalse("Try routing next track not equal to current", router.setDestination(c3));
		Assert.assertEquals("Check car's destination", "", c3.getDestinationName());
		
		// now try with next track equal to current
		c3.setNextDestination(Acton);
		c3.setNextDestTrack(AS1);
		Assert.assertTrue("Try routing next track equal to current", router.setDestination(c3));
		Assert.assertEquals("Check car's destination", "", c3.getDestinationName());
		
		// create a local train servicing Acton
		Train ActonTrain = tmanager.newTrain("Acton Local");
		Route routeA = rmanager.newRoute("A");
		RouteLocation rlA = routeA.addLocation(Acton);
		rlA.setTrainIconX(25);	// set train icon coordinates
		rlA.setTrainIconY(250);
		ActonTrain.setRoute(routeA);
		
		c3.setNextDestination(Acton);
		c3.setNextDestTrack(AS2);
		Assert.assertTrue("Try routing next track with Acton Local", router.setDestination(c3));
		Assert.assertEquals("Check car's destination", "Acton MA", c3.getDestinationName());
		Assert.assertEquals("Check car's destination track", "Acton Siding 2", c3.getDestinationTrackName());
		
		// don't allow train to service boxcars
		ActonTrain.deleteTypeName("Boxcar");
		// and the next destination for the car
		c3.setDestination(null, null);	// clear previous destination
		c3.setNextDestination(Acton);
		c3.setNextDestTrack(AS2);
		Assert.assertFalse("Try routing with train that doesn't service Boxcar", router.setDestination(c3));
		Assert.assertEquals("Check car's destination", "", c3.getDestinationName());
		
		// try the car type Flat
		c4.setDestination(null, null);	// clear previous destination
		c4.setNextDestination(Acton);
		c4.setNextDestTrack(AS2);
		Assert.assertTrue("Try routing with train that service Flat", router.setDestination(c4));
		Assert.assertEquals("Check car's destination", "Acton MA", c4.getDestinationName());

		// now allow Boxcar again
		ActonTrain.addTypeName("Boxcar");
		Assert.assertTrue("Try routing with train that does service Boxcar", router.setDestination(c3));
		Assert.assertEquals("Check car's destination", "Acton MA", c3.getDestinationName());
		
		// don't allow train to service boxcars with road name BA
		ActonTrain.addRoadName("BA");
		ActonTrain.setRoadOption(Train.EXCLUDEROADS);
		// and the next destination for the car
		c3.setDestination(null, null);	// clear previous destination
		c3.setNextDestination(Acton);
		c3.setNextDestTrack(AS2);
		Assert.assertFalse("Try routing with train that doesn't service road name BA", router.setDestination(c3));
		Assert.assertEquals("Check car's destination", "", c3.getDestinationName());
	
		// try the car road name BB
		c4.setDestination(null, null);	// clear previous destination
		c4.setNextDestination(Acton);
		c4.setNextDestTrack(AS2);
		Assert.assertTrue("Try routing with train that services road BB", router.setDestination(c4));
		Assert.assertEquals("Check car's destination", "Acton MA", c4.getDestinationName());

		// now try again but allow road name
		ActonTrain.setRoadOption(Train.ALLROADS);
		Assert.assertTrue("Try routing with train that does service road name BA", router.setDestination(c3));
		Assert.assertEquals("Check car's destination", "Acton MA", c3.getDestinationName());
		
		// don't service cars built before 1985
		ActonTrain.setBuiltStartYear("1985");
		ActonTrain.setBuiltEndYear("2010");
		// and the next destination for the car
		c3.setDestination(null, null);	// clear previous destination
		c3.setNextDestination(Acton);
		c3.setNextDestTrack(AS2);
		Assert.assertFalse("Try routing with train that doesn't service car built before 1985", router.setDestination(c3));
		Assert.assertEquals("Check car's destination", "", c3.getDestinationName());
		
		// try the car built after 1985
		c4.setDestination(null, null);	// clear previous destination
		c4.setNextDestination(Acton);
		c4.setNextDestTrack(AS2);
		Assert.assertTrue("Try routing with train that services car built after 1985", router.setDestination(c4));
		Assert.assertEquals("Check car's destination", "Acton MA", c4.getDestinationName());

		// car was built in 1984 should work
		ActonTrain.setBuiltStartYear("1983");
		Assert.assertTrue("Try routing with train that doesn't service car built before 1983", router.setDestination(c3));
		Assert.assertEquals("Check car's destination", "Acton MA", c3.getDestinationName());
		
		// try car loads
		c3.setLoad("Tools");
		ActonTrain.addLoadName("Tools");
		ActonTrain.setLoadOption(Train.EXCLUDELOADS);
		
		// and the next destination for the car
		c3.setDestination(null, null);	// clear previous destination
		c3.setNextDestination(Acton);
		c3.setNextDestTrack(AS2);
		Assert.assertFalse("Try routing with train that doesn't service load Tools", router.setDestination(c3));
		Assert.assertEquals("Check car's destination", "", c3.getDestinationName());
	
		// try the car load "E"
		c4.setDestination(null, null);	// clear previous destination
		c4.setNextDestination(Acton);
		c4.setNextDestTrack(AS2);
		Assert.assertTrue("Try routing with train that services load E", router.setDestination(c4));
		Assert.assertEquals("Check car's destination", "Acton MA", c4.getDestinationName());
		
		ActonTrain.setLoadOption(Train.ALLLOADS);
		Assert.assertTrue("Try routing with train that that does service load Tools", router.setDestination(c3));
		Assert.assertEquals("Check car's destination", "Acton MA", c3.getDestinationName());

		// now test by modifying the route
		rlA.setCanPickup(false);		
		// and the next destination for the car
		c3.setDestination(null, null);	// clear previous destination
		c3.setNextDestination(Acton);
		c3.setNextDestTrack(AS2);
		Assert.assertFalse("Try routing with train that doesn't pickup cars", router.setDestination(c3));
		Assert.assertEquals("Check car's destination", "", c3.getDestinationName());
		
		rlA.setCanPickup(true);
		Assert.assertTrue("Try routing with train that that can pickup cars", router.setDestination(c3));
		Assert.assertEquals("Check car's destination", "Acton MA", c3.getDestinationName());
		
		rlA.setCanDrop(false);		
		// and the next destination for the car
		c3.setDestination(null, null);	// clear previous destination
		c3.setNextDestination(Acton);
		c3.setNextDestTrack(AS2);
		Assert.assertFalse("Try routing with train that doesn't drop cars", router.setDestination(c3));
		Assert.assertEquals("Check car's destination", "", c3.getDestinationName());
		
		rlA.setCanDrop(true);
		Assert.assertTrue("Try routing with train that that can drop cars", router.setDestination(c3));
		Assert.assertEquals("Check car's destination", "Acton MA", c3.getDestinationName());
		
		rlA.setMaxCarMoves(0);		
		// and the next destination for the car
		c3.setDestination(null, null);	// clear previous destination
		c3.setNextDestination(Acton);
		c3.setNextDestTrack(AS2);
		Assert.assertFalse("Try routing with train that doesn't service location", router.setDestination(c3));
		Assert.assertEquals("Check car's destination", "", c3.getDestinationName());
		
		rlA.setMaxCarMoves(5);
		Assert.assertTrue("Try routing with train that does service location", router.setDestination(c3));
		Assert.assertEquals("Check car's destination", "Acton MA", c3.getDestinationName());
		
		// test train depart direction
		Assert.assertEquals("check default direction", Track.NORTH, rlA.getTrainDirection());
		// set the depart location Acton to service by South bound trains only
		Acton.setTrainDirections(Track.SOUTH);
		
		// and the next destination for the car
		c3.setDestination(null, null);	// clear previous destination
		c3.setNextDestination(Acton);
		c3.setNextDestTrack(AS2);
		Assert.assertTrue("Try routing with local train that departs north, location south", router.setDestination(c3));
		Assert.assertEquals("Check car's destination", "Acton MA", c3.getDestinationName());
		
		Acton.setTrainDirections(Track.NORTH);
		c3.setDestination(null, null);	// clear previous destination
		c3.setNextDestination(Acton);
		c3.setNextDestTrack(AS2);
		Assert.assertTrue("Try routing with local train that departs north, location north", router.setDestination(c3));
		Assert.assertEquals("Check car's destination", "Acton MA", c3.getDestinationName());
		
		// set the depart track Acton to service by local train only
		AS1.setTrainDirections(0);
		
		// and the next destination for the car
		c3.setDestination(null, null);	// clear previous destination
		c3.setNextDestination(Acton);
		c3.setNextDestTrack(AS2);
		Assert.assertTrue("Try routing with local only", router.setDestination(c3));
		Assert.assertEquals("Check car's destination", "Acton MA", c3.getDestinationName());
		
		c3.setDestination(null, null);	// clear previous destination
		c3.setNextDestination(Acton);
		c3.setNextDestTrack(AS2);
		AS1.setTrainDirections(Track.NORTH);
		Assert.assertTrue("Try routing with local train that departs north, track north", router.setDestination(c3));
		Assert.assertEquals("Check car's destination", "Acton MA", c3.getDestinationName());
		
		// test arrival directions
		
		// set the arrival track Acton to service by local trains only
		AS2.setTrainDirections(0);
				
		// and the next destination for the car
		c3.setDestination(null, null);	// clear previous destination
		c3.setNextDestination(Acton);
		c3.setNextDestTrack(AS2);	// now specify the actual track
		Assert.assertTrue("Try routing with local train", router.setDestination(c3));
		Assert.assertEquals("Check car's destination", "Acton MA", c3.getDestinationName());
		
		AS2.setTrainDirections(Track.NORTH);
		c3.setDestination(null, null);	// clear previous destination
		c3.setNextDestination(Acton);
		c3.setNextDestTrack(AS2);	// now specify the actual track
		Assert.assertTrue("Try routing with train that departs north, track north", router.setDestination(c3));
		Assert.assertEquals("Check car's destination", "Acton MA", c3.getDestinationName());
		
		// Two locations one train testing begins
		// set next destination Bedford
		c3.setDestination(null, null);	// clear previous destination
		c3.setNextDestination(Bedford);
		c3.setNextDestTrack(null);
		// should fail no train!
		Assert.assertFalse("Try routing with next destination", router.setDestination(c3));
		// create a train with a route from Acton to Bedford
		Train ActonToBedfordTrain = tmanager.newTrain("Acton to Bedford");
		Route routeAB = rmanager.newRoute("AB");
		RouteLocation rlActon = routeAB.addLocation(Acton);
		RouteLocation rlBedford = routeAB.addLocation(Bedford);
		rlBedford.setTrainIconX(100);	// set train icon coordinates
		rlBedford.setTrainIconY(250);
		ActonToBedfordTrain.setRoute(routeAB);
		
		// should work
		Assert.assertTrue("Try routing with next destination and train", router.setDestination(c3));
		Assert.assertEquals("Check car's destination", "Bedford MA", c3.getDestinationName());
		
		// don't allow train to service boxcars
		ActonToBedfordTrain.deleteTypeName("Boxcar");
		// and the next destination for the car
		c3.setDestination(null, null);	// clear previous destination
		c3.setNextDestination(Bedford);
		Assert.assertFalse("Try routing with train that doesn't service Boxcar", router.setDestination(c3));
		Assert.assertEquals("Check car's destination", "", c3.getDestinationName());
		
		// try the car type Flat
		c4.setDestination(null, null);	// clear previous destination
		c4.setNextDestination(Bedford);
		c4.setNextDestTrack(null);
		Assert.assertTrue("Try routing with train that service Flat", router.setDestination(c4));
		Assert.assertEquals("Check car's destination", "Bedford MA", c4.getDestinationName());

		// now allow Boxcar again
		ActonToBedfordTrain.addTypeName("Boxcar");
		Assert.assertTrue("Try routing with train that does service Boxcar", router.setDestination(c3));
		Assert.assertEquals("Check car's destination", "Bedford MA", c3.getDestinationName());
		
		// don't allow train to service boxcars with road name BA
		ActonToBedfordTrain.addRoadName("BA");
		ActonToBedfordTrain.setRoadOption(Train.EXCLUDEROADS);
		// and the next destination for the car
		c3.setDestination(null, null);	// clear previous destination
		c3.setNextDestination(Bedford);
		Assert.assertFalse("Try routing with train that doesn't service road name BA", router.setDestination(c3));
		Assert.assertEquals("Check car's destination", "", c3.getDestinationName());
	
		// try the car road name BB
		c4.setDestination(null, null);	// clear previous destination
		c4.setNextDestination(Bedford);
		Assert.assertTrue("Try routing with train that services road BB", router.setDestination(c4));
		Assert.assertEquals("Check car's destination", "Bedford MA", c4.getDestinationName());

		// now try again but allow road name
		ActonToBedfordTrain.setRoadOption(Train.ALLROADS);
		Assert.assertTrue("Try routing with train that does service road name BA", router.setDestination(c3));
		Assert.assertEquals("Check car's destination", "Bedford MA", c3.getDestinationName());
		
		// don't service cars built before 1985
		ActonToBedfordTrain.setBuiltStartYear("1985");
		ActonToBedfordTrain.setBuiltEndYear("2010");
		// and the next destination for the car
		c3.setDestination(null, null);	// clear previous destination
		c3.setNextDestination(Bedford);
		Assert.assertFalse("Try routing with train that doesn't service car built before 1985", router.setDestination(c3));
		Assert.assertEquals("Check car's destination", "", c3.getDestinationName());
		
		// try the car built after 1985
		c4.setDestination(null, null);	// clear previous destination
		c4.setNextDestination(Bedford);
		Assert.assertTrue("Try routing with train that services car built after 1985", router.setDestination(c4));
		Assert.assertEquals("Check car's destination", "Bedford MA", c4.getDestinationName());

		// car was built in 1984 should work
		ActonToBedfordTrain.setBuiltStartYear("1983");
		Assert.assertTrue("Try routing with train that doesn't service car built before 1983", router.setDestination(c3));
		Assert.assertEquals("Check car's destination", "Bedford MA", c3.getDestinationName());
		
		// try car loads
		c3.setLoad("Tools");
		ActonToBedfordTrain.addLoadName("Tools");
		ActonToBedfordTrain.setLoadOption(Train.EXCLUDELOADS);
		
		// and the next destination for the car
		c3.setDestination(null, null);	// clear previous destination
		c3.setNextDestination(Bedford);
		Assert.assertFalse("Try routing with train that doesn't service load Tools", router.setDestination(c3));
		Assert.assertEquals("Check car's destination", "", c3.getDestinationName());
	
		// try the car load "E"
		c4.setDestination(null, null);	// clear previous destination
		c4.setNextDestination(Bedford);
		Assert.assertTrue("Try routing with train that services load E", router.setDestination(c4));
		Assert.assertEquals("Check car's destination", "Bedford MA", c4.getDestinationName());
		
		ActonToBedfordTrain.setLoadOption(Train.ALLLOADS);
		Assert.assertTrue("Try routing with train that that does service load Tools", router.setDestination(c3));
		Assert.assertEquals("Check car's destination", "Bedford MA", c3.getDestinationName());

		// now test by modifying the route
		rlActon.setCanPickup(false);		
		// and the next destination for the car
		c3.setDestination(null, null);	// clear previous destination
		c3.setNextDestination(Bedford);
		Assert.assertFalse("Try routing with train that doesn't pickup cars", router.setDestination(c3));
		Assert.assertEquals("Check car's destination", "", c3.getDestinationName());
		
		rlActon.setCanPickup(true);
		Assert.assertTrue("Try routing with train that that can pickup cars", router.setDestination(c3));
		Assert.assertEquals("Check car's destination", "Bedford MA", c3.getDestinationName());
		
		rlBedford.setCanDrop(false);		
		// and the next destination for the car
		c3.setDestination(null, null);	// clear previous destination
		c3.setNextDestination(Bedford);
		Assert.assertFalse("Try routing with train that doesn't drop cars", router.setDestination(c3));
		Assert.assertEquals("Check car's destination", "", c3.getDestinationName());
		
		rlBedford.setCanDrop(true);
		Assert.assertTrue("Try routing with train that that can drop cars", router.setDestination(c3));
		Assert.assertEquals("Check car's destination", "Bedford MA", c3.getDestinationName());
		
		rlBedford.setMaxCarMoves(0);		
		// and the next destination for the car
		c3.setDestination(null, null);	// clear previous destination
		c3.setNextDestination(Bedford);
		Assert.assertFalse("Try routing with train that doesn't service destination", router.setDestination(c3));
		Assert.assertEquals("Check car's destination", "", c3.getDestinationName());
		
		rlBedford.setMaxCarMoves(5);
		Assert.assertTrue("Try routing with train that does service destination", router.setDestination(c3));
		Assert.assertEquals("Check car's destination", "Bedford MA", c3.getDestinationName());

		rlActon.setMaxCarMoves(0);		
		// and the next destination for the car
		c3.setDestination(null, null);	// clear previous destination
		c3.setNextDestination(Bedford);
		Assert.assertFalse("Try routing with train that doesn't service location", router.setDestination(c3));
		Assert.assertEquals("Check car's destination", "", c3.getDestinationName());
		
		rlActon.setMaxCarMoves(5);
		Assert.assertTrue("Try routing with train that does service location", router.setDestination(c3));
		Assert.assertEquals("Check car's destination", "Bedford MA", c3.getDestinationName());
		
		// test train depart direction
		Assert.assertEquals("check default direction", Track.NORTH, rlActon.getTrainDirection());
		// set the depart location Acton to service by South bound trains only
		Acton.setTrainDirections(Track.SOUTH);
		
		// and the next destination for the car
		c3.setDestination(null, null);	// clear previous destination
		c3.setNextDestination(Bedford);

		// remove the Action local by not allowing train to service boxcars
		ActonTrain.deleteTypeName("Boxcar");
		Assert.assertFalse("Try routing with train that departs north, location south", router.setDestination(c3));
		Assert.assertEquals("Check car's destination", "", c3.getDestinationName());
		
		Acton.setTrainDirections(Track.NORTH);
		Assert.assertTrue("Try routing with train that departs north, location north", router.setDestination(c3));
		Assert.assertEquals("Check car's destination", "Bedford MA", c3.getDestinationName());
		
		// set the depart track Acton to service by South bound trains only
		AS1.setTrainDirections(Track.SOUTH);
		
		// and the next destination for the car
		c3.setDestination(null, null);	// clear previous destination
		c3.setNextDestination(Bedford);
		Assert.assertFalse("Try routing with train that departs north, track south", router.setDestination(c3));
		Assert.assertEquals("Check car's destination", "", c3.getDestinationName());
		
		AS1.setTrainDirections(Track.NORTH);
		Assert.assertTrue("Try routing with train that departs north, track north", router.setDestination(c3));
		Assert.assertEquals("Check car's destination", "Bedford MA", c3.getDestinationName());
		
		// test arrival directions
		// set the arrival location Bedford to service by South bound trains only
		Bedford.setTrainDirections(Track.SOUTH);
		
		// and the next destination for the car
		c3.setDestination(null, null);	// clear previous destination
		c3.setNextDestination(Bedford);
		Assert.assertFalse("Try routing with train that arrives north, destination south", router.setDestination(c3));
		Assert.assertEquals("Check car's destination", "", c3.getDestinationName());
		
		Bedford.setTrainDirections(Track.NORTH);
		Assert.assertTrue("Try routing with train that arrives north, destination north", router.setDestination(c3));
		Assert.assertEquals("Check car's destination", "Bedford MA", c3.getDestinationName());
		
		// set the depart track Acton to service by South bound trains only
		BS1.setTrainDirections(Track.SOUTH);
		
		// and the next destination for the car
		c3.setDestination(null, null);	// clear previous destination
		c3.setNextDestination(Bedford);
		Assert.assertTrue("Try routing with train that arrives north, but no next track", router.setDestination(c3));
		Assert.assertEquals("Check car's destination", "Bedford MA", c3.getDestinationName());
		
		c3.setDestination(null, null);	// clear previous destination
		c3.setNextDestination(Bedford); // the next destination for the car
		c3.setNextDestTrack(BS1);	// now specify the actual track
		Assert.assertFalse("Try routing with train that arrives north, now with track", router.setDestination(c3));
		Assert.assertEquals("Check car's destination", "", c3.getDestinationName());
		
		BS1.setTrainDirections(Track.NORTH);
		Assert.assertTrue("Try routing with train that departs north, track north", router.setDestination(c3));
		Assert.assertEquals("Check car's destination", "Bedford MA", c3.getDestinationName());
		
		// One train tests complete. Start two train testing.
		// Force first move to be by local train
		AS1.setTrainDirections(0);
		ActonTrain.addTypeName("Boxcar");	// restore the local
				
		c3.setDestination(null, null);	// clear previous destination
		c3.setNextDestination(Bedford);	// the next destination for the car
		c3.setNextDestTrack(BS1);	// now specify the actual track
		
		Assert.assertTrue("Try routing two trains via interchange", router.setDestination(c3));
		Assert.assertEquals("Check car's destination", "Acton MA", c3.getDestinationName());
		Assert.assertEquals("Check car's destination track", "Acton Interchange", c3.getDestinationTrackName());
		
		// don't allow use of interchange track
		AI.setDropOption(Track.TRAINS);
		
		c3.setDestination(null, null);	// clear previous destination
		c3.setNextDestination(Bedford);	// the next destination for the car
		c3.setNextDestTrack(BS1);	// now specify the actual track
		
		Assert.assertTrue("Try routing two trains via yard", router.setDestination(c3));
		Assert.assertEquals("Check car's destination", "Acton MA", c3.getDestinationName());
		Assert.assertEquals("Check car's destination track", "Acton Yard", c3.getDestinationTrackName());
		
		// don't allow use of interchange track
		AI.setDropOption(Track.ANY);

		// two train testing done!
		// now try up to 5 trains to route car
		
		// set next destination Clinton
		c3.setDestination(null, null);	// clear previous destination
		c3.setNextDestination(Clinton);
		// should fail no train!
		Assert.assertFalse("Try routing with next destination", router.setDestination(c3));

		// create a train with a route from Bedford to Clinton
		Train BedfordToClintonTrain = tmanager.newTrain("Bedford to Clinton");
		Route routeBC = rmanager.newRoute("BC");
		routeBC.addLocation(Bedford);
		RouteLocation rlchelmsford = routeBC.addLocation(Clinton);
		rlchelmsford.setTrainIconX(175);	// set train icon coordinates
		rlchelmsford.setTrainIconY(250);
		BedfordToClintonTrain.setRoute(routeBC);
		
		// should work
		Assert.assertTrue("Try routing with next destination and train", router.setDestination(c3));
		Assert.assertEquals("Check car's destination", "Acton MA", c3.getDestinationName());
		Assert.assertEquals("Check car's destination track", "Acton Interchange", c3.getDestinationTrackName());
		
		// set next destination Danbury
		c3.setDestination(null, null);	// clear previous destination
		c3.setNextDestination(Danbury);
		// should fail no train!
		Assert.assertFalse("Try routing with next destination", router.setDestination(c3));
	
		// create a train with a route from Clinton to Danbury
		Train ClintonToDanburyTrain = tmanager.newTrain("Clinton to Danbury");
		Route routeCD = rmanager.newRoute("CD");
		routeCD.addLocation(Clinton);
		RouteLocation rlDanbury = routeCD.addLocation(Danbury);
		rlDanbury.setTrainIconX(250);	// set train icon coordinates
		rlDanbury.setTrainIconY(250);
		ClintonToDanburyTrain.setRoute(routeCD);
		
		// should work
		Assert.assertTrue("Try routing with next destination and train", router.setDestination(c3));
		Assert.assertEquals("Check car's destination", "Acton MA", c3.getDestinationName());
		Assert.assertEquals("Check car's destination track", "Acton Interchange", c3.getDestinationTrackName());
		
		// set next destination Essex
		c3.setDestination(null, null);	// clear previous destination
		c3.setNextDestination(Essex);
		// should fail no train!
		Assert.assertFalse("Try routing with next destination", router.setDestination(c3));
		
		// create a train with a route from Danbury to Essex
		Train DanburyToEssexTrain = tmanager.newTrain("Danbury to Essex");
		Route routeDE = rmanager.newRoute("DE");
		routeDE.addLocation(Danbury);
		RouteLocation rlEssex = routeDE.addLocation(Essex);
		rlEssex.setTrainIconX(25);	// set train icon coordinates
		rlEssex.setTrainIconY(275);
		DanburyToEssexTrain.setRoute(routeDE);
		
		// should work
		Assert.assertTrue("Try routing with next destination and train", router.setDestination(c3));
		Assert.assertEquals("Check car's destination", "Acton MA", c3.getDestinationName());
		Assert.assertEquals("Check car's destination track", "Acton Interchange", c3.getDestinationTrackName());

		// set next destination Foxboro
		c3.setDestination(null, null);	// clear previous destination
		c3.setNextDestination(Foxboro);
		// should fail no train!
		Assert.assertFalse("Try routing with next destination", router.setDestination(c3));
		
		// create a train with a route from Essex to Foxboro
		Train EssexToFoxboroTrain = tmanager.newTrain("Essex to Foxboro");
		Route routeEF = rmanager.newRoute("EF");
		routeEF.addLocation(Essex);
		RouteLocation rlFoxboro = routeEF.addLocation(Foxboro);
		rlFoxboro.setTrainIconX(100);	// set train icon coordinates
		rlFoxboro.setTrainIconY(275);
		EssexToFoxboroTrain.setRoute(routeEF);
		
		// 6th train should fail!  Only 5 trains supported
		Assert.assertFalse("Try routing with next destination and train", router.setDestination(c3));
		
		// get rid of the local train
		AS1.setTrainDirections(Track.NORTH);
		
		// now should work!
		Assert.assertTrue("Try routing with next destination and train", router.setDestination(c3));
		Assert.assertEquals("Check car's destination", "Bedford MA", c3.getDestinationName());
		Assert.assertEquals("Check car's destination track", "Bedford Interchange", c3.getDestinationTrackName());

		// require local train for next test
		AS1.setTrainDirections(0);
	}
	
	// Using the setup from the previous test
	// Use trains to move cars
	public void testRoutingWithTrains() {
		TrainManager tmanager = TrainManager.instance();
		CarManager cmanager = CarManager.instance();
		LocationManager lmanager = LocationManager.instance();

		List<String> trains = tmanager.getTrainsByNameList();
		Assert.assertEquals("confirm number of trains", 6, trains.size());
		
		Train ActonTrain = tmanager.getTrainByName("Acton Local");
		Train ActonToBedfordTrain = tmanager.getTrainByName("Acton to Bedford");
		Train BedfordToClintonTrain = tmanager.getTrainByName("Bedford to Clinton");
		Train ClintonToDanburyTrain = tmanager.getTrainByName("Clinton to Danbury");
		Train DanburyToEssexTrain = tmanager.getTrainByName("Danbury to Essex");
		Train EssexToFoxboroTrain = tmanager.getTrainByName("Essex to Foxboro");
		
		Car c3 = cmanager.getByRoadAndNumber("BA", "3");
		Car c4 = cmanager.getByRoadAndNumber("BB", "4");
		
		Location Essex = lmanager.getLocationByName("Essex MA");
		Track ES2 = Essex.getTrackByName("Essex Siding 2", Track.SIDING);
		Location Foxboro = lmanager.getLocationByName("Foxboro MA");
		
		Location Gulf = lmanager.newLocation("Gulf");
		
		// confirm cars are in Acton
		Assert.assertEquals("car's location Acton","Acton MA", c3.getLocationName());
		Assert.assertEquals("car's location Acton","Acton Siding 1", c3.getTrackName());
		
		Assert.assertEquals("car's location Acton","Acton MA", c4.getLocationName());
		Assert.assertEquals("car's location Acton","Acton Siding 1", c4.getTrackName());
		
		// set next destination Essex
		c3.setDestination(null, null);	// clear previous destination
		c3.setNextDestination(Essex);
		c3.setNextDestTrack(ES2);
		c3.setLoad("L");
		c3.setReturnWhenEmptyDestination(Foxboro);
		
		// next destination Gulf is not reachable
		c4.setDestination(null, null);	// clear previous destination
		c4.setNextDestination(Gulf);
		
		ActonTrain.build();
		ActonTrain.terminate();
		
		// confirm cars have moved
		Assert.assertEquals("car's location Acton","Acton MA", c3.getLocationName());
		Assert.assertEquals("car's location Acton","Acton Interchange", c3.getTrackName());
		Assert.assertEquals("car's destination","Bedford MA", c3.getDestinationName());
		Assert.assertEquals("car's destination track","Bedford Interchange", c3.getDestinationTrackName());
		Assert.assertEquals("car's load","L", c3.getLoad());
		
		Assert.assertEquals("car's location Acton","Acton MA", c4.getLocationName());
		Assert.assertEquals("car's location Acton","Acton Yard", c4.getTrackName());
		Assert.assertEquals("car's destination","", c4.getDestinationName());
		Assert.assertEquals("car's destination track","", c4.getDestinationTrackName());
		
		ActonToBedfordTrain.build();
		ActonToBedfordTrain.terminate();
		
		// confirm cars have moved
		Assert.assertEquals("car's location Bedford","Bedford MA", c3.getLocationName());
		Assert.assertEquals("car's location Bedford","Bedford Interchange", c3.getTrackName());
		Assert.assertEquals("car's destination","Clinton MA", c3.getDestinationName());
		Assert.assertEquals("car's destination track","Clinton Interchange", c3.getDestinationTrackName());
		Assert.assertEquals("car's load","L", c3.getLoad());
		
		Assert.assertEquals("car's location Bedford","Bedford MA", c4.getLocationName());
		Assert.assertEquals("car's location Bedford","Bedford Siding 2", c4.getTrackName());
		Assert.assertEquals("car's destination","", c4.getDestinationName());
		Assert.assertEquals("car's destination track","", c4.getDestinationTrackName());

		BedfordToClintonTrain.build();
		BedfordToClintonTrain.terminate();
		
		// confirm cars have moved
		Assert.assertEquals("car's location Clinton","Clinton MA", c3.getLocationName());
		Assert.assertEquals("car's location Clinton","Clinton Interchange", c3.getTrackName());
		Assert.assertEquals("car's destination","Danbury MA", c3.getDestinationName());
		Assert.assertEquals("car's destination track","Danbury Interchange", c3.getDestinationTrackName());
		Assert.assertEquals("car's load","L", c3.getLoad());
		
		Assert.assertEquals("car's location Clinton","Clinton MA", c4.getLocationName());
		Assert.assertEquals("car's location Clinton","Clinton Siding 1", c4.getTrackName());
		Assert.assertEquals("car's destination","", c4.getDestinationName());
		Assert.assertEquals("car's destination track","", c4.getDestinationTrackName());
		
		ClintonToDanburyTrain.build();
		ClintonToDanburyTrain.terminate();
		
		// confirm cars have moved
		Assert.assertEquals("car's location Danbury","Danbury MA", c3.getLocationName());
		Assert.assertEquals("car's location Danbury","Danbury Interchange", c3.getTrackName());
		Assert.assertEquals("car's destination","Essex MA", c3.getDestinationName());
		Assert.assertEquals("car's destination track","Essex Siding 2", c3.getDestinationTrackName());
		Assert.assertEquals("car's load","L", c3.getLoad());
		
		Assert.assertEquals("car's location Danbury","Danbury MA", c4.getLocationName());
		Assert.assertEquals("car's location Danbury","Danbury Siding 1", c4.getTrackName());
		Assert.assertEquals("car's destination","", c4.getDestinationName());
		Assert.assertEquals("car's destination track","", c4.getDestinationTrackName());
		
		DanburyToEssexTrain.build();
		DanburyToEssexTrain.terminate();
		
		// confirm cars have moved car has arrived at final destination Essex
		// car when empty must return to Foxboro
		Assert.assertEquals("car's location Essex","Essex MA", c3.getLocationName());
		Assert.assertEquals("car's location Essex","Essex Siding 2", c3.getTrackName());
		Assert.assertEquals("car's destination","Foxboro MA", c3.getDestinationName());
		Assert.assertEquals("car's destination track","", c3.getDestinationTrackName());
		Assert.assertEquals("car's load","E", c3.getLoad());
		
		Assert.assertEquals("car's location Essex","Essex MA", c4.getLocationName());
		Assert.assertEquals("car's location Essex","Essex Siding 1", c4.getTrackName());
		Assert.assertEquals("car's destination","", c4.getDestinationName());
		Assert.assertEquals("car's destination track","", c4.getDestinationTrackName());
		
		EssexToFoxboroTrain.build();
		EssexToFoxboroTrain.terminate();
		
		// confirm cars have moved
		Assert.assertEquals("car's location Foxboro","Foxboro MA", c3.getLocationName());
		Assert.assertEquals("car's location Foxboro","Foxboro Siding 1", c3.getTrackName());
		Assert.assertEquals("car's destination","", c3.getDestinationName());
		Assert.assertEquals("car's destination track","", c3.getDestinationTrackName());
		Assert.assertEquals("car's load","L", c3.getLoad());
		
		Assert.assertEquals("car's location Foxboro","Foxboro MA", c4.getLocationName());
		Assert.assertEquals("car's location Foxboro","Foxboro Siding 2", c4.getTrackName());
		Assert.assertEquals("car's destination","", c4.getDestinationName());
		Assert.assertEquals("car's destination track","", c4.getDestinationTrackName());

	}
	
	/* Using the setup from the previous tests
	* Use trains and schedules to move cars
	* This test creates 4 schedules.  Two cars are used a boxcar
	* and a flat.  They both start with a load of "Food".  They
	* should be routed to the correct schedule that is demanding
	* the car type and load.
	* 
	*/ 
	public void testRoutingWithSchedules() {
		TrainManager tmanager = TrainManager.instance();
		CarManager cmanager = CarManager.instance();
		LocationManager lmanager = LocationManager.instance();

		List<String> trains = tmanager.getTrainsByNameList();
		Assert.assertEquals("confirm number of trains", 6, trains.size());
		
		Train ActonTrain = tmanager.getTrainByName("Acton Local");
		Train ActonToBedfordTrain = tmanager.getTrainByName("Acton to Bedford");
		Train BedfordToClintonTrain = tmanager.getTrainByName("Bedford to Clinton");
		Train ClintonToDanburyTrain = tmanager.getTrainByName("Clinton to Danbury");
		Train DanburyToEssexTrain = tmanager.getTrainByName("Danbury to Essex");
		Train EssexToFoxboroTrain = tmanager.getTrainByName("Essex to Foxboro");
		
		Car c3 = cmanager.getByRoadAndNumber("BA", "3");
		Car c4 = cmanager.getByRoadAndNumber("BB", "4");
		
		Location Acton = lmanager.getLocationByName("Acton MA");
		//Location Bedford = lmanager.getLocationByName("Bedford MA");
		Location Clinton = lmanager.getLocationByName("Clinton MA");
		Location Danbury = lmanager.getLocationByName("Danbury MA");
		Location Essex = lmanager.getLocationByName("Essex MA");
		Location Foxboro = lmanager.getLocationByName("Foxboro MA");
		
		Track AS1 = Acton.getTrackByName("Acton Siding 1", Track.SIDING);
		Track CS1 = Clinton.getTrackByName("Clinton Siding 1", Track.SIDING);
		Track DS1 = Danbury.getTrackByName("Danbury Siding 1", Track.SIDING);
		Track DS2 = Danbury.getTrackByName("Danbury Siding 2", Track.SIDING);
		Track ES1 = Essex.getTrackByName("Essex Siding 1", Track.SIDING);
		Track ES2 = Essex.getTrackByName("Essex Siding 2", Track.SIDING);
		Track FS1 = Foxboro.getTrackByName("Foxboro Siding 1", Track.SIDING);
		
		// create schedules
		ScheduleManager scheduleManager = ScheduleManager.instance();
		Schedule schA = scheduleManager.newSchedule("Schedule A");		
		ScheduleItem schAItem1 = schA.addItem("Boxcar");
		schAItem1.setLoad("Food");
		schAItem1.setShip("Metal");
		schAItem1.setDestination(Danbury);
		schAItem1.setDestinationTrack(DS2);
		
		Schedule schB = scheduleManager.newSchedule("Schedule B");		
		ScheduleItem schBItem1 = schB.addItem("Flat");
		schBItem1.setLoad("Food");
		schBItem1.setShip("Junk");
		schBItem1.setDestination(Foxboro);
		schBItem1.setDestinationTrack(FS1);
		
		Schedule schC = scheduleManager.newSchedule("Schedule C");		
		ScheduleItem schCItem1 = schC.addItem("Boxcar");
		schCItem1.setShip("Screws");
		schCItem1.setDestination(Essex);
		
		Schedule schD = scheduleManager.newSchedule("Schedule D");		
		ScheduleItem schDItem1 = schD.addItem("Boxcar");
		schDItem1.setLoad("Screws");
		schDItem1.setShip("Nails");
		schDItem1.setDestination(Foxboro);
		schDItem1.setDestinationTrack(FS1);
		
		// Add schedule to tracks
		DS1.setScheduleName("Schedule B");
		DS2.setScheduleName("Schedule C");
		ES1.setScheduleName("Schedule D");
		ES2.setScheduleName("Schedule A");
		CS1.setScheduleName("Schedule A");
		
		// bias track
		ES2.setMoves(0);
		
		// place cars
		Assert.assertEquals("Place car", Car.OKAY, c3.setLocation(Acton, AS1));
		Assert.assertEquals("Place car", Car.OKAY, c4.setLocation(Acton, AS1));
		
		// c3 (BA 3) is a Boxcar
		c3.setLoad("Food");
		c3.setNextDestination(null);
		c3.setNextDestTrack(null);
		
		// c4 (BB 4) is a Flat
		c4.setLoad("Food");
		c4.setNextDestination(null);
		c4.setNextDestTrack(null);
		
		// build train
		ActonTrain.build();
		Assert.assertTrue("Acton train built", ActonTrain.isBuilt());
		
		// check car destinations
		Assert.assertEquals("Car BA 3 destination","Acton MA", c3.getDestinationName());
		Assert.assertEquals("Car BA 3 destination track","Acton Interchange", c3.getDestinationTrackName());
		Assert.assertEquals("Car BA 3 next destination","Essex MA", c3.getNextDestinationName());
		Assert.assertEquals("Car BA 3 next destination track","Essex Siding 2", c3.getNextDestTrackName());
		Assert.assertEquals("Car BB 4 destination","Acton MA", c4.getDestinationName());
		Assert.assertEquals("Car BB 4 destination track","Acton Interchange", c4.getDestinationTrackName());
		Assert.assertEquals("Car BB 4 next destination","Danbury MA", c4.getNextDestinationName());
		Assert.assertEquals("Car BB 4 next destination track","Danbury Siding 1", c4.getNextDestTrackName());
		
		ActonTrain.reset();
		// check car destinations after reset
		Assert.assertEquals("Car BA 3 destination","", c3.getDestinationName());
		Assert.assertEquals("Car BA 3 destination track","", c3.getDestinationTrackName());
		Assert.assertEquals("Car BA 3 next destination","Essex MA", c3.getNextDestinationName());
		Assert.assertEquals("Car BA 3 next destination track","Essex Siding 2", c3.getNextDestTrackName());
		Assert.assertEquals("Car BB 4 destination","", c4.getDestinationName());
		Assert.assertEquals("Car BB 4 destination track","", c4.getDestinationTrackName());
		Assert.assertEquals("Car BB 4 next destination","Danbury MA", c4.getNextDestinationName());
		Assert.assertEquals("Car BB 4 next destination track","Danbury Siding 1", c4.getNextDestTrackName());
		
		// bias track
		ES2.setMoves(100);
		
		// build train
		ActonTrain.build();
		Assert.assertTrue("Acton train built", ActonTrain.isBuilt());
		
		// destination should be the same since the next destinations were still active
		Assert.assertEquals("Car BA 3 destination","Acton MA", c3.getDestinationName());
		Assert.assertEquals("Car BA 3 destination track","Acton Interchange", c3.getDestinationTrackName());
		Assert.assertEquals("Car BA 3 next destination","Essex MA", c3.getNextDestinationName());
		Assert.assertEquals("Car BA 3 next destination track","Essex Siding 2", c3.getNextDestTrackName());
		Assert.assertEquals("Car BB 4 destination","Acton MA", c4.getDestinationName());
		Assert.assertEquals("Car BB 4 destination track","Acton Interchange", c4.getDestinationTrackName());
		Assert.assertEquals("Car BB 4 next destination","Danbury MA", c4.getNextDestinationName());
		Assert.assertEquals("Car BB 4 next destination track","Danbury Siding 1", c4.getNextDestTrackName());
		
		ActonTrain.reset();
		
		// now clear the next destinations
		c3.setNextDestination(null);
		c3.setNextDestTrack(null);
		c3.setNextLoad("");
		c4.setNextDestination(null);
		c4.setNextDestTrack(null);
		c4.setNextLoad("");
		
		// build train
		ActonTrain.build();
		Assert.assertTrue("Acton train built", ActonTrain.isBuilt());

		// next destination should change for BA 3
		Assert.assertEquals("Car BA 3 destination","Acton MA", c3.getDestinationName());
		Assert.assertEquals("Car BA 3 destination track","Acton Interchange", c3.getDestinationTrackName());
		Assert.assertEquals("Car BA 3 next destination","Clinton MA", c3.getNextDestinationName());
		Assert.assertEquals("Car BA 3 next destination track","Clinton Siding 1", c3.getNextDestTrackName());
		Assert.assertEquals("Car BB 4 destination","Acton MA", c4.getDestinationName());
		Assert.assertEquals("Car BB 4 destination track","Acton Interchange", c4.getDestinationTrackName());
		Assert.assertEquals("Car BB 4 next destination","Danbury MA", c4.getNextDestinationName());
		Assert.assertEquals("Car BB 4 next destination track","Danbury Siding 1", c4.getNextDestTrackName());
		
		// check next loads
		//Assert.assertEquals("Car BA 3 load","Metal", c3.getNextLoad());
		//Assert.assertEquals("Car BB 4 load","Junk", c4.getNextLoad());
		
		ActonTrain.terminate();
		
		// check destinations
		Assert.assertEquals("Car BA 3 destination","Bedford MA", c3.getDestinationName());
		Assert.assertEquals("Car BA 3 destination track","Bedford Interchange", c3.getDestinationTrackName());
		Assert.assertEquals("Car BA 3 next destination","Clinton MA", c3.getNextDestinationName());
		Assert.assertEquals("Car BA 3 next destination track","Clinton Siding 1", c3.getNextDestTrackName());
		Assert.assertEquals("Car BB 4 destination","Bedford MA", c4.getDestinationName());
		Assert.assertEquals("Car BB 4 destination track","Bedford Interchange", c4.getDestinationTrackName());
		Assert.assertEquals("Car BB 4 next destination","Danbury MA", c4.getNextDestinationName());
		Assert.assertEquals("Car BB 4 next destination track","Danbury Siding 1", c4.getNextDestTrackName());
		
		// check load
		Assert.assertEquals("Car BA 3 load","Food", c3.getLoad());
		Assert.assertEquals("Car BB 4 load","Food", c4.getLoad());
		
		// check next loads
		Assert.assertEquals("Car BA 3 load","", c3.getNextLoad());
		Assert.assertEquals("Car BB 4 load","", c4.getNextLoad());
		
		ActonToBedfordTrain.build();
		ActonToBedfordTrain.terminate();
		
		// check destinations
		Assert.assertEquals("Car BA 3 destination","Clinton MA", c3.getDestinationName());
		Assert.assertEquals("Car BA 3 destination track","Clinton Siding 1", c3.getDestinationTrackName());
		// schedule at Clinton (schedule A) forwards car BA 3 to Danbury, load Metal
		Assert.assertEquals("Car BA 3 next destination","Danbury MA", c3.getNextDestinationName());
		Assert.assertEquals("Car BA 3 next destination track","Danbury Siding 2", c3.getNextDestTrackName());
		Assert.assertEquals("Car BB 4 destination","Clinton MA", c4.getDestinationName());
		Assert.assertEquals("Car BB 4 destination track","Clinton Interchange", c4.getDestinationTrackName());
		Assert.assertEquals("Car BB 4 next destination","Danbury MA", c4.getNextDestinationName());
		Assert.assertEquals("Car BB 4 next destination track","Danbury Siding 1", c4.getNextDestTrackName());
		
		// check load
		Assert.assertEquals("Car BA 3 load","Food", c3.getLoad());
		Assert.assertEquals("Car BB 4 load","Food", c4.getLoad());
		
		// check next loads
		Assert.assertEquals("Car BA 3 load","Metal", c3.getNextLoad());
		Assert.assertEquals("Car BB 4 load","", c4.getNextLoad());
		
		BedfordToClintonTrain.build();
		BedfordToClintonTrain.terminate();
		
		// check destinations
		// schedule at Clinton (schedule A) forwards car BA 3 to Danbury, load Metal
		Assert.assertEquals("Car BA 3 destination","Danbury MA", c3.getDestinationName());
		Assert.assertEquals("Car BA 3 destination track","Danbury Siding 2", c3.getDestinationTrackName());
		// schedule at Danbury (schedule C) forwards car BA 3 to Essex, no track specified, load Screws
		Assert.assertEquals("Car BA 3 next destination","Essex MA", c3.getNextDestinationName());
		Assert.assertEquals("Car BA 3 next destination track","", c3.getNextDestTrackName());
		// schedule at Danbury (schedule B) forwards car BB 4 to Foxboro load Junk
		Assert.assertEquals("Car BB 4 destination","Danbury MA", c4.getDestinationName());
		Assert.assertEquals("Car BB 4 destination track","Danbury Siding 1", c4.getDestinationTrackName());
		Assert.assertEquals("Car BB 4 next destination","Foxboro MA", c4.getNextDestinationName());
		Assert.assertEquals("Car BB 4 next destination track","Foxboro Siding 1", c4.getNextDestTrackName());
		
		// check load
		Assert.assertEquals("Car BA 3 load","Metal", c3.getLoad());
		Assert.assertEquals("Car BB 4 load","Food", c4.getLoad());
		
		// check next loads
		Assert.assertEquals("Car BA 3 load","Screws", c3.getNextLoad());
		Assert.assertEquals("Car BB 4 load","Junk", c4.getNextLoad());
		
		ClintonToDanburyTrain.build();
		ClintonToDanburyTrain.terminate();
		
		// Train has arrived at Danbury, check destinations
		// schedule at Danbury (schedule C) forwards car BA 3 to Essex, no track specified, load Screws
		Assert.assertEquals("Car BA 3 destination","Essex MA", c3.getDestinationName());
		Assert.assertEquals("Car BA 3 destination track","", c3.getDestinationTrackName());
		Assert.assertEquals("Car BA 3 next destination","Essex MA", c3.getNextDestinationName());
		Assert.assertEquals("Car BA 3 next destination track","", c3.getNextDestTrackName());
		// schedule at Danbury (schedule B) forward car BB 4 to Foxboro Siding 1.
		Assert.assertEquals("Car BB 4 destination","Essex MA", c4.getDestinationName());
		Assert.assertEquals("Car BB 4 destination track","Essex Interchange", c4.getDestinationTrackName());
		Assert.assertEquals("Car BB 4 next destination","Foxboro MA", c4.getNextDestinationName());
		Assert.assertEquals("Car BB 4 next destination track","Foxboro Siding 1", c4.getNextDestTrackName());
		
		// check load
		Assert.assertEquals("Car BA 3 load","Screws", c3.getLoad());
		Assert.assertEquals("Car BB 4 load","Junk", c4.getLoad());
		
		// check next loads
		Assert.assertEquals("Car BA 3 load","", c3.getNextLoad());
		Assert.assertEquals("Car BB 4 load","", c4.getNextLoad());
		
		DanburyToEssexTrain.build();
		
		// schedule D at Essex Siding 1 is requesting load Screws, ship Nails  then forward car to Foxboro Siding 1
		Assert.assertEquals("Car BA 3 destination track","Essex Siding 1", c3.getDestinationTrackName());
		Assert.assertEquals("Car BA 3 next destination","Foxboro MA", c3.getNextDestinationName());
		Assert.assertEquals("Car BA 3 next destination track","Foxboro Siding 1", c3.getNextDestTrackName());
		
		// check next loads
		Assert.assertEquals("Car BA 3 load","Nails", c3.getNextLoad());
		
		DanburyToEssexTrain.terminate();
		
		// Train has arrived at Essex, check destinations
		// schedule at Essex (schedule D) forwards car BA 3 to Foxboro Siding 1 load Nails
		Assert.assertEquals("Car BA 3 destination","Foxboro MA", c3.getDestinationName());
		Assert.assertEquals("Car BA 3 destination track","Foxboro Siding 1", c3.getDestinationTrackName());
		Assert.assertEquals("Car BA 3 next destination","", c3.getNextDestinationName());
		Assert.assertEquals("Car BA 3 next destination track","", c3.getNextDestTrackName());

		Assert.assertEquals("Car BB 4 destination","Foxboro MA", c4.getDestinationName());
		Assert.assertEquals("Car BB 4 destination track","Foxboro Siding 1", c4.getDestinationTrackName());
		Assert.assertEquals("Car BB 4 next destination","", c4.getNextDestinationName());
		Assert.assertEquals("Car BB 4 next destination track","", c4.getNextDestTrackName());
		
		// check load
		Assert.assertEquals("Car BA 3 load","Nails", c3.getLoad());
		Assert.assertEquals("Car BB 4 load","Junk", c4.getLoad());
		
		// check next loads
		Assert.assertEquals("Car BA 3 load","", c3.getNextLoad());
		Assert.assertEquals("Car BB 4 load","", c4.getNextLoad());
		
		EssexToFoxboroTrain.build();
		EssexToFoxboroTrain.terminate();
		
		// Train has arrived at Foxboro, check destinations
		Assert.assertEquals("Car BA 3 destination","", c3.getDestinationName());
		Assert.assertEquals("Car BA 3 destination track","", c3.getDestinationTrackName());
		Assert.assertEquals("Car BA 3 next destination","", c3.getNextDestinationName());
		Assert.assertEquals("Car BA 3 next destination track","", c3.getNextDestTrackName());

		Assert.assertEquals("Car BB 4 destination","", c4.getDestinationName());
		Assert.assertEquals("Car BB 4 destination track","", c4.getDestinationTrackName());
		Assert.assertEquals("Car BB 4 next destination","", c4.getNextDestinationName());
		Assert.assertEquals("Car BB 4 next destination track","", c4.getNextDestTrackName());
		
		// check load
		Assert.assertEquals("Car BA 3 load","E", c3.getLoad());
		Assert.assertEquals("Car BB 4 load","E", c4.getLoad());
		
		// check next loads
		Assert.assertEquals("Car BA 3 load","", c3.getNextLoad());
		Assert.assertEquals("Car BB 4 load","", c4.getNextLoad());
	}
	
	// Ensure minimal setup for log4J
	@Override
	protected void setUp() {
		apps.tests.Log4JFixture.setUp();
		
		// set the locale to US English
		Locale.setDefault(Locale.ENGLISH);
		
		RouteManager.instance().dispose();
	}

	public OperationsCarRouterTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {"-noloading", OperationsCarRouterTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(OperationsCarRouterTest.class);
		return suite;
	}

	// The minimal setup for log4J
	@Override
	protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }
}
