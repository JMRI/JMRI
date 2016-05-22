// DispatcherTrainInfoFileTest.java

package jmri.jmrit.dispatcher;

import junit.framework.*;

/**
 * Swing jfcUnit tests for dispatcher train info
 * @author			Dave Duchamp
 * @version         $Revision$
 */
public class DispatcherTrainInfoFileTest extends TestCase {

	public void testFileRead() throws Exception {

		TrainInfoFile tif = new TrainInfoFile();
		tif.setFileLocation("java/test/jmri/jmrit/dispatcher/");
		TrainInfo ti = tif.readTrainInfo("TestTrain.xml");
		// test input information
		Assert.assertEquals("Transit Name",ti.getTransitName(),"IZ5( Red Main Loop CW )");
		Assert.assertEquals("Train Name",ti.getTrainName(),"GTW 6418");
		Assert.assertEquals("DCC Address",ti.getDCCAddress()," ");
		Assert.assertTrue("Train In Transit",ti.getTrainInTransit());
		Assert.assertEquals("Start Block Name",ti.getStartBlockName(),"IB1( Red Siding )-1");
		Assert.assertEquals("Destination Block Name",ti.getDestinationBlockName(),"IB1( Red Siding )-7");
		Assert.assertTrue("Train From Roster",ti.getTrainFromRoster());
		Assert.assertFalse("Train From Trains",ti.getTrainFromTrains());
		Assert.assertFalse("Train From User",ti.getTrainFromUser());
		Assert.assertEquals("Priority",ti.getPriority(),"7");
		Assert.assertTrue("Run Auto",ti.getRunAuto());
		Assert.assertFalse("Reset When Done",ti.getResetWhenDone());
		Assert.assertEquals("Delayed Start",ti.getDelayedStart(),1);
		Assert.assertEquals("Departure Time Hours",ti.getDepartureTimeHr(),"08");
		Assert.assertEquals("Departure Time Minutes",ti.getDepartureTimeMin(),"10");
		Assert.assertEquals("Train Type",ti.getTrainType(),"THROUGH_FREIGHT");

		Assert.assertEquals("Speed Factor",ti.getSpeedFactor(),"0.8");
		Assert.assertEquals("Maximum Speed",ti.getMaxSpeed(),"0.6");
		Assert.assertEquals("Ramp Rate",ti.getRampRate(),"RAMP_FAST");
		Assert.assertTrue("Resistance Wheels",ti.getResistanceWheels());
		Assert.assertFalse("Run In Reverse",ti.getRunInReverse());
		Assert.assertFalse("Sound Decoder",ti.getSoundDecoder());
		Assert.assertEquals("Maximum Train Length",ti.getMaxTrainLength(),"222.0");
	
	}
    
	// from here down is testing infrastructure
	public DispatcherTrainInfoFileTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {"-noloading", DispatcherTrainInfoFileTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(DispatcherTrainInfoFileTest.class);  
		return suite;
	}

    // The minimal setup for log4J
    protected void setUp() throws Exception { 
        super.setUp();
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
    }
    protected void tearDown() throws Exception { 
        apps.tests.Log4JFixture.tearDown();
        super.tearDown();
    }
}
