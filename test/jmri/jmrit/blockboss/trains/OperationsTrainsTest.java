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
import jmri.jmrit.operations.locations.SecondaryLocation;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.routes.RouteLocation;

import java.util.List;

/**
 * Tests for the OperationsTrains class
 * @author	Bob Coleman
 * @version $Revision: 1.1 $
 */
public class OperationsTrainsTest extends TestCase {

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
