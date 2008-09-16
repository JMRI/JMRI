// OperationsSetupTest.java

package jmri.jmrit.operations.setup;

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
 * Tests for the OperationsSetup class
 * @author	Bob Coleman
 * @version $Revision: 1.1 $
 */
public class OperationsSetupTest extends TestCase {

	// test creation
	public void testCreate() {
		Setup s = new Setup();
		s.setRailroadName("Test Railroad Name");
		Assert.assertEquals("Railroad Name", "Test Railroad Name", s.getRailroadName());
		s.setOwnerName("Test Owner Name");
		Assert.assertEquals("Owner Name", "Test Owner Name", s.getOwnerName());
	}

	
	// test scale attributes
	public void testScaleAttributes() {
		Setup s = new Setup();
		// Not really necessary
		s.setRailroadName("Test Railroad Name");
		Assert.assertEquals("Railroad Name", "Test Railroad Name", s.getRailroadName());
		s.setOwnerName("Test Owner Name");
		Assert.assertEquals("Owner Name", "Test Owner Name", s.getOwnerName());

		s.setScale(s.N_SCALE);
		Assert.assertEquals("Scale", 2, s.getScale());
		Assert.assertEquals("Scale Ratio", 160, s.getScaleRatio());
		Assert.assertEquals("Initial Weight", 500, s.getInitalWeight());
		Assert.assertEquals("Added Weight", 150, s.getAddWeight());
	}
	
	// test train attributes
	public void testTrainAttributes() {
		Setup s = new Setup();
		// Not really necessary
		s.setRailroadName("Test Railroad Name");
		Assert.assertEquals("Railroad Name", "Test Railroad Name", s.getRailroadName());
		s.setOwnerName("Test Owner Name");
		Assert.assertEquals("Owner Name", "Test Owner Name", s.getOwnerName());

		s.setTrainDirection(s.EAST);
		Assert.assertEquals("Direction East", 1, s.getTrainDirection());
		s.setTrainDirection(s.WEST);
		Assert.assertEquals("Direction West", 2, s.getTrainDirection());
		s.setTrainDirection(s.NORTH);
		Assert.assertEquals("Direction North", 4, s.getTrainDirection());
		s.setTrainDirection(s.SOUTH);
		Assert.assertEquals("Direction South", 8, s.getTrainDirection());

		s.setTrainLength(520);
		Assert.assertEquals("Train Length", 520, s.getTrainLength());

		s.setEngineSize(120);
		Assert.assertEquals("Engine Size", 120, s.getEngineSize());

		s.setCarMoves(12);
		Assert.assertEquals("Car Moves", 12, s.getCarMoves());
	}
	
	// test panel attributes
	public void testPanelAttributes() {
		Setup s = new Setup();
		// Not really necessary
		s.setRailroadName("Test Railroad Name");
		Assert.assertEquals("Railroad Name", "Test Railroad Name", s.getRailroadName());
		s.setOwnerName("Test Owner Name");
		Assert.assertEquals("Owner Name", "Test Owner Name", s.getOwnerName());

		s.setPanelName("Test Panel Name");
		Assert.assertEquals("Panel Name", "Test Panel Name", s.getPanelName());

		s.setTrainIconCordEnabled(true);
		Assert.assertEquals("Train Icon Cord Enabled True", true, s.isTrainIconCordEnabled());

		s.setTrainIconCordEnabled(false);
		Assert.assertEquals("Train Icon Cord Enabled False", false, s.isTrainIconCordEnabled());

		s.setTrainIconAppendEnabled(true);
		Assert.assertEquals("Train Icon Append Enabled True", true, s.isTrainIconAppendEnabled());

		s.setTrainIconAppendEnabled(false);
		Assert.assertEquals("Train Icon Append Enabled False", false, s.isTrainIconAppendEnabled());

		s.setTrainIconColorNorth("Red");
		Assert.assertEquals("Train Icon Color North", "Red", s.getTrainIconColorNorth());

		s.setTrainIconColorSouth("Blue");
		Assert.assertEquals("Train Icon Color South", "Blue", s.getTrainIconColorSouth());

		s.setTrainIconColorEast("Green");
		Assert.assertEquals("Train Icon Color East", "Green", s.getTrainIconColorEast());

		s.setTrainIconColorWest("Brown");
		Assert.assertEquals("Train Icon Color West", "Brown", s.getTrainIconColorWest());

		s.setTrainIconColorLocal("White");
		Assert.assertEquals("Train Icon Color Local", "White", s.getTrainIconColorLocal());

		s.setTrainIconColorTerminate("Black");
		Assert.assertEquals("Train Icon Color Terminate", "Black", s.getTrainIconColorTerminate());
	}

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

	public OperationsSetupTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {"-noloading", OperationsSetupTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(OperationsSetupTest.class);
		return suite;
	}

    // The minimal setup for log4J
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }
}
