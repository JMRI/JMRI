// OperationsCarsTest.java

package jmri.jmrit.operations.cars;

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
 * Tests for the OperationsCars class
 * @author	Bob Coleman
 * @version $Revision: 1.2 $
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
		cr1.deleteName("Road New3");
		Assert.assertFalse("Car Roads Delete New3", cr1.containsName("Road New3"));
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
		ct1.deleteName("Type New3");
		Assert.assertFalse("Car Types Delete New3", ct1.containsName("Type New3"));
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

	// TODO: Add tests for location

	// TODO: Add tests for secondary location

	// TODO: Add tests for destination

	// TODO: Add tests for secondary destination

	// TODO: Add tests for train

	// TODO: Add tests for route location

	// TODO: Add tests for route secondary location

	// TODO: Add tests for route destination

	// TODO: Add tests for route secondary destination

	// TODO: Add test for import

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
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }
}
