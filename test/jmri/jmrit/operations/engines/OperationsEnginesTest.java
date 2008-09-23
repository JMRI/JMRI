// OperationsEnginesTest.java

package jmri.jmrit.operations.engines;

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
 * Tests for the OperationsEngines class
 * @author	Bob Coleman
 * @version $Revision: 1.3 $
 */
public class OperationsEnginesTest extends TestCase {

	// test creation
	public void testCreate() {
		Engine e1 = new Engine("TESTROAD", "TESTNUMBER1");
		e1.setModel("TESTMODEL");
		e1.setLength("TESTLENGTH");

		Assert.assertEquals("Engine Road", "TESTROAD", e1.getRoad());
		Assert.assertEquals("Engine Number", "TESTNUMBER1", e1.getNumber());
		Assert.assertEquals("Engine Model", "TESTMODEL", e1.getModel());
		Assert.assertEquals("Engine Length", "TESTLENGTH", e1.getLength());
	}

	public void testEngineLengths() {
//  Comment out tests that rely upon manager having run until that gets fixed
		EngineLengths el1 = new EngineLengths();
//		EngineLengths el1;
//		el1 = EngineLengths.instance();

//		Assert.assertTrue("Engine Length Predefined 70", el1.containsName("70"));
//		Assert.assertTrue("Engine Length Predefined 80", el1.containsName("80"));
//		Assert.assertTrue("Engine Length Predefined 90", el1.containsName("90"));

		el1.addName("1");
		Assert.assertTrue("Engine Length Add 1", el1.containsName("1"));
		Assert.assertFalse("Engine Length Never Added 13", el1.containsName("13"));
		el1.addName("2");
		Assert.assertTrue("Engine Length Still Has 1", el1.containsName("1"));
		Assert.assertTrue("Engine Length Add s2", el1.containsName("2"));
		el1.deleteName("2");
		Assert.assertFalse("Engine Length Delete 2", el1.containsName("2"));
		el1.deleteName("1");
		Assert.assertFalse("Engine Length Delete 1", el1.containsName("1"));
	}

	public void testEngineModels() {
//  Comment out tests that rely upon manager having run until that gets fixed
		EngineModels em1 = new EngineModels();
//		EngineModels em1;
//		em1 = EngineModels.instance();

//		Assert.assertTrue("Engine Models Predefined GP35", em1.containsName("GP35"));
//		Assert.assertTrue("Engine Models Predefined SW1200", em1.containsName("SW1200"));
//		Assert.assertTrue("Engine Models Predefined TRAINMASTER", em1.containsName("TRAINMASTER"));
//		Assert.assertTrue("Engine Models Predefined E8", em1.containsName("E8"));

		em1.addName("Model New1");
		Assert.assertTrue("Engine Model Add New1", em1.containsName("Model New1"));
		Assert.assertFalse("Engine Model Never Added New2", em1.containsName("Model New2"));
		em1.addName("Model New3");
		Assert.assertTrue("Engine Model Still Has New1", em1.containsName("Model New1"));
		Assert.assertTrue("Engine Model Add New3", em1.containsName("Model New3"));
		em1.deleteName("Model New3");
		Assert.assertFalse("Engine Model Delete New3", em1.containsName("Model New3"));
		em1.deleteName("Model New1");
		Assert.assertFalse("Engine Model Delete New1", em1.containsName("Model New1"));
	}

	public void testConsist() {
		Consist c1 = new Consist("TESTCONSIST");
		Assert.assertEquals("Consist Name", "TESTCONSIST", c1.getName());

		Engine e1 = new Engine("TESTROAD", "TESTNUMBER1");
//		e1.setLength("56");
		e1.setModel("GP35");
		e1.setWeight("5000");
		Engine e2 = new Engine("TESTROAD", "TESTNUMBER2");
//		e2.setLength("59");
		e2.setModel("GP40");
		e2.setWeight("6000");
		Engine e3 = new Engine("TESTROAD", "TESTNUMBER3");
//		e3.setLength("45");
		e3.setModel("SW1500");
		e3.setWeight("7000");

		Assert.assertEquals("Consist Initial Length", 0, c1.getLength());
//		Assert.assertEquals("Consist Initial Weight", 0.0, c1.getWeight(), 0.0);

		c1.addEngine(e1);
		Assert.assertEquals("Consist Engine 1 Length", 56+4, c1.getLength());
//		Assert.assertEquals("Consist Engine 1 Weight", 5000.0, c1.getWeight(), 0.0);

		c1.addEngine(e2);
		Assert.assertEquals("Consist Engine 2 Length", 56+4+59+4, c1.getLength());
//		Assert.assertEquals("Consist Engine 2 Weight", 11000.0, c1.getWeight(), 0.0);

		c1.addEngine(e3);
		Assert.assertEquals("Consist Engine 3 Length", 56+4+59+4+45+4, c1.getLength());
//		Assert.assertEquals("Consist Engine 3 Weight", 18000.0, c1.getWeight(), 0.0);

		c1.setLeadEngine(e2);
		Assert.assertTrue("Consist Lead Engine 1", c1.isLeadEngine(e2));
		Assert.assertFalse("Consist Lead Engine 2", c1.isLeadEngine(e1));
		Assert.assertFalse("Consist Lead Engine 3", c1.isLeadEngine(e3));

		c1.deleteEngine(e2);
		Assert.assertEquals("Kernel Engine Delete 2 Length", 56+4+45+4, c1.getLength());
//		Assert.assertEquals("Kernel Engine Delete 2 Weight", 12000.0, c1.getWeight(), 0.0);

		c1.deleteEngine(e1);
		Assert.assertEquals("Kernel Engine Delete 1 Length", 45+4, c1.getLength());
//		Assert.assertEquals("Kernel Engine Delete 1 Weight", 7000.0, c1.getWeight(), 0.0);

		c1.deleteEngine(e3);
		Assert.assertEquals("Kernel Engine Delete 3 Length", 0, c1.getLength());
//		Assert.assertEquals("Kernel Engine Delete 3 Weight", 0.0, c1.getWeight(), 0.0);

	}

	public void testEngineConsist() {
		Consist cold = new Consist("TESTCONSISTOLD");
		Assert.assertEquals("Consist Name old", "TESTCONSISTOLD", cold.getName());

		Consist cnew = new Consist("TESTCONSISTNEW");
		Assert.assertEquals("Consist Name new", "TESTCONSISTNEW", cnew.getName());

		Engine e1 = new Engine("TESTROAD", "TESTNUMBER1");
//		e1.setLength("56");
		e1.setModel("GP35");
		e1.setWeight("5000");
		Engine e2 = new Engine("TESTROAD", "TESTNUMBER2");
//		e2.setLength("59");
		e2.setModel("GP40");
		e2.setWeight("6000");
		Engine e3 = new Engine("TESTROAD", "TESTNUMBER3");
//		e3.setLength("45");
		e3.setModel("SW1500");
		e3.setWeight("7000");

		//  All three engines start out in the old consist with engine 1 as the lead engine.
		e1.setConsist(cold);
		e2.setConsist(cold);
		e3.setConsist(cold);
		Assert.assertEquals("Consist Name for engine 1 before", "TESTCONSISTOLD", e1.getConsistName());
		Assert.assertEquals("Consist Name for engine 2 before", "TESTCONSISTOLD", e2.getConsistName());
		Assert.assertEquals("Consist Name for engine 3 before", "TESTCONSISTOLD", e3.getConsistName());
		Assert.assertEquals("Consist old length before", 56+4+59+4+45+4, cold.getLength());
		Assert.assertEquals("Consist new length before", 0, cnew.getLength());
		Assert.assertTrue("Consist old Lead is Engine 1 before", cold.isLeadEngine(e1));
		Assert.assertFalse("Consist old Lead is not Engine 2 before", cold.isLeadEngine(e2));
		Assert.assertFalse("Consist old Lead is not Engine 3 before", cold.isLeadEngine(e3));
		Assert.assertFalse("Consist new Lead is not Engine 1 before", cnew.isLeadEngine(e1));
		Assert.assertFalse("Consist new Lead is not Engine 2 before", cnew.isLeadEngine(e2));
		Assert.assertFalse("Consist new Lead is not Engine 3 before", cnew.isLeadEngine(e3));

		//  Move engine 1 to the new consist where it will be the lead engine.
		//  Engine 2 should now be the lead engine of the old consist.
		e1.setConsist(cnew);
		Assert.assertEquals("Consist Name for engine 1 after", "TESTCONSISTNEW", e1.getConsistName());
		Assert.assertEquals("Consist Name for engine 2 after", "TESTCONSISTOLD", e2.getConsistName());
		Assert.assertEquals("Consist Name for engine 3 after", "TESTCONSISTOLD", e3.getConsistName());
		Assert.assertEquals("Consist old length after", 59+4+45+4, cold.getLength());
		Assert.assertEquals("Consist new length after", 56+4, cnew.getLength());
		Assert.assertFalse("Consist old Lead is not Engine 1 after", cold.isLeadEngine(e1));
		Assert.assertTrue("Consist old Lead is Engine 2 after", cold.isLeadEngine(e2));
		Assert.assertFalse("Consist old Lead is not Engine 3 after", cold.isLeadEngine(e3));
		Assert.assertTrue("Consist new Lead is Engine 1 after", cnew.isLeadEngine(e1));
		Assert.assertFalse("Consist new Lead is not Engine 2 after", cnew.isLeadEngine(e2));
		Assert.assertFalse("Consist new Lead is not Engine 3 after", cnew.isLeadEngine(e3));

		//  Move engine 3 to the new consist.
		e3.setConsist(cnew);
		Assert.assertEquals("Consist Name for engine 1 after3", "TESTCONSISTNEW", e1.getConsistName());
		Assert.assertEquals("Consist Name for engine 2 after3", "TESTCONSISTOLD", e2.getConsistName());
		Assert.assertEquals("Consist Name for engine 3 after3", "TESTCONSISTNEW", e3.getConsistName());
		Assert.assertEquals("Consist old length after3", 59+4, cold.getLength());
		Assert.assertEquals("Consist new length after3", 56+4+45+4, cnew.getLength());
		Assert.assertFalse("Consist old Lead is not Engine 1 after3", cold.isLeadEngine(e1));
		Assert.assertTrue("Consist old Lead is Engine 2 after3", cold.isLeadEngine(e2));
		Assert.assertFalse("Consist old Lead is not Engine 3 after3", cold.isLeadEngine(e3));
		Assert.assertTrue("Consist new Lead is Engine 1 after3", cnew.isLeadEngine(e1));
		Assert.assertFalse("Consist new Lead is not Engine 2 after3", cnew.isLeadEngine(e2));
		Assert.assertFalse("Consist new Lead is not Engine 3 after3", cnew.isLeadEngine(e3));
	}

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

	public OperationsEnginesTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {"-noloading", OperationsEnginesTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(OperationsEnginesTest.class);
		return suite;
	}

    // The minimal setup for log4J
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }
}
