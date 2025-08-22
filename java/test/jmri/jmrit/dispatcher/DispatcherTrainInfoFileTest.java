package jmri.jmrit.dispatcher;

import jmri.InstanceManager;
import jmri.jmrit.logix.WarrantPreferences;
import jmri.util.JUnitUtil;
import java.awt.GraphicsEnvironment;
import org.junit.Assume;

import org.junit.jupiter.api.*;

import java.io.File;

import org.junit.Assert;

/**
 * Swing tests for dispatcher train info.
 *
 * @author Dave Duchamp
 */
public class DispatcherTrainInfoFileTest {

    DispatcherFrame d;  // need dispatcher now
    
    @Test
    public void testFileRead() throws Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        TrainInfoFile tif = new TrainInfoFile();
        tif.setFileLocation("java/test/jmri/jmrit/dispatcher/traininfo/");
        TrainInfo ti = tif.readTrainInfo("TestTrain.xml");
        // test input information
        Assert.assertEquals("Transit Name", "Red Main Loop CW", ti.getTransitName());
        Assert.assertEquals("Transit Id", "IZ5", ti.getTransitId());
        Assert.assertEquals("Train Name", "GTW 6418", ti.getTrainName());
        Assert.assertEquals("DCC Address", " ", ti.getDccAddress());
        Assert.assertTrue("Train In Transit", ti.getTrainInTransit());
        Assert.assertEquals("Start Block Name", "Red Siding-1", ti.getStartBlockName());
        Assert.assertEquals("Start Block Id", "IB1", ti.getStartBlockId());
        Assert.assertEquals("Start Block Sequ", 1, ti.getStartBlockSeq());

        Assert.assertEquals("Destination Block Name", "Red Siding-7", ti.getDestinationBlockName());
        Assert.assertEquals("Destination Block Id", "IB1", ti.getDestinationBlockId());
        Assert.assertEquals("Destination Block Sequ", 7, ti.getDestinationBlockSeq());
        Assert.assertTrue("Train From Roster", ti.getTrainFromRoster());
        Assert.assertFalse("Train From Trains", ti.getTrainFromTrains());
        Assert.assertFalse("Train From User", ti.getTrainFromUser());
        Assert.assertEquals("Priority", 7, ti.getPriority());
        Assert.assertFalse("Run Auto", ti.getAutoRun());
        Assert.assertFalse("Reset When Done", ti.getResetWhenDone());
        Assert.assertEquals("Delayed Start", 1, ti.getDelayedStart());
        Assert.assertEquals("Departure Time Hours", 8, ti.getDepartureTimeHr());
        Assert.assertEquals("Departure Time Minutes", 10, ti.getDepartureTimeMin());
        Assert.assertEquals("Train Type", "THROUGH_FREIGHT", ti.getTrainType());

        Assert.assertEquals("Speed Factor", ti.getSpeedFactor(), 0.8f, 0.0);
        Assert.assertEquals("Maximum Speed", ti.getMaxSpeed(), 0.6f, 0.0);
        Assert.assertEquals("Ramp Rate", "RAMP_FAST", ti.getRampRate());
        Assert.assertEquals("Resistance Wheels", ActiveTrain.TrainDetection.TRAINDETECTION_WHOLETRAIN,ti.getTrainDetection());
        Assert.assertFalse("Run In Reverse", ti.getRunInReverse());
        Assert.assertFalse("Sound Decoder", ti.getSoundDecoder());
        Assert.assertEquals("Maximum Train Length", ti.getMaxTrainLengthScaleFeet(), 222.0f, 0.01);
        Assert.assertEquals("Light Function key", 0, ti.getFNumberLight());
        Assert.assertEquals("Bell Function key", 1, ti.getFNumberBell());
        Assert.assertEquals("Horn Function key", 2, ti.getFNumberHorn());

    }

    // Version 2
    @Test
    public void testFileRead_V2() throws Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        TrainInfoFile tif = new TrainInfoFile();
        tif.setFileLocation("java/test/jmri/jmrit/dispatcher/traininfo/");
        TrainInfo ti = tif.readTrainInfo("TestTrainCW_V2.xml");
        // test input information
        Assert.assertEquals("Version is 2",ti.getVersion(),2);
        Assert.assertEquals("Transit Name", "SouthPlatform CW", ti.getTransitName());
        Assert.assertEquals("transitid", "IZ1", ti.getTransitId());
        Assert.assertEquals("Train Name", "1000", ti.getTrainName());
        Assert.assertEquals("DCC Address", "1000", ti.getDccAddress());
        Assert.assertTrue("Train In Transit", ti.getTrainInTransit());
        Assert.assertEquals("Start Block Name", "South Platform-1", ti.getStartBlockName());
        Assert.assertEquals("Start Block Id", "IB:AUTO:0003", ti.getStartBlockId());
        Assert.assertEquals("Start Block Sequ", 1, ti.getStartBlockSeq());
        Assert.assertEquals("Destination Block Name", "South Platform-5", ti.getDestinationBlockName());
        Assert.assertEquals("Destination Block Id", "IB:AUTO:0003", ti.getDestinationBlockId());
        Assert.assertEquals("Destination Block Sequ", 5, ti.getDestinationBlockSeq());
        Assert.assertFalse("Train From Roster", ti.getTrainFromRoster());
        Assert.assertFalse("Train From Trains", ti.getTrainFromTrains());
        Assert.assertTrue("Train From User", ti.getTrainFromUser());
        Assert.assertEquals("Priority", 5, ti.getPriority());
        Assert.assertTrue("Run Auto", ti.getAutoRun());
        Assert.assertFalse("Reset When Done", ti.getResetWhenDone());
        Assert.assertEquals("Delayed Start", 0, ti.getDelayedStart());
        Assert.assertEquals("Start Sensor", null, ti.getDelaySensorName());
        Assert.assertEquals("Departure Time Hours", 8, ti.getDepartureTimeHr());
        Assert.assertEquals("Departure Time Minutes", 0, ti.getDepartureTimeMin());
        Assert.assertEquals("Train Type", "LOCAL_PASSENGER", ti.getTrainType());
        Assert.assertEquals("Speed Factor", ti.getSpeedFactor(), 1.0f, 0.0);
        Assert.assertEquals("Maximum Speed", ti.getMaxSpeed(), 0.6f, 0.0);
        Assert.assertEquals("Ramp Rate", "None", ti.getRampRate());
        Assert.assertEquals("Resistance Wheels", ActiveTrain.TrainDetection.TRAINDETECTION_WHOLETRAIN,ti.getTrainDetection());
        Assert.assertFalse("Run In Reverse", ti.getRunInReverse());
        Assert.assertTrue("Sound Decoder", ti.getSoundDecoder());
        Assert.assertEquals("Maximum Train Length", ti.getMaxTrainLengthScaleFeet(), 200.0f, 0.0);
        Assert.assertEquals("Allocation Method", ti.getAllocationMethod(),3,0);
        Assert.assertFalse("Use Speed Profile", ti.getUseSpeedProfile());
        Assert.assertEquals("Use Speed Profile Adjust block length", ti.getStopBySpeedProfileAdjust(),1.0f,0.0f);
    }

    // Version 3
    @Test
    public void testFileRead_V3_withReverse() throws Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        TrainInfoFile tif = new TrainInfoFile();
        tif.setFileLocation("java/test/jmri/jmrit/dispatcher/traininfo/");
        TrainInfo ti = tif.readTrainInfo("FWDREV40.xml");
        // test input information
        Assert.assertEquals("Version is 3",ti.getVersion(),3);
        Assert.assertEquals("Transit Name", "StopFWDandREV", ti.getTransitName());
        Assert.assertEquals("transitid", "StopFWDandREV", ti.getTransitId()); // since version 3 same
        Assert.assertEquals("Train Name", "AAA", ti.getTrainName());
        Assert.assertEquals("DCC Address", "3", ti.getDccAddress());
        Assert.assertTrue("Train In Transit", ti.getTrainInTransit());
        Assert.assertEquals("Start Block Name", "Block1-1", ti.getStartBlockName());
        Assert.assertEquals("Start Block Id", "Block1", ti.getStartBlockId());
        Assert.assertEquals("Start Block Sequ", 1, ti.getStartBlockSeq());
        Assert.assertEquals("Destination Block Name", "Block7", ti.getDestinationBlockName());
        Assert.assertEquals("Destination Block Id", "Block7", ti.getDestinationBlockId());
        Assert.assertEquals("Destination Block Sequ", 3, ti.getDestinationBlockSeq());
        Assert.assertFalse("Train From Roster", ti.getTrainFromRoster());
        Assert.assertFalse("Train From Trains", ti.getTrainFromTrains());
        Assert.assertTrue("Train From User", ti.getTrainFromUser());
        Assert.assertEquals("Priority", 5, ti.getPriority());
        Assert.assertTrue("Run Auto", ti.getAutoRun());
        Assert.assertTrue("Reset When Done", ti.getResetWhenDone());
        Assert.assertTrue("Reset restart sensor", ti.getResetRestartSensor());
        Assert.assertEquals("Restart sensor", "TrainRestart", ti.getRestartSensorName());
        Assert.assertEquals("Reverse At End Sensor", "TrainRestart", ti.getReverseRestartSensorName()); // not if file should have defaulted to restart value.
        Assert.assertTrue("Reset Reverse Reverse At End sensor", ti.getReverseResetRestartSensor());
        Assert.assertEquals("Delayed Start", 0, ti.getDelayedStart());
        Assert.assertEquals("Start Sensor", null, ti.getDelaySensorName());
        Assert.assertEquals("Departure Time Hours", 8, ti.getDepartureTimeHr());
        Assert.assertEquals("Departure Time Minutes", 0, ti.getDepartureTimeMin());
        Assert.assertEquals("Train Type", "LOCAL_PASSENGER", ti.getTrainType());
        Assert.assertEquals("Speed Factor", ti.getSpeedFactor(), 1.0f, 0.0);
        Assert.assertEquals("Maximum Speed", ti.getMaxSpeed(), 1.0f, 0.0);
        Assert.assertEquals("Ramp Rate", "None", ti.getRampRate());
        Assert.assertEquals("Resistance Wheels", ActiveTrain.TrainDetection.TRAINDETECTION_WHOLETRAIN,ti.getTrainDetection());
        Assert.assertFalse("Run In Reverse", ti.getRunInReverse());
        Assert.assertFalse("Sound Decoder", ti.getSoundDecoder());
        Assert.assertEquals("Maximum Train Length", ti.getMaxTrainLengthScaleFeet(), 40.0f, 0.0);
        Assert.assertEquals("Allocation Method", ti.getAllocationMethod(),-1,0);
        Assert.assertFalse("Use Speed Profile", ti.getUseSpeedProfile());
        Assert.assertEquals("Use Speed Profile Adjust block length", ti.getStopBySpeedProfileAdjust(),1.0f,0.0f);
    }

    // Version 4
    @Test
    public void testFileRead_V4_withReverse() throws Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        TrainInfoFile tif = new TrainInfoFile();
        tif.setFileLocation("java/test/jmri/jmrit/dispatcher/traininfo/");
        TrainInfo ti = tif.readTrainInfo("FWDREV120.xml");
        // test input information
        Assert.assertEquals("Version is 4",ti.getVersion(),4);
        Assert.assertEquals("Transit Name", "StopFWDandREV", ti.getTransitName());
        Assert.assertEquals("transitid", "StopFWDandREV", ti.getTransitId()); // since version 3 same
        Assert.assertEquals("Train Name", "AAA", ti.getTrainName());
        Assert.assertEquals("DCC Address", "3", ti.getDccAddress());
        Assert.assertTrue("Train In Transit", ti.getTrainInTransit());
        Assert.assertEquals("Start Block Name", "Block3-1", ti.getStartBlockName());
        Assert.assertEquals("Start Block Sequ", 1, ti.getStartBlockSeq());
        Assert.assertEquals("Destination Block Name", "Block7", ti.getDestinationBlockName());
        Assert.assertEquals("Destination Block Id", "Block7", ti.getDestinationBlockId()); // since 3 both the same
        Assert.assertEquals("Destination Block Sequ", 3, ti.getDestinationBlockSeq());
        Assert.assertFalse("Train From Roster", ti.getTrainFromRoster());
        Assert.assertFalse("Train From Trains", ti.getTrainFromTrains());
        Assert.assertTrue("Train From User", ti.getTrainFromUser());
        Assert.assertEquals("Priority", 5, ti.getPriority());
        Assert.assertTrue("Run Auto", ti.getAutoRun());
        Assert.assertTrue("Reset When Done", ti.getResetWhenDone());
        Assert.assertEquals("Restart sensor", "TrainRestart", ti.getRestartSensorName());
        Assert.assertEquals("Reverse At End Sensor", "TrainReverseRestart", ti.getReverseRestartSensorName());
        Assert.assertFalse("Reset Reverse Reverse At End sensor", ti.getReverseResetRestartSensor());
        Assert.assertEquals("Delayed Start", 0, ti.getDelayedStart());
        Assert.assertEquals("Start Sensor", null, ti.getDelaySensorName());
        Assert.assertEquals("Departure Time Hours", 8, ti.getDepartureTimeHr());
        Assert.assertEquals("Departure Time Minutes", 0, ti.getDepartureTimeMin());
        Assert.assertEquals("Train Type", "LOCAL_PASSENGER", ti.getTrainType());
        Assert.assertEquals("Speed Factor", ti.getSpeedFactor(), 1.0f, 0.0);
        Assert.assertEquals("Maximum Speed", ti.getMaxSpeed(), 1.0f, 0.0);
        Assert.assertEquals("Ramp Rate", "None", ti.getRampRate());
        Assert.assertEquals("Resistance Wheels", ActiveTrain.TrainDetection.TRAINDETECTION_WHOLETRAIN,ti.getTrainDetection());
        Assert.assertFalse("Run In Reverse", ti.getRunInReverse());
        Assert.assertFalse("Sound Decoder", ti.getSoundDecoder());
        Assert.assertEquals("Maximum Train Length", 120.0f, ti.getMaxTrainLengthScaleFeet(), 0.0);
        Assert.assertEquals("Allocation Method", ti.getAllocationMethod(),-1,0);
        Assert.assertFalse("Use Speed Profile", ti.getUseSpeedProfile());
        Assert.assertEquals("Use Speed Profile Adjust block length", ti.getStopBySpeedProfileAdjust(),1.0f,0.0f);
    }

    // Version 5
    @Test
    public void testFileRead_V5() throws Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        TrainInfoFile tif = new TrainInfoFile();
        tif.setFileLocation("java/test/jmri/jmrit/dispatcher/traininfo/");
        TrainInfo ti = tif.readTrainInfo("TestInfoV5-WholeTrain.xml");
        // test input information
        Assert.assertEquals("Version is 5",ti.getVersion(),5);
        Assert.assertEquals("Transit Name", "0912M-0405-0912M-C", ti.getTransitName());
        Assert.assertEquals("transitid", "0912M-0405-0912M-C", ti.getTransitId()); // since version 3 same
        Assert.assertEquals("Train Name", " ", ti.getTrainName());
        Assert.assertEquals("DCC Address", " ", ti.getDccAddress());
        Assert.assertTrue("Train In Transit", ti.getTrainInTransit());
        Assert.assertEquals("Start Block Name", "B-09-12-M-1", ti.getStartBlockName());
        Assert.assertEquals("Start Block Name", "B-09-12-M", ti.getStartBlockId());
        Assert.assertEquals("Start Block Sequ", 1, ti.getStartBlockSeq());
        Assert.assertEquals("Destination Block Name", "B-09-12-M-13", ti.getDestinationBlockName());
        Assert.assertEquals("Destination Block Id", "B-09-12-M", ti.getDestinationBlockId()); // since 3 both the same
        Assert.assertEquals("Destination Block Sequ", 13, ti.getDestinationBlockSeq());
        Assert.assertFalse("Train From Roster", ti.getTrainFromRoster());
        Assert.assertFalse("Train From Trains", ti.getTrainFromTrains());
        Assert.assertFalse("Train From User", ti.getTrainFromUser());
        Assert.assertTrue("Train From SetLater",ti.getTrainFromSetLater());
        Assert.assertEquals("Priority", 5, ti.getPriority());
        Assert.assertTrue("Run Auto", ti.getAutoRun());
        Assert.assertTrue("Reset When Done", ti.getResetWhenDone());
        Assert.assertEquals("Restart sensor", null, ti.getRestartSensorName());
        Assert.assertEquals("Reverse At End Sensor", null, ti.getReverseRestartSensorName());
        Assert.assertTrue("Reset Reverse Reverse At End sensor", ti.getReverseResetRestartSensor());
        Assert.assertEquals("Delayed Start", 0, ti.getDelayedStart());
        Assert.assertEquals("Start Sensor", null, ti.getDelaySensorName());
        Assert.assertEquals("Departure Time Hours", 8, ti.getDepartureTimeHr());
        Assert.assertEquals("Departure Time Minutes", 0, ti.getDepartureTimeMin());
        Assert.assertEquals("Train Type", "LOCAL_FREIGHT", ti.getTrainType());
        Assert.assertEquals("Speed Factor", 1.0f, ti.getSpeedFactor(), 0.0);
        Assert.assertEquals("Maximum Speed", 1.0f, ti.getMaxSpeed(), 0.0);
        Assert.assertEquals("Ramp Rate", "Speed Profile", ti.getRampRate());
        Assert.assertEquals("TrainDetection", ActiveTrain.TrainDetection.TRAINDETECTION_HEADANDTAIL,ti.getTrainDetection());
        Assert.assertFalse("Run In Reverse", ti.getRunInReverse());
        Assert.assertTrue("Sound Decoder", ti.getSoundDecoder());
        Assert.assertEquals("Maximum Train Length",  80.0f, ti.getMaxTrainLengthScaleFeet(), 0.0);
        Assert.assertEquals("Allocation Method", 0, ti.getAllocationMethod());
        Assert.assertTrue("Use Speed Profile", ti.getUseSpeedProfile());
        Assert.assertEquals("Use Speed Profile Adjust block length", 0.5f, ti.getStopBySpeedProfileAdjust(), 0.0f);
        Assert.assertEquals("Light Function Key", 4, ti.getFNumberLight());
        Assert.assertEquals("Bell Function Key", 5, ti.getFNumberBell());
        Assert.assertEquals("Horn Function Key", 6, ti.getFNumberHorn());
    }

    @BeforeEach
    public void setUp() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JUnitUtil.setUp();
        jmri.configurexml.ConfigXmlManager cm = new jmri.configurexml.ConfigXmlManager() {
        };
        WarrantPreferences.getDefault().setShutdown(WarrantPreferences.Shutdown.NO_MERGE);
        File f = new File("java/test/jmri/jmrit/dispatcher/MultiBlockStop.xml");

        Assertions.assertDoesNotThrow(() -> {
            cm.load(f);
        });

        InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager.class)
                .initializeLayoutBlockPaths();

        // load dispatcher, with all the correct options
        OptionsFile.setDefaultFileName("java/test/jmri/jmrit/dispatcher/TestTrainDispatcherOptions.xml");
        d = InstanceManager.getDefault(DispatcherFrame.class);

    }

    @AfterEach
    public void tearDown() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JUnitUtil.dispose(d);
        InstanceManager.getDefault(jmri.SignalMastManager.class).dispose();
        InstanceManager.getDefault(jmri.SignalMastLogicManager.class).dispose();
        JUnitUtil.clearShutDownManager();
        JUnitUtil.resetWindows(false,false);
        JUnitUtil.resetFileUtilSupport();
        JUnitUtil.tearDown();
    }
}
