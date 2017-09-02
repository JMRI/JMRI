package jmri.jmrit.dispatcher;

import jmri.util.JUnitUtil;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.Assert;

/**
 * Swing jfcUnit tests for dispatcher train info
 *
 * @author	Dave Duchamp
 */
public class DispatcherTrainInfoFileTest extends TestCase {

    public void testFileRead() throws Exception {

        TrainInfoFile tif = new TrainInfoFile();
        tif.setFileLocation("java/test/jmri/jmrit/dispatcher/");
        TrainInfo ti = tif.readTrainInfo("TestTrain.xml");
        // test input information
        Assert.assertEquals("Transit Name", ti.getTransitName(), "IZ5( Red Main Loop CW )");
        Assert.assertEquals("Transit Id", ti.getTransitId(),"IZ5");
        Assert.assertEquals("Train Name", ti.getTrainName(), "GTW 6418");
        Assert.assertEquals("DCC Address", ti.getDCCAddress(), " ");
        Assert.assertTrue("Train In Transit", ti.getTrainInTransit());
        Assert.assertEquals("Start Block Name", ti.getStartBlockName(), "IB1( Red Siding )-1");
        Assert.assertEquals("Start Block Id", ti.getStartBlockId(), "IB1");
        Assert.assertEquals("Start Block Sequ", ti.getStartBlockSeq(), 1);
        
        Assert.assertEquals("Destination Block Name", ti.getDestinationBlockName(), "IB1( Red Siding )-7");
        Assert.assertEquals("Destination Block Id", ti.getDestinationBlockId(), "IB1");
        Assert.assertEquals("Destination Block Sequ", ti.getDestinationBlockSeq(), 7);
        Assert.assertTrue("Train From Roster", ti.getTrainFromRoster());
        Assert.assertFalse("Train From Trains", ti.getTrainFromTrains());
        Assert.assertFalse("Train From User", ti.getTrainFromUser());
        Assert.assertEquals("Priority", ti.getPriority(), 7);
        Assert.assertTrue("Run Auto", ti.getAutoRun());
        Assert.assertFalse("Reset When Done", ti.getResetWhenDone());
        Assert.assertEquals("Delayed Start", ti.getDelayedStart(), 1);
        Assert.assertEquals("Departure Time Hours", ti.getDepartureTimeHr(), 8);
        Assert.assertEquals("Departure Time Minutes", ti.getDepartureTimeMin(), 10);
        Assert.assertEquals("Train Type", ti.getTrainType(), "THROUGH_FREIGHT");

        Assert.assertEquals("Speed Factor", ti.getSpeedFactor(), 0.8f, 0.0);
        Assert.assertEquals("Maximum Speed", ti.getMaxSpeed(), 0.6f, 0.0);
        Assert.assertEquals("Ramp Rate", ti.getRampRate(), "RAMP_FAST");
        Assert.assertTrue("Resistance Wheels", ti.getResistanceWheels());
        Assert.assertFalse("Run In Reverse", ti.getRunInReverse());
        Assert.assertFalse("Sound Decoder", ti.getSoundDecoder());
        Assert.assertEquals("Maximum Train Length", ti.getMaxTrainLength(), 222.0f, 0.0);

    }

    // Version 2
    public void testFileRead_V2() throws Exception {

        TrainInfoFile tif = new TrainInfoFile();
        tif.setFileLocation("java/test/jmri/jmrit/dispatcher/");
        TrainInfo ti = tif.readTrainInfo("TestTrain2.xml");
        // test input information
        Assert.assertEquals("Transit Name", ti.getTransitName(), "IZ42(11WEST-2122)");
        Assert.assertEquals("transitid", ti.getTransitId(),"IZ42");
        Assert.assertEquals("Train Name", ti.getTrainName(), "CN-RDC-250");
        Assert.assertEquals("DCC Address", ti.getDCCAddress(), " ");
        Assert.assertTrue("Train In Transit", ti.getTrainInTransit());
        Assert.assertEquals("Start Block Name", ti.getStartBlockName(), "IB20(B-11-WEST)-1");
        Assert.assertEquals("Start Block Id", ti.getStartBlockId(), "IB20");
        Assert.assertEquals("Start Block Sequ", ti.getStartBlockSeq(), 1);
        Assert.assertEquals("Destination Block Name", ti.getDestinationBlockName(), "IB:AUTO:0002(B-21-22)");
        Assert.assertEquals("Destination Block Id", ti.getDestinationBlockId(), "IB:AUTO:0002");
        Assert.assertEquals("Destination Block Sequ", ti.getDestinationBlockSeq(), 10);
        Assert.assertTrue("Train From Roster", ti.getTrainFromRoster());
        Assert.assertFalse("Train From Trains", ti.getTrainFromTrains());
        Assert.assertFalse("Train From User", ti.getTrainFromUser());
        Assert.assertEquals("Priority", ti.getPriority(), 5);
        Assert.assertTrue("Run Auto", ti.getAutoRun());
        Assert.assertTrue("Reset When Done", ti.getResetWhenDone());
        Assert.assertEquals("Delayed Start", ti.getDelayedStart(), 2);
        Assert.assertEquals("Start Sensor", ti.getDelaySensorName(),"1-StartTrain1");
        Assert.assertEquals("Departure Time Hours", ti.getDepartureTimeHr(), 8);
        Assert.assertEquals("Departure Time Minutes", ti.getDepartureTimeMin(), 0);
        Assert.assertEquals("Train Type", ti.getTrainType(), "EXPRESS_PASSENGER");
        Assert.assertEquals("Speed Factor", ti.getSpeedFactor(), 1.0f, 0.0);
        Assert.assertEquals("Maximum Speed", ti.getMaxSpeed(), 1.0f, 0.0);
        Assert.assertEquals("Ramp Rate", ti.getRampRate(), "RAMP_NONE");
        Assert.assertTrue("Resistance Wheels", ti.getResistanceWheels());
        Assert.assertFalse("Run In Reverse", ti.getRunInReverse());
        Assert.assertTrue("Sound Decoder", ti.getSoundDecoder());
        Assert.assertEquals("Maximum Train Length", ti.getMaxTrainLength(), 120.0f, 0.0);

    }

    // from here down is testing infrastructure
    public DispatcherTrainInfoFileTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", DispatcherTrainInfoFileTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(DispatcherTrainInfoFileTest.class);
        return suite;
    }

    // The minimal setup for log4J
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        JUnitUtil.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        apps.tests.Log4JFixture.tearDown();
        super.tearDown();
    }
}
