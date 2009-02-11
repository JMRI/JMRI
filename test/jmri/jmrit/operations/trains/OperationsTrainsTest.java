// OperationsTrainsTest.java

package jmri.jmrit.operations.trains;

import java.io.File;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.jmrit.operations.rollingstock.cars.CarTypes;
import jmri.jmrit.operations.rollingstock.engines.Consist;
import jmri.jmrit.operations.rollingstock.engines.Engine;
import jmri.jmrit.operations.rollingstock.engines.EngineTypes;
import jmri.jmrit.operations.rollingstock.engines.EngineManager;
import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.routes.RouteLocation;

import java.util.List;
import jmri.jmrit.XmlFile;
import jmri.jmrit.operations.locations.LocationManagerXml;
import jmri.jmrit.operations.rollingstock.cars.CarColors;
import jmri.jmrit.operations.rollingstock.cars.CarLengths;
import jmri.jmrit.operations.rollingstock.cars.CarLoads;
import jmri.jmrit.operations.rollingstock.cars.CarManagerXml;
import jmri.jmrit.operations.rollingstock.cars.CarOwners;
import jmri.jmrit.operations.rollingstock.cars.CarRoads;
import jmri.jmrit.operations.rollingstock.engines.EngineManagerXml;
import jmri.jmrit.operations.rollingstock.engines.EngineModels;
import jmri.jmrit.operations.routes.RouteManager;
import jmri.jmrit.operations.routes.RouteManagerXml;
import jmri.jmrit.operations.setup.OperationsXml;

/**
 * Tests for the Operations Trains class
 * Last manually cross-checked on 20090131
 * 
 * Still to do:
 *  Train: DepartureTime, ArrivalTime
 *  Train: numberCarsWorked
 *  Train: isTraininRoute
 *  Train: getBuild, setBuild, buildIfSelected
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
 * @version $Revision: 1.14 $
 */
public class OperationsTrainsTest extends TestCase {

    synchronized void releaseThread() {
		try {
		    Thread.sleep(20);
			// super.wait(100);
		}
		catch (InterruptedException e) {
		    Assert.fail("failed due to InterruptedException");
		}
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
                
		Assert.assertEquals("Train Constant DISPOSE_CHANGED_PROPERTY", "dispose", Train.DISPOSE_CHANGED_PROPERTY);
		Assert.assertEquals("Train Constant STOPS_CHANGED_PROPERTY", "stops", Train.STOPS_CHANGED_PROPERTY);
		Assert.assertEquals("Train Constant TYPES_CHANGED_PROPERTY", "Types", Train.TYPES_CHANGED_PROPERTY);
		Assert.assertEquals("Train Constant ROADS_CHANGED_PROPERTY", "Road", Train.ROADS_CHANGED_PROPERTY);
		Assert.assertEquals("Train Constant LENGTH_CHANGED_PROPERTY", "length", Train.LENGTH_CHANGED_PROPERTY);
		Assert.assertEquals("Train Constant ENGINELOCATION_CHANGED_PROPERTY", "EngineLocation", Train.ENGINELOCATION_CHANGED_PROPERTY);
		Assert.assertEquals("Train Constant NUMBERCARS_CHANGED_PROPERTY", "numberCarsMoves", Train.NUMBERCARS_CHANGED_PROPERTY);
		Assert.assertEquals("Train Constant STATUS_CHANGED_PROPERTY", "status", Train.STATUS_CHANGED_PROPERTY);
		Assert.assertEquals("Train Constant DEPARTURETIME_CHANGED_PROPERTY", "departureTime", Train.DEPARTURETIME_CHANGED_PROPERTY);

//  Comment out test that relies upon a typo in the properties until that gets fixed
//		Assert.assertEquals("Train Constant AUTO", "Auto", t1.AUTO);
	}

	// test TrainIcon attributes
	public void testTrainIconAttributes() {
		Train train1 = new Train("TESTTRAINID", "TESTTRAINNAME");

		Assert.assertEquals("Train Id", "TESTTRAINID", train1.getId());
		Assert.assertEquals("Train Name", "TESTTRAINNAME", train1.getName());
		Assert.assertEquals("Train toString", "TESTTRAINNAME", train1.toString());

                TrainIcon trainicon1 = new TrainIcon();
                trainicon1.setTrain(train1);
		Assert.assertEquals("TrainIcon set train", "TESTTRAINNAME", trainicon1.getTrain().getName());
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
		Assert.assertTrue("Train Built true", train1.getBuilt());
		train1.setBuilt(false);
		Assert.assertFalse("Train Built false", train1.getBuilt());
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
		RouteLocation rladd;
		Location l1 = new Location("TESTLOCATIONID1", "TESTNEWROUTEDEPTNAME");
		rladd = rnew.addLocation(l1);
		Location l2 = new Location("TESTLOCATIONID2", "TESTLOCATIONNAME2");
		rladd = rnew.addLocation(l2);
		Location l3 = new Location("TESTLOCATIONID3", "TESTNEWROUTECURRNAME");
		rladd = rnew.addLocation(l3);
		Location l4 = new Location("TESTLOCATIONID4", "TESTLOCATIONNAME4");
		rladd = rnew.addLocation(l4);
		Location l5 = new Location("TESTLOCATIONID5", "TESTNEWROUTETERMNAME");
		rladd = rnew.addLocation(l5);

		train1.setRoute(rnew);
		Assert.assertEquals("Train New Route Name", "TESTNEWROUTENAME", train1.getTrainRouteName());

		Assert.assertEquals("Train New Route Departure Name", "TESTNEWROUTEDEPTNAME", train1.getTrainDepartsName());
		Assert.assertEquals("Train New Route Terminates Name", "TESTNEWROUTETERMNAME", train1.getTrainTerminatesName());

		RouteLocation rl1test;
		rl1test= rnew.getLocationByName("TESTNEWROUTECURRNAME");
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

		train1.addTypeName("Caboose");
		Assert.assertTrue("Train accepts type name Caboose", train1.acceptsTypeName("Caboose"));
		Assert.assertFalse("Train does not accept type name Hopper", train1.acceptsTypeName("Hopper"));

		train1.addTypeName("Hopper");
		Assert.assertTrue("Train still accepts type name Caboose", train1.acceptsTypeName("Caboose"));
		Assert.assertTrue("Train accepts type name Hopper", train1.acceptsTypeName("Hopper"));

		train1.deleteTypeName("Caboose");
		Assert.assertFalse("Train no longer accepts type name Caboose", train1.acceptsTypeName("Caboose"));
		Assert.assertTrue("Train still accepts type name Hopper", train1.acceptsTypeName("Hopper"));
	}

	// test train accepts road names support
	public void testAcceptsRoadNames() {
		Train train1 = new Train("TESTTRAINID", "TESTTRAINNAME");

		Assert.assertEquals("Train Id", "TESTTRAINID", train1.getId());
		Assert.assertEquals("Train Name", "TESTTRAINNAME", train1.getName());

		train1.setRoadOption("ALLROADS");
		Assert.assertTrue("Train accepts (ALLROADS) Road name CP", train1.acceptsRoadName("CP"));
		Assert.assertTrue("Train accepts (ALLROADS) Road name VIA", train1.acceptsRoadName("VIA"));

		train1.setRoadOption("Include");
		train1.addRoadName("CP");
		Assert.assertTrue("Train accepts (INCLUDEROADS) Road name CP", train1.acceptsRoadName("CP"));
		Assert.assertFalse("Train does not accept (INCLUDEROADS) Road name VIA", train1.acceptsRoadName("VIA"));

		train1.addRoadName("VIA");
		Assert.assertTrue("Train still accepts (INCLUDEROADS) Road name CP", train1.acceptsRoadName("CP"));
		Assert.assertTrue("Train accepts (INCLUDEROADS) Road name VIA", train1.acceptsRoadName("VIA"));

		train1.deleteRoadName("CP");
		Assert.assertFalse("Train no longer accepts (INCLUDEROADS) Road name CP", train1.acceptsRoadName("CP"));
		Assert.assertTrue("Train still accepts (INCLUDEROADS) Road name VIA", train1.acceptsRoadName("VIA"));

		train1.setRoadOption("Exclude");
		Assert.assertTrue("Train does accept (EXCLUDEROADS) Road name CP", train1.acceptsRoadName("CP"));
		Assert.assertFalse("Train does not accept (EXCLUDEROADS) Road name VIA", train1.acceptsRoadName("VIA"));

		train1.addRoadName("CP");
		Assert.assertFalse("Train does not accept (EXCLUDEROADS) Road name CP", train1.acceptsRoadName("CP"));
		Assert.assertFalse("Train still does not accept (EXCLUDEROADS) Road name VIA", train1.acceptsRoadName("VIA"));

		train1.deleteRoadName("VIA");
		Assert.assertFalse("Train still does not accepts (EXCLUDEROADS) Road name CP", train1.acceptsRoadName("CP"));
		Assert.assertTrue("Train now accepts (EXCLUDEROADS) Road name VIA", train1.acceptsRoadName("VIA"));
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
        		et.addName("Diesel");

                // Set up four engines in two consists 
                Consist con1 = new Consist("C16");
                Consist con2 = new Consist("C14");
                
                Engine e1 = new Engine("CP", "5016");
                e1.setModel("GP40");
                e1.setConsist(con1);
		Assert.assertEquals("Engine 1 Length", "59", e1.getLength());
                emanager.register(e1);
                
                Engine e2 = new Engine("CP", "5019");
                e2.setModel("GP40");
                e2.setConsist(con1);
		Assert.assertEquals("Engine 2 Length", "59", e2.getLength());
                emanager.register(e2);
                
                Engine e3 = new Engine("CP", "5524");
                e3.setModel("SD45");
                e3.setConsist(con2);
		Assert.assertEquals("Engine 3 Length", "66", e3.getLength());
                emanager.register(e3);

                Engine e4 = new Engine("CP", "5559");
                e4.setModel("SD45");
                e4.setConsist(con2);
		Assert.assertEquals("Engine 4 Length", "66", e4.getLength());
                emanager.register(e4);
                
                // Set up two cabooses and four box cars
                Car c1 = new Car("CP", "C10099");
                c1.setType("Caboose");
                c1.setLength("32");
                c1.setCaboose(true);
		Assert.assertEquals("Caboose 1 Length", "32", c1.getLength());
                cmanager.register(c1);
                
                Car c2 = new Car("CP", "C20099");
                c2.setType("Caboose");
                c2.setLength("32");
                c2.setCaboose(true);
		Assert.assertEquals("Caboose 2 Length", "32", c2.getLength());
                cmanager.register(c2);
                
                Car c3 = new Car("CP", "X10001");
                c3.setType("Boxcar");
                c3.setLength("40");
		Assert.assertEquals("Box Car X10001 Length", "40", c3.getLength());
                cmanager.register(c3);
                
                Car c4 = new Car("CP", "X10002");
                c4.setType("Boxcar");
                c4.setLength("40");
		Assert.assertEquals("Box Car X10002 Length", "40", c4.getLength());
                cmanager.register(c4);
                
                Car c5 = new Car("CP", "X20001");
                c5.setType("Boxcar");
                c5.setLength("40");
		Assert.assertEquals("Box Car X20001 Length", "40", c5.getLength());
                cmanager.register(c5);
                
                Car c6 = new Car("CP", "X20002");
                c6.setType("Boxcar");
                c6.setLength("40");
		Assert.assertEquals("Box Car X20002 Length", "40", c6.getLength());
                cmanager.register(c6);

                
                // Set up a route of 3 locations: North End Staging (2 tracks), 
                // North Industries, and South End Staging (2 tracks).
                Location l1 = new Location("1", "North End");
		Assert.assertEquals("Location 1 Id", "1", l1.getId());
		Assert.assertEquals("Location 1 Name", "North End", l1.getName());
		Assert.assertEquals("Location 1 Initial Length", 0, l1.getLength());
                l1.setLocationOps(Location.STAGING);
                l1.setTrainDirections(15);
                l1.setSwitchList(true);
                l1.addTypeName("Diesel");
                l1.addTypeName("Boxcar");
                l1.addTypeName("Caboose");
                lmanager.register(l1);
                
                Track l1s1 = new Track("1s1", "North End 1", Track.STAGING);
                l1s1.setLength(300);
		Assert.assertEquals("Location 1s1 Id", "1s1", l1s1.getId());
		Assert.assertEquals("Location 1s1 Name", "North End 1", l1s1.getName());
		Assert.assertEquals("Location 1s1 LocType", "Staging", l1s1.getLocType());
		Assert.assertEquals("Location 1s1 Length", 300, l1s1.getLength());
                l1s1.setTrainDirections(15);
                l1s1.addTypeName("Diesel");
                l1s1.addTypeName("Boxcar");
                l1s1.addTypeName("Caboose");
                l1s1.setRoadOption("All");
                l1s1.setDropOption("Any");
                l1s1.setPickupOption("Any");

                Track l1s2 = new Track("1s2", "North End 2", Track.STAGING);
                l1s2.setLength(400);
		Assert.assertEquals("Location 1s2 Id", "1s2", l1s2.getId());
		Assert.assertEquals("Location 1s2 Name", "North End 2", l1s2.getName());
		Assert.assertEquals("Location 1s2 LocType", "Staging", l1s2.getLocType());
		Assert.assertEquals("Location 1s2 Length", 400, l1s2.getLength());
                l1s2.setTrainDirections(15);
                l1s2.addTypeName("Diesel");
                l1s2.addTypeName("Boxcar");
                l1s2.addTypeName("Caboose");
                l1s2.setRoadOption("All");
                l1s2.setDropOption("Any");
                l1s2.setPickupOption("Any");

                l1.addTrack("North End 1", Track.STAGING);
                l1.addTrack("North End 2", Track.STAGING);
                List templist1 = l1.getTracksByNameList("");
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
                l2.setTrainDirections(15);
                l2.setSwitchList(true);
                l2.addTypeName("Diesel");
                l2.addTypeName("Boxcar");
                l2.addTypeName("Caboose");
                l2.setLength(200);
                lmanager.register(l2);
		Assert.assertEquals("Location 2 Length", 200, l2.getLength());

                Location l3 = new Location("3", "South End");
		Assert.assertEquals("Location 3 Id", "3", l3.getId());
		Assert.assertEquals("Location 3 Name", "South End", l3.getName());
		Assert.assertEquals("Location 3 Initial Length", 0, l3.getLength());
                l3.setLocationOps(Location.STAGING);
                l3.setTrainDirections(15);
                l3.setSwitchList(true);
                l3.addTypeName("Diesel");
                l3.addTypeName("Boxcar");
                l3.addTypeName("Caboose");
                lmanager.register(l3);

                Track l3s1 = new Track("3s1", "South End 1", Track.STAGING);
                l3s1.setLength(300);
		Assert.assertEquals("Location 3s1 Id", "3s1", l3s1.getId());
		Assert.assertEquals("Location 3s1 Name", "South End 1", l3s1.getName());
		Assert.assertEquals("Location 3s1 LocType", "Staging", l3s1.getLocType());
		Assert.assertEquals("Location 3s1 Length", 300, l3s1.getLength());
                l3s1.setTrainDirections(15);
                l3s1.addTypeName("Diesel");
                l3s1.addTypeName("Boxcar");
                l3s1.addTypeName("Caboose");
                l3s1.setRoadOption("All");
                l3s1.setDropOption("Any");
                l3s1.setPickupOption("Any");
                
                Track l3s2 = new Track("3s2", "South End 2", Track.STAGING);
                l3s2.setLength(400);
		Assert.assertEquals("Location 3s2 Id", "3s2", l3s2.getId());
		Assert.assertEquals("Location 3s2 Name", "South End 2", l3s2.getName());
		Assert.assertEquals("Location 3s2 LocType", "Staging", l3s2.getLocType());
		Assert.assertEquals("Location 3s2 Length", 400, l3s2.getLength());
                l3s2.setTrainDirections(15);
                l3s2.addTypeName("Diesel");
                l3s2.addTypeName("Boxcar");
                l3s2.addTypeName("Caboose");
                l3s2.setRoadOption("All");
                l3s2.setDropOption("Any");
                l3s2.setPickupOption("Any");
                
                l3.addTrack("South End 1", Track.STAGING);
                l3.addTrack("South End 2", Track.STAGING);
                List templist3 = l3.getTracksByNameList("");
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
                
		Assert.assertEquals("Location 3 Length", 700, l3.getLength());

                // Place Engines on Staging tracks
                Assert.assertEquals("Location 1s1 Init Used Length", 0, l1s1.getUsedLength());
                Assert.assertEquals("Location 1 Init Used Length", 0, l1s1.getUsedLength());
                Assert.assertEquals("Place e1", Engine.OKAY, e1.setLocation(l1, l1s1));
		Assert.assertEquals("Location 1s1 e1 Used Length", 63, l1s1.getUsedLength());
		Assert.assertEquals("Location 1 e1 Used Length", 63, l1.getUsedLength());
                Assert.assertEquals("Place e2", Engine.OKAY, e2.setLocation(l1, l1s1));
		Assert.assertEquals("Location 1s1 e2 Used Length", 126, l1s1.getUsedLength());
		Assert.assertEquals("Location 1 e2 Used Length", 126, l1.getUsedLength());

                Assert.assertEquals("Location 1s2 Init Used Length", 0, l1s2.getUsedLength());
                Assert.assertEquals("Place e3", Engine.OKAY, e3.setLocation(l1, l1s2));
		Assert.assertEquals("Location 1s2 e3 Used Length", 70, l1s2.getUsedLength());
		Assert.assertEquals("Location 1 e3 Used Length", 196, l1.getUsedLength());
                Assert.assertEquals("Place e4", Engine.OKAY, e4.setLocation(l1, l1s2));
		Assert.assertEquals("Location 1s2 e4 Used Length", 140, l1s2.getUsedLength());
		Assert.assertEquals("Location 1 e4 Used Length", 266, l1.getUsedLength());

                // Place Boxcars on Staging tracks
                Assert.assertTrue("l1 Accepts Boxcar", l1.acceptsTypeName("Boxcar"));
                Assert.assertTrue("l1s1 Accepts Boxcar", l1s1.acceptsTypeName("Boxcar"));

                Assert.assertEquals("Place c3", Car.OKAY, c3.setLocation(l1, l1s1));
		Assert.assertEquals("Location 1s1 c3 Used Length", 170, l1s1.getUsedLength());
		Assert.assertEquals("Location 1 c3 Used Length", 310, l1.getUsedLength());
                Assert.assertEquals("Place c4", Car.OKAY, c4.setLocation(l1, l1s1));
		Assert.assertEquals("Location 1s1 c4 Used Length", 214, l1s1.getUsedLength());
		Assert.assertEquals("Location 1 c4 Used Length", 354, l1.getUsedLength());

                Assert.assertEquals("Place c5", Car.OKAY, c5.setLocation(l1, l1s2));
		Assert.assertEquals("Location 1s2 c5 Used Length", 184, l1s2.getUsedLength());
		Assert.assertEquals("Location 1 c5 Used Length", 398, l1.getUsedLength());
                Assert.assertEquals("Place c6", Car.OKAY, c6.setLocation(l1, l1s2));
		Assert.assertEquals("Location 1s2 c6 Used Length", 228, l1s2.getUsedLength());
		Assert.assertEquals("Location 1 c6 Used Length", 442, l1.getUsedLength());

                // Place Cabooses on Staging tracks
                Assert.assertEquals("Place c1", Car.OKAY, c1.setLocation(l1, l1s1));
		Assert.assertEquals("Location 1s1 c1 Used Length", 250, l1s1.getUsedLength());
		Assert.assertEquals("Location 1 c1 Used Length", 478, l1.getUsedLength());

                Assert.assertEquals("Place c2", Car.OKAY, c2.setLocation(l1, l1s2));
		Assert.assertEquals("Location 1s2 c2 Used Length", 264, l1s2.getUsedLength());
		Assert.assertEquals("Location 1 c2 Used Length", 514, l1.getUsedLength());

                // Define the route.
                Route r1 = new Route("1", "Southbound Main Route");
		Assert.assertEquals("Route Id", "1", r1.getId());
		Assert.assertEquals("Route Name", "Southbound Main Route", r1.getName());
                
		RouteLocation rl1 = new RouteLocation("1r1", l1);
                rl1.setSequenceId(1);
                rl1.setTrainDirection(RouteLocation.SOUTH);
                rl1.setMaxCarMoves(5);
                rl1.setMaxTrainLength(1000);
                Assert.assertEquals("Route Location 1 Id", "1r1", rl1.getId());
		Assert.assertEquals("Route Location 1 Name", "North End", rl1.getName());
		RouteLocation rl2 = new RouteLocation("1r2", l2);
                rl2.setSequenceId(2);
                rl2.setTrainDirection(RouteLocation.SOUTH);
                rl2.setMaxCarMoves(5);
                rl2.setMaxTrainLength(1000);
		Assert.assertEquals("Route Location 2 Id", "1r2", rl2.getId());
		Assert.assertEquals("Route Location 2 Name", "North Industries", rl2.getName());
		RouteLocation rl3 = new RouteLocation("1r3", l3);
                rl3.setSequenceId(3);
                rl3.setTrainDirection(RouteLocation.SOUTH);
                rl3.setMaxCarMoves(5);
                rl3.setMaxTrainLength(1000);
		Assert.assertEquals("Route Location 3 Id", "1r3", rl3.getId());
		Assert.assertEquals("Route Location 3 Name", "South End", rl3.getName());

                r1.register(rl1);
                r1.register(rl2);
                r1.register(rl3);
                
                rmanager.register(r1);

                // Finally ready to define the train.
                Train train1 = new Train("1", "Southbound Through Freight");
		Assert.assertEquals("Train Id", "1", train1.getId());
		Assert.assertEquals("Train Name", "Southbound Through Freight", train1.getName());
                train1.setEngineRoad("CP");
//                train1.setEngineModel("SD45");
                train1.setNumberEngines("2");
                train1.setRequirements(Train.CABOOSE);
                train1.setCabooseRoad("CP");
                train1.addTypeName("Caboose");
                train1.addTypeName("Boxcar");
                train1.addTypeName("Diesel");
                train1.setRoadOption("All");
                train1.addTrainSkipsLocation("North Industries");
                train1.setRoute(r1);

                tmanager.register(train1);
                
                //  Last minute checks.
                Assert.assertEquals("Train 1 Departs Name", "North End", train1.getTrainDepartsName());
                Assert.assertEquals("Train 1 Route Departs Name", "North End", train1.getTrainDepartsRouteLocation().getName());
                Assert.assertEquals("Train 1 Terminates Name", "South End", train1.getTrainTerminatesName());
                Assert.assertEquals("Train 1 Route Terminates Name", "South End", train1.getTrainTerminatesRouteLocation().getName());
                Assert.assertEquals("Train 1 Next Location Name", "", train1.getNextLocationName());
                Assert.assertEquals("Train 1 Route Name", "Southbound Main Route", train1.getRoute().getName());

                //  Build the train!!
                train1.build();
                Assert.assertEquals("Train 1 After Build Departs Name", "North End", train1.getTrainDepartsName());
                Assert.assertEquals("Train 1 After Build Terminates Name", "South End", train1.getTrainTerminatesName());
                Assert.assertEquals("Train 1 After Build Next Location Name", "North Industries", train1.getNextLocationName());
                
                //  Move the train!!
                train1.move();
		Assert.assertEquals("Train 1 After 1st Move Current Name", "North Industries", train1.getCurrentLocationName());
                Assert.assertEquals("Train 1 After 1st Move Next Location Name", "South End", train1.getNextLocationName());

                //  Move the train again!!
                train1.move();
		Assert.assertEquals("Train 1 After 2nd Move Current Name", "South End", train1.getCurrentLocationName());
                Assert.assertEquals("Train 1 After 2nd Move Next Location Name", "South End", train1.getNextLocationName());

                //  Move the train again!!
                train1.move();
		Assert.assertEquals("Train 1 After 3rd Move Current Name", "South End", train1.getCurrentLocationName());
                Assert.assertEquals("Train 1 After 3rd Move Next Location Name", "South End", train1.getNextLocationName());

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
                cmanager.dispose();  // Clear out the Cars
                emanager.dispose();  // Clear out the Engines
                lmanager.dispose();  // Clear out the Locations
                rmanager.dispose();  // Clear out the Routes
                tmanager.dispose();  // Clear out the Trains
                
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
                loc1.addTypeName("Diesel");
                loc1.addTypeName("Boxcar");
                loc1.addTypeName("Caboose");
		Assert.assertEquals("Bob Test Location Westend Name", "Westend", loc1.getName());
		Assert.assertEquals("Bob Test Location Westend Directions", 3, loc1.getTrainDirections());
		Assert.assertEquals("Bob Test Location Westend Type Diesel", true, loc1.acceptsTypeName("Diesel"));
		Assert.assertEquals("Bob Test Location Westend Type Boxcar", true, loc1.acceptsTypeName("Boxcar"));
		Assert.assertEquals("Bob Test Location Westend Type Caboose", true, loc1.acceptsTypeName("Caboose"));
                
       		Location loc2;
                loc2 = lmanager.newLocation("Midtown");
                loc2.setTrainDirections(Location.WEST + Location.EAST);
                loc2.addTypeName("Diesel");
                loc2.addTypeName("Boxcar");
                loc2.addTypeName("Caboose");
		Assert.assertEquals("Bob Test Location Midtown Name", "Midtown", loc2.getName());
		Assert.assertEquals("Bob Test Location Midtown Directions", 3, loc2.getTrainDirections());
		Assert.assertEquals("Bob Test Location Midtown Type Diesel", true, loc2.acceptsTypeName("Diesel"));
		Assert.assertEquals("Bob Test Location Midtown Type Boxcar", true, loc2.acceptsTypeName("Boxcar"));
		Assert.assertEquals("Bob Test Location Midtown Type Caboose", true, loc2.acceptsTypeName("Caboose"));
                
       		Location loc3;
                loc3 = lmanager.newLocation("Eastend");
                loc3.setTrainDirections(Location.WEST + Location.EAST);
                loc3.addTypeName("Diesel");
                loc3.addTypeName("Boxcar");
                loc3.addTypeName("Caboose");
		Assert.assertEquals("Bob Test Location Eastend Name", "Eastend", loc3.getName());
		Assert.assertEquals("Bob Test Location Eastend Directions", 3, loc3.getTrainDirections());
		Assert.assertEquals("Bob Test Location Eastend Type Diesel", true, loc3.acceptsTypeName("Diesel"));
		Assert.assertEquals("Bob Test Location Eastend Type Boxcar", true, loc3.acceptsTypeName("Boxcar"));
		Assert.assertEquals("Bob Test Location Eastend Type Caboose", true, loc3.acceptsTypeName("Caboose"));

                Track loc1trk1;
                loc1trk1 = loc1.addTrack("Westend Staging 1", Track.YARD);
                loc1trk1.setTrainDirections(Track.WEST + Track.EAST);
                loc1trk1.setLength(500);
                loc1trk1.addTypeName("Diesel");
                loc1trk1.addTypeName("Boxcar");
                loc1trk1.addTypeName("Caboose");
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
                loc2trk1.addTypeName("Diesel");
                loc2trk1.addTypeName("Boxcar");
//                loc2trk1.addTypeName("Caboose");
		Assert.assertEquals("Bob Test Track Midtown West Inbound Name", "Midtown Inbound from West", loc2trk1.getName());
		Assert.assertEquals("Bob Test Track Midtown West Inbound Directions", 3, loc2trk1.getTrainDirections());
		Assert.assertEquals("Bob Test Track Midtown West Inbound Length", 500, loc2trk1.getLength());

                Track loc2trk2;
                loc2trk2 = loc2.addTrack("Midtown Inbound from East", Track.YARD);
                loc2trk2.setTrainDirections(Track.WEST + Track.EAST);
                loc2trk2.setLength(500);
                loc2trk2.addTypeName("Diesel");
                loc2trk2.addTypeName("Boxcar");
//                loc2trk2.addTypeName("Caboose");
		Assert.assertEquals("Bob Test Track Midtown East Inbound Name", "Midtown Inbound from East", loc2trk2.getName());
		Assert.assertEquals("Bob Test Track Midtown East Inbound Directions", 3, loc2trk2.getTrainDirections());
		Assert.assertEquals("Bob Test Track Midtown East Inbound Length", 500, loc2trk2.getLength());

                Track loc2trk3;
                loc2trk3 = loc2.addTrack("Midtown Outbound to West", Track.YARD);
                loc2trk3.setTrainDirections(Track.WEST);
                loc2trk3.setLength(500);
                loc2trk3.addTypeName("Diesel");
                loc2trk3.addTypeName("Boxcar");
		Assert.assertEquals("Bob Test Track Midtown West Outbound Name", "Midtown Outbound to West", loc2trk3.getName());
		Assert.assertEquals("Bob Test Track Midtown West Outbound Directions", 2, loc2trk3.getTrainDirections());
		Assert.assertEquals("Bob Test Track Midtown West Outbound Length", 500, loc2trk3.getLength());

                Track loc2trk4;
                loc2trk4 = loc2.addTrack("Midtown Outbound to East", Track.YARD);
                loc2trk4.setTrainDirections(Track.EAST);
                loc2trk4.setLength(500);
                loc2trk4.addTypeName("Diesel");
                loc2trk4.addTypeName("Boxcar");
		Assert.assertEquals("Bob Test Track Midtown East Outbound Name", "Midtown Outbound to East", loc2trk4.getName());
		Assert.assertEquals("Bob Test Track Midtown East Outbound Directions", 1, loc2trk4.getTrainDirections());
		Assert.assertEquals("Bob Test Track Midtown East Outbound Length", 500, loc2trk4.getLength());

                Track loc2trkc1;
                loc2trkc1 = loc2.addTrack("Midtown Caboose to East", Track.YARD);
                loc2trkc1.setTrainDirections(Track.EAST);
                loc2trkc1.setLength(100);
                loc2trkc1.addTypeName("Caboose");
		Assert.assertEquals("Bob Test Track Midtown East Caboose Name", "Midtown Caboose to East", loc2trkc1.getName());
		Assert.assertEquals("Bob Test Track Midtown East Caboose Directions", 1, loc2trkc1.getTrainDirections());
		Assert.assertEquals("Bob Test Track Midtown East Caboose Length", 100, loc2trkc1.getLength());

                Track loc2trkc2;
                loc2trkc2 = loc2.addTrack("Midtown Caboose to West", Track.YARD);
                loc2trkc2.setTrainDirections(Track.WEST);
                loc2trkc2.setLength(100);
                loc2trkc2.addTypeName("Caboose");
		Assert.assertEquals("Bob Test Track Midtown West Caboose Name", "Midtown Caboose to West", loc2trkc2.getName());
		Assert.assertEquals("Bob Test Track Midtown West Caboose Directions", 2, loc2trkc2.getTrainDirections());
		Assert.assertEquals("Bob Test Track Midtown west Caboose Length", 100, loc2trkc2.getLength());

                Track loc2trke1;
                loc2trke1 = loc2.addTrack("Midtown Engine to East", Track.YARD);
                loc2trke1.setTrainDirections(Track.EAST);
                loc2trke1.setLength(200);
                loc2trke1.addTypeName("Diesel");
		Assert.assertEquals("Bob Test Track Midtown East Engine Name", "Midtown Engine to East", loc2trke1.getName());
		Assert.assertEquals("Bob Test Track Midtown East Engine Directions", 1, loc2trke1.getTrainDirections());
		Assert.assertEquals("Bob Test Track Midtown East Engine Length", 200, loc2trke1.getLength());

                Track loc2trke2;
                loc2trke2 = loc2.addTrack("Midtown Engine to West", Track.YARD);
                loc2trke2.setTrainDirections(Track.WEST);
                loc2trke2.setLength(200);
                loc2trke2.addTypeName("Diesel");
		Assert.assertEquals("Bob Test Track Midtown West Engine Name", "Midtown Engine to West", loc2trke2.getName());
		Assert.assertEquals("Bob Test Track Midtown West Engine Directions", 2, loc2trke2.getTrainDirections());
		Assert.assertEquals("Bob Test Track Midtown west Engine Length", 200, loc2trke2.getLength());

                Track loc3trk1;
                loc3trk1 = loc3.addTrack("Eastend Staging 1", Track.YARD);
                loc3trk1.setTrainDirections(Track.WEST + Track.EAST);
                loc3trk1.setLength(500);
                loc3trk1.addTypeName("Diesel");
                loc3trk1.addTypeName("Boxcar");
                loc3trk1.addTypeName("Caboose");
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
//                b2.setLoad("E");
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
//                b5.setLoad("E");
		Assert.assertEquals("Bob Test Car CP71234567 Length", "40", b5.getLength());
		Assert.assertEquals("Bob Test Car CP71234567 Load", "E", b5.getLoad());
		Assert.assertEquals("Bob Test Test Car CP71234567 SetLocation 2s4", "okay", b5.setLocation(loc2, loc2trk3));

       		Car b6;
                b6 = cmanager.newCar("CP", "71234568");
                b6.setType("Boxcar");
                b6.setLength("40");
//                b6.setLoad("E");
		Assert.assertEquals("Bob Test Car CP71234568 Length", "40", b6.getLength());
		Assert.assertEquals("Bob Test Car CP71234568 Load", "E", b6.getLoad());
		Assert.assertEquals("Bob Test Test Car CP71234568 SetLocation 2s4", "okay", b6.setLocation(loc2, loc2trk3));

       		Car b7;
                b7 = cmanager.newCar("CP", "71234569");
                b7.setType("Boxcar");
                b7.setLength("40");
//                b7.setLoad("E");
		Assert.assertEquals("Bob Test Car CP71234569 Length", "40", b7.getLength());
		Assert.assertEquals("Bob Test Car CP71234569 Load", "E", b7.getLoad());
		Assert.assertEquals("Bob Test Test Car CP71234569 SetLocation 2s4", "okay", b7.setLocation(loc2, loc2trk3));

       		Car b8;
                b8 = cmanager.newCar("CP", "71234566");
                b8.setType("Boxcar");
                b8.setLength("40");
//                b2.setLoad("E");
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
		Assert.assertEquals("Bob Test Route Location rte1rln1 Name", "Midtown", rte1rln1.getName());
		Assert.assertEquals("Bob Test Route Location rte1rln1 Seq", 1, rte1rln1.getSequenceId());
        
                RouteLocation rte1rln2;
                rte1rln2 = rte1.addLocation(loc3);
                rte1rln2.setTrainDirection(RouteLocation.EAST);
		Assert.assertEquals("Bob Test Route Location rte1rln2 Name", "Eastend", rte1rln2.getName());
		Assert.assertEquals("Bob Test Route Location rte1rln2 Seq", 2, rte1rln2.getSequenceId());
                
       		Route rte2;
                rte2 = rmanager.newRoute("Midtown to Westend Through");
		Assert.assertEquals("Bob Test Route rte2 Name", "Midtown to Westend Through", rte2.getName());

                RouteLocation rte2rln1;
                rte2rln1 = rte2.addLocation(loc2);
                rte2rln1.setTrainDirection(RouteLocation.WEST);
		Assert.assertEquals("Bob Test Route Location rte2rln1 Name", "Midtown", rte2rln1.getName());
		Assert.assertEquals("Bob Test Route Location rte2rln1 Seq", 1, rte2rln1.getSequenceId());
        
                RouteLocation rte2rln2;
                rte2rln2 = rte2.addLocation(loc1);
                rte2rln2.setTrainDirection(RouteLocation.WEST);
		Assert.assertEquals("Bob Test Route Location rte2rln2 Name", "Westend", rte2rln2.getName());
		Assert.assertEquals("Bob Test Route Location rte2rln2 Seq", 2, rte2rln2.getSequenceId());
                
       		// Create trains used
       		Train train1;
                train1 = tmanager.newTrain("Midtown to Eastend Through 0800");
                train1.setRoute(rte1);
                train1.setNumberEngines("1");
                train1.setRequirements(Train.CABOOSE);
                train1.addTypeName("Diesel");
                train1.addTypeName("Boxcar");
                train1.addTypeName("Caboose");
		Assert.assertEquals("Bob Test Train train1 Name", "Midtown to Eastend Through 0800", train1.getName());
		Assert.assertEquals("Bob Test Train train1 Departs Name", "Midtown", train1.getTrainDepartsName());
		Assert.assertEquals("Bob Test Train train1 Terminates Name", "Eastend", train1.getTrainTerminatesName());
                
       		Train train2;
                train2 = tmanager.newTrain("Midtown to Westend Through 0900");
                train2.setRoute(rte2);
                train2.setNumberEngines("1");
                train2.setRequirements(Train.CABOOSE);
                train2.addTypeName("Diesel");
                train2.addTypeName("Boxcar");
                train2.addTypeName("Caboose");
		Assert.assertEquals("Bob Test Train train2 Name", "Midtown to Westend Through 0900", train2.getName());
		Assert.assertEquals("Bob Test Train train2 Departs Name", "Midtown", train2.getTrainDepartsName());
		Assert.assertEquals("Bob Test Train train2 Terminates Name", "Westend", train2.getTrainTerminatesName());
                
       		// Build trains
                train1.build();
                train2.build();
        }
        
	// test location Xml create support
	public void testXMLCreate() throws Exception {

                TrainManager manager = TrainManager.instance();
                List temptrainList = manager.getTrainsByIdList();

                Assert.assertEquals("Starting Number of Trains", 0, temptrainList.size());
                manager.newTrain("Test Number 1");
                manager.newTrain("Test Number 2");
                manager.newTrain("Test Number 3");

                temptrainList = manager.getTrainsByIdList();

                Assert.assertEquals("New Number of Trains", 3, temptrainList.size());
/*                
                Assert.assertEquals("New Engine by Id 1", "Test Number 1", manager.getEngineById("CPTest Number 1").getNumber());
                Assert.assertEquals("New Engine by Id 2", "Test Number 2", manager.getEngineById("ACLTest Number 2").getNumber());
                Assert.assertEquals("New Engine by Id 3", "Test Number 3", manager.getEngineById("CPTest Number 3").getNumber());

                Assert.assertEquals("New Location by Road+Name 1", "Test Number 1", manager.getEngineByRoadAndNumber("CP", "Test Number 1").getNumber());
                Assert.assertEquals("New Location by Road+Name 2", "Test Number 2", manager.getEngineByRoadAndNumber("ACL", "Test Number 2").getNumber());
                Assert.assertEquals("New Location by Road+Name 3", "Test Number 3", manager.getEngineByRoadAndNumber("CP", "Test Number 3").getNumber());

                manager.getEngineByRoadAndNumber("CP", "Test Number 1").setBuilt("1923");
                manager.getEngineByRoadAndNumber("CP", "Test Number 1").setColor("Black");
                manager.getEngineByRoadAndNumber("CP", "Test Number 1").setComment("Nice runner");
//                manager.getEngineByRoadAndNumber("CP", "Test Number 1").setConsist(consist);
//                manager.getEngineByRoadAndNumber("CP", "Test Number 1").setDestination(destination, track);
                manager.getEngineByRoadAndNumber("CP", "Test Number 1").setHp("23");
                manager.getEngineByRoadAndNumber("CP", "Test Number 1").setLength("50");
//                manager.getEngineByRoadAndNumber("CP", "Test Number 1").setLocation(location, track);
//                manager.getEngineByRoadAndNumber("CP", "Test Number 1").setModel("E8");
                manager.getEngineByRoadAndNumber("CP", "Test Number 1").setMoves(5);
                manager.getEngineByRoadAndNumber("CP", "Test Number 1").setOwner("TestOwner");
//                manager.getEngineByRoadAndNumber("CP", "Test Number 1").setRouteDestination(routeDestination);
//                manager.getEngineByRoadAndNumber("CP", "Test Number 1").setRouteLocation(routeLocation);
//                manager.getEngineByRoadAndNumber("CP", "Test Number 1").setSavedRouteId(id);
//                manager.getEngineByRoadAndNumber("CP", "Test Number 1").setTrain(train);
                manager.getEngineByRoadAndNumber("CP", "Test Number 1").setWeight("87");
                manager.getEngineByRoadAndNumber("CP", "Test Number 1").setWeightTons("97");
                
                
                manager.getEngineByRoadAndNumber("CP", "Test Number 1").setType("Gas Turbine");
                
                manager.getEngineByRoadAndNumber("CP", "Test Number 1").setModel("E8");
*/                
/*
		manager.getLocationByName("Test Location 1").setLocationOps(Location.NORMAL);
		manager.getLocationByName("Test Location 1").setSwitchList(true);
		manager.getLocationByName("Test Location 1").setTrainDirections(Location.EAST+Location.WEST);
		manager.getLocationByName("Test Location 1").addTypeName("Baggage");
		manager.getLocationByName("Test Location 1").addTypeName("BoxCar");
		manager.getLocationByName("Test Location 1").addTypeName("Caboose");
		manager.getLocationByName("Test Location 1").addTypeName("Coal");
		manager.getLocationByName("Test Location 1").addTypeName("Engine");
		manager.getLocationByName("Test Location 1").addTypeName("Hopper");
                manager.getLocationByName("Test Location 2").setComment("Test Location 2 Comment");
		manager.getLocationByName("Test Location 2").setLocationOps(Location.NORMAL);
		manager.getLocationByName("Test Location 2").setSwitchList(true);
		manager.getLocationByName("Test Location 2").setTrainDirections(Location.EAST+Location.WEST);
		manager.getLocationByName("Test Location 2").addTypeName("Baggage");
		manager.getLocationByName("Test Location 2").addTypeName("BoxCar");
		manager.getLocationByName("Test Location 2").addTypeName("Caboose");
		manager.getLocationByName("Test Location 2").addTypeName("Coal");
		manager.getLocationByName("Test Location 2").addTypeName("Engine");
		manager.getLocationByName("Test Location 2").addTypeName("Hopper");
                manager.getLocationByName("Test Location 3").setComment("Test Location 3 Comment");
		manager.getLocationByName("Test Location 3").setLocationOps(Location.NORMAL);
		manager.getLocationByName("Test Location 3").setSwitchList(true);
		manager.getLocationByName("Test Location 3").setTrainDirections(Location.EAST+Location.WEST);
		manager.getLocationByName("Test Location 3").addTypeName("Baggage");
		manager.getLocationByName("Test Location 3").addTypeName("BoxCar");
		manager.getLocationByName("Test Location 3").addTypeName("Caboose");
		manager.getLocationByName("Test Location 3").addTypeName("Coal");
		manager.getLocationByName("Test Location 3").addTypeName("Engine");
		manager.getLocationByName("Test Location 3").addTypeName("Hopper");
*/
/*                
                locationList = manager.getLocationsByIdList();
                Assert.assertEquals("New Number of Locations", 3, locationList.size());

                for (int i = 0; i < locationList.size(); i++) {
                    String locationId = (String)locationList.get(i);
                    Location loc = manager.getLocationById(locationId);
                    String locname = loc.getName();
                    if (i == 0) {
                        Assert.assertEquals("New Location by Id List 1", "Test Location 2", locname);
                    }
                    if (i == 1) {
                        Assert.assertEquals("New Location by Id List 2", "Test Location 1", locname);
                    }
                    if (i == 2) {
                        Assert.assertEquals("New Location by Id List 3", "Test Location 3", locname);
                    }
                }

*/
/*                
                locationList = manager.getLocationsByNameList();
                Assert.assertEquals("New Number of Locations", 3, locationList.size());

                for (int i = 0; i < locationList.size(); i++) {
                    String locationId = (String)locationList.get(i);
                    Location loc = manager.getLocationById(locationId);
                    String locname = loc.getName();
                    if (i == 0) {
                        Assert.assertEquals("New Location by Name List 1", "Test Location 1", locname);
                    }
                    if (i == 1) {
                        Assert.assertEquals("New Location by Name List 2", "Test Location 2", locname);
                    }
                    if (i == 2) {
                        Assert.assertEquals("New Location by Name List 3", "Test Location 3", locname);
                    }
                }
*/
                

                TrainManagerXml.instance().writeOperationsTrainFile();

                // Add some more engines and write file again
                // so we can test the backup facility
                manager.newTrain("Test Number 4");
                manager.newTrain("Test Number 5");
                manager.newTrain("Test Number 6");
//                manager.getRouteByRoadAndNumber("ACL", "Test Number 2").setComment("Test Engine 2 Changed Comment");
                
                TrainManagerXml.instance().writeOperationsTrainFile();
        }

        // TODO: Add test of build

	// TODO: Add test to create xml file

	// TODO: Add test to read xml file

	// from here down is testing infrastructure

    // Ensure minimal setup for log4J
    @Override
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
        // This test doesn't touch setup but we'll protect
        // Repoint OperationsXml to JUnitTest subdirectory
        String tempstring = OperationsXml.getOperationsDirectoryName();
        if (!tempstring.contains(File.separator+"JUnitTest")){
        	OperationsXml.setOperationsDirectoryName(OperationsXml.getOperationsDirectoryName()+File.separator+"JUnitTest");
        	OperationsXml.setOperationsFileName("OperationsJUnitTest.xml"); 
        }
        
        // This test doesn't touch routes but we'll protect
        // Repoint RouteManagerXml to JUnitTest subdirectory
        tempstring = RouteManagerXml.getOperationsDirectoryName();
        if (!tempstring.contains(File.separator+"JUnitTest")){
        	RouteManagerXml.setOperationsDirectoryName(RouteManagerXml.getOperationsDirectoryName()+File.separator+"JUnitTest");
        	RouteManagerXml.setOperationsFileName("OperationsJUnitTestRouteRoster.xml");
        }
        
        // Repoint EngineManagerXml to JUnitTest subdirectory
        tempstring = EngineManagerXml.getOperationsDirectoryName();
        if (!tempstring.contains(File.separator+"JUnitTest")){
        	EngineManagerXml.setOperationsDirectoryName(EngineManagerXml.getOperationsDirectoryName()+File.separator+"JUnitTest");
        	EngineManagerXml.setOperationsFileName("OperationsJUnitTestEngineRoster.xml");
        }
        
        // This test doesn't touch cars but we'll protect
        // Repoint CarManagerXml to JUnitTest subdirectory
        tempstring = CarManagerXml.getOperationsDirectoryName();
        if (!tempstring.contains(File.separator+"JUnitTest")){
        	CarManagerXml.setOperationsDirectoryName(CarManagerXml.getOperationsDirectoryName()+File.separator+"JUnitTest");
        	CarManagerXml.setOperationsFileName("OperationsJUnitTestCarRoster.xml");
        }
        
        // Repoint LocationManagerXml to JUnitTest subdirectory
        tempstring = LocationManagerXml.getOperationsDirectoryName();
        if (!tempstring.contains(File.separator+"JUnitTest")){
        	LocationManagerXml.setOperationsDirectoryName(LocationManagerXml.getOperationsDirectoryName()+File.separator+"JUnitTest");
        	LocationManagerXml.setOperationsFileName("OperationsJUnitTestLocationRoster.xml");
        }
        
        // Repoint TrainManagerXml to JUnitTest subdirectory
        tempstring = TrainManagerXml.getOperationsDirectoryName();
        if (!tempstring.contains(File.separator+"JUnitTest")){
        	TrainManagerXml.setOperationsDirectoryName(TrainManagerXml.getOperationsDirectoryName()+File.separator+"JUnitTest");
        	TrainManagerXml.setOperationsFileName("OperationsJUnitTestTrainRoster.xml");
        }
    	
        XmlFile.ensurePrefsPresent(XmlFile.prefsDir()+File.separator+LocationManagerXml.getOperationsDirectoryName());

        // Need to clear out TrainManager global variables
        TrainManager manager = TrainManager.instance();
        manager.dispose();
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
}
