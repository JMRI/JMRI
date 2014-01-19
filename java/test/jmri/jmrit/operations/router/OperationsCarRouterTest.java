//OperationsCarRouterTest.java

package jmri.jmrit.operations.router;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManagerXml;
import jmri.jmrit.operations.locations.Schedule;
import jmri.jmrit.operations.locations.ScheduleItem;
import jmri.jmrit.operations.locations.ScheduleManager;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarLoads;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.jmrit.operations.rollingstock.cars.CarManagerXml;
import jmri.jmrit.operations.rollingstock.cars.CarTypes;
import jmri.jmrit.operations.rollingstock.engines.EngineManagerXml;
import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.routes.RouteManagerXml;
import jmri.jmrit.operations.setup.OperationsSetupXml;
import jmri.jmrit.operations.setup.Setup;

import jmri.jmrit.operations.routes.RouteManager;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManagerXml;

import java.io.File;
import java.util.List;
import java.util.Locale;
import jmri.util.FileUtil;

/**
 * Tests for the Operations Router class
 *  
 * @author	Daniel Boudreau Copyright (C) 2010, 2011, 2013
 * @version $Revision$
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

		Track AS1 = Acton.addTrack("Acton Siding 1", Track.SPUR);
		AS1.setLength(300);
		Assert.assertEquals("Location AS1 Name", "Acton Siding 1", AS1.getName());
		Assert.assertEquals("Location AS1 Length", 300, AS1.getLength());
		
		Track AS2 = Acton.addTrack("Acton Siding 2", Track.SPUR);
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
		
		// add a second interchange track
		Track AI2 = Acton.addTrack("Acton Interchange 2", Track.INTERCHANGE);
		AI2.setLength(500);
		// bias tracks
		AI2.setMoves(100);
		Assert.assertEquals("Track AI2 Name", "Acton Interchange 2", AI2.getName());
		Assert.assertEquals("Track AI2 Length", 500, AI2.getLength());
		Assert.assertEquals("Track AI2 Train Directions", DIRECTION_ALL, AI2.getTrainDirections());

		Location Bedford = lmanager.newLocation("Bedford MA");
		Assert.assertEquals("Location 1 Name", "Bedford MA", Bedford.getName());
		Assert.assertEquals("Location 1 Initial Length", 0, Bedford.getLength());

		Track BS1 = Bedford.addTrack("Bedford Siding 1", Track.SPUR);
		BS1.setLength(300);
		Assert.assertEquals("Location BS1 Name", "Bedford Siding 1", BS1.getName());
		Assert.assertEquals("Location BS1 Length", 300, BS1.getLength());
		
		Track BS2 = Bedford.addTrack("Bedford Siding 2", Track.SPUR);
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

		Track CS1 = Clinton.addTrack("Clinton Siding 1", Track.SPUR);
		CS1.setLength(300);
		Assert.assertEquals("Location CS1 Name", "Clinton Siding 1", CS1.getName());
		Assert.assertEquals("Location CS1 Length", 300, CS1.getLength());
		
		Track CS2 = Clinton.addTrack("Clinton Siding 2", Track.SPUR);
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
		Track DS1 = Danbury.addTrack("Danbury Siding 1", Track.SPUR);
		DS1.setLength(300);
		Track DS2 = Danbury.addTrack("Danbury Siding 2", Track.SPUR);
		DS2.setLength(300);		
		Track DY = Danbury.addTrack("Danbury Yard", Track.YARD);
		DY.setLength(400);		
		Track DI = Danbury.addTrack("Danbury Interchange", Track.INTERCHANGE);
		DI.setLength(500);
		
		Location Essex = lmanager.newLocation("Essex MA");
		Track ES1 = Essex.addTrack("Essex Siding 1", Track.SPUR);
		ES1.setLength(300);	
		Track ES2 = Essex.addTrack("Essex Siding 2", Track.SPUR);
		ES2.setLength(300);		
		Track EY = Essex.addTrack("Essex Yard", Track.YARD);
		EY.setLength(400);		
		Track EI = Essex.addTrack("Essex Interchange", Track.INTERCHANGE);
		EI.setLength(500);
		
		Location Foxboro = lmanager.newLocation("Foxboro MA");
		Track FS1 = Foxboro.addTrack("Foxboro Siding 1", Track.SPUR);
		FS1.setLength(300);	
		Track FS2 = Foxboro.addTrack("Foxboro Siding 2", Track.SPUR);
		FS2.setLength(300);		
		Track FY = Foxboro.addTrack("Foxboro Yard", Track.YARD);
		FY.setLength(400);		
		Track FI = Foxboro.addTrack("Foxboro Interchange", Track.INTERCHANGE);
		FI.setLength(500);
				
		// create 2 cars
		Car c3 = cmanager.newCar("BA", "3");
		c3.setTypeName("Boxcar");
		c3.setLength("40");
		c3.setOwner("DAB");
		c3.setBuilt("1984");
		Assert.assertEquals("Box Car 3 Length", "40", c3.getLength());
		
		Car c4 = cmanager.newCar("BB", "4");
		c4.setTypeName("Flat");
		c4.setLength("40");
		c4.setOwner("AT");
		c4.setBuilt("1-86");
		Assert.assertEquals("Box Car 4 Length", "40", c4.getLength());
		
		Assert.assertEquals("place car at BI", Track.OKAY, c3.setLocation(Acton, AS1));
		Assert.assertFalse("Try routing no final destination", router.setDestination(c3, null, null));
		Assert.assertEquals("Check car's destination", "", c3.getDestinationName());
		
		Assert.assertEquals("place car at Acton", Track.OKAY, c4.setLocation(Acton, AS1));
		Assert.assertFalse("Try routing no final destination", router.setDestination(c4, null, null));
		Assert.assertEquals("Check car's destination", "", c4.getDestinationName());
		
		// disable routing
		Setup.setCarRoutingEnabled(false);
		c3.setFinalDestination(Bedford);
		Assert.assertFalse("Test router disabled", router.setDestination(c3, null, null));
		Assert.assertEquals("Router status", Router.STATUS_ROUTER_DISABLED, router.getStatus());
		Setup.setCarRoutingEnabled(true);
		
		// first try car routing with just one location
		c3.setFinalDestination(Acton);
		Assert.assertFalse("Try routing final destination equal to current", router.setDestination(c3, null, null));
		Assert.assertEquals("Check car's destination", "", c3.getDestinationName());
		Assert.assertEquals("Router status", Router.STATUS_CAR_AT_DESINATION, router.getStatus());
		
		// now try with next track not equal to current
		c3.setFinalDestination(Acton);
		c3.setFinalDestinationTrack(AS2);
		Assert.assertFalse("Try routing final track not equal to current", router.setDestination(c3, null, null));
		Assert.assertEquals("Check car's destination", "", c3.getDestinationName());
		Assert.assertEquals("Router status", Router.STATUS_NO_TRAINS, router.getStatus());
		
		// now try with next track equal to current
		c3.setFinalDestination(Acton);
		c3.setFinalDestinationTrack(AS1);
		Assert.assertFalse("Try routing final track equal to current", router.setDestination(c3, null, null));
		Assert.assertEquals("Check car's destination", "", c3.getDestinationName());
		Assert.assertEquals("Router status", Router.STATUS_CAR_AT_DESINATION, router.getStatus());
		
		// create a local train servicing Acton
		Train ActonTrain = tmanager.newTrain("Acton Local");
		Route routeA = rmanager.newRoute("A");
		RouteLocation rlA = routeA.addLocation(Acton);
		rlA.setTrainIconX(25);	// set train icon coordinates
		rlA.setTrainIconY(250);
		ActonTrain.setRoute(routeA);
		
		c3.setFinalDestination(Acton);
		c3.setFinalDestinationTrack(AS2);
		Assert.assertTrue("Try routing final track with Acton Local", router.setDestination(c3, null, null));
		Assert.assertEquals("Check car's destination", "Acton MA", c3.getDestinationName());
		Assert.assertEquals("Check car's destination track", "Acton Siding 2", c3.getDestinationTrackName());
		Assert.assertEquals("Router status", Track.OKAY, router.getStatus());
		
		// specify the Acton train
		c3.setDestination(null, null);	// clear previous destination
		c3.setFinalDestination(Acton);
		c3.setFinalDestinationTrack(AS2);
		Assert.assertTrue("Try routing final track with Acton Local", router.setDestination(c3, ActonTrain, null));
		Assert.assertEquals("Check car's destination", "Acton MA", c3.getDestinationName());
		Assert.assertEquals("Check car's destination track", "Acton Siding 2", c3.getDestinationTrackName());
		Assert.assertEquals("Router status", Track.OKAY, router.getStatus());
		
		// Set the track length to be less the length of c3
		AS2.setLength(c3.getTotalLength()-1);
		c3.setDestination(null, null);	// clear previous destination
		c3.setFinalDestination(Acton);	// local move, alternate or yard track option should be ignored
		c3.setFinalDestinationTrack(AS2);
		Assert.assertTrue("Try routing final track with Acton Local", router.setDestination(c3, ActonTrain, null));
		Assert.assertEquals("Check car's destination", "", c3.getDestinationName());
		Assert.assertEquals("Check car's destination track", "", c3.getDestinationTrackName());
		Assert.assertTrue("Should report that the issue was track length", router.getStatus().startsWith(Track.LENGTH));
		
		// restore track length
		AS2.setLength(300);
		
		// don't allow train to service boxcars
		ActonTrain.deleteTypeName("Boxcar");
		// and the next destination for the car
		c3.setDestination(null, null);	// clear previous destination
		c3.setFinalDestination(Acton);
		c3.setFinalDestinationTrack(AS2);
		Assert.assertFalse("Try routing with train that doesn't service Boxcar", router.setDestination(c3, null, null));
		Assert.assertEquals("Check car's destination", "", c3.getDestinationName());
		Assert.assertEquals("Router status", Router.STATUS_NO_TRAINS, router.getStatus());
		
		// try the car type Flat
		c4.setDestination(null, null);	// clear previous destination
		c4.setFinalDestination(Acton);
		c4.setFinalDestinationTrack(AS2);
		Assert.assertTrue("Try routing with train that service Flat", router.setDestination(c4, null, null));
		Assert.assertEquals("Check car's destination", "Acton MA", c4.getDestinationName());
		Assert.assertEquals("Router status", Track.OKAY, router.getStatus());

		// now allow Boxcar again
		ActonTrain.addTypeName("Boxcar");
		Assert.assertTrue("Try routing with train that does service Boxcar", router.setDestination(c3, null, null));
		Assert.assertEquals("Check car's destination", "Acton MA", c3.getDestinationName());
		Assert.assertEquals("Router status", Track.OKAY, router.getStatus());
		
		// don't allow train to service boxcars with road name BA
		ActonTrain.addRoadName("BA");
		ActonTrain.setRoadOption(Train.EXCLUDE_ROADS);
		// and the next destination for the car
		c3.setDestination(null, null);	// clear previous destination
		c3.setFinalDestination(Acton);
		c3.setFinalDestinationTrack(AS2);
		Assert.assertFalse("Try routing with train that doesn't service road name BA", router.setDestination(c3, null, null));
		Assert.assertEquals("Check car's destination", "", c3.getDestinationName());
		Assert.assertEquals("Router status", Router.STATUS_NO_TRAINS, router.getStatus());
	
		// try the car road name BB
		c4.setDestination(null, null);	// clear previous destination
		c4.setFinalDestination(Acton);
		c4.setFinalDestinationTrack(AS2);
		Assert.assertTrue("Try routing with train that services road BB", router.setDestination(c4, null, null));
		Assert.assertEquals("Check car's destination", "Acton MA", c4.getDestinationName());
		Assert.assertEquals("Router status", Track.OKAY, router.getStatus());

		// now try again but allow road name
		ActonTrain.setRoadOption(Train.ALL_ROADS);
		Assert.assertTrue("Try routing with train that does service road name BA", router.setDestination(c3, null, null));
		Assert.assertEquals("Check car's destination", "Acton MA", c3.getDestinationName());
		Assert.assertEquals("Router status", Track.OKAY, router.getStatus());
		
		// don't service cars built before 1985
		ActonTrain.setBuiltStartYear("1985");
		ActonTrain.setBuiltEndYear("2010");
		// and the next destination for the car
		c3.setDestination(null, null);	// clear previous destination
		c3.setFinalDestination(Acton);
		c3.setFinalDestinationTrack(AS2);
		Assert.assertFalse("Try routing with train that doesn't service car built before 1985", router.setDestination(c3, null, null));
		Assert.assertEquals("Check car's destination", "", c3.getDestinationName());
		Assert.assertEquals("Router status", Router.STATUS_NO_TRAINS, router.getStatus());
		
		// try the car built after 1985
		c4.setDestination(null, null);	// clear previous destination
		c4.setFinalDestination(Acton);
		c4.setFinalDestinationTrack(AS2);
		Assert.assertTrue("Try routing with train that services car built after 1985", router.setDestination(c4, null, null));
		Assert.assertEquals("Check car's destination", "Acton MA", c4.getDestinationName());
		Assert.assertEquals("Router status", Track.OKAY, router.getStatus());

		// car was built in 1984 should work
		ActonTrain.setBuiltStartYear("1983");
		Assert.assertTrue("Try routing with train that doesn't service car built before 1983", router.setDestination(c3, null, null));
		Assert.assertEquals("Check car's destination", "Acton MA", c3.getDestinationName());
		Assert.assertEquals("Router status", Track.OKAY, router.getStatus());
		
		// try car loads
		c3.setLoadName("Tools");
		ActonTrain.addLoadName("Tools");
		ActonTrain.setLoadOption(Train.EXCLUDE_LOADS);
		
		// and the next destination for the car
		c3.setDestination(null, null);	// clear previous destination
		c3.setFinalDestination(Acton);
		c3.setFinalDestinationTrack(AS2);
		Assert.assertFalse("Try routing with train that doesn't service load Tools", router.setDestination(c3, null, null));
		Assert.assertEquals("Check car's destination", "", c3.getDestinationName());
		Assert.assertEquals("Router status", Router.STATUS_NO_TRAINS, router.getStatus());
	
		// try the car load "E"
		c4.setDestination(null, null);	// clear previous destination
		c4.setFinalDestination(Acton);
		c4.setFinalDestinationTrack(AS2);
		Assert.assertTrue("Try routing with train that services load E", router.setDestination(c4, null, null));
		Assert.assertEquals("Check car's destination", "Acton MA", c4.getDestinationName());
		
		ActonTrain.setLoadOption(Train.ALL_LOADS);
		Assert.assertTrue("Try routing with train that that does service load Tools", router.setDestination(c3, null, null));
		Assert.assertEquals("Check car's destination", "Acton MA", c3.getDestinationName());

		// now test by modifying the route
		rlA.setPickUpAllowed(false);		
		// and the next destination for the car
		c3.setDestination(null, null);	// clear previous destination
		c3.setFinalDestination(Acton);
		c3.setFinalDestinationTrack(AS2);
		Assert.assertFalse("Try routing with train that doesn't pickup cars", router.setDestination(c3, null, null));
		Assert.assertEquals("Check car's destination", "", c3.getDestinationName());
		Assert.assertEquals("Router status", Router.STATUS_NO_TRAINS, router.getStatus());
		
		rlA.setPickUpAllowed(true);
		Assert.assertTrue("Try routing with train that that can pickup cars", router.setDestination(c3, null, null));
		Assert.assertEquals("Check car's destination", "Acton MA", c3.getDestinationName());
		
		rlA.setDropAllowed(false);		
		// and the next destination for the car
		c3.setDestination(null, null);	// clear previous destination
		c3.setFinalDestination(Acton);
		c3.setFinalDestinationTrack(AS2);
		Assert.assertFalse("Try routing with train that doesn't drop cars", router.setDestination(c3, null, null));
		Assert.assertEquals("Check car's destination", "", c3.getDestinationName());
		Assert.assertEquals("Router status", Router.STATUS_NO_TRAINS, router.getStatus());
		
		rlA.setDropAllowed(true);
		Assert.assertTrue("Try routing with train that that can drop cars", router.setDestination(c3, null, null));
		Assert.assertEquals("Check car's destination", "Acton MA", c3.getDestinationName());
		
		rlA.setMaxCarMoves(0);		
		// and the next destination for the car
		c3.setDestination(null, null);	// clear previous destination
		c3.setFinalDestination(Acton);
		c3.setFinalDestinationTrack(AS2);
		Assert.assertFalse("Try routing with train that doesn't service location", router.setDestination(c3, null, null));
		Assert.assertEquals("Check car's destination", "", c3.getDestinationName());
		Assert.assertEquals("Router status", Router.STATUS_NO_TRAINS, router.getStatus());
		
		rlA.setMaxCarMoves(10);
		Assert.assertTrue("Try routing with train that does service location", router.setDestination(c3, null, null));
		Assert.assertEquals("Check car's destination", "Acton MA", c3.getDestinationName());
		
		// test train depart direction
		Assert.assertEquals("check default direction", Track.NORTH, rlA.getTrainDirection());
		// set the depart location Acton to service by South bound trains only
		Acton.setTrainDirections(Track.SOUTH);
		
		// and the next destination for the car
		c3.setDestination(null, null);	// clear previous destination
		c3.setFinalDestination(Acton);
		c3.setFinalDestinationTrack(AS2);
		Assert.assertTrue("Try routing with local train that departs north, location south", router.setDestination(c3, null, null));
		Assert.assertEquals("Check car's destination", "Acton MA", c3.getDestinationName());
		
		Acton.setTrainDirections(Track.NORTH);
		c3.setDestination(null, null);	// clear previous destination
		c3.setFinalDestination(Acton);
		c3.setFinalDestinationTrack(AS2);
		Assert.assertTrue("Try routing with local train that departs north, location north", router.setDestination(c3, null, null));
		Assert.assertEquals("Check car's destination", "Acton MA", c3.getDestinationName());
		
		// set the depart track Acton to service by local train only
		AS1.setTrainDirections(0);
		
		// and the next destination for the car
		c3.setDestination(null, null);	// clear previous destination
		c3.setFinalDestination(Acton);
		c3.setFinalDestinationTrack(AS2);
		Assert.assertTrue("Try routing with local only", router.setDestination(c3, null, null));
		Assert.assertEquals("Check car's destination", "Acton MA", c3.getDestinationName());
		
		c3.setDestination(null, null);	// clear previous destination
		c3.setFinalDestination(Acton);
		c3.setFinalDestinationTrack(AS2);
		AS1.setTrainDirections(Track.NORTH);
		Assert.assertTrue("Try routing with local train that departs north, track north", router.setDestination(c3, null, null));
		Assert.assertEquals("Check car's destination", "Acton MA", c3.getDestinationName());
		
		// test arrival directions
		
		// set the arrival track Acton to service by local trains only
		AS2.setTrainDirections(0);
				
		// and the next destination for the car
		c3.setDestination(null, null);	// clear previous destination
		c3.setFinalDestination(Acton);
		c3.setFinalDestinationTrack(AS2);	// now specify the actual track
		Assert.assertTrue("Try routing with local train", router.setDestination(c3, null, null));
		Assert.assertEquals("Check car's destination", "Acton MA", c3.getDestinationName());
		
		AS2.setTrainDirections(Track.NORTH);
		c3.setDestination(null, null);	// clear previous destination
		c3.setFinalDestination(Acton);
		c3.setFinalDestinationTrack(AS2);	// now specify the actual track
		Assert.assertTrue("Try routing with train that departs north, track north", router.setDestination(c3, null, null));
		Assert.assertEquals("Check car's destination", "Acton MA", c3.getDestinationName());
		
		// add a second local train
		// create a local train servicing Acton
		Route routeA2 = rmanager.newRoute("A2");
		RouteLocation rlA2 = routeA2.addLocation(Acton);
		rlA2.setTrainIconX(25);	// set train icon coordinates
		rlA2.setTrainIconY(250);
		Train ActonTrain2 = tmanager.newTrain("Acton Local 2");
		ActonTrain2.setRoute(routeA2);
		
		// try routing with this train
		c3.setDestination(null, null);	// clear previous destination
		c3.setFinalDestination(Acton);
		c3.setFinalDestinationTrack(AS2);
		Assert.assertTrue("Try routing final track with Acton Local", router.setDestination(c3, ActonTrain2, null));
		Assert.assertEquals("Check car's destination", "Acton MA", c3.getDestinationName());
		Assert.assertEquals("Check car's destination track", "Acton Siding 2", c3.getDestinationTrackName());
		
		// don't allow Acton Local 2 to service boxcars
		ActonTrain2.deleteTypeName("Boxcar");
		// and the next destination for the car
		c3.setDestination(null, null);	// clear previous destination
		c3.setFinalDestination(Acton);
		c3.setFinalDestinationTrack(AS2);
		// Should be able to route using Acton Local, but destination should not be set
		Assert.assertTrue("Try routing with train that doesn't service Boxcar", router.setDestination(c3, ActonTrain2, null));
		Assert.assertEquals("Check car's destination", "", c3.getDestinationName());
		Assert.assertEquals("Router status", Router.STATUS_NOT_THIS_TRAIN, router.getStatus());
		
		// Two locations one train testing begins
		// set next destination Bedford
		c3.setDestination(null, null);	// clear previous destination
		c3.setFinalDestination(Bedford);
		c3.setFinalDestinationTrack(null);
		// should fail no train!
		Assert.assertFalse("Try routing with final destination", router.setDestination(c3, null, null));
		// create a train with a route from Acton to Bedford
		Train ActonToBedfordTrain = tmanager.newTrain("Acton to Bedford");
		Route routeAB = rmanager.newRoute("AB");
		RouteLocation rlActon = routeAB.addLocation(Acton);
		RouteLocation rlBedford = routeAB.addLocation(Bedford);
		rlBedford.setTrainIconX(100);	// set train icon coordinates
		rlBedford.setTrainIconY(250);
		ActonToBedfordTrain.setRoute(routeAB);
		
		// should work
		Assert.assertTrue("Try routing with final destination and train", router.setDestination(c3, null, null));
		Assert.assertEquals("Check car's destination", "Bedford MA", c3.getDestinationName());
		
		// try specific train
		c3.setDestination(null, null);	// clear previous destination
		c3.setFinalDestination(Bedford);
		c3.setFinalDestinationTrack(null);
		Assert.assertTrue("Try routing with final destination and train", router.setDestination(c3, ActonToBedfordTrain, null));
		Assert.assertEquals("Check car's destination", "Bedford MA", c3.getDestinationName());
		
		// don't allow train to service boxcars
		ActonToBedfordTrain.deleteTypeName("Boxcar");
		// and the next destination for the car
		c3.setDestination(null, null);	// clear previous destination
		c3.setFinalDestination(Bedford);
		Assert.assertFalse("Try routing with train that doesn't service Boxcar", router.setDestination(c3, null, null));
		Assert.assertEquals("Check car's destination", "", c3.getDestinationName());
		Assert.assertEquals("Router status", Router.STATUS_NOT_ABLE, router.getStatus());
		
		// try the car type Flat
		c4.setDestination(null, null);	// clear previous destination
		c4.setFinalDestination(Bedford);
		c4.setFinalDestinationTrack(null);
		Assert.assertTrue("Try routing with train that service Flat", router.setDestination(c4, null, null));
		Assert.assertEquals("Check car's destination", "Bedford MA", c4.getDestinationName());

		// now allow Boxcar again
		ActonToBedfordTrain.addTypeName("Boxcar");
		Assert.assertTrue("Try routing with train that does service Boxcar", router.setDestination(c3, null, null));
		Assert.assertEquals("Check car's destination", "Bedford MA", c3.getDestinationName());
		
		// don't allow train to service boxcars with road name BA
		ActonToBedfordTrain.addRoadName("BA");
		ActonToBedfordTrain.setRoadOption(Train.EXCLUDE_ROADS);
		// and the next destination for the car
		c3.setDestination(null, null);	// clear previous destination
		c3.setFinalDestination(Bedford);
		Assert.assertFalse("Try routing with train that doesn't service road name BA", router.setDestination(c3, null, null));
		Assert.assertEquals("Check car's destination", "", c3.getDestinationName());
		Assert.assertEquals("Router status", Router.STATUS_NOT_ABLE, router.getStatus());
	
		// try the car road name BB
		c4.setDestination(null, null);	// clear previous destination
		c4.setFinalDestination(Bedford);
		Assert.assertTrue("Try routing with train that services road BB", router.setDestination(c4, null, null));
		Assert.assertEquals("Check car's destination", "Bedford MA", c4.getDestinationName());

		// now try again but allow road name
		ActonToBedfordTrain.setRoadOption(Train.ALL_ROADS);
		Assert.assertTrue("Try routing with train that does service road name BA", router.setDestination(c3, null, null));
		Assert.assertEquals("Check car's destination", "Bedford MA", c3.getDestinationName());
		
		// don't service cars built before 1985
		ActonToBedfordTrain.setBuiltStartYear("1985");
		ActonToBedfordTrain.setBuiltEndYear("2010");
		// and the next destination for the car
		c3.setDestination(null, null);	// clear previous destination
		c3.setFinalDestination(Bedford);
		Assert.assertFalse("Try routing with train that doesn't service car built before 1985", router.setDestination(c3, null, null));
		Assert.assertEquals("Check car's destination", "", c3.getDestinationName());
		
		// try the car built after 1985
		c4.setDestination(null, null);	// clear previous destination
		c4.setFinalDestination(Bedford);
		Assert.assertTrue("Try routing with train that services car built after 1985", router.setDestination(c4, null, null));
		Assert.assertEquals("Check car's destination", "Bedford MA", c4.getDestinationName());

		// car was built in 1984 should work
		ActonToBedfordTrain.setBuiltStartYear("1983");
		Assert.assertTrue("Try routing with train that doesn't service car built before 1983", router.setDestination(c3, null, null));
		Assert.assertEquals("Check car's destination", "Bedford MA", c3.getDestinationName());
		
		// try car loads
		c3.setLoadName("Tools");
		ActonToBedfordTrain.addLoadName("Tools");
		ActonToBedfordTrain.setLoadOption(Train.EXCLUDE_LOADS);
		
		// and the next destination for the car
		c3.setDestination(null, null);	// clear previous destination
		c3.setFinalDestination(Bedford);
		Assert.assertFalse("Try routing with train that doesn't service load Tools", router.setDestination(c3, null, null));
		Assert.assertEquals("Check car's destination", "", c3.getDestinationName());
	
		// try the car load "E"
		c4.setDestination(null, null);	// clear previous destination
		c4.setFinalDestination(Bedford);
		Assert.assertTrue("Try routing with train that services load E", router.setDestination(c4, null, null));
		Assert.assertEquals("Check car's destination", "Bedford MA", c4.getDestinationName());
		
		ActonToBedfordTrain.setLoadOption(Train.ALL_LOADS);
		Assert.assertTrue("Try routing with train that that does service load Tools", router.setDestination(c3, null, null));
		Assert.assertEquals("Check car's destination", "Bedford MA", c3.getDestinationName());

		// now test by modifying the route
		rlActon.setPickUpAllowed(false);		
		// and the next destination for the car
		c3.setDestination(null, null);	// clear previous destination
		c3.setFinalDestination(Bedford);
		Assert.assertFalse("Try routing with train that doesn't pickup cars", router.setDestination(c3, null, null));
		Assert.assertEquals("Check car's destination", "", c3.getDestinationName());
		
		rlActon.setPickUpAllowed(true);
		Assert.assertTrue("Try routing with train that that can pickup cars", router.setDestination(c3, null, null));
		Assert.assertEquals("Check car's destination", "Bedford MA", c3.getDestinationName());
		
		rlBedford.setDropAllowed(false);		
		// and the next destination for the car
		c3.setDestination(null, null);	// clear previous destination
		c3.setFinalDestination(Bedford);
		Assert.assertFalse("Try routing with train that doesn't drop cars", router.setDestination(c3, null, null));
		Assert.assertEquals("Check car's destination", "", c3.getDestinationName());
		
		rlBedford.setDropAllowed(true);
		Assert.assertTrue("Try routing with train that that can drop cars", router.setDestination(c3, null, null));
		Assert.assertEquals("Check car's destination", "Bedford MA", c3.getDestinationName());
		
		rlBedford.setMaxCarMoves(0);		
		// and the next destination for the car
		c3.setDestination(null, null);	// clear previous destination
		c3.setFinalDestination(Bedford);
		Assert.assertFalse("Try routing with train that doesn't service destination", router.setDestination(c3, null, null));
		Assert.assertEquals("Check car's destination", "", c3.getDestinationName());
		
		rlBedford.setMaxCarMoves(5);
		Assert.assertTrue("Try routing with train that does service destination", router.setDestination(c3, null, null));
		Assert.assertEquals("Check car's destination", "Bedford MA", c3.getDestinationName());

		rlActon.setMaxCarMoves(0);		
		// and the next destination for the car
		c3.setDestination(null, null);	// clear previous destination
		c3.setFinalDestination(Bedford);
		Assert.assertFalse("Try routing with train that doesn't service location", router.setDestination(c3, null, null));
		Assert.assertEquals("Check car's destination", "", c3.getDestinationName());
		
		rlActon.setMaxCarMoves(5);
		Assert.assertTrue("Try routing with train that does service location", router.setDestination(c3, null, null));
		Assert.assertEquals("Check car's destination", "Bedford MA", c3.getDestinationName());
		
		// test train depart direction
		Assert.assertEquals("check default direction", Track.NORTH, rlActon.getTrainDirection());
		// set the depart location Acton to service by South bound trains only
		Acton.setTrainDirections(Track.SOUTH);
		
		// and the next destination for the car
		c3.setDestination(null, null);	// clear previous destination
		c3.setFinalDestination(Bedford);

		// remove the Action local by not allowing train to service boxcars
		ActonTrain.deleteTypeName("Boxcar");
		Assert.assertFalse("Try routing with train that departs north, location south", router.setDestination(c3, null, null));
		Assert.assertEquals("Check car's destination", "", c3.getDestinationName());
		
		Acton.setTrainDirections(Track.NORTH);
		Assert.assertTrue("Try routing with train that departs north, location north", router.setDestination(c3, null, null));
		Assert.assertEquals("Check car's destination", "Bedford MA", c3.getDestinationName());
		
		// set the depart track Acton to service by South bound trains only
		AS1.setTrainDirections(Track.SOUTH);
		
		// and the next destination for the car
		c3.setDestination(null, null);	// clear previous destination
		c3.setFinalDestination(Bedford);
		Assert.assertFalse("Try routing with train that departs north, track south", router.setDestination(c3, null, null));
		Assert.assertEquals("Check car's destination", "", c3.getDestinationName());
		
		AS1.setTrainDirections(Track.NORTH);
		Assert.assertTrue("Try routing with train that departs north, track north", router.setDestination(c3, null, null));
		Assert.assertEquals("Check car's destination", "Bedford MA", c3.getDestinationName());
		
		// test arrival directions
		// set the arrival location Bedford to service by South bound trains only
		Bedford.setTrainDirections(Track.SOUTH);
		
		// and the next destination for the car
		c3.setDestination(null, null);	// clear previous destination
		c3.setFinalDestination(Bedford);
		Assert.assertFalse("Try routing with train that arrives north, destination south", router.setDestination(c3, null, null));
		Assert.assertEquals("Check car's destination", "", c3.getDestinationName());
		
		Bedford.setTrainDirections(Track.NORTH);
		Assert.assertTrue("Try routing with train that arrives north, destination north", router.setDestination(c3, null, null));
		Assert.assertEquals("Check car's destination", "Bedford MA", c3.getDestinationName());
		
		// set the depart track Acton to service by South bound trains only
		BS1.setTrainDirections(Track.SOUTH);
		
		// and the next destination for the car
		c3.setDestination(null, null);	// clear previous destination
		c3.setFinalDestination(Bedford);
		Assert.assertTrue("Try routing with train that arrives north, but no final track", router.setDestination(c3, null, null));
		Assert.assertEquals("Check car's destination", "Bedford MA", c3.getDestinationName());
		
		c3.setDestination(null, null);	// clear previous destination
		c3.setFinalDestination(Bedford); // the final destination for the car
		c3.setFinalDestinationTrack(BS1);	// now specify the actual track
		Assert.assertFalse("Try routing with train that arrives north, now with track", router.setDestination(c3, null, null));
		Assert.assertEquals("Check car's destination", "", c3.getDestinationName());
		
		BS1.setTrainDirections(Track.NORTH);
		Assert.assertTrue("Try routing with train that departs north, track north", router.setDestination(c3, null, null));
		Assert.assertEquals("Check car's destination", "Bedford MA", c3.getDestinationName());
		
		Setup.setOnlyActiveTrainsEnabled(true);
		c3.setDestination(null, null);	// clear previous destination
		c3.setFinalDestination(Bedford);
		Assert.assertTrue("Try routing only active trains", router.setDestination(c3, null, null));
		
		// now deselect the Action to Bedford train
		ActonToBedfordTrain.setBuildEnabled(false);
		c3.setDestination(null, null);	// clear previous destination
		c3.setFinalDestination(Bedford);
		Assert.assertFalse("Try routing only active trains, Action to Beford deselected", router.setDestination(c3, null, null));
		Assert.assertEquals("Router status", Router.STATUS_NOT_ABLE, router.getStatus());
		
		Setup.setOnlyActiveTrainsEnabled(false);
		c3.setDestination(null, null);	// clear previous destination
		c3.setFinalDestination(Bedford);
		Assert.assertTrue("Try routing, only active trains deselected", router.setDestination(c3, null, null));
		
		// test yard and alternate track options
		BS1.setLength(c3.getTotalLength()-1);
		c3.setDestination(null, null);	// clear previous destination
		c3.setFinalDestination(Bedford);
		c3.setFinalDestinationTrack(BS1);
		Assert.assertTrue("Test search for yard", router.setDestination(c3, null, null));
		Assert.assertEquals("Destination", "Bedford MA", c3.getDestinationName());
		Assert.assertEquals("Destination track should be yard", "Bedford Yard", c3.getDestinationTrackName());
		// the car was sent to a yard track because the spur was full
		Assert.assertTrue("Should be reporting length issue", router.getStatus().startsWith(Track.LENGTH));
		
		// remove yard type
		BY.setTrackType(Track.SPUR);
		c3.setDestination(null, null);	// clear previous destination
		c3.setFinalDestination(Bedford);
		c3.setFinalDestinationTrack(BS1);
		Assert.assertTrue("Test search for yard that doesn't exist", router.setDestination(c3, null, null));
		Assert.assertEquals("Destination", "", c3.getDestinationName());
		Assert.assertTrue("Should be reporting length issue", router.getStatus().startsWith(Track.LENGTH));

		// restore yard type
		BY.setTrackType(Track.YARD);
		
		// test alternate track option
		BS1.setAlternateTrack(BS2);
		c3.setDestination(null, null);	// clear previous destination
		c3.setFinalDestination(Bedford);
		c3.setFinalDestinationTrack(BS1);
		Assert.assertTrue("Test use alternate", router.setDestination(c3, null, null));
		Assert.assertEquals("Destination", "Bedford MA", c3.getDestinationName());
		Assert.assertEquals("Destination track should be siding", "Bedford Siding 2", c3.getDestinationTrackName());
		// the car was sent to the alternate track because the spur was full
		Assert.assertTrue("Should be reporting length issue", router.getStatus().startsWith(Track.LENGTH));

		// restore track length and remove alternate
		BS1.setLength(300);
		BS1.setAlternateTrack(null);
		
		// One train tests complete. Start two train testing.
		// Force first move to be by local train
		AS1.setTrainDirections(0);
		ActonTrain.addTypeName("Boxcar");	// restore the local
				
		c3.setDestination(null, null);	// clear previous destination
		c3.setFinalDestination(Bedford);	// the final destination for the car
		c3.setFinalDestinationTrack(BS1);	// now specify the actual track
		
		Assert.assertTrue("Try routing two trains via interchange", router.setDestination(c3, null, null));
		Assert.assertEquals("Check car's destination", "Acton MA", c3.getDestinationName());
		Assert.assertEquals("Check car's destination track", "Acton Interchange", c3.getDestinationTrackName());
		
		// don't allow use of interchange track
		AI.setDropOption(Track.TRAINS);
		AI2.setDropOption(Track.TRAINS);
		
		c3.setDestination(null, null);	// clear previous destination
		c3.setFinalDestination(Bedford);	// the final destination for the car
		c3.setFinalDestinationTrack(BS1);	// now specify the actual track
		
		Assert.assertTrue("Try routing two trains via yard", router.setDestination(c3, null, null));
		Assert.assertEquals("Check car's destination", "Acton MA", c3.getDestinationName());
		Assert.assertEquals("Check car's destination track", "Acton Yard", c3.getDestinationTrackName());
		
		// allow use of interchange track
		AI.setDropOption(Track.ANY);
		AI2.setDropOption(Track.ANY);
		
		c3.setDestination(null, null);	// clear previous destination
		c3.setFinalDestination(Bedford);	// the final destination for the car
		c3.setFinalDestinationTrack(BS1);
		
		// allow Boxcars
		ActonTrain2.addTypeName("Boxcar");
		
		Assert.assertTrue("Try routing two trains", router.setDestination(c3, ActonTrain2, null));
		Assert.assertEquals("Check car's destination", "Acton MA", c3.getDestinationName());
		Assert.assertEquals("Check car's destination track", "Acton Interchange", c3.getDestinationTrackName());
		
		// test to see if second interchange track used if first is full
		AI.setLength(c3.getTotalLength()-1);
		c3.setDestination(null, null);	// clear previous destination
		c3.setFinalDestination(Bedford);	// the final destination for the car
		c3.setFinalDestinationTrack(BS1);
		Assert.assertTrue("Try routing two trains to interchange track 2", router.setDestination(c3, ActonTrain2, null));
		Assert.assertEquals("Check car's destination", "Acton MA", c3.getDestinationName());
		Assert.assertEquals("Check car's destination track", "Acton Interchange 2", c3.getDestinationTrackName());
		Assert.assertEquals("Router status", Track.OKAY, router.getStatus());
		
		// use yard track if interchange tracks are full
		AI2.setLength(c3.getTotalLength()-1);
		c3.setDestination(null, null);	// clear previous destination
		c3.setFinalDestination(Bedford);	// the final destination for the car
		c3.setFinalDestinationTrack(BS1);
		Assert.assertTrue("Try routing two trains to yard track", router.setDestination(c3, ActonTrain2, null));
		Assert.assertEquals("Check car's destination", "Acton MA", c3.getDestinationName());
		Assert.assertEquals("Check car's destination track", "Acton Yard", c3.getDestinationTrackName());
		
		// disable using yard tracks for routing
		Setup.setCarRoutingViaYardsEnabled(false);
		c3.setDestination(null, null);	// clear previous destination
		c3.setFinalDestination(Bedford);	// the final destination for the car
		c3.setFinalDestinationTrack(BS1);
		Assert.assertTrue("Try routing two trains to yard track", router.setDestination(c3, ActonTrain2, null));
		Assert.assertEquals("Check car's destination", "", c3.getDestinationName());
		Assert.assertEquals("Check car's destination track", "", c3.getDestinationTrackName());

		// restore track length
		AI.setLength(500);
		AI2.setLength(500);
		Setup.setCarRoutingViaYardsEnabled(true);
		
		// don't allow train 2 to service boxcars with road name BA
		ActonTrain2.addRoadName("BA");
		ActonTrain2.setRoadOption(Train.EXCLUDE_ROADS);
		
		c3.setDestination(null, null);	// clear previous destination
		c3.setFinalDestination(Bedford);	// the final destination for the car
		c3.setFinalDestinationTrack(BS1);
		
		// routing should work using train 1, destination and track should not be set
		Assert.assertTrue("Try routing two trains via yard", router.setDestination(c3, ActonTrain2, null));
		Assert.assertEquals("Check car's destination", "", c3.getDestinationName());
		Assert.assertEquals("Check car's destination track", "", c3.getDestinationTrackName());

		// two train testing done!
		// now try up to 5 trains to route car
		
		// set final destination Clinton
		c3.setDestination(null, null);	// clear previous destination
		c3.setFinalDestination(Clinton);
		c3.setFinalDestinationTrack(null);
		// should fail no train!
		Assert.assertFalse("Try routing with final destination", router.setDestination(c3, null, null));

		// create a train with a route from Bedford to Clinton
		Train BedfordToClintonTrain = tmanager.newTrain("Bedford to Clinton");
		Route routeBC = rmanager.newRoute("BC");
		routeBC.addLocation(Bedford);
		RouteLocation rlchelmsford = routeBC.addLocation(Clinton);
		rlchelmsford.setTrainIconX(175);	// set train icon coordinates
		rlchelmsford.setTrainIconY(250);
		BedfordToClintonTrain.setRoute(routeBC);
		
		// should work
		Assert.assertTrue("Try routing with final destination and train", router.setDestination(c3, null, null));
		Assert.assertEquals("Check car's destination", "Acton MA", c3.getDestinationName());
		Assert.assertEquals("Check car's destination track", "Acton Interchange", c3.getDestinationTrackName());
				
		// allow train 2 to service boxcars with road name BA
		ActonTrain2.setRoadOption(Train.ALL_ROADS);
		
		// try with train 2
		c3.setDestination(null, null);	// clear previous destination
		c3.setFinalDestination(Clinton);
		
		// routing should work using train 2
		Assert.assertTrue("Try routing three trains", router.setDestination(c3, ActonTrain2, null));
		Assert.assertEquals("Check car's destination", "Acton MA", c3.getDestinationName());
		Assert.assertEquals("Check car's destination track", "Acton Interchange", c3.getDestinationTrackName());
		
		c3.setDestination(null, null);	// clear previous destination
		c3.setFinalDestination(Clinton);
		
		// test to see if second interchange track used if first is full
		AI.setLength(c3.getTotalLength()-1);
		c3.setDestination(null, null);	// clear previous destination
		c3.setFinalDestination(Clinton);	// the final destination for the car
		Assert.assertTrue("Try routing three trains to interchange track 2", router.setDestination(c3, ActonTrain2, null));
		Assert.assertEquals("Check car's destination", "Acton MA", c3.getDestinationName());
		Assert.assertEquals("Check car's destination track", "Acton Interchange 2", c3.getDestinationTrackName());
		
		// use yard track if interchange tracks are full
		AI2.setLength(c3.getTotalLength()-1);
		c3.setDestination(null, null);	// clear previous destination
		c3.setFinalDestination(Clinton);	// the final destination for the car
		Assert.assertTrue("Try routing three trains to yard track", router.setDestination(c3, ActonTrain2, null));
		Assert.assertEquals("Check car's destination", "Acton MA", c3.getDestinationName());
		Assert.assertEquals("Check car's destination track", "Acton Yard", c3.getDestinationTrackName());
		
		// disable the use of yard tracks for routing
		Setup.setCarRoutingViaYardsEnabled(false);
		c3.setDestination(null, null);	// clear previous destination
		c3.setFinalDestination(Clinton);	// the final destination for the car
		Assert.assertTrue("Try routing three trains to yard track, option disabled", router.setDestination(c3, ActonTrain2, null));
		Assert.assertEquals("Check car's destination", "", c3.getDestinationName());
		Assert.assertEquals("Check car's destination track", "", c3.getDestinationTrackName());

		// restore track length
		AI.setLength(500);
		AI2.setLength(500);
		Setup.setCarRoutingViaYardsEnabled(true);
		
		c3.setDestination(null, null);	// clear previous destination
		c3.setFinalDestination(Clinton);	// the final destination for the car
		// don't allow train 2 to service cars built before 1985
		ActonTrain2.setBuiltStartYear("1985");
		ActonTrain2.setBuiltEndYear("2010");
		// routing should work using train 1, but destinations and track should not be set
		Assert.assertTrue("Try routing three trains", router.setDestination(c3, ActonTrain2, null));
		Assert.assertEquals("Check car's destination", "", c3.getDestinationName());
		Assert.assertEquals("Check car's destination track", "", c3.getDestinationTrackName());
		// allow car to be serviced
		// car was built in 1984 should work
		ActonTrain2.setBuiltStartYear("1983");
		
		// set final destination Danbury
		c3.setDestination(null, null);	// clear previous destination
		c3.setFinalDestination(Danbury);
		// should fail no train!
		Assert.assertFalse("Try routing with final destination", router.setDestination(c3, null, null));
	
		// create a train with a route from Clinton to Danbury
		Train ClintonToDanburyTrain = tmanager.newTrain("Clinton to Danbury");
		Route routeCD = rmanager.newRoute("CD");
		routeCD.addLocation(Clinton);
		RouteLocation rlDanbury = routeCD.addLocation(Danbury);
		rlDanbury.setTrainIconX(250);	// set train icon coordinates
		rlDanbury.setTrainIconY(250);
		ClintonToDanburyTrain.setRoute(routeCD);
		
		// should work
		Assert.assertTrue("Try routing with final destination and train", router.setDestination(c3, null, null));
		Assert.assertEquals("Check car's destination", "Acton MA", c3.getDestinationName());
		Assert.assertEquals("Check car's destination track", "Acton Interchange", c3.getDestinationTrackName());
		
		c3.setDestination(null, null);	// clear previous destination
		c3.setFinalDestination(Danbury);	// the final destination for the car
		
		// routing should work using train 2
		Assert.assertTrue("Try routing four trains", router.setDestination(c3, ActonTrain2, null));
		Assert.assertEquals("Check car's destination", "Acton MA", c3.getDestinationName());
		Assert.assertEquals("Check car's destination track", "Acton Interchange", c3.getDestinationTrackName());
		
		c3.setDestination(null, null);	// clear previous destination
		c3.setFinalDestination(Danbury);	// the final destination for the car
		
		// don't allow train 2 to service cars with load Tools
		ActonTrain2.addLoadName("Tools");
		ActonTrain2.setLoadOption(Train.EXCLUDE_LOADS);
		// routing should work using train 1, but destinations and track should not be set
		Assert.assertTrue("Try routing four trains", router.setDestination(c3, ActonTrain2, null));
		Assert.assertEquals("Check car's destination", "", c3.getDestinationName());
		Assert.assertEquals("Check car's destination track", "", c3.getDestinationTrackName());
		// restore train 2
		ActonTrain2.setLoadOption(Train.ALL_LOADS);
		
		// set final destination Essex
		c3.setDestination(null, null);	// clear previous destination
		c3.setFinalDestination(Essex);
		// should fail no train!
		Assert.assertFalse("Try routing with final destination", router.setDestination(c3, null, null));
		
		// create a train with a route from Danbury to Essex
		Train DanburyToEssexTrain = tmanager.newTrain("Danbury to Essex");
		Route routeDE = rmanager.newRoute("DE");
		RouteLocation rlDanbury2 = routeDE.addLocation(Danbury);
		RouteLocation rlEssex = routeDE.addLocation(Essex);
		// set the number of car moves to 8 for a later test
		rlDanbury2.setMaxCarMoves(8);
		rlEssex.setMaxCarMoves(8);
		rlEssex.setTrainIconX(25);	// set train icon coordinates
		rlEssex.setTrainIconY(275);
		DanburyToEssexTrain.setRoute(routeDE);
		
		// should work
		Assert.assertTrue("Try routing with final destination and train", router.setDestination(c3, null, null));
		Assert.assertEquals("Check car's destination", "Acton MA", c3.getDestinationName());
		Assert.assertEquals("Check car's destination track", "Acton Interchange", c3.getDestinationTrackName());
		
		// routing should work using train 2
		c3.setDestination(null, null);	// clear previous destination
		c3.setFinalDestination(Essex);
		Assert.assertTrue("Try routing five trains", router.setDestination(c3, ActonTrain2, null));
		Assert.assertEquals("Check car's destination", "Acton MA", c3.getDestinationName());
		Assert.assertEquals("Check car's destination track", "Acton Interchange", c3.getDestinationTrackName());
		
		c3.setDestination(null, null);	// clear previous destination
		c3.setFinalDestination(Essex);
		// don't allow train 2 to pickup
		rlA2.setPickUpAllowed(false);
		// routing should work using train 1, but destinations and track should not be set
		Assert.assertTrue("Try routing five trains", router.setDestination(c3, ActonTrain2, null));
		Assert.assertEquals("Check car's destination", "", c3.getDestinationName());
		Assert.assertEquals("Check car's destination track", "", c3.getDestinationTrackName());
		Assert.assertEquals("Check status", Router.STATUS_NOT_THIS_TRAIN, router.getStatus());

		// set final destination Foxboro
		c3.setDestination(null, null);	// clear previous destination
		c3.setFinalDestination(Foxboro);
		// should fail no train!
		Assert.assertFalse("Try routing with final destination", router.setDestination(c3, null, null));
		
		// create a train with a route from Essex to Foxboro
		Train EssexToFoxboroTrain = tmanager.newTrain("Essex to Foxboro");
		Route routeEF = rmanager.newRoute("EF");
		routeEF.addLocation(Essex);
		RouteLocation rlFoxboro = routeEF.addLocation(Foxboro);
		rlFoxboro.setTrainIconX(100);	// set train icon coordinates
		rlFoxboro.setTrainIconY(275);
		EssexToFoxboroTrain.setRoute(routeEF);
		
		// 6th train should fail!  Only 5 trains supported
		Assert.assertFalse("Try routing with final destination and train", router.setDestination(c3, null, null));		
		Assert.assertFalse("Try routing with final destination and train", router.setDestination(c3, ActonTrain, null));
		Assert.assertFalse("Try routing with final destination and train", router.setDestination(c3, ActonTrain2, null));
		
		// get rid of the local train
		AS1.setTrainDirections(Track.NORTH);
		
		// now should work!
		Assert.assertTrue("Try routing with final destination and train", router.setDestination(c3, null, null));
		Assert.assertEquals("Check car's destination", "Bedford MA", c3.getDestinationName());
		Assert.assertEquals("Check car's destination track", "Bedford Interchange", c3.getDestinationTrackName());

		// require local train for next test
		AS1.setTrainDirections(0);
		
		//TODO test restrict location by car type
		//TODO test restrict tracks by type road, load
	}
	
	// Use trains to move cars
	public void testRoutingWithTrains() {
		TrainManager tmanager = TrainManager.instance();
		CarManager cmanager = CarManager.instance();
		LocationManager lmanager = LocationManager.instance();
		
		loadLocationsTrainsAndCars();

		List<Train> trains = tmanager.getTrainsByNameList();
		Assert.assertEquals("confirm number of trains", 7, trains.size());
		
		Train ActonTrain = tmanager.getTrainByName("Acton Local");
		Train ActonToBedfordTrain = tmanager.getTrainByName("Acton to Bedford");
		Train BedfordToClintonTrain = tmanager.getTrainByName("Bedford to Clinton");
		Train ClintonToDanburyTrain = tmanager.getTrainByName("Clinton to Danbury");
		Train DanburyToEssexTrain = tmanager.getTrainByName("Danbury to Essex");
		Train EssexToFoxboroTrain = tmanager.getTrainByName("Essex to Foxboro");
		
		Car c3 = cmanager.getByRoadAndNumber("BA", "3");
		Car c4 = cmanager.getByRoadAndNumber("BB", "4");
		
		Location Acton = lmanager.getLocationByName("Acton MA");
		Track AS1 = Acton.getTrackByName("Acton Siding 1", Track.SPUR);
		// set the depart track Acton to service by local train only
		AS1.setTrainDirections(0);
		
		Location Essex = lmanager.getLocationByName("Essex MA");
		Track ES2 = Essex.getTrackByName("Essex Siding 2", Track.SPUR);
		Location Foxboro = lmanager.getLocationByName("Foxboro MA");
		
		Location Gulf = lmanager.newLocation("Gulf");
		
		// place cars
		Assert.assertEquals("Place car", Track.OKAY, c3.setLocation(Acton, AS1));
		Assert.assertEquals("Place car", Track.OKAY, c4.setLocation(Acton, AS1));
		
		// confirm cars are in Acton
		Assert.assertEquals("car's location Acton","Acton MA", c3.getLocationName());
		Assert.assertEquals("car's location Acton","Acton Siding 1", c3.getTrackName());
		
		Assert.assertEquals("car's location Acton","Acton MA", c4.getLocationName());
		Assert.assertEquals("car's location Acton","Acton Siding 1", c4.getTrackName());
		
		// set final destination Essex
		c3.setFinalDestination(Essex);
		c3.setFinalDestinationTrack(ES2);
		c3.setLoadName("L");
		c3.setReturnWhenEmptyDestination(Foxboro);
		
		// final destination Gulf is not reachable, so car must move
		c4.setFinalDestination(Gulf);
		
		ActonTrain.build();
		
		Assert.assertEquals("car's destination","Acton MA", c3.getDestinationName());
		Assert.assertEquals("car's destinaton track","Acton Interchange", c3.getDestinationTrackName());
		Assert.assertEquals("car's final destinaton",Essex, c3.getFinalDestination());
		Assert.assertEquals("car's final destinaton track",ES2, c3.getFinalDestinationTrack());
		
		Assert.assertEquals("car's destination","Acton MA", c4.getDestinationName());
		Assert.assertEquals("car's destinaton track","Acton Yard", c4.getDestinationTrackName());
		Assert.assertEquals("car's final destinaton",Gulf, c4.getFinalDestination());
		Assert.assertEquals("car's final destinaton track",null, c4.getFinalDestinationTrack());
		
		ActonTrain.reset();
		
		// check car's destinations after reset
		Assert.assertEquals("car's destination","", c3.getDestinationName());
		Assert.assertEquals("car's destinaton track","", c3.getDestinationTrackName());
		Assert.assertEquals("car's final destinaton",Essex, c3.getFinalDestination());
		Assert.assertEquals("car's final destinaton track",ES2, c3.getFinalDestinationTrack());
		Assert.assertEquals("car's load","L", c3.getLoadName());

		Assert.assertEquals("car's final destinaton",Gulf, c4.getFinalDestination());
		Assert.assertEquals("car's final destinaton track",null, c4.getFinalDestinationTrack());

		ActonTrain.build();		
		ActonTrain.terminate();
		
		// confirm cars have moved
		Assert.assertEquals("car's location Acton","Acton MA", c3.getLocationName());
		Assert.assertEquals("car's location Acton","Acton Interchange", c3.getTrackName());
		// as of 5/4/2011 the car's destination is set to null, but the final destination continues to exist
		Assert.assertEquals("car's destination","", c3.getDestinationName());
		Assert.assertEquals("car's destination track","", c3.getDestinationTrackName());
		Assert.assertEquals("car's final destinaton",Essex, c3.getFinalDestination());
		Assert.assertEquals("car's final destinaton track",ES2, c3.getFinalDestinationTrack());
		Assert.assertEquals("car's load","L", c3.getLoadName());
		
		Assert.assertEquals("car's location Acton","Acton MA", c4.getLocationName());
		Assert.assertEquals("car's location Acton","Acton Yard", c4.getTrackName());
		Assert.assertEquals("car's destination","", c4.getDestinationName());
		Assert.assertEquals("car's destination track","", c4.getDestinationTrackName());
		
		ActonToBedfordTrain.build();
		ActonToBedfordTrain.terminate();
		
		// confirm cars have moved
		Assert.assertEquals("car's location Bedford","Bedford MA", c3.getLocationName());
		Assert.assertEquals("car's location Bedford","Bedford Interchange", c3.getTrackName());
		Assert.assertEquals("car's destination","", c3.getDestinationName());
		Assert.assertEquals("car's destination track","", c3.getDestinationTrackName());
		Assert.assertEquals("car's load","L", c3.getLoadName());
		Assert.assertEquals("car's final destinaton",Essex, c3.getFinalDestination());
		Assert.assertEquals("car's final destinaton track",ES2, c3.getFinalDestinationTrack());
		
		Assert.assertEquals("car's location Bedford","Bedford MA", c4.getLocationName());
		Assert.assertEquals("car's location Bedford","Bedford Siding 1", c4.getTrackName());
		Assert.assertEquals("car's destination","", c4.getDestinationName());
		Assert.assertEquals("car's destination track","", c4.getDestinationTrackName());

		BedfordToClintonTrain.build();
		BedfordToClintonTrain.terminate();
		
		// confirm cars have moved
		Assert.assertEquals("car's location Clinton","Clinton MA", c3.getLocationName());
		Assert.assertEquals("car's location Clinton","Clinton Interchange", c3.getTrackName());
		Assert.assertEquals("car's destination","", c3.getDestinationName());
		Assert.assertEquals("car's destination track","", c3.getDestinationTrackName());
		Assert.assertEquals("car's load","L", c3.getLoadName());
		Assert.assertEquals("car's final destinaton",Essex, c3.getFinalDestination());
		Assert.assertEquals("car's final destinaton track",ES2, c3.getFinalDestinationTrack());
		
		Assert.assertEquals("car's location Clinton","Clinton MA", c4.getLocationName());
		Assert.assertEquals("car's location Clinton","Clinton Siding 1", c4.getTrackName());
		Assert.assertEquals("car's destination","", c4.getDestinationName());
		Assert.assertEquals("car's destination track","", c4.getDestinationTrackName());
		
		ClintonToDanburyTrain.build();
		ClintonToDanburyTrain.terminate();
		
		// confirm cars have moved
		Assert.assertEquals("car's location Danbury","Danbury MA", c3.getLocationName());
		Assert.assertEquals("car's location Danbury","Danbury Interchange", c3.getTrackName());
		Assert.assertEquals("car's destination","", c3.getDestinationName());
		Assert.assertEquals("car's destination track","", c3.getDestinationTrackName());
		Assert.assertEquals("car's load","L", c3.getLoadName());
		Assert.assertEquals("car's final destinaton",Essex, c3.getFinalDestination());
		Assert.assertEquals("car's final destinaton track",ES2, c3.getFinalDestinationTrack());
		
		Assert.assertEquals("car's location Danbury","Danbury MA", c4.getLocationName());
		Assert.assertEquals("car's location Danbury","Danbury Siding 1", c4.getTrackName());
		Assert.assertEquals("car's destination","", c4.getDestinationName());
		Assert.assertEquals("car's destination track","", c4.getDestinationTrackName());
		
		DanburyToEssexTrain.build();
		DanburyToEssexTrain.terminate();
		
		// confirm cars have moved car has arrived at final destination Essex
		
		Assert.assertEquals("car's location Essex","Essex MA", c3.getLocationName());
		Assert.assertEquals("car's location Essex","Essex Siding 2", c3.getTrackName());
		Assert.assertEquals("car's destination","", c3.getDestinationName());
		Assert.assertEquals("car's destination track","", c3.getDestinationTrackName());
		Assert.assertEquals("car's load","E", c3.getLoadName());
		// car when empty must return to Foxboro
		Assert.assertEquals("car's final destinaton",Foxboro, c3.getFinalDestination());
		Assert.assertEquals("car's final destinaton track",null, c3.getFinalDestinationTrack());
		
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
		Assert.assertEquals("car's load","L", c3.getLoadName());
		
		Assert.assertEquals("car's location Foxboro","Foxboro MA", c4.getLocationName());
		Assert.assertEquals("car's location Foxboro","Foxboro Siding 2", c4.getTrackName());
		Assert.assertEquals("car's destination","", c4.getDestinationName());
		Assert.assertEquals("car's destination track","", c4.getDestinationTrackName());

	}
	
	/*
	 * This test creates 4 schedules, and each schedule only has one item.
	 * Two cars are used a boxcar and a flat. They both start with a load of
	 * "Food". They should be routed to the correct schedule that is demanding
	 * the car type and load.
	 */
	public void testRoutingWithSimpleSchedules() {
		TrainManager tmanager = TrainManager.instance();
		CarManager cmanager = CarManager.instance();
		LocationManager lmanager = LocationManager.instance();
		
		loadLocationsTrainsAndCars();

		List<Train> trains = tmanager.getTrainsByNameList();
		Assert.assertEquals("confirm number of trains", 7, trains.size());
		
		Train ActonTrain = tmanager.getTrainByName("Acton Local");
		Train ActonToBedfordTrain = tmanager.getTrainByName("Acton to Bedford");
		Train BedfordToClintonTrain = tmanager.getTrainByName("Bedford to Clinton");
		Train ClintonToDanburyTrain = tmanager.getTrainByName("Clinton to Danbury");
		Train DanburyToEssexTrain = tmanager.getTrainByName("Danbury to Essex");
		Train EssexToFoxboroTrain = tmanager.getTrainByName("Essex to Foxboro");
		
		Car c3 = cmanager.getByRoadAndNumber("BA", "3");
		Car c4 = cmanager.getByRoadAndNumber("BB", "4");
		
		Location Acton = lmanager.getLocationByName("Acton MA");
		Location Clinton = lmanager.getLocationByName("Clinton MA");
		Location Danbury = lmanager.getLocationByName("Danbury MA");
		Location Essex = lmanager.getLocationByName("Essex MA");
		Location Foxboro = lmanager.getLocationByName("Foxboro MA");
		
		Track AS1 = Acton.getTrackByName("Acton Siding 1", Track.SPUR);
		Track CS1 = Clinton.getTrackByName("Clinton Siding 1", Track.SPUR);
		Track DS1 = Danbury.getTrackByName("Danbury Siding 1", Track.SPUR);
		Track DS2 = Danbury.getTrackByName("Danbury Siding 2", Track.SPUR);
		Track ES1 = Essex.getTrackByName("Essex Siding 1", Track.SPUR);
		Track ES2 = Essex.getTrackByName("Essex Siding 2", Track.SPUR);
		Track FS1 = Foxboro.getTrackByName("Foxboro Siding 1", Track.SPUR);
		
		// set the depart track Acton to service by local train only
		AS1.setTrainDirections(0);
		
		// create schedules
		ScheduleManager scheduleManager = ScheduleManager.instance();
		Schedule schA = scheduleManager.newSchedule("Schedule A");		
		ScheduleItem schAItem1 = schA.addItem("Boxcar");
		schAItem1.setReceiveLoadName("Food");
		schAItem1.setShipLoadName("Metal");
		schAItem1.setDestination(Danbury);
		schAItem1.setDestinationTrack(DS2);
		
		Schedule schB = scheduleManager.newSchedule("Schedule B");		
		ScheduleItem schBItem1 = schB.addItem("Flat");
		schBItem1.setReceiveLoadName("Food");
		schBItem1.setShipLoadName("Junk");
		schBItem1.setDestination(Foxboro);
		schBItem1.setDestinationTrack(FS1);
		
		Schedule schC = scheduleManager.newSchedule("Schedule C");		
		ScheduleItem schCItem1 = schC.addItem("Boxcar");
		schCItem1.setShipLoadName("Screws");
		schCItem1.setDestination(Essex);
		
		Schedule schD = scheduleManager.newSchedule("Schedule D");		
		ScheduleItem schDItem1 = schD.addItem("Boxcar");
		schDItem1.setReceiveLoadName("Screws");
		schDItem1.setShipLoadName("Nails");
		schDItem1.setWait(1);
		schDItem1.setDestination(Foxboro);
		schDItem1.setDestinationTrack(FS1);
		
		// Add schedule to tracks
		DS1.setScheduleId(schB.getId());
		DS2.setScheduleId(schC.getId());
		ES1.setScheduleId(schD.getId());
		ES2.setScheduleId(schA.getId());
		CS1.setScheduleId(schA.getId());
		
		// bias track
		ES2.setMoves(0);
		CS1.setMoves(1);
		DS2.setMoves(50);
		
		// place cars
		Assert.assertEquals("Place car", Track.OKAY, c3.setLocation(Acton, AS1));
		Assert.assertEquals("Place car", Track.OKAY, c4.setLocation(Acton, AS1));
		
		// c3 (BA 3) is a Boxcar
		c3.setLoadName("Food");	// Track Essex Siding 2 schedule is demanding this car
		c3.setReturnWhenEmptyDestination(Foxboro);

		// c4 (BB 4) is a Flat
		c4.setLoadName("Food");	// Track Danbury Siding 1 schedule is demanding this car
		
		// build train
		// Car c3 should be routed to Essex using 5 trains
		ActonTrain.build();
		Assert.assertTrue("Acton train built", ActonTrain.isBuilt());
		
		// check car destinations
		Assert.assertEquals("Car BA 3 destination","Acton MA", c3.getDestinationName());
		Assert.assertEquals("Car BA 3 destination track","Acton Interchange", c3.getDestinationTrackName());
		Assert.assertEquals("Car BA 3 final destination","Essex MA", c3.getFinalDestinationName());
		Assert.assertEquals("Car BA 3 final destination track","Essex Siding 2", c3.getFinalDestinationTrackName());
		Assert.assertEquals("Car BB 4 destination","Acton MA", c4.getDestinationName());
		Assert.assertEquals("Car BB 4 destination track","Acton Interchange", c4.getDestinationTrackName());
		Assert.assertEquals("Car BB 4 final destination","Danbury MA", c4.getFinalDestinationName());
		Assert.assertEquals("Car BB 4 final destination track","Danbury Siding 1", c4.getFinalDestinationTrackName());
		
		ActonTrain.reset();
		// check car destinations after reset
		Assert.assertEquals("Car BA 3 destination","", c3.getDestinationName());
		Assert.assertEquals("Car BA 3 destination track","", c3.getDestinationTrackName());
		Assert.assertEquals("Car BA 3 final destination","", c3.getFinalDestinationName());
		Assert.assertEquals("Car BA 3 final destination track","", c3.getFinalDestinationTrackName());
		Assert.assertEquals("Car BB 4 destination","", c4.getDestinationName());
		Assert.assertEquals("Car BB 4 destination track","", c4.getDestinationTrackName());
		Assert.assertEquals("Car BB 4 final destination","", c4.getFinalDestinationName());
		Assert.assertEquals("Car BB 4 final destination track","", c4.getFinalDestinationTrackName());
		
		// bias track
		ES2.setMoves(100);
		
		ActonTrain.reset();
		
		// build train
		ActonTrain.build();
		Assert.assertTrue("Acton train built", ActonTrain.isBuilt());

		Assert.assertEquals("Car BA 3 destination","Acton MA", c3.getDestinationName());
		Assert.assertEquals("Car BA 3 destination track","Acton Interchange", c3.getDestinationTrackName());
		Assert.assertEquals("Car BA 3 final destination","Clinton MA", c3.getFinalDestinationName());
		Assert.assertEquals("Car BA 3 final destination track","Clinton Siding 1", c3.getFinalDestinationTrackName());
		Assert.assertEquals("Car BB 4 destination","Acton MA", c4.getDestinationName());
		Assert.assertEquals("Car BB 4 destination track","Acton Interchange", c4.getDestinationTrackName());
		Assert.assertEquals("Car BB 4 final destination","Danbury MA", c4.getFinalDestinationName());
		Assert.assertEquals("Car BB 4 final destination track","Danbury Siding 1", c4.getFinalDestinationTrackName());
		
		// check next loads
		//Assert.assertEquals("Car BA 3 load","Metal", c3.getNextLoad());
		//Assert.assertEquals("Car BB 4 load","Junk", c4.getNextLoad());
		
		ActonTrain.terminate();
		
		// check destinations
		Assert.assertEquals("Car BA 3 destination","", c3.getDestinationName());
		Assert.assertEquals("Car BA 3 destination track","", c3.getDestinationTrackName());
		Assert.assertEquals("Car BA 3 final destination","Clinton MA", c3.getFinalDestinationName());
		Assert.assertEquals("Car BA 3 final destination track","Clinton Siding 1", c3.getFinalDestinationTrackName());
		Assert.assertEquals("Car BB 4 destination","", c4.getDestinationName());
		Assert.assertEquals("Car BB 4 destination track","", c4.getDestinationTrackName());
		Assert.assertEquals("Car BB 4 final destination","Danbury MA", c4.getFinalDestinationName());
		Assert.assertEquals("Car BB 4 final destination track","Danbury Siding 1", c4.getFinalDestinationTrackName());
		
		// check load
		Assert.assertEquals("Car BA 3 load","Food", c3.getLoadName());
		Assert.assertEquals("Car BB 4 load","Food", c4.getLoadName());
		
		// check next loads
		Assert.assertEquals("Car BA 3 load","", c3.getNextLoadName());
		Assert.assertEquals("Car BB 4 load","", c4.getNextLoadName());
		
		ActonToBedfordTrain.build();
		ActonToBedfordTrain.terminate();
		
		// check destinations
		Assert.assertEquals("Car BA 3 destination","", c3.getDestinationName());
		Assert.assertEquals("Car BA 3 destination track","", c3.getDestinationTrackName());
		// schedule at Clinton (schedule A) forwards car BA 3 to Danbury, load Metal
		Assert.assertEquals("Car BA 3 final destination","Clinton MA", c3.getFinalDestinationName());
		Assert.assertEquals("Car BA 3 final destination track","Clinton Siding 1", c3.getFinalDestinationTrackName());
		Assert.assertEquals("Car BB 4 destination","", c4.getDestinationName());
		Assert.assertEquals("Car BB 4 destination track","", c4.getDestinationTrackName());
		Assert.assertEquals("Car BB 4 final destination","Danbury MA", c4.getFinalDestinationName());
		Assert.assertEquals("Car BB 4 final destination track","Danbury Siding 1", c4.getFinalDestinationTrackName());
		
		// check load
		Assert.assertEquals("Car BA 3 load","Food", c3.getLoadName());
		Assert.assertEquals("Car BB 4 load","Food", c4.getLoadName());
		
		// check next loads
		Assert.assertEquals("Car BA 3 load","", c3.getNextLoadName());
		Assert.assertEquals("Car BB 4 load","", c4.getNextLoadName());
		
		BedfordToClintonTrain.build();
		BedfordToClintonTrain.terminate();
		
		// check destinations
		
		Assert.assertEquals("Car BA 3 destination","", c3.getDestinationName());
		Assert.assertEquals("Car BA 3 destination track","", c3.getDestinationTrackName());
		// schedule at Danbury (schedule C) forwards car BA 3 to Essex, no track specified, load Screws
		// schedule at Clinton (schedule A) forwards car BA 3 to Danbury, load Metal
		Assert.assertEquals("Car BA 3 final destination","Danbury MA", c3.getFinalDestinationName());
		Assert.assertEquals("Car BA 3 final destination track","Danbury Siding 2", c3.getFinalDestinationTrackName());
		// schedule at Danbury (schedule B) forwards car BB 4 to Foxboro load Junk
		Assert.assertEquals("Car BB 4 destination","", c4.getDestinationName());
		Assert.assertEquals("Car BB 4 destination track","", c4.getDestinationTrackName());
		Assert.assertEquals("Car BB 4 final destination","Danbury MA", c4.getFinalDestinationName());
		Assert.assertEquals("Car BB 4 final destination track","Danbury Siding 1", c4.getFinalDestinationTrackName());
		
		// check load
		Assert.assertEquals("Car BA 3 load","Metal", c3.getLoadName());
		Assert.assertEquals("Car BB 4 load","Food", c4.getLoadName());
		
		// check next loads
		Assert.assertEquals("Car BA 3 load","", c3.getNextLoadName());
		Assert.assertEquals("Car BB 4 load","", c4.getNextLoadName());
		
		ClintonToDanburyTrain.build();
		ClintonToDanburyTrain.terminate();
		
		// Train has arrived at Danbury, check destinations
		// schedule at Danbury (schedule C) forwards car BA 3 to Essex, no track specified, load Screws
		Assert.assertEquals("Car BA 3 destination","", c3.getDestinationName());
		Assert.assertEquals("Car BA 3 destination track","", c3.getDestinationTrackName());
		Assert.assertEquals("Car BA 3 final destination","Essex MA", c3.getFinalDestinationName());
		Assert.assertEquals("Car BA 3 final destination track","", c3.getFinalDestinationTrackName());
		// schedule at Danbury (schedule B) forward car BB 4 to Foxboro Siding 1.
		Assert.assertEquals("Car BB 4 destination","", c4.getDestinationName());
		Assert.assertEquals("Car BB 4 destination track","", c4.getDestinationTrackName());
		Assert.assertEquals("Car BB 4 final destination","Foxboro MA", c4.getFinalDestinationName());
		Assert.assertEquals("Car BB 4 final destination track","Foxboro Siding 1", c4.getFinalDestinationTrackName());
		
		// check load
		Assert.assertEquals("Car BA 3 load","Screws", c3.getLoadName());
		Assert.assertEquals("Car BB 4 load","Junk", c4.getLoadName());
		
		// check next loads
		Assert.assertEquals("Car BA 3 load","", c3.getNextLoadName());
		Assert.assertEquals("Car BB 4 load","", c4.getNextLoadName());
		
		DanburyToEssexTrain.build();
		
		// schedule D at Essex Siding 1 is requesting load Screws, ship Nails  then forward car to Foxboro Siding 1
		Assert.assertEquals("Car BA 3 destination track","Essex Siding 1", c3.getDestinationTrackName());
		Assert.assertEquals("Car BA 3 final destination","Foxboro MA", c3.getFinalDestinationName());
		Assert.assertEquals("Car BA 3 final destination track","Foxboro Siding 1", c3.getFinalDestinationTrackName());
		
		// check next loads
		Assert.assertEquals("Car BA 3 load","Nails", c3.getNextLoadName());
		Assert.assertEquals("Car BB 4 load","", c4.getNextLoadName());
		
		// check next wait
		Assert.assertEquals("Car BA 3 has wait", 1, c3.getNextWait());
		Assert.assertEquals("Car BB 4 has no wait", 0, c4.getNextWait());
		
		DanburyToEssexTrain.terminate();
		
		// Train has arrived at Essex, check destinations
		// schedule at Essex (schedule D) forwards car BA 3 to Foxboro Siding 1 load Nails, wait = 1
		Assert.assertEquals("Car BA 3 destination","", c3.getDestinationName());
		Assert.assertEquals("Car BA 3 destination track","", c3.getDestinationTrackName());
		Assert.assertEquals("Car BA 3 final destination","Foxboro MA", c3.getFinalDestinationName());
		Assert.assertEquals("Car BA 3 final destination track","Foxboro Siding 1", c3.getFinalDestinationTrackName());

		Assert.assertEquals("Car BB 4 destination","", c4.getDestinationName());
		Assert.assertEquals("Car BB 4 destination track","", c4.getDestinationTrackName());
		Assert.assertEquals("Car BB 4 final destination","Foxboro MA", c4.getFinalDestinationName());
		Assert.assertEquals("Car BB 4 final destination track","Foxboro Siding 1", c4.getFinalDestinationTrackName());
		
		// check load
		Assert.assertEquals("Car BA 3 load","Nails", c3.getLoadName());
		Assert.assertEquals("Car BB 4 load","Junk", c4.getLoadName());
		
		// check next loads
		Assert.assertEquals("Car BA 3 load","", c3.getNextLoadName());
		Assert.assertEquals("Car BB 4 load","", c4.getNextLoadName());
		
		// check wait
		Assert.assertEquals("Car BA 3 has wait", 1, c3.getWait());
		Assert.assertEquals("Car BB 4 has no wait", 0, c4.getWait());
		
		// check next wait
		Assert.assertEquals("Car BA 3 has wait", 0, c3.getNextWait());
		Assert.assertEquals("Car BB 4 has no wait", 0, c4.getNextWait());
		
		EssexToFoxboroTrain.build();
		
		// confirm that only BB 4 is in train, BA 3 has wait = 1
		Assert.assertEquals("Car BA 3 not in train", null, c3.getTrain());
		Assert.assertEquals("Car BB 4 in train", EssexToFoxboroTrain, c4.getTrain());		
		EssexToFoxboroTrain.terminate();
		
		// Train has arrived at Foxboro, check destinations
		Assert.assertEquals("Car BA 3 destination","", c3.getDestinationName());
		Assert.assertEquals("Car BA 3 destination track","", c3.getDestinationTrackName());
		Assert.assertEquals("Car BA 3 final destination","Foxboro MA", c3.getFinalDestinationName());
		Assert.assertEquals("Car BA 3 final destination track","Foxboro Siding 1", c3.getFinalDestinationTrackName());

		Assert.assertEquals("Car BB 4 destination","", c4.getDestinationName());
		Assert.assertEquals("Car BB 4 destination track","", c4.getDestinationTrackName());
		Assert.assertEquals("Car BB 4 final destination","", c4.getFinalDestinationName());
		Assert.assertEquals("Car BB 4 final destination track","", c4.getFinalDestinationTrackName());
		
		// check load
		Assert.assertEquals("Car BA 3 load","Nails", c3.getLoadName());
		Assert.assertEquals("Car BB 4 load","E", c4.getLoadName());
		
		// check next loads
		Assert.assertEquals("Car BA 3 load","", c3.getNextLoadName());
		Assert.assertEquals("Car BB 4 load","", c4.getNextLoadName());
		
		// check wait
		Assert.assertEquals("Car BA 3 has no wait", 0, c3.getWait());
		Assert.assertEquals("Car BB 4 has no wait", 0, c4.getWait());
	
		EssexToFoxboroTrain.build();
		// confirm that only BA 3 is in train
		Assert.assertEquals("Car BA 3 in train", EssexToFoxboroTrain, c3.getTrain());
		Assert.assertEquals("Car BB 4 not in train", null, c4.getTrain());		
		EssexToFoxboroTrain.terminate();
		
		// Train has arrived again at Foxboro, check destinations
		Assert.assertEquals("Car BA 3 destination","", c3.getDestinationName());
		Assert.assertEquals("Car BA 3 destination track","", c3.getDestinationTrackName());
		// Car BA 3 has return when empty destination of Foxboro, no track
		Assert.assertEquals("Car BA 3 final destination","Foxboro MA", c3.getFinalDestinationName());
		Assert.assertEquals("Car BA 3 final destination track","", c3.getFinalDestinationTrackName());

		Assert.assertEquals("Car BB 4 destination","", c4.getDestinationName());
		Assert.assertEquals("Car BB 4 destination track","", c4.getDestinationTrackName());
		Assert.assertEquals("Car BB 4 final destination","", c4.getFinalDestinationName());
		Assert.assertEquals("Car BB 4 final destination track","", c4.getFinalDestinationTrackName());
		
		// check load
		Assert.assertEquals("Car BA 3 load","E", c3.getLoadName());
		Assert.assertEquals("Car BB 4 load","E", c4.getLoadName());
		
		// check next loads
		Assert.assertEquals("Car BA 3 load","", c3.getNextLoadName());
		Assert.assertEquals("Car BB 4 load","", c4.getNextLoadName());
	}
	
	/*
	 * Using the setup from the previous tests, use trains and schedules to move
	 * cars. This test creates 1 schedule with multiple items. Four cars are
	 * used, three boxcars and a flat. They should be routed to the correct
	 * schedule that is demanding the car type and load.
	 */
	public void testRoutingWithSchedules() {
		TrainManager tmanager = TrainManager.instance();
		CarManager cmanager = CarManager.instance();
		LocationManager lmanager = LocationManager.instance();
		
		loadLocationsTrainsAndCars();

		List<Train> trains = tmanager.getTrainsByNameList();
		Assert.assertEquals("confirm number of trains", 7, trains.size());
		
		Train ActonTrain = tmanager.getTrainByName("Acton Local");
		Train ActonToBedfordTrain = tmanager.getTrainByName("Acton to Bedford");
		Train BedfordToClintonTrain = tmanager.getTrainByName("Bedford to Clinton");
		
		Car c3 = cmanager.getByRoadAndNumber("BA", "3");
		Car c4 = cmanager.getByRoadAndNumber("BB", "4");
		Car c5 = cmanager.getByRoadAndNumber("BC", "5");
		Car c6 = cmanager.getByRoadAndNumber("BD", "6");
		
		Location Acton = lmanager.getLocationByName("Acton MA");
		Location Clinton = lmanager.getLocationByName("Clinton MA");
		Location Danbury = lmanager.getLocationByName("Danbury MA");
		Location Essex = lmanager.getLocationByName("Essex MA");
		
		Track AS1 = Acton.getTrackByName("Acton Siding 1", Track.SPUR);
		Track CS1 = Clinton.getTrackByName("Clinton Siding 1", Track.SPUR);
		Track DS1 = Danbury.getTrackByName("Danbury Siding 1", Track.SPUR);
		Track DS2 = Danbury.getTrackByName("Danbury Siding 2", Track.SPUR);
		Track ES1 = Essex.getTrackByName("Essex Siding 1", Track.SPUR);
		Track ES2 = Essex.getTrackByName("Essex Siding 2", Track.SPUR);
		
		// set the depart track Acton to service by local train only
		AS1.setTrainDirections(0);
		
		// create schedules
		ScheduleManager scheduleManager = ScheduleManager.instance();
		Schedule schA = scheduleManager.newSchedule("Schedule AA");		
		ScheduleItem schAItem1 = schA.addItem("Boxcar");
		schAItem1.setReceiveLoadName("Empty");
		schAItem1.setShipLoadName("Metal");
		schAItem1.setDestination(Acton);
		schAItem1.setDestinationTrack(AS1);
		ScheduleItem schAItem2 = schA.addItem("Flat");
		schAItem2.setReceiveLoadName("Junk");
		schAItem2.setShipLoadName("Metal");
		schAItem2.setDestination(Danbury);
		schAItem2.setDestinationTrack(DS2);
		ScheduleItem schAItem3 = schA.addItem("Boxcar");
		schAItem3.setReceiveLoadName("Boxes");
		schAItem3.setShipLoadName("Screws");
		schAItem3.setDestination(Danbury);
		schAItem3.setDestinationTrack(DS1);
		
		// Add schedule to tracks
		CS1.setScheduleId(schA.getId());
		CS1.setScheduleMode(Track.SEQUENTIAL);
		DS1.setScheduleId("");
		DS2.setScheduleId("");
		ES1.setScheduleId("");
		ES2.setScheduleId("");
		
		// c3 (BA 3) is a Boxcar
		c3.setLoadName("Empty");
		
		// c4 (BB 4) is a Flat
		c4.setLoadName("Junk");
		
		c5.setLoadName("Boxes");
		
		c6.setLoadName("Empty");
		
		// place cars
		Assert.assertEquals("Place car", Track.OKAY, c3.setLocation(Acton, AS1));
		Assert.assertEquals("Place car", Track.OKAY, c4.setLocation(Acton, AS1));
		Assert.assertEquals("Place car", Track.OKAY, c5.setLocation(Acton, AS1));
		Assert.assertEquals("Place car", Track.OKAY, c6.setLocation(Acton, AS1));
		
		// note car move count is exactly the same order as schedule
		// build train
		ActonTrain.build();
		Assert.assertTrue("Acton train built", ActonTrain.isBuilt());
		
		// check car destinations
		Assert.assertEquals("Car BA 3 destination","Acton MA", c3.getDestinationName());
		Assert.assertEquals("Car BA 3 destination track","Acton Interchange", c3.getDestinationTrackName());
		Assert.assertEquals("Car BA 3 final destination","Clinton MA", c3.getFinalDestinationName());
		Assert.assertEquals("Car BA 3 final destination track","Clinton Siding 1", c3.getFinalDestinationTrackName());
		Assert.assertEquals("Car BB 4 destination","Acton MA", c4.getDestinationName());
		Assert.assertEquals("Car BB 4 destination track","Acton Interchange", c4.getDestinationTrackName());
		Assert.assertEquals("Car BB 4 final destination","Clinton MA", c4.getFinalDestinationName());
		Assert.assertEquals("Car BB 4 final destination track","Clinton Siding 1", c4.getFinalDestinationTrackName());
		Assert.assertEquals("Car BC 5 destination","Acton MA", c5.getDestinationName());
		Assert.assertEquals("Car BC 5 destination track","Acton Interchange", c5.getDestinationTrackName());
		Assert.assertEquals("Car BC 5 final destination","Clinton MA", c5.getFinalDestinationName());
		Assert.assertEquals("Car BC 5 final destination track","Clinton Siding 1", c5.getFinalDestinationTrackName());
		Assert.assertEquals("Car BD 6 destination","Acton MA", c6.getDestinationName());
		Assert.assertEquals("Car BD 6 destination track","Acton Interchange", c6.getDestinationTrackName());
		Assert.assertEquals("Car BD 6 final destination","Clinton MA", c6.getFinalDestinationName());
		Assert.assertEquals("Car BD 6 final destination track","Clinton Siding 1", c6.getFinalDestinationTrackName());
		
		// check car schedule ids
		Assert.assertEquals("Car BA 3 schedule id", schAItem1.getId(), c3.getScheduleId());
		Assert.assertEquals("Car BB 4 schedule id", schAItem2.getId(), c4.getScheduleId());
		Assert.assertEquals("Car BC 5 schedule id", schAItem3.getId(), c5.getScheduleId());
		Assert.assertEquals("Car BD 6 schedule id", schAItem1.getId(), c6.getScheduleId());

		ActonTrain.reset();
		
		// Next car in schedule is flat car
		// build train
		ActonTrain.build();
		Assert.assertTrue("Acton train built", ActonTrain.isBuilt());
		
		// check car destinations
		Assert.assertEquals("Car BA 3 destination","Acton MA", c3.getDestinationName());
		Assert.assertEquals("Car BA 3 destination track","Acton Yard", c3.getDestinationTrackName());
		Assert.assertEquals("Car BA 3 final destination","", c3.getFinalDestinationName());
		Assert.assertEquals("Car BA 3 final destination track","", c3.getFinalDestinationTrackName());
		Assert.assertEquals("Car BB 4 destination","Acton MA", c4.getDestinationName());
		Assert.assertEquals("Car BB 4 destination track","Acton Interchange", c4.getDestinationTrackName());
		Assert.assertEquals("Car BB 4 final destination","Clinton MA", c4.getFinalDestinationName());
		Assert.assertEquals("Car BB 4 final destination track","Clinton Siding 1", c4.getFinalDestinationTrackName());
		Assert.assertEquals("Car BC 5 destination","Acton MA", c5.getDestinationName());
		Assert.assertEquals("Car BC 5 destination track","Acton Interchange", c5.getDestinationTrackName());
		Assert.assertEquals("Car BC 5 final destination","Clinton MA", c5.getFinalDestinationName());
		Assert.assertEquals("Car BC 5 final destination track","Clinton Siding 1", c5.getFinalDestinationTrackName());
		Assert.assertEquals("Car BD 6 destination","Acton MA", c6.getDestinationName());
		Assert.assertEquals("Car BD 6 destination track","Acton Interchange", c6.getDestinationTrackName());
		Assert.assertEquals("Car BD 6 final destination","Clinton MA", c6.getFinalDestinationName());
		Assert.assertEquals("Car BD 6 final destination track","Clinton Siding 1", c6.getFinalDestinationTrackName());
		
		// check car schedule ids
		Assert.assertEquals("Car BA 3 schedule id", "", c3.getScheduleId());
		Assert.assertEquals("Car BB 4 schedule id", schAItem2.getId(), c4.getScheduleId());
		Assert.assertEquals("Car BC 5 schedule id", schAItem3.getId(), c5.getScheduleId());
		Assert.assertEquals("Car BD 6 schedule id", schAItem1.getId(), c6.getScheduleId());
		
		ActonTrain.terminate();
		// move the cars to Bedford
		ActonToBedfordTrain.build();
		Assert.assertTrue("Bedford train built", ActonToBedfordTrain.isBuilt());
		
		// check car destinations
		Assert.assertEquals("Car BA 3 destination","Bedford MA", c3.getDestinationName());
		Assert.assertEquals("Car BA 3 destination track","Bedford Yard", c3.getDestinationTrackName());
		Assert.assertEquals("Car BA 3 final destination","", c3.getFinalDestinationName());
		Assert.assertEquals("Car BA 3 final destination track","", c3.getFinalDestinationTrackName());
		Assert.assertEquals("Car BB 4 destination","Bedford MA", c4.getDestinationName());
		Assert.assertEquals("Car BB 4 destination track","Bedford Interchange", c4.getDestinationTrackName());
		Assert.assertEquals("Car BB 4 final destination","Clinton MA", c4.getFinalDestinationName());
		Assert.assertEquals("Car BB 4 final destination track","Clinton Siding 1", c4.getFinalDestinationTrackName());
		Assert.assertEquals("Car BC 5 destination","Bedford MA", c5.getDestinationName());
		Assert.assertEquals("Car BC 5 destination track","Bedford Interchange", c5.getDestinationTrackName());
		Assert.assertEquals("Car BC 5 final destination","Clinton MA", c5.getFinalDestinationName());
		Assert.assertEquals("Car BC 5 final destination track","Clinton Siding 1", c5.getFinalDestinationTrackName());
		Assert.assertEquals("Car BD 6 destination","Bedford MA", c6.getDestinationName());
		Assert.assertEquals("Car BD 6 destination track","Bedford Interchange", c6.getDestinationTrackName());
		Assert.assertEquals("Car BD 6 final destination","Clinton MA", c6.getFinalDestinationName());
		Assert.assertEquals("Car BD 6 final destination track","Clinton Siding 1", c6.getFinalDestinationTrackName());
		
		// check car schedule ids
		Assert.assertEquals("Car BA 3 schedule id", "", c3.getScheduleId());
		Assert.assertEquals("Car BB 4 schedule id", schAItem2.getId(), c4.getScheduleId());
		Assert.assertEquals("Car BC 5 schedule id", schAItem3.getId(), c5.getScheduleId());
		Assert.assertEquals("Car BD 6 schedule id", schAItem1.getId(), c6.getScheduleId());
		
		ActonToBedfordTrain.terminate();
		// move the cars to Bedford
		BedfordToClintonTrain.build();
		Assert.assertTrue("Bedford train built", BedfordToClintonTrain.isBuilt());
		
		// check car destinations
		Assert.assertEquals("Car BA 3 destination","Clinton MA", c3.getDestinationName());
		Assert.assertEquals("Car BA 3 destination track","Clinton Yard", c3.getDestinationTrackName());
		Assert.assertEquals("Car BA 3 final destination","", c3.getFinalDestinationName());
		Assert.assertEquals("Car BA 3 final destination track","", c3.getFinalDestinationTrackName());
		Assert.assertEquals("Car BB 4 destination","Clinton MA", c4.getDestinationName());
		Assert.assertEquals("Car BB 4 destination track","Clinton Siding 1", c4.getDestinationTrackName());
		Assert.assertEquals("Car BB 4 final destination","Danbury MA", c4.getFinalDestinationName());
		Assert.assertEquals("Car BB 4 final destination track","Danbury Siding 2", c4.getFinalDestinationTrackName());
		Assert.assertEquals("Car BC 5 destination","Clinton MA", c5.getDestinationName());
		Assert.assertEquals("Car BC 5 destination track","Clinton Siding 1", c5.getDestinationTrackName());
		Assert.assertEquals("Car BC 5 final destination","Danbury MA", c5.getFinalDestinationName());
		Assert.assertEquals("Car BC 5 final destination track","Danbury Siding 1", c5.getFinalDestinationTrackName());
		Assert.assertEquals("Car BD 6 destination","Clinton MA", c6.getDestinationName());
		Assert.assertEquals("Car BD 6 destination track","Clinton Siding 1", c6.getDestinationTrackName());
		Assert.assertEquals("Car BD 6 final destination","Acton MA", c6.getFinalDestinationName());
		Assert.assertEquals("Car BD 6 final destination track","Acton Siding 1", c6.getFinalDestinationTrackName());
		
		// check car schedule ids
		Assert.assertEquals("Car BA 3 schedule id", "", c3.getScheduleId());
		Assert.assertEquals("Car BB 4 schedule id", "", c4.getScheduleId());
		Assert.assertEquals("Car BC 5 schedule id", "", c5.getScheduleId());
		Assert.assertEquals("Car BD 6 schedule id", "", c6.getScheduleId());
		
		BedfordToClintonTrain.terminate();
			
	}
	
	/*
	 * Using the setup from the previous tests, use trains and schedules to move
	 * cars. This test creates 1 schedule in match mode with multiple items.
	 * Test uses car loads to activate schedule.
	 */
	public void testRoutingWithSchedulesMatchMode() {
		TrainManager tmanager = TrainManager.instance();
		CarManager cmanager = CarManager.instance();
		LocationManager lmanager = LocationManager.instance();
		
		loadLocationsTrainsAndCars();

		List<Train> trains = tmanager.getTrainsByNameList();
		Assert.assertEquals("confirm number of trains", 7, trains.size());
		
		Car c3 = cmanager.getByRoadAndNumber("BA", "3");
		Car c4 = cmanager.getByRoadAndNumber("BB", "4");
		Car c5 = cmanager.getByRoadAndNumber("BC", "5");
		Car c6 = cmanager.getByRoadAndNumber("BD", "6");
		Car c7 = cmanager.getByRoadAndNumber("BA", "7");
		Car c8 = cmanager.getByRoadAndNumber("BB", "8");
		Car c9 = cmanager.getByRoadAndNumber("BC", "9");
		
		Location Acton = lmanager.getLocationByName("Acton MA");
		Location Clinton = lmanager.getLocationByName("Clinton MA");
		Location Danbury = lmanager.getLocationByName("Danbury MA");
		Location Essex = lmanager.getLocationByName("Essex MA");
		
		Track AS1 = Acton.getTrackByName("Acton Siding 1", Track.SPUR);
		Track CS1 = Clinton.getTrackByName("Clinton Siding 1", Track.SPUR);
		Track DS1 = Danbury.getTrackByName("Danbury Siding 1", Track.SPUR);
		Track DS2 = Danbury.getTrackByName("Danbury Siding 2", Track.SPUR);
		Track ES1 = Essex.getTrackByName("Essex Siding 1", Track.SPUR);
		Track ES2 = Essex.getTrackByName("Essex Siding 2", Track.SPUR);

		// set the depart track Acton to service by local train only
		AS1.setTrainDirections(0);
		
		// create schedules
		ScheduleManager scheduleManager = ScheduleManager.instance();
		Schedule schA = scheduleManager.newSchedule("Schedule AAA");
		ScheduleItem schAItem1 = schA.addItem("Boxcar");
		schAItem1.setReceiveLoadName("Empty");
		schAItem1.setShipLoadName("Metal");
		schAItem1.setDestination(Acton);
		schAItem1.setDestinationTrack(AS1);
		ScheduleItem schAItem2 = schA.addItem("Flat");
		schAItem2.setReceiveLoadName("Junk");
		schAItem2.setShipLoadName("Metal");
		schAItem2.setDestination(Danbury);
		schAItem2.setDestinationTrack(DS2);
		ScheduleItem schAItem3 = schA.addItem("Boxcar");
		schAItem3.setReceiveLoadName("Boxes");
		schAItem3.setShipLoadName("Screws");
		schAItem3.setDestination(Essex);
		schAItem3.setDestinationTrack(ES1);
		ScheduleItem schAItem4 = schA.addItem("Boxcar");
		schAItem4.setReceiveLoadName("Boxes");
		schAItem4.setShipLoadName("Bolts");
		schAItem4.setDestination(Danbury);
		schAItem4.setDestinationTrack(DS1);
		ScheduleItem schAItem5 = schA.addItem("Boxcar");
		schAItem5.setReceiveLoadName("");
		schAItem5.setShipLoadName("Nuts");
		schAItem5.setDestination(Essex);
		schAItem5.setDestinationTrack(ES2);
		
		// Add schedule to tracks
		CS1.setScheduleId("");
		ES1.setScheduleId(schA.getId());
		ES1.setScheduleMode(Track.MATCH);	// set schedule into match mode
		
		// c3 (BA 3) is a Boxcar
		c3.setLoadName("Boxes");
		
		// c4 (BB 4) is a Flat
		c4.setLoadName("Junk");
		
		// c5 (BC 5) is a Boxcar
		c5.setLoadName("Boxes");
		
		// c6 (BD 6) is a Boxcar
		c6.setLoadName("Boxes");
		
		// place cars
		Assert.assertEquals("Place car", Track.OKAY, c3.setLocation(Acton, AS1));
		Assert.assertEquals("Place car", Track.OKAY, c4.setLocation(Acton, AS1));
		Assert.assertEquals("Place car", Track.OKAY, c5.setLocation(Acton, AS1));
		Assert.assertEquals("Place car", Track.OKAY, c6.setLocation(Acton, AS1));
		Assert.assertEquals("Place car", Track.OKAY, c7.setLocation(Acton, AS1));
		Assert.assertEquals("Place car", Track.OKAY, c8.setLocation(Acton, AS1));
		Assert.assertEquals("Place car", Track.OKAY, c9.setLocation(Acton, AS1));
		
		// build train
		Train ActonTrain = tmanager.getTrainByName("Acton Local");
		ActonTrain.build();
		Assert.assertTrue("Acton train built", ActonTrain.isBuilt());
		
		// check car destinations
		Assert.assertEquals("Car BA 3 destination","Acton MA", c3.getDestinationName());
		Assert.assertEquals("Car BA 3 destination track","Acton Interchange", c3.getDestinationTrackName());
		Assert.assertEquals("Car BA 3 final destination","Essex MA", c3.getFinalDestinationName());
		Assert.assertEquals("Car BA 3 final destination track","Essex Siding 1", c3.getFinalDestinationTrackName());
		Assert.assertEquals("Car BB 4 destination","Acton MA", c4.getDestinationName());
		Assert.assertEquals("Car BB 4 destination track","Acton Interchange", c4.getDestinationTrackName());
		Assert.assertEquals("Car BB 4 final destination","Essex MA", c4.getFinalDestinationName());
		Assert.assertEquals("Car BB 4 final destination track","Essex Siding 1", c4.getFinalDestinationTrackName());
		Assert.assertEquals("Car BC 5 destination","Acton MA", c5.getDestinationName());
		Assert.assertEquals("Car BC 5 destination track","Acton Interchange", c5.getDestinationTrackName());
		Assert.assertEquals("Car BC 5 final destination","Essex MA", c5.getFinalDestinationName());
		Assert.assertEquals("Car BC 5 final destination track","Essex Siding 1", c5.getFinalDestinationTrackName());
		Assert.assertEquals("Car BD 6 destination","Acton MA", c6.getDestinationName());
		Assert.assertEquals("Car BD 6 destination track","Acton Interchange", c6.getDestinationTrackName());
		Assert.assertEquals("Car BD 6 final destination","Essex MA", c6.getFinalDestinationName());
		Assert.assertEquals("Car BD 6 final destination track","Essex Siding 1", c6.getFinalDestinationTrackName());
		Assert.assertEquals("Car BD 7 destination","Acton MA", c7.getDestinationName());
		Assert.assertEquals("Car BD 7 destination track","Acton Interchange", c7.getDestinationTrackName());
		Assert.assertEquals("Car BD 7 final destination","Essex MA", c7.getFinalDestinationName());
		Assert.assertEquals("Car BD 7 final destination track","Essex Siding 1", c7.getFinalDestinationTrackName());

		
		// check car schedule ids
		Assert.assertEquals("Car BA 3 schedule id", schAItem3.getId(), c3.getScheduleId());
		Assert.assertEquals("Car BB 4 schedule id", schAItem2.getId(), c4.getScheduleId());
		Assert.assertEquals("Car BC 5 schedule id", schAItem3.getId(), c5.getScheduleId());
		Assert.assertEquals("Car BD 6 schedule id", schAItem4.getId(), c6.getScheduleId());
		Assert.assertEquals("Car BA 7 schedule id", schAItem5.getId(), c7.getScheduleId());
		Assert.assertEquals("Car BB 8 schedule id", schAItem1.getId(), c8.getScheduleId());
		Assert.assertEquals("Car BC 9 schedule id", schAItem5.getId(), c9.getScheduleId());

		ActonTrain.reset();
	}
	
	/*
	 * Using the setup from the previous tests, use trains and schedules to move
	 * cars. This test creates 1 schedule in match mode with multiple items.
	 * Cars use final destination to activate schedule
	 */
	public void testRoutingWithSchedulesMatchMode2() {
		TrainManager tmanager = TrainManager.instance();
		CarManager cmanager = CarManager.instance();
		LocationManager lmanager = LocationManager.instance();
		
		loadLocationsTrainsAndCars();

		List<Train> trains = tmanager.getTrainsByNameList();
		Assert.assertEquals("confirm number of trains", 7, trains.size());
		
		Car c3 = cmanager.getByRoadAndNumber("BA", "3");
		Car c4 = cmanager.getByRoadAndNumber("BB", "4");
		Car c5 = cmanager.getByRoadAndNumber("BC", "5");
		Car c6 = cmanager.getByRoadAndNumber("BD", "6");
		Car c7 = cmanager.getByRoadAndNumber("BA", "7");
		Car c8 = cmanager.getByRoadAndNumber("BB", "8");
		Car c9 = cmanager.getByRoadAndNumber("BC", "9");
		
		Assert.assertNotNull("confirm car not null", c3);
		Assert.assertNotNull("confirm car not null", c4);
		Assert.assertNotNull("confirm car not null", c5);
		Assert.assertNotNull("confirm car not null", c6);
		Assert.assertNotNull("confirm car not null", c7);
		Assert.assertNotNull("confirm car not null", c8);
		Assert.assertNotNull("confirm car not null", c9);
		
		Location Acton = lmanager.getLocationByName("Acton MA");
		Location Bedford = lmanager.getLocationByName("Bedford MA");
		Location Clinton = lmanager.getLocationByName("Clinton MA");
		Location Danbury = lmanager.getLocationByName("Danbury MA");
		Location Essex = lmanager.getLocationByName("Essex MA");
		Location Foxboro = lmanager.getLocationByName("Foxboro MA");
		
		Track AS1 = Acton.getTrackByName("Acton Siding 1", Track.SPUR);
		Track BS1 = Bedford.getTrackByName("Bedford Siding 1", Track.SPUR);
		Track CS1 = Clinton.getTrackByName("Clinton Siding 1", Track.SPUR);
		Track DS1 = Danbury.getTrackByName("Danbury Siding 1", Track.SPUR);
		Track DS2 = Danbury.getTrackByName("Danbury Siding 2", Track.SPUR);
		Track ES1 = Essex.getTrackByName("Essex Siding 1", Track.SPUR);
		//Track ES2 = Essex.getTrackByName("Essex Siding 2", Track.SIDING);
		Track FS1 = Foxboro.getTrackByName("Foxboro Siding 1", Track.SPUR);
		
		// set the depart track Acton to service by local train only
		AS1.setTrainDirections(0);
		
		// create schedules
		ScheduleManager scheduleManager = ScheduleManager.instance();
		Schedule schA = scheduleManager.newSchedule("Schedule ABC");
		ScheduleItem schAItem1 = schA.addItem("Boxcar");
		//schAItem1.setLoad("Empty");
		schAItem1.setShipLoadName("Metal");
		schAItem1.setDestination(Acton);
		schAItem1.setDestinationTrack(AS1);
		ScheduleItem schAItem2 = schA.addItem("Flat");
		//schAItem2.setLoad("Junk");
		schAItem2.setShipLoadName("Metal");
		schAItem2.setDestination(Danbury);
		schAItem2.setDestinationTrack(DS2);
		ScheduleItem schAItem3 = schA.addItem("Boxcar");
		//schAItem3.setLoad("Boxes");
		schAItem3.setShipLoadName("Screws");
		schAItem3.setDestination(Bedford);
		schAItem3.setDestinationTrack(BS1);
		ScheduleItem schAItem4 = schA.addItem("Boxcar");
		schAItem4.setReceiveLoadName(CarLoads.instance().getDefaultEmptyName());
		schAItem4.setShipLoadName("Bolts");
		schAItem4.setDestination(Danbury);
		schAItem4.setDestinationTrack(DS1);
		ScheduleItem schAItem5 = schA.addItem("Boxcar");
		schAItem5.setReceiveLoadName(CarLoads.instance().getDefaultLoadName());
		schAItem5.setShipLoadName("Nuts");
		schAItem5.setDestination(Foxboro);
		schAItem5.setDestinationTrack(FS1);
		
		// Add schedule to tracks
		CS1.setScheduleId("");
		ES1.setScheduleId(schA.getId());
		ES1.setScheduleMode(Track.MATCH);	// set schedule into match mode
		
		// c3 (BA 3) is a Boxcar
		c3.setLoadName(CarLoads.instance().getDefaultEmptyName());
		c3.setFinalDestination(Essex);
		
		// c4 (BB 4) is a Flat
		c4.setLoadName(CarLoads.instance().getDefaultEmptyName());
		c4.setFinalDestination(Essex);
		c4.setFinalDestinationTrack(ES1);
		
		// c5 (BC 5) is a Boxcar
		c5.setLoadName(CarLoads.instance().getDefaultLoadName());
		c5.setFinalDestination(Essex);
		
		// c6 (BD 6) is a Boxcar
		c6.setLoadName(CarLoads.instance().getDefaultEmptyName());
		c6.setFinalDestination(Essex);
		
		// c7 (BA 7) is a Boxcar
		c7.setLoadName(CarLoads.instance().getDefaultEmptyName());
		c7.setFinalDestination(Essex);
		c7.setFinalDestinationTrack(ES1);

		// c8 (BB 8) is a Boxcar
		c8.setLoadName(CarLoads.instance().getDefaultEmptyName());
		c8.setMoves(20);	// serve BB 8 and BC 9 after the other cars
		
		// c9 (BC 9) is a Boxcar
		c9.setLoadName(CarLoads.instance().getDefaultEmptyName());
		c9.setMoves(21);
		
		// place cars
		Assert.assertEquals("Place car", Track.OKAY, c3.setLocation(Acton, AS1));
		Assert.assertEquals("Place car", Track.OKAY, c4.setLocation(Acton, AS1));
		Assert.assertEquals("Place car", Track.OKAY, c5.setLocation(Acton, AS1));
		Assert.assertEquals("Place car", Track.OKAY, c6.setLocation(Acton, AS1));
		Assert.assertEquals("Place car", Track.OKAY, c7.setLocation(Acton, AS1));
		Assert.assertEquals("Place car", Track.OKAY, c8.setLocation(Danbury, DS1));
		Assert.assertEquals("Place car", Track.OKAY, c9.setLocation(Danbury, DS1));
		
		// build train
		Train ActonTrain = tmanager.getTrainByName("Acton Local");
		ActonTrain.build();
		Assert.assertTrue("Acton train built", ActonTrain.isBuilt());
		
		// check car destinations
		Assert.assertEquals("Car BA 3 destination","Acton MA", c3.getDestinationName());
		Assert.assertEquals("Car BA 3 destination track","Acton Interchange", c3.getDestinationTrackName());
		Assert.assertEquals("Car BA 3 final destination","Essex MA", c3.getFinalDestinationName());
		Assert.assertEquals("Car BA 3 final destination track","", c3.getFinalDestinationTrackName());
		Assert.assertEquals("Car BB 4 destination","Acton MA", c4.getDestinationName());
		Assert.assertEquals("Car BB 4 destination track","Acton Interchange", c4.getDestinationTrackName());
		Assert.assertEquals("Car BB 4 final destination","Essex MA", c4.getFinalDestinationName());
		Assert.assertEquals("Car BB 4 final destination track","Essex Siding 1", c4.getFinalDestinationTrackName());
		Assert.assertEquals("Car BC 5 destination","Acton MA", c5.getDestinationName());
		Assert.assertEquals("Car BC 5 destination track","Acton Interchange", c5.getDestinationTrackName());
		Assert.assertEquals("Car BC 5 final destination","Essex MA", c5.getFinalDestinationName());
		Assert.assertEquals("Car BC 5 final destination track","", c5.getFinalDestinationTrackName());
		Assert.assertEquals("Car BD 6 destination","Acton MA", c6.getDestinationName());
		Assert.assertEquals("Car BD 6 destination track","Acton Interchange", c6.getDestinationTrackName());
		Assert.assertEquals("Car BD 6 final destination","Essex MA", c6.getFinalDestinationName());
		Assert.assertEquals("Car BD 6 final destination track","", c6.getFinalDestinationTrackName());
		Assert.assertEquals("Car BA 7 destination","Acton MA", c7.getDestinationName());
		Assert.assertEquals("Car BA 7 destination track","Acton Interchange", c7.getDestinationTrackName());
		Assert.assertEquals("Car BA 7 final destination","Essex MA", c7.getFinalDestinationName());
		Assert.assertEquals("Car BA 7 final destination track","Essex Siding 1", c7.getFinalDestinationTrackName());
		Assert.assertEquals("Car BB 8 destination","", c8.getDestinationName());
		Assert.assertEquals("Car BB 8 destination track","", c8.getDestinationTrackName());
		Assert.assertEquals("Car BB 8 final destination","", c8.getFinalDestinationName());
		Assert.assertEquals("Car BB 8 final destination track","", c8.getFinalDestinationTrackName());
		Assert.assertEquals("Car BC 9 destination","", c9.getDestinationName());
		Assert.assertEquals("Car BC 9 destination track","", c9.getDestinationTrackName());
		Assert.assertEquals("Car BC 9 final destination","", c9.getFinalDestinationName());
		Assert.assertEquals("Car BC 9 final destination track","", c9.getFinalDestinationTrackName());
		
		// check car schedule ids (Car are being routed by destination not by load so id should be "")
		Assert.assertEquals("Car BA 3 schedule id", "", c3.getScheduleId());
		Assert.assertEquals("Car BB 4 schedule id", "", c4.getScheduleId());
		Assert.assertEquals("Car BC 5 schedule id", "", c5.getScheduleId());
		Assert.assertEquals("Car BD 6 schedule id", "", c6.getScheduleId());
		Assert.assertEquals("Car BA 7 schedule id", "", c7.getScheduleId());
		Assert.assertEquals("Car BB 8 schedule id", "", c8.getScheduleId());
		Assert.assertEquals("Car BC 9 schedule id", "", c9.getScheduleId());

		ActonTrain.terminate();
			
		// move the cars to Bedford
		Train ActonToBedfordTrain = tmanager.getTrainByName("Acton to Bedford");
		ActonToBedfordTrain.build();
		Assert.assertTrue("Acton train built", ActonToBedfordTrain.isBuilt());
		ActonToBedfordTrain.terminate();
		
		// move the cars to Clinton
		Train BedfordToClintonTrain = tmanager.getTrainByName("Bedford to Clinton");
		BedfordToClintonTrain.build();
		Assert.assertTrue("Bedford train built", BedfordToClintonTrain.isBuilt());
		BedfordToClintonTrain.terminate();
		
		// move the cars to Danbury
		Train ClintonToDanburyTrain = tmanager.getTrainByName("Clinton to Danbury");
		ClintonToDanburyTrain.build();
		Assert.assertTrue("Clinton train built", ClintonToDanburyTrain.isBuilt());
		ClintonToDanburyTrain.terminate();
		
		// move the cars to Essex (number of moves is 8)
		Train DanburyToEssexTrain = tmanager.getTrainByName("Danbury to Essex");
		DanburyToEssexTrain.build();
		Assert.assertTrue("Danbury train built", DanburyToEssexTrain.isBuilt());
		
		// check car destinations
		// BA 3 (Boxcar)
		Assert.assertEquals("Car BA 3 destination","Essex MA", c3.getDestinationName());
		Assert.assertEquals("Car BA 3 destination track","Essex Siding 1", c3.getDestinationTrackName());
		// new final destination and load for car BA 3
		Assert.assertEquals("Car BA 3 final destination","Bedford MA", c3.getFinalDestinationName());
		Assert.assertEquals("Car BA 3 final destination track","Bedford Siding 1", c3.getFinalDestinationTrackName());
		Assert.assertEquals("Car BA 3 final load","Screws", c3.getNextLoadName());
		Assert.assertEquals("Car BA 3 schedule id", "", c3.getScheduleId());
		// BB 4 (Flat)
		Assert.assertEquals("Car BB 4 destination","Essex MA", c4.getDestinationName());
		Assert.assertEquals("Car BB 4 destination track","Essex Siding 1", c4.getDestinationTrackName());
		// new final destination and load for car BB 4
		Assert.assertEquals("Car BB 4 final destination","Danbury MA", c4.getFinalDestinationName());
		Assert.assertEquals("Car BB 4 final destination track","Danbury Siding 2", c4.getFinalDestinationTrackName());
		Assert.assertEquals("Car BB 4 final load","Metal", c4.getNextLoadName());
		Assert.assertEquals("Car BB 4 schedule id", "", c4.getScheduleId());
		// BC 5 (Boxcar)
		Assert.assertEquals("Car BC 5 destination","Essex MA", c5.getDestinationName());
		Assert.assertEquals("Car BC 5 destination track","Essex Siding 1", c5.getDestinationTrackName());
		// new final destination and load for car BC 5, same as BA 3
		Assert.assertEquals("Car BC 5 final destination","Bedford MA", c5.getFinalDestinationName());
		Assert.assertEquals("Car BC 5 final destination track","Bedford Siding 1", c5.getFinalDestinationTrackName());
		Assert.assertEquals("Car BC 5 final load","Screws", c5.getNextLoadName());
		Assert.assertEquals("Car BC 5 schedule id", "", c5.getScheduleId());
		// BD 6 (Boxcar) note second Boxcar
		Assert.assertEquals("Car BD 6 destination","Essex MA", c6.getDestinationName());
		Assert.assertEquals("Car BD 6 destination track","Essex Siding 1", c6.getDestinationTrackName());
		// new final destination and load for car BD 6
		Assert.assertEquals("Car BD 6 final destination","Danbury MA", c6.getFinalDestinationName());
		Assert.assertEquals("Car BD 6 final destination track","Danbury Siding 1", c6.getFinalDestinationTrackName());
		Assert.assertEquals("Car BC 6 final load", "Bolts", c6.getNextLoadName());
		Assert.assertEquals("Car BD 6 schedule id", "", c6.getScheduleId());
		// BA 7 (Boxcar) note 3rd Boxcar
		Assert.assertEquals("Car BA 7 destination","Essex MA", c7.getDestinationName());
		Assert.assertEquals("Car BA 7 destination track","Essex Siding 1", c7.getDestinationTrackName());
		// new final destination and load for car BA 7
		Assert.assertEquals("Car BA 7 final destination","Acton MA", c7.getFinalDestinationName());
		Assert.assertEquals("Car BA 7 final destination track","Acton Siding 1", c7.getFinalDestinationTrackName());
		Assert.assertEquals("Car BA 7 final load", "Metal", c7.getNextLoadName());
		Assert.assertEquals("Car BA 7 schedule id", "", c7.getScheduleId());
		// BB 8 (Boxcar) at Danbury to be added to train
		Assert.assertEquals("Car BB 8 destination","Essex MA", c8.getDestinationName());
		Assert.assertEquals("Car BB 8 destination track","Essex Siding 1", c8.getDestinationTrackName());
		// Should match schedule item 16c3
		Assert.assertEquals("Car BB 8 final destination","Bedford MA", c8.getFinalDestinationName());
		Assert.assertEquals("Car BB 8 final destination track","Bedford Siding 1", c8.getFinalDestinationTrackName());
		Assert.assertEquals("Car BB 8 final load", "Screws", c8.getNextLoadName());
		Assert.assertEquals("Car BB 8 schedule id", "", c8.getScheduleId());
		// BB 9 (Boxcar) at Danbury to be added to train
		Assert.assertEquals("Car BC 9 destination","Essex MA", c9.getDestinationName());
		Assert.assertEquals("Car BC 9 destination track","Essex Siding 1", c9.getDestinationTrackName());
		// Should match schedule item 16c4
		Assert.assertEquals("Car BC 9 final destination","Danbury MA", c9.getFinalDestinationName());
		Assert.assertEquals("Car BC 9 final destination track","Danbury Siding 1", c9.getFinalDestinationTrackName());
		Assert.assertEquals("Car BC 9 final load", "Bolts", c9.getNextLoadName());
		Assert.assertEquals("Car BC 9 schedule id", "", c9.getScheduleId());
		
		
		// test reset, car final destinations should revert.
		DanburyToEssexTrain.reset();
		
		Assert.assertEquals("Car BA 3 destination","", c3.getDestinationName());
		Assert.assertEquals("Car BA 3 destination track","", c3.getDestinationTrackName());
		Assert.assertEquals("Car BA 3 final destination","Essex MA", c3.getFinalDestinationName());
		Assert.assertEquals("Car BA 3 final destination track","", c3.getFinalDestinationTrackName());
		Assert.assertEquals("Car BA 3 next load","", c3.getNextLoadName());
		Assert.assertEquals("Car BB 4 destination","", c4.getDestinationName());
		Assert.assertEquals("Car BB 4 destination track","", c4.getDestinationTrackName());
		Assert.assertEquals("Car BB 4 final destination","Essex MA", c4.getFinalDestinationName());
		Assert.assertEquals("Car BB 4 final destination track","Essex Siding 1", c4.getFinalDestinationTrackName());
		Assert.assertEquals("Car BC 5 destination","", c5.getDestinationName());
		Assert.assertEquals("Car BC 5 destination track","", c5.getDestinationTrackName());
		Assert.assertEquals("Car BC 5 final destination","Essex MA", c5.getFinalDestinationName());
		Assert.assertEquals("Car BC 5 final destination track","", c5.getFinalDestinationTrackName());
		Assert.assertEquals("Car BD 6 destination","", c6.getDestinationName());
		Assert.assertEquals("Car BD 6 destination track","", c6.getDestinationTrackName());
		Assert.assertEquals("Car BD 6 final destination","Essex MA", c6.getFinalDestinationName());
		Assert.assertEquals("Car BD 6 final destination track","", c6.getFinalDestinationTrackName());
		Assert.assertEquals("Car BA 7 destination","", c7.getDestinationName());
		Assert.assertEquals("Car BA 7 destination track","", c7.getDestinationTrackName());
		Assert.assertEquals("Car BA 7 final destination","Essex MA", c7.getFinalDestinationName());
		Assert.assertEquals("Car BA 7 final destination track","Essex Siding 1", c7.getFinalDestinationTrackName());
		Assert.assertEquals("Car BB 8 destination","", c8.getDestinationName());
		Assert.assertEquals("Car BB 8 destination track","", c8.getDestinationTrackName());
		Assert.assertEquals("Car BB 8 final destination","", c8.getFinalDestinationName());
		Assert.assertEquals("Car BB 8 final destination track","", c8.getFinalDestinationTrackName());
		Assert.assertEquals("Car BC 9 destination","", c9.getDestinationName());
		Assert.assertEquals("Car BC 9 destination track","", c9.getDestinationTrackName());
		Assert.assertEquals("Car BC 9 final destination","", c9.getFinalDestinationName());
		Assert.assertEquals("Car BC 9 final destination track","", c9.getFinalDestinationTrackName());
		
		// try again
		DanburyToEssexTrain.build();
		Assert.assertTrue("Bedford train built", DanburyToEssexTrain.isBuilt());
	
		// check car destinations
		// BA 3 (Boxcar) this car's load and final destination is now different
		Assert.assertEquals("Car BA 3 destination","Essex MA", c3.getDestinationName());
		Assert.assertEquals("Car BA 3 destination track","Essex Siding 1", c3.getDestinationTrackName());
		// new final destination and load for car BA 3
		Assert.assertEquals("Car BA 3 final destination","Acton MA", c3.getFinalDestinationName());
		Assert.assertEquals("Car BA 3 final destination track","Acton Siding 1", c3.getFinalDestinationTrackName());
		Assert.assertEquals("Car BA 3 next load","Metal", c3.getNextLoadName());
		Assert.assertEquals("Car BA 3 schedule id", "", c3.getScheduleId());
		// BB 4 (Flat) resets the match pointer so car BC 5 final destination and load is the same as last time
		Assert.assertEquals("Car BB 4 destination","Essex MA", c4.getDestinationName());
		Assert.assertEquals("Car BB 4 destination track","Essex Siding 1", c4.getDestinationTrackName());
		// new final destination and load for car BB 4
		Assert.assertEquals("Car BB 4 final destination","Danbury MA", c4.getFinalDestinationName());
		Assert.assertEquals("Car BB 4 final destination track","Danbury Siding 2", c4.getFinalDestinationTrackName());
		Assert.assertEquals("Car BB 4 next load","Metal", c4.getNextLoadName());
		Assert.assertEquals("Car BB 4 schedule id", "", c4.getScheduleId());
		// BC 5 (Boxcar)
		Assert.assertEquals("Car BC 5 destination","Essex MA", c5.getDestinationName());
		Assert.assertEquals("Car BC 5 destination track","Essex Siding 1", c5.getDestinationTrackName());
		// new final destination and load for car BC 5, same as BA 3
		Assert.assertEquals("Car BC 5 final destination","Bedford MA", c5.getFinalDestinationName());
		Assert.assertEquals("Car BC 5 final destination track","Bedford Siding 1", c5.getFinalDestinationTrackName());
		Assert.assertEquals("Car BC 5 next load","Screws", c5.getNextLoadName());
		Assert.assertEquals("Car BC 5 schedule id", "", c5.getScheduleId());
		// BD 6 (Boxcar) note second Boxcar
		Assert.assertEquals("Car BD 6 destination","Essex MA", c6.getDestinationName());
		Assert.assertEquals("Car BD 6 destination track","Essex Siding 1", c6.getDestinationTrackName());
		// new final destination and load for car BD 6
		Assert.assertEquals("Car BD 6 final destination","Danbury MA", c6.getFinalDestinationName());
		Assert.assertEquals("Car BD 6 final destination track","Danbury Siding 1", c6.getFinalDestinationTrackName());
		Assert.assertEquals("Car BC 6 next load", "Bolts", c6.getNextLoadName());
		Assert.assertEquals("Car BD 6 schedule id", "", c6.getScheduleId());
		// BA 7 (Boxcar) note 3rd Boxcar
		Assert.assertEquals("Car BA 7 destination","Essex MA", c7.getDestinationName());
		Assert.assertEquals("Car BA 7 destination track","Essex Siding 1", c7.getDestinationTrackName());
		// new final destination and load for car BA 7
		Assert.assertEquals("Car BA 7 final destination","Acton MA", c7.getFinalDestinationName());
		Assert.assertEquals("Car BA 7 final destination track","Acton Siding 1", c7.getFinalDestinationTrackName());
		Assert.assertEquals("Car BA 7 next load", "Metal", c7.getNextLoadName());
		Assert.assertEquals("Car BA 7 schedule id", "", c7.getScheduleId());
		// BB 8 (Boxcar) at Danbury to be added to train
		Assert.assertEquals("Car BB 8 destination","Essex MA", c8.getDestinationName());
		Assert.assertEquals("Car BB 8 destination track","Essex Siding 1", c8.getDestinationTrackName());
		// Should match schedule item 16c3
		Assert.assertEquals("Car BB 8 final destination","Bedford MA", c8.getFinalDestinationName());
		Assert.assertEquals("Car BB 8 final destination track","Bedford Siding 1", c8.getFinalDestinationTrackName());
		Assert.assertEquals("Car BB 8 next load", "Screws", c8.getNextLoadName());
		Assert.assertEquals("Car BB 8 schedule id", "", c8.getScheduleId());
		// BB 9 (Boxcar) at Danbury to be added to train
		Assert.assertEquals("Car BC 9 destination","Essex MA", c9.getDestinationName());
		Assert.assertEquals("Car BC 9 destination track","Essex Siding 1", c9.getDestinationTrackName());
		// Should match schedule item 16c4
		Assert.assertEquals("Car BC 9 final destination","Danbury MA", c9.getFinalDestinationName());
		Assert.assertEquals("Car BC 9 final destination track","Danbury Siding 1", c9.getFinalDestinationTrackName());
		Assert.assertEquals("Car BC 9 next load", "Bolts", c9.getNextLoadName());
		Assert.assertEquals("Car BC 9 schedule id", "", c9.getScheduleId());
		
		DanburyToEssexTrain.terminate();
		
		// check car destinations
		// BA 3 (Boxcar)
		Assert.assertEquals("Car BA 3 destination","", c3.getDestinationName());
		Assert.assertEquals("Car BA 3 destination track","", c3.getDestinationTrackName());
		// new final destination and load for car BA 3
		Assert.assertEquals("Car BA 3 final destination","Acton MA", c3.getFinalDestinationName());
		Assert.assertEquals("Car BA 3 final destination track","Acton Siding 1", c3.getFinalDestinationTrackName());
		Assert.assertEquals("Car BA 3 load","Metal", c3.getLoadName());
		Assert.assertEquals("Car BA 3 next load","", c3.getNextLoadName());
		Assert.assertEquals("Car BA 3 schedule id", "", c3.getScheduleId());
		// BB 4 (Flat)
		Assert.assertEquals("Car BB 4 destination","", c4.getDestinationName());
		Assert.assertEquals("Car BB 4 destination track","", c4.getDestinationTrackName());
		// new final destination and load for car BB 4
		Assert.assertEquals("Car BB 4 final destination","Danbury MA", c4.getFinalDestinationName());
		Assert.assertEquals("Car BB 4 final destination track","Danbury Siding 2", c4.getFinalDestinationTrackName());
		Assert.assertEquals("Car BB 4 load","Metal", c4.getLoadName());
		Assert.assertEquals("Car BB 4 next load","", c4.getNextLoadName());
		Assert.assertEquals("Car BB 4 schedule id", "", c4.getScheduleId());
		// BC 5 (Boxcar)
		Assert.assertEquals("Car BC 5 destination","", c5.getDestinationName());
		Assert.assertEquals("Car BC 5 destination track","", c5.getDestinationTrackName());
		// new final destination and load for car BC 5, same as BA 3
		Assert.assertEquals("Car BC 5 final destination","Bedford MA", c5.getFinalDestinationName());
		Assert.assertEquals("Car BC 5 final destination track","Bedford Siding 1", c5.getFinalDestinationTrackName());
		Assert.assertEquals("Car BC 5 load","Screws", c5.getLoadName());
		Assert.assertEquals("Car BC 5 next load","", c5.getNextLoadName());
		Assert.assertEquals("Car BC 5 schedule id", "", c5.getScheduleId());
		// BD 6 (Boxcar) note second Boxcar
		Assert.assertEquals("Car BD 6 destination","", c6.getDestinationName());
		Assert.assertEquals("Car BD 6 destination track","", c6.getDestinationTrackName());
		// new final destination and load for car BD 6
		Assert.assertEquals("Car BD 6 final destination","Danbury MA", c6.getFinalDestinationName());
		Assert.assertEquals("Car BD 6 final destination track","Danbury Siding 1", c6.getFinalDestinationTrackName());
		Assert.assertEquals("Car BC 6 load", "Bolts", c6.getLoadName());
		Assert.assertEquals("Car BC 6 next load", "", c6.getNextLoadName());
		Assert.assertEquals("Car BD 6 schedule id", "", c6.getScheduleId());
		// BA 7 (Boxcar) note 3rd Boxcar
		Assert.assertEquals("Car BA 7 destination","", c7.getDestinationName());
		Assert.assertEquals("Car BA 7 destination track","", c7.getDestinationTrackName());
		// new final destination and load for car BA 7
		Assert.assertEquals("Car BA 7 final destination","Acton MA", c7.getFinalDestinationName());
		Assert.assertEquals("Car BA 7 final destination track","Acton Siding 1", c7.getFinalDestinationTrackName());
		Assert.assertEquals("Car BA 7 load", "Metal", c7.getLoadName());
		Assert.assertEquals("Car BA 7 next load", "", c7.getNextLoadName());
		Assert.assertEquals("Car BA 7 schedule id", "", c7.getScheduleId());
		// BB 8 (Boxcar)
		Assert.assertEquals("Car BB 8 destination","", c8.getDestinationName());
		Assert.assertEquals("Car BB 8 destination track","", c8.getDestinationTrackName());
		// Should match schedule item 16c3
		Assert.assertEquals("Car BB 8 final destination","Bedford MA", c8.getFinalDestinationName());
		Assert.assertEquals("Car BB 8 final destination track","Bedford Siding 1", c8.getFinalDestinationTrackName());
		Assert.assertEquals("Car BB 8 load", "Screws", c8.getLoadName());
		Assert.assertEquals("Car BB 8 next load", "", c8.getNextLoadName());
		Assert.assertEquals("Car BB 8 schedule id", "", c8.getScheduleId());
		// BB 9 (Boxcar)
		Assert.assertEquals("Car BC 9 destination","", c9.getDestinationName());
		Assert.assertEquals("Car BC 9 destination track","", c9.getDestinationTrackName());
		// Should match schedule item 16c4
		Assert.assertEquals("Car BC 9 final destination","Danbury MA", c9.getFinalDestinationName());
		Assert.assertEquals("Car BC 9 final destination track","Danbury Siding 1", c9.getFinalDestinationTrackName());
		Assert.assertEquals("Car BC 9 load", "Bolts", c9.getLoadName());
		Assert.assertEquals("Car BC 9 next load", "", c9.getNextLoadName());
		Assert.assertEquals("Car BC 9 schedule id", "", c9.getScheduleId());
		
	}
	
	/* This test confirms that schedules can be linked together.
	 * Note that there are schedules at Essex that are still active
	 * but not reachable because the Clinton to Danbury train is
	 * removed as part of this test.
	 * has 5 tracks, 3 sidings, yard, and an interchange track.
	 *  
	 */
	public void testRoutingWithSchedulesLocal() {
		TrainManager tmanager = TrainManager.instance();
		CarManager cmanager = CarManager.instance();
		LocationManager lmanager = LocationManager.instance();
		
		loadLocationsTrainsAndCars();

		List<Train> trains = tmanager.getTrainsByNameList();
		Assert.assertEquals("confirm number of trains", 7, trains.size());
		
		Car c3 = cmanager.getByRoadAndNumber("BA", "3");
		Car c4 = cmanager.getByRoadAndNumber("BB", "4");
		Car c5 = cmanager.getByRoadAndNumber("BC", "5");
		Car c6 = cmanager.getByRoadAndNumber("BD", "6");
		Car c7 = cmanager.getByRoadAndNumber("BA", "7");
		Car c8 = cmanager.getByRoadAndNumber("BB", "8");
		Car c9 = cmanager.getByRoadAndNumber("BC", "9");
		
		// c3 (BA 3) is a Boxcar
		c3.setLoadName("Cardboard");
		
		// c4 (BB 4) is a Flat
		c4.setLoadName("Trucks");
		
		// c5 (BC 5) is a Boxcar
		c5.setLoadName(CarLoads.instance().getDefaultEmptyName());
		
		// c6 (BD 6) is a Boxcar
		c6.setLoadName(CarLoads.instance().getDefaultEmptyName());
		
		// c7 (7) is a Boxcar
		c7.setLoadName(CarLoads.instance().getDefaultEmptyName());
		
		// c8 (8) is a Boxcar
		c8.setLoadName("Trucks");
		
		// c9 (9) is a Boxcar
		c9.setLoadName(CarLoads.instance().getDefaultEmptyName());	
		
		Location Acton = lmanager.getLocationByName("Acton MA");
		Location Bedford = lmanager.getLocationByName("Bedford MA");
		Location Clinton = lmanager.getLocationByName("Clinton MA");
		Track CS1 = Clinton.getTrackByName("Clinton Siding 1", Track.SPUR);
		
		Track AS1 = Acton.getTrackByName("Acton Siding 1", Track.SPUR);
		AS1.setTrainDirections(Track.NORTH+Track.SOUTH);
		Track AS2 = Acton.getTrackByName("Acton Siding 2", Track.SPUR);
		Track AS3 = Acton.addTrack("Acton Siding 3", Track.SPUR);
		AS3.setLength(300);
		Track AY = Acton.getTrackByName("Acton Yard", Track.YARD);
		Track AI = Acton.getTrackByName("Acton Interchange", Track.INTERCHANGE);
		
		// create schedules
		ScheduleManager scheduleManager = ScheduleManager.instance();
		Schedule schA = scheduleManager.newSchedule("Schedule Action");		
		ScheduleItem schAItem1 = schA.addItem("Boxcar");
		schAItem1.setReceiveLoadName("Cardboard");
		schAItem1.setShipLoadName("Scrap");
		ScheduleItem schAItem2 = schA.addItem("Gon");
		schAItem2.setReceiveLoadName("Trucks");
		schAItem2.setShipLoadName("Tires");
		schAItem2.setDestination(Bedford);
		ScheduleItem schAItem3 = schA.addItem("Boxcar");
		schAItem3.setReceiveLoadName("Trucks");
		schAItem3.setShipLoadName("Wire");
		schAItem3.setDestination(Clinton);
		schAItem3.setDestinationTrack(CS1);
		ScheduleItem schAItem4 = schA.addItem("Flat");
		schAItem4.setReceiveLoadName("Trucks");
		schAItem4.setShipLoadName("Coils");
		schAItem4.setDestination(Bedford);
		ScheduleItem schAItem5 = schA.addItem("Flat");
		schAItem5.setReceiveLoadName("Coils");
		schAItem5.setShipLoadName("Trucks");
		schAItem5.setDestination(Bedford);
		ScheduleItem schAItem6 = schA.addItem("Boxcar");
		schAItem6.setReceiveLoadName("Scrap");
		schAItem6.setShipLoadName("E");
		ScheduleItem schAItem7 = schA.addItem("Boxcar");
		schAItem7.setReceiveLoadName("Wire");
		schAItem7.setShipLoadName("L");
		
		// add schedules to tracks
		AS1.setScheduleId(schA.getId());
		AS1.setScheduleMode(Track.SEQUENTIAL);
		AS2.setScheduleId(schA.getId());
		AS2.setScheduleMode(Track.SEQUENTIAL);
		AS3.setScheduleId(schA.getId());
		// put Action Siding 3 into match mode
		AS3.setScheduleMode(Track.MATCH);
			
		// place cars
		Assert.assertEquals("Place car", Track.OKAY, c3.setLocation(Acton, AS1));
		Assert.assertEquals("Place car", Track.OKAY, c4.setLocation(Acton, AS1));
		Assert.assertEquals("Place car", Track.OKAY, c5.setLocation(Acton, AS2));
		Assert.assertEquals("Place car", Track.OKAY, c6.setLocation(Acton, AS2));
		Assert.assertEquals("Place car", Track.OKAY, c7.setLocation(Acton, AS3));
		Assert.assertEquals("Place car", Track.OKAY, c8.setLocation(Acton, AY));
		Assert.assertEquals("Place car", Track.OKAY, c9.setLocation(Acton, AI));
		
		// Build train
		Train ActonToBedfordTrain = tmanager.getTrainByName("Acton to Bedford");
		Route ActonToBeford = ActonToBedfordTrain.getRoute();
		RouteLocation rl = ActonToBeford.getDepartsRouteLocation();
		RouteLocation rd = ActonToBeford.getLastLocationByName("Bedford MA");
		// increase the number of moves so all cars are used
		rl.setMaxCarMoves(10);
		rd.setMaxCarMoves(10);
		// kill the Clinton to Danbury train
		Train ClintonToDanburyTrain = tmanager.getTrainByName("Clinton to Danbury");
		tmanager.deregister(ClintonToDanburyTrain);
		
		ActonToBedfordTrain.build();
		Assert.assertTrue("Acton train built", ActonToBedfordTrain.isBuilt());
		
		// check cars
		Assert.assertEquals("Car BA 3 destination","Acton MA", c3.getDestinationName());
		Assert.assertEquals("Car BA 3 destination track","Acton Siding 2", c3.getDestinationTrackName());
		Assert.assertEquals("Car BA 3 final destination","", c3.getFinalDestinationName());
		Assert.assertEquals("Car BA 3 final destination track","", c3.getFinalDestinationTrackName());
		Assert.assertEquals("Car BA 3 load","Cardboard", c3.getLoadName());
		Assert.assertEquals("Car BA 3 next load","Scrap", c3.getNextLoadName());
		
		Assert.assertEquals("Car BB 4 destination","Acton MA", c4.getDestinationName());
		Assert.assertEquals("Car BB 4 destination track","Acton Siding 3", c4.getDestinationTrackName());
		Assert.assertEquals("Car BB 4 final destination","Bedford MA", c4.getFinalDestinationName());
		Assert.assertEquals("Car BB 4 final destination track","", c4.getFinalDestinationTrackName());
		Assert.assertEquals("Car BB 4 load","Trucks", c4.getLoadName());
		Assert.assertEquals("Car BB 4 next load","Coils", c4.getNextLoadName());
		
		Assert.assertEquals("Car BC 5 destination","Bedford MA", c5.getDestinationName());
		Assert.assertEquals("Car BC 5 destination track","Bedford Siding 1", c5.getDestinationTrackName());
		Assert.assertEquals("Car BC 5 final destination","", c5.getFinalDestinationName());
		Assert.assertEquals("Car BC 5 final destination track","", c5.getFinalDestinationTrackName());
		Assert.assertEquals("Car BC 5 load","E", c5.getLoadName());
		Assert.assertEquals("Car BC 5 next load","", c5.getNextLoadName());
		
		Assert.assertEquals("Car BD 6 destination","Bedford MA", c6.getDestinationName());
		Assert.assertEquals("Car BD 6 destination track","Bedford Siding 2", c6.getDestinationTrackName());
		Assert.assertEquals("Car BD 6 final destination","", c6.getFinalDestinationName());
		Assert.assertEquals("Car BD 6 final destination track","", c6.getFinalDestinationTrackName());
		Assert.assertEquals("Car BD 6 load","E", c6.getLoadName());
		Assert.assertEquals("Car BD 6 next load","", c6.getNextLoadName());
		
		Assert.assertEquals("Car BA 7 destination","Bedford MA", c7.getDestinationName());
		Assert.assertEquals("Car BA 7 destination track","Bedford Yard", c7.getDestinationTrackName());
		Assert.assertEquals("Car BA 7 final destination","", c7.getFinalDestinationName());
		Assert.assertEquals("Car BA 7 final destination track","", c7.getFinalDestinationTrackName());
		Assert.assertEquals("Car BA 7 load","E", c7.getLoadName());
		Assert.assertEquals("Car BA 7 next load","", c7.getNextLoadName());
		
		Assert.assertEquals("Car BB 8 destination","Acton MA", c8.getDestinationName());
		Assert.assertEquals("Car BB 8 destination track","Acton Siding 3", c8.getDestinationTrackName());
		Assert.assertEquals("Car BB 8 final destination","Clinton MA", c8.getFinalDestinationName());
		Assert.assertEquals("Car BB 8 final destination track","Clinton Siding 1", c8.getFinalDestinationTrackName());
		Assert.assertEquals("Car BB 8 load","Trucks", c8.getLoadName());
		Assert.assertEquals("Car BB 8 next load","Wire", c8.getNextLoadName());
		
		Assert.assertEquals("Car BC 9 destination","Bedford MA", c9.getDestinationName());
		Assert.assertEquals("Car BC 9 destination track","Bedford Interchange", c9.getDestinationTrackName());
		Assert.assertEquals("Car BC 9 final destination","", c9.getFinalDestinationName());
		Assert.assertEquals("Car BC 9 final destination track","", c9.getFinalDestinationTrackName());
		Assert.assertEquals("Car BC 9 load","E", c9.getLoadName());
		Assert.assertEquals("Car BC 9 next load","", c9.getNextLoadName());
			
		ActonToBedfordTrain.terminate();
		
		// Build train
		Train BedfordToActonTrain = tmanager.newTrain("BedfordToActonToBedford");
		Route BedfordToActon = RouteManager.instance().newRoute("BedfordToActonToBedford");
		RouteLocation rlB2 = BedfordToActon.addLocation(Bedford);
		RouteLocation rlA2 = BedfordToActon.addLocation(Acton);
		RouteLocation rlB3 = BedfordToActon.addLocation(Bedford);
		// increase the number of moves so all cars are used
		rlB2.setMaxCarMoves(10);
		rlA2.setMaxCarMoves(10);
		rlB3.setMaxCarMoves(10);
		BedfordToActonTrain.setRoute(BedfordToActon);
		
		BedfordToActonTrain.build();
		
		// check cars
		Assert.assertEquals("Car BA 3 destination","Acton MA", c3.getDestinationName());
		Assert.assertEquals("Car BA 3 destination track","Acton Siding 3", c3.getDestinationTrackName());
		Assert.assertEquals("Car BA 3 final destination","", c3.getFinalDestinationName());
		Assert.assertEquals("Car BA 3 final destination track","", c3.getFinalDestinationTrackName());
		Assert.assertEquals("Car BA 3 load","Scrap", c3.getLoadName());
		Assert.assertEquals("Car BA 3 next load","E", c3.getNextLoadName());
		
		Assert.assertEquals("Car BB 4 destination","Bedford MA", c4.getDestinationName());
		Assert.assertEquals("Car BB 4 destination track","Bedford Yard", c4.getDestinationTrackName());
		Assert.assertEquals("Car BB 4 final destination","", c4.getFinalDestinationName());
		Assert.assertEquals("Car BB 4 final destination track","", c4.getFinalDestinationTrackName());
		Assert.assertEquals("Car BB 4 load","Coils", c4.getLoadName());
		Assert.assertEquals("Car BB 4 next load","", c4.getNextLoadName());
		
		Assert.assertEquals("Car BC 5 destination","Acton MA", c5.getDestinationName());
		Assert.assertEquals("Car BC 5 destination track","Acton Yard", c5.getDestinationTrackName());
		Assert.assertEquals("Car BC 5 final destination","", c5.getFinalDestinationName());
		Assert.assertEquals("Car BC 5 final destination track","", c5.getFinalDestinationTrackName());
		Assert.assertEquals("Car BC 5 load","L", c5.getLoadName());
		Assert.assertEquals("Car BC 5 next load","", c5.getNextLoadName());
		
		Assert.assertEquals("Car BD 6 destination","Acton MA", c6.getDestinationName());
		Assert.assertEquals("Car BD 6 destination track","Acton Interchange", c6.getDestinationTrackName());
		Assert.assertEquals("Car BD 6 final destination","", c6.getFinalDestinationName());
		Assert.assertEquals("Car BD 6 final destination track","", c6.getFinalDestinationTrackName());
		Assert.assertEquals("Car BD 6 load","L", c6.getLoadName());
		Assert.assertEquals("Car BD 6 next load","", c6.getNextLoadName());
		
		Assert.assertEquals("Car BA 7 destination","Acton MA", c7.getDestinationName());
		Assert.assertEquals("Car BA 7 destination track","Acton Yard", c7.getDestinationTrackName());
		Assert.assertEquals("Car BA 7 final destination","", c7.getFinalDestinationName());
		Assert.assertEquals("Car BA 7 final destination track","", c7.getFinalDestinationTrackName());
		Assert.assertEquals("Car BA 7 load","E", c7.getLoadName());
		Assert.assertEquals("Car BA 7 next load","", c7.getNextLoadName());
		
		Assert.assertEquals("Car BB 8 destination","Bedford MA", c8.getDestinationName());
		Assert.assertEquals("Car BB 8 destination track","Bedford Interchange", c8.getDestinationTrackName());
		Assert.assertEquals("Car BB 8 final destination","Clinton MA", c8.getFinalDestinationName());
		Assert.assertEquals("Car BB 8 final destination track","Clinton Siding 1", c8.getFinalDestinationTrackName());
		Assert.assertEquals("Car BB 8 load","Wire", c8.getLoadName());
		Assert.assertEquals("Car BB 8 next load","", c8.getNextLoadName());
		
		Assert.assertEquals("Car BC 9 destination","Acton MA", c9.getDestinationName());
		Assert.assertEquals("Car BC 9 destination track","Acton Interchange", c9.getDestinationTrackName());
		Assert.assertEquals("Car BC 9 final destination","", c9.getFinalDestinationName());
		Assert.assertEquals("Car BC 9 final destination track","", c9.getFinalDestinationTrackName());
		Assert.assertEquals("Car BC 9 load","E", c9.getLoadName());
		Assert.assertEquals("Car BC 9 next load","", c9.getNextLoadName());
		
		BedfordToActonTrain.terminate();
	}
	
	private void loadLocationsTrainsAndCars() {
		LocationManager lmanager = LocationManager.instance();
		TrainManager tmanager = TrainManager.instance();
		RouteManager rmanager = RouteManager.instance();
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

		Track AS1 = Acton.addTrack("Acton Siding 1", Track.SPUR);
		AS1.setLength(300);
		Assert.assertEquals("Location AS1 Name", "Acton Siding 1", AS1.getName());
		Assert.assertEquals("Location AS1 Length", 300, AS1.getLength());
		
		Track AS2 = Acton.addTrack("Acton Siding 2", Track.SPUR);
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

		Track BS1 = Bedford.addTrack("Bedford Siding 1", Track.SPUR);
		BS1.setLength(300);
		Assert.assertEquals("Location BS1 Name", "Bedford Siding 1", BS1.getName());
		Assert.assertEquals("Location BS1 Length", 300, BS1.getLength());
		
		Track BS2 = Bedford.addTrack("Bedford Siding 2", Track.SPUR);
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

		Track CS1 = Clinton.addTrack("Clinton Siding 1", Track.SPUR);
		CS1.setLength(300);
		Assert.assertEquals("Location CS1 Name", "Clinton Siding 1", CS1.getName());
		Assert.assertEquals("Location CS1 Length", 300, CS1.getLength());
		
		Track CS2 = Clinton.addTrack("Clinton Siding 2", Track.SPUR);
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
		Track DS1 = Danbury.addTrack("Danbury Siding 1", Track.SPUR);
		DS1.setLength(300);
		Track DS2 = Danbury.addTrack("Danbury Siding 2", Track.SPUR);
		DS2.setLength(300);		
		Track DY = Danbury.addTrack("Danbury Yard", Track.YARD);
		DY.setLength(400);		
		Track DI = Danbury.addTrack("Danbury Interchange", Track.INTERCHANGE);
		DI.setLength(500);
		
		Location Essex = lmanager.newLocation("Essex MA");
		Track ES1 = Essex.addTrack("Essex Siding 1", Track.SPUR);
		ES1.setLength(300);	
		Track ES2 = Essex.addTrack("Essex Siding 2", Track.SPUR);
		ES2.setLength(300);		
		Track EY = Essex.addTrack("Essex Yard", Track.YARD);
		EY.setLength(400);		
		Track EI = Essex.addTrack("Essex Interchange", Track.INTERCHANGE);
		EI.setLength(500);
		
		Location Foxboro = lmanager.newLocation("Foxboro MA");
		Track FS1 = Foxboro.addTrack("Foxboro Siding 1", Track.SPUR);
		FS1.setLength(300);	
		Track FS2 = Foxboro.addTrack("Foxboro Siding 2", Track.SPUR);
		FS2.setLength(300);		
		Track FY = Foxboro.addTrack("Foxboro Yard", Track.YARD);
		FY.setLength(400);		
		Track FI = Foxboro.addTrack("Foxboro Interchange", Track.INTERCHANGE);
		FI.setLength(500);
		
		// create a local train servicing Acton
		Train ActonTrain = tmanager.newTrain("Acton Local");
		Route routeA = rmanager.newRoute("A");
		RouteLocation rlA = routeA.addLocation(Acton);
		rlA.setMaxCarMoves(10);
		rlA.setTrainIconX(25);	// set train icon coordinates
		rlA.setTrainIconY(250);
		ActonTrain.setRoute(routeA);
		
		// create a train with a route from Acton to Bedford
		Train ActonToBedfordTrain = tmanager.newTrain("Acton to Bedford");
		Route routeAB = rmanager.newRoute("AB");
		routeAB.addLocation(Acton);
		RouteLocation rlBedford = routeAB.addLocation(Bedford);
		rlBedford.setTrainIconX(100);	// set train icon coordinates
		rlBedford.setTrainIconY(250);
		ActonToBedfordTrain.setRoute(routeAB);
		
		// create a train with a route from Bedford to Clinton
		Train BedfordToClintonTrain = tmanager.newTrain("Bedford to Clinton");
		Route routeBC = rmanager.newRoute("BC");
		routeBC.addLocation(Bedford);
		RouteLocation rlchelmsford = routeBC.addLocation(Clinton);
		rlchelmsford.setTrainIconX(175);	// set train icon coordinates
		rlchelmsford.setTrainIconY(250);
		BedfordToClintonTrain.setRoute(routeBC);
		
		// create a train with a route from Clinton to Danbury
		Train ClintonToDanburyTrain = tmanager.newTrain("Clinton to Danbury");
		Route routeCD = rmanager.newRoute("CD");
		routeCD.addLocation(Clinton);
		RouteLocation rlDanbury = routeCD.addLocation(Danbury);
		rlDanbury.setTrainIconX(250);	// set train icon coordinates
		rlDanbury.setTrainIconY(250);
		ClintonToDanburyTrain.setRoute(routeCD);
		
		// create a train with a route from Danbury to Essex
		Train DanburyToEssexTrain = tmanager.newTrain("Danbury to Essex");
		Route routeDE = rmanager.newRoute("DE");
		RouteLocation rlDanbury2 = routeDE.addLocation(Danbury);
		RouteLocation rlEssex = routeDE.addLocation(Essex);
		// set the number of car moves to 8 for a later test
		rlDanbury2.setMaxCarMoves(8);
		rlEssex.setMaxCarMoves(8);
		rlEssex.setTrainIconX(25);	// set train icon coordinates
		rlEssex.setTrainIconY(275);
		DanburyToEssexTrain.setRoute(routeDE);
		
		// create a train with a route from Essex to Foxboro
		Train EssexToFoxboroTrain = tmanager.newTrain("Essex to Foxboro");
		Route routeEF = rmanager.newRoute("EF");
		routeEF.addLocation(Essex);
		RouteLocation rlFoxboro = routeEF.addLocation(Foxboro);
		rlFoxboro.setTrainIconX(100);	// set train icon coordinates
		rlFoxboro.setTrainIconY(275);
		EssexToFoxboroTrain.setRoute(routeEF);
		
		Train BedfordToActonTrain = tmanager.newTrain("BedfordToActonToBedford");
		Route BedfordToActon = RouteManager.instance().newRoute("BedfordToActonToBedford");
		RouteLocation rlB2 = BedfordToActon.addLocation(Bedford);
		RouteLocation rlA3 = BedfordToActon.addLocation(Acton);
		RouteLocation rlB3 = BedfordToActon.addLocation(Bedford);
		// increase the number of moves so all cars are used
		rlB2.setMaxCarMoves(10);
		rlA3.setMaxCarMoves(10);
		rlB3.setMaxCarMoves(10);
		BedfordToActonTrain.setRoute(BedfordToActon);
		
		// add cars
		
		Car c3 = cmanager.newCar("BA", "3");
		c3.setTypeName("Boxcar");
		c3.setLength("40");
		c3.setOwner("DAB");
		c3.setBuilt("1984");
		
		Car c4 = cmanager.newCar("BB", "4");
		c4.setTypeName("Flat");
		c4.setLength("40");
		c4.setOwner("AT");
		c4.setBuilt("1-86");
		c4.setMoves(2);
		
		Car c5 = cmanager.newCar("BC", "5");
		c5.setTypeName("Boxcar");
		c5.setLoadName("Boxes");
		c5.setLength("40");
		c5.setBuilt("2000");
		c5.setMoves(3);
		
		Car c6 = cmanager.newCar("BD", "6");
		c6.setTypeName("Boxcar");
		c6.setLoadName("Empty");
		c6.setLength("40");
		c6.setBuilt("2000");
		c6.setMoves(4);
		
		Car c7 = cmanager.newCar("BA", "7");
		c7.setTypeName("Boxcar");
		c7.setLoadName("Boxes");
		c7.setLength("4");
		c7.setBuilt("2000");
		c7.setMoves(5);
		
		Car c8 = cmanager.newCar("BB", "8");
		c8.setTypeName("Boxcar");
		c8.setLoadName("Empty");
		c8.setLength("4");
		c8.setBuilt("2000");
		c8.setMoves(6);
		
		Car c9 = cmanager.newCar("BC", "9");
		c9.setTypeName("Boxcar");
		c9.setLoadName("Empty");
		c9.setLength("4");
		c9.setBuilt("2000");
		c9.setMoves(7);
	}
	
	// Ensure minimal setup for log4J
	@Override
	protected void setUp() {
		apps.tests.Log4JFixture.setUp();
		
		// set the locale to US English
		Locale.setDefault(Locale.ENGLISH);
		
		// Repoint OperationsSetupXml to JUnitTest subdirectory
		String tempstring = OperationsSetupXml.getOperationsDirectoryName();
		if (!tempstring.contains(File.separator+"JUnitTest")){
			OperationsSetupXml.setOperationsDirectoryName("operations"+File.separator+"JUnitTest");
		}
		// Change file names to ...Test.xml
		OperationsSetupXml.instance().setOperationsFileName("OperationsJUnitTest.xml"); 
		RouteManagerXml.instance().setOperationsFileName("OperationsJUnitTestRouteRoster.xml");
		EngineManagerXml.instance().setOperationsFileName("OperationsJUnitTestEngineRoster.xml");
		CarManagerXml.instance().setOperationsFileName("OperationsJUnitTestCarRoster.xml");
		LocationManagerXml.instance().setOperationsFileName("OperationsJUnitTestLocationRoster.xml");
		TrainManagerXml.instance().setOperationsFileName("OperationsJUnitTestTrainRoster.xml");
		
		FileUtil.createDirectory(FileUtil.getUserFilesPath()+OperationsSetupXml.getOperationsDirectoryName());

		// Need to clear out TrainManager global variables
		TrainManager.instance().dispose();
		LocationManager.instance().dispose();
		RouteManager.instance().dispose();
		CarManager.instance().dispose();
		
		Setup.setRouterBuildReportLevel(Setup.BUILD_REPORT_VERY_DETAILED);
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
