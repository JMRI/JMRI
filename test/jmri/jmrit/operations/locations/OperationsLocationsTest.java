// OperationsLocationsTest.java

package jmri.jmrit.operations.locations;

import jmri.jmrit.XmlFile;
import jmri.jmrit.operations.locations.LocationManagerXml;
import java.util.List;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintStream;

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
 * @version $Revision: 1.3 $
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

	// test public constants
	public void testConstants() {
		Location l = new Location("Test id", "Test Name");
		Assert.assertEquals("Location id", "Test id", l.getId());
		Assert.assertEquals("Location Name", "Test Name", l.getName());

		Assert.assertEquals("Location Constant NORMAL", 1, l.NORMAL);
		Assert.assertEquals("Location Constant STAGING", 2, l.STAGING);

		Assert.assertEquals("Location Constant EAST", 1, l.EAST);
		Assert.assertEquals("Location Constant WEST", 2, l.WEST);
		Assert.assertEquals("Location Constant NORTH", 4, l.NORTH);
		Assert.assertEquals("Location Constant SOUTH", 8, l.SOUTH);

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

	// test car duplicates support
	public void testCarDuplicatesSupport() {
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

		l.addCar(c1);

		Assert.assertEquals("Number of Cars", 3, l.getNumberCars());
		Assert.assertEquals("Used Length one car", 40+4+33+4+40+4, l.getUsedLength()); // Drawbar length is 4

	}

	// test location Xml create support
	public void testXMLCreate() {

                LocationManager manager = LocationManager.instance();
                List locationList = manager.getLocationsByIdList();
                Assert.assertEquals("Starting Number of Locations", 0, locationList.size());
                manager.newLocation("Test Location 2");
                manager.newLocation("Test Location 1");
                manager.newLocation("Test Location 3");

                Assert.assertEquals("New Location by Id 1", "Test Location 2", manager.getLocationById("1").getName());
                Assert.assertEquals("New Location by Id 2", "Test Location 1", manager.getLocationById("2").getName());
                Assert.assertEquals("New Location by Id 3", "Test Location 3", manager.getLocationById("3").getName());

                Assert.assertEquals("New Location by Name 1", "Test Location 1", manager.getLocationByName("Test Location 1").getName());
                Assert.assertEquals("New Location by Name 2", "Test Location 2", manager.getLocationByName("Test Location 2").getName());
                Assert.assertEquals("New Location by Name 3", "Test Location 3", manager.getLocationByName("Test Location 3").getName());

                manager.getLocationByName("Test Location 1").setComment("Test Location 1 Comment");
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

                LocationManagerXml.instance().writeOperationsLocationFile();

                manager.newLocation("Test Location 4");
                manager.newLocation("Test Location 5");
                manager.newLocation("Test Location 6");
                manager.getLocationByName("Test Location 2").setComment("Test Location 2 Changed Comment");
                
                LocationManagerXml.instance().writeOperationsLocationFile();
        }

	// test location Xml read support preparation
	public void testXMLReadPrep() {
                LocationManager manager = LocationManager.instance();
                List locationList = manager.getLocationsByIdList();
                Assert.assertEquals("Starting Number of Locations", 6, locationList.size());

                //  Revert the main xml file back to the backup file.
                LocationManagerXml.instance().revertBackupFile(XmlFile.prefsDir()+File.separator+"operations"+File.separator+"temp"+File.separator+"OperationsTestLocationRoster.xml");

                //  Need to dispose of the LocationManager's list and hash table
                manager.dispose();	
	}

	// test location Xml read support
	public void testXMLRead() throws Exception  {
                LocationManager manager = LocationManager.instance();
                List locationList = manager.getLocationsByNameList();

                // The dispose has removed all locations from the Manager.
                Assert.assertEquals("Starting Number of Locations", 0, locationList.size());

                // Need to force a re-read of the xml file.
                LocationManagerXml.instance().readFile(XmlFile.prefsDir()+"operations"+File.separator+"temp"+File.separator+"OperationsTestLocationRoster.xml");

                locationList = manager.getLocationsByNameList();
                Assert.assertEquals("Starting Number of Locations", 3, locationList.size());

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
        new LocationManagerXml(){ {_instance = this; setOperationsFileName("temp"+File.separator+"OperationsTestLocationRoster.xml");}};
	// store files in "temp"
	XmlFile.ensurePrefsPresent(XmlFile.prefsDir());
	XmlFile.ensurePrefsPresent(XmlFile.prefsDir()+File.separator+"operations");
	XmlFile.ensurePrefsPresent(XmlFile.prefsDir()+File.separator+"operations"+File.separator+"temp");
	XmlFile.ensurePrefsPresent(XmlFile.prefsDir()+"temp");

	// remove existing Operations file if its there
/*
        File fr = new File(XmlFile.prefsDir()+"operations"+File.separator+"temp"+File.separator+"OperationsTestLocationRoster.xml");
	fr.delete();
	File fb = new File(XmlFile.prefsDir()+"operations"+File.separator+"temp"+File.separator+"OperationsTestLocationRoster.xml.bak");
	fb.delete();
*/
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
    protected void tearDown() { 
        apps.tests.Log4JFixture.tearDown();
    }
}
