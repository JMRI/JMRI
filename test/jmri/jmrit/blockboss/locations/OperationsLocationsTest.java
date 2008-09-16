// OperationsLocationsTest.java

package jmri.jmrit.operations.locations;

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

/**
 * Tests for the OperationsLocations class
 * @author	Bob Coleman
 * @version $Revision: 1.1 $
 */
public class OperationsLocationsTest extends TestCase {

	// test creation
	public void testCreate() {
		Location l = new Location("Test id", "Test Name");
		Assert.assertEquals("Location id", "Test id", l.getId());
		Assert.assertEquals("Location Name", "Test Name", l.getName());
		l.setName("New Test Name");
		Assert.assertEquals("New Location Name", "New Test Name", l.getName());
		l.setComment("Test Location Comment");
		Assert.assertEquals("Location Comment", "Test Location Comment", l.getComment());
	}

	// test length attributes
	public void testLengthAttributes() {
		Location l = new Location("Test id", "Test Name");
		Assert.assertEquals("Location id", "Test id", l.getId());
		Assert.assertEquals("Location Name", "Test Name", l.getName());

		l.setLength(400);
		Assert.assertEquals("Length", 400, l.getLength());

		l.setUsedLength(200);
		Assert.assertEquals("Used Length", 200, l.getUsedLength());
	}

	// test operation attributes
	public void testOperationAttributes() {
		Location l = new Location("Test id", "Test Name");
		Assert.assertEquals("Location id", "Test id", l.getId());
		Assert.assertEquals("Location Name", "Test Name", l.getName());

		l.setLocationOps(l.STAGING);
		Assert.assertEquals("Location Ops Staging", l.STAGING, l.getLocationOps());

		l.setLocationOps(l.NORMAL);
		Assert.assertEquals("Location Ops Normal", l.NORMAL, l.getLocationOps());
	}

	// test direction attributes
	public void testDirectionAttributes() {
		Location l = new Location("Test id", "Test Name");
		Assert.assertEquals("Location id", "Test id", l.getId());
		Assert.assertEquals("Location Name", "Test Name", l.getName());

		l.setTrainDirections(l.NORTH);
		Assert.assertEquals("Location Direction North", l.NORTH, l.getTrainDirections());

		l.setTrainDirections(l.SOUTH);
		Assert.assertEquals("Location Direction South", l.SOUTH, l.getTrainDirections());

		l.setTrainDirections(l.EAST);
		Assert.assertEquals("Location Direction East", l.EAST, l.getTrainDirections());

		l.setTrainDirections(l.WEST);
		Assert.assertEquals("Location Direction West", l.WEST, l.getTrainDirections());

		l.setTrainDirections(l.NORTH+l.SOUTH);
		Assert.assertEquals("Location Direction North+South", l.NORTH+l.SOUTH, l.getTrainDirections());

		l.setTrainDirections(l.EAST+l.WEST);
		Assert.assertEquals("Location Direction East+West", l.EAST+l.WEST, l.getTrainDirections());

		l.setTrainDirections(l.NORTH+l.SOUTH+l.EAST+l.WEST);
		Assert.assertEquals("Location Direction North+South+East+West", l.NORTH+l.SOUTH+l.EAST+l.WEST, l.getTrainDirections());
	}

	// test car attributes
	public void testCarAttributes() {
		Location l = new Location("Test id", "Test Name");
		Assert.assertEquals("Location id", "Test id", l.getId());
		Assert.assertEquals("Location Name", "Test Name", l.getName());

		l.setNumberCars(8);
		Assert.assertEquals("Location Number of Cars", 8, l.getNumberCars());
	}

	// test switchlist attributes
	public void testSwitchlistAttributes() {
		Location l = new Location("Test id", "Test Name");
		Assert.assertEquals("Location id", "Test id", l.getId());
		Assert.assertEquals("Location Name", "Test Name", l.getName());

		l.setSwitchList(true);
		Assert.assertEquals("Switch List True", true, l.getSwitchList());

		l.setSwitchList(false);
		Assert.assertEquals("Switch List True", false, l.getSwitchList());
	}

	// test typename support
	public void testTypeNameSupport() {
		Location l = new Location("Test id", "Test Name");
		Assert.assertEquals("Location id", "Test id", l.getId());
		Assert.assertEquals("Location Name", "Test Name", l.getName());

		Assert.assertEquals("Accepts Type Name undefined", false, l.acceptsTypeName("TestTypeName"));

		l.addTypeName("TestTypeName");
		Assert.assertEquals("Accepts Type Name defined", true, l.acceptsTypeName("TestTypeName"));

		l.deleteTypeName("TestTypeName");
		Assert.assertEquals("Accepts Type Name undefined2", false, l.acceptsTypeName("TestTypeName"));

		l.addTypeName("Baggage");
		l.addTypeName("BoxCar");
		l.addTypeName("Caboose");
		l.addTypeName("Coal");
		l.addTypeName("Engine");
		l.addTypeName("Hopper");
		l.addTypeName("MOW");
		l.addTypeName("Passenger");
		l.addTypeName("Reefer");
		l.addTypeName("Stock");
		l.addTypeName("Tank Oil");
		Assert.assertEquals("Accepts Type Name BoxCar", true, l.acceptsTypeName("BoxCar"));
		Assert.assertEquals("Accepts Type Name Boxcar", false, l.acceptsTypeName("Boxcar"));
		Assert.assertEquals("Accepts Type Name MOW", true, l.acceptsTypeName("MOW"));
		Assert.assertEquals("Accepts Type Name Caboose", true, l.acceptsTypeName("Caboose"));
		Assert.assertEquals("Accepts Type Name BoxCar", true, l.acceptsTypeName("BoxCar"));
		Assert.assertEquals("Accepts Type Name undefined3", false, l.acceptsTypeName("TestTypeName"));
	}

	// test pickup support
	public void testPickUpSupport() {
		Location l = new Location("Test id", "Test Name");
		Assert.assertEquals("Location id", "Test id", l.getId());
		Assert.assertEquals("Location Name", "Test Name", l.getName());

		Assert.assertEquals("Pick Ups Start Condition", 0 , l.getPickupCars());

		l.addPickupCar();
		Assert.assertEquals("Pick Up 1", 1, l.getPickupCars());

		l.addPickupCar();
		Assert.assertEquals("Pick Up second", 2, l.getPickupCars());

		l.deletePickupCar();
		Assert.assertEquals("Delete Pick Up", 1, l.getPickupCars());

		l.deletePickupCar();
		Assert.assertEquals("Delete Pick Up second", 0, l.getPickupCars());
	}

	// test drop support
	public void testDropSupport() {
		Location l = new Location("Test id", "Test Name");
		Assert.assertEquals("Location id", "Test id", l.getId());
		Assert.assertEquals("Location Name", "Test Name", l.getName());

		Assert.assertEquals("Drop Start Condition", 0 , l.getPickupCars());

		l.addDropCar();
		Assert.assertEquals("Drop 1", 1, l.getDropCars());

		l.addDropCar();
		Assert.assertEquals("Drop second", 2, l.getDropCars());

		l.deleteDropCar();
		Assert.assertEquals("Delete Drop", 1, l.getDropCars());

		l.deleteDropCar();
		Assert.assertEquals("Delete Drop second", 0, l.getDropCars());
	}

	// test car support
	public void testCarSupport() {
		Location l = new Location("Test id", "Test Name");
		Assert.assertEquals("Location id", "Test id", l.getId());
		Assert.assertEquals("Location Name", "Test Name", l.getName());

		Assert.assertEquals("Used Length", 0, l.getUsedLength());
		Assert.assertEquals("Number of Cars", 0, l.getNumberCars());

		jmri.jmrit.operations.cars.Car c1 = new jmri.jmrit.operations.cars.Car("TESTROAD", "TESTNUMBER1");
		c1.setLength("40");
		l.addCar(c1);

		Assert.assertEquals("Number of Cars", 1, l.getNumberCars());
		Assert.assertEquals("Used Length one car", 44, l.getUsedLength()); // Drawbar length is 4

		jmri.jmrit.operations.cars.Car c2 = new jmri.jmrit.operations.cars.Car("TESTROAD", "TESTNUMBER2");
		c2.setLength("33");
		l.addCar(c2);

		Assert.assertEquals("Number of Cars", 2, l.getNumberCars());
		Assert.assertEquals("Used Length one car", 40+4+33+4, l.getUsedLength()); // Drawbar length is 4

		jmri.jmrit.operations.cars.Car c3 = new jmri.jmrit.operations.cars.Car("TESTROAD", "TESTNUMBER3");
		c3.setLength("50");
		l.addCar(c3);

		Assert.assertEquals("Number of Cars", 3, l.getNumberCars());
		Assert.assertEquals("Used Length one car", 40+4+33+4+50+4, l.getUsedLength()); // Drawbar length is 4

		l.deleteCar(c2);

		Assert.assertEquals("Number of Cars", 2, l.getNumberCars());
		Assert.assertEquals("Used Length one car", 40+4+50+4, l.getUsedLength()); // Drawbar length is 4

		l.deleteCar(c1);

		Assert.assertEquals("Number of Cars", 1, l.getNumberCars());
		Assert.assertEquals("Used Length one car", 50+4, l.getUsedLength()); // Drawbar length is 4

		l.deleteCar(c3);

		Assert.assertEquals("Number of Cars", 0, l.getNumberCars());
		Assert.assertEquals("Used Length one car", 0, l.getUsedLength()); // Drawbar length is 4
	}

	// TODO: Add tests for adding + deleting the same cars

	// TODO: Add tests for secondary locations

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

	public OperationsLocationsTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {"-noloading", OperationsLocationsTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(OperationsLocationsTest.class);
		return suite;
	}

    // The minimal setup for log4J
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }
}
