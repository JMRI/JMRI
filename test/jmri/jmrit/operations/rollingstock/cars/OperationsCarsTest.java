// OperationsCarsTest.java

package jmri.jmrit.operations.rollingstock.cars;

import java.io.File;
import java.util.List;
import javax.swing.JComboBox;
import jmri.jmrit.operations.locations.LocationManagerXml;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.rollingstock.engines.EngineManagerXml;
import jmri.jmrit.operations.routes.RouteManagerXml;
import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.setup.OperationsSetupXml;
import jmri.jmrit.operations.trains.TrainManagerXml;
import jmri.jmrit.operations.trains.Train;
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
 * @version $Revision: 1.14 $
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
		CarColors cc1 = CarColors.instance();
		cc1.getNames();	// load predefined colors

		Assert.assertTrue("Car Color Predefined Red", cc1.containsName("Red"));
		Assert.assertTrue("Car Color Predefined Blue", cc1.containsName("Blue"));

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
		CarLengths cl1 = CarLengths.instance();
		cl1.getNames();	// load predefined lengths

		Assert.assertTrue("Car Length Predefined 40", cl1.containsName("40"));
		Assert.assertTrue("Car Length Predefined 32", cl1.containsName("32"));
		Assert.assertTrue("Car Length Predefined 60", cl1.containsName("60"));

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

	public void testCarOwners() {
		CarOwners co1 = CarOwners.instance();

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
		CarRoads cr1 = CarRoads.instance();
		cr1.getNames();	//load predefined roads

		Assert.assertTrue("Car Roads Predefined AA", cr1.containsName("AA"));
		Assert.assertTrue("Car Roads Predefined CP", cr1.containsName("CP"));
		Assert.assertTrue("Car Roads Predefined CN", cr1.containsName("CN"));
		Assert.assertTrue("Car Roads Predefined UP", cr1.containsName("UP"));

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
		CarTypes ct1 = CarTypes.instance();
		ct1.getNames();	//Load predefined car types

		Assert.assertTrue("Car Types Predefined Boxcar", ct1.containsName("Boxcar"));
		Assert.assertTrue("Car Types Predefined Caboose", ct1.containsName("Caboose"));

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
	
	public void testCarManager(){
        CarManager manager = CarManager.instance();
        List<String> carList = manager.getByIdList();

        Assert.assertEquals("Starting Number of Cars", 0, carList.size());
        Car c1 = manager.newCar("CP", "1");
        Car c2 = manager.newCar("ACL", "3");
        Car c3 = manager.newCar("CP", "3");
        Car c4 = manager.newCar("CP", "3-1");
        Car c5 = manager.newCar("PC", "2");
        Car c6 = manager.newCar("AA", "1");
        
        //setup the cars
        c1.setBuilt("2800");
        c2.setBuilt("1212");
        c3.setBuilt("100");
        c4.setBuilt("10");
        c5.setBuilt("1000");
        c6.setBuilt("1956");
        
        c1.setColor("RED");
        c2.setColor("BLUE");
        c3.setColor("YELLOW");
        c4.setColor("BLACK");
        c5.setColor("ROSE");
        c6.setColor("TUSCAN");
        
        c1.setType("Boxcar");
        c2.setType("Boxcar");
        c3.setType("Boxcar");
        c4.setType("Boxcar");
        c5.setType("Boxcar");
        c6.setType("Boxcar");
        
        c1.setLength("13");
        c2.setLength("9");
        c3.setLength("12");
        c4.setLength("10");
        c5.setLength("11");
        c6.setLength("14");
        
        Location l1 = new Location("id1", "B");
        Track l1t1 = l1.addTrack("A",Track.SIDING);
        Track l1t2 = l1.addTrack("B",Track.SIDING);
        Location l2 = new Location("id2", "C");
        Track l2t1 = l1.addTrack("B",Track.SIDING);
        Track l2t2 = l1.addTrack("A",Track.SIDING);
        Location l3 = new Location("id3", "A");
        Track l3t1 = l1.addTrack("B",Track.SIDING);
        Track l3t2 = l1.addTrack("A",Track.SIDING);

        // add track lengths       
        l1t1.setLength(100);
        l1t2.setLength(100);
        l2t1.setLength(100);
        l2t2.setLength(100);
        l3t1.setLength(100);
        l3t2.setLength(100);
        
        l1.addTypeName("Boxcar");
        l2.addTypeName("Boxcar");
        l3.addTypeName("Boxcar");
        l1t1.addTypeName("Boxcar");
        l1t2.addTypeName("Boxcar");
        l2t1.addTypeName("Boxcar");
        l2t2.addTypeName("Boxcar");
        l3t1.addTypeName("Boxcar");
        l3t2.addTypeName("Boxcar");
        
        CarTypes ct = CarTypes.instance();
        ct.addName("Boxcar");
        
        // place cars on tracks
        Assert.assertEquals("place c1", Car.OKAY, c1.setLocation(l1, l1t1));
        Assert.assertEquals("place c2", Car.OKAY, c2.setLocation(l1, l1t2));
        Assert.assertEquals("place c3", Car.OKAY, c3.setLocation(l2, l2t1));
        Assert.assertEquals("place c4", Car.OKAY, c4.setLocation(l2, l2t2));
        Assert.assertEquals("place c5", Car.OKAY, c5.setLocation(l3, l3t1));
        Assert.assertEquals("place c6", Car.OKAY, c6.setLocation(l3, l3t2));

        // set car destinations
        Assert.assertEquals("destination c1", Car.OKAY, c1.setDestination(l3, l3t1));
        Assert.assertEquals("destination c2", Car.OKAY, c2.setDestination(l3, l3t2));
        Assert.assertEquals("destination c3", Car.OKAY, c3.setDestination(l2, l2t2));
        Assert.assertEquals("destination c4", Car.OKAY, c4.setDestination(l2, l2t1));
        Assert.assertEquals("destination c5", Car.OKAY, c5.setDestination(l1, l1t1));
        Assert.assertEquals("destination c6", Car.OKAY, c6.setDestination(l1, l1t2));

        // set car weight so there won't be an exception when setting car in a kernel
        c1.setWeight("20");
        c2.setWeight("6");
        c3.setWeight("21");
        c4.setWeight("20");
        c5.setWeight("50");
        c6.setWeight("30"); 
        
        c1.setKernel(new Kernel("F"));
        c2.setKernel(new Kernel("D"));
        c3.setKernel(new Kernel("B"));
        c4.setKernel(new Kernel("A"));
        c5.setKernel(new Kernel("C"));
        c6.setKernel(new Kernel("E"));
        
        c1.setMoves(2);
        c2.setMoves(44);
        c3.setMoves(99999);
        c4.setMoves(33);
        c5.setMoves(4);
        c6.setMoves(9999);
        
        c1.setRfid("SQ1");
        c2.setRfid("1Ab");
        c3.setRfid("Ase");
        c4.setRfid("asd");
        c5.setRfid("93F");
        c6.setRfid("B12");
        
        c1.setLoad("Nuts");
        c2.setLoad("Screws");
        c3.setLoad("Tools");
        c4.setLoad("Fuel");
        c5.setLoad("Bags");
        c6.setLoad("Nails");

        c1.setOwner("LAST");
        c2.setOwner("FOOL");
        c3.setOwner("AAA");
        c4.setOwner("DAD");
        c5.setOwner("DAB");
        c6.setOwner("BOB");
        
        // make a couple of cabooses
        c4.setCaboose(true);
        c6.setCaboose(true);
        
        // car with FRED
        c5.setFred(true);
        
        Route r = new Route("id","Test");
        r.addLocation(l1);
        r.addLocation(l2);
        r.addLocation(l3);
        
        Train t1 = new Train("id1", "F");
        t1.setRoute(r);
        Train t3 = new Train("id3", "E");
        t3.setRoute(r);
        
        c1.setTrain(t1);
        c2.setTrain(t3);
        c3.setTrain(t3);
        c4.setTrain(new Train("id4", "B"));
        c5.setTrain(t3);
        c6.setTrain(new Train("id6", "A"));

        // now get cars by id
        carList = manager.getByIdList();
        Assert.assertEquals("Number of Cars by id", 6, carList.size());
        Assert.assertEquals("1st car in list by id", c6, manager.getById(carList.get(0)));
        Assert.assertEquals("2nd car in list by id", c2, manager.getById(carList.get(1)));
        Assert.assertEquals("3rd car in list by id", c1, manager.getById(carList.get(2)));
        Assert.assertEquals("4th car in list by id", c3, manager.getById(carList.get(3)));
        Assert.assertEquals("5th car in list by id", c4, manager.getById(carList.get(4)));
        Assert.assertEquals("6th car in list by id", c5, manager.getById(carList.get(5)));
   
        // now get cars by built
        carList = manager.getByBuiltList();
        Assert.assertEquals("Number of Cars by built", 6, carList.size());
        Assert.assertEquals("1st car in list by built", c4, manager.getById(carList.get(0)));
        Assert.assertEquals("2nd car in list by built", c3, manager.getById(carList.get(1)));
        Assert.assertEquals("3rd car in list by built", c5, manager.getById(carList.get(2)));
        Assert.assertEquals("4th car in list by built", c2, manager.getById(carList.get(3)));
        Assert.assertEquals("5th car in list by built", c6, manager.getById(carList.get(4)));
        Assert.assertEquals("6th car in list by built", c1, manager.getById(carList.get(5)));
  
        // now get cars by moves
        carList = manager.getByMovesList();
        Assert.assertEquals("Number of Cars by move", 6, carList.size());
        Assert.assertEquals("1st car in list by move", c1, manager.getById(carList.get(0)));
        Assert.assertEquals("2nd car in list by move", c5, manager.getById(carList.get(1)));
        Assert.assertEquals("3rd car in list by move", c4, manager.getById(carList.get(2)));
        Assert.assertEquals("4th car in list by move", c2, manager.getById(carList.get(3)));
        Assert.assertEquals("5th car in list by move", c6, manager.getById(carList.get(4)));
        Assert.assertEquals("6th car in list by move", c3, manager.getById(carList.get(5)));
  
        // now get cars by owner
        carList = manager.getByOwnerList();
        Assert.assertEquals("Number of Cars by owner", 6, carList.size());
        Assert.assertEquals("1st car in list by owner", c3, manager.getById(carList.get(0)));
        Assert.assertEquals("2nd car in list by owner", c6, manager.getById(carList.get(1)));
        Assert.assertEquals("3rd car in list by owner", c5, manager.getById(carList.get(2)));
        Assert.assertEquals("4th car in list by owner", c4, manager.getById(carList.get(3)));
        Assert.assertEquals("5th car in list by owner", c2, manager.getById(carList.get(4)));
        Assert.assertEquals("6th car in list by owner", c1, manager.getById(carList.get(5)));
 
        // now get cars by color
        carList = manager.getByColorList();
        Assert.assertEquals("Number of Cars by color", 6, carList.size());
        Assert.assertEquals("1st car in list by color", c4, manager.getById(carList.get(0)));
        Assert.assertEquals("2nd car in list by color", c2, manager.getById(carList.get(1)));
        Assert.assertEquals("3rd car in list by color", c1, manager.getById(carList.get(2)));
        Assert.assertEquals("4th car in list by color", c5, manager.getById(carList.get(3)));
        Assert.assertEquals("5th car in list by color", c6, manager.getById(carList.get(4)));
        Assert.assertEquals("6th car in list by color", c3, manager.getById(carList.get(5)));
 
        // now get cars by road name
        carList = manager.getByRoadNameList();
        Assert.assertEquals("Number of Cars by road name", 6, carList.size());
        Assert.assertEquals("1st car in list by road name", c6, manager.getById(carList.get(0)));
        Assert.assertEquals("2nd car in list by road name", c2, manager.getById(carList.get(1)));
        Assert.assertEquals("3rd car in list by road name", c1, manager.getById(carList.get(2)));
        Assert.assertEquals("4th car in list by road name", c3, manager.getById(carList.get(3)));
        Assert.assertEquals("5th car in list by road name", c4, manager.getById(carList.get(4)));
        Assert.assertEquals("6th car in list by road name", c5, manager.getById(carList.get(5)));

        // now get cars by load
        carList = manager.getByLoadList();
        Assert.assertEquals("Number of Cars by load", 6, carList.size());
        Assert.assertEquals("1st car in list by load", c5, manager.getById(carList.get(0)));
        Assert.assertEquals("2nd car in list by load", c4, manager.getById(carList.get(1)));
        Assert.assertEquals("3rd car in list by load", c6, manager.getById(carList.get(2)));
        Assert.assertEquals("4th car in list by load", c1, manager.getById(carList.get(3)));
        Assert.assertEquals("5th car in list by load", c2, manager.getById(carList.get(4)));
        Assert.assertEquals("6th car in list by load", c3, manager.getById(carList.get(5)));

        // now get cars by kernel
        carList = manager.getByKernelList();
        Assert.assertEquals("Number of Cars by kernel", 6, carList.size());
        Assert.assertEquals("1st car in list by kernel", c4, manager.getById(carList.get(0)));
        Assert.assertEquals("2nd car in list by kernel", c3, manager.getById(carList.get(1)));
        Assert.assertEquals("3rd car in list by kernel", c5, manager.getById(carList.get(2)));
        Assert.assertEquals("4th car in list by kernel", c2, manager.getById(carList.get(3)));
        Assert.assertEquals("5th car in list by kernel", c6, manager.getById(carList.get(4)));
        Assert.assertEquals("6th car in list by kernel", c1, manager.getById(carList.get(5)));

        // now get cars by location
        carList = manager.getByLocationList();
        Assert.assertEquals("Number of Cars by location", 6, carList.size());
        Assert.assertEquals("1st car in list by location", c6, manager.getById(carList.get(0)));
        Assert.assertEquals("2nd car in list by location", c5, manager.getById(carList.get(1)));
        Assert.assertEquals("3rd car in list by location", c1, manager.getById(carList.get(2)));
        Assert.assertEquals("4th car in list by location", c2, manager.getById(carList.get(3)));
        Assert.assertEquals("5th car in list by location", c4, manager.getById(carList.get(4)));
        Assert.assertEquals("6th car in list by location", c3, manager.getById(carList.get(5)));

        // now get cars by destination
        carList = manager.getByDestinationList();
        Assert.assertEquals("Number of Cars by destination", 6, carList.size());
        Assert.assertEquals("1st car in list by destination", c2, manager.getById(carList.get(0)));
        Assert.assertEquals("2nd car in list by destination", c1, manager.getById(carList.get(1)));
        Assert.assertEquals("3rd car in list by destination", c5, manager.getById(carList.get(2)));
        Assert.assertEquals("4th car in list by destination", c6, manager.getById(carList.get(3)));
        Assert.assertEquals("5th car in list by destination", c3, manager.getById(carList.get(4)));
        Assert.assertEquals("6th car in list by destination", c4, manager.getById(carList.get(5)));

        // now get cars by train
        carList = manager.getByTrainList();
        Assert.assertEquals("Number of Cars by train", 6, carList.size());
        Assert.assertEquals("1st car in list by train", c6, manager.getById(carList.get(0)));
        Assert.assertEquals("2nd car in list by train", c4, manager.getById(carList.get(1)));
        Assert.assertEquals("3rd car in list by train", c5, manager.getById(carList.get(2)));
        Assert.assertEquals("4th car in list by train", c2, manager.getById(carList.get(3)));
        Assert.assertEquals("5th car in list by train", c3, manager.getById(carList.get(4)));
        Assert.assertEquals("6th car in list by train", c1, manager.getById(carList.get(5)));

        // now get cars by specific train
        carList = manager.getByTrainList(t1);
        Assert.assertEquals("Number of Cars in t1", 1, carList.size());
        Assert.assertEquals("1st car in list by t1", c1, manager.getById(carList.get(0)));
        carList = manager.getByTrainList(t3);
        Assert.assertEquals("Number of Cars in t3", 3, carList.size());
        Assert.assertEquals("1st car in list by t3", c2, manager.getById(carList.get(0)));
        Assert.assertEquals("2nd car in list by t3", c3, manager.getById(carList.get(1)));
        Assert.assertEquals("3rd car in list by t3", c5, manager.getById(carList.get(2)));
        
        // now get cars by specific train
        carList = manager.getByTrainDestinationList(t1);
        Assert.assertEquals("Number of Cars in t1 by dest", 1, carList.size());
        Assert.assertEquals("1st car in list by t1 by dest", c1, manager.getById(carList.get(0)));
        carList = manager.getByTrainDestinationList(t3);
        Assert.assertEquals("Number of Cars in t3 by dest", 3, carList.size());
        Assert.assertEquals("1st car in list by t3 by dest", c2, manager.getById(carList.get(0)));
        Assert.assertEquals("2nd car in list by t3 by dest", c3, manager.getById(carList.get(1)));
        Assert.assertEquals("3rd car in list by t3 by dest", c5, manager.getById(carList.get(2)));
            
        // how many cars available?
        carList = manager.getAvailableTrainList(t1);
        Assert.assertEquals("Number of Cars available for t1", 1, carList.size());
        Assert.assertEquals("1st car in list available for t1", c1, manager.getById(carList.get(0)));

        carList = manager.getAvailableTrainList(t3);
        Assert.assertEquals("Number of Cars available for t3", 2, carList.size());
        Assert.assertEquals("1st car in list available for t3", c2, manager.getById(carList.get(0)));
        Assert.assertEquals("2nd car in list available for t3", c3, manager.getById(carList.get(1)));
        // note that c5 isn't available since it is located at the end of the train's route
        
        // release cars from trains
        c2.setTrain(null);
        c4.setTrain(null);
        c6.setTrain(null);	// c6 is located at the end of the route, therefore not available
        
        // there should be more cars now
        carList = manager.getAvailableTrainList(t1);
        Assert.assertEquals("Number of Cars available t1 after release", 3, carList.size());
        // should be sorted by moves
        Assert.assertEquals("1st car in list available for t1", c1, manager.getById(carList.get(0)));
        Assert.assertEquals("2nd car in list available for t1", c4, manager.getById(carList.get(1)));
        Assert.assertEquals("3rd car in list available for t1", c2, manager.getById(carList.get(2)));

        carList = manager.getAvailableTrainList(t3);
        Assert.assertEquals("Number of Cars available for t3 after release", 3, carList.size());
        Assert.assertEquals("1st car in list available for t3", c4, manager.getById(carList.get(0)));
        Assert.assertEquals("2nd car in list available for t3", c2, manager.getById(carList.get(1)));
        Assert.assertEquals("3rd car in list available for t3", c3, manager.getById(carList.get(2)));

        // now get cars by road number
        carList = manager.getByNumberList();
        Assert.assertEquals("Number of Cars by number", 6, carList.size());
        Assert.assertEquals("1st car in list by number", c6, manager.getById(carList.get(0)));
        Assert.assertEquals("2nd car in list by number", c1, manager.getById(carList.get(1)));
        Assert.assertEquals("3rd car in list by number", c5, manager.getById(carList.get(2)));
        Assert.assertEquals("4th car in list by number", c2, manager.getById(carList.get(3)));
        Assert.assertEquals("5th car in list by number", c3, manager.getById(carList.get(4)));
        Assert.assertEquals("6th car in list by number", c4, manager.getById(carList.get(5)));
        
        // find car by road and number
        Assert.assertEquals("find c1 by road and number", c1, manager.getByRoadAndNumber("CP", "1"));
        Assert.assertEquals("find c2 by road and number", c2, manager.getByRoadAndNumber("ACL", "3"));
        Assert.assertEquals("find c3 by road and number", c3, manager.getByRoadAndNumber("CP", "3"));
        Assert.assertEquals("find c4 by road and number", c4, manager.getByRoadAndNumber("CP", "3-1"));
        Assert.assertEquals("find c5 by road and number", c5, manager.getByRoadAndNumber("PC", "2"));
        Assert.assertEquals("find c6 by road and number", c6, manager.getByRoadAndNumber("AA", "1"));

        // now get cars by RFID
        carList = manager.getByRfidList();
        Assert.assertEquals("Number of Cars by rfid", 6, carList.size());
        Assert.assertEquals("1st car in list by rfid", c2, manager.getById(carList.get(0)));
        Assert.assertEquals("2nd car in list by rfid", c5, manager.getById(carList.get(1)));
        Assert.assertEquals("3rd car in list by rfid", c4, manager.getById(carList.get(2)));
        Assert.assertEquals("4th car in list by rfid", c3, manager.getById(carList.get(3)));
        Assert.assertEquals("5th car in list by rfid", c6, manager.getById(carList.get(4)));
        Assert.assertEquals("6th car in list by rfid", c1, manager.getById(carList.get(5)));

        // find car by RFID
        Assert.assertEquals("find c1 by rfid", c1, manager.getByRfid("SQ1"));
        Assert.assertEquals("find c2 by rfid", c2, manager.getByRfid("1Ab"));
        Assert.assertEquals("find c3 by rfid", c3, manager.getByRfid("Ase"));
        Assert.assertEquals("find c4 by rfid", c4, manager.getByRfid("asd"));
        Assert.assertEquals("find c5 by rfid", c5, manager.getByRfid("93F"));
        Assert.assertEquals("find c6 by rfid", c6, manager.getByRfid("B12"));
        
        // chance car types so sort will work
        c1.setType("F");
        c2.setType("D");
        c3.setType("A");
        c4.setType("B");
        c5.setType("C");
        c6.setType("E");
        
        // now get cars by type
        carList = manager.getByTypeList();
        Assert.assertEquals("Number of Cars by type", 6, carList.size());
        Assert.assertEquals("1st car in list by type", c3, manager.getById(carList.get(0)));
        Assert.assertEquals("2nd car in list by type", c4, manager.getById(carList.get(1)));
        Assert.assertEquals("3rd car in list by type", c5, manager.getById(carList.get(2)));
        Assert.assertEquals("4th car in list by type", c2, manager.getById(carList.get(3)));
        Assert.assertEquals("5th car in list by type", c6, manager.getById(carList.get(4)));
        Assert.assertEquals("6th car in list by type", c1, manager.getById(carList.get(5)));
    
        // check caboose roads
        List<String> cabooseRoads = manager.getCabooseRoadNames();
        Assert.assertEquals("Number of cabooses", 2, cabooseRoads.size());
        Assert.assertEquals("1st road","AA",cabooseRoads.get(0));
        Assert.assertEquals("2nd road","CP",cabooseRoads.get(1));
  
        // check FRED roads
        List<String> fredRoads = manager.getFredRoadNames();
        Assert.assertEquals("Number of FRED", 1, fredRoads.size());
        Assert.assertEquals("1st road","PC",fredRoads.get(0));
        
        manager.dispose();
        carList = manager.getByIdList();
        Assert.assertEquals("After dispose Number of Cars", 0, carList.size());		
	}

	// test location Xml create support
	public void testXMLCreate() throws Exception {

                CarManager manager = CarManager.instance();
                List<String> tempcarList = manager.getByIdList();

                Assert.assertEquals("Starting Number of Cars", 0, tempcarList.size());
                manager.newCar("CP", "Test Number 1");
                manager.newCar("ACL", "Test Number 2");
                manager.newCar("CP", "Test Number 3");

                tempcarList = manager.getByIdList();

                Assert.assertEquals("New Number of Cars", 3, tempcarList.size());
/*                
                Assert.assertEquals("New Engine by Id 1", "Test Number 1", manager.getById("CPTest Number 1").getNumber());
                Assert.assertEquals("New Engine by Id 2", "Test Number 2", manager.getById("ACLTest Number 2").getNumber());
                Assert.assertEquals("New Engine by Id 3", "Test Number 3", manager.getById("CPTest Number 3").getNumber());

                Assert.assertEquals("New Location by Road+Name 1", "Test Number 1", manager.getByRoadAndNumber("CP", "Test Number 1").getNumber());
                Assert.assertEquals("New Location by Road+Name 2", "Test Number 2", manager.getByRoadAndNumber("ACL", "Test Number 2").getNumber());
                Assert.assertEquals("New Location by Road+Name 3", "Test Number 3", manager.getByRoadAndNumber("CP", "Test Number 3").getNumber());

                manager.getByRoadAndNumber("CP", "Test Number 1").setBuilt("1923");
                manager.getByRoadAndNumber("CP", "Test Number 1").setColor("Black");
                manager.getByRoadAndNumber("CP", "Test Number 1").setComment("Nice runner");
//                manager.getByRoadAndNumber("CP", "Test Number 1").setConsist(consist);
//                manager.getByRoadAndNumber("CP", "Test Number 1").setDestination(destination, track);
                manager.getByRoadAndNumber("CP", "Test Number 1").setHp("23");
                manager.getByRoadAndNumber("CP", "Test Number 1").setLength("50");
//                manager.getByRoadAndNumber("CP", "Test Number 1").setLocation(location, track);
//                manager.getByRoadAndNumber("CP", "Test Number 1").setModel("E8");
                manager.getByRoadAndNumber("CP", "Test Number 1").setMoves(5);
                manager.getByRoadAndNumber("CP", "Test Number 1").setOwner("TestOwner");
//                manager.getByRoadAndNumber("CP", "Test Number 1").setRouteDestination(routeDestination);
//                manager.getByRoadAndNumber("CP", "Test Number 1").setRouteLocation(routeLocation);
//                manager.getByRoadAndNumber("CP", "Test Number 1").setSavedRouteId(id);
//                manager.getByRoadAndNumber("CP", "Test Number 1").setTrain(train);
                manager.getByRoadAndNumber("CP", "Test Number 1").setWeight("87");
                manager.getByRoadAndNumber("CP", "Test Number 1").setWeightTons("97");
                
                
                manager.getByRoadAndNumber("CP", "Test Number 1").setType("Gas Turbine");
                
                manager.getByRoadAndNumber("CP", "Test Number 1").setModel("E8");
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
                

                CarManagerXml.instance().writeOperationsFile();

                // Add some more engines and write file again
                // so we can test the backup facility
                manager.newCar("CP", "Test Number 4");
                manager.newCar("CP", "Test Number 5");
                manager.newCar("CP", "Test Number 6");
                manager.getByRoadAndNumber("ACL", "Test Number 2").setComment("Test Engine 2 Changed Comment");
                
                CarManagerXml.instance().writeOperationsFile();
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
        
		// Repoint OperationsSetupXml to JUnitTest subdirectory
		OperationsSetupXml.setOperationsDirectoryName("operations"+File.separator+"JUnitTest");
		// Change file names to ...Test.xml
		OperationsSetupXml.instance().setOperationsFileName("OperationsJUnitTest.xml"); 
		RouteManagerXml.instance().setOperationsFileName("OperationsJUnitTestRouteRoster.xml");
		EngineManagerXml.instance().setOperationsFileName("OperationsJUnitTestEngineRoster.xml");
		CarManagerXml.instance().setOperationsFileName("OperationsJUnitTestCarRoster.xml");
		LocationManagerXml.instance().setOperationsFileName("OperationsJUnitTestLocationRoster.xml");
		TrainManagerXml.instance().setOperationsFileName("OperationsJUnitTestTrainRoster.xml");

        // Need to clear out CarManager global variables
        CarManager manager = CarManager.instance();
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
