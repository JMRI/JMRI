// OperationsCarsTest.java

package jmri.jmrit.operations.rollingstock.cars;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import javax.swing.JComboBox;

import org.jdom.JDOMException;

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
 * @version $Revision$
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
	
	public void testCarLoads(){
		CarLoads cl = CarLoads.instance();
		List<String> names = cl.getNames("BoXcaR");
		
		Assert.assertEquals("Two default names", 2, names.size());
		Assert.assertTrue("Default load", cl.containsName("BoXcaR", "L"));
		Assert.assertTrue("Default empty", cl.containsName("BoXcaR", "E"));
		
		names = cl.getNames("bOxCaR");
		
		Assert.assertEquals("Two default names", 2, names.size());
		Assert.assertTrue("Default load", cl.containsName("bOxCaR", "L"));
		Assert.assertTrue("Default empty", cl.containsName("bOxCaR", "E"));
		
		cl.addName("BoXcaR", "New Boxcar Load");
		cl.addName("bOxCaR", "A boxcar load");
		cl.addName("bOxCaR", "B boxcar load");
		names = cl.getNames("BoXcaR");
		
		Assert.assertEquals("number of names", 3, names.size());
		Assert.assertTrue("Default load", cl.containsName("BoXcaR", "L"));
		Assert.assertTrue("Default empty", cl.containsName("BoXcaR", "E"));
		Assert.assertTrue("new load", cl.containsName("BoXcaR", "New Boxcar Load"));
		
		names = cl.getNames("bOxCaR");
		
		Assert.assertEquals("number of names", 4, names.size());
		Assert.assertTrue("Default load", cl.containsName("bOxCaR", "L"));
		Assert.assertTrue("Default empty", cl.containsName("bOxCaR", "E"));
		Assert.assertTrue("new load", cl.containsName("bOxCaR", "A boxcar load"));
		Assert.assertTrue("new load", cl.containsName("bOxCaR", "B boxcar load"));
		
		cl.replaceName("bOxCaR", "A boxcar load", "C boxcar load");
		
		names = cl.getNames("bOxCaR");
		
		Assert.assertEquals("number of names", 4, names.size());
		Assert.assertTrue("Default load", cl.containsName("bOxCaR", "L"));
		Assert.assertTrue("Default empty", cl.containsName("bOxCaR", "E"));
		Assert.assertFalse("new load", cl.containsName("bOxCaR", "A boxcar load"));
		Assert.assertTrue("new load", cl.containsName("bOxCaR", "B boxcar load"));
		Assert.assertTrue("new load", cl.containsName("bOxCaR", "C boxcar load"));
		
		cl.deleteName("bOxCaR", "B boxcar load");
		
		names = cl.getNames("bOxCaR");
		
		Assert.assertEquals("number of names", 3, names.size());
		Assert.assertTrue("Default load", cl.containsName("bOxCaR", "L"));
		Assert.assertTrue("Default empty", cl.containsName("bOxCaR", "E"));
		Assert.assertFalse("new load", cl.containsName("bOxCaR", "A boxcar load"));
		Assert.assertFalse("new load", cl.containsName("bOxCaR", "B boxcar load"));
		Assert.assertTrue("new load", cl.containsName("bOxCaR", "C boxcar load"));
		
		Assert.assertEquals("default empty", "E", cl.getDefaultEmptyName());
		Assert.assertEquals("default load", "L", cl.getDefaultLoadName());
		
		cl.setDefaultEmptyName("E<mpty>");
		cl.setDefaultLoadName("L<oad>");
		
		Assert.assertEquals("default empty", "E<mpty>", cl.getDefaultEmptyName());
		Assert.assertEquals("default load", "L<oad>", cl.getDefaultLoadName());
		
		names = cl.getNames("BOXCAR");
		
		Assert.assertEquals("number of names", 2, names.size());
		Assert.assertFalse("Default load", cl.containsName("BOXCAR", "L"));
		Assert.assertFalse("Default empty", cl.containsName("BOXCAR", "E"));
		Assert.assertTrue("Default load", cl.containsName("BOXCAR", "L<oad>"));
		Assert.assertTrue("Default empty", cl.containsName("BOXCAR", "E<mpty>"));
		
		// bOxCaR was created using old defaults
		Assert.assertTrue("Default load", cl.containsName("bOxCaR", "L"));
		Assert.assertTrue("Default empty", cl.containsName("bOxCaR", "E"));
		Assert.assertTrue("new load", cl.containsName("bOxCaR", "C boxcar load"));
		
		cl.setDefaultEmptyName("E");
		cl.setDefaultLoadName("L");
		
		Assert.assertEquals("default empty", "E", cl.getDefaultEmptyName());
		Assert.assertEquals("default load", "L", cl.getDefaultLoadName());
	}

	public void testKernel() {
		Kernel k1 = new Kernel("TESTKERNEL");
		Assert.assertEquals("Kernel Name", "TESTKERNEL", k1.getName());

		Car c1 = new Car("TESTCARROAD", "TESTCARNUMBER1");
		c1.setLength("40");
		c1.setWeight("1000");
		c1.setWeightTons("10");
		c1.setLoad("L");
		Car c2 = new Car("TESTCARROAD", "TESTCARNUMBER2");
		c2.setLength("60");
		c2.setWeight("2000");
		c2.setWeightTons("20");
		c2.setLoad("L");
		Car c3 = new Car("TESTCARROAD", "TESTCARNUMBER3");
		c3.setLength("50");
		c3.setWeight("1500");
		c3.setWeightTons("15");
		c3.setLoad("E");

		Assert.assertEquals("Kernel Initial Length", 0, k1.getTotalLength());
		Assert.assertEquals("Kernel Initial Weight Tons", 0, k1.getAdjustedWeightTons());

		k1.add(c1);
		Assert.assertEquals("Kernel Car 1 Length", 40+Car.COUPLER, k1.getTotalLength());
		Assert.assertEquals("Kernel Car 1 Weight Tons", 10, k1.getAdjustedWeightTons());

		k1.add(c2);
		Assert.assertEquals("Kernel Car 2 Length", 40+Car.COUPLER+60+Car.COUPLER, k1.getTotalLength());
		Assert.assertEquals("Kernel Car 2 Weight Tons", 30, k1.getAdjustedWeightTons());

		k1.add(c3);
		Assert.assertEquals("Kernel Car 3 Length", 40+Car.COUPLER+60+Car.COUPLER+50+Car.COUPLER, k1.getTotalLength());
		// car 3 is empty, so only 5 tons, 15/3
		Assert.assertEquals("Kernel Car 3 Weight Tons", 35, k1.getAdjustedWeightTons());

		k1.setLead(c2);
		Assert.assertTrue("Kernel Lead Car 1", k1.isLead(c2));
		Assert.assertFalse("Kernel Lead Car 2", k1.isLead(c1));
		Assert.assertFalse("Kernel Lead Car 3", k1.isLead(c3));

		k1.delete(c2);
		Assert.assertEquals("Kernel Car Delete 2 Length", 40+Car.COUPLER+50+Car.COUPLER, k1.getTotalLength());
		Assert.assertEquals("Kernel Car Delete 2 Weight Tons", 15, k1.getAdjustedWeightTons());

		k1.delete(c1);
		Assert.assertEquals("Kernel Car Delete 1 Length", 50+Car.COUPLER, k1.getTotalLength());
		Assert.assertEquals("Kernel Car Delete 1 Weight Tons", 5, k1.getAdjustedWeightTons());

		k1.delete(c3);
		Assert.assertEquals("Kernel Car Delete 3 Length", 0, k1.getTotalLength());
		Assert.assertEquals("Kernel Car Delete 3 Weight Tons", 0, k1.getAdjustedWeightTons());

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
		Assert.assertEquals("Kernel old length before", 40+4+60+4+50+4, kold.getTotalLength());
		Assert.assertEquals("Kernel new length before", 0, knew.getTotalLength());
		Assert.assertTrue("Kernel old Lead is Car 1 before", kold.isLead(c1));
		Assert.assertFalse("Kernel old Lead is not Car 2 before", kold.isLead(c2));
		Assert.assertFalse("Kernel old Lead is not Car 3 before", kold.isLead(c3));
		Assert.assertFalse("Kernel new Lead is not Car 1 before", knew.isLead(c1));
		Assert.assertFalse("Kernel new Lead is not Car 2 before", knew.isLead(c2));
		Assert.assertFalse("Kernel new Lead is not Car 3 before", knew.isLead(c3));

		//  Move car 1 to the new kernel where it will be the lead car.
		//  Car 2 should now be the lead car of the old kernel.
		c1.setKernel(knew);
		Assert.assertEquals("Kernel Name for car 1 after", "TESTKERNELNEW", c1.getKernelName());
		Assert.assertEquals("Kernel Name for car 2 after", "TESTKERNELOLD", c2.getKernelName());
		Assert.assertEquals("Kernel Name for car 3 after", "TESTKERNELOLD", c3.getKernelName());
		Assert.assertEquals("Kernel old length after", 60+4+50+4, kold.getTotalLength());
		Assert.assertEquals("Kernel new length after", 40+4, knew.getTotalLength());
		Assert.assertFalse("Kernel old Lead is not Car 1 after", kold.isLead(c1));
		Assert.assertTrue("Kernel old Lead is Car 2 after", kold.isLead(c2));
		Assert.assertFalse("Kernel old Lead is not Car 3 after", kold.isLead(c3));
		Assert.assertTrue("Kernel new Lead is Car 1 after", knew.isLead(c1));
		Assert.assertFalse("Kernel new Lead is not Car 2 after", knew.isLead(c2));
		Assert.assertFalse("Kernel new Lead is not Car 3 after", knew.isLead(c3));

		//  Move car 3 to the new kernel.
		c3.setKernel(knew);
		Assert.assertEquals("Kernel Name for car 1 after3", "TESTKERNELNEW", c1.getKernelName());
		Assert.assertEquals("Kernel Name for car 2 after3", "TESTKERNELOLD", c2.getKernelName());
		Assert.assertEquals("Kernel Name for car 3 after3", "TESTKERNELNEW", c3.getKernelName());
		Assert.assertEquals("Kernel old length after3", 60+4, kold.getTotalLength());
		Assert.assertEquals("Kernel new length after3", 40+4+50+4, knew.getTotalLength());
		Assert.assertFalse("Kernel old Lead is not Car 1 after3", kold.isLead(c1));
		Assert.assertTrue("Kernel old Lead is Car 2 after3", kold.isLead(c2));
		Assert.assertFalse("Kernel old Lead is not Car 3 after3", kold.isLead(c3));
		Assert.assertTrue("Kernel new Lead is Car 1 after3", knew.isLead(c1));
		Assert.assertFalse("Kernel new Lead is not Car 2 after3", knew.isLead(c2));
		Assert.assertFalse("Kernel new Lead is not Car 3 after3", knew.isLead(c3));
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
        Track l1t1 = l1.addTrack("A",Track.SPUR);
        Track l1t2 = l1.addTrack("B",Track.SPUR);
        Location l2 = new Location("id2", "C");
        Track l2t1 = l2.addTrack("B",Track.SPUR);
        Track l2t2 = l2.addTrack("A",Track.SPUR);
        Location l3 = new Location("id3", "A");
        Track l3t1 = l3.addTrack("B",Track.SPUR);
        Track l3t2 = l3.addTrack("A",Track.SPUR);

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
        Assert.assertEquals("place c1", Track.OKAY, c1.setLocation(l1, l1t1));
        Assert.assertEquals("place c2", Track.OKAY, c2.setLocation(l1, l1t2));
        Assert.assertEquals("place c3", Track.OKAY, c3.setLocation(l2, l2t1));
        Assert.assertEquals("place c4", Track.OKAY, c4.setLocation(l2, l2t2));
        Assert.assertEquals("place c5", Track.OKAY, c5.setLocation(l3, l3t1));
        Assert.assertEquals("place c6", Track.OKAY, c6.setLocation(l3, l3t2));

        // set car destinations
        Assert.assertEquals("destination c1", Track.OKAY, c1.setDestination(l3, l3t1));
        Assert.assertEquals("destination c2", Track.OKAY, c2.setDestination(l3, l3t2));
        Assert.assertEquals("destination c3", Track.OKAY, c3.setDestination(l2, l2t2));
        Assert.assertEquals("destination c4", Track.OKAY, c4.setDestination(l2, l2t1));
        Assert.assertEquals("destination c5", Track.OKAY, c5.setDestination(l1, l1t1));
        Assert.assertEquals("destination c6", Track.OKAY, c6.setDestination(l1, l1t2));

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
        Assert.assertEquals("1st car in list by t3", c5, manager.getById(carList.get(0)));
        Assert.assertEquals("2nd car in list by t3", c2, manager.getById(carList.get(1)));
        Assert.assertEquals("3rd car in list by t3", c3, manager.getById(carList.get(2)));
        
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
        
        // change car types so sort will work
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

	// test Xml create support
	public void testXMLCreate(){
		
		// confirm that file name has been modified for testing
		Assert.assertEquals("OperationsJUnitTestCarRoster.xml", CarManagerXml.instance().getOperationsFileName());

		CarManager manager = CarManager.instance();
		List<String> tempcarList = manager.getByIdList();

		Assert.assertEquals("Starting Number of Cars", 0, tempcarList.size());
		Car c1 = manager.newCar("CP", "Test Number 1");
		Car c2 = manager.newCar("ACL", "Test Number 2");
		Car c3 = manager.newCar("CP", "Test Number 3");

		// modify car attributes
		c1.setBuilt("5619");
		c1.setCaboose(false);
		c1.setColor("black");
		c1.setComment("no comment");
		c1.setLength("04");
		c1.setLoad("FULL");
		c1.setMoves(1);
		c1.setNumber("X Test Number c1");  
		c1.setOutOfService(false);
		c1.setRfid("norfidc1");
		c1.setRoad("OLDRoad");
		c1.setType("noCaboose");
		c1.setWait(6);
		c1.setWeight("54");
		c1.setWeightTons("001");

		c2.setBuilt("1234");
		c2.setFred(true);
		c2.setColor("red");
		c2.setComment("c2 comment");
		c2.setLength("77");
		c2.setLoad("c2 Load");
		c2.setMoves(10000);
		c2.setNumber("X Test Number c2");  
		c2.setOutOfService(true);
		c2.setRfid("rfidc2");
		c2.setRoad("c2 Road");
		c2.setType("c2 Boxcar");
		c2.setWait(61);
		c2.setWeight("33");
		c2.setWeightTons("798");

		c3.setBuilt("234");
		c3.setCaboose(true);
		c3.setColor("green");
		c3.setComment("c3 comment");
		c3.setLength("453");
		c3.setLoad("c3 Load");
		c3.setMoves(243);
		c3.setNumber("X Test Number c3");  
		c3.setOutOfService(false);
		c3.setRfid("rfidc3");
		c3.setRoad("c3 Road");
		c3.setType("c3 Boxcar");
		c3.setWait(0);
		c3.setWeight("345");
		c3.setWeightTons("1798");

		tempcarList = manager.getByIdList();
		Assert.assertEquals("New Number of Cars", 3, tempcarList.size());

		CarManagerXml.instance().writeOperationsFile();

		// Add some more cars and write file again
		// so we can test the backup facility
		Car c4 = manager.newCar("PC", "Test Number 4");
		Car c5 = manager.newCar("BM", "Test Number 5");
		Car c6 = manager.newCar("SP", "Test Number 6");

		Assert.assertNotNull("car c4 exists", c4);
		Assert.assertNotNull("car c5 exists", c5);
		Assert.assertNotNull("car c6 exists", c6);

		// modify car attributes
		c1.setBuilt("1956");
		c1.setCaboose(true);
		c1.setColor("white");
		c1.setComment("c1 comment");
		c1.setLength("40");
		c1.setLoad("Empty");
		c1.setMoves(3);
		c1.setNumber("New Test Number c1");  
		c1.setOutOfService(true);
		c1.setRfid("rfidc1");
		c1.setRoad("newRoad");
		c1.setType("bigCaboose");
		c1.setWait(5);
		c1.setWeight("45");
		c1.setWeightTons("100");

		c5.setBuilt("2010");
		c5.setCaboose(false);
		c5.setColor("blue");
		c5.setComment("c5 comment");
		c5.setLength("44");
		c5.setLoad("Full");
		c5.setMoves(5);
		c5.setNumber("New Test Number c5");  
		c5.setOutOfService(true);
		c5.setRfid("rfidc5");
		c5.setRoad("c5Road");
		c5.setType("smallCaboose");
		c5.setWait(55);
		c5.setWeight("66");
		c5.setWeightTons("77");      
		
		tempcarList = manager.getByIdList();
		Assert.assertEquals("New Number of Cars", 6, tempcarList.size());

		CarManagerXml.instance().writeOperationsFile();
	}

	/**
	 * Test reading xml car file
	 * @throws JDOMException
	 * @throws IOException
	 */
	public void testXMLRead() throws JDOMException, IOException{
		CarManager manager = CarManager.instance();
		List<String> tempcarList = manager.getByIdList();
		Assert.assertEquals("Starting Number of Cars", 0, tempcarList.size());

		CarManagerXml.instance().readFile(CarManagerXml.instance().getDefaultOperationsFilename());	

		tempcarList = manager.getByIdList();
		Assert.assertEquals("Number of Cars", 6, tempcarList.size());

		Car c1 = manager.getByRoadAndNumber("CP", "Test Number 1"); // must find car by original id
		Car c2 = manager.getByRoadAndNumber("ACL", "Test Number 2"); // must find car by original id
		Car c3 = manager.getByRoadAndNumber("CP", "Test Number 3"); // must find car by original id
		Car c4 = manager.getByRoadAndNumber("PC", "Test Number 4"); // must find car by original id 
		Car c5 = manager.getByRoadAndNumber("BM", "Test Number 5"); // must find car by original id
		Car c6 = manager.getByRoadAndNumber("SP", "Test Number 6"); // must find car by original id

		Assert.assertNotNull("car c1 exists", c1);
		Assert.assertNotNull("car c2 exists", c2);
		Assert.assertNotNull("car c3 exists", c3);
		Assert.assertNotNull("car c4 exists", c4);
		Assert.assertNotNull("car c5 exists", c5);
		Assert.assertNotNull("car c6 exists", c6);

		Assert.assertEquals("car c1 built date", "1956", c1.getBuilt());
		Assert.assertEquals("car c1 caboose", true, c1.isCaboose());
		Assert.assertEquals("car c1 color", "white", c1.getColor());
		Assert.assertEquals("car c1 comment", "c1 comment", c1.getComment());
		Assert.assertEquals("car c1 length", "40", c1.getLength());
		Assert.assertEquals("car c1 load", "Empty", c1.getLoad());
		Assert.assertEquals("car c1 moves", 3, c1.getMoves());
		Assert.assertEquals("car c1 number", "New Test Number c1", c1.getNumber());
		Assert.assertEquals("car c1 out of service", true, c1.isOutOfService());
		Assert.assertEquals("car c1 rfid", "rfidc1", c1.getRfid());
		Assert.assertEquals("car c1 road", "newRoad", c1.getRoad());
		Assert.assertEquals("car c1 type", "bigCaboose", c1.getType());
		Assert.assertEquals("car c1 wait", 5, c1.getWait());
		Assert.assertEquals("car c1 weight", "45", c1.getWeight());
		Assert.assertEquals("car c1 weight tons", "100", c1.getWeightTons());

		Assert.assertEquals("car c2 built date", "1234", c2.getBuilt());
		Assert.assertEquals("car c2 caboose", false, c2.isCaboose());
		Assert.assertEquals("car c2 fred", true, c2.hasFred());
		Assert.assertEquals("car c2 color", "red", c2.getColor());
		Assert.assertEquals("car c2 comment", "c2 comment", c2.getComment());
		Assert.assertEquals("car c2 length", "77", c2.getLength());
		Assert.assertEquals("car c2 load", "c2 Load", c2.getLoad());
		Assert.assertEquals("car c2 moves", 10000, c2.getMoves());
		Assert.assertEquals("car c2 number", "X Test Number c2", c2.getNumber());
		Assert.assertEquals("car c2 out of service", true, c2.isOutOfService());
		Assert.assertEquals("car c2 rfid", "rfidc2", c2.getRfid());
		Assert.assertEquals("car c2 road", "c2 Road", c2.getRoad());
		Assert.assertEquals("car c2 type", "c2 Boxcar", c2.getType());
		Assert.assertEquals("car c2 wait", 61, c2.getWait());
		Assert.assertEquals("car c2 weight", "33", c2.getWeight());
		Assert.assertEquals("car c2 weight tons", "798", c2.getWeightTons());

		Assert.assertEquals("car c3 built date", "234", c3.getBuilt());
		Assert.assertEquals("car c3 caboose", true, c3.isCaboose());
		Assert.assertEquals("car c3 fred", false, c3.hasFred());
		Assert.assertEquals("car c3 color", "green", c3.getColor());
		Assert.assertEquals("car c3 comment", "c3 comment", c3.getComment());
		Assert.assertEquals("car c3 length", "453", c3.getLength());
		Assert.assertEquals("car c3 load", "c3 Load", c3.getLoad());
		Assert.assertEquals("car c3 moves", 243, c3.getMoves());
		Assert.assertEquals("car c3 number", "X Test Number c3", c3.getNumber());
		Assert.assertEquals("car c3 out of service", false, c3.isOutOfService());
		Assert.assertEquals("car c3 rfid", "rfidc3", c3.getRfid());
		Assert.assertEquals("car c3 road", "c3 Road", c3.getRoad());
		Assert.assertEquals("car c3 type", "c3 Boxcar", c3.getType());
		Assert.assertEquals("car c3 wait", 0, c3.getWait());
		Assert.assertEquals("car c3 weight", "345", c3.getWeight());
		Assert.assertEquals("car c3 weight tons", "1798", c3.getWeightTons());

		// c4 and c6 use defaults for most of their attributes.
		Assert.assertEquals("car c4 built date", "", c4.getBuilt());
		Assert.assertEquals("car c4 caboose", false, c4.isCaboose());
		Assert.assertEquals("car c4 fred", false, c4.hasFred());
		Assert.assertEquals("car c4 color", "", c4.getColor());
		Assert.assertEquals("car c4 comment", "", c4.getComment());
		Assert.assertEquals("car c4 length", "", c4.getLength());
		Assert.assertEquals("car c4 load", "E", c4.getLoad());
		Assert.assertEquals("car c4 moves", 0, c4.getMoves());
		Assert.assertEquals("car c4 number", "Test Number 4", c4.getNumber());
		Assert.assertEquals("car c4 out of service", false, c4.isOutOfService());
		Assert.assertEquals("car c4 rfid", "", c4.getRfid());
		Assert.assertEquals("car c4 road", "PC", c4.getRoad());
		Assert.assertEquals("car c4 type", "", c4.getType());
		Assert.assertEquals("car c4 wait", 0, c4.getWait());
		Assert.assertEquals("car c4 weight", "0", c4.getWeight());
		Assert.assertEquals("car c4 weight tons", "0", c4.getWeightTons());

		Assert.assertEquals("car c5 built date", "2010", c5.getBuilt());
		Assert.assertEquals("car c5 caboose", false, c5.isCaboose());
		Assert.assertEquals("car c5 color", "blue", c5.getColor());
		Assert.assertEquals("car c5 comment", "c5 comment", c5.getComment());
		Assert.assertEquals("car c5 length", "44", c5.getLength());
		Assert.assertEquals("car c5 load", "Full", c5.getLoad());
		Assert.assertEquals("car c5 moves", 5, c5.getMoves());
		Assert.assertEquals("car c5 number", "New Test Number c5", c5.getNumber());
		Assert.assertEquals("car c5 out of service", true, c5.isOutOfService());
		Assert.assertEquals("car c5 rfid", "rfidc5", c5.getRfid());
		Assert.assertEquals("car c5 road", "c5Road", c5.getRoad());
		Assert.assertEquals("car c5 type", "smallCaboose", c5.getType());
		Assert.assertEquals("car c5 wait", 55, c5.getWait());
		Assert.assertEquals("car c5 weight", "66", c5.getWeight());
		Assert.assertEquals("car c5 weight tons", "77", c5.getWeightTons());

		Assert.assertEquals("car c6 built date", "", c6.getBuilt());
		Assert.assertEquals("car c6 caboose", false, c6.isCaboose());
		Assert.assertEquals("car c6 fred", false, c6.hasFred());
		Assert.assertEquals("car c6 color", "", c6.getColor());
		Assert.assertEquals("car c6 comment", "", c6.getComment());
		Assert.assertEquals("car c6 length", "", c6.getLength());
		Assert.assertEquals("car c6 load", "E", c6.getLoad());
		Assert.assertEquals("car c6 moves", 0, c6.getMoves());
		Assert.assertEquals("car c6 number", "Test Number 6", c6.getNumber());
		Assert.assertEquals("car c6 out of service", false, c6.isOutOfService());
		Assert.assertEquals("car c6 rfid", "", c6.getRfid());
		Assert.assertEquals("car c6 road", "SP", c6.getRoad());
		Assert.assertEquals("car c6 type", "", c6.getType());
		Assert.assertEquals("car c6 wait", 0, c6.getWait());
		Assert.assertEquals("car c6 weight", "0", c6.getWeight());
		Assert.assertEquals("car c6 weight tons", "0", c6.getWeightTons());

	}

	/**
	 * Test backup file.
	 * @throws JDOMException
	 * @throws IOException
	 */
	public void testXMLReadBackup() throws JDOMException, IOException{
		CarManager manager = CarManager.instance();
		List<String> tempcarList = manager.getByIdList();
		Assert.assertEquals("Starting Number of Cars", 0, tempcarList.size());

		// change default file name to backup
		CarManagerXml.instance().setOperationsFileName("OperationsJUnitTestCarRoster.xml.bak");

		CarManagerXml.instance().readFile(CarManagerXml.instance().getDefaultOperationsFilename());	

		tempcarList = manager.getByIdList();
		Assert.assertEquals("Number of Cars", 3, tempcarList.size());

		Car c1 = manager.getByRoadAndNumber("CP", "Test Number 1"); // must find car by original id
		Car c2 = manager.getByRoadAndNumber("ACL", "Test Number 2"); // must find car by original id
		Car c3 = manager.getByRoadAndNumber("CP", "Test Number 3"); // must find car by original id
		Car c4 = manager.getByRoadAndNumber("PC", "Test Number 4"); // must find car by original id 
		Car c5 = manager.getByRoadAndNumber("BM", "Test Number 5"); // must find car by original id
		Car c6 = manager.getByRoadAndNumber("SP", "Test Number 6"); // must find car by original id

		Assert.assertNotNull("car c1 exists", c1);
		Assert.assertNotNull("car c2 exists", c2);
		Assert.assertNotNull("car c3 exists", c3);
		Assert.assertNull("car c4 does not exist", c4);
		Assert.assertNull("car c5 does not exist", c5);
		Assert.assertNull("car c6 does not exist", c6);

		Assert.assertEquals("car c1 built date", "5619", c1.getBuilt());
		Assert.assertEquals("car c1 caboose", false, c1.isCaboose());
		Assert.assertEquals("car c1 color", "black", c1.getColor());
		Assert.assertEquals("car c1 comment", "no comment", c1.getComment());
		Assert.assertEquals("car c1 length", "04", c1.getLength());
		Assert.assertEquals("car c1 load", "FULL", c1.getLoad());
		Assert.assertEquals("car c1 moves", 1, c1.getMoves());
		Assert.assertEquals("car c1 number", "X Test Number c1", c1.getNumber());
		Assert.assertEquals("car c1 out of service", false, c1.isOutOfService());
		Assert.assertEquals("car c1 rfid", "norfidc1", c1.getRfid());
		Assert.assertEquals("car c1 road", "OLDRoad", c1.getRoad());
		Assert.assertEquals("car c1 type", "noCaboose", c1.getType());
		Assert.assertEquals("car c1 wait", 6, c1.getWait());
		Assert.assertEquals("car c1 weight", "54", c1.getWeight());
		Assert.assertEquals("car c1 weight tons", "001", c1.getWeightTons());

		Assert.assertEquals("car c2 built date", "1234", c2.getBuilt());
		Assert.assertEquals("car c2 caboose", false, c2.isCaboose());
		Assert.assertEquals("car c2 fred", true, c2.hasFred());
		Assert.assertEquals("car c2 color", "red", c2.getColor());
		Assert.assertEquals("car c2 comment", "c2 comment", c2.getComment());
		Assert.assertEquals("car c2 length", "77", c2.getLength());
		Assert.assertEquals("car c2 load", "c2 Load", c2.getLoad());
		Assert.assertEquals("car c2 moves", 10000, c2.getMoves());
		Assert.assertEquals("car c2 number", "X Test Number c2", c2.getNumber());
		Assert.assertEquals("car c2 out of service", true, c2.isOutOfService());
		Assert.assertEquals("car c2 rfid", "rfidc2", c2.getRfid());
		Assert.assertEquals("car c2 road", "c2 Road", c2.getRoad());
		Assert.assertEquals("car c2 type", "c2 Boxcar", c2.getType());
		Assert.assertEquals("car c2 wait", 61, c2.getWait());
		Assert.assertEquals("car c2 weight", "33", c2.getWeight());
		Assert.assertEquals("car c2 weight tons", "798", c2.getWeightTons());

		Assert.assertEquals("car c3 built date", "234", c3.getBuilt());
		Assert.assertEquals("car c3 caboose", true, c3.isCaboose());
		Assert.assertEquals("car c3 fred", false, c3.hasFred());
		Assert.assertEquals("car c3 color", "green", c3.getColor());
		Assert.assertEquals("car c3 comment", "c3 comment", c3.getComment());
		Assert.assertEquals("car c3 length", "453", c3.getLength());
		Assert.assertEquals("car c3 load", "c3 Load", c3.getLoad());
		Assert.assertEquals("car c3 moves", 243, c3.getMoves());
		Assert.assertEquals("car c3 number", "X Test Number c3", c3.getNumber());
		Assert.assertEquals("car c3 out of service", false, c3.isOutOfService());
		Assert.assertEquals("car c3 rfid", "rfidc3", c3.getRfid());
		Assert.assertEquals("car c3 road", "c3 Road", c3.getRoad());
		Assert.assertEquals("car c3 type", "c3 Boxcar", c3.getType());
		Assert.assertEquals("car c3 wait", 0, c3.getWait());
		Assert.assertEquals("car c3 weight", "345", c3.getWeight());
		Assert.assertEquals("car c3 weight tons", "1798", c3.getWeightTons());
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
        
		// set the locale to US English
		Locale.setDefault(Locale.ENGLISH);
        
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
