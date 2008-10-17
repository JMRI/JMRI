// OperationsTrainsTest.java

package jmri.jmrit.operations.trains;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import jmri.InstanceManager;
import jmri.managers.InternalTurnoutManager;
import jmri.managers.InternalSensorManager;
import jmri.Sensor;
import jmri.SignalHead;
import jmri.Turnout;

import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.jmrit.operations.rollingstock.engines.Consist;
import jmri.jmrit.operations.rollingstock.engines.Engine;
import jmri.jmrit.operations.rollingstock.engines.EngineManager;
import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.routes.RouteLocation;

import jmri.jmrit.XmlFile;
import java.util.List;
import java.io.File;
import jmri.jmrit.operations.locations.LocationManagerXml;
import jmri.jmrit.operations.routes.RouteManager;

/**
 * Tests for the OperationsTrains class
 * @author	Bob Coleman
 * @version $Revision: 1.3 $
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
	

	// test creation
	public void testCreate() {
		Train t1 = new Train("TESTTRAINID", "TESTTRAINNAME");

		Assert.assertEquals("Train Id", "TESTTRAINID", t1.getId());
		Assert.assertEquals("Train Name", "TESTTRAINNAME", t1.getName());
	}

	// test public constants
	public void testConstants() {
		Train t1 = new Train("TESTTRAINID", "TESTTRAINNAME");

		Assert.assertEquals("Train Id", "TESTTRAINID", t1.getId());
		Assert.assertEquals("Train Name", "TESTTRAINNAME", t1.getName());

		Assert.assertEquals("Train Constant NONE", 0, t1.NONE);
		Assert.assertEquals("Train Constant CABOOSE", 1, t1.CABOOSE);
		Assert.assertEquals("Train Constant FRED", 2, t1.FRED);

		Assert.assertEquals("Train Constant ALLROADS", "All", t1.ALLROADS);
		Assert.assertEquals("Train Constant INCLUDEROADS", "Include", t1.INCLUDEROADS);
		Assert.assertEquals("Train Constant EXCLUDEROADS", "Exclude", t1.EXCLUDEROADS);

//  Comment out test that relies upon a typo in the properties until that gets fixed
//		Assert.assertEquals("Train Constant AUTO", "Auto", t1.AUTO);
	}

	// test attributes
	public void testAttributes() {
		Train t1 = new Train("TESTTRAINID", "TESTTRAINNAME");

		Assert.assertEquals("Train Id", "TESTTRAINID", t1.getId());
		Assert.assertEquals("Train Name", "TESTTRAINNAME", t1.getName());

		t1.setName("TESTNEWNAME");
		Assert.assertEquals("Train New Name", "TESTNEWNAME", t1.getName());
		t1.setComment("TESTCOMMENT");
		Assert.assertEquals("Train Comment", "TESTCOMMENT", t1.getComment());
		t1.setDescription("TESTDESCRIPTION");
		Assert.assertEquals("Train Description", "TESTDESCRIPTION", t1.getDescription());
		t1.setCabooseRoad("TESTCABOOSEROAD");
		Assert.assertEquals("Train Caboose Road", "TESTCABOOSEROAD", t1.getCabooseRoad());
		t1.setEngineModel("TESTENGINEMODEL");
		Assert.assertEquals("Train Engine Model", "TESTENGINEMODEL", t1.getEngineModel());
		t1.setEngineRoad("TESTENGINEROAD");
		Assert.assertEquals("Train Engine Road", "TESTENGINEROAD", t1.getEngineRoad());
		t1.setBuilt(true);
		Assert.assertTrue("Train Built true", t1.getBuilt());
		t1.setBuilt(false);
		Assert.assertFalse("Train Built false", t1.getBuilt());
		t1.setNumberEngines("13");
		Assert.assertEquals("Train Number Engines", "13", t1.getNumberEngines());
		t1.setRoadOption("INCLUDEROADS");
		Assert.assertEquals("Train Road Option INCLUDEROADS", "INCLUDEROADS", t1.getRoadOption());
		t1.setRoadOption("EXCLUDEROADS");
		Assert.assertEquals("Train Road Option EXCLUDEROADS", "EXCLUDEROADS", t1.getRoadOption());
		t1.setRoadOption("ALLROADS");
		Assert.assertEquals("Train Road Option ALLROADS", "ALLROADS", t1.getRoadOption());
		t1.setStatus("TESTSTATUS");
		Assert.assertEquals("Train Status", "TESTSTATUS", t1.getStatus());
		t1.setRequirements(t1.CABOOSE);
		Assert.assertEquals("Train Requirements CABOOSE", 1, t1.getRequirements());
		t1.setRequirements(t1.FRED);
		Assert.assertEquals("Train Requirements FRED", 2, t1.getRequirements());
		t1.setRequirements(t1.NONE);
		Assert.assertEquals("Train Requirements NONE", 0, t1.getRequirements());
	}

	// test train route
	public void testRoute() {
		Train t1 = new Train("TESTTRAINID", "TESTTRAINNAME");

		Assert.assertEquals("Train Id", "TESTTRAINID", t1.getId());
		Assert.assertEquals("Train Name", "TESTTRAINNAME", t1.getName());

		Route r1 = new Route("TESTROUTEID", "TESTROUTENAME");

		t1.setRoute(r1);
		Assert.assertEquals("Train Route Name", "TESTROUTENAME", t1.getTrainRouteName());

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

		t1.setRoute(rnew);
		Assert.assertEquals("Train New Route Name", "TESTNEWROUTENAME", t1.getTrainRouteName());

		Assert.assertEquals("Train New Route Departure Name", "TESTNEWROUTEDEPTNAME", t1.getTrainDepartsName());
		Assert.assertEquals("Train New Route Terminates Name", "TESTNEWROUTETERMNAME", t1.getTrainTerminatesName());

		RouteLocation rl1test;
		rl1test= rnew.getLocationByName("TESTNEWROUTECURRNAME");
		t1.setCurrent(rl1test);
		Assert.assertEquals("Train New Route Current Name", "TESTNEWROUTECURRNAME", t1.getCurrentName());
		rl1test= t1.getCurrent();
		Assert.assertEquals("Train New Route Current Name by Route Location", "TESTNEWROUTECURRNAME", rl1test.getName());
	}

	// test train skip locations support
	public void testSkipLocations() {
		Train t1 = new Train("TESTTRAINID", "TESTTRAINNAME");

		Assert.assertEquals("Train Id", "TESTTRAINID", t1.getId());
		Assert.assertEquals("Train Name", "TESTTRAINNAME", t1.getName());
/*
		Route rnew = new Route("TESTROUTEID2", "TESTNEWROUTENAME");
		t1.setRoute(rnew);

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
*/
		t1.addTrainSkipsLocation("TESTLOCATIONID2");
		Assert.assertTrue("Location 2 to be skipped", t1.skipsLocation("TESTLOCATIONID2"));

		t1.addTrainSkipsLocation("TESTLOCATIONID4");
		Assert.assertTrue("Location 4 to be skipped", t1.skipsLocation("TESTLOCATIONID4"));

		t1.deleteTrainSkipsLocation("TESTLOCATIONID2");
		Assert.assertFalse("Location 2 not to be skipped", t1.skipsLocation("TESTLOCATIONID2"));
		Assert.assertTrue("Location 4 still to be skipped", t1.skipsLocation("TESTLOCATIONID4"));

		t1.deleteTrainSkipsLocation("TESTLOCATIONID4");
		Assert.assertFalse("Location 2 still not to be skipped", t1.skipsLocation("TESTLOCATIONID2"));
		Assert.assertFalse("Location 4 not to be skipped", t1.skipsLocation("TESTLOCATIONID4"));
	}

	// test train accepts types support
	public void testAcceptsTypes() {
		Train t1 = new Train("TESTTRAINID", "TESTTRAINNAME");

		Assert.assertEquals("Train Id", "TESTTRAINID", t1.getId());
		Assert.assertEquals("Train Name", "TESTTRAINNAME", t1.getName());

		t1.addTypeName("Caboose");
		Assert.assertTrue("Train accepts type name Caboose", t1.acceptsTypeName("Caboose"));
		Assert.assertFalse("Train does not accept type name Hopper", t1.acceptsTypeName("Hopper"));

		t1.addTypeName("Hopper");
		Assert.assertTrue("Train still accepts type name Caboose", t1.acceptsTypeName("Caboose"));
		Assert.assertTrue("Train accepts type name Hopper", t1.acceptsTypeName("Hopper"));

		t1.deleteTypeName("Caboose");
		Assert.assertFalse("Train no longer accepts type name Caboose", t1.acceptsTypeName("Caboose"));
		Assert.assertTrue("Train still accepts type name Hopper", t1.acceptsTypeName("Hopper"));
	}

	// test train accepts road names support
	public void testAcceptsRoadNames() {
		Train t1 = new Train("TESTTRAINID", "TESTTRAINNAME");

		Assert.assertEquals("Train Id", "TESTTRAINID", t1.getId());
		Assert.assertEquals("Train Name", "TESTTRAINNAME", t1.getName());

		t1.setRoadOption("ALLROADS");
		Assert.assertTrue("Train accepts (ALLROADS) Road name CP", t1.acceptsRoadName("CP"));
		Assert.assertTrue("Train accepts (ALLROADS) Road name VIA", t1.acceptsRoadName("VIA"));

		t1.setRoadOption("Include");
		t1.addRoadName("CP");
		Assert.assertTrue("Train accepts (INCLUDEROADS) Road name CP", t1.acceptsRoadName("CP"));
		Assert.assertFalse("Train does not accept (INCLUDEROADS) Road name VIA", t1.acceptsRoadName("VIA"));

		t1.addRoadName("VIA");
		Assert.assertTrue("Train still accepts (INCLUDEROADS) Road name CP", t1.acceptsRoadName("CP"));
		Assert.assertTrue("Train accepts (INCLUDEROADS) Road name VIA", t1.acceptsRoadName("VIA"));

		t1.deleteRoadName("CP");
		Assert.assertFalse("Train no longer accepts (INCLUDEROADS) Road name CP", t1.acceptsRoadName("CP"));
		Assert.assertTrue("Train still accepts (INCLUDEROADS) Road name VIA", t1.acceptsRoadName("VIA"));

		t1.setRoadOption("Exclude");
		Assert.assertTrue("Train does accept (EXCLUDEROADS) Road name CP", t1.acceptsRoadName("CP"));
		Assert.assertFalse("Train does not accept (EXCLUDEROADS) Road name VIA", t1.acceptsRoadName("VIA"));

		t1.addRoadName("CP");
		Assert.assertFalse("Train does not accept (EXCLUDEROADS) Road name CP", t1.acceptsRoadName("CP"));
		Assert.assertFalse("Train still does not accept (EXCLUDEROADS) Road name VIA", t1.acceptsRoadName("VIA"));

		t1.deleteRoadName("VIA");
		Assert.assertFalse("Train still does not accepts (EXCLUDEROADS) Road name CP", t1.acceptsRoadName("CP"));
		Assert.assertTrue("Train now accepts (EXCLUDEROADS) Road name VIA", t1.acceptsRoadName("VIA"));
	}



	// test train staging to staging
	public void testStagingtoStaging() {
                TrainManager tmanager = TrainManager.instance();
                RouteManager rmanager = RouteManager.instance();
                LocationManager lmanager = LocationManager.instance();
                EngineManager emanager = EngineManager.instance();
                CarManager cmanager = CarManager.instance();

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
                l1.addTypeName("Engine");
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
                l1s1.addTypeName("Engine");
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
                l1s2.addTypeName("Engine");
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
                l2.addTypeName("Engine");
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
                l3.addTypeName("Engine");
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
                l3s1.addTypeName("Engine");
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
                l3s2.addTypeName("Engine");
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
                Assert.assertEquals("Place e1", "Okay", e1.setLocation(l1, l1s1));
		Assert.assertEquals("Location 1s1 e1 Used Length", 63, l1s1.getUsedLength());
		Assert.assertEquals("Location 1 e1 Used Length", 63, l1.getUsedLength());
                Assert.assertEquals("Place e2", "Okay", e2.setLocation(l1, l1s1));
		Assert.assertEquals("Location 1s1 e2 Used Length", 126, l1s1.getUsedLength());
		Assert.assertEquals("Location 1 e2 Used Length", 126, l1.getUsedLength());

                Assert.assertEquals("Location 1s2 Init Used Length", 0, l1s2.getUsedLength());
                Assert.assertEquals("Place e3", "Okay", e3.setLocation(l1, l1s2));
		Assert.assertEquals("Location 1s2 e3 Used Length", 70, l1s2.getUsedLength());
		Assert.assertEquals("Location 1 e3 Used Length", 196, l1.getUsedLength());
                Assert.assertEquals("Place e4", "Okay", e4.setLocation(l1, l1s2));
		Assert.assertEquals("Location 1s2 e4 Used Length", 140, l1s2.getUsedLength());
		Assert.assertEquals("Location 1 e4 Used Length", 266, l1.getUsedLength());

                // Place Boxcars on Staging tracks
                Assert.assertTrue("l1 Accepts Boxcar", l1.acceptsTypeName("Boxcar"));
                Assert.assertTrue("l1s1 Accepts Boxcar", l1s1.acceptsTypeName("Boxcar"));

                Assert.assertEquals("Place c3", "Okay", c3.setLocation(l1, l1s1));
		Assert.assertEquals("Location 1s1 c3 Used Length", 170, l1s1.getUsedLength());
		Assert.assertEquals("Location 1 c3 Used Length", 310, l1.getUsedLength());
                Assert.assertEquals("Place c4", "Okay", c4.setLocation(l1, l1s1));
		Assert.assertEquals("Location 1s1 c4 Used Length", 214, l1s1.getUsedLength());
		Assert.assertEquals("Location 1 c4 Used Length", 354, l1.getUsedLength());

                Assert.assertEquals("Place c5", "Okay", c5.setLocation(l1, l1s2));
		Assert.assertEquals("Location 1s2 c5 Used Length", 184, l1s2.getUsedLength());
		Assert.assertEquals("Location 1 c5 Used Length", 398, l1.getUsedLength());
                Assert.assertEquals("Place c6", "Okay", c6.setLocation(l1, l1s2));
		Assert.assertEquals("Location 1s2 c6 Used Length", 228, l1s2.getUsedLength());
		Assert.assertEquals("Location 1 c6 Used Length", 442, l1.getUsedLength());

                // Place Cabooses on Staging tracks
                Assert.assertEquals("Place c1", "Okay", c1.setLocation(l1, l1s1));
		Assert.assertEquals("Location 1s1 c1 Used Length", 250, l1s1.getUsedLength());
		Assert.assertEquals("Location 1 c1 Used Length", 478, l1.getUsedLength());

                Assert.assertEquals("Place c2", "Okay", c2.setLocation(l1, l1s2));
		Assert.assertEquals("Location 1s2 c2 Used Length", 264, l1s2.getUsedLength());
		Assert.assertEquals("Location 1 c2 Used Length", 514, l1.getUsedLength());

                // Define the route.
                Route r1 = new Route("1", "Southbound Main Route");
		Assert.assertEquals("Route Id", "1", r1.getId());
		Assert.assertEquals("Route Name", "Southbound Main Route", r1.getName());
                
		RouteLocation rl1 = new RouteLocation("1r1", l1);
                rl1.setSequenceId(1);
                rl1.setTrainDirection("South");
                rl1.setMaxCarMoves(5);
                rl1.setMaxTrainLength(1000);
                Assert.assertEquals("Route Location 1 Id", "1r1", rl1.getId());
		Assert.assertEquals("Route Location 1 Name", "North End", rl1.getName());
		RouteLocation rl2 = new RouteLocation("1r2", l2);
                rl2.setSequenceId(2);
                rl2.setTrainDirection("South");
                rl2.setMaxCarMoves(5);
                rl2.setMaxTrainLength(1000);
		Assert.assertEquals("Route Location 2 Id", "1r2", rl2.getId());
		Assert.assertEquals("Route Location 2 Name", "North Industries", rl2.getName());
		RouteLocation rl3 = new RouteLocation("1r3", l3);
                rl3.setSequenceId(3);
                rl3.setTrainDirection("South");
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
		Assert.assertEquals("Train 1 After 1st Move Current Name", "North Industries", train1.getCurrentName());
                Assert.assertEquals("Train 1 After 1st Move Next Location Name", "South End", train1.getNextLocationName());

                //  Move the train again!!
                train1.move();
		Assert.assertEquals("Train 1 After 2nd Move Current Name", "South End", train1.getCurrentName());
                Assert.assertEquals("Train 1 After 2nd Move Next Location Name", "South End", train1.getNextLocationName());

                //  Move the train again!!
                train1.move();
		Assert.assertEquals("Train 1 After 3rd Move Current Name", "South End", train1.getCurrentName());
                Assert.assertEquals("Train 1 After 3rd Move Next Location Name", "South End", train1.getNextLocationName());

        }
        
        // TODO: Add test of build

	// TODO: Add test to create xml file

	// TODO: Add test to read xml file

	// from here down is testing infrastructure

    // Ensure minimal setup for log4J

    Turnout t1, t2, t3;
    Sensor s1, s2, s3, s4, s5;
    SignalHead h1, h2, h3, h4;
    
    /**
    * Test-by test initialization.
    * Does log4j for standalone use, and then
    * creates a set of turnouts, sensors and signals
    * as common background for testing
    */
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
        // create a new instance manager
        InstanceManager i = new InstanceManager(){
            protected void init() {
                root = null;
                super.init();
                root = this;
            }
        };
        
        InstanceManager.setTurnoutManager(new InternalTurnoutManager());
        t1 = InstanceManager.turnoutManagerInstance().newTurnout("IT1", "1");
        t2 = InstanceManager.turnoutManagerInstance().newTurnout("IT2", "2");
        t3 = InstanceManager.turnoutManagerInstance().newTurnout("IT3", "3");

        InstanceManager.setSensorManager(new InternalSensorManager());
        s1 = InstanceManager.sensorManagerInstance().newSensor("IS1", "1");
        s2 = InstanceManager.sensorManagerInstance().newSensor("IS2", "2");
        s3 = InstanceManager.sensorManagerInstance().newSensor("IS3", "3");
        s4 = InstanceManager.sensorManagerInstance().newSensor("IS4", "4");
        s5 = InstanceManager.sensorManagerInstance().newSensor("IS5", "5");

        h1 = new jmri.VirtualSignalHead("IH1");
        InstanceManager.signalHeadManagerInstance().register(h1);
        h2 = new jmri.VirtualSignalHead("IH2");
        InstanceManager.signalHeadManagerInstance().register(h2);
        h3 = new jmri.VirtualSignalHead("IH3");
        InstanceManager.signalHeadManagerInstance().register(h3);
        h4 = new jmri.VirtualSignalHead("IH4");
        InstanceManager.signalHeadManagerInstance().register(h4);
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
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }
}
