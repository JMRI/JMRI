// OperationsCarsTest.java

package jmri.jmrit.operations.rollingstock.cars;

import java.io.File;
import java.util.List;
import javax.swing.JComboBox;
import jmri.jmrit.XmlFile;
import jmri.jmrit.operations.locations.LocationManagerXml;
import jmri.jmrit.operations.rollingstock.engines.EngineManagerXml;
import jmri.jmrit.operations.routes.RouteManagerXml;
import jmri.jmrit.operations.setup.OperationsXml;
import jmri.jmrit.operations.trains.TrainManagerXml;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Tests for the Operations RollingStock Cars class
 * Last manually cross-checked on 20090131
 * 
 * Still to do:
 *   Everything  
 * 
 * @author	Bob Coleman Copyright (C) 2008, 2009
 * @version $Revision: 1.4 $
 */
public class OperationsCarsTest extends TestCase {

	// test creation
	public void testCreate() {
		Car c1 = new Car("TESTROAD", "TESTNUMBER1");
		c1.setType("TESTTYPE");
		c1.setLength("TESTLENGTH");
		c1.setColor("TESTCOLOR");
		c1.setHazardous(true);
		c1.setFred(true);
		c1.setCaboose(true);
		c1.setWeight("TESTWEIGHT");
		c1.setBuilt("TESTBUILT");
		c1.setOwner("TESTOWNER");
		c1.setComment("TESTCOMMENT");
		c1.setMoves(5);

		Assert.assertEquals("Car Road", "TESTROAD", c1.getRoad());
		Assert.assertEquals("Car Number", "TESTNUMBER1", c1.getNumber());
		Assert.assertEquals("Car ID", "TESTROAD"+"TESTNUMBER1", c1.getId());
		Assert.assertEquals("Car Type", "TESTTYPE", c1.getType());
		Assert.assertEquals("Car Length", "TESTLENGTH", c1.getLength());
		Assert.assertEquals("Car Color", "TESTCOLOR", c1.getColor());
		Assert.assertTrue("Car Hazardous", c1.isHazardous());
		Assert.assertTrue("Car Fred", c1.hasFred());
		Assert.assertTrue("Car Caboose", c1.isCaboose());
		Assert.assertEquals("Car Weight", "TESTWEIGHT", c1.getWeight());
		Assert.assertEquals("Car Built", "TESTBUILT", c1.getBuilt());
		Assert.assertEquals("Car Owner", "TESTOWNER", c1.getOwner());
		Assert.assertEquals("Car Comment", "TESTCOMMENT", c1.getComment());
		Assert.assertEquals("Car Moves", 5, c1.getMoves());
	}

	public void testCarColors() {
//  Comment out tests that rely upon manager having run until that gets fixed
		CarColors cc1 = new CarColors();
//		CarColors cc1;
//		cc1 = CarColors.instance();

//		Assert.assertTrue("Car Color Predefined Red", cc1.containsName("Red"));
//		Assert.assertTrue("Car Color Predefined Red", cc1.containsName("Blue"));

		cc1.addName("BoxCar Red");
		Assert.assertTrue("Car Color Add", cc1.containsName("BoxCar Red"));
		Assert.assertFalse("Car Color Never Added Dirty Blue", cc1.containsName("Dirty Blue"));
		cc1.addName("Ugly Brown");
		Assert.assertTrue("Car Color Still Has BoxCar Red", cc1.containsName("BoxCar Red"));
		Assert.assertTrue("Car Color Add Ugly Brown", cc1.containsName("Ugly Brown"));
		String[] colors = cc1.getNames();
		Assert.assertEquals("First color name", "Ugly Brown", colors[0]);
		Assert.assertEquals("2nd color name", "BoxCar Red", colors[1]);
		JComboBox box = cc1.getComboBox();
		Assert.assertEquals("First comboBox color name", "Ugly Brown", box.getItemAt(0));
		Assert.assertEquals("2nd comboBox color name", "BoxCar Red", box.getItemAt(1));
		cc1.deleteName("Ugly Brown");
		Assert.assertFalse("Car Color Delete Ugly Brown", cc1.containsName("Ugly Brown"));
		cc1.deleteName("BoxCar Red");
		Assert.assertFalse("Car Color Delete BoxCar Red", cc1.containsName("BoxCar Red"));
	}

	public void testCarLengths() {
//  Comment out tests that rely upon manager having run until that gets fixed
		CarLengths cl1 = new CarLengths();
//		CarLengths cl1;
//		cl1 = CarLengths.instance();

//		Assert.assertTrue("Car Length Predefined 40", cl1.containsName("40"));
//		Assert.assertTrue("Car Length Predefined 32", cl1.containsName("32"));
//		Assert.assertTrue("Car Length Predefined 60", cl1.containsName("60"));

		cl1.addName("1");
		Assert.assertTrue("Car Length Add 1", cl1.containsName("1"));
		Assert.assertFalse("Car Length Never Added 13", cl1.containsName("13"));
		cl1.addName("2");
		Assert.assertTrue("Car Length Still Has 1", cl1.containsName("1"));
		Assert.assertTrue("Car Length Add s2", cl1.containsName("2"));
		String[] lengths = cl1.getNames();
		Assert.assertEquals("First length name", "2", lengths[0]);
		Assert.assertEquals("2nd length name", "1", lengths[1]);
		JComboBox box = cl1.getComboBox();
		Assert.assertEquals("First comboBox length name", "2", box.getItemAt(0));
		Assert.assertEquals("2nd comboBox length name", "1", box.getItemAt(1));
		cl1.deleteName("2");
		Assert.assertFalse("Car Length Delete 2", cl1.containsName("2"));
		cl1.deleteName("1");
		Assert.assertFalse("Car Length Delete 1", cl1.containsName("1"));
	}

	public void testCarOwnwers() {
//  Comment out tests that rely upon manager having run until that gets fixed
		CarOwners co1 = new CarOwners();
//		CarOwners co1;
//		co1 = CarOwners.instance();

		co1.addName("Rich Guy 1");
		Assert.assertTrue("Car Owner Add", co1.containsName("Rich Guy 1"));
		Assert.assertFalse("Car Owner Never Added", co1.containsName("Richer Guy 2"));
		co1.addName("Really Rich 3");
		Assert.assertTrue("Car Owner Still Has", co1.containsName("Rich Guy 1"));
		Assert.assertTrue("Car Owner Add second", co1.containsName("Really Rich 3"));
		String[] owners = co1.getNames();
		Assert.assertEquals("First owner name", "Really Rich 3", owners[0]);
		Assert.assertEquals("2nd owner name", "Rich Guy 1", owners[1]);
		JComboBox box = co1.getComboBox();
		Assert.assertEquals("First comboBox owner name", "Really Rich 3", box.getItemAt(0));
		Assert.assertEquals("2nd comboBox owner name", "Rich Guy 1", box.getItemAt(1));
		co1.deleteName("Really Rich 3");
		Assert.assertFalse("Car Owner Delete", co1.containsName("Really Rich 3"));
		co1.deleteName("Rich Guy 1");
		Assert.assertFalse("Car Owner Delete second", co1.containsName("Rich Guy 1"));
	}

	public void testCarRoads() {
//  Comment out tests that rely upon manager having run until that gets fixed
		CarRoads cr1 = new CarRoads();
//		CarRoads cr1;
//		cr1 = CarRoads.instance();

//		Assert.assertTrue("Car Roads Predefined AA", cr1.containsName("AA"));
//		Assert.assertTrue("Car Roads Predefined CP", cr1.containsName("CP"));
//		Assert.assertTrue("Car Roads Predefined CN", cr1.containsName("CN"));
//		Assert.assertTrue("Car Roads Predefined UP", cr1.containsName("UP"));

		cr1.addName("Road New1");
		Assert.assertTrue("Car Roads Add New1", cr1.containsName("Road New1"));
		Assert.assertFalse("Car Roads Never Added New2", cr1.containsName("Road New2"));
		cr1.addName("Road New3");
		Assert.assertTrue("Car Roads Still Has New1", cr1.containsName("Road New1"));
		Assert.assertTrue("Car Roads Add New3", cr1.containsName("Road New3"));
		cr1.replaceName("Road New3", "Road New4");
		Assert.assertFalse("Car Roads replace New3", cr1.containsName("Road New3"));
		Assert.assertTrue("Car Roads replace New3 with New4", cr1.containsName("Road New4"));
		String[] roads = cr1.getNames();
		Assert.assertEquals("First road name", "Road New4", roads[0]);
		Assert.assertEquals("2nd road name", "Road New1", roads[1]);
		JComboBox box = cr1.getComboBox();
		Assert.assertEquals("First comboBox road name", "Road New4", box.getItemAt(0));
		Assert.assertEquals("2nd comboBox road name", "Road New1", box.getItemAt(1));
		cr1.deleteName("Road New4");
		Assert.assertFalse("Car Roads Delete New4", cr1.containsName("Road New4"));
		cr1.deleteName("Road New1");
		Assert.assertFalse("Car Roads Delete New1", cr1.containsName("Road New1"));
	}

	public void testCarTypes() {
//  Comment out tests that rely upon manager having run until that gets fixed
		CarTypes ct1 = new CarTypes();
//		CarTypes ct1;
//		ct1 = CarTypes.instance();

//		Assert.assertTrue("Car Types Predefined Engine", ct1.containsName("Engine"));
//		Assert.assertTrue("Car Types Predefined Caboose", ct1.containsName("Caboose"));

		ct1.addName("Type New1");
		Assert.assertTrue("Car Types Add New1", ct1.containsName("Type New1"));
		Assert.assertFalse("Car Types Never Added New2", ct1.containsName("Type New2"));
		ct1.addName("Type New3");
		Assert.assertTrue("Car Types Still Has New1", ct1.containsName("Type New1"));
		Assert.assertTrue("Car Types Add New3", ct1.containsName("Type New3"));
		ct1.replaceName("Type New3", "Type New4");
		Assert.assertFalse("Car Types replace New3", ct1.containsName("Type New3"));
		Assert.assertTrue("Car Types replace New3 with New4", ct1.containsName("Type New4"));
		String[] types = ct1.getNames();
		Assert.assertEquals("First type name", "Type New4", types[0]);
		Assert.assertEquals("2nd type name", "Type New1", types[1]);
		JComboBox box = ct1.getComboBox();
		Assert.assertEquals("First comboBox type name", "Type New4", box.getItemAt(0));
		Assert.assertEquals("2nd comboBox type name", "Type New1", box.getItemAt(1));
		ct1.deleteName("Type New4");
		Assert.assertFalse("Car Types Delete New4", ct1.containsName("Type New4"));
		ct1.deleteName("Type New1");
		Assert.assertFalse("Car Types Delete New1", ct1.containsName("Type New1"));
	}

	public void testKernel() {
		Kernel k1 = new Kernel("TESTKERNEL");
		Assert.assertEquals("Kernel Name", "TESTKERNEL", k1.getName());

		Car c1 = new Car("TESTCARROAD", "TESTCARNUMBER1");
		c1.setLength("40");
		c1.setWeight("1000");
		Car c2 = new Car("TESTCARROAD", "TESTCARNUMBER2");
		c2.setLength("60");
		c2.setWeight("2000");
		Car c3 = new Car("TESTCARROAD", "TESTCARNUMBER3");
		c3.setLength("50");
		c3.setWeight("1500");

		Assert.assertEquals("Kernel Initial Length", 0, k1.getLength());
		Assert.assertEquals("Kernel Initial Weight", 0.0, k1.getWeight(), 0.0);

		k1.addCar(c1);
		Assert.assertEquals("Kernel Car 1 Length", 40+4, k1.getLength());
		Assert.assertEquals("Kernel Car 1 Weight", 1000.0, k1.getWeight(), 0.0);

		k1.addCar(c2);
		Assert.assertEquals("Kernel Car 2 Length", 40+4+60+4, k1.getLength());
		Assert.assertEquals("Kernel Car 2 Weight", 3000.0, k1.getWeight(), 0.0);

		k1.addCar(c3);
		Assert.assertEquals("Kernel Car 3 Length", 40+4+60+4+50+4, k1.getLength());
		Assert.assertEquals("Kernel Car 3 Weight", 4500.0, k1.getWeight(), 0.0);

		k1.setLeadCar(c2);
		Assert.assertTrue("Kernel Lead Car 1", k1.isLeadCar(c2));
		Assert.assertFalse("Kernel Lead Car 2", k1.isLeadCar(c1));
		Assert.assertFalse("Kernel Lead Car 3", k1.isLeadCar(c3));

		k1.deleteCar(c2);
		Assert.assertEquals("Kernel Car Delete 2 Length", 40+4+50+4, k1.getLength());
		Assert.assertEquals("Kernel Car Delete 2 Weight", 2500.0, k1.getWeight(), 0.0);

		k1.deleteCar(c1);
		Assert.assertEquals("Kernel Car Delete 1 Length", 50+4, k1.getLength());
		Assert.assertEquals("Kernel Car Delete 1 Weight", 1500.0, k1.getWeight(), 0.0);

		k1.deleteCar(c3);
		Assert.assertEquals("Kernel Car Delete 3 Length", 0, k1.getLength());
		Assert.assertEquals("Kernel Car Delete 3 Weight", 0.0, k1.getWeight(), 0.0);

	}

	public void testCarKernel() {
		Kernel kold = new Kernel("TESTKERNELOLD");
		Assert.assertEquals("Kernel Name old", "TESTKERNELOLD", kold.getName());

		Kernel knew = new Kernel("TESTKERNELNEW");
		Assert.assertEquals("Kernel Name new", "TESTKERNELNEW", knew.getName());

		Car c1 = new Car("TESTCARROAD", "TESTCARNUMBER1");
		c1.setLength("40");
		c1.setWeight("1000");
		Car c2 = new Car("TESTCARROAD", "TESTCARNUMBER2");
		c2.setLength("60");
		c2.setWeight("2000");
		Car c3 = new Car("TESTCARROAD", "TESTCARNUMBER3");
		c3.setLength("50");
		c3.setWeight("1500");

		//  All three cars start out in the old kernel with car 1 as the lead car.
		c1.setKernel(kold);
		c2.setKernel(kold);
		c3.setKernel(kold);
		Assert.assertEquals("Kernel Name for car 1 before", "TESTKERNELOLD", c1.getKernelName());
		Assert.assertEquals("Kernel Name for car 2 before", "TESTKERNELOLD", c2.getKernelName());
		Assert.assertEquals("Kernel Name for car 3 before", "TESTKERNELOLD", c3.getKernelName());
		Assert.assertEquals("Kernel old length before", 40+4+60+4+50+4, kold.getLength());
		Assert.assertEquals("Kernel new length before", 0, knew.getLength());
		Assert.assertTrue("Kernel old Lead is Car 1 before", kold.isLeadCar(c1));
		Assert.assertFalse("Kernel old Lead is not Car 2 before", kold.isLeadCar(c2));
		Assert.assertFalse("Kernel old Lead is not Car 3 before", kold.isLeadCar(c3));
		Assert.assertFalse("Kernel new Lead is not Car 1 before", knew.isLeadCar(c1));
		Assert.assertFalse("Kernel new Lead is not Car 2 before", knew.isLeadCar(c2));
		Assert.assertFalse("Kernel new Lead is not Car 3 before", knew.isLeadCar(c3));

		//  Move car 1 to the new kernel where it will be the lead car.
		//  Car 2 should now be the lead car of the old kernel.
		c1.setKernel(knew);
		Assert.assertEquals("Kernel Name for car 1 after", "TESTKERNELNEW", c1.getKernelName());
		Assert.assertEquals("Kernel Name for car 2 after", "TESTKERNELOLD", c2.getKernelName());
		Assert.assertEquals("Kernel Name for car 3 after", "TESTKERNELOLD", c3.getKernelName());
		Assert.assertEquals("Kernel old length after", 60+4+50+4, kold.getLength());
		Assert.assertEquals("Kernel new length after", 40+4, knew.getLength());
		Assert.assertFalse("Kernel old Lead is not Car 1 after", kold.isLeadCar(c1));
		Assert.assertTrue("Kernel old Lead is Car 2 after", kold.isLeadCar(c2));
		Assert.assertFalse("Kernel old Lead is not Car 3 after", kold.isLeadCar(c3));
		Assert.assertTrue("Kernel new Lead is Car 1 after", knew.isLeadCar(c1));
		Assert.assertFalse("Kernel new Lead is not Car 2 after", knew.isLeadCar(c2));
		Assert.assertFalse("Kernel new Lead is not Car 3 after", knew.isLeadCar(c3));

		//  Move car 3 to the new kernel.
		c3.setKernel(knew);
		Assert.assertEquals("Kernel Name for car 1 after3", "TESTKERNELNEW", c1.getKernelName());
		Assert.assertEquals("Kernel Name for car 2 after3", "TESTKERNELOLD", c2.getKernelName());
		Assert.assertEquals("Kernel Name for car 3 after3", "TESTKERNELNEW", c3.getKernelName());
		Assert.assertEquals("Kernel old length after3", 60+4, kold.getLength());
		Assert.assertEquals("Kernel new length after3", 40+4+50+4, knew.getLength());
		Assert.assertFalse("Kernel old Lead is not Car 1 after3", kold.isLeadCar(c1));
		Assert.assertTrue("Kernel old Lead is Car 2 after3", kold.isLeadCar(c2));
		Assert.assertFalse("Kernel old Lead is not Car 3 after3", kold.isLeadCar(c3));
		Assert.assertTrue("Kernel new Lead is Car 1 after3", knew.isLeadCar(c1));
		Assert.assertFalse("Kernel new Lead is not Car 2 after3", knew.isLeadCar(c2));
		Assert.assertFalse("Kernel new Lead is not Car 3 after3", knew.isLeadCar(c3));
	}

	// test location Xml create support
	public void testXMLCreate() throws Exception {

                CarManager manager = CarManager.instance();
                List tempcarList = manager.getCarsByIdList();

                Assert.assertEquals("Starting Number of Cars", 0, tempcarList.size());
                manager.newCar("CP", "Test Number 1");
                manager.newCar("ACL", "Test Number 2");
                manager.newCar("CP", "Test Number 3");

                tempcarList = manager.getCarsByIdList();

                Assert.assertEquals("New Number of Cars", 3, tempcarList.size());
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
                

                CarManagerXml.instance().writeOperationsCarFile();

                // Add some more engines and write file again
                // so we can test the backup facility
                manager.newCar("CP", "Test Number 4");
                manager.newCar("CP", "Test Number 5");
                manager.newCar("CP", "Test Number 6");
                manager.getCarByRoadAndNumber("ACL", "Test Number 2").setComment("Test Engine 2 Changed Comment");
                
                CarManagerXml.instance().writeOperationsCarFile();
        }

	// TODO: Add tests for location

	// TODO: Add tests for track location

	// TODO: Add tests for destination

	// TODO: Add tests for track destination

	// TODO: Add tests for train

	// TODO: Add tests for route location

	// TODO: Add tests for route track location

	// TODO: Add tests for route destination

	// TODO: Add tests for route track destination

	// TODO: Add test for import

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

        // Need to clear out CarManager global variables
        CarManager manager = CarManager.instance();
        
        List tempkernelList = manager.getKernelNameList();
        for (int i = 0; i < tempkernelList.size(); i++) {
            String kernelId = (String)tempkernelList.get(i);
            manager.deleteKernel(kernelId);
        }
 
        CarColors.instance().dispose();
        CarLengths.instance().dispose();
        CarLoads.instance().dispose();
        CarRoads.instance().dispose();
        CarTypes.instance().dispose();
        manager.dispose();
    }

	public OperationsCarsTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {"-noloading", OperationsCarsTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(OperationsCarsTest.class);
		return suite;
	}

    // The minimal setup for log4J
    @Override
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }
}
