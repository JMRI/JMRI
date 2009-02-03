// OperationsRollingStockTest.java

package jmri.jmrit.operations.rollingstock;

import jmri.InstanceManager;
import jmri.Sensor;
import jmri.SignalHead;
import jmri.Turnout;
import jmri.managers.InternalSensorManager;
import jmri.managers.InternalTurnoutManager;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the Operations RollingStock class
 * 
 * Just the beginning, ...
 * 
 * @author	Bob Coleman     Copyright 2009
 * 
 */
public class OperationsRollingStockTest extends TestCase {

	// test creation
	public void testCreate() {
		RollingStock rs1 = new RollingStock("TESTROAD", "TESTNUMBER1");
                
		rs1.setType("TESTTYPE");
		rs1.setLength("TESTLENGTH");
		rs1.setColor("TESTCOLOR");
		rs1.setWeight("TESTWEIGHT");
//		rs1.setWeightTons("TESTWEIGHTTONS");
		rs1.setBuilt("TESTBUILT");
		rs1.setOwner("TESTOWNER");
		rs1.setComment("TESTCOMMENT");
		rs1.setMoves(5);
/*                
		c1.setHazardous(true);
		c1.setFred(true);
		c1.setCaboose(true);
*/
		Assert.assertEquals("Car Road", "TESTROAD", rs1.getRoad());
		Assert.assertEquals("Car Number", "TESTNUMBER1", rs1.getNumber());
		Assert.assertEquals("Car ID", "TESTROAD"+"TESTNUMBER1", rs1.getId());
                
		Assert.assertEquals("Car Type", "TESTTYPE", rs1.getType());
                /* Also need to test location length */
		Assert.assertEquals("Car Length", "TESTLENGTH", rs1.getLength());
		Assert.assertEquals("Car Color", "TESTCOLOR", rs1.getColor());
		Assert.assertEquals("Car Weight", "TESTWEIGHT", rs1.getWeight());
//		Assert.assertEquals("Car WeightTons", "TESTWEIGHTTONS", rs1.getWeightTons());
		Assert.assertEquals("Car Built", "TESTBUILT", rs1.getBuilt());
		Assert.assertEquals("Car Owner", "TESTOWNER", rs1.getOwner());
		Assert.assertEquals("Car Comment", "TESTCOMMENT", rs1.getComment());
		Assert.assertEquals("Car Moves", 5, rs1.getMoves());
/*
                Assert.assertTrue("Car Hazardous", c1.isHazardous());
		Assert.assertTrue("Car Fred", c1.hasFred());
		Assert.assertTrue("Car Caboose", c1.isCaboose());
 */
	}

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
    @Override
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
        
        // create a new instance manager
        InstanceManager i = new InstanceManager(){
            @Override
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

	public OperationsRollingStockTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {"-noloading", OperationsRollingStockTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(OperationsRollingStockTest.class);
		return suite;
	}

    // The minimal setup for log4J
    @Override
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }
}
