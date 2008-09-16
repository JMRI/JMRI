// OperationsRoutesTest.java

package jmri.jmrit.operations.routes;

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

import java.util.List;

/**
 * Tests for the OperationsRoutes class
 * @author	Bob Coleman
 * @version $Revision: 1.1 $
 */
public class OperationsRoutesTest extends TestCase {

	// test creation
	public void testCreate() {
		Route r1 = new Route("TESTROUTEID", "TESTROUTENAME");
		r1.setComment("TESTCOMMENT");

		Assert.assertEquals("Route Id", "TESTROUTEID", r1.getId());
		Assert.assertEquals("Route Name", "TESTROUTENAME", r1.getName());
		Assert.assertEquals("Route Comment", "TESTCOMMENT", r1.getComment());
	}

	// test public constants
	public void testConstants() {
		Route r1 = new Route("TESTROUTEID", "TESTROUTENAME");

		Assert.assertEquals("Route Id", "TESTROUTEID", r1.getId());
		Assert.assertEquals("Route Name", "TESTROUTENAME", r1.getName());

		Assert.assertEquals("Route Constant EAST", 1, r1.EAST);
		Assert.assertEquals("Route Constant WEST", 2, r1.WEST);
		Assert.assertEquals("Route Constant NORTH", 4, r1.NORTH);
		Assert.assertEquals("Route Constant SOUTH", 8, r1.SOUTH);
	}

	// test attributes
	public void testAttributes() {
		Route r1 = new Route("TESTROUTEID", "TESTROUTENAME");

		Assert.assertEquals("Route Id", "TESTROUTEID", r1.getId());
		Assert.assertEquals("Route Name", "TESTROUTENAME", r1.getName());

		r1.setName("TESTNEWNAME");
		Assert.assertEquals("Route New Name", "TESTNEWNAME", r1.getName());
	}

	// test route location
	public void testRouteLocation() {
		Route r1 = new Route("TESTROUTEID", "TESTROUTENAME");

		Assert.assertEquals("Route Id", "TESTROUTEID", r1.getId());
		Assert.assertEquals("Route Name", "TESTROUTENAME", r1.getName());

		Location l1 = new Location("TESTLOCATIONID1", "TESTLOCATIONNAME1");

		RouteLocation rl1 = new RouteLocation("TESTROUTELOCATIONID", l1);

		Assert.assertEquals("Route Location Id", "TESTROUTELOCATIONID", rl1.getId());
		Assert.assertEquals("Route Location Name", "TESTLOCATIONNAME1", rl1.getName());
	}

	// test public route location constants
	public void testRouteLocationConstants() {
		Route r1 = new Route("TESTROUTEID", "TESTROUTENAME");

		Location l1 = new Location("TESTLOCATIONID1", "TESTLOCATIONNAME1");

		RouteLocation rl1 = new RouteLocation("TESTROUTELOCATIONID", l1);

		Assert.assertEquals("Route Id", "TESTROUTEID", r1.getId());
		Assert.assertEquals("Route Name", "TESTROUTENAME", r1.getName());

		Assert.assertEquals("Route Constant EAST", "East", rl1.EAST);
		Assert.assertEquals("Route Constant WEST", "West", rl1.WEST);
		Assert.assertEquals("Route Constant NORTH", "North", rl1.NORTH);
		Assert.assertEquals("Route Constant SOUTH", "South", rl1.SOUTH);
	}

	// test route location attributes
	public void testRouteLocationAttributes() {
		Route r1 = new Route("TESTROUTEID", "TESTROUTENAME");

		Assert.assertEquals("Route Id", "TESTROUTEID", r1.getId());
		Assert.assertEquals("Route Name", "TESTROUTENAME", r1.getName());

		Location l1 = new Location("TESTLOCATIONID1", "TESTLOCATIONNAME1");

		RouteLocation rl1 = new RouteLocation("TESTROUTELOCATIONID", l1);
		rl1.setSequenceId(4);
		rl1.setComment("TESTROUTELOCATIONCOMMENT");
		rl1.setMaxTrainLength(320);
		rl1.setTrainLength(220);
		rl1.setMaxCarMoves(32);
		rl1.setCarMoves(10);
		rl1.setTrainIconX(12);
		rl1.setTrainIconY(8);

		Assert.assertEquals("Route Location Id", "TESTROUTELOCATIONID", rl1.getId());
		Assert.assertEquals("Route Location Name", "TESTLOCATIONNAME1", rl1.getName());

		Assert.assertEquals("Route Location Comment", "TESTROUTELOCATIONCOMMENT", rl1.getComment());
		Assert.assertEquals("Route Location Sequence", 4, rl1.getSequenceId());

		Assert.assertEquals("Route Location Max Train Length", 320, rl1.getMaxTrainLength());
		Assert.assertEquals("Route Location Train Length", 220, rl1.getTrainLength());
		Assert.assertEquals("Route Location Max Car Moves", 32, rl1.getMaxCarMoves());
		Assert.assertEquals("Route Location Car Moves", 10, rl1.getCarMoves());
		Assert.assertEquals("Route Location Icon X", 12, rl1.getTrainIconX());
		Assert.assertEquals("Route Location Icon Y", 8, rl1.getTrainIconY());

		rl1.setTrainDirection(rl1.EAST);
		Assert.assertEquals("Route Location Train Direction East", "East", rl1.getTrainDirection());

		rl1.setTrainDirection(rl1.WEST);
		Assert.assertEquals("Route Location Train Direction West", "West", rl1.getTrainDirection());

		rl1.setTrainDirection(rl1.NORTH);
		Assert.assertEquals("Route Location Train Direction North", "North", rl1.getTrainDirection());

		rl1.setTrainDirection(rl1.SOUTH);
		Assert.assertEquals("Route Location Train Direction South", "South", rl1.getTrainDirection());
	}

	// test route location management
	public void testRouteLocationManagement() {
		Route r1 = new Route("TESTROUTEID", "TESTROUTENAME");

		Assert.assertEquals("Route Id", "TESTROUTEID", r1.getId());
		Assert.assertEquals("Route Name", "TESTROUTENAME", r1.getName());

		RouteLocation rladd;

		Location l1 = new Location("TESTLOCATIONID1", "TESTLOCATIONNAME1");
		rladd = r1.addLocation(l1);

		Location l2 = new Location("TESTLOCATIONID2", "TESTLOCATIONNAME2");
		rladd = r1.addLocation(l2);

		Location l3 = new Location("TESTLOCATIONID3", "TESTLOCATIONNAME3");
		rladd = r1.addLocation(l3);

		RouteLocation rl1test;

		rl1test= r1.getLocationByName("TESTLOCATIONNAME1");
		Assert.assertEquals("Add Location 1", "TESTLOCATIONNAME1", rl1test.getName());

		rl1test= r1.getLocationByName("TESTLOCATIONNAME2");
		Assert.assertEquals("Add Location 2", "TESTLOCATIONNAME2", rl1test.getName());

		rl1test= r1.getLocationByName("TESTLOCATIONNAME3");
		Assert.assertEquals("Add Location 3", "TESTLOCATIONNAME3", rl1test.getName());

		//  Check that locations are in the expected order
		List list = r1.getLocationsBySequenceList();
		for (int i = 0; i < list.size(); i++) {
			rl1test = r1.getLocationById((String) (list.get(i)));
			if (i == 0) {			
				Assert.assertEquals("List Location 1 before", "TESTLOCATIONNAME1", rl1test.getName());
			}
			if (i == 1) {			
				Assert.assertEquals("List Location 2 before", "TESTLOCATIONNAME2", rl1test.getName());
			}
			if (i == 2) {			
				Assert.assertEquals("List Location 3 before", "TESTLOCATIONNAME3", rl1test.getName());
			}
		}

		//  Add a fourth location but put it in the second spot and check that locations are in the expected order
		Location l4 = new Location("TESTLOCATIONID4", "TESTLOCATIONNAME4");
		rladd = r1.addLocation(l4,2);

		rl1test= r1.getLocationByName("TESTLOCATIONNAME4");
		Assert.assertEquals("Add Location 4", "TESTLOCATIONNAME4", rl1test.getName());

		list = r1.getLocationsBySequenceList();
		for (int i = 0; i < list.size(); i++) {
			rl1test = r1.getLocationById((String) (list.get(i)));
			if (i == 0) {			
				Assert.assertEquals("List Location 1 after", "TESTLOCATIONNAME1", rl1test.getName());
			}
			if (i == 1) {			
				Assert.assertEquals("List Location 2 after", "TESTLOCATIONNAME4", rl1test.getName());
			}
			if (i == 2) {			
				Assert.assertEquals("List Location 3 after", "TESTLOCATIONNAME2", rl1test.getName());
			}
			if (i == 3) {			
				Assert.assertEquals("List Location 4 after", "TESTLOCATIONNAME3", rl1test.getName());
			}
		}

		//  Move up the third location and check that locations are in the expected order
		rl1test= r1.getLocationByName("TESTLOCATIONNAME3");
		r1.moveLocationUp(rl1test);
		list = r1.getLocationsBySequenceList();
		for (int i = 0; i < list.size(); i++) {
			rl1test = r1.getLocationById((String) (list.get(i)));
			if (i == 0) {			
				Assert.assertEquals("List Location 1 after move up", "TESTLOCATIONNAME1", rl1test.getName());
			}
			if (i == 1) {			
				Assert.assertEquals("List Location 2 after move up", "TESTLOCATIONNAME4", rl1test.getName());
			}
			if (i == 2) {			
				Assert.assertEquals("List Location 3 after move up", "TESTLOCATIONNAME3", rl1test.getName());
			}
			if (i == 3) {			
				Assert.assertEquals("List Location 4 after move up", "TESTLOCATIONNAME2", rl1test.getName());
			}
		}

		//  Move down the first location down 2 and check that locations are in the expected order
		rl1test= r1.getLocationByName("TESTLOCATIONNAME1");
		r1.moveLocationDown(rl1test);
		r1.moveLocationDown(rl1test);
		list = r1.getLocationsBySequenceList();
		for (int i = 0; i < list.size(); i++) {
			rl1test = r1.getLocationById((String) (list.get(i)));
			if (i == 0) {			
				Assert.assertEquals("List Location 1 after move up", "TESTLOCATIONNAME4", rl1test.getName());
			}
			if (i == 1) {			
				Assert.assertEquals("List Location 2 after move up", "TESTLOCATIONNAME3", rl1test.getName());
			}
			if (i == 2) {			
				Assert.assertEquals("List Location 3 after move up", "TESTLOCATIONNAME1", rl1test.getName());
			}
			if (i == 3) {			
				Assert.assertEquals("List Location 4 after move up", "TESTLOCATIONNAME2", rl1test.getName());
			}
		}

		//  Delete the third location and check that locations are in the expected order
		rl1test= r1.getLocationByName("TESTLOCATIONNAME3");
		r1.deleteLocation(rl1test);
		list = r1.getLocationsBySequenceList();
		for (int i = 0; i < list.size(); i++) {
			rl1test = r1.getLocationById((String) (list.get(i)));
			if (i == 0) {			
				Assert.assertEquals("List Location 1 after move up", "TESTLOCATIONNAME4", rl1test.getName());
			}
			if (i == 1) {			
				Assert.assertEquals("List Location 2 after move up", "TESTLOCATIONNAME1", rl1test.getName());
			}
			if (i == 2) {			
				Assert.assertEquals("List Location 3 after move up", "TESTLOCATIONNAME2", rl1test.getName());
			}
		}
	}

	// TODO: Add tests for Route location secondary location

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

	public OperationsRoutesTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {"-noloading", OperationsRoutesTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(OperationsRoutesTest.class);
		return suite;
	}

    // The minimal setup for log4J
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }
}
