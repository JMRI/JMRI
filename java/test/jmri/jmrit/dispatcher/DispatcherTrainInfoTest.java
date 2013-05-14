// DispatcherTrainInfoTest.java

package jmri.jmrit.dispatcher;

import junit.framework.*;

/**
 * Swing jfcUnit tests for dispatcher train info
 * @author			Dave Duchamp
 * @version         $Revision$
 */
public class DispatcherTrainInfoTest extends TestCase {

	public void testAccessMethods() throws Exception {

		TrainInfo ti = new TrainInfo();
		// set up TrainInfo object
		ti.setTransitName("Transit");
		ti.setTrainName("Train");
		ti.setDCCAddress("346");
		ti.setTrainInTransit(true);
		ti.setStartBlockName("IB12");
		ti.setDestinationBlockName("IB22");
		ti.setTrainFromRoster(true);
		ti.setTrainFromTrains(false);
		ti.setTrainFromUser(false);
		ti.setPriority("8");
		ti.setRunAuto(true);
		ti.setResetWhenDone(false);
		ti.setDelayedStart(0x01);
		ti.setDepartureTimeHr("10");
		ti.setDepartureTimeMin("30");
		ti.setTrainType("2");
		
		ti.setSpeedFactor("0.8");
		ti.setMaxSpeed("0.6");
		ti.setRampRate("2");
		ti.setResistanceWheels(true);
		ti.setRunInReverse(false);
		ti.setSoundDecoder(true);
		ti.setMaxTrainLength("225");
		// test it
		Assert.assertEquals("Transit Name",ti.getTransitName(),"Transit");
		Assert.assertEquals("Train Name",ti.getTrainName(),"Train");
		Assert.assertEquals("DCC Address",ti.getDCCAddress(),"346");
		Assert.assertTrue("Train In Transit",ti.getTrainInTransit());
		Assert.assertEquals("Start Block Name",ti.getStartBlockName(),"IB12");
		Assert.assertEquals("Destination Block Name",ti.getDestinationBlockName(),"IB22");
		Assert.assertTrue("Train From Roster",ti.getTrainFromRoster());
		Assert.assertFalse("Train From Trains",ti.getTrainFromTrains());
		Assert.assertFalse("Train From User",ti.getTrainFromUser());
		Assert.assertEquals("Priority",ti.getPriority(),"8");
		Assert.assertTrue("Run Auto",ti.getRunAuto());
		Assert.assertFalse("Reset When Done",ti.getResetWhenDone());
		Assert.assertEquals("Delayed Start",ti.getDelayedStart(),"yes");
		Assert.assertEquals("Departure Time Hours",ti.getDepartureTimeHr(),"10");
		Assert.assertEquals("Departure Time Minutes",ti.getDepartureTimeMin(),"30");
		Assert.assertEquals("Train Type",ti.getTrainType(),"2");

		Assert.assertEquals("Speed Factor",ti.getSpeedFactor(),"0.8");
		Assert.assertEquals("Maximum Speed",ti.getMaxSpeed(),"0.6");
		Assert.assertEquals("Ramp Rate",ti.getRampRate(),"2");
		Assert.assertTrue("Resistance Wheels",ti.getResistanceWheels());
		Assert.assertFalse("Run In Reverse",ti.getRunInReverse());
		Assert.assertTrue("Sound Decoder",ti.getSoundDecoder());		
		Assert.assertEquals("Maximum Train Length",ti.getMaxTrainLength(),"225");
	
	}
    
	// from here down is testing infrastructure
	public DispatcherTrainInfoTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {"-noloading", DispatcherTrainInfoTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(DispatcherTrainInfoTest.class);  
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
