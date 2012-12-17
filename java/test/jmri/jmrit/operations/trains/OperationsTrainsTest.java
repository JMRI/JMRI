//OperationsTrainsTest.java

package jmri.jmrit.operations.trains;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.Schedule;
import jmri.jmrit.operations.locations.ScheduleItem;
import jmri.jmrit.operations.locations.ScheduleManager;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.jmrit.operations.rollingstock.cars.CarTypes;
import jmri.jmrit.operations.rollingstock.engines.Consist;
import jmri.jmrit.operations.rollingstock.engines.Engine;
import jmri.jmrit.operations.rollingstock.engines.EngineTypes;
import jmri.jmrit.operations.rollingstock.engines.EngineManager;
import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import org.jdom.JDOMException;

import jmri.jmrit.operations.rollingstock.cars.CarColors;
import jmri.jmrit.operations.rollingstock.cars.CarLengths;
import jmri.jmrit.operations.rollingstock.cars.CarLoad;
import jmri.jmrit.operations.rollingstock.cars.CarLoads;
import jmri.jmrit.operations.rollingstock.cars.CarOwners;
import jmri.jmrit.operations.rollingstock.cars.CarRoads;
import jmri.jmrit.operations.rollingstock.cars.Kernel;
import jmri.jmrit.operations.rollingstock.engines.EngineModels;
import jmri.jmrit.operations.routes.RouteManager;

/**
 * Tests for the Operations Trains class
 * Last manually cross-checked on 20090131
 * 
 * Still to do:
 *  Train: DepartureTime, ArrivalTime
 *  Train: numberCarsWorked
 *  Train: isTraininRoute
 *  Train: getBuild, setBuildEnabled, buildIfSelected
 *  Train: printBuildReport, printManifest, printReport
 *  Train: getPrint, setPrint, printIfSelected
 *  Train: setTrainIconCoordinates
 *  Train: terminateIfSelected
 *  Train: load/move/get/create Train Icon
 *  Train: get/set Lead Engine
 *  Train: setIconColor
 *  Train: reset
 *  Train: xml read/write
 *  Train: Most build scenarios.
 * 
 *  TrainBuilder: Everything.
 *  TrainSwitchLists: Everything.
 *  
 * @author	Bob Coleman Copyright (C) 2008, 2009
 * @version $Revision$
 */
public class OperationsTrainsTest extends TestCase {

	private final int DIRECTION_ALL = Location.EAST+Location.WEST+Location.NORTH+Location.SOUTH;
	
	// test train manager
	public void testTrainManager(){
		TrainManager manager = TrainManager.instance();

		// test defaults
		Assert.assertTrue("Build Messages", manager.isBuildMessagesEnabled());
		Assert.assertFalse("Build Reports", manager.isBuildReportEnabled());
		Assert.assertFalse("Print Preview", manager.isPrintPreviewEnabled());
		
		// Swap them
		manager.setBuildMessagesEnabled(false);
		manager.setBuildReportEnabled(true);
		manager.setPrintPreviewEnabled(true);
		
		Assert.assertFalse("Build Messages", manager.isBuildMessagesEnabled());
		Assert.assertTrue("Build Reports", manager.isBuildReportEnabled());
		Assert.assertTrue("Print Preview", manager.isPrintPreviewEnabled());
	
	}

	// test Train creation
	public void testCreate() {
		Train train1 = new Train("TESTTRAINID", "TESTTRAINNAME");

		Assert.assertEquals("Train Id", "TESTTRAINID", train1.getId());
		Assert.assertEquals("Train Name", "TESTTRAINNAME", train1.getName());
	}

	// test Train public constants
	public void testConstants() {
		Train train1 = new Train("TESTTRAINID", "TESTTRAINNAME");

		Assert.assertEquals("Train Id", "TESTTRAINID", train1.getId());
		Assert.assertEquals("Train Name", "TESTTRAINNAME", train1.getName());

		Assert.assertEquals("Train Constant NONE", 0, Train.NONE);
		Assert.assertEquals("Train Constant CABOOSE", 1, Train.CABOOSE);
		Assert.assertEquals("Train Constant FRED", 2, Train.FRED);

		Assert.assertEquals("Train Constant ALLROADS", "All", Train.ALLROADS);
		Assert.assertEquals("Train Constant INCLUDEROADS", "Include", Train.INCLUDEROADS);
		Assert.assertEquals("Train Constant EXCLUDEROADS", "Exclude", Train.EXCLUDEROADS);

		Assert.assertEquals("Train Constant DISPOSE_CHANGED_PROPERTY", "TrainDispose", Train.DISPOSE_CHANGED_PROPERTY);
		Assert.assertEquals("Train Constant STOPS_CHANGED_PROPERTY", "TrainStops", Train.STOPS_CHANGED_PROPERTY);
		Assert.assertEquals("Train Constant TYPES_CHANGED_PROPERTY", "TrainTypes", Train.TYPES_CHANGED_PROPERTY);
		Assert.assertEquals("Train Constant ROADS_CHANGED_PROPERTY", "TrainRoads", Train.ROADS_CHANGED_PROPERTY);
		Assert.assertEquals("Train Constant STATUS_CHANGED_PROPERTY", "TrainStatus", Train.STATUS_CHANGED_PROPERTY);
		Assert.assertEquals("Train Constant DEPARTURETIME_CHANGED_PROPERTY", "TrainDepartureTime", Train.DEPARTURETIME_CHANGED_PROPERTY);
		
		Assert.assertEquals("Train Constant AUTO", "Auto", Train.AUTO);
	}

	// test Train attributes
	public void testAttributes() {
		Train train1 = new Train("TESTTRAINID", "TESTTRAINNAME");

		Assert.assertEquals("Train Id", "TESTTRAINID", train1.getId());
		Assert.assertEquals("Train Name", "TESTTRAINNAME", train1.getName());
		Assert.assertEquals("Train toString", "TESTTRAINNAME", train1.toString());

		train1.setName("TESTNEWNAME");
		Assert.assertEquals("Train New Name", "TESTNEWNAME", train1.getName());
		train1.setComment("TESTCOMMENT");
		Assert.assertEquals("Train Comment", "TESTCOMMENT", train1.getComment());
		train1.setDescription("TESTDESCRIPTION");
		Assert.assertEquals("Train Description", "TESTDESCRIPTION", train1.getDescription());
		train1.setCabooseRoad("TESTCABOOSEROAD");
		Assert.assertEquals("Train Caboose Road", "TESTCABOOSEROAD", train1.getCabooseRoad());
		train1.setEngineModel("TESTENGINEMODEL");
		Assert.assertEquals("Train Engine Model", "TESTENGINEMODEL", train1.getEngineModel());
		train1.setEngineRoad("TESTENGINEROAD");
		Assert.assertEquals("Train Engine Road", "TESTENGINEROAD", train1.getEngineRoad());
		train1.setBuilt(true);
		Assert.assertTrue("Train Built true", train1.isBuilt());
		train1.setBuilt(false);
		Assert.assertFalse("Train Built false", train1.isBuilt());
		train1.setNumberEngines("13");
		Assert.assertEquals("Train Number Engines", "13", train1.getNumberEngines());
		train1.setRoadOption("INCLUDEROADS");
		Assert.assertEquals("Train Road Option INCLUDEROADS", "INCLUDEROADS", train1.getRoadOption());
		train1.setRoadOption("EXCLUDEROADS");
		Assert.assertEquals("Train Road Option EXCLUDEROADS", "EXCLUDEROADS", train1.getRoadOption());
		train1.setRoadOption("ALLROADS");
		Assert.assertEquals("Train Road Option ALLROADS", "ALLROADS", train1.getRoadOption());
		train1.setStatus("TESTSTATUS");
		Assert.assertEquals("Train Status", "TESTSTATUS", train1.getStatus());
		train1.setRequirements(Train.CABOOSE);
		Assert.assertEquals("Train Requirements CABOOSE", 1, train1.getRequirements());
		train1.setRequirements(Train.FRED);
		Assert.assertEquals("Train Requirements FRED", 2, train1.getRequirements());
		train1.setRequirements(Train.NONE);
		Assert.assertEquals("Train Requirements NONE", 0, train1.getRequirements());
		train1.setDepartureTime("12", "55");
		Assert.assertEquals("Train departure hour", "12", train1.getDepartureTimeHour());
		Assert.assertEquals("Train departure minute", "55", train1.getDepartureTimeMinute());
		Assert.assertEquals("Train departure hour and minute", "12:55", train1.getDepartureTime());
	}

	// test Train route
	public void testRoute() {
		Train train1 = new Train("TESTTRAINID", "TESTTRAINNAME");

		Assert.assertEquals("Train Id", "TESTTRAINID", train1.getId());
		Assert.assertEquals("Train Name", "TESTTRAINNAME", train1.getName());

		Route r1 = new Route("TESTROUTEID", "TESTROUTENAME");

		train1.setRoute(r1);
		Assert.assertEquals("Train Route Name", "TESTROUTENAME", train1.getTrainRouteName());

		Route rnew = new Route("TESTROUTEID2", "TESTNEWROUTENAME");

		Location l1 = new Location("TESTLOCATIONID1", "TESTNEWROUTEDEPTNAME");
		rnew.addLocation(l1);
		Location l2 = new Location("TESTLOCATIONID2", "TESTLOCATIONNAME2");
		rnew.addLocation(l2);
		Location l3 = new Location("TESTLOCATIONID3", "TESTNEWROUTECURRNAME");
		rnew.addLocation(l3);
		Location l4 = new Location("TESTLOCATIONID4", "TESTLOCATIONNAME4");
		rnew.addLocation(l4);
		Location l5 = new Location("TESTLOCATIONID5", "TESTNEWROUTETERMNAME");
		rnew.addLocation(l5);

		train1.setRoute(rnew);
		Assert.assertEquals("Train New Route Name", "TESTNEWROUTENAME", train1.getTrainRouteName());

		Assert.assertEquals("Train New Route Departure Name", "TESTNEWROUTEDEPTNAME", train1.getTrainDepartsName());
		Assert.assertEquals("Train New Route Terminates Name", "TESTNEWROUTETERMNAME", train1.getTrainTerminatesName());

		RouteLocation rl1test;
		rl1test= rnew.getLastLocationByName("TESTNEWROUTECURRNAME");
		train1.setCurrentLocation(rl1test);
		Assert.assertEquals("Train New Route Current Name", "TESTNEWROUTECURRNAME", train1.getCurrentLocationName());
		rl1test= train1.getCurrentLocation();
		Assert.assertEquals("Train New Route Current Name by Route Location", "TESTNEWROUTECURRNAME", rl1test.getName());
	}

	// test Train skip locations support
	public void testSkipLocations() {
		Train train1 = new Train("TESTTRAINID", "TESTTRAINNAME");

		Assert.assertEquals("Train Id", "TESTTRAINID", train1.getId());
		Assert.assertEquals("Train Name", "TESTTRAINNAME", train1.getName());

		train1.addTrainSkipsLocation("TESTLOCATIONID2");
		Assert.assertTrue("Location 2 to be skipped", train1.skipsLocation("TESTLOCATIONID2"));

		train1.addTrainSkipsLocation("TESTLOCATIONID4");
		Assert.assertTrue("Location 4 to be skipped", train1.skipsLocation("TESTLOCATIONID4"));

		train1.deleteTrainSkipsLocation("TESTLOCATIONID2");
		Assert.assertFalse("Location 2 not to be skipped", train1.skipsLocation("TESTLOCATIONID2"));
		Assert.assertTrue("Location 4 still to be skipped", train1.skipsLocation("TESTLOCATIONID4"));

		train1.deleteTrainSkipsLocation("TESTLOCATIONID4");
		Assert.assertFalse("Location 2 still not to be skipped", train1.skipsLocation("TESTLOCATIONID2"));
		Assert.assertFalse("Location 4 not to be skipped", train1.skipsLocation("TESTLOCATIONID4"));
	}

	// test Train accepts types support
	public void testAcceptsTypes() {
		Train train1 = new Train("TESTTRAINID", "TESTTRAINNAME");

		Assert.assertEquals("Train Id", "TESTTRAINID", train1.getId());
		Assert.assertEquals("Train Name", "TESTTRAINNAME", train1.getName());

		// Caboose is one of the default car types
		Assert.assertTrue("Train accepts type name Caboose", train1.acceptsTypeName("Caboose"));
		Assert.assertFalse("Train does not accept type name HopperTest", train1.acceptsTypeName("HopperTest"));

		train1.addTypeName("HopperTest");
		Assert.assertTrue("Train still accepts type name Caboose", train1.acceptsTypeName("Caboose"));
		Assert.assertTrue("Train accepts type name HopperTest", train1.acceptsTypeName("HopperTest"));

		train1.deleteTypeName("Caboose");
		Assert.assertFalse("Train no longer accepts type name Caboose", train1.acceptsTypeName("Caboose"));
		Assert.assertTrue("Train still accepts type name HopperTest", train1.acceptsTypeName("HopperTest"));
	}

	// test train accepts road names support
	public void testAcceptsRoadNames() {
		Train train1 = new Train("TESTTRAINID", "TESTTRAINNAME");

		Assert.assertEquals("Train Id", "TESTTRAINID", train1.getId());
		Assert.assertEquals("Train Name", "TESTTRAINNAME", train1.getName());

		train1.setRoadOption(Train.ALLROADS);
		Assert.assertTrue("Train accepts (ALLROADS) Road name CP", train1.acceptsRoadName("CP"));
		Assert.assertTrue("Train accepts (ALLROADS) Road name VIA", train1.acceptsRoadName("VIA"));

		train1.setRoadOption(Train.INCLUDEROADS);
		train1.addRoadName("CP");
		Assert.assertTrue("Train accepts (INCLUDEROADS) Road name CP", train1.acceptsRoadName("CP"));
		Assert.assertFalse("Train does not accept (INCLUDEROADS) Road name VIA", train1.acceptsRoadName("VIA"));

		train1.addRoadName("VIA");
		Assert.assertTrue("Train still accepts (INCLUDEROADS) Road name CP", train1.acceptsRoadName("CP"));
		Assert.assertTrue("Train accepts (INCLUDEROADS) Road name VIA", train1.acceptsRoadName("VIA"));

		train1.deleteRoadName("CP");
		Assert.assertFalse("Train no longer accepts (INCLUDEROADS) Road name CP", train1.acceptsRoadName("CP"));
		Assert.assertTrue("Train still accepts (INCLUDEROADS) Road name VIA", train1.acceptsRoadName("VIA"));

		train1.setRoadOption(Train.EXCLUDEROADS);
		Assert.assertTrue("Train does accept (EXCLUDEROADS) Road name CP", train1.acceptsRoadName("CP"));
		Assert.assertFalse("Train does not accept (EXCLUDEROADS) Road name VIA", train1.acceptsRoadName("VIA"));

		train1.addRoadName("CP");
		Assert.assertFalse("Train does not accept (EXCLUDEROADS) Road name CP", train1.acceptsRoadName("CP"));
		Assert.assertFalse("Train still does not accept (EXCLUDEROADS) Road name VIA", train1.acceptsRoadName("VIA"));

		train1.deleteRoadName("VIA");
		Assert.assertFalse("Train still does not accepts (EXCLUDEROADS) Road name CP", train1.acceptsRoadName("CP"));
		Assert.assertTrue("Train now accepts (EXCLUDEROADS) Road name VIA", train1.acceptsRoadName("VIA"));
	}
	
	// test train accepts load names support
	public void testAcceptsLoadNames() {
		Train train1 = new Train("TESTTRAINID", "TESTTRAINNAME");

		Assert.assertEquals("Train Id", "TESTTRAINID", train1.getId());
		Assert.assertEquals("Train Name", "TESTTRAINNAME", train1.getName());

		train1.setLoadOption(Train.ALLLOADS);
		Assert.assertTrue("Train accepts (ALLLOADS) Load name BOXES", train1.acceptsLoadName("BOXES"));
		Assert.assertTrue("Train accepts (ALLLOADS) Load name WOOD", train1.acceptsLoadName("WOOD"));

		train1.setLoadOption(Train.INCLUDELOADS);
		train1.addLoadName("BOXES");
		Assert.assertTrue("Train accepts (INCLUDELOADS) Load name BOXES", train1.acceptsLoadName("BOXES"));
		Assert.assertFalse("Train does not accept (INCLUDELOADS) Load name WOOD", train1.acceptsLoadName("WOOD"));

		train1.addLoadName("WOOD");
		Assert.assertTrue("Train still accepts (INCLUDELOADS) Load name BOXES", train1.acceptsLoadName("BOXES"));
		Assert.assertTrue("Train accepts (INCLUDELOADS) Load name WOOD", train1.acceptsLoadName("WOOD"));
		
		train1.addLoadName("Boxcar"+CarLoad.SPLIT_CHAR+"SCREWS");
		Assert.assertFalse("Train does not accept (INCLUDELOADS) Load name SCREWS", train1.acceptsLoadName("SCREWS"));
		Assert.assertTrue("Train still accepts (INCLUDELOADS) Load name BOXES", train1.acceptsLoad("BOXES", "Boxcar"));
		Assert.assertTrue("Train accepts (INCLUDELOADS) Load WOOD carried by Boxcar", train1.acceptsLoad("WOOD", "Boxcar"));
		Assert.assertTrue("Train accepts (INCLUDELOADS) Load Boxcar with SCREWS", train1.acceptsLoad("SCREWS", "Boxcar"));

		train1.deleteLoadName("BOXES");
		Assert.assertFalse("Train no longer accepts (INCLUDELOADS) Load name BOXES", train1.acceptsLoadName("BOXES"));
		Assert.assertTrue("Train still accepts (INCLUDELOADS) Load name WOOD", train1.acceptsLoadName("WOOD"));

		train1.setLoadOption(Train.EXCLUDELOADS);
		Assert.assertTrue("Train does accept (EXCLUDELOADS) Load name BOXES", train1.acceptsLoadName("BOXES"));
		Assert.assertFalse("Train does not accept (EXCLUDELOADS) Load name WOOD", train1.acceptsLoadName("WOOD"));

		train1.addLoadName("BOXES");
		Assert.assertFalse("Train does not accept (EXCLUDELOADS) Load name BOXES", train1.acceptsLoadName("BOXES"));
		Assert.assertFalse("Train still does not accept (EXCLUDELOADS) Load name WOOD", train1.acceptsLoadName("WOOD"));

		train1.deleteLoadName("WOOD");
		Assert.assertFalse("Train still does not accepts (EXCLUDELOADS) Load name BOXES", train1.acceptsLoadName("BOXES"));
		Assert.assertTrue("Train now accepts (EXCLUDELOADS) Load name WOOD", train1.acceptsLoadName("WOOD"));
	}
	
	public void testBuild(){
		TrainManager tmanager = TrainManager.instance();
		RouteManager rmanager = RouteManager.instance();
		LocationManager lmanager = LocationManager.instance();
		
		// disable build messages
		tmanager.setBuildMessagesEnabled(false);
		// disable build reports
		tmanager.setBuildReportEnabled(false);
		
		Train train = tmanager.newTrain("Test");
		
		// exercise manifest build
		train.setRailroadName("Working Railroad");
		train.setComment("One Hard Working Train");
		
		// build train without a route, should fail
		train.build();		
		Assert.assertFalse("Train should not build, no route", train.isBuilt());
		
		// now add a route that doesn't have any locations
		Route route = rmanager.newRoute("TestRoute");
		train.setRoute(route);
		train.build();
		Assert.assertFalse("Train should not build, no route locations", train.isBuilt());
		
		// now add a location to the route
		Location depart = lmanager.newLocation("depart");
		RouteLocation rl = route.addLocation(depart);		
		train.build();
		Assert.assertTrue("Train should build", train.isBuilt());
		
		// delete location
		lmanager.deregister(depart);
		train.build();
		Assert.assertFalse("Train should not build, location deleted", train.isBuilt());
		
		// recreate location
		depart = lmanager.newLocation("depart");
		train.build();
		Assert.assertFalse("Train should not build, location recreated, but not part of route", train.isBuilt());
		
		route.deleteLocation(rl);
		route.addLocation(depart);
		train.build();
		Assert.assertTrue("Train should build, route repaired", train.isBuilt());
		
		Location terminate = lmanager.newLocation("terminate");
		rl = route.addLocation(terminate);
		train.build();
		Assert.assertTrue("Train should build, route has two locations", train.isBuilt());
		
		// delete terminal location
		lmanager.deregister(terminate);
		train.build();
		Assert.assertFalse("Train should not build, terminal location deleted", train.isBuilt());

		route.deleteLocation(rl);
		terminate = lmanager.newLocation("terminate");
		rl = route.addLocation(terminate);
		train.build();
		Assert.assertTrue("Train should build, route has been repaired", train.isBuilt());
		
		Location middle = lmanager.newLocation("middle");
		// staging tracks in the middle of the route are ignored
		middle.addTrack("staging in the middle", Track.STAGING);
		
		// next 5 lines exercise manifest build messages
		Setup.setPrintLocationCommentsEnabled(true);
		middle.setComment("Middle comment");
		rl = route.addLocation(middle, 2);	// put location in middle of route
		rl.setDepartureTime("12:30");
		rl.setComment("This location has a departure time");
		
		train.build();
		Assert.assertTrue("Train should build, three location route", train.isBuilt());
		
		// delete location in the middle
		lmanager.deregister(middle);
		train.build();
		Assert.assertFalse("Train should not build, middle location deleted", train.isBuilt());
		
		// remove the middle location from the route
		route.deleteLocation(rl);
		train.build();
		Assert.assertTrue("Train should build, two location route", train.isBuilt());
	
		// Build option require cars
		Control.fullTrainOnly = true;
		train.build();
		Assert.assertFalse("Train should not build, requires cars", train.isBuilt());
		
		// restore control
		Control.fullTrainOnly = false;
		train.build();
		Assert.assertTrue("Train should build, build doesn require cars", train.isBuilt());

	}
	
	public void testAutoEngines(){
		TrainManager tmanager = TrainManager.instance();
		RouteManager rmanager = RouteManager.instance();
		LocationManager lmanager = LocationManager.instance();
		EngineManager emanager = EngineManager.instance();
		
		Train train = tmanager.newTrain("AutoEngineTest");
		train.setNumberEngines(Train.AUTO);
		
		Route route = rmanager.newRoute("AutoEngineTest");
		train.setRoute(route);
		
		Location A = lmanager.newLocation("A");
		Location B = lmanager.newLocation("B");
		Location C = lmanager.newLocation("C");
		Track At = A.addTrack("track", Track.SIDING);
		Track Bt = B.addTrack("track", Track.SIDING);
		Track Ct = C.addTrack("track", Track.SIDING);
		At.setLength(300);
		Bt.setLength(300);
		Ct.setLength(300);
		route.addLocation(A);
		RouteLocation rB = route.addLocation(B);
		route.addLocation(C);
		
		// Auto Engines calculates the number of engines based on maximum train length
		train.build();
		Assert.assertFalse("Train should not build, no engines", train.isBuilt());
		
		Engine e1 = emanager.newEngine("E", "1");
		e1.setModel("GP40");
		Engine e2 = emanager.newEngine("E", "2");
		e2.setModel("GP40");
		Engine e3 = emanager.newEngine("E", "3");
		e3.setModel("GP40");
		Engine e4 = emanager.newEngine("E", "4");
		e4.setModel("GP40");
		
		e1.setLocation(A, At);
		e2.setLocation(A, At);
		e3.setLocation(A, At);
		e4.setLocation(A, At);
		
		train.build();
		Assert.assertFalse("Train should not build, only single engines", train.isBuilt());
		
		Consist c = emanager.newConsist("c");
		e1.setConsist(c);
		e2.setConsist(c);
		
		// train should require two engines
		train.build();
		Assert.assertTrue("Train should build", train.isBuilt());
		
		Assert.assertEquals("e1 should be assigned to train", train, e1.getTrain());
		Assert.assertEquals("e2 should be assigned to train", train, e2.getTrain());
		
		rB.setGrade(2.5);	// 2.5% grade!
		
		// train should require four engines
		train.build();
		Assert.assertFalse("Train should not build, needs four engines, only two", train.isBuilt());
		
		e3.setConsist(c);
		train.build();
		Assert.assertFalse("Train should not build, needs four engines, only three", train.isBuilt());
		
		e4.setConsist(c);
		train.build();
		Assert.assertTrue("Train should build, four engines available", train.isBuilt());
		
		Setup.setEngineSize(3);	// limit the maximum to three engines
		train.build();
		Assert.assertFalse("Train should not build, needs four engines, three is the maximum allowed", train.isBuilt());

		// remove one engine from consist, train should build 
		c.delete(e4);
		train.build();
		Assert.assertTrue("Train should build, three engines available", train.isBuilt());

	}
	
	// test siding and yard moves
	// tests manual setting of destinations and trains for engines, cars, and cabooses.
	// tests consists and kernels
	public void testSidingsYards(){
		TrainManager tmanager = TrainManager.instance();
		RouteManager rmanager = RouteManager.instance();
		LocationManager lmanager = LocationManager.instance();
		EngineManager emanager = EngineManager.instance();
		CarManager cmanager = CarManager.instance();
		CarTypes ct = CarTypes.instance();
		
		// register the car and engine types used
		ct.addName("Boxcar");
		ct.addName("Caboose");
		ct.addName("Flat");
		
		// Set up two cabooses and six box cars
		Car c1 = cmanager.newCar("CP", "10");
		c1.setType("Caboose");
		c1.setLength("32");
		c1.setMoves(10);
		c1.setCaboose(true);

		Car c2 = cmanager.newCar("CP", "200");
		c2.setType("Caboose");
		c2.setLength("32");
		c2.setMoves(11);
		c2.setCaboose(true);

		Car c3 = cmanager.newCar("CP", "30");
		c3.setType("Boxcar");
		c3.setLength("40");
		c2.setMoves(12);

		Car c4 = cmanager.newCar("CP", "4000");
		c4.setType("Boxcar");
		c4.setLength("40");
		c4.setMoves(13);

		Car c5 = cmanager.newCar("CP", "5");
		c5.setType("Boxcar");
		c5.setLength("40");
		c5.setMoves(14);

		Car c6 = cmanager.newCar("CP", "60");
		c6.setType("Boxcar");
		c6.setLength("40");
		c6.setMoves(15);
		
		Car c7 = cmanager.newCar("CP", "700");
		c7.setType("Flat");
		c7.setLength("50");
		c7.setMoves(16);	
		
		Car c8 = cmanager.newCar("CP", "8000");
		c8.setType("Boxcar");
		c8.setLength("60");
		c8.setMoves(17);
		
		Car c9 = cmanager.newCar("CP", "9");
		c9.setType("Flat");
		c9.setLength("40");
		c9.setMoves(18);
		
		Car c10 = cmanager.newCar("CP", "1000");
		c10.setType("Flat");
		c10.setLength("40");
		c10.setMoves(19);
		
		// place two engines in a consist
		Consist con1 = emanager.newConsist("CP");
		
		Engine e1 = emanager.newEngine("CP", "10");
		e1.setModel("GP30");
		e1.setConsist(con1);
		Engine e2 = emanager.newEngine("CP", "20");
		e2.setModel("GP30");
		e2.setConsist(con1);
		
		// Set up a route of 3 locations: Foxboro (2 tracks, yard and siding), 
		// Acton (2 tracks, sidings), and Nashua (2 tracks, yards).
		Location l1 = lmanager.newLocation("Foxboro");
		Assert.assertEquals("Location 1 Name", "Foxboro", l1.getName());
		Assert.assertEquals("Location 1 Initial Length", 0, l1.getLength());

		Track l1s1 = l1.addTrack("Foxboro Siding", Track.SIDING);
		l1s1.setLength(600);
		Assert.assertEquals("Location 1s1 Name", "Foxboro Siding", l1s1.getName());
		Assert.assertEquals("Location 1s1 LocType", "Siding", l1s1.getLocType());
		Assert.assertEquals("Location 1s1 Length", 600, l1s1.getLength());
		Assert.assertEquals("Default directions", DIRECTION_ALL, l1s1.getTrainDirections());
	
		Track l1s2 = l1.addTrack("Foxboro Yard", Track.YARD);
		l1s2.setLength(400);
		Assert.assertEquals("Location 1s2 Name", "Foxboro Yard", l1s2.getName());
		Assert.assertEquals("Location 1s2 LocType", "Yard", l1s2.getLocType());
		Assert.assertEquals("Location 1s2 Length", 400, l1s2.getLength());

		Assert.assertEquals("Location 1 Length", 1000, l1.getLength());

		Location l2 = lmanager.newLocation("Acton");
		Assert.assertEquals("Location 2 Name", "Acton", l2.getName());
				
		Track l2s1 = l2.addTrack("Acton Siding 1", Track.SIDING);
		l2s1.setLength(543);
		l2s1.setMoves(1);
		Assert.assertEquals("Location 2s1 Name", "Acton Siding 1", l2s1.getName());
		Assert.assertEquals("Location 2s1 LocType", Track.SIDING, l2s1.getLocType());
		Assert.assertEquals("Location 2s1 Length", 543, l2s1.getLength());

		Track l2s2 = l2.addTrack("Acton Siding 2", Track.SIDING);
		l2s2.setLength(345);
				
		Assert.assertEquals("Acton Length", 888, l2.getLength());

		Location l3 = lmanager.newLocation("Nashua");
		Track l3s1 = l3.addTrack("Nashua Yard 1", Track.YARD);
		l3s1.setLength(301);

		Track l3s2 = l3.addTrack("Nashua Yard 2", Track.YARD);
		l3s2.setLength(402);

		Assert.assertEquals("Location 3 Length", 703, l3.getLength());
		
		// define the route
		Setup.setCarMoves(6);	// set default to 6 moves per location
		Route r1 = rmanager.newRoute("Foxboro-Acton-Nashua-Acton-Foxboro");
		RouteLocation rl1 = r1.addLocation(l1);
		rl1.setTrainIconX(25);	// set the train icon coordinates
		rl1.setTrainIconY(225);
		RouteLocation rl2 = r1.addLocation(l2);
		rl2.setTrainIconX(75);	// set the train icon coordinates
		rl2.setTrainIconY(225);
		RouteLocation rl3 = r1.addLocation(l3);
		rl3.setTrainIconX(125);	// set the train icon coordinates
		rl3.setTrainIconY(225);
		RouteLocation rl4 = r1.addLocation(l2);
		rl4.setTrainIconX(175);	// set the train icon coordinates
		rl4.setTrainIconY(225);
		RouteLocation rl5 = r1.addLocation(l1);
		rl5.setTrainIconX(225);	// set the train icon coordinates
		rl5.setTrainIconY(225);
		
		// turn off build fail messages
		tmanager.setBuildMessagesEnabled(false);
		
		// define the train		
		Train t1 = tmanager.newTrain("FF");
		t1.setRoute(r1);
		
		t1.build();	
		Assert.assertTrue("train built 1", t1.isBuilt());
		Assert.assertEquals("should be 0 cars", 0, t1.getNumberCarsWorked());
		
		// place the cars on the tracks
		// Place cars
		Assert.assertEquals("Place c1", Track.OKAY, c1.setLocation(l1, l1s1));
		Assert.assertEquals("Place c2", Track.OKAY, c2.setLocation(l1, l1s1));
		Assert.assertEquals("Place c3", Track.OKAY, c3.setLocation(l1, l1s1));
		Assert.assertEquals("Place c4", Track.OKAY, c4.setLocation(l1, l1s1));	
		Assert.assertEquals("Place c5", Track.OKAY, c5.setLocation(l1, l1s1));
		Assert.assertEquals("Place c6", Track.OKAY, c6.setLocation(l1, l1s1));
		Assert.assertEquals("Place c7", Track.OKAY, c7.setLocation(l1, l1s1));
		Assert.assertEquals("Place c8", Track.OKAY, c8.setLocation(l1, l1s1));		
		Assert.assertEquals("Place c9", Track.OKAY, c9.setLocation(l1, l1s1));
		Assert.assertEquals("Place c10", Track.OKAY, c10.setLocation(l1, l1s1));
		Assert.assertEquals("Place e1", Track.OKAY, e1.setLocation(l1, l1s1));
		
		t1.build();	
		Assert.assertTrue("train built 2", t1.isBuilt());
		Assert.assertEquals("should be 6 cars", 6, t1.getNumberCarsWorked());

		// check car destinations
		Assert.assertEquals("Destination c1", "", c1.getDestinationName());
		Assert.assertEquals("Destination c2", "", c2.getDestinationName());
		Assert.assertEquals("Destination c3", "Acton", c3.getDestinationName());
		Assert.assertEquals("Destination c4", "Nashua", c4.getDestinationName());	
		Assert.assertEquals("Destination c5", "Acton", c5.getDestinationName());
		Assert.assertEquals("Destination c6", "Nashua", c6.getDestinationName());
		Assert.assertEquals("Destination c7", "Acton", c7.getDestinationName());
		Assert.assertEquals("Destination c8", "Nashua", c8.getDestinationName());		
		Assert.assertEquals("Destination c9", "", c9.getDestinationName());
		Assert.assertEquals("Destination c10", "", c10.getDestinationName());
		Assert.assertEquals("Destination e1", "", e1.getDestinationName());
		
		// release cars from train
		Assert.assertTrue("reset train", t1.reset());
		// set c3, c5, c6, c8, c10 destination to be Nashua
		c3.setDestination(l3, l3s1);
		c5.setDestination(l3, l3s1);
		c6.setDestination(l3, l3s1);
		c8.setDestination(l3, l3s1);
		c10.setDestination(l3, l3s1);
		// set c5 and c9 to be serviced by train TT
		Train t2 = tmanager.newTrain("TT");
		c5.setTrain(t2);
		c9.setTrain(t2);
		// set c6 to be serviced by train FF
		c6.setTrain(t1);
		
		// require a caboose
		t1.setRequirements(Train.CABOOSE);
		t1.build();
		Assert.assertTrue("train built 3", t1.isBuilt());

		// check car destinations
		Assert.assertEquals("2 Destination  c1", "Foxboro", c1.getDestinationName());
		Assert.assertEquals("2 Destination  c2", "", c2.getDestinationName());
		Assert.assertEquals("2 Destination  c3", "Nashua", c3.getDestinationName());
		Assert.assertEquals("2 Destination  c4", "Acton", c4.getDestinationName());	
		Assert.assertEquals("2 Destination  c5", "Nashua", c5.getDestinationName());
		Assert.assertEquals("2 Destination  c6", "Nashua", c6.getDestinationName());
		Assert.assertEquals("2 Destination  c7", "Acton", c7.getDestinationName());
		Assert.assertEquals("2 Destination  c8", "Nashua", c8.getDestinationName());		
		Assert.assertEquals("2 Destination  c9", "", c9.getDestinationName());
		Assert.assertEquals("2 Destination  c10", "Nashua", c10.getDestinationName());		

		// move and terminate the train
		t1.move(); 	// to Acton
		t1.move();	// to Nashua
		t1.move();	// to Acton
		t1.move();	// to Foxboro
		t1.move();	// terminate
		
		// check car destinations
		Assert.assertEquals("3 Destination c1", "", c1.getDestinationName());
		Assert.assertEquals("3 Destination c2", "", c2.getDestinationName());
		Assert.assertEquals("3 Destination c3", "", c3.getDestinationName());
		Assert.assertEquals("3 Destination c4", "", c4.getDestinationName());	
		Assert.assertEquals("3 Destination c5", "Nashua", c5.getDestinationName());
		Assert.assertEquals("3 Destination c6", "", c6.getDestinationName());
		Assert.assertEquals("3 Destination c7", "", c7.getDestinationName());
		Assert.assertEquals("3 Destination c8", "", c8.getDestinationName());		
		Assert.assertEquals("3 Destination c9", "", c9.getDestinationName());
		Assert.assertEquals("3 Destination c10", "Nashua", c10.getDestinationName());
		
		// check car locations
		Assert.assertEquals("Location c1", "Foxboro", c1.getLocationName());
		Assert.assertEquals("Location c2", "Foxboro", c2.getLocationName());
		Assert.assertEquals("Location c3", "Nashua", c3.getLocationName());
		Assert.assertEquals("Location c4", "Acton", c4.getLocationName());	
		Assert.assertEquals("Location c5", "Foxboro", c5.getLocationName());
		Assert.assertEquals("Location c6", "Nashua", c6.getLocationName());
		Assert.assertEquals("Location c7", "Acton", c7.getLocationName());
		Assert.assertEquals("Location c8", "Nashua", c8.getLocationName());		
		Assert.assertEquals("Location c9", "Foxboro", c9.getLocationName());
		Assert.assertEquals("Location c10", "Foxboro", c10.getLocationName());

		// now set caboose destinations that aren't the terminal
		Assert.assertEquals("set destination c1", Track.OKAY, c1.setDestination(l2, l2s1));
		Assert.assertEquals("set destination c2", Track.OKAY, c2.setDestination(l3, l3s1));
		
		// train requires a caboose, should fail	
		t1.build();
		Assert.assertFalse("train built 4", t1.isBuilt());
		
		// Set caboose destination to be the terminal
		Assert.assertEquals("set caboose destination", Track.OKAY, c2.setDestination(l1, l1s2));
		t1.build();
		Assert.assertTrue("train built 5", t1.isBuilt());
		Assert.assertTrue("train reset 5", t1.reset());
		
		// set the cabooses to train FF
		c1.setTrain(t2);
		c2.setTrain(t2);
		
		// build should fail
		t1.build();
		Assert.assertFalse("train built 6", t1.isBuilt());
		
		// set caboose to train TT
		c1.setTrain(t1);
		t1.build();
		Assert.assertTrue("train built 7", t1.isBuilt());
		
		// check car destinations
		Assert.assertEquals("4 Destination c1", "Foxboro", c1.getDestinationName());
		Assert.assertEquals("4 Destination c2", "", c2.getDestinationName());
		Assert.assertEquals("4 Destination c3", "Acton", c3.getDestinationName());
		Assert.assertEquals("4 Destination c4", "Nashua", c4.getDestinationName());	
		Assert.assertEquals("4 Destination c5", "Nashua", c5.getDestinationName());
		Assert.assertEquals("4 Destination c6", "Foxboro", c6.getDestinationName());
		Assert.assertEquals("4 Destination c7", "Foxboro", c7.getDestinationName());
		Assert.assertEquals("4 Destination c8", "Foxboro", c8.getDestinationName());		
		Assert.assertEquals("4 Destination c9", "", c9.getDestinationName());
		Assert.assertEquals("4 Destination c10", "Acton", c10.getDestinationName());
		
		// add an engine
		t1.setNumberEngines("2");
		t1.build();
		Assert.assertTrue("train built 8", t1.isBuilt());
		Assert.assertEquals("5 Destination e1", "Foxboro", e1.getDestinationName());
		Assert.assertEquals("5 Destination e2", "Foxboro", e2.getDestinationName());
		
		t1.reset();
		// assign lead engine to train TT
		e1.setTrain(t2);
		// should fail
		t1.build();
		Assert.assertFalse("train built 9", t1.isBuilt());
		
		// assign one of the consist engine to train TT
		e1.setTrain(t1);
		e2.setTrain(t2);	// shouldn't pay attention to the other engine
		// should build
		t1.build();
		Assert.assertTrue("train built 10", t1.isBuilt());
		t1.reset();
		
		// both engines should release
		Assert.assertEquals("6 Destination e1", "", e1.getDestinationName());
		Assert.assertEquals("6 Train e1", "", e1.getTrainName());
		Assert.assertEquals("6 Destination e2", "", e2.getDestinationName());
		Assert.assertEquals("6 Train e2", "", e2.getTrainName());

		// now try setting engine destination that isn't the terminal
		Assert.assertEquals("set destination e1", Track.OKAY, e1.setDestination(l2, l2s1));
		// should fail
		t1.build();
		Assert.assertFalse("train built 11", t1.isBuilt());
		
		e1.setDestination(l1, l1s2);
		e2.setDestination(l2, l2s1);	// program should ignore
		// should build
		t1.build();
		Assert.assertTrue("train built 12", t1.isBuilt());
		
		// set lead engine's track to null
		Assert.assertEquals("Place e1", Track.OKAY, e1.setLocation(l1, null));
		// should not build
		t1.build();
		Assert.assertFalse("train will not build engine track is null", t1.isBuilt());
		Assert.assertEquals("Place e1", Track.OKAY, e1.setLocation(l1, l1s1));
		
		// should now build
		t1.build();
		
		// move and terminate the train
		t1.move(); 	// to Acton
		t1.move();	// to Nashua
		t1.move();	// to Acton
		t1.move();	// to Foxboro
		t1.move();	// terminate
		
		// check engine final locations
		Assert.assertEquals("Location e1", "Foxboro", e1.getLocationName());
		Assert.assertEquals("Location e2", "Foxboro", e2.getLocationName());
		
		// move c7 & c8 to Foxboro to help test kernels
		Assert.assertEquals("Place c7", Track.OKAY, c7.setLocation(l1, l1s1));
		Assert.assertEquals("Place c8", Track.OKAY, c8.setLocation(l1, l1s1));
		// now test kernels
		Kernel k1 = cmanager.newKernel("group of cars");
		c8.setKernel(k1);	// lead car
		c7.setKernel(k1);
		c7.setTrain(t2);	// program should ignore
		c3.setLocation(l1, l1s1);
		c3.setKernel(k1);
		c3.setDestination(l1, l1s1);	// program should ignore (produces debug warning)
		
		// should build
		t1.build();
		Assert.assertTrue("train built 12", t1.isBuilt());
		Assert.assertEquals("12 Location c3", "Foxboro", c3.getLocationName());
		Assert.assertEquals("12 Location c7", "Foxboro", c7.getLocationName());
		Assert.assertEquals("12 Location c8", "Foxboro", c8.getLocationName());
		Assert.assertEquals("12 Destination c3", "Nashua", c3.getDestinationName());
		Assert.assertEquals("12 Destination c7", "Nashua", c7.getDestinationName());
		Assert.assertEquals("12 Destination c8", "Nashua", c8.getDestinationName());
		
		// move and terminate the train
		t1.move(); 	// to Acton
		t1.move();	// to Nashua
		t1.move();	// to Acton
		t1.move();	// to Foxboro
		t1.move();	// terminate
		
		Assert.assertEquals("13 Location c3", "Nashua", c3.getLocationName());
		Assert.assertEquals("13 Location c7", "Nashua", c7.getLocationName());
		Assert.assertEquals("13 Location c8", "Nashua", c8.getLocationName());
		Assert.assertEquals("13 Destination c3", "", c3.getDestinationName());
		Assert.assertEquals("13 Destination c7", "", c7.getDestinationName());
		Assert.assertEquals("13 Destination c8", "", c8.getDestinationName());
		Assert.assertEquals("13 Train c3", "", c3.getTrainName());
		Assert.assertEquals("13 Train c7", "", c7.getTrainName());
		Assert.assertEquals("13 Train c8", "", c8.getTrainName());
		
		// now test car with FRED
		c1.setCaboose(false);
		c2.setCaboose(false);
		c1.setFred(true);
		c2.setFred(true);
		c1.setType("Boxcar");	// change the type, now Boxcar with FRED
		c2.setType("Boxcar");
		c2.setTrain(null);
		
		// train requires a caboose, there are none, should fail	
		t1.build();
		Assert.assertFalse("train built 14", t1.isBuilt());
		
		// change requirement to car with FRED
		t1.setRequirements(Train.FRED);
		
		// train requires a car with FRED, should pass	
		t1.build();
		Assert.assertTrue("train built 15", t1.isBuilt());
		Assert.assertTrue("train reset 15", t1.reset()); // release cars
				
		// now set FRED destinations that aren't the terminal
		Assert.assertEquals("set destination c1", Track.OKAY, c1.setDestination(l2, l2s1));
		Assert.assertEquals("set destination c2", Track.OKAY, c2.setDestination(l3, l3s1));
		
		// train requires a car with FRED, should fail	
		t1.build();
		Assert.assertFalse("train built 16", t1.isBuilt());
		
		// Set FRED destination to be the terminal
		Assert.assertEquals("set destination c2", Track.OKAY, c2.setDestination(l1, l1s2));
		t1.build();
		Assert.assertTrue("train built 17", t1.isBuilt());
		Assert.assertTrue("train reset 17", t1.reset());
			
		// set the cars with FRED to train FF
		c1.setTrain(t2);
		c2.setTrain(t2);
		
		// build should fail
		t1.build();
		Assert.assertFalse("train built 18", t1.isBuilt());
		
		// set car with FRED to train TT
		c2.setTrain(t1);
		t1.build();
		Assert.assertTrue("train built 19", t1.isBuilt());
		
		// test car out of service
		c2.setOutOfService(true);
		t1.build();
		Assert.assertFalse("required car is out of service", t1.isBuilt());
		
		// test location unknown
		c2.setOutOfService(false);
		c2.setLocationUnknown(true);
		t1.build();
		Assert.assertFalse("required car location is unknown", t1.isBuilt());
		
		c2.setLocationUnknown(false);
		t1.build();
		Assert.assertTrue("need car is available", t1.isBuilt());
		
		c2.setWait(1);
		t1.build();
		Assert.assertFalse("required car will wait for next train", t1.isBuilt());
		
		t1.build();
		Assert.assertTrue("next train!", t1.isBuilt());
		Assert.assertEquals("CP 4000 destination", "Nashua", c4.getDestinationName());
		
		// test train and location direction controls
		c8.setLocation(l2, l2s1);	// place led car of kernel in Action Siding 1
		l2.setTrainDirections(Location.EAST + Location.SOUTH + Location.WEST);	// train is north bound
		t1.build();
		
		// build should fail, cars c3 and c7 which is part of c8 kernel are on the wrong track
		Assert.assertFalse("Train direction test", t1.isBuilt());
		c3.setLocation(l2, l2s1);	// place c3 Action Siding 1
		c7.setLocation(l2, l2s1);	// place c7 Action Siding 1
		
		t1.build();
		Assert.assertTrue("Train direction test", t1.isBuilt());
		Assert.assertEquals("CP 1000 destination is now Nashua", "Nashua", c10.getDestinationName());
		Assert.assertEquals("CP 30 at Acton, not serviced", null, c3.getTrain());
		Assert.assertEquals("CP 700 at Acton, not serviced", null, c7.getTrain());
		Assert.assertEquals("CP 8000 at Acton, not serviced", null, c8.getTrain());
		
		// restore Acton
		l2.setTrainDirections(Location.NORTH);	// train is north bound
		t1.build();
		
		Assert.assertEquals("CP 1000 destination is now", "Acton", c10.getDestinationName());
		Assert.assertEquals("CP 30 at Acton", t1, c3.getTrain());
		Assert.assertEquals("CP 700 at Acton", t1, c7.getTrain());
		Assert.assertEquals("CP 8000 at Acton", t1, c8.getTrain());

		// restrict train direction at the track level
		l2s2.setTrainDirections(Track.EAST + Track.SOUTH + Track.WEST);
		// take one car out of kernel
		c3.setKernel(null);
		c3.setLocation(l2, l2s1);	// place car in Action Siding 1	
		c8.setLocation(l2, l2s2);	// place lead car in Action Yard
		c7.setLocation(l2, l2s2);	// place c7 in Action Yard
		t1.build();
		
		Assert.assertEquals("CP 1000 destination track", "Acton Siding 1", c1.getDestinationTrackName());
		Assert.assertEquals("CP 200 at Foxboro Siding", t1, c2.getTrain());
		Assert.assertEquals("CP 30 at Acton Siding 1", t1, c3.getTrain());
		Assert.assertEquals("CP 30 destination track", "Nashua Yard 2", c3.getDestinationTrackName());
		Assert.assertEquals("CP 4000 at Foxboro Siding", t1, c4.getTrain());
		Assert.assertEquals("CP 4000 destination", "Foxboro", c4.getDestinationName());
		Assert.assertEquals("CP 4000 destination track", "Foxboro Yard", c4.getDestinationTrackName());
		Assert.assertEquals("CP 60 destination track", "", c6.getDestinationTrackName());
		Assert.assertEquals("CP 700 at Acton, not serviced, part of kernel CP 8000", null, c7.getTrain());
		Assert.assertEquals("CP 8000 at Acton, Acton Siding 2 not serviced", null, c8.getTrain());
		
		// test train length restrictions
		rl1.setMaxTrainLength(155);	// only enough for the two engines, train needs a car with FRED
		t1.build();
		
		Assert.assertFalse("Train length test, can't service car with FRED", t1.isBuilt());
		// build failed after engines were assigned to train 1
		Assert.assertEquals("Engine assignment ignores train length restrictions", t1, e1.getTrain());
		Assert.assertEquals("Engine assignment ignores train length restrictions", t1, e2.getTrain());
		Assert.assertEquals("Engine destination ignores train length restrictions", "Foxboro", e1.getDestinationName());
		Assert.assertEquals("Engine destination ignores train length restrictions", "Foxboro", e2.getDestinationName());
		
		Assert.assertEquals("Check CP30 engine length", "56", e1.getLength());
		Assert.assertEquals("Check CP 200 length", "32", c2.getLength());
		rl1.setMaxTrainLength(156);	// enough for the two engines and a car with FRED 56 + 56 + 32 + 12(couplers) = 156
		
		t1.build();
		Assert.assertTrue("Train length test, just enough length for engines and car with FRED", t1.isBuilt());
		Assert.assertEquals("CP 200 at Foxboro Siding", t1, c2.getTrain());
		Assert.assertEquals("CP 200 destination track", "Foxboro Siding", c2.getDestinationTrackName());
		Assert.assertEquals("CP 30 at Acton Siding 1", t1, c3.getTrain());
		Assert.assertEquals("CP 30 destination track", "Nashua Yard 1", c3.getDestinationTrackName());
		Assert.assertEquals("CP 4000 at Foxboro Siding", t1, c4.getTrain());
		Assert.assertEquals("CP 60 destination track", "", c6.getDestinationTrackName());
		Assert.assertEquals("CP 700 at Acton, not serviced, part of kernel CP 8000", null, c7.getTrain());
		Assert.assertEquals("CP 8000 at Acton, Acton Siding 2 not serviced", null, c8.getTrain());
		Assert.assertEquals("CP 1000 not part of train", null, c10.getTrain());
		
		// Increase the train length from the departure location
		rl1.setMaxTrainLength(1000);
		rl2.setMaxTrainLength(156);	// restrict train length from Acton
		t1.build();
		Assert.assertTrue("Train length test, just enough length for engines and car with FRED", t1.isBuilt());
		Assert.assertEquals("CP 200 at Foxboro Siding", t1, c2.getTrain());
		Assert.assertEquals("CP 200 destination track", "Foxboro Yard", c2.getDestinationTrackName());
		Assert.assertEquals("CP 30 at Acton Siding 1", t1, c3.getTrain());
		Assert.assertEquals("CP 30 destination track", "Foxboro Yard", c3.getDestinationTrackName());
		Assert.assertEquals("CP 4000 at Foxboro Yard", t1, c4.getTrain());
		Assert.assertEquals("CP 4000 destination track", "Foxboro Siding", c4.getDestinationTrackName());
		Assert.assertEquals("CP 60 destination track", "", c6.getDestinationTrackName());
		Assert.assertEquals("CP 1000 part of train", t1, c10.getTrain());
		Assert.assertEquals("CP 1000 destination track", "Acton Siding 1", c10.getDestinationTrackName());
		
		// test setting car's destination to Foxboro Siding
		c2.setDestination(l1, l1s1);
		t1.build();
		Assert.assertTrue("car with FRED has destination", t1.isBuilt());
		t1.reset();
		
		// again, but now change car type serviced by Foxboro Yard 
		c2.setDestination(l1, l1s1);
		l1s1.deleteTypeName("Boxcar");
		t1.build();
		Assert.assertFalse("car with FRED has destination that won't accept it", t1.isBuilt());
		l1s1.addTypeName("Boxcar");
		
		c6.setDestination(l2, l2s2);	// destination Action Siding 2
		l2s2.deleteTypeName("Boxcar");	// don't allow Boxcar to drop
		t1.build();
		Assert.assertTrue("car with FRED has destination that will now accept it", t1.isBuilt());
		Assert.assertEquals("CP 60 can't be delivered", null, c6.getTrain());
				
		c2.setTrack(null);
		t1.build();
		Assert.assertFalse("need car doesn't have a track assignment", t1.isBuilt());		
			
		// end testSidingsYards
	}

	// test train staging to staging
	public void testStagingtoStaging() {
		TrainManager tmanager = TrainManager.instance();
		RouteManager rmanager = RouteManager.instance();
		LocationManager lmanager = LocationManager.instance();
		EngineManager emanager = EngineManager.instance();
		CarManager cmanager = CarManager.instance();
		CarTypes ct = CarTypes.instance();
		EngineTypes et = EngineTypes.instance();

		// register the car and engine types used
		ct.addName("Boxcar");
		ct.addName("Caboose");
		ct.addName("Flat");
		et.addName("Diesel");

		// Set up four engines in two consists 
		Consist con1 = emanager.newConsist("C16");	
		Consist con2 = emanager.newConsist("C14");

		Engine e1 = new Engine("PC", "5016");
		e1.setModel("GP40");
		e1.setConsist(con1);
		e1.setMoves(123);
		e1.setOwner("AT");
		e1.setBuilt("1990");
		Assert.assertEquals("Engine 1 Length", "59", e1.getLength());
		emanager.register(e1);

		Engine e2 = new Engine("PC", "5019");
		e2.setModel("GP40");
		e2.setConsist(con1);
		e2.setMoves(321);
		e2.setOwner("AT");
		e2.setBuilt("1990");
		Assert.assertEquals("Engine 2 Length", "59", e2.getLength());
		emanager.register(e2);

		Engine e3 = new Engine("PC", "5524");
		e3.setModel("SD45");
		e3.setConsist(con2);
		e3.setOwner("DAB");
		e3.setBuilt("1980");
		Assert.assertEquals("Engine 3 Length", "66", e3.getLength());
		emanager.register(e3);

		Engine e4 = new Engine("PC", "5559");
		e4.setModel("SD45");
		e4.setConsist(con2);
		e4.setOwner("DAB");
		e4.setBuilt("1980");
		Assert.assertEquals("Engine 4 Length", "66", e4.getLength());
		emanager.register(e4);

		// Set up two cabooses and six box cars
		Car c1 = new Car("CP", "C10099");
		c1.setType("Caboose");
		c1.setLength("32");
		c1.setMoves(23);
		c1.setOwner("AT");
		c1.setBuilt("1980");
		c1.setCaboose(true);
		Assert.assertEquals("Caboose 1 Length", "32", c1.getLength());
		cmanager.register(c1);

		Car c2 = new Car("CP", "C20099");
		c2.setType("Caboose");
		c2.setLength("32");
		c2.setMoves(54);
		c2.setOwner("DAB");
		c2.setBuilt("1984");
		c2.setCaboose(true);
		Assert.assertEquals("Caboose 2 Length", "32", c2.getLength());
		cmanager.register(c2);

		Car c3 = new Car("CP", "X10001");
		c3.setType("Boxcar");
		c3.setLength("40");
		c3.setOwner("DAB");
		c3.setBuilt("1984");
		Assert.assertEquals("Box Car X10001 Length", "40", c3.getLength());
		cmanager.register(c3);

		Car c4 = new Car("CP", "X10002");
		c4.setType("Boxcar");
		c4.setLength("40");
		c4.setOwner("AT");
		c4.setBuilt("1-84");
		c4.setMoves(4444);
		Assert.assertEquals("Box Car X10002 Length", "40", c4.getLength());
		cmanager.register(c4);

		Car c5 = new Car("CP", "X20001");
		c5.setType("Boxcar");
		c5.setLength("40");
		c5.setOwner("DAB");
		c5.setBuilt("1980");
		Assert.assertEquals("Box Car X20001 Length", "40", c5.getLength());
		cmanager.register(c5);

		Car c6 = new Car("CP", "X20002");
		c6.setType("Boxcar");
		c6.setLength("40");
		c6.setOwner("DAB");
		c6.setBuilt("1978");
		Assert.assertEquals("Box Car X20002 Length", "40", c6.getLength());
		cmanager.register(c6);
		
		Car c7 = new Car("CP", "777");
		c7.setType("Flat");
		c7.setLength("50");
		c7.setOwner("AT");
		c7.setBuilt("1990");
		c7.setMoves(6);	
		Assert.assertEquals("Box Car 777 Length", "50", c7.getLength());
		cmanager.register(c7);
		
		Car c8 = new Car("CP", "888");
		c8.setType("Boxcar");
		c8.setLength("60");
		c8.setOwner("DAB");
		c8.setBuilt("1985");
		Assert.assertEquals("Box Car 888 Length", "60", c8.getLength());
		cmanager.register(c8);
		
		Car c9 = new Car("CP", "99");
		c9.setType("Flat");
		c9.setLength("90");
		c9.setOwner("AT");
		c9.setBuilt("6-80");
		Assert.assertEquals("Box Car 888 Length", "90", c9.getLength());
		cmanager.register(c9);
		
		// do cars have the right default loads?
		Assert.assertEquals("Car c1 load should be E", "E", c1.getLoad());
		Assert.assertEquals("Car c2 load should be E", "E", c2.getLoad());
		Assert.assertEquals("Car c3 load should be E", "E", c3.getLoad());
		Assert.assertEquals("Car c4 load should be E", "E", c4.getLoad());
		Assert.assertEquals("Car c5 load should be E", "E", c5.getLoad());
		Assert.assertEquals("Car c6 load should be E", "E", c6.getLoad());
		Assert.assertEquals("Car c7 load should be E", "E", c7.getLoad());
		Assert.assertEquals("Car c8 load should be E", "E", c8.getLoad());
		Assert.assertEquals("Car c9 load should be E", "E", c9.getLoad());

		// Set up a route of 3 locations: North End Staging (2 tracks), 
		// North Industries (1 track), and South End Staging (2 tracks).
		Location l1 = new Location("1", "North End");
		Assert.assertEquals("Location 1 Id", "1", l1.getId());
		Assert.assertEquals("Location 1 Name", "North End", l1.getName());
		Assert.assertEquals("Location 1 Initial Length", 0, l1.getLength());
		l1.setLocationOps(Location.STAGING);
		l1.setTrainDirections(DIRECTION_ALL);
		l1.setSwitchListEnabled(true);
		lmanager.register(l1);

		Track l1s1 = new Track("1s1", "North End 1", Track.STAGING, l1);
		l1s1.setLength(300);
		Assert.assertEquals("Location 1s1 Id", "1s1", l1s1.getId());
		Assert.assertEquals("Location 1s1 Name", "North End 1", l1s1.getName());
		Assert.assertEquals("Location 1s1 LocType", "Staging", l1s1.getLocType());
		Assert.assertEquals("Location 1s1 Length", 300, l1s1.getLength());
		l1s1.setTrainDirections(DIRECTION_ALL);
		l1s1.setRoadOption(Track.ALLROADS);
		l1s1.setDropOption(Track.ANY);
		l1s1.setPickupOption(Track.ANY);

		Track l1s2 = new Track("1s2", "North End 2", Track.STAGING, l1);
		l1s2.setLength(400);
		Assert.assertEquals("Location 1s2 Id", "1s2", l1s2.getId());
		Assert.assertEquals("Location 1s2 Name", "North End 2", l1s2.getName());
		Assert.assertEquals("Location 1s2 LocType", "Staging", l1s2.getLocType());
		Assert.assertEquals("Location 1s2 Length", 400, l1s2.getLength());
		l1s2.setTrainDirections(DIRECTION_ALL);
		l1s2.setRoadOption(Track.ALLROADS);
		l1s2.setDropOption(Track.ANY);
		l1s2.setPickupOption(Track.ANY);

		l1.addTrack("North End 1", Track.STAGING);
		l1.addTrack("North End 2", Track.STAGING);
		List<String> templist1 = l1.getTrackIdsByNameList("");
		for (int i = 0; i < templist1.size(); i++){
			if (i == 0) {
				Assert.assertEquals("RL 1 Staging 1 Name", "North End 1", templist1.get(i));
			}
			if (i == 1) {
				Assert.assertEquals("RL 1 Staging 2 Name", "North End 2", templist1.get(i));
			}
		}

		l1.register(l1s1);
		l1.register(l1s2);

		Assert.assertEquals("Location 1 Length", 700, l1.getLength());

		Location l2 = new Location("2", "North Industries");
		Assert.assertEquals("Location 2 Id", "2", l2.getId());
		Assert.assertEquals("Location 2 Name", "North Industries", l2.getName());
		l2.setLocationOps(Location.NORMAL);
		l2.setTrainDirections(DIRECTION_ALL);
		l2.setSwitchListEnabled(true);
		lmanager.register(l2);
				
		Track l2s1 = new Track("2s1", "NI Yard", Track.YARD, l2);
		l2s1.setLength(432);
		Assert.assertEquals("Location 2s1 Id", "2s1", l2s1.getId());
		Assert.assertEquals("Location 2s1 Name", "NI Yard", l2s1.getName());
		Assert.assertEquals("Location 2s1 LocType", Track.YARD, l2s1.getLocType());
		Assert.assertEquals("Location 2s1 Length", 432, l2s1.getLength());
		l2s1.setTrainDirections(DIRECTION_ALL);
		
		l2.register(l2s1);
		Assert.assertEquals("Location 2 Length", 432, l2.getLength());

		Location l3 = new Location("3", "South End");
		Assert.assertEquals("Location 3 Id", "3", l3.getId());
		Assert.assertEquals("Location 3 Name", "South End", l3.getName());
		Assert.assertEquals("Location 3 Initial Length", 0, l3.getLength());
		l3.setLocationOps(Location.STAGING);
		l3.setTrainDirections(DIRECTION_ALL);
		l3.setSwitchListEnabled(true);
		lmanager.register(l3);

		Track l3s1 = new Track("3s1", "South End 1", Track.STAGING, l3);
		l3s1.setLength(300);
		Assert.assertEquals("Location 3s1 Id", "3s1", l3s1.getId());
		Assert.assertEquals("Location 3s1 Name", "South End 1", l3s1.getName());
		Assert.assertEquals("Location 3s1 LocType", "Staging", l3s1.getLocType());
		Assert.assertEquals("Location 3s1 Length", 300, l3s1.getLength());
		l3s1.setTrainDirections(DIRECTION_ALL);
		l3s1.setRoadOption(Track.ALLROADS);
		l3s1.setDropOption(Track.ANY);
		l3s1.setPickupOption(Track.ANY);

		Track l3s2 = new Track("3s2", "South End 2", Track.STAGING, l3);
		l3s2.setLength(401);
		Assert.assertEquals("Location 3s2 Id", "3s2", l3s2.getId());
		Assert.assertEquals("Location 3s2 Name", "South End 2", l3s2.getName());
		Assert.assertEquals("Location 3s2 LocType", "Staging", l3s2.getLocType());
		Assert.assertEquals("Location 3s2 Length", 401, l3s2.getLength());
		l3s2.setTrainDirections(DIRECTION_ALL);
		l3s2.setRoadOption(Track.ALLROADS);
		l3s2.setDropOption(Track.ANY);
		l3s2.setPickupOption(Track.ANY);

		l3.addTrack("South End 1", Track.STAGING);
		l3.addTrack("South End 2", Track.STAGING);
		List<String> templist3 = l3.getTrackIdsByNameList("");
		for (int i = 0; i < templist3.size(); i++){
			if (i == 0) {
				Assert.assertEquals("RL 3 Staging 1 Name", "South End 1", templist3.get(i));
			}
			if (i == 1) {
				Assert.assertEquals("RL 3 Staging 2 Name", "South End 2", templist3.get(i));
			}
		}

		l3.register(l3s1);
		l3.register(l3s2);

		Assert.assertEquals("Location 3 Length", 701, l3.getLength());

		// Place 4 Boxcars on Staging tracks
		Assert.assertTrue("l1 Accepts Boxcar", l1.acceptsTypeName("Boxcar"));
		Assert.assertTrue("l1s1 Accepts Boxcar", l1s1.acceptsTypeName("Boxcar"));

		Assert.assertEquals("Location 1s1 Init Used Length", 0, l1s1.getUsedLength());
		Assert.assertEquals("Location 1s2 Init Used Length", 0, l1s2.getUsedLength());
		Assert.assertEquals("Location 1 Init Used Length", 0, l1s1.getUsedLength());
		Assert.assertEquals("Place c3", Track.OKAY, c3.setLocation(l1, l1s1));
		Assert.assertEquals("Location 1s1 c3 Used Length", 44, l1s1.getUsedLength());
		Assert.assertEquals("Location 1 c3 Used Length", 44, l1.getUsedLength());
		Assert.assertEquals("Place c4", Track.OKAY, c4.setLocation(l1, l1s1));
		Assert.assertEquals("Location 1s1 c4 Used Length", 88, l1s1.getUsedLength());
		Assert.assertEquals("Location 1 c4 Used Length", 88, l1.getUsedLength());

		Assert.assertEquals("Place c5", Track.OKAY, c5.setLocation(l1, l1s2));
		Assert.assertEquals("Location 1s2 c5 Used Length", 44, l1s2.getUsedLength());
		Assert.assertEquals("Location 1 c5 Used Length", 132, l1.getUsedLength());
		Assert.assertEquals("Place c6", Track.OKAY, c6.setLocation(l1, l1s2));
		Assert.assertEquals("Location 1s2 c6 Used Length", 88, l1s2.getUsedLength());
		Assert.assertEquals("Location 1 c6 Used Length", 176, l1.getUsedLength());
		
		// Place 2 Boxcars and Flat in yard
		Assert.assertEquals("Place c7", Track.OKAY, c7.setLocation(l2, l2s1));
		Assert.assertEquals("Location 2s1 c7 Used Length", 54, l2s1.getUsedLength());
		Assert.assertEquals("Location 2 c7 Used Length", 54, l2.getUsedLength());
		Assert.assertEquals("Place c8", Track.OKAY, c8.setLocation(l2, l2s1));
		Assert.assertEquals("Location 2s1 c4 Used Length", 118, l2s1.getUsedLength());
		Assert.assertEquals("Location 2 c4 Used Length", 118, l2.getUsedLength());
		Assert.assertEquals("Place c9", Track.OKAY, c9.setLocation(l2, l2s1));
		Assert.assertEquals("Location 2s1 c9 Used Length", 212, l2s1.getUsedLength());
		Assert.assertEquals("Location 2 c9 Used Length", 212, l2.getUsedLength());
	
		// Place Cabooses on Staging tracks
		Assert.assertEquals("Place c1", Track.OKAY, c1.setLocation(l1, l1s1));
		Assert.assertEquals("Location 1s1 c1 Used Length", 124, l1s1.getUsedLength());
		Assert.assertEquals("Location 1 c1 Used Length", 212, l1.getUsedLength());

		Assert.assertEquals("Place c2", Track.OKAY, c2.setLocation(l1, l1s2));
		Assert.assertEquals("Location 1s2 c2 Used Length", 124, l1s2.getUsedLength());
		Assert.assertEquals("Location 1 c2 Used Length", 248, l1.getUsedLength());

		// Define the route.
		Route r1 = new Route("1", "Southbound Main Route");
		Assert.assertEquals("Route Id", "1", r1.getId());
		Assert.assertEquals("Route Name", "Southbound Main Route", r1.getName());

		RouteLocation rl1 = new RouteLocation("1r1", l1);
		rl1.setSequenceId(1);
		rl1.setTrainDirection(RouteLocation.SOUTH);
		rl1.setMaxCarMoves(5);
		rl1.setMaxTrainLength(1000);		
		rl1.setTrainIconX(25);	// set the train icon coordinates
		rl1.setTrainIconY(25);

		Assert.assertEquals("Route Location 1 Id", "1r1", rl1.getId());
		Assert.assertEquals("Route Location 1 Name", "North End", rl1.getName());
		RouteLocation rl2 = new RouteLocation("1r2", l2);
		rl2.setSequenceId(2);
		rl2.setTrainDirection(RouteLocation.SOUTH);
		// test for only 1 pickup and 1 drop
		rl2.setMaxCarMoves(2);
		rl2.setMaxTrainLength(1000);
		rl2.setTrainIconX(75);	// set the train icon coordinates
		rl2.setTrainIconY(25);

		Assert.assertEquals("Route Location 2 Id", "1r2", rl2.getId());
		Assert.assertEquals("Route Location 2 Name", "North Industries", rl2.getName());
		RouteLocation rl3 = new RouteLocation("1r3", l3);
		rl3.setSequenceId(3);
		rl3.setTrainDirection(RouteLocation.SOUTH);
		rl3.setMaxCarMoves(5);
		rl3.setMaxTrainLength(1000);
		rl3.setTrainIconX(125);	// set the train icon coordinates
		rl3.setTrainIconY(25);

		Assert.assertEquals("Route Location 3 Id", "1r3", rl3.getId());
		Assert.assertEquals("Route Location 3 Name", "South End", rl3.getName());

		r1.register(rl1);
		r1.register(rl2);
		r1.register(rl3);

		rmanager.register(r1);

		// Finally ready to define the trains.
		Train train1 = new Train("1", "STF");
		Assert.assertEquals("Train Id", "1", train1.getId());
		Assert.assertEquals("Train Name", "STF", train1.getName());
		train1.setRequirements(Train.CABOOSE);
		train1.setCabooseRoad("CP");
		train1.deleteTypeName("Flat");
		train1.setRoadOption(Train.ALLROADS);
		train1.setRoute(r1);
		train1.setDepartureTime("6", "5");
		tmanager.register(train1);
		
		Train train2 = new Train("2", "SFF");
		Assert.assertEquals("Train Id", "2", train2.getId());
		Assert.assertEquals("Train Name", "SFF", train2.getName());
		// there are boxcars waiting in staging so build should fail
		train2.deleteTypeName("Boxcar");
		train2.deleteTypeName("Flat");
		train2.setRoute(r1);
		train2.setDepartureTime("22", "45");
		tmanager.register(train2);

		//  Last minute checks.
		Assert.assertEquals("Train 1 Departs Name", "North End", train1.getTrainDepartsName());
		Assert.assertEquals("Train 1 Route Departs Name", "North End", train1.getTrainDepartsRouteLocation().getName());
		Assert.assertEquals("Train 1 Terminates Name", "South End", train1.getTrainTerminatesName());
		Assert.assertEquals("Train 1 Route Terminates Name", "South End", train1.getTrainTerminatesRouteLocation().getName());
		Assert.assertEquals("Train 1 Next Location Name", "", train1.getNextLocationName());
		Assert.assertEquals("Train 1 Route Name", "Southbound Main Route", train1.getRoute().getName());

		Assert.assertEquals("Train 2 Departs Name", "North End", train2.getTrainDepartsName());
		Assert.assertEquals("Train 2 Route Departs Name", "North End", train2.getTrainDepartsRouteLocation().getName());
		Assert.assertEquals("Train 2 Terminates Name", "South End", train2.getTrainTerminatesName());
		Assert.assertEquals("Train 2 Route Terminates Name", "South End", train2.getTrainTerminatesRouteLocation().getName());
		Assert.assertEquals("Train 2 Next Location Name", "", train2.getNextLocationName());
		Assert.assertEquals("Train 2 Route Name", "Southbound Main Route", train2.getRoute().getName());

		// disable build messages
		tmanager.setBuildMessagesEnabled(false);
		// disable build reports
		tmanager.setBuildReportEnabled(false);
		
		// Try building without engines
		train1.build();
		train2.build();
		//Only train 1 should build
		Assert.assertEquals("Train 1 After 1st Build without engines", true, train1.isBuilt());
		Assert.assertEquals("Train 2 After 1st Build exclude Boxcar", false, train2.isBuilt());
		 
		// now allow train 2 to service Boxcars
		train2.addTypeName("Boxcar");
		// Try again, but exclude road name CP
		train2.setRoadOption(Train.EXCLUDEROADS);
		train2.addRoadName("CP");
		train2.build();
		Assert.assertEquals("Train 2 After Build but exclude road CP", false, train2.isBuilt());
		train2.setRoadOption(Train.ALLROADS);
		
		train2.build();
		Assert.assertEquals("Train 2 After Build include Boxcar", true, train2.isBuilt());
		
		// check train 1
		Assert.assertEquals("Car c1 After Build without engines should be assigned to Train 1", train1, c1.getTrain());
		Assert.assertEquals("Car c3 After Build without engines should be assigned to Train 1", train1, c3.getTrain());
		Assert.assertEquals("Car c4 After Build without engines should be assigned to Train 1", train1, c4.getTrain());
		Assert.assertEquals("Car c8 After Build without engines should be assigned to Train 1", train1, c8.getTrain());

		// car destinations correct?
		Assert.assertEquals("Car c1 After Build without engines destination", "South End", c1.getDestinationName());
		Assert.assertEquals("Car c3 After Build without engines destination", "North Industries", c3.getDestinationName());
		Assert.assertEquals("Car c4 After Build without engines destination", "South End", c4.getDestinationName());
		Assert.assertEquals("Car c8 After Build without engines destination", "South End", c8.getDestinationName());
		
		// car destination track correct?
		Assert.assertEquals("Car c1 After without engines Build track", "South End 1", c1.getDestinationTrackName());
		Assert.assertEquals("Car c3 After without engines Build track", "NI Yard", c3.getDestinationTrackName());
		Assert.assertEquals("Car c4 After without engines Build track", "South End 1", c4.getDestinationTrackName());
		Assert.assertEquals("Car c8 After without engines Build track", "South End 1", c8.getDestinationTrackName());

		// Try again building without engines on staging tracks but require them
		train1.setEngineRoad("PC");
		train1.setEngineModel("GP40");
		train1.setNumberEngines("2");
		train2.setNumberEngines("2");

		train1.build();
		train2.build();
		Assert.assertEquals("Train 1 After 2nd Build without engines", false, train1.isBuilt());
		Assert.assertEquals("Train 2 After 2nd Build without engines", false, train2.isBuilt());
		
		// Place Engines on Staging tracks
		Assert.assertEquals("Place e1", Track.OKAY, e1.setLocation(l1, l1s1));
		Assert.assertEquals("Location 1s1 e1 Used Length", 187, l1s1.getUsedLength());
		Assert.assertEquals("Location 1 e1 Used Length", 311, l1.getUsedLength());
		Assert.assertEquals("Place e2", Track.OKAY, e2.setLocation(l1, l1s1));
		Assert.assertEquals("Location 1s1 e2 Used Length", 250, l1s1.getUsedLength());
		Assert.assertEquals("Location 1 e2 Used Length", 374, l1.getUsedLength());
		
		Assert.assertEquals("Place e3", Track.OKAY, e3.setLocation(l1, l1s2));
		Assert.assertEquals("Location 1s2 e3 Used Length", 194, l1s2.getUsedLength());
		Assert.assertEquals("Location 1 e3 Used Length", 444, l1.getUsedLength());
		Assert.assertEquals("Place e4", Track.OKAY, e4.setLocation(l1, l1s2));
		Assert.assertEquals("Location 1s2 e4 Used Length", 264, l1s2.getUsedLength());
		Assert.assertEquals("Location 1 e4 Used Length", 514, l1.getUsedLength());
		
		// Build the trains with engines
		train1.build();
		train2.build();
		// Both should build
		Assert.assertEquals("Train 1 After Build with engines", true, train1.isBuilt());
		Assert.assertEquals("Train 2 After Build with engines", true, train2.isBuilt());

		// Check train 1
		Assert.assertEquals("Train 1 After Build Departs Name", "North End", train1.getTrainDepartsName());
		Assert.assertEquals("Train 1 After Build Terminates Name", "South End", train1.getTrainTerminatesName());
		Assert.assertEquals("Train 1 After Build Next Location Name", "North Industries", train1.getNextLocationName());
		Assert.assertEquals("Train 1 After Build Built Status", true, train1.isBuilt());
		
		// Are the proper engines and cars assigned to train 1?
		Assert.assertEquals("Engine e1 After Build should be assigned to Train 1", train1, e1.getTrain());
		Assert.assertEquals("Engine e2 After Build should be assigned to Train 1", train1, e2.getTrain());
		Assert.assertEquals("Car c1 After Build should be assigned to Train 1", train1, c1.getTrain());
		Assert.assertEquals("Car c3 After Build should be assigned to Train 1", train1, c3.getTrain());
		Assert.assertEquals("Car c4 After Build should be assigned to Train 1", train1, c4.getTrain());
		Assert.assertEquals("Car c8 After Build should be assigned to Train 1", train1, c8.getTrain());
		
		// Are the proper engines and cars assigned to train 2?
		Assert.assertEquals("Engine e3 After Build should be assigned to Train 2", train2, e3.getTrain());
		Assert.assertEquals("Engine e4 After Build should be assigned to Train 2", train2, e4.getTrain());
		Assert.assertEquals("Car c2 After Build should be assigned to Train 2", train2, c2.getTrain());
		Assert.assertEquals("Car c5 After Build should be assigned to Train 2", train2, c5.getTrain());
		Assert.assertEquals("Car c7 After Build should be not be assigned", null, c7.getTrain());
		Assert.assertEquals("Car c9 After Build should be not be assigned", null, c9.getTrain());
		
		// Are the engine and car destinations correct?
		Assert.assertEquals("Engine e1 After Build destination", "South End", e1.getDestinationName());
		Assert.assertEquals("Engine e2 After Build destination", "South End", e2.getDestinationName());
		Assert.assertEquals("Car c1 After Build destination", "South End", c1.getDestinationName());
		Assert.assertEquals("Car c3 After Build destination", "North Industries", c3.getDestinationName());
		Assert.assertEquals("Car c4 After Build destination", "South End", c4.getDestinationName());
		Assert.assertEquals("Car c8 After Build destination", "South End", c8.getDestinationName());
		
		// Are the engine and car destination track correct?
		Assert.assertEquals("Engine e1 After Build track", "South End 2", e1.getDestinationTrackName());
		Assert.assertEquals("Engine e2 After Build track", "South End 2", e2.getDestinationTrackName());
		Assert.assertEquals("Car c1 After Build track", "South End 2", c1.getDestinationTrackName());
		Assert.assertEquals("Car c3 After Build track", "NI Yard", c3.getDestinationTrackName());
		Assert.assertEquals("Car c4 After Build track", "South End 2", c4.getDestinationTrackName());
		Assert.assertEquals("Car c8 After Build track", "South End 2", c8.getDestinationTrackName());
	
		// Are the location pickup and drop counts correct?
		Assert.assertEquals("Drop count for North End", 0, l1.getDropRS());  
		// Each train has one drop at North Industries
		Assert.assertEquals("Drop count for North Industries", 2, l2.getDropRS()); 
		// Train1 has 5 drops and Train2 has 4 drops for South End
		Assert.assertEquals("Drop count for South End", 9, l3.getDropRS()); 
		// Each train has 5 pickups
		Assert.assertEquals("Pickup count for North End", 10, l1.getPickupRS());  
		Assert.assertEquals("Pickup count for North Industries", 1, l2.getPickupRS()); 
		Assert.assertEquals("Pickup count for South End", 0, l3.getPickupRS()); 
		
		// Are the track pickup and drop counts correct?
		Assert.assertEquals("Drop count for North End, track North End 1", 0, l1s1.getDropRS()); 
		Assert.assertEquals("Drop count for North End, track North End 2", 0, l1s2.getDropRS()); 
		Assert.assertEquals("Pickup count for North End, track North End 1", 5, l1s1.getPickupRS()); 
		Assert.assertEquals("Pickup count for North End, track North End 2", 5, l1s2.getPickupRS()); 
		// Each train has one drop at NI Yard
		Assert.assertEquals("Drop count for North Industries, track NI Yard", 2, l2s1.getDropRS()); 		
		Assert.assertEquals("Pickup count for North Industries, track NI Yard", 1, l2s1.getPickupRS()); 
		Assert.assertEquals("Drop count for South End, track South End 1", 4, l3s1.getDropRS()); 
		Assert.assertEquals("Drop count for South End, track South End 2", 5, l3s2.getDropRS()); 
		Assert.assertEquals("Pickup count for South End, track South End 1", 0, l3s1.getPickupRS()); 
		Assert.assertEquals("Pickup count for South End, track South End 2", 0, l3s2.getPickupRS()); 

		// Are the other engines and cars assigned correctly?
		Assert.assertEquals("Engine e3 After Build should NOT be assigned to Train 1", train2, e3.getTrain());
		Assert.assertEquals("Engine e4 After Build should NOT be assigned to Train 1", train2, e4.getTrain());
		Assert.assertEquals("Car c2 After Build should NOT be assigned to Train 1", train2, c2.getTrain());
		Assert.assertEquals("Car c5 After Build should NOT be assigned to Train 1", train2, c5.getTrain());
		Assert.assertEquals("Car c6 After Build should NOT be assigned to Train 1", train2, c6.getTrain());
		Assert.assertEquals("Car c7 After Build should NOT be assigned to Train 1", null, c7.getTrain());
		Assert.assertEquals("Car c9 After Build should NOT be assigned to Train 1", null, c9.getTrain());
		
		// Check expected arrival times
		Assert.assertEquals("Train 1 expected departure time", "06:05", train1.getExpectedArrivalTime(rl1));
		// Check time for car moves and train travel times
		Assert.assertEquals("Per Car move time", 11, Setup.getSwitchTime());
		Assert.assertEquals("Train travel time", 111, Setup.getTravelTime());
		
		Assert.assertEquals("Train 1 expected North End", "07:56", train1.getExpectedArrivalTime(rl2));
		// one car dropped and one is picked up at North End, so travel time + two car moves
		Assert.assertEquals("Train 1 expected North Industries", "10:09", train1.getExpectedArrivalTime(rl3));

		// Reset the train!
		Assert.assertEquals("Train 1 Reset should be true", true, train1.reset());
		Assert.assertEquals("Train 1 After Reset Departs Name", "North End", train1.getTrainDepartsName());
		Assert.assertEquals("Train 1 After Reset Terminates Name", "South End", train1.getTrainTerminatesName());
		Assert.assertEquals("Train 1 After Reset Next Location Name", "", train1.getNextLocationName());
		Assert.assertEquals("Train 1 After Reset Built Status", false, train1.isBuilt());
		
		// Are the engines and cars released from train 1?
		Assert.assertEquals("Engine e1 After Reset should NOT be assigned to Train 1", null, e1.getTrain());
		Assert.assertEquals("Engine e2 After Reset should NOT be assigned to Train 1", null, e2.getTrain());
		Assert.assertEquals("Car c1 After Reset should NOT be assigned to Train 1", null, c1.getTrain());
		Assert.assertEquals("Car c3 After Reset should NOT be assigned to Train 1", null, c3.getTrain());
		Assert.assertEquals("Car c4 After Reset should NOT be assigned to Train 1", null, c4.getTrain());
		Assert.assertEquals("Car c8 After Reset should NOT be assigned to Train 1", null, c8.getTrain());
		
		// Are the location pickup and drop counts correct?
		Assert.assertEquals("Reset Drop count for North End", 0, l1.getDropRS());  
		Assert.assertEquals("Reset Drop count for North Industries", 1, l2.getDropRS()); 
		Assert.assertEquals("Reset Drop count for South End", 4, l3.getDropRS()); 
		Assert.assertEquals("Reset Pickup count for North End", 5, l1.getPickupRS());  
		Assert.assertEquals("Reset Pickup count for North Industries", 0, l2.getPickupRS()); 
		Assert.assertEquals("Reset Pickup count for South End", 0, l3.getPickupRS()); 

		// Are the track pickup and drop counts correct?
		Assert.assertEquals("Reset Drop count for North End, track North End 1", 0, l1s1.getDropRS()); 
		Assert.assertEquals("Reset Drop count for North End, track North End 2", 0, l1s2.getDropRS()); 
		Assert.assertEquals("Reset Pickup count for North End, track North End 1", 0, l1s1.getPickupRS()); 
		Assert.assertEquals("Reset Pickup count for North End, track North End 2", 5, l1s2.getPickupRS()); 
		Assert.assertEquals("Reset Drop count for North Industries, track NI Yard", 1, l2s1.getDropRS()); 
		Assert.assertEquals("Reset Pickup count for North Industries, track NI Yard", 1, l2s1.getDropRS()); 
		Assert.assertEquals("Reset Drop count for South End, track South End 1", 4, l3s1.getDropRS()); 
		Assert.assertEquals("Reset Drop count for South End, track South End 2", 0, l3s2.getDropRS()); 
		Assert.assertEquals("Reset Pickup count for South End, track South End 1", 0, l3s1.getPickupRS()); 
		Assert.assertEquals("Reset Pickup count for South End, track South End 2", 0, l3s2.getPickupRS()); 

		// Try again, but exclude caboose
		// there are cabooses waiting in staging so build should fail
		train1.deleteTypeName("Caboose");
		train1.build();
		Assert.assertEquals("Train 1 After Build with engines but exclude Caboose", false, train1.isBuilt());
		train1.addTypeName("Caboose");
		
		// Try again, but exclude road name CP
		train1.setRoadOption(Train.EXCLUDEROADS);
		train1.addRoadName("CP");
		train1.build();
		Assert.assertEquals("Train 1 After Build with engines but exclude road CP", false, train1.isBuilt());
		train1.setRoadOption(Train.ALLROADS);
		
		// try again, but set the caboose destination to NI yard
		c1.setDestination(l2, null);
		train1.build();
		Assert.assertEquals("Train 1 build should fail, caboose destination isn't terminal", false, train1.isBuilt());
		
		// send caboose to last location which is staging
		c1.setDestination(l3, null);
		train1.build();
		Assert.assertEquals("Train 1 build, caboose destination is terminal", true, train1.isBuilt());
		
		// don't allow cabooses road
		l3.deleteTypeName("Caboose");
		train1.build();
		Assert.assertEquals("Train 1 build, caboose destination is terminal", false, train1.isBuilt());
		l3.addTypeName("Caboose");
		
		// Try again, but only allow rolling stock built before 1985
		train2.reset();
		train1.setBuiltEndYear("1985");
		train1.build();
		// should fail, required engines have built dates after 1985
		Assert.assertEquals("Train 1 After Build with rs built before 1985", false, train1.isBuilt());
		// change the engine built date
		e1.setBuilt("7-84");
		e2.setBuilt("1984");
		train1.build();
		Assert.assertEquals("Train 1 After 2nd Build with rs built before 1985", true, train1.isBuilt());
		// change one of the car's built date to after 1985
		c4.setBuilt("1-85");
		train1.build();
		// should fail
		Assert.assertEquals("Train 1 After 3rd Build with rs built before 1985", false, train1.isBuilt());
		train1.setBuiltEndYear("");
		train1.build();
		Assert.assertEquals("Train 1 After 4th Build with rs built before 1985", true, train1.isBuilt());
		
		// Try again, but now exclude engine type Diesel
		train1.deleteTypeName("Diesel");
		train1.build();
		Assert.assertEquals("Train 1 After 1st Build type Diesel not serviced", false, train1.isBuilt());
		train1.addTypeName("Diesel");
		train1.build();
		Assert.assertEquals("Train 1 After 2nd Build type Diesel serviced", true, train1.isBuilt());
		
		// Try again, but now restrict owner 
		train1.addOwnerName("DAB");
		train1.setOwnerOption(Train.INCLUDEOWNERS);
		train1.build();
		Assert.assertEquals("Train 1 After 1st Build owner DAB", false, train1.isBuilt());
		train1.addOwnerName("AT");
		train1.build();
		Assert.assertEquals("Train 1 After 2nd Build owners DAB and AT", true, train1.isBuilt());
		train1.setOwnerOption(Train.EXCLUDEOWNERS);
		train1.build();
		Assert.assertEquals("Train 1 After 3rd Build exclude owners DAB and AT", false, train1.isBuilt());
		train1.deleteOwnerName("AT");
		train1.build();
		Assert.assertEquals("Train 1 After 4th Build exclude owner DAB", false, train1.isBuilt());
		train1.setOwnerOption(Train.ALLOWNERS);
		train1.build();
		Assert.assertEquals("Train 1 After 5th Build all owners", true, train1.isBuilt());
		
		// Try again, but now restrict load
		train1.addLoadName("L");
		train1.setLoadOption(Train.INCLUDELOADS);
		train1.build();
		// build should fail, cars in staging have E loads
		Assert.assertEquals("Train 1 After include load L", false, train1.isBuilt());
		
		train1.deleteLoadName("L");
		c8.setLoad("L");			// this car shouldn't be picked up.
		train1.addLoadName("E");
		train1.build();
		Assert.assertEquals("Train 1 After include load E", true, train1.isBuilt());
		
		Assert.assertEquals("car C10099 in staging should be assigned to train", train1, c1.getTrain());
		Assert.assertEquals("car X1001 in staging should be assigned to train", train1, c3.getTrain());
		Assert.assertEquals("car X1002 in staging should be assigned to train", train1, c4.getTrain());
		
		Assert.assertEquals("car 888 at siding has load L, excluded", null, c8.getTrain());
		
		train1.addLoadName("L");
		train1.build();
		Assert.assertEquals("Train 1 After include load L", true, train1.isBuilt());
		
		Assert.assertEquals("car C10099 in staging should be assigned to train", train1, c1.getTrain());
		Assert.assertEquals("car X1001 in staging should be assigned to train", train1, c3.getTrain());
		Assert.assertEquals("car X1002 in staging should be assigned to train", train1, c4.getTrain());
		
		Assert.assertEquals("car 888 at siding has load L, now included", train1, c8.getTrain());
		
		train1.setLoadOption(Train.EXCLUDELOADS);
		// cars in staging have E loads, so build should fail
		train1.build();
		Assert.assertEquals("Train 1 After exclude loads", false, train1.isBuilt());
		
		// allow train to carry E loads
		train1.deleteLoadName("E");
		train1.build();
		Assert.assertEquals("Train 1 After exclude loads L", true, train1.isBuilt());
		
		Assert.assertEquals("car C10099 in staging should be assigned to train", train1, c1.getTrain());
		Assert.assertEquals("car X1001 in staging should be assigned to train", train1, c3.getTrain());
		Assert.assertEquals("car X1002 in staging should be assigned to train", train1, c4.getTrain());
		
		Assert.assertEquals("car 888 at siding has load L, now excluded", null, c8.getTrain());
		
		//done
		train1.setLoadOption(Train.ALLLOADS);
		
		// Try again, but now set staging track service direction NORTH, train departs to the south
		l1s1.setTrainDirections(Location.NORTH);
		l1s2.setTrainDirections(Location.NORTH);
		train1.build();
		Assert.assertEquals("Train 1 After 1st Build staging set to North", false, train1.isBuilt());
		
		l1s1.setTrainDirections(Location.SOUTH);
		l1s2.setTrainDirections(Location.SOUTH);
		train1.build();
		Assert.assertEquals("Train 1 After 2nd Build staging set to South", true, train1.isBuilt());
		
		// need to reset train to release cars
		train1.reset();
		// Try again, but now have a car with a destination not serviced by train
		Location nowhere = lmanager.newLocation("nowhere");
		c4.setDestination(nowhere, null);
		train1.build();
		Assert.assertEquals("Train 1 After Build car to nowhere", false, train1.isBuilt());
		c4.setDestination(null, null);
		
		// Build the trains again!!
		train1.build();
		train2.build();
		Assert.assertEquals("Train 1 After Build with engines include all roads", true, train1.isBuilt());
		Assert.assertEquals("Train 1 After Build Departs Name", "North End", train1.getTrainDepartsName());
		Assert.assertEquals("Train 1 After Build Terminates Name", "South End", train1.getTrainTerminatesName());
		Assert.assertEquals("Train 1 After Build Next Location Name", "North Industries", train1.getNextLocationName());

		//  Move the train #1
		train1.move();
		Assert.assertEquals("Train 1 After 1st Move Current Name", "North Industries", train1.getCurrentLocationName());
		Assert.assertEquals("Train 1 After 1st Move Next Location Name", "South End", train1.getNextLocationName());
		
		// Is the train in route?
		Assert.assertEquals("Train 1 in route after 1st", true, train1.isTrainInRoute());
		
		// Try and reset the train
		Assert.assertEquals("Train 1 Reset should be false", false, train1.reset());

		// Are the engine and car locations correct?
		Assert.assertEquals("Engine e1 After After 1st Move", "North Industries", e1.getLocationName());
		Assert.assertEquals("Engine e2 After After 1st Move", "North Industries", e2.getLocationName());
		Assert.assertEquals("Car c1 After After 1st Move", "North Industries", c1.getLocationName());
		Assert.assertEquals("Car c3 After After 1st Move", "North Industries", c3.getLocationName());
		Assert.assertEquals("Car c4 After After 1st Move", "North Industries", c4.getLocationName());
		Assert.assertEquals("Car c8 After After 1st Move", "North Industries", c8.getLocationName());

		// Are the location pickup and drop counts correct?
		Assert.assertEquals("Move 1 Drop count for North End", 0, l1.getDropRS());  
		Assert.assertEquals("Move 1 Drop count for North Industries", 2, l2.getDropRS()); 
		Assert.assertEquals("Move 1 Drop count for South End", 9, l3.getDropRS()); 
		Assert.assertEquals("Move 1 Pickup count for North End", 5, l1.getPickupRS());  
		Assert.assertEquals("Move 1 Pickup count for North Industries", 1, l2.getPickupRS()); 
		Assert.assertEquals("Move 1 Pickup count for South End", 0, l3.getPickupRS()); 
		
		// Are the track pickup and drop counts correct?
		Assert.assertEquals("Move 1 Drop count for North End, track North End 1", 0, l1s1.getDropRS()); 
		Assert.assertEquals("Move 1 Drop count for North End, track North End 2", 0, l1s2.getDropRS()); 
		Assert.assertEquals("Move 1 Pickup count for North End, track North End 1", 0, l1s1.getPickupRS()); 
		Assert.assertEquals("Move 1 Pickup count for North End, track North End 2", 5, l1s2.getPickupRS()); 
		Assert.assertEquals("Move 1 Drop count for North Industries, track NI Yard", 2, l2s1.getDropRS()); 
		Assert.assertEquals("Move 1 Pickup count for North Industries, track NI Yard", 1, l2s1.getPickupRS()); 
		Assert.assertEquals("Move 1 Drop count for South End, track South End 1", 4, l3s1.getDropRS()); 
		Assert.assertEquals("Move 1 Drop count for South End, track South End 2", 5, l3s2.getDropRS()); 
		Assert.assertEquals("Move 1 Pickup count for South End, track South End 1", 0, l3s1.getPickupRS()); 
		Assert.assertEquals("Move 1 Pickup count for South End, track South End 2", 0, l3s2.getPickupRS()); 

		//  Move the train #2
		train1.move();
		Assert.assertEquals("Train 1 After 2nd Move Current Name", "South End", train1.getCurrentLocationName());
		Assert.assertEquals("Train 1 After 2nd Move Next Location Name", "", train1.getNextLocationName());
		// Is the train in route?
		Assert.assertEquals("Train 1 in route after 2nd", true, train1.isTrainInRoute());

		// Are the engine and car locations correct?
		Assert.assertEquals("Engine e1 After After 2nd Move", "South End", e1.getLocationName());
		Assert.assertEquals("Engine e2 After After 2nd Move", "South End", e2.getLocationName());
		Assert.assertEquals("Car c1 After After 2nd Move", "South End", c1.getLocationName());
		Assert.assertEquals("Car c3 After After 2nd Move", "North Industries", c3.getLocationName());
		Assert.assertEquals("Car c4 After After 2nd Move", "South End", c4.getLocationName());
		Assert.assertEquals("Car c8 After After 2nd Move", "South End", c8.getLocationName());
		
		// was c3 released from train?
		Assert.assertEquals("Car c3 After drop should NOT be assigned to Train 1", null, c3.getTrain());
		Assert.assertEquals("Car c3 destination After 2nd Move", "", c3.getDestinationTrackName());
		Assert.assertEquals("Car c3 After 2nd Move location", "North Industries", c3.getLocationName());
		Assert.assertEquals("Car c3 After 2nd Move", "NI Yard", c3.getTrackName());
		Assert.assertEquals("Car c3 Moves after drop should be 13", 13, c3.getMoves());
		
		// Are the location pickup and drop counts correct?
		Assert.assertEquals("Move 2 Drop count for North End", 0, l1.getDropRS());  
		Assert.assertEquals("Move 2 Drop count for North Industries", 1, l2.getDropRS()); 
		Assert.assertEquals("Move 2 Drop count for South End", 9, l3.getDropRS()); 
		Assert.assertEquals("Move 2 Pickup count for North End", 5, l1.getPickupRS());  
		Assert.assertEquals("Move 2 Pickup count for North Industries", 0, l2.getPickupRS()); 
		Assert.assertEquals("Move 2 Pickup count for South End", 0, l3.getPickupRS()); 
		
		// Are the track pickup and drop counts correct?
		Assert.assertEquals("Move 2 Drop count for North End, track North End 1", 0, l1s1.getDropRS()); 
		Assert.assertEquals("Move 2 Drop count for North End, track North End 2", 0, l1s2.getDropRS()); 
		Assert.assertEquals("Move 2 Pickup count for North End, track North End 1", 0, l1s1.getPickupRS()); 
		Assert.assertEquals("Move 2 Pickup count for North End, track North End 2", 5, l1s2.getPickupRS()); 
		Assert.assertEquals("Move 2 Drop count for North Industries, track NI Yard", 1, l2s1.getDropRS()); 
		Assert.assertEquals("Move 2 Pickup count for North Industries, track NI Yard", 0, l2s1.getPickupRS()); 
		Assert.assertEquals("Move 2 Drop count for South End, track South End 1", 4, l3s1.getDropRS()); 
		Assert.assertEquals("Move 2 Drop count for South End, track South End 2", 5, l3s2.getDropRS()); 
		Assert.assertEquals("Move 2 Pickup count for South End, track South End 1", 0, l3s1.getPickupRS()); 
		Assert.assertEquals("Move 2 Pickup count for South End, track South End 2", 0, l3s2.getPickupRS()); 

		//  Move the train #3 (Terminate)
		train1.move();
		Assert.assertEquals("Train 1 After 3rd Move Current Name", "", train1.getCurrentLocationName());
		Assert.assertEquals("Train 1 After 3rd Move Next Location Name", "", train1.getNextLocationName());
		Assert.assertEquals("Train 1 After 3rd Move Status", Train.TERMINATED, getTrainStatus(train1));
		// Is the train in route?
		Assert.assertEquals("Train 1 in route after 3rd", false, train1.isTrainInRoute());

		// Are the engine and car destinations correct?
		Assert.assertEquals("Engine e1 After 3rd Move", "", e1.getDestinationTrackName());
		Assert.assertEquals("Engine e2 After 3rd Move", "", e2.getDestinationTrackName());
		Assert.assertEquals("Car c1 After 3rd Move", "", c1.getDestinationTrackName());
		Assert.assertEquals("Car c4 After 3rd Move", "", c4.getDestinationTrackName());
		Assert.assertEquals("Car c8 After 3rd Move", "", c8.getDestinationTrackName());

		// Are the engine and car final locations correct?
		Assert.assertEquals("Engine e1 After Terminate location", "South End", e1.getLocationName());
		Assert.assertEquals("Engine e2 After Terminate location", "South End", e2.getLocationName());
		Assert.assertEquals("Car c1 After Terminate location", "South End", c1.getLocationName());
		Assert.assertEquals("Car c4 After Terminate location", "South End", c4.getLocationName());
		Assert.assertEquals("Car c8 After Terminate location", "South End", c8.getLocationName());
	
		// Are the engine and car final staging track correct?
		Assert.assertEquals("Engine e1 After Terminate track", "South End 2", e1.getTrackName());
		Assert.assertEquals("Engine e2 After Terminate track", "South End 2", e2.getTrackName());
		Assert.assertEquals("Car c1 After Terminate track", "South End 2", c1.getTrackName());
		Assert.assertEquals("Car c4 After Terminate track", "South End 2", c4.getTrackName());
		Assert.assertEquals("Car c8 After Terminate track", "South End 2", c8.getTrackName());
		
		// Are the engine and car moves correct
		Assert.assertEquals("Engine e1 Moves after Terminate should be 136", 136, e1.getMoves());
		Assert.assertEquals("Engine e2 Moves after Terminate should be 334", 334, e2.getMoves());
		Assert.assertEquals("Car c1 Moves after Terminate should be 38", 36, c1.getMoves());
		Assert.assertEquals("Car c4 Moves after Terminate should be 4457", 4457, c4.getMoves());
		Assert.assertEquals("Car c8 Moves after Terminate should be 10", 10, c8.getMoves());

		// Are the location pickup and drop counts correct?
		Assert.assertEquals("Move 3 Drop count for North End", 0, l1.getDropRS());  
		Assert.assertEquals("Move 3 Drop count for North Industries", 1, l2.getDropRS()); 
		Assert.assertEquals("Move 3 Drop count for South End", 4, l3.getDropRS()); 
		Assert.assertEquals("Move 3 Pickup count for North End", 5, l1.getPickupRS());  
		Assert.assertEquals("Move 3 Pickup count for North Industries", 0, l2.getPickupRS()); 
		Assert.assertEquals("Move 3 Pickup count for South End", 0, l3.getPickupRS()); 
		
		// Are the track pickup and drop counts correct?
		Assert.assertEquals("Move 3 Drop count for North End, track North End 1", 0, l1s1.getDropRS()); 
		Assert.assertEquals("Move 3 Drop count for North End, track North End 2", 0, l1s2.getDropRS()); 
		Assert.assertEquals("Move 3 Pickup count for North End, track North End 1", 0, l1s1.getPickupRS()); 
		Assert.assertEquals("Move 3 Pickup count for North End, track North End 2", 5, l1s2.getPickupRS()); 
		Assert.assertEquals("Move 3 Drop count for North Industries, track NI Yard", 1, l2s1.getDropRS()); 
		Assert.assertEquals("Move 3 Pickup count for North Industries, track NI Yard", 0, l2s1.getPickupRS()); 
		Assert.assertEquals("Move 3 Drop count for South End, track South End 1", 4, l3s1.getDropRS()); 
		Assert.assertEquals("Move 3 Drop count for South End, track South End 2", 0, l3s2.getDropRS()); 
		Assert.assertEquals("Move 3 Pickup count for South End, track South End 1", 0, l3s1.getPickupRS()); 
		Assert.assertEquals("Move 3 Pickup count for South End, track South End 2", 0, l3s2.getPickupRS()); 

		//  Move the train 1 for the forth time, this shouldn't change anything
		train1.move();
		Assert.assertEquals("Train 1 After 4th Move Current Name", "", train1.getCurrentLocationName());
		Assert.assertEquals("Train 1 After 4th Move Next Location Name", "", train1.getNextLocationName());
		Assert.assertEquals("Train 1 After 4th Move Status", Train.TERMINATED, getTrainStatus(train1));		
		// Is the train in route?
		Assert.assertEquals("Train 1 sould not be in route", false, train1.isTrainInRoute());
		
		// Are the engines and cars released from train 1?
		Assert.assertEquals("Engine e1 After Terminate should NOT be assigned to Train 1", null, e1.getTrain());
		Assert.assertEquals("Engine e2 After Terminate should NOT be assigned to Train 1", null, e2.getTrain());
		Assert.assertEquals("Car c1 After Terminate should NOT be assigned to Train 1", null, c1.getTrain());
		Assert.assertEquals("Car c4 After Terminate should NOT be assigned to Train 1", null, c4.getTrain());
	
		// do cars have the right loads?
		Assert.assertEquals("Car c1 load after Terminate", "E", c1.getLoad());
		Assert.assertEquals("Car c2 load after Terminate", "E", c2.getLoad());
		Assert.assertEquals("Car c3 load after Terminate", "E", c3.getLoad());
		Assert.assertEquals("Car c4 load after Terminate", "E", c4.getLoad());
		Assert.assertEquals("Car c5 load after Terminate", "E", c5.getLoad());
		Assert.assertEquals("Car c6 load after Terminate", "E", c6.getLoad());
		Assert.assertEquals("Car c7 load after Terminate", "E", c7.getLoad());
		Assert.assertEquals("Car c8 load after Terminate", "L", c8.getLoad());
		Assert.assertEquals("Car c9 load after Terminate", "E", c9.getLoad());	
		
		// reset train 2
		Assert.assertTrue("reset train2",train2.reset());
		
		// Are the location pickup and drop counts correct?
		Assert.assertEquals("Terminated Drop count for North End", 0, l1.getDropRS());  
		Assert.assertEquals("Terminated Drop count for North Industries", 0, l2.getDropRS()); 
		Assert.assertEquals("Terminated Drop count for South End", 0, l3.getDropRS()); 
		Assert.assertEquals("Terminated Pickup count for North End", 0, l1.getPickupRS());  
		Assert.assertEquals("Terminated Pickup count for North Industries", 0, l2.getPickupRS()); 
		Assert.assertEquals("Terminated Pickup count for South End", 0, l3.getPickupRS()); 

		// Are the track pickup and drop counts correct?
		Assert.assertEquals("Terminated Drop count for North End, track North End 1", 0, l1s1.getDropRS()); 
		Assert.assertEquals("Terminated Drop count for North End, track North End 2", 0, l1s2.getDropRS()); 
		Assert.assertEquals("Terminated Pickup count for North End, track North End 1", 0, l1s1.getPickupRS()); 
		Assert.assertEquals("Terminated Pickup count for North End, track North End 2", 0, l1s2.getPickupRS()); 
		Assert.assertEquals("Terminated Drop count for North Industries, track NI Yard", 0, l2s1.getDropRS()); 
		Assert.assertEquals("Terminated Pickup count for North Industries, track NI Yard", 0, l2s1.getDropRS()); 
		Assert.assertEquals("Terminated Drop count for South End, track South End 1", 0, l3s1.getDropRS()); 
		Assert.assertEquals("Terminated Drop count for South End, track South End 2", 0, l3s2.getDropRS()); 
		Assert.assertEquals("Terminated Pickup count for South End, track South End 1", 0, l3s1.getPickupRS()); 
		Assert.assertEquals("Terminated Pickup count for South End, track South End 2", 0, l3s2.getPickupRS()); 

		
		// this should fail, there are two engines in staging
		train2.setNumberEngines("1");
		// now build train 2 testing failure modes
		train2.build();
		// build required 1 engine and there were two
		Assert.assertEquals("Train 2 After Build require 1 engine", false, train2.isBuilt());
		// take one engine out of the consist
		e4.setConsist(null);
		train2.build();
		Assert.assertEquals("Train 2 After Build require 1 engine, but 2 engines on staging track", false, train2.isBuilt());
		e4.setLocation(null, null);	// remove engine from staging
		train2.build();
		Assert.assertEquals("Train 2 After Build require 1 engine, 1 consisted engine on staging track", true, train2.isBuilt());
		// take lead engine out of consist
		e3.setConsist(null);
		train2.build();
		Assert.assertEquals("Train 2 After Build require 1 engine, single engine on staging track", true, train2.isBuilt());
		// restore engines and place back on staging tracks
		e3.setConsist(con2);
		e4.setConsist(con2);
		e3.setLocation(l1, l1s2);
		e4.setLocation(l1, l1s2);
		
		// should work for 0
		train2.setNumberEngines("0");
		train2.build();
		Assert.assertEquals("Train 2 After Build require 0 engine", true, train2.isBuilt());
		train2.setNumberEngines("3");
		train2.build();
		Assert.assertEquals("Train 2 After Build require 3 engines", false, train2.isBuilt());
		train2.setNumberEngines("2");
		train2.build();
		Assert.assertEquals("Train 2 After Build require 2 engine", true, train2.isBuilt());
		// take engine out of service
		e3.setOutOfService(true);
		train2.build();
		Assert.assertEquals("Train 2 After Build engine out of service", false, train2.isBuilt());
		// put back into service
		e3.setOutOfService(false);
		train2.build();
		Assert.assertEquals("Train 2 After Build engine in service", true, train2.isBuilt());
		// try different road
		train2.setEngineRoad("CP");
		train2.build();
		Assert.assertEquals("Train 2 After Build require road CP", false, train2.isBuilt());
		train2.setEngineRoad("PC");
		// try requiring FRED, should fail
		train2.setRequirements(Train.FRED);
		train2.build();
		Assert.assertEquals("Train 2 After Build requires FRED", false, train2.isBuilt());
		// Add FRED to boxcar
		c5.setFred(true);
		train2.build();
		Assert.assertEquals("Train 2 After Build 2 requires FRED", true, train2.isBuilt());
		// try engine wrong model
		train2.setEngineModel("DS45");
		train2.build();
		Assert.assertEquals("Train 2 After Build 2 requires model DS45", false, train2.isBuilt());
		// try engine correct model
		train2.setEngineModel("SD45");
		train2.build();
		Assert.assertEquals("Train 2 After Build 2 requires model SD45", true, train2.isBuilt());
	
		// Are the engines and cars assigned to train 2?
		Assert.assertEquals("Engine e3 After Build should be assigned to Train 2", train2, e3.getTrain());
		Assert.assertEquals("Engine e4 After Build should be assigned to Train 2", train2, e4.getTrain());
		Assert.assertEquals("Car c2 After Build should be assigned to Train 2", train2, c2.getTrain());
		Assert.assertEquals("Car c3 After Build should be assigned to Train 2", train2, c3.getTrain());
		Assert.assertEquals("Car c5 After Build should be assigned to Train 2", train2, c5.getTrain());
		Assert.assertEquals("Car c6 After Build should be assigned to Train 2", train2, c6.getTrain());
		// train 2 does not accept Flat
		Assert.assertEquals("Car c7 After Build should NOT be assigned to Train 2", null, c7.getTrain());
		Assert.assertEquals("Car c9 After Build should NOT be assigned to Train 2", null, c9.getTrain());

		// now allow Flat
		train2.addTypeName("Flat");
		c9.setLength("200");
		train2.build();
		// c9 and c7 have less moves than c3, but there's not enough room for c9 at destination
		Assert.assertEquals("Car c3 After Build 3 should be assigned to Train 2", null, c3.getTrain());
		Assert.assertEquals("Car c7 After Build 3 should be assigned to Train 2", train2, c7.getTrain());
		Assert.assertEquals("Car c9 After Build 3 should NOT be assigned to Train 2", null, c9.getTrain());
		// c7 is assigned to Staging Track South End 1 its load will swap
		Assert.assertEquals("Car c7 After Build 3 destination", "South End 1", c7.getDestinationTrackName());
		Assert.assertEquals("Car c7 load After Build 3", "E", c7.getLoad());
		// increase the size of staging
		l3s2.setLength(500);
		// allow default load swaps
		l3s2.setLoadSwapsEnabled(true);  // South End 2
		
		train2.build();
		// Check expected arrival times
		Assert.assertEquals("Train 2 expected departure time", "22:45", train2.getExpectedArrivalTime(rl1));
		Assert.assertEquals("Train 2 expected North End", "1:00:36", train2.getExpectedArrivalTime(rl2));
		// one car dropped and one is picked up at North End, so travel time + two car moves
		Assert.assertEquals("Train 2 expected North Industries", "1:02:49", train2.getExpectedArrivalTime(rl3));

		// the build first resets which removes cars from the train, c3 load should NOT swap
		Assert.assertEquals("Car c3 load After Build 4", "E", c3.getLoad());
		// c9 has less moves than c3 and c7, and now there's enough room for c9
		Assert.assertEquals("Car c3 After Build 4 should NOT be assigned to Train 2", null, c3.getTrain());
		Assert.assertEquals("Car c7 After Build 4 should be assigned to Train 2", train2, c7.getTrain());
		Assert.assertEquals("Car c9 After Build 4 should NOT be assigned to Train 2", null, c9.getTrain());
		// move the train #1
		train2.move();
		// Is the train in route?
		Assert.assertEquals("Train 2 in route after 1st", true, train2.isTrainInRoute());
		train2.move();	// #2
		// Is the train in route?
		Assert.assertEquals("Train 2 in route after 2nd", true, train2.isTrainInRoute());
		train2.move();  // #3
		// Is the train in route?
		Assert.assertEquals("Train 2 in route after 3rd", false, train2.isTrainInRoute());
		
		// Are the engine and car final tracks correct?
		Assert.assertEquals("Engine e1 After Terminate track", "South End 2", e1.getTrackName());
		Assert.assertEquals("Engine e2 After Terminate track", "South End 2", e2.getTrackName());
		Assert.assertEquals("Engine e3 After Terminate track", "South End 1", e3.getTrackName());
		Assert.assertEquals("Engine e4 After Terminate track", "South End 1", e4.getTrackName());
		Assert.assertEquals("Car c1 After Terminate track", "South End 2", c1.getTrackName());
		Assert.assertEquals("Car c2 After Terminate track", "South End 1", c2.getTrackName());
		Assert.assertEquals("Car c3 After Terminate track", "NI Yard", c3.getTrackName());
		Assert.assertEquals("Car c4 After Terminate track", "South End 2", c4.getTrackName());
		Assert.assertEquals("Car c5 After Terminate track", "South End 1", c5.getTrackName());
		Assert.assertEquals("Car c6 After Terminate track", "NI Yard", c6.getTrackName());
		Assert.assertEquals("Car c7 After Terminate track", "South End 1", c7.getTrackName());
		Assert.assertEquals("Car c8 After Terminate track", "South End 2", c8.getTrackName());
		Assert.assertEquals("Car c9 After Terminate track", "NI Yard", c9.getTrackName());
		
		// do cars have the right loads?
		Assert.assertEquals("Car c1 load after Terminate Train 2", "E", c1.getLoad());
		Assert.assertEquals("Car c2 load after Terminate Train 2", "E", c2.getLoad());
		Assert.assertEquals("Car c3 load after Terminate Train 2", "E", c3.getLoad());
		Assert.assertEquals("Car c4 load after Terminate Train 2", "E", c4.getLoad());
		Assert.assertEquals("Car c5 load after Terminate Train 2", "E", c5.getLoad());
		Assert.assertEquals("Car c6 load after Terminate Train 2", "E", c6.getLoad());
		Assert.assertEquals("Car c7 load after Terminate Train 2", "E", c7.getLoad());
		Assert.assertEquals("Car c8 load after Terminate Train 2", "L", c8.getLoad());
		Assert.assertEquals("Car c9 load after Terminate Train 2", "E", c9.getLoad());	
		
		// try building again
		// Place caboose on Staging tracks
		Assert.assertEquals("Place c1", Track.OKAY, c1.setLocation(l1, l1s1));
		train2.setRequirements(Train.CABOOSE);
		train2.setNumberEngines("0");
		train2.build();
		// Should fail both staging tracks are full
		Assert.assertFalse("Train 2 not built", train2.isBuilt());
		
		// add a new staging track
		Track l3s3 =  l3.addTrack("South End 3", Track.STAGING);
		l3s3.setLength(200);
		train2.build();
		// Should build
		Assert.assertTrue("Train 2 built", train2.isBuilt());
		
		// make staging track too small for caboose 
		l3s3.setLength(20);
		train2.build();
		// Should not build
		Assert.assertFalse("Train 2 built", train2.isBuilt());
		l3s3.setLength(200);	// restore
		
		// Car X10001 is a location North Industries, NI Yard, send boxcar X10001 to staging
		Assert.assertEquals("set destination", Track.OKAY, c3.setDestination(l3, null));
		train2.build();
		// Should build
		Assert.assertTrue("Train 2 built", train2.isBuilt());
		Assert.assertEquals("Car X10001 to staging track", l3s3, c3.getDestinationTrack());
		Assert.assertEquals("Car X10001 assigned to train 2", train2, c3.getTrain());

		// Send car X10001 to staging and track that isn't being used
		train2.reset();
		Assert.assertEquals("set destination", Track.OKAY, c3.setDestination(l3, l3s2));
		train2.build();
		// Should build
		Assert.assertFalse("Train 2 built", train2.isBuilt());
		Assert.assertEquals("Car X10001 not assigned to train 2", null, c3.getTrain());
		
		// Send car X10001 to staging and the only track available
		train2.reset();
		c3.setDestination(l3, l3s3);	// this removes track, it is now reserved
		train2.build();
		// Should not build
		Assert.assertFalse("Train 2 built", train2.isBuilt());
		Assert.assertEquals("Car X10001 assigned to train 2", null, c3.getTrain());
		
		train2.setRequirements(Train.NONE);
		train2.reset();
		
		ct.addName("BOXCAR");
		train2.addTypeName("BOXCAR");
		c3.setType("BOXCAR");
		l3.addTypeName("BOXCAR");
		c3.setDestination(l3, null);
		train2.build();
		// Should Not build, staging track South End 3 doesn't service type BOXCAR
		Assert.assertFalse("Train 2 will not build due to BOXCAR", train2.isBuilt());
		Assert.assertEquals("Car X10001 NOT assigned to train 2", null, c3.getTrain());
		
		// turn off staging check
		Setup.setTrainIntoStagingCheckEnabled(false);
		train2.build();
		Assert.assertTrue("Train 2 will now build ignoring BOXCAR", train2.isBuilt());
		Assert.assertEquals("Car X10001 NOT assigned to train 2", null, c3.getTrain());
		Setup.setTrainIntoStagingCheckEnabled(true);
		
		train2.deleteTypeName("BOXCAR");
		c3.setType("Boxcar");
		// control which road will go into staging
		l3s3.setRoadOption(Track.INCLUDEROADS);
		
		train2.build();
		Assert.assertFalse("Train 2 will NOT build road restriction", train2.isBuilt());
		
		train2.setRoadOption(Train.INCLUDEROADS);
		train2.build();
		Assert.assertFalse("Train 2 will NOT build road restriction CP", train2.isBuilt());

		l3s3.addRoadName("CP");
		train2.build();
		Assert.assertTrue("Train 2 will build road restriction CP removed", train2.isBuilt());

		train2.setRoadOption(Train.EXCLUDEROADS);
		train2.deleteRoadName("CP");
		train2.build();
		Assert.assertFalse("Train 2 will NOT build road restriction exclude road CP", train2.isBuilt());
		
		// now allow road into staging
		l3s3.setRoadOption(Track.EXCLUDEROADS);
		l3s3.deleteRoadName("CP");
		
		train2.build();
		Assert.assertTrue("Train 2 will build no road restrictions", train2.isBuilt());
		
		l3s3.addRoadName("BM");
		train2.build();
		Assert.assertFalse("Train 2 will Not build, staging track will not accept road BM", train2.isBuilt());
		
		// end staging to staging 1
	}

	// test train staging to staging
	public void testStagingtoStaging2() {
		TrainManager tmanager = TrainManager.instance();
		RouteManager rmanager = RouteManager.instance();
		LocationManager lmanager = LocationManager.instance();
		EngineManager emanager = EngineManager.instance();
		CarManager cmanager = CarManager.instance();
		CarColors cc = CarColors.instance();
		CarLengths cl = CarLengths.instance();
		CarOwners co = CarOwners.instance();
		CarRoads cr = CarRoads.instance();
		CarLoads cld = CarLoads.instance();
		CarTypes ct = CarTypes.instance();
		EngineModels em = EngineModels.instance();

		// Clear out the global lists
		cc.dispose();  // Clear out the CarColors
		cl.dispose();  // Clear out the CarLengths
		co.dispose();  // Clear out the CarOwners
		cr.dispose();  // Clear out the CarRoads
		ct.dispose();  // Clear out the CarTypes
		cld.dispose(); // Clear out the CarLoads
		em.dispose();  // Clear out the EngineModels
	
		// register the car colors used
		Assert.assertEquals("Bob Test CarColor Silver false", false, cc.containsName("Silver"));
		Assert.assertEquals("Bob Test CarColor Black false", false, cc.containsName("Black"));
		Assert.assertEquals("Bob Test CarColor Red false", false, cc.containsName("Red"));
		cc.addName("Silver");
		Assert.assertEquals("Bob Test CarColor Silver true", true, cc.containsName("Silver"));
		cc.addName("Black");
		Assert.assertEquals("Bob Test CarColor Black true", true, cc.containsName("Black"));
		cc.addName("Red");
		Assert.assertEquals("Bob Test CarColor Red true", true, cc.containsName("Red"));

		// register the car lengths used
		Assert.assertEquals("Bob Test CarLength 32 false", false, cl.containsName("32"));
		Assert.assertEquals("Bob Test CarLength 38 false", false, cl.containsName("38"));
		Assert.assertEquals("Bob Test CarLength 40 false", false, cl.containsName("40"));
		cl.addName("32");
		Assert.assertEquals("Bob Test CarLength 32 true", true, cl.containsName("32"));
		cl.addName("38");
		Assert.assertEquals("Bob Test CarLength 38 true", true, cl.containsName("38"));
		cl.addName("40");
		Assert.assertEquals("Bob Test CarLength 40 true", true, cl.containsName("40"));

		// register the car owners used
		Assert.assertEquals("Bob Test CarOwner Owner1 false", false, co.containsName("Owner1"));
		Assert.assertEquals("Bob Test CarOwner Owner2 false", false, co.containsName("Owner2"));
		Assert.assertEquals("Bob Test CarOwner Owner3 false", false, co.containsName("Owner3"));
		co.addName("Owner1");
		Assert.assertEquals("Bob Test CarOwner Owner1 true", true, co.containsName("Owner1"));
		co.addName("Owner2");
		Assert.assertEquals("Bob Test CarOwner Owner2 true", true, co.containsName("Owner2"));
		co.addName("Owner3");
		Assert.assertEquals("Bob Test CarOwner Owner3 true", true, co.containsName("Owner3"));

		// register the car roads used
		Assert.assertEquals("Bob Test CarRoads CP false", false, cr.containsName("CP"));
		Assert.assertEquals("Bob Test CarRoads Road2 false", false, cr.containsName("Road2"));
		Assert.assertEquals("Bob Test CarRoads Road3 false", false, cr.containsName("Road3"));
		cr.addName("CP");
		Assert.assertEquals("Bob Test CarRoads CP true", true, cr.containsName("CP"));
		cr.addName("Road2");
		Assert.assertEquals("Bob Test CarRoads Road2 true", true, cr.containsName("Road2"));
		cr.addName("Road3");
		Assert.assertEquals("Bob Test CarRoads Road3 true", true, cr.containsName("Road3"));

		// register the car types used
		Assert.assertEquals("Bob Test CarType Caboose false", false, ct.containsName("Caboose"));
		Assert.assertEquals("Bob Test CarType Tanker false", false, ct.containsName("Tanker"));
		Assert.assertEquals("Bob Test CarType Boxcar false", false, ct.containsName("Boxcar"));
		ct.addName("Caboose");
		Assert.assertEquals("Bob Test CarType Caboose true", true, ct.containsName("Caboose"));
		ct.addName("Tanker");
		Assert.assertEquals("Bob Test CarType Tanker true", true, ct.containsName("Tanker"));
		ct.addName("Boxcar");
		Assert.assertEquals("Bob Test CarType Boxcar true", true, ct.containsName("Boxcar"));

		// register the car loads used
		cld.addType("Boxcar");
		Assert.assertEquals("Bob Test CarLoad Boxcar Flour false", false, cld.containsName("Boxcar", "Flour"));
		Assert.assertEquals("Bob Test CarLoad Boxcar Bags false", false, cld.containsName("Boxcar", "Bags"));
		cld.addName("Boxcar", "Flour");
		Assert.assertEquals("Bob Test CarLoad Boxcar Flour true", true, cld.containsName("Boxcar", "Flour"));
		cld.addName("Boxcar", "Bags");
		Assert.assertEquals("Bob Test CarLoad Boxcar Bags true", true, cld.containsName("Boxcar", "Bags"));

		// register the engine models used
		Assert.assertEquals("Bob Test EngineModel GP40 false", false, em.containsName("GP40"));
		Assert.assertEquals("Bob Test EngineModel GP30 false", false, em.containsName("GP30"));
		em.addName("GP40");
		Assert.assertEquals("Bob Test EngineModel GP40 true", true, em.containsName("GP40"));
		em.addName("GP30");
		Assert.assertEquals("Bob Test EngineModel GP30 true", true, em.containsName("GP30"));

		// Create locations used
		Location loc1;
		loc1 = lmanager.newLocation("Westend");
		loc1.setTrainDirections(Location.WEST + Location.EAST);
		Assert.assertEquals("Bob Test Location Westend Name", "Westend", loc1.getName());
		Assert.assertEquals("Bob Test Location Westend Directions", 3, loc1.getTrainDirections());
		Assert.assertEquals("Bob Test Location Westend Type Diesel", true, loc1.acceptsTypeName("Diesel"));
		Assert.assertEquals("Bob Test Location Westend Type Boxcar", true, loc1.acceptsTypeName("Boxcar"));
		Assert.assertEquals("Bob Test Location Westend Type Caboose", true, loc1.acceptsTypeName("Caboose"));

		Location loc2;
		loc2 = lmanager.newLocation("Midtown");
		loc2.setTrainDirections(Location.WEST + Location.EAST);
		Assert.assertEquals("Bob Test Location Midtown Name", "Midtown", loc2.getName());
		Assert.assertEquals("Bob Test Location Midtown Directions", 3, loc2.getTrainDirections());
		Assert.assertEquals("Bob Test Location Midtown Type Diesel", true, loc2.acceptsTypeName("Diesel"));
		Assert.assertEquals("Bob Test Location Midtown Type Boxcar", true, loc2.acceptsTypeName("Boxcar"));
		Assert.assertEquals("Bob Test Location Midtown Type Caboose", true, loc2.acceptsTypeName("Caboose"));

		Location loc3;
		loc3 = lmanager.newLocation("Eastend");
		loc3.setTrainDirections(Location.WEST + Location.EAST);
		Assert.assertEquals("Bob Test Location Eastend Name", "Eastend", loc3.getName());
		Assert.assertEquals("Bob Test Location Eastend Directions", 3, loc3.getTrainDirections());
		Assert.assertEquals("Bob Test Location Eastend Type Diesel", true, loc3.acceptsTypeName("Diesel"));
		Assert.assertEquals("Bob Test Location Eastend Type Boxcar", true, loc3.acceptsTypeName("Boxcar"));
		Assert.assertEquals("Bob Test Location Eastend Type Caboose", true, loc3.acceptsTypeName("Caboose"));

		Track loc1trk1;
		loc1trk1 = loc1.addTrack("Westend Staging 1", Track.YARD);
		loc1trk1.setTrainDirections(Track.WEST + Track.EAST);
		loc1trk1.setLength(500);
		Assert.assertEquals("Bob Test Track Westend Staging 1 Name", "Westend Staging 1", loc1trk1.getName());
		Assert.assertEquals("Bob Test Track Westend Staging 1 Directions", 3, loc1trk1.getTrainDirections());
		Assert.assertEquals("Bob Test Track Westend Staging 1 Length", 500, loc1trk1.getLength());
		Assert.assertEquals("Bob Test Track Westend Staging 1 Type Diesel", true, loc1trk1.acceptsTypeName("Diesel"));
		Assert.assertEquals("Bob Test Track Westend Staging 1 Type Boxcar", true, loc1trk1.acceptsTypeName("Boxcar"));
		Assert.assertEquals("Bob Test Track Westend Staging 1 Type Caboose", true, loc1trk1.acceptsTypeName("Caboose"));

		Track loc2trk1;
		loc2trk1 = loc2.addTrack("Midtown Inbound from West", Track.YARD);
		loc2trk1.setTrainDirections(Track.WEST + Track.EAST);
		loc2trk1.setLength(500);
		Assert.assertEquals("Bob Test Track Midtown West Inbound Name", "Midtown Inbound from West", loc2trk1.getName());
		Assert.assertEquals("Bob Test Track Midtown West Inbound Directions", 3, loc2trk1.getTrainDirections());
		Assert.assertEquals("Bob Test Track Midtown West Inbound Length", 500, loc2trk1.getLength());

		Track loc2trk2;
		loc2trk2 = loc2.addTrack("Midtown Inbound from East", Track.YARD);
		loc2trk2.setTrainDirections(Track.WEST + Track.EAST);
		loc2trk2.setLength(500);
		Assert.assertEquals("Bob Test Track Midtown East Inbound Name", "Midtown Inbound from East", loc2trk2.getName());
		Assert.assertEquals("Bob Test Track Midtown East Inbound Directions", 3, loc2trk2.getTrainDirections());
		Assert.assertEquals("Bob Test Track Midtown East Inbound Length", 500, loc2trk2.getLength());

		Track loc2trk3;
		loc2trk3 = loc2.addTrack("Midtown Outbound to West", Track.YARD);
		loc2trk3.setTrainDirections(Track.WEST);
		loc2trk3.setLength(500);
		Assert.assertEquals("Bob Test Track Midtown West Outbound Name", "Midtown Outbound to West", loc2trk3.getName());
		Assert.assertEquals("Bob Test Track Midtown West Outbound Directions", 2, loc2trk3.getTrainDirections());
		Assert.assertEquals("Bob Test Track Midtown West Outbound Length", 500, loc2trk3.getLength());

		Track loc2trk4;
		loc2trk4 = loc2.addTrack("Midtown Outbound to East", Track.YARD);
		loc2trk4.setTrainDirections(Track.EAST);
		loc2trk4.setLength(500);
		Assert.assertEquals("Bob Test Track Midtown East Outbound Name", "Midtown Outbound to East", loc2trk4.getName());
		Assert.assertEquals("Bob Test Track Midtown East Outbound Directions", 1, loc2trk4.getTrainDirections());
		Assert.assertEquals("Bob Test Track Midtown East Outbound Length", 500, loc2trk4.getLength());

		Track loc2trkc1;
		loc2trkc1 = loc2.addTrack("Midtown Caboose to East", Track.YARD);
		loc2trkc1.setTrainDirections(Track.EAST);
		loc2trkc1.setLength(100);
		Assert.assertEquals("Bob Test Track Midtown East Caboose Name", "Midtown Caboose to East", loc2trkc1.getName());
		Assert.assertEquals("Bob Test Track Midtown East Caboose Directions", 1, loc2trkc1.getTrainDirections());
		Assert.assertEquals("Bob Test Track Midtown East Caboose Length", 100, loc2trkc1.getLength());

		Track loc2trkc2;
		loc2trkc2 = loc2.addTrack("Midtown Caboose to West", Track.YARD);
		loc2trkc2.setTrainDirections(Track.WEST);
		loc2trkc2.setLength(100);
		Assert.assertEquals("Bob Test Track Midtown West Caboose Name", "Midtown Caboose to West", loc2trkc2.getName());
		Assert.assertEquals("Bob Test Track Midtown West Caboose Directions", 2, loc2trkc2.getTrainDirections());
		Assert.assertEquals("Bob Test Track Midtown west Caboose Length", 100, loc2trkc2.getLength());

		Track loc2trke1;
		loc2trke1 = loc2.addTrack("Midtown Engine to East", Track.YARD);
		loc2trke1.setTrainDirections(Track.EAST);
		loc2trke1.setLength(200);
		Assert.assertEquals("Bob Test Track Midtown East Engine Name", "Midtown Engine to East", loc2trke1.getName());
		Assert.assertEquals("Bob Test Track Midtown East Engine Directions", 1, loc2trke1.getTrainDirections());
		Assert.assertEquals("Bob Test Track Midtown East Engine Length", 200, loc2trke1.getLength());

		Track loc2trke2;
		loc2trke2 = loc2.addTrack("Midtown Engine to West", Track.YARD);
		loc2trke2.setTrainDirections(Track.WEST);
		loc2trke2.setLength(200);
		Assert.assertEquals("Bob Test Track Midtown West Engine Name", "Midtown Engine to West", loc2trke2.getName());
		Assert.assertEquals("Bob Test Track Midtown West Engine Directions", 2, loc2trke2.getTrainDirections());
		Assert.assertEquals("Bob Test Track Midtown west Engine Length", 200, loc2trke2.getLength());

		Track loc3trk1;
		loc3trk1 = loc3.addTrack("Eastend Staging 1", Track.YARD);
		loc3trk1.setTrainDirections(Track.WEST + Track.EAST);
		loc3trk1.setLength(500);
		Assert.assertEquals("Bob Test Track Eastend Staging 1 Name", "Eastend Staging 1", loc3trk1.getName());
		Assert.assertEquals("Bob Test Track Eastend Staging 1 Directions", 3, loc3trk1.getTrainDirections());
		Assert.assertEquals("Bob Test Track Eastend Staging 1 Length", 500, loc3trk1.getLength());
		Assert.assertEquals("Bob Test Track Eastend Staging 1 Type Diesel", true, loc3trk1.acceptsTypeName("Diesel"));
		Assert.assertEquals("Bob Test Track Eastend Staging 1 Type Boxcar", true, loc3trk1.acceptsTypeName("Boxcar"));
		Assert.assertEquals("Bob Test Track Eastend Staging 1 Type Caboose", true, loc3trk1.acceptsTypeName("Caboose"));

		Assert.assertEquals("Bob Test Location Westend Length", 500, loc1.getLength());
		Assert.assertEquals("Bob Test Location Midtown Length", 2600, loc2.getLength());
		Assert.assertEquals("Bob Test Location Eastend Length", 500, loc3.getLength());

		// Create engines used
		Engine e1;
		e1 = emanager.newEngine("CP", "5501");
		e1.setModel("GP30");
		e1.setMoves(5);
		Assert.assertEquals("Bob Test Engine CP1801 Type", "Diesel", e1.getType());
		Assert.assertEquals("Bob Test Engine CP1801 Length", "56", e1.getLength());
		Assert.assertEquals("Bob Test Engine CP1801 Hp", "2250", e1.getHp());
		// Test that first "Diesel" is an acceptable type at all locations and tracks
		Assert.assertEquals("Bob Test Engine CP1801 SetLocation 1s1", "okay", e1.setLocation(loc1, loc1trk1));
		Assert.assertEquals("Bob Test Engine CP1801 SetLocation 2s1", "okay", e1.setLocation(loc2, loc2trk1));
		Assert.assertEquals("Bob Test Engine CP1801 SetLocation 2s2", "okay", e1.setLocation(loc2, loc2trk2));
		Assert.assertEquals("Bob Test Engine CP1801 SetLocation 2s3", "okay", e1.setLocation(loc2, loc2trk3));
		Assert.assertEquals("Bob Test Engine CP1801 SetLocation 2s4", "okay", e1.setLocation(loc2, loc2trk4));
		Assert.assertEquals("Bob Test Engine CP1801 SetLocation 3s1", "okay", e1.setLocation(loc3, loc3trk1));
		Assert.assertEquals("Bob Test Engine CP1801 SetLocation 2s4 for real", "okay", e1.setLocation(loc2, loc2trke1));

		Engine e2;
		e2 = emanager.newEngine("CP", "5888");
		e2.setModel("GP40");
		Assert.assertEquals("Bob Test Engine CP5801 Type", "Diesel", e2.getType());
		Assert.assertEquals("Bob Test Engine CP5801 Length", "59", e2.getLength());
		Assert.assertEquals("Bob Test Engine CP5801 Hp", "3000", e2.getHp());
		Assert.assertEquals("Bob Test Engine CP5801 SetLocation 2s4", "okay", e2.setLocation(loc2, loc2trke2));

		// Create cars used
		Car b1;
		b1 = cmanager.newCar("CP", "81234567");
		b1.setType("Boxcar");
		b1.setLength("40");
		b1.setLoad("L");
		b1.setMoves(5);
		Assert.assertEquals("Bob Test Car CP81234567 Length", "40", b1.getLength());
		Assert.assertEquals("Bob Test Car CP81234567 Load", "L", b1.getLoad());
		// Test that first "Boxcar" is an acceptable type at all locations and tracks
		Assert.assertEquals("Bob Test Test Car CP81234567 SetLocation 1s1", "okay", b1.setLocation(loc1, loc1trk1));
		Assert.assertEquals("Bob Test Test Car CP81234567 SetLocation 2s1", "okay", b1.setLocation(loc2, loc2trk1));
		Assert.assertEquals("Bob Test Test Car CP81234567 SetLocation 2s2", "okay", b1.setLocation(loc2, loc2trk2));
		Assert.assertEquals("Bob Test Test Car CP81234567 SetLocation 2s3", "okay", b1.setLocation(loc2, loc2trk3));
		Assert.assertEquals("Bob Test Test Car CP81234567 SetLocation 2s4", "okay", b1.setLocation(loc2, loc2trk4));
		Assert.assertEquals("Bob Test Test Car CP81234567 SetLocation 3s1", "okay", b1.setLocation(loc3, loc3trk1));
		Assert.assertEquals("Bob Test Test Car CP81234567 SetLocation 2s4 for real", "okay", b1.setLocation(loc2, loc2trk4));

		Car b2;
		b2 = cmanager.newCar("CP", "81234568");
		b2.setType("Boxcar");
		b2.setLength("40");
//		b2.setLoad("E");
		b2.setMoves(5);
		Assert.assertEquals("Bob Test Car CP81234568 Length", "40", b2.getLength());
		Assert.assertEquals("Bob Test Car CP81234568 Load", "E", b2.getLoad());
		Assert.assertEquals("Bob Test Test Car CP81234568 SetLocation 2s4", "okay", b2.setLocation(loc2, loc2trk4));

		Car b3;
		b3 = cmanager.newCar("CP", "81234569");
		b3.setType("Boxcar");
		b3.setLength("40");
		b3.setLoad("Flour");
		b3.setMoves(5);
		Assert.assertEquals("Bob Test Car CP81234569 Length", "40", b3.getLength());
		Assert.assertEquals("Bob Test Car CP81234569 Load", "Flour", b3.getLoad());
		Assert.assertEquals("Bob Test Test Car CP81234569 SetLocation 2s4", "okay", b3.setLocation(loc2, loc2trk4));

		Car b4;
		b4 = cmanager.newCar("CP", "81234566");
		b4.setType("Boxcar");
		b4.setLength("40");
		b4.setLoad("Bags");
		b4.setMoves(5);
		Assert.assertEquals("Bob Test Car CP81234566 Length", "40", b4.getLength());
		Assert.assertEquals("Bob Test Car CP81234566 Load", "Bags", b4.getLoad());
		Assert.assertEquals("Bob Test Test Car CP81234566 SetLocation 2s4", "okay", b4.setLocation(loc2, loc2trk4));

		Car b5;
		b5 = cmanager.newCar("CP", "71234567");
		b5.setType("Boxcar");
		b5.setLength("40");
//		b5.setLoad("E");
		Assert.assertEquals("Bob Test Car CP71234567 Length", "40", b5.getLength());
		Assert.assertEquals("Bob Test Car CP71234567 Load", "E", b5.getLoad());
		Assert.assertEquals("Bob Test Test Car CP71234567 SetLocation 2s4", "okay", b5.setLocation(loc2, loc2trk3));

		Car b6;
		b6 = cmanager.newCar("CP", "71234568");
		b6.setType("Boxcar");
		b6.setLength("40");
//		b6.setLoad("E");
		Assert.assertEquals("Bob Test Car CP71234568 Length", "40", b6.getLength());
		Assert.assertEquals("Bob Test Car CP71234568 Load", "E", b6.getLoad());
		Assert.assertEquals("Bob Test Test Car CP71234568 SetLocation 2s4", "okay", b6.setLocation(loc2, loc2trk3));

		Car b7;
		b7 = cmanager.newCar("CP", "71234569");
		b7.setType("Boxcar");
		b7.setLength("40");
//		b7.setLoad("E");
		Assert.assertEquals("Bob Test Car CP71234569 Length", "40", b7.getLength());
		Assert.assertEquals("Bob Test Car CP71234569 Load", "E", b7.getLoad());
		Assert.assertEquals("Bob Test Test Car CP71234569 SetLocation 2s4", "okay", b7.setLocation(loc2, loc2trk3));

		Car b8;
		b8 = cmanager.newCar("CP", "71234566");
		b8.setType("Boxcar");
		b8.setLength("40");
//		b2.setLoad("E");
		Assert.assertEquals("Bob Test Car CP71234566 Length", "40", b8.getLength());
		Assert.assertEquals("Bob Test Car CP71234566 Load", "E", b8.getLoad());
		Assert.assertEquals("Bob Test Test Car CP71234566 SetLocation 2s4", "okay", b8.setLocation(loc2, loc2trk3));

		// Create cars used
		Car c1;
		c1 = cmanager.newCar("CP", "12345678");
		c1.setType("Caboose");
		c1.setLength("32");
		c1.setCaboose(true);
		c1.setMoves(5);
		Assert.assertEquals("Bob Test Caboose CP12345678 Length", "32", c1.getLength());
		Assert.assertEquals("Bob Test Caboose CP12345678 Load", "E", c1.getLoad());
		// Test that first "Caboose" is an acceptable type at all locations and tracks
		Assert.assertEquals("Bob Test Test Caboose CP12345678 SetLocation 1s1", "okay", c1.setLocation(loc1, loc1trk1));
		Assert.assertEquals("Bob Test Test Caboose CP12345678 SetLocation 3s1", "okay", c1.setLocation(loc3, loc3trk1));
		Assert.assertEquals("Bob Test Test Caboose CP12345678 SetLocation 2s5 for real", "okay", c1.setLocation(loc2, loc2trkc1));

		Car c2;
		c2 = cmanager.newCar("CP", "12345679");
		c2.setType("Caboose");
		c2.setLength("32");
		c2.setCaboose(true);
		Assert.assertEquals("Bob Test Caboose CP12345679 Length", "32", c2.getLength());
		Assert.assertEquals("Bob Test Caboose CP12345679 Load", "E", c2.getLoad());
		Assert.assertEquals("Bob Test Test Caboose CP12345679 SetLocation 2s5 for real", "okay", c2.setLocation(loc2, loc2trkc2));

		Assert.assertEquals("Bob Test Location Westend Used Length", 0, loc1.getUsedLength());
		// 56+4 + 59+4 + 2*(4*(40+4) + 32+4) = 123 + 2*(176 + 36) = 547
		Assert.assertEquals("Bob Test Location Midtown Used Length", 547, loc2.getUsedLength());
		Assert.assertEquals("Bob Test Location Eastend Used Length", 0, loc3.getUsedLength());

		// Create routes used
		Route rte1;
		rte1 = rmanager.newRoute("Midtown to Eastend Through");
		Assert.assertEquals("Bob Test Route rte1 Name", "Midtown to Eastend Through", rte1.getName());

		RouteLocation rte1rln1;
		rte1rln1 = rte1.addLocation(loc2);
		rte1rln1.setTrainDirection(RouteLocation.EAST);
		rte1rln1.setTrainIconX(175);	// set the train icon coordinates
		rte1rln1.setTrainIconY(25);
		Assert.assertEquals("Bob Test Route Location rte1rln1 Name", "Midtown", rte1rln1.getName());
		Assert.assertEquals("Bob Test Route Location rte1rln1 Seq", 1, rte1rln1.getSequenceId());

		RouteLocation rte1rln2;
		rte1rln2 = rte1.addLocation(loc3);
		rte1rln2.setTrainDirection(RouteLocation.EAST);
		rte1rln2.setTrainIconX(25);	// set the train icon coordinates
		rte1rln2.setTrainIconY(50);
		Assert.assertEquals("Bob Test Route Location rte1rln2 Name", "Eastend", rte1rln2.getName());
		Assert.assertEquals("Bob Test Route Location rte1rln2 Seq", 2, rte1rln2.getSequenceId());

		Route rte2;
		rte2 = rmanager.newRoute("Midtown to Westend Through");
		Assert.assertEquals("Bob Test Route rte2 Name", "Midtown to Westend Through", rte2.getName());

		RouteLocation rte2rln1;
		rte2rln1 = rte2.addLocation(loc2);
		rte2rln1.setTrainDirection(RouteLocation.WEST);
		rte2rln1.setTrainIconX(75);	// set the train icon coordinates
		rte2rln1.setTrainIconY(50);

		Assert.assertEquals("Bob Test Route Location rte2rln1 Name", "Midtown", rte2rln1.getName());
		Assert.assertEquals("Bob Test Route Location rte2rln1 Seq", 1, rte2rln1.getSequenceId());

		RouteLocation rte2rln2;
		rte2rln2 = rte2.addLocation(loc1);
		rte2rln2.setTrainDirection(RouteLocation.WEST);
		rte2rln2.setTrainIconX(125);	// set the train icon coordinates
		rte2rln2.setTrainIconY(50);

		Assert.assertEquals("Bob Test Route Location rte2rln2 Name", "Westend", rte2rln2.getName());
		Assert.assertEquals("Bob Test Route Location rte2rln2 Seq", 2, rte2rln2.getSequenceId());

		// Create trains used
		Train train1;
		train1 = tmanager.newTrain("MET");
		train1.setRoute(rte1);
		train1.setNumberEngines("1");
		train1.setRequirements(Train.CABOOSE);
		//train1.addTypeName("Diesel");
		//train1.addTypeName("Boxcar");
		//train1.addTypeName("Caboose");
		Assert.assertEquals("Bob Test Train train1 Name", "MET", train1.getName());
		Assert.assertEquals("Bob Test Train train1 Departs Name", "Midtown", train1.getTrainDepartsName());
		Assert.assertEquals("Bob Test Train train1 Terminates Name", "Eastend", train1.getTrainTerminatesName());

		Train train2;
		train2 = tmanager.newTrain("MWT");
		train2.setRoute(rte2);
		train2.setNumberEngines("1");
		train2.setRequirements(Train.CABOOSE);
		//train2.addTypeName("Diesel");
		//train2.addTypeName("Boxcar");
		//train2.addTypeName("Caboose");
		Assert.assertEquals("Bob Test Train train2 Name", "MWT", train2.getName());
		Assert.assertEquals("Bob Test Train train2 Departs Name", "Midtown", train2.getTrainDepartsName());
		Assert.assertEquals("Bob Test Train train2 Terminates Name", "Westend", train2.getTrainTerminatesName());
				
		// disable build messages
		tmanager.setBuildMessagesEnabled(false);
		// Build trains
		train1.build();
		train2.build();
		
		Assert.assertTrue("Bob test train1 built", train1.isBuilt());
		Assert.assertTrue("Bob test train2 built", train2.isBuilt());
	
	}
	
	// Test a route of one location (local train).
	// Locations that don't have a train direction assigned
	// can only be served by a local train.
	// Creates two locations Westford and Chelmsford and 9 cars.
	// Westford has 2 yards, 2 sidings, 3 interchange tracks.
	// Chelmsford has 1 yard.  Chelmsford is used to test that a
	// train with two locations will not service certain tracks.
	public void testLocal(){
		TrainManager tmanager = TrainManager.instance();
		RouteManager rmanager = RouteManager.instance();
		LocationManager lmanager = LocationManager.instance();
		CarManager cmanager = CarManager.instance();
		CarTypes ct = CarTypes.instance();
		
		// Create locations used
		Location loc1;
		loc1 = lmanager.newLocation("Westford");
		loc1.setTrainDirections(DIRECTION_ALL);	
		loc1.addTypeName("Flat Car");
		
		Location loc2;
		loc2 = lmanager.newLocation("Chelmsford");
		loc2.setTrainDirections(DIRECTION_ALL);	
		loc2.addTypeName("Flat Car");
		
		Track loc1trk1;
		loc1trk1 = loc1.addTrack("Westford Yard 1", Track.YARD);
		loc1trk1.setTrainDirections(Track.WEST + Track.EAST);
		loc1trk1.setLength(500);
		loc1trk1.addTypeName("Flat Car");
		
		Track loc1trk2;
		loc1trk2 = loc1.addTrack("Westford Yard 2", Track.YARD);
		loc1trk2.setTrainDirections(Track.WEST + Track.EAST);
		loc1trk2.setLength(500);
		loc1trk2.addTypeName("Flat Car");
		
		Track loc1trk3;
		loc1trk3 = loc1.addTrack("Westford Siding 3", Track.SIDING);
		loc1trk3.setTrainDirections(0);		// Only local moves allowed
		loc1trk3.setLength(300);
		loc1trk3.addTypeName("Flat Car");
		
		Track loc1trk4;
		loc1trk4 = loc1.addTrack("Westford Siding 4", Track.SIDING);
		loc1trk4.setTrainDirections(0);		// Only local moves allowed
		loc1trk4.setLength(300);
		loc1trk4.addTypeName("Flat Car");
		
		Track loc1trk5;
		loc1trk5 = loc1.addTrack("Westford Interchange 5", Track.INTERCHANGE);
		loc1trk5.setTrainDirections(0);		// Only local moves allowed
		loc1trk5.setLength(300);
		loc1trk5.addTypeName("Flat Car");
		
		Track loc1trk6;
		loc1trk6 = loc1.addTrack("Westford Interchange 6", Track.INTERCHANGE);
		loc1trk6.setTrainDirections(Track.WEST + Track.EAST);		
		loc1trk6.setLength(300);
		loc1trk6.addTypeName("Flat Car");
		
		Track loc1trk7;
		loc1trk7 = loc1.addTrack("Westford Interchange 7", Track.INTERCHANGE);
		loc1trk7.setTrainDirections(0);		// Only local moves allowed
		loc1trk7.setLength(300);
		loc1trk7.addTypeName("Flat Car");
		
		Track loc2trk1;
		loc2trk1 = loc2.addTrack("Chelmsford Yard 1", Track.YARD);
		loc2trk1.setTrainDirections(Track.WEST + Track.EAST);
		loc2trk1.setLength(900);
		loc2trk1.addTypeName("Flat Car");
		
		// now bias track selection by moves
		loc1trk1.setMoves(3);		// no yard to yard moves expected
		loc1trk2.setMoves(4);		// no yard to yard moves expected
		loc1trk3.setMoves(10);		// this will be the 5th location assigned
		loc1trk4.setMoves(10);		// this will be the 6th location assigned
		loc1trk5.setMoves(9);		// this will be the 2nd location assigned
		loc1trk6.setMoves(9);		// this will be the 3rd location assigned
		loc1trk7.setMoves(8);		// this will be the first and 4th location assigned

		// Create route with only one location
		Route rte1 = rmanager.newRoute("Local Route");
		Setup.setCarMoves(7);	// set the default moves to 7
		RouteLocation rl1 = rte1.addLocation(loc1);
		
		// Create train
		Train train1 = tmanager.newTrain("Local Train");
		train1.setRoute(rte1);
		// Flat Car isn't registered yet so add it now
		train1.addTypeName("Flat Car");
		
		// Set up 7 box cars and 2 flat cars
		Car c1 = new Car("BM", "1");
		c1.setType("Boxcar");
		c1.setLength("90");
		c1.setMoves(17);		// should be the 7th car assigned to train
		cmanager.register(c1);

		Car c2 = new Car("CP", "2");
		c2.setType("Boxcar");
		c2.setLength("80");
		c2.setMoves(15);		// should be the 6th car assigned to train
		cmanager.register(c2);

		Car c3 = new Car("XP", "3");
		c3.setType("Flat Car");
		c3.setLength("70");
								// default c3 moves = 0 should be the 1st car assigned
		cmanager.register(c3);

		Car c4 = new Car("UP", "4");
		c4.setType("Boxcar");
		c4.setLength("60");
		c4.setMoves(6);			// should be the 5th car assigned to train
		cmanager.register(c4);

		Car c5 = new Car("UP", "5");
		c5.setType("Boxcar");
		c5.setLength("50");
		c5.setMoves(1);			// should be the 2nd car assigned to train
		cmanager.register(c5);

		Car c6 = new Car("CP", "6");
		c6.setType("Boxcar");
		c6.setLength("40");
		c6.setMoves(3);			// should be the 4th car assigned to train
		cmanager.register(c6);
		
		Car c7 = new Car("UP", "7");
		c7.setType("Boxcar");
		c7.setLength("50");
		c7.setMoves(18);	
		cmanager.register(c7);
		
		Car c8 = new Car("XP", "8");
		c8.setType("Boxcar");
		c8.setLength("60");
		c8.setMoves(2);			// should be the 2rd car assigned to train
		cmanager.register(c8);
		
		Car c9 = new Car("XP", "9");
		c9.setType("Flat Car");
		c9.setLength("90");
		c9.setMoves(19);
		cmanager.register(c9);
		
		Assert.assertEquals("Westford should not accept Flat Car", false, loc1.acceptsTypeName("Flat Car"));
		// add Flat Car as a valid type so Westford will accept
		ct.addName("Flat Car");
		Assert.assertEquals("Westford should now accepts Flat Car", true, loc1.acceptsTypeName("Flat Car"));
		
		// place the cars in the yards
		Assert.assertEquals("Place c1", Track.OKAY, c1.setLocation(loc1, loc1trk1));
		Assert.assertEquals("Place c2", Track.OKAY, c2.setLocation(loc1, loc1trk1));
		Assert.assertEquals("Place c3", Track.OKAY, c3.setLocation(loc1, loc1trk1));
		Assert.assertEquals("Place c4", Track.OKAY, c4.setLocation(loc1, loc1trk1));
		
		Assert.assertEquals("Place c5", Track.OKAY, c5.setLocation(loc1, loc1trk2));
		Assert.assertEquals("Place c6", Track.OKAY, c6.setLocation(loc1, loc1trk2));
		Assert.assertEquals("Place c7", Track.OKAY, c7.setLocation(loc1, loc1trk2));
		Assert.assertEquals("Place c8", Track.OKAY, c8.setLocation(loc1, loc1trk2));
		
		Assert.assertEquals("Place c9", Track.OKAY, c9.setLocation(loc1, loc1trk3));

		// do cars have the right default loads?
		Assert.assertEquals("Car c1 load should be E", "E", c1.getLoad());
		Assert.assertEquals("Car c2 load should be E", "E", c2.getLoad());
		Assert.assertEquals("Car c3 load should be E", "E", c3.getLoad());
		Assert.assertEquals("Car c4 load should be E", "E", c4.getLoad());
		Assert.assertEquals("Car c5 load should be E", "E", c5.getLoad());
		Assert.assertEquals("Car c6 load should be E", "E", c6.getLoad());
		Assert.assertEquals("Car c7 load should be E", "E", c7.getLoad());
		Assert.assertEquals("Car c8 load should be E", "E", c8.getLoad());
		Assert.assertEquals("Car c9 load should be E", "E", c9.getLoad());
		
		// Build train
		train1.build();

		Assert.assertEquals("Train 1 After Build Departs Name", "Westford", train1.getTrainDepartsName());
		Assert.assertEquals("Train 1 After Build Terminates Name", "Westford", train1.getTrainTerminatesName());
		Assert.assertEquals("Train 1 After Build Next Location Name", "", train1.getNextLocationName());
		Assert.assertEquals("Train 1 After Build Built Status", true, train1.isBuilt());

		// are the right cars assigned to the train?
		// the default moves is 7, therefore only 7 cars should be moved based on their move counts
		Assert.assertEquals("Car c1 After Build should be assigned to Train 1", train1, c1.getTrain());
		Assert.assertEquals("Car c2 After Build should be assigned to Train 1", train1, c2.getTrain());
		Assert.assertEquals("Car c3 After Build should be assigned to Train 1", train1, c3.getTrain());
		Assert.assertEquals("Car c4 After Build should NOT be assigned to Train 1", train1, c4.getTrain());
		Assert.assertEquals("Car c5 After Build should be assigned to Train 1", train1, c5.getTrain());
		Assert.assertEquals("Car c6 After Build should be assigned to Train 1", train1, c6.getTrain());
		Assert.assertEquals("Car c7 After Build should NOT be assigned to Train 1", null, c7.getTrain());
		Assert.assertEquals("Car c8 After Build should be assigned to Train 1", train1, c8.getTrain());
		Assert.assertEquals("Car c9 After Build should NOT be assigned to Train 1", null, c9.getTrain());
		
		// now check to see if cars are going to be delivered to the right places?
		Assert.assertEquals("Car c1 After Build destination", "Westford Interchange 5", c1.getDestinationTrackName());
		Assert.assertEquals("Car c2 After Build destination", "Westford Siding 4", c2.getDestinationTrackName());
		Assert.assertEquals("Car c3 After Build destination", "Westford Interchange 7", c3.getDestinationTrackName());
		Assert.assertEquals("Car c4 After Build destination", "Westford Siding 3", c4.getDestinationTrackName());
		Assert.assertEquals("Car c5 After Build destination", "Westford Interchange 5", c5.getDestinationTrackName());
		Assert.assertEquals("Car c6 After Build destination", "Westford Interchange 7", c6.getDestinationTrackName());
		Assert.assertEquals("Car c8 After Build destination", "Westford Interchange 6", c8.getDestinationTrackName());
		
		// are the pickup and drop counts correct?
		Assert.assertEquals("Drop count for Westford", 7, loc1.getDropRS()); 
		Assert.assertEquals("Drop count for Westford track Westford Yard 1", 0, loc1trk1.getDropRS());
		Assert.assertEquals("Drop count for Westford track Westford Yard 2", 0, loc1trk2.getDropRS());
		Assert.assertEquals("Drop count for Westford track Westford Siding 3", 1, loc1trk3.getDropRS());
		Assert.assertEquals("Drop count for Westford track Westford Siding 4", 1, loc1trk4.getDropRS());
		Assert.assertEquals("Drop count for Westford track Westford Interchange 5", 2, loc1trk5.getDropRS());
		Assert.assertEquals("Drop count for Westford track Westford Interchange 6", 1, loc1trk6.getDropRS());
		Assert.assertEquals("Drop count for Westford track Westford Interchange 7", 2, loc1trk7.getDropRS());
		Assert.assertEquals("Pickup count for Westford", 7, loc1.getPickupRS());
		Assert.assertEquals("Pickup count for Westford track Westford Yard 1", 4, loc1trk1.getPickupRS());
		Assert.assertEquals("Pickup count for Westford track Westford Yard 2", 3, loc1trk2.getPickupRS());
		Assert.assertEquals("Pickup count for Westford track Westford Siding 3", 0, loc1trk3.getPickupRS());
		Assert.assertEquals("Pickup count for Westford track Westford Siding 4", 0, loc1trk4.getPickupRS());
		Assert.assertEquals("Pickup count for Westford track Westford Interchange 5", 0, loc1trk5.getPickupRS());
		Assert.assertEquals("Pickup count for Westford track Westford Interchange 6", 0, loc1trk6.getPickupRS());
		Assert.assertEquals("Pickup count for Westford track Westford Interchange 7", 0, loc1trk7.getPickupRS());

		train1.move();
		// Train should not be in route since there's only one location
		Assert.assertEquals("Train 1 in route", false, train1.isTrainInRoute());
		// check for correct tracks
		Assert.assertEquals("Car c1 After Move location", "Westford Interchange 5", c1.getTrackName());
		Assert.assertEquals("Car c2 After Move location", "Westford Siding 4", c2.getTrackName());
		Assert.assertEquals("Car c3 After Move location", "Westford Interchange 7", c3.getTrackName());
		Assert.assertEquals("Car c4 After Move location", "Westford Siding 3", c4.getTrackName());
		Assert.assertEquals("Car c5 After Move location", "Westford Interchange 5", c5.getTrackName());
		Assert.assertEquals("Car c6 After Move location", "Westford Interchange 7", c6.getTrackName());
		Assert.assertEquals("Car c8 After Move location", "Westford Interchange 6", c8.getTrackName());

		// do cars have the right loads?
		Assert.assertEquals("Car c1 After Move load should be E", "E", c1.getLoad());
		Assert.assertEquals("Car c2 After Move load should be L", "L", c2.getLoad());
		Assert.assertEquals("Car c3 After Move load should be E", "E", c3.getLoad());
		Assert.assertEquals("Car c4 After Move load should be L", "L", c4.getLoad());
		Assert.assertEquals("Car c5 After Move load should be E", "E", c5.getLoad());
		Assert.assertEquals("Car c6 After Move load should be E", "E", c6.getLoad());
		Assert.assertEquals("Car c7 After Move load should be E", "E", c7.getLoad());
		Assert.assertEquals("Car c8 After Move load should be E", "E", c8.getLoad());
		Assert.assertEquals("Car c9 After Move load should be E", "E", c9.getLoad());

		// are the pickup and drop counts correct?
		Assert.assertEquals("Move 1 Drop count for Westford", 0, loc1.getDropRS()); 
		Assert.assertEquals("Move 1 Drop count for Westford track Westford Yard 1", 0, loc1trk1.getDropRS());
		Assert.assertEquals("Move 1 Drop count for Westford track Westford Yard 2", 0, loc1trk2.getDropRS());
		Assert.assertEquals("Move 1 Drop count for Westford track Westford Siding 3", 0, loc1trk3.getDropRS());
		Assert.assertEquals("Move 1 Drop count for Westford track Westford Siding 4", 0, loc1trk4.getDropRS());
		Assert.assertEquals("Move 1 Drop count for Westford track Westford Interchange 5", 0, loc1trk5.getDropRS());
		Assert.assertEquals("Move 1 Drop count for Westford track Westford Interchange 6", 0, loc1trk6.getDropRS());
		Assert.assertEquals("Move 1 Drop count for Westford track Westford Interchange 7", 0, loc1trk7.getDropRS());
		Assert.assertEquals("Move 1 Pickup count for Westford", 0, loc1.getPickupRS());
		Assert.assertEquals("Move 1 Pickup count for Westford track Westford Yard 1", 0, loc1trk1.getPickupRS());
		Assert.assertEquals("Move 1 Pickup count for Westford track Westford Yard 2", 0, loc1trk2.getPickupRS());
		Assert.assertEquals("Move 1 Pickup count for Westford track Westford Siding 3", 0, loc1trk3.getPickupRS());
		Assert.assertEquals("Move 1 Pickup count for Westford track Westford Siding 4", 0, loc1trk4.getPickupRS());
		Assert.assertEquals("Move 1 Pickup count for Westford track Westford Interchange 5", 0, loc1trk5.getPickupRS());
		Assert.assertEquals("Move 1 Pickup count for Westford track Westford Interchange 6", 0, loc1trk6.getPickupRS());
		Assert.assertEquals("Move 1 Pickup count for Westford track Westford Interchange 7", 0, loc1trk7.getPickupRS());

		// This move should terminate the train.
		train1.move();
		Assert.assertEquals("Train 1 After 2nd Move Status", Train.TERMINATED, getTrainStatus(train1));

		// build the train again, now there are cars on all tracks
		rl1.setMaxCarMoves(10);	// try and use all 9/10 of the cars
		train1.build();
		// c1, c3, c5, c6, c8 are at interchange tracks and should not be assigned to train1
		Assert.assertEquals("Car c1 After Build 2 should NOT be assigned to Train 1", null, c1.getTrain());
		Assert.assertEquals("Car c2 After Build 2 should be assigned to Train 1", train1, c2.getTrain());
		Assert.assertEquals("Car c3 After Build 2 should NOT be assigned to Train 1", null, c3.getTrain());
		Assert.assertEquals("Car c4 After Build 2 should be assigned to Train 1", train1, c4.getTrain());
		Assert.assertEquals("Car c5 After Build 2 should NOT be assigned to Train 1", null, c5.getTrain());
		Assert.assertEquals("Car c6 After Build 2 should NOT be assigned to Train 1", null, c6.getTrain());
		Assert.assertEquals("Car c7 After Build 2 should be assigned to Train 1", train1, c7.getTrain());
		Assert.assertEquals("Car c8 After Build 2 should NOT be assigned to Train 1", null, c8.getTrain());
		Assert.assertEquals("Car c9 After Build 2 should be assigned to Train 1", train1, c9.getTrain());

		// now check to see if cars are going to be delivered to the right places?
		Assert.assertEquals("Car c2 After Build 2 destination", "Westford Yard 1", c2.getDestinationTrackName());
		Assert.assertEquals("Car c4 After Build 2 destination", "Westford Yard 1", c4.getDestinationTrackName());
		Assert.assertEquals("Car c7 After Build 2 destination", "Westford Interchange 6", c7.getDestinationTrackName());
		Assert.assertEquals("Car c9 After Build 2 destination", "Westford Yard 2", c9.getDestinationTrackName());
		// move and terminate
		train1.move();
		train1.move();
		Assert.assertEquals("Train 1 After 2nd build Status", Train.TERMINATED, getTrainStatus(train1));
		
		// are cars at the right location?
		Assert.assertEquals("Car c2 After Move 2 location", "Westford Yard 1", c2.getTrackName());
		Assert.assertEquals("Car c4 After Move 2 location", "Westford Yard 1", c4.getTrackName());
		Assert.assertEquals("Car c7 After Move 2 location", "Westford Interchange 6", c7.getTrackName());
		Assert.assertEquals("Car c9 After Move 2 location", "Westford Yard 2", c9.getTrackName());
	
		// do cars have the right loads?
		Assert.assertEquals("Car c1 After Move 2 load should be E", "E", c1.getLoad());
		Assert.assertEquals("Car c2 After Move 2 load should be L", "L", c2.getLoad());
		Assert.assertEquals("Car c3 After Move 2 load should be E", "E", c3.getLoad());
		Assert.assertEquals("Car c4 After Move 2 load should be L", "L", c4.getLoad());
		Assert.assertEquals("Car c5 After Move 2 load should be E", "E", c5.getLoad());
		Assert.assertEquals("Car c6 After Move 2 load should be E", "E", c6.getLoad());
		Assert.assertEquals("Car c7 After Move 2 load should be E", "E", c7.getLoad());
		Assert.assertEquals("Car c8 After Move 2 load should be E", "E", c8.getLoad());
		Assert.assertEquals("Car c9 After Move 2 load should be E", "E", c9.getLoad());
		
		// try a new route, this should allow cars to move from interchange
		// Create route with only one location
		Route rte2;
		rte2 = rmanager.newRoute("Local Route 2");
		RouteLocation rl2 = rte2.addLocation(loc1);
		rl2.setMaxCarMoves(8);	// move 8 of the 9 cars available
		// and assign the new route to train 1
		train1.setRoute(rte2);
		train1.build();
		// we should be able to pickup cars at the interchange tracks
		Assert.assertEquals("Car c1 After Build 3 should be assigned to Train 1", train1, c1.getTrain());
		Assert.assertEquals("Car c2 After Build 3 should be assigned to Train 1", train1, c2.getTrain());
		Assert.assertEquals("Car c3 After Build 3 should be assigned to Train 1", train1, c3.getTrain());
		Assert.assertEquals("Car c4 After Build 3 should be assigned to Train 1", train1, c4.getTrain());
		Assert.assertEquals("Car c5 After Build 3 should be assigned to Train 1", train1, c5.getTrain());
		Assert.assertEquals("Car c6 After Build 3 should be assigned to Train 1", train1, c6.getTrain());
		Assert.assertEquals("Car c7 After Build 3 should be assigned to Train 1", train1, c7.getTrain());
		Assert.assertEquals("Car c8 After Build 3 should be assigned to Train 1", train1, c8.getTrain());
		Assert.assertEquals("Car c9 After Build 3 should NOT be assigned to Train 1", null, c9.getTrain());

		// now check to see if cars are going to be delivered to the right places?
		Assert.assertEquals("Car c1 After Build 3 destination", "Westford Yard 1", c1.getDestinationTrackName());
		Assert.assertEquals("Car c2 After Build 3 destination", "Westford Siding 3", c2.getDestinationTrackName());
		Assert.assertEquals("Car c3 After Build 3 destination", "Westford Yard 1", c3.getDestinationTrackName());
		Assert.assertEquals("Car c4 After Build 3 destination", "Westford Interchange 7", c4.getDestinationTrackName());
		Assert.assertEquals("Car c5 After Build 3 destination", "Westford Yard 2", c5.getDestinationTrackName());
		Assert.assertEquals("Car c6 After Build 3 destination", "Westford Yard 2", c6.getDestinationTrackName());
		Assert.assertEquals("Car c7 After Build 3 destination", "Westford Yard 2", c7.getDestinationTrackName());
		Assert.assertEquals("Car c8 After Build 3 destination", "Westford Yard 1", c8.getDestinationTrackName());
		// move and terminate
		train1.move();
		train1.move();
		Assert.assertEquals("Train 1 After 2nd build Status", Train.TERMINATED, getTrainStatus(train1));
		
		// Final check to see if cars were delivered. 
		Assert.assertEquals("Car c1 After Move 3 location", "Westford Yard 1", c1.getTrackName());
		Assert.assertEquals("Car c2 After Move 3 location", "Westford Siding 3", c2.getTrackName());
		Assert.assertEquals("Car c3 After Move 3 location", "Westford Yard 1", c3.getTrackName());
		Assert.assertEquals("Car c4 After Move 3 location", "Westford Interchange 7", c4.getTrackName());
		Assert.assertEquals("Car c5 After Move 3 location", "Westford Yard 2", c5.getTrackName());
		Assert.assertEquals("Car c6 After Move 3 location", "Westford Yard 2", c6.getTrackName());
		Assert.assertEquals("Car c7 After Move 3 location", "Westford Yard 2", c7.getTrackName());
		Assert.assertEquals("Car c8 After Move 3 location", "Westford Yard 1", c8.getTrackName());

		// do cars have the right loads?
		Assert.assertEquals("Car c1 After Move 3 load should be E", "E", c1.getLoad());
		Assert.assertEquals("Car c2 After Move 3 load should be E", "E", c2.getLoad());
		Assert.assertEquals("Car c3 After Move 3 load should be E", "E", c3.getLoad());
		Assert.assertEquals("Car c4 After Move 3 load should be L", "L", c4.getLoad());
		Assert.assertEquals("Car c5 After Move 3 load should be E", "E", c5.getLoad());
		Assert.assertEquals("Car c6 After Move 3 load should be E", "E", c6.getLoad());
		Assert.assertEquals("Car c7 After Move 3 load should be E", "E", c7.getLoad());
		Assert.assertEquals("Car c8 After Move 3 load should be E", "E", c8.getLoad());
		Assert.assertEquals("Car c9 After Move 3 load should be E", "E", c9.getLoad());
		
		// check car move counts
		Assert.assertEquals("Car c1 Move count", 19, c1.getMoves());
		Assert.assertEquals("Car c2 Move count", 18, c2.getMoves());
		Assert.assertEquals("Car c3 Move count", 2, c3.getMoves());
		Assert.assertEquals("Car c4 Move count", 9, c4.getMoves());
		Assert.assertEquals("Car c5 Move count", 3, c5.getMoves());
		Assert.assertEquals("Car c6 Move count", 5, c6.getMoves());
		Assert.assertEquals("Car c7 Move count", 20, c7.getMoves());
		Assert.assertEquals("Car c8 Move count", 4, c8.getMoves());
		Assert.assertEquals("Car c9 Move count", 20, c9.getMoves());

		// now try and use a train with more than one location
		// Create route with two locations
		Route rte3;
		rte3 = rmanager.newRoute("Westford to Chelmsford");
		RouteLocation rl3 = rte3.addLocation(loc1);
		rl3.setTrainDirection(RouteLocation.WEST);
		rl3.setMaxCarMoves(10);
		RouteLocation rl4 = rte3.addLocation(loc2);
		rl4.setTrainDirection(RouteLocation.WEST);
		// and assign the new route to train 1
		train1.setRoute(rte3);
		rl4.setMaxCarMoves(10);
		rl4.setTrainIconX(175);	// set the train icon coordinates
		rl4.setTrainIconY(50);

		train1.build();
		// should not pick up cars at Westford Siding 3, Westford Siding 4, Westford Interchange 5
		// and Westford Interchange 7
		Assert.assertEquals("Car c1 After Build 4 should be assigned to Train 1", train1, c1.getTrain());
		Assert.assertEquals("Car c2 After Build 4 should NOT be assigned to Train 1", null, c2.getTrain());
		Assert.assertEquals("Car c3 After Build 4 should be assigned to Train 1", train1, c3.getTrain());
		Assert.assertEquals("Car c4 After Build 4 should NOT be assigned to Train 1", null, c4.getTrain());
		Assert.assertEquals("Car c5 After Build 4 should be assigned to Train 1", train1, c5.getTrain());
		Assert.assertEquals("Car c6 After Build 4 should be assigned to Train 1", train1, c6.getTrain());
		Assert.assertEquals("Car c7 After Build 4 should be assigned to Train 1", train1, c7.getTrain());
		Assert.assertEquals("Car c8 After Build 4 should be assigned to Train 1", train1, c8.getTrain());
		Assert.assertEquals("Car c9 After Build 4 should be assigned to Train 1", train1, c9.getTrain());

		train1.move();
		// Train in route since there's two locations
		Assert.assertEquals("Train 1 in route to Chelmsford", true, train1.isTrainInRoute());
		train1.move();
		// 7 cars should in Chelmsford, the other 2 in Westford
		Assert.assertEquals("Car c1 After Move 4 location", "Chelmsford Yard 1", c1.getTrackName());
		Assert.assertEquals("Car c2 verify location", "Westford Siding 3", c2.getTrackName());
		Assert.assertEquals("Car c3 After Move 4 location", "Chelmsford Yard 1", c3.getTrackName());
		Assert.assertEquals("Car c4 verify location", "Westford Interchange 7", c4.getTrackName());
		Assert.assertEquals("Car c5 After Move 4 location", "Chelmsford Yard 1", c5.getTrackName());
		Assert.assertEquals("Car c6 After Move 4 location", "Chelmsford Yard 1", c6.getTrackName());
		Assert.assertEquals("Car c7 After Move 4 location", "Chelmsford Yard 1", c7.getTrackName());
		Assert.assertEquals("Car c8 After Move 4 location", "Chelmsford Yard 1", c8.getTrackName());
		Assert.assertEquals("Car c9 After Move 4 location", "Chelmsford Yard 1", c9.getTrackName());
	
		// do cars have the right loads?
		Assert.assertEquals("Car c1 After Move 4 load should be E", "E", c1.getLoad());
		Assert.assertEquals("Car c2 After Move 4 load should be E", "E", c2.getLoad());
		Assert.assertEquals("Car c3 After Move 4 load should be E", "E", c3.getLoad());
		Assert.assertEquals("Car c4 After Move 4 load should be L", "L", c4.getLoad());
		Assert.assertEquals("Car c5 After Move 4 load should be E", "E", c5.getLoad());
		Assert.assertEquals("Car c6 After Move 4 load should be E", "E", c6.getLoad());
		Assert.assertEquals("Car c7 After Move 4 load should be E", "E", c7.getLoad());
		Assert.assertEquals("Car c8 After Move 4 load should be E", "E", c8.getLoad());
		Assert.assertEquals("Car c9 After Move 4 load should be E", "E", c9.getLoad());

		// check car move counts
		Assert.assertEquals("Car c1 Move count", 20, c1.getMoves());
		Assert.assertEquals("Car c2 Move count", 18, c2.getMoves());
		Assert.assertEquals("Car c3 Move count", 3, c3.getMoves());
		Assert.assertEquals("Car c4 Move count", 9, c4.getMoves());
		Assert.assertEquals("Car c5 Move count", 4, c5.getMoves());
		Assert.assertEquals("Car c6 Move count", 6, c6.getMoves());
		Assert.assertEquals("Car c7 Move count", 21, c7.getMoves());
		Assert.assertEquals("Car c8 Move count", 5, c8.getMoves());
		Assert.assertEquals("Car c9 Move count", 21, c9.getMoves());
		
		train1.move();
		Assert.assertEquals("Train 1 After 4th build Status", Train.TERMINATED, getTrainStatus(train1));
		
		// test siding to siding
		train1.setRoute(rte1);
		
		// bias track selection to sidings
		loc1trk3.setMoves(2);
		loc1trk4.setMoves(2);
		train1.build();

		Assert.assertTrue("local testing siding to siding", train1.isBuilt());
		Assert.assertEquals("car UP 4 at interchange, destination Westford Siding 3", loc1trk3, c4.getDestinationTrack());
		Assert.assertEquals("car CP 2 at siding, destination Westford Yard 1", loc1trk1, c2.getDestinationTrack());
		
		// bias track selection to interchanges
		loc1trk3.setMoves(12);
		loc1trk4.setMoves(12);
		loc1trk5.setMoves(2);
		loc1trk6.setMoves(2);
		train1.build();

		Assert.assertTrue("local testing siding to siding", train1.isBuilt());
		Assert.assertEquals("car UP 4 at interchange, destination", "Westford Yard 2", c4.getDestinationTrackName());
		Assert.assertEquals("car CP 2 at siding, destination", "Westford Interchange 5", c2.getDestinationTrackName());

		// set CP 2 destination, currently at Westford, Westford Siding 3
		train1.reset();			// release CP2 from train so we can set the car's destination
		c2.setDestination(loc1, null);
		loc1trk3.setMoves(1);	// bias to same track
		train1.build();

		Assert.assertTrue("local testing siding to siding", train1.isBuilt());
		Assert.assertEquals("car UP 4 at interchange, destination", "Westford Siding 3", c4.getDestinationTrackName());
		Assert.assertEquals("car CP 2 at siding, destination", "Westford Interchange 6", c2.getDestinationTrackName());
		
		// CP 2 is at Westford Siding 3, set destination to be the same
		train1.reset();
		c2.setDestination(loc1, loc1trk3);
		train1.build();

		Assert.assertTrue("local testing siding to siding", train1.isBuilt());
		Assert.assertEquals("car UP 4 at interchange, destination", "Westford Siding 3", c4.getDestinationTrackName());
		Assert.assertEquals("car CP 2 at siding, destination", "Westford Siding 3", c2.getDestinationTrackName());
		
		train1.move();
		Assert.assertEquals("Train 1 terminated", Train.TERMINATED, getTrainStatus(train1));

	}
	
	public void testScheduleLoads(){
		TrainManager tmanager = TrainManager.instance();
		RouteManager rmanager = RouteManager.instance();
		LocationManager lmanager = LocationManager.instance();
		CarManager cmanager = CarManager.instance();
		ScheduleManager smanager = ScheduleManager.instance();		
		CarTypes ct = CarTypes.instance();
		
		ct.addName("Gon");
		ct.addName("Coil Car");		
		
		// create schedules
		Schedule sch1 = smanager.newSchedule("Schedule 1");
		ScheduleItem sch1Item1 = sch1.addItem("Boxcar");
		// request a UP Boxcar
		sch1Item1.setRoad("UP");
		ScheduleItem sch1Item2 = sch1.addItem("Flat Car");
		// request an empty car and load it with Scrap
		sch1Item2.setLoad("E");
		sch1Item2.setShip("Scrap");
		ScheduleItem sch1Item3 = sch1.addItem("Gon");
		// request a loaded car and load it with Tin
		sch1Item3.setLoad("L");
		sch1Item3.setShip("Tin");
		
		Schedule sch2 = smanager.newSchedule("Schedule 2");
		ScheduleItem sch2Item1 = sch2.addItem("Coil Car");
		sch2Item1.setCount(2);
		sch2.addItem("Boxcar");
		
		// Create locations used
		Location loc1;
		loc1 = lmanager.newLocation("New Westford");
		loc1.setTrainDirections(DIRECTION_ALL);	
		
		Location loc2;
		loc2 = lmanager.newLocation("New Chelmsford");
		loc2.setTrainDirections(DIRECTION_ALL);	
		
		Location loc3;
		loc3 = lmanager.newLocation("New Bedford");
		loc3.setTrainDirections(DIRECTION_ALL);	
		
		Track loc1trk1;
		loc1trk1 = loc1.addTrack("Westford Yard 1", Track.YARD);
		loc1trk1.setTrainDirections(Track.WEST + Track.EAST);
		loc1trk1.setLength(900);

		Track loc1trk2;
		loc1trk2 = loc1.addTrack("Westford Yard 2", Track.YARD);
		loc1trk2.setTrainDirections(Track.WEST + Track.EAST);
		loc1trk2.setLength(500);
		loc1trk2.deleteTypeName("Coil Car");
		
		Track loc1trk3;
		loc1trk3 = loc1.addTrack("Westford Express 3", Track.SIDING);
		loc1trk3.setTrainDirections(Track.WEST + Track.EAST);	
		loc1trk3.setLength(300);
		loc1trk3.deleteTypeName("Gon");
		loc1trk3.deleteTypeName("Coil Car");
		
		Track loc1trk4;
		loc1trk4 = loc1.addTrack("Westford Express 4", Track.SIDING);
		loc1trk4.setTrainDirections(Track.WEST + Track.EAST);
		loc1trk4.setLength(300);
		loc1trk4.deleteTypeName("Gon");
		loc1trk4.deleteTypeName("Coil Car");
		
		Track loc2trk1;
		loc2trk1 = loc2.addTrack("Chelmsford Freight 1", Track.SIDING);
		loc2trk1.setTrainDirections(Track.WEST + Track.EAST);
		loc2trk1.setLength(900);
		loc2trk1.deleteTypeName("Coil Car");
		loc2trk1.setScheduleId(sch1.getId());
		// start the schedule with 2nd item Flat Car
		loc2trk1.setScheduleItemId(sch1.getItemsBySequenceList().get(1));
		
		Track loc2trk2;
		loc2trk2 = loc2.addTrack("Chelmsford Freight 2", Track.SIDING);
		loc2trk2.setTrainDirections(Track.WEST + Track.EAST);
		loc2trk2.setLength(900);
		loc2trk2.deleteTypeName("Coil Car");
		loc2trk2.setScheduleId(sch1.getId());
		// start the schedule with 3rd item Gon
		loc2trk2.setScheduleItemId(sch1.getItemsBySequenceList().get(2));
		
		Track loc2trk3;
		loc2trk3 = loc2.addTrack("Chelmsford Yard 3", Track.YARD);
		loc2trk3.setTrainDirections(Track.WEST + Track.EAST);
		loc2trk3.setLength(900);
		loc2trk3.deleteTypeName("Gon");
		loc2trk3.deleteTypeName("Coil Car");
		
		Track loc2trk4;
		loc2trk4 = loc2.addTrack("Chelmsford Freight 4", Track.SIDING);
		loc2trk4.setTrainDirections(Track.WEST + Track.EAST);
		loc2trk4.setLength(900);
		loc2trk4.setScheduleId(sch2.getId());
		
		Track loc3trk1;
		loc3trk1 = loc3.addTrack("Bedford Yard 1", Track.STAGING);
		loc3trk1.setTrainDirections(Track.WEST + Track.EAST);
		loc3trk1.setLength(900);
		loc3trk1.setRemoveLoadsEnabled(true);
		
		// Create route with 2 location
		Route rte1;
		rte1 = rmanager.newRoute("Two Location Route");
		RouteLocation rl1 = rte1.addLocation(loc1);
		rl1.setTrainDirection(RouteLocation.EAST);
		rl1.setMaxCarMoves(12);
		rl1.setTrainIconX(25);	// set the train icon coordinates
		rl1.setTrainIconY(75);

		RouteLocation rl2 = rte1.addLocation(loc2);
		rl2.setTrainDirection(RouteLocation.EAST);
		rl2.setMaxCarMoves(12);
		rl2.setTrainIconX(75);	// set the train icon coordinates
		rl2.setTrainIconY(75);

		
		// Create train
		Train train1;
		train1 = tmanager.newTrain("NWNC");
		train1.setRoute(rte1);
		
		// Set up 13 cars
		Car c1 = new Car("BM", "S1");
		c1.setType("Gon");
		c1.setLength("90");
		c1.setMoves(13);
		c1.setLoad("L");
		cmanager.register(c1);

		Car c2 = new Car("UP", "S2");
		c2.setType("Boxcar");
		c2.setLength("80");
		c2.setMoves(12);		
		cmanager.register(c2);

		Car c3 = new Car("XP", "S3");
		c3.setType("Flat Car");
		c3.setLength("70");
		c3.setMoves(0);	
		c3.setLoad("L");
		c3.setDestination(loc2, null);	// force this car to Chelmsford
		cmanager.register(c3);

		Car c4 = new Car("PU", "S4");
		c4.setType("Boxcar");
		c4.setLength("60");
		c4.setMoves(10);			
		cmanager.register(c4);

		// place two cars in a kernel
		Kernel k1 = cmanager.newKernel("TwoCars");
				
		Car c5 = new Car("UP", "S5");
		c5.setType("Gon");
		c5.setLength("50");
		c5.setMoves(9);	
		c5.setLoad("L");
		c5.setKernel(k1);
		cmanager.register(c5);

		Car c6 = new Car("CP", "S6");
		c6.setType("Boxcar");
		c6.setLength("40");
		c6.setMoves(8);	
		c6.setLoad("L");
		cmanager.register(c6);
		
		Car c7 = new Car("UP", "S7");
		c7.setType("Boxcar");
		c7.setLength("50");
		c7.setMoves(7);	
		cmanager.register(c7);
		
		Car c8 = new Car("XP", "S8");
		c8.setType("Gon");
		c8.setLength("60");
		c8.setMoves(6);			
		cmanager.register(c8);
		
		Car c9 = new Car("XP", "S9");
		c9.setType("Flat Car");
		c9.setLength("90");
		c9.setMoves(5);
		c9.setLoad("E");
		cmanager.register(c9);
		
		Car c10 = new Car("CP", "S10");
		c10.setType("Coil Car");
		c10.setLength("40");
		c10.setMoves(2);
		c10.setLoad("L");
		cmanager.register(c10);
		
		Car c11 = new Car("CP", "S11");
		c11.setType("Coil Car");
		c11.setLength("40");
		c11.setMoves(3);
		c11.setLoad("Coils");
		cmanager.register(c11);
		
		Car c12 = new Car("CP", "S12");
		c12.setType("Coil Car");
		c12.setLength("40");
		c12.setMoves(4);
		cmanager.register(c12);
		
		// place car in kernel with c5
		Car c13 = new Car("UP", "S13");
		c13.setType("Gon");
		c13.setLength("50");
		c13.setMoves(1);	
		c13.setLoad("L");
		c13.setKernel(k1);
		cmanager.register(c13);
		
		// place the cars in the yards
		Assert.assertEquals("Place c1", Track.OKAY, c1.setLocation(loc1, loc1trk1));
		Assert.assertEquals("Place c2", Track.OKAY, c2.setLocation(loc1, loc1trk1));
		Assert.assertEquals("Place c3", Track.OKAY, c3.setLocation(loc1, loc1trk1));
		Assert.assertEquals("Place c4", Track.OKAY, c4.setLocation(loc1, loc1trk1));
		
		Assert.assertEquals("Place c5", Track.OKAY, c5.setLocation(loc1, loc1trk2));
		Assert.assertEquals("Place c6", Track.OKAY, c6.setLocation(loc1, loc1trk2));
		Assert.assertEquals("Place c7", Track.OKAY, c7.setLocation(loc1, loc1trk2));
		Assert.assertEquals("Place c8", Track.OKAY, c8.setLocation(loc1, loc1trk2));	
		Assert.assertEquals("Place c9", Track.OKAY, c9.setLocation(loc1, loc1trk2));
		
		Assert.assertEquals("Place c10", Track.OKAY, c10.setLocation(loc1, loc1trk1));
		Assert.assertEquals("Place c11", Track.OKAY, c11.setLocation(loc1, loc1trk1));
		Assert.assertEquals("Place c12", Track.OKAY, c12.setLocation(loc1, loc1trk1));
		Assert.assertEquals("Place c13", Track.OKAY, c13.setLocation(loc1, loc1trk2));
	
		train1.build();
		
		// Schedule sch1 should cause c2 to be delivered to Chelmsford Freight 2
		Assert.assertEquals("c2 destination", "Chelmsford Freight 2", c2.getDestinationTrackName());
		Assert.assertEquals("c2 next load", "", c2.getNextLoad());
		// Schedule sch1 and sch2 should reject c3, to be delivered to Chelmsford Yard 3
		Assert.assertEquals("c3 destination", "Chelmsford Yard 3", c3.getDestinationTrackName());
		Assert.assertEquals("c3 next load", "", c3.getNextLoad());
		Assert.assertEquals("c4 destination", "Chelmsford Yard 3", c4.getDestinationTrackName());
		// Schedule sch1 should cause c5 & c13 to be delivered to Chelmsford Freight 2
		Assert.assertEquals("c5 destination", "Chelmsford Freight 2", c5.getDestinationTrackName());
		Assert.assertEquals("c5 next load", "Tin", c5.getNextLoad());
		Assert.assertEquals("c6 destination", "Chelmsford Yard 3", c6.getDestinationTrackName());
		Assert.assertEquals("c7 destination", "Chelmsford Freight 4", c7.getDestinationTrackName());
		Assert.assertEquals("c9 destination", "Chelmsford Freight 1", c9.getDestinationTrackName());
		Assert.assertEquals("c9 next load", "Scrap", c9.getNextLoad());
		Assert.assertEquals("c10 destination", "Chelmsford Freight 4", c10.getDestinationTrackName());
		Assert.assertEquals("c11 destination", "Chelmsford Freight 4", c11.getDestinationTrackName());
		// C13 is part of kernel, load will flip between E and L
		Assert.assertEquals("c13 destination", "Chelmsford Freight 2", c13.getDestinationTrackName());
		Assert.assertEquals("c13 next load", "Tin", c13.getNextLoad());

		// move and terminate train	
		train1.move();
		train1.move();
		train1.move();
	
		Assert.assertEquals("c1 track", "Chelmsford Freight 1", c1.getTrackName());
		Assert.assertEquals("c1 load", "Tin", c1.getLoad());
		Assert.assertEquals("c2 track", "Chelmsford Freight 2", c2.getTrackName());
		Assert.assertEquals("c2 load", "L", c2.getLoad());
		Assert.assertEquals("c3 track", "Chelmsford Yard 3", c3.getTrackName());
		Assert.assertEquals("c3 load", "L", c3.getLoad());
		Assert.assertEquals("c4 track", "Chelmsford Yard 3", c4.getTrackName());
		Assert.assertEquals("c4 load", "E", c4.getLoad());
		Assert.assertEquals("c5 track", "Chelmsford Freight 2", c5.getTrackName());
		Assert.assertEquals("c5 load", "Tin", c5.getLoad());
		Assert.assertEquals("c6 track", "Chelmsford Yard 3", c6.getTrackName());
		Assert.assertEquals("c6 load", "L", c6.getLoad());
		Assert.assertEquals("c7 track", "Chelmsford Freight 4", c7.getTrackName());
		Assert.assertEquals("c7 load", "L", c7.getLoad());
		Assert.assertEquals("c8 track", "Westford Yard 2", c8.getTrackName());
		Assert.assertEquals("c8 load", "E", c8.getLoad());
		Assert.assertEquals("c9 track", "Chelmsford Freight 1", c9.getTrackName());
		Assert.assertEquals("c9 load", "Scrap", c9.getLoad());
		Assert.assertEquals("c10 track", "Chelmsford Freight 4", c10.getTrackName());
		Assert.assertEquals("c10 load", "E", c10.getLoad());
		Assert.assertEquals("c11 track", "Chelmsford Freight 4", c11.getTrackName());
		Assert.assertEquals("c11 load", "E", c11.getLoad());
		Assert.assertEquals("c12 track", "Westford Yard 1", c12.getTrackName());
		Assert.assertEquals("c12 load", "E", c12.getLoad());
		Assert.assertEquals("c13 track", "Chelmsford Freight 2", c13.getTrackName());
		Assert.assertEquals("c13 load", "Tin", c13.getLoad());
	
		// create a route to staging to test remove schedule load
		// Create route with 2 location
		Route rte2;
		rte2 = rmanager.newRoute("Chelmsford to Staging");
		RouteLocation r2rl1 = rte2.addLocation(loc2);
		r2rl1.setTrainDirection(RouteLocation.EAST);
		r2rl1.setMaxCarMoves(12);
		r2rl1.setTrainIconX(125);	// set the train icon coordinates
		r2rl1.setTrainIconY(75);
		RouteLocation r2rl3 = rte2.addLocation(loc3);
		r2rl3.setTrainDirection(RouteLocation.EAST);
		r2rl3.setMaxCarMoves(12);
		r2rl3.setTrainIconX(175);	// set the train icon coordinates
		r2rl3.setTrainIconY(75);

		
		train1.setRoute(rte2);
		train1.setName("Chelmsford to Bedford");
		train1.build();
		
		// move and terminate train	
		train1.move();
		train1.move();
		train1.move();
		
		Assert.assertEquals("c1 track to staging", "Bedford Yard 1", c1.getTrackName());
		Assert.assertEquals("c1 load to staging", "E", c1.getLoad());
		Assert.assertEquals("c2 track to staging", "Bedford Yard 1", c2.getTrackName());
		Assert.assertEquals("c2 load to staging", "L", c2.getLoad());
		Assert.assertEquals("c3 track to staging", "Bedford Yard 1", c3.getTrackName());
		Assert.assertEquals("c3 load to staging", "L", c3.getLoad());
		Assert.assertEquals("c4 track to staging", "Bedford Yard 1", c4.getTrackName());
		Assert.assertEquals("c4 load to staging", "E", c4.getLoad());
		Assert.assertEquals("c5 track to staging", "Bedford Yard 1", c5.getTrackName());
		Assert.assertEquals("c5 load to staging", "E", c5.getLoad());
		Assert.assertEquals("c6 track to staging", "Bedford Yard 1", c6.getTrackName());
		Assert.assertEquals("c6 load to staging", "L", c6.getLoad());
		Assert.assertEquals("c7 track to staging", "Bedford Yard 1", c7.getTrackName());
		Assert.assertEquals("c7 load to staging", "L", c7.getLoad());
		Assert.assertEquals("c8 track to staging", "Westford Yard 2", c8.getTrackName());
		Assert.assertEquals("c8 load to staging", "E", c8.getLoad());
		Assert.assertEquals("c9 track to staging", "Bedford Yard 1", c9.getTrackName());
		Assert.assertEquals("c9 load to staging", "E", c9.getLoad());
		Assert.assertEquals("c10 track to staging", "Bedford Yard 1", c10.getTrackName());
		Assert.assertEquals("c10 load to staging", "E", c10.getLoad());
		Assert.assertEquals("c11 track to staging", "Bedford Yard 1", c11.getTrackName());
		Assert.assertEquals("c11 load to staging", "E", c11.getLoad());
		Assert.assertEquals("c12 track to staging", "Westford Yard 1", c12.getTrackName());
		Assert.assertEquals("c12 load to staging", "E", c12.getLoad());
		Assert.assertEquals("c13 track to staging", "Bedford Yard 1", c13.getTrackName());
		Assert.assertEquals("c13 load to staging", "E", c13.getLoad());

		
		// create a route from staging to test generate schedule load
		// Create route with 3 locations
		Route rte3;
		rte3 = rmanager.newRoute("Staging to Chelmsford");
		RouteLocation r3rl1 = rte3.addLocation(loc3);
		r3rl1.setTrainDirection(RouteLocation.EAST);
		r3rl1.setMaxCarMoves(0);		// all cars must move from staging
		r3rl1.setTrainIconX(25);	// set the train icon coordinates
		r3rl1.setTrainIconY(100);
		RouteLocation r3rl2 = rte3.addLocation(loc2);
		r3rl2.setTrainDirection(RouteLocation.EAST);
		r3rl2.setMaxCarMoves(12);
		RouteLocation r3rl3 = rte3.addLocation(loc1);
		r3rl3.setTrainDirection(RouteLocation.EAST);
		r3rl3.setMaxCarMoves(12);
		r3rl3.setTrainIconX(75);	// set the train icon coordinates
		r3rl3.setTrainIconY(100);
		
		loc3trk1.setRemoveLoadsEnabled(false);
		loc3trk1.setAddLoadsEnabled(true);		// generate schedule loads
		
		sch1Item1.setLoad("Metal 1");		// request these loads from staging
		sch1Item2.setLoad("Metal 2");
		sch1Item3.setLoad("Metal 3");
		
		train1.setRoute(rte3);
		train1.setName("BCW");
		train1.build();
		
		Assert.assertEquals("Train Bedford Chelmsford Westford build status", true, train1.isBuilt());
		Assert.assertEquals("c1 load from staging", "E", c1.getLoad());
		Assert.assertEquals("c2 load from staging", "L", c2.getLoad());
		Assert.assertEquals("c3 load from staging", "L", c3.getLoad());
		Assert.assertEquals("c4 load from staging", "E", c4.getLoad());
		Assert.assertEquals("c5 load from staging", "Metal 3", c5.getLoad());
		Assert.assertEquals("c6 load from staging", "L", c6.getLoad());
		Assert.assertEquals("c7 load from staging", "L", c7.getLoad());
		Assert.assertEquals("c8 load from staging", "E", c8.getLoad());
		Assert.assertEquals("c9 load from staging", "Metal 2", c9.getLoad());
		Assert.assertEquals("c9 next load from staging", "Scrap", c9.getNextLoad());
		Assert.assertEquals("c10 load from staging", "E", c10.getLoad());
		Assert.assertEquals("c11 load from staging", "E", c11.getLoad());
		Assert.assertEquals("c13 load from staging", "Metal 3", c13.getLoad());
		
		// move and terminate train	
		train1.move();
		train1.move();
		train1.move();
		train1.move();
		
		Assert.assertEquals("c1 track from staging terminated", "Westford Yard 1", c1.getTrackName());
		Assert.assertEquals("c1 load from staging terminated", "E", c1.getLoad());
		Assert.assertEquals("c2 track from staging terminated", "Westford Yard 2", c2.getTrackName());
		Assert.assertEquals("c2 load from staging terminated", "L", c2.getLoad());
		Assert.assertEquals("c3 track from staging terminated", "Westford Yard 1", c3.getTrackName());
		Assert.assertEquals("c3 load from staging terminated", "L", c3.getLoad());
		Assert.assertEquals("c4 track from staging terminated", "Westford Express 4", c4.getTrackName());
		Assert.assertEquals("c4 load from staging terminated", "L", c4.getLoad());
		Assert.assertEquals("c5 track from staging terminated", "Chelmsford Freight 2", c5.getTrackName());
		Assert.assertEquals("c5 load from staging terminated", "Tin", c5.getLoad());
		Assert.assertEquals("c6 track from staging terminated", "Westford Express 3", c6.getTrackName());
		Assert.assertEquals("c6 load from staging terminated", "E", c6.getLoad());
		Assert.assertEquals("c7 track from staging terminated", "Westford Yard 2", c7.getTrackName());
		Assert.assertEquals("c7 load from staging terminated", "L", c7.getLoad());
		Assert.assertEquals("c8 track from staging terminated", "Westford Yard 2", c8.getTrackName());
		Assert.assertEquals("c8 load from staging terminated", "E", c8.getLoad());
		Assert.assertEquals("c9 track from staging terminated", "Chelmsford Freight 2", c9.getTrackName());
		Assert.assertEquals("c9 load from staging terminated", "Scrap", c9.getLoad());
		Assert.assertEquals("c10 track from staging terminated", "Chelmsford Freight 4", c10.getTrackName());
		Assert.assertEquals("c10 load from staging terminated", "L", c10.getLoad());
		Assert.assertEquals("c11 track from staging terminated", "Westford Yard 1", c11.getTrackName());
		Assert.assertEquals("c11 load from staging terminated", "E", c11.getLoad());
		Assert.assertEquals("c12 track from staging terminated", "Westford Yard 1", c12.getTrackName());
		Assert.assertEquals("c12 load from staging terminated", "E", c12.getLoad());
		Assert.assertEquals("c13 track from staging terminated", "Chelmsford Freight 2", c13.getTrackName());
		Assert.assertEquals("c13 load from staging terminated", "Tin", c13.getLoad());

	}
	
	public void testInterchange(){
		TrainManager tmanager = TrainManager.instance();
		RouteManager rmanager = RouteManager.instance();
		LocationManager lmanager = LocationManager.instance();
		CarManager cmanager = CarManager.instance();
		CarTypes ct = CarTypes.instance();
		
		Setup.setTrainLength(500);
		ct.addName("Gon");
		ct.addName("Coil Car");	
		ct.addName("XCaboose");	
		
		// Create locations used
		Location loc1;
		loc1 = lmanager.newLocation("Old Westford");
		loc1.setTrainDirections(DIRECTION_ALL);	
		
		Location loc2;
		loc2 = lmanager.newLocation("Old Chelmsford");
		loc2.setTrainDirections(DIRECTION_ALL);	
		
		Location loc3;
		loc3 = lmanager.newLocation("Old Bedford");
		loc3.setTrainDirections(DIRECTION_ALL);	
		
		Track loc1trk1;
		loc1trk1 = loc1.addTrack("Westford Yard 1", Track.YARD);
		loc1trk1.setTrainDirections(Track.WEST + Track.EAST);
		loc1trk1.setLength(900);
		
		Track loc1trk2;
		loc1trk2 = loc1.addTrack("Westford Yard 2", Track.YARD);
		loc1trk2.setTrainDirections(Track.WEST + Track.EAST);
		loc1trk2.setLength(500);
		loc1trk2.deleteTypeName("Coil Car");
		loc1trk2.deleteTypeName("XCaboose");
		
		Track loc2trk1;
		loc2trk1 = loc2.addTrack("Chelmsford Interchange 1", Track.INTERCHANGE);
		loc2trk1.setTrainDirections(Track.WEST + Track.EAST);
		loc2trk1.setLength(900);
		loc2trk1.deleteTypeName("Coil Car");
		loc2trk1.deleteTypeName("XCaboose");

		Track loc2trk2;
		loc2trk2 = loc2.addTrack("Chelmsford Interchange 2", Track.INTERCHANGE);
		loc2trk2.setTrainDirections(Track.WEST + Track.EAST);
		loc2trk2.setLength(900);
		loc2trk2.deleteTypeName("XCaboose");
		
		Track loc2trk3;
		loc2trk3 = loc2.addTrack("Chelmsford Yard 3", Track.YARD);
		loc2trk3.setTrainDirections(Track.WEST + Track.EAST);
		loc2trk3.setLength(900);
		loc2trk3.deleteTypeName("Gon");
		loc2trk3.deleteTypeName("Coil Car");
		loc2trk3.deleteTypeName("XCaboose");
		
		Track loc2trk4;
		loc2trk4 = loc2.addTrack("Chelmsford Freight 4", Track.SIDING);
		loc2trk4.setTrainDirections(Track.WEST + Track.EAST);
		loc2trk4.setLength(900);
		loc2trk4.deleteTypeName("Gon");
		loc2trk4.deleteTypeName("XCaboose");
		
		loc2trk3.setMoves(20);	// bias interchange tracks
		loc2trk4.setMoves(20);
		
		Track loc3trk1;
		loc3trk1 = loc3.addTrack("Bedford Yard 1", Track.YARD);
		loc3trk1.setTrainDirections(Track.WEST + Track.EAST);
		loc3trk1.setLength(900);
		
		// Create route with 3 location
		Route rte1;
		rte1 = rmanager.newRoute("Route 1 East");
		RouteLocation r1l1 = rte1.addLocation(loc1);
		r1l1.setTrainDirection(RouteLocation.EAST);
		r1l1.setMaxCarMoves(4);
		r1l1.setTrainIconX(125);	// set the train icon coordinates
		r1l1.setTrainIconY(100);
		RouteLocation r1l2 = rte1.addLocation(loc2);
		r1l2.setTrainDirection(RouteLocation.EAST);
		r1l2.setMaxCarMoves(3);
		r1l2.setTrainIconX(25);	// set the train icon coordinates
		r1l2.setTrainIconY(125);
		RouteLocation r1l3 = rte1.addLocation(loc3);
		r1l3.setTrainDirection(RouteLocation.EAST);
		r1l3.setMaxCarMoves(3);
		r1l3.setTrainIconX(75);	// set the train icon coordinates
		r1l3.setTrainIconY(125);
		
		// Create route with 3 location
		Route rte2;
		rte2 = rmanager.newRoute("Route 2 East");
		RouteLocation r2l1 = rte2.addLocation(loc1);
		r2l1.setTrainDirection(RouteLocation.EAST);
		r2l1.setMaxCarMoves(2);
		r2l1.setTrainIconX(125);	// set the train icon coordinates
		r2l1.setTrainIconY(125);
		RouteLocation r2l2 = rte2.addLocation(loc2);
		r2l2.setTrainDirection(RouteLocation.EAST);
		r2l2.setMaxCarMoves(6);
		r2l2.setTrainIconX(175);	// set the train icon coordinates
		r2l2.setTrainIconY(125);
		RouteLocation r2l3 = rte2.addLocation(loc3);
		r2l3.setTrainDirection(RouteLocation.EAST);
		r2l3.setMaxCarMoves(6);
		r2l3.setTrainIconX(25);	// set the train icon coordinates
		r2l3.setTrainIconY(150);
		
		// Create trains
		Train train1;
		train1 = tmanager.newTrain("T1OWOB");
		train1.setRoute(rte1);
		
		Train train2;
		train2 = tmanager.newTrain("T2OWOB");
		train2.setRoute(rte1);
		
		Train train3;
		train3 = tmanager.newTrain("T3OWOB");
		train3.setRoute(rte1);
		
		// Set up 7 box cars and 2 flat cars
		Car c1 = new Car("BM", "Q1");
		c1.setType("Gon");
		c1.setLength("90");
		c1.setMoves(19);
		c1.setLoad("L");
		c1.setWeightTons("10");
		cmanager.register(c1);

		Car c2 = new Car("UP", "Q2");
		c2.setType("Boxcar");
		c2.setLength("80");
		c2.setMoves(18);	
		c2.setWeightTons("20");
		cmanager.register(c2);

		Car c3 = new Car("XP", "Q3");
		c3.setType("Flat Car");
		c3.setLength("70");
		c3.setMoves(17);	
		c3.setWeightTons("30");
		cmanager.register(c3);

		Car c4 = new Car("PU", "Q4");
		c4.setType("Boxcar");
		c4.setLength("60");
		c4.setMoves(16);	
		c4.setWeightTons("40");
		cmanager.register(c4);

		Car c5 = new Car("UP", "Q5");
		c5.setType("Gon");
		c5.setLength("50");
		c5.setMoves(15);	
		c5.setLoad("L");
		c5.setWeightTons("50");
		cmanager.register(c5);

		Car c6 = new Car("CP", "Q6");
		c6.setType("Boxcar");
		c6.setLength("40");
		c6.setMoves(14);	
		c6.setLoad("L");
		c6.setWeightTons("60");
		cmanager.register(c6);
		
		Car c7 = new Car("UP", "Q7");
		c7.setType("Boxcar");
		c7.setLength("50");
		c7.setMoves(13);
		c7.setWeightTons("70");
		cmanager.register(c7);
		
		Car c8 = new Car("XP", "Q8");
		c8.setType("Gon");
		c8.setLength("60");
		c8.setMoves(12);
		c8.setWeightTons("80");
		cmanager.register(c8);
		
		Car c9 = new Car("XP", "Q9");
		c9.setType("Flat Car");
		c9.setLength("90");
		c9.setMoves(11);
		c9.setLoad("L");
		c9.setWeightTons("90");
		cmanager.register(c9);
		
		Car c10 = new Car("CP", "Q10");
		c10.setType("Coil Car");
		c10.setLength("40");
		c10.setMoves(8);
		c10.setLoad("L");
		c10.setWeightTons("100");
		cmanager.register(c10);
		
		Car c11 = new Car("CP", "Q11");
		c11.setType("Coil Car");
		c11.setLength("40");
		c11.setMoves(9);
		c11.setLoad("Coils");
		c11.setWeightTons("110");
		cmanager.register(c11);
		
		Car c12 = new Car("CP", "Q12");
		c12.setType("Coil Car");
		c12.setLength("40");
		c12.setMoves(10);
		c12.setWeightTons("120");
		cmanager.register(c12);
		
		Car c13 = new Car("CP", "Q13");
		c13.setType("XCaboose");
		c13.setCaboose(true);
		c13.setLength("40");
		c13.setMoves(7);
		c13.setWeightTons("130");
		cmanager.register(c13);
		
		// place the cars in the yards
		Assert.assertEquals("Place c1", Track.OKAY, c1.setLocation(loc1, loc1trk1));
		Assert.assertEquals("Place c2", Track.OKAY, c2.setLocation(loc1, loc1trk1));
		Assert.assertEquals("Place c3", Track.OKAY, c3.setLocation(loc1, loc1trk1));
		Assert.assertEquals("Place c4", Track.OKAY, c4.setLocation(loc1, loc1trk1));
		
		Assert.assertEquals("Place c5", Track.OKAY, c5.setLocation(loc1, loc1trk2));
		Assert.assertEquals("Place c6", Track.OKAY, c6.setLocation(loc1, loc1trk2));
		Assert.assertEquals("Place c7", Track.OKAY, c7.setLocation(loc1, loc1trk2));
		Assert.assertEquals("Place c8", Track.OKAY, c8.setLocation(loc1, loc1trk2));	
		Assert.assertEquals("Place c9", Track.OKAY, c9.setLocation(loc1, loc1trk2));
		
		Assert.assertEquals("Place c10", Track.OKAY, c10.setLocation(loc1, loc1trk1));
		Assert.assertEquals("Place c11", Track.OKAY, c11.setLocation(loc1, loc1trk1));
		Assert.assertEquals("Place c12", Track.OKAY, c12.setLocation(loc1, loc1trk1));
		Assert.assertEquals("Place c13", Track.OKAY, c13.setLocation(loc1, loc1trk1));
		
		train1.build();
		train2.build();
		
		// now check to where cars are going to be delivered
		Assert.assertEquals("c9 destination", "Chelmsford Interchange 1", c9.getDestinationTrackName());
		Assert.assertEquals("c10 destination", "Bedford Yard 1", c10.getDestinationTrackName());
		Assert.assertEquals("c11 destination", "Chelmsford Interchange 2", c11.getDestinationTrackName());
		Assert.assertEquals("c12 destination", "Bedford Yard 1", c12.getDestinationTrackName());
		
		Assert.assertEquals("c5 destination", "Chelmsford Interchange 2", c5.getDestinationTrackName());
		Assert.assertEquals("c6 destination", "Bedford Yard 1", c6.getDestinationTrackName());
		Assert.assertEquals("c7 destination", "Chelmsford Interchange 1", c7.getDestinationTrackName());
		Assert.assertEquals("c8 destination", "Bedford Yard 1", c8.getDestinationTrackName());
		
		// now check which trains
		Assert.assertEquals("c9 train", train1, c9.getTrain());
		Assert.assertEquals("c10 train", train1, c10.getTrain());
		Assert.assertEquals("c11 train", train1, c11.getTrain());
		Assert.assertEquals("c12 train", train1, c12.getTrain());
		
		Assert.assertEquals("c5 train", train2, c5.getTrain());
		Assert.assertEquals("c6 train", train2, c6.getTrain());
		Assert.assertEquals("c7 train", train2, c7.getTrain());
		Assert.assertEquals("c8 train", train2, c8.getTrain());
		
		// try restricting interchange 1 to train1 and interchange 2 to train2
		
		loc2trk1.setDropOption(Track.TRAINS);
		loc2trk1.addDropId(train1.getId());
		loc2trk2.setDropOption(Track.TRAINS);
		loc2trk2.addDropId(train2.getId());
		
		train1.build();
		train2.build();
		
		// now check to where cars are going to be delivered
		Assert.assertEquals("c9 destination 2", "Chelmsford Interchange 1", c9.getDestinationTrackName());
		Assert.assertEquals("c10 destination 2", "Bedford Yard 1", c10.getDestinationTrackName());
		Assert.assertEquals("c11 destination 2", "Bedford Yard 1", c11.getDestinationTrackName());
		Assert.assertEquals("c12 destination 2", "Chelmsford Freight 4", c12.getDestinationTrackName());

		Assert.assertEquals("c4 destination 2", "Chelmsford Interchange 2", c4.getDestinationTrackName());
		Assert.assertEquals("c6 destination 2", "Bedford Yard 1", c6.getDestinationTrackName());
		Assert.assertEquals("c7 destination 2", "Chelmsford Interchange 2", c7.getDestinationTrackName());
		Assert.assertEquals("c8 destination 2", "Bedford Yard 1", c8.getDestinationTrackName());

		// now check which trains
		Assert.assertEquals("c9 train", train1, c9.getTrain());
		Assert.assertEquals("c10 train", train1, c10.getTrain());
		Assert.assertEquals("c11 train", train1, c11.getTrain());
		Assert.assertEquals("c12 train", train1, c12.getTrain());
		
		Assert.assertEquals("c4 train", train2, c4.getTrain());
		Assert.assertEquals("c6 train", train2, c6.getTrain());
		Assert.assertEquals("c7 train", train2, c7.getTrain());
		Assert.assertEquals("c8 train", train2, c8.getTrain());

		// move and terminate
		Assert.assertEquals("Check train 1 departure location name", "Old Westford", train1.getCurrentLocationName());
		Assert.assertEquals("Check train 1 departure location", r1l1, train1.getCurrentLocation());
		train1.move();	//#1
		Assert.assertEquals("Check train 1 location name", "Old Chelmsford", train1.getCurrentLocationName());
		Assert.assertEquals("Check train 1 location", r1l2, train1.getCurrentLocation());
		train1.move();	//#2
		Assert.assertEquals("Check train 1 location name", "Old Bedford", train1.getCurrentLocationName());
		Assert.assertEquals("Check train 1 location", r1l3, train1.getCurrentLocation());
		train1.move();	//#3 terminate
		Assert.assertEquals("Check train 1 location name", "", train1.getCurrentLocationName());
		Assert.assertEquals("Check train 1 location", null, train1.getCurrentLocation());

		Assert.assertEquals("Check train 2 departure location name", "Old Westford", train2.getCurrentLocationName());
		Assert.assertEquals("Check train 2 departure location", r1l1, train2.getCurrentLocation());
		train2.move();	//#1
		Assert.assertEquals("Check train 2 location name", "Old Chelmsford", train2.getCurrentLocationName());
		Assert.assertEquals("Check train 2 location", r1l2, train2.getCurrentLocation());
		train2.move();	//#2
		Assert.assertEquals("Check train 2 location name", "Old Bedford", train2.getCurrentLocationName());
		Assert.assertEquals("Check train 2 location", r1l3, train2.getCurrentLocation());
		train2.move();	//#3 terminate
		Assert.assertEquals("Check train 2 location name", "", train2.getCurrentLocationName());
		Assert.assertEquals("Check train 2 location", null, train2.getCurrentLocation());
		
		r1l1.setMaxCarMoves(2);
		r1l2.setMaxCarMoves(6);
		r1l3.setMaxCarMoves(6);
		train3.build();	// note that train3 uses rte1, should not pickup cars at interchange
		
		Assert.assertEquals("c1 destination 3", "", c1.getDestinationTrackName());
		Assert.assertEquals("c2 destination 3", "", c2.getDestinationTrackName());
		Assert.assertEquals("c3 destination 3", "Chelmsford Yard 3", c3.getDestinationTrackName());
		Assert.assertEquals("c4 destination 3", "", c4.getDestinationTrackName());
		Assert.assertEquals("c5 destination 3", "Bedford Yard 1", c5.getDestinationTrackName());
		Assert.assertEquals("c6 destination 3", "", c6.getDestinationTrackName());
		Assert.assertEquals("c7 destination 3", "", c7.getDestinationTrackName());
		Assert.assertEquals("c8 destination 3", "", c8.getDestinationTrackName());
		Assert.assertEquals("c9 destination 3", "", c9.getDestinationTrackName());
		Assert.assertEquals("c12 destination 3", "Bedford Yard 1", c12.getDestinationTrackName());
		
		// Change the route to 2, should be able to pickup c4, c7, c9
		train3.reset();
		train3.setRoute(rte2);
		train3.build();
		
		Assert.assertEquals("c1 destination 4", "", c1.getDestinationTrackName());
		Assert.assertEquals("c2 destination 4", "Chelmsford Yard 3", c2.getDestinationTrackName());
		Assert.assertEquals("c3 destination 4", "", c3.getDestinationTrackName());
		Assert.assertEquals("c4 destination 4", "Bedford Yard 1", c4.getDestinationTrackName());
		Assert.assertEquals("c5 destination 4", "Bedford Yard 1", c5.getDestinationTrackName());
		Assert.assertEquals("c6 destination 4", "", c6.getDestinationTrackName());
		Assert.assertEquals("c7 destination 4", "Bedford Yard 1", c7.getDestinationTrackName());
		Assert.assertEquals("c8 destination 4", "", c8.getDestinationTrackName());
		Assert.assertEquals("c9 destination 4", "Bedford Yard 1", c9.getDestinationTrackName());
		Assert.assertEquals("c12 destination 4", "Bedford Yard 1", c12.getDestinationTrackName());

		// Change back to route to 1, should be able to pickup c4, c7
		train3.reset();
		train3.setRoute(rte1);
		loc2trk2.setPickupOption(Track.TRAINS);
		loc2trk2.addPickupId(train3.getId());
		train3.build();
		
		Assert.assertEquals("c1 destination 5", "", c1.getDestinationTrackName());
		Assert.assertEquals("c2 destination 5", "", c2.getDestinationTrackName());
		Assert.assertEquals("c3 destination 5", "Chelmsford Freight 4", c3.getDestinationTrackName());
		Assert.assertEquals("c4 destination 5", "Bedford Yard 1", c4.getDestinationTrackName());
		Assert.assertEquals("c5 destination 5", "Bedford Yard 1", c5.getDestinationTrackName());
		Assert.assertEquals("c6 destination 5", "", c6.getDestinationTrackName());
		Assert.assertEquals("c7 destination 5", "Bedford Yard 1", c7.getDestinationTrackName());
		Assert.assertEquals("c8 destination 5", "", c8.getDestinationTrackName());
		Assert.assertEquals("c9 destination 5", "", c9.getDestinationTrackName());
		Assert.assertEquals("c12 destination 5", "Bedford Yard 1", c12.getDestinationTrackName());

		// Change back to route to 1, should be able to pickup c4, c7, and c9
		train3.reset();
		train3.setRoute(rte1);
		loc2trk1.setPickupOption(Track.ROUTES);
		loc2trk1.addPickupId(rte1.getId());
		train3.build();
		
		Assert.assertEquals("c1 destination 6", "Bedford Yard 1", c1.getDestinationTrackName());
		Assert.assertEquals("c2 destination 6", "", c2.getDestinationTrackName());
		Assert.assertEquals("c3 destination 6", "", c3.getDestinationTrackName());
		Assert.assertEquals("c4 destination 6", "Bedford Yard 1", c4.getDestinationTrackName());
		Assert.assertEquals("c5 destination 6", "Bedford Yard 1", c5.getDestinationTrackName());
		Assert.assertEquals("c6 destination 6", "", c6.getDestinationTrackName());
		Assert.assertEquals("c7 destination 6", "Bedford Yard 1", c7.getDestinationTrackName());
		Assert.assertEquals("c8 destination 6", "", c8.getDestinationTrackName());
		Assert.assertEquals("c9 destination 6", "Bedford Yard 1", c9.getDestinationTrackName());
		Assert.assertEquals("c10 destination 6", "", c10.getDestinationTrackName());
		Assert.assertEquals("c11 destination 6", "", c11.getDestinationTrackName());
		Assert.assertEquals("c12 destination 6", "Bedford Yard 1", c12.getDestinationTrackName());
		
		// now allow train 3 to drop
		train3.reset();
		loc2trk1.setDropOption(Track.ROUTES);
		loc2trk1.addDropId(rte1.getId());
		train3.build();
		
		Assert.assertEquals("c1 destination 7", "", c1.getDestinationTrackName());
		Assert.assertEquals("c2 destination 7", "Bedford Yard 1", c2.getDestinationTrackName());
		Assert.assertEquals("c3 destination 7", "Chelmsford Interchange 1", c3.getDestinationTrackName());
		Assert.assertEquals("c4 destination 7", "Bedford Yard 1", c4.getDestinationTrackName());
		Assert.assertEquals("c5 destination 7", "", c5.getDestinationTrackName());
		Assert.assertEquals("c6 destination 7", "", c6.getDestinationTrackName());
		Assert.assertEquals("c7 destination 7", "Bedford Yard 1", c7.getDestinationTrackName());
		Assert.assertEquals("c8 destination 7", "", c8.getDestinationTrackName());
		Assert.assertEquals("c9 destination 7", "Bedford Yard 1", c9.getDestinationTrackName());
		Assert.assertEquals("c12 destination 7", "Bedford Yard 1", c12.getDestinationTrackName());

		// move and terminate
		train3.move();
		train3.move();
		train3.move();
		train3.move();
		
		// check tracks
		Assert.assertEquals("c1 track", "Westford Yard 1", c1.getTrackName());
		Assert.assertEquals("c2 track", "Bedford Yard 1", c2.getTrackName());
		Assert.assertEquals("c3 track", "Chelmsford Interchange 1", c3.getTrackName());
		Assert.assertEquals("c4 track", "Bedford Yard 1", c4.getTrackName());
		Assert.assertEquals("c5 track", "Westford Yard 2", c5.getTrackName());
		Assert.assertEquals("c6 track", "Bedford Yard 1", c6.getTrackName());
		Assert.assertEquals("c7 track", "Bedford Yard 1", c7.getTrackName());
		Assert.assertEquals("c8 track", "Bedford Yard 1", c8.getTrackName());
		Assert.assertEquals("c9 track", "Bedford Yard 1", c9.getTrackName());
		Assert.assertEquals("c10 track", "Bedford Yard 1", c10.getTrackName());
		Assert.assertEquals("c11 track", "Bedford Yard 1", c11.getTrackName());
		Assert.assertEquals("c12 track", "Bedford Yard 1", c12.getTrackName());
		
		// check train length and tonnage
		Assert.assertEquals("Depart Westford length", 158, r1l1.getTrainLength());
		Assert.assertEquals("Depart Old Chelmsford length", 340, r1l2.getTrainLength());
		
		// c2 E and c3 E loaded car weight 20 + 50 = 70 tons, empty weight 20/3 + 30/3 = 16
		Assert.assertEquals("Depart Old Westford tonnage", 16, r1l1.getTrainWeight());
		// In train, c2 E, c4 E, c7 E, c9 L, c12 L = 20/3 + 40/3 + 70/3 + 90 + 120 = 252 
		Assert.assertEquals("Depart Old Chelmsford tonnage", 252, r1l2.getTrainWeight());
		
		// test route pickup and drop controls
		train3.setRequirements(Train.CABOOSE);
		r1l1.setCanPickup(false);
		train3.build();
		
		Assert.assertEquals("c1 destination 8", "", c1.getDestinationTrackName());
		Assert.assertEquals("c5 destination 8", "", c5.getDestinationTrackName());
		Assert.assertEquals("c3 destination 8", "Bedford Yard 1", c3.getDestinationTrackName());
		Assert.assertEquals("c13 destination 8", "Bedford Yard 1", c13.getDestinationTrackName());
		
		r1l1.setCanPickup(true);
		r1l2.setCanPickup(false);
		train3.build();
		
		Assert.assertEquals("c1 destination 9", "Chelmsford Interchange 1", c1.getDestinationTrackName());
		Assert.assertEquals("c5 destination 9", "", c5.getDestinationTrackName());
		Assert.assertEquals("c3 destination 9", "", c3.getDestinationTrackName());
		Assert.assertEquals("c13 destination 9", "Bedford Yard 1", c13.getDestinationTrackName());

		r1l2.setCanPickup(true);
		r1l2.setCanDrop(false);	// Old Chelmsford
		train3.build();
		
		Assert.assertEquals("c1 destination 10", "", c1.getDestinationTrackName());
		Assert.assertEquals("c5 destination 10", "Bedford Yard 1", c5.getDestinationTrackName());
		Assert.assertEquals("c3 destination 10", "Bedford Yard 1", c3.getDestinationTrackName());
		Assert.assertEquals("c13 destination 10", "Bedford Yard 1", c13.getDestinationTrackName());
		
		// try forcing c1 to Chelmsford
		c1.setDestination(loc2, null);
		train3.build();
		Assert.assertEquals("c1 destination Old Chelmsford", "", c1.getDestinationTrackName());
		
		// confirm that c1 isn't part of this train
		Assert.assertNull("c1 isn't assigned to a train",c1.getTrain());
		Assert.assertNull("c1 destination has been set to null",c1.getDestination());
		Assert.assertNull("c1 next destination should be null",c1.getNextDestination());
		Assert.assertNull("c1 next destination track should be null",c1.getNextDestTrack());
		
		// try without moves
		r1l2.setCanDrop(true);
		r1l2.setMaxCarMoves(0);
		c1.setDestination(loc2, null);
		train3.build();
		Assert.assertEquals("c1 destination Old Chelmsford, no moves", "", c1.getDestinationTrackName());
		
		c1.setDestination(null, null);
		r1l2.setMaxCarMoves(6);
		r1l3.setCanDrop(false);	// Should be able to drop off caboose
		train3.build();
		
		Assert.assertEquals("c1 destination 11", "Chelmsford Interchange 1", c1.getDestinationTrackName());
		Assert.assertEquals("c5 destination 11", "", c5.getDestinationTrackName());
		Assert.assertEquals("c3 destination 11", "", c3.getDestinationTrackName());
		Assert.assertEquals("c13 destination 11", "Bedford Yard 1", c13.getDestinationTrackName());
	
		// test to see if FRED also get delivered
		train3.setRequirements(Train.FRED);
		Assert.assertEquals("Place c2 at start of route", Track.OKAY, c2.setLocation(loc1, loc1trk2));
		c2.setFred(true);
		train3.build();
		
		Assert.assertTrue("Train 3 built", train3.isBuilt());
		Assert.assertEquals("c1 destination 12", "Chelmsford Interchange 1", c1.getDestinationTrackName());
		Assert.assertEquals("c2 destination 12", "Bedford Yard 1", c2.getDestinationTrackName());
		Assert.assertEquals("c3 destination 12", "", c3.getDestinationTrackName());
		Assert.assertEquals("c13 destination 12", "", c13.getDestinationTrackName());
		
		// move and terminate
		train3.move();
		train3.move();
		train3.move();
		train3.move();
		
		Assert.assertEquals("c1 track 12", "Chelmsford Interchange 1", c1.getTrackName());
		Assert.assertEquals("c2 track 12", "Bedford Yard 1", c2.getTrackName());
		Assert.assertEquals("c3 track 12", "Chelmsford Interchange 1", c3.getTrackName());
		Assert.assertEquals("c13 track 12", "Westford Yard 1", c13.getTrackName());
		
		// test previous car delivered pickup interchange operation
		loc2trk1.setDropOption(Track.ANY);
		loc2trk1.setPickupOption(Track.TRAINS);
		loc2trk2.setDropOption(Track.ANY);
		loc2trk2.setPickupOption(Track.TRAINS);
		
		// Place car with FRED back at start of route
		Assert.assertEquals("Place c2 again", Track.OKAY, c2.setLocation(loc1, loc1trk2));
		
		train3.build();
		Assert.assertTrue("train 3 should build", train3.isBuilt());
		Assert.assertEquals("car BM Q1 should not be part of train", null, c1.getTrain());
		Assert.assertEquals("car XP Q3 should not be part of train", null, c3.getTrain());
		
		// put some cars at start of Route
		Assert.assertEquals("Place c1 again", Track.OKAY, c1.setLocation(loc1, loc1trk2));
		Assert.assertEquals("Place c2 again", Track.OKAY, c2.setLocation(loc1, loc1trk2));
		Assert.assertEquals("Place c3 again", Track.OKAY, c3.setLocation(loc1, loc1trk2));
		Assert.assertEquals("Place c4 again", Track.OKAY, c4.setLocation(loc1, loc1trk2));
		Assert.assertEquals("Place c6 again", Track.OKAY, c6.setLocation(loc2, loc2trk1));
		Assert.assertEquals("Place c7 again", Track.OKAY, c7.setLocation(loc2, loc2trk2));
		
		r1l1.setMaxCarMoves(5);
		loc2trk1.setDropOption(Track.ROUTES);
		loc2trk1.setPickupOption(Track.ROUTES);
		loc2trk2.setDropOption(Track.ROUTES);
		loc2trk2.setPickupOption(Track.ROUTES);

		train3.build();
		Assert.assertTrue("train 3 should build", train3.isBuilt());
		Assert.assertEquals("BM Q1 in train", null, c1.getTrain());
		Assert.assertEquals("UP Q2 in train", train3, c2.getTrain());
		Assert.assertEquals("XP Q3 in train", train3, c3.getTrain());
		Assert.assertEquals("PU Q4 in train", train3, c4.getTrain());
		Assert.assertEquals("UP Q5 in train", null, c5.getTrain());
		Assert.assertEquals("CP Q6 in train", null, c6.getTrain());
		Assert.assertEquals("UP Q7 in train", null, c7.getTrain());
		
		Assert.assertEquals("UP Q2 destination", "Bedford Yard 1", c2.getDestinationTrackName());	
		Assert.assertEquals("XP Q3 destination", "Chelmsford Freight 4", c3.getDestinationTrackName());
		Assert.assertEquals("PU Q4 destination", "Chelmsford Yard 3", c4.getDestinationTrackName());
		Assert.assertEquals("UP Q5 destination", "", c5.getDestinationTrackName());
		
		// interchange testing done, now test replace car type and road
		
		Assert.assertTrue("loc1 should accept Boxcar", loc1.acceptsTypeName("Boxcar"));
		Assert.assertTrue("loc2 should accept Boxcar", loc2.acceptsTypeName("Boxcar"));
		
		// replace should modify locations and trains
		ct.replaceName("Boxcar", "boxcar");
		
		Assert.assertFalse("loc1 should not accept Boxcar", loc1.acceptsTypeName("Boxcar"));
		Assert.assertFalse("loc2 should not accept Boxcar", loc2.acceptsTypeName("Boxcar"));
		Assert.assertFalse("track loc1trk1 should not accept Boxcar", loc1trk1.acceptsTypeName("Boxcar"));
		Assert.assertFalse("track loc2trk1 should not accept Boxcar", loc2trk1.acceptsTypeName("Boxcar"));
		Assert.assertFalse("train 1 should not accept Boxcar", train1.acceptsTypeName("Boxcar"));
		Assert.assertFalse("train 2 should not accept Boxcar", train2.acceptsTypeName("Boxcar"));
		Assert.assertFalse("train 3 should not accept Boxcar", train3.acceptsTypeName("Boxcar"));

		Assert.assertTrue("loc1 should accept boxcar", loc1.acceptsTypeName("boxcar"));
		Assert.assertTrue("loc2 should accept boxcar", loc2.acceptsTypeName("boxcar"));
		Assert.assertTrue("track loc1trk1 should accept boxcar", loc1trk1.acceptsTypeName("boxcar"));
		Assert.assertTrue("track loc2trk1 should accept boxcar", loc2trk1.acceptsTypeName("boxcar"));
		Assert.assertTrue("train 1 should accept boxcar", train1.acceptsTypeName("boxcar"));
		Assert.assertTrue("train 2 should accept boxcar", train2.acceptsTypeName("boxcar"));
		Assert.assertTrue("train 3 should accept boxcar", train3.acceptsTypeName("boxcar"));

		ct.replaceName("boxcar", "Boxcar");
		
		Assert.assertTrue("loc1 should accept Boxcar", loc1.acceptsTypeName("Boxcar"));
		Assert.assertTrue("loc2 should accept Boxcar", loc2.acceptsTypeName("Boxcar"));
		Assert.assertTrue("track loc1trk1 should accept Boxcar", loc1trk1.acceptsTypeName("Boxcar"));
		Assert.assertTrue("track loc2trk1 should accept Boxcar", loc2trk1.acceptsTypeName("Boxcar"));
		Assert.assertTrue("train 1 should accept Boxcar", train1.acceptsTypeName("Boxcar"));
		Assert.assertTrue("train 2 should accept Boxcar", train2.acceptsTypeName("Boxcar"));
		Assert.assertTrue("train 3 should accept Boxcar", train3.acceptsTypeName("Boxcar"));

		// now test road name replace
		CarRoads cr = CarRoads.instance();
		cr.addName("CP");
				
		loc1trk1.setRoadOption(Track.INCLUDEROADS);
		loc1trk1.addRoadName("CP");
		loc1trk1.addRoadName("PC");
		train1.setRoadOption(Train.INCLUDEROADS);
		train1.addRoadName("CP");
		train1.addRoadName("PC");
		train1.setCabooseRoad("CP");
		train1.setEngineRoad("CP");
		
		Assert.assertTrue("track loc1trk1 should accept road CP", loc1trk1.acceptsRoadName("CP"));
		Assert.assertTrue("track loc1trk1 should accept road PC", loc1trk1.acceptsRoadName("PC"));
		Assert.assertFalse("track loc1trk1 should Not accept road PC", loc1trk1.acceptsRoadName("UP"));
		Assert.assertTrue("Train 1 should accept road CP", train1.acceptsRoadName("CP"));
		Assert.assertTrue("Train 1 should accept road PC", train1.acceptsRoadName("PC"));
		Assert.assertFalse("Train 1 should Not accept road UP", train1.acceptsRoadName("UP"));
		Assert.assertEquals("Caboose road", "CP", train1.getCabooseRoad());
		Assert.assertEquals("Engine road", "CP", train1.getEngineRoad());

		cr.replaceName("CP", "UP");
		
		Assert.assertFalse("after replace track loc1trk1 should Not accept road CP", loc1trk1.acceptsRoadName("CP"));
		Assert.assertTrue("after replace track loc1trk1 should accept road PC", loc1trk1.acceptsRoadName("PC"));
		Assert.assertTrue("after replace track loc1trk1 should accept road PC", loc1trk1.acceptsRoadName("UP"));
		Assert.assertFalse("after replace Train 1 should Not accept road CP", train1.acceptsRoadName("CP"));
		Assert.assertTrue("after replace Train 1 should accept road PC", train1.acceptsRoadName("PC"));
		Assert.assertTrue("after replace Train 1 should accept road UP", train1.acceptsRoadName("UP"));
		Assert.assertEquals("Caboose road", "UP", train1.getCabooseRoad());
		Assert.assertEquals("Engine road", "UP", train1.getEngineRoad());

	}
	
	public void testCaboose() {
		TrainManager tmanager = TrainManager.instance();
		RouteManager rmanager = RouteManager.instance();
		LocationManager lmanager = LocationManager.instance();
		EngineManager emanager = EngineManager.instance();
		CarManager cmanager = CarManager.instance();
		CarTypes ct = CarTypes.instance();
		EngineTypes et = EngineTypes.instance();
		
		// register the car and engine types used
		ct.addName("Boxcar");
		ct.addName("Caboose");
		ct.addName("Flat");
		et.addName("Diesel");
		
		// place two engines in a consist
		Consist con1 = emanager.newConsist("C1");
		
		Engine e1 = emanager.newEngine("UP", "1");
		e1.setModel("GP40");
		e1.setConsist(con1);
		Engine e2 = emanager.newEngine("SP", "2");
		e2.setModel("GP40");
		e2.setConsist(con1);
		
		// Set up three cabooses and six box cars
		Car c1 = cmanager.newCar("UP", "1");
		c1.setType("Caboose");
		c1.setLength("32");
		c1.setMoves(10);
		c1.setCaboose(true);
		
		Car c2 = cmanager.newCar("SP", "2");
		c2.setType("Caboose");
		c2.setLength("30");
		c2.setMoves(5);
		c2.setCaboose(true);
		
		Car c3 = cmanager.newCar("NH", "3");
		c3.setType("Caboose");
		c3.setLength("33");
		c3.setCaboose(true);
		
		Car c4 = cmanager.newCar("UP", "4");
		c4.setType("Boxcar");
		c4.setLength("40");
		c4.setMoves(16);
		c4.setFred(true);

		Car c5 = cmanager.newCar("SP", "5");
		c5.setType("Boxcar");
		c5.setLength("40");
		c5.setMoves(8);
		c5.setFred(true);
		
		Car c6 = cmanager.newCar("NH", "6");
		c6.setType("Boxcar");
		c6.setLength("40");
		c6.setMoves(2);
		c6.setFred(true);
		
		Car c7 = cmanager.newCar("UP", "7");
		c7.setType("Flat");
		c7.setLength("40");
		c7.setMoves(5);

		Car c8 = cmanager.newCar("SP", "8");
		c8.setType("Boxcar");
		c8.setLength("40");
		c8.setMoves(4);
		
		Car c9 = cmanager.newCar("NH", "9");
		c9.setType("Boxcar");
		c9.setLength("40");
		c9.setMoves(3);
		
		Car c10 = cmanager.newCar("NH", "10");
		c10.setType("Boxcar");
		c10.setLength("40");
		c10.setMoves(10);
		
		Car c11 = cmanager.newCar("SP", "11");
		c11.setType("Boxcar");
		c11.setLength("40");
		c11.setMoves(3);

		// Create 3 locations
		Location loc1 = lmanager.newLocation("Harvard");
		
		Track loc1trk1 = loc1.addTrack("Harvard Yard", Track.YARD);
		loc1trk1.setLength(1000);
		
		Location loc2 = lmanager.newLocation("Arlington");
		
		Track loc2trk1 = loc2.addTrack("Arlington Yard", Track.YARD);
		loc2trk1.setLength(1000);
		
		Location loc3 = lmanager.newLocation("Boston");
		
		Track loc3trk1 = loc3.addTrack("Boston Yard", Track.YARD);
		loc3trk1.setLength(1000);
		loc3trk1.deleteTypeName("Diesel");
		
		Track loc3trk2 = loc3.addTrack("Boston Engine Yard", Track.YARD);
		loc3trk2.setLength(200);
		loc3trk2.deleteTypeName("Boxcar");
		loc3trk2.deleteTypeName("Flat");
		loc3trk2.deleteTypeName("Caboose");
		
		// Create route with 3 location
		Setup.setCarMoves(7);	// set default to 7 moves per location
		Route rte1 = rmanager.newRoute("Route 2 Boston");
		rte1.addLocation(loc1);
		RouteLocation rl2 = rte1.addLocation(loc2);
		rl2.setTrainIconX(75);	// set the train icon coordinates
		rl2.setTrainIconY(150);
		RouteLocation rl3 = rte1.addLocation(loc3);
		rl3.setTrainIconX(125);	// set the train icon coordinates
		rl3.setTrainIconY(150);
		
		// Create train
		Train train1 = tmanager.newTrain("HTB");
		train1.setRoute(rte1);
		
		// turn off build fail messages
		tmanager.setBuildMessagesEnabled(false);
		
		// Place cars
		Assert.assertEquals("Place c1", Track.OKAY, c1.setLocation(loc1, loc1trk1));
		Assert.assertEquals("Place c2", Track.OKAY, c2.setLocation(loc1, loc1trk1));
		Assert.assertEquals("Place c3", Track.OKAY, c3.setLocation(loc1, loc1trk1));
		Assert.assertEquals("Place c4", Track.OKAY, c4.setLocation(loc1, loc1trk1));
		
		Assert.assertEquals("Place c5", Track.OKAY, c5.setLocation(loc1, loc1trk1));
		Assert.assertEquals("Place c6", Track.OKAY, c6.setLocation(loc1, loc1trk1));
		Assert.assertEquals("Place c7", Track.OKAY, c7.setLocation(loc1, loc1trk1));
		Assert.assertEquals("Place c8", Track.OKAY, c8.setLocation(loc1, loc1trk1));
		
		Assert.assertEquals("Place c9", Track.OKAY, c9.setLocation(loc1, loc1trk1));
		Assert.assertEquals("Place c10", Track.OKAY, c10.setLocation(loc2, loc2trk1));
		Assert.assertEquals("Place c11", Track.OKAY, c11.setLocation(loc2, loc2trk1));
		
		// Place engines
		Assert.assertEquals("Place e1", Track.OKAY, e1.setLocation(loc1, loc1trk1));
		Assert.assertEquals("Place e2", Track.OKAY, e2.setLocation(loc1, loc1trk1));
		
		// no requirements, so no caboose or FRED or engines
		train1.build();
		Assert.assertEquals("Train 1 After Build 1", true, train1.isBuilt());
		
		// check destinations
		Assert.assertEquals("c1 destination 1", "", c1.getDestinationTrackName());
		Assert.assertEquals("c2 destination 1", "", c2.getDestinationTrackName());
		Assert.assertEquals("c3 destination 1", "", c3.getDestinationTrackName());
		Assert.assertEquals("c4 destination 1", "", c4.getDestinationTrackName());
		
		Assert.assertEquals("c5 destination 1", "", c5.getDestinationTrackName());
		Assert.assertEquals("c6 destination 1", "", c6.getDestinationTrackName());
		Assert.assertEquals("c7 destination 1", "Boston Yard", c7.getDestinationTrackName());
		Assert.assertEquals("c8 destination 1", "Arlington Yard", c8.getDestinationTrackName());
		
		Assert.assertEquals("c9 destination 1", "Boston Yard", c9.getDestinationTrackName());
		Assert.assertEquals("c10 destination 1", "Boston Yard", c10.getDestinationTrackName());
		Assert.assertEquals("c11 destination 1", "Boston Yard", c11.getDestinationTrackName());

		Assert.assertEquals("e1 destination 1", "", e1.getDestinationTrackName());
		Assert.assertEquals("e2 destination 1", "", e2.getDestinationTrackName());

		// no engines, so the caboose with least moves should be used
		train1.setRequirements(Train.CABOOSE);
		// don't allow pickups at second location Arlington
		rl2.setCanPickup(false);
		train1.build();
		Assert.assertEquals("Train 1 After Build 2", true, train1.isBuilt());
		
		// check destinations
		Assert.assertEquals("c1 destination 2", "", c1.getDestinationTrackName());
		Assert.assertEquals("c2 destination 2", "", c2.getDestinationTrackName());
		Assert.assertEquals("c3 destination 2", "Boston Yard", c3.getDestinationTrackName());
		Assert.assertEquals("c4 destination 2", "", c4.getDestinationTrackName());
		
		Assert.assertEquals("c5 destination 2", "", c5.getDestinationTrackName());
		Assert.assertEquals("c6 destination 2", "", c6.getDestinationTrackName());
		Assert.assertEquals("c7 destination 2", "Boston Yard", c7.getDestinationTrackName());
		Assert.assertEquals("c8 destination 2", "Boston Yard", c8.getDestinationTrackName());
		
		Assert.assertEquals("c9 destination 2", "Arlington Yard", c9.getDestinationTrackName());
		Assert.assertEquals("c10 destination 2", "", c10.getDestinationTrackName());
		Assert.assertEquals("c11 destination 2", "", c11.getDestinationTrackName());

		Assert.assertEquals("e1 destination 2", "", e1.getDestinationTrackName());
		Assert.assertEquals("e2 destination 2", "", e2.getDestinationTrackName());
		
		// there's a caboose c1 that matches lead engine
		train1.setNumberEngines("2");
		train1.build();
		Assert.assertEquals("Train 1 After Build 3", true, train1.isBuilt());
		
		// check destinations
		Assert.assertEquals("c1 destination 3", "Boston Yard", c1.getDestinationTrackName());
		Assert.assertEquals("c2 destination 3", "", c2.getDestinationTrackName());
		Assert.assertEquals("c3 destination 3", "", c3.getDestinationTrackName());
		Assert.assertEquals("c4 destination 3", "", c4.getDestinationTrackName());
		
		Assert.assertEquals("c5 destination 3", "", c5.getDestinationTrackName());
		Assert.assertEquals("c6 destination 3", "", c6.getDestinationTrackName());
		Assert.assertEquals("c7 destination 3", "Boston Yard", c7.getDestinationTrackName());
		Assert.assertEquals("c8 destination 3", "Boston Yard", c8.getDestinationTrackName());
		
		Assert.assertEquals("c9 destination 3", "Arlington Yard", c9.getDestinationTrackName());
		Assert.assertEquals("c10 destination 3", "", c10.getDestinationTrackName());
		Assert.assertEquals("c11 destination 3", "", c11.getDestinationTrackName());

		Assert.assertEquals("e1 destination 3", "Boston Engine Yard", e1.getDestinationTrackName());
		Assert.assertEquals("e2 destination 3", "Boston Engine Yard", e2.getDestinationTrackName());

		// should default to the caboose with the least moves
		e1.setRoad("X");
		// allow pickups at Arlington
		rl2.setCanPickup(true);
		train1.build();
		Assert.assertEquals("Train 1 After Build 4", true, train1.isBuilt());
		
		// check destinations
		Assert.assertEquals("c1 destination 4", "", c1.getDestinationTrackName());
		Assert.assertEquals("c2 destination 4", "", c2.getDestinationTrackName());
		Assert.assertEquals("c3 destination 4", "Boston Yard", c3.getDestinationTrackName());
		Assert.assertEquals("c4 destination 4", "", c4.getDestinationTrackName());
		
		Assert.assertEquals("c5 destination 4", "", c5.getDestinationTrackName());
		Assert.assertEquals("c6 destination 4", "", c6.getDestinationTrackName());
		Assert.assertEquals("c7 destination 4", "Boston Yard", c7.getDestinationTrackName());
		Assert.assertEquals("c8 destination 4", "Boston Yard", c8.getDestinationTrackName());
		
		Assert.assertEquals("c9 destination 4", "Arlington Yard", c9.getDestinationTrackName());
		Assert.assertEquals("c10 destination 4", "Boston Yard", c10.getDestinationTrackName());
		Assert.assertEquals("c11 destination 4", "Boston Yard", c11.getDestinationTrackName());

		Assert.assertEquals("e1 destination 4", "Boston Engine Yard", e1.getDestinationTrackName());
		Assert.assertEquals("e2 destination 4", "Boston Engine Yard", e2.getDestinationTrackName());

		// don't allow drops at Boston, caboose and engines should still drop there
		rl3.setCanDrop(false);
		// should not take NH caboose
		e1.setRoad("NH");
		// now require a SP caboose
		train1.setCabooseRoad("SP");
		train1.build();
		Assert.assertEquals("Train 1 After Build 5", true, train1.isBuilt());
		// check destinations
		Assert.assertEquals("c1 destination 5", "", c1.getDestinationTrackName());
		Assert.assertEquals("c2 destination 5", "Boston Yard", c2.getDestinationTrackName());
		Assert.assertEquals("c3 destination 5", "", c3.getDestinationTrackName());
		Assert.assertEquals("c4 destination 5", "", c4.getDestinationTrackName());
		
		Assert.assertEquals("c5 destination 5", "", c5.getDestinationTrackName());
		Assert.assertEquals("c6 destination 5", "", c6.getDestinationTrackName());
		Assert.assertEquals("c7 destination 5", "Arlington Yard", c7.getDestinationTrackName());
		Assert.assertEquals("c8 destination 5", "Arlington Yard", c8.getDestinationTrackName());
		
		Assert.assertEquals("c9 destination 5", "Arlington Yard", c9.getDestinationTrackName());
		Assert.assertEquals("c10 destination 5", "", c10.getDestinationTrackName());
		Assert.assertEquals("c11 destination 5", "", c11.getDestinationTrackName());

		Assert.assertEquals("e1 destination 5", "Boston Engine Yard", e1.getDestinationTrackName());
		Assert.assertEquals("e2 destination 5", "Boston Engine Yard", e2.getDestinationTrackName());

		// allow drops at Boston
		rl3.setCanDrop(true);
		// should take car with FRED and road SP
		train1.setRequirements(Train.FRED);
		train1.build();
		Assert.assertEquals("Train 1 After Build 6", true, train1.isBuilt());
		// check destinations
		Assert.assertEquals("c1 destination 6", "", c1.getDestinationTrackName());
		Assert.assertEquals("c2 destination 6", "", c2.getDestinationTrackName());
		Assert.assertEquals("c3 destination 6", "", c3.getDestinationTrackName());
		Assert.assertEquals("c4 destination 6", "", c4.getDestinationTrackName());
		
		Assert.assertEquals("c5 destination 6", "Boston Yard", c5.getDestinationTrackName());
		Assert.assertEquals("c6 destination 6", "", c6.getDestinationTrackName());
		Assert.assertEquals("c7 destination 6", "Boston Yard", c7.getDestinationTrackName());
		Assert.assertEquals("c8 destination 6", "Boston Yard", c8.getDestinationTrackName());
		
		Assert.assertEquals("c9 destination 6", "Arlington Yard", c9.getDestinationTrackName());
		Assert.assertEquals("c10 destination 6", "Boston Yard", c10.getDestinationTrackName());
		Assert.assertEquals("c11 destination 6", "Boston Yard", c11.getDestinationTrackName());

		Assert.assertEquals("e1 destination 6", "Boston Engine Yard", e1.getDestinationTrackName());
		Assert.assertEquals("e2 destination 6", "Boston Engine Yard", e2.getDestinationTrackName());

		// should take car with FRED least number of moves
		train1.setCabooseRoad("");
		train1.build();
		Assert.assertEquals("Train 1 After Build 7", true, train1.isBuilt());
		// check destinations
		Assert.assertEquals("c1 destination 7", "", c1.getDestinationTrackName());
		Assert.assertEquals("c2 destination 7", "", c2.getDestinationTrackName());
		Assert.assertEquals("c3 destination 7", "", c3.getDestinationTrackName());
		Assert.assertEquals("c4 destination 7", "", c4.getDestinationTrackName());
		
		Assert.assertEquals("c5 destination 7", "", c5.getDestinationTrackName());
		Assert.assertEquals("c6 destination 7", "Boston Yard", c6.getDestinationTrackName());
		Assert.assertEquals("c7 destination 7", "Boston Yard", c7.getDestinationTrackName());
		Assert.assertEquals("c8 destination 7", "Boston Yard", c8.getDestinationTrackName());
		
		Assert.assertEquals("c9 destination 7", "Arlington Yard", c9.getDestinationTrackName());
		Assert.assertEquals("c10 destination 7", "Boston Yard", c10.getDestinationTrackName());
		Assert.assertEquals("c11 destination 7", "Boston Yard", c11.getDestinationTrackName());
		
		Assert.assertEquals("e1 destination 7", "Boston Engine Yard", e1.getDestinationTrackName());
		Assert.assertEquals("e2 destination 7", "Boston Engine Yard", e2.getDestinationTrackName());

		// now exclude road NH, engine road is NH and should be rejected
		train1.addRoadName("NH");
		train1.setRoadOption(Train.EXCLUDEROADS);
		train1.build();
		Assert.assertEquals("Train 1 After Build 7a", false, train1.isBuilt());
		// now override by setting a road for the engine
		train1.setEngineRoad("NH");
		train1.build();
		Assert.assertEquals("Train 1 After Build 8", true, train1.isBuilt());
		// check destinations
		Assert.assertEquals("c1 destination 8", "", c1.getDestinationTrackName());
		Assert.assertEquals("c2 destination 8", "", c2.getDestinationTrackName());
		Assert.assertEquals("c3 destination 8", "", c3.getDestinationTrackName());
		Assert.assertEquals("c4 destination 8", "", c4.getDestinationTrackName());
		
		Assert.assertEquals("c5 destination 8", "Boston Yard", c5.getDestinationTrackName());
		Assert.assertEquals("c6 destination 8", "", c6.getDestinationTrackName());
		Assert.assertEquals("c7 destination 8", "Boston Yard", c7.getDestinationTrackName());
		Assert.assertEquals("c8 destination 8", "Arlington Yard", c8.getDestinationTrackName());
		
		Assert.assertEquals("c9 destination 8", "", c9.getDestinationTrackName());
		Assert.assertEquals("c10 destination 8", "", c10.getDestinationTrackName());
		Assert.assertEquals("c11 destination 8", "Boston Yard", c11.getDestinationTrackName());

		Assert.assertEquals("e1 destination 8", "Boston Engine Yard", e1.getDestinationTrackName());
		Assert.assertEquals("e2 destination 8", "Boston Engine Yard", e2.getDestinationTrackName());

		// now only include NH
		train1.setRoadOption(Train.INCLUDEROADS);
		train1.build();
		Assert.assertEquals("Train 1 After Build 9", true, train1.isBuilt());
		// check destinations
		Assert.assertEquals("c1 destination 9", "", c1.getDestinationTrackName());
		Assert.assertEquals("c2 destination 9", "", c2.getDestinationTrackName());
		Assert.assertEquals("c3 destination 9", "", c3.getDestinationTrackName());
		Assert.assertEquals("c4 destination 9", "", c4.getDestinationTrackName());
		
		Assert.assertEquals("c5 destination 9", "", c5.getDestinationTrackName());
		Assert.assertEquals("c6 destination 9", "Boston Yard", c6.getDestinationTrackName());
		Assert.assertEquals("c7 destination 9", "", c7.getDestinationTrackName());
		Assert.assertEquals("c8 destination 9", "", c8.getDestinationTrackName());
		
		Assert.assertEquals("c9 destination 9", "Arlington Yard", c9.getDestinationTrackName());
		Assert.assertEquals("c10 destination 9", "Boston Yard", c10.getDestinationTrackName());
		Assert.assertEquals("c11 destination 9", "", c11.getDestinationTrackName());

		Assert.assertEquals("e1 destination 9", "Boston Engine Yard", e1.getDestinationTrackName());
		Assert.assertEquals("e2 destination 9", "Boston Engine Yard", e2.getDestinationTrackName());

		// don't allow boxcar, car with FRED required, build should fail
		loc3.deleteTypeName("Boxcar");
		train1.build();
		Assert.assertEquals("Train 1 After Build 9a", false, train1.isBuilt());
		loc3.addTypeName("Boxcar");
		
		// add staging
		Track loc1trk2 = loc1.addTrack("Harvard Staging", Track.STAGING);
		loc1trk2.setLength(1000);
		// now depart staging, must take all cars in staging
		// Place cars
		Assert.assertEquals("Move c1", Track.OKAY, c1.setLocation(loc1, loc1trk2));
		Assert.assertEquals("Move c2", Track.OKAY, c2.setLocation(loc1, loc1trk2));
		Assert.assertEquals("Move c3", Track.OKAY, c3.setLocation(loc1, loc1trk2));
		Assert.assertEquals("Move c4", Track.OKAY, c4.setLocation(loc1, loc1trk2));
		
		Assert.assertEquals("Move c5", Track.OKAY, c5.setLocation(loc1, loc1trk2));
		Assert.assertEquals("Move c6", Track.OKAY, c6.setLocation(loc1, loc1trk2));
		Assert.assertEquals("Move c7", Track.OKAY, c7.setLocation(loc1, loc1trk2));
		Assert.assertEquals("Move c8", Track.OKAY, c8.setLocation(loc1, loc1trk2));	
		Assert.assertEquals("Move c9", Track.OKAY, c9.setLocation(loc1, loc1trk2));
		
		// Place engines
		Assert.assertEquals("Place e1", Track.OKAY, e1.setLocation(loc1, loc1trk2));
		Assert.assertEquals("Place e2", Track.OKAY, e2.setLocation(loc1, loc1trk2));

		// program requires only staging at any location, so we don't test with yard
		loc1.deleteTrack(loc1trk1);
		// All engines and cars in staging must move!  Cabooses and cars with FRED to terminal
		train1.setNumberEngines("0");
		train1.build();
		// train only accepted engine and cars with NH road therefore build should fail
		Assert.assertEquals("Train 1 After Build from staging", false, train1.isBuilt());
		// try again but now accept all roads
		train1.setRoadOption(Train.ALLROADS);
		train1.build();
		Assert.assertEquals("Train 1 After Build 10", true, train1.isBuilt());
		// check destinations
		Assert.assertEquals("c1 destination 10", "Boston Yard", c1.getDestinationTrackName());
		Assert.assertEquals("c2 destination 10", "Boston Yard", c2.getDestinationTrackName());
		Assert.assertEquals("c3 destination 10", "Boston Yard", c3.getDestinationTrackName());
		Assert.assertEquals("c4 destination 10", "Boston Yard", c4.getDestinationTrackName());
		
		Assert.assertEquals("c5 destination 10", "Boston Yard", c5.getDestinationTrackName());
		Assert.assertEquals("c6 destination 10", "Boston Yard", c6.getDestinationTrackName());
		Assert.assertEquals("c7 destination 10", "Arlington Yard", c7.getDestinationTrackName());
		Assert.assertEquals("c8 destination 10", "Arlington Yard", c8.getDestinationTrackName());
		
		Assert.assertEquals("c9 destination 10", "Arlington Yard", c9.getDestinationTrackName());
		Assert.assertEquals("c10 destination 10", "", c10.getDestinationTrackName());
		Assert.assertEquals("c11 destination 10", "Boston Yard", c11.getDestinationTrackName());

		Assert.assertEquals("e1 destination 10", "Boston Engine Yard", e1.getDestinationTrackName());
		Assert.assertEquals("e2 destination 10", "Boston Engine Yard", e2.getDestinationTrackName());

		// exclude road NH
		train1.setRoadOption(Train.EXCLUDEROADS);
		train1.build();
		// should fail since there are NH roads in staging
		Assert.assertEquals("Train 1 After Build 11", false, train1.isBuilt());

		// reduce Boston moves to 6, to force non caboose and FRED cars to Arlington
		rl3.setMaxCarMoves(6);
		train1.setRoadOption(Train.ALLROADS);
		train1.build();
		Assert.assertEquals("Train 1 After Build 12", true, train1.isBuilt());
		// check destinations
		Assert.assertEquals("c1 destination 12", "Boston Yard", c1.getDestinationTrackName());
		Assert.assertEquals("c2 destination 12", "Boston Yard", c2.getDestinationTrackName());
		Assert.assertEquals("c3 destination 12", "Boston Yard", c3.getDestinationTrackName());
		Assert.assertEquals("c4 destination 12", "Boston Yard", c4.getDestinationTrackName());
		
		Assert.assertEquals("c5 destination 12", "Boston Yard", c5.getDestinationTrackName());
		Assert.assertEquals("c6 destination 12", "Boston Yard", c6.getDestinationTrackName());
		Assert.assertEquals("c7 destination 12", "Arlington Yard", c7.getDestinationTrackName());
		Assert.assertEquals("c8 destination 12", "Arlington Yard", c8.getDestinationTrackName());
		
		Assert.assertEquals("c9 destination 12", "Arlington Yard", c9.getDestinationTrackName());
		Assert.assertEquals("c10 destination 12", "", c10.getDestinationTrackName());
		Assert.assertEquals("c11 destination 12", "", c11.getDestinationTrackName());
		
		Assert.assertEquals("e1 destination 12", "Boston Engine Yard", e1.getDestinationTrackName());
		Assert.assertEquals("e2 destination 12", "Boston Engine Yard", e2.getDestinationTrackName());

		// Reduce Arlington to only two moves, this should cause train build to fail
		rl2.setMaxCarMoves(2);
		// disable build messages
		tmanager.setBuildMessagesEnabled(false);
		train1.build();
		Assert.assertEquals("Train 1 After Build 13", false, train1.isBuilt());
		
		// restore number of moves
		rl2.setMaxCarMoves(7);
		rl3.setMaxCarMoves(7);
		// don't allow drops at Boston
		rl3.setCanDrop(false);
		train1.build();
		Assert.assertEquals("Train 1 After Build 14", true, train1.isBuilt());
		
		// check destinations
		Assert.assertEquals("c1 destination 14", "Boston Yard", c1.getDestinationTrackName());
		Assert.assertEquals("c2 destination 14", "Boston Yard", c2.getDestinationTrackName());
		Assert.assertEquals("c3 destination 14", "Boston Yard", c3.getDestinationTrackName());
		Assert.assertEquals("c4 destination 14", "Boston Yard", c4.getDestinationTrackName());
		
		Assert.assertEquals("c5 destination 14", "Boston Yard", c5.getDestinationTrackName());
		Assert.assertEquals("c6 destination 14", "Boston Yard", c6.getDestinationTrackName());
		Assert.assertEquals("c7 destination 14", "Arlington Yard", c7.getDestinationTrackName());
		Assert.assertEquals("c8 destination 14", "Arlington Yard", c8.getDestinationTrackName());
		
		Assert.assertEquals("c9 destination 14", "Arlington Yard", c9.getDestinationTrackName());
		Assert.assertEquals("c10 destination 14", "", c10.getDestinationTrackName());
		Assert.assertEquals("c11 destination 14", "", c11.getDestinationTrackName());
		
		Assert.assertEquals("e1 destination 14", "Boston Engine Yard", e1.getDestinationTrackName());
		Assert.assertEquals("e2 destination 14", "Boston Engine Yard", e2.getDestinationTrackName());

		// Reduce Arlington to only two moves, this should cause train build to fail
		rl2.setMaxCarMoves(2);
		train1.build();
		Assert.assertEquals("Train 1 After Build 15", false, train1.isBuilt());
		
		// Don't allow cabooses at Boston, should cause build failure
		rl2.setMaxCarMoves(7);
		loc3.deleteTypeName("Caboose");
		train1.build();
		Assert.assertEquals("Train 1 After Build 16", false, train1.isBuilt());
		
		// Don't allow boxcars, should also cause build failure
		loc3.addTypeName("Caboose");
		loc3.deleteTypeName("Boxcar");
		train1.setRequirements(Train.NONE);
		train1.build();
		Assert.assertEquals("Train 1 After Build 17", false, train1.isBuilt());
		
		// allow the three road names we're testing
		loc3.addTypeName("Boxcar");
		loc3trk1.addRoadName("NH");
		loc3trk1.addRoadName("SP");
		loc3trk1.addRoadName("UP");
		loc3trk1.setRoadOption(Track.INCLUDEROADS);
		loc3trk2.addRoadName("NH");
		loc3trk2.addRoadName("SP");
		loc3trk2.addRoadName("UP");
		loc3trk2.setRoadOption(Track.INCLUDEROADS);
		train1.build();
		Assert.assertEquals("Train 1 After Build 18", true, train1.isBuilt());
		
		// now remove type Diesel, this should cause a failure
		loc3trk2.deleteTypeName("Diesel");
		train1.build();
		Assert.assertEquals("Train 1 After Build 19", false, train1.isBuilt());
		
		// now restore type Diesel
		loc3trk2.addTypeName("Diesel");
		train1.build();
		Assert.assertEquals("Train 1 After Build 20", true, train1.isBuilt());
		
		// Set the track length too short missing one set of couplers
		loc3trk2.setLength(Integer.parseInt(e1.getLength())+ Integer.parseInt(e2.getLength())+Engine.COUPLER);
		train1.build();
		Assert.assertEquals("Train 1 After Build 20.1", false, train1.isBuilt());
	
		// restore track length
		loc3trk2.setLength(Integer.parseInt(e1.getLength())+Integer.parseInt(e2.getLength())+2*Engine.COUPLER);
		train1.build();
		Assert.assertEquals("Train 1 After Build 20.2", true, train1.isBuilt());

		// change lead engine road name, should cause build failure since Boston only 
		// accepts NH, SP, and UP.
		train1.setEngineRoad("");	// reset engine road requirements, was "NH"
		e1.setRoad("X");	// was "NH"
		train1.build();
		Assert.assertEquals("Train 1 After Build 21", false, train1.isBuilt());
		
		e1.setRoad("UP");
		loc3trk1.deleteRoadName("NH");	// this test that a caboose fails
		train1.build();
		Assert.assertEquals("Train 1 After Build 22", false, train1.isBuilt());
		
		loc3trk1.addRoadName("NH");	
		c6.setRoad("X");	// this test that a car with FRED fails
		train1.build();
		Assert.assertEquals("Train 1 After Build 23", false, train1.isBuilt());
		
		loc3trk1.addRoadName("X");
		loc2trk1.deleteTypeName("Flat");	// this test that an ordinary car must move
		train1.build();
		Assert.assertEquals("Train 1 After Build 24", false, train1.isBuilt());
		
		loc2trk1.addTypeName("Flat");	// restore
		train1.build();
		Assert.assertEquals("Train 1 After Build 25", true, train1.isBuilt());

		// check destinations
		Assert.assertEquals("c1 destination 25", "Boston Yard", c1.getDestinationTrackName());
		Assert.assertEquals("c2 destination 25", "Boston Yard", c2.getDestinationTrackName());
		Assert.assertEquals("c3 destination 25", "Boston Yard", c3.getDestinationTrackName());
		Assert.assertEquals("c4 destination 25", "Boston Yard", c4.getDestinationTrackName());
		
		Assert.assertEquals("c5 destination 25", "Boston Yard", c5.getDestinationTrackName());
		Assert.assertEquals("c6 destination 25", "Boston Yard", c6.getDestinationTrackName());
		Assert.assertEquals("c7 destination 25", "Arlington Yard", c7.getDestinationTrackName());
		Assert.assertEquals("c8 destination 25", "Arlington Yard", c8.getDestinationTrackName());
		
		Assert.assertEquals("c9 destination 25", "Arlington Yard", c9.getDestinationTrackName());
		Assert.assertEquals("c10 destination 25", "", c10.getDestinationTrackName());
		Assert.assertEquals("c11 destination 25", "", c11.getDestinationTrackName());
		
		Assert.assertEquals("e1 destination 25", "Boston Engine Yard", e1.getDestinationTrackName());
		Assert.assertEquals("e2 destination 25", "Boston Engine Yard", e2.getDestinationTrackName());
		
		train1.reset();
		// send caboose SP 2 from staging to track that will not service it
		loc3trk2.addTypeName("Caboose");
		loc3trk2.setLength(200);
		c2.setDestination(loc3, loc3trk2);
		loc3trk2.deleteTypeName("Caboose");
		train1.build();
		
		Assert.assertEquals("Train 1 After Build with caboose bad destination", false, train1.isBuilt());
		c2.setDestination(null, null);
		train1.build();

		train1.move();
		train1.move();
		train1.move();
		
		// check final locations
		Assert.assertEquals("c1 location 24", "Boston Yard", c1.getTrackName());
		Assert.assertEquals("c2 location 24", "Boston Yard", c2.getTrackName());
		Assert.assertEquals("c3 location 24", "Boston Yard", c3.getTrackName());
		Assert.assertEquals("c4 location 24", "Boston Yard", c4.getTrackName());
		
		Assert.assertEquals("c5 location 24", "Boston Yard", c5.getTrackName());
		Assert.assertEquals("c6 location 24", "Boston Yard", c6.getTrackName());
		Assert.assertEquals("c7 location 24", "Arlington Yard", c7.getTrackName());
		Assert.assertEquals("c8 location 24", "Arlington Yard", c8.getTrackName());
		
		Assert.assertEquals("c9 location 24", "Arlington Yard", c9.getTrackName());
		Assert.assertEquals("c10 location 24", "Arlington Yard", c10.getTrackName());
		Assert.assertEquals("c11 location 24", "Arlington Yard", c11.getTrackName());
		
		Assert.assertEquals("e1 location 24", "Boston Engine Yard", e1.getTrackName());
		Assert.assertEquals("e2 location 24", "Boston Engine Yard", e2.getTrackName());
	}
	
	public void testTrainBuildOptions() {
		TrainManager tmanager = TrainManager.instance();
		RouteManager rmanager = RouteManager.instance();
		LocationManager lmanager = LocationManager.instance();
		EngineManager emanager = EngineManager.instance();
		CarManager cmanager = CarManager.instance();
		CarTypes ct = CarTypes.instance();
		EngineTypes et = EngineTypes.instance();
		
		// register the car and engine types used
		ct.addName("Boxcar");
		ct.addName("Caboose");
		ct.addName("Flat");
		et.addName("Diesel");
		
		// create 2 consists and a single engine for testing
		Consist con1 = emanager.newConsist("C1");
		
		Engine e1 = emanager.newEngine("UP", "1");
		e1.setModel("GP30");
		e1.setOwner("AT");
		e1.setBuilt("1957");
		e1.setConsist(con1);
		e1.setMoves(5);
		Engine e2 = emanager.newEngine("SP", "2");
		e2.setModel("GP30");
		e2.setOwner("AT");
		e2.setBuilt("1957");
		e2.setConsist(con1);
		e2.setMoves(5);
		
		// one engine
		Engine e3 = emanager.newEngine("SP", "3");
		e3.setModel("GP40");
		e3.setOwner("DAB");
		e3.setBuilt("1957");
		
		Consist con2 = emanager.newConsist("C2");
		
		Engine e4 = emanager.newEngine("UP", "10");
		e4.setModel("GP40");
		e4.setOwner("DAB");
		e4.setBuilt("1944");
		e4.setConsist(con2);
		e4.setMoves(20);
		Engine e5 = emanager.newEngine("SP", "20");
		e5.setModel("GP40");
		e5.setOwner("DAB");
		e5.setBuilt("1944");
		e5.setConsist(con2);
		e5.setMoves(20);
		
		// 3 engine consist
		Consist con3 = emanager.newConsist("C3");
		
		Engine e6 = emanager.newEngine("UP", "100");
		e6.setModel("GP40");
		e6.setOwner("DAB");
		e6.setBuilt("1944");
		e6.setConsist(con3);
		e6.setMoves(2);
		Engine e7 = emanager.newEngine("SP", "200");
		e7.setModel("GP40");
		e7.setOwner("DAB");
		e7.setBuilt("1944");
		e7.setConsist(con3);
		e7.setMoves(2);
		Engine e8 = emanager.newEngine("SP", "300");
		e8.setModel("GP40");
		e8.setOwner("DAB");
		e8.setBuilt("1944");
		e8.setConsist(con3);
		e8.setMoves(2);
		
		// Set up three cabooses and six box cars
		Car c1 = cmanager.newCar("PU", "1");
		c1.setType("Caboose");
		c1.setLength("32");
		c1.setMoves(10);
		c1.setOwner("AT");
		c1.setBuilt("1943");
		c1.setCaboose(true);
		
		Car c2 = cmanager.newCar("SP", "2");
		c2.setType("Caboose");
		c2.setLength("30");
		c2.setMoves(5);
		c2.setOwner("DAB");
		c2.setBuilt("1957");
		c2.setCaboose(true);
		
		Car c3 = cmanager.newCar("UP", "3");
		c3.setType("Caboose");
		c3.setLength("33");
		c3.setMoves(0);
		c3.setOwner("DAB");
		c3.setBuilt("1944");
		c3.setCaboose(true);
		
		Car c4 = cmanager.newCar("UP", "4");
		c4.setType("Boxcar");
		c4.setLength("40");
		c4.setMoves(16);
		c4.setOwner("DAB");
		c4.setBuilt("1958");
		c4.setFred(true);

		Car c5 = cmanager.newCar("SP", "5");
		c5.setType("Boxcar");
		c5.setLength("40");
		c5.setMoves(8);
		c5.setOwner("DAB");
		c5.setBuilt("1958");
		c5.setFred(true);
		
		Car c6 = cmanager.newCar("NH", "6");
		c6.setType("Boxcar");
		c6.setLength("40");
		c6.setMoves(2);
		c6.setOwner("DAB");
		c6.setBuilt("1958");
		c6.setFred(true);
		
		Car c7 = cmanager.newCar("UP", "7");
		c7.setType("Flat");
		c7.setLength("40");
		c7.setMoves(5);
		c7.setOwner("DAB");
		c7.setBuilt("1958");

		Car c8 = cmanager.newCar("SP", "8");
		c8.setType("Boxcar");
		c8.setLength("40");
		c8.setMoves(4);
		c8.setOwner("DAB");
		c8.setBuilt("1958");
		
		Car c9 = cmanager.newCar("NH", "9");
		c9.setType("Boxcar");
		c9.setLength("40");
		c9.setMoves(3);
		c9.setOwner("DAB");
		c9.setBuilt("1944");
		
		Car c10 = cmanager.newCar("NH", "10");
		c10.setType("Boxcar");
		c10.setLength("40");
		c10.setMoves(10);
		c10.setOwner("DAB");
		c10.setBuilt("1958");
		
		Car c11 = cmanager.newCar("SP", "11");
		c11.setType("Boxcar");
		c11.setLength("40");
		c11.setMoves(3);
		c11.setOwner("DAB");
		c11.setBuilt("1958");

		// Create 5 locations
		Location loc1 = lmanager.newLocation("Harvard");
		
		Track loc1trk1 = loc1.addTrack("Harvard Yard", Track.YARD);
		loc1trk1.setLength(1000);
		
		Track loc1trk2 = loc1.addTrack("Harvard Yard 2", Track.YARD);
		loc1trk2.setLength(1000);
		
		Location loc2 = lmanager.newLocation("Arlington");
		
		Track loc2trk1 = loc2.addTrack("Arlington Yard", Track.YARD);
		loc2trk1.setLength(1000);
		
		Location loc3 = lmanager.newLocation("Boston");
		
		Track loc3trk1 = loc3.addTrack("Boston Yard", Track.YARD);
		loc3trk1.setLength(1000);
		
		Track loc3trk2 = loc3.addTrack("Boston Yard 2", Track.YARD);
		loc3trk2.setLength(1000);
		
		Location loc4 = lmanager.newLocation("Chelmsford");
		
		Track loc4trk1 = loc4.addTrack("Chelmsford Yard", Track.YARD);
		loc4trk1.setLength(1000);
		
		Track loc4trk2 = loc4.addTrack("Chelmsford Yard 2", Track.YARD);
		loc4trk2.setLength(1000);
		
		Location loc5 = lmanager.newLocation("Westford");
		
		Track loc5trk1 = loc5.addTrack("Westford Yard", Track.YARD);
		loc5trk1.setLength(1000);
		
		Track loc5trk2 = loc5.addTrack("Westford Yard 2", Track.YARD);
		loc5trk2.setLength(1000);
		
		// Create route with 4 location
		Setup.setCarMoves(7);	// set default to 7 moves per location
		Route rte1 = rmanager.newRoute("Route 2 Westford");
		rte1.addLocation(loc1);
		RouteLocation rl2 = rte1.addLocation(loc2);
		rl2.setTrainIconX(175);	// set the train icon coordinates
		rl2.setTrainIconY(150);
		RouteLocation rl3 = rte1.addLocation(loc3);
		rl3.setTrainIconX(25);	// set the train icon coordinates
		rl3.setTrainIconY(175);
		RouteLocation rl4 = rte1.addLocation(loc4);
		rl4.setTrainIconX(75);	// set the train icon coordinates
		rl4.setTrainIconY(175);
		
		// don't allow pickup or drops at Arlington
		rl2.setCanDrop(false);
		rl2.setCanPickup(false);
		
		// Create train
		Train train1 = tmanager.newTrain("Harvard to Chelmsford");
		train1.setRoute(rte1);
		
		// train skips Boston
		train1.addTrainSkipsLocation(rl3.getId());
				
		// turn off build fail messages
		tmanager.setBuildMessagesEnabled(false);
		
		// Place cars
		Assert.assertEquals("Place c1", Track.OKAY, c1.setLocation(loc1, loc1trk1));
		Assert.assertEquals("Place c2", Track.OKAY, c2.setLocation(loc1, loc1trk2));
		Assert.assertEquals("Place c3", Track.OKAY, c3.setLocation(loc2, loc2trk1));
		Assert.assertEquals("Place c4", Track.OKAY, c4.setLocation(loc5, loc5trk1));
		
		Assert.assertEquals("Place c5", Track.OKAY, c5.setLocation(loc1, loc1trk1));
		Assert.assertEquals("Place c6", Track.OKAY, c6.setLocation(loc1, loc1trk1));
		Assert.assertEquals("Place c7", Track.OKAY, c7.setLocation(loc1, loc1trk1));
		Assert.assertEquals("Place c8", Track.OKAY, c8.setLocation(loc1, loc1trk1));
		
		Assert.assertEquals("Place c9", Track.OKAY, c9.setLocation(loc1, loc1trk1));
		Assert.assertEquals("Place c10", Track.OKAY, c10.setLocation(loc2, loc2trk1));
		Assert.assertEquals("Place c11", Track.OKAY, c11.setLocation(loc2, loc2trk1));
		
		// set c9 destination not part of train's route
		Assert.assertEquals("Destination c9", Track.OKAY, c9.setDestination(loc5, loc5trk1));
		
		// Place engines
		Assert.assertEquals("Place e1", Track.OKAY, e1.setLocation(loc1, loc1trk1));
		Assert.assertEquals("Place e2", Track.OKAY, e2.setLocation(loc1, loc1trk1));
		Assert.assertEquals("Place e3", Track.OKAY, e3.setLocation(loc1, loc1trk1));
		Assert.assertEquals("Place e4", Track.OKAY, e4.setLocation(loc1, loc1trk1));
		Assert.assertEquals("Place e5", Track.OKAY, e5.setLocation(loc1, loc1trk1));
		Assert.assertEquals("Place e6", Track.OKAY, e6.setLocation(loc1, loc1trk1));
		Assert.assertEquals("Place e7", Track.OKAY, e7.setLocation(loc1, loc1trk1));
		Assert.assertEquals("Place e8", Track.OKAY, e8.setLocation(loc1, loc1trk1));
		
		train1.setRequirements(Train.CABOOSE);
		train1.setNumberEngines("2");
		train1.setOwnerOption(Train.ALLOWNERS);
		train1.build();
		Assert.assertEquals("Train 1 After Build 1", true, train1.isBuilt());
		
		// check destinations
		Assert.assertEquals("c1 destination 1", "", c1.getDestinationTrackName());
		Assert.assertEquals("c2 destination 1", "Chelmsford Yard 2", c2.getDestinationTrackName());
		Assert.assertEquals("c3 destination 1", "", c3.getDestinationTrackName());
		Assert.assertEquals("c4 destination 1", "", c4.getDestinationTrackName());
		
		Assert.assertEquals("c5 destination 1", "", c5.getDestinationTrackName());
		Assert.assertEquals("c6 destination 1", "", c6.getDestinationTrackName());
		Assert.assertEquals("c7 destination 1", "Chelmsford Yard", c7.getDestinationTrackName());
		Assert.assertEquals("c8 destination 1", "Chelmsford Yard 2", c8.getDestinationTrackName());
		
		Assert.assertEquals("c9 destination 1", "Westford Yard", c9.getDestinationTrackName());
		Assert.assertEquals("c10 destination 1", "", c10.getDestinationTrackName());
		Assert.assertEquals("c11 destination 1", "", c11.getDestinationTrackName());

		Assert.assertEquals("e1 destination 1", "Chelmsford Yard", e1.getDestinationTrackName());
		Assert.assertEquals("e2 destination 1", "Chelmsford Yard", e2.getDestinationTrackName());
		Assert.assertEquals("e3 destination 1", "", e3.getDestinationTrackName());
		Assert.assertEquals("e4 destination 1", "", e4.getDestinationTrackName());
		Assert.assertEquals("e5 destination 1", "", e5.getDestinationTrackName());
		Assert.assertEquals("e6 destination 1", "", e6.getDestinationTrackName());
		Assert.assertEquals("e7 destination 1", "", e7.getDestinationTrackName());
		Assert.assertEquals("e8 destination 1", "", e8.getDestinationTrackName());
		
		// Allow c9 to be used
		// set c9 destination Chelmsford
		Assert.assertEquals("Destination c9", Track.OKAY, c9.setDestination(loc4, null));
		
		// check that train direction and track direction feature works properly
		loc1trk2.setTrainDirections(Location.SOUTH);
		
		train1.build();
		Assert.assertEquals("Train 1 After Build test track direction", true, train1.isBuilt());
		
		// check destinations
		Assert.assertEquals("c1 destination 1a", "Chelmsford Yard", c1.getDestinationTrackName());
		Assert.assertEquals("c2 destination 1a", "", c2.getDestinationTrackName());
		Assert.assertEquals("c3 destination 1a", "", c3.getDestinationTrackName());
		Assert.assertEquals("c4 destination 1a", "", c4.getDestinationTrackName());
		
		Assert.assertEquals("c5 destination 1a", "", c5.getDestinationTrackName());
		Assert.assertEquals("c6 destination 1a", "", c6.getDestinationTrackName());
		Assert.assertEquals("c7 destination 1a", "Chelmsford Yard", c7.getDestinationTrackName());
		Assert.assertEquals("c8 destination 1a", "Chelmsford Yard 2", c8.getDestinationTrackName());
		
		Assert.assertEquals("c9 destination 1a", "Chelmsford Yard", c9.getDestinationTrackName());
		Assert.assertEquals("c10 destination 1a", "", c10.getDestinationTrackName());
		Assert.assertEquals("c11 destination 1a", "", c11.getDestinationTrackName());

		Assert.assertEquals("e1 destination 1a", "Chelmsford Yard 2", e1.getDestinationTrackName());
		Assert.assertEquals("e2 destination 1a", "Chelmsford Yard 2", e2.getDestinationTrackName());
		Assert.assertEquals("e3 destination 1a", "", e3.getDestinationTrackName());
		Assert.assertEquals("e4 destination 1a", "", e4.getDestinationTrackName());
		Assert.assertEquals("e5 destination 1a", "", e5.getDestinationTrackName());
		Assert.assertEquals("e6 destination 1a", "", e6.getDestinationTrackName());
		Assert.assertEquals("e7 destination 1a", "", e7.getDestinationTrackName());
		Assert.assertEquals("e8 destination 1a", "", e8.getDestinationTrackName());

		loc1trk2.setTrainDirections(Location.NORTH);
		
		train1.addOwnerName("DAB");
		train1.setOwnerOption(Train.INCLUDEOWNERS);
		
		train1.build();
		Assert.assertEquals("Train 1 After Build 2", true, train1.isBuilt());
		
		// check destinations
		Assert.assertEquals("c1 destination 2", "", c1.getDestinationTrackName());
		Assert.assertEquals("c2 destination 2", "Chelmsford Yard", c2.getDestinationTrackName());
		Assert.assertEquals("c3 destination 2", "", c3.getDestinationTrackName());
		Assert.assertEquals("c4 destination 2", "", c4.getDestinationTrackName());
		
		Assert.assertEquals("c5 destination 2", "", c5.getDestinationTrackName());
		Assert.assertEquals("c6 destination 2", "", c6.getDestinationTrackName());
		Assert.assertEquals("c7 destination 2", "Chelmsford Yard", c7.getDestinationTrackName());
		Assert.assertEquals("c8 destination 2", "Chelmsford Yard 2", c8.getDestinationTrackName());
		
		Assert.assertEquals("c9 destination 2", "Chelmsford Yard", c9.getDestinationTrackName());
		Assert.assertEquals("c10 destination 2", "", c10.getDestinationTrackName());
		Assert.assertEquals("c11 destination 2", "", c11.getDestinationTrackName());

		Assert.assertEquals("e1 destination 2", "", e1.getDestinationTrackName());
		Assert.assertEquals("e2 destination 2", "", e2.getDestinationTrackName());
		Assert.assertEquals("e3 destination 2", "", e3.getDestinationTrackName());
		Assert.assertEquals("e4 destination 2", "Chelmsford Yard 2", e4.getDestinationTrackName());
		Assert.assertEquals("e5 destination 2", "Chelmsford Yard 2", e5.getDestinationTrackName());
		Assert.assertEquals("e6 destination 2", "", e6.getDestinationTrackName());
		Assert.assertEquals("e7 destination 2", "", e7.getDestinationTrackName());
		Assert.assertEquals("e8 destination 2", "", e8.getDestinationTrackName());
		
		// test that build fails if Diesel type is not allowed
		train1.deleteTypeName("Diesel");
		train1.build();
		Assert.assertEquals("Train 1 After Build 3", false, train1.isBuilt());
		
		// restore type Diesel and allow all owners
		train1.addTypeName("Diesel");
		train1.setOwnerOption(Train.ALLOWNERS);
		train1.build();
		Assert.assertEquals("Train 1 After Build 4", true, train1.isBuilt());
		
		// Only allow rolling stock built after 1956
		train1.setBuiltStartYear("1956");
		train1.build();
		Assert.assertEquals("Train 1 After Build 5", true, train1.isBuilt());
		// check destinations
		Assert.assertEquals("c1 destination 5", "", c1.getDestinationTrackName());
		Assert.assertEquals("c2 destination 5", "Chelmsford Yard", c2.getDestinationTrackName());
		Assert.assertEquals("c3 destination 5", "", c3.getDestinationTrackName());
		Assert.assertEquals("c4 destination 5", "", c4.getDestinationTrackName());
		
		Assert.assertEquals("c5 destination 5", "", c5.getDestinationTrackName());
		Assert.assertEquals("c6 destination 5", "", c6.getDestinationTrackName());
		Assert.assertEquals("c7 destination 5", "Chelmsford Yard 2", c7.getDestinationTrackName());
		Assert.assertEquals("c8 destination 5", "Chelmsford Yard", c8.getDestinationTrackName());
		
		Assert.assertEquals("c9 destination 5", "", c9.getDestinationTrackName());
		Assert.assertEquals("c10 destination 5", "", c10.getDestinationTrackName());
		Assert.assertEquals("c11 destination 5", "", c11.getDestinationTrackName());

		Assert.assertEquals("e1 destination 5", "Chelmsford Yard 2", e1.getDestinationTrackName());
		Assert.assertEquals("e2 destination 5", "Chelmsford Yard 2", e2.getDestinationTrackName());
		Assert.assertEquals("e3 destination 5", "", e3.getDestinationTrackName());
		Assert.assertEquals("e4 destination 5", "", e4.getDestinationTrackName());
		Assert.assertEquals("e5 destination 5", "", e5.getDestinationTrackName());
		Assert.assertEquals("e6 destination 5", "", e6.getDestinationTrackName());
		Assert.assertEquals("e7 destination 5", "", e7.getDestinationTrackName());
		Assert.assertEquals("e8 destination 5", "", e8.getDestinationTrackName());
		
		train1.reset();

	}
	
	public void testAggressiveBuildOption() {
		
		Setup.setBuildAggressive(true);
		
		TrainManager tmanager = TrainManager.instance();
		RouteManager rmanager = RouteManager.instance();
		LocationManager lmanager = LocationManager.instance();
		EngineManager emanager = EngineManager.instance();
		CarManager cmanager = CarManager.instance();
		CarTypes ct = CarTypes.instance();
		EngineTypes et = EngineTypes.instance();
		
		// register the car and engine types used
		ct.addName("Boxcar");
		ct.addName("Caboose");
		ct.addName("Flat");
		et.addName("Diesel");
		
		// create 2 consists and a single engine for testing
		Consist con1 = emanager.newConsist("C1");
		
		Engine e1 = emanager.newEngine("UP", "1");
		e1.setModel("GP30");
		e1.setOwner("AT");
		e1.setBuilt("1957");
		e1.setConsist(con1);
		e1.setMoves(5);
		Engine e2 = emanager.newEngine("SP", "2");
		e2.setModel("GP30");
		e2.setOwner("AT");
		e2.setBuilt("1957");
		e2.setConsist(con1);
		e2.setMoves(5);
		
		// one engine
		Engine e3 = emanager.newEngine("SP", "3");
		e3.setModel("GP40");
		e3.setOwner("DAB");
		e3.setBuilt("1957");
		
		Consist con2 = emanager.newConsist("C2");
		
		Engine e4 = emanager.newEngine("UP", "10");
		e4.setModel("GP40");
		e4.setOwner("DAB");
		e4.setBuilt("1944");
		e4.setConsist(con2);
		e4.setMoves(20);
		Engine e5 = emanager.newEngine("SP", "20");
		e5.setModel("GP40");
		e5.setOwner("DAB");
		e5.setBuilt("1944");
		e5.setConsist(con2);
		e5.setMoves(20);
		
		// 3 engine consist
		Consist con3 = emanager.newConsist("C3");
		
		Engine e6 = emanager.newEngine("UP", "100");
		e6.setModel("GP40");
		e6.setOwner("DAB");
		e6.setBuilt("1944");
		e6.setConsist(con3);
		e6.setMoves(2);
		Engine e7 = emanager.newEngine("SP", "200");
		e7.setModel("GP40");
		e7.setOwner("DAB");
		e7.setBuilt("1944");
		e7.setConsist(con3);
		e7.setMoves(2);
		Engine e8 = emanager.newEngine("SP", "300");
		e8.setModel("GP40");
		e8.setOwner("DAB");
		e8.setBuilt("1944");
		e8.setConsist(con3);
		e8.setMoves(2);
		
		// Set up cars
		Car c1 = cmanager.newCar("PU", "13");
		c1.setType("Caboose");
		c1.setLength("32");
		c1.setMoves(10);
		c1.setOwner("AT");
		c1.setBuilt("1943");
		c1.setCaboose(true);
		
		Car c2 = cmanager.newCar("SP", "23");
		c2.setType("Boxcar");
		c2.setLength("30");
		c2.setMoves(5);
		c2.setOwner("DAB");
		c2.setBuilt("1957");
		
		Car c3 = cmanager.newCar("UP", "33");
		c3.setType("Boxcar");
		c3.setLength("33");
		c3.setMoves(0);
		c3.setOwner("DAB");
		c3.setBuilt("1944");
		
		Car c4 = cmanager.newCar("UP", "43");
		c4.setType("Boxcar");
		c4.setLength("40");
		c4.setMoves(16);
		c4.setOwner("DAB");
		c4.setBuilt("1958");

		Car c5 = cmanager.newCar("SP", "53");
		c5.setType("Boxcar");
		c5.setLength("40");
		c5.setMoves(8);
		c5.setOwner("DAB");
		c5.setBuilt("1958");
		
		Car c6 = cmanager.newCar("NH", "63");
		c6.setType("Boxcar");
		c6.setLength("40");
		c6.setMoves(2);
		c6.setOwner("DAB");
		c6.setBuilt("1958");
		
		Car c7 = cmanager.newCar("UP", "73");
		c7.setType("Flat");
		c7.setLength("40");
		c7.setMoves(5);
		c7.setOwner("DAB");
		c7.setBuilt("1958");

		Car c8 = cmanager.newCar("SP", "83");
		c8.setType("Boxcar");
		c8.setLength("40");
		c8.setMoves(4);
		c8.setOwner("DAB");
		c8.setBuilt("1958");
		
		Car c9 = cmanager.newCar("NH", "93");
		c9.setType("Boxcar");
		c9.setLength("40");
		c9.setMoves(3);
		c9.setOwner("DAB");
		c9.setBuilt("1944");
		
		Car c10 = cmanager.newCar("NH", "103");
		c10.setType("Boxcar");
		c10.setLength("40");
		c10.setMoves(10);
		c10.setOwner("DAB");
		c10.setBuilt("1958");
		
		Car c11 = cmanager.newCar("SP", "113");
		c11.setType("Boxcar");
		c11.setLength("40");
		c11.setMoves(3);
		c11.setOwner("DAB");
		c11.setBuilt("1958");

		// Create 5 locations
		Location loc1 = lmanager.newLocation("New Harvard");
		
		Track loc1trk1 = loc1.addTrack("Harvard Yard 1", Track.YARD);
		loc1trk1.setLength(1000);
		
		Track loc1trk2 = loc1.addTrack("Harvard Yard 2", Track.YARD);
		loc1trk2.setLength(1000);
		
		Location loc2 = lmanager.newLocation("New Arlington");
		
		Track loc2trk1 = loc2.addTrack("Arlington Siding", Track.SIDING);
		loc2trk1.setLength(50);
		
		Location loc3 = lmanager.newLocation("New Boston");
		
		Track loc3trk1 = loc3.addTrack("Boston Yard 1", Track.YARD);
		loc3trk1.setLength(50);
		
		Track loc3trk2 = loc3.addTrack("Boston Yard 2", Track.YARD);
		loc3trk2.setLength(50);
		
		Location loc4 = lmanager.newLocation("New Chelmsford");
		
		Track loc4trk1 = loc4.addTrack("Chelmsford Yard 1", Track.YARD);
		loc4trk1.setLength(50);
		
		Track loc4trk2 = loc4.addTrack("Chelmsford Yard 2", Track.YARD);
		loc4trk2.setLength(50);
		
		Location loc5 = lmanager.newLocation("New Westford");
		
		Track loc5trk1 = loc5.addTrack("Westford Yard 1", Track.YARD);
		loc5trk1.setLength(1000);
		
		Track loc5trk2 = loc5.addTrack("Westford Yard 2", Track.YARD);
		loc5trk2.setLength(1000);
		
		// Create route with 5 location
		Setup.setCarMoves(7);	// set default to 7 moves per location
		Route rte1 = rmanager.newRoute("Route 3 Westford");
		rte1.addLocation(loc1);
		rte1.addLocation(loc2);
		rte1.addLocation(loc3);
		rte1.addLocation(loc4);
		rte1.addLocation(loc5);		
		
		// Create train
		Train train1 = tmanager.newTrain("Harvard to Westford Aggressive");
		train1.setRoute(rte1);
				
		// turn off build fail messages
		tmanager.setBuildMessagesEnabled(false);
		
		// Place cars
		Assert.assertEquals("Place c1", Track.OKAY, c1.setLocation(loc1, loc1trk1));
		Assert.assertEquals("Place c2", Track.OKAY, c2.setLocation(loc1, loc1trk2));
		Assert.assertEquals("Place c3", Track.OKAY, c3.setLocation(loc2, loc2trk1));
		Assert.assertEquals("Place c4", Track.OKAY, c4.setLocation(loc1, loc1trk1));
		
		Assert.assertEquals("Place c5", Track.OKAY, c5.setLocation(loc1, loc1trk2));
		Assert.assertEquals("Place c6", Track.OKAY, c6.setLocation(loc1, loc1trk1));
		Assert.assertEquals("Place c7", Track.OKAY, c7.setLocation(loc3, loc3trk1));
		Assert.assertEquals("Place c8", Track.OKAY, c8.setLocation(loc3, loc3trk2));
		
		Assert.assertEquals("Place c9", Track.OKAY, c9.setLocation(loc4, loc4trk1));
		Assert.assertEquals("Place c10", Track.OKAY, c10.setLocation(loc4, loc4trk2));
		Assert.assertEquals("Place c11", Track.OKAY, c11.setLocation(loc1, loc1trk1));
		
		// Place engines
		Assert.assertEquals("Place e1", Track.OKAY, e1.setLocation(loc1, loc1trk1));
		Assert.assertEquals("Place e2", Track.OKAY, e2.setLocation(loc1, loc1trk1));
		Assert.assertEquals("Place e3", Track.OKAY, e3.setLocation(loc1, loc1trk1));
		Assert.assertEquals("Place e4", Track.OKAY, e4.setLocation(loc1, loc1trk1));
		Assert.assertEquals("Place e5", Track.OKAY, e5.setLocation(loc1, loc1trk1));
		Assert.assertEquals("Place e6", Track.OKAY, e6.setLocation(loc1, loc1trk1));
		Assert.assertEquals("Place e7", Track.OKAY, e7.setLocation(loc1, loc1trk1));
		Assert.assertEquals("Place e8", Track.OKAY, e8.setLocation(loc1, loc1trk1));
		
		train1.setRequirements(Train.CABOOSE);
		train1.setNumberEngines("3");
		train1.setOwnerOption(Train.ALLOWNERS);
		train1.build();
		Assert.assertEquals("Train 1 After Build 1", true, train1.isBuilt());
		
		// check destinations
		Assert.assertEquals("c1 destination 1", "Westford Yard 2", c1.getDestinationTrackName());
		Assert.assertEquals("c2 destination 1", "Westford Yard 1", c2.getDestinationTrackName());
		Assert.assertEquals("c3 destination 1", "Westford Yard 2", c3.getDestinationTrackName());
		Assert.assertEquals("c4 destination 1", "Boston Yard 1", c4.getDestinationTrackName());
		
		Assert.assertEquals("c5 destination 1", "Arlington Siding", c5.getDestinationTrackName());
		Assert.assertEquals("c6 destination 1", "Westford Yard 2", c6.getDestinationTrackName());
		Assert.assertEquals("c7 destination 1", "Westford Yard 2", c7.getDestinationTrackName());
		Assert.assertEquals("c8 destination 1", "Westford Yard 1", c8.getDestinationTrackName());
		
		Assert.assertEquals("c9 destination 1", "", c9.getDestinationTrackName());
		Assert.assertEquals("c10 destination 1", "", c10.getDestinationTrackName());
		Assert.assertEquals("c11 destination 1", "Westford Yard 2", c11.getDestinationTrackName());

		Assert.assertEquals("e1 destination 1", "", e1.getDestinationTrackName());
		Assert.assertEquals("e2 destination 1", "", e2.getDestinationTrackName());
		Assert.assertEquals("e3 destination 1", "", e3.getDestinationTrackName());
		Assert.assertEquals("e4 destination 1", "", e4.getDestinationTrackName());
		Assert.assertEquals("e5 destination 1", "", e5.getDestinationTrackName());
		Assert.assertEquals("e6 destination 1", "Westford Yard 1", e6.getDestinationTrackName());
		Assert.assertEquals("e7 destination 1", "Westford Yard 1", e7.getDestinationTrackName());
		Assert.assertEquals("e8 destination 1", "Westford Yard 1", e8.getDestinationTrackName());
		
		// test departing from staging in aggressive mode
		Assert.assertTrue(train1.reset());
		loc1trk1.setLocType(Track.STAGING);
		loc1trk2.setLocType(Track.STAGING);
		loc1.setLocationOps(Location.STAGING);
		train1.build();
		Assert.assertFalse("Train 1 After Build from staging, eight loco on departure track", train1.isBuilt());
		
		// move locos to other departure track
		Assert.assertEquals("Place e1", Track.OKAY, e1.setLocation(loc1, loc1trk2));
		Assert.assertEquals("Place e2", Track.OKAY, e2.setLocation(loc1, loc1trk2));
		Assert.assertEquals("Place e3", Track.OKAY, e3.setLocation(loc1, loc1trk2));
		Assert.assertEquals("Place e4", Track.OKAY, e4.setLocation(loc1, loc1trk2));
		Assert.assertEquals("Place e5", Track.OKAY, e5.setLocation(loc1, loc1trk2));
		
		train1.build();
		Assert.assertTrue("Train 1 After Build from staging, three loco on departure track", train1.isBuilt());
		
		// check destinations
		Assert.assertEquals("c1 destination 2", "Westford Yard 2", c1.getDestinationTrackName());
		Assert.assertEquals("c2 destination 2", "", c2.getDestinationTrackName());
		Assert.assertEquals("c3 destination 2", "Westford Yard 2", c3.getDestinationTrackName());
		Assert.assertEquals("c4 destination 2", "Boston Yard 2", c4.getDestinationTrackName());
		
		Assert.assertEquals("c5 destination 2", "", c5.getDestinationTrackName());
		Assert.assertEquals("c6 destination 2", "Arlington Siding", c6.getDestinationTrackName());
		Assert.assertEquals("c7 destination 2", "Westford Yard 1", c7.getDestinationTrackName());
		Assert.assertEquals("c8 destination 2", "Westford Yard 2", c8.getDestinationTrackName());
		
		Assert.assertEquals("c9 destination 2", "Westford Yard 2", c9.getDestinationTrackName());
		Assert.assertEquals("c10 destination 2", "Westford Yard 1", c10.getDestinationTrackName());
		Assert.assertEquals("c11 destination 2", "Chelmsford Yard 1", c11.getDestinationTrackName());

		Assert.assertEquals("e1 destination 2", "", e1.getDestinationTrackName());
		Assert.assertEquals("e2 destination 2", "", e2.getDestinationTrackName());
		Assert.assertEquals("e3 destination 2", "", e3.getDestinationTrackName());
		Assert.assertEquals("e4 destination 2", "", e4.getDestinationTrackName());
		Assert.assertEquals("e5 destination 2", "", e5.getDestinationTrackName());
		Assert.assertEquals("e6 destination 2", "Westford Yard 1", e6.getDestinationTrackName());
		Assert.assertEquals("e7 destination 2", "Westford Yard 1", e7.getDestinationTrackName());
		Assert.assertEquals("e8 destination 2", "Westford Yard 1", e8.getDestinationTrackName());
		
		// test train move to a specific location
		Assert.assertFalse("Old Harvard is not part of this trains route", train1.move("Old Harvard"));
		Assert.assertFalse("Train departs New Harvard already there", train1.move("New Harvard"));
		Assert.assertTrue("Next location in train's route is New Arlington", train1.move("New Arlington"));
		Assert.assertFalse("Train is at New Arlington", train1.move("New Arlington"));
		// next location is New Boston, skip it and go directly to New Chelmsford
		Assert.assertTrue("New Chelmsford is in train's route", train1.move("New Chelmsford"));
		Assert.assertTrue("Next location in train's route is New Westford", train1.move("New Westford"));
		Assert.assertFalse("Train is at New Westford last location in train's route", train1.move("New Westford"));
		
		train1.move();	// terminate train
		
		// now try with a train returning to staging, test alternate track feature
		// Create train
		Train train2 = tmanager.newTrain("Westford to Harvard Aggressive");
		Route rte2 = rmanager.copyRoute(rte1, "Route 4 Harvard" ,true);
		train2.setRoute(rte2);
		train2.setRequirements(Train.CABOOSE);
		train2.setNumberEngines("3");
		
		// add 2 yard tracks to siding at Arlington
		Track loc2trk2 = loc2.addTrack("Arlington Yard 1", Track.YARD);
		loc2trk2.setLength(50);		// only enough room for one car
		Track loc2trk3 = loc2.addTrack("Arlington Alternate Track", Track.SIDING);
		loc2trk3.setLength(100);	// only enough room for two cars
		
		// set the alternate for Arlington siding
		loc2trk1.setAlternativeTrack(loc2trk3);
		
		// send cars to Arlington siding
		c3.setNextDestination(loc2);
		c3.setNextDestTrack(loc2trk1);
		c8.setNextDestination(loc2);
		c8.setNextDestTrack(loc2trk1);
		c9.setNextDestination(loc2);
		c9.setNextDestTrack(loc2trk1);
		c11.setNextDestination(loc2);
		c11.setNextDestTrack(loc2trk1);
		
		train2.build();
		Assert.assertTrue("Train 2 returns to staging", train2.isBuilt());
		
		// check destinations
		Assert.assertEquals("c1 destination 3", "Harvard Yard 1", c1.getDestinationTrackName());
		Assert.assertEquals("c2 destination 3", "", c2.getDestinationTrackName());
		Assert.assertEquals("c3 destination 3", "Arlington Alternate Track", c3.getDestinationTrackName());
		Assert.assertEquals("c4 destination 3", "Harvard Yard 1", c4.getDestinationTrackName());
		
		Assert.assertEquals("c5 destination 3", "", c5.getDestinationTrackName());
		Assert.assertEquals("c6 destination 3", "Harvard Yard 1", c6.getDestinationTrackName());
		Assert.assertEquals("c7 destination 3", "Chelmsford Yard 2", c7.getDestinationTrackName());
		Assert.assertEquals("c8 destination 3", "Arlington Yard 1", c8.getDestinationTrackName());
		
		Assert.assertEquals("c9 destination 3", "Arlington Alternate Track", c9.getDestinationTrackName());
		Assert.assertEquals("c10 destination 3", "Boston Yard 1", c10.getDestinationTrackName());
		Assert.assertEquals("c11 destination 3", "Arlington Siding", c11.getDestinationTrackName());

		Assert.assertEquals("e1 destination 3", "", e1.getDestinationTrackName());
		Assert.assertEquals("e2 destination 3", "", e2.getDestinationTrackName());
		Assert.assertEquals("e3 destination 3", "", e3.getDestinationTrackName());
		Assert.assertEquals("e4 destination 3", "", e4.getDestinationTrackName());
		Assert.assertEquals("e5 destination 3", "", e5.getDestinationTrackName());
		Assert.assertEquals("e6 destination 3", "Harvard Yard 1", e6.getDestinationTrackName());
		Assert.assertEquals("e7 destination 3", "Harvard Yard 1", e7.getDestinationTrackName());
		Assert.assertEquals("e8 destination 3", "Harvard Yard 1", e8.getDestinationTrackName());
		
		// check that cars on alternate track are sent to Arlington Siding
		Assert.assertEquals("next dest Arlingtion", loc2, c3.getNextDestination());
		Assert.assertEquals("next dest track Arlingtion Siding", loc2trk1, c3.getNextDestTrack());
		Assert.assertEquals("next dest Arlingtion", loc2, c8.getNextDestination());
		Assert.assertEquals("next dest track Arlingtion Siding", loc2trk1, c8.getNextDestTrack());
		Assert.assertEquals("next dest Arlingtion", loc2, c9.getNextDestination());
		Assert.assertEquals("next dest track Arlingtion Siding", loc2trk1, c9.getNextDestTrack());
		Assert.assertEquals("next dest null", null, c11.getNextDestination());
		Assert.assertEquals("next dest track null", null, c11.getNextDestTrack());
		
		// test train move to a an exact location
		Assert.assertFalse("Old Harvard is not part of this trains route", train2.moveToNextLocation("Old Harvard"));
		Assert.assertFalse("New Harvard is the last location in this trains route", train2.moveToNextLocation("New Harvard"));
		Assert.assertFalse("New Boston is the 3rd to last location in this trains route", train2.moveToNextLocation("New Boston"));
		Assert.assertFalse("New Westford is the current location in this trains route", train2.moveToNextLocation("New Westford"));
		Assert.assertFalse("New Arlington is the 2nd to last location in this trains route", train2.moveToNextLocation("New Arlington"));
		Assert.assertTrue("New Chelmsford is the next location in this trains route", train2.moveToNextLocation("New Chelmsford"));
		Assert.assertTrue("New Boston is the next location in this trains route", train2.moveToNextLocation("New Boston"));
		Assert.assertTrue("New Arlington is the next location in this trains route", train2.moveToNextLocation("New Arlington"));
		Assert.assertTrue("New Harvard is the next location in this trains route", train2.moveToNextLocation("New Harvard"));
		Assert.assertFalse("Train is at New Harvard", train2.moveToNextLocation("New Harvard"));
		
		train2.move();	// terminate train
		
		// now test train returning to staging
		rte1.addLocation(loc1);
		train1.build();
		// should fail, can't return to staging track
		Assert.assertEquals("Train 1 deaprting and returning to staging", false, train1.isBuilt());
		// change mode
		Setup.setStagingTrackImmediatelyAvail(true);
		train1.build();
		Assert.assertEquals("Train 1 deaprting and returning to staging", true, train1.isBuilt());
		train1.terminate();
		
		// check car locations
		Assert.assertEquals("c1 location", "Harvard Yard 1", c1.getTrackName());
		Assert.assertEquals("c2 location", "Harvard Yard 2", c2.getTrackName());
		Assert.assertEquals("c3 location", "Arlington Alternate Track", c3.getTrackName());
		Assert.assertEquals("c4 location", "Boston Yard 2", c4.getTrackName());
		
		Assert.assertEquals("c5 location", "Harvard Yard 2", c5.getTrackName());
		Assert.assertEquals("c6 location", "Westford Yard 1", c6.getTrackName());
		Assert.assertEquals("c7 location", "Harvard Yard 1", c7.getTrackName());
		Assert.assertEquals("c8 location", "Arlington Siding", c8.getTrackName());
		
		Assert.assertEquals("c9 location", "Arlington Alternate Track", c9.getTrackName());
		Assert.assertEquals("c10 location", "Chelmsford Yard 1", c10.getTrackName());
		Assert.assertEquals("c11 location", "Westford Yard 2", c11.getTrackName());

		Assert.assertEquals("e1 location", "Harvard Yard 2", e1.getTrackName());
		Assert.assertEquals("e2 location", "Harvard Yard 2", e2.getTrackName());
		Assert.assertEquals("e3 location", "Harvard Yard 2", e3.getTrackName());
		Assert.assertEquals("e4 location", "Harvard Yard 2", e4.getTrackName());
		Assert.assertEquals("e5 location", "Harvard Yard 2", e5.getTrackName());
		Assert.assertEquals("e6 location", "Harvard Yard 1", e6.getTrackName());
		Assert.assertEquals("e7 location", "Harvard Yard 1", e7.getTrackName());
		Assert.assertEquals("e8 location", "Harvard Yard 1", e8.getTrackName());
		
		Setup.setBuildAggressive(false);

	}

	// test location Xml create support
	public void testXMLCreate() throws Exception {
		
		// confirm that file name has been modified
		Assert.assertEquals("test file name", "OperationsJUnitTestTrainRoster.xml", TrainManagerXml.instance().getOperationsFileName());
		
		RouteManager rmanager = RouteManager.instance();
		Route A = rmanager.newRoute("A");
		Route B = rmanager.newRoute("B");
		Route C = rmanager.newRoute("C");
		
		LocationManager lmanager = LocationManager.instance();
		Location Arlington = lmanager.newLocation("Arlington");
		Location Westford = lmanager.newLocation("Westford");
		Location Bedford = lmanager.newLocation("Bedford");
		
		RouteLocation startA = A.addLocation(Westford);
		startA.setTrainIconX(125);	// set the train icon coordinates
		startA.setTrainIconY(175);
		RouteLocation startB = B.addLocation(Arlington);
		startB.setTrainIconX(175);	// set the train icon coordinates
		startB.setTrainIconY(175);
		RouteLocation startC = C.addLocation(Bedford);
		startC.setTrainIconX(25);	// set the train icon coordinates
		startC.setTrainIconY(200);
		
		RouteLocation midC = C.addLocation(Arlington);
		RouteLocation endC = C.addLocation(Westford);
		
		
		TrainManager manager = TrainManager.instance();
		List<String> temptrainList = manager.getTrainsByIdList();

		Assert.assertEquals("Starting Number of Trains", 0, temptrainList.size());
		Train t1 = manager.newTrain("Test Number 1");
		Train t2 = manager.newTrain("Test Number 2");
		Train t3 = manager.newTrain("Test Number 3");

		temptrainList = manager.getTrainsByIdList();
		Assert.assertEquals("New Number of Trains", 3, temptrainList.size());
		
		EngineManager emanager = EngineManager.instance();
		Engine e1 = emanager.newEngine("UP", "1");
		Engine e2 = emanager.newEngine("UP", "2");
		Engine e3 = emanager.newEngine("UP", "3");

		// save in backup file
		t3.setBuildEnabled(true);
		t3.setBuildFailed(false);
		t3.setBuildTrainNormalEnabled(false);
		t3.setBuilt(true);
		t3.setBuiltEndYear("1950");
		t3.setBuiltStartYear("1925");
		t3.setCabooseRoad("t3 X caboose road");
		t3.setComment("t3 X comment");
		t3.setDescription("t3 X description");
		t3.setEngineModel("t3 X engine model");
		t3.setEngineRoad("t3 X engine road");
		t3.setLeadEngine(e1);
		t3.setLoadOption("t3 X load option");
		t3.setManifestLogoURL("t3 X pathName");
		t3.setNumberEngines("7");
		t3.setOwnerOption("t3 X owner option");
		t3.setRailroadName("t3 X railroad name");
		t3.setRequirements(Train.CABOOSE);
		t3.setRoadOption("t3 X raod option");
		t3.setRoute(B);
		t3.setStatus("t3 X status");
		
		
		TrainManagerXml.instance().writeOperationsFile();

		// Add some more engines and write file again
		// so we can test the backup facility
		Train t4 = manager.newTrain("Test Number 4");
		Train t5 = manager.newTrain("Test Number 5");
		Train t6 = manager.newTrain("Test Number 6");
		
		Assert.assertNotNull("train 1",t1);
		Assert.assertNotNull("train 2",t2);
		Assert.assertNotNull("train 3",t3);
		Assert.assertNotNull("train 4",t4);
		Assert.assertNotNull("train 5",t5);
		Assert.assertNotNull("train 6",t6);
		
		t1.setBuildEnabled(true);
		t1.setBuildFailed(true);
		t1.setBuildTrainNormalEnabled(true);
		t1.setBuilt(false);
		t1.setBuiltEndYear("1956");
		t1.setBuiltStartYear("1932");
		t1.setCabooseRoad("t1 caboose road");
		t1.setComment("t1 comment");
		t1.setCurrentLocation(startC);
		t1.setDepartureTime("1", "35");
		t1.setDescription("t1 description");
		t1.setEngineModel("t1 engine model");
		t1.setEngineRoad("t1 engine road");
		t1.setLeadEngine(e1);
		t1.setLoadOption("t1 load option");
		t1.setManifestLogoURL("t1 pathName");
		t1.setNumberEngines("1");
		t1.setOwnerOption("t1 owner option");
		t1.setRailroadName("t1 railroad name");
		t1.setRequirements(Train.NONE);
		t1.setRoadOption("t1 raod option");
		t1.setRoute(C);
		t1.setSecondLegCabooseRoad("t1 second leg caboose road");
		t1.setSecondLegEndLocation(midC);
		t1.setSecondLegEngineModel("t1 second leg engine model");
		t1.setSecondLegEngineRoad("t1 second leg engine road");
		t1.setSecondLegNumberEngines("5");
		t1.setSecondLegOptions(Train.ADD_CABOOSE);
		t1.setSecondLegStartLocation(endC);
		t1.setSendCarsToTerminalEnabled(true);
		t1.setStatus("t1 status");
		t1.setSwitchListStatus(Train.PRINTED);
		t1.setThirdLegCabooseRoad("t1 third leg caboose road");
		t1.setThirdLegEndLocation(startC);
		t1.setThirdLegEngineModel("t1 third leg engine model");
		t1.setThirdLegEngineRoad("t1 third leg engine road");
		t1.setThirdLegNumberEngines("3");
		t1.setThirdLegOptions(Train.HELPER_ENGINES);
		t1.setThirdLegStartLocation(midC);
		t1.addTrainSkipsLocation(midC.getId());
		
		t3.setBuildEnabled(false);
		t3.setBuildFailed(true);
		t3.setBuildTrainNormalEnabled(false);
		t3.setBuilt(false);
		t3.setBuiltEndYear("1955");
		t3.setBuiltStartYear("1931");
		t3.setCabooseRoad("t3 caboose road");
		t3.setComment("t3 comment");		
		t3.setCurrentLocation(startA);
		t3.setDepartureTime("4", "55");
		t3.setDescription("t3 description");
		t3.setEngineModel("t3 engine model");
		t3.setEngineRoad("t3 engine road");
		t3.setLeadEngine(e2);
		t3.setLoadOption("t3 load option");
		t3.setManifestLogoURL("t3 pathName");
		t3.setNumberEngines("1");
		t3.setOwnerOption("t3 owner option");
		t3.setRailroadName("t3 railroad name");
		t3.setRequirements(Train.NONE);
		t3.setRoadOption("t3 raod option");
		t3.setRoute(A);
		t3.setStatus("t3 status");
		
		t5.setBuildEnabled(true);
		t5.setBuildFailed(false);
		t5.setBuildTrainNormalEnabled(false);
		t5.setBuilt(true);
		t5.setBuiltEndYear("1954");
		t5.setBuiltStartYear("1930");
		t5.setCabooseRoad("t5 caboose road");
		t5.setComment("t5 comment");
		t5.setCurrentLocation(startB);
		t5.setDepartureTime("23", "15");
		t5.setDescription("t5 description");
		t5.setEngineModel("t5 engine model");
		t5.setEngineRoad("t5 engine road");
		t5.setLeadEngine(e3);
		t5.setLoadOption("t5 load option");
		t5.setManifestLogoURL("t5 pathName");
		t5.setNumberEngines("1");
		t5.setOwnerOption("t5 owner option");
		t5.setRailroadName("t5 railroad name");
		t5.setRequirements(Train.NONE);
		t5.setRoadOption("t5 raod option");
		t5.setRoute(B);
		t5.setStatus("t5 status");

		TrainManagerXml.instance().writeOperationsFile();
	}
	
	public void testXMLRead() throws JDOMException, IOException{
		
		// prevent swing access when loading train icon
		Setup.setPanelName("");
		
		RouteManager rmanager = RouteManager.instance();
		Route A = rmanager.getRouteByName("A");
		Route B = rmanager.getRouteByName("B");
		Route C = rmanager.getRouteByName("C");
		
		RouteLocation startC = C.getDepartsRouteLocation();
		RouteLocation midC = C.getLastLocationByName("Arlington");
		RouteLocation endC = C.getLastLocationByName("Westford");

		
		TrainManager manager = TrainManager.instance();
		List<String> temptrainList = manager.getTrainsByIdList();

		Assert.assertEquals("Starting Number of Trains", 0, temptrainList.size());
		
		TrainManagerXml.instance().readFile(TrainManagerXml.instance().getDefaultOperationsFilename());	
		
		temptrainList = manager.getTrainsByIdList();

		Assert.assertEquals("Number of Trains", 6, temptrainList.size());
		
		Train t1 = manager.getTrainByName("Test Number 1");
		Train t2 = manager.getTrainByName("Test Number 2");
		Train t3 = manager.getTrainByName("Test Number 3");
		Train t4 = manager.getTrainByName("Test Number 4");
		Train t5 = manager.getTrainByName("Test Number 5");
		Train t6 = manager.getTrainByName("Test Number 6");
		
		Assert.assertNotNull("train 1",t1);
		Assert.assertNotNull("train 2",t2);
		Assert.assertNotNull("train 3",t3);
		Assert.assertNotNull("train 4",t4);
		Assert.assertNotNull("train 5",t5);
		Assert.assertNotNull("train 6",t6);
		
		Assert.assertEquals("t1 build", true, t1.isBuildEnabled());
		Assert.assertEquals("t1 build failed", true, t1.getBuildFailed());
		Assert.assertEquals("t1 build normal", true, t1.isBuildTrainNormalEnabled());
		Assert.assertEquals("t1 built", false, t1.isBuilt());
		Assert.assertEquals("t1 built end year", "1956", t1.getBuiltEndYear());
		Assert.assertEquals("t1 built start year", "1932", t1.getBuiltStartYear());
		Assert.assertEquals("t1 caboose roadr", "t1 caboose road", t1.getCabooseRoad());
		Assert.assertEquals("t1 comment", "t1 comment", t1.getComment());
		Assert.assertEquals("t1 current location name", "Bedford", t1.getCurrentLocationName());
		Assert.assertEquals("t1 departure hour", "01", t1.getDepartureTimeHour());
		Assert.assertEquals("t1 departure minute", "35", t1.getDepartureTimeMinute());
		Assert.assertEquals("t1 engine model", "t1 engine model", t1.getEngineModel());
		Assert.assertEquals("t1 engine road", "t1 engine road", t1.getEngineRoad());
		Assert.assertEquals("t1 lead engine number", "1", t1.getLeadEngine().getNumber());
		Assert.assertEquals("t1 load option", "t1 load option", t1.getLoadOption());
		Assert.assertEquals("t1 path name", "t1 pathName", t1.getManifestLogoURL());
		Assert.assertEquals("t1 number of engines", "1", t1.getNumberEngines());
		Assert.assertEquals("t1 Owner option", "t1 owner option", t1.getOwnerOption());
		Assert.assertEquals("t1 railroad name", "t1 railroad name", t1.getRailroadName());
		Assert.assertEquals("t1 requirements", Train.NONE, t1.getRequirements());
		Assert.assertEquals("t1 road option", "t1 raod option", t1.getRoadOption());
		Assert.assertEquals("t1 route", C, t1.getRoute());
		Assert.assertEquals("t1 second leg caboose road", "t1 second leg caboose road", t1.getSecondLegCabooseRoad());
		Assert.assertEquals("t1 second leg end location", midC, t1.getSecondLegEndLocation());
		Assert.assertEquals("t1 second leg engine model", "t1 second leg engine model", t1.getSecondLegEngineModel());
		Assert.assertEquals("t1 second leg engine road", "t1 second leg engine road", t1.getSecondLegEngineRoad());
		Assert.assertEquals("t1 second leg number of engines", "5", t1.getSecondLegNumberEngines());
		Assert.assertEquals("t1 second leg options", Train.ADD_CABOOSE, t1.getSecondLegOptions());
		Assert.assertEquals("t1 second leg start location", endC, t1.getSecondLegStartLocation());
		Assert.assertEquals("t1 send cars to terminal", true, t1.isSendCarsToTerminalEnabled());
		Assert.assertEquals("t1 status", "t1 status", t1.getStatus());
		Assert.assertEquals("t1 switch list status", Train.PRINTED, t1.getSwitchListStatus());
		Assert.assertEquals("t1 third leg caboose road", "t1 third leg caboose road", t1.getThirdLegCabooseRoad());
		Assert.assertEquals("t1 third leg end location", startC, t1.getThirdLegEndLocation());
		Assert.assertEquals("t1 third leg engine model", "t1 third leg engine model", t1.getThirdLegEngineModel());
		Assert.assertEquals("t1 third leg engine road", "t1 third leg engine road", t1.getThirdLegEngineRoad());
		Assert.assertEquals("t1 third leg number of engines", "3", t1.getThirdLegNumberEngines());
		Assert.assertEquals("t1 third leg options", Train.HELPER_ENGINES, t1.getThirdLegOptions());
		Assert.assertEquals("t1 third leg start location", midC, t1.getThirdLegStartLocation());
		Assert.assertEquals("t1 skips location", false, t1.skipsLocation(startC.getId()));
		Assert.assertEquals("t1 skips location", true, t1.skipsLocation(midC.getId()));
		Assert.assertEquals("t1 skips location", false, t1.skipsLocation(endC.getId()));
				
		Assert.assertEquals("t3 build", false, t3.isBuildEnabled());
		Assert.assertEquals("t3 build failed", true, t3.getBuildFailed());
		Assert.assertEquals("t3 build normal", false, t3.isBuildTrainNormalEnabled());
		Assert.assertEquals("t3 built", false, t3.isBuilt());
		Assert.assertEquals("t3 built end year", "1955", t3.getBuiltEndYear());
		Assert.assertEquals("t3 built start year", "1931", t3.getBuiltStartYear());
		Assert.assertEquals("t3 caboose roadr", "t3 caboose road", t3.getCabooseRoad());
		Assert.assertEquals("t3 comment", "t3 comment", t3.getComment());
		Assert.assertEquals("t3 current location name", "Westford", t3.getCurrentLocationName());
		Assert.assertEquals("t3 departure hour", "04", t3.getDepartureTimeHour());
		Assert.assertEquals("t3 departure minute", "55", t3.getDepartureTimeMinute());
		Assert.assertEquals("t3 engine model", "t3 engine model", t3.getEngineModel());
		Assert.assertEquals("t3 engine road", "t3 engine road", t3.getEngineRoad());
		Assert.assertEquals("t3 lead engine number", "2", t3.getLeadEngine().getNumber());
		Assert.assertEquals("t3 load option", "t3 load option", t3.getLoadOption());
		Assert.assertEquals("t3 path name", "t3 pathName", t3.getManifestLogoURL());
		Assert.assertEquals("t3 number of engines", "1", t3.getNumberEngines());
		Assert.assertEquals("t3 Owner option", "t3 owner option", t3.getOwnerOption());
		Assert.assertEquals("t3 railroad name", "t3 railroad name", t3.getRailroadName());
		Assert.assertEquals("t3 requirements", Train.NONE, t3.getRequirements());
		Assert.assertEquals("t3 road option", "t3 raod option", t3.getRoadOption());
		Assert.assertEquals("t3 route", A, t3.getRoute());
		// test second leg defaults
		Assert.assertEquals("t3 second leg caboose road", "", t3.getSecondLegCabooseRoad());
		Assert.assertEquals("t3 second leg end location", null, t3.getSecondLegEndLocation());
		Assert.assertEquals("t3 second leg engine model", "", t3.getSecondLegEngineModel());
		Assert.assertEquals("t3 second leg engine road", "", t3.getSecondLegEngineRoad());
		Assert.assertEquals("t3 second leg number of engines", "0", t3.getSecondLegNumberEngines());
		Assert.assertEquals("t3 second leg options", Train.NONE, t3.getSecondLegOptions());
		Assert.assertEquals("t3 second leg start location", null, t3.getSecondLegStartLocation());
		Assert.assertEquals("t3 send cars to terminal", false, t3.isSendCarsToTerminalEnabled());
		Assert.assertEquals("t3 status", "t3 status", t3.getStatus());
		Assert.assertEquals("t3 switch list status", Train.UNKNOWN, t3.getSwitchListStatus());
		// test third leg defaults
		Assert.assertEquals("t3 third leg caboose road", "", t3.getThirdLegCabooseRoad());
		Assert.assertEquals("t3 third leg end location", null, t3.getThirdLegEndLocation());
		Assert.assertEquals("t3 third leg engine model", "", t3.getThirdLegEngineModel());
		Assert.assertEquals("t3 third leg engine road", "", t3.getThirdLegEngineRoad());
		Assert.assertEquals("t3 third leg number of engines", "0", t3.getThirdLegNumberEngines());
		Assert.assertEquals("t3 third leg options", Train.NONE, t3.getThirdLegOptions());
		Assert.assertEquals("t3 third leg start location", null, t3.getThirdLegStartLocation());

		Assert.assertEquals("t5 build", true, t5.isBuildEnabled());
		Assert.assertEquals("t5 build failed", false, t5.getBuildFailed());
		Assert.assertEquals("t5 built", true, t5.isBuilt());
		Assert.assertEquals("t5 built end year", "1954", t5.getBuiltEndYear());
		Assert.assertEquals("t5 built start year", "1930", t5.getBuiltStartYear());
		Assert.assertEquals("t5 caboose roadr", "t5 caboose road", t5.getCabooseRoad());
		Assert.assertEquals("t5 comment", "t5 comment", t5.getComment());
		Assert.assertEquals("t5 current location name", "Arlington", t5.getCurrentLocationName());
		Assert.assertEquals("t5 departure hour", "23", t5.getDepartureTimeHour());
		Assert.assertEquals("t5 departure minute", "15", t5.getDepartureTimeMinute());
		Assert.assertEquals("t5 engine model", "t5 engine model", t5.getEngineModel());
		Assert.assertEquals("t5 engine road", "t5 engine road", t5.getEngineRoad());
		Assert.assertEquals("t5 lead engine number", "3", t5.getLeadEngine().getNumber());
		Assert.assertEquals("t5 load option", "t5 load option", t5.getLoadOption());
		Assert.assertEquals("t5 path name", "t5 pathName", t5.getManifestLogoURL());
		Assert.assertEquals("t5 number of engines", "1", t5.getNumberEngines());
		Assert.assertEquals("t5 Owner option", "t5 owner option", t5.getOwnerOption());
		Assert.assertEquals("t5 railroad name", "t5 railroad name", t5.getRailroadName());
		Assert.assertEquals("t5 requirements", Train.NONE, t5.getRequirements());
		Assert.assertEquals("t5 road option", "t5 raod option", t5.getRoadOption());
		Assert.assertEquals("t5 route", B, t5.getRoute());
		Assert.assertEquals("t5 status", "t5 status", t5.getStatus());
		
	}
	
	public void testXMLreadBackup() throws JDOMException, IOException{
		TrainManager manager = TrainManager.instance();
		List<String> temptrainList = manager.getTrainsByIdList();

		Assert.assertEquals("Starting Number of Trains", 0, temptrainList.size());

		// set file name to backup
		TrainManagerXml.instance().setOperationsFileName("OperationsJUnitTestTrainRoster.xml.bak");
		
		TrainManagerXml.instance().readFile(TrainManagerXml.instance().getDefaultOperationsFilename());
		
		// restore file name
		TrainManagerXml.instance().setOperationsFileName("OperationsJUnitTestTrainRoster.xml");
		
		temptrainList = manager.getTrainsByIdList();

		Assert.assertEquals("Number of Trains", 3, temptrainList.size());
		
		Train t1 = manager.getTrainByName("Test Number 1");
		Train t2 = manager.getTrainByName("Test Number 2");
		Train t3 = manager.getTrainByName("Test Number 3");
		Train t4 = manager.getTrainByName("Test Number 4");
		Train t5 = manager.getTrainByName("Test Number 5");
		Train t6 = manager.getTrainByName("Test Number 6");
		
		Assert.assertNotNull("train 1",t1);
		Assert.assertNotNull("train 2",t2);
		Assert.assertNotNull("train 3",t3);
		Assert.assertNull("train 4",t4);
		Assert.assertNull("train 5",t5);
		Assert.assertNull("train 6",t6);
		
		Assert.assertEquals("t3 build", true, t3.isBuildEnabled());
		Assert.assertEquals("t3 build failed", false, t3.getBuildFailed());
		Assert.assertEquals("t3 built", true, t3.isBuilt());
		Assert.assertEquals("t3 built end year", "1950", t3.getBuiltEndYear());
		Assert.assertEquals("t3 built start year", "1925", t3.getBuiltStartYear());
		Assert.assertEquals("t3 caboose roadr", "t3 X caboose road", t3.getCabooseRoad());
		Assert.assertEquals("t3 comment", "t3 X comment", t3.getComment());
		Assert.assertEquals("t3 engine model", "t3 X engine model", t3.getEngineModel());
		Assert.assertEquals("t3 engine road", "t3 X engine road", t3.getEngineRoad());
		Assert.assertEquals("t3 load option", "t3 X load option", t3.getLoadOption());
		Assert.assertEquals("t3 path name", "t3 X pathName", t3.getManifestLogoURL());
		Assert.assertEquals("t3 number of engines", "7", t3.getNumberEngines());
		Assert.assertEquals("t3 Owner option", "t3 X owner option", t3.getOwnerOption());
		Assert.assertEquals("t3 railroad name", "t3 X railroad name", t3.getRailroadName());
		Assert.assertEquals("t3 requirements", Train.CABOOSE, t3.getRequirements());
		Assert.assertEquals("t3 raod option", "t3 X raod option", t3.getRoadOption());
		Assert.assertEquals("t3 status", "t3 X status", t3.getStatus());
		
		LocationManager.instance().dispose();

	}

	// TODO: Add test of build

	// from here down is testing infrastructure

	// Ensure minimal setup for log4J
	@Override
	protected void setUp() {
		apps.tests.Log4JFixture.setUp();
		
		// set the locale to US English
		Locale.setDefault(Locale.ENGLISH);

		// Need to clear out TrainManager global variables
		TrainManager.instance().dispose();
		LocationManager.instance().dispose();
	}

	public OperationsTrainsTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {"-noloading", OperationsTrainsTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(OperationsTrainsTest.class);
		return suite;
	}

	// The minimal setup for log4J
	@Override
	protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }
	
	private String getTrainStatus(Train train) {
		String[] status = train.getStatus().split(" ");
		return status[0];
	}
}
